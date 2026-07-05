package xyz.iwolfking.woldsvaults.integration.occultism.init;


import net.minecraft.core.Registry;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import xyz.iwolfking.woldsvaults.integration.occultism.AugmentRitualRecipe;

public class OccultismRecipeTypes {
    public static final RecipeType<AugmentRitualRecipe> AUGMENT_RITUAL_RECIPE = new OccultismRecipeTypes.ModRecipeType<>();

    private static class ModRecipeType<T extends Recipe<?>> implements RecipeType<T> {
        public String toString() {
            return Registry.RECIPE_TYPE.getKey(this).toString();
        }
    }
}
