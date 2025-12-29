package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.skill.ability.AbilityType;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractAbilitiesGroupsProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class ModAbilityGroupProvider extends AbstractAbilitiesGroupsProvider {
    protected ModAbilityGroupProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        add("wolds_abilities", builder -> {
            builder.addAbilitiesToGroup(AbilityType.POWERUP, abilities -> {
                abilities.add("Colossus_Base");
                abilities.add("Sneaky_Getaway");
            });
            builder.addAbilitiesToGroup(AbilityType.UTILITY, abilities -> {
                abilities.add("Expunge_Base");
                abilities.add("Concentrate_Base");
                abilities.add("Levitate");
                abilities.add("Vein_Miner_Chain");
            });
        });
    }
}
