package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.config.entry.ItemEntry;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.vhapi.api.datagen.lib.BasicListBuilder;
import xyz.iwolfking.vhapi.api.datagen.recipes.AbstractCrystalSealRecipesProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModItems;

import java.util.function.Consumer;

public class ModCrystalSealRecipeProvider extends AbstractCrystalSealRecipesProvider {
    protected ModCrystalSealRecipeProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        addFileForSeal(new ItemStack(ModItems.CRYSTAL_SEAL_ALCHEMY), 50, 0, ingredients -> {
            ingredients.add(new ItemEntry(iskallia.vault.init.ModItems.CRYSTAL_SEAL_EMPTY, 1));
            ingredients.add(new ItemEntry(iskallia.vault.init.ModItems.ALEXANDRITE_GEM, 32));
            ingredients.add(new ItemEntry(iskallia.vault.init.ModItems.VAULT_ESSENCE, 64));
        });
        addFileForSeal(new ItemStack(ModItems.CRYSTAL_SEAL_DOOMSAYER), 50, 0, ingredients -> {
            ingredients.add(new ItemEntry(iskallia.vault.init.ModItems.CRYSTAL_SEAL_PROPHET, 1));
            ingredients.add(new ItemEntry(iskallia.vault.init.ModItems.ALEXANDRITE_GEM, 32));
            ingredients.add(new ItemEntry(iskallia.vault.init.ModItems.VAULT_ESSENCE, 64));
        });
        addFileForSeal(new ItemStack(ModItems.CRYSTAL_SEAL_SPIRITS), 50, 0, ingredients -> {
            ingredients.add(new ItemEntry(iskallia.vault.init.ModItems.CRYSTAL_SEAL_SCOUT, 1));
            ingredients.add(new ItemEntry(iskallia.vault.init.ModItems.ALEXANDRITE_GEM, 32));
            ingredients.add(new ItemEntry(iskallia.vault.init.ModItems.ETERNAL_SOUL, 8));
        });
        addFileForSeal(new ItemStack(ModItems.CRYSTAL_SEAL_ENCHANTER), 50, 0, ingredients -> {
            ingredients.add(new ItemEntry(iskallia.vault.init.ModItems.CRYSTAL_SEAL_SAGE, 1));
            ingredients.add(new ItemEntry(iskallia.vault.init.ModItems.ALEXANDRITE_GEM, 32));
            ingredients.add(new ItemEntry(iskallia.vault.init.ModItems.VAULT_ESSENCE, 64));
        });
        addFileForSeal(new ItemStack(ModItems.CRYSTAL_SEAL_UNHINGED), 50, 0, ingredients -> {
            ingredients.add(new ItemEntry(iskallia.vault.init.ModItems.CRYSTAL_SEAL_HUNTER, 1));
            ingredients.add(new ItemEntry(iskallia.vault.init.ModItems.CRYSTAL_SEAL_CHAOS, 1));
            ingredients.add(new ItemEntry(iskallia.vault.init.ModItems.ALEXANDRITE_GEM, 32));
        });
        addFileForSeal(new ItemStack(ModItems.CRYSTAL_SEAL_SURVIVOR), 50, 0, ingredients -> {
            ingredients.add(new ItemEntry(iskallia.vault.init.ModItems.CRYSTAL_SEAL_EMPTY, 1));
            ingredients.add(new ItemEntry(iskallia.vault.init.ModItems.ETERNAL_SOUL, 4));
            ingredients.add(new ItemEntry(iskallia.vault.init.ModItems.ALEXANDRITE_GEM, 32));
        });
        addFileForSeal(new ItemStack(ModItems.CRYSTAL_SEAL_TITAN), 50, 0, ingredients -> {
            ingredients.add(new ItemEntry(iskallia.vault.init.ModItems.CRYSTAL_SEAL_EMPTY, 1));
            ingredients.add(new ItemEntry(iskallia.vault.init.ModItems.PAINITE_GEM, 16));
            ingredients.add(new ItemEntry(iskallia.vault.init.ModItems.ALEXANDRITE_GEM, 32));
        });
        addFileForSeal(new ItemStack(ModItems.CRYSTAL_SEAL_UNHINGED_SCAVINGO), 50, 0, ingredients -> {
            ingredients.add(new ItemEntry(iskallia.vault.init.ModItems.CRYSTAL_SEAL_COLLECTOR, 1));
            ingredients.add(new ItemEntry(ModItems.CRYSTAL_SEAL_UNHINGED, 16));
            ingredients.add(new ItemEntry(iskallia.vault.init.ModItems.ETERNAL_SOUL, 4));
        });
        addFileForSeal(new ItemStack(ModItems.CRYSTAL_SEAL_ZEALOT), 40, 0, ingredients -> {
            ingredients.add(new ItemEntry(iskallia.vault.init.ModItems.CRYSTAL_SEAL_EMPTY, 1));
            ingredients.add(new ItemEntry(ModItems.GOD_OFFERING, 1));
        });
    }

    private void addFileForSeal(ItemStack sealStack, int unlockLevel, int greedTier, Consumer<BasicListBuilder<ItemEntry>> ingredientsConsumer) {
        add(sealStack.getItem().getRegistryName().getPath(), builder -> {
            builder.addSeal(sealStack, unlockLevel, greedTier, ingredientsConsumer);
        });
    }
}
