package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.block.VaultOreBlock;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.iwolfking.vhapi.api.datagen.AbstractTooltipProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractEnigmaEggProvider;
import xyz.iwolfking.woldsvaults.init.ModItems;

import java.util.List;

public class ModEnigmaEggProvider extends AbstractEnigmaEggProvider {
    protected ModEnigmaEggProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        add("overwrite/add_all_spawn_eggs", builder -> {
            builder.addItems(productEntryListBuilder -> {
               getAllSpawnEggs().forEach(spawnEggItem -> {
                   productEntryListBuilder.add(spawnEggItem, 1);
               });
            });
        });
    }

    private final static List<String> NAMESPACES_TO_FILTER = List.of("minecraft");

    public static List<SpawnEggItem> getAllSpawnEggs() {
        return ForgeRegistries.ITEMS.getValues().stream()
                .filter(b -> b instanceof SpawnEggItem)
                .filter(b -> b.getRegistryName() != null && NAMESPACES_TO_FILTER.contains(b.getRegistryName().getNamespace()))
                .map(b -> (SpawnEggItem) b)
                .toList();
    }
}
