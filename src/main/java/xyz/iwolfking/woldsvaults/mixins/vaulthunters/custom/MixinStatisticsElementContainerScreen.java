package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import iskallia.vault.client.gui.screen.player.StatisticsElementContainerScreen;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "ftbquests"),
        }
)
//TODO: Make sure this is pointing to right button press
@Mixin(value = StatisticsElementContainerScreen.class, remap = false)
public class MixinStatisticsElementContainerScreen {
    @Inject(method = "lambda$new$15", at = @At(value = "HEAD"), cancellable = true)
    private static void replaceQuestButton(CallbackInfoReturnable<Boolean> cir) {
        cir.cancel();
        ClientQuestFile.INSTANCE.openQuestGui();
    }
}
