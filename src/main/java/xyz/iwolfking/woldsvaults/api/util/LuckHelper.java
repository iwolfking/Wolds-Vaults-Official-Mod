package xyz.iwolfking.woldsvaults.api.util;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public class LuckHelper {
    public static float getLuckAffectedChance(float originalValue, LivingEntity target) {
        if(!target.hasEffect(MobEffects.LUCK) || !target.hasEffect(MobEffects.UNLUCK)) {
            return originalValue;
        }

        int luckAmount = target.hasEffect(MobEffects.LUCK) ? target.getEffect(MobEffects.LUCK).getAmplifier() + 1 : 0;
        int unluckAmount = target.hasEffect(MobEffects.UNLUCK) ? target.getEffect(MobEffects.UNLUCK).getAmplifier() + 1 : 0;

        int totalLuck = luckAmount - unluckAmount;

        if(totalLuck > 0) {
            return (float) (originalValue * Math.pow(1.07, totalLuck));
        }
        else if(totalLuck < 0) {
            return (float) (originalValue * Math.pow(0.7, Math.abs(totalLuck)));
        }

        return originalValue;
    }
}
