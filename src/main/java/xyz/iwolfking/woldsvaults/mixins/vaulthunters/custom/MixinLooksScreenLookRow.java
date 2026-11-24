package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.client.gui.screen.player.looks.LooksCatalog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(targets = "iskallia.vault.client.gui.screen.player.looks.LooksScreen$LookRow", remap = false)
public abstract class MixinLooksScreenLookRow {

    @Shadow
    private LooksCatalog.Entry entry;

    @ModifyArg(
        method = "drawContent",
        at = @At(
            value = "INVOKE",
            target = "Liskallia/vault/client/gui/screen/options/ui/OptionsCanvas;textRight(Ljava/lang/String;FFIZ)V"
        ),
        index = 3
    )
    private int woldsVaults$fixLockedAuraStatusColor(int originalColor) {
        if (this.entry != null && !this.entry.unlocked() && this.entry.patronTier() == null) {
            return 16733525;
        }

        return originalColor;
    }
}