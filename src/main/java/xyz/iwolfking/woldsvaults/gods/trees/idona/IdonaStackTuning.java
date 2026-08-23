package xyz.iwolfking.woldsvaults.gods.trees.idona;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

/** Super Stacker and Stack Hoarder. Both apply after every additive stack bonus is folded in. */
public final class IdonaStackTuning {
    private IdonaStackTuning() {
    }

    /** The effective max stack count for a talent whose additive bonuses are already folded in. */
    public static int maxStacks(ServerPlayer player, int resolvedMax) {
        if (resolvedMax <= 0) {
            return resolvedMax;
        }
        int points = IdonaNodes.points(player, IdonaNodes.SUPER_STACKER);
        if (points <= 0) {
            return resolvedMax;
        }
        float multiplier = IdonaNodeHandlers.params(IdonaNodes.SUPER_STACKER,
                IdonaNodeHandlers.SuperStackerParams.class).multiplier();
        double scaled = resolvedMax * Math.pow(multiplier, points);
        return (int) Math.min(Integer.MAX_VALUE, Math.floor(scaled));
    }

    /** The effective stack duration in ticks, after the base effect-duration adjustment. */
    public static int stackDuration(LivingEntity entity, int ticks) {
        if (ticks <= 0 || !(entity instanceof ServerPlayer player)) {
            return ticks;
        }
        int points = IdonaNodes.points(player, IdonaNodes.STACK_HOARDER);
        if (points <= 0) {
            return ticks;
        }
        float multiplier = IdonaNodeHandlers.params(IdonaNodes.STACK_HOARDER,
                IdonaNodeHandlers.StackHoarderParams.class).multiplier();
        double scaled = ticks * Math.pow(multiplier, points);
        return (int) Math.min(Integer.MAX_VALUE, Math.floor(scaled));
    }
}
