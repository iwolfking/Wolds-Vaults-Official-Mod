package xyz.iwolfking.woldsvaults.objectives.hyper;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import xyz.iwolfking.woldsvaults.objectives.HyperVaultObjective;

/**
 * Soft-caps the summed "+% Mob Spawns" a hyper vault feeds into an ispawner spawner.
 *
 * <p>Every spawner_mobs modifier writes its own entry into the spawner's AttemptModifiers map,
 * and iSpawner's SpawnerManager#spawn turns the sum into one burst:
 * {@code attempts = Attempts x (1 + sum)}. Nothing else bounds it — vault spawners are placed
 * with the MONSTER cap disabled (generic_settings palette, {@code Limit: -1}), and the
 * per-category gate is evaluated once before the burst rather than during it. A deep hyper
 * vault reaches a summed +4500% or more, which is a single spawner dropping hundreds of mobs
 * in one tick.
 *
 * <p>The curve is piecewise in PERCENT space: identity below the threshold, then
 * {@code c * sqrt(x - p) + q}. Both offsets are derived from the threshold T and coefficient c
 * rather than configured, which is what forces the join to be smooth:
 * requiring {@code f(T) = T} and {@code f'(T) = 1} gives {@code p = T - c^2/4} and
 * {@code q = T - c^2/2}. At the defaults (T = 2500, c = 15) that is
 * {@code 15 * sqrt(x - 2443.75) + 2387.5} — value and slope both continuous at 2500%, so there
 * is no step or kink where the cap engages. Configuring the offsets independently would let a
 * threshold change silently introduce a discontinuity.
 *
 * <p>The curve alone still lets a deep vault fire about 33x a spawner's base burst in one tick,
 * and vault mobs never despawn, so on top of it every burst is clamped to the headroom left
 * under a living-monster ceiling around the spawner ({@code spawnDensityCap} within
 * {@code spawnDensityRadius} blocks). A full area spawns nothing; the burst still spends one
 * of the spawner's uses.
 */
public final class HyperSpawnSoftCap {
    private HyperSpawnSoftCap() {
    }

    /**
     * True when this world is a vault running the hyper objective. Non-hyper vaults and the
     * overworld keep iSpawner's stock additive behaviour.
     */
    public static boolean appliesTo(Level world) {
        if (world == null) {
            return false;
        }
        return ServerVaults.get(world)
                .map(vault -> !vault.get(Vault.OBJECTIVES).getAll(HyperVaultObjective.class).isEmpty())
                .orElse(false);
    }

    /**
     * Maps a summed spawn increase (1.0 = +100%) through the soft cap. Values at or below the
     * threshold, and non-positive coefficients, pass through untouched.
     */
    public static double effectiveIncrease(double summedIncrease) {
        double threshold = HyperVaultObjective.cfg().getSpawnSoftCapThreshold();
        double coefficient = HyperVaultObjective.cfg().getSpawnSoftCapCoefficient();
        double percent = summedIncrease * 100.0;
        if (coefficient <= 0.0 || percent <= threshold) {
            return summedIncrease;
        }
        double innerOffset = threshold - coefficient * coefficient / 4.0;
        double outerOffset = threshold - coefficient * coefficient / 2.0;
        return (coefficient * Math.sqrt(percent - innerOffset) + outerOffset) / 100.0;
    }

    /**
     * Living MONSTER-category entities within the density radius of a spawner. Eternals, pets
     * and the other CREATURE-category vault entities are not counted.
     */
    public static int countNearbyMonsters(Level world, BlockPos pos) {
        int radius = HyperVaultObjective.cfg().getSpawnDensityRadius();
        return world.getEntities((Entity) null, new AABB(pos).inflate(radius),
                entity -> entity.isAlive() && entity.getType().getCategory() == MobCategory.MONSTER).size();
    }

    /**
     * Lowers an increase so the whole burst, {@code baseAttempts x (1 + increase)}, fits in the
     * headroom left under the density cap. The result reaches -1.0 (no attempts) once the area
     * is full.
     */
    public static double densityCappedIncrease(double baseAttempts, double increase, int nearbyMonsters) {
        int headroom = Math.max(0, HyperVaultObjective.cfg().getSpawnDensityCap() - nearbyMonsters);
        double burst = baseAttempts * (1.0 + increase);
        if (burst <= headroom) {
            return increase;
        }
        return headroom / baseAttempts - 1.0;
    }
}
