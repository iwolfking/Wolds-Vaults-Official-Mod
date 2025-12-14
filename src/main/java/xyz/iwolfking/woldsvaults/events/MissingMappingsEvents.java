package xyz.iwolfking.woldsvaults.events;

import iskallia.vault.VaultMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.init.ModCompressibleBlocks;
import xyz.iwolfking.woldsvaults.init.ModItems;

public class MissingMappingsEvents {
    @SubscribeEvent
    public static void onMissingMappings(RegistryEvent.MissingMappings<Block> event) {
        WoldsVaults.LOGGER.info("Running remapping event for blocks");
            event.getMappings("kubejs").forEach(mapping -> {
                remapUnobtaniumBlock(mapping);
                if(mapping.key.equals(new ResourceLocation("kubejs", "rainbow_unobtanium_block"))) {
                    mapping.remap(ModBlocks.RAINBOW_UNOBTANIUM);
                }
            });

            event.getMappings(VaultMod.MOD_ID).forEach(mapping -> {
                if (mapping.key.equals(VaultMod.id("chromatic_gold_block"))) {
                    mapping.remap(ModBlocks.CHROMATIC_GOLD_BLOCK);
                }
                else if(mapping.key.equals(VaultMod.id("vault_salvager"))) {
                    mapping.remap(ModBlocks.VAULT_SALVAGER_BLOCK);
                }
                else if(mapping.key.equals(VaultMod.id("omega_pog_1"))) {
                    mapping.remap(ModBlocks.OMEGA_POG_BLOCK);
                }
                else if(mapping.key.equals(VaultMod.id("echo_pog_1"))) {
                    mapping.remap(ModBlocks.ECHO_POG_BLOCK);
                }
                else if(mapping.key.equals(VaultMod.id("gem_pog_1"))) {
                    mapping.remap(ModBlocks.POG_BLOCK);
                }
                else if(mapping.key.equals(VaultMod.id("velvet_block_1"))) {
                    mapping.remap(ModCompressibleBlocks.getCompressed("velvet_block", 1));
                }
                else if(mapping.key.equals(VaultMod.id("ancient_copper_block_1"))) {
                    mapping.remap(ModCompressibleBlocks.getCompressed("ancient_copper_block", 1));
                }
                else if(mapping.key.equals(VaultMod.id("ornate_block_1"))) {
                    mapping.remap(ModCompressibleBlocks.getCompressed("ornate_block", 1));
                }
                else if(mapping.key.equals(VaultMod.id("gilded_block_1"))) {
                    mapping.remap(ModCompressibleBlocks.getCompressed("gilded_block", 1));
                }
                else if(mapping.key.equals(VaultMod.id("living_rock_block_cobble_1"))) {
                    mapping.remap(ModCompressibleBlocks.getCompressed("living_rock_block_cobble", 1));
                }
                else if(mapping.key.equals(VaultMod.id("sandy_block_1"))) {
                    mapping.remap(ModCompressibleBlocks.getCompressed("sandy_block", 1));
                }
                else if(mapping.key.equals(VaultMod.id("rotten_meat_block_1"))) {
                    mapping.remap(ModCompressibleBlocks.getCompressed("rotten_meat_block", 1));
                }
                else if(mapping.key.equals(VaultMod.id("magic_silk_block_1"))) {
                    mapping.remap(ModCompressibleBlocks.getCompressed("magic_silk_block", 1));
                }
                else if(mapping.key.equals(VaultMod.id("vault_plating_block_1"))) {
                    mapping.remap(ModCompressibleBlocks.getCompressed("vault_plating_block", 1));
                }
                else if(mapping.key.equals(VaultMod.id("vault_diamond_block_1"))) {
                    mapping.remap(ModCompressibleBlocks.getCompressed("vault_diamond_block", 1));
                }
                else if(mapping.key.equals(VaultMod.id("silver_scrap_2"))) {
                    mapping.remap(ModCompressibleBlocks.getCompressed("silver_scrap_block", 1));
                }
                else if(mapping.key.equals(VaultMod.id("vault_cobblestone_2"))) {
                    mapping.remap(ModCompressibleBlocks.getCompressed("vault_cobblestone", 2));
                }
                else if(mapping.key.equals(VaultMod.id("vault_essence_2"))) {
                    mapping.remap(ModCompressibleBlocks.getCompressed("vault_essence_block", 1));
                }
                else if(mapping.key.equals(VaultMod.id("vault_stone_2"))) {
                    mapping.remap(ModCompressibleBlocks.getCompressed("vault_stone", 2));
                }
                else if(mapping.key.equals(VaultMod.id("vault_plating_block"))) {
                    mapping.remap(ModBlocks.VAULT_PLATING_BLOCK);
                }
                else if(mapping.key.equals(VaultMod.id("carbon_block"))) {
                    mapping.remap(ModBlocks.CARBON_BLOCK);
                }
                remapOldCompressedBlock(mapping, VaultMod.id("vault_essence_1"), ModBlocks.VAULT_ESSENCE_BLOCK, ModCompressibleBlocks.getCompressed("vault_essence_block", 1));
                remapOldCompressedBlock(mapping, VaultMod.id("silver_scrap_1"), ModBlocks.SILVER_SCRAP_BLOCK, ModCompressibleBlocks.getCompressed("silver_scrap_block", 1));
                remapOldCompressedBlock(mapping, VaultMod.id("vault_ingot_1"), ModBlocks.VAULT_INGOT_BLOCK, ModCompressibleBlocks.getCompressed("vault_ingot_block", 1));
                remapOldCompressedBlock(mapping, VaultMod.id("vault_stone_1"), ModCompressibleBlocks.getCompressed("vault_stone", 1), ModCompressibleBlocks.getCompressed("vault_stone", 2));
                remapOldCompressedBlock(mapping, VaultMod.id("vault_cobblestone_1"), ModCompressibleBlocks.getCompressed("vault_cobblestone", 1), ModCompressibleBlocks.getCompressed("vault_cobblestone", 2));
            });
    }

    @SubscribeEvent
    public static void onMissingMappingsItem(RegistryEvent.MissingMappings<Item> event) {
        WoldsVaults.LOGGER.info("Running remapping event for items");
        event.getMappings("kubejs").forEach(mapping -> {
            if(mapping.key.equals(new ResourceLocation("kubejs", "rainbow_unobtanium_block"))) {
                mapping.remap(ModBlocks.RAINBOW_UNOBTANIUM.asItem());
            }
            else if(mapping.key.equals(new ResourceLocation("kubejs", "rainbow_unobtanium"))) {
                mapping.remap(ModItems.RAINBOW_UNOBTANIUM);
            }
            else {
                remapUnobtaniumItem(mapping);
                remapUnobtaniumBlockItem(mapping);
            }
        });
        event.getMappings("botania").forEach(mapping -> {
            if (mapping.key.equals(new ResourceLocation("botania", "uninfused_terrasteel_ingot"))) {
                mapping.remap(ModItems.UNINFUSED_TERRASTEEL_INGOT);
            }
        });

        event.getMappings("mysticalagriculture").forEach(mapping -> {
            if (mapping.key.equals(new ResourceLocation("mysticalagriculture", "pogging_seed_base"))) {
                mapping.remap(ModItems.POGGING_SEED_BASE);
            }
            else if (mapping.key.equals(new ResourceLocation("mysticalagriculture", "echoing_seed_base"))) {
                mapping.remap(ModItems.ECHOING_SEED_BASE);
            }
        });

        event.getMappings(VaultMod.MOD_ID).forEach(mapping -> {
                if (mapping.key.equals(VaultMod.id("chromatic_gold_ingot"))) {
                    mapping.remap(ModItems.CHROMATIC_GOLD_INGOT);
                }
                else if (mapping.key.equals(VaultMod.id("chromatic_gold_nugget"))) {
                    mapping.remap(ModItems.CHROMATIC_GOLD_NUGGET);
                }
//                else if (mapping.key.equals(VaultMod.id("crystal_seal_monolith"))) {
//                    mapping.remap(iskallia.vault.init.ModItems.CRYSTAL_SEAL_SCOUT);
//                }
                else if (mapping.key.equals(VaultMod.id("crystal_seal_unhinged"))) {
                    mapping.remap(ModItems.CRYSTAL_SEAL_UNHINGED);
                }
                else if (mapping.key.equals(VaultMod.id("crystal_seal_spirits"))) {
                    mapping.remap(ModItems.CRYSTAL_SEAL_SPIRITS);
                }
                else if (mapping.key.equals(VaultMod.id("crystal_seal_enchanter"))) {
                    mapping.remap(ModItems.CRYSTAL_SEAL_ENCHANTER);
                }
                else if (mapping.key.equals(VaultMod.id("crystal_seal_titan"))) {
                    mapping.remap(ModItems.CRYSTAL_SEAL_TITAN);
                }
                else if (mapping.key.equals(VaultMod.id("crystal_seal_doomsayer"))) {
                    mapping.remap(ModItems.CRYSTAL_SEAL_DOOMSAYER);
                }
                else if (mapping.key.equals(VaultMod.id("gem_box"))) {
                    mapping.remap(ModItems.GEM_BOX);
                }
                else if (mapping.key.equals(VaultMod.id("supply_box"))) {
                    mapping.remap(ModItems.SUPPLY_BOX);
                }
                else if (mapping.key.equals(VaultMod.id("augment_box"))) {
                    mapping.remap(ModItems.AUGMENT_BOX);
                }
                else if (mapping.key.equals(VaultMod.id("altar_recatalyzer"))) {
                    mapping.remap(ModItems.ALTAR_DECATALYZER);
                }
                else if (mapping.key.equals(VaultMod.id("wold_star_chunk"))) {
                    mapping.remap(ModItems.WOLD_STAR_CHUNK);
                }
                else if (mapping.key.equals(VaultMod.id("wold_star"))) {
                    mapping.remap(ModItems.WOLD_STAR);
                }
                else if (mapping.key.equals(VaultMod.id("pog_prism"))) {
                    mapping.remap(ModItems.POG_PRISM);
                }
                else if (mapping.key.equals(VaultMod.id("gem_reagent_ashium"))) {
                    mapping.remap(ModItems.GEM_REAGENT_ASHIUM);
                }
                else if (mapping.key.equals(VaultMod.id("gem_reagent_bomignite"))) {
                    mapping.remap(ModItems.GEM_REAGENT_BOMIGNITE);
                }
                else if (mapping.key.equals(VaultMod.id("gem_reagent_gorginite"))) {
                    mapping.remap(ModItems.GEM_REAGENT_GORGINITE);
                }
                else if (mapping.key.equals(VaultMod.id("gem_reagent_petzanite"))) {
                    mapping.remap(ModItems.GEM_REAGENT_PETEZANITE);
                }
                else if (mapping.key.equals(VaultMod.id("gem_reagent_iskallium"))) {
                    mapping.remap(ModItems.GEM_REAGENT_ISKALLIUM);
                }
                else if (mapping.key.equals(VaultMod.id("gem_reagent_tubium"))) {
                    mapping.remap(ModItems.GEM_REAGENT_TUBIUM);
                }
                else if (mapping.key.equals(VaultMod.id("gem_reagent_upaline"))) {
                    mapping.remap(ModItems.GEM_REAGENT_UPALINE);
                }
                else if (mapping.key.equals(VaultMod.id("gem_reagent_xenium"))) {
                    mapping.remap(ModItems.GEM_REAGENT_XENIUM);
                }
                else if (mapping.key.equals(VaultMod.id("gem_reagent_sparkletine"))) {
                    mapping.remap(ModItems.GEM_REAGENT_SPARKLETINE);
                }
                else if (mapping.key.equals(VaultMod.id("smashed_vault_gem"))) {
                    mapping.remap(ModItems.SMASHED_VAULT_GEM);
                }
                else if (mapping.key.equals(VaultMod.id("smashed_vault_gem_cluster"))) {
                    mapping.remap(ModItems.SMASHED_VAULT_GEM_CLUSTER);
                }
                else if (mapping.key.equals(VaultMod.id("chromatic_gold_block"))) {
                    mapping.remap(ModBlocks.CHROMATIC_GOLD_BLOCK.asItem());
                }
                else if(mapping.key.equals(VaultMod.id("vault_salvager"))) {
                    mapping.remap(ModBlocks.VAULT_SALVAGER_BLOCK.asItem());
                }
                else if(mapping.key.equals(VaultMod.id("omega_pog_1"))) {
                    mapping.remap(ModBlocks.OMEGA_POG_BLOCK.asItem());
                }
                else if(mapping.key.equals(VaultMod.id("echo_pog_1"))) {
                    mapping.remap(ModBlocks.ECHO_POG_BLOCK.asItem());
                }
                else if(mapping.key.equals(VaultMod.id("gem_pog_1"))) {
                    mapping.remap(ModBlocks.POG_BLOCK.asItem());
                }
                else if(mapping.key.equals(VaultMod.id("velvet_block_1"))) {
                    mapping.remap(ModCompressibleBlocks.getCompressed("velvet_block", 1).asItem());
                }
                else if(mapping.key.equals(VaultMod.id("ancient_copper_block_1"))) {
                    mapping.remap(ModCompressibleBlocks.getCompressed("ancient_copper_block", 1).asItem());
                }
                else if(mapping.key.equals(VaultMod.id("ornate_block_1"))) {
                    mapping.remap(ModCompressibleBlocks.getCompressed("ornate_block", 1).asItem());
                }
                else if(mapping.key.equals(VaultMod.id("gilded_block_1"))) {
                    mapping.remap(ModCompressibleBlocks.getCompressed("gilded_block", 1).asItem());
                }
                else if(mapping.key.equals(VaultMod.id("living_rock_block_cobble_1"))) {
                    mapping.remap(ModCompressibleBlocks.getCompressed("living_rock_block_cobble", 1).asItem());
                }
                else if(mapping.key.equals(VaultMod.id("sandy_block_1"))) {
                    mapping.remap(ModCompressibleBlocks.getCompressed("sandy_block", 1).asItem());
                }
                else if(mapping.key.equals(VaultMod.id("rotten_meat_block_1"))) {
                    mapping.remap(ModCompressibleBlocks.getCompressed("rotten_meat_block", 1).asItem());
                }
                else if(mapping.key.equals(VaultMod.id("magic_silk_block_1"))) {
                    mapping.remap(ModCompressibleBlocks.getCompressed("magic_silk_block", 1).asItem());
                }
                else if(mapping.key.equals(VaultMod.id("vault_plating_block_1"))) {
                    mapping.remap(ModCompressibleBlocks.getCompressed("vault_plating_block", 1).asItem());
                }
                else if(mapping.key.equals(VaultMod.id("vault_diamond_block_1"))) {
                    mapping.remap(ModCompressibleBlocks.getCompressed("vault_diamond_block", 1).asItem());
                }
                else if(mapping.key.equals(VaultMod.id("vault_plating_block"))) {
                    mapping.remap(ModBlocks.VAULT_PLATING_BLOCK.asItem());
                }
                else if(mapping.key.equals(VaultMod.id("carbon_block"))) {
                    mapping.remap(ModBlocks.CARBON_BLOCK.asItem());
                }
                remapOldCompressedItem(mapping, VaultMod.id("vault_essence_1"), ModBlocks.VAULT_ESSENCE_BLOCK.asItem(), ModCompressibleBlocks.getCompressed("vault_essence_block", 1).asItem());
                remapOldCompressedItem(mapping, VaultMod.id("silver_scrap_1"), ModBlocks.SILVER_SCRAP_BLOCK.asItem(), ModCompressibleBlocks.getCompressed("silver_scrap_block", 1).asItem());
                remapOldCompressedItem(mapping, VaultMod.id("vault_ingot_1"), ModBlocks.VAULT_INGOT_BLOCK.asItem(), ModCompressibleBlocks.getCompressed("vault_ingot_block", 1).asItem());
                remapOldCompressedItem(mapping, VaultMod.id("vault_stone_1"), ModCompressibleBlocks.getCompressed("vault_stone", 1).asItem(), ModCompressibleBlocks.getCompressed("vault_stone", 2).asItem());
                remapOldCompressedItem(mapping, VaultMod.id("vault_cobblestone_1"), ModCompressibleBlocks.getCompressed("vault_cobblestone", 1).asItem(), ModCompressibleBlocks.getCompressed("vault_cobblestone", 2).asItem());
            });
    }

    private static void remapOldCompressedBlock(RegistryEvent.MissingMappings.Mapping<Block> mapping, ResourceLocation old, Block blockOne, Block blockTwo) {
        if(mapping.key.equals(old)) {
            mapping.remap(blockOne);
        }
        if(mapping.key.equals(new ResourceLocation(old.getPath().replace("_1", "_2")))) {
            mapping.remap(blockTwo);
        }
    }

    private static void remapOldCompressedItem(RegistryEvent.MissingMappings.Mapping<Item> mapping, ResourceLocation old, Item blockOne, Item blockTwo) {
        if(mapping.key.equals(old)) {
            mapping.remap(blockOne);
        }
        if(mapping.key.equals(new ResourceLocation(old.getNamespace(), old.getPath().replace("_1", "_2")))) {
            mapping.remap(blockTwo);
        }
    }

    private static void remapUnobtaniumBlock(RegistryEvent.MissingMappings.Mapping<Block> mapping) {
        for(DyeColor color : DyeColor.values()) {
            if(mapping.key.equals(new ResourceLocation("kubejs", color.getSerializedName() + "_unobtanium_block"))) {
                mapping.remap(ModBlocks.COLORED_UNOBTANIUMS.get(color));
            }
        }
    }

    private static void remapUnobtaniumBlockItem(RegistryEvent.MissingMappings.Mapping<Item> mapping) {
        for(DyeColor color : DyeColor.values()) {
            if(mapping.key.equals(new ResourceLocation("kubejs", color.getSerializedName() + "_unobtanium_block"))) {
                mapping.remap(ModBlocks.COLORED_UNOBTANIUMS.get(color).asItem());
            }
        }
    }

    private static void remapUnobtaniumItem(RegistryEvent.MissingMappings.Mapping<Item> mapping) {
        for(DyeColor color : DyeColor.values()) {
            if(mapping.key.equals(new ResourceLocation("kubejs", color.getSerializedName() + "_unobtanium"))) {
                mapping.remap(ModItems.COLORED_UNOBTANIUMS.get(color));
            }
        }
    }
}
