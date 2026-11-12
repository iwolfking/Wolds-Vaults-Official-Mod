package xyz.iwolfking.woldsvaults.gods.trees.velara;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/** Sanitation: divides the duration of harmful effects applied to a player the aura covers. */
public final class VelaraSanitation {
    private VelaraSanitation() {
    }

    /** The instance to apply, or the original when uncovered, unharmful or too short to split. */
    public static MobEffectInstance shorten(LivingEntity entity, MobEffectInstance instance) {
        if (instance == null || !(entity instanceof Player) || entity.getLevel().isClientSide()) {
            return instance;
        }
        if (!VelaraAuras.isSanitized(entity.getUUID())) {
            return instance;
        }
        if (instance.getEffect().getCategory() != MobEffectCategory.HARMFUL) {
            return instance;
        }
        int duration = instance.getDuration();
        if (duration <= VelaraValues.sanitationDurationDivisor()) {
            return instance;
        }
        MobEffectInstance shortened = new MobEffectInstance(instance.getEffect(),
                duration / VelaraValues.sanitationDurationDivisor(), instance.getAmplifier(),
                instance.isAmbient(), instance.isVisible(), instance.showIcon());
        shortened.setCurativeItems(instance.getCurativeItems());
        return shortened;
    }
}
