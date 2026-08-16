package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import iskallia.vault.client.gui.screen.player.legacy.tab.split.dialog.PrestigePowersDialog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import xyz.iwolfking.woldsvaults.api.util.PrestigeRankDisplay;

@Mixin(value = PrestigePowersDialog.class, remap = false)
public class MixinPrestigePowersDialog {

    @ModifyConstant(
        method = "getAdditionalDescription",
        constant = @Constant(stringValue = PrestigeRankDisplay.LEGACY_DIALOG_LABEL)
    )
    private String useRankLabel(String constant) {
        return PrestigeRankDisplay.DIALOG_LABEL;
    }

    /**
     * The dialog prints the gate as a bare number per tier; this is the second String.valueOf(int)
     * in the method (knowledge cost first, level requirement third), confirmed against the shipped
     * the_vault jar's disassembly.
     */
    @ModifyExpressionValue(
        method = "getAdditionalDescription",
        at = @At(value = "INVOKE", target = "Ljava/lang/String;valueOf(I)Ljava/lang/String;", ordinal = 1)
    )
    private String useRankNameAsRequirement(String value) {
        return PrestigeRankDisplay.rewriteDialogRequirement(value);
    }
}
