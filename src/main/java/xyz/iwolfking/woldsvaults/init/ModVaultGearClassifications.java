package xyz.iwolfking.woldsvaults.init;

import iskallia.vault.gear.VaultGearClassification;
import xyz.iwolfking.vhapi.api.extensions.gear.VaultGearClassificationFactory;
import xyz.iwolfking.woldsvaults.api.util.VaultGearClassificationHelper;

public class ModVaultGearClassifications {
    public static final VaultGearClassification MAP =
            VaultGearClassificationFactory.INSTANCE.create(
                    "MAP",
                    VaultGearClassificationHelper::getMapModifierCount
            );

    public static void init() {
    }
}