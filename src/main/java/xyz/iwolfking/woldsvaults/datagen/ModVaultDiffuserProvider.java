package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import iskallia.vault.config.VaultDiffuserConfig;
import iskallia.vault.init.ModConfigs;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import xyz.iwolfking.vhapi.api.datagen.AbstractVaultDiffuserProvider;
import xyz.iwolfking.vhapi.api.util.ResourceLocUtils;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.init.ModCompressibleBlocks;
import xyz.iwolfking.woldsvaults.init.ModItems;

import java.util.concurrent.atomic.AtomicInteger;

public class ModVaultDiffuserProvider extends AbstractVaultDiffuserProvider {
    protected ModVaultDiffuserProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        ModConfigs.VAULT_DIFFUSER = new VaultDiffuserConfig().readConfig();
        add("wolds_items", builder -> {
            builder.add(ModItems.CHUNK_OF_POWER.getRegistryName(), 2048)
                    .add(ModItems.SOUL_ICHOR.getRegistryName(), 128)
                    .add(ModItems.GEM_BOX.getRegistryName(), 128)
                    .add(ModItems.SUPPLY_BOX.getRegistryName(), 96)
                    .add(ModItems.AUGMENT_BOX.getRegistryName(), 128)
                    .add(ModItems.ALTAR_DECATALYZER.getRegistryName(), 256)
                    .add(ModItems.CHROMATIC_GOLD_INGOT.getRegistryName(), 104)
                    .add(ModItems.WOLD_STAR_CHUNK.getRegistryName(), 9999)
                    .add(ModItems.ENIGMA_EGG.getRegistryName(), 128)
                    .add(ModItems.ARCANE_SHARD.getRegistryName(), 576)
                    .add(ModItems.ARCANE_ESSENCE.getRegistryName(), 64)
                    .add(iskallia.vault.init.ModItems.BANISHED_SOUL.getRegistryName(), 42)
                    .add(ModItems.VAULT_DECO_SCROLL.getRegistryName(), 4096)
                    .add(ModItems.OMEGA_BOX.getRegistryName(), 4096)
                    .add(ModItems.INSCRIPTION_BOX.getRegistryName(), 1024)
                    .add(ModItems.VAULT_DIAMOND_NUGGET.getRegistryName(), 14)
                    .add(ModItems.HASTY_POMEGRANATE.getRegistryName(), 486)
                    .add(ModItems.POLTERGEIST_PLUM.getRegistryName(), 666)
                    .add(iskallia.vault.init.ModItems.MYSTIC_PEAR.getRegistryName(), 4096)
                    .add(ModBlocks.VAULT_ESSENCE_BLOCK.getRegistryName(), 99)
                    .add(ModBlocks.CHROMATIC_GOLD_BLOCK.getRegistryName(), 936)
                    .add(ModBlocks.SILVER_SCRAP_BLOCK.getRegistryName(), 54)
                    .add(ModBlocks.VAULT_PLATING_BLOCK.getRegistryName(), 36)
                    .add(ModBlocks.CARBON_BLOCK.getRegistryName(), 162);
                    ModCompressibleBlocks.getRegisteredBlocks().forEach(((compressibleBlock, suppliers) -> {
                        compressed(compressibleBlock.name(), compressibleBlock.name(), compressibleBlock.getNestedDepth(), builder);
                    }));
                    builder.build();
        });
    }

    private static void compressed(int baseValue, String baseName, int maxDepth, Builder builder) {
        for(int i = 0; i < maxDepth + 1; i++) {
            if(i == 0) {
                builder.add(WoldsVaults.id(baseName), baseValue);
            }
            else {
                builder.add(ResourceLocation.fromNamespaceAndPath("compressium", baseName + "_" + i), (int) (baseValue * Math.pow(9, i)));
            }
        }
    }

    private static void compressed(String baseItem, String baseName, int maxDepth, Builder builder) {
        int baseValue = 0;

        if(builder.get(WoldsVaults.id(baseItem)) != 0) {
            baseValue = builder.get(WoldsVaults.id(baseItem));
        }
        else if(ModConfigs.VAULT_DIFFUSER.getDiffuserOutputMap().containsKey(WoldsVaults.id(baseItem))) {
            baseValue = ModConfigs.VAULT_DIFFUSER.getDiffuserOutputMap().get(WoldsVaults.id(baseItem));
        }
        else if(ModConfigs.VAULT_DIFFUSER.getDiffuserOutputMap().containsKey(VaultMod.id(baseItem))) {
            baseValue = ModConfigs.VAULT_DIFFUSER.getDiffuserOutputMap().get(VaultMod.id(baseItem));
        }

        if(baseValue == 0) {
            return;
        }

        for(int i = 1; i < maxDepth + 1; i++) {
            builder.add(ResourceLocation.fromNamespaceAndPath("compressium", baseName + "_" + i), (int) (baseValue * Math.pow(9, i)));
        }
    }
}
