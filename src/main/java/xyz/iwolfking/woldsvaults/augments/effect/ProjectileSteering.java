package xyz.iwolfking.woldsvaults.augments.effect;

import iskallia.vault.entity.entity.LightningOrbEntity;
import iskallia.vault.util.EntityHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.lib.HomingJavelin;
import xyz.iwolfking.woldsvaults.augments.FocusAugments;
import xyz.iwolfking.woldsvaults.augments.cast.AimOverride;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.network.packets.OrbSteerMessage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Homing and Orbit: steers projectiles a cast spawned by setting their motion each server tick; the projectile's own
 * logic still moves it and handles hits. Steering stops when the projectile is gone, stuck, or its owner left.
 * Javelins already homing through the Warbound flag are left to that steering, so no projectile is steered twice.
 * Homing flies straight until an enemy is within the table's lock-on range, then turns toward it at its rolled rate
 * (degrees per second). The enemy nearest the owner's crosshair when the projectile was fired is preferred whenever it
 * is in range; otherwise the nearest enemy is chased. Lock-on needs no line of sight, so split children at a wall still
 * find mobs past it.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class ProjectileSteering {
    private static final double GOLDEN_ANGLE = 2.39996;
    private static final double RESEND_COS = Math.cos(Math.toRadians(2.0));
    private static final int RESEND_TICKS = 10;
    private static final List<Steered> STEERED = new ArrayList<>();

    private ProjectileSteering() {
    }

    public static void orbit(Projectile projectile, ServerPlayer owner, float seconds) {
        long orbiting = STEERED.stream().filter(s -> s.orbit && s.owner == owner).count();
        STEERED.add(new Steered(projectile, owner, true, orbiting * GOLDEN_ANGLE, 0, 0, Math.max(1, Math.round(seconds * 20.0F))));
    }

    public static void home(Projectile projectile, ServerPlayer owner, float range, float turnRate) {
        if (isNativelyHoming(projectile)) {
            return;
        }
        Steered steered = new Steered(projectile, owner, false, 0, range, turnRate, 0);
        FocusAugmentsConfig config = FocusAugments.config();
        if (config != null) {
            steered.preferred = cursorTarget(projectile, owner, config).orElse(null);
        }
        STEERED.add(steered);
    }

    /** The enemy nearest the owner's crosshair: inside the aim cone and range, in sight, smallest angle wins. */
    private static Optional<LivingEntity> cursorTarget(Projectile projectile, ServerPlayer owner, FocusAugmentsConfig config) {
        Vec3 eye = owner.getEyePosition();
        Vec3 look = AimOverride.realLook(owner).normalize();
        double minCos = Math.cos(Math.toRadians(config.tuning.homingAimConeDegrees));
        double range = config.tuning.homingAimRange;
        AABB area = owner.getBoundingBox().inflate(range);
        LivingEntity best = null;
        double bestCos = minCos;
        for (LivingEntity candidate : owner.level.getEntitiesOfClass(LivingEntity.class, area, e -> canTarget(projectile, owner, e))) {
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

    private static boolean isNativelyHoming(Projectile projectile) {
        return projectile instanceof HomingJavelin javelin && javelin.woldsvaults$isHoming();
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || STEERED.isEmpty()) {
            return;
        }
        FocusAugmentsConfig config = FocusAugments.config();
        if (config == null) {
            STEERED.clear();
            return;
        }
        for (Steered steered : new ArrayList<>(STEERED)) {
            Projectile projectile = steered.projectile;
            if (projectile.isRemoved() || steered.owner.isRemoved() || steered.owner.level != projectile.level
                    || steered.isStuck() || (!steered.orbit && isNativelyHoming(projectile))) {
                STEERED.remove(steered);
                continue;
            }
            steered.age++;
            if (steered.orbit) {
                if (steered.age > steered.orbitTicks) {
                    projectile.discard();
                    STEERED.remove(steered);
                    continue;
                }
                steerOrbit(steered, config);
            } else {
                steerHoming(steered, config);
            }
            projectile.hurtMarked = true;
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        STEERED.clear();
    }

    private static void steerOrbit(Steered steered, FocusAugmentsConfig config) {
        double radius = config.tuning.orbitRadius;
        double angularSpeed = Math.max(0.15, Math.min(0.6, steered.speed / radius));
        double angle = steered.angleOffset + steered.age * angularSpeed;
        Vec3 center = steered.owner.position().add(0, config.tuning.orbitHeight, 0);
        Vec3 target = center.add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
        move(steered, target.subtract(steered.projectile.position()));
    }

    private static void steerHoming(Steered steered, FocusAugmentsConfig config) {
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
        move(steered, rotateToward(current, desired.normalize(), Math.toRadians(steered.turnRate / 20.0)).scale(speed));
    }

    private static boolean inRange(Projectile projectile, ServerPlayer owner, LivingEntity target, AABB area) {
        return target.level == projectile.level && target.getBoundingBox().intersects(area) && canTarget(projectile, owner, target);
    }

    /** Turns a unit vector toward another by at most {@code maxAngle} radians, along the great circle. */
    private static Vec3 rotateToward(Vec3 from, Vec3 to, double maxAngle) {
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

    private static void move(Steered steered, Vec3 motion) {
        steered.projectile.setDeltaMovement(motion);
        if (steered.projectile instanceof LightningOrbEntity orb && motion.lengthSqr() > 1.0E-8) {
            Vec3 direction = motion.normalize();
            float speed = (float) motion.length();
            LightningOrbs.steer(orb, direction, speed);
            if (steered.needsResend(direction, speed)) {
                ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> orb),
                        new OrbSteerMessage(orb.getId(), direction, speed));
            }
        }
    }

    private static Optional<LivingEntity> findTarget(Projectile projectile, ServerPlayer owner, AABB area) {
        return projectile.level.getEntitiesOfClass(LivingEntity.class, area, e -> canTarget(projectile, owner, e))
                .stream()
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(projectile)));
    }

    private static boolean canTarget(Projectile projectile, ServerPlayer owner, LivingEntity e) {
        return e != owner && e.isAlive() && EntityHelper.VAULT_TARGET_SELECTOR.test(e)
                && !(projectile instanceof LightningOrbEntity orb && LightningOrbs.alreadyHit(orb).contains(e.getId()));
    }

    private static final class Steered {
        final Projectile projectile;
        final ServerPlayer owner;
        final boolean orbit;
        final double angleOffset;
        final double speed;
        final double range;
        final double turnRate;
        final int orbitTicks;
        int age;
        Vec3 lastPosition;
        int stillTicks;
        LivingEntity target;
        LivingEntity preferred;
        Vec3 sentDirection;
        float sentSpeed;
        int sentAge;

        Steered(Projectile projectile, ServerPlayer owner, boolean orbit, double angleOffset, double range, double turnRate, int orbitTicks) {
            this.range = range;
            this.turnRate = turnRate;
            this.orbitTicks = orbitTicks;
            this.projectile = projectile;
            this.owner = owner;
            this.orbit = orbit;
            this.angleOffset = angleOffset;
            this.speed = projectile.getDeltaMovement().length();
            this.lastPosition = projectile.position();
        }

        boolean isStuck() {
            Vec3 position = this.projectile.position();
            this.stillTicks = position.distanceToSqr(this.lastPosition) < 1.0E-4 ? this.stillTicks + 1 : 0;
            this.lastPosition = position;
            return this.age > 2 && this.stillTicks >= 2;
        }

        boolean needsResend(Vec3 direction, float speed) {
            if (this.sentDirection != null && this.age - this.sentAge < RESEND_TICKS && direction.dot(this.sentDirection) > RESEND_COS
                    && Math.abs(speed - this.sentSpeed) <= this.sentSpeed * 0.05F) {
                return false;
            }
            this.sentDirection = direction;
            this.sentSpeed = speed;
            this.sentAge = this.age;
            return true;
        }
    }
}
