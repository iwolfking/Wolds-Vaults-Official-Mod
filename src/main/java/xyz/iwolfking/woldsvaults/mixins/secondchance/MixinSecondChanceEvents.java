package xyz.iwolfking.woldsvaults.mixins.secondchance;

import iskallia.vault.core.vault.VaultUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import org.infernalstudios.secondchanceforge.SecondChanceEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.util.HealthReductionHelper;

@Mixin(value = SecondChanceEvents.class, remap = false)
public class MixinSecondChanceEvents {
    @Inject(method = "onEntityDamage", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/entity/living/LivingDamageEvent;setAmount(F)V", shift = At.Shift.AFTER))
    private void reducePlayerHealthOnProc(LivingDamageEvent event, CallbackInfo ci) {
        if(event.getEntity() instanceof ServerPlayer player && VaultUtils.getVault(event.getEntity().getLevel()).isPresent() && event.getEntity().isAlive()) {
            HealthReductionHelper.reducePlayerMaxHealth(player);
        }
    }
}
