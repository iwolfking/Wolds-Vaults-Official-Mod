package xyz.iwolfking.woldsvaults.blocks.containers;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.blocks.tiles.ExtractorForgeTileEntity;
import xyz.iwolfking.woldsvaults.client.screens.IndustrialStyle;
import xyz.iwolfking.woldsvaults.entities.extraction.client.ExtractorPreviewRenderer;
import xyz.iwolfking.woldsvaults.extraction.costs.CostLedger;
import xyz.iwolfking.woldsvaults.extraction.costs.ExtractorCosts;
import xyz.iwolfking.woldsvaults.extraction.parts.ExtractorPartType;
import xyz.iwolfking.woldsvaults.extraction.parts.ExtractorStat;
import xyz.iwolfking.woldsvaults.extraction.parts.ExtractorStats;
import xyz.iwolfking.woldsvaults.extraction.parts.ExtractorTooltips;
import xyz.iwolfking.woldsvaults.extraction.parts.PartData;
import xyz.iwolfking.woldsvaults.items.ExtractorBlueprintItem;
import xyz.iwolfking.woldsvaults.items.ExtractorPartItem;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Extraction Forge screen in the ornate industrial style. Top: title plate, the blueprint slot on a crest plate, the
 * Stage / Fabricate button and a status readout. Left rail: the seven deposit slots, each showing its ledger line's
 * item as a ghost (a tick once paid). Centre: the cost ledger, then the part's stats beside a turning 3D preview of
 * the part on the rig. Right rail: FE and fluid gauges. Bottom: materials and lock readout, the player inventory,
 * and build time and FE/t. While fabricating, both side edges glow like a forge. Layout spec:
 * {@code library/ui_preview/screens/extraction_forge_v2.py}.
 */
public class ExtractorForgeScreen extends AbstractContainerScreen<ExtractorForgeContainer> {
    private static final int W = ExtractorForgeContainer.IMAGE_WIDTH;
    private static final int H = ExtractorForgeContainer.IMAGE_HEIGHT;
    private static final int M = ExtractorForgeContainer.MARGIN;
    private static final int RAIL_W = ExtractorForgeContainer.RAIL_W;
    private static final int LRAIL_X0 = M;
    private static final int LRAIL_X1 = M + RAIL_W;
    private static final int RRAIL_X0 = W - M - RAIL_W;
    private static final int RRAIL_X1 = W - M;
    private static final int CX0 = LRAIL_X1 + 4;
    private static final int CX1 = RRAIL_X0 - 4;
    private static final int CX = ExtractorForgeContainer.CENTER_X;
    private static final int TOP_Y0 = 8;
    private static final int TOP_Y1 = 32;
    private static final int RAIL_Y0 = ExtractorForgeContainer.RAIL_Y0;
    private static final int RAIL_Y1 = 169;
    private static final int LEDGER_Y0 = 35;
    private static final int ROW_H = 10;
    private static final int VISIBLE_ROWS = 8;
    private static final int LEDGER_Y1 = LEDGER_Y0 + 3 + VISIBLE_ROWS * ROW_H + 3;
    private static final int BAND_Y0 = LEDGER_Y1 + 3;
    private static final int BAND_Y1 = RAIL_Y1;
    private static final int PREVIEW_W = 66;
    private static final int STATS_X1 = CX1 - PREVIEW_W - 4;
    private static final int BUTTON_X = CX + 18;
    private static final int BUTTON_W = 56;
    private static final int STATUS_X0 = BUTTON_X + BUTTON_W + 4;
    private static final int GAUGE_X = RRAIL_X0 + (RAIL_W - 28) / 2;
    private static final int GAUGE_Y0 = RAIL_Y0 + 13;
    private static final int GAUGE_Y1 = RAIL_Y1 - 3;
    private static final int INV_X = ExtractorForgeContainer.INVENTORY_X;
    private static final int INV_Y = ExtractorForgeContainer.INVENTORY_Y;
    private static final int READOUT_L_X1 = INV_X - 6;
    private static final int READOUT_R_X0 = INV_X + 162 + 5;
    private static final int BP_X = ExtractorForgeContainer.BLUEPRINT_X;
    private static final int BP_Y = ExtractorForgeContainer.BLUEPRINT_Y;
    private static final int CREST = 0xFF7A8086;
    private static final int LOCKED_SLOT = 0xFF5A5F64;
    private static final int TICK = 0xFF3FBF5A;
    private static final int UNUSED_ICON = 0xFF5B6168;
    private static final int FE_ICON = 0xFFFF5A4A;
    private static final int RULE = 0xFF3A4046;
    private static final int FLOOR_HI = 0xFF56606A;
    private static final int FLOOR_LO = 0xFF3A4249;
    private static final int ACTIVE_TINT = 0x55FF8A2A;
    private static final int CREST_GLOW = 0x66FF7A1E;
    private static final float RIG_BLOCKS_TALL = 7.5F;
    private static final float PREVIEW_PITCH = -14.0F;
    private static final Map<ExtractorStat, String> SHORT_NAMES = Map.ofEntries(
            Map.entry(ExtractorStat.ENERGY_CAPACITY, "Capacity"),
            Map.entry(ExtractorStat.ENERGY_PER_CELL, "Recharge"),
            Map.entry(ExtractorStat.DRAIN, "Drain"),
            Map.entry(ExtractorStat.SHIELD_TOUGHNESS, "Toughness"),
            Map.entry(ExtractorStat.SHIELD_REGEN, "Shld Regen"),
            Map.entry(ExtractorStat.HEALTH_REGEN, "Regen"),
            Map.entry(ExtractorStat.QUANTITY, "Quantity"),
            Map.entry(ExtractorStat.RARITY, "Rarity"),
            Map.entry(ExtractorStat.EFFECT_RESISTANCE, "Effect Res"),
            Map.entry(ExtractorStat.ELITE_RESISTANCE, "Elite Res"),
            Map.entry(ExtractorStat.HEALING_EFFICIENCY, "Healing"),
            Map.entry(ExtractorStat.TURRET, "Turret"));
    private static boolean warnedPreview;

    private ExtractorPreviewRenderer preview;
    private int scroll;

    private enum State {
        IDLE("IDLE", IndustrialStyle.TEXT_DIM),
        UNIDENTIFIED("CHECK", IndustrialStyle.AMBER),
        PREVIEW("PREVIEW", IndustrialStyle.TEXT),
        STAGED("STAGED", IndustrialStyle.AMBER),
        READY("READY", IndustrialStyle.GREEN),
        ACTIVE("ACTIVE", IndustrialStyle.EMBER_TEXT),
        DONE("DONE", IndustrialStyle.GREEN);

        final String label;
        final int color;

        State(String label, int color) {
            this.label = label;
            this.color = color;
        }
    }

    public ExtractorForgeScreen(ExtractorForgeContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
        this.imageWidth = W;
        this.imageHeight = H;
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(new IndustrialStyle.MachineButton(this.leftPos + BUTTON_X, this.topPos + TOP_Y0 + 2, BUTTON_W, 20,
                this::buttonLabel, this::buttonStyle, button -> {
                    State state = this.state();
                    int id = state == State.READY ? ExtractorForgeContainer.BUTTON_FABRICATE : ExtractorForgeContainer.BUTTON_STAGE;
                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
                }));
    }

    private ItemStack slotStack() {
        return this.menu.getSlot(ExtractorForgeTileEntity.BLUEPRINT_SLOT).getItem();
    }

    private State state() {
        if (this.menu.isFabricating()) {
            return State.ACTIVE;
        }
        if (this.menu.isStaged()) {
            return CostLedger.isComplete(this.menu.stagedLedger()) ? State.READY : State.STAGED;
        }
        ItemStack stack = this.slotStack();
        if (stack.getItem() instanceof ExtractorPartItem) {
            return State.DONE;
        }
        if (stack.getItem() instanceof ExtractorBlueprintItem) {
            return ExtractorBlueprintItem.isIdentified(stack) ? State.PREVIEW : State.UNIDENTIFIED;
        }
        return State.IDLE;
    }

    private String buttonLabel() {
        State state = this.state();
        return state == State.READY || state == State.ACTIVE ? "Fabricate" : "Stage";
    }

    private IndustrialStyle.ButtonStyle buttonStyle() {
        return switch (this.state()) {
            case READY -> IndustrialStyle.ButtonStyle.GREEN;
            case PREVIEW -> IndustrialStyle.ButtonStyle.STEEL;
            default -> IndustrialStyle.ButtonStyle.DISABLED;
        };
    }

    /** The blueprint whose ledger and stats the screen shows: the staged one, else an identified one in the slot. */
    private ItemStack shownBlueprint() {
        if (this.menu.isStaged()) {
            return this.menu.staged();
        }
        ItemStack stack = this.slotStack();
        return stack.getItem() instanceof ExtractorBlueprintItem && ExtractorBlueprintItem.isIdentified(stack) ? stack : ItemStack.EMPTY;
    }

    @Nullable
    private CompoundTag shownLedger() {
        ItemStack blueprint = this.shownBlueprint();
        return blueprint.isEmpty() ? null : ExtractorForgeTileEntity.stagedLedger(blueprint);
    }

    @Nullable
    private PartData shownPart() {
        ItemStack blueprint = this.shownBlueprint();
        if (!blueprint.isEmpty()) {
            return ExtractorBlueprintItem.getPart(blueprint);
        }
        ItemStack stack = this.slotStack();
        return stack.getItem() instanceof ExtractorPartItem ? ExtractorPartItem.getPart(stack) : null;
    }

    /** Item lines first (in deposit-slot order), then fluid lines. */
    private static List<ExtractorCosts.CostLine> ledgerRows(@Nullable CompoundTag ledger) {
        List<ExtractorCosts.CostLine> rows = new ArrayList<>(ExtractorForgeContainer.itemLines(ledger));
        for (ExtractorCosts.CostLine line : CostLedger.lines(ledger)) {
            if (line.fluid()) {
                rows.add(line);
            }
        }
        return rows;
    }

    private static List<ExtractorCosts.CostLine> fluidLines(@Nullable CompoundTag ledger) {
        List<ExtractorCosts.CostLine> out = new ArrayList<>();
        for (ExtractorCosts.CostLine line : CostLedger.lines(ledger)) {
            if (line.fluid()) {
                out.add(line);
            }
        }
        return out;
    }

    private static ItemStack fluidIcon(ExtractorCosts.CostLine line) {
        Fluid fluid = line.fluidType();
        return fluid == null || fluid.getBucket() == Items.AIR ? new ItemStack(Items.BUCKET) : new ItemStack(fluid.getBucket());
    }

    /** The fluid's tint, else its block's map colour, else water blue. */
    private static int[] fluidShades(ExtractorCosts.CostLine line) {
        Fluid fluid = line.fluidType();
        if (fluid == null) {
            return IndustrialStyle.WATER_GAUGE;
        }
        int tint = fluid.getAttributes().getColor();
        if ((tint & 0xFFFFFF) != 0xFFFFFF) {
            return IndustrialStyle.shades(tint & 0xFFFFFF);
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            MaterialColor map = fluid.defaultFluidState().createLegacyBlock().getMapColor(mc.level, BlockPos.ZERO);
            if (map != null && map != MaterialColor.NONE) {
                return IndustrialStyle.shades(map.col);
            }
        }
        return IndustrialStyle.WATER_GAUGE;
    }

    private static String fluidName(ExtractorCosts.CostLine line) {
        Fluid fluid = line.fluidType();
        return fluid == null ? line.id() : new FluidStack(fluid, 1000).getDisplayName().getString();
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(pose);
        super.render(pose, mouseX, mouseY, partialTicks);
        this.renderItems();
        this.renderTooltip(pose, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack pose, float partialTicks, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        State state = this.state();
        boolean active = state == State.ACTIVE;
        boolean locked = this.menu.isStaged();

        IndustrialStyle.panel(pose, x, y, x + W, y + H);
        if (active) {
            float pulse = 0.85F + 0.15F * Mth.sin((Util.getMillis() % 2000L) / 2000.0F * Mth.TWO_PI);
            IndustrialStyle.emberGlow(pose, x + 2, y + 20, x + 7, y + H - 20, pulse);
            IndustrialStyle.emberGlow(pose, x + W - 7, y + 20, x + W - 2, y + H - 20, pulse);
        }
        IndustrialStyle.bracket(pose, x + 1, y + 1, 1, 1);
        IndustrialStyle.bracket(pose, x + W - 1, y + 1, -1, 1);
        IndustrialStyle.bracket(pose, x + 1, y + H - 1, 1, -1);
        IndustrialStyle.bracket(pose, x + W - 1, y + H - 1, -1, -1);

        IndustrialStyle.plate(pose, x + CX0, y + TOP_Y0 + 2, x + CX - 17, y + TOP_Y1 - 2);
        IndustrialStyle.bolt(pose, x + CX0 + 3, y + TOP_Y0 + 10);
        IndustrialStyle.bolt(pose, x + CX - 24, y + TOP_Y0 + 10);
        IndustrialStyle.plate(pose, x + CX - 14, y + TOP_Y0, x + CX + 14, y + TOP_Y1, CREST);
        if (active) {
            fill(pose, x + CX - 12, y + TOP_Y0 + 2, x + CX + 12, y + TOP_Y1 - 2, CREST_GLOW);
        }
        IndustrialStyle.slot(pose, x + BP_X, y + BP_Y, locked ? LOCKED_SLOT : IndustrialStyle.SLOT_FILL);
        if (locked) {
            this.lockIcon(pose, x + BP_X, y + BP_Y);
        }
        IndustrialStyle.display(pose, x + STATUS_X0, y + TOP_Y0 + 2, x + CX1, y + TOP_Y1 - 2);

        IndustrialStyle.plate(pose, x + LRAIL_X0, y + RAIL_Y0, x + LRAIL_X1, y + RAIL_Y1);
        for (int i = 0; i < ExtractorForgeTileEntity.DEPOSIT_SLOTS; i++) {
            IndustrialStyle.slot(pose, x + ExtractorForgeContainer.DEPOSIT_X, y + ExtractorForgeContainer.DEPOSIT_Y0 + i * ExtractorForgeContainer.DEPOSIT_PITCH);
        }

        CompoundTag ledger = this.shownLedger();
        IndustrialStyle.plate(pose, x + RRAIL_X0, y + RAIL_Y0, x + RRAIL_X1, y + RAIL_Y1);
        this.boltIcon(pose, x + GAUGE_X, y + RAIL_Y0 + 3);
        int capacity = this.menu.data(ExtractorForgeTileEntity.DATA_CAPACITY);
        int energy = this.menu.data(ExtractorForgeTileEntity.DATA_ENERGY);
        IndustrialStyle.gauge(pose, x + GAUGE_X, y + GAUGE_Y0, x + GAUGE_X + 8, y + GAUGE_Y1,
                capacity > 0 ? (float) energy / capacity : 0.0F, IndustrialStyle.FE_GAUGE, -1.0F);
        List<ExtractorCosts.CostLine> fluids = fluidLines(ledger);
        for (int i = 0; i < 2; i++) {
            int gx = x + GAUGE_X + 10 * (i + 1);
            if (i < fluids.size()) {
                ExtractorCosts.CostLine line = fluids.get(i);
                float fraction = line.amount() > 0 ? (float) CostLedger.paid(ledger, line) / line.amount() : 0.0F;
                IndustrialStyle.gauge(pose, gx, y + GAUGE_Y0, gx + 8, y + GAUGE_Y1, fraction, fluidShades(line), -1.0F);
            } else {
                this.droplet(pose, gx, y + RAIL_Y0 + 3, UNUSED_ICON);
                IndustrialStyle.gauge(pose, gx, y + GAUGE_Y0, gx + 8, y + GAUGE_Y1, 0.0F, IndustrialStyle.WATER_GAUGE, -1.0F);
            }
        }

        IndustrialStyle.display(pose, x + CX0, y + LEDGER_Y0, x + CX1, y + LEDGER_Y1);
        IndustrialStyle.display(pose, x + CX0, y + BAND_Y0, x + STATS_X1, y + BAND_Y1);
        IndustrialStyle.display(pose, x + CX1 - PREVIEW_W, y + BAND_Y0, x + CX1, y + BAND_Y1);
        if (this.shownPart() != null) {
            int pcx = x + (CX1 - PREVIEW_W + CX1) / 2;
            int floor = y + BAND_Y1 - 8;
            fill(pose, pcx - 22, floor - 1, pcx + 22, floor, FLOOR_HI);
            fill(pose, pcx - 19, floor, pcx + 19, floor + 1, FLOOR_LO);
            fill(pose, pcx - 15, floor + 1, pcx + 15, floor + 2, FLOOR_LO);
        }

        IndustrialStyle.display(pose, x + LRAIL_X0, y + INV_Y - 1, x + READOUT_L_X1, y + H - M);
        IndustrialStyle.display(pose, x + READOUT_R_X0, y + INV_Y - 1, x + RRAIL_X1, y + H - M);
        List<ExtractorCosts.CostLine> rows = ledgerRows(ledger);
        int done = 0;
        for (ExtractorCosts.CostLine line : rows) {
            if (CostLedger.remaining(ledger, line) <= 0) {
                done++;
            }
        }
        IndustrialStyle.hbar(pose, x + LRAIL_X0 + 4, y + H - M - 11, x + INV_X - 10, y + H - M - 5,
                rows.isEmpty() ? 0.0F : (float) done / rows.size(), IndustrialStyle.PAID_BAR);
        int total = this.menu.data(ExtractorForgeTileEntity.DATA_TOTAL_TICKS);
        int progress = this.menu.data(ExtractorForgeTileEntity.DATA_PROGRESS);
        float jobFraction = active && total > 0 ? (float) progress / total : state == State.DONE ? 1.0F : 0.0F;
        IndustrialStyle.hbar(pose, x + READOUT_R_X0 + 4, y + H - M - 11, x + RRAIL_X1 - 4, y + H - M - 5, jobFraction, IndustrialStyle.EMBER_BAR);

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 9; col++) {
                IndustrialStyle.slot(pose, x + INV_X + col * 18, y + (row < 3 ? INV_Y + row * 18 : ExtractorForgeContainer.HOTBAR_Y));
            }
        }

        this.renderPreview(x, y, active);
    }

    private void renderPreview(int x, int y, boolean active) {
        PartData part = this.shownPart();
        if (part == null) {
            return;
        }
        if (this.preview == null) {
            try {
                this.preview = new ExtractorPreviewRenderer();
            } catch (RuntimeException e) {
                if (!warnedPreview) {
                    warnedPreview = true;
                    WoldsVaults.LOGGER.error("Extraction Forge could not create its 3D preview renderer; the preview box stays empty.", e);
                }
                return;
            }
        }
        List<String> solid = new ArrayList<>();
        List<String> ghost = new ArrayList<>();
        if (part.type == ExtractorPartType.ATTACHMENT) {
            solid.addAll(ExtractorStats.attachmentBones(null, List.of(part), 1));
        } else {
            solid.add(ExtractorStats.slotBone(part, part.type));
        }
        for (ExtractorPartType type : ExtractorPartType.CORE_PARTS) {
            if (type != part.type) {
                ghost.add(ExtractorStats.slotBone(null, type));
            }
        }
        int x0 = x + CX1 - PREVIEW_W + 1;
        int y0 = y + BAND_Y0 + 1;
        int x1 = x + CX1 - 1;
        int y1 = y + BAND_Y1 - 1;
        float yaw = (Util.getMillis() % 12000L) / 12000.0F * 360.0F;
        this.scissor(x0, y0, x1, y1);
        this.preview.draw((x0 + x1) / 2, (y0 + y1) / 2, (y1 - y0 - 2) / RIG_BLOCKS_TALL, yaw, PREVIEW_PITCH, solid, ghost, List.of());
        RenderSystem.disableScissor();
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        Lighting.setupFor3DItems();
        if (active) {
            RenderSystem.enableBlend();
            fill(new PoseStack(), x0, y0, x1, y1, ACTIVE_TINT);
        }
    }

    private void scissor(int x0, int y0, int x1, int y1) {
        double guiScale = Minecraft.getInstance().getWindow().getGuiScale();
        int windowHeight = Minecraft.getInstance().getWindow().getHeight();
        RenderSystem.enableScissor((int) (x0 * guiScale), (int) (windowHeight - y1 * guiScale), (int) ((x1 - x0) * guiScale), (int) ((y1 - y0) * guiScale));
    }

    private void lockIcon(PoseStack pose, int sx, int sy) {
        fill(pose, sx + 5, sy + 7, sx + 11, sy + 13, IndustrialStyle.ENGRAVE_DARK);
        fill(pose, sx + 6, sy + 3, sx + 10, sy + 4, IndustrialStyle.ENGRAVE_DARK);
        fill(pose, sx + 5, sy + 4, sx + 6, sy + 7, IndustrialStyle.ENGRAVE_DARK);
        fill(pose, sx + 10, sy + 4, sx + 11, sy + 7, IndustrialStyle.ENGRAVE_DARK);
        fill(pose, sx + 7, sy + 9, sx + 9, sy + 11, IndustrialStyle.AMBER);
    }

    private void boltIcon(PoseStack pose, int bx, int by) {
        int[][] rows = {{4, 7}, {3, 6}, {2, 5}, {1, 7}, {3, 6}, {2, 5}, {2, 4}, {1, 3}};
        for (int i = 0; i < rows.length; i++) {
            fill(pose, bx + rows[i][0], by + i, bx + rows[i][1], by + i + 1, FE_ICON);
        }
    }

    private void droplet(PoseStack pose, int dx, int dy, int color) {
        int[][] rows = {{3, 5}, {3, 5}, {2, 6}, {1, 7}, {1, 7}, {1, 7}, {2, 6}, {3, 5}};
        for (int i = 0; i < rows.length; i++) {
            fill(pose, dx + rows[i][0], dy + i, dx + rows[i][1], dy + i + 1, color);
        }
    }

    private void tick(PoseStack pose, int sx, int sy) {
        fill(pose, sx + 4, sy + 8, sx + 6, sy + 10, TICK);
        fill(pose, sx + 6, sy + 10, sx + 8, sy + 12, TICK);
        fill(pose, sx + 8, sy + 4, sx + 10, sy + 10, TICK);
        fill(pose, sx + 10, sy + 4, sx + 12, sy + 6, TICK);
    }

    /** Ghosts and ticks in the deposit column, the blueprint in the slot, and small icons in the ledger and gauges. */
    private void renderItems() {
        PoseStack pose = new PoseStack();
        int x = this.leftPos;
        int y = this.topPos;
        CompoundTag staged = this.menu.stagedLedger();
        List<ExtractorCosts.CostLine> items = ExtractorForgeContainer.itemLines(this.shownLedger());
        boolean showDeposits = this.state() != State.DONE;
        for (int i = 0; i < ExtractorForgeTileEntity.DEPOSIT_SLOTS && showDeposits; i++) {
            if (i >= items.size()) {
                continue;
            }
            ExtractorCosts.CostLine line = items.get(i);
            int sx = x + ExtractorForgeContainer.DEPOSIT_X;
            int sy = y + ExtractorForgeContainer.DEPOSIT_Y0 + i * ExtractorForgeContainer.DEPOSIT_PITCH;
            if (staged != null && CostLedger.remaining(staged, line) <= 0) {
                RenderSystem.disableDepthTest();
                this.tick(pose, sx, sy);
                RenderSystem.enableDepthTest();
            } else if (line.item() != null) {
                IndustrialStyle.ghost(pose, new ItemStack(line.item()), sx, sy);
            }
        }
        CompoundTag ledger = this.shownLedger();
        List<ExtractorCosts.CostLine> rows = ledgerRows(ledger);
        this.scroll = Math.max(0, Math.min(this.scroll, rows.size() - VISIBLE_ROWS));
        for (int i = 0; i < VISIBLE_ROWS && i + this.scroll < rows.size(); i++) {
            ExtractorCosts.CostLine line = rows.get(i + this.scroll);
            ItemStack icon = line.fluid() ? fluidIcon(line) : line.item() == null ? ItemStack.EMPTY : new ItemStack(line.item());
            if (!icon.isEmpty()) {
                IndustrialStyle.item(icon, x + CX0 + 4, y + LEDGER_Y0 + 3 + i * ROW_H, 0.5F);
            }
        }
        List<ExtractorCosts.CostLine> fluids = fluidLines(ledger);
        for (int i = 0; i < Math.min(2, fluids.size()); i++) {
            IndustrialStyle.item(fluidIcon(fluids.get(i)), x + GAUGE_X + 10 * (i + 1), y + RAIL_Y0 + 3, 0.5F);
        }
    }

    @Override
    protected void renderLabels(PoseStack pose, int mouseX, int mouseY) {
        State state = this.state();
        IndustrialStyle.engravedCentered(pose, this.font, this.title.getString(), (CX0 + CX - 17) / 2, TOP_Y0 + 8);
        drawCentered(pose, state.label, (STATUS_X0 + CX1) / 2, TOP_Y0 + 8, state.color);

        CompoundTag ledger = this.shownLedger();
        List<ExtractorCosts.CostLine> rows = ledgerRows(ledger);
        if (rows.isEmpty()) {
            String message = switch (state) {
                case DONE -> "Take the finished part";
                case UNIDENTIFIED -> "Identify the blueprint first";
                default -> "Insert an identified blueprint";
            };
            drawCentered(pose, message, CX, LEDGER_Y0 + 3 + 3 * ROW_H + 1, IndustrialStyle.TEXT_DIM);
        } else {
            for (int i = 0; i < VISIBLE_ROWS && i + this.scroll < rows.size(); i++) {
                ExtractorCosts.CostLine line = rows.get(i + this.scroll);
                int ry = LEDGER_Y0 + 3 + i * ROW_H;
                long paid = Math.min(CostLedger.paid(ledger, line), line.amount());
                String amounts = String.format("%,d/%,d%s", paid, line.amount(), line.fluid() ? " mB" : "");
                int aw = this.font.width(amounts);
                int ax = CX1 - 5 - aw;
                String name = line.fluid() ? fluidName(line) : line.displayName().getString();
                this.font.drawShadow(pose, this.font.plainSubstrByWidth(name, ax - (CX0 + 16) - 6), CX0 + 16, ry + 1, IndustrialStyle.TEXT);
                this.font.drawShadow(pose, amounts, ax, ry + 1, paid >= line.amount() ? IndustrialStyle.GREEN : IndustrialStyle.AMBER);
            }
            if (rows.size() > VISIBLE_ROWS) {
                int track = VISIBLE_ROWS * ROW_H;
                int thumb = Math.max(6, track * VISIBLE_ROWS / rows.size());
                int top = LEDGER_Y0 + 3 + (track - thumb) * this.scroll / Math.max(1, rows.size() - VISIBLE_ROWS);
                fill(pose, CX1 - 3, top, CX1 - 2, top + thumb, IndustrialStyle.TEXT_DIM);
            }
        }

        PartData part = this.shownPart();
        if (part == null) {
            drawCentered(pose, "Part stats", (CX0 + STATS_X1) / 2, BAND_Y0 + 18, IndustrialStyle.TEXT_DIM);
        } else {
            String quality = String.format("Q%.1f %s", part.quality, ExtractorTooltips.rarityName(part.rarity));
            int qw = this.font.width(quality);
            this.font.drawShadow(pose, this.font.plainSubstrByWidth(part.name, STATS_X1 - CX0 - 8 - qw - 6), CX0 + 4, BAND_Y0 + 3, IndustrialStyle.TEXT);
            this.font.drawShadow(pose, quality, STATS_X1 - 4 - qw, BAND_Y0 + 3, part.rarity.getColor().getValue() | 0xFF000000);
            fill(pose, CX0 + 3, BAND_Y0 + 13, STATS_X1 - 3, BAND_Y0 + 14, RULE);
            int colW = (STATS_X1 - CX0 - 8 - 6) / 2;
            int shown = 0;
            for (ExtractorStat[] group : ExtractorTooltips.GROUPS) {
                for (ExtractorStat stat : group) {
                    Float value = part.stats.get(stat);
                    if (value == null || shown >= 6 || (Math.abs(value) < 0.05F && stat.getUnit() != ExtractorStat.Unit.FLAG)) {
                        continue;
                    }
                    int sx = CX0 + 4 + (shown % 2) * (colW + 6);
                    int sy = BAND_Y0 + 16 + (shown / 2) * 9;
                    String text = statValue(stat, value);
                    int vw = this.font.width(text);
                    String label = this.font.plainSubstrByWidth(SHORT_NAMES.getOrDefault(stat, stat.getDisplayName()), colW - vw - 4);
                    this.font.drawShadow(pose, label, sx, sy, IndustrialStyle.TEXT_DIM);
                    this.font.drawShadow(pose, text, sx + colW - vw, sy, stat.getColor() | 0xFF000000);
                    shown++;
                }
            }
        }

        int mx = LRAIL_X0 + 4;
        this.font.drawShadow(pose, "Materials", mx, INV_Y + 3, IndustrialStyle.TEXT_DIM);
        int done = 0;
        for (ExtractorCosts.CostLine line : rows) {
            if (CostLedger.remaining(ledger, line) <= 0) {
                done++;
            }
        }
        boolean allDone = !rows.isEmpty() && done == rows.size();
        this.font.drawShadow(pose, rows.isEmpty() ? "-" : done + " of " + rows.size() + " done", mx, INV_Y + 13,
                allDone ? IndustrialStyle.GREEN : IndustrialStyle.TEXT);
        this.font.drawShadow(pose, "Contents", mx, INV_Y + 29, IndustrialStyle.TEXT_DIM);
        String lock = state == State.PREVIEW ? "Open" : this.menu.isStaged() ? "Locked" : "-";
        this.font.drawShadow(pose, lock, mx, INV_Y + 39, this.menu.isStaged() ? IndustrialStyle.AMBER : IndustrialStyle.TEXT);

        int tx = READOUT_R_X0 + 4;
        int total = this.menu.data(ExtractorForgeTileEntity.DATA_TOTAL_TICKS);
        int progress = this.menu.data(ExtractorForgeTileEntity.DATA_PROGRESS);
        int fePerTick = this.menu.data(ExtractorForgeTileEntity.DATA_FE_PER_TICK);
        boolean active = state == State.ACTIVE;
        boolean known = total > 0 && state != State.DONE && state != State.IDLE && state != State.UNIDENTIFIED;
        this.font.drawShadow(pose, "Build time", tx, INV_Y + 3, IndustrialStyle.TEXT_DIM);
        this.font.drawShadow(pose, known ? formatTime(active ? total - progress : total) : "-", tx, INV_Y + 13,
                active ? IndustrialStyle.EMBER_TEXT : IndustrialStyle.TEXT);
        this.font.drawShadow(pose, "Power draw", tx, INV_Y + 29, IndustrialStyle.TEXT_DIM);
        this.font.drawShadow(pose, known ? this.font.plainSubstrByWidth(String.format("%,d FE/t", fePerTick), RRAIL_X1 - 4 - tx) : "-",
                tx, INV_Y + 39, IndustrialStyle.RED_TEXT);
    }

    private void drawCentered(PoseStack pose, String text, int cx, int y, int color) {
        this.font.drawShadow(pose, text, cx - this.font.width(text) / 2.0F, y, color);
    }

    private static String statValue(ExtractorStat stat, float value) {
        if (stat.getUnit() == ExtractorStat.Unit.FLAG) {
            return "Yes";
        }
        String number = ExtractorTooltips.formatNumber(value);
        if (stat.getUnit() == ExtractorStat.Unit.PERCENT) {
            return number + "%";
        }
        if (stat.getUnit() == ExtractorStat.Unit.RATE) {
            return number + "/s";
        }
        return number;
    }

    /** {@code 4m 30s}; ticks at 20 per second. */
    static String formatTime(int ticks) {
        int seconds = Math.max(0, (ticks + 19) / 20);
        if (seconds >= 3600) {
            return String.format("%dh %02dm", seconds / 3600, (seconds % 3600) / 60);
        }
        return String.format("%dm %02ds", seconds / 60, seconds % 60);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        this.scroll -= (int) Math.signum(delta);
        return true;
    }

    private boolean over(int mouseX, int mouseY, int x0, int y0, int x1, int y1) {
        int mx = mouseX - this.leftPos;
        int my = mouseY - this.topPos;
        return mx >= x0 && mx < x1 && my >= y0 && my < y1;
    }

    @Override
    protected void renderTooltip(PoseStack pose, int mouseX, int mouseY) {
        super.renderTooltip(pose, mouseX, mouseY);
        CompoundTag ledger = this.shownLedger();
        if (this.over(mouseX, mouseY, GAUGE_X, GAUGE_Y0, GAUGE_X + 8, GAUGE_Y1)) {
            this.renderComponentTooltip(pose, List.of(
                    new TextComponent(String.format("%,d / %,d FE", this.menu.data(ExtractorForgeTileEntity.DATA_ENERGY),
                            this.menu.data(ExtractorForgeTileEntity.DATA_CAPACITY))),
                    new TextComponent("Fabricating draws FE every tick and pauses while the buffer is short")), mouseX, mouseY);
            return;
        }
        List<ExtractorCosts.CostLine> fluids = fluidLines(ledger);
        for (int i = 0; i < 2; i++) {
            int gx = GAUGE_X + 10 * (i + 1);
            if (this.over(mouseX, mouseY, gx, GAUGE_Y0, gx + 8, GAUGE_Y1)) {
                List<Component> lines = new ArrayList<>();
                if (i < fluids.size()) {
                    ExtractorCosts.CostLine line = fluids.get(i);
                    lines.add(new TextComponent(fluidName(line)));
                    lines.add(new TextComponent(String.format("%,d / %,d mB", Math.min(CostLedger.paid(ledger, line), line.amount()), line.amount())));
                    lines.add(new TextComponent("Pipe it in, or use a filled bucket on the forge once staged"));
                } else {
                    lines.add(new TextComponent("No fluid needed"));
                }
                this.renderComponentTooltip(pose, lines, mouseX, mouseY);
                return;
            }
        }
        if (this.over(mouseX, mouseY, CX0, BAND_Y0, STATS_X1, BAND_Y1)) {
            ItemStack shown = this.shownBlueprint().isEmpty() ? this.slotStack() : this.shownBlueprint();
            if (!shown.isEmpty()) {
                this.renderComponentTooltip(pose, this.getTooltipFromItem(shown), mouseX, mouseY);
            }
            return;
        }
        if (this.hoveredSlot == null || this.hoveredSlot.hasItem()) {
            return;
        }
        int index = this.hoveredSlot.index;
        if (index == ExtractorForgeTileEntity.BLUEPRINT_SLOT) {
            if (this.menu.isStaged()) {
                PartData part = ExtractorBlueprintItem.getPart(this.menu.staged());
                this.renderComponentTooltip(pose, List.of(new TextComponent("Staged: " + (part == null ? "blueprint" : part.name)),
                        new TextComponent("Locked in until the part is fabricated")), mouseX, mouseY);
            } else {
                this.renderComponentTooltip(pose, List.of(new TextComponent("Identified blueprint"),
                        new TextComponent("Press Stage to load it; it cannot be taken back out")), mouseX, mouseY);
            }
            return;
        }
        if (index >= ExtractorForgeTileEntity.DEPOSIT_SLOT_START && index < ExtractorForgeTileEntity.SLOT_COUNT) {
            List<ExtractorCosts.CostLine> items = ExtractorForgeContainer.itemLines(ledger);
            int line = index - ExtractorForgeTileEntity.DEPOSIT_SLOT_START;
            if (line >= items.size()) {
                this.renderTooltip(pose, new TextComponent("Unused"), mouseX, mouseY);
                return;
            }
            ExtractorCosts.CostLine cost = items.get(line);
            List<Component> lines = new ArrayList<>();
            lines.add(cost.displayName());
            lines.add(new TextComponent(String.format("%,d / %,d", Math.min(CostLedger.paid(ledger, cost), cost.amount()), cost.amount())));
            lines.add(new TextComponent(this.menu.isStaged() ? "Click or shift-click items in to pay; they cannot be taken back"
                    : "Stage the blueprint to start paying"));
            this.renderComponentTooltip(pose, lines, mouseX, mouseY);
        }
    }
}
