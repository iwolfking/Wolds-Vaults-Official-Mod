package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.item.crystal.layout.ClassicCircleCrystalLayout;
import iskallia.vault.item.crystal.layout.ClassicPolygonCrystalLayout;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.layout.definitions.PolygonLayoutDefinition;
import xyz.iwolfking.woldsvaults.api.core.layout.impl.ClassicTunnelCrystalLayout;
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
                   crystalLayoutWeightedListBuilder.add(new ClassicTunnelCrystalLayout(1, 10, 6, 10), 1);
                   crystalLayoutWeightedListBuilder.add(new ClassicCircleCrystalLayout(1, 8), 1);
                   crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-4,  4, 4,  4, 4, -4, -4, -4}), 1);
                   crystalLayoutWeightedListBuilder.add(new ClassicPolygonCrystalLayout(1, new int[] {-4,  0, 0,  4, 4, 0, 0, -4}), 1);
                });
            });
        });
    }
}
