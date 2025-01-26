package xyz.iwolfking.woldsvaults.integration.scannable.scanning;

import iskallia.vault.init.ModBlocks;
import li.cil.scannable.api.API;
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
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

@Mod.EventBusSubscriber(modid = API.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public enum VaultOresBlockScannerModule implements BlockScannerModule {
    INSTANCE;

    private Predicate<BlockState> filter;

    public static Set<ResourceLocation> vaultOreBlocks = Util.make(new HashSet<>(), c -> {
        c.add(ModBlocks.LARIMAR_ORE.getRegistryName());
        c.add(ModBlocks.BENITOITE_ORE.getRegistryName());
        c.add(ModBlocks.ALEXANDRITE_ORE.getRegistryName());
        c.add(ModBlocks.PAINITE_ORE.getRegistryName());
        c.add(ModBlocks.ASHIUM_ORE.getRegistryName());
        c.add(ModBlocks.BLACK_OPAL_ORE.getRegistryName());
        c.add(ModBlocks.CHROMATIC_IRON_ORE.getRegistryName());
        c.add(ModBlocks.BOMIGNITE_ORE.getRegistryName());
        c.add(ModBlocks.ECHO_ORE.getRegistryName());
        c.add(ModBlocks.GORGINITE_ORE.getRegistryName());
        c.add(ModBlocks.ISKALLIUM_ORE.getRegistryName());
        c.add(ModBlocks.PETZANITE_ORE.getRegistryName());
        c.add(ModBlocks.XENIUM_ORE.getRegistryName());
        c.add(ModBlocks.WUTODIE_ORE.getRegistryName());
        c.add(ModBlocks.UPALINE_ORE.getRegistryName());
        c.add(ModBlocks.SPARKLETINE_ORE.getRegistryName());
        c.add(ModBlocks.TUBIUM_ORE.getRegistryName());
        c.add(ModBlocks.PUFFIUM_ORE.getRegistryName());
    });

    @Override
    public int getEnergyCost(final ItemStack module) {
        return 500;
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
        for (final ResourceLocation location : vaultOreBlocks) {
            final Block block = ForgeRegistries.BLOCKS.getValue(location);
            if (block != null) {
                filters.add(new BlockScanFilter(block));
            }
        }
        final ITagManager<Block> tags = ForgeRegistries.BLOCKS.tags();
        if (tags != null) {
            for (final ResourceLocation location : vaultOreBlocks) {
                final TagKey<Block> tag = TagKey.create(Registry.BLOCK_REGISTRY, location);
                if (tags.isKnownTagName(tag)) {
                    filters.add(new BlockTagScanFilter(tag));
                }
            }
        }
        filter = new BlockCacheScanFilter(filters);
    }


}
