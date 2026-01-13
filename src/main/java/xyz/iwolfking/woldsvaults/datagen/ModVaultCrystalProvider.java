package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import iskallia.vault.core.vault.modifier.VaultModifierStack;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.core.world.roll.IntRoll;
import iskallia.vault.item.crystal.theme.ValueCrystalTheme;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.vhapi.api.datagen.AbstractTooltipProvider;
import xyz.iwolfking.vhapi.api.datagen.AbstractVaultCrystalConfigProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.layout.impl.ClassicRingsCrystalLayout;
import xyz.iwolfking.woldsvaults.api.core.layout.impl.ClassicTunnelCrystalLayout;
import xyz.iwolfking.woldsvaults.api.core.layout.impl.ClassicWaveCrystalLayout;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.objectives.HauntedBraziersCrystalObjective;

import java.util.List;

public class ModVaultCrystalProvider extends AbstractVaultCrystalConfigProvider {
    protected ModVaultCrystalProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        add("wolds_layouts", builder -> {
            builder.addLayouts(layoutEntries -> {
               layoutEntries.add(10, crystalLayoutWeightedListBuilder -> {
                   crystalLayoutWeightedListBuilder.add(new ClassicTunnelCrystalLayout(1, 6, 7, 6), 1);
                   crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 6, 1, 1.2), 1);
                   crystalLayoutWeightedListBuilder.add(new ClassicRingsCrystalLayout(1, 12, 2), 1);
               });
               layoutEntries.add(25, crystalLayoutWeightedListBuilder -> {
                    crystalLayoutWeightedListBuilder.add(new ClassicTunnelCrystalLayout(1, 8, 8, 6), 1);
                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 8, 1, 1.2), 1);
                    crystalLayoutWeightedListBuilder.add(new ClassicRingsCrystalLayout(1, 12, 2), 1);
               });
               layoutEntries.add(50, crystalLayoutWeightedListBuilder -> {
                    crystalLayoutWeightedListBuilder.add(new ClassicTunnelCrystalLayout(1, 11, 8, 1), 1);
                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 12, 1, 1.2), 1);
                    crystalLayoutWeightedListBuilder.add(new ClassicRingsCrystalLayout(1, 14, 2), 1);
               });
               layoutEntries.add(65, crystalLayoutWeightedListBuilder -> {
                    crystalLayoutWeightedListBuilder.add(new ClassicTunnelCrystalLayout(1, 11, 8, 1), 1);
                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 12, 1, 1.2), 1);
                    crystalLayoutWeightedListBuilder.add(new ClassicRingsCrystalLayout(1, 14, 2), 1);
               });
               layoutEntries.add(100, crystalLayoutWeightedListBuilder -> {
                    crystalLayoutWeightedListBuilder.add(new ClassicTunnelCrystalLayout(1, 13, 11, 2), 1);
                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 14, 1, 1.4), 1);
                    crystalLayoutWeightedListBuilder.add(new ClassicRingsCrystalLayout(1, 16, 2), 1);
               });
            });
        });
//        add("wolds_seals", builder -> {
//            builder.addSeal(ModItems.CRYSTAL_SEAL_SPIRITS.getRegistryName(), sealListBuilder -> {
//                sealListBuilder.add(0, sealEntryBuilder -> {
//                    sealEntryBuilder.input(iskallia.vault.init.ModItems.VAULT_CRYSTAL.getRegistryName());
//                    sealEntryBuilder.objective(new HauntedBraziersCrystalObjective(IntRoll.ofUniform(3, 5), 1.0F));
//                    sealEntryBuilder.theme(new ValueCrystalTheme(VaultMod.id("classic_vault_cave_deepslate")));
//                });
//                sealListBuilder.add(20, sealEntryBuilder -> {
//                    sealEntryBuilder.input(iskallia.vault.init.ModItems.VAULT_CRYSTAL.getRegistryName());
//                    sealEntryBuilder.objective(new HauntedBraziersCrystalObjective(IntRoll.ofUniform(4, 6), 1.0F));
//                    sealEntryBuilder.theme(new ValueCrystalTheme(VaultMod.id("classic_vault_cave_deepslate")));
//                });
//                sealListBuilder.add(50, sealEntryBuilder -> {
//                    sealEntryBuilder.input(iskallia.vault.init.ModItems.VAULT_CRYSTAL.getRegistryName());
//                    sealEntryBuilder.objective(new HauntedBraziersCrystalObjective(IntRoll.ofUniform(4, 7), 1.0F));
//                    sealEntryBuilder.theme(new ValueCrystalTheme(VaultMod.id("classic_vault_cave_deepslate")));
//                });
//                sealListBuilder.add(75, sealEntryBuilder -> {
//                    sealEntryBuilder.input(iskallia.vault.init.ModItems.VAULT_CRYSTAL.getRegistryName());
//                    sealEntryBuilder.objective(new HauntedBraziersCrystalObjective(IntRoll.ofUniform(4, 8), 1.0F));
//                    sealEntryBuilder.theme(new ValueCrystalTheme(VaultMod.id("classic_vault_cave_deepslate")));
//                });
//                sealListBuilder.add(90, sealEntryBuilder -> {
//                    sealEntryBuilder.input(iskallia.vault.init.ModItems.VAULT_CRYSTAL.getRegistryName());
//                    sealEntryBuilder.objective(new HauntedBraziersCrystalObjective(IntRoll.ofUniform(5, 8), 1.0F));
//                    sealEntryBuilder.theme(new ValueCrystalTheme(VaultMod.id("classic_vault_cave_deepslate")));
//                });
//            });
//        });
    }
}
