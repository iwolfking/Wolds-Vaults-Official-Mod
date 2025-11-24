package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractVaultModifiersProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class ModVaultModifiersProvider extends AbstractVaultModifiersProvider {
    public ModVaultModifiersProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {

    }
}
