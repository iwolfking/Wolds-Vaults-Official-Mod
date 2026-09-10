package xyz.iwolfking.woldsvaults.integration.arsnouveau.init;

import net.minecraft.world.item.crafting.RecipeSerializer;
import xyz.iwolfking.woldsvaults.integration.arsnouveau.recipe.VaultAugmentInfusionRecipe;
import xyz.iwolfking.woldsvaults.integration.arsnouveau.recipe.VaultCatalystInfusionRecipe;
import xyz.iwolfking.woldsvaults.integration.arsnouveau.recipe.VaultGearModificationRecipe;

public class ArsRecipeSerializers {
    public static final RecipeSerializer<VaultGearModificationRecipe> VAULT_GEAR_ENCHANTING_APPARATUS = new VaultGearModificationRecipe.Serializer();
    public static final RecipeSerializer<VaultCatalystInfusionRecipe> VAULT_CATALYST_INFUSION = new VaultCatalystInfusionRecipe.Serializer();
    public static final RecipeSerializer<VaultAugmentInfusionRecipe> VAULT_AUGMENT_INFUSION = new VaultAugmentInfusionRecipe.Serializer();
}
