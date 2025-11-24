package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractTooltipProvider;
import xyz.iwolfking.vhapi.api.datagen.gen.AbstractLootTableProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModItems;

public class ModVaultLootTablesProvider extends AbstractLootTableProvider {
    protected ModVaultLootTablesProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }


    @Override
    public void registerLootTables() {
        add(VaultMod.id("iskallian_leaves_merge"), lootBuilder -> {
            lootBuilder.entry(entryBuilder -> {
                entryBuilder.rolls(1, 3)
                        .pool(1, poolBuilder -> {
                            poolBuilder.item(1, "minecraft:dirt", 1, 32);
                            poolBuilder.item(1, "minecraft:sand", 1, 32);
                            poolBuilder.item(1, "minecraft:stone", 1, 32);
                        });
            });
        });
    }
}
