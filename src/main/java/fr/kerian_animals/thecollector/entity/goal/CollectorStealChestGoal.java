package fr.kerian_animals.thecollector.entity.goal;

import fr.kerian_animals.thecollector.config.TheCollectorConfig;
import fr.kerian_animals.thecollector.entity.CollectorEntity;
import fr.kerian_animals.thecollector.entity.state.CollectorState;
import fr.kerian_animals.thecollector.loot.ItemValueHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CollectorStealChestGoal extends Goal {
    private final CollectorEntity collector;
    private @Nullable BlockPos targetChestPos;
    private int scanCooldown = 0;
    private int stealCooldown = 0;

    public CollectorStealChestGoal(CollectorEntity collector) {
        this.collector = collector;
    }

    @Override
    public boolean canUse() {
        if (!TheCollectorConfig.CHEST_THEFT_ENABLED.get()) {
            return false;
        }
        if (collector.getCollectorState() == CollectorState.ESCAPING || collector.getCollectorState() == CollectorState.DESPAWNING) {
            return false;
        }
        if (!collector.hasInventoryCapacity() || collector.getCurrentTarget() != null) {
            return false;
        }

        if (scanCooldown-- > 0) {
            return targetChestPos != null && isValidTargetChest(targetChestPos);
        }

        targetChestPos = findBestChest();
        scanCooldown = 20;
        return targetChestPos != null;
    }

    @Override
    public boolean canContinueToUse() {
        return targetChestPos != null
                && collector.hasInventoryCapacity()
                && collector.getCollectorState() != CollectorState.ESCAPING
                && collector.getCollectorState() != CollectorState.DESPAWNING
                && isValidTargetChest(targetChestPos);
    }

    @Override
    public void start() {
        collector.setCollectorState(CollectorState.COLLECTING);
    }

    @Override
    public void tick() {
        if (targetChestPos == null) {
            return;
        }

        collector.getNavigation().moveTo(targetChestPos.getX() + 0.5D, targetChestPos.getY(), targetChestPos.getZ() + 0.5D, 1.10D);
        if (collector.blockPosition().distSqr(targetChestPos) > 6.25D) {
            return;
        }
        if (stealCooldown-- > 0) {
            return;
        }
        stealCooldown = 12;

        BlockEntity blockEntity = collector.level().getBlockEntity(targetChestPos);
        if (!(blockEntity instanceof ChestBlockEntity chest)) {
            targetChestPos = null;
            return;
        }

        stealWholeChest(chest, targetChestPos);
        targetChestPos = null;
        collector.setEscaping("chest_raided");
    }

    @Override
    public void stop() {
        targetChestPos = null;
        if (collector.getCollectorState() == CollectorState.COLLECTING) {
            collector.setCollectorState(CollectorState.SCOUTING);
        }
    }

    private @Nullable BlockPos findBestChest() {
        int radius = TheCollectorConfig.CHEST_SEARCH_RADIUS.get();
        BlockPos origin = collector.blockPosition();

        BlockPos bestPos = null;
        double bestScore = 0.0D;

        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -2, -radius), origin.offset(radius, 2, radius))) {
            BlockEntity blockEntity = collector.level().getBlockEntity(pos);
            if (!(blockEntity instanceof ChestBlockEntity chest)) {
                continue;
            }
            int slot = findBestSlot(chest);
            if (slot < 0) {
                continue;
            }

            ItemStack stack = chest.getItem(slot);
            double value = ItemValueHelper.score(stack) * stack.getCount();
            if (value <= 0.0D) {
                continue;
            }

            double dist = origin.distSqr(pos);
            double score = value / Math.max(1.0D, dist);
            if (score > bestScore) {
                bestScore = score;
                bestPos = pos.immutable();
            }
        }
        return bestPos;
    }

    private boolean isValidTargetChest(BlockPos pos) {
        BlockEntity blockEntity = collector.level().getBlockEntity(pos);
        if (!(blockEntity instanceof ChestBlockEntity chest)) {
            return false;
        }
        return hasInterestingLoot(chest);
    }

    private static int findBestSlot(Container container) {
        int bestSlot = -1;
        int bestValue = 0;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            int score = ItemValueHelper.isInteresting(stack) ? ItemValueHelper.score(stack) : 0;
            if (score > bestValue) {
                bestValue = score;
                bestSlot = i;
            }
        }
        return bestValue > 0 ? bestSlot : -1;
    }

    private static boolean hasInterestingLoot(Container container) {
        return findBestSlot(container) >= 0;
    }

    private void stealWholeChest(ChestBlockEntity chest, BlockPos pos) {
        List<ItemStack> toSteal = collectStacksForTheft(chest, pos);
        collector.recordTheftAt(pos);

        // The Collector steals the physical chest block too.
        toSteal.add(new ItemStack(Items.CHEST));

        for (ItemStack stack : toSteal) {
            collector.storeStolenStackGuaranteed(stack);
        }

        chest.clearContent();
        chest.setChanged();
        collector.level().removeBlock(pos, false);
    }

    private List<ItemStack> collectStacksForTheft(ChestBlockEntity chest, BlockPos pos) {
        List<Integer> nonEmptySlots = new ArrayList<>();
        for (int i = 0; i < chest.getContainerSize(); i++) {
            if (!chest.getItem(i).isEmpty()) {
                nonEmptySlots.add(i);
            }
        }

        BlockState state = collector.level().getBlockState(pos);
        ChestType chestType = state.hasProperty(ChestBlock.TYPE) ? state.getValue(ChestBlock.TYPE) : ChestType.SINGLE;
        int targetCount = chestType == ChestType.SINGLE
                ? nonEmptySlots.size()
                : Math.max(1, (nonEmptySlots.size() + 1) / 2);

        RandomSource random = collector.getRandom();
        nonEmptySlots.sort(Comparator.comparingInt((Integer i) -> ItemValueHelper.score(chest.getItem(i))).reversed());

        // Keep valuable stacks prioritized, then randomize a bit for variety.
        if (chestType != ChestType.SINGLE && nonEmptySlots.size() > 2) {
            List<Integer> tail = new ArrayList<>(nonEmptySlots.subList(2, nonEmptySlots.size()));
            java.util.Collections.shuffle(tail, new java.util.Random(random.nextLong()));
            nonEmptySlots = new ArrayList<>(nonEmptySlots.subList(0, 2));
            nonEmptySlots.addAll(tail);
        }

        List<ItemStack> stolen = new ArrayList<>();
        int count = 0;
        for (int slot : nonEmptySlots) {
            if (count >= targetCount) {
                break;
            }
            ItemStack stack = chest.removeItem(slot, chest.getItem(slot).getCount());
            if (!stack.isEmpty()) {
                stolen.add(stack);
                count++;
            }
        }
        return stolen;
    }

}
