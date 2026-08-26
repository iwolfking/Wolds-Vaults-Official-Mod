package xyz.iwolfking.woldsvaults.client.screens.gods;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.framework.ScreenRenderers;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.screen.player.AbstractSkillTabElementContainerScreen;
import iskallia.vault.client.gui.screen.player.legacy.ILegacySkillTreeScreen;
import iskallia.vault.client.gui.screen.player.legacy.TabContent;
import iskallia.vault.client.render.TextureRegion;
import iskallia.vault.client.render.TextureRegionRenderer;
import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.ChatFormatting;
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
import xyz.iwolfking.woldsvaults.gods.ClientGodNodePreviews;
import xyz.iwolfking.woldsvaults.gods.container.GodTreeContainer;
import xyz.iwolfking.woldsvaults.gods.network.ServerboundRequestGodNodePreviewMessage;
import xyz.iwolfking.woldsvaults.network.NetworkHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * The gods tab of the player menu: the God's Mastery overview, then one star chart per god. Tabs run
 * down the frame's left edge.
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

    private static final int SIDE_TAB_SIZE = 20;
    private static final int SIDE_TAB_GAP = 2;
    private static final int XP_BAR_WIDTH = 130;
    private static final int PREVIEW_REFRESH_TICKS = 20;

    private static GodScreenTab selectedTab = GodScreenTab.OVERVIEW;
    private static VaultGod viewedGod = VaultGod.IDONA;

    private final GodTreeDialog dialog = new GodTreeDialog(this, viewedGod);
    private final GodTreePanRegion panRegion = new GodTreePanRegion(this, this.dialog, viewedGod);
    private final GodOverviewPage overviewPage = new GodOverviewPage(this);
    private long seenRevision = -1L;
    private long seenPreviewRevision = -1L;
    private String requestedPreviewEffect;
    private int previewTicks;

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

    private Rectangle getOverviewBounds() {
        return new Rectangle(30, 60, this.width - 51, this.height - 90);
    }

    private Rectangle getFrameBounds() {
        return selectedTab.isOverview() ? this.getOverviewBounds() : this.panRegion.getBounds();
    }

    private Rectangle getSideTabBounds(int index) {
        Rectangle frame = this.getFrameBounds();
        return new Rectangle(frame.x + 4, frame.y + 6 + index * (SIDE_TAB_SIZE + SIDE_TAB_GAP), SIDE_TAB_SIZE, SIDE_TAB_SIZE);
    }

    private Rectangle getXpBarBounds() {
        Rectangle pan = this.panRegion.getBounds();
        int assembled = 16 + 4 + XP_BAR_WIDTH;
        int x = pan.x + (pan.width - assembled) / 2 + 20;
        return new Rectangle(x, pan.y + 17, XP_BAR_WIDTH, GodXpBarRenderer.BAR_HEIGHT);
    }

    public void selectTab(GodScreenTab tab) {
        if (selectedTab == tab) {
            return;
        }
        selectedTab = tab;
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        this.panRegion.closePopup();
        VaultGod god = tab.god();
        if (god != null) {
            viewedGod = god;
            this.dialog.setGod(god);
            this.panRegion.setGod(god);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        GodScreenTab[] tabs = GodScreenTab.values();
        for (int i = 0; i < tabs.length; i++) {
            if (this.getSideTabBounds(i).contains(mouseX, mouseY)) {
                if (button == 0) {
                    this.selectTab(tabs[i]);
                }
                return true;
            }
        }
        if (selectedTab.isOverview()) {
            if (this.overviewPage.mouseClicked(this.getOverviewBounds(), mouseX, mouseY, button)) {
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
        Rectangle panBounds = this.panRegion.getBounds();
        if (!panBounds.contains(mouseX, mouseY)) {
            this.panRegion.closePopup();
        }
        Rectangle contentBounds = this.getContentBounds();
        if (contentBounds.contains(mouseX, mouseY)) {
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
        if (selectedTab.isOverview()) {
            return super.mouseScrolled(mouseX, mouseY, delta);
        }
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
        this.pollPreview();
        long previewRevision = ClientGodNodePreviews.revision();
        if (previewRevision != this.seenPreviewRevision) {
            this.seenPreviewRevision = previewRevision;
            this.dialog.refreshPreview();
        }
    }

    /** Polls the server for the selected node's live preview; nodes answered as static are never polled. */
    private void pollPreview() {
        String effectId = selectedTab.isOverview() ? null : this.dialog.selectedEffectId();
        if (effectId == null || ClientGodNodePreviews.isKnownStatic(effectId)) {
            this.requestedPreviewEffect = null;
            return;
        }
        if (effectId.equals(this.requestedPreviewEffect) && ++this.previewTicks < PREVIEW_REFRESH_TICKS) {
            return;
        }
        this.requestedPreviewEffect = effectId;
        this.previewTicks = 0;
        NetworkHandler.INSTANCE.sendToServer(new ServerboundRequestGodNodePreviewMessage(effectId));
    }

    @Override
    public void render(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackgroundFill(matrixStack);
        List<Runnable> postRender = new ArrayList<>();
        if (selectedTab.isOverview()) {
            Rectangle frame = this.getOverviewBounds();
            fill(matrixStack, frame.x, frame.y, frame.x + frame.width, frame.y + frame.height, GodTreeTheme.SPACE_BACKGROUND);
            this.overviewPage.render(matrixStack, frame, mouseX, mouseY, postRender);
            this.renderContainerBorders(matrixStack, frame);
            this.renderSideTabs(matrixStack, mouseX, mouseY, postRender);
        } else {
            Rectangle panBounds = this.panRegion.getBounds();
            postRender.addAll(this.panRegion.render(matrixStack, panBounds, mouseX, mouseY, partialTicks));
            this.renderContainerBorders(matrixStack, panBounds);
            this.renderSideTabs(matrixStack, mouseX, mouseY, postRender);
            GodXpBarRenderer.render(matrixStack, this.font, viewedGod, this.getXpBarBounds(), true, mouseX, mouseY,
                    lines -> this.renderComponentTooltip(matrixStack, lines, mouseX, mouseY));
            this.renderPietyReadout(matrixStack, mouseX, mouseY);
            int dialogX = panBounds.x + panBounds.width + 15;
            int dialogY = panBounds.y - 18;
            Rectangle dialogBounds = new Rectangle(dialogX, dialogY, this.width - 21 - dialogX, this.height - 21 - dialogY);
            this.dialog.setBounds(dialogBounds);
            this.dialog.render(matrixStack, mouseX, mouseY, partialTicks);
            Style hovered = this.dialog.hoveredStyle(mouseX, mouseY);
            if (hovered != null) {
                postRender.add(() -> this.renderComponentHoverEffect(matrixStack, hovered, mouseX, mouseY));
            }
            this.renderPointsOverlay(matrixStack);
        }
        postRender.forEach(Runnable::run);
        if (this.needsLayout) {
            this.layout(Spatials.zero());
            this.needsLayout = false;
        }
        this.renderElements(matrixStack, mouseX, mouseY, partialTicks);
        this.renderSlotItems(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltips(matrixStack, mouseX, mouseY);
    }

    private void renderContainerBorders(PoseStack matrixStack, Rectangle bounds) {
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

    private void renderPietyReadout(PoseStack matrixStack, int mouseX, int mouseY) {
        Rectangle bounds = this.panRegion.getBounds();
        String pietyText = String.format("%,d Piety", ClientGodAlignmentData.getPiety(viewedGod));
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
            Component tooltip = new TextComponent("Piety with " + viewedGod.getName()
                    + ": 10 per god reputation, 20 per god level, plus tree bonuses. Scales your god charm.");
            this.renderTooltip(matrixStack, this.font.split(tooltip, 200), mouseX, mouseY);
        }
    }

    /** The column of square tabs down the frame's left edge. Hover text is deferred to {@code postRender}. */
    private void renderSideTabs(PoseStack matrixStack, int mouseX, int mouseY, List<Runnable> postRender) {
        VaultGod charmGod = this.minecraft != null && this.minecraft.player != null
                ? ActiveGodResolver.getActiveGod(this.minecraft.player).orElse(null)
                : null;
        GodScreenTab[] tabs = GodScreenTab.values();
        matrixStack.pushPose();
        matrixStack.translate(0.0D, 0.0D, 100.0D);
        for (int i = 0; i < tabs.length; i++) {
            GodScreenTab tab = tabs[i];
            Rectangle bounds = this.getSideTabBounds(i);
            boolean selected = tab == selectedTab;
            boolean hovered = bounds.contains(mouseX, mouseY);
            VaultGod god = tab.god();
            int accent = god == null ? GodTreeTheme.OVERVIEW_ACCENT : GodTreeTheme.accent(god);
            int deep = god == null ? 0xFF2A2620 : GodTreeTheme.accentDeep(god);
            int border = selected ? accent : (hovered ? GodTreeTheme.PLATE_BORDER_HOVER : GodTreeTheme.PLATE_BORDER);
            int back = selected ? (deep & 0xFFFFFF) | 0xF2000000 : GodTreeTheme.PLATE_FILL;
            fill(matrixStack, bounds.x - 1, bounds.y - 1, bounds.x + bounds.width + 1, bounds.y + bounds.height + 1, border);
            fill(matrixStack, bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height, back);
            float alpha = selected || hovered ? 1.0F : 0.6F;
            if (god == null) {
                GodDrawing.texture(matrixStack, GodTreeTheme.GODS_MASTERY_ICON, bounds.x + 2, bounds.y + 2, 16, alpha);
            } else {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
                GodTreeTheme.godIcon(god).blit(matrixStack, bounds.x + 2, bounds.y + 2, 0, 16, 16);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            }
            if (god != null && god == charmGod) {
                fill(matrixStack, bounds.x + bounds.width - 4, bounds.y + 1, bounds.x + bounds.width - 1, bounds.y + 4, accent);
            }
            if (hovered) {
                List<Component> lines = sideTabTooltip(tab, charmGod);
                postRender.add(() -> this.renderComponentTooltip(matrixStack, lines, mouseX, mouseY));
            }
        }
        matrixStack.popPose();
    }

    private static List<Component> sideTabTooltip(GodScreenTab tab, @Nullable VaultGod charmGod) {
        List<Component> lines = new ArrayList<>();
        VaultGod god = tab.god();
        if (god == null) {
            lines.add(new TextComponent("God's Mastery")
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(GodTreeTheme.OVERVIEW_ACCENT & 0xFFFFFF))));
            lines.add(new TextComponent("All four gods and your transfer slots at a glance").withStyle(ChatFormatting.GRAY));
            return lines;
        }
        lines.add(new TextComponent(god.getName() + " - Level " + ClientGodAlignmentData.getLevel(god))
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(GodTreeTheme.accent(god) & 0xFFFFFF))));
        int unspent = ClientGodAlignmentData.getUnspentPoints(god);
        if (!GodCharting.isCharted(god)) {
            lines.add(new TextComponent("Uncharted - reach " + GodCharting.reputationRequired() + " reputation with "
                    + god.getName()).withStyle(ChatFormatting.GRAY));
        } else if (unspent > 0) {
            lines.add(new TextComponent(unspent + " unspent god " + (unspent == 1 ? "point" : "points"))
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(GodTreeTheme.pointsColor(god) & 0xFFFFFF))));
        } else {
            lines.add(new TextComponent("No unspent god points").withStyle(ChatFormatting.GRAY));
        }
        if (god == charmGod) {
            lines.add(new TextComponent("Active - charm equipped").withStyle(ChatFormatting.GREEN));
        }
        return lines;
    }

    private void renderPointsOverlay(PoseStack matrixStack) {
        int unspent = ClientGodAlignmentData.getUnspentPoints(viewedGod);
        int rightEdge = this.getContentBounds().width;
        int color = unspent > 0 ? GodTreeTheme.pointsColor(viewedGod) : 0xFFAAAAAA;
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
