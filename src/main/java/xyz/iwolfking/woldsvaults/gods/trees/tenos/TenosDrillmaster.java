package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

/** Drillmaster: raises the Fortune cap for the holder. Server-side reads only. */
public final class TenosDrillmaster {
    private TenosDrillmaster() {
    }

    public static boolean hasRaisedFortuneCap(LivingEntity entity) {
        return entity instanceof ServerPlayer player && TenosNodes.isActive(player, TenosNodes.DRILLMASTER);
    }

    public static int raisedFortuneCap() {
        return TenosNodeHandlers.params(TenosNodes.DRILLMASTER,
                TenosNodeHandlers.DrillmasterParams.class).raised_fortune_cap();
    }
}
