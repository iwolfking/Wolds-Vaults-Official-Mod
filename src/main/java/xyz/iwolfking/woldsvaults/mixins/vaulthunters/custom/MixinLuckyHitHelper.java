package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.util.calc.LuckyHitHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LuckyHitHelper.class, remap = false)
public class MixinLuckyHitHelper {
    @Inject(method = "getLuckyHitChanceUnlimited", at = @At("TAIL"), cancellable = true)
    private static void unluckyReducesLuckyHit(LivingEntity entity, CallbackInfoReturnable<Float> cir) {
        if(entity.hasEffect(MobEffects.UNLUCK)) {
            MobEffectInstance effectInstance = entity.getEffect(MobEffects.UNLUCK);
            if(effectInstance != null) {
                cir.setReturnValue((float) (cir.getReturnValue() * Math.pow(0.7, effectInstance.getAmplifier() + 1)));
            }
        }
    }
}
