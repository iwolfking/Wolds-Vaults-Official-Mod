package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.skill.ability.effect.RampageAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = RampageAbility.class, remap = false)
public class MixinRampageAbility {
    @ModifyConstant(method = "isDirectMeleeHit", constant = @Constant(floatValue = 1.0F, ordinal = 0))
    private static float adjustAttackScaleRequirementLuckyHit(float constant) {
        return 0.5F;
    }
}
