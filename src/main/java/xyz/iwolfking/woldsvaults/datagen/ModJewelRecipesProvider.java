package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.init.ModBlocks;
import iskallia.vault.init.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.vhapi.api.datagen.recipes.AbstractJewelCraftingRecipesProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;

public class ModJewelRecipesProvider extends AbstractJewelCraftingRecipesProvider {
    protected ModJewelRecipesProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        add("wold_jewel_recipes", builder -> {
            builder.addRecipe(WoldsVaults.id("jewel/dismantle_chance"), ModGearAttributes.DISMANTLE_CHANCE.getRegistryName(), 1, "Mining", ingredients -> {
               ingredients.add(new ItemStack(ModItems.GEMSTONE));
               ingredients.add(new ItemStack(ModItems.SILVER_SCRAP, 64));
               ingredients.add(new ItemStack(ModBlocks.VAULT_GOLD, 1));
            });
        });
    }
}
