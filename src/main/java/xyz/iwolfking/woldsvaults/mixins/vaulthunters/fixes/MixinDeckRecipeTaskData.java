package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.world.data.DeckRecipeTaskData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DeckRecipeTaskData.class, remap = false)
public class MixinDeckRecipeTaskData {
    @Inject(method = "onConfigReload", at = @At("HEAD"), cancellable = true)
    private static void cancelThis(CallbackInfo ci) {
        ci.cancel();
        return;
    }
}
