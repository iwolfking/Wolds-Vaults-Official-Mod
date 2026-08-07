package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.util.damage.PlayerDamageHelper;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.events.WoldActiveFlags;

@Mixin(value = PlayerDamageHelper.class, remap = false)
public class MixinPlayerDamageHelper {
    /**
     * @author PoorMansPhysicist
     * @reason keep proc fangs from re-applying player damage multipliers (Rampage etc.)
     * already contained in their stored damage
     */
    @Inject(method = "onPlayerDamage", at = @At("HEAD"), cancellable = true)
    private static void onPlayerDamage(LivingHurtEvent event, CallbackInfo ci) {
        if(WoldActiveFlags.IS_PROC_FANG_ATTACKING.isSet()) {
            ci.cancel();
        }
    }
}
