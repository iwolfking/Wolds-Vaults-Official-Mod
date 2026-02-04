package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.item.crystal.layout.ClassicCircleCrystalLayout;
import iskallia.vault.item.crystal.layout.ClassicInfiniteCrystalLayout;
import iskallia.vault.item.crystal.layout.ClassicPolygonCrystalLayout;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.layout.impl.ClassicTunnelCrystalLayout;
import xyz.iwolfking.woldsvaults.api.core.layout.impl.ClassicWaveCrystalLayout;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractEtchedVaultLayoutProvider;

public class ModEtchedVaultLayoutsProvider extends AbstractEtchedVaultLayoutProvider {
    protected ModEtchedVaultLayoutsProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
//        add("wolds_layouts", builder -> {
//            builder.addLayouts("default", layoutEntries -> {
//                layoutEntries.add(0, crystalLayoutWeightedListBuilder -> {
//                   crystalLayoutWeightedListBuilder.add(new ClassicTunnelCrystalLayout(1, 5, 4, 10), 1);
//                   crystalLayoutWeightedListBuilder.add(new ClassicCircleCrystalLayout(1, 4), 1);
//                   crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-4,  4, 4,  4, 4, -4, -4, -4}), 1);
//                   crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-4,  0, 0,  4, 4, 0, 0, -4}), 1);
//                   crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 5, 1, 1.2), 1);
//                   crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {0, -6, 5, -3, 5, 3, 0, 6, -5, 3, -5, -3}), 1);
//                   crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] { -2, -6, 4, -5, 7, -1, 5, 4, 1, 6, -4, 3, -6, -1}), 1);
//                   crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {  -3, -6, 3, -6, 6, -2, 6, 2, 3, 6, -3, 6, -6, 2, -6, -2}), 1);
//                   crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] { -6, -4, -2, -6, 4, -5, 6, -1, 4, 4, -1, 6, -5, 3}), 1);
//                   crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {4,-4,4,-4,4,4,-4,4}), 1);
//                   crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-5,-3,0,-3,0,3,5,3,5,5,-5,5,-5,-3}), 1);
//                   crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-3,-5,3,-5,5,-2,3,3,-3,5,-5,2,-3,-3}), 1);
//                });
//                layoutEntries.add(10, crystalLayoutWeightedListBuilder -> {
//                    crystalLayoutWeightedListBuilder.add(new ClassicTunnelCrystalLayout(1, 8, 7, 6), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicCircleCrystalLayout(1, 6), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-6,  6, 6,  6, 6, -6, -6, -6}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-6,  0, 0,  6, 6, 0, 0, -6}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 6, 1, 1.2), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-5,-3,0,-3,0,3,5,3,5,5,-5,5,-5,-3}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-3,-5,3,-5,5,-2,3,3,-3,5,-5,2,-3,-3}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {0,-5,2,-2,5,-1,2,2,0,5,-2,2,-5,1,-2,-2}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 8, 2, 0.5), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 4, 0, 15), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 4, 1, 42), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 4, 2, 50), 1);
//                });
//                layoutEntries.add(25, crystalLayoutWeightedListBuilder -> {
//                    crystalLayoutWeightedListBuilder.add(new ClassicTunnelCrystalLayout(1, 8, 7, 6), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicCircleCrystalLayout(1, 8), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-8,  8, 8,  8, 8, -8, -8, -8}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-8,  0, 0,  8, 8, 0, 0, -8}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 8, 1, 1.2), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 11, 1, 1.2), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 5, 2, 0.1), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 5, 4, 18.2), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {4,-3,-1,-5,2,-4,5,0,3,3,0,5,-3,3,-5,0,-3,-2}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {0,-9,4,-5,7,0,4,5,0,9,-4,5,-7,0,-4,-5}), 1);
//                });
//                layoutEntries.add(50, crystalLayoutWeightedListBuilder -> {
//                    crystalLayoutWeightedListBuilder.add(new ClassicTunnelCrystalLayout(1, 10, 8, 6), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicCircleCrystalLayout(1, 12), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicCircleCrystalLayout(1, 7), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-12,  12, 12,  12, 12, -12, -12, -12}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-12,  0, 0,  12, 12, 0, 0, -12}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-7,  0, 0,  7, 7, 0, 0, -7}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 12, 1, 1.2), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 11, 1, 1.2), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 5, 2, 0.1), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 5, 4, 18.2), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {4,-3,-1,-5,2,-4,5,0,3,3,0,5,-3,3,-5,0,-3,-2}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {0,-9,4,-5,7,0,4,5,0,9,-4,5,-7,0,-4,-5}), 1);
//                });
//                layoutEntries.add(65, crystalLayoutWeightedListBuilder -> {
//                    crystalLayoutWeightedListBuilder.add(new ClassicTunnelCrystalLayout(1, 11, 9, 3), 2);
//                    crystalLayoutWeightedListBuilder.add(new ClassicCircleCrystalLayout(1, 14), 2);
//                    crystalLayoutWeightedListBuilder.add(new ClassicCircleCrystalLayout(1, 9), 3);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-12,  12, 12,  12, 12, -12, -12, -12}), 3);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-12,  0, 0,  12, 12, 0, 0, -12}), 2);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-9,  0, 0,  9, 9, 0, 0, -9}), 2);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 14, 1, 1.2), 3);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 12, 1, 10), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {0,-9,4,-5,7,0,4,5,0,9,-4,5,-7,0,-4,-5}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-7,-3,-2,-6,4,-6,8,-2,4,6,-2,6,-7,3}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-6,-5,4,-6,7,-2,6,5,-3,6,-7,2}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-6,-2,-2,-6,4,-4,6,2,2,6,-4,4,-6,2}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {0,-7,3,-4,7,-3,4,0,7,3,3,4,0,7,-3,4,-7,3,-4,0}), 1);
//                });
//                layoutEntries.add(100, crystalLayoutWeightedListBuilder -> {
//                    crystalLayoutWeightedListBuilder.add(new ClassicTunnelCrystalLayout(1, 12, 10, 3), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicCircleCrystalLayout(1, 16), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicCircleCrystalLayout(1, 14), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicCircleCrystalLayout(1, 12), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-14,  14, 14,  14, 14, -14, -14, -14}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-14,  0, 0,  14, 14, 0, 0, -14}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-12,  0, 0,  12, 12, 0, 0, -12}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 16, 1, 1.2), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicInfiniteCrystalLayout(1), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 24, 3, 0.4), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 25, 3, 0.15), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-8,-6,0,-8,8,-6,8,0,4,6,0,8,-4,6,-8,0,-8,-2}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {0,-7,3,-4,7,-3,4,0,7,3,3,4,0,7,-3,4,-7,3,-4,0}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-8,-6,0,-8,8,-6,8,0,6,8,0,8,-6,6,-8,0}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-8,-8,8,-8,8,8,0,12,-8,8,-12,0}), 1);
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
//                            -3,-6}), 1);
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
//                            -2,-9}), 1);
//                });
//            });
//            builder.addLayouts("shop_default", layoutEntries -> {
//                layoutEntries.add(0, crystalLayoutWeightedListBuilder -> {
//                    crystalLayoutWeightedListBuilder.add(new ClassicTunnelCrystalLayout(1, 5, 4, 10), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicCircleCrystalLayout(1, 4), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-4,  4, 4,  4, 4, -4, -4, -4}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-4,  0, 0,  4, 4, 0, 0, -4}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 5, 1, 1.2), 1);
//                });
//                layoutEntries.add(10, crystalLayoutWeightedListBuilder -> {
//                    crystalLayoutWeightedListBuilder.add(new ClassicTunnelCrystalLayout(1, 8, 7, 6), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicCircleCrystalLayout(1, 6), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-6,  6, 6,  6, 6, -6, -6, -6}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-6,  0, 0,  6, 6, 0, 0, -6}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 6, 1, 1.2), 1);
//                });
//                layoutEntries.add(25, crystalLayoutWeightedListBuilder -> {
//                    crystalLayoutWeightedListBuilder.add(new ClassicTunnelCrystalLayout(1, 8, 7, 6), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicCircleCrystalLayout(1, 8), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-8,  8, 8,  8, 8, -8, -8, -8}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-8,  0, 0,  8, 8, 0, 0, -8}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 8, 1, 1.2), 1);
//                });
//                layoutEntries.add(50, crystalLayoutWeightedListBuilder -> {
//                    crystalLayoutWeightedListBuilder.add(new ClassicTunnelCrystalLayout(1, 10, 8, 6), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicCircleCrystalLayout(1, 12), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicCircleCrystalLayout(1, 7), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-12,  12, 12,  12, 12, -12, -12, -12}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-12,  0, 0,  12, 12, 0, 0, -12}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-7,  0, 0,  7, 7, 0, 0, -7}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 12, 1, 1.2), 1);
//                });
//                layoutEntries.add(65, crystalLayoutWeightedListBuilder -> {
//                    crystalLayoutWeightedListBuilder.add(new ClassicTunnelCrystalLayout(1, 11, 9, 3), 2);
//                    crystalLayoutWeightedListBuilder.add(new ClassicCircleCrystalLayout(1, 14), 2);
//                    crystalLayoutWeightedListBuilder.add(new ClassicCircleCrystalLayout(1, 9), 3);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-12,  12, 12,  12, 12, -12, -12, -12}), 3);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-12,  0, 0,  12, 12, 0, 0, -12}), 2);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-9,  0, 0,  9, 9, 0, 0, -9}), 2);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 14, 1, 1.2), 3);
//                });
//                layoutEntries.add(100, crystalLayoutWeightedListBuilder -> {
//                    crystalLayoutWeightedListBuilder.add(new ClassicTunnelCrystalLayout(1, 12, 10, 3), 2);
//                    crystalLayoutWeightedListBuilder.add(new ClassicCircleCrystalLayout(1, 16), 2);
//                    crystalLayoutWeightedListBuilder.add(new ClassicCircleCrystalLayout(1, 14), 2);
//                    crystalLayoutWeightedListBuilder.add(new ClassicCircleCrystalLayout(1, 12), 2);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-14,  14, 14,  14, 14, -14, -14, -14}), 2);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-14,  0, 0,  14, 14, 0, 0, -14}), 2);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-12,  0, 0,  12, 12, 0, 0, -12}), 2);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 16, 1, 1.2), 2);
//                    crystalLayoutWeightedListBuilder.add(new ClassicInfiniteCrystalLayout(1), 1);
//                });
//            });
//            builder.addLayouts("omega", layoutEntries -> {
//                layoutEntries.add(0, crystalLayoutWeightedListBuilder -> {
//                    crystalLayoutWeightedListBuilder.add(new ClassicTunnelCrystalLayout(1, 7, 7, 1), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicCircleCrystalLayout(1, 10), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-8,  8, 8,  8, 8, -8, -8, -8}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-8,  0, 0,  8, 8, 0, 0, -8}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 10, 1, 1.2), 1);
//                });
//                layoutEntries.add(10, crystalLayoutWeightedListBuilder -> {
//                    crystalLayoutWeightedListBuilder.add(new ClassicTunnelCrystalLayout(1, 12, 10, 2), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicCircleCrystalLayout(1, 12), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-10,  10, 10,  10, 10, -10, -10, -10}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-10,  0, 0,  10, 10, 0, 0, -10}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 11, 1, 1.2), 1);
//                });
//                layoutEntries.add(25, crystalLayoutWeightedListBuilder -> {
//                    crystalLayoutWeightedListBuilder.add(new ClassicTunnelCrystalLayout(1, 12, 11, 1), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicCircleCrystalLayout(1, 14), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-13,  13, 13,  13, 13, -13, -13, -13}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-13,  0, 0,  13, 13, 0, 0, -13}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 12, 1, 1.2), 1);
//                });
//                layoutEntries.add(50, crystalLayoutWeightedListBuilder -> {
//                    crystalLayoutWeightedListBuilder.add(new ClassicTunnelCrystalLayout(1, 13, 11, 1), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicCircleCrystalLayout(1, 16), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicCircleCrystalLayout(1, 14), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-14,  14, 14,  14, 14, -14, -14, -14}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-14,  0, 0,  14, 14, 0, 0, -14}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-12,  0, 0,  12, 12, 0, 0, -12}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 12, 1, 1.2), 1);
//                });
//                layoutEntries.add(65, crystalLayoutWeightedListBuilder -> {
//                    crystalLayoutWeightedListBuilder.add(new ClassicTunnelCrystalLayout(1, 13, 11, 1), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicCircleCrystalLayout(1, 16), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicCircleCrystalLayout(1, 14), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-14,  14, 14,  14, 14, -14, -14, -14}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-14,  0, 0,  14, 14, 0, 0, -14}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-12,  0, 0,  12, 12, 0, 0, -12}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 12, 1, 1.2), 1);
//                });
//                layoutEntries.add(100, crystalLayoutWeightedListBuilder -> {
//                    crystalLayoutWeightedListBuilder.add(new ClassicTunnelCrystalLayout(1, 15, 15, 1), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicTunnelCrystalLayout(1, 13, 13, 1), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicTunnelCrystalLayout(1, 11, 11, 1), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicCircleCrystalLayout(1, 20), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicCircleCrystalLayout(1, 16), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicCircleCrystalLayout(1, 14), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-20,  20, 20,  20, 20, -20, -20, -20}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-16,  16, 16,  16, 16, -16, -16, -16}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-20,  0, 0,  20, 20, 0, 0, -20}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-16,  0, 0,  16, 16, 0, 0, -16}), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 12, 1, 1.2), 1);
//                    crystalLayoutWeightedListBuilder.add(new ClassicInfiniteCrystalLayout(1), 1);
//                });
//            });
//        });
    }
}
