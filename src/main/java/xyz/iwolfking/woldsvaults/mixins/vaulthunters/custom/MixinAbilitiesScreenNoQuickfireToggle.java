package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.client.gui.framework.element.spi.IElement;
import iskallia.vault.client.gui.screen.player.AbilitiesElementContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = AbilitiesElementContainerScreen.class, remap = false)
public abstract class MixinAbilitiesScreenNoQuickfireToggle {

    /**
     * Drops the Abilities tab's "Toggle Quick Ability Keybind" tile and its backing panel, which sat on the spell bar.
     * The spell bar turns hover-to-bind on in classic mode and off in slot mode instead.
     */
    @Redirect(method = "<init>", at = @At(value = "INVOKE",
            target = "Liskallia/vault/client/gui/screen/player/AbilitiesElementContainerScreen;addElement(Liskallia/vault/client/gui/framework/element/spi/IElement;)Liskallia/vault/client/gui/framework/element/spi/IElement;"))
    private IElement woldsvaults$skipQuickfireToggle(AbilitiesElementContainerScreen screen, IElement element) {
        return element;
    }
}
