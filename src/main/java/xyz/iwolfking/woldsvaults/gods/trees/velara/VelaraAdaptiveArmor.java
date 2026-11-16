package xyz.iwolfking.woldsvaults.gods.trees.velara;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import xyz.iwolfking.woldsvaults.gods.GodNodeState;

import java.util.UUID;

/**
 * Adaptive Armor's consecutive-hit stacks, keyed on the attacker. Each hit is reduced by the stacks
 * earlier hits built, then adds one. A different attacker restarts the run; damage with no entity
 * behind it clears it.
 */
public final class VelaraAdaptiveArmor {
    private VelaraAdaptiveArmor() {
    }

    /** Returns the multiplier for this hit from the stacks already built, then advances the run. */
    public static float advanceAndGetMultiplier(ServerPlayer defender, DamageSource source) {
        Entity attacker = source.getEntity();
        if (attacker == null) {
            GodNodeState.clear(defender.getUUID(), VelaraNodes.ADAPTIVE_ARMOR);
            return 1.0F;
        }
        UUID attackerId = attacker.getUUID();
        Run run = GodNodeState.get(defender.getUUID(), VelaraNodes.ADAPTIVE_ARMOR, Run::new);
        synchronized (run) {
            if (!attackerId.equals(run.attackerId)) {
                run.attackerId = attackerId;
                run.stacks = 0;
            }
            float multiplier = 1.0F - VelaraValues.adaptiveArmorPerStack() * run.stacks;
            run.stacks = Math.min(run.stacks + 1, VelaraValues.adaptiveArmorMaxStacks());
            return multiplier;
        }
    }

    private static final class Run {
        private UUID attackerId;
        private int stacks;
    }
}
