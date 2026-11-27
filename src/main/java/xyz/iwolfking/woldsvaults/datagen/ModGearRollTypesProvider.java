package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.gear.VaultGearRarity;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractGearRollTypeProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.data.WoldConstants;

public class ModGearRollTypesProvider extends AbstractGearRollTypeProvider {
    protected ModGearRollTypesProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        VaultGearRarity mythic = VaultGearRarity.valueOf("MYTHIC");
        add("gear_roll_types", builder -> builder
                .add(WoldConstants.MYTHIC_ROLL_TYPE, 12000284, pool -> pool
                        .add(mythic, 1))
                .add(WoldConstants.OMEGA_PLUS_ROLL_TYPE, 3125022, pool -> pool
                        .add(VaultGearRarity.OMEGA, 98)
                        .add(mythic, 2))
                .add(WoldConstants.OMEGA_PLUS_HIGH_ROLL_TYPE, 3125022, pool -> pool
                        .add(VaultGearRarity.OMEGA, 95)
                        .add(mythic, 5))
                .add(WoldConstants.SCRAPPY_PLUS_PLUS_ROLL_TYPE, 13818334, pool -> pool
                        .add(VaultGearRarity.SCRAPPY, 20)
                        .add(VaultGearRarity.COMMON, 25)
                        .add(VaultGearRarity.RARE, 30)
                        .add(VaultGearRarity.EPIC, 14)
                        .add(VaultGearRarity.OMEGA, 10)
                        .add(mythic, 1)));
    }
}
