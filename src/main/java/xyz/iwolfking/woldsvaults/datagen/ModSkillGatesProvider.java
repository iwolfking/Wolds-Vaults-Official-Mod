package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.config.ResearchesGUIConfig;
import iskallia.vault.init.ModConfigs;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractSkillGatesProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class ModSkillGatesProvider extends AbstractSkillGatesProvider {
    protected ModSkillGatesProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        ModConfigs.RESEARCHES_GUI = new ResearchesGUIConfig().readConfig();
        add("wolds_skill_gate_overrides", builder -> {
            builder.add("Automatic Genius", skillGateEntryBuilder -> {
                skillGateEntryBuilder.hideArrow(true);
                ModConfigs.RESEARCHES_GUI.getStyles().forEach((skill, skillStyle) -> {
                   if(skill.equals("Automatic Genius")) {
                       return;
                   }

                   skillGateEntryBuilder.dependsOn(typeBuilder -> {
                      typeBuilder.constant(skill);
                   });

               });
            });
        });
    }
}
