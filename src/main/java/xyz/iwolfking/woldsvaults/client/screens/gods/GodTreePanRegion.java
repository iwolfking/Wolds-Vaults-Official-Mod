package xyz.iwolfking.woldsvaults.client.screens.gods;

import com.mojang.blaze3d.platform.InputConstants;
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
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Mth;
import net.minecraft.Util;
import net.minecraft.world.phys.Vec2;
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

    private static final float FOCUS_SCALE = 0.7F;
    private static final long FOCUS_MILLIS = 450L;

    private static final int FIT_INSET_LEFT = 28;
    private static final int FIT_INSET_TOP = 32;
    private static final int FIT_INSET = 10;
    private static final float FIT_PADDING = 24.0F;

    private static final float RING_GAP = 3.0F;
    private static final float RING_BREATH = 1.0F;
    private static final float RING_BREATH_RATE = 0.5F;
    private static final int RING_SEGMENTS = 64;

    private static final float LINKED_CONE = (float) Math.toRadians(70.0D);
    private static final float OPEN_CONE = (float) Math.toRadians(50.0D);

    private record Glide(float fromScale, float toScale, Vec2 fromOffset, Vec2 point, long start) {}
    private Glide glide;

    protected final GodTreeScreen parentScreen;
    protected final GodTreeView view;
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
    private boolean fitPending;

    public GodTreePanRegion(GodTreeScreen parentScreen, GodTreeView view, GodTreeDialog dialog, VaultGod god) {
        this.parentScreen = parentScreen;
        this.view = view;
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
        this.tree = GodCharting.isCharted(this.view, this.god) ? GodNodeRegistry.tree(this.god).orElse(null) : null;
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
            boolean unlocked = this.view.isTreeNodePurchased(this.god, node.id());
            boolean available = this.tree.isPurchasable(node.id(),
                    id -> this.view.isTreeNodePurchased(this.god, id));
            GodNodeWidget widget = this.createWidget(node, style, unlocked, available);
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

    protected GodNodeWidget createWidget(GodNode node, SkillStyle style, boolean unlocked, boolean available) {
        return new GodNodeWidget(node, style, this.god, unlocked, available);
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
        Vec2 translation = persistedTranslations.get(this.god);
        Float scale = persistedScales.get(this.god);
        if (translation == null || scale == null) {
            this.resetView();
            return;
        }
        this.viewportTranslation = translation;
        this.viewportScale = scale;
    }

    protected void saveViewportTransforms() {
        persistedTranslations.put(this.god, this.viewportTranslation);
        persistedScales.put(this.god, this.viewportScale);
    }

    public void resetView() {
        this.glide = null;
        Rectangle bounds = this.getBounds();
        if (bounds.width <= 0 || bounds.height <= 0) {
            this.fitPending = true;
            return;
        }
        this.fitPending = false;
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        for (GodNodeWidget widget : this.nodeWidgets.values()) {
            minX = Math.min(minX, widget.x);
            minY = Math.min(minY, widget.y);
            maxX = Math.max(maxX, widget.x + widget.getWidth());
            maxY = Math.max(maxY, widget.y + widget.getHeight());
        }
        if (this.tree != null) {
            for (GodTreeModel.Label label : this.tree.getLabels()) {
                float half = Minecraft.getInstance().font.width(label.text().toUpperCase(java.util.Locale.ROOT)) / 2.0F;
                minX = Math.min(minX, label.x() - half);
                minY = Math.min(minY, label.y());
                maxX = Math.max(maxX, label.x() + half);
                maxY = Math.max(maxY, label.y() + Minecraft.getInstance().font.lineHeight);
            }
        }
        if (minX > maxX) {
            this.viewportTranslation = new Vec2(0.0F, 0.0F);
            this.viewportScale = this.clampViewportScale(0.45F);
            this.saveViewportTransforms();
            return;
        }
        float roomWidth = bounds.width - FIT_INSET_LEFT - FIT_INSET;
        float roomHeight = bounds.height - FIT_INSET_TOP - FIT_INSET;
        float scale = this.clampViewportScale(Math.min(roomWidth / (maxX - minX + FIT_PADDING * 2.0F),
                roomHeight / (maxY - minY + FIT_PADDING * 2.0F)));
        float shiftX = (FIT_INSET_LEFT - FIT_INSET) / 2.0F;
        float shiftY = (FIT_INSET_TOP - FIT_INSET) / 2.0F;
        this.viewportScale = scale;
        this.viewportTranslation = new Vec2(shiftX / scale - (minX + maxX) / 2.0F, shiftY / scale - (minY + maxY) / 2.0F);
        this.saveViewportTransforms();
    }

    protected float clampViewportScale(float scale) {
        return Math.max(0.1F, Math.min(2.0F, scale));
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
            this.select(hit);
            if (button == 0) {
                this.focusOn(hit);
            }
            if (button == 1 && hit.getNode().type() == GodNodeType.MINOR && hit.isUnlocked() && this.view.isLive()) {
                this.popup = new TransferSlotPopup(this.view, this.god, hit.getNode());
            }
            return true;
        }
        if (button == 0) {
            this.grabbedPos = new Vec2((float) mouseX, (float) mouseY);
            this.dragging = true;
            this.glide = null;
        }
        return true;
    }

    private void select(GodNodeWidget widget) {
        if (this.selectedWidget != null) {
            this.selectedWidget.deselect();
        }
        this.selectedWidget = widget;
        widget.select();
        this.dialog.setSelectedNode(widget.getNode().id());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_F8) {
            GodNodeEffects.togglePath();
            return true;
        }
        float dirX;
        float dirY;
        switch (keyCode) {
            case InputConstants.KEY_LEFT -> { dirX = -1.0F; dirY = 0.0F; }
            case InputConstants.KEY_RIGHT -> { dirX = 1.0F; dirY = 0.0F; }
            case InputConstants.KEY_UP -> { dirX = 0.0F; dirY = -1.0F; }
            case InputConstants.KEY_DOWN -> { dirX = 0.0F; dirY = 1.0F; }
            default -> {
                return false;
            }
        }
        if (this.nodeWidgets.isEmpty()) {
            return false;
        }
        GodNodeWidget next;
        if (this.selectedWidget == null) {
            next = this.nearestToViewCentre();
        } else {
            List<GodNodeWidget> linked = new ArrayList<>();
            for (String id : this.tree.getAdjacent(this.selectedWidget.getNode().id())) {
                GodNodeWidget widget = this.nodeWidgets.get(id);
                if (widget != null) {
                    linked.add(widget);
                }
            }
            next = this.closestToward(this.selectedWidget, dirX, dirY, linked, LINKED_CONE);
            if (next == null) {
                next = this.closestToward(this.selectedWidget, dirX, dirY, this.nodeWidgets.values(), OPEN_CONE);
            }
        }
        if (next != null) {
            this.popup = null;
            this.select(next);
            this.focusOn(next);
        }
        return true;
    }

    @Nullable
    private GodNodeWidget nearestToViewCentre() {
        GodNodeWidget nearest = null;
        float best = Float.MAX_VALUE;
        for (GodNodeWidget widget : this.nodeWidgets.values()) {
            float dx = widget.x + widget.getWidth() / 2.0F + this.viewportTranslation.x;
            float dy = widget.y + widget.getHeight() / 2.0F + this.viewportTranslation.y;
            float distance = dx * dx + dy * dy;
            if (distance < best) {
                best = distance;
                nearest = widget;
            }
        }
        return nearest;
    }

    @Nullable
    private GodNodeWidget closestToward(GodNodeWidget from, float dirX, float dirY, Iterable<GodNodeWidget> candidates, float cone) {
        float fromX = from.x + from.getWidth() / 2.0F;
        float fromY = from.y + from.getHeight() / 2.0F;
        GodNodeWidget closest = null;
        float best = Float.MAX_VALUE;
        for (GodNodeWidget widget : candidates) {
            if (widget == from) {
                continue;
            }
            float dx = widget.x + widget.getWidth() / 2.0F - fromX;
            float dy = widget.y + widget.getHeight() / 2.0F - fromY;
            float along = dx * dirX + dy * dirY;
            if (along <= 0.0F) {
                continue;
            }
            float angle = (float) Math.atan2(Math.abs(dx * dirY - dy * dirX), along);
            if (angle > cone) {
                continue;
            }
            float cost = (float) Math.sqrt(dx * dx + dy * dy) * (1.0F + 2.0F * angle);
            if (cost < best) {
                best = cost;
                closest = widget;
            }
        }
        return closest;
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
            this.glide = null;
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
        if (this.fitPending) {
            this.resetView();
        }
        this.updateGlide();
        RenderSystem.enableBlend();
        float tick = (float) ClientScheduler.INSTANCE.getTick() + pTicks;
        GodTreeBackdrop.forGod(this.god).render(renderStack, containerBounds, this.viewportTranslation, this.viewportScale, tick);

        RenderSystem.enableBlend();
        Point2D.Float midpoint = MiscUtils.getMidpoint(this.getBounds());
        renderStack.pushPose();
        renderStack.translate(midpoint.x, midpoint.y, 0.0D);
        renderStack.scale(this.viewportScale, this.viewportScale, 1.0F);
        renderStack.translate(this.viewportTranslation.x, this.viewportTranslation.y, 0.0D);
        int containerMouseX = (int) (((float) mouseX - midpoint.x) / this.viewportScale - this.viewportTranslation.x);
        int containerMouseY = (int) (((float) mouseY - midpoint.y) / this.viewportScale - this.viewportTranslation.y);
        this.renderBackgroundStars(renderStack, tick);
        if (this.tree == null) {
            this.renderUnchartedSky(renderStack);
            renderStack.popPose();
            return;
        }
        this.renderDecorations(renderStack, false);
        this.renderConstellationLines(renderStack);
        this.renderConstellationLabels(renderStack);
        this.renderNodeEffects(renderStack, false);
        for (GodNodeWidget widget : this.nodeWidgets.values()) {
            widget.renderWidget(renderStack, containerBounds, mouseX, mouseY, containerMouseX, containerMouseY, pTicks, postRender);
        }
        this.renderNodeEffects(renderStack, true);
        this.renderStartRings(renderStack);
        for (GodNodeWidget widget : this.nodeWidgets.values()) {
            widget.renderIcon(renderStack);
        }
        this.renderDecorations(renderStack, true);
        if (this.popup != null) {
            GodNodeWidget anchor = this.nodeWidgets.get(this.popup.anchorNodeId());
            if (anchor != null) {
                this.popup.render(renderStack, anchor, this.tree, containerMouseX, containerMouseY, mouseX, mouseY, postRender);
            }
        }
        renderStack.popPose();
    }

    private void renderDecorations(PoseStack renderStack, boolean front) {
        float[] view = this.visibleArea();
        GodTreeDecorations.render(renderStack, this.god, front, view[0], view[1], view[2], view[3]);
    }

    private void renderNodeEffects(PoseStack renderStack, boolean front) {
        float[] view = this.visibleArea();
        GodNodeEffects.render(renderStack, this.god, this.nodeWidgets.values(), front,
                view[0], view[1], view[2], view[3], this.viewportScale);
    }

    private void renderStartRings(PoseStack renderStack) {
        float screenPerUnit = this.viewportScale * (float) Minecraft.getInstance().getWindow().getGuiScale();
        float width = Math.max(1.0F, 1.0F / screenPerUnit);
        float seconds = (Util.getMillis() % 3_600_000L) / 1000.0F;
        float breath = 0.5F - 0.5F * Mth.cos((float) (Math.PI * 2.0D) * RING_BREATH_RATE * seconds);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        Matrix4f pose = renderStack.last().pose();
        ScreenDrawHelper.draw(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR, buf -> {
            for (GodNodeWidget widget : this.nodeWidgets.values()) {
                if (!widget.getNode().isRoot()) {
                    continue;
                }
                float cx = widget.x + widget.getWidth() / 2.0F;
                float cy = widget.y + widget.getHeight() / 2.0F;
                float inner = widget.getWidth() / 2.0F + RING_GAP;
                if (widget.isUnlocked()) {
                    this.ring(buf, pose, cx, cy, inner, width, 0.55F);
                    this.ring(buf, pose, cx, cy, inner + RING_GAP, width, 0.35F);
                } else {
                    this.ring(buf, pose, cx, cy, inner, width, 0.9F);
                    this.ring(buf, pose, cx, cy, inner + RING_GAP + RING_BREATH * breath, width, 0.5F + 0.4F * breath);
                }
            }
        });
        RenderSystem.enableTexture();
    }

    private void ring(BufferBuilder buf, Matrix4f pose, float cx, float cy, float radius, float width, float alpha) {
        int a = Math.round(alpha * 255.0F);
        float in = radius - width / 2.0F;
        float out = radius + width / 2.0F;
        for (int i = 0; i < RING_SEGMENTS; i++) {
            float a0 = (float) (Math.PI * 2.0D) * i / RING_SEGMENTS;
            float a1 = (float) (Math.PI * 2.0D) * (i + 1) / RING_SEGMENTS;
            float cos0 = Mth.cos(a0);
            float sin0 = Mth.sin(a0);
            float cos1 = Mth.cos(a1);
            float sin1 = Mth.sin(a1);
            buf.vertex(pose, cx + cos0 * in, cy + sin0 * in, 0.0F).color(255, 255, 255, a).endVertex();
            buf.vertex(pose, cx + cos1 * in, cy + sin1 * in, 0.0F).color(255, 255, 255, a).endVertex();
            buf.vertex(pose, cx + cos1 * out, cy + sin1 * out, 0.0F).color(255, 255, 255, a).endVertex();
            buf.vertex(pose, cx + cos0 * out, cy + sin0 * out, 0.0F).color(255, 255, 255, a).endVertex();
        }
    }

    private float[] visibleArea() {
        Rectangle bounds = this.getBounds();
        float halfWidth = bounds.width / 2.0F / this.viewportScale;
        float halfHeight = bounds.height / 2.0F / this.viewportScale;
        return new float[]{-halfWidth - this.viewportTranslation.x, -halfHeight - this.viewportTranslation.y,
                halfWidth - this.viewportTranslation.x, halfHeight - this.viewportTranslation.y};
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

    private void renderConstellationLines(PoseStack renderStack) {
        float[] view = this.visibleArea();
        GodConstellationLines.render(renderStack, this.god, this.edges, this.nodeWidgets,
                view[0], view[1], view[2], view[3], this.viewportScale);
    }

    /**
     * Draws the constellation names. A font batch clears the depth test that
     * {@link UIHelper#renderOverflowHidden} clips this region with, so it is restored afterwards -
     * without it every star drawn later escapes the star chart and paints over the whole screen.
     */
    private void renderConstellationLabels(PoseStack renderStack) {
        Minecraft minecraft = Minecraft.getInstance();
        int color = (GodTreeTheme.accentDim(this.god) & 0xFFFFFF) | 0xAA000000;
        for (GodTreeModel.Label label : this.tree.getLabels()) {
            String text = label.text().toUpperCase(java.util.Locale.ROOT);
            int width = minecraft.font.width(text);
            minecraft.font.draw(renderStack, text, label.x() - width / 2.0F, label.y(), color);
        }
        RenderSystem.enableDepthTest();
    }

    private void renderUnchartedSky(PoseStack renderStack) {
        String key = GodCharting.isCharted(this.view, this.god) ? "uncharted" : "uncharted.reputation";
        Component text = GodTreeTheme.lang(key, this.god.getName(), GodCharting.reputationRequired())
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(GodTreeTheme.accent(this.god) & 0xFFFFFF)));
        Minecraft minecraft = Minecraft.getInstance();
        int width = minecraft.font.width(text);
        Vec2 center = this.viewportTranslation;
        minecraft.font.draw(renderStack, text.copy(), -center.x - width / 2.0F, -center.y - 4.0F, 0xFFFFFF);
        RenderSystem.enableDepthTest();
    }

    private void focusOn(GodNodeWidget widget) {
        Vec2 point = new Vec2(widget.x + widget.getWidth() / 2.0F, widget.y + widget.getHeight() / 2.0F);
        Vec2 offset = new Vec2(this.viewportScale * (point.x + this.viewportTranslation.x),
                this.viewportScale * (point.y + this.viewportTranslation.y));
        float toScale = this.clampViewportScale(Math.max(this.viewportScale, FOCUS_SCALE));
        this.glide = new Glide(this.viewportScale, toScale, offset, point, Util.getMillis());
    }

    private void updateGlide() {
        if (this.glide == null) {
            return;
        }
        Glide g = this.glide;
        float t = Mth.clamp((Util.getMillis() - g.start()) / (float) FOCUS_MILLIS, 0.0F, 1.0F);
        float e = 1.0F - (1.0F - t) * (1.0F - t) * (1.0F - t);
        this.viewportScale = g.fromScale() * (float) Math.pow(g.toScale() / g.fromScale(), e);
        float offsetX = g.fromOffset().x * (1.0F - e), offsetY = g.fromOffset().y * (1.0F - e);
        this.viewportTranslation = new Vec2(offsetX / this.viewportScale - g.point().x,
                offsetY / this.viewportScale - g.point().y);
        if (t >= 1.0F) {
            this.glide = null;
            this.saveViewportTransforms();
        }
    }
}
