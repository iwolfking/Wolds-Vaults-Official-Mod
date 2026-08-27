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
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.events.HyperVaultEvents;

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

        if (!Float.isFinite(healthReduction) && HyperVaultEvents.isInHyperVault(entity)) {
            WoldsVaults.LOGGER.error("HYPER NaN-guard: skipped a non-finite Bleed tick on {} (max health {}).",
                    entity.getType().getRegistryName(), entity.getMaxHealth());
            return;
        }

        entity.setHealth(entity.getHealth() - healthReduction);
        if (entity.isDeadOrDying()) {
            try {
                if(source instanceof Player player) {
                    entity.die(DamageSource.playerAttack(player));
                }
                else {
                    entity.die(DamageSource.MAGIC);
                }
            }
            catch (Exception e) {
                WoldsVaults.LOGGER.error("Bleed death handling threw for {} at {}; the entity is left at zero health with death processing incomplete.", entity.getType().getRegistryName(), entity.blockPosition(), e);
            }
        }
    }

    public static float applyScaledMaxHealthDamageBonus(LivingEntity target, float original, float maxHealthPercent) {
        maxHealthPercent *= getDamageReductionForType(target);

        float result = original + (target.getMaxHealth() * maxHealthPercent);
        if (!Float.isFinite(result) && HyperVaultEvents.isInHyperVault(target)) {
            WoldsVaults.LOGGER.error("HYPER NaN-guard: non-finite reaving bonus against {} dropped (max health {}).",
                    target.getType().getRegistryName(), target.getMaxHealth());
            return Float.isFinite(original) ? original : 0.0F;
        }

        return result;
    }
}
