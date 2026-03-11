package fr.kerian_animals.thecollector.entity.goal;

import fr.kerian_animals.thecollector.config.TheCollectorConfig;
import fr.kerian_animals.thecollector.entity.CollectorEntity;
import fr.kerian_animals.thecollector.entity.state.CollectorState;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class CollectorEscapeGoal extends Goal {
    private final CollectorEntity collector;
    private int recalcTicks = 0;

    public CollectorEscapeGoal(CollectorEntity collector) {
        this.collector = collector;
    }

    @Override
    public boolean canUse() {
        return collector.getCollectorState() == CollectorState.ESCAPING;
    }

    @Override
    public boolean canContinueToUse() {
        return collector.getCollectorState() == CollectorState.ESCAPING;
    }

    @Override
    public void tick() {
        if (recalcTicks-- > 0) {
            return;
        }
        recalcTicks = 10;

        Player nearest = collector.level().getNearestPlayer(collector, 32.0D);
        if (nearest == null) {
            collector.getNavigation().moveTo(collector.getX() + collector.getRandom().nextInt(12) - 6,
                    collector.getY(),
                    collector.getZ() + collector.getRandom().nextInt(12) - 6,
                    1.45D);
            return;
        }

        Vec3 away = collector.position().subtract(nearest.position()).normalize();
        double distance = TheCollectorConfig.ESCAPE_DISTANCE.get();
        Vec3 target = collector.position().add(away.scale(distance));
        collector.getNavigation().moveTo(target.x, target.y, target.z, 1.55D);
    }
}

