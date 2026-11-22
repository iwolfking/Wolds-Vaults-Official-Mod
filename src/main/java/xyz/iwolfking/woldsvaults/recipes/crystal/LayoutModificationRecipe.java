package xyz.iwolfking.woldsvaults.recipes.crystal;

import iskallia.vault.config.VaultCrystalConfig;
import iskallia.vault.config.entry.LevelEntryList;
import iskallia.vault.item.crystal.VaultCrystalItem;
import iskallia.vault.item.crystal.layout.*;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.vhapi.integration.jevh.categories.CrystalWorkbenchRecipeCategory;
import xyz.iwolfking.vhapi.integration.jevh.lib.CrystalWorkbenchRecipe;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.items.LayoutModificationItem;
import xyz.iwolfking.woldsvaults.recipes.crystal.lib.AbstractCrystalModificationRecipe;
import java.util.ArrayList;
import java.util.List;

public class LayoutModificationRecipe extends AbstractCrystalModificationRecipe<LayoutModificationItem> {

    @Override
    protected LayoutModificationItem getCraftingIngredient() {
        return ModItems.LAYOUT_MANIPULATOR;
    }

    @Override @SuppressWarnings("removal")
    public void onRegisterJEI(IRecipeRegistration registry) {
        List<CrystalWorkbenchRecipe> recipes = new ArrayList<>();

        LevelEntryList<VaultCrystalConfig.LayoutEntry> layoutEntries = ModConfigs.ETCHED_VAULT_LAYOUT.ETCHED_VAULT_LAYOUTS.get("default");

        if(layoutEntries != null) {
            layoutEntries.getForLevel(100).ifPresent(layoutEntry -> {
                layoutEntry.pool.forEach((crystalLayout, aDouble) -> {
                    ItemStack layoutItem = LayoutModificationItem.create(crystalLayout);
                    ItemStack crystalOutput = VaultCrystalItem.create(crystalData -> {
                        crystalData.getProperties().setLevel(0);
                        CrystalLayout layout = LayoutModificationItem.getLayout(layoutItem).orElse(new ClassicInfiniteCrystalLayout(1));
                        crystalData.setLayout(layout);
                    });
                    recipes.add(new CrystalWorkbenchRecipe(layoutItem, crystalOutput));
                });
            });
        }

        registry.addRecipes(recipes, CrystalWorkbenchRecipeCategory.UID);

    }

}
