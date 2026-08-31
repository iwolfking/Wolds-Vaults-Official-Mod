package xyz.iwolfking.woldsvaults.mixins.botania.jei10;

import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraftforge.fluids.FluidStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import vazkii.botania.client.integration.jei.PureDaisyRecipeCategory;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "botania"),
        @Condition(type = Condition.Type.MOD, value = "jei")
    }
)
@Mixin(value = PureDaisyRecipeCategory.class, remap = false)
public class MixinPureDaisyRecipeCategory {
    @SuppressWarnings({"removal"})
    @Redirect(method = "setRecipe(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;Lvazkii/botania/api/recipe/IPureDaisyRecipe;Lmezz/jei/api/recipe/IFocusGroup;)V",
        at = @At(value = "FIELD", target = "Lmezz/jei/api/constants/VanillaTypes;FLUID:Lmezz/jei/api/ingredients/IIngredientType;", opcode = Opcodes.GETSTATIC))
    private IIngredientType<FluidStack> replaceRef(){
        return ForgeTypes.FLUID;
    }
}
