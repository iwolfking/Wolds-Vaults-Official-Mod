package xyz.iwolfking.woldsvaults.integration.arsnouveau.init;

import com.hollingsworth.arsnouveau.api.enchanting_apparatus.EnchantingApparatusRecipe;
import net.minecraft.core.Registry;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

public class ArsRecipeTypes {
    public static final RecipeType<EnchantingApparatusRecipe> VAULT_GEAR_APPARATUS_TYPE = new ModRecipeType<>();
    public static final RecipeType<EnchantingApparatusRecipe> CATALYST_APPARATUS_TYPE = new ModRecipeType<>();

    private static class ModRecipeType<T extends Recipe<?>> implements RecipeType<T> {
        public String toString() {
            return Registry.RECIPE_TYPE.getKey(this).toString();
        }
    }
}
