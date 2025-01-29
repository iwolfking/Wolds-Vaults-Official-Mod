package xyz.iwolfking.woldsvaults.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.init.ModEffects;

import javax.annotation.Nullable;


@Mixin(LivingEntity.class)
abstract class MixinLivingEntity extends Entity {

    private MixinLivingEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Shadow public abstract boolean hasEffect(MobEffect pEffect);
    @Shadow @Nullable public abstract MobEffectInstance getEffect(MobEffect pEffect);
    @Shadow public abstract boolean shouldDiscardFriction();


    @Inject(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hasEffect(Lnet/minecraft/world/effect/MobEffect;)Z", ordinal = 2), cancellable = true)
    private void doLevitates(Vec3 pTravelVector, CallbackInfo ci, @Local(ordinal = 1) float f3, @Local(ordinal = 1) Vec3 vec35) {

        int levels = (this.hasEffect(ModEffects.LEVITATEII)
                    ? this.getEffect(ModEffects.LEVITATEII).getAmplifier()+1
                    : 0)
                   + (this.hasEffect(MobEffects.LEVITATION)
                    ? this.getEffect(MobEffects.LEVITATION).getAmplifier()+1
                    : 0);

        if(levels != 0) {
            double dY = vec35.y + (0.05 * (double)levels - vec35.y) * 0.2;

            if (this.shouldDiscardFriction()) {
                this.setDeltaMovement(vec35.x, dY, vec35.z);
            } else {
                this.setDeltaMovement(vec35.x * (double)f3, dY * (double)0.98F, vec35.z * (double)f3);
            }

            ci.cancel();
        }
    }
}
