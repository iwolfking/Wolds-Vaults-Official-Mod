package xyz.iwolfking.woldsvaults.augments.effect;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.event.common.PlayerStatEvent;
import iskallia.vault.util.calc.PlayerStat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.augments.cast.CastModifiers;
import xyz.iwolfking.woldsvaults.augments.cast.CastOrigin;
import xyz.iwolfking.woldsvaults.augments.cast.CastScope;

import javax.annotation.Nullable;

/**
 * Applies augments to what VH does during and after a cast. During a cast, Amplify Area and Quicken add to VH's own
 * area-of-effect and cooldown reduction stats, before VH clamps them to the player's caps. Projectiles the cast spawns
 * are tagged (and steered, resized, re-weighted); a tagged projectile's area reads and hits during its own tick count
 * as the cast's, and the children it spawns then inherit its tags. Damage is multiplied only for the cast itself, for
 * hits by a tagged projectile, and for hits made while one is ticking, so other damage of the same player is untouched.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class AugmentEvents {
    private static final Object LISTENER_OWNER = new Object();

    private AugmentEvents() {
    }

    /** Registers the VH PLAYER_STAT listener; called once from common setup. */
    public static void register() {
        CommonEvents.PLAYER_STAT.register(LISTENER_OWNER, AugmentEvents::onPlayerStat);
    }

    private static void onPlayerStat(PlayerStatEvent.Data data) {
        Entity entity = data.getEntity();
        if (entity == null) {
            return;
        }
        if (data.getStat() == PlayerStat.AREA_OF_EFFECT) {
            float bonus = scopeFor(entity) != null ? scopeFor(entity).areaBonus() : tickingProjectileOf(entity) != null
                    ? AugmentTags.area(tickingProjectileOf(entity)) : 0.0F;
            if (bonus > 0) {
                data.setValue(data.getValue() + bonus);
            }
        } else if (data.getStat() == PlayerStat.COOLDOWN_REDUCTION) {
            CastModifiers scope = scopeFor(entity);
            if (scope != null && scope.quickenBonus() > 0) {
                data.setValue(data.getValue() + scope.quickenBonus());
            }
        }
    }

    @Nullable
    private static CastModifiers scopeFor(Entity entity) {
        return CastScope.current().filter(scope -> scope.player() == entity).map(CastScope.Active::modifiers).orElse(null);
    }

    @Nullable
    private static Projectile tickingProjectileOf(Entity owner) {
        return TickingEntity.current() instanceof Projectile projectile && projectile.getOwner() == owner
                && AugmentTags.isTagged(projectile) ? projectile : null;
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinWorldEvent event) {
        if (event.getWorld().isClientSide() || !(event.getEntity() instanceof Projectile projectile)) {
            return;
        }
        CastScope.Active scope = CastScope.current().orElse(null);
        if (scope != null && projectile.getOwner() == scope.player()) {
            if (scope.origin() != null || scope.modifiers().affectsProjectiles()) {
                applyFromCast(projectile, scope);
            }
            return;
        }
        Entity parent = TickingEntity.current();
        if (parent instanceof Projectile parentProjectile && parent != projectile && AugmentTags.isTagged(parent)
                && parentProjectile.getOwner() != null && projectile.getOwner() == parentProjectile.getOwner()
                && projectile.getOwner() instanceof ServerPlayer owner) {
            AugmentTags.copy(parent, projectile);
            activate(projectile, owner);
        }
    }

    private static void applyFromCast(Projectile projectile, CastScope.Active scope) {
        CastModifiers modifiers = scope.modifiers();
        if (scope.origin() != null) {
            relaunchFrom(projectile, scope.origin());
        }
        if (modifiers.speedMultiplier() != 1.0F) {
            projectile.setDeltaMovement(projectile.getDeltaMovement().scale(modifiers.speedMultiplier()));
        }
        AugmentTags.write(projectile, modifiers);
        activate(projectile, scope.player());
    }

    private static void activate(Projectile projectile, ServerPlayer owner) {
        if (AugmentTags.scale(projectile) != 1.0F) {
            ProjectileSizing.apply(projectile, AugmentTags.scale(projectile));
        }
        if (AugmentTags.gravity(projectile) > 0 || AugmentTags.ethereal(projectile)) {
            ProjectilePhysics.track(projectile, AugmentTags.gravity(projectile), AugmentTags.ethereal(projectile));
        }
        if (AugmentTags.orbit(projectile)) {
            ProjectileSteering.orbit(projectile, owner);
        } else if (AugmentTags.homing(projectile)) {
            ProjectileSteering.home(projectile, owner);
        }
    }

    private static void relaunchFrom(Projectile projectile, CastOrigin origin) {
        double speed = projectile.getDeltaMovement().length();
        Vec3 direction = origin.direction().lengthSqr() > 1.0E-6
                ? origin.direction().normalize() : projectile.getDeltaMovement().normalize();
        projectile.setPos(origin.position());
        projectile.setDeltaMovement(direction.scale(speed));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        Projectile projectile = event.getProjectile();
        if (!projectile.level.isClientSide() && ProjectilePhysics.ignoresBlockHit(projectile, event.getRayTraceResult())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        Entity direct = event.getSource().getDirectEntity();
        Entity attacker = event.getSource().getEntity();
        float multiplier = 1.0F;
        if (direct instanceof Projectile && direct.getPersistentData().contains(AugmentTags.DAMAGE)) {
            multiplier = AugmentTags.damage(direct);
        } else if (attacker != null) {
            CastModifiers scope = scopeFor(attacker);
            if (scope != null) {
                multiplier = scope.totalDamage();
            } else {
                Projectile ticking = tickingProjectileOf(attacker);
                if (ticking != null) {
                    multiplier = AugmentTags.damage(ticking);
                }
            }
        }
        if (multiplier != 1.0F) {
            event.setAmount(event.getAmount() * multiplier);
        }
    }
}
