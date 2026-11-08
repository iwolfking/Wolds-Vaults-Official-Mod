package xyz.iwolfking.woldsvaults.augments.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.augments.FocusAugments;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;

import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Low Gravity and Ethereal for VH ability projectiles, after the entities ticked. Low Gravity gives back a share of
 * the vanilla gravity just applied. Ethereal projectiles ignore block hits; arrows also get the no-physics flag while
 * inside a block, never near their owner (a no-physics javelin touching its owner is picked up into a null item,
 * which crashes the tick). Ethereal projectiles are removed after the table's lifetime.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class ProjectilePhysics {
    private static final double ARROW_GRAVITY = 0.05;
    private static final double THROWN_GRAVITY = 0.03;
    private static final double OWNER_CLEARANCE_SQR = 3.0 * 3.0;
    private static final Map<Projectile, Physics> TRACKED = new WeakHashMap<>();

    private ProjectilePhysics() {
    }

    public static void track(Projectile projectile, float gravityReduction, boolean ethereal) {
        TRACKED.put(projectile, new Physics(gravityReduction, ethereal));
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
        FocusAugmentsConfig config = FocusAugments.config();
        int maxEtherealTicks = config == null ? 0 : config.tuning.etherealMaxSeconds * 20;
        for (Iterator<Map.Entry<Projectile, Physics>> it = TRACKED.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Projectile, Physics> entry = it.next();
            Projectile projectile = entry.getKey();
            Physics physics = entry.getValue();
            if (projectile.isRemoved()) {
                it.remove();
                continue;
            }
            physics.age++;
            if (physics.gravityReduction > 0 && !projectile.isNoGravity()) {
                double gravity = projectile instanceof AbstractArrow ? ARROW_GRAVITY
                        : projectile instanceof ThrowableProjectile ? THROWN_GRAVITY : 0.0;
                projectile.setDeltaMovement(projectile.getDeltaMovement().add(0, gravity * physics.gravityReduction, 0));
                projectile.hurtMarked = true;
            }
            if (physics.ethereal) {
                if (physics.age > maxEtherealTicks) {
                    projectile.discard();
                    it.remove();
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
        int age;

        Physics(float gravityReduction, boolean ethereal) {
            this.gravityReduction = gravityReduction;
            this.ethereal = ethereal;
        }
    }
}
