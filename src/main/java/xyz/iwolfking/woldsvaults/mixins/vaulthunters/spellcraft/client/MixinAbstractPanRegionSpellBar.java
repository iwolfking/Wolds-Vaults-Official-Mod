package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft.client;

import iskallia.vault.client.gui.screen.player.legacy.tab.split.pan.AbilityPanRegion;
import iskallia.vault.client.gui.screen.player.legacy.tab.split.spi.AbstractPanRegion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.client.spellcraft.bar.SpellBarStrip;

import java.awt.Rectangle;

@Mixin(value = AbstractPanRegion.class, remap = false)
public class MixinAbstractPanRegionSpellBar {
    /** Ends the Abilities tree pan higher so the spell bar fits under it, leaving the Armory's pan subclass alone. */
    @Inject(method = "getBounds", at = @At("RETURN"), cancellable = true)
    private void woldsvaults$makeRoomForSpellBar(CallbackInfoReturnable<Rectangle> cir) {
        if (((Object) this).getClass() != AbilityPanRegion.class) {
            return;
        }
        Rectangle bounds = cir.getReturnValue();
        cir.setReturnValue(new Rectangle(bounds.x, bounds.y, bounds.width, bounds.height - SpellBarStrip.BAND));
    }
}
