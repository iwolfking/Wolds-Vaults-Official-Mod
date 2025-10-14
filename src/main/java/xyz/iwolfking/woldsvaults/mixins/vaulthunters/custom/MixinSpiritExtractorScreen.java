package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.client.gui.screen.block.SpiritExtractorScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = SpiritExtractorScreen.class, remap = false)
public class MixinSpiritExtractorScreen {
    @Redirect(method = "getPurchaseButtonTooltipLines", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(II)I"))
    private int capSpiritCostLevelScaling(int a, int b) {
        if(b > 100) {
            return 100;
        }

        return Math.max(a, b);
    }
}
