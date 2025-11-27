package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.skill.ability.effect.*;
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
                    return createTieredSkill("Colossus_Base", "Colossus", 0, 2, 2, 30, (i2) -> createAndAssignId("colossus", i2, new ColossusAbility(0, 2, 1, 2000, 25, 400 + (i2 * 60), (float)Math.min(2.0, 1.4F + (0.1F * i2)), 0.2F + (i2 * 0.02F))  ));
                }
                else {
                    return createTieredSkill("Sneaky_Getaway", "Sneaky Getaway", 0, 2, 2, 30, (i2) -> createAndAssignId("sneaky_getaway", i2, new SneakyGetawayAbility(0, 2, 1, 1000, 25, 80 + (i2 * 20), 0.4F, 0.4F + (i2*0.1F))));
                }
            }).build();
        });

        add("replace/fireball", builder -> {
            builder.addSpecializedAbility("Fireball", "Fireball", 8, 0, 1, 3, i ->
            {
                if (i == 0) {
                    return createTieredSkill("Fireball_Base", "Fireball", 0, 1, 1, 30, (i2) -> createAndAssignId("fireball", i2, new FireballAbility(0, 1, 1, 400, 22 + (i2*3), 0.4F + (0.2F * i2), 3F + (0.5F * i2))));
                }
                else if(i == 1) {
                    return createTieredSkill("Fireball_Volley", "Fire Volley", 0, 1, 1, 30, (i2) -> createAndAssignId("fireball_volley", i2, new BouncingFireballAbility(0, 1, 1, 400, 25, 0.25F + (i2 * 0.15F), 1.5F + (0.5F * i2), 2)));
                }
                else {
                    return createTieredSkill("Fireball_Fireshot", "Fireshot", 0, 1, 1, 30, (i2) -> createAndAssignId("fireball_fireshot", i2, new FireballFireshotAbility(0, 1, 1, 40, 6 + (i2 * 2), 0.2F + (0.1F * i2), 0)));
                }
            }).build();
        });

        add("replace/smite", builder -> {
            builder.addSpecializedAbility("Smite", "Smite", 8, 0, 1, 2, i ->
            {
                if (i == 0) {
                    return createTieredSkill("Smite_Base", "Smite", 0, 1, 1, 30, (i2) -> createAndAssignId("smite", i2, new SmiteAbility(0, 1, 1, 2400, 0.4F + (0.2F * i2), 1.6F + (0.1F * i2), Math.max(260 - (i2 * 20), 120), 0.4F + (0.1F * i2), 2F + (0.5F * i2), 16776959, 4F + i2)));
                }
                else {
                    return createTieredSkill("Smite_Archon", "Archon", 0, 1, 1, 30, (i2) -> createAndAssignId("smite_archon", i2, new SmiteArchonAbility(0, 1, 1, 1200, 0.5F, 0.8F, 2, 0.2F + (i2 * 0.2F), 0.8F + (0.1F * i2), 14282239, 1F + (0.2F * i2), 0F)));
                }
            }).build();
        });

        add("replace/arcane", builder -> {
            builder.addSpecializedAbility("Arcane", "Arcane", 8, 0, 1, 2, i ->
            {
                if (i == 0) {
                    return createTieredSkill("Arcane_Base", "Arcane", 0, 1, 1, 30, (i2) -> createAndAssignId("arcane", i2, new ArcaneAbility(0, 1, 1, 10, 10.0F, 0.075F + (0.05F * i2), 10F + (2F * i2))));
                }
                else {
                    return createTieredSkill("Arcane_Rail", "Rail", 0, 1, 1, 30, (i2) -> createAndAssignId("arcane_rail", i2, new ArcaneRailAbility(0, 1, 1, 200, 40 + (2 * i2), 1.0F + (0.2F * i2), 1.5F + (0.5F * i2))));
                }
            }).build();
        });

        add("add_specialization/mana_shield", builder -> {
            builder.addSpecializedAbility("Mana_Shield", "Mana Shield", 8, 0, 1, 1, i ->
            {
                    return createTieredSkill("Mana_Shield_Base", "Mana Shield", 0, 1, 1, 30, (i2) -> createAndAssignId("mana_shield", i2, new ManaShieldAbility(0, 1, 1, 600, 30 + (i2 * 5), 400 + (40 * i2), 8 + (i2 * 4))));
            }).build();
        });

        add("add_specialization/shield_bash", builder -> {
            builder.addSpecializedAbility("Shield_Bash", "Shield Bash", 8, 0, 1, 1, i ->
            {
                return createTieredSkill("Shield_Bash", "Shield_Bash", 0, 1, 1, 30, (i2) -> createAndAssignId("shield_bash", i2, new ShieldBashAbility(0, 1, 1, 240, 18 + (i2 * 2), 3F, 0.75F + (0.25F * i2))));
            }).build();
        });

        add("add_specialization/earthquake", builder -> {
            builder.addSpecializedAbility("Earthquake", "Earthquake", 8, 0, 1, 1, i ->
            {
                return createTieredSkill("Earthquake_Base", "Earthquake", 0, 1, 1, 30, (i2) -> createAndAssignId("earthquake", i2, new EarthquakeAbility(0, 1, 1, 400, 30 + (i2), 4.0F + (i2), 0.5F + (0.2F * i2), 0.5F, 80 - (2 * i2), 0.3F, 0.5F)));
            }).build();
        });

        add("add_specialization/implode", builder -> {
            builder.addSpecializedAbility("Implode", "Implode", 8, 0, 1, 1, i ->
            {
                return createTieredSkill("Mana_Shield_Implode", "Implode", 0, 1, 1, 30, (i2) -> createAndAssignId("implode", i2, new ImplodeAbility(0, 1, 1, 200, 0, 4F + (0.5F * i2), 1.0F + (i2 * 0.2F))));
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
