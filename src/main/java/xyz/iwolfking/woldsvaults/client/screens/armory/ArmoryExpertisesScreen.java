package xyz.iwolfking.woldsvaults.client.screens.armory;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.data.ClientExpertiseData;
import iskallia.vault.client.gui.component.ScrollableContainer;
import iskallia.vault.client.gui.screen.player.ExpertisesElementContainerScreen;
import iskallia.vault.client.gui.screen.player.legacy.SplitTabContent;
import iskallia.vault.client.gui.screen.player.legacy.TabContent;
import iskallia.vault.client.gui.screen.player.legacy.tab.split.dialog.ExpertiseDialog;
import iskallia.vault.client.gui.screen.player.legacy.tab.split.pan.ExpertisePanRegion;
import iskallia.vault.client.gui.screen.player.legacy.widget.ExpertiseWidget;
import iskallia.vault.container.NBTElementContainer;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.skill.base.TieredSkill;
import iskallia.vault.skill.tree.ExpertiseTree;
import iskallia.vault.util.MiscUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Read-only Expertises tab of an armory page: VH's expertise screen fed the saved tree, with no learn button. */
@OnlyIn(Dist.CLIENT)
public class ArmoryExpertisesScreen extends ExpertisesElementContainerScreen {
    public ArmoryExpertisesScreen(NBTElementContainer<ExpertiseTree> container, Inventory inventory, Component title) {
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
        this.view().renderPointOverlay(poseStack, this.width, "expertise point");
    }

    /** The viewer's live expertise tiers, by id (VH's client cache only exposes the learned nodes). */
    static Map<String, Integer> liveTiers() {
        Map<String, Integer> out = new HashMap<>();
        for (TieredSkill node : ClientExpertiseData.getLearnedTalentNodes()) {
            if (node.getId() != null) {
                out.put(node.getId(), node.getUnmodifiedTier());
            }
        }
        return out;
    }

    /** ExpertiseDialog with the learn button hidden and inactive, and the armory heading and Load line. */
    static class Dialog extends ExpertiseDialog {
        private final ArmoryTreeView view;

        Dialog(ExpertiseTree tree, ArmoryExpertisesScreen screen, ArmoryTreeView view) {
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
            WoldsVaults.LOGGER.warn("Armory: blocked an expertise learn from the read-only armory view");
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
                    this.view.savedText(saved, this.skillGroup.getMaxLearnableTier()), saved > 0, 5.0,
                    mouseX, mouseY, partialTicks);
        }

        private void renderArmoryDescription(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
            String id = this.skillGroup.getId();
            Component load = this.view.loadLine(liveTiers().getOrDefault(id, 0),
                    this.skillGroup.getUnmodifiedTier(), this.skillGroup.getMaxLearnableTier());
            ArmoryTreeView.renderDescription(poseStack, this.descriptionComponent, load, ModConfigs.SKILL_DESCRIPTIONS.getDescriptionFor(id));
        }
    }

    /** ExpertisePanRegion that also draws the +N / -N tags (saved tier minus the viewer's live tier). */
    static class PanRegion extends ExpertisePanRegion {
        private Map<String, Integer> live = Map.of();

        PanRegion(Dialog dialog, ArmoryExpertisesScreen screen) {
            super(dialog, screen);
        }

        @Override
        public void update() {
            super.update();
            this.live = liveTiers();
        }

        @Override
        public void renderTabForeground(PoseStack poseStack, Rectangle containerBounds, int mouseX, int mouseY, float partialTicks,
                                        List<Runnable> postContainerRender) {
            super.renderTabForeground(poseStack, containerBounds, mouseX, mouseY, partialTicks, postContainerRender);
            Point2D.Float midpoint = MiscUtils.getMidpoint(this.getBounds());
            poseStack.pushPose();
            poseStack.translate(midpoint.x, midpoint.y, 0.0);
            poseStack.scale(this.viewportScale, this.viewportScale, 1.0F);
            poseStack.translate(this.viewportTranslation.x, this.viewportTranslation.y, 0.0);
            for (Map.Entry<String, ExpertiseWidget> entry : this.skillWidgets.entrySet()) {
                ExpertiseWidget widget = entry.getValue();
                if (widget.getSkill() == null) {
                    continue;
                }
                float half = (float) (widget.getRenderWidth() + 8.0) / 2.0F;
                int delta = widget.getSkill().getUnmodifiedTier() - this.live.getOrDefault(entry.getKey(), 0);
                ArmoryTreeView.renderTag(poseStack, widget.x + half, widget.y - half, delta);
            }
            poseStack.popPose();
        }
    }
}
