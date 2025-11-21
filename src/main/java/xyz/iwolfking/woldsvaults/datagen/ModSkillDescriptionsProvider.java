package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.config.SkillDescriptionsConfig;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.lib.core.AbstractSkillDescriptionsProvider;
import xyz.iwolfking.vhapi.api.util.builder.description.JsonDescription;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class ModSkillDescriptionsProvider extends AbstractSkillDescriptionsProvider {
    public ModSkillDescriptionsProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerDescriptions() {
        addDescription("Ars Nouveau", jsonElements -> {
            jsonElements.add(JsonDescription.simpleDescription("Test description.", "yellow"));
            jsonElements.add(JsonDescription.simpleDescription("\nTest description line 2", "yellow"));
        });
    }
}
