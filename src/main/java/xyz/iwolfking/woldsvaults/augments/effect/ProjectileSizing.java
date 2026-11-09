package xyz.iwolfking.woldsvaults.augments.effect;

import iskallia.vault.entity.entity.LightningOrbEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.mixins.accessors.AccessorProjectile;
import xyz.iwolfking.woldsvaults.network.packets.ProjectileScaleMessage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Size Up and Size Down on VH ability projectiles. The hitbox follows Forge's size event; clients learn the scale
 * when they start tracking the projectile and draw it scaled. Vanilla projectiles find entities with a thin ray, so an
 * enlarged projectile also hits any entity its enlarged box swept through this tick, through the projectile's own
 * impact event and hit handling. Those hits can spawn more sized projectiles, so the tick walks a snapshot. Ball of Lightning damages by its own synced radius, so its radius is scaled instead.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class ProjectileSizing {
    private static final Map<Projectile, Set<Entity>> ENLARGED = new WeakHashMap<>();

    private ProjectileSizing() {
    }

    public static void apply(Projectile projectile, float scale) {
        if (projectile instanceof LightningOrbEntity orb) {
            orb.setRadius(orb.getRadius() * scale);
            return;
        }
        projectile.refreshDimensions();
        if (scale > 1.0F) {
            ENLARGED.put(projectile, new HashSet<>());
        }
    }

    @SubscribeEvent
    public static void onSize(EntityEvent.Size event) {
        Entity entity = event.getEntity();
        if (entity instanceof Projectile && !(entity instanceof LightningOrbEntity) && entity.getPersistentData().contains(AugmentTags.SCALE)) {
            event.setNewSize(event.getNewSize().scale(AugmentTags.scale(entity)), true);
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        Entity target = event.getTarget();
        if (target instanceof Projectile && target.getPersistentData().contains(AugmentTags.SCALE)
                && event.getPlayer() instanceof ServerPlayer player) {
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ProjectileScaleMessage(target.getId(), AugmentTags.scale(target)));
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || ENLARGED.isEmpty()) {
            return;
        }
        for (Map.Entry<Projectile, Set<Entity>> entry : new ArrayList<>(ENLARGED.entrySet())) {
            Projectile projectile = entry.getKey();
            if (projectile.isRemoved()) {
                ENLARGED.remove(projectile);
            } else if (projectile.getDeltaMovement().lengthSqr() > 1.0E-4) {
                hitTouchingEntities(projectile, entry.getValue());
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        ENLARGED.clear();
    }

    private static void hitTouchingEntities(Projectile projectile, Set<Entity> alreadyHit) {
        AABB box = projectile.getBoundingBox();
        AABB swept = box.minmax(box.move(projectile.xo - projectile.getX(), projectile.yo - projectile.getY(),
                projectile.zo - projectile.getZ()));
        AccessorProjectile access = (AccessorProjectile) projectile;
        for (Entity target : projectile.level.getEntities(projectile, swept,
                e -> !e.isSpectator() && e.isAlive() && e.isPickable() && !alreadyHit.contains(e))) {
            if (projectile.isRemoved()) {
                return;
            }
            if (!access.woldsvaults$canHitEntity(target)) {
                continue;
            }
            alreadyHit.add(target);
            EntityHitResult hit = new EntityHitResult(target);
            if (!ForgeEventFactory.onProjectileImpact(projectile, hit)) {
                TickingEntity.push(projectile);
                try {
                    access.woldsvaults$onHit(hit);
                } finally {
                    TickingEntity.pop();
                }
            }
        }
    }
}
