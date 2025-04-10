package xyz.iwolfking.woldsvaults.integration.jei.category;

import com.blakebr0.cucumber.util.Localizable;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.recipes.lib.InfuserRecipe;

import java.util.Collections;
import java.util.List;

public class InfuserCraftingCategory implements IRecipeCategory<InfuserRecipe> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(WoldsVaults.MOD_ID, "textures/gui/jei/infuser.png");
    public static final ResourceLocation UID = new ResourceLocation(WoldsVaults.MOD_ID, "infuser");

    private final IDrawable background;
    private final IDrawable icon;

    public InfuserCraftingCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 149, 78);
        this.icon = helper.createDrawableIngredient(new ItemStack(ModBlocks.VAULT_INFUSER_BLOCK));
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public Class<? extends InfuserRecipe> getRecipeClass() {
        return InfuserRecipe.class;
    }

    @Override
    public Component getTitle() {
        return Localizable.of("jei.category.woldsvaults.infuser").build();
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
    public List<Component> getTooltipStrings(InfuserRecipe recipe, double mouseX, double mouseY) {

        if (mouseX > 54 && mouseX < 78 && mouseY > 58 && mouseY < 68) {
            return Collections.singletonList(new TextComponent(String.valueOf(recipe.getInputCount())));
        }

        return Collections.emptyList();
    }

    @Override
    public void setIngredients(InfuserRecipe recipe, IIngredients ingredients) {
        ingredients.setOutput(VanillaTypes.ITEM, recipe.getResultItem());

        NonNullList<Ingredient> inputs = NonNullList.create();

        inputs.addAll(recipe.getIngredients());
        inputs.add(recipe.getCatalyst());

        ingredients.setInputIngredients(inputs);
    }

    @Override
    public void setRecipe(IRecipeLayout layout, InfuserRecipe recipe, IIngredients ingredients) {
        var stacks = layout.getItemStacks();
        var inputs = ingredients.getInputs(VanillaTypes.ITEM);
        var outputs = ingredients.getOutputs(VanillaTypes.ITEM).get(0);

        stacks.init(0, true, 57, 30);
        stacks.init(1, true, 30, 30);
        stacks.init(2, false, 127, 30);

        stacks.set(0, inputs.get(0));
        stacks.set(1, inputs.get(1));
        stacks.set(2, outputs);
    }
}
