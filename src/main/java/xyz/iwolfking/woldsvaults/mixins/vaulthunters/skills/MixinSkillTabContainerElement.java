package xyz.iwolfking.woldsvaults.mixins.vaulthunters.skills;

import iskallia.vault.client.atlas.TextureAtlasRegion;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.overlay.VaultBarOverlay;
import iskallia.vault.client.gui.screen.player.element.SkillTabContainerElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.network.message.ServerboundOpenDivinityScreenMessage;

@Mixin(value = SkillTabContainerElement.class, remap = false)
public class MixinSkillTabContainerElement {
    @ModifyVariable(
            method = "<init>",
            at = @At(value = "STORE"),
            ordinal = 0
    )
    private TextureAtlasRegion[] modifyIcons(TextureAtlasRegion[] icons) {
        TextureAtlasRegion[] newIcons;
        if(VaultBarOverlay.vaultLevel == 100) { //TODO: another condition?
            newIcons = new TextureAtlasRegion[icons.length + 1];
            System.arraycopy(icons, 0, newIcons, 0, icons.length);
            newIcons[icons.length] = ScreenTextures.TAB_ICON_ARCHETYPES;
            return newIcons;
        } else {
            return icons;
        }
    }


    @Inject(method = "lambda$new$0(II)V", at = @At("TAIL"))
    private static void handleAdditionalIndex(int selectedIndex, int index, CallbackInfo ci) {
        if (selectedIndex != index) {
            if(index == 5) {
                ModNetwork.CHANNEL.sendToServer(ServerboundOpenDivinityScreenMessage.INSTANCE);
            }
        }
    }
}

