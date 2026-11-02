package xyz.iwolfking.woldsvaults.client.screens.armory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.data.ClientPrestigePowersData;
import iskallia.vault.client.gui.component.ScrollableContainer;
import iskallia.vault.client.gui.screen.player.PrestigePowersElementContainerScreen;
import iskallia.vault.client.gui.screen.player.legacy.SplitTabContent;
import iskallia.vault.client.gui.screen.player.legacy.TabContent;
import iskallia.vault.client.gui.screen.player.legacy.tab.split.dialog.PrestigePowersDialog;
import iskallia.vault.client.gui.screen.player.legacy.tab.split.pan.PrestigePowersPanRegion;
import iskallia.vault.client.gui.screen.player.legacy.widget.PrestigeHeaderWidget;
import iskallia.vault.client.gui.screen.player.legacy.widget.PrestigePowerWidget;
import iskallia.vault.client.gui.screen.player.legacy.widget.TalentGroupWidget;
import iskallia.vault.client.gui.screen.player.legacy.widget.connect.ConnectorWidget;
import iskallia.vault.config.entry.PrestigeHeaderStyle;
import iskallia.vault.container.NBTElementContainer;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.skill.tree.PrestigeTree;
import iskallia.vault.util.MiscUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Read-only Prestige tab of an armory page: VH's prestige power screen fed the saved tree, with no learn button.
 * The pan region always draws the saved tree (VH hides it below level 100 or before the Herald).
 */
@OnlyIn(Dist.CLIENT)
public class ArmoryPrestigeScreen extends PrestigePowersElementContainerScreen {
    public ArmoryPrestigeScreen(NBTElementContainer<PrestigeTree> container, Inventory inventory, Component title) {
        super(container, inventory, title);
        this.view().installChrome(this.elementStore, this::removeElement, this::addElement, this::getTabContentSpatial, element -> false);
    }

    private ArmoryTreeView view() {
        return ArmoryTreeView.of(this.menu);
    }

    @Override
    public MutableComponent getTabTitle() {
        return this.view().title();
    }

    @Override
    public TabContent getTabContent() {
        ArmoryTreeView view = this.view();
        Dialog dialog = new Dialog(this.getSkillTree(), this, view);
        return new SplitTabContent(this, dialog, new PanRegion(dialog, this));
    }

    @Override
    protected void renderPointOverlay(PoseStack poseStack) {
        this.view().renderPointOverlay(poseStack, this.width, "power");
    }

    /** PrestigePowersDialog with the learn button hidden and inactive, and the armory heading and Load line. */
    static class Dialog extends PrestigePowersDialog {
        private final ArmoryTreeView view;

        Dialog(PrestigeTree tree, ArmoryPrestigeScreen screen, ArmoryTreeView view) {
            super(tree, screen);
            this.view = view;
        }

        @Override
        public void update() {
            super.update();
            if (this.skillGroup == null) {
                return;
            }
            if (this.learnButton != null) {
                this.learnButton.active = false;
                this.learnButton.visible = false;
            }
            this.regretButton = null;
            this.descriptionComponent = new ScrollableContainer(this::renderArmoryDescription);
        }

        @Override
        protected void sendUpgradeMessage() {
            WoldsVaults.LOGGER.warn("Armory: blocked a prestige power learn from the read-only armory view");
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return false;
        }

        @Override
        protected void renderFooter(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        }

        @Override
        public Rectangle getDescriptionsBounds() {
            return ArmoryTreeView.descriptionBounds(this.getHeadingBounds(), this.bounds);
        }

        @Override
        protected void renderHeading(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
            int saved = this.skillGroup.getUnmodifiedTier();
            ArmoryTreeView.renderSkillHeading(this, poseStack, this.getHeadingBounds(), this.skillWidget,
                    this.getStyles().get(this.skillGroup.getId()), this.skillGroup.getName(),
                    this.view.savedText(saved, this.skillGroup.getMaxLearnableTier()), saved > 0, 10.0,
                    mouseX, mouseY, partialTicks);
        }

        private void renderArmoryDescription(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
            String id = this.skillGroup.getId();
            Component load = this.view.loadLine(ArmoryTreeView.tier(ClientPrestigePowersData.getTree(), id),
                    this.skillGroup.getUnmodifiedTier(), this.skillGroup.getMaxLearnableTier());
            ArmoryTreeView.renderDescription(poseStack, this.descriptionComponent, load, ModConfigs.SKILL_DESCRIPTIONS.getDescriptionFor(id));
        }
    }

    /**
     * PrestigePowersPanRegion that always builds and draws the saved tree (no level 100 / Herald gate, no lock
     * overlay) and draws the +N / -N tags.
     */
    static class PanRegion extends PrestigePowersPanRegion {
        private final List<PrestigeHeaderWidget> headers = new ArrayList<>();
        private Map<String, Integer> live = Map.of();

        PanRegion(Dialog dialog, ArmoryPrestigeScreen screen) {
            super(dialog, screen);
        }

        @Override
        public void update() {
            this.skillWidgets.clear();
            this.groupWidgets.clear();
            this.skillConnectors.clear();
            this.headers.clear();
            PrestigeTree tree = this.parentScreen.getSkillTree();
            this.getStyles().forEach((id, style) -> this.initSkillWidget(tree, id, style, this.skillWidgets, this.groupWidgets));
            for (PrestigeHeaderStyle header : ModConfigs.PRESTIGE_POWERS_GUI.getHeaders()) {
                this.headers.add(new PrestigeHeaderWidget(header));
            }
            this.live = ArmoryTreeView.tiers(ClientPrestigePowersData.getTree());
            this.loadViewportTransforms(false);
        }

        @Override
        public void renderTabForeground(PoseStack poseStack, Rectangle containerBounds, int mouseX, int mouseY, float partialTicks,
                                        List<Runnable> postContainerRender) {
            RenderSystem.enableBlend();
            Point2D.Float midpoint = MiscUtils.getMidpoint(this.getBounds());
            poseStack.pushPose();
            poseStack.translate(midpoint.x, midpoint.y, 0.0);
            poseStack.scale(this.viewportScale, this.viewportScale, 1.0F);
            poseStack.translate(this.viewportTranslation.x, this.viewportTranslation.y, 0.0);
            int containerMouseX = (int) ((mouseX - midpoint.x) / this.viewportScale - this.viewportTranslation.x);
            int containerMouseY = (int) ((mouseY - midpoint.y) / this.viewportScale - this.viewportTranslation.y);
            for (PrestigeHeaderWidget header : this.headers) {
                header.render(poseStack);
            }
            for (TalentGroupWidget group : this.groupWidgets) {
                group.renderWidget(poseStack, containerBounds, mouseX, mouseY, containerMouseX, containerMouseY, partialTicks, postContainerRender);
            }
            for (ConnectorWidget connector : this.skillConnectors) {
                connector.renderConnection(poseStack);
            }
            for (PrestigePowerWidget widget : this.skillWidgets.values()) {
                widget.renderWidget(poseStack, containerBounds, mouseX, mouseY, containerMouseX, containerMouseY, partialTicks, postContainerRender);
            }
            for (Map.Entry<String, PrestigePowerWidget> entry : this.skillWidgets.entrySet()) {
                PrestigePowerWidget widget = entry.getValue();
                float half = (float) (widget.getRenderWidth() + 8.0) / 2.0F;
                int delta = widget.getSkill().getUnmodifiedTier() - this.live.getOrDefault(entry.getKey(), 0);
                ArmoryTreeView.renderTag(poseStack, widget.x + half, widget.y - half, delta);
            }
            poseStack.popPose();
        }
    }
}
