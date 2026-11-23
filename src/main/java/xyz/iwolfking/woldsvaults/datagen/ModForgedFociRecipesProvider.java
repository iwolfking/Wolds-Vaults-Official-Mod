package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.config.entry.ItemEntry;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.vhapi.api.datagen.lib.BasicListBuilder;
import xyz.iwolfking.vhapi.api.datagen.recipes.AbstractFocusRecipesProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModItems;

import java.util.function.Consumer;

public class ModForgedFociRecipesProvider extends AbstractFocusRecipesProvider {
    protected ModForgedFociRecipesProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        addFileForFocus(new ItemStack(ModItems.ECCENTRIC_FOCUS), ingredients -> {
            ingredients.add(new ItemEntry(iskallia.vault.init.ModItems.VAULT_SCRAP, 500));
            ingredients.add(new ItemEntry(iskallia.vault.init.ModItems.WUTODIE_GEM, 16));
        });
    }

    private void addFileForFocus(ItemStack sealStack, Consumer<BasicListBuilder<ItemEntry>> ingredientsConsumer) {
        add(sealStack.getItem().getRegistryName().getPath(), builder -> {
            builder.addFocus(sealStack, ingredientsConsumer);
        });
    }
}
