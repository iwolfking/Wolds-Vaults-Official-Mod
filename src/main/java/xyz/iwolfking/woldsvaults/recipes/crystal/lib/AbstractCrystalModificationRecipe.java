package xyz.iwolfking.woldsvaults.recipes.crystal.lib;

import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.item.crystal.VaultCrystalItem;
import iskallia.vault.recipe.anvil.AnvilContext;
import iskallia.vault.recipe.anvil.VanillaAnvilRecipe;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AnvilBlock;
import xyz.iwolfking.woldsvaults.items.lib.IVaultCrystalModifier;

public abstract class AbstractCrystalModificationRecipe<T extends Item & IVaultCrystalModifier> extends VanillaAnvilRecipe {

    public boolean onSimpleCraft(AnvilContext context) {
        if (context.getBlockState().map((state) -> state.getBlock() instanceof AnvilBlock).orElse(false)) {
            return false;
        }

        ItemStack primary = context.getInput()[0];
        ItemStack secondary = context.getInput()[1];
        if (primary.getItem() instanceof VaultCrystalItem && secondary.getItem() == getCraftingIngredient()) {
            ItemStack output = primary.copy();
            CrystalData data = CrystalData.read(output);

            if (data.getProperties().isUnmodifiable()) {
                return false;
            }

            if (!(secondary.getItem().getClass().isInstance(getCraftingIngredient()))) {
                return false;
            }
            else {
                return getCraftingIngredient().applyCrystalRecipe(context, data, secondary, output);
            }
        }

        return false;
    }

    protected abstract T getCraftingIngredient();
}
