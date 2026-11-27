package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.entity.entity.EffectCloudEntity;
import net.minecraft.server.level.ServerLevel;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.api.util.ducks.DuckEffectCloudStacks;

import java.util.List;
import java.util.UUID;

public class EffectCloudMerger {
    private static final double MERGE_RADIUS_FRACTION = 0.5;
    private static final double SAME_LAYER = 1.0;
    private static final double OWNER_SCAN_RANGE = 32.0;
    private static final int MAX_CLOUDS_PER_OWNER = 24;
    private static final long WARN_INTERVAL_TICKS = 200L;
    private static long nextCapWarning = 0L;

    /**
     * Folds a cloud that is about to spawn into a matching live cloud from the same owner instead of adding a new entity.
     * A match must be the same cloud (effects, colour, particle, flags) centred within half the live cloud's radius and
     * on the same layer; the live cloud then gains a stack. When the owner already has {@link #MAX_CLOUDS_PER_OWNER}
     * clouds nearby, the new cloud stacks onto the closest identical cloud anywhere in range, or is dropped if none exists.
     *
     * @return true when the cloud was absorbed or dropped and must not be added to the level
     */
    public static boolean absorbIntoExisting(ServerLevel level, EffectCloudEntity cloud) {
        UUID owner = ((DuckEffectCloudStacks) cloud).woldsvaults$getOwnerId();
        if (owner == null) {
            return false;
        }
        List<EffectCloudEntity> owned = level.getEntitiesOfClass(EffectCloudEntity.class, cloud.getBoundingBox().inflate(OWNER_SCAN_RANGE),
                other -> other != cloud && other.isAlive() && owner.equals(((DuckEffectCloudStacks) other).woldsvaults$getOwnerId()));
        EffectCloudEntity covering = null;
        double coveringDistance = Double.MAX_VALUE;
        EffectCloudEntity closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (EffectCloudEntity other : owned) {
            if (!((DuckEffectCloudStacks) other).woldsvaults$canAbsorb(cloud)) {
                continue;
            }
            double dx = other.getX() - cloud.getX();
            double dz = other.getZ() - cloud.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (Math.abs(other.getY() - cloud.getY()) <= SAME_LAYER && distance <= other.getRadius() * MERGE_RADIUS_FRACTION && distance < coveringDistance) {
                covering = other;
                coveringDistance = distance;
            }
            if (distance < closestDistance) {
                closest = other;
                closestDistance = distance;
            }
        }
        if (covering != null) {
            ((DuckEffectCloudStacks) covering).woldsvaults$absorb(cloud);
            return true;
        }
        if (owned.size() < MAX_CLOUDS_PER_OWNER) {
            return false;
        }
        boolean warn = level.getGameTime() >= nextCapWarning;
        if (warn) {
            nextCapWarning = level.getGameTime() + WARN_INTERVAL_TICKS;
        }
        if (closest != null) {
            ((DuckEffectCloudStacks) closest).woldsvaults$absorb(cloud);
            if (warn) {
                WoldLogger.warn("Effect cloud cap ({} per owner) reached for {}; stacked the new cloud onto an identical cloud {} blocks away.",
                        MAX_CLOUDS_PER_OWNER, owner, String.format("%.1f", closestDistance));
            }
            return true;
        }
        if (warn) {
            WoldLogger.warn("Effect cloud cap ({} per owner) reached for {} with no identical cloud to stack onto; dropped the new cloud.",
                    MAX_CLOUDS_PER_OWNER, owner);
        }
        return true;
    }
}
