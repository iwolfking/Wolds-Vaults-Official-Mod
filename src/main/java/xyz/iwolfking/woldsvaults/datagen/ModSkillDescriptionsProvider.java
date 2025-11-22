package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.lib.core.datagen.AbstractSkillDescriptionsProvider;
import xyz.iwolfking.vhapi.api.util.builder.description.JsonDescription;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class ModSkillDescriptionsProvider extends AbstractSkillDescriptionsProvider {
    public ModSkillDescriptionsProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        addConfig("descriptions", new SkillDescriptionsConfigBuilder()
                .addDescription("Ars Nouveau", jsonElements -> {
                    jsonElements.add(JsonDescription.simpleDescription("Test", "yellow"));
                    jsonElements.add(JsonDescription.simpleDescription("\nConfig Datagen", "yellow"));
                })
                .addDescription("Ars Nouveau 2", jsonElements -> {
                    jsonElements.add(JsonDescription.simpleDescription("Test\n", "yellow"));
                    jsonElements.add(JsonDescription.simpleDescription("Whatever\n", "yellow"));
                })
                .build());
    }
}
