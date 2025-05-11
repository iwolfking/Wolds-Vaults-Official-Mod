package xyz.iwolfking.woldsvaults.api.helper;

import gaia.entity.FleshLich;
import iskallia.vault.config.ShopPedestalConfig;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultLevel;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import xyz.iwolfking.woldsvaults.init.ModBlocks;

public class ShopPedestalHelper {

    public static ShopPedestalConfig.ShopOffer generatePedestalOffer(BlockState state, VirtualWorld world, Vault vault, RandomSource random) {
        int level = (Integer) vault.getOptional(Vault.LEVEL).map(VaultLevel::get).orElse(0);

        return getOffer(state, level, random);
    }
    public static ShopPedestalConfig.ShopOffer generatePedestalOffer(BlockState state, Level world, RandomSource random) {
        Vault vault = ServerVaults.get(world).orElse(null);

        if(vault == null) {
            return ModConfigs.SHOP_PEDESTAL.getForLevel(0, random);
        }

        int level = (Integer) vault.getOptional(Vault.LEVEL).map(VaultLevel::get).orElse(0);

        return getOffer(state, level, random);
    }

    private static ShopPedestalConfig.ShopOffer getOffer(BlockState state, int level, RandomSource random) {
        if(state.getBlock().equals(ModBlocks.ETCHING_PEDESTAL)) {
            return xyz.iwolfking.woldsvaults.init.ModConfigs.ETCHING_SHOP_PEDESTAL.getForLevel(level, random);
        }
        else if(state.getBlock().equals(ModBlocks.GOD_VENDOR_PEDESTAL)) {
            return xyz.iwolfking.woldsvaults.init.ModConfigs.GOD_SHOP_PEDESTAL.getForLevel(level, random);
        }
        else if(state.getBlock().equals(ModBlocks.BLACKSMITH_VENDOR_PEDESTAL)) {
            return xyz.iwolfking.woldsvaults.init.ModConfigs.BLACKSMITH_SHOP_PEDESTAL.getForLevel(level, random);
        }
        else if(state.getBlock().equals(ModBlocks.RARE_VENDOR_PEDESTAL)) {
            return xyz.iwolfking.woldsvaults.init.ModConfigs.RARE_SHOP_PEDESTAL.getForLevel(level, random);
        }
        else if(state.getBlock().equals(ModBlocks.OMEGA_VENDOR_PEDESTAL)) {
            return xyz.iwolfking.woldsvaults.init.ModConfigs.OMEGA_SHOP_PEDESTAL.getForLevel(level, random);
        }
        else if(state.getBlock().equals(ModBlocks.SPOOKY_VENDOR_PEDESTAL)) {
            return xyz.iwolfking.woldsvaults.init.ModConfigs.SPOOKY_SHOP_PEDESTAL.getForLevel(level, random);
        }
        else {
            return ModConfigs.SHOP_PEDESTAL.getForLevel(level, random);
        }
    }
}
