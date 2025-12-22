package xyz.iwolfking.woldsvaults.init;

import iskallia.vault.VaultMod;
import me.dinnerbeef.compressium.CompressibleBlock;
import me.dinnerbeef.compressium.CompressibleType;
import me.dinnerbeef.compressium.Compressium;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.*;
import java.util.function.Supplier;

public class ModCompressibleBlocks {
    public static final Map<String, CompressibleBlock> ADDITIONAL_COMPRESSIBLE_BLOCKS = new LinkedHashMap<>();
    private static final Map<CompressibleBlock, List<Supplier<Block>>> REGISTERED_BLOCKS = new LinkedHashMap<>();

    public static void addBuiltInBlocks() {
        ADDITIONAL_COMPRESSIBLE_BLOCKS.put("vault_stone", new CompressibleBlock("vault_stone", VaultMod.id("vault_stone"), VaultMod.id("block/vault_stone"), VaultMod.id("block/vault_stone"), CompressibleType.BLOCK, 9, true));
        ADDITIONAL_COMPRESSIBLE_BLOCKS.put("vault_cobblestone", new CompressibleBlock("vault_cobblestone", VaultMod.id("vault_cobblestone"), VaultMod.id("block/vault_cobblestone"), VaultMod.id("block/vault_cobblestone"), CompressibleType.BLOCK, 9, true));
        ADDITIONAL_COMPRESSIBLE_BLOCKS.put("ornate_block", new CompressibleBlock("ornate_block", VaultMod.id("ornate_block"), VaultMod.id("block/ornate_block"), VaultMod.id("block/ornate_block"), CompressibleType.BLOCK, 9, true));
        ADDITIONAL_COMPRESSIBLE_BLOCKS.put("gilded_block", new CompressibleBlock("gilded_block", VaultMod.id("gilded_block"), VaultMod.id("block/gilded_block"), VaultMod.id("block/gilded_block"), CompressibleType.BLOCK, 9, true));
        ADDITIONAL_COMPRESSIBLE_BLOCKS.put("vault_diamond_block", new CompressibleBlock("vault_diamond_block", VaultMod.id("vault_diamond_block"), VaultMod.id("block/vault_diamond_block"), VaultMod.id("block/vault_diamond_block"), CompressibleType.BLOCK, 9, true));
        ADDITIONAL_COMPRESSIBLE_BLOCKS.put("magic_silk_block", new CompressibleBlock("magic_silk_block", VaultMod.id("magic_silk_block"), VaultMod.id("block/magic_silk_block"), VaultMod.id("block/magic_silk_block"), CompressibleType.BLOCK, 9, true));
        ADDITIONAL_COMPRESSIBLE_BLOCKS.put("ancient_copper_block", new CompressibleBlock("ancient_copper_block", VaultMod.id("ancient_copper_block"), VaultMod.id("block/ancient_copper_block"), VaultMod.id("block/ancient_copper_block"), CompressibleType.BLOCK, 9, true));
        ADDITIONAL_COMPRESSIBLE_BLOCKS.put("vault_plating_block", new CompressibleBlock("vault_plating_block", WoldsVaults.id("vault_plating_block"), WoldsVaults.id("block/vault_plating_block"), WoldsVaults.id("block/vault_plating_block"), CompressibleType.BLOCK, 9, true));
        ADDITIONAL_COMPRESSIBLE_BLOCKS.put("carbon_block", new CompressibleBlock("carbon_block", WoldsVaults.id("carbon_block"), WoldsVaults.id("block/carbon_block"), WoldsVaults.id("block/carbon_block"), CompressibleType.BLOCK, 9, true));
        ADDITIONAL_COMPRESSIBLE_BLOCKS.put("vault_essence_block", new CompressibleBlock("vault_essence_block", WoldsVaults.id("vault_essence_block"), WoldsVaults.id("block/vault_essence_block"), WoldsVaults.id("block/vault_essence_block"), CompressibleType.BLOCK, 9, true));
        ADDITIONAL_COMPRESSIBLE_BLOCKS.put("chromatic_iron_block", new CompressibleBlock("chromatic_iron_block", VaultMod.id("chromatic_iron_block"), VaultMod.id("block/chromatic_iron_block"), VaultMod.id("block/chromatic_iron_block"), CompressibleType.BLOCK, 9, true));
        ADDITIONAL_COMPRESSIBLE_BLOCKS.put("chromatic_steel_block", new CompressibleBlock("chromatic_steel_block", VaultMod.id("chromatic_steel_block"), VaultMod.id("block/chromatic_steel_block"), VaultMod.id("block/chromatic_steel_block"), CompressibleType.BLOCK, 9, true));
        ADDITIONAL_COMPRESSIBLE_BLOCKS.put("chromatic_gold_block", new CompressibleBlock("chromatic_gold_block", WoldsVaults.id("chromatic_gold_block"), WoldsVaults.id("block/chromatic_gold_block"), WoldsVaults.id("block/chromatic_gold_block"), CompressibleType.BLOCK, 9, true));
        ADDITIONAL_COMPRESSIBLE_BLOCKS.put("silver_scrap_block", new CompressibleBlock("silver_scrap_block", WoldsVaults.id("silver_scrap_block"), WoldsVaults.id("block/silver_scrap_block"), WoldsVaults.id("block/silver_scrap_block"), CompressibleType.BLOCK, 9, true));
        ADDITIONAL_COMPRESSIBLE_BLOCKS.put("living_rock_block_cobble", new CompressibleBlock("living_rock_block_cobble", VaultMod.id("living_rock_block_cobble"), VaultMod.id("block/living_rock_block_cobble"), VaultMod.id("block/living_rock_block_cobble"), CompressibleType.BLOCK, 9, true));
        ADDITIONAL_COMPRESSIBLE_BLOCKS.put("sandy_block", new CompressibleBlock("sandy_block", VaultMod.id("sandy_block"), VaultMod.id("block/sandy_block"), VaultMod.id("block/sandy_block"), CompressibleType.BLOCK, 9, true));
        ADDITIONAL_COMPRESSIBLE_BLOCKS.put("rotten_meat_block", new CompressibleBlock("rotten_meat_block", VaultMod.id("rotten_meat_block"), VaultMod.id("block/rotten_meat_block"), VaultMod.id("block/rotten_meat_block"), CompressibleType.BLOCK, 9, true));
        ADDITIONAL_COMPRESSIBLE_BLOCKS.put("velvet_block", new CompressibleBlock("velvet_block", VaultMod.id("velvet_block"), VaultMod.id("block/velvet"), VaultMod.id("block/velvet_block"), CompressibleType.BLOCK, 9, true));
    }

    public static Map<CompressibleBlock, List<Supplier<Block>>> getRegisteredBlocks() {
        if(REGISTERED_BLOCKS.isEmpty()) {
            Compressium.REGISTERED_BLOCKS.entrySet().stream().sorted(
                Comparator.comparing((Map.Entry<CompressibleBlock, List<Supplier<Block>>> n) -> n.getKey().name())
            ).forEach((compressibleBlock) -> {
                ADDITIONAL_COMPRESSIBLE_BLOCKS.forEach((string, compressibleBlock1) -> {
                    if(compressibleBlock1.equals(compressibleBlock.getKey())) {
                        REGISTERED_BLOCKS.put(compressibleBlock.getKey(), compressibleBlock.getValue());
                    }
                });
            });
        }

        return REGISTERED_BLOCKS;
    }

    public static Block getCompressed(String blockName, int compressionLevel) {
        if(compressionLevel <= 0) {
            return Blocks.STONE;
        }

        if(ADDITIONAL_COMPRESSIBLE_BLOCKS.get(blockName) == null) {
            return Blocks.STONE;
        }

        CompressibleBlock block = ADDITIONAL_COMPRESSIBLE_BLOCKS.get(blockName);
        int maxDepth = block.getNestedDepth();
        return getRegisteredBlocks().get(block).get(Math.min(compressionLevel - 1, maxDepth)).get();
    }
}
