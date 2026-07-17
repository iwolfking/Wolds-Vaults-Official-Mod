package xyz.iwolfking.woldsvaults.recipes.crystal;

import iskallia.vault.block.VaultPortalBlock;
import iskallia.vault.block.entity.VaultPortalTileEntity;
import iskallia.vault.config.VaultCrystalConfig;
import iskallia.vault.config.entry.LevelEntryList;
import iskallia.vault.core.vault.modifier.VaultModifierStack;
import iskallia.vault.core.vault.modifier.modifier.GroupedModifier;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.item.crystal.CrystalEntry;
import iskallia.vault.item.crystal.VaultCrystalItem;
import iskallia.vault.item.crystal.layout.*;
import iskallia.vault.recipe.anvil.AnvilContext;
import iskallia.vault.recipe.anvil.VanillaAnvilRecipe;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.vhapi.integration.jevh.categories.CrystalWorkbenchRecipeCategory;
import xyz.iwolfking.vhapi.integration.jevh.lib.CrystalWorkbenchRecipe;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.items.LayoutModificationItem;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.CompoundCrystalLayoutAccessor;
import xyz.iwolfking.woldsvaults.modifiers.vault.map.modifiers.GreedyVaultModifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class LayoutModificationRecipe extends VanillaAnvilRecipe {
    @Override
    public boolean onSimpleCraft(AnvilContext context) {
        ItemStack primary = context.getInput()[0];
        ItemStack secondary = context.getInput()[1];
        if (primary.getItem() instanceof VaultCrystalItem && secondary.getItem() == ModItems.LAYOUT_MANIPULATOR) {
            ItemStack output = primary.copy();
            CrystalData data = CrystalData.read(output);

            if(data.getProperties().isUnmodifiable()) {
                return false;
            }

            if(LayoutModificationItem.getLayout(secondary).isEmpty()) {
                return false;
            }

            CrystalLayout newLayout = LayoutModificationItem.getLayout(secondary).get();

            if (data.getLayout() instanceof CompoundCrystalLayout compound) {
                List<CrystalLayout> architectLayouts = new ArrayList<>(compound.getChildren().stream()
                        .filter(child -> child instanceof CrystalLayout)
                        .map(child -> (CrystalLayout) child)
                        .filter(cl -> cl instanceof ArchitectCrystalLayout)
                        .toList());

                architectLayouts.add(newLayout);

                ((CompoundCrystalLayoutAccessor) compound).setChildren(architectLayouts);
            } else {
                data.setLayout(newLayout);
            }


            data.write(output);
            context.setOutput(output);

            context.onTake(context.getTake().append(() -> {
                context.getInput()[0].shrink(1);
                context.getInput()[1].shrink(1);
            }));
            return true;
        } else {
            return false;
        }
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
