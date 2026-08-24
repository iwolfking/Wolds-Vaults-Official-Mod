package xyz.iwolfking.woldsvaults.client.screens.gods;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import iskallia.vault.client.gui.helper.ScreenDrawHelper;
import iskallia.vault.client.gui.helper.UIHelper;
import iskallia.vault.client.util.ClientScheduler;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.util.MiscUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.phys.Vec2;
import xyz.iwolfking.woldsvaults.gods.ClientGodAlignmentData;
import iskallia.vault.config.entry.SkillStyle;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.node.GodNode;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeRegistry;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeType;
import xyz.iwolfking.woldsvaults.gods.node.GodTreeModel;

import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** The pannable star chart of one god's tree, a region of {@link GodTreeScreen}. Pan and zoom persist per god. */
public class GodTreePanRegion extends GuiComponent implements GuiEventListener {
    private static final Map<VaultGod, Vec2> persistedTranslations = new EnumMap<>(VaultGod.class);
    private static final Map<VaultGod, Float> persistedScales = new EnumMap<>(VaultGod.class);
    private static final int BACKGROUND_STARS = 420;
    private static final long STAR_SEED = 0x1D0A57A2L;

    protected final GodTreeScreen parentScreen;
    protected final GodTreeDialog dialog;
    protected final Map<String, GodNodeWidget> nodeWidgets = new HashMap<>();
    protected final List<GodTreeModel.Edge> edges = new LinkedList<>();
    protected final List<float[]> backgroundStars = new ArrayList<>();
    protected VaultGod god;
    protected GodTreeModel tree;
    protected GodNodeWidget selectedWidget;
    protected Vec2 viewportTranslation = new Vec2(0.0F, 0.0F);
    protected float viewportScale = 0.5F;
    protected boolean dragging;
    protected Vec2 grabbedPos = new Vec2(0.0F, 0.0F);
    @Nullable
    private TransferSlotPopup popup;

    public GodTreePanRegion(GodTreeScreen parentScreen, GodTreeDialog dialog, VaultGod god) {
        this.parentScreen = parentScreen;
        this.dialog = dialog;
        this.setGod(god);
    }

    public Rectangle getBounds() {
        return new Rectangle(30, 60, (int) ((float) this.parentScreen.width * 0.55F) - 30, this.parentScreen.height - 30 - 60);
    }

    public VaultGod getGod() {
        return this.god;
    }

    public void setGod(VaultGod god) {
        this.god = god;
        this.selectedWidget = null;
        this.popup = null;
        this.update();
        this.loadViewportTransforms();
    }

    public void closePopup() {
        this.popup = null;
    }

    public void update() {
        String selectedId = this.selectedWidget != null ? this.selectedWidget.getNode().id() : null;
        this.nodeWidgets.clear();
        this.edges.clear();
        this.selectedWidget = null;
        this.tree = GodCharting.isCharted(this.god) ? GodNodeRegistry.tree(this.god).orElse(null) : null;
        this.rebuildBackgroundStars();
        if (this.tree == null) {
            this.popup = null;
            return;
        }
        for (GodNode node : this.tree.getNodes()) {
            SkillStyle style = this.tree.getStyle(node.id());
            if (style == null) {
                WoldsVaults.LOGGER.error("God tree node {} has no style entry in god_tree_{}_gui_styles.json; "
                        + "it cannot be drawn.", node.id(), this.god.getName().toLowerCase(java.util.Locale.ROOT));
                continue;
            }
            boolean unlocked = ClientGodAlignmentData.isTreeNodePurchased(this.god, node.id());
            boolean available = this.tree.isPurchasable(node.id(),
                    id -> ClientGodAlignmentData.isTreeNodePurchased(this.god, id));
            GodNodeWidget widget = new GodNodeWidget(node, style, this.god, unlocked, available);
            this.nodeWidgets.put(node.id(), widget);
        }
        this.edges.addAll(this.tree.getEdges());
        if (selectedId != null) {
            GodNodeWidget reselected = this.nodeWidgets.get(selectedId);
            if (reselected != null) {
                this.selectedWidget = reselected;
                reselected.select();
            }
        }
        if (this.popup != null) {
            GodNodeWidget anchor = this.nodeWidgets.get(this.popup.anchorNodeId());
            if (anchor == null || !anchor.isUnlocked()) {
                this.popup = null;
            }
        }
    }

    /** Deterministic background starfield in tree space; entries are x, y, size and twinkle phase. */
    private void rebuildBackgroundStars() {
        this.backgroundStars.clear();
        float minX = -700.0F;
        float maxX = 700.0F;
        float minY = -1050.0F;
        float maxY = 520.0F;
        if (this.tree != null) {
            for (GodNode node : this.tree.getNodes()) {
                SkillStyle style = this.tree.getStyle(node.id());
                if (style == null) {
                    continue;
                }
                minX = Math.min(minX, style.x - 260);
                maxX = Math.max(maxX, style.x + 260);
                minY = Math.min(minY, style.y - 260);
                maxY = Math.max(maxY, style.y + 260);
            }
        }
        Random random = new Random(STAR_SEED + this.god.ordinal() * 7919L);
        for (int i = 0; i < BACKGROUND_STARS; i++) {
            float x = minX + random.nextFloat() * (maxX - minX);
            float y = minY + random.nextFloat() * (maxY - minY);
            float roll = random.nextFloat();
            float size = roll < 0.72F ? 1.0F : (roll < 0.94F ? 1.6F : 2.4F);
            float phase = random.nextFloat() * (float) (Math.PI * 2.0D);
            float tint = random.nextFloat();
            this.backgroundStars.add(new float[]{x, y, size, phase, tint});
        }
    }

    protected void loadViewportTransforms() {
        this.viewportTranslation = persistedTranslations.computeIfAbsent(this.god, god -> this.getCenter());
        this.viewportScale = persistedScales.computeIfAbsent(this.god, god -> this.clampViewportScale(0.45F));
    }

    protected void saveViewportTransforms() {
        persistedTranslations.put(this.god, this.viewportTranslation);
        persistedScales.put(this.god, this.viewportScale);
    }

    protected Vec2 getCenter() {
        if (this.nodeWidgets.isEmpty()) {
            return new Vec2(0.0F, 0.0F);
        }
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (GodNodeWidget widget : this.nodeWidgets.values()) {
            minX = Math.min(minX, widget.x);
            maxX = Math.max(maxX, widget.x);
            minY = Math.min(minY, widget.y);
            maxY = Math.max(maxY, widget.y);
        }
        return new Vec2((minX + maxX) / -2.0F, (minY + maxY) / -2.0F);
    }

    protected float clampViewportScale(float scale) {
        return Math.max(0.25F, Math.min(2.0F, scale));
    }

    /** Left-click selects or drags a star; right-click on a learned minor opens the transfer-slot picker. */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.getBounds().contains(mouseX, mouseY) || (button != 0 && button != 1)) {
            return false;
        }
        Point2D.Float midpoint = MiscUtils.getMidpoint(this.getBounds());
        int containerMouseX = (int) ((mouseX - midpoint.x) / this.viewportScale - this.viewportTranslation.x);
        int containerMouseY = (int) ((mouseY - midpoint.y) / this.viewportScale - this.viewportTranslation.y);
        if (button == 0 && this.popup != null) {
            GodNodeWidget anchor = this.nodeWidgets.get(this.popup.anchorNodeId());
            int slot = anchor == null ? -1 : this.popup.slotAt(anchor, containerMouseX, containerMouseY);
            if (slot >= 0) {
                this.popup.click(slot);
                return true;
            }
        }
        this.popup = null;
        GodNodeWidget hit = null;
        for (GodNodeWidget widget : this.nodeWidgets.values()) {
            if (widget.isMouseOver(containerMouseX, containerMouseY)) {
                hit = widget;
                break;
            }
        }
        if (hit != null) {
            if (this.selectedWidget != null) {
                this.selectedWidget.deselect();
            }
            this.selectedWidget = hit;
            hit.select();
            this.dialog.setSelectedNode(hit.getNode().id());
            if (button == 1 && hit.getNode().type() == GodNodeType.MINOR && hit.isUnlocked()) {
                this.popup = new TransferSlotPopup(this.god, hit.getNode());
            }
            return true;
        }
        if (button == 0) {
            this.grabbedPos = new Vec2((float) mouseX, (float) mouseY);
            this.dragging = true;
        }
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.dragging = false;
        this.saveViewportTransforms();
        return false;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (this.dragging) {
            Vec2 currentPos = new Vec2((float) mouseX, (float) mouseY);
            float dx = (currentPos.x - this.grabbedPos.x) / this.viewportScale;
            float dy = (currentPos.y - this.grabbedPos.y) / this.viewportScale;
            this.viewportTranslation = new Vec2(this.viewportTranslation.x + dx, this.viewportTranslation.y + dy);
            this.grabbedPos = currentPos;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.getBounds().contains(mouseX, mouseY)) {
            float newScale = this.viewportScale + (float) delta * 0.1F;
            this.viewportScale = this.clampViewportScale(newScale);
            this.saveViewportTransforms();
            return true;
        }
        return false;
    }

    public List<Runnable> render(PoseStack renderStack, Rectangle containerBounds, int mouseX, int mouseY, float pTicks) {
        List<Runnable> postRender = new ArrayList<>();
        UIHelper.renderOverflowHidden(renderStack,
                ms -> this.renderBackground(ms, containerBounds),
                ms -> this.renderForeground(ms, containerBounds, mouseX, mouseY, pTicks, postRender));
        return postRender;
    }

    protected void renderBackground(PoseStack matrixStack, Rectangle containerBounds) {
        fill(matrixStack, containerBounds.x, containerBounds.y,
                containerBounds.x + containerBounds.width, containerBounds.y + containerBounds.height,
                GodTreeTheme.SPACE_BACKGROUND);
    }

    protected void renderForeground(PoseStack renderStack, Rectangle containerBounds, int mouseX, int mouseY,
                                    float pTicks, List<Runnable> postRender) {
        RenderSystem.enableBlend();
        Point2D.Float midpoint = MiscUtils.getMidpoint(this.getBounds());
        renderStack.pushPose();
        renderStack.translate(midpoint.x, midpoint.y, 0.0D);
        renderStack.scale(this.viewportScale, this.viewportScale, 1.0F);
        renderStack.translate(this.viewportTranslation.x, this.viewportTranslation.y, 0.0D);
        int containerMouseX = (int) (((float) mouseX - midpoint.x) / this.viewportScale - this.viewportTranslation.x);
        int containerMouseY = (int) (((float) mouseY - midpoint.y) / this.viewportScale - this.viewportTranslation.y);
        float tick = (float) ClientScheduler.INSTANCE.getTick() + pTicks;
        this.renderBackgroundStars(renderStack, tick);
        if (this.tree == null) {
            this.renderUnchartedSky(renderStack);
            renderStack.popPose();
            return;
        }
        this.renderConstellationLines(renderStack);
        this.renderConstellationLabels(renderStack);
        for (GodNodeWidget widget : this.nodeWidgets.values()) {
            widget.renderWidget(renderStack, containerBounds, mouseX, mouseY, containerMouseX, containerMouseY, pTicks, postRender);
        }
        if (this.popup != null) {
            GodNodeWidget anchor = this.nodeWidgets.get(this.popup.anchorNodeId());
            if (anchor != null) {
                this.popup.render(renderStack, anchor, this.tree, containerMouseX, containerMouseY, mouseX, mouseY, postRender);
            }
        }
        renderStack.popPose();
    }

    private void renderBackgroundStars(PoseStack renderStack, float tick) {
        RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        Matrix4f pose = renderStack.last().pose();
        ScreenDrawHelper.draw(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR, buf -> {
            for (float[] star : this.backgroundStars) {
                float twinkle = 0.45F + 0.35F * (float) Math.sin(tick / 26.0D + star[3]);
                int alpha = (int) (255 * Math.min(1.0F, Math.max(0.12F, twinkle)));
                int red = 235;
                int green = star[4] < 0.25F ? 170 : 225;
                int blue = star[4] < 0.25F ? 140 : (star[4] > 0.85F ? 255 : 215);
                this.starQuad(buf, pose, star[0], star[1], star[2], red, green, blue, alpha);
            }
        });
        RenderSystem.enableTexture();
    }

    private void starQuad(BufferBuilder buf, Matrix4f pose, float x, float y, float size, int r, int g, int b, int a) {
        float half = size / 2.0F;
        buf.vertex(pose, x - half, y + half, 0.0F).color(r, g, b, a).endVertex();
        buf.vertex(pose, x + half, y + half, 0.0F).color(r, g, b, a).endVertex();
        buf.vertex(pose, x + half, y - half, 0.0F).color(r, g, b, a).endVertex();
        buf.vertex(pose, x - half, y - half, 0.0F).color(r, g, b, a).endVertex();
    }

    /** Draws every lattice edge in the god's accent, brightest between two owned stars. */
    private void renderConstellationLines(PoseStack renderStack) {
        RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        Matrix4f pose = renderStack.last().pose();
        int accent = GodTreeTheme.accent(this.god);
        int dim = GodTreeTheme.accentDim(this.god);
        int deep = GodTreeTheme.accentDeep(this.god);
        ScreenDrawHelper.draw(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR, buf -> {
            for (GodTreeModel.Edge edge : this.edges) {
                GodNodeWidget from = this.nodeWidgets.get(edge.from());
                GodNodeWidget to = this.nodeWidgets.get(edge.to());
                if (from == null || to == null) {
                    continue;
                }
                boolean lit = from.isUnlocked() && to.isUnlocked();
                boolean warm = !lit && (from.isUnlocked() || to.isUnlocked());
                int glowColor = lit ? dim : deep;
                int coreColor = lit ? accent : (warm ? dim : deep);
                int glowAlpha = lit ? 150 : 105;
                int coreAlpha = lit ? 230 : (warm ? 200 : 160);
                this.lineQuad(buf, pose, from, to, 2.6F, glowColor, glowAlpha);
                this.lineQuad(buf, pose, from, to, lit ? 1.1F : 0.9F, coreColor, coreAlpha);
            }
        });
        RenderSystem.enableTexture();
    }

    private void lineQuad(BufferBuilder buf, Matrix4f pose, GodNodeWidget from, GodNodeWidget to, float width, int color, int alpha) {
        float x1 = from.x + from.getWidth() / 2.0F;
        float y1 = from.y + from.getHeight() / 2.0F;
        float x2 = to.x + to.getWidth() / 2.0F;
        float y2 = to.y + to.getHeight() / 2.0F;
        float dx = x2 - x1;
        float dy = y2 - y1;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length < 0.001F) {
            return;
        }
        float trimFrom = from.getWidth() / 2.0F - 2.0F;
        float trimTo = to.getWidth() / 2.0F - 2.0F;
        if (trimFrom + trimTo >= length) {
            return;
        }
        float ux = dx / length;
        float uy = dy / length;
        x1 += ux * trimFrom;
        y1 += uy * trimFrom;
        x2 -= ux * trimTo;
        y2 -= uy * trimTo;
        float px = -uy * width / 2.0F;
        float py = ux * width / 2.0F;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        buf.vertex(pose, x1 + px, y1 + py, 0.0F).color(r, g, b, alpha).endVertex();
        buf.vertex(pose, x2 + px, y2 + py, 0.0F).color(r, g, b, alpha).endVertex();
        buf.vertex(pose, x2 - px, y2 - py, 0.0F).color(r, g, b, alpha).endVertex();
        buf.vertex(pose, x1 - px, y1 - py, 0.0F).color(r, g, b, alpha).endVertex();
    }

    private void renderConstellationLabels(PoseStack renderStack) {
        Minecraft minecraft = Minecraft.getInstance();
        int color = (GodTreeTheme.accentDim(this.god) & 0xFFFFFF) | 0xAA000000;
        for (GodTreeModel.Label label : this.tree.getLabels()) {
            String text = label.text().toUpperCase(java.util.Locale.ROOT);
            int width = minecraft.font.width(text);
            minecraft.font.draw(renderStack, text, label.x() - width / 2.0F, label.y(), color);
        }
    }

    private void renderUnchartedSky(PoseStack renderStack) {
        String key = GodCharting.isCharted(this.god) ? "uncharted" : "uncharted.reputation";
        Component text = GodTreeTheme.lang(key, this.god.getName(), GodCharting.REPUTATION_REQUIRED)
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(GodTreeTheme.accent(this.god) & 0xFFFFFF)));
        Minecraft minecraft = Minecraft.getInstance();
        int width = minecraft.font.width(text);
        Vec2 center = this.viewportTranslation;
        minecraft.font.draw(renderStack, text.copy(), -center.x - width / 2.0F, -center.y - 4.0F, 0xFFFFFF);
    }
}
