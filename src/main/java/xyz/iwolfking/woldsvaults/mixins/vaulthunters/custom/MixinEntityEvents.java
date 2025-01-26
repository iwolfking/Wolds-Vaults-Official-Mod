package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.event.EntityEvents;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityEvents.class, remap = false)
public class MixinEntityEvents {
    @Inject(method = "entityDealCrit", at = @At("HEAD"), cancellable = true)
    private static void cancelCritWhenUnlucky(LivingHurtEvent event, CallbackInfo ci) {
        if(event.getEntity() != null && event.getEntity() instanceof LivingEntity entity) {
            if(entity.hasEffect(MobEffects.UNLUCK)) {
                ci.cancel();
            }
        }
    }
}
