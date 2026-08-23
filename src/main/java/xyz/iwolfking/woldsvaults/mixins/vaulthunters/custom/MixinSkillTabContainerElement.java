package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.client.atlas.TextureAtlasRegion;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.ContainerElement;
import iskallia.vault.client.gui.framework.element.TabElement;
import iskallia.vault.client.gui.framework.element.TextureAtlasElement;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.IPosition;
import iskallia.vault.client.gui.screen.player.element.SkillTabContainerElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.client.screens.gods.GodTreeScreen;
import xyz.iwolfking.woldsvaults.client.screens.gods.GodTreeTheme;
import xyz.iwolfking.woldsvaults.gods.network.ServerboundOpenGodTreeMessage;
import xyz.iwolfking.woldsvaults.client.screens.greed.GreedMilestonesScreen;
import xyz.iwolfking.woldsvaults.network.NetworkHandler;
import xyz.iwolfking.woldsvaults.milestones.network.ServerboundOpenMilestonesMessage;

import java.util.Arrays;

@Mixin(value = SkillTabContainerElement.class, remap = false)
public class MixinSkillTabContainerElement {
    /** Drops the trailing greed tree entry (index 6) from the player menu's hardcoded icon array. */
    @ModifyVariable(method = "<init>", at = @At("STORE"), name = "icons", remap = false)
    private TextureAtlasRegion[] removeGreedTreeTab(TextureAtlasRegion[] icons) {
        if (icons.length < 2 || icons[icons.length - 1] != ScreenTextures.TAB_ICON_GREED) {
            WoldsVaults.LOGGER.warn("Player menu tab strip does not end in the greed tab ({} tabs); leaving it untouched",
                    icons.length);
            return icons;
        }
        return Arrays.copyOf(icons, icons.length - 1);
    }

    /** Appends the greed tab in the vacated slot and the gods tab after it, on base's 31px tab pitch. */
    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void addGreedMilestonesTab(IPosition position, int selectedIndex, CallbackInfo ci) {
        this.woldsVaults$addTab(GreedMilestonesScreen.TAB_INDEX, selectedIndex, ScreenTextures.TAB_ICON_GREED,
                () -> NetworkHandler.INSTANCE.sendToServer(new ServerboundOpenMilestonesMessage()));
        this.woldsVaults$addTab(GodTreeScreen.TAB_INDEX, selectedIndex, GodTreeTheme.TAB_ICON_GODS,
                () -> NetworkHandler.INSTANCE.sendToServer(new ServerboundOpenGodTreeMessage()));
    }

    private void woldsVaults$addTab(int index, int selectedIndex, TextureAtlasRegion icon, Runnable open) {
        boolean selected = selectedIndex == index;
        TextureAtlasElement<?> background = selected
                ? new TextureAtlasElement<>(ScreenTextures.TAB_BACKGROUND_TOP_SELECTED)
                : new TextureAtlasElement<>(Spatials.positionY(4), ScreenTextures.TAB_BACKGROUND_TOP);
        TabElement<?> tab = new TabElement<>(Spatials.positionX(31 * index - 10), background,
                new TextureAtlasElement<>(Spatials.positionXYZ(6, 9, 1), icon),
                () -> {
                    if (!selected) {
                        open.run();
                    }
                });
        ((ContainerElement<?>) (Object) this).getElementStore().addElement(tab);
    }
}
