package xyz.iwolfking.woldsvaults.client.screens.gods;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.component.ScrollableContainer;
import iskallia.vault.client.gui.helper.FontHelper;
import iskallia.vault.client.gui.helper.Renderable;
import iskallia.vault.client.gui.helper.UIHelper;
import iskallia.vault.client.gui.screen.player.legacy.tab.split.spi.AbstractDialog;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.FormattedCharSequence;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.client.screens.greed.GreedTheme;
import xyz.iwolfking.woldsvaults.gods.ActiveGodResolver;
import xyz.iwolfking.woldsvaults.gods.ClientGodAlignmentData;
import xyz.iwolfking.woldsvaults.gods.ClientGodNodePreviews;
import xyz.iwolfking.woldsvaults.gods.GodLevels;
import xyz.iwolfking.woldsvaults.gods.node.GodNodePreviews;
import xyz.iwolfking.woldsvaults.network.NetworkHandler;
import xyz.iwolfking.woldsvaults.gods.network.ServerboundUnlockGodNodeMessage;
import iskallia.vault.config.entry.SkillStyle;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.gods.node.GodNode;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeRegistry;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeType;
import xyz.iwolfking.woldsvaults.gods.node.GodTreeModel;

import java.awt.Rectangle;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * The right-hand pane of the gods tab: the selected node's name, standing, description and unlock
 * button, or the god's summary when nothing is selected. Descriptions come from
 * {@code ModConfigs.SKILL_DESCRIPTIONS}, keyed by effect id.
 */
public class GodTreeDialog extends AbstractDialog<GodTreeScreen> {
    private static final int COLOR_MUTED = 0xC4C4C4;
    private static final int TEXT_PADDING = 10;
    private static final int LINE_HEIGHT = 10;
    private static final int TEXT_COLOR = 0xFF192022;
    private static final Set<String> WARNED_FORMULAS = new HashSet<>();

    private VaultGod god;
    private String selectedNodeId;
    private GodNode selectedNode;
    private MutableComponent descriptionContentComponent;
    private List<FormattedCharSequence> renderedLines = List.of();

    private static final class DescriptionContainer extends ScrollableContainer {
        DescriptionContainer(Renderable renderer) {
            super(renderer);
        }

        int scrollOffset() {
            return this.yOffset;
        }

        boolean hasBounds() {
            return this.bounds != null;
        }
    }

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
            this.descriptionComponent = new DescriptionContainer(this::renderDescriptions);
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
        this.descriptionComponent = new DescriptionContainer(this::renderDescriptions);
        this.descriptionContentComponent = this.buildNodeDescription(unlocked);
    }

    /** Rebuilds the selected node's text in place - after a preview answer - without resetting the scroll. */
    public void refreshPreview() {
        if (this.selectedNode != null) {
            this.descriptionContentComponent = this.buildNodeDescription(
                    ClientGodAlignmentData.isTreeNodePurchased(this.god, this.selectedNodeId));
        }
    }

    /** The effect id the dialog is showing, for the screen's preview polling, or null for the god summary. */
    @Nullable
    public String selectedEffectId() {
        return this.selectedNode == null ? null : this.selectedNode.ledgerKey();
    }

    private MutableComponent buildNodeDescription(boolean unlocked) {
        MutableComponent description = this.withPreview(
                ModConfigs.SKILL_DESCRIPTIONS.getDescriptionFor(this.selectedNode.ledgerKey()).copy());
        GodNodeType type = this.selectedNode.type();
        int transferSlot = this.transferSlotOf(this.selectedNode);
        if (this.selectedNode.enabled() && !this.isCharmActive()) {
            if (type == GodNodeType.MINOR && transferSlot >= 0) {
                description.append(new TextComponent("\n\nCarried by transfer slot "
                        + GreedTheme.roman(transferSlot + 1) + " - applies while " + this.god.getName()
                        + " is not your active god.")
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(GodTreeTheme.STATUS_GOOD & 0xFFFFFF))));
            } else if (type == GodNodeType.MINOR || type == GodNodeType.MAJOR) {
                description.append(new TextComponent("\n\nOnly functions while a "
                        + this.god.getName() + " charm is equipped.")
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFF7A6A))));
            } else if (type == GodNodeType.STAT && unlocked) {
                description.append(new TextComponent("\n\nCarrying over at 25% while "
                        + this.god.getName() + " is not your active god.")
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x999999))));
            }
        }
        if (type == GodNodeType.MINOR && unlocked && this.selectedNode.enabled()) {
            description.append(new TextComponent(transferSlot >= 0
                    ? "\n\nRight-click this star to move it to another transfer slot or take it out."
                    : "\n\nRight-click this star to put it in a transfer slot.")
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x999999))));
        }
        return description;
    }

    /** Swaps a live formula for the server's last resolved value, with the worked math as hover text. */
    private MutableComponent withPreview(MutableComponent description) {
        String effectId = this.selectedNode.ledgerKey();
        ClientGodNodePreviews.Preview preview = ClientGodNodePreviews.get(effectId).orElse(null);
        if (preview == null) {
            return description;
        }
        HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, GodNodePreviews.hoverText(preview.lines()));
        MutableComponent value = new TextComponent(GodNodePreviews.percentText(preview.multiplier()));
        MutableComponent result = description;
        boolean replaced = false;
        if (preview.formulaText().equals(description.getContents())) {
            result = value.setStyle(description.getStyle().withHoverEvent(hover));
            for (Component sibling : description.getSiblings()) {
                result.append(sibling);
            }
            replaced = true;
        } else {
            List<Component> siblings = description.getSiblings();
            for (int i = 0; i < siblings.size(); i++) {
                Component sibling = siblings.get(i);
                if (preview.formulaText().equals(sibling.getContents())) {
                    siblings.set(i, value.setStyle(sibling.getStyle().withHoverEvent(hover)));
                    replaced = true;
                    break;
                }
            }
        }
        if (!replaced) {
            if (WARNED_FORMULAS.add(effectId)) {
                WoldsVaults.LOGGER.warn("God node '{}' answered a live preview for formula '{}' but its description "
                        + "does not contain that text; showing the value after the description instead.",
                        effectId, preview.formulaText());
            }
            result.append(new TextComponent(" Currently ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xDDDDDD))))
                    .append(value.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(GodTreeTheme.accent(this.god) & 0xFFFFFF))
                            .withHoverEvent(hover)))
                    .append(new TextComponent(".").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xDDDDDD))));
        }
        result.append(new TextComponent(" (Hover the bonus percentage for math logic)")
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x999999))));
        return result;
    }

    /** The transfer slot carrying a node, or -1; only a learned minor in an unlocked slot counts. */
    private int transferSlotOf(GodNode node) {
        if (node.type() != GodNodeType.MINOR) {
            return -1;
        }
        int slot = ClientGodAlignmentData.findMinorTransferSlot(this.god, node.ledgerKey());
        return slot >= 0 && ClientGodAlignmentData.isMinorTransferLive(this.god, slot) ? slot : -1;
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
        Font font = Minecraft.getInstance().font;
        MutableComponent text = this.descriptionContentComponent;
        List<FormattedText> lines = UIHelper.getLines(ComponentUtils.mergeStyles(text.copy(), text.getStyle()),
                bounds.width - 3 * TEXT_PADDING);
        this.renderedLines = Language.getInstance().getVisualOrder(lines);
        for (int i = 0; i < this.renderedLines.size(); i++) {
            font.draw(matrixStack, this.renderedLines.get(i), TEXT_PADDING, LINE_HEIGHT * i + TEXT_PADDING, TEXT_COLOR);
        }
        this.descriptionComponent.setInnerHeight(this.renderedLines.size() * LINE_HEIGHT + 20);
        RenderSystem.enableDepthTest();
    }

    @Nullable
    public Style hoveredStyle(double mouseX, double mouseY) {
        if (this.bounds == null || !(this.descriptionComponent instanceof DescriptionContainer container)
                || !container.hasBounds()) {
            return null;
        }
        Rectangle renderable = container.getRenderableBounds();
        int originX = this.bounds.x + 5 + renderable.x + 1;
        int originY = this.bounds.y + 5 + renderable.y + 1;
        if (mouseX < originX || mouseY < originY || mouseX >= originX + renderable.width - 2
                || mouseY >= originY + renderable.height - 2) {
            return null;
        }
        int localX = (int) (mouseX - originX) - TEXT_PADDING;
        int localY = (int) (mouseY - originY) - TEXT_PADDING + container.scrollOffset();
        if (localX < 0 || localY < 0) {
            return null;
        }
        int line = localY / LINE_HEIGHT;
        if (line >= this.renderedLines.size()) {
            return null;
        }
        Style style = Minecraft.getInstance().font.getSplitter().componentStyleAtWidth(this.renderedLines.get(line), localX);
        return style != null && style.getHoverEvent() != null ? style : null;
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
            int transferSlot = this.transferSlotOf(this.selectedNode);
            if (!this.selectedNode.enabled()) {
                subText = "Coming Soon";
                subColor = COLOR_MUTED;
            } else if (unlocked && transferSlot >= 0) {
                subText = "Unlocked - Transfer slot " + GreedTheme.roman(transferSlot + 1);
                subColor = accent;
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
