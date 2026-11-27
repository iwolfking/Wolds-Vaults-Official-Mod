package xyz.iwolfking.woldsvaults.spellcraft.projectile;

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
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import xyz.iwolfking.woldsvaults.api.cast.CastCarrier;
import xyz.iwolfking.woldsvaults.api.cast.CastContext;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.api.lib.IWoldAbilityEnhancements;
import xyz.iwolfking.woldsvaults.api.util.ElementHelper;
import xyz.iwolfking.woldsvaults.mixins.accessors.AccessorProjectile;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.LightningOrbEntityAccessor;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.ToxicGrenadeEntityAccessor;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.VaultDecoyProjectileInvoker;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.VaultGrenadeAccessor;
import xyz.iwolfking.woldsvaults.spellcraft.scope.TickingEntity;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

final class ProjectileDetonation {
    private static final Set<Class<?>> UNDETONATED_WARNED = ConcurrentHashMap.newKeySet();

    private ProjectileDetonation() {
    }

    static EntityDataAccessor<Integer> fuseOf(Projectile projectile) {
        if (projectile instanceof VaultGrenade) {
            return VaultGrenadeAccessor.woldsvaults$duration();
        }
        if (projectile instanceof ToxicGrenadeEntity) {
            return ToxicGrenadeEntityAccessor.woldsvaults$duration();
        }
        return null;
    }

    /** Holds the projectile's own lifetime timers for {@code remaining} more ticks, one tick past the orbit's end. */
    static void holdTimers(Projectile projectile, int remaining) {
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
        } else if (projectile instanceof BundledSpellOrb bundle) {
            bundle.holdFor(remaining + 1);
        }
    }

    /** Detonates an orbiting projectile whose orbit ended, inside its cast's context, so its glyphs still apply. */
    static void detonate(Projectile projectile) {
        TickingEntity.push(projectile);
        try {
            CastContext.runTask(CastContext.tagOf(projectile), CastCarrier.ENTITY, () -> detonateByType(projectile));
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
        } else if (projectile instanceof BundledSpellOrb bundle) {
            bundle.landHere();
        } else if (!endsByRemoval(projectile) && UNDETONATED_WARNED.add(projectile.getClass())) {
            WoldLogger.error("Orbit: no detonation is known for projectile class {}; it is removed at its orbit end instead.",
                    projectile.getClass().getName());
        }
    }

    private static boolean endsByRemoval(Projectile projectile) {
        return projectile instanceof LightningOrbEntity || projectile instanceof VaultBlizzardShard
                || projectile instanceof VaultThrownJavelin || projectile instanceof VaultStormArrow
                || projectile instanceof ChainLightningAbility.ChainLightningProjectile
                || projectile instanceof NecromancyAbility.NecromancyBolt;
    }
}
