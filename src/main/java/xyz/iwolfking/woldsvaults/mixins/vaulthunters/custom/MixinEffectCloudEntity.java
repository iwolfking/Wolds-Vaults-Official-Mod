package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.entity.entity.EffectCloudEntity;
import iskallia.vault.init.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.effect.mobeffects.BleedOverrideEffect;

import javax.annotation.Nullable;

@Mixin(value = EffectCloudEntity.class, remap = false)
public abstract class MixinEffectCloudEntity {

    @Shadow
    @Nullable
    public abstract LivingEntity getOwner();

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z", shift = At.Shift.BEFORE), remap = true)
    private void registerBleedSource(CallbackInfo ci, @Local(name = "effectinstance") MobEffectInstance effectinstance, @Local(name = "livingentity") LivingEntity livingentity) {
        if(effectinstance.getEffect() == ModEffects.BLEED) {
            BleedOverrideEffect.registerSource(livingentity, this.getOwner());
        }
    }
}
