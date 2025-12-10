package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.init.ModGearAttributes;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractTalentProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class ModTalentsProvider extends AbstractTalentProvider {
    protected ModTalentsProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
//        add("replace/intelligence", builder -> {
//            builder
//                    .addGearAttributeTalent("Intelligence", "Intelligence", 8, 0, 1, 100, ModGearAttributes.ABILITY_POWER, (i) -> 10 * i)
//                    .build();
//            });

    }
}
