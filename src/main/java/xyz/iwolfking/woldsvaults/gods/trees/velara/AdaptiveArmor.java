package xyz.iwolfking.woldsvaults.gods.trees.velara;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adaptive Armor's consecutive-hit stacks.
 *
 * <p>Stacks are per defender and keyed on the attacker's identity: a hit from the tracked attacker
 * raises the count, a hit from anything else restarts it at one, and damage with no entity behind
 * it (fall, void, the vault timer) clears the run entirely. State is transient by design -  it is
 * meaningless outside the fight that built it.
 */
public final class AdaptiveArmor {
    private static final Map<UUID, Run> RUNS = new ConcurrentHashMap<>();

    private AdaptiveArmor() {
    }

    /**
     * Advances the run for this hit and returns the resulting damage multiplier. Stacks apply to
     * the hit that creates them, so six consecutive hits from one enemy reach the -60% floor.
     */
    public static float advanceAndGetMultiplier(ServerPlayer defender, DamageSource source) {
        Entity attacker = source.getEntity();
        if (attacker == null) {
            RUNS.remove(defender.getUUID());
            return 1.0F;
        }
        UUID attackerId = attacker.getUUID();
        Run run = RUNS.compute(defender.getUUID(), (id, current) -> {
            if (current == null || !current.attackerId.equals(attackerId)) {
                return new Run(attackerId);
            }
            current.stacks = Math.min(current.stacks + 1, VelaraValues.ADAPTIVE_ARMOR_MAX_STACKS);
            return current;
        });
        return 1.0F - VelaraValues.ADAPTIVE_ARMOR_PER_STACK * run.stacks;
    }

    public static int getStacks(UUID defenderId) {
        Run run = RUNS.get(defenderId);
        return run == null ? 0 : run.stacks;
    }

    public static void clear(UUID defenderId) {
        RUNS.remove(defenderId);
    }

    private static final class Run {
        private final UUID attackerId;
        private int stacks;

        private Run(UUID attackerId) {
            this.attackerId = attackerId;
            this.stacks = 1;
        }
    }
}
