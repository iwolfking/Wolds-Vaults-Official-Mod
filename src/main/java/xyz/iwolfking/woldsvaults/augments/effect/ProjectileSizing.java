package xyz.iwolfking.woldsvaults.augments.effect;

import iskallia.vault.entity.entity.LightningOrbEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.cast.CastContext;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.mixins.accessors.AccessorProjectile;
import xyz.iwolfking.woldsvaults.network.packets.ProjectileScaleMessage;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Size Up and Size Down on VH ability projectiles. The hitbox follows Forge's size event; clients learn the scale
 * when they start tracking the projectile and draw it scaled. Vanilla projectiles find entities with a thin ray, so an
 * enlarged projectile also hits any entity its enlarged box swept through since the last check (from the position seen
 * then, never from {@code xo}, which is still the world origin before a new projectile's first tick), through the projectile's own
 * impact event and hit handling, run in the projectile's cast context as its own tick's hits are. Those hits can spawn
 * more sized projectiles, so the tick walks a snapshot. Ball of Lightning damages by its own synced radius, so its radius is scaled instead.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class ProjectileSizing {
    private static final Map<Projectile, Tracked> ENLARGED = new WeakHashMap<>();

    private ProjectileSizing() {
    }

    public static void apply(Projectile projectile, float scale) {
        if (projectile instanceof LightningOrbEntity orb) {
            orb.setRadius(orb.getRadius() * scale);
            return;
        }
        projectile.refreshDimensions();
        if (scale > 1.0F) {
            ENLARGED.put(projectile, new Tracked());
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
        for (Map.Entry<Projectile, Tracked> entry : new ArrayList<>(ENLARGED.entrySet())) {
            Projectile projectile = entry.getKey();
            Tracked tracked = entry.getValue();
            if (projectile.isRemoved()) {
                ENLARGED.remove(projectile);
                continue;
            }
            Vec3 last = tracked.last;
            tracked.last = projectile.position();
            if (projectile.getDeltaMovement().lengthSqr() > 1.0E-4) {
                hitTouchingEntities(projectile, tracked.hit, last);
            }
        }
    }

    /** An enlarged projectile's entities already hit and its position at the previous check (null before the first). */
    private static final class Tracked {
        private final Set<Entity> hit = new HashSet<>();
        @Nullable
        private Vec3 last;
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        ENLARGED.clear();
    }

    private static void hitTouchingEntities(Projectile projectile, Set<Entity> alreadyHit, @Nullable Vec3 last) {
        AABB box = projectile.getBoundingBox();
        AABB swept = last == null ? box : box.minmax(box.move(last.subtract(projectile.position())));
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
                    CastContext.runTask(CastContext.tagOf(projectile), SpellHits.Carrier.ENTITY, () -> access.woldsvaults$onHit(hit));
                } finally {
                    TickingEntity.pop();
                }
            }
        }
    }
}
