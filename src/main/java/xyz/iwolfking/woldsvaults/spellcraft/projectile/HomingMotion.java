package xyz.iwolfking.woldsvaults.spellcraft.projectile;

import iskallia.vault.entity.entity.LightningOrbEntity;
import iskallia.vault.util.EntityHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;

import java.util.Comparator;
import java.util.Optional;

final class HomingMotion {
    private HomingMotion() {
    }

    static void steerHoming(Steered steered, SpellcraftConfig config) {
        Projectile projectile = steered.projectile;
        AABB area = projectile.getBoundingBox().inflate(steered.range);
        LivingEntity preferred = steered.preferred;
        LivingEntity target = steered.target;
        if (preferred != null && inRange(projectile, steered.owner, preferred, area)) {
            target = preferred;
        } else if (steered.age % Math.max(1, config.tuning.homingRetargetTicks) == 0 || target == null
                || !inRange(projectile, steered.owner, target, area)) {
            target = findTarget(projectile, steered.owner, area).orElse(null);
        }
        steered.target = target;
        if (target == null) {
            return;
        }
        Vec3 motion = projectile.getDeltaMovement();
        double speed = Math.max(motion.length(), steered.speed);
        Vec3 current = motion.lengthSqr() > 1.0E-8 ? motion.normalize() : projectile.getLookAngle();
        Vec3 desired = target.getBoundingBox().getCenter().subtract(projectile.position());
        if (desired.lengthSqr() < 1.0E-8) {
            return;
        }
        ProjectileSteering.move(steered, rotateToward(current, desired.normalize(), Math.toRadians(steered.turnRate / 20.0)).scale(speed));
    }

    private static boolean inRange(Projectile projectile, ServerPlayer owner, LivingEntity target, AABB area) {
        return target.level == projectile.level && target.getBoundingBox().intersects(area) && canTarget(projectile, owner, target);
    }

    /** Turns a unit vector toward another by at most {@code maxAngle} radians, along the great circle. */
    static Vec3 rotateToward(Vec3 from, Vec3 to, double maxAngle) {
        double dot = Math.max(-1.0, Math.min(1.0, from.dot(to)));
        double angle = Math.acos(dot);
        if (angle <= maxAngle || angle < 1.0E-6) {
            return to;
        }
        Vec3 ortho = to.subtract(from.scale(dot));
        if (ortho.lengthSqr() < 1.0E-10) {
            ortho = Math.abs(from.y) < 0.9 ? from.cross(new Vec3(0, 1, 0)) : from.cross(new Vec3(1, 0, 0));
        }
        ortho = ortho.normalize();
        return from.scale(Math.cos(maxAngle)).add(ortho.scale(Math.sin(maxAngle))).normalize();
    }

    private static Optional<LivingEntity> findTarget(Projectile projectile, ServerPlayer owner, AABB area) {
        return projectile.level.getEntitiesOfClass(LivingEntity.class, area, e -> canTarget(projectile, owner, e))
                .stream()
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(projectile)));
    }

    static boolean canTarget(Projectile projectile, ServerPlayer owner, LivingEntity e) {
        return e != owner && e.isAlive() && EntityHelper.VAULT_TARGET_SELECTOR.test(e)
                && !(projectile instanceof LightningOrbEntity orb && LightningOrbs.alreadyHit(orb).contains(e.getId()));
    }
}
