package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.TabElement;
import iskallia.vault.client.gui.framework.element.TextureAtlasElement;
import iskallia.vault.client.gui.framework.render.spi.IElementRenderer;
import iskallia.vault.client.gui.framework.render.spi.ITooltipRendererFactory;
import iskallia.vault.client.gui.framework.screen.AbstractElementContainerScreen;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.screen.GreedTraderScreen;
import iskallia.vault.container.GreedTraderContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.client.screens.greed.GreedTraderAchievementsScreen;

@Mixin(value = GreedTraderScreen.class, remap = false)
public abstract class MixinGreedTraderScreenAchievements extends AbstractElementContainerScreen<GreedTraderContainer> {
    @Shadow
    private boolean suppressContainerClose;

    public MixinGreedTraderScreenAchievements(GreedTraderContainer container, Inventory inventory, Component title,
                                              IElementRenderer elementRenderer,
                                              ITooltipRendererFactory<AbstractElementContainerScreen<GreedTraderContainer>> tooltipRendererFactory) {
        super(container, inventory, title, elementRenderer, tooltipRendererFactory);
    }

    /** Appends an Achievements tab to the greed trader's tab strip; the screen it opens shares this container. */
    @Inject(method = "init", at = @At("TAIL"), remap = true)
    private void addAchievementsTab(CallbackInfo ci) {
        boolean selected = (Object) this instanceof GreedTraderAchievementsScreen;
        TextureAtlasElement<?> background = new TextureAtlasElement<>(selected
                ? ScreenTextures.TAB_BACKGROUND_LEFT_SELECTED
                : ScreenTextures.TAB_BACKGROUND_LEFT);
        TabElement<?> tab = new TabElement<>(Spatials.positionXY(selected ? -28 : -24, 95), background,
                new TextureAtlasElement<>(Spatials.positionXY(selected ? 8 : 4, 6), ScreenTextures.TAB_ICON_GREED),
                () -> {
                    if (!selected) {
                        this.openAchievements();
                    }
                });
        this.addElement(tab.layout(this.translateWorldSpatial()));
    }

    private void openAchievements() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        this.suppressContainerClose = true;
        minecraft.setScreen(new GreedTraderAchievementsScreen(this.getMenu(), minecraft.player.getInventory(), this.getTitle()));
    }
}
