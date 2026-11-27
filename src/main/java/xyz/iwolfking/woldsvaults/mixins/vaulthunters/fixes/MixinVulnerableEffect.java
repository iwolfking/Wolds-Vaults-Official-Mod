package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import iskallia.vault.effect.VulnerableEffect;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.events.WoldActiveFlags;

@Mixin(value = VulnerableEffect.class, remap = false)
public class MixinVulnerableEffect {
    /**
     * Keeps proc fangs from re-applying Vulnerable, which the triggering hit already paid for.
     */
    @Inject(method = "on", at = @At("HEAD"), cancellable = true)
    private static void woldsVaults$skipProcFangs(LivingHurtEvent event, CallbackInfo ci) {
        if (WoldActiveFlags.IS_PROC_FANG_ATTACKING.isSet()) {
            ci.cancel();
        }
    }

    /**
     * Counts the effect level instead of the amplifier, so Vulnerable I gives +10% instead of nothing.
     */
    @ModifyExpressionValue(method = "on", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffectInstance;getAmplifier()I", remap = true))
    private static int woldsVaults$levelNotAmplifier(int amplifier) {
        return amplifier + 1;
    }
}
