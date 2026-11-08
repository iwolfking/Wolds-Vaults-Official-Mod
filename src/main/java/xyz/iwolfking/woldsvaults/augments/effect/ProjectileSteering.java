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
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.network.packets.OrbSteerMessage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Homing and Orbit: steers projectiles a cast spawned by setting their motion each server tick; the projectile's own
 * logic still moves it and handles hits. Steering stops when the projectile is gone, stuck, or its owner left.
 * Javelins already homing through the Warbound flag are left to that steering, so no projectile is steered twice.
 * Homing targets need no line of sight from the owner, so split children at a wall still find mobs past it.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class ProjectileSteering {
    private static final double GOLDEN_ANGLE = 2.39996;
    private static final double ORBIT_HEIGHT = 1.2;
    private static final double RESEND_COS = Math.cos(Math.toRadians(2.0));
    private static final int RESEND_TICKS = 10;
    private static final List<Steered> STEERED = new ArrayList<>();

    private ProjectileSteering() {
    }

    public static void orbit(Projectile projectile, ServerPlayer owner) {
        long orbiting = STEERED.stream().filter(s -> s.orbit && s.owner == owner).count();
        STEERED.add(new Steered(projectile, owner, true, orbiting * GOLDEN_ANGLE));
    }

    public static void home(Projectile projectile, ServerPlayer owner) {
        if (isNativelyHoming(projectile)) {
            return;
        }
        STEERED.add(new Steered(projectile, owner, false, 0));
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
        int maxOrbitTicks = config.tuning.orbitMaxSeconds * 20;
        for (Iterator<Steered> it = STEERED.iterator(); it.hasNext(); ) {
            Steered steered = it.next();
            Projectile projectile = steered.projectile;
            if (projectile.isRemoved() || steered.owner.isRemoved() || steered.owner.level != projectile.level
                    || steered.isStuck() || (!steered.orbit && isNativelyHoming(projectile))) {
                it.remove();
                continue;
            }
            steered.age++;
            if (steered.orbit) {
                if (steered.age > maxOrbitTicks) {
                    projectile.discard();
                    it.remove();
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
        Vec3 center = steered.owner.position().add(0, ORBIT_HEIGHT, 0);
        Vec3 target = center.add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
        move(steered, target.subtract(steered.projectile.position()));
    }

    private static void steerHoming(Steered steered, FocusAugmentsConfig config) {
        Projectile projectile = steered.projectile;
        AABB area = projectile.getBoundingBox().inflate(config.tuning.homingRange);
        LivingEntity target = steered.target;
        if (steered.age % Math.max(1, config.tuning.homingRetargetTicks) == 0 || target == null || target.level != projectile.level
                || !target.getBoundingBox().intersects(area) || !canTarget(projectile, steered.owner, target)) {
            target = findTarget(projectile, steered.owner, area).orElse(null);
            steered.target = target;
        }
        if (target == null) {
            return;
        }
        Vec3 motion = projectile.getDeltaMovement();
        double speed = Math.max(motion.length(), steered.speed);
        Vec3 desired = target.getBoundingBox().getCenter().subtract(projectile.position()).normalize().scale(speed);
        Vec3 turned = motion.add(desired.subtract(motion).scale(config.tuning.homingTurnRate));
        move(steered, turned.normalize().scale(speed));
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
        int age;
        Vec3 lastPosition;
        int stillTicks;
        LivingEntity target;
        Vec3 sentDirection;
        float sentSpeed;
        int sentAge;

        Steered(Projectile projectile, ServerPlayer owner, boolean orbit, double angleOffset) {
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
