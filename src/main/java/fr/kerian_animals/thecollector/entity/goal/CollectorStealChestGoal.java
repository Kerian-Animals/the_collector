package fr.kerian_animals.thecollector.entity.goal;

import fr.kerian_animals.thecollector.config.TheCollectorConfig;
import fr.kerian_animals.thecollector.entity.CollectorEntity;
import fr.kerian_animals.thecollector.entity.state.CollectorState;
import fr.kerian_animals.thecollector.loot.ItemValueHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.jetbrains.annotations.Nullable;

public class CollectorStealChestGoal extends Goal {
    private final CollectorEntity collector;
    private @Nullable BlockPos targetChestPos;
    private int stealsFromCurrentChest = 0;
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
        stealsFromCurrentChest = 0;
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

        int bestSlot = findBestSlot(chest);
        if (bestSlot < 0) {
            targetChestPos = null;
            return;
        }

        ItemStack inChest = chest.getItem(bestSlot);
        if (inChest.isEmpty()) {
            return;
        }

        ItemStack stolen = chest.removeItem(bestSlot, inChest.getCount());
        if (stolen.isEmpty()) {
            return;
        }
        if (!collector.storeStolenStack(stolen)) {
            chest.setItem(bestSlot, stolen);
            collector.setEscaping("inventory_limit");
            return;
        }

        chest.setChanged();
        stealsFromCurrentChest++;
        if (stealsFromCurrentChest >= TheCollectorConfig.MAX_STEALS_PER_CHEST.get()) {
            targetChestPos = null;
            stealsFromCurrentChest = 0;
        }
    }

    @Override
    public void stop() {
        targetChestPos = null;
        stealsFromCurrentChest = 0;
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
        return findBestSlot(chest) >= 0;
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
}
