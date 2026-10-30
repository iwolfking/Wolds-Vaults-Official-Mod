package xyz.iwolfking.woldsvaults.mixins.decorative_blocks.jei10;

import lilypuree.decorative_blocks.compat.jei.DecorativeBlocksJEIPlugin;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraftforge.fluids.FluidStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "decorative_blocks")
    }
)
@Mixin(value = DecorativeBlocksJEIPlugin.class, remap = false)
public class MixinDecorativeBlocksJEIPlugin {
    @SuppressWarnings("removal")
    @Redirect(method = "registerRecipes", at = @At(value = "FIELD", target = "Lmezz/jei/api/constants/VanillaTypes;FLUID:Lmezz/jei/api/ingredients/IIngredientType;", opcode = Opcodes.GETSTATIC))
    private IIngredientType<FluidStack> replaceRef(){
        return ForgeTypes.FLUID;
    }
}
