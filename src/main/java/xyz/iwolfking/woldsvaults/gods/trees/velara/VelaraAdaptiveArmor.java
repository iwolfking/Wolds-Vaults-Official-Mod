package xyz.iwolfking.woldsvaults.gods.trees.velara;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import xyz.iwolfking.woldsvaults.gods.GodNodeState;

import java.util.UUID;

/**
 * Adaptive Armor's consecutive-hit stacks, keyed on the attacker. A different attacker restarts the
 * run at one; damage with no entity behind it clears it.
 */
public final class VelaraAdaptiveArmor {
    private VelaraAdaptiveArmor() {
    }

    /** Advances the run and returns the multiplier. Stacks apply to the hit that creates them. */
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
                run.stacks = 1;
            } else {
                run.stacks = Math.min(run.stacks + 1, VelaraValues.adaptiveArmorMaxStacks());
            }
            return 1.0F - VelaraValues.adaptiveArmorPerStack() * run.stacks;
        }
    }

    private static final class Run {
        private UUID attackerId;
        private int stacks;
    }
}
