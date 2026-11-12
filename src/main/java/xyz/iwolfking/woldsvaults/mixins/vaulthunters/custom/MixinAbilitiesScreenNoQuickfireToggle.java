package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.client.VaultAbilityKeyBinding;
import iskallia.vault.client.gui.framework.element.spi.IElement;
import iskallia.vault.client.gui.screen.player.AbilitiesElementContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AbilitiesElementContainerScreen.class, remap = false)
public abstract class MixinAbilitiesScreenNoQuickfireToggle {

    /**
     * Drops the Abilities tab's "Toggle Quick Ability Keybind" tile and its backing panel: quick-fire keys are retired
     * (abilities are cast from spell slots), and the tile sat on the spell bar.
     */
    @Redirect(method = "<init>", at = @At(value = "INVOKE",
            target = "Liskallia/vault/client/gui/screen/player/AbilitiesElementContainerScreen;addElement(Liskallia/vault/client/gui/framework/element/spi/IElement;)Liskallia/vault/client/gui/framework/element/spi/IElement;"))
    private IElement woldsvaults$skipQuickfireToggle(AbilitiesElementContainerScreen screen, IElement element) {
        return element;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void woldsvaults$quickfireBindingOff(CallbackInfo ci) {
        VaultAbilityKeyBinding.isEnabled = false;
    }
}
