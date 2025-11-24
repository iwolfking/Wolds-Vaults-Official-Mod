package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractVaultDiffuserProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModItems;

public class ModVaultDiffuserProvider extends AbstractVaultDiffuserProvider {
    protected ModVaultDiffuserProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
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
                    .add(VaultMod.id("silver_scrap_1"), 54)
                    .add(VaultMod.id("silver_scrap_2"), 486)
                    .add(VaultMod.id("velvet_block_1"), 36)
                    .add(VaultMod.id("ornate_block_1"), 36)
                    .add(VaultMod.id("gilded_block_1"), 36)
                    .add(VaultMod.id("living_rock_block_cobble_1"), 36)
                    .add(VaultMod.id("sandy_block_1"), 36)
                    .add(VaultMod.id("rotten_meat_block"), 36)
                    .add(VaultMod.id("magic_silk_block_1"), 405)
                    .add(VaultMod.id("vault_diamond_block"), 1152)
                    .add(VaultMod.id("vault_diamond_block_1"), 10368)
                    .add(VaultMod.id("vault_essence_1"), 99)
                    .add(VaultMod.id("vault_essence_2"), 891)
                    .add(VaultMod.id("vault_plating_block"), 36)
                    .add(VaultMod.id("vault_plating_block_1"), 486)
                    .add(VaultMod.id("ancient_copper_block_1"), 81)
                    .build();
        });
    }
}
