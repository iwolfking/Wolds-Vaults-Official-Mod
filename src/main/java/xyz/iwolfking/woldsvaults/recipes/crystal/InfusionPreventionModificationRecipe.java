package xyz.iwolfking.woldsvaults.recipes.crystal;

import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.item.crystal.VaultCrystalItem;
import iskallia.vault.item.crystal.properties.CapacityCrystalProperties;
import iskallia.vault.recipe.anvil.AnvilContext;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.vhapi.integration.jevh.categories.CrystalWorkbenchRecipeCategory;
import xyz.iwolfking.vhapi.integration.jevh.lib.CrystalWorkbenchRecipe;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.items.InfusionPreventionItem;
import xyz.iwolfking.woldsvaults.items.lib.IVaultCrystalModifier;
import xyz.iwolfking.woldsvaults.recipes.crystal.lib.AbstractCrystalModificationRecipe;

import java.util.List;

public class InfusionPreventionModificationRecipe extends AbstractCrystalModificationRecipe<InfusionPreventionItem> {
    @Override
    protected InfusionPreventionItem getCraftingIngredient() {
        return ModItems.INFUSION_PREVENTION;
    }

    @Override @SuppressWarnings("removal")
    public void onRegisterJEI(IRecipeRegistration registry) {
        ItemStack ingredient = new ItemStack(ModItems.INFUSION_PREVENTION);
        ItemStack crystalOutput = VaultCrystalItem.create(crystalData -> {
            crystalData.getProperties().setLevel(100);
            crystalData.setProperties(new CapacityCrystalProperties());
            if(crystalData.getProperties() instanceof CapacityCrystalProperties capacityCrystalProperties) {
                capacityCrystalProperties.setSize(300);
            }
        });

        if(ingredient.getItem() instanceof IVaultCrystalModifier crystalModifier) {
            crystalModifier.applyCrystalRecipe(AnvilContext.ofSimulated(crystalOutput, ingredient), CrystalData.read(crystalOutput), ingredient, crystalOutput);
        }

        registry.addRecipes(List.of(new CrystalWorkbenchRecipe(ingredient, crystalOutput, List.of("Adds Dulled, preventing a crystal from randomly being Infused"))), CrystalWorkbenchRecipeCategory.UID);
    }

}
