package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.skill.ability.effect.RampageAbility;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.events.WoldActiveFlags;

@Mixin(value = RampageAbility.class, remap = false)
public class MixinRampageAbility {
    @ModifyConstant(method = "isDirectMeleeHit", constant = @Constant(floatValue = 1.0F, ordinal = 0))
    private static float adjustAttackScaleRequirementLuckyHit(float constant) {
        return 0.5F;
    }

    @Inject(method = "isDirectMeleeHit", at = @At("HEAD"), cancellable = true)
    private static void woldsvaults$excludeScepterCasts(LivingHurtEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (WoldActiveFlags.IS_SCEPTER_CAST_ATTACKING.isSet()) {
            cir.setReturnValue(false);
        }
    }
}
