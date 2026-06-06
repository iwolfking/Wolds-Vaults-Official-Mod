package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.init.ModBlocks;
import iskallia.vault.init.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.vhapi.api.datagen.AbstractTooltipProvider;
import xyz.iwolfking.vhapi.api.datagen.gen.AbstractLootTableProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModVaultLootTablesProvider extends AbstractLootTableProvider {
    protected ModVaultLootTablesProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    private static final List<VaultGearItem> gearItems = new ArrayList<VaultGearItem>(Arrays.asList(ModItems.CHESTPLATE, ModItems.BOOTS, ModItems.LEGGINGS, ModItems.HELMET, ModItems.SWORD, ModItems.AXE, ModItems.FOCUS, ModItems.WAND, ModItems.MAGNET, ModItems.SHIELD, xyz.iwolfking.woldsvaults.init.ModItems.BATTLESTAFF, xyz.iwolfking.woldsvaults.init.ModItems.LOOT_SACK, xyz.iwolfking.woldsvaults.init.ModItems.RANG, xyz.iwolfking.woldsvaults.init.ModItems.PLUSHIE, xyz.iwolfking.woldsvaults.init.ModItems.TRIDENT));

    @Override
    public void registerLootTables() {
        add(VaultMod.id("survival_gear_cache"), lootBuilder -> {
            lootBuilder.entry(entryBuilder -> {
                entryBuilder.rolls(1, 1)
                        .pool(1, poolBuilder -> {
                            gearItems.forEach(item -> poolBuilder.itemNbt(6, item.getItem().getRegistryName().toString(), 1, 1, nbt -> {
                                nbt.put("the_vault:gear_roll_type", "Rare+");
                            }));
                            gearItems.forEach(item -> poolBuilder.itemNbt(2, item.getItem().getRegistryName().toString(), 1, 1, nbt -> {
                                nbt.put("the_vault:gear_roll_type", "Rare+");
                                nbt.put("the_vault:is_legendary", "true");
                            }));
                            gearItems.forEach(item -> poolBuilder.itemNbt(1, item.getItem().getRegistryName().toString(), 1, 1, nbt -> {
                                nbt.put("the_vault:gear_roll_type_pool", "Chaotic");
                            }));
                            poolBuilder.itemNbt(6, ModItems.MAGNET.getRegistryName().toString(), 1, 1, nbt -> {
                                nbt.put("the_vault:gear_unique_pool", "woldsvaults:aural_magnet");
                                nbt.put("the_vault:gear_roll_type", "Unique");
                            });
                            poolBuilder.itemNbt(6, ModItems.SWORD.getRegistryName().toString(), 1, 1, nbt -> {
                                nbt.put("the_vault:gear_unique_pool", "woldsvaults:lava_chicken_sword");
                                nbt.put("the_vault:gear_roll_type", "Unique");
                            });
                            poolBuilder.itemNbt(6, xyz.iwolfking.woldsvaults.init.ModItems.TRIDENT.getRegistryName().toString(), 1, 1, nbt -> {
                                nbt.put("the_vault:gear_unique_pool", "woldsvaults:fork_of_the_glutton");
                                nbt.put("the_vault:gear_roll_type", "Unique");
                            });
                        });
            });
        });
        add(WoldsVaults.id("greed_crate_bonus_scavenger"), builder -> {
            builder.entry(entryBuilder -> {
                entryBuilder.pool(1, poolBuilder -> {
                    poolBuilder.item(1, ModItems.GREED_COIN.getRegistryName().toString(), 6, 10);
                });
            });
            addGenericGreedPools(builder);
        });
        add(WoldsVaults.id("greed_crate_bonus_zealot"), builder -> {
            builder.entry(entryBuilder -> {
                entryBuilder.pool(1, poolBuilder -> {
                    poolBuilder.item(1, ModItems.GREED_COIN.getRegistryName().toString(), 5, 7);
                });
            });
            addGenericGreedPools(builder);
        });
        add(WoldsVaults.id("greed_crate_bonus_unhinged_scavenger"), builder -> {
            builder.entry(entryBuilder -> {
                entryBuilder.pool(1, poolBuilder -> {
                    poolBuilder.item(1, ModItems.GREED_COIN.getRegistryName().toString(), 10, 15);
                });
            });
            addGenericGreedPools(builder);
        });
        add(WoldsVaults.id("greed_crate_bonus_survival"), builder -> {
            builder.entry(entryBuilder -> {
                entryBuilder.pool(1, poolBuilder -> {
                    poolBuilder.item(1, ModItems.GREED_COIN.getRegistryName().toString(), 5, 5);
                });
            });
            addGenericGreedPools(builder);
        });
        add(WoldsVaults.id("greed_crate_bonus_ballistic_bingo"), builder -> {
            builder.entry(entryBuilder -> {
                entryBuilder.pool(1, poolBuilder -> {
                    poolBuilder.item(1, ModItems.GREED_COIN.getRegistryName().toString(), 6, 9);
                });
            });
            addGenericGreedPools(builder);
        });
        add(WoldsVaults.id("greed_crate_bonus_bingo"), builder -> {
            builder.entry(entryBuilder -> {
                entryBuilder.pool(1, poolBuilder -> {
                    poolBuilder.item(1, ModItems.GREED_COIN.getRegistryName().toString(), 4, 6);
                });
            });
            addGenericGreedPools(builder);
        });
        add(WoldsVaults.id("greed_crate_bonus_elixir"), builder -> {
            builder.entry(entryBuilder -> {
                entryBuilder.pool(1, poolBuilder -> {
                    poolBuilder.item(1, ModItems.GREED_COIN.getRegistryName().toString(), 4, 6);
                });
            });
            addGenericGreedPools(builder);
        });
        add(WoldsVaults.id("greed_crate_bonus_enchanted_elixir"), builder -> {
            builder.entry(entryBuilder -> {
                entryBuilder.pool(1, poolBuilder -> {
                    poolBuilder.item(1, ModItems.GREED_COIN.getRegistryName().toString(), 5, 7);
                });
            });
            addGenericGreedPools(builder);
        });
        add(WoldsVaults.id("greed_crate_bonus_brutal_bosses"), builder -> {
            builder.entry(entryBuilder -> {
                entryBuilder.pool(1, poolBuilder -> {
                    poolBuilder.item(1, ModItems.GREED_COIN.getRegistryName().toString(), 7, 12);
                });
            });
            addGenericGreedPools(builder);
        });
        add(WoldsVaults.id("greed_crate_bonus_rune_boss"), builder -> {
            builder.entry(entryBuilder -> {
                entryBuilder.pool(1, poolBuilder -> {
                    poolBuilder.item(1, ModItems.GREED_COIN.getRegistryName().toString(), 7, 10);
                });
            });
            addGenericGreedPools(builder);
        });
        add(WoldsVaults.id("greed_crate_bonus_scavenger_bingo"), builder -> {
            builder.entry(entryBuilder -> {
                entryBuilder.pool(1, poolBuilder -> {
                    poolBuilder.item(1, ModItems.GREED_COIN.getRegistryName().toString(), 6, 9);
                });
            });
            addGenericGreedPools(builder);
        });
        add(WoldsVaults.id("greed_crate_bonus_alchemy"), builder -> {
            builder.entry(entryBuilder -> {
                entryBuilder.pool(1, poolBuilder -> {
                    poolBuilder.item(1, ModItems.GREED_COIN.getRegistryName().toString(), 7, 10);
                });
            });
            addGenericGreedPools(builder);
        });
        add(WoldsVaults.id("greed_crate_bonus_obelisk"), builder -> {
            builder.entry(entryBuilder -> {
                entryBuilder.pool(1, poolBuilder -> {
                    poolBuilder.item(1, ModItems.GREED_COIN.getRegistryName().toString(), 5, 5);
                });
            });
            addGenericGreedPools(builder);
        });
    }

    private void addGenericGreedPools(AbstractLootTableProvider.LootBuilder builder) {
        builder.entry(entryBuilder -> {
            entryBuilder.pool(1, poolBuilder -> {
                poolBuilder.item(1, ModItems.CRYONIC_FOCUS.getRegistryName().toString(), 1, 1);
                poolBuilder.item(1, ModItems.PYRETIC_FOCUS.getRegistryName().toString(), 1, 1);
                poolBuilder.item(1, ModItems.OPPORTUNISTIC_FOCUS.getRegistryName().toString(), 1, 1);
                poolBuilder.item(1, ModItems.EMPOWERED_CHAOTIC_FOCUS.getRegistryName().toString(), 1, 1);
                poolBuilder.item(2, ModItems.VORPAL_FOCUS.getRegistryName().toString(), 2, 2);
                poolBuilder.item(2, ModItems.WANING_FOCUS.getRegistryName().toString(), 2, 2);
                poolBuilder.item(2, ModItems.WAXING_FOCUS.getRegistryName().toString(), 2, 2);
                poolBuilder.item(1, xyz.iwolfking.woldsvaults.init.ModItems.ECCENTRIC_FOCUS.getRegistryName().toString(), 2, 2);
            }).rolls(3, 3);
        });
        builder.entry(entryBuilder -> {
            entryBuilder.pool(1, poolBuilder -> {
                poolBuilder.item(3, ModBlocks.VAULT_GOLD.getRegistryName().toString(), 4, 9);
                poolBuilder.item(1, ModBlocks.VAULT_PLATINUM.getRegistryName().toString(), 3, 5);
            }).rolls(3, 3);
        });
        builder.entry(entryBuilder -> {
            entryBuilder.pool(1, poolBuilder -> {
                poolBuilder.item(16, ModItems.SOUL_VORTEX.getRegistryName().toString(), 1, 1);
                poolBuilder.item(16, ModItems.SPICY_HEARTY_BURGER.getRegistryName().toString(), 1, 4);
                poolBuilder.item(2, ModItems.COMPANION_EGG.getRegistryName().toString(), 1, 1);
                poolBuilder.item(16, ModItems.POG.getRegistryName().toString(), 3, 3);
                poolBuilder.item(16, ModItems.SOUR_ORANGE.getRegistryName().toString(), 3, 3);
                poolBuilder.item(1, ModItems.MYSTIC_PEAR.getRegistryName().toString(), 1, 1);
                poolBuilder.item(12, ModItems.UNIDENTIFIED_TREASURE_KEY.getRegistryName().toString(), 2, 2);
                poolBuilder.item(16, ModItems.BITTER_LEMON.getRegistryName().toString(), 6, 6);
                poolBuilder.item(10, ModItems.CARD_JUICE.getRegistryName().toString(), 32, 32);
                poolBuilder.item(12, ModItems.RECHARGE_CORE.getRegistryName().toString(), 1, 1);
                poolBuilder.item(10, ModItems.JEWEL_POUCH.getRegistryName().toString(), 16, 16);
                poolBuilder.item(8, xyz.iwolfking.woldsvaults.init.ModItems.OMEGA_BOX.getRegistryName().toString(), 1, 4);
                poolBuilder.item(8, xyz.iwolfking.woldsvaults.init.ModItems.INSCRIPTION_BOX.getRegistryName().toString(), 1, 4);
                poolBuilder.item(8, xyz.iwolfking.woldsvaults.init.ModItems.CATALYST_BOX.getRegistryName().toString(), 2, 4);
                poolBuilder.item(12, xyz.iwolfking.woldsvaults.init.ModItems.ALTAR_DECATALYZER.getRegistryName().toString(), 8, 8);
                poolBuilder.item(14, xyz.iwolfking.woldsvaults.init.ModItems.SOUL_ICHOR.getRegistryName().toString(), 8, 8);
                poolBuilder.item(2, xyz.iwolfking.woldsvaults.init.ModItems.UBER_CHAOS_CATALYST.getRegistryName().toString(), 1, 1);
                poolBuilder.item(1, xyz.iwolfking.woldsvaults.init.ModItems.ALL_SEEING_EYE_CAPSTONE.getRegistryName().toString(), 1, 1);
                poolBuilder.item(10, xyz.iwolfking.woldsvaults.init.ModItems.AUGMENT_BOX.getRegistryName().toString(), 1, 4);
                poolBuilder.item(12, xyz.iwolfking.woldsvaults.init.ModItems.COMPANION_REROLLER.getRegistryName().toString(), 1, 1);
                poolBuilder.item(12, xyz.iwolfking.woldsvaults.init.ModItems.GEM_BOX.getRegistryName().toString(), 32, 32);
                poolBuilder.item(8, xyz.iwolfking.woldsvaults.init.ModItems.REPAIR_AUGMENTER.getRegistryName().toString(), 1, 1);
                poolBuilder.item(4, xyz.iwolfking.woldsvaults.init.ModItems.CONCEALED_CHAOS.getRegistryName().toString(), 1, 1);
                poolBuilder.item(4, xyz.iwolfking.woldsvaults.init.ModItems.VAULT_DECO_SCROLL.getRegistryName().toString(), 1, 1);
                poolBuilder.item(10, xyz.iwolfking.woldsvaults.init.ModItems.HASTY_POMEGRANATE.getRegistryName().toString(), 3, 3);
            }).rolls(1, 1);
        });
    }
}
