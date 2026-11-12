package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.config.sigil.SigilConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(value = SigilConfig.class, remap = false)
public class MixinSigilConfig {
    /**
     * A sigil carries nothing but its difficulty: every per-sigil config lookup (objective pools, elixir multiplier,
     * rune counts, crate tiers) reports no config so the objective falls back to its default, and
     * {@link xyz.iwolfking.woldsvaults.api.util.ObjectiveDifficulty} scales that default instead.
     */
    @Inject(method = "getConfig", at = @At("HEAD"), cancellable = true)
    private static void woldsvaults$sigilsCarryOnlyDifficulty(String id, CallbackInfoReturnable<Optional<SigilConfig>> cir) {
        cir.setReturnValue(Optional.empty());
    }
}
