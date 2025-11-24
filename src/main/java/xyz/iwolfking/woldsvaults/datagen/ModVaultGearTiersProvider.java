package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import iskallia.vault.config.gear.VaultGearTierConfig;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractVaultGearConfigProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;
import java.util.List;

public class ModVaultGearTiersProvider extends AbstractVaultGearConfigProvider {
    public ModVaultGearTiersProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        addConfig("axe", new GearModifierConfigBuilder()
                .add(VaultGearTierConfig.ModifierAffixTagGroup.PREFIX, vaultGearAttributeGroupBuilder -> {
                    vaultGearAttributeGroupBuilder
                            .addModifier(ModGearAttributes.INCREASED_EFFECT_CLOUD_CHANCE, "BaseEffectCloudChance", "mod_effect_cloud_chance", List.of(), vaultGearModifierTiersBuilder -> {
                                vaultGearModifierTiersBuilder.add(0, 20, 10, 0.01F, 0.02F, 0.01F);
                                vaultGearModifierTiersBuilder.add(20, 40, 10, 0.02F, 0.03F, 0.01F);
                                vaultGearModifierTiersBuilder.add(40, -1, 10, 0.03F, 0.04F, 0.01F);
                                vaultGearModifierTiersBuilder.add(101, -1, 10, 0.05F, 0.09F, 0.01F);
                                vaultGearModifierTiersBuilder.add(102, -1, 10, 0.09F, 0.12F, 0.01F);
                            });
                }).build(VaultMod.id("axe")));
    }
}
