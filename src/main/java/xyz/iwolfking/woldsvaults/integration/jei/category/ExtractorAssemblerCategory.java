package xyz.iwolfking.woldsvaults.integration.jei.category;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
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
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.extraction.parts.ExtractorPartType;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.init.ModItems;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** The JEI page for the Extractor Assembler: one part of each core type, optional attachments, to an Extractor. */
public class ExtractorAssemblerCategory implements IRecipeCategory<ExtractorAssemblerCategory.Page> {
    public static final RecipeType<Page> TYPE = RecipeType.create(WoldsVaults.MOD_ID, "extractor_assembler", Page.class);

    private static final int SLOT = 18;
    private static final int PER_ROW = 4;
    private static final int ARROW_X = PER_ROW * SLOT + 8;
    private static final int OUTPUT_X = ARROW_X + 30;
    private static final int TEXT_Y = 2 * SLOT + 6;
    private static final int TEXT_COLOR = 0x404040;

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableStatic slot;
    private final IDrawableStatic arrow;

    public ExtractorAssemblerCategory(IGuiHelper helper) {
        this.background = helper.createBlankDrawable(162, TEXT_Y + 10);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.EXTRACTOR_ASSEMBLER_BLOCK));
        this.slot = helper.getSlotDrawable();
        this.arrow = helper.createDrawable(WoldsVaults.id("textures/gui/trinket_fuser.png"), 176, 0, 24, 16);
    }

    /** The assembly page: every display part per core type and every display attachment, from the Forge pages. */
    public record Page(Map<ExtractorPartType, List<ItemStack>> cores, List<ItemStack> attachments) {
    }

    /** The single assembly page, built from the same part recipes the Extraction Forge pages list. */
    public static List<Page> pages(List<ExtractionForgeCategory.Page> forgePages) {
        Map<ExtractorPartType, List<ItemStack>> cores = new EnumMap<>(ExtractorPartType.class);
        List<ItemStack> attachments = new ArrayList<>();
        for (ExtractionForgeCategory.Page page : forgePages) {
            ItemStack part = ExtractionForgeCategory.displayPart(page.type(), page.key());
            if (page.type() == ExtractorPartType.ATTACHMENT) {
                attachments.add(part);
            } else {
                cores.computeIfAbsent(page.type(), type -> new ArrayList<>()).add(part);
            }
        }
        List<Page> pages = new ArrayList<>();
        if (!cores.isEmpty()) {
            pages.add(new Page(cores, attachments));
        }
        return pages;
    }

    @Override
    public RecipeType<Page> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return new TranslatableComponent("jei.woldsvaults.extractor_assembler");
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
        int index = 0;
        for (ExtractorPartType type : ExtractorPartType.values()) {
            if (type == ExtractorPartType.ATTACHMENT) {
                continue;
            }
            List<ItemStack> parts = page.cores().getOrDefault(type, List.of(new ItemStack(ModItems.EXTRACTOR_PARTS.get(type))));
            builder.addSlot(RecipeIngredientRole.INPUT, (index % PER_ROW) * SLOT + 1, (index / PER_ROW) * SLOT + 1)
                    .setBackground(this.slot, -1, -1)
                    .addItemStacks(parts);
            index++;
        }
        if (!page.attachments().isEmpty()) {
            Component optional = new TranslatableComponent("jei.woldsvaults.extractor_assembler.optional").withStyle(ChatFormatting.DARK_AQUA);
            Component hardpoints = new TranslatableComponent("jei.woldsvaults.extractor_assembler.hardpoints").withStyle(ChatFormatting.GRAY);
            builder.addSlot(RecipeIngredientRole.INPUT, (index % PER_ROW) * SLOT + 1, (index / PER_ROW) * SLOT + 1)
                    .setBackground(this.slot, -1, -1)
                    .addItemStacks(page.attachments())
                    .addTooltipCallback((view, tooltip) -> {
                        tooltip.add(optional);
                        tooltip.add(hardpoints);
                    });
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X + 1, SLOT / 2 + 1)
                .setBackground(this.slot, -1, -1)
                .addItemStack(new ItemStack(ModItems.EXTRACTOR));
    }

    @Override
    public void draw(Page page, IRecipeSlotsView slots, PoseStack poseStack, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        this.arrow.draw(poseStack, ARROW_X, SLOT / 2 + 1);
        font.draw(poseStack, new TranslatableComponent("jei.woldsvaults.extractor_assembler.optional"), 0, TEXT_Y, TEXT_COLOR);
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
