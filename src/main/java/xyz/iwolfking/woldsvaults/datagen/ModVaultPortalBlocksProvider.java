package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.block.VaultOreBlock;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.iwolfking.vhapi.api.datagen.AbstractVaultPortalBlocksProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.List;

public class ModVaultPortalBlocksProvider extends AbstractVaultPortalBlocksProvider {
    protected ModVaultPortalBlocksProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {

        add("vault_ores", builder -> {
            getAllVaultOres().forEach(builder::add);
        });

        add("copycats", builder -> {
            builder.add("copycats:copycat_block");
        });

        add("vh_compressium", builder -> {
            builder.add("the_vault:vault_stone_1");
            builder.add("the_vault:vault_stone_2");
            builder.add("the_vault:vault_cobblestone_1");
            builder.add("the_vault:vault_cobblestone_2");
            builder.add("the_vault:gem_pog_1");
            builder.add("the_vault:echo_pog_1");
            builder.add("the_vault:omega_pog_1");
        });

        add("vault_non_whole_blocks", builder -> {
            builder.add("the_vault:vault_stone_stairs");
            builder.add("the_vault:vault_stone_slab");
            builder.add("the_vault:polished_vault_stone_slab");
            builder.add("the_vault:polished_vault_stone_stairs");
            builder.add("the_vault:vault_stone_brick_slab");
            builder.add("the_vault:vault_stone_bricks_stairs");
        });

    }

    public static List<VaultOreBlock> getAllVaultOres() {
        return ForgeRegistries.BLOCKS.getValues().stream()
                .filter(b -> b instanceof VaultOreBlock)
                .map(b -> (VaultOreBlock) b)
                .toList();
    }
}
