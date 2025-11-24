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
        add("test", builder -> {
            builder
                    .addGearAttributeTalent("Test", "Test", 4, 0, 1, 100, ModGearAttributes.THORNS_DAMAGE_FLAT, (i) -> 4 + (8 * i))
                    .build();
            });

    }
}
