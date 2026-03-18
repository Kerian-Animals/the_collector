package fr.kerian_animals.thecollector.menu;

import fr.kerian_animals.thecollector.block.entity.AlembicBlockEntity;
import fr.kerian_animals.thecollector.item.AlembicRecipes;
import fr.kerian_animals.thecollector.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class AlembicMenu extends AbstractContainerMenu {
    private static final int BOTTLE_SLOT_START = 0;
    private static final int BOTTLE_SLOT_END = 2;
    private static final int INGREDIENT_SLOT = 3;
    private static final int FUEL_SLOT = 4;
    private static final int SLOT_COUNT = 5;
    private static final int PLAYER_INV_START = 5;
    private static final int PLAYER_INV_END = 32;
    private static final int HOTBAR_START = 32;
    private static final int HOTBAR_END = 41;

    private final Container container;
    private final ContainerData brewingStandData;
    private final Slot ingredientSlot;

    public AlembicMenu(int containerId, Inventory inventory, FriendlyByteBuf extraData) {
        this(containerId, inventory, getBlockEntity(inventory, extraData.readBlockPos()), new SimpleContainerData(2));
    }

    public AlembicMenu(int containerId, Inventory inventory, Container container, ContainerData brewingStandData) {
        super(ModMenus.ALEMBIC.get(), containerId);
        checkContainerSize(container, SLOT_COUNT);
        checkContainerDataCount(brewingStandData, 2);
        this.container = container;
        this.brewingStandData = brewingStandData;

        addSlot(new BottleSlot(container, AlembicBlockEntity.BOTTLE_SLOT_LEFT, 56, 51));
        addSlot(new BottleSlot(container, AlembicBlockEntity.BOTTLE_SLOT_CENTER, 79, 58));
        addSlot(new BottleSlot(container, AlembicBlockEntity.BOTTLE_SLOT_RIGHT, 102, 51));
        this.ingredientSlot = addSlot(new IngredientSlot(container, INGREDIENT_SLOT, 79, 17));
        addSlot(new FuelSlot(container, FUEL_SLOT, 17, 17));

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }

        for (int slot = 0; slot < 9; slot++) {
            addSlot(new Slot(inventory, slot, 8 + slot * 18, 142));
        }

        addDataSlots(brewingStandData);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        result = stack.copy();
        if ((index >= BOTTLE_SLOT_START && index <= BOTTLE_SLOT_END) || index == INGREDIENT_SLOT || index == FUEL_SLOT) {
            if (!this.moveItemStackTo(stack, PLAYER_INV_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, result);
        } else if (FuelSlot.mayPlaceItem(stack)) {
            if (!this.moveItemStackTo(stack, FUEL_SLOT, SLOT_COUNT, false)
                    && (!this.ingredientSlot.mayPlace(stack) || !this.moveItemStackTo(stack, INGREDIENT_SLOT, FUEL_SLOT, false))) {
                return ItemStack.EMPTY;
            }
        } else if (this.ingredientSlot.mayPlace(stack)) {
            if (!this.moveItemStackTo(stack, INGREDIENT_SLOT, FUEL_SLOT, false)) {
                return ItemStack.EMPTY;
            }
        } else if (BottleSlot.mayPlaceItem(stack)) {
            if (!this.moveItemStackTo(stack, BOTTLE_SLOT_START, INGREDIENT_SLOT, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= PLAYER_INV_START && index < PLAYER_INV_END) {
            if (!this.moveItemStackTo(stack, HOTBAR_START, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= HOTBAR_START && index < HOTBAR_END) {
            if (!this.moveItemStackTo(stack, PLAYER_INV_START, PLAYER_INV_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, PLAYER_INV_START, HOTBAR_END, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == result.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return result;
    }

    public int getFuel() {
        return this.brewingStandData.get(1);
    }

    public int getBrewingTicks() {
        return this.brewingStandData.get(0);
    }

    private static AlembicBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        return (AlembicBlockEntity) inventory.player.level().getBlockEntity(pos);
    }

    private static final class FuelSlot extends Slot {
        private FuelSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return mayPlaceItem(stack);
        }

        private static boolean mayPlaceItem(ItemStack stack) {
            return AlembicRecipes.isValidFuel(stack);
        }
    }

    private static final class IngredientSlot extends Slot {
        private IngredientSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return AlembicRecipes.isValidReagent(stack);
        }
    }

    private static final class BottleSlot extends Slot {
        private BottleSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return mayPlaceItem(stack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        private static boolean mayPlaceItem(ItemStack stack) {
            return AlembicRecipes.isValidInput(stack);
        }
    }
}
