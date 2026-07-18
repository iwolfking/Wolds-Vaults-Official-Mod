package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.entity.entity.EffectCloudEntity;
import iskallia.vault.init.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.api.util.BleedSourceRegistry;

@Mixin(value = EffectCloudEntity.class, remap = false)
public abstract class MixinEffectCloudEntity {
    /**
     * Registers the cloud's owner as the Bleed source before the effect lands, so Bleed
     * applied by effect-cloud gear modifiers awards kill credit.
     */
    @WrapOperation(
        method = "tick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z"),
        remap = true
    )
    private boolean woldsvaults$registerBleedSource(LivingEntity target, MobEffectInstance effect, Operation<Boolean> original) {
        if (effect.getEffect() == ModEffects.BLEED) {
            BleedSourceRegistry.registerSource(target, ((EffectCloudEntity) (Object) this).getOwner());
        }
        return original.call(target, effect);
    }
}
