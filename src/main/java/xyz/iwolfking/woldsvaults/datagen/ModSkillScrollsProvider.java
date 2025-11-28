package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractSkillScrollsProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class ModSkillScrollsProvider extends AbstractSkillScrollsProvider {
    protected ModSkillScrollsProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        add("wolds_additions", builder -> {
            builder.addAbility("Heal", 4);
            builder.addAbility("Colossus", 2);
            builder.addAbility("Expunge", 4);
        });
    }
}
