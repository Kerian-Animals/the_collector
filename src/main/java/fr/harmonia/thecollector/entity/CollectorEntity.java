package fr.harmonia.thecollector.entity;

import fr.harmonia.thecollector.config.TheCollectorConfig;
import fr.harmonia.thecollector.entity.goal.CollectorCollectItemGoal;
import fr.harmonia.thecollector.entity.goal.CollectorEscapeGoal;
import fr.harmonia.thecollector.entity.goal.CollectorScoutGoal;
import fr.harmonia.thecollector.entity.state.CollectorState;
import fr.harmonia.thecollector.stash.CollectorStashManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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

public class CollectorEntity extends PathfinderMob {
    private static final String TAG_STATE = "CollectorState";
    private static final String TAG_STOLEN_ITEMS = "StolenItems";
    private static final String TAG_STOLEN_STACKS = "StolenStacks";
    private static final String TAG_AGE = "AgeTicks";

    private CollectorState state = CollectorState.IDLE;
    private @Nullable ItemEntity currentTarget;
    private final SimpleContainer stolenInventory = new SimpleContainer(54);
    private int stolenStacks = 0;
    private int ageTicks = 0;
    private int escapeTicks = 0;

    public CollectorEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
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
        this.goalSelector.addGoal(2, new CollectorCollectItemGoal(this));
        this.goalSelector.addGoal(3, new CollectorScoutGoal(this));
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

        int slot = firstEmptySlot();
        if (slot < 0) {
            return false;
        }

        this.stolenInventory.setItem(slot, stack);
        this.stolenStacks++;
        this.playSound(SoundEvents.ITEM_PICKUP, 0.3F, 0.9F + this.random.nextFloat() * 0.2F);
        itemEntity.discard();

        if (!hasInventoryCapacity()) {
            setEscaping("inventory_full");
        }
        return true;
    }

    public List<ItemStack> getStolenItems() {
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < this.stolenInventory.getContainerSize(); i++) {
            ItemStack stack = this.stolenInventory.getItem(i);
            if (!stack.isEmpty()) {
                stacks.add(stack.copy());
            }
        }
        return stacks;
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

        ListTag listTag = new ListTag();
        int maxSlots = Math.min(TheCollectorConfig.COLLECTOR_INVENTORY_SLOTS.get(), this.stolenInventory.getContainerSize());
        for (int i = 0; i < maxSlots; i++) {
            ItemStack stack = this.stolenInventory.getItem(i);
            if (!stack.isEmpty()) {
                listTag.add(stack.save(this.registryAccess()));
            }
        }
        tag.put(TAG_STOLEN_ITEMS, listTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains(TAG_STATE)) {
            this.state = CollectorState.valueOf(tag.getString(TAG_STATE));
        }
        this.stolenStacks = tag.getInt(TAG_STOLEN_STACKS);
        this.ageTicks = tag.getInt(TAG_AGE);

        for (int i = 0; i < this.stolenInventory.getContainerSize(); i++) {
            this.stolenInventory.setItem(i, ItemStack.EMPTY);
        }

        ListTag listTag = tag.getList(TAG_STOLEN_ITEMS, Tag.TAG_COMPOUND);
        int maxSlots = Math.min(listTag.size(), this.stolenInventory.getContainerSize());
        for (int i = 0; i < maxSlots; i++) {
            this.stolenInventory.setItem(i, ItemStack.parse(this.registryAccess(), listTag.getCompound(i)).orElse(ItemStack.EMPTY));
        }
    }
}
