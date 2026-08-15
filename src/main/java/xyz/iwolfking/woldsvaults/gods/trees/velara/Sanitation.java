package xyz.iwolfking.woldsvaults.gods.trees.velara;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Sanitation: harmful effects applied to the player or to allies within 50 blocks last a third as
 * long. The aura membership is resolved once a second by {@link VelaraTicker}; this class only
 * answers "is this instance shortened, and to what".
 *
 * <p>Shortening happens at {@code LivingEntity.addEffect} rather than on the base mod's effect
 * check event, because that event can only deny an effect outright. Granted effects that the
 * snapshot re-applies every tick are unaffected, which is correct -  those are not debuffs being
 * inflicted.
 */
public final class Sanitation {
    private Sanitation() {
    }

    /**
     * The instance that should actually be applied. Returns the original when the entity is not
     * covered, when the effect is not harmful, or when the duration is already too short to split.
     */
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
        if (duration <= VelaraValues.SANITATION_DURATION_DIVISOR) {
            return instance;
        }
        MobEffectInstance shortened = new MobEffectInstance(instance.getEffect(),
                duration / VelaraValues.SANITATION_DURATION_DIVISOR, instance.getAmplifier(),
                instance.isAmbient(), instance.isVisible(), instance.showIcon());
        shortened.setCurativeItems(instance.getCurativeItems());
        return shortened;
    }
}
