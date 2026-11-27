package xyz.iwolfking.woldsvaults.spellcraft.projectile;

import iskallia.vault.skill.ability.effect.ChainLightningAbility;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xyz.iwolfking.woldsvaults.mixins.accessors.ChainLightningProjectileAccessor;
import xyz.iwolfking.woldsvaults.spellcraft.ability.CastOrigin;
import xyz.iwolfking.woldsvaults.spellcraft.scope.CastModifiers;
import xyz.iwolfking.woldsvaults.spellcraft.scope.CastScope;
import xyz.iwolfking.woldsvaults.spellcraft.scope.ProjectileGlyphTags;
import xyz.iwolfking.woldsvaults.spellcraft.scope.TickingEntity;

/**
 * Tags the projectiles a spell step spawns with the step's glyphs and starts their speed, size, physics and steering. A
 * Bundled Spell payload's projectiles are relaunched from the landing point. Children a tagged projectile spawns during
 * its own tick inherit its tags.
 */
public final class ProjectileGlyphs {
    private ProjectileGlyphs() {
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
        if (parent instanceof Projectile parentProjectile && parent != projectile && ProjectileGlyphTags.isTagged(parent)
                && parentProjectile.getOwner() != null && projectile.getOwner() == parentProjectile.getOwner()
                && projectile.getOwner() instanceof ServerPlayer owner) {
            ProjectileGlyphTags.copy(parent, projectile);
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
        ProjectileGlyphTags.write(projectile, modifiers, scope.spec(), scope.shot());
        activate(projectile, scope.player());
    }

    private static void activate(Projectile projectile, ServerPlayer owner) {
        if (ProjectileGlyphTags.scale(projectile) != 1.0F) {
            ProjectileSizing.apply(projectile, ProjectileGlyphTags.scale(projectile));
        }
        if (ProjectileGlyphTags.gravity(projectile) > 0 || ProjectileGlyphTags.etherealSeconds(projectile) > 0) {
            ProjectilePhysics.track(projectile, ProjectileGlyphTags.gravity(projectile), ProjectileGlyphTags.etherealSeconds(projectile));
        }
        boolean orbiting = ProjectileGlyphTags.orbitSeconds(projectile) > 0
                && ProjectileSteering.orbit(projectile, owner, ProjectileGlyphTags.orbitSeconds(projectile));
        if (!orbiting && ProjectileGlyphTags.homingRange(projectile) > 0) {
            ProjectileSteering.home(projectile, owner, ProjectileGlyphTags.homingRange(projectile), ProjectileGlyphTags.homingTurn(projectile));
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
}
