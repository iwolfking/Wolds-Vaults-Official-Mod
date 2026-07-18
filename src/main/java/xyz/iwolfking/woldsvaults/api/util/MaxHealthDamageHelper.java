package xyz.iwolfking.woldsvaults.api.util;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import gaia.entity.AbstractGaiaEntity;
import iskallia.vault.entity.boss.ArtifactBossEntity;
import iskallia.vault.entity.boss.TheVesselEntity;
import iskallia.vault.entity.champion.ChampionLogic;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class MaxHealthDamageHelper {

    public static float getDamageReductionForType(LivingEntity target) {
        if(target instanceof TheVesselEntity) {
            return 0.01F;
        }
        else if(target instanceof ArtifactBossEntity) {
            return 0.1F;
        }
        else if(VaultMobUtils.isSpecialMob(target)) {
            return 0.25F;
        }
        else if(ChampionLogic.isChampion(target) || InfernalMobsCore.getMobModifiers(target) != null) {
            return 0.5F;
        }
        else if(target instanceof AbstractGaiaEntity) {
            return 0.75F;
        }
        else {
            return 1.0F;
        }
    }


    public static void applyBleedDamage(MobEffectInstance instance, LivingEntity entity, Entity source) {
        if(entity.level.isClientSide || (source != null && source.level.isClientSide)) {
            return;
        }

        float maxHealthPercent = instance.getAmplifier() * 0.025F;

        maxHealthPercent *= getDamageReductionForType(entity);

        float healthReduction = Math.max((entity.getMaxHealth() * maxHealthPercent), instance.getAmplifier() + 1);

        entity.setHealth(entity.getHealth() - healthReduction);
        if (entity.isDeadOrDying()) {
            if(source instanceof Player player) {
                entity.die(DamageSource.playerAttack(player));
            }
            else {
                entity.die(DamageSource.MAGIC);
            }
        }
    }

    public static float applyScaledMaxHealthDamageBonus(LivingEntity target, float original, float maxHealthPercent) {
        maxHealthPercent *= getDamageReductionForType(target);

        original += (target.getMaxHealth() * maxHealthPercent);

        return original;
    }
}
