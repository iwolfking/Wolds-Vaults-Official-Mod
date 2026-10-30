package xyz.iwolfking.woldsvaults.integration.mekanism.recipe.jei;

import mekanism.common.registries.MekanismBlocks;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.integration.mekanism.init.ModModuleToVaultGearModifications;

import java.util.ArrayList;
import java.util.List;

public class MekanismJEIProvider {

    public static void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new MekanismModificationWorkbenchCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    public static void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
                new ItemStack(MekanismBlocks.MODIFICATION_STATION),
                MekanismModificationWorkbenchCategory.UID
        );
    }

    public static void registerRecipes(IRecipeRegistration registration) {
        List<MekanismModificationWorkbenchCategory.MekanismModificationStationRecipe> recipes = new ArrayList<>();

        ModModuleToVaultGearModifications.getRegisteredModifiers().forEach((moduleItem, modifier) -> {
            ItemStack moduleStack = new ItemStack(moduleItem);
            List<ItemStack> inputGears = ModModuleToVaultGearModifications.getMatchingGearSamples(modifier.itemsSupported());

            List<ItemStack> outputGears = new ArrayList<>();
            for (ItemStack input : inputGears) {
                outputGears.add(modifier.apply(input));
            }

            if (!inputGears.isEmpty()) {
                recipes.add(new MekanismModificationWorkbenchCategory.MekanismModificationStationRecipe(
                        moduleStack,
                        inputGears,
                        outputGears
                ));
            }
        });

        registration.addRecipes(recipes, MekanismModificationWorkbenchCategory.UID);
    }
}
