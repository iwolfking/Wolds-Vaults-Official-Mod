package xyz.iwolfking.woldsvaults.api.helper;

import iskallia.vault.gear.VaultGearRarity;

public class VaultGearClassificationHelper {
    public static int getMapModifierCount(VaultGearRarity rarity) {
        return switch (rarity) {
            case OMEGA -> 10;
            case EPIC -> 8;
            case RARE -> 6;
            case COMMON -> 4;
            case SCRAPPY -> 2;
            default -> 0;
        };
    }
}
