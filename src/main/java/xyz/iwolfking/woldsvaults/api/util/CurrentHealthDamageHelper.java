package xyz.iwolfking.woldsvaults.api.util;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import gaia.entity.AbstractGaiaEntity;
import iskallia.vault.entity.boss.ArtifactBossEntity;
import iskallia.vault.entity.boss.TheVesselEntity;
import iskallia.vault.entity.champion.ChampionLogic;
import net.minecraft.world.entity.LivingEntity;

public class CurrentHealthDamageHelper {

    public static float getDamageReductionForType(LivingEntity target) {
        if(target instanceof TheVesselEntity) {
            return 0.025F;
        }
        else if(target instanceof ArtifactBossEntity) {
            return 0.05F;
        }
        else if(VaultMobUtils.isSpecialMob(target)) {
            return 0.1F;
        }
        else if(ChampionLogic.isChampion(target) || InfernalMobsCore.getMobModifiers(target) != null) {
            return 0.25F;
        }
        else if(target instanceof AbstractGaiaEntity) {
            return 0.5F;
        }
        else {
            return 1.0F;
        }
    }


    public static float getCurrentHealthDamage(float damagePercentage, LivingEntity target) {
        return (damagePercentage * target.getHealth()) * getDamageReductionForType(target);
    }
}
