package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import iskallia.vault.core.vault.modifier.VaultModifierStack;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.core.world.roll.IntRoll;
import iskallia.vault.item.crystal.layout.ClassicPolygonCrystalLayout;
import iskallia.vault.item.crystal.objective.CrystalObjective;
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
import xyz.iwolfking.woldsvaults.objectives.ScalingBallisticBingoCrystalObjective;
import xyz.iwolfking.woldsvaults.objectives.SurvivalCrystalObjective;

import java.util.List;

public class ModVaultCrystalProvider extends AbstractVaultCrystalConfigProvider {
    protected ModVaultCrystalProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
//        add("wolds_layouts", builder -> {
//            builder.addLayouts(layoutEntries -> {
//               layoutEntries.add(10, crystalLayoutWeightedListBuilder -> {
//                   crystalLayoutWeightedListBuilder.add(new ClassicTunnelCrystalLayout(1, 6, 7, 6), 25);
//                   crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 6, 1, 1.2), 50);
//                   crystalLayoutWeightedListBuilder.add(new ClassicRingsCrystalLayout(1, 12, 2), 25);
//                   crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {0, -6, 5, -3, 5, 3, 0, 6, -5, 3, -5, -3}), 25);
//                   crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] { -2, -6, 4, -5, 7, -1, 5, 4, 1, 6, -4, 3, -6, -1}), 25);
//                   crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {  -3, -6, 3, -6, 6, -2, 6, 2, 3, 6, -3, 6, -6, 2, -6, -2}), 25);
//                   crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] { -6, -4, -2, -6, 4, -5, 6, -1, 4, 4, -1, 6, -5, 3}), 25);
//                   crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {4,-4,4,-4,4,4,-4,4}), 25);
//                   crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-5,-3,0,-3,0,3,5,3,5,5,-5,5,-5,-3}), 25);
//                   crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-3,-5,3,-5,5,-2,3,3,-3,5,-5,2,-3,-3}), 25);
//                   crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 3, 0, 15), 50);
//               });
//               layoutEntries.add(25, crystalLayoutWeightedListBuilder -> {
//                    crystalLayoutWeightedListBuilder.add(new ClassicTunnelCrystalLayout(1, 8, 8, 6), 25);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 8, 1, 1.2), 50);
//                    crystalLayoutWeightedListBuilder.add(new ClassicRingsCrystalLayout(1, 12, 2), 25);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-5,-3,0,-3,0,3,5,3,5,5,-5,5,-5,-3}), 25);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-3,-5,3,-5,5,-2,3,3,-3,5,-5,2,-3,-3}), 25);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {0,-5,2,-2,5,-1,2,2,0,5,-2,2,-5,1,-2,-2}), 25);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 8, 2, 0.5), 50);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 4, 0, 15), 50);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 4, 1, 42), 50);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 4, 2, 50), 50);
//               });
//               layoutEntries.add(50, crystalLayoutWeightedListBuilder -> {
//                    crystalLayoutWeightedListBuilder.add(new ClassicTunnelCrystalLayout(1, 11, 8, 1), 25);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 12, 1, 1.2), 50);
//                    crystalLayoutWeightedListBuilder.add(new ClassicRingsCrystalLayout(1, 14, 2), 25);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 11, 1, 1.2), 50);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 5, 2, 0.1), 50);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 5, 4, 18.2), 50);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {4,-3,-1,-5,2,-4,5,0,3,3,0,5,-3,3,-5,0,-3,-2}), 50);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {0,-9,4,-5,7,0,4,5,0,9,-4,5,-7,0,-4,-5}), 50);
//               });
//               layoutEntries.add(65, crystalLayoutWeightedListBuilder -> {
//                    crystalLayoutWeightedListBuilder.add(new ClassicTunnelCrystalLayout(1, 11, 8, 1), 25);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 12, 1, 1.2), 50);
//                    crystalLayoutWeightedListBuilder.add(new ClassicRingsCrystalLayout(1, 14, 2), 25);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 12, 1, 10), 50);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {0,-9,4,-5,7,0,4,5,0,9,-4,5,-7,0,-4,-5}), 50);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-7,-3,-2,-6,4,-6,8,-2,4,6,-2,6,-7,3}), 50);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-6,-5,4,-6,7,-2,6,5,-3,6,-7,2}), 50);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-6,-2,-2,-6,4,-4,6,2,2,6,-4,4,-6,2}), 50);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {0,-7,3,-4,7,-3,4,0,7,3,3,4,0,7,-3,4,-7,3,-4,0}), 50);
//               });
//               layoutEntries.add(100, crystalLayoutWeightedListBuilder -> {
//                    crystalLayoutWeightedListBuilder.add(new ClassicTunnelCrystalLayout(1, 13, 11, 2), 25);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 14, 1, 1.4), 50);
//                    crystalLayoutWeightedListBuilder.add(new ClassicRingsCrystalLayout(1, 16, 2), 25);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 24, 3, 0.4), 50);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 25, 3, 0.15), 50);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-8,-6,0,-8,8,-6,8,0,4,6,0,8,-4,6,-8,0,-8,-2}), 50);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {0,-7,3,-4,7,-3,4,0,7,3,3,4,0,7,-3,4,-7,3,-4,0}), 50);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-8,-6,0,-8,8,-6,8,0,6,8,0,8,-6,6,-8,0}), 50);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-8,-8,8,-8,8,8,0,12,-8,8,-12,0}), 50);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {0,-9,
//                            3,-6,
//                            6,-6,
//                            6,-3,
//                            9,0,
//                            6,3,
//                            6,6,
//                            3,6,
//                            0,9,
//                            -3,6,
//                            -6,6,
//                            -6,3,
//                            -9,0,
//                            -6,-3,
//                            -6,-6,
//                            -3,-6}), 50);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {0,-9,
//                            2,-9,
//                            2,-7,
//                            5,-7,
//                            5,-5,
//                            2,-5,
//                            2,-2,
//                            4,-2,
//                            4,2,
//                            2,2,
//                            2,5,
//                            5,5,
//                            5,7,
//                            2,7,
//                            2,9,
//                            0,9,
//                            -2,9,
//                            -2,7,
//                            -5,7,
//                            -5,5,
//                            -2,5,
//                            -2,2,
//                            -4,2,
//                            -4,-2,
//                            -2,-2,
//                            -2,-5,
//                            -5,-5,
//                            -5,-7,
//                            -2,-7,
//                            -2,-9}), 50);
//               });
//            });
//        });
        add("wolds_seals", builder -> {
            builder.addSeal(ModItems.CRYSTAL_SEAL_DOOMSAYER.getRegistryName(), sealListBuilder -> {
                sealListBuilder.add(0, sealEntryBuilder -> {
                    sealEntryBuilder.input(iskallia.vault.init.ModItems.VAULT_CRYSTAL.getRegistryName());
                    sealEntryBuilder.objective(new ScalingBallisticBingoCrystalObjective(0.25F, 0));
                });
            });
            builder.addSeal(ModItems.CRYSTAL_SEAL_SURVIVOR.getRegistryName(), sealListBuilder -> {
                sealListBuilder.add(0, sealEntryBuilder -> {
                    sealEntryBuilder.input(iskallia.vault.init.ModItems.VAULT_CRYSTAL.getRegistryName());
                    sealEntryBuilder.objective(new SurvivalCrystalObjective(0.1F, 10, List.of("t1", "t1_t2", "t2", "t2_t3", "t3", "t3_t4", "t4")));
                });
            });
        });
    }
}
