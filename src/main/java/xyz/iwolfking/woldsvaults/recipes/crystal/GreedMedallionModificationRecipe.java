package xyz.iwolfking.woldsvaults.recipes.crystal;

import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.item.crystal.VaultCrystalItem;
import iskallia.vault.recipe.anvil.AnvilContext;
import iskallia.vault.recipe.anvil.VanillaAnvilRecipe;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AnvilBlock;
import xyz.iwolfking.vhapi.integration.jevh.categories.CrystalWorkbenchRecipeCategory;
import xyz.iwolfking.vhapi.integration.jevh.lib.CrystalWorkbenchRecipe;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.items.GreedMedallionItem;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionApplication;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionTier;

import java.util.ArrayList;
import java.util.List;

/** Applies a greed medallion to a vault crystal through the workbench's anvil flow; false refuses. */
public class GreedMedallionModificationRecipe extends VanillaAnvilRecipe {
    @Override
    public boolean onSimpleCraft(AnvilContext context) {
        if (context.getBlockState().map(state -> state.getBlock() instanceof AnvilBlock).orElse(false)) {
            return false;
        }

        ItemStack primary = context.getInput()[0];
        ItemStack secondary = context.getInput()[1];
        if (!(primary.getItem() instanceof VaultCrystalItem)) {
            return false;
        }
        GreedMedallionTier tier = GreedMedallionItem.getTier(secondary);
        if (tier == null) {
            return false;
        }

        ItemStack output = primary.copy();
        CrystalData data = CrystalData.read(output);
        if (!GreedMedallionApplication.canApply(context.getPlayer().orElse(null), data, tier)) {
            return false;
        }

        GreedMedallionApplication.apply(data, tier);
        data.write(output);
        context.setOutput(output);
        context.onTake(context.getTake().append(() -> {
            context.getInput()[0].shrink(1);
            context.getInput()[1].shrink(1);
        }));
        return true;
    }

    @Override @SuppressWarnings("removal")
    public void onRegisterJEI(IRecipeRegistration registry) {
        List<CrystalWorkbenchRecipe> recipes = new ArrayList<>();
        ModItems.GREED_MEDALLIONS.forEach((tier, item) -> {
            ItemStack crystalOutput = VaultCrystalItem.create(crystalData -> {
                crystalData.getProperties().setLevel(0);
                GreedMedallionApplication.apply(crystalData, tier);
            });
            recipes.add(new CrystalWorkbenchRecipe(new ItemStack(item), crystalOutput));
        });
        registry.addRecipes(recipes, CrystalWorkbenchRecipeCategory.UID);
    }
}
