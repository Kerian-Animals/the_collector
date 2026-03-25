package fr.kerian_animals.thecollector.entity;

import fr.kerian_animals.thecollector.advancement.CollectorAdvancementHelper;
import fr.kerian_animals.thecollector.config.TheCollectorConfig;
import fr.kerian_animals.thecollector.entity.goal.CollectorCollectItemGoal;
import fr.kerian_animals.thecollector.entity.goal.CollectorEscapeGoal;
import fr.kerian_animals.thecollector.entity.goal.CollectorScoutGoal;
import fr.kerian_animals.thecollector.entity.goal.CollectorStealChestGoal;
import fr.kerian_animals.thecollector.entity.state.CollectorState;
import fr.kerian_animals.thecollector.stash.CollectorStashManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Core entity implementation for The Collector.
 *
 * <p>The entity alternates between scouting, stealing, escaping, and despawning. When it leaves
 * the world it converts its stolen inventory into a persistent stash.</p>
 */
public class CollectorEntity extends PathfinderMob {
    private static final String TAG_STATE = "CollectorState";
    private static final String TAG_STOLEN_ITEMS = "StolenItems";
    private static final String TAG_OVERFLOW_ITEMS = "OverflowItems";
    private static final String TAG_STOLEN_STACKS = "StolenStacks";
    private static final String TAG_AGE = "AgeTicks";
    private static final String TAG_DEBUG_FIXED = "DebugFixed";
    private static final String TAG_DEBUG_NO_DESPAWN = "DebugNoDespawn";
    private static final String TAG_LAST_THEFT_POS = "LastTheftPos";

    private CollectorState state = CollectorState.IDLE;
    private @Nullable ItemEntity currentTarget;
    private final SimpleContainer stolenInventory = new SimpleContainer(54);
    private final List<ItemStack> overflowStolenItems = new ArrayList<>();
    private int stolenStacks = 0;
    private int ageTicks = 0;
    private int escapeTicks = 0;
    private boolean debugFixed = false;
    private boolean debugNoDespawn = false;
    private @Nullable BlockPos lastTheftPos;

    public CollectorEntity(EntityType<? extends CollectorEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 5;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.33D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new CollectorEscapeGoal(this));
        this.goalSelector.addGoal(2, new CollectorStealChestGoal(this));
        this.goalSelector.addGoal(3, new CollectorCollectItemGoal(this));
        this.goalSelector.addGoal(4, new CollectorScoutGoal(this));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 0.95D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide || !TheCollectorConfig.MOD_ENABLED.get()) {
            return;
        }

        if (this.debugNoDespawn && this.state == CollectorState.DESPAWNING) {
            this.state = CollectorState.IDLE;
        }

        if (this.debugNoDespawn) {
            return;
        }

        this.ageTicks++;
        if (this.ageTicks > TheCollectorConfig.MAX_PRESENCE_TICKS.get() && this.state != CollectorState.DESPAWNING) {
            setEscaping("time_limit");
        }

        if (this.state == CollectorState.ESCAPING) {
            this.escapeTicks++;
            if (this.escapeTicks >= 120) {
                this.state = CollectorState.DESPAWNING;
            }
        }

        if (this.state == CollectorState.DESPAWNING) {
            if (this.level() instanceof ServerLevel serverLevel) {
                CollectorStashManager.createStashFromCollector(serverLevel, this);
            }
            this.discard();
            return;
        }

        Player nearestPlayer = this.level().getNearestPlayer(this, 4.0D);
        if (nearestPlayer != null && this.state != CollectorState.ESCAPING) {
            setEscaping("player_too_close");
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide) {
            setEscaping("attacked");
            randomShortTeleport();
        }
        return hurt;
    }

    public void setEscaping(String reason) {
        this.state = CollectorState.ESCAPING;
        this.escapeTicks = 0;
    }

    public CollectorState getCollectorState() {
        return state;
    }

    public void setCollectorState(CollectorState state) {
        this.state = state;
    }

    public @Nullable ItemEntity getCurrentTarget() {
        return currentTarget;
    }

    public void setCurrentTarget(@Nullable ItemEntity target) {
        this.currentTarget = target;
    }

    public boolean hasInventoryCapacity() {
        return this.stolenStacks < Math.min(TheCollectorConfig.MAX_STOLEN_STACKS.get(), TheCollectorConfig.COLLECTOR_INVENTORY_SLOTS.get());
    }

    public boolean steal(ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getItem().copy();
        if (stack.isEmpty() || !hasInventoryCapacity()) {
            return false;
        }

        if (!storeStolenStack(stack)) {
            return false;
        }

        recordTheftAt(itemEntity.blockPosition());
        itemEntity.discard();
        return true;
    }

    public boolean storeStolenStack(ItemStack stack) {
        if (stack.isEmpty() || !hasInventoryCapacity()) {
            return false;
        }

        int slot = firstEmptySlot();
        if (slot < 0) {
            return false;
        }

        this.stolenInventory.setItem(slot, stack.copy());
        this.stolenStacks++;
        this.playSound(SoundEvents.ITEM_PICKUP, 0.3F, 0.9F + this.random.nextFloat() * 0.2F);

        if (!hasInventoryCapacity()) {
            setEscaping("inventory_full");
        }
        return true;
    }

    public void storeStolenStackGuaranteed(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        if (!storeStolenStack(stack)) {
            this.overflowStolenItems.add(stack.copy());
        }
    }

    private void randomShortTeleport() {
        for (int i = 0; i < 8; i++) {
            double x = this.getX() + (this.random.nextDouble() - 0.5D) * 16.0D;
            double y = this.getY() + this.random.nextInt(5) - 2;
            double z = this.getZ() + (this.random.nextDouble() - 0.5D) * 16.0D;
            if (this.randomTeleport(x, y, z, true)) {
                this.playSound(SoundEvents.ENDERMAN_TELEPORT, 0.8F, 0.9F + this.random.nextFloat() * 0.2F);
                return;
            }
        }
    }

    public List<ItemStack> getStolenItems() {
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < this.stolenInventory.getContainerSize(); i++) {
            ItemStack stack = this.stolenInventory.getItem(i);
            if (!stack.isEmpty()) {
                stacks.add(stack.copy());
            }
        }
        for (ItemStack stack : this.overflowStolenItems) {
            if (!stack.isEmpty()) {
                stacks.add(stack.copy());
            }
        }
        return stacks;
    }

    public void setDebugFixed(boolean debugFixed) {
        this.debugFixed = debugFixed;
        this.setNoAi(debugFixed);
    }

    public void setDebugNoDespawn(boolean debugNoDespawn) {
        this.debugNoDespawn = debugNoDespawn;
    }

    public void recordTheftAt(BlockPos pos) {
        this.lastTheftPos = pos.immutable();
        if (this.level() instanceof ServerLevel serverLevel) {
            CollectorAdvancementHelper.awardNearby(serverLevel, pos, 24.0D, "something_is_missing");
        }
    }

    public @Nullable BlockPos getLastTheftPos() {
        return this.lastTheftPos;
    }

    private int firstEmptySlot() {
        int maxSlots = Math.min(TheCollectorConfig.COLLECTOR_INVENTORY_SLOTS.get(), this.stolenInventory.getContainerSize());
        for (int i = 0; i < maxSlots; i++) {
            if (this.stolenInventory.getItem(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean shouldDespawnInPeaceful() {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString(TAG_STATE, this.state.name());
        tag.putInt(TAG_STOLEN_STACKS, this.stolenStacks);
        tag.putInt(TAG_AGE, this.ageTicks);
        tag.putBoolean(TAG_DEBUG_FIXED, this.debugFixed);
        tag.putBoolean(TAG_DEBUG_NO_DESPAWN, this.debugNoDespawn);
        if (this.lastTheftPos != null) {
            tag.putLong(TAG_LAST_THEFT_POS, this.lastTheftPos.asLong());
        }

        ListTag listTag = new ListTag();
        int maxSlots = Math.min(TheCollectorConfig.COLLECTOR_INVENTORY_SLOTS.get(), this.stolenInventory.getContainerSize());
        for (int i = 0; i < maxSlots; i++) {
            ItemStack stack = this.stolenInventory.getItem(i);
            if (!stack.isEmpty()) {
                listTag.add(stack.save(this.registryAccess()));
            }
        }
        tag.put(TAG_STOLEN_ITEMS, listTag);

        ListTag overflowTag = new ListTag();
        for (ItemStack stack : this.overflowStolenItems) {
            if (!stack.isEmpty()) {
                overflowTag.add(stack.save(this.registryAccess()));
            }
        }
        tag.put(TAG_OVERFLOW_ITEMS, overflowTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains(TAG_STATE)) {
            this.state = CollectorState.valueOf(tag.getString(TAG_STATE));
        }
        this.stolenStacks = tag.getInt(TAG_STOLEN_STACKS);
        this.ageTicks = tag.getInt(TAG_AGE);
        this.debugFixed = tag.getBoolean(TAG_DEBUG_FIXED);
        this.debugNoDespawn = tag.getBoolean(TAG_DEBUG_NO_DESPAWN);
        this.lastTheftPos = tag.contains(TAG_LAST_THEFT_POS) ? BlockPos.of(tag.getLong(TAG_LAST_THEFT_POS)) : null;
        this.setNoAi(this.debugFixed);

        for (int i = 0; i < this.stolenInventory.getContainerSize(); i++) {
            this.stolenInventory.setItem(i, ItemStack.EMPTY);
        }
        this.overflowStolenItems.clear();

        ListTag listTag = tag.getList(TAG_STOLEN_ITEMS, Tag.TAG_COMPOUND);
        int maxSlots = Math.min(listTag.size(), this.stolenInventory.getContainerSize());
        for (int i = 0; i < maxSlots; i++) {
            this.stolenInventory.setItem(i, ItemStack.parse(this.registryAccess(), listTag.getCompound(i)).orElse(ItemStack.EMPTY));
        }

        ListTag overflowTag = tag.getList(TAG_OVERFLOW_ITEMS, Tag.TAG_COMPOUND);
        for (int i = 0; i < overflowTag.size(); i++) {
            ItemStack.parse(this.registryAccess(), overflowTag.getCompound(i)).ifPresent(this.overflowStolenItems::add);
        }
    }
}

