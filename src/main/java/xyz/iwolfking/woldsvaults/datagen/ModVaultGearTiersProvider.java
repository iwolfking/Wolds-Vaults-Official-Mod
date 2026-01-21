package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.gear.attribute.ability.AbilityLevelAttribute;
import iskallia.vault.gear.attribute.custom.effect.EffectCloudAttribute;
import iskallia.vault.gear.attribute.talent.TalentLevelAttribute;
import iskallia.vault.init.ModEffects;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
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
        add("unique", builder -> {
            builder.key(VaultMod.id("unique")).add(VaultGearTierConfig.ModifierAffixTagGroup.IMPLICIT, vaultGearAttributeGroupBuilder -> {
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.ATTACK_DAMAGE, "BaseDamage", "trident_damage_low", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, 15, 10, 8, 14, 0.5);
                            vaultGearModifierTiersBuilder.add(10, 47, 10, 14, 16, 0.5);
                            vaultGearModifierTiersBuilder.add(16, 57, 10, 16, 20, 0.5);
                            vaultGearModifierTiersBuilder.add(25, 63, 10, 20, 30, 0.5);
                            vaultGearModifierTiersBuilder.add(36, 71, 10, 30, 34, 0.5);
                            vaultGearModifierTiersBuilder.add(48, 82, 10, 34, 40, 0.5);
                            vaultGearModifierTiersBuilder.add(58, -1, 10, 40, 45, 0.5);
                            vaultGearModifierTiersBuilder.add(68, -1, 10, 45, 50, 0.5);
                            vaultGearModifierTiersBuilder.add(80, -1, 10, 50, 55, 0.5);
                            vaultGearModifierTiersBuilder.add(90, -1, 10, 55, 65, 0.5);
                            vaultGearModifierTiersBuilder.add(95, -1, 10, 65, 70, 0.5);
                            vaultGearModifierTiersBuilder.add(100, -1, 10, 70, 80, 0.5);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.ATTACK_DAMAGE, "BaseDamage", "trident_damage", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, 15, 10, 10, 16, 0.5);
                            vaultGearModifierTiersBuilder.add(10, 47, 10, 16, 20, 0.5);
                            vaultGearModifierTiersBuilder.add(16, 57, 10, 20, 24, 0.5);
                            vaultGearModifierTiersBuilder.add(25, 63, 10, 24, 32, 0.5);
                            vaultGearModifierTiersBuilder.add(36, 71, 10, 32, 38, 0.5);
                            vaultGearModifierTiersBuilder.add(48, 82, 10, 38, 44, 0.5);
                            vaultGearModifierTiersBuilder.add(58, -1, 10, 44, 54, 0.5);
                            vaultGearModifierTiersBuilder.add(68, -1, 10, 54, 68, 0.5);
                            vaultGearModifierTiersBuilder.add(80, -1, 10, 68, 74, 0.5);
                            vaultGearModifierTiersBuilder.add(90, -1, 10, 74, 84, 0.5);
                            vaultGearModifierTiersBuilder.add(95, -1, 10, 84, 95, 0.5);
                            vaultGearModifierTiersBuilder.add(100, -1, 10, 95, 115, 0.5);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(ModGearAttributes.TRIDENT_LOYALTY, "BaseLoyalty", "trident_loyalty_zeus", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, 40, 10, 1, 2, 1);
                            vaultGearModifierTiersBuilder.add(40, -1, 10, 2, 4, 1);
                            vaultGearModifierTiersBuilder.add(70, -1, 10, 4, 6, 1);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(ModGearAttributes.TRIDENT_LOYALTY, "BaseLoyalty", "trident_loyalty", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, -1, 10, 1, 2, 1);
                            vaultGearModifierTiersBuilder.add(40, -1, 10, 2, 3, 1);
                            vaultGearModifierTiersBuilder.add(70, -1, 10, 3, 4, 1);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(ModGearAttributes.TRIDENT_CHANNELING, "BaseChanneling", "trident_channeling", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, 40, 10, true);
                        });
            }).build();
            builder.key(VaultMod.id("unique")).add(VaultGearTierConfig.ModifierAffixTagGroup.PREFIX, vaultGearAttributeGroupBuilder -> {
                vaultGearAttributeGroupBuilder
                        .addModifier(ModGearAttributes.INCREASED_EFFECT_CLOUD_CHANCE, "BaseEffectCloudChance", "mod_effect_cloud_chance", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, 20, 10, 0.01F, 0.02F, 0.01F);
                            vaultGearModifierTiersBuilder.add(20, 40, 10, 0.02F, 0.03F, 0.01F);
                            vaultGearModifierTiersBuilder.add(40, -1, 10, 0.03F, 0.04F, 0.01F);
                            vaultGearModifierTiersBuilder.add(101, -1, 10, 0.05F, 0.09F, 0.01F);
                            vaultGearModifierTiersBuilder.add(102, -1, 10, 0.09F, 0.12F, 0.01F);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.TALENT_LEVEL, "ModPrimeAmpLevel", "mod_prime_amp_level", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, -1, 10, new TalentLevelAttribute.Config("Prime_Amplification", 1));
                            vaultGearModifierTiersBuilder.add(0, -1, 10, new TalentLevelAttribute.Config("Prime_Amplification", 2));
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.ABILITY_LEVEL, "ModEmpowerLevel", "mod_empower_level", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, -1, 10, new AbilityLevelAttribute.Config("Empower", 1));
                            vaultGearModifierTiersBuilder.add(0, -1, 10, new AbilityLevelAttribute.Config("Empower", 2));
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.ABILITY_LEVEL, "ModNovaLevel", "mod_nova_level", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, -1, 10, new AbilityLevelAttribute.Config("Nova", 1));
                            vaultGearModifierTiersBuilder.add(0, -1, 10, new AbilityLevelAttribute.Config("Nova", 2));
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.HIT_HEARTS, "ModHitHearts", "mod_hit_hearts_fork", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, 20, 10, 0.1F, 0.25F, 0.01F);
                            vaultGearModifierTiersBuilder.add(20, 40, 10, 0.2F, 0.35F, 0.01F);
                            vaultGearModifierTiersBuilder.add(40, -1, 10, 0.3F, 0.45F, 0.01F);
                            vaultGearModifierTiersBuilder.add(65, -1, 10, 0.4F, 0.55F, 0.01F);
                            vaultGearModifierTiersBuilder.add(85, -1, 10, 0.5F, 0.75F, 0.01F);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.DAMAGE_TANK, "ModTankDamage", "mod_tank_damage_fork", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, -1, 10, 0.5F, 1F, 0.01F);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.SOUL_QUANTITY_PERCENTILE, "ModSoulQuantity", "mod_soul_quantity_fork", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, -1, 10, 0.25F, 0.25F, 0F);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(ModGearAttributes.TRIDENT_WINDUP, "ModTridentWindup", "windup_time_zeus", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, 64, 10, 0.5F, 0.5F, 0F);
                            vaultGearModifierTiersBuilder.add(65, -1, 10, 0.5F, 0.75F, 0.01F);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(ModGearAttributes.SECOND_JUDGEMENT, "ModSecondJudgement", "second_judgement_zeus", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, -1, 10, 0.25F, 0.5F, 0.01F);
                            vaultGearModifierTiersBuilder.add(65, -1, 10, 0.5F, 0.75F, 0.01F);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(ModGearAttributes.CHANNELING_CHANCE, "ModChannelingChance", "channeling_chance_zeus", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, -1, 10, 0.25F, 0.5F, 0.01F);
                            vaultGearModifierTiersBuilder.add(65, -1, 10, 0.5F, 0.75F, 0.01F);
                            vaultGearModifierTiersBuilder.add(90, -1, 10, 0.75F, 1.0F, 0.01F);
                        });
            }).build();
            builder.key(VaultMod.id("unique")).add(VaultGearTierConfig.ModifierAffixTagGroup.SUFFIX, vaultGearAttributeGroupBuilder -> {
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.SHOCKING_HIT_CHANCE, "ModShockingHit", "shocking_hit_zeus", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, -1, 10, 0.25F, 0.25F, 0F);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.EFFECT_CLOUD, "ModEffectCloud", "slowness_cloud_zeus", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, -1, 10, "Slowness Cloud I", ResourceLocation.withDefaultNamespace("empty"), 120, 4.0f, MobEffects.MOVEMENT_SLOWDOWN.getColor(), false, 0.05F, MobEffects.MOVEMENT_SLOWDOWN.getRegistryName(), 140, 0);
                            vaultGearModifierTiersBuilder.add(0, -1, 10, "Slowness Cloud II", ResourceLocation.withDefaultNamespace("empty"), 160, 4.0f, MobEffects.MOVEMENT_SLOWDOWN.getColor(), false, 0.05F, MobEffects.MOVEMENT_SLOWDOWN.getRegistryName(), 140, 1);
                            vaultGearModifierTiersBuilder.add(0, -1, 10, "Slowness Cloud III", ResourceLocation.withDefaultNamespace("empty"), 200, 4.0f, MobEffects.MOVEMENT_SLOWDOWN.getColor(), false, 0.05F, MobEffects.MOVEMENT_SLOWDOWN.getRegistryName(), 140, 2);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.EFFECT_CLOUD, "ModEffectCloud", "mod_healing_cloud_fork", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, -1, 10, "Healing Cloud I", ResourceLocation.withDefaultNamespace("empty"), 80, 4.0f, MobEffects.HEAL.getColor(), false, 0.05F, MobEffects.HEAL.getRegistryName(), 20, 0);
                            vaultGearModifierTiersBuilder.add(0, -1, 10, "Healing Cloud II", ResourceLocation.withDefaultNamespace("empty"), 120, 4.0f, MobEffects.HEAL.getColor(), false, 0.05F, MobEffects.HEAL.getRegistryName(), 20, 0);
                            vaultGearModifierTiersBuilder.add(0, -1, 10, "Healing Cloud III", ResourceLocation.withDefaultNamespace("empty"), 160, 4.0f, MobEffects.HEAL.getColor(), false, 0.05F, MobEffects.HEAL.getRegistryName(), 20, 0);
                        });
            }).build();
        });

    }
}
