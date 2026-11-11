package xyz.iwolfking.woldsvaults.augments.effect;

import iskallia.vault.entity.entity.IceBoltEntity;
import iskallia.vault.entity.entity.LightningOrbEntity;
import iskallia.vault.entity.entity.ToxicGrenadeEntity;
import iskallia.vault.entity.entity.VaultBlizzardShard;
import iskallia.vault.entity.entity.VaultDecoyProjectile;
import iskallia.vault.entity.entity.VaultFireball;
import iskallia.vault.entity.entity.VaultGrenade;
import iskallia.vault.entity.entity.VaultStormArrow;
import iskallia.vault.entity.entity.VaultThrownJavelin;
import iskallia.vault.skill.ability.effect.ChainLightningAbility;
import iskallia.vault.skill.ability.effect.NecromancyAbility;
import iskallia.vault.util.EntityHelper;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.cast.CastContext;
import xyz.iwolfking.woldsvaults.api.lib.HomingJavelin;
import xyz.iwolfking.woldsvaults.api.lib.IWoldAbilityEnhancements;
import xyz.iwolfking.woldsvaults.api.util.ElementHelper;
import xyz.iwolfking.woldsvaults.augments.FocusAugments;
import xyz.iwolfking.woldsvaults.augments.bundle.AugmentBundleEntity;
import xyz.iwolfking.woldsvaults.augments.cast.AimOverride;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.mixins.accessors.AccessorProjectile;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.LightningOrbEntityAccessor;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.ToxicGrenadeEntityAccessor;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.VaultDecoyProjectileInvoker;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.VaultGrenadeAccessor;
import xyz.iwolfking.woldsvaults.network.packets.OrbSteerMessage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Homing and Orbit: steers projectiles a cast spawned by setting their motion each server tick; the projectile's own
 * logic still moves it and handles hits. Steering stops when the projectile is gone, stuck, or its owner left.
 * Javelins already homing through the Warbound flag are left to that steering, so no projectile is steered twice.
 * Homing flies straight until an enemy is within the table's lock-on range, then turns toward it at its rolled rate
 * (degrees per second). The enemy nearest the owner's crosshair when the projectile was fired is preferred whenever it
 * is in range; otherwise the nearest enemy is chased. Lock-on needs no line of sight, so split children at a wall still
 * find mobs past it. Steered projectiles send their position and motion to clients every tick.
 * <p>
 * An orbit ends on an absolute game tick kept in the projectile's tags, so copies it spawns (bounces, ricochets, splits)
 * orbit only for the time left; a copy made after the end does not orbit. The projectile's own timers (grenade fuses,
 * Ball of Lightning duration, Volley duration, Bundled Spell flight; Taunt decoy and Blizzard shard through their
 * mixins) are held until the orbit ends. At the end, or when an orbiting projectile gets stuck, it detonates by type
 * inside its cast's context: fireballs explode, grenades explode, ice bolts run their impact area, decoys spawn their
 * decoy, Bundled Spell orbs land their payload, and the rest are removed. Orbiting grenades explode on contact.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class ProjectileSteering {
    private static final double GOLDEN_ANGLE = 2.39996;
    private static final double RESEND_COS = Math.cos(Math.toRadians(2.0));
    private static final int RESEND_TICKS = 10;
    private static final List<Steered> STEERED = new ArrayList<>();
    private static final Map<Projectile, Steered> ORBITING = new WeakHashMap<>();
    private static final Set<Class<?>> UNDETONATED_WARNED = ConcurrentHashMap.newKeySet();

    private ProjectileSteering() {
    }

    /**
     * Starts orbiting {@code projectile} around {@code owner} until its orbit's end tick: the one in its tags when it
     * inherited an orbit, else {@code seconds} from now (stored in its tags when it carries the Orbit tag). Returns
     * false, steering nothing, when that end has already passed.
     */
    public static boolean orbit(Projectile projectile, ServerPlayer owner, float seconds) {
        long now = projectile.level.getGameTime();
        CompoundTag data = projectile.getPersistentData();
        long end;
        if (data.contains(AugmentTags.ORBIT_END)) {
            end = data.getLong(AugmentTags.ORBIT_END);
        } else {
            end = now + Math.max(1, Math.round(seconds * 20.0F));
            if (data.contains(AugmentTags.ORBIT)) {
                data.putLong(AugmentTags.ORBIT_END, end);
            }
        }
        if (end <= now) {
            return false;
        }
        long orbiting = STEERED.stream().filter(s -> s.orbit && s.owner == owner).count();
        Steered steered = new Steered(projectile, owner, true, orbiting * GOLDEN_ANGLE, 0, 0, end);
        STEERED.add(steered);
        ORBITING.put(projectile, steered);
        holdTimers(projectile, (int) Math.min(Integer.MAX_VALUE / 2, end - now));
        return true;
    }

    /** Whether the server-side {@code entity} is orbiting right now (its orbit has not ended or been dropped). */
    public static boolean isOrbiting(Entity entity) {
        if (entity.level.isClientSide() || !(entity instanceof Projectile projectile)) {
            return false;
        }
        Steered steered = ORBITING.get(projectile);
        return steered != null && steered.projectile == entity;
    }

    public static void home(Projectile projectile, ServerPlayer owner, float range, float turnRate) {
        if (isNativelyHoming(projectile)) {
            return;
        }
        Steered steered = new Steered(projectile, owner, false, 0, range, turnRate, 0L);
        FocusAugmentsConfig config = FocusAugments.config();
        if (config != null) {
            steered.preferred = cursorTarget(owner, config, e -> canTarget(projectile, owner, e)).orElse(null);
        }
        STEERED.add(steered);
    }

    /** The enemy nearest the owner's crosshair: inside the aim cone and range, in sight, smallest angle wins. */
    public static Optional<LivingEntity> cursorTarget(ServerPlayer owner, FocusAugmentsConfig config, Predicate<LivingEntity> valid) {
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
            ORBITING.clear();
            return;
        }
        for (Steered steered : new ArrayList<>(STEERED)) {
            Projectile projectile = steered.projectile;
            if (projectile.isRemoved() || steered.owner.isRemoved() || steered.owner.level != projectile.level
                    || (!steered.orbit && isNativelyHoming(projectile))) {
                drop(steered);
                continue;
            }
            boolean stuck = steered.isStuck();
            if (steered.orbit && (stuck || projectile.level.getGameTime() >= steered.orbitEnd)) {
                drop(steered);
                detonate(projectile);
                continue;
            }
            if (stuck) {
                drop(steered);
                continue;
            }
            steered.age++;
            if (steered.orbit) {
                steerOrbit(steered, config);
            } else {
                steerHoming(steered, config);
            }
            projectile.hurtMarked = true;
            projectile.hasImpulse = true;
        }
    }

    /** An orbiting grenade explodes on contact: its fuse is cut to now, so its own tick explodes it this tick. */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        Projectile projectile = event.getProjectile();
        if (event.getRayTraceResult().getType() == HitResult.Type.MISS || !isOrbiting(projectile)) {
            return;
        }
        EntityDataAccessor<Integer> fuse = fuseOf(projectile);
        if (fuse != null) {
            projectile.getEntityData().set(fuse, Math.max(1, projectile.tickCount));
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        STEERED.clear();
        ORBITING.clear();
    }

    private static void drop(Steered steered) {
        STEERED.remove(steered);
        if (steered.orbit) {
            ORBITING.remove(steered.projectile);
        }
    }

    private static EntityDataAccessor<Integer> fuseOf(Projectile projectile) {
        if (projectile instanceof VaultGrenade) {
            return VaultGrenadeAccessor.woldsvaults$duration();
        }
        if (projectile instanceof ToxicGrenadeEntity) {
            return ToxicGrenadeEntityAccessor.woldsvaults$duration();
        }
        return null;
    }

    /** Holds the projectile's own lifetime timers for {@code remaining} more ticks, one tick past the orbit's end. */
    private static void holdTimers(Projectile projectile, int remaining) {
        EntityDataAccessor<Integer> fuse = fuseOf(projectile);
        if (fuse != null) {
            int fuseTicks = projectile.getEntityData().get(fuse);
            if (fuseTicks > 0 && fuseTicks <= projectile.tickCount + remaining) {
                projectile.getEntityData().set(fuse, projectile.tickCount + remaining + 1);
            }
        } else if (projectile instanceof LightningOrbEntity orb) {
            int until = orb.getEntityData().get(LightningOrbEntityAccessor.woldsvaults$getAge()) + remaining + 1;
            if (orb.getEntityData().get(LightningOrbEntityAccessor.woldsvaults$getDuration()) < until) {
                orb.getEntityData().set(LightningOrbEntityAccessor.woldsvaults$getDuration(), until);
            }
        } else if (projectile instanceof VaultFireball fireball && fireball.getFireballType() == VaultFireball.FireballType.BOUNCING
                && fireball.getDuration() > 0) {
            int seconds = (fireball.tickCount + remaining) / 20 + 1;
            if (fireball.getDuration() < seconds) {
                fireball.setDuration(seconds);
            }
        } else if (projectile instanceof AugmentBundleEntity bundle) {
            bundle.holdFor(remaining + 1);
        }
    }

    /** Detonates an orbiting projectile whose orbit ended, inside its cast's context, so its glyphs still apply. */
    private static void detonate(Projectile projectile) {
        TickingEntity.push(projectile);
        try {
            CastContext.runTask(CastContext.tagOf(projectile), SpellHits.Carrier.ENTITY, () -> detonateByType(projectile));
        } finally {
            TickingEntity.pop();
        }
        if (!projectile.isRemoved()) {
            projectile.discard();
        }
    }

    private static void detonateByType(Projectile projectile) {
        if (projectile instanceof VaultFireball fireball) {
            ElementHelper.runWithElements(((IWoldAbilityEnhancements) fireball).getElementTypes(), () -> fireball.explode(fireball.position()));
        } else if (projectile instanceof VaultGrenade) {
            ((VaultGrenadeAccessor) projectile).woldsvaults$explode();
        } else if (projectile instanceof ToxicGrenadeEntity) {
            ((ToxicGrenadeEntityAccessor) projectile).woldsvaults$explode();
        } else if (projectile instanceof IceBoltEntity) {
            Vec3 motion = projectile.getDeltaMovement();
            Direction facing = Direction.getNearest(motion.x, motion.y, motion.z);
            ((AccessorProjectile) projectile).woldsvaults$onHit(BlockHitResult.miss(projectile.position(), facing, projectile.blockPosition()));
        } else if (projectile instanceof VaultDecoyProjectile && projectile.level instanceof ServerLevel level) {
            ((VaultDecoyProjectileInvoker) projectile).woldsvaults$spawnDecoy(level, projectile.position());
        } else if (projectile instanceof AugmentBundleEntity bundle) {
            bundle.landHere();
        } else if (!endsByRemoval(projectile) && UNDETONATED_WARNED.add(projectile.getClass())) {
            WoldsVaults.LOGGER.warn("Orbit: no detonation is known for projectile class {}; it is removed at its orbit end instead.",
                    projectile.getClass().getName());
        }
    }

    /** Projectiles that only act on contact, so the end of their orbit simply removes them. */
    private static boolean endsByRemoval(Projectile projectile) {
        return projectile instanceof LightningOrbEntity || projectile instanceof VaultBlizzardShard
                || projectile instanceof VaultThrownJavelin || projectile instanceof VaultStormArrow
                || projectile instanceof ChainLightningAbility.ChainLightningProjectile
                || projectile instanceof NecromancyAbility.NecromancyBolt;
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
    public static Vec3 rotateToward(Vec3 from, Vec3 to, double maxAngle) {
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
        final long orbitEnd;
        int age;
        Vec3 lastPosition;
        int stillTicks;
        LivingEntity target;
        LivingEntity preferred;
        Vec3 sentDirection;
        float sentSpeed;
        int sentAge;

        Steered(Projectile projectile, ServerPlayer owner, boolean orbit, double angleOffset, double range, double turnRate, long orbitEnd) {
            this.range = range;
            this.turnRate = turnRate;
            this.orbitEnd = orbitEnd;
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
