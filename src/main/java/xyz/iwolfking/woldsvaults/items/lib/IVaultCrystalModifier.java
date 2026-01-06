package xyz.iwolfking.woldsvaults.items.lib;

import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.recipe.anvil.AnvilContext;
import net.minecraft.world.item.ItemStack;

public interface IVaultCrystalModifier {
    boolean applyCrystalRecipe(AnvilContext context, CrystalData data, ItemStack ingredient, ItemStack output);
}
