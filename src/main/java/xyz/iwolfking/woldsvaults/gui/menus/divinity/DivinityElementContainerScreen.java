package xyz.iwolfking.woldsvaults.gui.menus.divinity;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.framework.ScreenRenderers;
import iskallia.vault.client.gui.screen.player.SkillsElementContainerScreen;
import iskallia.vault.client.gui.screen.player.legacy.SplitTabContent;
import iskallia.vault.client.gui.screen.player.legacy.TabContent;
import iskallia.vault.container.NBTElementContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Inventory;
import xyz.iwolfking.woldsvaults.util.DivinityUtils;

public class DivinityElementContainerScreen extends SkillsElementContainerScreen<DivinityTree> {
    public static final int TAB_INDEX = 5;

    public DivinityElementContainerScreen(NBTElementContainer<DivinityTree> container, Inventory inventory, Component title) {
        super(container, inventory, title, ScreenRenderers.getImmediate());
    }

    public int getTabIndex() {
        return TAB_INDEX;
    }

    public MutableComponent getTabTitle() {
        return new TextComponent("Divinity");
    }

    public TabContent getTabContent() {
        DivinityDialog dialog = new DivinityDialog(this.getSkillTree(), this);
        DivinityPanRegion panningContent = new DivinityPanRegion(dialog, this);
        return new SplitTabContent(this, dialog, panningContent);
    }

    //TODO:
    protected void renderPointOverlay(PoseStack matrixStack) {
        this.renderPointOverlay(matrixStack, DivinityUtils.unspentDivinityPoints, TextColor.fromRgb(16724414), " unspent divinity point" + (DivinityUtils.unspentDivinityPoints == 1 ? "" : "s"));
    }

}
