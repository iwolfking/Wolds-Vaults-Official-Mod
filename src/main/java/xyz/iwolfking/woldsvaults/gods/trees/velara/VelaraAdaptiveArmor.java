package xyz.iwolfking.woldsvaults.gods.trees.velara;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import xyz.iwolfking.woldsvaults.gods.GodNodeState;

import java.util.UUID;

/**
 * Adaptive Armor's consecutive-hit stacks.
 *
 * <p>Stacks are per defender and keyed on the attacker's identity: a hit from the tracked attacker
 * raises the count, a hit from anything else restarts it at one, and damage with no entity behind
 * it (fall, void, the vault timer) clears the run entirely. State is transient by design - it is
 * meaningless outside the fight that built it - and lives in the shared
 * {@link GodNodeState} scratch, so logout and leaving a vault tear it down without this class
 * owning a map or a teardown path of its own.
 */
public final class VelaraAdaptiveArmor {
    private VelaraAdaptiveArmor() {
    }

    /**
     * Advances the run for this hit and returns the resulting damage multiplier. Stacks apply to
     * the hit that creates them, so six consecutive hits from one enemy reach the -60% floor.
     */
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
