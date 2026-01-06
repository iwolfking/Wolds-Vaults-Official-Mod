package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.core.data.key.registry.SupplierRegistry;
import iskallia.vault.core.vault.VaultRegistry;
import iskallia.vault.core.vault.time.modifier.ClockModifier;
import iskallia.vault.core.world.generator.layout.GridLayout;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.modifiers.clock.KillMobTimeExtension;

@Mixin(value = VaultRegistry.class, remap = false)
public class MixinVaultRegistry {
    @Shadow
    @Final
    public static SupplierRegistry<ClockModifier> CLOCK_MODIFIER;

    @Shadow
    @Final
    public static SupplierRegistry<GridLayout> GRID_LAYOUT;

    @Inject(method = "<clinit>", at = @At(value = "TAIL"))
    private static void injectRegistries(CallbackInfo ci) {
        CLOCK_MODIFIER.add(KillMobTimeExtension.KEY);
    }
}