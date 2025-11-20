package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.lib.core.datagen.AbstractPlayerTitlesProvider;
import xyz.iwolfking.vhapi.api.lib.core.datagen.AbstractVaultModifierPoolsProvider;
import xyz.iwolfking.vhapi.api.lib.core.datagen.lib.modifier_pools.ModifierPoolBuilder;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.Map;
import java.util.function.Consumer;

public class ModPlayerTitlesProvider extends AbstractPlayerTitlesProvider {
    public ModPlayerTitlesProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }


    @Override
    protected void buildModifiers() {
        addPrefix("test_prefix", "Tester", "#33333", 1);
        addSuffix("test_suffix", "of Testing", "#33333", 1);
    }
}
