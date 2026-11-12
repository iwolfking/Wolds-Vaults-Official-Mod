package xyz.iwolfking.woldsvaults.blocks.containers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.render.TextureRegion;
import iskallia.vault.client.render.TextureRegionRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.opengl.GL11;
import xyz.iwolfking.woldsvaults.blocks.tiles.ExtractorAssemblerTileEntity;
import net.minecraft.nbt.CompoundTag;
import xyz.iwolfking.woldsvaults.entities.extraction.client.ExtractorPreviewRenderer;
import xyz.iwolfking.woldsvaults.extraction.costs.CostLedger;
import xyz.iwolfking.woldsvaults.extraction.costs.ExtractorCosts;
import xyz.iwolfking.woldsvaults.extraction.jobs.ExtractorJobPreview;
import xyz.iwolfking.woldsvaults.extraction.parts.ExtractorPartType;
import xyz.iwolfking.woldsvaults.extraction.parts.ExtractorStat;
import xyz.iwolfking.woldsvaults.extraction.parts.ExtractorStats;
import xyz.iwolfking.woldsvaults.extraction.parts.ExtractorTooltips;
import xyz.iwolfking.woldsvaults.extraction.parts.PartData;
import xyz.iwolfking.woldsvaults.items.ExtractorBlueprintItem;
import xyz.iwolfking.woldsvaults.items.ExtractorItem;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The assembly diagram, framed like the other Vault Hunters tab screens: a panel inset from the window edges
 * with the live rig in the middle, part slots at the ends of leader lines, the stats and preview panel
 * bottom-left, the player's inventory bottom-centre and the dock plus Assemble button bottom-right.
 * Every region is laid out from the panel size; the panel always follows the window.
 */
public class ExtractorAssemblerScreen extends AbstractContainerScreen<ExtractorAssemblerContainer> {
    private static final int MARGIN = 20;
    private static final int BAND_HEIGHT = 126;
    private static final int PANEL_HEIGHT = 120;
    private static final int ASSEMBLY_WIDTH = 132;
    private static final int BUTTON_WIDTH = 108;
    private static final int BUTTON_HEIGHT = 26;
    private static final int INVENTORY_WIDTH = 162;

    private static final TextureRegion BORDER_CORNER_TOP_LEFT = new TextureRegion(0, 0, 15, 24);
    private static final TextureRegion BORDER_CORNER_TOP_RIGHT = new TextureRegion(18, 0, 15, 24);
    private static final TextureRegion BORDER_CORNER_BOTTOM_LEFT = new TextureRegion(0, 27, 15, 16);
    private static final TextureRegion BORDER_CORNER_BOTTOM_RIGHT = new TextureRegion(18, 27, 15, 16);
    private static final TextureRegion BORDER_TOP = new TextureRegion(16, 0, 1, 24);
    private static final TextureRegion BORDER_BOTTOM = new TextureRegion(16, 27, 1, 16);
    private static final TextureRegion BORDER_LEFT = new TextureRegion(0, 25, 15, 1);
    private static final TextureRegion BORDER_RIGHT = new TextureRegion(18, 25, 15, 1);

    private static final int BACKGROUND = 0xFF0B0D12;
    private static final int GRID = 0xFF11151C;
    private static final int FRAME = 0xFF3A4654;
    private static final int PANEL_FILL = 0xE0121720;
    private static final int PAINTING_FILL = 0xFF161B23;
    private static final int INK = 0xFFD8CBA0;
    private static final int INK_DIM = 0x99D8CBA0;
    private static final int TEXT_DIM = 0xFF8A8A8A;
    private static final int TEXT_DEAD = 0xFF4A5563;
    private static final int SLOT_FILL = 0xFF0E1218;
    private static final int SLOT_RING = 0xFF3A4654;
    private static final int SLOT_RING_HOVER = 0xFF8A8A8A;
    private static final int SLOT_DEAD_FILL = 0xFF1C1A22;
    private static final int SLOT_DEAD_CROSS = 0xD0FF5A5A;
    private static final int GHOST_WASH = 0xA0101418;
    private static final int RED_WASH = 0x66FF3030;
    private static final int RED_RING = 0xFFFF5A5A;
    private static final int GOOD = 0xFF6EF08C;
    private static final int BAD = 0xFFFF5A5A;
    private static final int GOLD = 0xFFFFAA00;
    private static final int BUTTON_VALID_FILL = 0xFF3D2F0C;
    private static final int BUTTON_INVALID_FILL = 0xFF161C24;
    private static final int TITLE = 0xFFE6DCC0;
    private static final float PAINTING_PITCH = -14.0F;
    private static final float PAINTING_YAW = -35.0F;
    private static final float RIG_BLOCKS_TALL = 7.5F;
    private static final float MODEL_UNITS_PER_BLOCK = 16.0F;
    private static final float RIG_PIVOT_BLOCKS = 2.0F;

    /**
     * Where each leader line ends, as a point of the rig in geo model units. Every point sits inside its
     * slot's own envelope band (art/extractor_rig/tools/envelopes.py) and on the side of the machine the
     * slot's label is written on, so the line lands on the part it names for every series.
     */
    private static final Map<ExtractorPartType, float[]> LEADER_POINTS = Map.of(
            ExtractorPartType.CHASSIS, new float[]{24.0F, 65.0F, 6.0F},
            ExtractorPartType.POWER_CORE, new float[]{30.0F, 54.0F, 6.0F},
            ExtractorPartType.LEGS, new float[]{28.0F, 12.0F, 28.0F},
            ExtractorPartType.SHIELD_ARRAY, new float[]{-21.0F, 76.0F, -12.0F},
            ExtractorPartType.RADIATOR, new float[]{-29.0F, 72.0F, -12.0F},
            ExtractorPartType.SCANNER, new float[]{0.0F, 88.0F, 0.0F},
            ExtractorPartType.DRILL, new float[]{-5.0F, 39.0F, -5.0F});

    private static final ExtractorStat[] MISC_ALWAYS = {ExtractorStat.QUANTITY, ExtractorStat.RARITY, ExtractorStat.DURABILITY_DRAIN_MULT};
    private static final Map<String, Integer> RARITY_KEY_COLORS = Map.of(
            "rare", 0xFFE24A, "epic", 0xC56BFF, "omega", 0x4EF08A, "mythic", 0xFF4E9A, "unique", 0xFFAA00, "ancient", 0xFF7A3D);

    @Nullable
    private ExtractorPreviewRenderer preview;
    private final List<ItemStack> lastSlots = new ArrayList<>();
    private List<PartData> previewParts = new ArrayList<>();
    private List<PartData> previewAttachments = new ArrayList<>();
    @Nullable
    private ExtractorStats.Block previewBlock;
    @Nullable
    private ExtractorJobPreview.Result jobPreview;
    private boolean previewDirty = true;

    private int bx0;
    private int by0;
    private int bw;
    private int bh;
    private int cx;
    private int midTop;
    private int midBottom;
    private int paintingX;
    private int paintingY;
    private int paintingW;
    private int paintingH;
    private int py;
    private int bandTop;
    private int panelX;
    private int panelW;
    private int invX;
    private int invY;
    private int assemblyX;
    private int buttonX;
    private int buttonY;
    private final int[] leaderAnchorX = new int[ExtractorAssemblerTileEntity.PART_SLOTS];
    private final int[] leaderAnchorY = new int[ExtractorAssemblerTileEntity.PART_SLOTS];

    public ExtractorAssemblerScreen(ExtractorAssemblerContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    @Override
    protected void init() {
        this.imageWidth = this.width;
        this.imageHeight = this.height;
        super.init();
        this.leftPos = 0;
        this.topPos = 0;
        this.layout();
        if (this.preview == null) {
            this.preview = new ExtractorPreviewRenderer();
        }
    }

    /** Positions every region and slot from the framed panel, which is the window minus a fixed margin. */
    private void layout() {
        this.bx0 = MARGIN;
        this.by0 = MARGIN;
        this.bw = this.width - 2 * MARGIN;
        this.bh = this.height - 2 * MARGIN;
        this.cx = this.width / 2;
        this.bandTop = this.by0 + this.bh - BAND_HEIGHT;
        this.midTop = this.by0 + 18;
        this.midBottom = this.bandTop - 6;
        int leftX = this.bx0 + 96;
        int rightX = this.bx0 + this.bw - 116;
        this.paintingW = Math.max(120, Math.min(240, rightX - leftX - 110));
        this.paintingH = Math.max(100, this.midBottom - this.midTop - 6);
        this.paintingX = this.cx - this.paintingW / 2;
        this.paintingY = this.midTop + 2;
        this.py = this.paintingY + this.paintingH / 2;
        int firstRow = this.midTop + 34;
        int step = Math.max(24, Math.min(60, (this.midBottom - firstRow - 20) / 2));
        int[] rows = {firstRow, firstRow + step, firstRow + 2 * step};
        this.placeSlot(this.menu.partSlot(ExtractorAssemblerTileEntity.slotFor(ExtractorPartType.CHASSIS)), leftX, rows[0]);
        this.placeSlot(this.menu.partSlot(ExtractorAssemblerTileEntity.slotFor(ExtractorPartType.POWER_CORE)), leftX, rows[1]);
        this.placeSlot(this.menu.partSlot(ExtractorAssemblerTileEntity.slotFor(ExtractorPartType.LEGS)), leftX, rows[2]);
        this.placeSlot(this.menu.partSlot(ExtractorAssemblerTileEntity.slotFor(ExtractorPartType.SCANNER)), this.cx - 10, this.midTop + 6);
        this.placeSlot(this.menu.partSlot(ExtractorAssemblerTileEntity.slotFor(ExtractorPartType.SHIELD_ARRAY)), rightX, rows[0]);
        this.placeSlot(this.menu.partSlot(ExtractorAssemblerTileEntity.slotFor(ExtractorPartType.RADIATOR)), rightX, rows[1]);
        this.placeSlot(this.menu.partSlot(ExtractorAssemblerTileEntity.slotFor(ExtractorPartType.DRILL)), rightX, rows[2]);
        for (int i = 0; i < ExtractorAssemblerTileEntity.ATTACHMENT_SLOTS; i++) {
            this.placeSlot(this.menu.attachmentSlot(i), this.cx + 44 + i * 24, this.midTop + 6);
        }
        this.panelX = this.bx0 + 4;
        this.assemblyX = this.bx0 + this.bw - 4 - ASSEMBLY_WIDTH;
        this.invX = this.assemblyX - 8 - INVENTORY_WIDTH;
        this.invY = this.bandTop + (BAND_HEIGHT - 76) / 2;
        this.panelW = Math.max(160, this.invX - 8 - this.panelX);
        this.buttonX = this.bx0 + this.bw - 4 - BUTTON_WIDTH;
        this.buttonY = this.bandTop + BAND_HEIGHT - BUTTON_HEIGHT - 4;
        this.placeSlot(this.menu.dockSlot(), this.assemblyX + 4, this.bandTop + 18);
        this.placeSlot(this.menu.repairSlot(), this.assemblyX + 4, this.bandTop + 60);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                Slot slot = this.menu.slots.get(ExtractorAssemblerContainer.PLAYER_SLOTS_START + row * 9 + col);
                slot.x = this.invX + col * 18 + 1;
                slot.y = this.invY + row * 18 + 1;
            }
        }
        for (int col = 0; col < 9; col++) {
            Slot slot = this.menu.slots.get(ExtractorAssemblerContainer.PLAYER_SLOTS_START + 27 + col);
            slot.x = this.invX + col * 18 + 1;
            slot.y = this.invY + 58 + 1;
        }
        LEADER_POINTS.forEach(this::anchor);
    }

    /**
     * Projects a point of the rig onto the plate with the same transform {@link ExtractorPreviewRenderer#draw}
     * draws it with, so the leader line ends on the part rather than near it. GeckoLib mirrors model X and the
     * entity renderer turns the rig a further 180 degrees about Y, so the two cancel on X and leave Z flipped.
     */
    private void anchor(ExtractorPartType type, float[] point) {
        int index = ExtractorAssemblerTileEntity.slotFor(type);
        float scale = this.paintingH / RIG_BLOCKS_TALL;
        float bx = point[0] / MODEL_UNITS_PER_BLOCK;
        float by = point[1] / MODEL_UNITS_PER_BLOCK - RIG_PIVOT_BLOCKS;
        float bz = -point[2] / MODEL_UNITS_PER_BLOCK;
        float yaw = (float) Math.toRadians(PAINTING_YAW);
        float pitch = (float) Math.toRadians(PAINTING_PITCH);
        float rx = bx * Mth.cos(yaw) + bz * Mth.sin(yaw);
        float rz = bz * Mth.cos(yaw) - bx * Mth.sin(yaw);
        float ry = by * Mth.cos(pitch) - rz * Mth.sin(pitch);
        this.leaderAnchorX[index] = Mth.clamp(this.cx - Math.round(rx * scale), this.paintingX + 2, this.paintingX + this.paintingW - 3);
        this.leaderAnchorY[index] = Mth.clamp(this.py - Math.round(ry * scale), this.paintingY + 2, this.paintingY + this.paintingH - 3);
    }

    /** {@code (x, y)} is the top-left of the 20x20 slot box; the vanilla slot interior sits one pixel in. */
    private void placeSlot(Slot slot, int x, int y) {
        slot.x = x + 1;
        slot.y = y + 1;
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        this.dropForeignWidgets();
        this.refreshPreview();
        this.renderBackground(pose);
        super.render(pose, mouseX, mouseY, partialTicks);
        this.renderOverlays(pose);
        this.renderTooltip(pose, mouseX, mouseY);
    }

    /**
     * This screen draws every control itself and registers no widgets of its own, so anything sitting in
     * the widget lists was bolted on by another mod after init -- FTB Library's sidebar buttons, Dark Mode
     * Everywhere's shader toggle -- and would render on top of the diagram. None of it is ours to keep.
     */
    private void dropForeignWidgets() {
        for (Widget widget : new ArrayList<>(this.renderables)) {
            if (widget instanceof GuiEventListener listener) {
                this.removeWidget(listener);
            }
        }
        for (GuiEventListener listener : new ArrayList<>(this.children())) {
            this.removeWidget(listener);
        }
    }

    @Override
    public void renderBackground(PoseStack pose) {
        super.renderBackground(pose);
        fill(pose, this.bx0, this.by0, this.bx0 + this.bw, this.by0 + this.bh, BACKGROUND);
        for (int x = this.bx0; x < this.bx0 + this.bw; x += 16) {
            fill(pose, x, this.by0, x + 1, this.by0 + this.bh, GRID);
        }
        for (int y = this.by0; y < this.by0 + this.bh; y += 16) {
            fill(pose, this.bx0, y, this.bx0 + this.bw, y + 1, GRID);
        }
        this.renderContainerBorders(pose);
    }

    private void renderContainerBorders(PoseStack pose) {
        RenderSystem.enableBlend();
        TextureRegionRenderer.getInstance().begin(ScreenTextures.UI_RESOURCE).with(pose)
                .draw(this.bx0 - 9, this.by0 - 18, BORDER_CORNER_TOP_LEFT)
                .draw(this.bx0 + this.bw - 7, this.by0 - 18, BORDER_CORNER_TOP_RIGHT)
                .draw(this.bx0 - 9, this.by0 + this.bh - 7, BORDER_CORNER_BOTTOM_LEFT)
                .draw(this.bx0 + this.bw - 7, this.by0 + this.bh - 7, BORDER_CORNER_BOTTOM_RIGHT)
                .push().translateXY(this.bx0 + 6, this.by0 - 18).scaleX(this.bw - 13).draw(BORDER_TOP)
                .translateY(this.bh + 11).draw(BORDER_BOTTOM).pop()
                .push().translateXY(this.bx0 - 9, this.by0 + 6).scaleY(this.bh - 13).draw(BORDER_LEFT)
                .translateX(this.bw + 2).draw(BORDER_RIGHT).pop()
                .end();
    }

    private void refreshPreview() {
        ExtractorAssemblerTileEntity tile = this.menu.getTile();
        if (tile == null) {
            return;
        }
        boolean changed = this.previewDirty || this.lastSlots.size() != ExtractorAssemblerTileEntity.SLOT_COUNT;
        for (int i = 0; i < ExtractorAssemblerTileEntity.SLOT_COUNT && !changed; i++) {
            changed = !ItemStack.matches(this.lastSlots.get(i), tile.getInventory().getStackInSlot(i));
        }
        if (!changed) {
            return;
        }
        this.lastSlots.clear();
        for (int i = 0; i < ExtractorAssemblerTileEntity.SLOT_COUNT; i++) {
            this.lastSlots.add(tile.getInventory().getStackInSlot(i).copy());
        }
        this.previewDirty = false;
        tile.invalidateGhosts();
        this.previewParts = tile.previewParts();
        this.previewAttachments = tile.previewAttachments();
        this.previewBlock = this.previewParts.isEmpty() && this.previewAttachments.isEmpty() ? null : ExtractorStats.compose(this.previewParts, this.previewAttachments);
        boolean complete = tile.isDocked() || this.previewParts.size() == ExtractorAssemblerTileEntity.PART_SLOTS;
        this.jobPreview = complete && this.previewBlock != null ? ExtractorJobPreview.of(this.previewBlock) : null;
    }

    @Override
    protected void renderBg(PoseStack pose, float partialTicks, int mouseX, int mouseY) {
        ExtractorAssemblerTileEntity tile = this.menu.getTile();
        this.renderPainting(pose, tile);
        this.renderTitle(pose);
        this.renderLeaders(pose);
        this.renderSlotBoxes(pose, mouseX, mouseY);
        this.renderPanel(pose, tile);
        this.renderInventoryBox(pose);
        this.renderAssemblyBlock(pose, tile, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(PoseStack pose, int mouseX, int mouseY) {
    }

    private void renderTitle(PoseStack pose) {
        String title = "Extractor Assembler";
        this.font.draw(pose, title, this.cx - this.font.width(title) / 2.0F, this.by0 + 5, TITLE);
    }

    /** The rig is clipped to its plate and its depth is cleared afterwards, so nothing drawn later can sink into it. */
    private void renderPainting(PoseStack pose, @Nullable ExtractorAssemblerTileEntity tile) {
        int x0 = this.paintingX;
        int y0 = this.paintingY;
        int x1 = x0 + this.paintingW;
        int y1 = y0 + this.paintingH;
        fill(pose, x0, y0, x1, y1, PAINTING_FILL);
        this.fillGradient(pose, x0, y0, x1, y0 + this.paintingH / 3, 0x30000000, 0x00000000);
        this.fillGradient(pose, x0, y1 - this.paintingH / 3, x1, y1, 0x00000000, 0x50000000);
        this.frame(pose, x0, y0, x1, y1, FRAME);
        if (this.preview == null) {
            return;
        }
        List<String> solid = new ArrayList<>();
        List<String> ghost = new ArrayList<>();
        List<String> blueprint = new ArrayList<>();
        boolean docked = tile != null && tile.isDocked();
        PartData chassis = ExtractorStats.find(this.previewParts, ExtractorPartType.CHASSIS);
        List<String> chassisTarget = solid;
        for (ExtractorPartType type : ExtractorPartType.CORE_PARTS) {
            PartData part = ExtractorStats.find(this.previewParts, type);
            int index = ExtractorAssemblerTileEntity.slotFor(type);
            boolean fromBlueprint = !docked && tile != null && tile.getInventory().getStackInSlot(index).getItem() instanceof ExtractorBlueprintItem;
            List<String> target = part == null ? ghost : fromBlueprint ? blueprint : solid;
            target.add(ExtractorStats.slotBone(part, type));
            if (type == ExtractorPartType.CHASSIS) {
                chassisTarget = target;
            }
        }
        int slots = this.previewBlock == null ? 0 : this.previewBlock.slots;
        chassisTarget.addAll(ExtractorStats.attachmentBones(chassis, this.previewAttachments, slots));
        float scale = this.paintingH / RIG_BLOCKS_TALL;
        this.scissor(x0 + 1, y0 + 1, x1 - 1, y1 - 1);
        this.preview.draw(this.cx, this.py, scale, PAINTING_YAW, PAINTING_PITCH, solid, ghost, blueprint);
        RenderSystem.disableScissor();
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        RenderSystem.enableBlend();
        this.fillGradient(pose, x0, y0, x1, y1, 0x14D8CBA0, 0x0AD8CBA0);
    }

    private void scissor(int x0, int y0, int x1, int y1) {
        double guiScale = Minecraft.getInstance().getWindow().getGuiScale();
        int windowHeight = Minecraft.getInstance().getWindow().getHeight();
        RenderSystem.enableScissor((int) (x0 * guiScale), (int) (windowHeight - y1 * guiScale), (int) ((x1 - x0) * guiScale), (int) ((y1 - y0) * guiScale));
    }

    private void renderLeaders(PoseStack pose) {
        int busLeft = this.paintingX - 14;
        int busRight = this.paintingX + this.paintingW + 14;
        for (int index = 0; index < ExtractorAssemblerTileEntity.PART_SLOTS; index++) {
            ExtractorPartType type = ExtractorPartType.CORE_PARTS[index];
            Slot slot = this.menu.partSlot(index);
            int sx = slot.x + 8;
            int sy = slot.y + 8;
            int ax = this.leaderAnchorX[index];
            int ay = this.leaderAnchorY[index];
            if (type == ExtractorPartType.SCANNER) {
                this.vline(pose, sx, sy + 10, ay);
                this.hline(pose, sx, ax, ay);
            } else if (type == ExtractorPartType.DRILL) {
                int under = this.paintingY + this.paintingH + 3;
                this.hline(pose, busRight, sx - 10, sy);
                this.vline(pose, busRight, sy, under);
                this.hline(pose, ax, busRight, under);
                this.vline(pose, ax, ay, under);
            } else if (sx < this.cx) {
                this.hline(pose, sx + 10, busLeft, sy);
                this.vline(pose, busLeft, Math.min(sy, ay), Math.max(sy, ay));
                this.hline(pose, busLeft, ax, ay);
            } else {
                this.hline(pose, busRight, sx - 10, sy);
                this.vline(pose, busRight, Math.min(sy, ay), Math.max(sy, ay));
                this.hline(pose, ax, busRight, ay);
            }
            fill(pose, ax - 1, ay - 1, ax + 2, ay + 2, INK);
            this.renderSlotLabel(pose, index, slot, sx < this.cx || type == ExtractorPartType.SCANNER);
        }
        Slot first = this.menu.attachmentSlot(0);
        this.font.draw(pose, "Hardpoints", first.x - 1, first.y - 11, INK_DIM);
    }

    private void renderSlotLabel(PoseStack pose, int index, Slot slot, boolean leftSide) {
        ExtractorPartType type = ExtractorPartType.CORE_PARTS[index];
        String name = type.getDisplayName();
        ItemStack stack = slot.getItem();
        PartData part = ExtractorAssemblerTileEntity.partIn(stack);
        String detail;
        int color;
        if (part == null) {
            detail = "Empty";
            color = TEXT_DEAD;
        } else {
            boolean blueprint = stack.getItem() instanceof ExtractorBlueprintItem;
            detail = (blueprint ? "Blueprint: " : "") + part.name + String.format("  Q%.1f", part.quality);
            color = blueprint ? ExtractorTooltips.BLUEPRINT_COLOR | 0xFF000000 : part.rarity.getColor().getValue() | 0xFF000000;
        }
        if (type == ExtractorPartType.SCANNER) {
            int x = slot.x - 6;
            int room = x - (this.bx0 + 4);
            this.drawFit(pose, name, x, slot.y - 1, room, 1.0F, INK, true);
            this.drawFit(pose, detail, x, slot.y + 9, room, 0.75F, color, true);
            return;
        }
        if (leftSide) {
            int right = slot.x - 6;
            int room = right - (this.bx0 + 4);
            this.drawFit(pose, name, right, slot.y - 1, room, 1.0F, INK, true);
            this.drawFit(pose, detail, right, slot.y + 9, room, 0.75F, color, true);
        } else {
            int x = slot.x + 24;
            int room = this.bx0 + this.bw - 4 - x;
            this.drawFit(pose, name, x, slot.y - 1, room, 1.0F, INK, false);
            this.drawFit(pose, detail, x, slot.y + 9, room, 0.75F, color, false);
        }
    }

    private void renderSlotBoxes(PoseStack pose, int mouseX, int mouseY) {
        for (int index = 0; index <= ExtractorAssemblerTileEntity.REPAIR_SLOT; index++) {
            Slot slot = this.menu.slots.get(index);
            if (index == ExtractorAssemblerTileEntity.REPAIR_SLOT && !this.menu.isDocked() && !slot.hasItem()) {
                continue;
            }
            boolean dock = index == ExtractorAssemblerTileEntity.OUTPUT_SLOT;
            int x0 = slot.x - 1 - (dock ? 3 : 0);
            int y0 = slot.y - 1 - (dock ? 3 : 0);
            int x1 = slot.x + 17 + (dock ? 3 : 0);
            int y1 = slot.y + 17 + (dock ? 3 : 0);
            boolean hovered = mouseX >= x0 && mouseX < x1 && mouseY >= y0 && mouseY < y1;
            if (!slot.isActive()) {
                fill(pose, x0, y0, x1, y1, SLOT_DEAD_FILL);
                this.frame(pose, x0, y0, x1, y1, SLOT_RING);
                this.cross(pose, x0 + 4, y0 + 4, x1 - 4, y1 - 4, SLOT_DEAD_CROSS);
                continue;
            }
            fill(pose, x0, y0, x1, y1, SLOT_FILL);
            this.frame(pose, x0, y0, x1, y1, hovered ? SLOT_RING_HOVER : SLOT_RING);
        }
    }

    private void renderInventoryBox(PoseStack pose) {
        int x0 = this.invX - 4;
        int y0 = this.invY - 4;
        int x1 = this.invX + INVENTORY_WIDTH + 4;
        int y1 = this.invY + 76 + 4;
        fill(pose, x0, y0, x1, y1, PANEL_FILL);
        this.frame(pose, x0, y0, x1, y1, FRAME);
        for (int i = 0; i < ExtractorAssemblerContainer.PLAYER_SLOT_COUNT; i++) {
            Slot slot = this.menu.slots.get(ExtractorAssemblerContainer.PLAYER_SLOTS_START + i);
            fill(pose, slot.x - 1, slot.y - 1, slot.x + 17, slot.y + 17, SLOT_FILL);
            this.frame(pose, slot.x - 1, slot.y - 1, slot.x + 17, slot.y + 17, 0xFF2A323D);
        }
    }

    private void renderPanel(PoseStack pose, @Nullable ExtractorAssemblerTileEntity tile) {
        int x0 = this.panelX;
        int y0 = this.bandTop + (BAND_HEIGHT - PANEL_HEIGHT) / 2;
        int x1 = x0 + this.panelW;
        int y1 = y0 + PANEL_HEIGHT;
        fill(pose, x0, y0, x1, y1, PANEL_FILL);
        this.frame(pose, x0, y0, x1, y1, FRAME);
        boolean docked = tile != null && tile.isDocked();
        int partCount = this.previewParts.size();
        List<String> header = new ArrayList<>();
        List<Integer> headerColors = new ArrayList<>();
        if (docked || (this.previewBlock != null && partCount == ExtractorAssemblerTileEntity.PART_SLOTS)) {
            String series = this.previewBlock == null ? "" : this.previewBlock.frameSeries;
            int seriesColor = ExtractorTooltips.seriesColor(series) | 0xFF000000;
            header.add(ExtractorTooltips.extractorName(series));
            headerColors.add(seriesColor);
            if (this.previewBlock != null) {
                header.add(" (" + shortDurability(this.previewBlock.get(ExtractorStat.DURABILITY)) + ")");
                headerColors.add(ExtractorStat.DURABILITY.getColor() | 0xFF000000);
            }
            if (!docked) {
                header.add(" (Preview)");
                headerColors.add(seriesColor);
            }
        } else if (partCount > 0 || !this.previewAttachments.isEmpty()) {
            header.add("Incomplete Assembly");
            headerColors.add(INK);
        } else {
            header.add("Empty Assembly");
            headerColors.add(TEXT_DIM);
        }
        String right = docked && tile != null
                ? this.previewAttachments.size() + " / " + tile.hardpoints() + " Hardpoints"
                : partCount + " / " + ExtractorAssemblerTileEntity.PART_SLOTS + " Parts";
        int rightWidth = this.font.width(right);
        this.drawFitRuns(pose, header, headerColors, x0 + 6, y0 + 5, this.panelW - 18 - rightWidth, 1.0F);
        this.font.draw(pose, right, x1 - 6 - rightWidth, y0 + 5, partCount == ExtractorAssemblerTileEntity.PART_SLOTS || docked ? GOOD : TEXT_DIM);
        int colW = (this.panelW - 12) / ExtractorTooltips.GROUPS.length;
        for (int group = 0; group < ExtractorTooltips.GROUPS.length; group++) {
            int x = x0 + 6 + group * colW;
            this.drawFit(pose, ExtractorTooltips.GROUP_NAMES[group], x, y0 + 17, colW - 4, 0.8F, INK, false);
            List<String> lines = new ArrayList<>();
            List<Integer> colors = new ArrayList<>();
            this.groupRows(group, lines, colors);
            for (int row = 0; row < lines.size() && row < 6; row++) {
                this.drawFit(pose, lines.get(row), x, y0 + 27 + row * 8, colW - 4, 0.8F, colors.get(row), false);
            }
        }
        fill(pose, x0 + 6, y0 + 79, x1 - 6, y0 + 80, FRAME);
        this.renderPreviewBlock(pose, tile, x0, y0 + 83, x1);
    }

    /** The rows of one stat group: the group's fixed stats always (dead ones greyed), the optional Misc stats only when non-zero. */
    private void groupRows(int group, List<String> lines, List<Integer> colors) {
        ExtractorStats.Block block = this.previewBlock;
        for (ExtractorStat stat : ExtractorTooltips.GROUPS[group]) {
            String line = block == null ? null : ExtractorTooltips.blockStatText(block, stat);
            boolean fixed = group < 3 || isMiscAlways(stat);
            if (line == null && !fixed) {
                continue;
            }
            lines.add(line == null ? "- " + stat.getDisplayName() : line);
            colors.add(line == null ? TEXT_DEAD : stat.getColor() | 0xFF000000);
        }
    }

    private static boolean isMiscAlways(ExtractorStat stat) {
        for (ExtractorStat always : MISC_ALWAYS) {
            if (always == stat) {
                return true;
            }
        }
        return false;
    }

    private void renderPreviewBlock(PoseStack pose, @Nullable ExtractorAssemblerTileEntity tile, int x0, int y, int x1) {
        int labelX = x0 + 6;
        int valueX = x0 + 36;
        int room = x1 - 6 - valueX;
        float scale = 0.85F;
        if (this.jobPreview == null) {
            if (tile != null && tile.isDocked()) {
                this.drawFit(pose, "This extractor's drill class names no job; it cannot deploy.", labelX, y + 2, x1 - 6 - labelX, scale, BAD, false);
                return;
            }
            int n = this.previewParts.size();
            if (n == ExtractorAssemblerTileEntity.PART_SLOTS) {
                this.drawFit(pose, "The drill class names no job; check extractor_parts.json.", labelX, y + 2, x1 - 6 - labelX, scale, BAD, false);
                return;
            }
            this.drawFit(pose, "Preview requires all 7 parts (" + n + "/7 installed).", labelX, y + 2, x1 - 6 - labelX, scale, TEXT_DIM, false);
            this.drawFit(pose, "Stats shown reflect the installed parts only.", labelX, y + 11, x1 - 6 - labelX, scale, TEXT_DIM, false);
            return;
        }
        ExtractorJobPreview.Result job = this.jobPreview;
        int labelColor = ExtractorTooltips.SLOT_COLOR | 0xFF000000;
        this.drawFit(pose, "Job", labelX, y + 2, 28, scale, labelColor, false);
        String rank = job.job.minRank > 0 ? " · Rank " + job.job.minRank + " Req." : "";
        this.drawFit(pose, ExtractorTooltips.titleCase(job.jobId) + " · " + ExtractorTooltips.drillClassName(job.drillClass) + " Drill · " + job.job.minPower + " Power Req." + rank,
                valueX, y + 2, room, scale, 0xFFFFFFFF, false);
        this.drawFit(pose, "Time", labelX, y + 11, 28, scale, labelColor, false);
        if (!job.deployable) {
            this.drawFit(pose, "Cannot deploy: Power " + job.totalPower + " is below the " + job.job.minPower + " required.", valueX, y + 11, room, scale, BAD, false);
        } else {
            String cells = job.cells == 1 ? " Power Cell" : " Power Cells";
            this.drawFit(pose, ExtractorJobPreview.formatTime(job.ticks) + " · " + ExtractorTooltips.formatNumber(job.energy) + " Energy · " + job.cells + cells,
                    valueX, y + 11, room, scale, 0xFFFFFFFF, false);
        }
        this.drawFit(pose, "Yield", labelX, y + 20, 28, scale, labelColor, false);
        String tier = job.tierMax > 0 ? " · Tier " + job.tierMin + "-" + job.tierMax : "";
        String yield = String.format("%.1f-%.1f ", job.yieldMin, job.yieldMax) + job.unit + tier + (job.pending ? " (Pending)" : "");
        this.drawFit(pose, yield, valueX, y + 20, room, scale, job.pending ? TEXT_DIM : 0xFFFFFFFF, false);
        if (job.shares.isEmpty()) {
            return;
        }
        this.drawFit(pose, "Odds", labelX, y + 29, 28, scale, labelColor, false);
        List<String> segments = new ArrayList<>();
        List<Integer> segmentColors = new ArrayList<>();
        float total = 0.0F;
        for (Map.Entry<String, Double> entry : job.shares.entrySet()) {
            if (!segments.isEmpty()) {
                segments.add(" · ");
                segmentColors.add(TEXT_DIM);
                total += this.font.width(" · ");
            }
            String text = ExtractorTooltips.titleCase(entry.getKey()) + " " + Math.round(entry.getValue() * 100.0D) + "%";
            segments.add(text);
            segmentColors.add(RARITY_KEY_COLORS.getOrDefault(entry.getKey(), 0xFFFFFF) | 0xFF000000);
            total += this.font.width(text);
        }
        float fit = Math.min(scale, room / Math.max(1.0F, total));
        float x = valueX;
        for (int i = 0; i < segments.size(); i++) {
            this.drawScaled(pose, segments.get(i), x, y + 29, fit, segmentColors.get(i));
            x += this.font.width(segments.get(i)) * fit;
        }
    }

    private void renderAssemblyBlock(PoseStack pose, @Nullable ExtractorAssemblerTileEntity tile, int mouseX, int mouseY) {
        int x0 = this.assemblyX;
        int y0 = this.bandTop;
        this.font.draw(pose, "Assembly", x0, y0 + 4, INK_DIM);
        Slot dock = this.menu.dockSlot();
        boolean docked = tile != null && tile.isDocked();
        String dockLabel = docked ? "Docked" : "Output";
        this.drawScaled(pose, dockLabel, dock.x + 8 - this.font.width(dockLabel) * 0.75F / 2.0F, dock.y + 22, 0.75F, INK_DIM);
        int textX = x0 + 36;
        int room = this.bx0 + this.bw - 4 - textX;
        int parts = tile == null ? 0 : tile.filledPartSlots();
        int blueprints = tile == null ? 0 : tile.blueprintCount();
        int hardpoints = tile == null ? 0 : tile.hardpoints();
        if (docked) {
            this.drawFit(pose, "Extractor docked", textX, y0 + 18, room, 0.85F, GOOD, false);
            this.drawFit(pose, "Attachments " + this.previewAttachments.size() + " / " + hardpoints, textX, y0 + 27, room, 0.85F, 0xFFFFFFFF, false);
            CompoundTag data = ExtractorItem.getData(tile.getDocked());
            int durability = data.getInt("Durability");
            int maxDurability = data.getInt("MaxDurability");
            this.drawFit(pose, "Durability " + shortDurability(durability) + " / " + shortDurability(maxDurability), textX, y0 + 36, room, 0.85F,
                    durability >= maxDurability ? GOOD : GOLD, false);
            Slot repairSlot = this.menu.repairSlot();
            this.drawScaled(pose, "Repair", repairSlot.x + 8 - this.font.width("Repair") * 0.75F / 2.0F, repairSlot.y + 19, 0.75F, INK_DIM);
            CompoundTag ledger = data.getCompound(ExtractorAssemblerTileEntity.REPAIR_TAG);
            if (durability < maxDurability && CostLedger.hasCost(ledger)) {
                int paidLines = 0;
                List<ExtractorCosts.CostLine> lines = CostLedger.lines(ledger);
                for (ExtractorCosts.CostLine line : lines) {
                    if (CostLedger.paid(ledger, line) >= line.amount()) {
                        paidLines++;
                    }
                }
                this.drawFit(pose, "Repair paid " + paidLines + " / " + lines.size() + " lines", textX, y0 + 62, room, 0.85F,
                        paidLines == lines.size() ? GOOD : 0xFFFFFFFF, false);
                this.drawFit(pose, "Hover the button for the list", textX, y0 + 71, room, 0.75F, TEXT_DIM, false);
            }
        } else {
            this.drawFit(pose, "Parts " + parts + " / " + ExtractorAssemblerTileEntity.PART_SLOTS, textX, y0 + 18, room, 0.85F, parts == ExtractorAssemblerTileEntity.PART_SLOTS ? GOOD : 0xFFFFFFFF, false);
            this.drawFit(pose, "Blueprints " + blueprints, textX, y0 + 27, room, 0.85F, blueprints > 0 ? BAD : 0xFFFFFFFF, false);
            this.drawFit(pose, "Hardpoints " + hardpoints + " available", textX, y0 + 36, room, 0.85F, 0xFFFFFFFF, false);
        }
        List<String> refusals = tile == null ? List.of("No assembler found.") : this.buttonRefusals(tile);
        boolean valid = refusals.isEmpty();
        int bx1 = this.buttonX + BUTTON_WIDTH;
        int by1 = this.buttonY + BUTTON_HEIGHT;
        boolean hovered = this.isOverButton(mouseX, mouseY);
        fill(pose, this.buttonX, this.buttonY, bx1, by1, valid ? BUTTON_VALID_FILL : BUTTON_INVALID_FILL);
        this.frame(pose, this.buttonX, this.buttonY, bx1, by1, valid ? GOLD : FRAME);
        if (valid) {
            this.frame(pose, this.buttonX + 1, this.buttonY + 1, bx1 - 1, by1 - 1, hovered ? 0xFFFFD27A : GOLD);
        }
        String label = docked ? "Repair" : "Assemble";
        this.font.draw(pose, label, this.buttonX + (BUTTON_WIDTH - this.font.width(label)) / 2.0F, this.buttonY + 9, valid ? (hovered ? 0xFFFFE9B0 : GOLD) : TEXT_DEAD);
    }

    /** The main button assembles, or repairs while an extractor is docked. */
    private List<String> buttonRefusals(ExtractorAssemblerTileEntity tile) {
        if (tile.isDocked()) {
            String refusal = tile.repairRefusal();
            return refusal == null ? List.of() : List.of(refusal);
        }
        return tile.assemblyRefusals();
    }

    private boolean isOverButton(double mouseX, double mouseY) {
        return mouseX >= this.buttonX && mouseX < this.buttonX + BUTTON_WIDTH && mouseY >= this.buttonY && mouseY < this.buttonY + BUTTON_HEIGHT;
    }

    /** Drawn above the items: the ghost wash on docked parts and the red refusal rings while an extractor is carried. */
    private void renderOverlays(PoseStack pose) {
        ExtractorAssemblerTileEntity tile = this.menu.getTile();
        if (tile == null) {
            return;
        }
        pose.pushPose();
        pose.translate(0.0D, 0.0D, 350.0D);
        RenderSystem.enableBlend();
        if (tile.isDocked()) {
            for (int i = 0; i < ExtractorAssemblerTileEntity.PART_SLOTS; i++) {
                Slot slot = this.menu.partSlot(i);
                if (slot.hasItem()) {
                    fill(pose, slot.x, slot.y, slot.x + 16, slot.y + 16, GHOST_WASH);
                }
            }
        }
        if (this.dockRefused(tile)) {
            for (int i = 0; i < ExtractorAssemblerTileEntity.OUTPUT_SLOT; i++) {
                Slot slot = this.menu.slots.get(i);
                if (slot.hasItem()) {
                    fill(pose, slot.x, slot.y, slot.x + 16, slot.y + 16, RED_WASH);
                    this.frame(pose, slot.x - 1, slot.y - 1, slot.x + 17, slot.y + 17, RED_RING);
                }
            }
            Slot dock = this.menu.dockSlot();
            fill(pose, dock.x, dock.y, dock.x + 16, dock.y + 16, RED_WASH);
            this.frame(pose, dock.x - 4, dock.y - 4, dock.x + 20, dock.y + 20, RED_RING);
        }
        pose.popPose();
    }

    /** True while the cursor carries a dockable extractor that the occupied slots keep out. */
    private boolean dockRefused(ExtractorAssemblerTileEntity tile) {
        ItemStack carried = this.menu.getCarried();
        return ExtractorAssemblerTileEntity.isDockable(carried) && !ExtractorAssemblerTileEntity.assemblyEmpty(tile.getInventory());
    }

    @Override
    protected void renderTooltip(PoseStack pose, int mouseX, int mouseY) {
        ExtractorAssemblerTileEntity tile = this.menu.getTile();
        if (this.isOverButton(mouseX, mouseY) && tile != null && tile.isDocked()) {
            List<Component> lines = new ArrayList<>();
            String refusal = tile.repairRefusal();
            lines.add(refusal == null ? new TextComponent("Repair Extractor").withStyle(ChatFormatting.GOLD) : new TextComponent(refusal).withStyle(ChatFormatting.RED));
            CompoundTag ledger = ExtractorItem.getData(tile.getDocked()).getCompound(ExtractorAssemblerTileEntity.REPAIR_TAG);
            for (ExtractorCosts.CostLine line : CostLedger.lines(ledger)) {
                long paid = Math.min(CostLedger.paid(ledger, line), line.amount());
                lines.add(new TextComponent("  ").append(line.displayName().copy().withStyle(ChatFormatting.WHITE))
                        .append(new TextComponent(String.format(": %,d / %,d%s", paid, line.amount(), line.fluid() ? " mB" : ""))
                                .withStyle(paid >= line.amount() ? ChatFormatting.GREEN : ChatFormatting.GRAY)));
            }
            this.renderComponentTooltip(pose, lines, mouseX, mouseY);
            return;
        }
        if (this.isOverButton(mouseX, mouseY) && tile != null) {
            List<String> refusals = tile.assemblyRefusals();
            List<Component> lines = new ArrayList<>();
            if (refusals.isEmpty()) {
                lines.add(new TextComponent("Assemble Extractor").withStyle(ChatFormatting.GOLD));
                lines.add(ExtractorTooltips.hint("The parts and attachments are consumed."));
            } else {
                lines.add(new TextComponent("Cannot Assemble").withStyle(ChatFormatting.RED));
                for (String reason : refusals) {
                    lines.add(new TextComponent("  " + reason).withStyle(ChatFormatting.RED));
                }
            }
            this.renderComponentTooltip(pose, lines, mouseX, mouseY);
            return;
        }
        Slot hovered = this.hoveredSlot;
        if (hovered != null && hovered.index == ExtractorAssemblerTileEntity.REPAIR_SLOT && !hovered.hasItem()) {
            this.renderComponentTooltip(pose, List.of(new TextComponent("Repair materials").withStyle(ChatFormatting.WHITE),
                    ExtractorTooltips.hint("Items and filled buckets here pay the docked extractor's repair cost.")), mouseX, mouseY);
            return;
        }
        if (hovered != null && hovered.index <= ExtractorAssemblerTileEntity.OUTPUT_SLOT && tile != null) {
            List<Component> lines = this.slotTooltip(tile, hovered);
            if (lines != null) {
                this.renderComponentTooltip(pose, lines, mouseX, mouseY);
                return;
            }
        }
        if (hovered == null && tile != null) {
            for (int i = ExtractorAssemblerTileEntity.ATTACHMENT_SLOT_START; i < ExtractorAssemblerTileEntity.OUTPUT_SLOT; i++) {
                Slot slot = this.menu.slots.get(i);
                if (!slot.isActive() && mouseX >= slot.x - 1 && mouseX < slot.x + 17 && mouseY >= slot.y - 1 && mouseY < slot.y + 17) {
                    this.renderComponentTooltip(pose, List.of(new TextComponent("No hardpoint available on this chassis.").withStyle(ChatFormatting.RED)), mouseX, mouseY);
                    return;
                }
            }
        }
        super.renderTooltip(pose, mouseX, mouseY);
    }

    @Nullable
    private List<Component> slotTooltip(ExtractorAssemblerTileEntity tile, Slot slot) {
        boolean docked = tile.isDocked();
        int index = slot.index;
        if (index == ExtractorAssemblerTileEntity.OUTPUT_SLOT) {
            ItemStack carried = this.menu.getCarried();
            if (this.dockRefused(tile)) {
                return List.of(new TextComponent("Remove the parts and attachments before docking an extractor.").withStyle(ChatFormatting.RED),
                        ExtractorTooltips.hint("The highlighted slots must be empty."));
            }
            if (carried.getItem() instanceof ExtractorItem && ExtractorItem.isDeployed(carried)) {
                return List.of(new TextComponent("A deployed extractor cannot be docked.").withStyle(ChatFormatting.RED));
            }
            if (!slot.hasItem()) {
                return List.of(new TextComponent("Output").withStyle(ChatFormatting.WHITE),
                        ExtractorTooltips.hint("The assembled extractor is placed here."),
                        ExtractorTooltips.hint("Place an assembled extractor here to manage its attachments."));
            }
            return null;
        }
        if (index < ExtractorAssemblerTileEntity.PART_SLOTS) {
            if (slot.hasItem()) {
                return null;
            }
            String name = ExtractorPartType.CORE_PARTS[index].getDisplayName();
            return List.of(new TextComponent(name).withStyle(ChatFormatting.WHITE),
                    ExtractorTooltips.hint("Accepts a " + name.toLowerCase() + " part or an identified blueprint."),
                    ExtractorTooltips.hint("Blueprints preview the result and cannot be assembled."));
        }
        int hardpoint = index - ExtractorAssemblerTileEntity.ATTACHMENT_SLOT_START;
        ItemStack carried = this.menu.getCarried();
        if (docked && !carried.isEmpty()) {
            String refusal = tile.attachmentRefusal(hardpoint, carried);
            if (refusal != null) {
                return List.of(new TextComponent(refusal).withStyle(ChatFormatting.RED));
            }
        }
        if (slot.hasItem()) {
            return null;
        }
        return List.of(new TextComponent("Hardpoint " + (hardpoint + 1)).withStyle(ChatFormatting.WHITE),
                ExtractorTooltips.hint(docked ? "Place an attachment here to install it." : "Accepts one attachment, installed at assembly."));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && this.isOverButton(mouseX, mouseY) && this.minecraft != null && this.minecraft.gameMode != null) {
            ExtractorAssemblerTileEntity tile = this.menu.getTile();
            if (tile != null && tile.isDocked()) {
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, ExtractorAssemblerContainer.BUTTON_REPAIR);
            } else if (tile != null && tile.assemblyRefusals().isEmpty()) {
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, ExtractorAssemblerContainer.BUTTON_ASSEMBLE);
                this.previewDirty = true;
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton) {
        return false;
    }

    /**
     * The extractor's wear pool written short enough to sit beside its name: 112,400 becomes {@code 112k}.
     * The whole number would run the header past the panel on any well-built rig.
     */
    private static String shortDurability(float value) {
        long rounded = Math.round(value);
        if (rounded < 1000L) {
            return String.valueOf(rounded);
        }
        if (rounded < 10000L) {
            return String.format("%.1fk", rounded / 1000.0D);
        }
        return Math.round(rounded / 1000.0D) + "k";
    }

    /** Draws consecutive coloured runs as one line, shrinking them together so the whole line fits {@code maxWidth}. */
    private void drawFitRuns(PoseStack pose, List<String> runs, List<Integer> colors, int x, int y, int maxWidth, float baseScale) {
        float total = 0.0F;
        for (String run : runs) {
            total += this.font.width(run);
        }
        float scale = Math.min(baseScale, Math.max(1, maxWidth) / Math.max(1.0F, total));
        float cursor = x;
        for (int i = 0; i < runs.size(); i++) {
            this.drawScaled(pose, runs.get(i), cursor, y, scale, colors.get(i));
            cursor += this.font.width(runs.get(i)) * scale;
        }
    }

    /** Draws at {@code baseScale}, shrinking further whenever the text would otherwise overrun {@code maxWidth}. */
    private void drawFit(PoseStack pose, String text, int x, int y, int maxWidth, float baseScale, int color, boolean rightAlign) {
        int width = Math.max(1, this.font.width(text));
        float scale = Math.min(baseScale, (float) Math.max(1, maxWidth) / width);
        float drawn = width * scale;
        this.drawScaled(pose, text, rightAlign ? x - drawn : x, y, scale, color);
    }

    private void drawScaled(PoseStack pose, String text, float x, float y, float scale, int color) {
        pose.pushPose();
        pose.translate(x, y, 0.0D);
        pose.scale(scale, scale, 1.0F);
        this.font.draw(pose, text, 0.0F, 0.0F, color);
        pose.popPose();
    }

    private void frame(PoseStack pose, int x0, int y0, int x1, int y1, int color) {
        fill(pose, x0, y0, x1, y0 + 1, color);
        fill(pose, x0, y1 - 1, x1, y1, color);
        fill(pose, x0, y0, x0 + 1, y1, color);
        fill(pose, x1 - 1, y0, x1, y1, color);
    }

    private void cross(PoseStack pose, int x0, int y0, int x1, int y1, int color) {
        int n = Math.min(x1 - x0, y1 - y0);
        for (int i = 0; i < n; i++) {
            fill(pose, x0 + i, y0 + i, x0 + i + 1, y0 + i + 1, color);
            fill(pose, x1 - 1 - i, y0 + i, x1 - i, y0 + i + 1, color);
        }
    }

    private void hline(PoseStack pose, int xa, int xb, int y) {
        fill(pose, Math.min(xa, xb), y, Math.max(xa, xb) + 1, y + 1, INK_DIM);
    }

    private void vline(PoseStack pose, int x, int ya, int yb) {
        fill(pose, x, Math.min(ya, yb), x + 1, Math.max(ya, yb) + 1, INK_DIM);
    }
}
