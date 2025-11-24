package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractAbilityProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.abilities.ConcentrateAbility;
import xyz.iwolfking.woldsvaults.abilities.ExpungeAbility;

public class ModAbilitiesProvider extends AbstractAbilityProvider {
    protected ModAbilitiesProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {


        addConfig("test", new AbilityBuilder()
                .addSpecializedAbility("Expunge", "Diffuse", 8, 0, 1, 2, i ->
                        {
                            if (i == 0) {
                                return createTieredSkill("Expunge_Base", "Diffuse", 0, 1, 8, 100, (i2) -> {
                                    return createAndAssignId("expunge", i2, new ExpungeAbility(0, 1, 1, 600, 30 + (i2 * 2), 2.0F + (0.5F * i2), 200 + (20 * i2)));
                                });
                            }
                            else {
                                return createTieredSkill("Concentrate_Base", "Concentrate", 0, 1, 8, 100, (i2) -> {
                                    return createAndAssignId("concentrate", i2, new ConcentrateAbility(0, 1, 1, 600, 30 + i2, 2.0F + (0.5F * i2), i2, i2, 1.0F, 30, 2));
                                });
                            }
                        })
                .build());
    }
}
