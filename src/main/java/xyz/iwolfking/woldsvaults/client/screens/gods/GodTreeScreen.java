package xyz.iwolfking.woldsvaults.client.screens.gods;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.framework.ScreenRenderers;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.helper.FontHelper;
import iskallia.vault.client.gui.screen.player.AbstractSkillTabElementContainerScreen;
import iskallia.vault.client.gui.screen.player.legacy.ILegacySkillTreeScreen;
import iskallia.vault.client.gui.screen.player.legacy.TabContent;
import iskallia.vault.client.render.TextureRegion;
import iskallia.vault.client.render.TextureRegionRenderer;
import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import xyz.iwolfking.woldsvaults.gods.ActiveGodResolver;
import xyz.iwolfking.woldsvaults.gods.ClientGodAlignmentData;
import xyz.iwolfking.woldsvaults.gods.GodLevels;
import xyz.iwolfking.woldsvaults.gods.container.GodTreeContainer;

import javax.annotation.Nonnull;
import java.awt.Rectangle;
import java.util.List;

/**
 * The gods tab of the player menu: the constellation star chart on the left, the node dialog on
 * the right, a god experience bar centered over the sky, the unspent god points readout in the
 * god's color, and one selector tab per god along the chart's right edge. The layout and input
 * routing deliberately mirror the retired greed tree screen, which this tab succeeds.
 */
public class GodTreeScreen extends AbstractSkillTabElementContainerScreen<GodTreeContainer>
        implements ILegacySkillTreeScreen {
    public static final int TAB_INDEX = 7;

    private static final TextureRegion CONTAINER_BORDER_CORNER_TOP_LEFT = new TextureRegion(0, 0, 15, 24);
    private static final TextureRegion CONTAINER_BORDER_CORNER_TOP_RIGHT = new TextureRegion(18, 0, 15, 24);
    private static final TextureRegion CONTAINER_BORDER_CORNER_BOTTOM_LEFT = new TextureRegion(0, 27, 15, 16);
    private static final TextureRegion CONTAINER_BORDER_CORNER_BOTTOM_RIGHT = new TextureRegion(18, 27, 15, 16);
    private static final TextureRegion CONTAINER_BORDER_TOP = new TextureRegion(16, 0, 1, 24);
    private static final TextureRegion CONTAINER_BORDER_BOTTOM = new TextureRegion(16, 27, 1, 16);
    private static final TextureRegion CONTAINER_BORDER_LEFT = new TextureRegion(0, 25, 15, 1);
    private static final TextureRegion CONTAINER_BORDER_RIGHT = new TextureRegion(18, 25, 15, 1);

    private static final int GOD_TAB_WIDTH = 50;
    private static final int GOD_TAB_HEIGHT = 14;
    private static final int GOD_TAB_GAP = 3;
    private static final float GOD_TAB_TEXT_SCALE = 0.7F;
    private static final int XP_BAR_WIDTH = 130;
    private static final int XP_BAR_HEIGHT = 5;

    private static VaultGod selectedGod = VaultGod.IDONA;

    private final GodTreeDialog dialog = new GodTreeDialog(this, selectedGod);
    private final GodTreePanRegion panRegion = new GodTreePanRegion(this, this.dialog, selectedGod);
    private long seenRevision = -1L;

    public GodTreeScreen(GodTreeContainer container, Inventory inventory, Component title) {
        super(container, inventory, title, ScreenRenderers.getImmediate());
        if (Minecraft.getInstance().player != null) {
            ActiveGodResolver.invalidate(Minecraft.getInstance().player);
        }
        this.dialog.update();
    }

    @Override
    public int getTabIndex() {
        return TAB_INDEX;
    }

    @Override
    public MutableComponent getTabTitle() {
        return GodTreeTheme.lang("title");
    }

    private Rectangle getContentBounds() {
        return new Rectangle(30, 60, this.width - 30, this.height - 30 - 60);
    }

    private Rectangle getGodTabBounds(int index) {
        Rectangle pan = this.panRegion.getBounds();
        int x = pan.x + pan.width - GOD_TAB_WIDTH - 4;
        int y = pan.y + 6 + index * (GOD_TAB_HEIGHT + GOD_TAB_GAP);
        return new Rectangle(x, y, GOD_TAB_WIDTH, GOD_TAB_HEIGHT);
    }

    private Rectangle getXpBarBounds() {
        Rectangle pan = this.panRegion.getBounds();
        int assembled = 16 + 4 + XP_BAR_WIDTH;
        int x = pan.x + (pan.width - assembled) / 2 + 20;
        return new Rectangle(x, pan.y + 17, XP_BAR_WIDTH, XP_BAR_HEIGHT);
    }

    private void selectGod(VaultGod god) {
        if (selectedGod == god) {
            return;
        }
        selectedGod = god;
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        this.dialog.setGod(god);
        this.panRegion.setGod(god);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (VaultGod god : VaultGod.values()) {
                if (this.getGodTabBounds(god.ordinal()).contains(mouseX, mouseY)) {
                    this.selectGod(god);
                    return true;
                }
            }
        }
        Rectangle contentBounds = this.getContentBounds();
        if (contentBounds.contains(mouseX, mouseY)) {
            Rectangle panBounds = this.panRegion.getBounds();
            boolean handled = panBounds.contains(mouseX, mouseY)
                    ? this.panRegion.mouseClicked(mouseX, mouseY, button)
                    : this.dialog.mouseClicked(mouseX, mouseY, button);
            if (handled) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.dialog.mouseReleased(mouseX, mouseY, button);
        this.panRegion.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        this.panRegion.mouseMoved(mouseX, mouseY);
        this.dialog.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        Rectangle panBounds = this.panRegion.getBounds();
        if (panBounds.contains(mouseX, mouseY)) {
            return this.panRegion.mouseScrolled(mouseX, mouseY, delta);
        }
        return this.dialog.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        InputConstants.Key key = InputConstants.getKey(keyCode, scanCode);
        if (keyCode == 256 || Minecraft.getInstance().options.keyInventory.isActiveAndMatches(key)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        long revision = ClientGodAlignmentData.revision();
        if (revision != this.seenRevision) {
            this.seenRevision = revision;
            this.update();
        }
    }

    @Override
    public void render(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackgroundFill(matrixStack);
        Rectangle panBounds = this.panRegion.getBounds();
        List<Runnable> postRender = this.panRegion.render(matrixStack, panBounds, mouseX, mouseY, partialTicks);
        this.renderContainerBorders(matrixStack);
        this.renderGodTabs(matrixStack, mouseX, mouseY);
        this.renderGodXpBar(matrixStack, mouseX, mouseY);
        this.renderPietyReadout(matrixStack, mouseX, mouseY);
        int dialogX = panBounds.x + panBounds.width + 15;
        int dialogY = panBounds.y - 18;
        Rectangle dialogBounds = new Rectangle(dialogX, dialogY, this.width - 21 - dialogX, this.height - 21 - dialogY);
        this.dialog.setBounds(dialogBounds);
        this.dialog.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderPointsOverlay(matrixStack);
        postRender.forEach(Runnable::run);
        if (this.needsLayout) {
            this.layout(Spatials.zero());
            this.needsLayout = false;
        }
        this.renderElements(matrixStack, mouseX, mouseY, partialTicks);
        this.renderSlotItems(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltips(matrixStack, mouseX, mouseY);
    }

    private void renderContainerBorders(PoseStack matrixStack) {
        Rectangle bounds = this.panRegion.getBounds();
        RenderSystem.enableBlend();
        TextureRegionRenderer.getInstance().begin(ScreenTextures.UI_RESOURCE).with(matrixStack)
                .draw(bounds.x - 9, bounds.y - 18, CONTAINER_BORDER_CORNER_TOP_LEFT)
                .draw(bounds.x + bounds.width - 7, bounds.y - 18, CONTAINER_BORDER_CORNER_TOP_RIGHT)
                .draw(bounds.x - 9, bounds.y + bounds.height - 7, CONTAINER_BORDER_CORNER_BOTTOM_LEFT)
                .draw(bounds.x + bounds.width - 7, bounds.y + bounds.height - 7, CONTAINER_BORDER_CORNER_BOTTOM_RIGHT)
                .push().translateXY(bounds.x + 6, bounds.y - 18).scaleX(bounds.width - 13).draw(CONTAINER_BORDER_TOP)
                .translateY(bounds.height + 11).draw(CONTAINER_BORDER_BOTTOM).pop()
                .push().translateXY(bounds.x - 9, bounds.y + 6).scaleY(bounds.height - 13).draw(CONTAINER_BORDER_LEFT)
                .translateX(bounds.width + 2).draw(CONTAINER_BORDER_RIGHT).pop()
                .end();
    }

    /**
     * The god experience readout floated over the top center of the sky: the god's icon, the
     * level beside it, and a slim bar filling toward the next level in the god's accent.
     * Hovering it shows the exact experience numbers.
     */
    private void renderGodXpBar(PoseStack matrixStack, int mouseX, int mouseY) {
        VaultGod god = selectedGod;
        Rectangle bar = this.getXpBarBounds();
        int level = ClientGodAlignmentData.getLevel(god);
        long xp = ClientGodAlignmentData.getXp(god);
        long currentThreshold = GodLevels.xpForLevel(level);
        long nextThreshold = GodLevels.xpForLevel(level + 1);
        float progress = nextThreshold > currentThreshold
                ? (float) (xp - currentThreshold) / (float) (nextThreshold - currentThreshold)
                : 1.0F;
        progress = Math.max(0.0F, Math.min(1.0F, progress));
        int accent = GodTreeTheme.pointsColor(god);
        matrixStack.pushPose();
        matrixStack.translate(0.0D, 0.0D, 100.0D);
        GodTreeTheme.godIcon(god).blit(matrixStack, bar.x - 20, bar.y - 6, 0, 16, 16);
        fill(matrixStack, bar.x - 1, bar.y - 1, bar.x + bar.width + 1, bar.y + bar.height + 1, 0xCC000000);
        fill(matrixStack, bar.x, bar.y, bar.x + bar.width, bar.y + bar.height, 0xFF1A0F12);
        int filled = (int) (bar.width * progress);
        if (filled > 0) {
            fill(matrixStack, bar.x, bar.y, bar.x + filled, bar.y + bar.height, accent);
        }
        String levelText = String.valueOf(level);
        FontHelper.drawStringWithBorder(matrixStack, levelText,
                bar.x + (bar.width - this.font.width(levelText)) / 2.0F, bar.y - 10,
                accent & 0xFFFFFF | 0xFF000000, 0xFF1E1E1E);
        matrixStack.popPose();
        Rectangle hover = new Rectangle(bar.x - 20, bar.y - 10, bar.width + 20, bar.height + 12);
        if (hover.contains(mouseX, mouseY)) {
            java.util.List<Component> lines = new java.util.ArrayList<>();
            lines.add(new TextComponent(god.getName() + " - Level " + level + "  (")
                    .append(String.format("%,d", xp)).append(" / ").append(String.format("%,d", nextThreshold))
                    .append(" XP)"));
            int sacrifices = ClientGodAlignmentData.getSacrifices(god);
            if (sacrifices >= xyz.iwolfking.woldsvaults.gods.sacrifice.GodSacrifices.GATE_COUNT) {
                lines.add(new TextComponent("Every cauldron sacrifice has been performed.")
                        .withStyle(net.minecraft.ChatFormatting.GRAY));
            } else if (sacrifices > level) {
                lines.add(new TextComponent("Next sacrifice: already performed.")
                        .withStyle(net.minecraft.ChatFormatting.GREEN));
            } else {
                String gate = xyz.iwolfking.woldsvaults.gods.sacrifice.GodSacrifices.gateLabel(sacrifices);
                if (GodLevels.levelForXp(xp) > level) {
                    lines.add(new TextComponent(gate + " required at the Greed Cauldron to advance!")
                            .withStyle(net.minecraft.ChatFormatting.RED));
                } else {
                    lines.add(new TextComponent(gate + ": not yet performed.")
                            .withStyle(net.minecraft.ChatFormatting.GRAY));
                }
            }
            this.renderComponentTooltip(matrixStack, lines, mouseX, mouseY);
        }
    }

    /**
     * The piety readout, embedded in the top-right run of the container border the way the talent
     * tabs embed the vault XP bar: a small purple number sitting inside the frame, out of the way
     * of the god selector plates.
     */
    private void renderPietyReadout(PoseStack matrixStack, int mouseX, int mouseY) {
        Rectangle bounds = this.panRegion.getBounds();
        String pietyText = String.format("%,d Piety", ClientGodAlignmentData.getPiety(selectedGod));
        float scale = 0.9F;
        float width = this.font.width(pietyText) * scale;
        float pietyX = bounds.x + bounds.width - width - 14.0F;
        float pietyY = bounds.y - 12.0F;
        matrixStack.pushPose();
        matrixStack.translate(0.0D, 0.0D, 120.0D);
        matrixStack.scale(scale, scale, 1.0F);
        this.font.drawShadow(matrixStack, pietyText, pietyX / scale, pietyY / scale, 0xFFC77DFF);
        matrixStack.popPose();
        Rectangle pietyHover = new Rectangle((int) pietyX, (int) pietyY - 1, (int) width + 2, 10);
        if (pietyHover.contains(mouseX, mouseY)) {
            Component tooltip = new TextComponent("Piety with " + selectedGod.getName()
                    + ": 10 per god reputation, 20 per god level, plus tree bonuses. Scales your god charm.");
            this.renderTooltip(matrixStack, this.font.split(tooltip, 200), mouseX, mouseY);
        }
    }

    /**
     * One selector plate per god along the chart's right edge: the viewed god lit in its color,
     * and a small corner spark on whichever god's charm is currently equipped.
     */
    private void renderGodTabs(PoseStack matrixStack, int mouseX, int mouseY) {
        VaultGod charmGod = this.minecraft != null && this.minecraft.player != null
                ? ActiveGodResolver.getActiveGod(this.minecraft.player).orElse(null)
                : null;
        matrixStack.pushPose();
        matrixStack.translate(0.0D, 0.0D, 100.0D);
        for (VaultGod god : VaultGod.values()) {
            Rectangle tab = this.getGodTabBounds(god.ordinal());
            boolean selected = god == selectedGod;
            boolean hovered = tab.contains(mouseX, mouseY);
            int accent = GodTreeTheme.accent(god);
            int border = selected ? accent : (hovered ? 0xFF8A8A8A : 0xFF2E2430);
            int back = selected ? (GodTreeTheme.accentDeep(god) & 0xFFFFFF) | 0xF2000000 : 0xC01A1420;
            fill(matrixStack, tab.x - 1, tab.y - 1, tab.x + tab.width + 1, tab.y + tab.height + 1, border);
            fill(matrixStack, tab.x, tab.y, tab.x + tab.width, tab.y + tab.height, back);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, selected || hovered ? 1.0F : 0.6F);
            GodTreeTheme.godIcon(god).blit(matrixStack, tab.x + 2, tab.y + 2, 0, 10, 10);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            int textColor = selected ? accent : (hovered ? 0xFFDCDCDC : 0xFF8A8A8A);
            matrixStack.pushPose();
            matrixStack.translate(tab.x + 14, tab.y + 5, 0.0D);
            matrixStack.scale(GOD_TAB_TEXT_SCALE, GOD_TAB_TEXT_SCALE, 1.0F);
            this.font.draw(matrixStack, god.getName(), 0.0F, 0.0F, textColor);
            matrixStack.popPose();
            if (god == charmGod) {
                fill(matrixStack, tab.x + tab.width - 4, tab.y + 2, tab.x + tab.width - 1, tab.y + 5, accent);
            }
        }
        matrixStack.popPose();
    }

    /** The unspent-points line every skill tab carries, in the selected god's color. */
    private void renderPointsOverlay(PoseStack matrixStack) {
        int unspent = ClientGodAlignmentData.getUnspentPoints(selectedGod);
        int rightEdge = this.getContentBounds().width;
        int color = unspent > 0 ? GodTreeTheme.pointsColor(selectedGod) : 0xFFAAAAAA;
        FormattedCharSequence pointsText = new TextComponent(unspent + " Unspent God Points")
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color & 0xFFFFFF))).getVisualOrderText();
        int pointsWidth = this.font.width(pointsText);
        this.font.drawShadow(matrixStack, pointsText, rightEdge - pointsWidth, 12.0F, 0xFFFFFFFF);
    }

    @Override
    public void update() {
        this.panRegion.update();
        this.dialog.update();
    }

    @Override
    public TabContent getTabContent() {
        return null;
    }
}
