package xyz.iwolfking.woldsvaults.augments.effect;

import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.entity.IPlayerAttackIgnore;
import iskallia.vault.entity.entity.PetEntity;
import iskallia.vault.event.ActiveFlags;
import iskallia.vault.gear.etching.EtchingGearAttributes;
import iskallia.vault.gear.etching.EtchingHelper;
import iskallia.vault.skill.ability.effect.ArcaneRailAbility;
import iskallia.vault.util.calc.AbilityPowerHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import xyz.iwolfking.woldsvaults.augments.AugmentAbility;
import xyz.iwolfking.woldsvaults.augments.cast.CastModifiers;

import java.util.Comparator;
import java.util.List;

/**
 * Size Up on Arcane Rail. VH fires the rail natively (mana, cooldown, its own thin hit); right after, this applies
 * VH's rail rules (ability power times a roll between its min and max percent, as many targets as the Rail Pierce
 * etching allows, nearest first, players only in PvP vaults) to every mob within the wider radius of the same line.
 * Mobs the native bolt just hit are immune for half a second, so they are skipped rather than hit twice.
 */
public final class RailBolts {
    private static final int JUST_HIT = 10;
    private static final double BASE_RADIUS = 0.3;

    private RailBolts() {
    }

    public static double extraRadius(CastModifiers modifiers) {
        return Math.max(0.0, modifiers.scale() - 1.0);
    }

    public static void widen(ServerPlayer player, AugmentAbility glyph, CastModifiers modifiers, Vec3 aim, double extraRadius, double range) {
        if (!(glyph.findAbility(player).orElse(null) instanceof ArcaneRailAbility rail)) {
            return;
        }
        Vec3 start = player.getEyePosition().add(0.0, -0.5, 0.0);
        Vec3 direction = aim.normalize();
        HitResult blockHit = player.level.clip(new ClipContext(start, start.add(direction.scale(range)),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        double length = blockHit.getType() == HitResult.Type.MISS ? range : blockHit.getLocation().distanceTo(start);
        float size = EtchingHelper.getEtchings(player, EtchingGearAttributes.ARCANE_RAIL_SIZE).stream().findFirst()
                .map(instance -> instance.getValue()).orElse(1.0F);
        double radius = BASE_RADIUS * Math.max(1.0F, size) + extraRadius;
        int pierce = EtchingHelper.getEtchings(player, EtchingGearAttributes.RAIL_PIERCE).stream().findFirst()
                .map(instance -> instance.getValue()).orElse(1);
        boolean pvp = VaultUtils.getVault(player.level).map(VaultUtils::isPvPVault).orElse(false);

        Vec3 end = start.add(direction.scale(length));
        List<LivingEntity> targets = player.level.getEntitiesOfClass(LivingEntity.class, new AABB(start, end).inflate(radius + 1.0),
                        e -> e != player && e.isPickable() && e.isAlive() && !(e instanceof PetEntity)
                                && !IPlayerAttackIgnore.isIgnoredByPlayerAttack(player, e)
                                && touches(e, start, direction, length, radius))
                .stream()
                .sorted(Comparator.comparingDouble(e -> along(e, start, direction)))
                .toList();
        int hits = 0;
        for (LivingEntity target : targets) {
            if (hits >= pierce || (target instanceof ServerPlayer && !pvp)) {
                break;
            }
            hits++;
            if (target.invulnerableTime > JUST_HIT) {
                continue;
            }
            float percent = Mth.lerp(player.getRandom().nextFloat(), rail.getPercentAbilityPowerDealtMin(),
                    rail.getPercentAbilityPowerDealtMax());
            float damage = AbilityPowerHelper.getAbilityPower(player) * percent * modifiers.totalDamage();
            ActiveFlags.IS_AP_ATTACKING.runIfNotSet(() -> ActiveFlags.IS_ARCANE_RAIL_ATTACKING
                    .runIfNotSet(() -> target.hurt(DamageSource.playerAttack(player), damage)));
        }
    }

    private static double along(Entity entity, Vec3 start, Vec3 direction) {
        return entity.getBoundingBox().getCenter().subtract(start).dot(direction);
    }

    private static boolean touches(Entity entity, Vec3 start, Vec3 direction, double length, double radius) {
        Vec3 center = entity.getBoundingBox().getCenter();
        double along = Mth.clamp(center.subtract(start).dot(direction), 0.0, length);
        return center.distanceTo(start.add(direction.scale(along))) <= radius + entity.getBbWidth() * 0.5;
    }
}
