package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractCompanionsProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class ModCompanionProvider extends AbstractCompanionsProvider {
    protected ModCompanionProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        add("wolds_pets", builder -> {
            builder.addPet("bmo", 1.0);
        });
    }
}
