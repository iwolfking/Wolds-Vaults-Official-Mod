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
            builder.add("cactus", "cactus_deck");
            builder.add("champion", "champion_deck");
            builder.add("wall", "rook_deck");
            builder.add("cake", "cake_deck");
            builder.add("puzzle", "puzzle_deck");
            builder.add("mutant", "mutant_deck");
            builder.add("pillager", "pillager_deck");
            builder.add("belt", "belt_deck");
            builder.add("villager", "villager_deck");
            builder.add("runic", "runic_deck");
            builder.add("fairy", "fairy_deck");
            builder.add("snake", "snake_deck");
        });
    }
}
