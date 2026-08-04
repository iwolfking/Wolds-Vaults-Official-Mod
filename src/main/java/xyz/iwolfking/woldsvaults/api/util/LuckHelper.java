package xyz.iwolfking.woldsvaults.api.util;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import java.util.function.Function;

public class LuckHelper {
    public static float getLuckAffectedChance(float originalValue, LivingEntity target, Function<Integer, Double> unluckyFunction, Function<Integer, Double> luckyFunction) {
        if(!target.hasEffect(MobEffects.LUCK) && !target.hasEffect(MobEffects.UNLUCK)) {
            return originalValue;
        }

        int luckAmount = target.hasEffect(MobEffects.LUCK) ? target.getEffect(MobEffects.LUCK).getAmplifier() + 1 : 0;
        int unluckAmount = target.hasEffect(MobEffects.UNLUCK) ? target.getEffect(MobEffects.UNLUCK).getAmplifier() + 1 : 0;

        int totalLuck = luckAmount - unluckAmount;

        if(totalLuck > 0) {
            return (float) (originalValue * luckyFunction.apply(totalLuck));
        }
        else if(totalLuck < 0) {
            return (float) (originalValue * unluckyFunction.apply(Math.abs(totalLuck)));
        }

        return originalValue;
    }

    public static float getLuckAffectedChance(float originalValue, LivingEntity target) {
        return getLuckAffectedChance(originalValue, target, (i) -> Math.pow(0.7F, i), (integer -> Math.pow(1.15F, integer)));
    }

    public static float getLuckAffectedChanceInverse(float originalValue, LivingEntity target) {
        return getLuckAffectedChance(originalValue, target, (i) -> Math.pow(1.15F, i), (integer -> Math.pow(0.7F, integer)));
    }
}
