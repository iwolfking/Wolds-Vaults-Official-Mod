package xyz.iwolfking.woldsvaults.augments.effect;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.event.common.PlayerStatEvent;
import iskallia.vault.skill.ability.effect.ChainLightningAbility;
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
import xyz.iwolfking.woldsvaults.mixins.accessors.ChainLightningProjectileAccessor;

import javax.annotation.Nullable;

/**
 * Applies glyphs to what VH does during and after a cast. During a cast, Amplify Area and Strengthen's area penalty
 * multiply VH's area-of-effect multiplier ({@code 1 + AoE}, the value VH's cast-time area helper passes) before VH clamps it
 * to the player's limit (spell haste is applied by the bar). Projectiles the cast spawns
 * are tagged (and steered, resized, re-weighted); a tagged projectile's area reads and hits during its own tick count
 * as the cast's, and the children it spawns then inherit its tags. Damage is multiplied only for the ability's own hits
 * ({@link SpellHits}: its damage source and VH attack flags) made during the cast, by a tagged projectile, or while one
 * is ticking, so procs, lucky-hit extras and other damage of the same player are untouched.
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
            CastModifiers scope = scopeFor(entity);
            Projectile ticking = scope == null ? tickingProjectileOf(entity) : null;
            float factor = scope != null ? scope.area() : ticking != null ? AugmentTags.area(ticking) : 1.0F;
            if (factor != 1.0F) {
                data.setValue(data.getValue() * factor);
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
            if (scope.origin() != null || scope.modifiers().affectsProjectiles() || scope.shot() != null) {
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
        if (modifiers.speed() != 1.0F) {
            projectile.setDeltaMovement(projectile.getDeltaMovement().scale(modifiers.speed()));
            projectile.hurtMarked = true;
        }
        AugmentTags.write(projectile, modifiers, scope.spec(), scope.shot());
        activate(projectile, scope.player());
    }

    private static void activate(Projectile projectile, ServerPlayer owner) {
        if (AugmentTags.scale(projectile) != 1.0F) {
            ProjectileSizing.apply(projectile, AugmentTags.scale(projectile));
        }
        if (AugmentTags.gravity(projectile) > 0 || AugmentTags.etherealSeconds(projectile) > 0) {
            ProjectilePhysics.track(projectile, AugmentTags.gravity(projectile), AugmentTags.etherealSeconds(projectile));
        }
        if (AugmentTags.orbitSeconds(projectile) > 0) {
            ProjectileSteering.orbit(projectile, owner, AugmentTags.orbitSeconds(projectile));
        } else if (AugmentTags.homingRange(projectile) > 0) {
            ProjectileSteering.home(projectile, owner, AugmentTags.homingRange(projectile), AugmentTags.homingTurn(projectile));
        }
    }

    private static void relaunchFrom(Projectile projectile, CastOrigin origin) {
        double speed = projectile.getDeltaMovement().length();
        Vec3 direction = origin.direction().lengthSqr() > 1.0E-6
                ? origin.direction().normalize() : projectile.getDeltaMovement().normalize();
        projectile.setPos(origin.position());
        projectile.setDeltaMovement(direction.scale(speed));
        if (projectile instanceof ChainLightningAbility.ChainLightningProjectile lightning) {
            restartRangeCheck(lightning, origin.position());
        }
    }

    /** Chain Lightning discards its bolt by distance from where it spawned; a relaunched bolt measures from its new start. */
    private static void restartRangeCheck(ChainLightningAbility.ChainLightningProjectile lightning, Vec3 start) {
        lightning.getEntityData().set(ChainLightningProjectileAccessor.woldsvaults$startX(), (float) start.x);
        lightning.getEntityData().set(ChainLightningProjectileAccessor.woldsvaults$startY(), (float) start.y);
        lightning.getEntityData().set(ChainLightningProjectileAccessor.woldsvaults$startZ(), (float) start.z);
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
        String spec = null;
        if (direct instanceof Projectile && direct.getPersistentData().contains(AugmentTags.DAMAGE)) {
            multiplier = AugmentTags.damage(direct);
            spec = AugmentTags.spec(direct);
        } else if (attacker != null) {
            CastScope.Active scope = CastScope.current().filter(active -> active.player() == attacker).orElse(null);
            if (scope != null) {
                multiplier = scope.modifiers().damage();
                spec = scope.spec();
            } else {
                Projectile ticking = tickingProjectileOf(attacker);
                if (ticking != null) {
                    multiplier = AugmentTags.damage(ticking);
                    spec = AugmentTags.spec(ticking);
                }
            }
        }
        if (multiplier != 1.0F && SpellHits.isOwnHit(spec, event.getSource())) {
            event.setAmount(event.getAmount() * multiplier);
        }
    }
}
