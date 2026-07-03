package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractImplicitDeckModifiersProvider;

public class ModImplicitDeckCoresProvider extends AbstractImplicitDeckModifiersProvider {
    protected ModImplicitDeckCoresProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        add("wolds_implicits", builder -> {
            builder.add("merchant", "merchant_deck");
            builder.add("extended", "extended_deck");
            builder.add("treasure", "treasure_deck");
            builder.add("arcane", "arcane_deck");
            builder.add("idona", "idona_deck");
            builder.add("velara", "velara_deck");
            builder.add("tenos", "tenos_deck");
            builder.add("wendarr", "wendarr_deck");
        });
    }
}
