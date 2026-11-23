package xyz.iwolfking.woldsvaults.integration.jei.category;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.gear.VaultGearRarity;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.config.ExtractorCostsConfig;
import xyz.iwolfking.woldsvaults.extraction.costs.ExtractorCosts;
import xyz.iwolfking.woldsvaults.extraction.parts.ExtractorPartType;
import xyz.iwolfking.woldsvaults.extraction.parts.ExtractorTooltips;
import xyz.iwolfking.woldsvaults.extraction.parts.PartData;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.init.ModItems;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JEI pages for the Extraction Forge: one per part recipe of the costs table, blueprint to part, with each cost row at
 * quality 0 and Scrappy rarity. Rows a part only owes from a higher quality sit in a second row at their entry amount.
 */
public class ExtractionForgeCategory implements IRecipeCategory<ExtractionForgeCategory.Page> {
    public static final RecipeType<Page> TYPE = RecipeType.create(WoldsVaults.MOD_ID, "extraction_forge", Page.class);

    private static final int SLOT = 18;
    private static final int PER_ROW = 9;
    private static final int WIDTH = PER_ROW * SLOT;
    private static final int BASE_LABEL_Y = 22;
    private static final int BASE_Y = BASE_LABEL_Y + 10;
    private static final int GATED_LABEL_Y = BASE_Y + SLOT + 4;
    private static final int GATED_Y = GATED_LABEL_Y + 10;
    private static final int FOOTER_Y = GATED_Y + SLOT + 4;
    private static final int TEXT_COLOR = 0x404040;

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableStatic slot;
    private final IDrawableStatic arrow;

    public ExtractionForgeCategory(IGuiHelper helper) {
        this.background = helper.createBlankDrawable(WIDTH, FOOTER_Y + 20);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.EXTRACTOR_FORGE_BLOCK));
        this.slot = helper.getSlotDrawable();
        this.arrow = helper.createDrawable(WoldsVaults.id("textures/gui/trinket_fuser.png"), 176, 0, 24, 16);
    }

    /** One Extraction Forge recipe: the part type, its series, drill class or attachment key, and the cost rows. */
    public record Page(ExtractorPartType type, String key, List<ExtractorCostsConfig.Ingredient> rows) {
        /** Whether a row only enters from a higher quality. */
        static boolean gated(ExtractorCostsConfig.Ingredient row) {
            return row.gate > 0.0D;
        }
    }

    /** Every part recipe of the loaded costs table: core parts by series, then drills, then attachments. */
    public static List<Page> pages() {
        List<Page> pages = new ArrayList<>();
        ExtractorCostsConfig costs = ExtractorCosts.config();
        if (costs == null || costs.PARTS == null || costs.PARTS.isEmpty()) {
            WoldLogger.error("The extractor costs table has no part recipes; JEI shows no Extraction Forge pages.");
            return pages;
        }
        for (Map.Entry<String, ? extends Map<String, List<ExtractorCostsConfig.Ingredient>>> byType : costs.PARTS.entrySet()) {
            ExtractorPartType type = ExtractorPartType.byId(byType.getKey());
            if (type == null) {
                WoldLogger.error("The extractor costs table lists recipes for unknown part type '{}'; JEI skips them.", byType.getKey());
                continue;
            }
            byType.getValue().forEach((series, rows) -> pages.add(new Page(type, series, rows)));
        }
        if (costs.DRILLS != null) {
            costs.DRILLS.forEach((drillClass, rows) -> pages.add(new Page(ExtractorPartType.DRILL, drillClass, rows)));
        }
        if (costs.ATTACHMENTS != null) {
            costs.ATTACHMENTS.forEach((attachment, rows) -> pages.add(new Page(ExtractorPartType.ATTACHMENT, attachment, rows)));
        }
        return pages;
    }

    /** A Scrappy quality 0 part of the given type and key, carrying only its name, for display. */
    public static ItemStack displayPart(ExtractorPartType type, String key) {
        PartData part = new PartData();
        part.type = type;
        if (type == ExtractorPartType.DRILL) {
            part.drillClass = key;
        } else if (type == ExtractorPartType.ATTACHMENT) {
            part.attachmentId = key;
        } else {
            part.series = key;
        }
        part.rarity = VaultGearRarity.SCRAPPY;
        part.quality = 0.0F;
        part.name = ExtractorTooltips.partBaseName(type, key);
        part.baseName = part.name;
        ItemStack stack = new ItemStack(ModItems.EXTRACTOR_PARTS.get(type));
        stack.getOrCreateTag().put(PartData.TAG, part.write());
        return stack;
    }

    @Override
    public RecipeType<Page> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return new TranslatableComponent("jei.woldsvaults.extraction_forge");
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Page page, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                .setBackground(this.slot, -1, -1)
                .addItemStack(new ItemStack(ModItems.EXTRACTOR_BLUEPRINTS.get(page.type())));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 55, 1)
                .setBackground(this.slot, -1, -1)
                .addItemStack(displayPart(page.type(), page.key()));
        int base = 0;
        int gated = 0;
        for (ExtractorCostsConfig.Ingredient row : page.rows()) {
            boolean isGated = Page.gated(row);
            int index = isGated ? gated++ : base++;
            if (index >= PER_ROW) {
                WoldLogger.errorOnce("jei-forge-row-overflow-" + page.type().getId() + "-" + page.key(),
                        "The Extraction Forge recipe for {} '{}' has more than {} cost rows of one kind; JEI leaves the rest out.",
                        page.type().getId(), page.key(), PER_ROW);
                continue;
            }
            addCost(builder, row, index * SLOT + 1, (isGated ? GATED_Y : BASE_Y) + 1);
        }
    }

    private void addCost(IRecipeLayoutBuilder builder, ExtractorCostsConfig.Ingredient row, int x, int y) {
        ExtractorCosts.AmountRange range = ExtractorCosts.entryRange(row, VaultGearRarity.SCRAPPY);
        Component amount = new TranslatableComponent("jei.woldsvaults.extraction_forge.amount",
                range.min() == range.max() ? String.valueOf(range.min()) : range.min() + " - " + range.max()).withStyle(ChatFormatting.YELLOW);
        Component entry = Page.gated(row)
                ? new TranslatableComponent("jei.woldsvaults.extraction_forge.from_quality", String.format("%.0f", row.gate)).withStyle(ChatFormatting.DARK_AQUA)
                : null;
        IRecipeSlotBuilder slotBuilder = builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                .setBackground(this.slot, -1, -1)
                .addTooltipCallback((view, tooltip) -> {
                    tooltip.add(amount);
                    if (entry != null) {
                        tooltip.add(entry);
                    }
                });
        if (row.fluid) {
            Fluid fluid = ExtractorCosts.resolveFluid(row.id);
            if (fluid != null) {
                slotBuilder.setFluidRenderer(Math.max(1L, range.max()), false, 16, 16)
                        .addFluidStack(fluid, Math.max(1L, range.min()));
            }
            return;
        }
        Item item = ExtractorCosts.resolveItem(row.id);
        if (item != null) {
            slotBuilder.addItemStack(new ItemStack(item, (int) Math.max(1L, Math.min(Integer.MAX_VALUE, range.min()))));
        }
    }

    @Override
    public void draw(Page page, IRecipeSlotsView slots, PoseStack poseStack, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        this.arrow.draw(poseStack, 24, 1);
        font.draw(poseStack, new TranslatableComponent("jei.woldsvaults.extraction_forge.base"), 0, BASE_LABEL_Y, TEXT_COLOR);
        if (page.rows().stream().anyMatch(Page::gated)) {
            font.draw(poseStack, new TranslatableComponent("jei.woldsvaults.extraction_forge.gated"), 0, GATED_LABEL_Y, TEXT_COLOR);
        }
        ExtractorCostsConfig.ForgeEntry forge = ExtractorCosts.config().FORGE;
        font.draw(poseStack, new TranslatableComponent("jei.woldsvaults.extraction_forge.scaling"), 0, FOOTER_Y, TEXT_COLOR);
        if (forge != null) {
            font.draw(poseStack, new TranslatableComponent("jei.woldsvaults.extraction_forge.energy",
                    String.format("%,.0f", forge.fePerTickPerQuality), String.format("%.0f", forge.secondsPerQuality)), 0, FOOTER_Y + 10, TEXT_COLOR);
        }
    }

    @Override @SuppressWarnings("removal")
    public ResourceLocation getUid() {
        return TYPE.getUid();
    }

    @Override @SuppressWarnings("removal")
    public Class<? extends Page> getRecipeClass() {
        return Page.class;
    }
}
