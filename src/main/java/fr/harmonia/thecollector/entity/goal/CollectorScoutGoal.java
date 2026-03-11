package fr.harmonia.thecollector.entity.goal;

import fr.harmonia.thecollector.config.TheCollectorConfig;
import fr.harmonia.thecollector.entity.CollectorEntity;
import fr.harmonia.thecollector.entity.state.CollectorState;
import fr.harmonia.thecollector.loot.ItemValueHelper;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;

import java.util.Comparator;
import java.util.List;

public class CollectorScoutGoal extends Goal {
    private final CollectorEntity collector;
    private int cooldown = 0;

    public CollectorScoutGoal(CollectorEntity collector) {
        this.collector = collector;
    }

    @Override
    public boolean canUse() {
        if (collector.getCollectorState() == CollectorState.ESCAPING || collector.getCollectorState() == CollectorState.DESPAWNING) {
            return false;
        }
        if (!collector.hasInventoryCapacity()) {
            collector.setEscaping("inventory_limit");
            return false;
        }
        if (cooldown-- > 0) {
            return false;
        }

        int radius = TheCollectorConfig.ITEM_SEARCH_RADIUS.get();
        List<ItemEntity> nearbyItems = collector.level()
                .getEntitiesOfClass(ItemEntity.class, collector.getBoundingBox().inflate(radius), e -> !e.hasPickUpDelay());

        ItemEntity best = nearbyItems.stream()
                .filter(item -> ItemValueHelper.isInteresting(item.getItem()))
                .max(Comparator.comparingDouble(item -> score(item, collector)))
                .orElse(null);

        if (best == null) {
            collector.setCollectorState(CollectorState.IDLE);
            cooldown = 40;
            return false;
        }

        collector.setCurrentTarget(best);
        collector.setCollectorState(CollectorState.SCOUTING);
        return true;
    }

    @Override
    public void start() {
        ItemEntity target = collector.getCurrentTarget();
        if (target != null) {
            collector.getNavigation().moveTo(target, 1.0D);
        }
    }

    @Override
    public void stop() {
        if (collector.getCollectorState() == CollectorState.SCOUTING) {
            collector.setCollectorState(CollectorState.IDLE);
        }
    }

    private static double score(ItemEntity item, CollectorEntity collector) {
        int value = ItemValueHelper.score(item.getItem());
        double distance = item.distanceToSqr(collector);
        return (value * item.getItem().getCount()) / Math.max(1.0D, distance);
    }
}
