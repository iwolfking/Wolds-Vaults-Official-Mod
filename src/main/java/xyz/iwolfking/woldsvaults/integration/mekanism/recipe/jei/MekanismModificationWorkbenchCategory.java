package xyz.iwolfking.woldsvaults.integration.mekanism.recipe.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import mekanism.common.registries.MekanismBlocks;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.List;

public class MekanismModificationWorkbenchCategory implements IRecipeCategory<MekanismModificationWorkbenchCategory.MekanismModificationStationRecipe> {
    public static final ResourceLocation UID = WoldsVaults.id("mekanism_modification_station");
    
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slotBackground;

    public MekanismModificationWorkbenchCategory(IGuiHelper helper) {
        this.background = helper.createBlankDrawable(140, 60);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(MekanismBlocks.MODIFICATION_STATION));
        this.slotBackground = helper.getSlotDrawable();
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public Class<? extends MekanismModificationStationRecipe> getRecipeClass() {
        return MekanismModificationStationRecipe.class;
    }

    @Override
    public Component getTitle() {
        return new TranslatableComponent("gui.woldsvaults.jei.mekanism_modification_station");
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
    public void setRecipe(IRecipeLayoutBuilder builder, MekanismModificationStationRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 15, 20)
                .addItemStack(recipe.moduleInput());

        builder.addSlot(RecipeIngredientRole.INPUT, 45, 20)
                .addItemStacks(recipe.supportedGearInputs());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 105, 20)
                .addItemStacks(recipe.gearOutputs());
    }

    @Override
    public void draw(MekanismModificationStationRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack matrixStack, double mouseX, double mouseY) {
        this.slotBackground.draw(matrixStack, 15, 20);
        this.slotBackground.draw(matrixStack, 45, 20);
        this.slotBackground.draw(matrixStack, 105, 20);
    }

    public record MekanismModificationStationRecipe(
            ItemStack moduleInput,
            List<ItemStack> supportedGearInputs,
            List<ItemStack> gearOutputs
    ) {}
}