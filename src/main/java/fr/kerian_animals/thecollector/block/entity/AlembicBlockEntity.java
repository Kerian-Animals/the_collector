package fr.kerian_animals.thecollector.block.entity;

import fr.kerian_animals.thecollector.block.AlembicBlock;
import fr.kerian_animals.thecollector.menu.AlembicMenu;
import fr.kerian_animals.thecollector.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * Block entity backing the alembic processing station.
 *
 * <p>The implementation mirrors part of the vanilla brewing stand contract so the menu and screen
 * can reuse familiar progress semantics while still running custom recipes and fuel rules.</p>
 */
public final class AlembicBlockEntity extends BlockEntity implements MenuProvider, Container {
    public static final int BOTTLE_SLOT_LEFT = 0;
    public static final int BOTTLE_SLOT_CENTER = 1;
    public static final int BOTTLE_SLOT_RIGHT = 2;
    public static final int REAGENT_SLOT = 3;
    public static final int FUEL_SLOT = 4;

    private final ItemStackHandler items = new ItemStackHandler(5) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case BOTTLE_SLOT_LEFT, BOTTLE_SLOT_CENTER, BOTTLE_SLOT_RIGHT ->
                        fr.kerian_animals.thecollector.item.AlembicRecipes.isValidInput(stack)
                                && items.getStackInSlot(slot).isEmpty();
                case REAGENT_SLOT -> fr.kerian_animals.thecollector.item.AlembicRecipes.isValidReagent(stack);
                case FUEL_SLOT -> fr.kerian_animals.thecollector.item.AlembicRecipes.isValidFuel(stack);
                default -> super.isItemValid(slot, stack);
            };
        }

        @Override
        protected void onContentsChanged(int slot) {
            updateBottleState();
            setChanged();
        }
    };

    private final ContainerData data = new SimpleContainerData(2);
    private int progress;
    private int maxProgress;
    private int fuelCharges;

    public AlembicBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.ALEMBIC.get(), pos, blockState);
    }

    /**
     * Server tick for recipe resolution, fuel consumption, crafting, and visual feedback.
     */
    public static void tick(Level level, BlockPos pos, BlockState state, AlembicBlockEntity alembic) {
        if (level.isClientSide) {
            return;
        }

        fr.kerian_animals.thecollector.item.AlembicRecipes.AlembicRecipe recipe = alembic.findActiveRecipe();
        if (recipe == null) {
            alembic.progress = 0;
            alembic.maxProgress = 0;
            alembic.syncData();
            return;
        }

        if (alembic.fuelCharges <= 0) {
            ItemStack fuelStack = alembic.items.getStackInSlot(FUEL_SLOT);
            if (!fuelStack.isEmpty()) {
                alembic.items.extractItem(FUEL_SLOT, 1, false);
                alembic.fuelCharges = 20;
                alembic.setChanged();
            } else {
                alembic.progress = 0;
                alembic.maxProgress = 0;
                alembic.syncData();
                return;
            }
        }

        if (alembic.maxProgress != recipe.processTicks()) {
            alembic.maxProgress = recipe.processTicks();
        }

        alembic.progress++;
        if (alembic.progress >= alembic.maxProgress) {
            alembic.progress = 0;
            alembic.craft(recipe);
        }

        alembic.emitActivity(level, pos);
        alembic.syncData();
        setChanged(level, pos, state);
    }

    /**
     * Resolves the active recipe from the bottle slots using the current reagent and fuel state.
     */
    private fr.kerian_animals.thecollector.item.AlembicRecipes.AlembicRecipe findActiveRecipe() {
        ItemStack reagent = items.getStackInSlot(REAGENT_SLOT);
        ItemStack fuel = items.getStackInSlot(FUEL_SLOT);
        if (reagent.isEmpty() || (fuel.isEmpty() && fuelCharges <= 0)) {
            return null;
        }

        fr.kerian_animals.thecollector.item.AlembicRecipes.AlembicRecipe left = fr.kerian_animals.thecollector.item.AlembicRecipes.find(
                items.getStackInSlot(BOTTLE_SLOT_LEFT), reagent, fuel
        );
        if (left != null) {
            return left;
        }

        fr.kerian_animals.thecollector.item.AlembicRecipes.AlembicRecipe center = fr.kerian_animals.thecollector.item.AlembicRecipes.find(
                items.getStackInSlot(BOTTLE_SLOT_CENTER), reagent, fuel
        );
        if (center != null) {
            return center;
        }

        return fr.kerian_animals.thecollector.item.AlembicRecipes.find(
                items.getStackInSlot(BOTTLE_SLOT_RIGHT), reagent, fuel
        );
    }

    /**
     * Consumes inputs for the current recipe and transforms every compatible bottle slot.
     */
    private void craft(fr.kerian_animals.thecollector.item.AlembicRecipes.AlembicRecipe recipe) {
        items.extractItem(REAGENT_SLOT, 1, false);
        if (fuelCharges > 0) {
            fuelCharges--;
        }
        transformBottleSlot(BOTTLE_SLOT_LEFT, recipe);
        transformBottleSlot(BOTTLE_SLOT_CENTER, recipe);
        transformBottleSlot(BOTTLE_SLOT_RIGHT, recipe);
    }

    private void transformBottleSlot(int slot, fr.kerian_animals.thecollector.item.AlembicRecipes.AlembicRecipe recipe) {
        ItemStack input = items.getStackInSlot(slot);
        if (fr.kerian_animals.thecollector.item.AlembicRecipes.find(input, items.getStackInSlot(REAGENT_SLOT), items.getStackInSlot(FUEL_SLOT)) != null
                || input.is(recipe.input())) {
            items.setStackInSlot(slot, recipe.output().getDefaultInstance());
        }
    }

    /**
     * Updates the exposed menu data so the client can render brewing-style progress and fuel.
     */
    private void syncData() {
        int remaining = maxProgress > 0 ? Math.max(0, maxProgress - progress) : 0;
        int vanillaRemaining = 0;
        if (remaining > 0 && maxProgress > 0) {
            vanillaRemaining = Math.max(1, remaining * 400 / maxProgress);
        }
        data.set(0, vanillaRemaining);
        data.set(1, fuelCharges);
    }

    /**
     * Emits low-frequency particles and sounds while a recipe is in progress.
     */
    private void emitActivity(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel) || progress <= 0) {
            return;
        }

        if (serverLevel.getGameTime() % 6L == 0L) {
            serverLevel.sendParticles(
                    ParticleTypes.SOUL,
                    pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D,
                    2,
                    0.15D, 0.12D, 0.15D,
                    0.0D
            );
            serverLevel.sendParticles(
                    ParticleTypes.SMOKE,
                    pos.getX() + 0.5D, pos.getY() + 0.95D, pos.getZ() + 0.5D,
                    3,
                    0.18D, 0.1D, 0.18D,
                    0.002D
            );
        }

        if (serverLevel.getGameTime() % 40L == 0L) {
            serverLevel.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.25F, 1.3F);
        }
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public ContainerData getData() {
        syncData();
        return data;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.the_collector.alembic");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new AlembicMenu(containerId, inventory, this, getData());
    }

    /**
     * Mirrors inventory occupancy into the blockstate to drive bottle rendering on the block
     * model.
     */
    private void updateBottleState() {
        if (level == null) {
            return;
        }

        BlockState state = getBlockState();
        if (!(state.getBlock() instanceof AlembicBlock)) {
            return;
        }

        BlockState updatedState = state
                .setValue(AlembicBlock.HAS_BOTTLE_0, !items.getStackInSlot(BOTTLE_SLOT_LEFT).isEmpty())
                .setValue(AlembicBlock.HAS_BOTTLE_1, !items.getStackInSlot(BOTTLE_SLOT_CENTER).isEmpty())
                .setValue(AlembicBlock.HAS_BOTTLE_2, !items.getStackInSlot(BOTTLE_SLOT_RIGHT).isEmpty());
        if (updatedState != state) {
            level.setBlock(worldPosition, updatedState, 2);
        }
    }

    @Override
    public int getContainerSize() {
        return items.getSlots();
    }

    @Override
    public boolean isEmpty() {
        for (int slot = 0; slot < items.getSlots(); slot++) {
            if (!items.getStackInSlot(slot).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.getStackInSlot(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack extracted = items.extractItem(slot, amount, false);
        if (!extracted.isEmpty()) {
            setChanged();
        }
        return extracted;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = items.getStackInSlot(slot);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        items.setStackInSlot(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.setStackInSlot(slot, stack);
    }

    @Override
    public boolean stillValid(Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this) {
            return false;
        }
        return player.distanceToSqr(
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D
        ) <= 64.0D;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return items.isItemValid(slot, stack);
    }

    @Override
    public void clearContent() {
        for (int slot = 0; slot < items.getSlots(); slot++) {
            items.setStackInSlot(slot, ItemStack.EMPTY);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", items.serializeNBT(registries));
        tag.putInt("Progress", progress);
        tag.putInt("MaxProgress", maxProgress);
        tag.putInt("FuelCharges", fuelCharges);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.deserializeNBT(registries, tag.getCompound("Inventory"));
        if (items.getSlots() != 5) {
            ItemStack[] migrated = new ItemStack[Math.min(items.getSlots(), 5)];
            for (int i = 0; i < migrated.length; i++) {
                migrated[i] = items.getStackInSlot(i).copy();
            }
            items.setSize(5);
            for (int i = 0; i < migrated.length; i++) {
                items.setStackInSlot(i, migrated[i]);
            }
        }
        progress = tag.getInt("Progress");
        maxProgress = tag.getInt("MaxProgress");
        fuelCharges = tag.getInt("FuelCharges");
        updateBottleState();
        syncData();
    }
}
