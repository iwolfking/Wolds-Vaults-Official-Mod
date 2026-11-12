package xyz.iwolfking.woldsvaults.blocks.containers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import xyz.iwolfking.woldsvaults.blocks.tiles.VaultFabricatorTileEntity;
import xyz.iwolfking.woldsvaults.client.screens.IndustrialStyle;
import xyz.iwolfking.woldsvaults.config.ExtractorCostsConfig;
import xyz.iwolfking.woldsvaults.extraction.costs.ExtractorCosts;
import xyz.iwolfking.woldsvaults.items.FabricationPatternItem;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Vault Fabricator screen in the industrial style: centred title plate, pattern slot with full-height previous/next
 * buttons, a readout of the selected recipe's energy, fluid and time, the 5×5 grid with each cell's required item
 * as a ghost, red FE and blue fluid gauges the length of the grid (a white line marks what the recipe needs), and
 * the result slot with its progress bar. Layout spec: {@code library/ui_preview/screens/vault_fabricator_v2.py}.
 */
public class VaultFabricatorScreen extends AbstractContainerScreen<VaultFabricatorContainer> {
    private static final int GRID_X = VaultFabricatorContainer.GRID_X;
    private static final int GRID_Y = VaultFabricatorContainer.GRID_Y;
    private static final int GRID_SPAN = 89;
    private static final int LEFT_X0 = 8;
    private static final int LEFT_X1 = GRID_X - 5;
    private static final int GAUGE_Y0 = GRID_Y - 1;
    private static final int GAUGE_Y1 = GRID_Y + GRID_SPAN;
    private static final int FE_X = GRID_X + GRID_SPAN + 4;
    private static final int FLUID_X = FE_X + 11;
    private static final int GAUGE_W = 9;
    private static final int READOUT_Y = GRID_Y + 40;
    private static final int OUT_X = VaultFabricatorContainer.OUTPUT_X;
    private static final int OUT_Y = VaultFabricatorContainer.OUTPUT_Y;

    public VaultFabricatorScreen(VaultFabricatorContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
        this.imageWidth = VaultFabricatorContainer.IMAGE_WIDTH;
        this.imageHeight = VaultFabricatorContainer.IMAGE_HEIGHT;
        this.inventoryLabelY = this.imageHeight - 94;
        this.inventoryLabelX = VaultFabricatorContainer.INVENTORY_X;
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(new IndustrialStyle.MachineButton(this.leftPos + LEFT_X0 + 3, this.topPos + GRID_Y + 21, 19, 16,
                () -> "<", () -> IndustrialStyle.ButtonStyle.STEEL,
                button -> this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, VaultFabricatorContainer.BUTTON_PREVIOUS)));
        this.addRenderableWidget(new IndustrialStyle.MachineButton(this.leftPos + LEFT_X1 - 22, this.topPos + GRID_Y + 21, 19, 16,
                () -> ">", () -> IndustrialStyle.ButtonStyle.STEEL,
                button -> this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, VaultFabricatorContainer.BUTTON_NEXT)));
    }

    @Nullable
    private ExtractorCostsConfig.ComponentEntry recipe() {
        return FabricationPatternItem.recipe(this.menu.getSlot(VaultFabricatorTileEntity.PATTERN_SLOT).getItem());
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(pose);
        super.render(pose, mouseX, mouseY, partialTicks);
        this.renderGhosts(pose);
        this.renderTooltip(pose, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack pose, float partialTicks, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        int w = this.imageWidth;
        int h = this.imageHeight;
        IndustrialStyle.panel(pose, x, y, x + w, y + h);
        IndustrialStyle.bolt(pose, x + 4, y + 4);
        IndustrialStyle.bolt(pose, x + w - 8, y + 4);
        IndustrialStyle.bolt(pose, x + 4, y + h - 8);
        IndustrialStyle.bolt(pose, x + w - 8, y + h - 8);
        int titleW = this.font.width(this.title);
        IndustrialStyle.plate(pose, x + w / 2 - titleW / 2 - 12, y + 3, x + w / 2 + titleW / 2 + 12, y + 18);
        IndustrialStyle.bolt(pose, x + w / 2 - titleW / 2 - 9, y + 8);
        IndustrialStyle.bolt(pose, x + w / 2 + titleW / 2 + 5, y + 8);
        IndustrialStyle.tray(pose, x + GRID_X - 3, y + GRID_Y - 3, x + GRID_X + GRID_SPAN + 2, y + GRID_Y + GRID_SPAN + 2);
        for (Slot slot : this.menu.slots) {
            if (slot.index != VaultFabricatorTileEntity.OUTPUT_SLOT) {
                IndustrialStyle.slot(pose, x + slot.x, y + slot.y);
            }
        }
        IndustrialStyle.display(pose, x + LEFT_X0, y + READOUT_Y, x + LEFT_X1, y + GAUGE_Y1);

        ExtractorCostsConfig.ComponentEntry recipe = this.recipe();
        int energy = this.menu.data(VaultFabricatorTileEntity.DATA_ENERGY);
        int fluid = this.menu.data(VaultFabricatorTileEntity.DATA_FLUID);
        float feMarker = recipe == null ? -1.0F : (float) recipe.fe / VaultFabricatorTileEntity.ENERGY_CAPACITY;
        float fluidMarker = recipe == null || recipe.fluidAmount <= 0 ? -1.0F : (float) recipe.fluidAmount / VaultFabricatorTileEntity.TANK_CAPACITY;
        IndustrialStyle.gauge(pose, x + FE_X, y + GAUGE_Y0, x + FE_X + GAUGE_W, y + GAUGE_Y1,
                (float) energy / VaultFabricatorTileEntity.ENERGY_CAPACITY, IndustrialStyle.FE_GAUGE, feMarker);
        IndustrialStyle.gauge(pose, x + FLUID_X, y + GAUGE_Y0, x + FLUID_X + GAUGE_W, y + GAUGE_Y1,
                (float) fluid / VaultFabricatorTileEntity.TANK_CAPACITY, IndustrialStyle.WATER_GAUGE, fluidMarker);

        IndustrialStyle.bigSlot(pose, x + OUT_X, y + OUT_Y);
        int ticks = this.menu.data(VaultFabricatorTileEntity.DATA_TICKS);
        int progress = this.menu.data(VaultFabricatorTileEntity.DATA_PROGRESS);
        IndustrialStyle.hbar(pose, x + OUT_X - 5, y + OUT_Y + 24, x + OUT_X + 21, y + OUT_Y + 30,
                ticks > 0 ? (float) progress / ticks : 0.0F, IndustrialStyle.PROGRESS_BAR);
    }

    private void renderGhosts(PoseStack pose) {
        ExtractorCostsConfig.ComponentEntry recipe = this.recipe();
        if (recipe == null) {
            return;
        }
        for (int cell = 0; cell < VaultFabricatorTileEntity.GRID_CELLS; cell++) {
            Slot slot = this.menu.getSlot(VaultFabricatorTileEntity.GRID_START + cell);
            Item item = VaultFabricatorTileEntity.cellItem(recipe, cell);
            if (item != null && !slot.hasItem()) {
                IndustrialStyle.ghost(pose, new ItemStack(item), this.leftPos + slot.x, this.topPos + slot.y);
            }
        }
    }

    @Override
    protected void renderLabels(PoseStack pose, int mouseX, int mouseY) {
        IndustrialStyle.engravedCentered(pose, this.font, this.title.getString(), this.imageWidth / 2, 7);
        IndustrialStyle.engraved(pose, this.font, this.playerInventoryTitle.getString(), this.inventoryLabelX, this.inventoryLabelY);
        ExtractorCostsConfig.ComponentEntry recipe = this.recipe();
        int tx = LEFT_X0 + 3;
        int ty = READOUT_Y + 2;
        int room = LEFT_X1 - LEFT_X0 - 6;
        this.font.drawShadow(pose, "Requires", tx, ty, IndustrialStyle.TEXT_DIM);
        if (recipe == null) {
            this.font.drawShadow(pose, "- FE", tx, ty + 9, IndustrialStyle.TEXT_DIM);
            this.font.drawShadow(pose, "-", tx, ty + 18, IndustrialStyle.TEXT_DIM);
            this.font.drawShadow(pose, "- mB", tx, ty + 27, IndustrialStyle.TEXT_DIM);
            this.font.drawShadow(pose, "- s", tx, ty + 36, IndustrialStyle.TEXT_DIM);
            return;
        }
        boolean hasFluid = recipe.fluid != null && !recipe.fluid.isEmpty() && recipe.fluidAmount > 0;
        this.font.drawShadow(pose, this.font.plainSubstrByWidth(compactEnergy(recipe.fe) + " FE", room), tx, ty + 9, IndustrialStyle.RED_TEXT);
        this.font.drawShadow(pose, this.font.plainSubstrByWidth(hasFluid ? fluidName(recipe.fluid) : "No fluid", room), tx, ty + 18,
                hasFluid ? IndustrialStyle.BLUE_TEXT : IndustrialStyle.TEXT_DIM);
        this.font.drawShadow(pose, this.font.plainSubstrByWidth(hasFluid ? String.format("%,d mB", recipe.fluidAmount) : "- mB", room), tx, ty + 27,
                hasFluid ? IndustrialStyle.BLUE_TEXT : IndustrialStyle.TEXT_DIM);
        this.font.drawShadow(pose, this.font.plainSubstrByWidth(String.format("%.1f s", recipe.ticks / 20.0F), room), tx, ty + 36, IndustrialStyle.TEXT);
    }

    /** {@code 30k}, {@code 1.25M}; exact below ten thousand. */
    static String compactEnergy(long value) {
        if (value >= 1_000_000L) {
            return String.format("%.2fM", value / 1_000_000.0D).replace(".00M", "M");
        }
        if (value >= 10_000L) {
            return (value / 1000L) + "k";
        }
        return String.format("%,d", value);
    }

    private static String fluidName(String id) {
        Fluid fluid = ExtractorCosts.resolveFluid(id);
        return fluid == null ? id : new FluidStack(fluid, 1000).getDisplayName().getString();
    }

    private boolean over(int mouseX, int mouseY, int x0, int y0, int x1, int y1) {
        int mx = mouseX - this.leftPos;
        int my = mouseY - this.topPos;
        return mx >= x0 && mx < x1 && my >= y0 && my < y1;
    }

    @Override
    protected void renderTooltip(PoseStack pose, int mouseX, int mouseY) {
        super.renderTooltip(pose, mouseX, mouseY);
        ExtractorCostsConfig.ComponentEntry recipe = this.recipe();
        if (this.over(mouseX, mouseY, FE_X, GAUGE_Y0, FE_X + GAUGE_W, GAUGE_Y1)) {
            List<Component> lines = new ArrayList<>();
            lines.add(new TextComponent(String.format("%,d / %,d FE", this.menu.data(VaultFabricatorTileEntity.DATA_ENERGY),
                    VaultFabricatorTileEntity.ENERGY_CAPACITY)));
            if (recipe != null) {
                lines.add(new TextComponent(String.format("Recipe needs %,d FE", recipe.fe)));
            }
            this.renderComponentTooltip(pose, lines, mouseX, mouseY);
            return;
        }
        if (this.over(mouseX, mouseY, FLUID_X, GAUGE_Y0, FLUID_X + GAUGE_W, GAUGE_Y1)) {
            List<Component> lines = new ArrayList<>();
            lines.add(new TextComponent(String.format("%,d / %,d mB", this.menu.data(VaultFabricatorTileEntity.DATA_FLUID),
                    VaultFabricatorTileEntity.TANK_CAPACITY)));
            if (recipe != null && recipe.fluid != null && !recipe.fluid.isEmpty() && recipe.fluidAmount > 0) {
                lines.add(new TextComponent(String.format("Recipe needs %,d mB %s", recipe.fluidAmount, fluidName(recipe.fluid))));
            }
            this.renderComponentTooltip(pose, lines, mouseX, mouseY);
            return;
        }
        if (this.hoveredSlot == null || this.hoveredSlot.hasItem()) {
            return;
        }
        int index = this.hoveredSlot.index;
        if (index == VaultFabricatorTileEntity.PATTERN_SLOT) {
            this.renderComponentTooltip(pose, List.of(new TextComponent("Fabrication Pattern"),
                    new TextComponent("Insert a pattern, then use < > to pick the component")), mouseX, mouseY);
            return;
        }
        if (recipe != null && index >= VaultFabricatorTileEntity.GRID_START && index < VaultFabricatorTileEntity.SLOT_COUNT) {
            String id = recipe.cell(index - VaultFabricatorTileEntity.GRID_START);
            if (id != null) {
                this.renderTooltip(pose, new ExtractorCosts.CostLine(id, false, 1).displayName(), mouseX, mouseY);
            }
        }
    }
}
