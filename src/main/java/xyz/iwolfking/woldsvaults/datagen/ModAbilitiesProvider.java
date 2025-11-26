package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractAbilityProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.abilities.*;

public class ModAbilitiesProvider extends AbstractAbilityProvider {
    protected ModAbilitiesProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {

        add("replace/diffuse", builder -> {
            builder.addSpecializedAbility("Expunge", "Diffuse", 8, 0, 1, 2, i ->
            {
                if (i == 0) {
                    return createTieredSkill("Expunge_Base", "Diffuse", 0, 1, 8, 30, (i2) -> createAndAssignId("expunge", i2, new ExpungeAbility(0, 1, 1, 700, 20 + (i2 * 4), 0.6F + (i2 * 0.2F), 0.6F + (i2 * 0.2F))));
                }
                else {
                    return createTieredSkill("Concentrate_Base", "Concentrate", 0, 1, 8, 30, (i2) -> createAndAssignId("concentrate", i2, new ConcentrateAbility(0, 1, 1, 900, 35 + (i2 * 2), 4.0F + (0.5F * i2), 120 + (i2 * 20), (i / 8), i2 * 0.1F, 30, 2)));
                }
            }).build();
        });

        add("replace/colossus", builder -> {
            builder.addSpecializedAbility("Colossus", "Colossus", 2, 0, 2, 2, i ->
            {
                if (i == 0) {
                    return createTieredSkill("Colossus_Base", "Colossus", 0, 2, 2, 30, (i2) -> createAndAssignId("colossus", i2, new ColossusAbility(0, 2, 2, 2000, 25, 400 + (i2 * 60), (float)Math.min(2.0, 1.4F + (0.1F * i2)), 0.2F + (i2 * 0.02F))  ));
                }
                else {
                    return createTieredSkill("Sneaky_Getaway", "Sneaky Getaway", 0, 2, 2, 30, (i2) -> createAndAssignId("sneaky_getaway", i2, new SneakyGetawayAbility(0, 2, 2, 1000, 25, 80 + (i2 * 20), 0.4F, 0.4F + (i2*0.1F))));
                }
            }).build();
        });

        add("add_specialization/levitation_specialization", builder -> {
            builder.addSpecializedAbility("Mega_Jump", "Mega Jump", 5, 0, 1, 1, i ->
                    createTieredSkill("Levitate", "Levitate", 0, 1, 5, 30, (i2) -> createAndAssignId("levitate", i2, new LevitateAbility(0, 1, 1, 0, 12 + (i2 * 2), 1 + i2)))).build();
        });

        add("add_specialization/chain_miner_specialization", builder -> {
            builder.addSpecializedAbility("Vein_Miner", "Vein Miner", 4, 0, 1, 1, i ->
                    createTieredSkill("Vein_Miner_Chain", "Chain Miner", 0, 1, 4, 30, (i2) -> createAndAssignId("chain_miner", i2, new VeinMinerChainAbility(0, 1, 1, 0, 3 + (i2) + (i2/4), 2 + (i2/6))))).build();
        });


    }
}
