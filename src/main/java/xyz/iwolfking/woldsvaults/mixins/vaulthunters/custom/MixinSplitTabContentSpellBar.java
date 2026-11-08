package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.client.gui.screen.player.legacy.SplitTabContent;
import iskallia.vault.client.gui.screen.player.legacy.tab.split.pan.AbilityPanRegion;
import iskallia.vault.client.gui.screen.player.legacy.tab.split.spi.AbstractPanRegion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xyz.iwolfking.woldsvaults.client.spellcraft.SpellBarStrip;

import java.awt.Rectangle;

@Mixin(value = SplitTabContent.class, remap = false)
public class MixinSplitTabContentSpellBar {
    @Shadow @Final protected AbstractPanRegion<?> panRegion;

    /** Ends the Abilities dialog as high as the pan, leaving the band under both to the spell bar. */
    @ModifyArg(method = "render", at = @At(value = "INVOKE",
            target = "Liskallia/vault/client/gui/screen/player/legacy/tab/split/spi/AbstractDialog;setBounds(Ljava/awt/Rectangle;)V"))
    private Rectangle woldsvaults$shrinkDialog(Rectangle bounds) {
        if (this.panRegion.getClass() != AbilityPanRegion.class) {
            return bounds;
        }
        return new Rectangle(bounds.x, bounds.y, bounds.width, bounds.height - SpellBarStrip.BAND);
    }
}
