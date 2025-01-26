package xyz.iwolfking.woldsvaults.data.recipes;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import xyz.iwolfking.woldsvaults.init.ModRecipeTypes;
import xyz.iwolfking.woldsvaults.recipes.lib.InfuserRecipe;

import java.util.HashSet;
import java.util.Set;

public class CachedInfuserRecipeData {
    private static final Set<Item> CATALYST_ITEMS = new HashSet<>();
    private static final Set<Item> INGREDIENT_ITEMS = new HashSet<>();

    public static void cacheCatalysts(Level level) {
        System.out.println("CACHED CATALYSTS");
        for(InfuserRecipe infuserRecipe : level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.INFUSER)) {
            for(ItemStack catalystStack : infuserRecipe.getCatalyst().getItems()) {
                CATALYST_ITEMS.add(catalystStack.getItem());
            }
        }
    }

    public static void cacheIngredients(Level level) {
        System.out.println("CACHED INGREDIENTS");
        for(InfuserRecipe infuserRecipe : level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.INFUSER)) {
            for(Ingredient ingredient : infuserRecipe.getIngredients()) {
                for(ItemStack stack : ingredient.getItems()) {
                    INGREDIENT_ITEMS.add(stack.getItem());
                }
            }
        }
    }

    public static Set<Item> getCatalysts(Level level) {
        if(CATALYST_ITEMS.isEmpty()) {
            CachedInfuserRecipeData.cacheCatalysts(level);
        }

        return CATALYST_ITEMS;
    }

    public static Set<Item> getCatalysts() {
        return CATALYST_ITEMS;
    }

    public static Set<Item> getIngredients(Level level) {
        if(INGREDIENT_ITEMS.isEmpty()) {
            CachedInfuserRecipeData.cacheIngredients(level);
        }

        return INGREDIENT_ITEMS;
    }

    public static Set<Item> getIngredients() {
        return INGREDIENT_ITEMS;
    }

    public static boolean shouldCache() {
        return CATALYST_ITEMS.isEmpty() || INGREDIENT_ITEMS.isEmpty();
    }


}
