package xyz.iwolfking.woldsvaults.mixins.powah.jei10;

import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraftforge.fluids.FluidStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import owmii.powah.forge.compat.jei.HeatSourceCategory;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "powah"),
        @Condition(type = Condition.Type.MOD, value = "jei")
    }
)
@Mixin(value = HeatSourceCategory.class, remap = false)
public class MixinHeatSourceCategory {
    @SuppressWarnings("removal")
    @Redirect(method = "setIngredients(Lowmii/powah/forge/compat/jei/HeatSourceCategory$Recipe;Lmezz/jei/api/ingredients/IIngredients;)V", at = @At(value = "FIELD", target = "Lmezz/jei/api/constants/VanillaTypes;FLUID:Lmezz/jei/api/ingredients/IIngredientType;", opcode = Opcodes.GETSTATIC))
    private IIngredientType<FluidStack> replaceRef(){
        return ForgeTypes.FLUID;
    }

    /**
     * @author radimous
     * @reason JEI 10 compat
     */
    @SuppressWarnings("removal")
    @Overwrite
    public void setRecipe(IRecipeLayout iRecipeLayout, HeatSourceCategory.Recipe recipe, IIngredients ingredients) {
        if (recipe.fluid() == null) {
            IGuiItemStackGroup itemStacks = iRecipeLayout.getItemStacks();
            itemStacks.init(0, true, 3, 4);
            itemStacks.set(ingredients);
        } else {
            IGuiIngredientGroup<FluidStack> fluidStack = iRecipeLayout.getIngredientsGroup(ForgeTypes.FLUID);
            fluidStack.init(0, true, 4, 5);
            fluidStack.set(ingredients);
        }
    }
}
