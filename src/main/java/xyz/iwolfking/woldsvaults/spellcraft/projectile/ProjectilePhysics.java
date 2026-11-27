package xyz.iwolfking.woldsvaults.spellcraft.projectile;

import iskallia.vault.entity.entity.ToxicGrenadeEntity;
import iskallia.vault.entity.entity.VaultDecoyProjectile;
import iskallia.vault.entity.entity.VaultFireball;
import iskallia.vault.entity.entity.VaultGrenade;
import iskallia.vault.entity.entity.VaultStormArrow;
import iskallia.vault.entity.entity.VaultThrownJavelin;
import iskallia.vault.skill.ability.effect.NecromancyAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Low Gravity and Ethereal for VH ability projectiles, applied after the entities ticked. Low Gravity gives back the
 * rolled share of the gravity the projectile's own class applies each tick, so it falls {@code 1 - roll} as fast and
 * never rises. Ethereal projectiles ignore block hits and are removed after their glyph's rolled lifetime unless still
 * orbiting, and arrows also get the no-physics flag inside a block but never near their owner, since a no-physics
 * javelin touching its owner is picked up into a null item and crashes the tick.
 */
public final class ProjectilePhysics {
    private static final double FIREBALL_GRAVITY = 0.025;
    private static final double VOLLEY_GRAVITY = FIREBALL_GRAVITY * 4.0;
    private static final double STORM_ARROW_GRAVITY = 0.025;
    private static final double JAVELIN_GRAVITY = 0.05;
    private static final double VANILLA_ARROW_GRAVITY = 0.05;
    private static final double VANILLA_THROWN_GRAVITY = 0.03;
    private static final Set<Class<?>> UNKNOWN_WARNED = ConcurrentHashMap.newKeySet();
    private static final double OWNER_CLEARANCE_SQR = 3.0 * 3.0;
    private static final Map<Projectile, Physics> TRACKED = new WeakHashMap<>();

    private ProjectilePhysics() {
    }

    public static void track(Projectile projectile, float gravityReduction, float etherealSeconds) {
        TRACKED.put(projectile, new Physics(gravityReduction, etherealSeconds > 0, Math.max(1, Math.round(etherealSeconds * 20.0F))));
    }

    static boolean ignoresBlockHit(Projectile projectile, HitResult hit) {
        Physics physics = TRACKED.get(projectile);
        return physics != null && physics.ethereal && hit.getType() == HitResult.Type.BLOCK;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || TRACKED.isEmpty()) {
            return;
        }
        for (Map.Entry<Projectile, Physics> entry : new ArrayList<>(TRACKED.entrySet())) {
            Projectile projectile = entry.getKey();
            Physics physics = entry.getValue();
            if (projectile.isRemoved()) {
                TRACKED.remove(projectile);
                continue;
            }
            physics.age++;
            if (physics.gravityReduction > 0 && !projectile.isNoGravity()) {
                double gravity = gravityOf(projectile);
                if (gravity > 0.0) {
                    projectile.setDeltaMovement(projectile.getDeltaMovement().add(0, gravity * physics.gravityReduction, 0));
                    projectile.hurtMarked = true;
                }
            }
            if (physics.ethereal) {
                if (physics.age > physics.etherealTicks && !ProjectileSteering.isOrbiting(projectile)) {
                    projectile.discard();
                    TRACKED.remove(projectile);
                    continue;
                }
                if (projectile instanceof AbstractArrow arrow) {
                    Vec3 next = arrow.position().add(arrow.getDeltaMovement());
                    boolean nearOwner = arrow.getOwner() != null && arrow.getOwner().distanceToSqr(arrow) < OWNER_CLEARANCE_SQR;
                    arrow.setNoPhysics(!nearOwner && (isInsideBlock(arrow, arrow.position()) || isInsideBlock(arrow, next)));
                }
            }
        }
    }

    private static double gravityOf(Projectile projectile) {
        if (projectile instanceof VaultFireball fireball) {
            return fireball.getFireballType() == VaultFireball.FireballType.BOUNCING ? VOLLEY_GRAVITY : FIREBALL_GRAVITY;
        }
        if (projectile instanceof VaultStormArrow) {
            return STORM_ARROW_GRAVITY;
        }
        if (projectile instanceof VaultThrownJavelin) {
            return JAVELIN_GRAVITY;
        }
        if (projectile instanceof NecromancyAbility.NecromancyBolt) {
            return VANILLA_ARROW_GRAVITY;
        }
        if (projectile instanceof VaultGrenade || projectile instanceof ToxicGrenadeEntity || projectile instanceof VaultDecoyProjectile) {
            return VANILLA_THROWN_GRAVITY;
        }
        if (UNKNOWN_WARNED.add(projectile.getClass())) {
            WoldLogger.error("Low Gravity: no gravity entry for projectile class {}; the glyph does nothing on it.", projectile.getClass().getName());
        }
        return 0.0;
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        TRACKED.clear();
    }

    private static boolean isInsideBlock(Projectile projectile, Vec3 position) {
        BlockPos pos = new BlockPos(position);
        BlockState state = projectile.level.getBlockState(pos);
        return !state.isAir() && !state.getCollisionShape(projectile.level, pos).isEmpty();
    }

    private static final class Physics {
        final float gravityReduction;
        final boolean ethereal;
        final int etherealTicks;
        int age;

        Physics(float gravityReduction, boolean ethereal, int etherealTicks) {
            this.gravityReduction = gravityReduction;
            this.ethereal = ethereal;
            this.etherealTicks = etherealTicks;
        }
    }
}
