package xyz.iwolfking.woldsvaults.mixins.jeienchantmentinfo;

import com.github.phylogeny.jeienchantmentinfo.JEIEnchantmentInfo;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "jeienchantmentinfo")
        }
)
@Mixin(value = JEIEnchantmentInfo.class, remap = false)
public class MixinJEIEnchantmentInfo {
    @WrapWithCondition(method = "lambda$registerRecipes$4", at = @At(value = "INVOKE", target = "Lmezz/jei/api/registration/IRecipeRegistration;addIngredientInfo(Ljava/util/List;Lmezz/jei/api/ingredients/IIngredientType;[Lnet/minecraft/network/chat/Component;)V"))
    private static <T> boolean dontRegisterIfEmpty(IRecipeRegistration instance, List<T> ts, IIngredientType<T> tiIngredientType, Component[] components){
        return !ts.isEmpty();
    }
}
