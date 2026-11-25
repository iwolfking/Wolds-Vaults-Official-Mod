package xyz.iwolfking.woldsvaults.init;

import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.init.ModSounds;
import net.minecraft.network.chat.TextColor;
import xyz.iwolfking.vhapi.api.extensions.gear.VaultGearRarityFactory;

public class ModVaultGearRarities {
    public static VaultGearRarity MYTHIC;

    public static void init() {
        MYTHIC = VaultGearRarityFactory.INSTANCE.create(
                "MYTHIC",
                TextColor.fromRgb(12000284),
                7, 6, 6, 6, 6, 6, 6, 6, 0, ModSounds.IDENTIFY_LEGENDARY, 16
        );
    }
}