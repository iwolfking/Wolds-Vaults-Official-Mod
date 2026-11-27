package xyz.iwolfking.woldsvaults.spellcraft.projectile;

import iskallia.vault.entity.entity.LightningOrbEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;
import xyz.iwolfking.woldsvaults.api.lib.HomingJavelin;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.network.packets.OrbSteerMessage;
import xyz.iwolfking.woldsvaults.spellcraft.scope.ProjectileGlyphTags;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Homing and Orbit: steers projectiles a cast spawned by setting their motion each server tick, while the projectile's
 * own logic still moves it and handles hits. Homing flies straight until an enemy is within lock-on range, then turns
 * toward it at its rolled rate, preferring the enemy nearest the owner's crosshair when the projectile was fired. An
 * orbit ends on an absolute game tick kept in the projectile's tags, so copies it spawns orbit only for the time left,
 * and at the end the projectile detonates by type inside its cast's context.
 */
public final class ProjectileSteering {
    private static final double GOLDEN_ANGLE = 2.39996;
    private static final List<Steered> STEERED = new ArrayList<>();
    private static final Map<Projectile, Steered> ORBITING = new WeakHashMap<>();

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
        if (data.contains(ProjectileGlyphTags.ORBIT_END)) {
            end = data.getLong(ProjectileGlyphTags.ORBIT_END);
        } else {
            end = now + Math.max(1, Math.round(seconds * 20.0F));
            if (data.contains(ProjectileGlyphTags.ORBIT)) {
                data.putLong(ProjectileGlyphTags.ORBIT_END, end);
            }
        }
        if (end <= now) {
            return false;
        }
        long orbiting = STEERED.stream().filter(s -> s.orbit && s.owner == owner).count();
        Steered steered = new Steered(projectile, owner, true, orbiting * GOLDEN_ANGLE, 0, 0, end);
        STEERED.add(steered);
        ORBITING.put(projectile, steered);
        ProjectileDetonation.holdTimers(projectile, (int) Math.min(Integer.MAX_VALUE / 2, end - now));
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
        SpellcraftConfig config = ModConfigs.SPELLCRAFT;
        if (config != null) {
            steered.preferred = CursorTarget.cursorTarget(owner, config, e -> HomingMotion.canTarget(projectile, owner, e)).orElse(null);
        }
        STEERED.add(steered);
    }

    private static boolean isNativelyHoming(Projectile projectile) {
        return projectile instanceof HomingJavelin javelin && javelin.woldsvaults$isHoming();
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || STEERED.isEmpty()) {
            return;
        }
        SpellcraftConfig config = ModConfigs.SPELLCRAFT;
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
                ProjectileDetonation.detonate(projectile);
                continue;
            }
            if (stuck) {
                drop(steered);
                continue;
            }
            steered.age++;
            if (steered.orbit) {
                OrbitMotion.steerOrbit(steered, config);
            } else {
                HomingMotion.steerHoming(steered, config);
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
        EntityDataAccessor<Integer> fuse = ProjectileDetonation.fuseOf(projectile);
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

    static void move(Steered steered, Vec3 motion) {
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
}
