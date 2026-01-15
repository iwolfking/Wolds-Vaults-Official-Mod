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
        add("wolds_layouts", builder -> {
            builder.addLayouts("default", layoutEntries -> {
                layoutEntries.add(0, crystalLayoutWeightedListBuilder -> {
                   crystalLayoutWeightedListBuilder.add(new ClassicTunnelCrystalLayout(1, 5, 4, 10), 1);
                   crystalLayoutWeightedListBuilder.add(new ClassicCircleCrystalLayout(1, 4), 1);
                   crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-4,  4, 4,  4, 4, -4, -4, -4}), 1);
                   crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-4,  0, 0,  4, 4, 0, 0, -4}), 1);
                   crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 5, 1, 1.2), 1);
                });
                layoutEntries.add(10, crystalLayoutWeightedListBuilder -> {
                    crystalLayoutWeightedListBuilder.add(new ClassicTunnelCrystalLayout(1, 8, 7, 6), 1);
                    crystalLayoutWeightedListBuilder.add(new ClassicCircleCrystalLayout(1, 6), 1);
                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-6,  6, 6,  6, 6, -6, -6, -6}), 1);
                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-6,  0, 0,  6, 6, 0, 0, -6}), 1);
                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 6, 1, 1.2), 1);
                });
                layoutEntries.add(25, crystalLayoutWeightedListBuilder -> {
                    crystalLayoutWeightedListBuilder.add(new ClassicTunnelCrystalLayout(1, 8, 7, 6), 1);
                    crystalLayoutWeightedListBuilder.add(new ClassicCircleCrystalLayout(1, 8), 1);
                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-8,  8, 8,  8, 8, -8, -8, -8}), 1);
                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-8,  0, 0,  8, 8, 0, 0, -8}), 1);
                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 8, 1, 1.2), 1);
                });
                layoutEntries.add(50, crystalLayoutWeightedListBuilder -> {
                    crystalLayoutWeightedListBuilder.add(new ClassicTunnelCrystalLayout(1, 10, 8, 6), 1);
                    crystalLayoutWeightedListBuilder.add(new ClassicCircleCrystalLayout(1, 12), 1);
                    crystalLayoutWeightedListBuilder.add(new ClassicCircleCrystalLayout(1, 7), 1);
                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-12,  12, 12,  12, 12, -12, -12, -12}), 1);
                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-12,  0, 0,  12, 12, 0, 0, -12}), 1);
                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-7,  0, 0,  7, 7, 0, 0, -7}), 1);
                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 12, 1, 1.2), 1);
                });
                layoutEntries.add(65, crystalLayoutWeightedListBuilder -> {
                    crystalLayoutWeightedListBuilder.add(new ClassicTunnelCrystalLayout(1, 11, 9, 3), 2);
                    crystalLayoutWeightedListBuilder.add(new ClassicCircleCrystalLayout(1, 14), 2);
                    crystalLayoutWeightedListBuilder.add(new ClassicCircleCrystalLayout(1, 9), 3);
                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-12,  12, 12,  12, 12, -12, -12, -12}), 3);
                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-12,  0, 0,  12, 12, 0, 0, -12}), 2);
                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-9,  0, 0,  9, 9, 0, 0, -9}), 2);
                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 14, 1, 1.2), 3);
                });
                layoutEntries.add(100, crystalLayoutWeightedListBuilder -> {
                    crystalLayoutWeightedListBuilder.add(new ClassicTunnelCrystalLayout(1, 12, 10, 3), 1);
                    crystalLayoutWeightedListBuilder.add(new ClassicCircleCrystalLayout(1, 16), 1);
                    crystalLayoutWeightedListBuilder.add(new ClassicCircleCrystalLayout(1, 14), 1);
                    crystalLayoutWeightedListBuilder.add(new ClassicCircleCrystalLayout(1, 12), 1);
                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-14,  14, 14,  14, 14, -14, -14, -14}), 1);
                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-14,  0, 0,  14, 14, 0, 0, -14}), 1);
                    crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-12,  0, 0,  12, 12, 0, 0, -12}), 1);
                    crystalLayoutWeightedListBuilder.add(new ClassicWaveCrystalLayout(1, 16, 1, 1.2), 1);
                    crystalLayoutWeightedListBuilder.add(new ClassicInfiniteCrystalLayout(1), 1);
                });
            });
        });
    }
}
