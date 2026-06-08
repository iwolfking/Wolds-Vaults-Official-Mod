package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import iskallia.vault.core.world.roll.IntRoll;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.init.ModBlocks;
import iskallia.vault.init.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import xyz.iwolfking.vhapi.api.datagen.gen.AbstractLootTableProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ModVaultLootTablesProvider extends AbstractLootTableProvider {
    protected ModVaultLootTablesProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    private static final List<VaultGearItem> gearItems = new ArrayList<VaultGearItem>(Arrays.asList(ModItems.CHESTPLATE, ModItems.BOOTS, ModItems.LEGGINGS, ModItems.HELMET, ModItems.SWORD, ModItems.AXE, ModItems.FOCUS, ModItems.WAND, ModItems.MAGNET, ModItems.SHIELD, xyz.iwolfking.woldsvaults.init.ModItems.BATTLESTAFF, xyz.iwolfking.woldsvaults.init.ModItems.LOOT_SACK, xyz.iwolfking.woldsvaults.init.ModItems.RANG, xyz.iwolfking.woldsvaults.init.ModItems.PLUSHIE, xyz.iwolfking.woldsvaults.init.ModItems.TRIDENT));
    private static final int[] standardLevels = new int[]{0, 20, 50, 100};
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
                    poolBuilder.item(1, ModItems.GREED_COIN.getRegistryName().toString(), 3, 5);
                });
            });
            addGenericGreedPools(builder);
        });
        add(WoldsVaults.id("greed_crate_bonus_zealot"), builder -> {
            builder.entry(entryBuilder -> {
                entryBuilder.pool(1, poolBuilder -> {
                    poolBuilder.item(1, ModItems.GREED_COIN.getRegistryName().toString(), 3, 5);
                });
            });
            addGenericGreedPools(builder);
        });
        add(WoldsVaults.id("greed_crate_bonus_unhinged_scavenger"), builder -> {
            builder.entry(entryBuilder -> {
                entryBuilder.pool(1, poolBuilder -> {
                    poolBuilder.item(1, ModItems.GREED_COIN.getRegistryName().toString(), 5, 7);
                });
            });
            addGenericGreedPools(builder);
        });
        add(WoldsVaults.id("greed_crate_bonus_survival"), builder -> {
            builder.entry(entryBuilder -> {
                entryBuilder.pool(1, poolBuilder -> {
                    poolBuilder.item(1, ModItems.GREED_COIN.getRegistryName().toString(), 3, 4);
                });
            });
            addGenericGreedPools(builder);
        });
        add(WoldsVaults.id("greed_crate_bonus_ballistic_bingo"), builder -> {
            builder.entry(entryBuilder -> {
                entryBuilder.pool(1, poolBuilder -> {
                    poolBuilder.item(1, ModItems.GREED_COIN.getRegistryName().toString(), 3, 4);
                });
            });
            addGenericGreedPools(builder);
        });
        add(WoldsVaults.id("greed_crate_bonus_bingo"), builder -> {
            builder.entry(entryBuilder -> {
                entryBuilder.pool(1, poolBuilder -> {
                    poolBuilder.item(1, ModItems.GREED_COIN.getRegistryName().toString(), 2, 3);
                });
            });
            addGenericGreedPools(builder);
        });
        add(WoldsVaults.id("greed_crate_bonus_elixir"), builder -> {
            builder.entry(entryBuilder -> {
                entryBuilder.pool(1, poolBuilder -> {
                    poolBuilder.item(1, ModItems.GREED_COIN.getRegistryName().toString(), 2, 2);
                });
            });
            addGenericGreedPools(builder);
        });
        add(WoldsVaults.id("greed_crate_bonus_enchanted_elixir"), builder -> {
            builder.entry(entryBuilder -> {
                entryBuilder.pool(1, poolBuilder -> {
                    poolBuilder.item(1, ModItems.GREED_COIN.getRegistryName().toString(), 2, 3);
                });
            });
            addGenericGreedPools(builder);
        });
        add(WoldsVaults.id("greed_crate_bonus_brutal_bosses"), builder -> {
            builder.entry(entryBuilder -> {
                entryBuilder.pool(1, poolBuilder -> {
                    poolBuilder.item(1, ModItems.GREED_COIN.getRegistryName().toString(), 2, 3);
                });
            });
            addGenericGreedPools(builder);
        });
        add(WoldsVaults.id("greed_crate_bonus_rune_boss"), builder -> {
            builder.entry(entryBuilder -> {
                entryBuilder.pool(1, poolBuilder -> {
                    poolBuilder.item(1, ModItems.GREED_COIN.getRegistryName().toString(), 3, 5);
                });
            });
            addGenericGreedPools(builder);
        });
        add(WoldsVaults.id("greed_crate_bonus_scavenger_bingo"), builder -> {
            builder.entry(entryBuilder -> {
                entryBuilder.pool(1, poolBuilder -> {
                    poolBuilder.item(1, ModItems.GREED_COIN.getRegistryName().toString(), 3, 4);
                });
            });
            addGenericGreedPools(builder);
        });
        add(WoldsVaults.id("greed_crate_bonus_alchemy"), builder -> {
            builder.entry(entryBuilder -> {
                entryBuilder.pool(1, poolBuilder -> {
                    poolBuilder.item(1, ModItems.GREED_COIN.getRegistryName().toString(), 3, 5);
                });
            });
            addGenericGreedPools(builder);
        });
        add(WoldsVaults.id("greed_crate_bonus_obelisk"), builder -> {
            builder.entry(entryBuilder -> {
                entryBuilder.pool(1, poolBuilder -> {
                    poolBuilder.item(1, ModItems.GREED_COIN.getRegistryName().toString(), 3, 4);
                });
            });
            addGenericGreedPools(builder);
        });
        add(WoldsVaults.id("greed_crate_bonus_chaos"), builder -> {
            builder.entry(entryBuilder -> {
                entryBuilder.pool(1, poolBuilder -> {
                    poolBuilder.item(1, ModItems.GREED_COIN.getRegistryName().toString(), 3, 5);
                });
            });
            addGenericGreedPools(builder);
        });
        generateCompletionCrates("chaos_crate", IntRoll.ofUniform(2, 4), IntRoll.ofUniform(2, 3), IntRoll.ofUniform(3, 4), IntRoll.ofUniform(3, 4), IntRoll.ofConstant(1), IntRoll.ofUniform(1, 4), IntRoll.ofUniform(1, 3), IntRoll.ofUniform(1, 6), IntRoll.ofUniform(1, 8), IntRoll.ofUniform(0, 3), IntRoll.ofUniform(2, 4), 50, entryBuilder -> {
            entryBuilder.pool(1, poolBuilder -> {
                poolBuilder.item(6, xyz.iwolfking.woldsvaults.init.ModItems.CONCEALED_CHAOS.getRegistryName().toString(), 1, 1);
                poolBuilder.item(1, xyz.iwolfking.woldsvaults.init.ModItems.HEART_OF_CHAOS.getRegistryName().toString(), 1, 1);
                poolBuilder.item(6, xyz.iwolfking.woldsvaults.init.ModItems.SOUL_ICHOR.getRegistryName().toString(), 2, 4);
                poolBuilder.item(8, xyz.iwolfking.woldsvaults.init.ModItems.CATALYST_BOX.getRegistryName().toString(), 2, 4);
                poolBuilder.item(8, xyz.iwolfking.woldsvaults.init.ModItems.AUGMENT_BOX.getRegistryName().toString(), 1, 1);
                poolBuilder.item(6, xyz.iwolfking.woldsvaults.init.ModItems.INSCRIPTION_BOX.getRegistryName().toString(), 1, 2);
                poolBuilder.item(4, xyz.iwolfking.woldsvaults.init.ModItems.GEM_BOX.getRegistryName().toString(), 8, 8);
                poolBuilder.item(4, ModItems.EMPOWERED_CHAOTIC_FOCUS.getRegistryName().toString(), 1, 2);
            }).rolls(1, 1);
        });
    }

    private void generateCompletionCrates(String name, IntRoll eternalSoulRange, IntRoll jewelPouchesRoll, IntRoll gearPiecesRolls, IntRoll vaultCurrencyRolls, IntRoll sealCount, IntRoll focusRolls, IntRoll fundamentalRolls, IntRoll fruitRolls, IntRoll packRolls, IntRoll rareRolls, IntRoll arcaneEssenceRolls, int additionalEntriesLevel, @Nullable Consumer<EntryBuilder> additionalEntriesBuilder) {
        Arrays.stream(standardLevels).forEach(value -> {
            add(WoldsVaults.id(name + "_" + value), builder -> {
                if(value < 20) {
                    completionCrate(builder, value, eternalSoulRange, jewelPouchesRoll, gearPiecesRolls, ModBlocks.VAULT_BRONZE, IntRoll.ofUniform(3, 4), IntRoll.ofConstant(1), IntRoll.ofUniform(1, 4), IntRoll.ofUniform(1, 3), IntRoll.ofUniform(1, 6), IntRoll.ofUniform(1, 8), IntRoll.ofUniform(0, 3), IntRoll.ofUniform(2, 4), null);
                }
                else if(value < additionalEntriesLevel) {
                    completionCrate(builder, value, eternalSoulRange, jewelPouchesRoll, gearPiecesRolls, ModBlocks.VAULT_GOLD, vaultCurrencyRolls, sealCount, focusRolls, fundamentalRolls, fruitRolls, packRolls, rareRolls, arcaneEssenceRolls, null);
                }
                else {
                    completionCrate(builder, value, eternalSoulRange, jewelPouchesRoll, gearPiecesRolls, ModBlocks.VAULT_GOLD, vaultCurrencyRolls, sealCount, focusRolls, fundamentalRolls, fruitRolls, packRolls, rareRolls, arcaneEssenceRolls, additionalEntriesBuilder);
                }
            });
        });
    }

    private void completionCrate(AbstractLootTableProvider.LootBuilder builder, int level, IntRoll eternalSoulRange, IntRoll jewelPouchesRoll, IntRoll gearPiecesRolls, Item vaultCurrencyItem, IntRoll vaultCurrencyRolls, IntRoll sealCount, IntRoll focusRolls, IntRoll fundamentalRolls, IntRoll fruitRolls, IntRoll packRolls, IntRoll rareRolls, IntRoll arcaneEssenceRolls, @Nullable Consumer<EntryBuilder> additionalEntriesBuilder) {
        builder.entry(entryBuilder -> {
            entryBuilder.pool(1, poolBuilder -> {
                poolBuilder.item(1, ModItems.ETERNAL_SOUL.getRegistryName().toString(), eternalSoulRange.getMin(), eternalSoulRange.getMax());
            });
        });
        builder.entry(entryBuilder -> {
            entryBuilder.pool(1, poolBuilder -> {
                poolBuilder.item(1, ModItems.JEWEL_POUCH.getRegistryName().toString(), 1, 2);
            }).rolls(jewelPouchesRoll.getMin(), jewelPouchesRoll.getMax());
        });
        builder.entry(entryBuilder -> {
            entryBuilder.pool(1, poolBuilder -> {
                gearItems.forEach(vaultGearItem -> {
                    gearPiece(poolBuilder, 10, vaultGearItem.getItem(), "gear_completion");
                });
            }).rolls(gearPiecesRolls.getMin(), gearPiecesRolls.getMax());
        });
        builder.entry(entryBuilder -> {
            entryBuilder.pool(1, poolBuilder -> {
                poolBuilder.item(1, vaultCurrencyItem.getRegistryName().toString(), 7, 10);
            }).rolls(vaultCurrencyRolls.getMin(), vaultCurrencyRolls.getMax());
        });

        if(level >= 20) {
            builder.entry(entryBuilder -> {
                entryBuilder.pool(1, poolBuilder -> {
                    poolBuilder.item(1, ModItems.CRYSTAL_SEAL_EMPTY.getRegistryName().toString(), sealCount.getMin(), sealCount.getMax());
                });
            });
            completionCrateFoci(builder, focusRolls, fundamentalRolls);
            completionCrateFruit(builder, fruitRolls);
        }
        if(level >= 50) {
            completionCrateBoosterPacks(builder, packRolls);
            completionCrateRares(builder, rareRolls);
        }
        if(level >= 100) {
            builder.entry(entryBuilder -> {
                entryBuilder.pool(1, poolBuilder -> {
                    poolBuilder.item(1, xyz.iwolfking.woldsvaults.init.ModItems.ARCANE_ESSENCE.getRegistryName().toString(), arcaneEssenceRolls.getMin(), arcaneEssenceRolls.getMax());
                });
            });
        }

        if(additionalEntriesBuilder != null) {
            builder.entry(additionalEntriesBuilder);
        }

    }

    private void completionCrateFoci(AbstractLootTableProvider.LootBuilder builder, IntRoll focusRolls, IntRoll fundamentalRolls) {
        builder.entry(entryBuilder -> {
            entryBuilder.pool(1, poolBuilder -> {
                poolBuilder.item(1, ModItems.WANING_FOCUS.getRegistryName().toString(), 1, 1);
                poolBuilder.item(1, ModItems.WAXING_FOCUS.getRegistryName().toString(), 1, 1);
                poolBuilder.item(1, ModItems.CHAOTIC_FOCUS.getRegistryName().toString(), 1, 1);
            }).rolls(focusRolls.getMin(), focusRolls.getMax());
        });
        builder.entry(entryBuilder -> {
            entryBuilder.pool(1, poolBuilder -> {
                poolBuilder.item(1, ModItems.FUNDAMENTAL_FOCUS.getRegistryName().toString(), 1, 1);
            }).rolls(fundamentalRolls.getMin(), fundamentalRolls.getMax());
        });
    }

    private void completionCrateFruit(AbstractLootTableProvider.LootBuilder builder, IntRoll fruitRolls) {
        builder.entry(entryBuilder -> {
            entryBuilder.pool(1, poolBuilder -> {
                poolBuilder.item(1, ModItems.BITTER_LEMON.getRegistryName().toString(), 1, 1);
                poolBuilder.item(6, ModItems.SWEET_KIWI.getRegistryName().toString(), 2, 4);
                poolBuilder.item(4, ModItems.GRAPES.getRegistryName().toString(), 1, 2);
            }).rolls(fruitRolls.getMin(), fruitRolls.getMax());
        });
    }

    private void completionCrateBoosterPacks(AbstractLootTableProvider.LootBuilder builder, IntRoll packRolls) {
        builder.entry(entryBuilder -> {
            entryBuilder.pool(1, poolBuilder -> {
                boosterPack(poolBuilder, 36, VaultMod.id("stat_pack"));
                boosterPack(poolBuilder, 4, VaultMod.id("arcane_pack"));
                boosterPack(poolBuilder, 1, VaultMod.id("mega_arcane_pack"));
                boosterPack(poolBuilder, 2, VaultMod.id("mega_stat_pack"));
                boosterPack(poolBuilder, 18, VaultMod.id("mix_pack"));
                boosterPack(poolBuilder, 2, VaultMod.id("mega_mix_pack"));
                boosterPack(poolBuilder, 10, VaultMod.id("resource_pack"));
                boosterPack(poolBuilder, 16, VaultMod.id("evolution_pack"));
                boosterPack(poolBuilder, 2, VaultMod.id("mega_evolution_pack"));
            }).rolls(packRolls.getMin(), packRolls.getMax());
        });
    }

    private void completionCrateRares(AbstractLootTableProvider.LootBuilder builder, IntRoll rareRolls) {
        builder.entry(entryBuilder -> {
            entryBuilder.pool(6, poolBuilder -> {
                poolBuilder.item(4, ModItems.SOUL_VORTEX.getRegistryName().toString(), 1, 1);
                poolBuilder.item(5, ModItems.TRINKET.getRegistryName().toString(), 1, 1);
                poolBuilder.item(5, xyz.iwolfking.woldsvaults.init.ModItems.CATALYST_BOX.getRegistryName().toString(), 2, 2);
            }).pool(8, poolBuilder -> {
                poolBuilder.item(18, ModItems.MANGO.getRegistryName().toString(), 1, 1);
                poolBuilder.item(12, ModItems.SOUR_ORANGE.getRegistryName().toString(), 1, 1);
                poolBuilder.item(5, xyz.iwolfking.woldsvaults.init.ModItems.WISDOM_FRUIT.getRegistryName().toString(), 1, 2);
                poolBuilder.item(12, xyz.iwolfking.woldsvaults.init.ModItems.HASTY_POMEGRANATE.getRegistryName().toString(), 1, 2);
            }).pool(8, poolBuilder -> {
                poolBuilder.item(6, ModItems.DUNGEON_CAPSTONE.getRegistryName().toString(), 1, 1);
                poolBuilder.item(1, ModItems.TREASURE_CAPSTONE.getRegistryName().toString(), 1, 1);
                poolBuilder.item(12, ModItems.EYE_OF_AVARICE.getRegistryName().toString(), 1, 1);
            }).pool(8, poolBuilder -> {
                poolBuilder.item(8, ModItems.POG.getRegistryName().toString(), 1, 1);
                inscription(poolBuilder, 8, VaultMod.id("random"));
            }).rolls(rareRolls.getMin(), rareRolls.getMax());
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
                poolBuilder.item(1, Blocks.AIR.getRegistryName().toString(), 1, 1);
                vaultMap(poolBuilder, 1, "gear_completion", -1);
            }).rolls(1, 1);
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

    private void gearPiece(AbstractLootTableProvider.PoolBuilder poolBuilder, int weight, Item gearPiece, String gearRollTypePool) {
        poolBuilder.itemNbt(weight, gearPiece.getRegistryName().toString(), 1, 1, nbt -> {
            nbt.put("the_vault:gear_roll_type_pool", gearRollTypePool);
        });
    }

    private void vaultMap(AbstractLootTableProvider.PoolBuilder poolBuilder, int weight, String gearRollTypePool, int tier) {
        poolBuilder.itemNbt(weight, xyz.iwolfking.woldsvaults.init.ModItems.MAP.getRegistryName().toString(), 1, 1, nbt -> {
            nbt.put("the_vault:gear_roll_type_pool", gearRollTypePool);
            nbt.put("the_vault:map_tier", tier);
        });
    }

    private void boosterPack(AbstractLootTableProvider.PoolBuilder poolBuilder, int weight, ResourceLocation packType) {
        poolBuilder.itemNbt(weight, ModItems.BOOSTER_PACK.getRegistryName().toString(), 1, 1, nbt -> {
            nbt.put("id", packType.toString());
        });
    }

    private void inscription(AbstractLootTableProvider.PoolBuilder poolBuilder, int weight, ResourceLocation pool) {
        poolBuilder.itemNbt(weight, ModItems.INSCRIPTION.getRegistryName().toString(), 1, 1, nbt -> {
            nbt.put("pool", pool.toString());
        });
    }
}
