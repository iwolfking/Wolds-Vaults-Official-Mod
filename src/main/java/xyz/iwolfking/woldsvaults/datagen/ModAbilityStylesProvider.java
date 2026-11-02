package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.config.AbilitiesGUIConfig;
import iskallia.vault.init.ModConfigs;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.vhapi.api.datagen.AbstractAbilityGUIStylesProvider;
import xyz.iwolfking.vhapi.mixin.accessors.AbilitiesGUIConfigAccessor;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.ultimates.UltimateIds;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ModAbilityStylesProvider extends AbstractAbilityGUIStylesProvider {
    protected ModAbilityStylesProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        ModConfigs.ABILITIES_GUI = new AbilitiesGUIConfig().readConfig();
        add("wolds_abilities", builder -> {
            displayToRightOf("Necromancy", "Colossus", builder, styleBuilder -> {
                styleBuilder.specialization("Colossus_Base", WoldsVaults.id("gui/abilities/colossus"));
                styleBuilder.specialization("Sneaky_Getaway", WoldsVaults.id("gui/abilities/sneaky_getaway"));
            });
            displayToRightOf("Taunt", "Expunge", builder, styleBuilder -> {
                styleBuilder.specialization("Expunge_Base", WoldsVaults.id("gui/abilities/expunge"));
                styleBuilder.specialization("Concentrate_Base", WoldsVaults.id("gui/abilities/concentrate"));
            });
        });

        add("add_spec/levitate", builder -> {
            displayToRightOf("Hunter", "Mega_Jump", builder, styleBuilder -> {
                styleBuilder.specialization("Levitate", WoldsVaults.id("gui/abilities/levitate"));
            });
        });

        add("add_spec/chain_miner", builder -> {
            builder.add("Vein_Miner", 0, -100, styleBuilder -> {
                styleBuilder.specialization("Vein_Miner_Chain", WoldsVaults.id("gui/abilities/vein_miner_chain"));
            });
        });

        Map<String, ResourceLocation> ultimateSpecializations = new LinkedHashMap<>();
        ultimateSpecializations.put(UltimateIds.DORMANT, WoldsVaults.id("gui/abilities/stirrings_of_power"));
        ultimateSpecializations.put(UltimateIds.COPE_DE_GRACE, WoldsVaults.id("gui/abilities/cope_de_grace"));
        ultimateSpecializations.put(UltimateIds.SAVIOR, WoldsVaults.id("gui/abilities/savior"));
        ultimateSpecializations.put(UltimateIds.EYES_OF_GOD, WoldsVaults.id("gui/abilities/eyes_of_god"));
        ultimateSpecializations.put(UltimateIds.BULLET_TIME, WoldsVaults.id("gui/abilities/bullet_time"));
        addOrdered("god_ultimates", UltimateIds.STIRRINGS_OF_POWER, 70, 265, ultimateSpecializations);

        Map<String, ResourceLocation> blockSpecializations = new LinkedHashMap<>();
        blockSpecializations.put("Life_Block_Base", WoldsVaults.id("gui/abilities/life_block"));
        blockSpecializations.put("Mana_Block", WoldsVaults.id("gui/abilities/mana_block"));
        addOrdered("life_block", "Life_Block", 105, 265, blockSpecializations);
    }

    /**
     * Adds a single ability style whose specializations keep their insertion order. The ability tab places nodes in
     * map order but draws the specialization track from the skill's first specialization, so a reordered map (VHAPI's
     * StyleBuilder uses a HashMap) leaves the track hanging below the column.
     */
    private void addOrdered(String fileName, String abilityName, int x, int y, Map<String, ResourceLocation> specializations) {
        Map<String, AbilitiesGUIConfig.SpecializationStyle> specializationStyles = new LinkedHashMap<>();
        specializations.forEach((name, icon) -> specializationStyles.put(name, new AbilitiesGUIConfig.SpecializationStyle(icon)));
        Map<String, AbilitiesGUIConfig.AbilityStyle> styles = new LinkedHashMap<>();
        styles.put(abilityName, new AbilitiesGUIConfig.AbilityStyle(x, y, specializationStyles));
        AbilitiesGUIConfig config = new AbilitiesGUIConfig();
        ((AbilitiesGUIConfigAccessor) config).setStyles(styles);
        ((AbilitiesGUIConfigAccessor) config).setLabels(new ArrayList<>());
        ((AbilitiesGUIConfigAccessor) config).setLines(new ArrayList<>());
        addConfig(fileName, config);
    }

    public void displayToRightOf(String targetAbility, String abilityName, Builder builder, Consumer<Builder.StyleBuilder> builderConsumer) {
        if(ModConfigs.ABILITIES_GUI.getStyles().containsKey(targetAbility)) {
            AbilitiesGUIConfig.AbilityStyle style = ModConfigs.ABILITIES_GUI.getStyles().get(targetAbility);
            builder.add(abilityName, style.getX() + 35, style.getY(), builderConsumer);
        }
    }

}
