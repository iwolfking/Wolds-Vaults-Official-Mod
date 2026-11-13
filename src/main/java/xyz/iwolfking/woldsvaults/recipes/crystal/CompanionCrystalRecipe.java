package xyz.iwolfking.woldsvaults.recipes.crystal;

import iskallia.vault.item.CompanionItem;
import mezz.jei.api.registration.IRecipeRegistration;
import xyz.iwolfking.woldsvaults.recipes.crystal.lib.AbstractCrystalModificationRecipe;

public class CompanionCrystalRecipe extends AbstractCrystalModificationRecipe<CompanionItem> {
    @Override
    protected CompanionItem getCraftingIngredient() {
        return iskallia.vault.init.ModItems.COMPANION;
    }

    @Override @SuppressWarnings("removal")
    public void onRegisterJEI(IRecipeRegistration registry) {
//
//        ItemStack map = new ItemStack(ModItems.MAP);
//        if(map.getItem() instanceof IdentifiableItem identifiableItem) {
//            identifiableItem.instantIdentify(null, map);
//        }
//
//        VaultGearData mapData = VaultGearData.read(map);
//
//        ItemStack crystalOutput = VaultCrystalItem.create(crystalData -> {
//            crystalData.getProperties().setLevel(100);
//            applySpecialModifiers(crystalData, mapData, VaultGearModifier.AffixType.IMPLICIT, null, null, false);
//            applySpecialModifiers(crystalData, mapData, VaultGearModifier.AffixType.PREFIX, null, null, false);
//            applySpecialModifiers(crystalData, mapData, VaultGearModifier.AffixType.SUFFIX, null, null, false);
//            crystalData.getProperties().setUnmodifiable(true);
//        });
//
//        registry.addRecipes(List.of(new CrystalWorkbenchRecipe(map, crystalOutput, List.of("Consume 10 x Tier capacity"))), CrystalWorkbenchRecipeCategory.UID);
    }

}
