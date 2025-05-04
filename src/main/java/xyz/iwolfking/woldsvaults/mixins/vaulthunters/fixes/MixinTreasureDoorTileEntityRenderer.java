package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.client.render.TreasureDoorTileEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TreasureDoorTileEntityRenderer.class, remap = false)
public class MixinTreasureDoorTileEntityRenderer {
    @Inject(method = "formatDoorTypeName", at = @At("HEAD"), cancellable = true)
    private void fixDisplayName(String doorType, CallbackInfoReturnable<String> cir){
        if (doorType.equals("ISKALLIUM")){
            cir.setReturnValue("WOLDIUM");
        }
    }
}
