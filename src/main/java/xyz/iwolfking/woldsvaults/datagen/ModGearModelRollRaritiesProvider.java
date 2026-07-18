package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import iskallia.vault.gear.VaultGearRarity;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractVanillaGearModelRollsProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class ModGearModelRollRaritiesProvider extends AbstractVanillaGearModelRollsProvider {
    protected ModGearModelRollRaritiesProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        add("new_armor_models", builder -> {
            builder.addForRarity(VaultGearRarity.valueOf("MYTHIC"), ModelType.ARMOR, models -> {
                models.add(VaultMod.id("gear/armor/heatwave").toString());
            });
            builder.addForRarity(VaultGearRarity.UNIQUE, ModelType.ARMOR, models -> {
                models.add(VaultMod.id("gear/armor/plague").toString());
            });
        });
        add("new_shield_models", builder -> {
            builder.addForRarity(VaultGearRarity.valueOf("OMEGA"), ModelType.SHIELD, models -> {
                models.add(VaultMod.id("gear/shield/nokia").toString());
            });
        });
    }
}
