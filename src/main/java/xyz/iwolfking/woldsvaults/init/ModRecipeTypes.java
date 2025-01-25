package xyz.iwolfking.woldsvaults.init;

import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import xyz.iwolfking.woldsvaults.recipes.lib.InfuserRecipe;

import java.util.Optional;

public class ModRecipeTypes {
    public static final RecipeType<InfuserRecipe> INFUSER = new RecipeType<InfuserRecipe>() {
        @Override
        public <C extends Container> Optional<InfuserRecipe> tryMatch(Recipe<C> recipe, Level world, C inv) {
            return recipe.matches(inv, world) ? Optional.of((InfuserRecipe) recipe) : Optional.empty();
        }
    };
}
