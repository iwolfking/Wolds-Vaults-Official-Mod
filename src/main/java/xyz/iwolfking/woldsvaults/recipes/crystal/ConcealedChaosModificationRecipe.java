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
import xyz.iwolfking.woldsvaults.items.ConcealedChaosItem;
import xyz.iwolfking.woldsvaults.items.lib.IVaultCrystalModifier;
import xyz.iwolfking.woldsvaults.recipes.crystal.lib.AbstractCrystalModificationRecipe;

import java.util.List;

public class ConcealedChaosModificationRecipe extends AbstractCrystalModificationRecipe<ConcealedChaosItem> {
    @Override
    protected ConcealedChaosItem getCraftingIngredient() {
        return ModItems.CONCEALED_CHAOS;
    }

    @Override
    public void onRegisterJEI(IRecipeRegistration registry) {
        ItemStack ingredient = new ItemStack(ModItems.CONCEALED_CHAOS);
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

        registry.addRecipes(List.of(new CrystalWorkbenchRecipe(ingredient, crystalOutput, List.of("Adds random positive modifiers ", "with a chance of making crystal", "unmodifiable and adding", "some negative modifiers", "as well."))), CrystalWorkbenchRecipeCategory.UID);
    }

}
