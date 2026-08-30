package xyz.iwolfking.woldsvaults.api.util;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class MobEffectHelper {
    public static boolean hasNegativeEffect(LivingEntity entity) {
        if (entity == null) return false;

        for (MobEffectInstance instance : entity.getActiveEffects()) {
            if (instance.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
                return true;
            }
        }
        return false;
    }
}
