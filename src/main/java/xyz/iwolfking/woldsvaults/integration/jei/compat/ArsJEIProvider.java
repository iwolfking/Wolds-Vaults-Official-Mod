package xyz.iwolfking.woldsvaults.integration.jei.compat;

import com.hollingsworth.arsnouveau.client.jei.EnchantingApparatusRecipeCategory;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.integration.arsnouveau.recipe.VaultGearModificationRecipe;

import java.util.List;

public class ArsJEIProvider {

    public static void registerRecipes(IRecipeRegistration registration) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        RecipeManager recipeManager = level.getRecipeManager();

        List<VaultGearModificationRecipe> modificationRecipes = (List) recipeManager.getAllRecipesFor(
                (net.minecraft.world.item.crafting.RecipeType) Registry.RECIPE_TYPE.get(
                        ResourceLocation.fromNamespaceAndPath(WoldsVaults.MOD_ID, VaultGearModificationRecipe.RECIPE_ID)
                )
        );

        // Register them to Ars Nouveau's existing Enchanting Apparatus UI category
        registration.addRecipes(modificationRecipes, EnchantingApparatusRecipeCategory.UID);
    }

}
