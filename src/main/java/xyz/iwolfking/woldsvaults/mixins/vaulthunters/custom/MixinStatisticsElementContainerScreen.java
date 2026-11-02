package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import iskallia.vault.client.gui.screen.player.StatisticsElementContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = StatisticsElementContainerScreen.class, remap = false)
public class MixinStatisticsElementContainerScreen {
    @Inject(method = "lambda$new$15", at = @At(value = "HEAD"), cancellable = true)
    private static void replaceQuestButton(CallbackInfo ci) {
        ci.cancel();
        ClientQuestFile.INSTANCE.openQuestGui();
    }
}
