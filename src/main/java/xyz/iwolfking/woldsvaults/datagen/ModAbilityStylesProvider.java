package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import iskallia.vault.config.AbilitiesGUIConfig;
import iskallia.vault.init.ModConfigs;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractAbilityGUIStylesProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.function.Consumer;

public class ModAbilityStylesProvider extends AbstractAbilityGUIStylesProvider {
    protected ModAbilityStylesProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        ModConfigs.ABILITIES_GUI = new AbilitiesGUIConfig().readConfig();
        add("wolds_abilities", builder -> {
            displayToRightOf("Rampage", "Colossus", builder, styleBuilder -> {
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
    }

    public void displayToRightOf(String targetAbility, String abilityName, Builder builder, Consumer<Builder.StyleBuilder> builderConsumer) {
        if(ModConfigs.ABILITIES_GUI.getStyles().containsKey(targetAbility)) {
            AbilitiesGUIConfig.AbilityStyle style = ModConfigs.ABILITIES_GUI.getStyles().get(targetAbility);
            builder.add(abilityName, style.getX() + 35, style.getY(), builderConsumer);
        }
    }

}
