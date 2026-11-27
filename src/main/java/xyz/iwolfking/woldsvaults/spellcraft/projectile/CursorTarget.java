package xyz.iwolfking.woldsvaults.spellcraft.projectile;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.spellcraft.scope.AimOverride;

import java.util.Optional;
import java.util.function.Predicate;

final class CursorTarget {
    private CursorTarget() {
    }

    /** The enemy nearest the owner's crosshair: inside the aim cone and range, in sight, smallest angle wins. */
    static Optional<LivingEntity> cursorTarget(ServerPlayer owner, SpellcraftConfig config, Predicate<LivingEntity> valid) {
        Vec3 eye = owner.getEyePosition();
        Vec3 look = AimOverride.realLook(owner).normalize();
        double minCos = Math.cos(Math.toRadians(config.tuning.homingAimConeDegrees));
        double range = config.tuning.homingAimRange;
        AABB area = owner.getBoundingBox().inflate(range);
        LivingEntity best = null;
        double bestCos = minCos;
        for (LivingEntity candidate : owner.level.getEntitiesOfClass(LivingEntity.class, area, valid)) {
            Vec3 to = candidate.getBoundingBox().getCenter().subtract(eye);
            double distance = to.length();
            if (distance < 1.0E-4 || distance > range) {
                continue;
            }
            double cos = to.dot(look) / distance;
            if (cos > bestCos && owner.hasLineOfSight(candidate)) {
                bestCos = cos;
                best = candidate;
            }
        }
        return Optional.ofNullable(best);
    }
}
