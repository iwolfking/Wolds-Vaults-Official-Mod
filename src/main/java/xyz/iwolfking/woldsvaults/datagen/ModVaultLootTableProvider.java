package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.init.ModItems;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.gen.AbstractLootTableProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class ModVaultLootTableProvider extends AbstractLootTableProvider {
    public ModVaultLootTableProvider(DataGenerator gen) {
        super(gen, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerLootTables() {
        //TODO: add more loot!
        add(WoldsVaults.id("greed_crate_bonus_scavenger"), builder -> {
            builder.entry(entryBuilder -> {
                entryBuilder.pool(1, poolBuilder -> {
                    poolBuilder.item(1, ModItems.GREED_COIN.getRegistryName().toString(), 6, 10);
                });
            });
        });
        add(WoldsVaults.id("greed_crate_bonus_zealot"), builder -> {
            builder.entry(entryBuilder -> {
                entryBuilder.pool(1, poolBuilder -> {
                    poolBuilder.item(1, ModItems.GREED_COIN.getRegistryName().toString(), 5, 7);
                });
            });
        });
        add(WoldsVaults.id("greed_crate_bonus_unhinged_scavenger"), builder -> {
            builder.entry(entryBuilder -> {
                entryBuilder.pool(1, poolBuilder -> {
                    poolBuilder.item(1, ModItems.GREED_COIN.getRegistryName().toString(), 10, 15);
                });
            });
        });
        add(WoldsVaults.id("greed_crate_bonus_survival"), builder -> {
            builder.entry(entryBuilder -> {
                entryBuilder.pool(1, poolBuilder -> {
                    poolBuilder.item(1, ModItems.GREED_COIN.getRegistryName().toString(), 5, 5);
                });
            });
        });
        add(WoldsVaults.id("greed_crate_bonus_ballistic_bingo"), builder -> {
            builder.entry(entryBuilder -> {
                entryBuilder.pool(1, poolBuilder -> {
                    poolBuilder.item(1, ModItems.GREED_COIN.getRegistryName().toString(), 6, 9);
                });
            });
        });
        add(WoldsVaults.id("greed_crate_bonus_bingo"), builder -> {
            builder.entry(entryBuilder -> {
                entryBuilder.pool(1, poolBuilder -> {
                    poolBuilder.item(1, ModItems.GREED_COIN.getRegistryName().toString(), 4, 6);
                });
            });
        });
        add(WoldsVaults.id("greed_crate_bonus_elixir"), builder -> {
            builder.entry(entryBuilder -> {
                entryBuilder.pool(1, poolBuilder -> {
                    poolBuilder.item(1, ModItems.GREED_COIN.getRegistryName().toString(), 4, 6);
                });
            });
        });
        add(WoldsVaults.id("greed_crate_bonus_enchanted_elixir"), builder -> {
            builder.entry(entryBuilder -> {
                entryBuilder.pool(1, poolBuilder -> {
                    poolBuilder.item(1, ModItems.GREED_COIN.getRegistryName().toString(), 5, 7);
                });
            });
        });
        add(WoldsVaults.id("greed_crate_bonus_brutal_bosses"), builder -> {
            builder.entry(entryBuilder -> {
                entryBuilder.pool(1, poolBuilder -> {
                    poolBuilder.item(1, ModItems.GREED_COIN.getRegistryName().toString(), 7, 12);
                });
            });
        });
        add(WoldsVaults.id("greed_crate_bonus_rune_boss"), builder -> {
            builder.entry(entryBuilder -> {
                entryBuilder.pool(1, poolBuilder -> {
                    poolBuilder.item(1, ModItems.GREED_COIN.getRegistryName().toString(), 7, 10);
                });
            });
        });
        add(WoldsVaults.id("greed_crate_bonus_scavenger_bingo"), builder -> {
            builder.entry(entryBuilder -> {
                entryBuilder.pool(1, poolBuilder -> {
                    poolBuilder.item(1, ModItems.GREED_COIN.getRegistryName().toString(), 6, 9);
                });
            });
        });
        add(WoldsVaults.id("greed_crate_bonus_vault_royale"), builder -> {
            builder.entry(entryBuilder -> {
                entryBuilder.pool(1, poolBuilder -> {
                    poolBuilder.item(1, ModItems.GREED_COIN.getRegistryName().toString(), 10, 10);
                });
            });
        });
        add(WoldsVaults.id("greed_crate_bonus_alchemy"), builder -> {
            builder.entry(entryBuilder -> {
                entryBuilder.pool(1, poolBuilder -> {
                    poolBuilder.item(1, ModItems.GREED_COIN.getRegistryName().toString(), 7, 10);
                });
            });
        });
        add(WoldsVaults.id("greed_crate_bonus_obelisk"), builder -> {
            builder.entry(entryBuilder -> {
                entryBuilder.pool(1, poolBuilder -> {
                    poolBuilder.item(1, ModItems.GREED_COIN.getRegistryName().toString(), 5, 5);
                });
            });
        });
    }
}
