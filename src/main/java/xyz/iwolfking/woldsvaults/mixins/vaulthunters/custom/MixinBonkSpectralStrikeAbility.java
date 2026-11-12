package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.skill.ability.effect.BonkSpectralStrikeAbility;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BonkSpectralStrikeAbility.class, remap = false)
public class MixinBonkSpectralStrikeAbility {
    /**
     * Disables the base Spectral Strike hit handler; {@code SpectralStrikeDamage} handles it in the
     * pre-multiplier hook instead.
     */
    @Inject(method = "on", at = @At("HEAD"), cancellable = true)
    private static void woldsvaults$moveSpectralStrike(LivingHurtEvent event, CallbackInfo ci) {
        ci.cancel();
    }
}
