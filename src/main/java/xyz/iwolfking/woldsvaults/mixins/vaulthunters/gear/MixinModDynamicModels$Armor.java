package xyz.iwolfking.woldsvaults.mixins.vaulthunters.gear;

import iskallia.vault.dynamodel.registry.ArmorPieceModelRegistry;
import iskallia.vault.init.ModDynamicModels;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.models.AdditionalModels;

@Mixin(value = ModDynamicModels.Armor.class, remap = false)
public class MixinModDynamicModels$Armor {
    @Shadow
    @Final
    public static ArmorPieceModelRegistry PIECE_REGISTRY;

    @Inject(method = "<clinit>", at = @At(value = "TAIL"))
    private static void injectArmorModels(CallbackInfo ci) {
        PIECE_REGISTRY.registerAll(AdditionalModels.HEATWAVE);
        PIECE_REGISTRY.registerAll(AdditionalModels.PLAGUE);
        PIECE_REGISTRY.registerAll(AdditionalModels.PASTA);
    }
}
