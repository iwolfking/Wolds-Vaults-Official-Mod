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
import net.minecraft.client.Minecraft;
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
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.items.FabricationPatternItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** JEI pages for the Vault Fabricator: one per component, showing the pattern, the 5x5 grid, the fluid, energy and time. */
public class VaultFabricatorCategory implements IRecipeCategory<VaultFabricatorCategory.Page> {
    public static final RecipeType<Page> TYPE = RecipeType.create(WoldsVaults.MOD_ID, "vault_fabricator", Page.class);

    private static final int GRID = 5;
    private static final int SLOT = 18;
    private static final int GRID_X = 24;
    private static final int OUTPUT_X = GRID_X + GRID * SLOT + 30;
    private static final int TEXT_Y = GRID * SLOT + 4;

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableStatic slot;
    private final IDrawableStatic arrow;

    public VaultFabricatorCategory(IGuiHelper helper) {
        this.background = helper.createBlankDrawable(OUTPUT_X + 22, TEXT_Y + 12);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.VAULT_FABRICATOR_BLOCK));
        this.slot = helper.getSlotDrawable();
        this.arrow = helper.createDrawable(WoldsVaults.id("textures/gui/trinket_fuser.png"), 176, 0, 24, 16);
    }

    /** One Vault Fabricator recipe: its component key and table entry. */
    public record Page(String key, ExtractorCostsConfig.ComponentEntry entry) {
    }

    /** Every component recipe of the loaded costs table, in table order. */
    public static List<Page> pages() {
        List<Page> pages = new ArrayList<>();
        ExtractorCostsConfig costs = ExtractorCosts.config();
        if (costs == null || costs.COMPONENTS == null || costs.COMPONENTS.isEmpty()) {
            WoldLogger.error("The extractor costs table has no Vault Fabricator components; JEI shows no Vault Fabricator pages.");
            return pages;
        }
        for (Map.Entry<String, ExtractorCostsConfig.ComponentEntry> entry : costs.COMPONENTS.entrySet()) {
            pages.add(new Page(entry.getKey(), entry.getValue()));
        }
        return pages;
    }

    @Override
    public RecipeType<Page> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return new TranslatableComponent("jei.woldsvaults.vault_fabricator");
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
        builder.addSlot(RecipeIngredientRole.CATALYST, 1, 2 * SLOT + 1)
                .setBackground(this.slot, -1, -1)
                .addItemStack(FabricationPatternItem.encode(page.key()));
        for (int index = 0; index < GRID * GRID; index++) {
            int x = GRID_X + (index % GRID) * SLOT + 1;
            int y = (index / GRID) * SLOT + 1;
            var cell = builder.addSlot(RecipeIngredientRole.INPUT, x, y).setBackground(this.slot, -1, -1);
            String id = page.entry().cell(index);
            Item item = id == null ? null : ExtractorCosts.resolveItem(id);
            if (item != null) {
                cell.addItemStack(new ItemStack(item));
            }
        }
        Item product = ExtractorCosts.resolveItem(page.entry().item);
        if (product != null) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X + 1, 2 * SLOT + 1)
                    .setBackground(this.slot, -1, -1)
                    .addItemStack(new ItemStack(product));
        }
        if (page.entry().fluid != null && !page.entry().fluid.isEmpty() && page.entry().fluidAmount > 0) {
            Fluid fluid = ExtractorCosts.resolveFluid(page.entry().fluid);
            if (fluid != null) {
                builder.addSlot(RecipeIngredientRole.INPUT, OUTPUT_X + 1, 1)
                        .setFluidRenderer(page.entry().fluidAmount, false, 16, 2 * SLOT - 4)
                        .addFluidStack(fluid, page.entry().fluidAmount);
            }
        }
    }

    @Override
    public void draw(Page page, IRecipeSlotsView slots, PoseStack poseStack, double mouseX, double mouseY) {
        this.arrow.draw(poseStack, GRID_X + GRID * SLOT + 3, 2 * SLOT + 1);
        Component text = new TranslatableComponent("jei.woldsvaults.vault_fabricator.cost",
                String.format("%,d", page.entry().fe), String.format("%.1f", page.entry().ticks / 20.0D));
        Minecraft.getInstance().font.draw(poseStack, text, 0, TEXT_Y, 0x404040);
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
