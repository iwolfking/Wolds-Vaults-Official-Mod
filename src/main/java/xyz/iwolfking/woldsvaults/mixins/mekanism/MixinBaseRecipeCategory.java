package xyz.iwolfking.woldsvaults.mixins.mekanism;

import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.CardItem;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.gui.element.slot.GuiSlot;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

@Mixin(value = BaseRecipeCategory.class, remap = false)
public abstract class MixinBaseRecipeCategory {

    @ModifyVariable(
        method = "initItem(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;Lmezz/jei/api/recipe/RecipeIngredientRole;Lmekanism/client/gui/element/slot/GuiSlot;Ljava/util/List;)Lmezz/jei/api/gui/builder/IRecipeSlotBuilder;",
        at = @At("HEAD"),
        argsOnly = true
    )
    private List<ItemStack> modifyInputStacks(List<ItemStack> stacks, IRecipeLayoutBuilder builder, RecipeIngredientRole role, GuiSlot slot) {
        if (stacks == null || stacks.isEmpty()) {
            return stacks;
        }

        return stacks.stream().map(stack -> {
            if(stack.getItem() instanceof CardItem) {
                if(role.equals(RecipeIngredientRole.INPUT)) {
                    return CardItem.create(ModConfigs.BOOSTER_PACK.getOutcomes("the_vault:stat_pack", JavaRandom.ofInternal(123L)).get(0));
                }
            }
            
            return stack;
        }).toList();
    }
}