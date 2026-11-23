package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.vault_forge;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(targets = "iskallia.vault.client.gui.screen.block.forge.ForgeLevelTrack", remap = false)
public class MixinForgeLevelTrack {

    @Unique
    private static final int WOLDSVAULTS$DELTA_Y = 24;

    @ModifyVariable(method = "draw", at = @At(value = "STORE"), name = "y")
    private float woldsVaults$shiftTrackY(float y) {
        return y == 126.0F ? y + WOLDSVAULTS$DELTA_Y : y;
    }

    @ModifyArg(
            method = "drawLabel",
            at = @At(value = "INVOKE", target = "Liskallia/vault/client/gui/screen/options/ui/OptionsCanvas;text(Ljava/lang/String;FFIZ)V"),
            index = 2
    )
    private float woldsVaults$shiftLevelLabelY(float y) {
        return y + WOLDSVAULTS$DELTA_Y;
    }

    @ModifyArg(
            method = "drawLabel",
            at = @At(value = "INVOKE", target = "Liskallia/vault/client/gui/screen/options/ui/OptionsCanvas;textRight(Ljava/lang/String;FFIZ)V"),
            index = 2
    )
    private float woldsVaults$shiftProficiencyLabelY(float y) {
        return y + WOLDSVAULTS$DELTA_Y;
    }
}