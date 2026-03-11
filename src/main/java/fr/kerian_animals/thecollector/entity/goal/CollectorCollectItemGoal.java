package fr.kerian_animals.thecollector.entity.goal;

import fr.kerian_animals.thecollector.entity.CollectorEntity;
import fr.kerian_animals.thecollector.entity.state.CollectorState;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;

public class CollectorCollectItemGoal extends Goal {
    private final CollectorEntity collector;

    public CollectorCollectItemGoal(CollectorEntity collector) {
        this.collector = collector;
    }

    @Override
    public boolean canUse() {
        if (collector.getCollectorState() == CollectorState.ESCAPING || collector.getCollectorState() == CollectorState.DESPAWNING) {
            return false;
        }
        ItemEntity target = collector.getCurrentTarget();
        return target != null && target.isAlive() && collector.hasInventoryCapacity();
    }

    @Override
    public boolean canContinueToUse() {
        ItemEntity target = collector.getCurrentTarget();
        return target != null && target.isAlive() && collector.hasInventoryCapacity()
                && collector.getCollectorState() != CollectorState.ESCAPING
                && collector.getCollectorState() != CollectorState.DESPAWNING;
    }

    @Override
    public void start() {
        collector.setCollectorState(CollectorState.COLLECTING);
    }

    @Override
    public void tick() {
        ItemEntity target = collector.getCurrentTarget();
        if (target == null || !target.isAlive()) {
            collector.setCurrentTarget(null);
            collector.setCollectorState(CollectorState.SCOUTING);
            return;
        }

        collector.getNavigation().moveTo(target, 1.15D);
        if (collector.distanceToSqr(target) <= 2.0D) {
            collector.steal(target);
            collector.setCurrentTarget(null);
            collector.setCollectorState(CollectorState.SCOUTING);
        }
    }

    @Override
    public void stop() {
        if (collector.getCollectorState() == CollectorState.COLLECTING) {
            collector.setCollectorState(CollectorState.SCOUTING);
        }
    }
}

