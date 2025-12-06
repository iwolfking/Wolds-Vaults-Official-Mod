package xyz.iwolfking.woldsvaults.init;

import iskallia.vault.VaultMod;
import me.dinnerbeef.compressium.CompressibleBlock;
import me.dinnerbeef.compressium.CompressibleType;
import me.dinnerbeef.compressium.Compressium;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ModCompressibleBlocks {
    public static final Map<String, CompressibleBlock> ADDITIONAL_COMPRESSIBLE_BLOCKS = new HashMap<>();
    private static final Map<CompressibleBlock, List<Supplier<Block>>> REGISTERED_BLOCKS = new HashMap<>();

    public static void addBuiltInBlocks() {
        ADDITIONAL_COMPRESSIBLE_BLOCKS.put("vault_stone", new CompressibleBlock("vault_stone", VaultMod.id("vault_stone"), VaultMod.id("block/vault_stone"), VaultMod.id("block/vault_stone"), CompressibleType.BLOCK, 9, true));
        ADDITIONAL_COMPRESSIBLE_BLOCKS.put("vault_cobblestone", new CompressibleBlock("vault_cobblestone", VaultMod.id("vault_cobblestone"), VaultMod.id("block/vault_cobblestone"), VaultMod.id("block/vault_cobblestone"), CompressibleType.BLOCK, 9, true));
        ADDITIONAL_COMPRESSIBLE_BLOCKS.put("ornate_block", new CompressibleBlock("ornate_block", VaultMod.id("ornate_block"), VaultMod.id("block/ornate_block"), VaultMod.id("block/ornate_block"), CompressibleType.BLOCK, 9, true));
        ADDITIONAL_COMPRESSIBLE_BLOCKS.put("gilded_block", new CompressibleBlock("gilded_block", VaultMod.id("gilded_block"), VaultMod.id("block/gilded_block"), VaultMod.id("block/gilded_block"), CompressibleType.BLOCK, 9, true));
        ADDITIONAL_COMPRESSIBLE_BLOCKS.put("vault_diamond_block", new CompressibleBlock("vault_diamond_block", VaultMod.id("vault_diamond_block"), VaultMod.id("block/vault_diamond_block"), VaultMod.id("block/vault_diamond_block"), CompressibleType.BLOCK, 9, true));
        ADDITIONAL_COMPRESSIBLE_BLOCKS.put("magic_silk_block", new CompressibleBlock("magic_silk_block", VaultMod.id("magic_silk_block"), VaultMod.id("block/magic_silk_block"), VaultMod.id("block/magic_silk_block"), CompressibleType.BLOCK, 9, true));
        ADDITIONAL_COMPRESSIBLE_BLOCKS.put("ancient_copper_block", new CompressibleBlock("ancient_copper_block", VaultMod.id("ancient_copper_block"), VaultMod.id("block/ancient_copper_block"), VaultMod.id("block/ancient_copper_block"), CompressibleType.BLOCK, 9, true));
    }

    public static Map<CompressibleBlock, List<Supplier<Block>>> getRegisteredBlocks() {
        if(REGISTERED_BLOCKS.isEmpty()) {
            Compressium.REGISTERED_BLOCKS.forEach((compressibleBlock, suppliers) -> {
                ADDITIONAL_COMPRESSIBLE_BLOCKS.forEach((string, compressibleBlock1) -> {
                    if(compressibleBlock1.equals(compressibleBlock)) {
                        REGISTERED_BLOCKS.put(compressibleBlock, suppliers);
                    }
                });
            });
        }

        return REGISTERED_BLOCKS;
    }
}
