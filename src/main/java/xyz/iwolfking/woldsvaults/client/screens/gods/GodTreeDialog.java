package xyz.iwolfking.woldsvaults.client.screens.gods;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.component.ScrollableContainer;
import iskallia.vault.client.gui.helper.FontHelper;
import iskallia.vault.client.gui.helper.UIHelper;
import iskallia.vault.client.gui.screen.player.legacy.tab.split.spi.AbstractDialog;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import xyz.iwolfking.woldsvaults.gods.ActiveGodResolver;
import xyz.iwolfking.woldsvaults.gods.ClientGodAlignmentData;
import xyz.iwolfking.woldsvaults.gods.GodLevels;
import xyz.iwolfking.woldsvaults.network.NetworkHandler;
import xyz.iwolfking.woldsvaults.gods.network.ServerboundUnlockGodNodeMessage;
import iskallia.vault.config.entry.SkillStyle;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.gods.node.GodNode;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeRegistry;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeType;
import xyz.iwolfking.woldsvaults.gods.node.GodTreeModel;

import java.awt.Rectangle;

/**
 * The right-hand pane of the gods tab. With a star selected it shows the node's name, its
 * standing, the shared skill description for its effect and the unlock button; with nothing
 * selected it shows the god's own summary - level, experience, points and how the tree works.
 * Descriptions come from {@code ModConfigs.SKILL_DESCRIPTIONS} keyed by the node's effect id
 * (its own id for start stars), the same pipeline every other skill screen reads.
 */
import javax.annotation.Nullable;

public class GodTreeDialog extends AbstractDialog<GodTreeScreen> {
    private static final int COLOR_MUTED = 0xC4C4C4;

    private VaultGod god;
    private String selectedNodeId;
    private GodNode selectedNode;
    private MutableComponent descriptionContentComponent;

    public GodTreeDialog(GodTreeScreen parentScreen, VaultGod god) {
        super(parentScreen);
        this.god = god;
    }

    public void setGod(VaultGod god) {
        this.god = god;
        this.selectedNodeId = null;
        this.update();
    }

    public void setSelectedNode(String nodeId) {
        this.selectedNodeId = nodeId;
        this.update();
    }

    /**
     * The selected node's icon, which lives with its position in the tree's style config rather
     * than on the node itself, or null for a node the styles do not give one.
     */
    @Nullable
    private ResourceLocation selectedIcon() {
        if (this.selectedNode == null) {
            return null;
        }
        SkillStyle style = GodNodeRegistry.tree(this.god).map(tree -> tree.getStyle(this.selectedNode.id())).orElse(null);
        return style == null ? null : style.icon;
    }

    @Override
    public void update() {
        GodTreeModel tree = GodNodeRegistry.tree(this.god).orElse(null);
        this.selectedNode = tree != null && this.selectedNodeId != null ? tree.getNode(this.selectedNodeId) : null;
        if (this.selectedNode == null) {
            this.selectedNodeId = null;
            this.learnButton = null;
            this.descriptionContentComponent = this.buildGodSummary();
            this.descriptionComponent = new ScrollableContainer(this::renderDescriptions);
            return;
        }
        boolean unlocked = ClientGodAlignmentData.isTreeNodePurchased(this.god, this.selectedNodeId);
        boolean reachable = tree.isPurchasable(this.selectedNodeId,
                id -> ClientGodAlignmentData.isTreeNodePurchased(this.god, id));
        boolean hasPoints = ClientGodAlignmentData.getUnspentPoints(this.god) > 0;
        String buttonText;
        boolean buttonActive;
        if (!this.selectedNode.enabled()) {
            buttonText = "Coming Soon";
            buttonActive = false;
        } else if (unlocked) {
            buttonText = "Unlocked";
            buttonActive = false;
        } else if (!reachable) {
            buttonText = "Locked";
            buttonActive = false;
        } else if (!hasPoints) {
            buttonText = "No God Points";
            buttonActive = false;
        } else {
            buttonText = "Unlock";
            buttonActive = true;
        }
        this.learnButton = new Button(0, 0, 0, 0, new TextComponent(buttonText), button -> this.unlockNode(), Button.NO_TOOLTIP);
        this.learnButton.active = buttonActive;
        this.descriptionComponent = new ScrollableContainer(this::renderDescriptions);
        this.descriptionContentComponent = ModConfigs.SKILL_DESCRIPTIONS.getDescriptionFor(this.selectedNode.ledgerKey()).copy();
        if (this.selectedNode.enabled() && !this.isCharmActive()) {
            GodNodeType type = this.selectedNode.type();
            if (type == GodNodeType.MINOR || type == GodNodeType.MAJOR) {
                this.descriptionContentComponent.append(new TextComponent("\n\nOnly functions while a "
                        + this.god.getName() + " charm is equipped.")
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFF7A6A))));
            } else if (type == GodNodeType.STAT && unlocked) {
                this.descriptionContentComponent.append(new TextComponent("\n\nCarrying over at 25% while "
                        + this.god.getName() + " is not your active god.")
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x999999))));
            }
        }
    }

    private MutableComponent buildGodSummary() {
        int level = ClientGodAlignmentData.getLevel(this.god);
        long xp = ClientGodAlignmentData.getXp(this.god);
        long nextXp = GodLevels.xpForLevel(level + 1);
        int unspent = ClientGodAlignmentData.getUnspentPoints(this.god);
        int accent = GodTreeTheme.accent(this.god) & 0xFFFFFF;
        MutableComponent desc = new TextComponent("");
        desc.append(new TextComponent(this.god.getName() + ", " + this.god.getTitle() + "\n\n")
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(accent))));
        desc.append(line("Level ", String.valueOf(level), accent));
        desc.append(line("Experience ", String.format("%,d / %,d", xp, nextXp), accent));
        desc.append(line("Unspent God Points ", String.valueOf(unspent), accent));
        if (this.isCharmActive()) {
            desc.append(new TextComponent("Active - " + this.god.getName() + " charm equipped\n")
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(accent))));
        } else {
            desc.append(new TextComponent("Not active - no " + this.god.getName() + " charm equipped\n")
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x999999))));
        }
        desc.append(new TextComponent("\n"));
        desc.append(new TextComponent("Complete god altars or run god maps to earn god experience. "
                + "Each level grants god points to spend on this constellation.\n\n")
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xDDDDDD))));
        desc.append(new TextComponent("Equipping a god's charm makes their constellation active: minor and "
                + "major stars only function while their god is active, and stat stars carry over to other "
                + "gods at 25%.\n\n")
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xDDDDDD))));
        desc.append(new TextComponent("Click a star to inspect it. Every star costs one point and "
                + "must connect to a star you already own, starting from the marked start stars.")
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x999999))));
        return desc;
    }

    /** Whether the viewed god's charm is currently equipped, resolved from the client player. */
    private boolean isCharmActive() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player != null && ActiveGodResolver.isActive(minecraft.player, this.god);
    }

    private static MutableComponent line(String label, String value, int accent) {
        return new TextComponent(label).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xDDDDDD)))
                .append(new TextComponent(value + "\n").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(accent))));
    }

    private void unlockNode() {
        if (this.selectedNodeId == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.playSound(ModSounds.SKILL_TREE_LEARN_SFX, 1.0F, 1.0F);
        }
        NetworkHandler.INSTANCE.sendToServer(new ServerboundUnlockGodNodeMessage(this.god, this.selectedNodeId));
    }

    private void renderDescriptions(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        Rectangle bounds = this.descriptionComponent.getRenderableBounds();
        int renderedLineCount = UIHelper.renderWrappedText(matrixStack, this.descriptionContentComponent, bounds.width, 10);
        this.descriptionComponent.setInnerHeight(renderedLineCount * 10 + 20);
        RenderSystem.enableDepthTest();
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (this.bounds == null) {
            return;
        }
        if (this.descriptionComponent == null) {
            this.update();
        }
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderBackground(matrixStack, mouseX, mouseY, partialTicks);
        matrixStack.pushPose();
        matrixStack.translate(this.bounds.x + 5, this.bounds.y + 5, 0.0D);
        this.renderHeading(matrixStack, mouseX, mouseY, partialTicks);
        this.descriptionComponent.setBounds(this.getDescriptionsBounds());
        this.descriptionComponent.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderFooter(matrixStack, mouseX, mouseY, partialTicks);
        matrixStack.popPose();
    }

    private void renderHeading(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        String heading;
        String subText;
        int headingColor;
        int subColor;
        int accent = GodTreeTheme.accent(this.god) & 0xFFFFFF;
        if (this.selectedNode == null) {
            heading = this.god.getName();
            subText = "Level " + ClientGodAlignmentData.getLevel(this.god);
            headingColor = accent;
            subColor = COLOR_MUTED;
        } else {
            boolean unlocked = ClientGodAlignmentData.isTreeNodePurchased(this.god, this.selectedNodeId);
            boolean functional = this.selectedNode.type() == GodNodeType.MINOR
                    || this.selectedNode.type() == GodNodeType.MAJOR;
            heading = this.selectedNode.name();
            headingColor = unlocked ? accent : 0xFFFFFF;
            if (!this.selectedNode.enabled()) {
                subText = "Coming Soon";
                subColor = COLOR_MUTED;
            } else if (unlocked && functional && !this.isCharmActive()) {
                subText = "Unlocked - Inactive";
                subColor = COLOR_MUTED;
            } else if (unlocked) {
                subText = "Unlocked";
                subColor = accent;
            } else if (GodNodeRegistry.tree(this.god).map(tree -> tree.isPurchasable(this.selectedNodeId,
                    id -> ClientGodAlignmentData.isTreeNodePurchased(this.god, id))).orElse(false)) {
                int cost = this.selectedNode.cost();
                subText = "Available - " + cost + (cost == 1 ? " God Point" : " God Points");
                subColor = COLOR_MUTED;
            } else {
                subText = "Locked";
                subColor = COLOR_MUTED;
            }
        }
        UIHelper.renderContainerBorder(this, matrixStack, this.getHeadingBounds(), 14, 44, 2, 2, 2, 2, 0xFF8B8B8B);
        matrixStack.pushPose();
        matrixStack.translate(10.0D, 0.0D, 0.0D);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        if (this.selectedNode == null) {
            GodTreeTheme.godIcon(this.god).blit(matrixStack, 0, 11, 0, 16, 16);
        } else if (this.selectedIcon() != null) {
            boolean unlocked = ClientGodAlignmentData.isTreeNodePurchased(this.god, this.selectedNodeId);
            RenderSystem.setShaderTexture(0, this.selectedIcon());
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, unlocked ? 1.0F : 0.6F);
            GuiComponent.blit(matrixStack, 0, 11, 0.0F, 0.0F, 16, 16, 16, 16);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        } else {
            GodTreeTheme.godIcon(this.god).blit(matrixStack, 0, 11, 0, 16, 16);
        }
        int textOffsetX = 22;
        matrixStack.pushPose();
        matrixStack.translate(textOffsetX, 13.0D, 0.0D);
        FontHelper.drawTextComponent(matrixStack,
                new TextComponent(heading).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(headingColor))), false);
        matrixStack.translate(0.0D, 10.0D, 0.0D);
        FontHelper.drawTextComponent(matrixStack,
                new TextComponent(subText).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(subColor))), false);
        matrixStack.popPose();
        matrixStack.popPose();
    }

    private void renderFooter(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (this.selectedNode != null && this.learnButton != null) {
            int containerX = mouseX - this.bounds.x - 5;
            int containerY = mouseY - this.bounds.y - 5;
            this.learnButton.render(matrixStack, containerX, containerY, partialTicks);
        }
    }

    public boolean mouseReleased(double screenX, double screenY, int button) {
        return false;
    }
}
