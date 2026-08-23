package xyz.iwolfking.woldsvaults.mixins.vaulthunters.optimizations;

import iskallia.vault.event.SetupEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = SetupEvents.class, remap = false)
public class MixinSetupEvents {

    @Redirect(method = "setupCommon", at = @At(value = "INVOKE", target = "Liskallia/vault/init/ModConfigs;registerGen()V"))
    private static void ignoreSetupLoad() {}
}