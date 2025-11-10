package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

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
public class MixinModDynamicModelsArmor {
    @Shadow
    @Final
    public static ArmorPieceModelRegistry PIECE_REGISTRY;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void injectNewArmorModels(CallbackInfo ci) {
    }
}
