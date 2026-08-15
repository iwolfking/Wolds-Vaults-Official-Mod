package xyz.iwolfking.woldsvaults.gods.trees.idona;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

/**
 * Super Stacker and Stack Hoarder. Both nodes change numbers owned by the stacking-talent classes,
 * of which there are three independent implementations with no shared cap or duration helper, so
 * every one of them calls into here.
 *
 * <p>The multiplier is applied <em>after</em> the additive bonuses (Stack Master, the Pyramid
 * Scheme etching, Stack Stack Stack), because those are the flat grants a build already has and
 * multiplying the finished cap is what "1.25x max stacks" reads as.
 */
public final class IdonaStackTuning {
    private IdonaStackTuning() {
    }

    /** The effective max stack count for a talent whose additive bonuses are already folded in. */
    public static int maxStacks(ServerPlayer player, int resolvedMax) {
        if (resolvedMax <= 0) {
            return resolvedMax;
        }
        int points = IdonaNodes.minorPoints(player, IdonaNodes.SUPER_STACKER);
        if (points <= 0) {
            return resolvedMax;
        }
        double scaled = resolvedMax * Math.pow(IdonaNodes.SUPER_STACKER_MULTIPLIER, points);
        return (int) Math.min(Integer.MAX_VALUE, Math.floor(scaled));
    }

    /** The effective stack duration in ticks, after the base effect-duration adjustment. */
    public static int stackDuration(LivingEntity entity, int ticks) {
        if (ticks <= 0 || !(entity instanceof ServerPlayer player)) {
            return ticks;
        }
        int points = IdonaNodes.minorPoints(player, IdonaNodes.STACK_HOARDER);
        if (points <= 0) {
            return ticks;
        }
        double scaled = ticks * Math.pow(IdonaNodes.STACK_HOARDER_MULTIPLIER, points);
        return (int) Math.min(Integer.MAX_VALUE, Math.floor(scaled));
    }
}
