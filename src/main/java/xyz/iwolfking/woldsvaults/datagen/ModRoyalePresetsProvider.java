package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.config.RoyalePresetConfig;
import iskallia.vault.core.world.roll.IntRoll;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractRoyalePresetsProvider;
import xyz.iwolfking.vhapi.api.datagen.AbstractTooltipProvider;
import xyz.iwolfking.vhapi.api.util.builder.description.JsonDescription;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModItems;

import javax.json.Json;

public class ModRoyalePresetsProvider extends AbstractRoyalePresetsProvider {
    protected ModRoyalePresetsProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        add("wolds_presets", builder -> {
            builder.add(0, skillPresetBuilder -> {
               skillPresetBuilder.add("Gassy", 1, skillEntryBasicListBuilder -> {
                   skillEntryBasicListBuilder.add(new RoyalePresetConfig.SkillEntry("Dash_Base", IntRoll.ofUniform(1, 3)));
                   skillEntryBasicListBuilder.add(new RoyalePresetConfig.SkillEntry("Vein_Miner_Base", IntRoll.ofUniform(1, 1)));
                   skillEntryBasicListBuilder.add(new RoyalePresetConfig.SkillEntry("Expunge", IntRoll.ofUniform(4, 8)));
                   skillEntryBasicListBuilder.add(new RoyalePresetConfig.SkillEntry("Grenade_Base", IntRoll.ofUniform(4, 8)));
                   skillEntryBasicListBuilder.add(new RoyalePresetConfig.SkillEntry("Battle_Cry_Base", IntRoll.ofUniform(2, 4)));
               }, talents -> {
                   talents.add(new RoyalePresetConfig.SkillEntry("Strength", IntRoll.ofUniform(2, 4)));
                   talents.add(new RoyalePresetConfig.SkillEntry("Speed", IntRoll.ofUniform(1, 2)));
                   talents.add(new RoyalePresetConfig.SkillEntry("Frenzy", IntRoll.ofUniform(1, 4)));
               }, descriptionDataBuilder -> {
                   descriptionDataBuilder.description(descriptions -> {
                      descriptions.add(JsonDescription.simple("This is a test preset!"));
                   });
               });
            }, (builder1) -> {}, (builder2) -> {});
        });
    }
}
