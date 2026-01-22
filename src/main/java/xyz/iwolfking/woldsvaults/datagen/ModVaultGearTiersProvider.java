package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.gear.attribute.ability.AbilityFloatValueAttribute;
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
                            vaultGearModifierTiersBuilder.add(0, -1, 10, true);
                        });
            }).build();
            builder.key(VaultMod.id("unique")).add(VaultGearTierConfig.ModifierAffixTagGroup.PREFIX, vaultGearAttributeGroupBuilder -> {
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
                        .addModifier(iskallia.vault.init.ModGearAttributes.ABILITY_LEVEL, "ModJavelinLevel", "mod_javelin_base_level", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, -1, 10, new AbilityLevelAttribute.Config("Javelin_Base", 1));
                            vaultGearModifierTiersBuilder.add(0, -1, 10, new AbilityLevelAttribute.Config("Javelin_Base", 2));
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.ABILITY_LEVEL, "ModImplodeLevel", "mod_implode_level", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, -1, 10, new AbilityLevelAttribute.Config("Implode", 1));
                            vaultGearModifierTiersBuilder.add(0, -1, 10, new AbilityLevelAttribute.Config("Implode", 2));
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(ModGearAttributes.IMPLODING_JAVELIN, "ModImplodingJavelin", "mod_imploding_javelin", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, -1, 10, true);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.ABILITY_COOLDOWN_PERCENT, "ModJavelinCooldownIncrease", "javelin_cooldown_increase", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.addPercentAbilityCooldown(0, -1, 10, new AbilityFloatValueAttribute.Config("Javelin_Base", 12F, 16F, 0.5F));
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.ABILITY_MANACOST_PERCENT, "ModJavelinManaIncrease", "javelin_mana_cost", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.addPercentManaCost(0, -1, 10, new AbilityFloatValueAttribute.Config("Javelin_Base", 1.5F, 3F, 0.01F));
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
                //Chroma Brew
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.EFFECT_CLOUD, "ModEffectCloud1", "mod_regen_cloud_brew", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, -1, 10, "Regeneration I", ResourceLocation.withDefaultNamespace("empty"), 120, 4.0f, MobEffects.REGENERATION.getColor(), true, 0.05F, MobEffects.REGENERATION.getRegistryName(), 120, 0);
                            vaultGearModifierTiersBuilder.add(0, -1, 10, "Regeneration II", ResourceLocation.withDefaultNamespace("empty"), 160, 4.0f, MobEffects.REGENERATION.getColor(), true, 0.05F, MobEffects.REGENERATION.getRegistryName(), 140, 0);
                            vaultGearModifierTiersBuilder.add(25, -1, 10, "Regeneration III", ResourceLocation.withDefaultNamespace("empty"), 200, 4.0f, MobEffects.REGENERATION.getColor(), true, 0.05F, MobEffects.REGENERATION.getRegistryName(), 160, 0);
                            vaultGearModifierTiersBuilder.add(50, -1, 10, "Regeneration IV", ResourceLocation.withDefaultNamespace("empty"), 240, 4.0f, MobEffects.REGENERATION.getColor(), true, 0.05F, MobEffects.REGENERATION.getRegistryName(), 180, 0);
                            vaultGearModifierTiersBuilder.add(75, -1, 10, "Regeneration V", ResourceLocation.withDefaultNamespace("empty"), 300, 4.0f, MobEffects.REGENERATION.getColor(), true, 0.05F, MobEffects.REGENERATION.getRegistryName(), 200, 0);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.EFFECT_CLOUD, "ModEffectCloud1", "mod_healing_cloud_brew", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, -1, 10, "Healing I", ResourceLocation.withDefaultNamespace("empty"), 80, 4.0f, MobEffects.HEAL.getColor(), true, 0.05F, MobEffects.HEAL.getRegistryName(), 20, 0);
                            vaultGearModifierTiersBuilder.add(0, -1, 10, "Healing II", ResourceLocation.withDefaultNamespace("empty"), 120, 4.0f, MobEffects.HEAL.getColor(), true, 0.05F, MobEffects.HEAL.getRegistryName(), 20, 0);
                            vaultGearModifierTiersBuilder.add(25, -1, 10, "Healing III", ResourceLocation.withDefaultNamespace("empty"), 160, 4.0f, MobEffects.HEAL.getColor(), true, 0.05F, MobEffects.HEAL.getRegistryName(), 20, 0);
                            vaultGearModifierTiersBuilder.add(50, -1, 10, "Healing IV", ResourceLocation.withDefaultNamespace("empty"), 200, 4.0f, MobEffects.HEAL.getColor(), true, 0.05F, MobEffects.HEAL.getRegistryName(), 20, 1);
                            vaultGearModifierTiersBuilder.add(75, -1, 10, "Healing V", ResourceLocation.withDefaultNamespace("empty"), 240, 4.0f, MobEffects.HEAL.getColor(), true, 0.05F, MobEffects.HEAL.getRegistryName(), 20, 2);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.EFFECT_CLOUD, "ModEffectCloud1", "mod_resistance_cloud_brew", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, -1, 10, "Resistance I", ResourceLocation.withDefaultNamespace("empty"), 80, 4.0f, MobEffects.DAMAGE_RESISTANCE.getColor(), true, 0.05F, MobEffects.DAMAGE_RESISTANCE.getRegistryName(), 120, 0);
                            vaultGearModifierTiersBuilder.add(0, -1, 10, "Resistance II", ResourceLocation.withDefaultNamespace("empty"), 120, 4.0f, MobEffects.DAMAGE_RESISTANCE.getColor(), true, 0.05F, MobEffects.DAMAGE_RESISTANCE.getRegistryName(), 160, 1);
                            vaultGearModifierTiersBuilder.add(25, -1, 10, "Resistance III", ResourceLocation.withDefaultNamespace("empty"), 160, 4.0f, MobEffects.DAMAGE_RESISTANCE.getColor(), true, 0.05F, MobEffects.DAMAGE_RESISTANCE.getRegistryName(), 200, 2);
                            vaultGearModifierTiersBuilder.add(50, -1, 10, "Resistance IV", ResourceLocation.withDefaultNamespace("empty"), 200, 4.0f, MobEffects.DAMAGE_RESISTANCE.getColor(), true, 0.05F, MobEffects.DAMAGE_RESISTANCE.getRegistryName(), 240, 3);
                            vaultGearModifierTiersBuilder.add(75, -1, 10, "Resistance V", ResourceLocation.withDefaultNamespace("empty"), 240, 4.0f, MobEffects.DAMAGE_RESISTANCE.getColor(), true, 0.05F, MobEffects.DAMAGE_RESISTANCE.getRegistryName(), 300, 4);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.EFFECT_CLOUD, "ModEffectCloud2", "mod_wither_cloud_brew", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, -1, 10, "Wither I", ResourceLocation.withDefaultNamespace("empty"), 120, 4.0f, MobEffects.WITHER.getColor(), false, 0.05F, MobEffects.WITHER.getRegistryName(), 120, 0);
                            vaultGearModifierTiersBuilder.add(0, -1, 10, "Wither II", ResourceLocation.withDefaultNamespace("empty"), 160, 4.0f, MobEffects.WITHER.getColor(), false, 0.05F, MobEffects.WITHER.getRegistryName(), 140, 1);
                            vaultGearModifierTiersBuilder.add(25, -1, 10, "Wither III", ResourceLocation.withDefaultNamespace("empty"), 200, 4.0f, MobEffects.WITHER.getColor(), false, 0.05F, MobEffects.WITHER.getRegistryName(), 160, 2);
                            vaultGearModifierTiersBuilder.add(50, -1, 10, "Wither IV", ResourceLocation.withDefaultNamespace("empty"), 240, 4.0f, MobEffects.WITHER.getColor(), false, 0.05F, MobEffects.WITHER.getRegistryName(), 180, 3);
                            vaultGearModifierTiersBuilder.add(75, -1, 10, "Wither V", ResourceLocation.withDefaultNamespace("empty"), 300, 4.0f, MobEffects.WITHER.getColor(), false, 0.05F, MobEffects.WITHER.getRegistryName(), 200, 4);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.EFFECT_CLOUD, "ModEffectCloud2", "mod_bleed_cloud_brew", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, -1, 10, "Bleed I", ResourceLocation.withDefaultNamespace("empty"), 120, 4.0f, ModEffects.BLEED.getColor(), false, 0.05F, ModEffects.BLEED.getRegistryName(), 120, 0);
                            vaultGearModifierTiersBuilder.add(0, -1, 10, "Bleed II", ResourceLocation.withDefaultNamespace("empty"), 160, 4.0f, ModEffects.BLEED.getColor(), false, 0.05F, ModEffects.BLEED.getRegistryName(), 140, 1);
                            vaultGearModifierTiersBuilder.add(25, -1, 10, "Bleed III", ResourceLocation.withDefaultNamespace("empty"), 200, 4.0f, ModEffects.BLEED.getColor(), false, 0.05F, ModEffects.BLEED.getRegistryName(), 160, 2);
                            vaultGearModifierTiersBuilder.add(50, -1, 10, "Bleed IV", ResourceLocation.withDefaultNamespace("empty"), 240, 4.0f, ModEffects.BLEED.getColor(), false, 0.05F, ModEffects.BLEED.getRegistryName(), 180, 3);
                            vaultGearModifierTiersBuilder.add(75, -1, 10, "Bleed V", ResourceLocation.withDefaultNamespace("empty"), 300, 4.0f, ModEffects.BLEED.getColor(), false, 0.05F, ModEffects.BLEED.getRegistryName(), 200, 4);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.EFFECT_CLOUD, "ModEffectCloud2", "mod_poison_cloud_brew", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, -1, 10, "Poison I", ResourceLocation.withDefaultNamespace("empty"), 120, 4.0f, MobEffects.POISON.getColor(), false, 0.05F, MobEffects.POISON.getRegistryName(), 120, 0);
                            vaultGearModifierTiersBuilder.add(0, -1, 10, "Poison II", ResourceLocation.withDefaultNamespace("empty"), 160, 4.0f, MobEffects.POISON.getColor(), false, 0.05F, MobEffects.POISON.getRegistryName(), 140, 1);
                            vaultGearModifierTiersBuilder.add(25, -1, 10, "Poison III", ResourceLocation.withDefaultNamespace("empty"), 200, 4.0f, MobEffects.POISON.getColor(), false, 0.05F, MobEffects.POISON.getRegistryName(), 160, 2);
                            vaultGearModifierTiersBuilder.add(50, -1, 10, "Poison IV", ResourceLocation.withDefaultNamespace("empty"), 240, 4.0f, MobEffects.POISON.getColor(), false, 0.05F, MobEffects.POISON.getRegistryName(), 180, 3);
                            vaultGearModifierTiersBuilder.add(75, -1, 10, "Poison V", ResourceLocation.withDefaultNamespace("empty"), 300, 4.0f, MobEffects.POISON.getColor(), false, 0.05F, MobEffects.POISON.getRegistryName(), 200, 4);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.EFFECT_CLOUD, "ModEffectCloud3", "mod_slowness_cloud_brew", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, -1, 10, "Slowness I", ResourceLocation.withDefaultNamespace("empty"), 120, 4.0f, MobEffects.MOVEMENT_SLOWDOWN.getColor(), false, 0.05F, MobEffects.MOVEMENT_SLOWDOWN.getRegistryName(), 120, 0);
                            vaultGearModifierTiersBuilder.add(0, -1, 10, "Slowness II", ResourceLocation.withDefaultNamespace("empty"), 160, 4.0f, MobEffects.MOVEMENT_SLOWDOWN.getColor(), false, 0.05F, MobEffects.MOVEMENT_SLOWDOWN.getRegistryName(), 140, 1);
                            vaultGearModifierTiersBuilder.add(25, -1, 10, "Slowness III", ResourceLocation.withDefaultNamespace("empty"), 200, 4.0f, MobEffects.MOVEMENT_SLOWDOWN.getColor(), false, 0.05F, MobEffects.MOVEMENT_SLOWDOWN.getRegistryName(), 160, 2);
                            vaultGearModifierTiersBuilder.add(50, -1, 10, "Slowness IV", ResourceLocation.withDefaultNamespace("empty"), 240, 4.0f, MobEffects.MOVEMENT_SLOWDOWN.getColor(), false, 0.05F, MobEffects.MOVEMENT_SLOWDOWN.getRegistryName(), 180, 3);
                            vaultGearModifierTiersBuilder.add(75, -1, 10, "Slowness V", ResourceLocation.withDefaultNamespace("empty"), 300, 4.0f, MobEffects.MOVEMENT_SLOWDOWN.getColor(), false, 0.05F, MobEffects.MOVEMENT_SLOWDOWN.getRegistryName(), 200, 4);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.EFFECT_CLOUD, "ModEffectCloud3", "mod_unluck_cloud_brew", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, -1, 10, "Unluck I", ResourceLocation.withDefaultNamespace("empty"), 120, 4.0f, MobEffects.UNLUCK.getColor(), false, 0.05F, MobEffects.UNLUCK.getRegistryName(), 120, 0);
                            vaultGearModifierTiersBuilder.add(0, -1, 10, "Unluck II", ResourceLocation.withDefaultNamespace("empty"), 160, 4.0f, MobEffects.UNLUCK.getColor(), false, 0.05F, MobEffects.UNLUCK.getRegistryName(), 160, 0);
                            vaultGearModifierTiersBuilder.add(25, -1, 10, "Unluck III", ResourceLocation.withDefaultNamespace("empty"), 200, 4.0f, MobEffects.UNLUCK.getColor(), false, 0.05F, MobEffects.UNLUCK.getRegistryName(), 200, 0);
                            vaultGearModifierTiersBuilder.add(50, -1, 10, "Unluck IV", ResourceLocation.withDefaultNamespace("empty"), 240, 4.0f, MobEffects.UNLUCK.getColor(), false, 0.05F, MobEffects.UNLUCK.getRegistryName(), 240, 0);
                            vaultGearModifierTiersBuilder.add(75, -1, 10, "Unluck V", ResourceLocation.withDefaultNamespace("empty"), 300, 4.0f, MobEffects.UNLUCK.getColor(), false, 0.05F, MobEffects.UNLUCK.getRegistryName(), 300, 0);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.EFFECT_CLOUD, "ModEffectCloud3", "mod_chilling_cloud_brew", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, -1, 10, "Chilling I", ResourceLocation.withDefaultNamespace("empty"), 120, 4.0f, ModEffects.CHILLED.getColor(), false, 0.05F, ModEffects.CHILLED.getRegistryName(), 120, 0);
                            vaultGearModifierTiersBuilder.add(0, -1, 10, "Chilling II", ResourceLocation.withDefaultNamespace("empty"), 160, 4.0f, ModEffects.CHILLED.getColor(), false, 0.05F, ModEffects.CHILLED.getRegistryName(), 140, 1);
                            vaultGearModifierTiersBuilder.add(25, -1, 10, "Chilling III", ResourceLocation.withDefaultNamespace("empty"), 200, 4.0f, ModEffects.CHILLED.getColor(), false, 0.05F, ModEffects.CHILLED.getRegistryName(), 160, 2);
                            vaultGearModifierTiersBuilder.add(50, -1, 10, "Chilling IV", ResourceLocation.withDefaultNamespace("empty"), 240, 4.0f, ModEffects.CHILLED.getColor(), false, 0.05F, ModEffects.CHILLED.getRegistryName(), 180, 3);
                            vaultGearModifierTiersBuilder.add(75, -1, 10, "Chilling V", ResourceLocation.withDefaultNamespace("empty"), 300, 4.0f, ModEffects.CHILLED.getColor(), false, 0.05F, ModEffects.CHILLED.getRegistryName(), 200, 4);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.EFFECT_CLOUD, "ModEffectCloud3", "mod_weakness_cloud_brew", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, -1, 10, "Weakness I", ResourceLocation.withDefaultNamespace("empty"), 120, 4.0f, MobEffects.WEAKNESS.getColor(), false, 0.05F, MobEffects.WEAKNESS.getRegistryName(), 120, 0);
                            vaultGearModifierTiersBuilder.add(0, -1, 10, "Weakness II", ResourceLocation.withDefaultNamespace("empty"), 160, 4.0f, MobEffects.WEAKNESS.getColor(), false, 0.05F, MobEffects.WEAKNESS.getRegistryName(), 160, 0);
                            vaultGearModifierTiersBuilder.add(25, -1, 10, "Weakness III", ResourceLocation.withDefaultNamespace("empty"), 200, 4.0f, MobEffects.WEAKNESS.getColor(), false, 0.05F, MobEffects.WEAKNESS.getRegistryName(), 200, 0);
                            vaultGearModifierTiersBuilder.add(50, -1, 10, "Weakness IV", ResourceLocation.withDefaultNamespace("empty"), 240, 4.0f, MobEffects.WEAKNESS.getColor(), false, 0.05F, MobEffects.WEAKNESS.getRegistryName(), 240, 0);
                            vaultGearModifierTiersBuilder.add(75, -1, 10, "Weakness V", ResourceLocation.withDefaultNamespace("empty"), 300, 4.0f, MobEffects.WEAKNESS.getColor(), false, 0.05F, MobEffects.WEAKNESS.getRegistryName(), 300, 0);
                        });
            }).build();
            builder.key(VaultMod.id("unique")).add(VaultGearTierConfig.ModifierAffixTagGroup.SUFFIX, vaultGearAttributeGroupBuilder -> {
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.SHOCKING_HIT_CHANCE, "ModShockingHit", "shocking_hit_zeus", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, -1, 10, 0.25F, 0.25F, 0F);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.EFFECT_CLOUD, "ModEffectCloud", "slowness_cloud_zeus", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, -1, 10, "Slowness I", ResourceLocation.withDefaultNamespace("empty"), 120, 4.0f, MobEffects.MOVEMENT_SLOWDOWN.getColor(), false, 0.05F, MobEffects.MOVEMENT_SLOWDOWN.getRegistryName(), 140, 0);
                            vaultGearModifierTiersBuilder.add(0, -1, 10, "Slowness II", ResourceLocation.withDefaultNamespace("empty"), 160, 4.0f, MobEffects.MOVEMENT_SLOWDOWN.getColor(), false, 0.05F, MobEffects.MOVEMENT_SLOWDOWN.getRegistryName(), 140, 1);
                            vaultGearModifierTiersBuilder.add(0, -1, 10, "Slowness III", ResourceLocation.withDefaultNamespace("empty"), 200, 4.0f, MobEffects.MOVEMENT_SLOWDOWN.getColor(), false, 0.05F, MobEffects.MOVEMENT_SLOWDOWN.getRegistryName(), 140, 2);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.EFFECT_CLOUD, "ModEffectCloud", "mod_healing_cloud_fork", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, -1, 10, "Healing I", ResourceLocation.withDefaultNamespace("empty"), 80, 4.0f, MobEffects.HEAL.getColor(), true, 0.05F, MobEffects.HEAL.getRegistryName(), 20, 0);
                            vaultGearModifierTiersBuilder.add(0, -1, 10, "Healing II", ResourceLocation.withDefaultNamespace("empty"), 120, 4.0f, MobEffects.HEAL.getColor(), true, 0.05F, MobEffects.HEAL.getRegistryName(), 20, 0);
                            vaultGearModifierTiersBuilder.add(0, -1, 10, "Healing III", ResourceLocation.withDefaultNamespace("empty"), 160, 4.0f, MobEffects.HEAL.getColor(), true, 0.05F, MobEffects.HEAL.getRegistryName(), 20, 0);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(ModGearAttributes.INCREASED_EFFECT_CLOUD_CHANCE, "ModEffectCloudChance", "mod_effect_cloud_chance", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, 20, 10, 0.01F, 0.02F, 0.01F);
                            vaultGearModifierTiersBuilder.add(20, 40, 10, 0.02F, 0.03F, 0.01F);
                            vaultGearModifierTiersBuilder.add(40, -1, 10, 0.03F, 0.04F, 0.01F);
                            vaultGearModifierTiersBuilder.add(65, -1, 10, 0.05F, 0.09F, 0.01F);
                            vaultGearModifierTiersBuilder.add(85, -1, 10, 0.09F, 0.12F, 0.01F);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.ABILITY_LEVEL, "ModDiffuseLevel", "mod_diffuse_level", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, -1, 10, new AbilityLevelAttribute.Config("Expunge_Base", 1));
                            vaultGearModifierTiersBuilder.add(0, -1, 10, new AbilityLevelAttribute.Config("Expunge_Base", 2));
                            vaultGearModifierTiersBuilder.add(50, -1, 10, new AbilityLevelAttribute.Config("Expunge_Base", 3));
                            vaultGearModifierTiersBuilder.add(75, -1, 10, new AbilityLevelAttribute.Config("Expunge_Base", 4));
                        });
            }).build();
        });

    }
}
