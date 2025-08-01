package xyz.iwolfking.woldsvaults.integration.scannable.scanning;

import iskallia.vault.init.ModBlocks;
import li.cil.scannable.api.scanning.BlockScannerModule;
import li.cil.scannable.api.scanning.ScanResultProvider;
import li.cil.scannable.client.scanning.ScanResultProviders;
import li.cil.scannable.client.scanning.filter.BlockCacheScanFilter;
import li.cil.scannable.client.scanning.filter.BlockScanFilter;
import li.cil.scannable.client.scanning.filter.BlockTagScanFilter;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public enum VaultObjectivesBlockScannerModule implements BlockScannerModule {
    INSTANCE;

    private Predicate<BlockState> filter;

    public static Set<ResourceLocation> vaultBlocks = Util.make(new HashSet<>(), c -> {
        c.add(ModBlocks.MONOLITH.getRegistryName());
        c.add(ModBlocks.GOD_ALTAR.getRegistryName());
        c.add(ModBlocks.LODESTONE.getRegistryName());
        c.add(ModBlocks.OBELISK.getRegistryName());
        c.add(ModBlocks.ALCHEMY_ARCHIVE.getRegistryName());
        c.add(ModBlocks.MODIFIER_DISCOVERY.getRegistryName());
        c.add(ModBlocks.ENHANCEMENT_ALTAR.getRegistryName());
        c.add(ModBlocks.SHOP_PEDESTAL.getRegistryName());
        c.add(xyz.iwolfking.woldsvaults.init.ModBlocks.BLACKSMITH_VENDOR_PEDESTAL.getRegistryName());
        c.add(xyz.iwolfking.woldsvaults.init.ModBlocks.ETCHING_PEDESTAL.getRegistryName());
        c.add(xyz.iwolfking.woldsvaults.init.ModBlocks.CARD_VENDOR_PEDESTAL.getRegistryName());
        c.add(xyz.iwolfking.woldsvaults.init.ModBlocks.GOD_VENDOR_PEDESTAL.getRegistryName());
        c.add(xyz.iwolfking.woldsvaults.init.ModBlocks.RARE_VENDOR_PEDESTAL.getRegistryName());
        c.add(xyz.iwolfking.woldsvaults.init.ModBlocks.OMEGA_VENDOR_PEDESTAL.getRegistryName());
        c.add(ModBlocks.CRAKE_PEDESTAL.getRegistryName());
        c.add(ModBlocks.SCAVENGER_ALTAR.getRegistryName());
        c.add(ModBlocks.VAULT_PORTAL.getRegistryName());
        c.add(ModBlocks.GRID_GATEWAY.getRegistryName());
        c.add(xyz.iwolfking.woldsvaults.init.ModBlocks.BREWING_ALTAR.getRegistryName());
        c.add(ModBlocks.RUNE_PILLAR.getRegistryName());
    });

    @Override
    public int getEnergyCost(final ItemStack module) {
        return 2500;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public ScanResultProvider getResultProvider() {
        return ScanResultProviders.BLOCKS.get();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public float adjustLocalRange(final float range) {
        return range * 0.7F;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Predicate<BlockState> getFilter(final ItemStack module) {
        validateFilter();
        return filter;
    }

    @OnlyIn(Dist.CLIENT)
    private void validateFilter() {
        if (filter != null) {
            return;
        }

        final List<Predicate<BlockState>> filters = new ArrayList<>();
        for (final ResourceLocation location : vaultBlocks) {
            final Block block = ForgeRegistries.BLOCKS.getValue(location);
            if (block != null) {
                filters.add(new BlockScanFilter(block));
            }
        }
        final ITagManager<Block> tags = ForgeRegistries.BLOCKS.tags();
        if (tags != null) {
            for (final ResourceLocation location : vaultBlocks) {
                final TagKey<Block> tag = TagKey.create(Registry.BLOCK_REGISTRY, location);
                if (tags.isKnownTagName(tag)) {
                    filters.add(new BlockTagScanFilter(tag));
                }
            }
        }
        filter = new BlockCacheScanFilter(filters);
    }
}
