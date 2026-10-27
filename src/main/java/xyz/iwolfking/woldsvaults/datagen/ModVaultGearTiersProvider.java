package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import iskallia.vault.config.TalentsGUIConfig;
import iskallia.vault.config.entry.IntRollRangeEntry;
import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.gear.attribute.ability.AbilityFloatValueAttribute;
import iskallia.vault.gear.attribute.ability.AbilityLevelAttribute;
import iskallia.vault.gear.attribute.talent.TalentLevelAttribute;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModEffects;
import iskallia.vault.init.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import xyz.iwolfking.woldsvaults.api.data.gear.lib.UnusualModifierLib;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractWoldsVaultGearConfigProvider;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;
import java.util.List;

public class ModVaultGearTiersProvider extends AbstractWoldsVaultGearConfigProvider {
    public ModVaultGearTiersProvider(DataGenerator generator) {
        super(generator);
    }

    @Override
    public void registerConfigs() {
        add("battlestaff", builder -> {
           builder.add(VaultGearTierConfig.ModifierAffixTagGroup.IMPLICIT, vaultGearAttributeGroupBuilder -> {
              vaultGearAttributeGroupBuilder.addModifier(ModGearAttributes.DAMAGE_BURST_COMBO, "BaseCombo", "base_magic_burst_combo", List.of(), vaultGearModifierTiersBuilder -> {
                  addDamageBurstCombo(vaultGearModifierTiersBuilder, 0, -1, 100, xyz.iwolfking.woldsvaults.init.ModEffects.MAGIC_BURST_COMBO.getRegistryName(), 160, iskallia.vault.init.ModGearAttributes.ABILITY_POWER.getRegistryName(), 4, 6, 1, 0.5F, 1.0F, 0.1F);
              });
               vaultGearAttributeGroupBuilder.addModifier(ModGearAttributes.INVOKE_ABILITY_COMBO, "BaseCombo", "base_invoke_frost_nova", List.of(), vaultGearModifierTiersBuilder -> {
                   addInvokeComboAttribute(vaultGearModifierTiersBuilder, 0, -1, 100, xyz.iwolfking.woldsvaults.init.ModEffects.CHILLING_COMBO.getRegistryName(), 160, "Nova_Slow", 4, 6, 1, 1, 3, 1);
               });
           });
        });
        add("scepter", builder -> {
            builder.add(VaultGearTierConfig.ModifierAffixTagGroup.BASE_ATTRIBUTES, vaultGearAttributeGroupBuilder -> {
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.DURABILITY, "BaseDurability", "base_durability", List.of("resilientFocusTarget"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, 40, 10, 3425, 4225, 1);
                    vaultGearModifierTiersBuilder.add(8, 70, 10, 4226, 5225, 1);
                    vaultGearModifierTiersBuilder.add(25, -1, 10, 5226, 6325, 1);
                    vaultGearModifierTiersBuilder.add(50, -1, 10, 6826, 8325, 1);
                    vaultGearModifierTiersBuilder.add(70, -1, 10, 8576, 10375, 1);
                });
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.SOULBOUND, "BaseSoulbound", "base_soulbound", List.of(), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, -1, 20, true);
                });
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.SOULBOUND, "BaseSoulbound", "base_no_soulbound", List.of(), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, -1, 80, false);
                });
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.LIVING, "BaseLiving", "base_no_living", List.of(), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, -1, 98, false);
                });
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.LIVING, "BaseLiving", "base_living", List.of(), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, -1, 2, true);
                });
            });
            builder.add(VaultGearTierConfig.ModifierAffixTagGroup.IMPLICIT, vaultGearAttributeGroupBuilder -> {
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.ABILITY_POWER, "BaseAbilityPower", "base_ability_power", List.of(), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, 15, 10, 9, 15, 1);
                    vaultGearModifierTiersBuilder.add(10, 47, 10, 16, 24, 1);
                    vaultGearModifierTiersBuilder.add(16, 57, 10, 24, 32, 1);
                    vaultGearModifierTiersBuilder.add(25, 63, 10, 32, 42, 1);
                    vaultGearModifierTiersBuilder.add(48, 82, 10, 42, 55, 1);
                    vaultGearModifierTiersBuilder.add(58, -1, 10, 55, 70, 1);
                    vaultGearModifierTiersBuilder.add(68, -1, 10, 70, 80, 1);
                    vaultGearModifierTiersBuilder.add(80, -1, 10, 80, 90, 1);
                    vaultGearModifierTiersBuilder.add(90, -1, 10, 90, 105, 1);
                    vaultGearModifierTiersBuilder.add(95, -1, 10, 105, 110, 1);
                    vaultGearModifierTiersBuilder.add(100, -1, 10, 110, 120, 1);
                });
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.ATTACK_SPEED, "BaseAttackSpeed", "base_attack_speed", List.of(), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, -1, 10, -2.4, -2.3, 0.01);
                    vaultGearModifierTiersBuilder.add(50, -1, 10, -2.3, -2.2, 0.01);
                });
                vaultGearAttributeGroupBuilder.addModifier(ModGearAttributes.SCEPTER_SPARKLES, "ModScepterType", "base_sparkles_type", List.of(), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, -1, 20, true);
                });
                vaultGearAttributeGroupBuilder.addModifier(ModGearAttributes.SCEPTER_BEAM, "ModScepterType", "base_beam_type", List.of(), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, -1, 20, true);
                });
                vaultGearAttributeGroupBuilder.addModifier(ModGearAttributes.SCEPTER_INVOKE, "ModScepterType", "base_invoke_type_fireball", List.of(), vaultGearModifierTiersBuilder -> {
                   addScepterInvoke(vaultGearModifierTiersBuilder, 0, 30, 4, "Fireball_Base", 1, 4, 1, 60, 100, 10);
                   addScepterInvoke(vaultGearModifierTiersBuilder, 23, 56, 4, "Fireball_Base", 3, 7, 1, 55, 95, 10);
                   addScepterInvoke(vaultGearModifierTiersBuilder, 54, 90, 4, "Fireball_Base", 5, 9, 1, 50, 90, 10);
                   addScepterInvoke(vaultGearModifierTiersBuilder, 89, -1, 4, "Fireball_Base", 6, 12, 1, 45, 85, 10);
                });
                vaultGearAttributeGroupBuilder.addModifier(ModGearAttributes.SCEPTER_INVOKE, "ModScepterType", "base_invoke_type_nova", List.of(), vaultGearModifierTiersBuilder -> {
                    addScepterInvoke(vaultGearModifierTiersBuilder, 0, 30, 4, "Nova_Base", 1, 4, 1, 40, 80, 10);
                    addScepterInvoke(vaultGearModifierTiersBuilder, 23, 56, 4, "Nova_Base", 3, 7, 1, 40, 80, 10);
                    addScepterInvoke(vaultGearModifierTiersBuilder, 54, 90, 4, "Nova_Base", 5, 9, 1, 35, 75, 10);
                    addScepterInvoke(vaultGearModifierTiersBuilder, 89, -1, 4, "Nova_Base", 6, 12, 1, 35, 75, 10);
                });
                vaultGearAttributeGroupBuilder.addModifier(ModGearAttributes.SCEPTER_INVOKE, "ModScepterType", "base_invoke_type_implode", List.of(), vaultGearModifierTiersBuilder -> {
                    addScepterInvoke(vaultGearModifierTiersBuilder, 0, 30, 4, "Mana_Shield_Implode", 1, 4, 1, 50, 80, 10);
                    addScepterInvoke(vaultGearModifierTiersBuilder, 23, 56, 4, "Mana_Shield_Implode", 5, 8, 1, 50, 80, 10);
                    addScepterInvoke(vaultGearModifierTiersBuilder, 54, 90, 4, "Mana_Shield_Implode", 8, 11, 1, 50, 80, 10);
                    addScepterInvoke(vaultGearModifierTiersBuilder, 89, -1, 4, "Mana_Shield_Implode", 9, 16, 1, 50, 80, 10);
                });
                vaultGearAttributeGroupBuilder.addModifier(ModGearAttributes.SCEPTER_INVOKE, "ModScepterType", "base_invoke_type_arcane_prism", List.of(), vaultGearModifierTiersBuilder -> {
                    addScepterInvoke(vaultGearModifierTiersBuilder, 0, 30, 4, "Arcane_Prism", 1, 4, 1, 60, 100, 10);
                    addScepterInvoke(vaultGearModifierTiersBuilder, 23, 56, 4, "Arcane_Prism", 3, 7, 1, 55, 95, 10);
                    addScepterInvoke(vaultGearModifierTiersBuilder, 54, 90, 4, "Arcane_Prism", 5, 9, 1, 50, 90, 10);
                    addScepterInvoke(vaultGearModifierTiersBuilder, 89, -1, 4, "Arcane_Prism", 6, 12, 1, 45, 85, 10);
                });
                vaultGearAttributeGroupBuilder.addModifier(ModGearAttributes.SCEPTER_INVOKE, "ModScepterType", "base_invoke_type_lightning_ball", List.of(), vaultGearModifierTiersBuilder -> {
                    addScepterInvoke(vaultGearModifierTiersBuilder, 0, 30, 4, "Chain_Lightning_Orbs", 1, 4, 1, 50, 80, 10);
                    addScepterInvoke(vaultGearModifierTiersBuilder, 23, 56, 4, "Chain_Lightning_Orbs", 5, 8, 1, 50, 80, 10);
                    addScepterInvoke(vaultGearModifierTiersBuilder, 54, 90, 4, "Chain_Lightning_Orbs", 8, 11, 1, 50, 80, 10);
                    addScepterInvoke(vaultGearModifierTiersBuilder, 89, -1, 4, "Chain_Lightning_Orbs", 9, 16, 1, 50, 80, 10);
                });
                vaultGearAttributeGroupBuilder.addModifier(ModGearAttributes.SCEPTER_INVOKE, "ModScepterType", "base_invoke_type_charged_bolts", List.of(), vaultGearModifierTiersBuilder -> {
                    addScepterInvoke(vaultGearModifierTiersBuilder, 0, 30, 4, "Chain_Lightning_Charged_Bolts", 1, 4, 1, 40, 70, 10);
                    addScepterInvoke(vaultGearModifierTiersBuilder, 23, 56, 4, "Chain_Lightning_Charged_Bolts", 5, 8, 1, 35, 70, 10);
                    addScepterInvoke(vaultGearModifierTiersBuilder, 54, 90, 4, "Chain_Lightning_Charged_Bolts", 8, 11, 1, 30, 70, 10);
                    addScepterInvoke(vaultGearModifierTiersBuilder, 89, -1, 4, "Chain_Lightning_Charged_Bolts", 9, 16, 1, 30, 65, 10);
                });
                vaultGearAttributeGroupBuilder.addModifier(ModGearAttributes.SCEPTER_INVOKE, "ModScepterType", "base_invoke_type_necromancy", List.of(), vaultGearModifierTiersBuilder -> {
                    addScepterInvoke(vaultGearModifierTiersBuilder, 0, 30, 4, "Necromancy_Base", 1, 4, 1, 40, 70, 10);
                    addScepterInvoke(vaultGearModifierTiersBuilder, 23, 56, 4, "Necromancy_Base", 5, 8, 1, 35, 70, 10);
                    addScepterInvoke(vaultGearModifierTiersBuilder, 54, 90, 4, "Necromancy_Base", 8, 11, 1, 30, 70, 10);
                    addScepterInvoke(vaultGearModifierTiersBuilder, 89, -1, 4, "Necromancy_Base", 9, 16, 1, 30, 65, 10);
                });
                //TODO: Rework this into "Magibash" implicit
                vaultGearAttributeGroupBuilder.addModifier(ModGearAttributes.AP_SCALING_DAMAGE, "ModScepterType", "base_ap_scaling_damage", List.of(), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, 60, 12, 0.28F, 0.35F, 0.01F);
                    vaultGearModifierTiersBuilder.add(50, -1, 12, 0.36, 0.45F, 0.01F);
                    vaultGearModifierTiersBuilder.add(75, -1, 12, 0.45, 0.65F, 0.01F);
                });
            });
            builder.add(VaultGearTierConfig.ModifierAffixTagGroup.PREFIX, vaultGearAttributeGroupBuilder -> {
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.ABILITY_POWER, "ModOnHitAddition", "mod_ability_power", List.of("focusAbilityDamage"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, 49, 10, 2, 6, 1);
                    vaultGearModifierTiersBuilder.add(25, 74, 10, 7, 12, 1);
                    vaultGearModifierTiersBuilder.add(50, -1, 10, 12, 17, 1);
                    vaultGearModifierTiersBuilder.add(75, -1, 10, 17, 25, 1);
                    vaultGearModifierTiersBuilder.add(100, -1, 10, 25, 30, 1);
                    vaultGearModifierTiersBuilder.add(101, -1, 10, 30, 40, 1);
                    vaultGearModifierTiersBuilder.add(102, -1, 10, 40, 65, 1);
                });
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.ABILITY_POWER_PERCENTILE, "ModOnHitAddition", "mod_ability_power_percent", List.of("focusAbilityDamage"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, 86, 10, 0.04, 0.07, 0.01);
                    vaultGearModifierTiersBuilder.add(42, -1, 10, 0.08, 0.12, 0.01);
                    vaultGearModifierTiersBuilder.add(68, -1, 10, 0.13, 0.15, 0.01);
                    vaultGearModifierTiersBuilder.add(87, -1, 10, 0.16, 0.2, 0.01);
                    vaultGearModifierTiersBuilder.add(101, -1, 10, 0.21, 0.25, 0.01);
                    vaultGearModifierTiersBuilder.add(102, -1, 10, 0.26, 0.3, 0.01);
                });
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.SOUL_QUANTITY, "ModSoulChance", "mod_soul_chance", List.of("focusSoulQuantity"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, -1, 10, 0.1, 0.2, 0.01);
                    vaultGearModifierTiersBuilder.add(28, -1, 10, 0.21, 0.3, 0.01);
                    vaultGearModifierTiersBuilder.add(45, -1, 10, 0.31, 0.4, 0.01);
                    vaultGearModifierTiersBuilder.add(75, -1, 10, 0.41, 0.5, 0.01);
                    vaultGearModifierTiersBuilder.add(101, -1, 10, 0.51, 0.7, 0.01);
                    vaultGearModifierTiersBuilder.add(102, -1, 10, 0.71, 1.0, 0.01);
                });
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.AREA_OF_EFFECT, "ModEffectRadius", "mod_effect_radius", List.of("focusEffectRadius"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, 62, 10, 0.04, 0.08, 0.01);
                    vaultGearModifierTiersBuilder.add(40, -1, 10, 0.09, 0.14, 0.01);
                    vaultGearModifierTiersBuilder.add(63, -1, 10, 0.15, 0.2, 0.01);
                    vaultGearModifierTiersBuilder.add(89, -1, 10, 0.21, 0.25, 0.01);
                    vaultGearModifierTiersBuilder.add(101, -1, 10, 0.26, 0.34, 0.01);
                    vaultGearModifierTiersBuilder.add(102, -1, 10, 0.35, 0.45, 0.01);
                });
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.HEALING_EFFECTIVENESS, "ModHealth", "mod_healing_effect", List.of("focusHealingEff"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, 62, 10, 0.05, 0.1, 0.01);
                    vaultGearModifierTiersBuilder.add(22, -1, 10, 0.11, 0.18, 0.01);
                    vaultGearModifierTiersBuilder.add(43, -1, 10, 0.19, 0.26, 0.01);
                    vaultGearModifierTiersBuilder.add(70, -1, 10, 0.27, 0.35, 0.01);
                    vaultGearModifierTiersBuilder.add(101, -1, 10, 0.36, 0.5, 0.01);
                    vaultGearModifierTiersBuilder.add(102, -1, 10, 0.51, 0.7, 0.01);
                });
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.COOLDOWN_REDUCTION, "ModCooldownReduction", "mod_cooldown_reduction", List.of("focusCooldown"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, 62, 10, 0.05, 0.1, 0.01);
                    vaultGearModifierTiersBuilder.add(22, -1, 10, 0.11, 0.15, 0.01);
                    vaultGearModifierTiersBuilder.add(43, -1, 10, 0.16, 0.22, 0.01);
                    vaultGearModifierTiersBuilder.add(70, -1, 10, 0.23, 0.27, 0.01);
                    vaultGearModifierTiersBuilder.add(101, -1, 10, 0.28, 0.34, 0.01);
                    vaultGearModifierTiersBuilder.add(102, -1, 10, 0.35, 0.45, 0.01);
                });
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.DAMAGE_CHAMPION, "ModDamageChampion", "mod_damage_champion", List.of("focusDamageChampion"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, 79, 10, 0.05, 0.1, 0.01);
                    vaultGearModifierTiersBuilder.add(36, -1, 10, 0.11, 0.15, 0.01);
                    vaultGearModifierTiersBuilder.add(62, -1, 10, 0.21, 0.25, 0.01);
                    vaultGearModifierTiersBuilder.add(80, -1, 10, 0.26, 0.3, 0.01);
                    vaultGearModifierTiersBuilder.add(101, -1, 10, 0.31, 0.4, 0.01);
                    vaultGearModifierTiersBuilder.add(102, -1, 10, 0.41, 0.5, 0.01);
                });
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.DAMAGE_DUNGEON, "ModDamageDungeon", "mod_damage_dungeon", List.of("focusDamageDungeon"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, 79, 10, 0.05, 0.1, 0.01);
                    vaultGearModifierTiersBuilder.add(36, -1, 10, 0.11, 0.15, 0.01);
                    vaultGearModifierTiersBuilder.add(62, -1, 10, 0.21, 0.25, 0.01);
                    vaultGearModifierTiersBuilder.add(80, -1, 10, 0.26, 0.3, 0.01);
                    vaultGearModifierTiersBuilder.add(101, -1, 10, 0.31, 0.4, 0.01);
                    vaultGearModifierTiersBuilder.add(102, -1, 10, 0.41, 0.5, 0.01);
                });
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.DAMAGE_HORDE, "ModDamageHorde", "mod_damage_horde", List.of("focusDamageHorde"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, 79, 10, 0.05, 0.1, 0.01);
                    vaultGearModifierTiersBuilder.add(36, -1, 10, 0.11, 0.15, 0.01);
                    vaultGearModifierTiersBuilder.add(62, -1, 10, 0.21, 0.25, 0.01);
                    vaultGearModifierTiersBuilder.add(80, -1, 10, 0.26, 0.3, 0.01);
                    vaultGearModifierTiersBuilder.add(101, -1, 10, 0.31, 0.4, 0.01);
                    vaultGearModifierTiersBuilder.add(102, -1, 10, 0.41, 0.5, 0.01);
                });
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.DAMAGE_ASSASSIN, "ModDamageAssassin", "mod_damage_assassin", List.of("focusDamageAssassin"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, 79, 10, 0.05, 0.1, 0.01);
                    vaultGearModifierTiersBuilder.add(36, -1, 10, 0.11, 0.15, 0.01);
                    vaultGearModifierTiersBuilder.add(62, -1, 10, 0.21, 0.25, 0.01);
                    vaultGearModifierTiersBuilder.add(80, -1, 10, 0.26, 0.3, 0.01);
                    vaultGearModifierTiersBuilder.add(101, -1, 10, 0.31, 0.4, 0.01);
                    vaultGearModifierTiersBuilder.add(102, -1, 10, 0.41, 0.5, 0.01);
                });
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.DAMAGE_TANK, "ModDamageTank", "mod_damage_tank", List.of("focusDamageTank"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, 79, 10, 0.05, 0.1, 0.01);
                    vaultGearModifierTiersBuilder.add(36, -1, 10, 0.11, 0.15, 0.01);
                    vaultGearModifierTiersBuilder.add(62, -1, 10, 0.21, 0.25, 0.01);
                    vaultGearModifierTiersBuilder.add(80, -1, 10, 0.26, 0.3, 0.01);
                    vaultGearModifierTiersBuilder.add(101, -1, 10, 0.31, 0.4, 0.01);
                    vaultGearModifierTiersBuilder.add(102, -1, 10, 0.41, 0.5, 0.01);
                });
            });
            builder.add(VaultGearTierConfig.ModifierAffixTagGroup.SUFFIX, vaultGearAttributeGroupBuilder -> {
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.ATTACK_SPEED, "ModAttackType", "mod_attack_speed_percent", List.of("focusAttackSpeed"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, -1, 10, 0.05, 0.1, 0.01);
                    vaultGearModifierTiersBuilder.add(36, -1, 10, 0.11, 0.15, 0.01);
                    vaultGearModifierTiersBuilder.add(65, -1, 10, 0.16, 0.2, 0.01);
                    vaultGearModifierTiersBuilder.add(96, -1, 10, 0.21, 0.25, 0.01);
                    vaultGearModifierTiersBuilder.add(101, -1, 10, 0.26, 3, 0.01);
                    vaultGearModifierTiersBuilder.add(102, -1, 10, 0.31, 0.35, 0.01);
                });
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.ATTACK_RANGE, "ModAttackType", "mod_attack_range", List.of("focusAttackRange"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, -1, 10, 0.1, 0.2, 0.01);
                    vaultGearModifierTiersBuilder.add(20, -1, 10, 0.21, 0.3, 0.01);
                    vaultGearModifierTiersBuilder.add(45, -1, 10, 0.31, 0.4, 0.01);
                    vaultGearModifierTiersBuilder.add(75, -1, 10, 0.41, 0.5, 0.01);
                    vaultGearModifierTiersBuilder.add(101, -1, 10, 0.5, 0.65, 0.01);
                    vaultGearModifierTiersBuilder.add(102, -1, 10, 0.66, 1.0, 0.01);
                });
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.MANA_REGEN_ADDITIVE_PERCENTILE, "ModManaRegen", "mod_mana_regen", List.of("focusManaRegen"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, 54, 10, 0.1, 0.2, 0.01);
                    vaultGearModifierTiersBuilder.add(23, 66, 10, 0.21, 0.3, 0.01);
                    vaultGearModifierTiersBuilder.add(34, 80, 10, 0.31, 0.4, 0.01);
                    vaultGearModifierTiersBuilder.add(46, -1, 10, 0.41, 0.5, 0.01);
                    vaultGearModifierTiersBuilder.add(55, -1, 10, 0.51, 0.6, 0.01);
                    vaultGearModifierTiersBuilder.add(67, -1, 10, 0.61, 0.7, 0.01);
                    vaultGearModifierTiersBuilder.add(81, -1, 10, 0.71, 0.8, 0.01);
                    vaultGearModifierTiersBuilder.add(92, -1, 10, 0.81, 0.9, 0.01);
                    vaultGearModifierTiersBuilder.add(101, -1, 10, 0.91, 1.2, 0.01);
                    vaultGearModifierTiersBuilder.add(102, -1, 10, 1.21, 1.45, 0.01);
                });
                vaultGearAttributeGroupBuilder.addModifier(ModGearAttributes.ECHOING_CHANCE, "ModElementType", "mod_echoing_chance", List.of("focusEchoingChance"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(50, 85, 10, 0.03, 0.06, 0.01);
                    vaultGearModifierTiersBuilder.add(60, -1, 10, 0.6, 0.1, 0.01);
                    vaultGearModifierTiersBuilder.add(70, -1, 10, 0.11, 0.15, 0.01);
                    vaultGearModifierTiersBuilder.add(90, -1, 10, 0.16, 0.2, 0.01);
                    vaultGearModifierTiersBuilder.add(101, -1, 10, 0.21, 0.25, 0.01);
                    vaultGearModifierTiersBuilder.add(102, -1, 10, 0.26, 0.3, 0.01);
                });
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.ON_HIT_STUN, "ModElementType", "mod_stun_chance", List.of("focusStunChance"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(50, 85, 10, 0.03, 0.06, 0.01);
                    vaultGearModifierTiersBuilder.add(60, -1, 10, 0.6, 0.1, 0.01);
                    vaultGearModifierTiersBuilder.add(70, -1, 10, 0.11, 0.15, 0.01);
                    vaultGearModifierTiersBuilder.add(90, -1, 10, 0.16, 0.2, 0.01);
                    vaultGearModifierTiersBuilder.add(101, -1, 10, 0.21, 0.25, 0.01);
                    vaultGearModifierTiersBuilder.add(102, -1, 10, 0.26, 0.3, 0.01);
                });
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.EFFECT_DURATION, "ModEffectDuration", "mod_effect_duration", List.of("focusEffectDuration"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, 85, 10, 0.05, 0.1, 0.01);
                    vaultGearModifierTiersBuilder.add(32, -1, 10, 0.11, 0.16, 0.01);
                    vaultGearModifierTiersBuilder.add(52, -1, 10, 0.17, 0.22, 0.01);
                    vaultGearModifierTiersBuilder.add(70, -1, 10, 0.23, 0.3, 0.01);
                    vaultGearModifierTiersBuilder.add(101, -1, 10, 0.31, 0.4, 0.01);
                    vaultGearModifierTiersBuilder.add(102, -1, 10, 0.41, 0.55, 0.01);
                });
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.ABILITY_COOLDOWN_SKIP_CHANCE, "ModAttackType", "mod_ability_cooldown_skip", List.of("focusAbilitySkip"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(20, 85, 10, 0.03, 0.06, 0.01);
                    vaultGearModifierTiersBuilder.add(60, -1, 10, 0.6, 0.1, 0.01);
                    vaultGearModifierTiersBuilder.add(70, -1, 10, 0.11, 0.15, 0.01);
                    vaultGearModifierTiersBuilder.add(90, -1, 10, 0.16, 0.2, 0.01);
                    vaultGearModifierTiersBuilder.add(101, -1, 10, 0.21, 0.25, 0.01);
                    vaultGearModifierTiersBuilder.add(102, -1, 10, 0.26, 0.3, 0.01);
                });
                vaultGearAttributeGroupBuilder.addModifier(ModGearAttributes.HEXING_CHANCE, "ModAttackType", "mod_hexing_chance", List.of("focusHexingChance"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(20, 85, 10, 0.03, 0.06, 0.01);
                    vaultGearModifierTiersBuilder.add(60, -1, 10, 0.6, 0.1, 0.01);
                    vaultGearModifierTiersBuilder.add(70, -1, 10, 0.11, 0.15, 0.01);
                    vaultGearModifierTiersBuilder.add(90, -1, 10, 0.16, 0.2, 0.01);
                    vaultGearModifierTiersBuilder.add(101, -1, 10, 0.21, 0.25, 0.01);
                    vaultGearModifierTiersBuilder.add(102, -1, 10, 0.26, 0.3, 0.01);
                });
                vaultGearAttributeGroupBuilder.addModifier(ModGearAttributes.FIRE_ELEMENT, "ModElementType", "mod_fire_element", List.of("focusElement"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, -1, 10, true);
                });
                vaultGearAttributeGroupBuilder.addModifier(ModGearAttributes.ICE_ELEMENT, "ModElementType", "mod_ice_element", List.of("focusElement"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, -1, 10, true);
                });
                vaultGearAttributeGroupBuilder.addModifier(ModGearAttributes.POISON_ELEMENT, "ModElementType", "mod_poison_element", List.of("focusElement"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, -1, 10, true);
                });
                vaultGearAttributeGroupBuilder.addModifier(ModGearAttributes.LIGHTNING_ELEMENT, "ModElementType", "mod_lightning_element", List.of("focusElement"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, -1, 10, true);
                });
            });
        });

        //TODO: Configure Mythic values
        add("scepter_mythic", builder -> {
            builder.add(VaultGearTierConfig.ModifierAffixTagGroup.BASE_ATTRIBUTES, vaultGearAttributeGroupBuilder -> {
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.DURABILITY, "BaseDurability", "base_durability", List.of("resilientFocusTarget"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, 40, 10, 3425, 4225, 1);
                    vaultGearModifierTiersBuilder.add(8, 70, 10, 4226, 5225, 1);
                    vaultGearModifierTiersBuilder.add(25, -1, 10, 5226, 6325, 1);
                    vaultGearModifierTiersBuilder.add(50, -1, 10, 6826, 8325, 1);
                    vaultGearModifierTiersBuilder.add(70, -1, 10, 8576, 10375, 1);
                });
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.SOULBOUND, "BaseSoulbound", "base_soulbound", List.of(), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, -1, 20, true);
                });
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.SOULBOUND, "BaseSoulbound", "base_no_soulbound", List.of(), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, -1, 80, false);
                });
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.LIVING, "BaseLiving", "base_no_living", List.of(), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, -1, 98, false);
                });
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.LIVING, "BaseLiving", "base_living", List.of(), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, -1, 2, true);
                });
            });
            builder.add(VaultGearTierConfig.ModifierAffixTagGroup.IMPLICIT, vaultGearAttributeGroupBuilder -> {
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.ABILITY_POWER, "BaseAbilityPower", "base_ability_power", List.of(), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, 15, 10, 9, 15, 1);
                    vaultGearModifierTiersBuilder.add(10, 47, 10, 16, 24, 1);
                    vaultGearModifierTiersBuilder.add(16, 57, 10, 24, 32, 1);
                    vaultGearModifierTiersBuilder.add(25, 63, 10, 32, 42, 1);
                    vaultGearModifierTiersBuilder.add(48, 82, 10, 42, 55, 1);
                    vaultGearModifierTiersBuilder.add(58, -1, 10, 55, 70, 1);
                    vaultGearModifierTiersBuilder.add(68, -1, 10, 70, 80, 1);
                    vaultGearModifierTiersBuilder.add(80, -1, 10, 80, 90, 1);
                    vaultGearModifierTiersBuilder.add(90, -1, 10, 90, 105, 1);
                    vaultGearModifierTiersBuilder.add(95, -1, 10, 105, 110, 1);
                    vaultGearModifierTiersBuilder.add(100, -1, 10, 110, 120, 1);
                });
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.ATTACK_SPEED, "BaseAttackSpeed", "base_attack_speed", List.of(), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, -1, 10, -2.4, -2.3, 0.01);
                    vaultGearModifierTiersBuilder.add(50, -1, 10, -2.3, -2.2, 0.01);
                });
                vaultGearAttributeGroupBuilder.addModifier(ModGearAttributes.SCEPTER_SPARKLES, "ModScepterType", "base_sparkles_type", List.of(), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, -1, 20, true);
                });
                vaultGearAttributeGroupBuilder.addModifier(ModGearAttributes.SCEPTER_BEAM, "ModScepterType", "base_beam_type", List.of(), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, -1, 20, true);
                });
                vaultGearAttributeGroupBuilder.addModifier(ModGearAttributes.SCEPTER_INVOKE, "ModScepterType", "base_invoke_type_fireball", List.of(), vaultGearModifierTiersBuilder -> {
                    addScepterInvoke(vaultGearModifierTiersBuilder, 0, 30, 4, "Fireball_Base", 1, 4, 1, 60, 100, 10);
                    addScepterInvoke(vaultGearModifierTiersBuilder, 23, 56, 4, "Fireball_Base", 3, 7, 1, 55, 95, 10);
                    addScepterInvoke(vaultGearModifierTiersBuilder, 54, 90, 4, "Fireball_Base", 5, 9, 1, 50, 90, 10);
                    addScepterInvoke(vaultGearModifierTiersBuilder, 89, -1, 4, "Fireball_Base", 6, 12, 1, 45, 85, 10);
                });
                vaultGearAttributeGroupBuilder.addModifier(ModGearAttributes.SCEPTER_INVOKE, "ModScepterType", "base_invoke_type_nova", List.of(), vaultGearModifierTiersBuilder -> {
                    addScepterInvoke(vaultGearModifierTiersBuilder, 0, 30, 4, "Nova_Base", 1, 4, 1, 40, 80, 10);
                    addScepterInvoke(vaultGearModifierTiersBuilder, 23, 56, 4, "Nova_Base", 3, 7, 1, 40, 80, 10);
                    addScepterInvoke(vaultGearModifierTiersBuilder, 54, 90, 4, "Nova_Base", 5, 9, 1, 35, 75, 10);
                    addScepterInvoke(vaultGearModifierTiersBuilder, 89, -1, 4, "Nova_Base", 6, 12, 1, 35, 75, 10);
                });
                vaultGearAttributeGroupBuilder.addModifier(ModGearAttributes.SCEPTER_INVOKE, "ModScepterType", "base_invoke_type_implode", List.of(), vaultGearModifierTiersBuilder -> {
                    addScepterInvoke(vaultGearModifierTiersBuilder, 0, 30, 4, "Mana_Shield_Implode", 1, 4, 1, 50, 80, 10);
                    addScepterInvoke(vaultGearModifierTiersBuilder, 23, 56, 4, "Mana_Shield_Implode", 5, 8, 1, 50, 80, 10);
                    addScepterInvoke(vaultGearModifierTiersBuilder, 54, 90, 4, "Mana_Shield_Implode", 8, 11, 1, 50, 80, 10);
                    addScepterInvoke(vaultGearModifierTiersBuilder, 89, -1, 4, "Mana_Shield_Implode", 9, 16, 1, 50, 80, 10);
                });
                vaultGearAttributeGroupBuilder.addModifier(ModGearAttributes.SCEPTER_INVOKE, "ModScepterType", "base_invoke_type_arcane_prism", List.of(), vaultGearModifierTiersBuilder -> {
                    addScepterInvoke(vaultGearModifierTiersBuilder, 0, 30, 4, "Arcane_Prism", 1, 4, 1, 60, 100, 10);
                    addScepterInvoke(vaultGearModifierTiersBuilder, 23, 56, 4, "Arcane_Prism", 3, 7, 1, 55, 95, 10);
                    addScepterInvoke(vaultGearModifierTiersBuilder, 54, 90, 4, "Arcane_Prism", 5, 9, 1, 50, 90, 10);
                    addScepterInvoke(vaultGearModifierTiersBuilder, 89, -1, 4, "Arcane_Prism", 6, 12, 1, 45, 85, 10);
                });
                vaultGearAttributeGroupBuilder.addModifier(ModGearAttributes.SCEPTER_INVOKE, "ModScepterType", "base_invoke_type_lightning_ball", List.of(), vaultGearModifierTiersBuilder -> {
                    addScepterInvoke(vaultGearModifierTiersBuilder, 0, 30, 4, "Chain_Lightning_Orbs", 1, 4, 1, 50, 80, 10);
                    addScepterInvoke(vaultGearModifierTiersBuilder, 23, 56, 4, "Chain_Lightning_Orbs", 5, 8, 1, 50, 80, 10);
                    addScepterInvoke(vaultGearModifierTiersBuilder, 54, 90, 4, "Chain_Lightning_Orbs", 8, 11, 1, 50, 80, 10);
                    addScepterInvoke(vaultGearModifierTiersBuilder, 89, -1, 4, "Chain_Lightning_Orbs", 9, 16, 1, 50, 80, 10);
                });
                vaultGearAttributeGroupBuilder.addModifier(ModGearAttributes.SCEPTER_INVOKE, "ModScepterType", "base_invoke_type_charged_bolts", List.of(), vaultGearModifierTiersBuilder -> {
                    addScepterInvoke(vaultGearModifierTiersBuilder, 0, 30, 4, "Chain_Lightning_Charged_Bolts", 1, 4, 1, 40, 70, 10);
                    addScepterInvoke(vaultGearModifierTiersBuilder, 23, 56, 4, "Chain_Lightning_Charged_Bolts", 5, 8, 1, 35, 70, 10);
                    addScepterInvoke(vaultGearModifierTiersBuilder, 54, 90, 4, "Chain_Lightning_Charged_Bolts", 8, 11, 1, 30, 70, 10);
                    addScepterInvoke(vaultGearModifierTiersBuilder, 89, -1, 4, "Chain_Lightning_Charged_Bolts", 9, 16, 1, 30, 65, 10);
                });
                vaultGearAttributeGroupBuilder.addModifier(ModGearAttributes.SCEPTER_INVOKE, "ModScepterType", "base_invoke_type_necromancy", List.of(), vaultGearModifierTiersBuilder -> {
                    addScepterInvoke(vaultGearModifierTiersBuilder, 0, 30, 4, "Necromancy_Base", 1, 4, 1, 40, 70, 10);
                    addScepterInvoke(vaultGearModifierTiersBuilder, 23, 56, 4, "Necromancy_Base", 5, 8, 1, 35, 70, 10);
                    addScepterInvoke(vaultGearModifierTiersBuilder, 54, 90, 4, "Necromancy_Base", 8, 11, 1, 30, 70, 10);
                    addScepterInvoke(vaultGearModifierTiersBuilder, 89, -1, 4, "Necromancy_Base", 9, 16, 1, 30, 65, 10);
                });
                //TODO: Rework this into "Magibash" implicit
                vaultGearAttributeGroupBuilder.addModifier(ModGearAttributes.AP_SCALING_DAMAGE, "ModScepterType", "base_ap_scaling_damage", List.of(), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, 60, 12, 0.28F, 0.35F, 0.01F);
                    vaultGearModifierTiersBuilder.add(50, -1, 12, 0.36, 0.45F, 0.01F);
                    vaultGearModifierTiersBuilder.add(75, -1, 12, 0.45, 0.65F, 0.01F);
                });
            });
            builder.add(VaultGearTierConfig.ModifierAffixTagGroup.PREFIX, vaultGearAttributeGroupBuilder -> {
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.ABILITY_POWER, "ModOnHitAddition", "mod_ability_power", List.of("focusAbilityDamage"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, 49, 10, 2, 6, 1);
                    vaultGearModifierTiersBuilder.add(25, 74, 10, 7, 12, 1);
                    vaultGearModifierTiersBuilder.add(50, -1, 10, 12, 17, 1);
                    vaultGearModifierTiersBuilder.add(75, -1, 10, 17, 25, 1);
                    vaultGearModifierTiersBuilder.add(100, -1, 10, 25, 30, 1);
                    vaultGearModifierTiersBuilder.add(101, -1, 10, 30, 40, 1);
                    vaultGearModifierTiersBuilder.add(102, -1, 10, 40, 65, 1);
                });
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.ABILITY_POWER_PERCENTILE, "ModOnHitAddition", "mod_ability_power_percent", List.of("focusAbilityDamage"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, 86, 10, 0.04, 0.07, 0.01);
                    vaultGearModifierTiersBuilder.add(42, -1, 10, 0.08, 0.12, 0.01);
                    vaultGearModifierTiersBuilder.add(68, -1, 10, 0.13, 0.15, 0.01);
                    vaultGearModifierTiersBuilder.add(87, -1, 10, 0.16, 0.2, 0.01);
                    vaultGearModifierTiersBuilder.add(101, -1, 10, 0.21, 0.25, 0.01);
                    vaultGearModifierTiersBuilder.add(102, -1, 10, 0.26, 0.3, 0.01);
                });
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.SOUL_QUANTITY, "ModSoulChance", "mod_soul_chance", List.of("focusSoulQuantity"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, -1, 10, 0.1, 0.2, 0.01);
                    vaultGearModifierTiersBuilder.add(28, -1, 10, 0.21, 0.3, 0.01);
                    vaultGearModifierTiersBuilder.add(45, -1, 10, 0.31, 0.4, 0.01);
                    vaultGearModifierTiersBuilder.add(75, -1, 10, 0.41, 0.5, 0.01);
                    vaultGearModifierTiersBuilder.add(101, -1, 10, 0.51, 0.7, 0.01);
                    vaultGearModifierTiersBuilder.add(102, -1, 10, 0.71, 1.0, 0.01);
                });
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.AREA_OF_EFFECT, "ModEffectRadius", "mod_effect_radius", List.of("focusEffectRadius"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, 62, 10, 0.04, 0.08, 0.01);
                    vaultGearModifierTiersBuilder.add(40, -1, 10, 0.09, 0.14, 0.01);
                    vaultGearModifierTiersBuilder.add(63, -1, 10, 0.15, 0.2, 0.01);
                    vaultGearModifierTiersBuilder.add(89, -1, 10, 0.21, 0.25, 0.01);
                    vaultGearModifierTiersBuilder.add(101, -1, 10, 0.26, 0.34, 0.01);
                    vaultGearModifierTiersBuilder.add(102, -1, 10, 0.35, 0.45, 0.01);
                });
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.HEALING_EFFECTIVENESS, "ModHealth", "mod_healing_effect", List.of("focusHealingEff"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, 62, 10, 0.05, 0.1, 0.01);
                    vaultGearModifierTiersBuilder.add(22, -1, 10, 0.11, 0.18, 0.01);
                    vaultGearModifierTiersBuilder.add(43, -1, 10, 0.19, 0.26, 0.01);
                    vaultGearModifierTiersBuilder.add(70, -1, 10, 0.27, 0.35, 0.01);
                    vaultGearModifierTiersBuilder.add(101, -1, 10, 0.36, 0.5, 0.01);
                    vaultGearModifierTiersBuilder.add(102, -1, 10, 0.51, 0.7, 0.01);
                });
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.COOLDOWN_REDUCTION, "ModCooldownReduction", "mod_cooldown_reduction", List.of("focusCooldown"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, 62, 10, 0.05, 0.1, 0.01);
                    vaultGearModifierTiersBuilder.add(22, -1, 10, 0.11, 0.15, 0.01);
                    vaultGearModifierTiersBuilder.add(43, -1, 10, 0.16, 0.22, 0.01);
                    vaultGearModifierTiersBuilder.add(70, -1, 10, 0.23, 0.27, 0.01);
                    vaultGearModifierTiersBuilder.add(101, -1, 10, 0.28, 0.34, 0.01);
                    vaultGearModifierTiersBuilder.add(102, -1, 10, 0.35, 0.45, 0.01);
                });
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.DAMAGE_CHAMPION, "ModDamageChampion", "mod_damage_champion", List.of("focusDamageChampion"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, 79, 10, 0.05, 0.1, 0.01);
                    vaultGearModifierTiersBuilder.add(36, -1, 10, 0.11, 0.15, 0.01);
                    vaultGearModifierTiersBuilder.add(62, -1, 10, 0.21, 0.25, 0.01);
                    vaultGearModifierTiersBuilder.add(80, -1, 10, 0.26, 0.3, 0.01);
                    vaultGearModifierTiersBuilder.add(101, -1, 10, 0.31, 0.4, 0.01);
                    vaultGearModifierTiersBuilder.add(102, -1, 10, 0.41, 0.5, 0.01);
                });
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.DAMAGE_DUNGEON, "ModDamageDungeon", "mod_damage_dungeon", List.of("focusDamageDungeon"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, 79, 10, 0.05, 0.1, 0.01);
                    vaultGearModifierTiersBuilder.add(36, -1, 10, 0.11, 0.15, 0.01);
                    vaultGearModifierTiersBuilder.add(62, -1, 10, 0.21, 0.25, 0.01);
                    vaultGearModifierTiersBuilder.add(80, -1, 10, 0.26, 0.3, 0.01);
                    vaultGearModifierTiersBuilder.add(101, -1, 10, 0.31, 0.4, 0.01);
                    vaultGearModifierTiersBuilder.add(102, -1, 10, 0.41, 0.5, 0.01);
                });
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.DAMAGE_HORDE, "ModDamageHorde", "mod_damage_horde", List.of("focusDamageHorde"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, 79, 10, 0.05, 0.1, 0.01);
                    vaultGearModifierTiersBuilder.add(36, -1, 10, 0.11, 0.15, 0.01);
                    vaultGearModifierTiersBuilder.add(62, -1, 10, 0.21, 0.25, 0.01);
                    vaultGearModifierTiersBuilder.add(80, -1, 10, 0.26, 0.3, 0.01);
                    vaultGearModifierTiersBuilder.add(101, -1, 10, 0.31, 0.4, 0.01);
                    vaultGearModifierTiersBuilder.add(102, -1, 10, 0.41, 0.5, 0.01);
                });
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.DAMAGE_ASSASSIN, "ModDamageAssassin", "mod_damage_assassin", List.of("focusDamageAssassin"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, 79, 10, 0.05, 0.1, 0.01);
                    vaultGearModifierTiersBuilder.add(36, -1, 10, 0.11, 0.15, 0.01);
                    vaultGearModifierTiersBuilder.add(62, -1, 10, 0.21, 0.25, 0.01);
                    vaultGearModifierTiersBuilder.add(80, -1, 10, 0.26, 0.3, 0.01);
                    vaultGearModifierTiersBuilder.add(101, -1, 10, 0.31, 0.4, 0.01);
                    vaultGearModifierTiersBuilder.add(102, -1, 10, 0.41, 0.5, 0.01);
                });
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.DAMAGE_TANK, "ModDamageTank", "mod_damage_tank", List.of("focusDamageTank"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, 79, 10, 0.05, 0.1, 0.01);
                    vaultGearModifierTiersBuilder.add(36, -1, 10, 0.11, 0.15, 0.01);
                    vaultGearModifierTiersBuilder.add(62, -1, 10, 0.21, 0.25, 0.01);
                    vaultGearModifierTiersBuilder.add(80, -1, 10, 0.26, 0.3, 0.01);
                    vaultGearModifierTiersBuilder.add(101, -1, 10, 0.31, 0.4, 0.01);
                    vaultGearModifierTiersBuilder.add(102, -1, 10, 0.41, 0.5, 0.01);
                });
            });
            builder.add(VaultGearTierConfig.ModifierAffixTagGroup.SUFFIX, vaultGearAttributeGroupBuilder -> {
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.ATTACK_SPEED, "ModAttackType", "mod_attack_speed_percent", List.of("focusAttackSpeed"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, -1, 10, 0.05, 0.1, 0.01);
                    vaultGearModifierTiersBuilder.add(36, -1, 10, 0.11, 0.15, 0.01);
                    vaultGearModifierTiersBuilder.add(65, -1, 10, 0.16, 0.2, 0.01);
                    vaultGearModifierTiersBuilder.add(96, -1, 10, 0.21, 0.25, 0.01);
                    vaultGearModifierTiersBuilder.add(101, -1, 10, 0.26, 3, 0.01);
                    vaultGearModifierTiersBuilder.add(102, -1, 10, 0.31, 0.35, 0.01);
                });
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.ATTACK_RANGE, "ModAttackType", "mod_attack_range", List.of("focusAttackRange"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, -1, 10, 0.1, 0.2, 0.01);
                    vaultGearModifierTiersBuilder.add(20, -1, 10, 0.21, 0.3, 0.01);
                    vaultGearModifierTiersBuilder.add(45, -1, 10, 0.31, 0.4, 0.01);
                    vaultGearModifierTiersBuilder.add(75, -1, 10, 0.41, 0.5, 0.01);
                    vaultGearModifierTiersBuilder.add(101, -1, 10, 0.5, 0.65, 0.01);
                    vaultGearModifierTiersBuilder.add(102, -1, 10, 0.66, 1.0, 0.01);
                });
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.MANA_REGEN_ADDITIVE_PERCENTILE, "ModManaRegen", "mod_mana_regen", List.of("focusManaRegen"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, 54, 10, 0.1, 0.2, 0.01);
                    vaultGearModifierTiersBuilder.add(23, 66, 10, 0.21, 0.3, 0.01);
                    vaultGearModifierTiersBuilder.add(34, 80, 10, 0.31, 0.4, 0.01);
                    vaultGearModifierTiersBuilder.add(46, -1, 10, 0.41, 0.5, 0.01);
                    vaultGearModifierTiersBuilder.add(55, -1, 10, 0.51, 0.6, 0.01);
                    vaultGearModifierTiersBuilder.add(67, -1, 10, 0.61, 0.7, 0.01);
                    vaultGearModifierTiersBuilder.add(81, -1, 10, 0.71, 0.8, 0.01);
                    vaultGearModifierTiersBuilder.add(92, -1, 10, 0.81, 0.9, 0.01);
                    vaultGearModifierTiersBuilder.add(101, -1, 10, 0.91, 1.2, 0.01);
                    vaultGearModifierTiersBuilder.add(102, -1, 10, 1.21, 1.45, 0.01);
                });
                vaultGearAttributeGroupBuilder.addModifier(ModGearAttributes.ECHOING_CHANCE, "ModElementType", "mod_echoing_chance", List.of("focusEchoingChance"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(50, 85, 10, 0.03, 0.06, 0.01);
                    vaultGearModifierTiersBuilder.add(60, -1, 10, 0.6, 0.1, 0.01);
                    vaultGearModifierTiersBuilder.add(70, -1, 10, 0.11, 0.15, 0.01);
                    vaultGearModifierTiersBuilder.add(90, -1, 10, 0.16, 0.2, 0.01);
                    vaultGearModifierTiersBuilder.add(101, -1, 10, 0.21, 0.25, 0.01);
                    vaultGearModifierTiersBuilder.add(102, -1, 10, 0.26, 0.3, 0.01);
                });
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.ON_HIT_STUN, "ModElementType", "mod_stun_chance", List.of("focusStunChance"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(50, 85, 10, 0.03, 0.06, 0.01);
                    vaultGearModifierTiersBuilder.add(60, -1, 10, 0.6, 0.1, 0.01);
                    vaultGearModifierTiersBuilder.add(70, -1, 10, 0.11, 0.15, 0.01);
                    vaultGearModifierTiersBuilder.add(90, -1, 10, 0.16, 0.2, 0.01);
                    vaultGearModifierTiersBuilder.add(101, -1, 10, 0.21, 0.25, 0.01);
                    vaultGearModifierTiersBuilder.add(102, -1, 10, 0.26, 0.3, 0.01);
                });
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.EFFECT_DURATION, "ModEffectDuration", "mod_effect_duration", List.of("focusEffectDuration"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, 85, 10, 0.05, 0.1, 0.01);
                    vaultGearModifierTiersBuilder.add(32, -1, 10, 0.11, 0.16, 0.01);
                    vaultGearModifierTiersBuilder.add(52, -1, 10, 0.17, 0.22, 0.01);
                    vaultGearModifierTiersBuilder.add(70, -1, 10, 0.23, 0.3, 0.01);
                    vaultGearModifierTiersBuilder.add(101, -1, 10, 0.31, 0.4, 0.01);
                    vaultGearModifierTiersBuilder.add(102, -1, 10, 0.41, 0.55, 0.01);
                });
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.ABILITY_COOLDOWN_SKIP_CHANCE, "ModAttackType", "mod_ability_cooldown_skip", List.of("focusAbilitySkip"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(20, 85, 10, 0.03, 0.06, 0.01);
                    vaultGearModifierTiersBuilder.add(60, -1, 10, 0.6, 0.1, 0.01);
                    vaultGearModifierTiersBuilder.add(70, -1, 10, 0.11, 0.15, 0.01);
                    vaultGearModifierTiersBuilder.add(90, -1, 10, 0.16, 0.2, 0.01);
                    vaultGearModifierTiersBuilder.add(101, -1, 10, 0.21, 0.25, 0.01);
                    vaultGearModifierTiersBuilder.add(102, -1, 10, 0.26, 0.3, 0.01);
                });
                vaultGearAttributeGroupBuilder.addModifier(ModGearAttributes.HEXING_CHANCE, "ModAttackType", "mod_hexing_chance", List.of("focusHexingChance"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(20, 85, 10, 0.03, 0.06, 0.01);
                    vaultGearModifierTiersBuilder.add(60, -1, 10, 0.6, 0.1, 0.01);
                    vaultGearModifierTiersBuilder.add(70, -1, 10, 0.11, 0.15, 0.01);
                    vaultGearModifierTiersBuilder.add(90, -1, 10, 0.16, 0.2, 0.01);
                    vaultGearModifierTiersBuilder.add(101, -1, 10, 0.21, 0.25, 0.01);
                    vaultGearModifierTiersBuilder.add(102, -1, 10, 0.26, 0.3, 0.01);
                });
                vaultGearAttributeGroupBuilder.addModifier(ModGearAttributes.FIRE_ELEMENT, "ModElementType", "mod_fire_element", List.of("focusElement"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, -1, 10, true);
                });
                vaultGearAttributeGroupBuilder.addModifier(ModGearAttributes.ICE_ELEMENT, "ModElementType", "mod_ice_element", List.of("focusElement"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, -1, 10, true);
                });
                vaultGearAttributeGroupBuilder.addModifier(ModGearAttributes.POISON_ELEMENT, "ModElementType", "mod_poison_element", List.of("focusElement"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, -1, 10, true);
                });
                vaultGearAttributeGroupBuilder.addModifier(ModGearAttributes.LIGHTNING_ELEMENT, "ModElementType", "mod_lightning_element", List.of("focusElement"), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, -1, 10, true);
                });
            });
        });
        addToAllOffhands(VaultGearTierConfig.ModifierAffixTagGroup.valueOf("UNUSUAL_PREFIX"), vaultGearAttributeGroupBuilder -> {
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.LEECH);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.MANA_ADDITIVE);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.MANA_REGEN);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.COOLDOWN_REDUCTION);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.AXE_CLEAVE);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.EFFECT_CLOUD_CHANCE);
        });

        addToAllOffhands(VaultGearTierConfig.ModifierAffixTagGroup.valueOf("UNUSUAL_SUFFIX"), vaultGearAttributeGroupBuilder -> {
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.EFFECT_DURATION);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.EFFECT_RADIUS);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.KINETIC_IMMUNITY);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.KNOCKBACK_RESISTANCE);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.DURABILITY_WEAR_REDUCTION);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.HEALING_EFFECTIVENESS);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.DODGE_CHANCE);
        });

        addToAllOffhandsExcept(VaultGearTierConfig.ModifierAffixTagGroup.valueOf("UNUSUAL_SUFFIX"), List.of("loot_sack", "magnet"), vaultGearAttributeGroupBuilder -> {
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.ITEM_QUANTITY);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.ITEM_RARITY);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.COPIOUSLY);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.TRAP_DISARMING);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.REACH);
        });

        addToAllOffhandsExcept(VaultGearTierConfig.ModifierAffixTagGroup.valueOf("UNUSUAL_PREFIX"), List.of("wand"), vaultGearAttributeGroupBuilder -> {
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.ABILITY_POWER_PERCENTILE);
        });

        addToAllOffhandsExcept(VaultGearTierConfig.ModifierAffixTagGroup.valueOf("UNUSUAL_PREFIX"), List.of("plushie", "shield"), vaultGearAttributeGroupBuilder -> {
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.RESISTANCE);
        });

        addToAllOffhandsExcept(VaultGearTierConfig.ModifierAffixTagGroup.valueOf("UNUSUAL_PREFIX"), List.of("plushie"), vaultGearAttributeGroupBuilder -> {
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.DAMAGE_INCREASE);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.MOVEMENT_SPEED);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.ABILITY_POWER);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.CHAINING);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.CRITICAL_HIT_MITIGATION);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.DAMAGE_INCREASE);
        });

        addToAllNonAxeMainHands(VaultGearTierConfig.ModifierAffixTagGroup.valueOf("UNUSUAL_PREFIX"), vaultGearAttributeGroupBuilder -> {
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.AXE_BLEED_CLOUD);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.AXE_REAVING);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.AXE_CLEAVE);
        });

        addToAllMainHands(VaultGearTierConfig.ModifierAffixTagGroup.valueOf("UNUSUAL_PREFIX"), vaultGearAttributeGroupBuilder -> {
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.MOVEMENT_SPEED);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.RESISTANCE);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.DAMAGE_INCREASE);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.LEECH);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.CRITICAL_HIT_MITIGATION);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.MANA_ADDITIVE);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.MANA_REGEN);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.COOLDOWN_REDUCTION);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.EFFECT_CLOUD_CHANCE);
        });

        addToAllMainHands(VaultGearTierConfig.ModifierAffixTagGroup.valueOf("UNUSUAL_SUFFIX"), vaultGearAttributeGroupBuilder -> {
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.EFFECT_DURATION);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.EFFECT_RADIUS);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.KINETIC_IMMUNITY);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.KNOCKBACK_RESISTANCE);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.DURABILITY_WEAR_REDUCTION);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.ABILITY_POWER_PERCENTILE);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.HEALING_EFFECTIVENESS);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.HEXING_HIT);
        });

        addToJewel(VaultGearTierConfig.ModifierAffixTagGroup.valueOf("UNUSUAL_SUFFIX"), vaultGearAttributeGroupBuilder -> {
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.HEALING_EFFECTIVENESS_JEWEL);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.MANA_REGEN_JEWEL);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.KNOCKBACK_RESISTANCE_JEWEL);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.VEIN_MINER_LEVEL_JEWEL);
        });

        addToMaps(VaultGearTierConfig.ModifierAffixTagGroup.valueOf("UNUSUAL_PREFIX"), vaultGearAttributeGroupBuilder -> {
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.CORROSIVE);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.EXTRA_REINFORCED);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.TIRED);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.LEECH);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.BLESSED);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.FRENZY);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.JUMPY);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.KILL_NOVA);
        });

        addToMaps(VaultGearTierConfig.ModifierAffixTagGroup.valueOf("UNUSUAL_SUFFIX"), vaultGearAttributeGroupBuilder -> {
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.NON_LETHAL);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.DOOR_HUNTER);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.SWEET_RETRO);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.HUNGER);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.HAUNTED_MANSION);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.KILL_FROST_NOVA);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.BACKWARDS);
        });

        addToAllArmor(VaultGearTierConfig.ModifierAffixTagGroup.valueOf("UNUSUAL_PREFIX"), vaultGearAttributeGroupBuilder -> {
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.MOVEMENT_SPEED);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.COPIOUSLY);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.POISON_CLOUD_ON_HIT);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.VULNERABLE_CLOUD_ON_HIT);
        });

        addToAllArmor(VaultGearTierConfig.ModifierAffixTagGroup.valueOf("UNUSUAL_SUFFIX"), vaultGearAttributeGroupBuilder -> {
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.KINETIC_IMMUNITY);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.DURABILITY_WEAR_REDUCTION);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.LUCKY_HIT_CHANCE);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.HEALING_CLOUD_ON_HIT);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.WEAKNESS_CLOUD_ON_HIT);
            vaultGearAttributeGroupBuilder.addModifier(UnusualModifierLib.SLOWNESS_CLOUD_ON_HIT);
        });

        addToAllOffhands(VaultGearTierConfig.ModifierAffixTagGroup.CORRUPTED_IMPLICIT, vaultGearAttributeGroupBuilder -> {
            vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.TALENT_LEVEL, "ModTalentLevel", "u_all_talent_levels", List.of(), vaultGearModifierTiersBuilder -> {
                vaultGearModifierTiersBuilder.add(50, -1, 200, new TalentLevelAttribute.Config(TalentLevelAttribute.ALL_TALENTS, 1));
            });
        });

        ModConfigs.TALENTS_GUI = new TalentsGUIConfig().readConfig();
        addTo(ModItems.VAULT_NECKLACE, VaultGearTierConfig.ModifierAffixTagGroup.CORRUPTED_IMPLICIT, vaultGearAttributeGroupBuilder -> {
            ModConfigs.TALENTS_GUI.getStyles().keySet().forEach(skill -> {
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.TALENT_LEVEL, "ModTalentLevel", "u_talent_level_" + skill.toLowerCase(), List.of(), vaultGearModifierTiersBuilder -> {
                    vaultGearModifierTiersBuilder.add(0, -1, 200, new TalentLevelAttribute.Config(skill, 2));
                    vaultGearModifierTiersBuilder.add(50, -1, 200, new TalentLevelAttribute.Config(skill, 3));
                    vaultGearModifierTiersBuilder.add(90, -1, 200, new TalentLevelAttribute.Config(skill, 4));
                });
            });
        });

        addToAllStandardGearConfigs(VaultGearTierConfig.ModifierAffixTagGroup.CORRUPTED_IMPLICIT, vaultGearAttributeGroupBuilder -> {
            //U22 Additions
            vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.PHOENIX, "ModPhoenix", "u_phoenix", List.of(), vaultGearModifierTiersBuilder -> {
                vaultGearModifierTiersBuilder.add(0, -1, 500, 1, 3, 1);
            });
            vaultGearAttributeGroupBuilder
                    .addModifier(iskallia.vault.init.ModGearAttributes.ABILITY_LEVEL, "ModAbility", "mod_jav_scatter_level", List.of(), vaultGearModifierTiersBuilder -> {
                        vaultGearModifierTiersBuilder.add(0, -1, 166, new AbilityLevelAttribute.Config("Javelin_Scatter", 1));
                        vaultGearModifierTiersBuilder.add(25, -1, 166, new AbilityLevelAttribute.Config("Javelin_Scatter", 2));
                        vaultGearModifierTiersBuilder.add(50, -1, 166, new AbilityLevelAttribute.Config("Javelin_Scatter", 3));
                    });

            vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.JESTER_LUCKY_HIT_CHANCE_PERCENTILE, "BaseBonusPool", "jester_lucky_hit", List.of(), vaultGearModifierTiersBuilder -> {
                vaultGearModifierTiersBuilder.add(0, -1, 250, 0.05, 0.1, 0.01);
                vaultGearModifierTiersBuilder.add(50, -1, 250, 0.06, 0.15, 0.01);
            });

            vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.BROODMOTHER_WEB, "ModEnhancement", "u_broodmother_web", List.of(), vaultGearModifierTiersBuilder -> {
               addBroodmotherWeb(vaultGearModifierTiersBuilder, 0, -1, 250, 0.2f, 0.7f, 0.05f, 0.2f, 0.6f, 0.05f);
               addBroodmotherWeb(vaultGearModifierTiersBuilder, 65, -1, 250, 0.2f, 0.7f, 0.05f, 0.6f, 1.2f, 0.05f);
            });

            vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.EFFECT, "uSaturation", "u_saturation", List.of(), vaultGearModifierTiersBuilder -> {
                vaultGearModifierTiersBuilder.add(0, -1, 500, MobEffects.SATURATION, 1);
            });

            vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.CRITICAL_HIT_TAKEN_REDUCTION, "uCritHitImplicit", "u_critical_hit_mitigation", List.of(), vaultGearModifierTiersBuilder -> {
                vaultGearModifierTiersBuilder.add(0, -1, 250, 0.05, 0.2, 0.01);
                vaultGearModifierTiersBuilder.add(0, -1, 250, 0.21, 0.5, 0.01);
            });

            //Wold's Additions
            vaultGearAttributeGroupBuilder
                    .addModifier(iskallia.vault.init.ModGearAttributes.EFFECT, "ModEffect", "u_lucky_lucky", List.of(), vaultGearModifierTiersBuilder -> {
                        vaultGearModifierTiersBuilder.add(0, -1, 10, MobEffects.LUCK.getRegistryName(), 1);
                    });
            vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.IMMORTALITY, "ModImmortality", "u_immortal_durability", List.of(), vaultGearModifierTiersBuilder -> {
                vaultGearModifierTiersBuilder.add(0, -1, 100, 1.0F, 1.0F, 0.01F);
            });

            vaultGearAttributeGroupBuilder.addModifier(ModGearAttributes.EXECUTION_DAMAGE, "ModOnHitType", "mod_corrupt_execution_damage", List.of(), vaultGearModifierTiersBuilder -> {
                vaultGearModifierTiersBuilder.add(0, -1, 100, 0.03F, 0.05F, 0.01F);
                vaultGearModifierTiersBuilder.add(50, -1, 100, 0.05F, 0.07F, 0.01F);
                vaultGearModifierTiersBuilder.add(90, -1, 100, 0.07F, 0.1F, 0.01F);
            });
            vaultGearAttributeGroupBuilder
                    .addModifier(ModGearAttributes.BURNING_HIT_CHANCE, "ModBurningHit", "mod_corrupt_burning_hit", List.of(), vaultGearModifierTiersBuilder -> {
                        vaultGearModifierTiersBuilder.add(0, 40, 100, 0.06F, 0.12F, 0.01F);
                        vaultGearModifierTiersBuilder.add(25, 65, 100, 0.12F, 0.14F, 0.01F);
                        vaultGearModifierTiersBuilder.add(50, -1, 100, 0.14F, 0.16F, 0.01F);
                        vaultGearModifierTiersBuilder.add(75, -1, 100, 0.16F, 0.2F, 0.01F);
            });
            vaultGearAttributeGroupBuilder
                    .addModifier(iskallia.vault.init.ModGearAttributes.SOUL_QUANTITY_PERCENTILE, "ModSoulQuantity", "mod_corrupt_soul_quantity", List.of(), vaultGearModifierTiersBuilder -> {
                        vaultGearModifierTiersBuilder.add(0, -1, 100, 0.25F, 0.25F, 0F);
            });
        });
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
                        .addModifier(iskallia.vault.init.ModGearAttributes.EFFECT, "ModEffect", "u_lucky_lucky_tm", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, -1, 10, MobEffects.LUCK.getRegistryName(), 1);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.EFFECT, "ModEffect", "u_cursed_unlucky", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, -1, 10, MobEffects.UNLUCK.getRegistryName(), 1);
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
                        .addModifier(ModGearAttributes.DRIPPING_LAVA, "ModDrippingLava", "mod_dripping_lava", List.of(), vaultGearModifierTiersBuilder -> {
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
                        .addModifier(ModGearAttributes.BURNING_HIT_CHANCE, "ModBurningHit", "mod_burning_hit_lava_chicken", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, 40, 10, 0.06F, 0.12F, 0.01F);
                            vaultGearModifierTiersBuilder.add(25, 65, 10, 0.12F, 0.14F, 0.01F);
                            vaultGearModifierTiersBuilder.add(50, -1, 10, 0.14F, 0.16F, 0.01F);
                            vaultGearModifierTiersBuilder.add(75, -1, 10, 0.16F, 0.2F, 0.01F);
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
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.ABILITY_LEVEL, "ModPoisonNovaLevel", "mod_poison_nova_level", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, -1, 10, new AbilityLevelAttribute.Config("Nova_Dot", 1));
                            vaultGearModifierTiersBuilder.add(0, -1, 10, new AbilityLevelAttribute.Config("Nova_Dot", 2));
                            vaultGearModifierTiersBuilder.add(50, -1, 10, new AbilityLevelAttribute.Config("Nova_Dot", 3));
                            vaultGearModifierTiersBuilder.add(75, -1, 10, new AbilityLevelAttribute.Config("Nova_Dot", 4));
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
                vaultGearAttributeGroupBuilder.addModifier(iskallia.vault.init.ModGearAttributes.EFFECT_TRAIL, "ModEffectTrail", "mod_poison_trail", List.of(), vaultGearModifierTiersBuilder -> {
                   vaultGearModifierTiersBuilder.add(0, 65, 10, MobEffects.POISON.getRegistryName(), new IntRollRangeEntry(140, 200, 20));
                   vaultGearModifierTiersBuilder.add(65, -1, 10, MobEffects.POISON.getRegistryName(), new IntRollRangeEntry(200, 240, 20));
                   vaultGearModifierTiersBuilder.add(90, -1, 10, MobEffects.POISON.getRegistryName(), new IntRollRangeEntry(240, 300, 20));
                });
            }).build();
        });

        add("dagger", builder -> {
            builder.key(VaultMod.id("dagger_main")).add(VaultGearTierConfig.ModifierAffixTagGroup.BASE_ATTRIBUTES, vaultGearAttributeGroupBuilder -> {
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.DURABILITY, "BaseDurability", "base_durability",   List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,  40, 10, 1800, 2600, 1);
                            vaultGearModifierTiersBuilder.add(8,  70, 10, 2601, 3600, 1);
                            vaultGearModifierTiersBuilder.add(25, -1, 10, 3601, 4700, 1);
                            vaultGearModifierTiersBuilder.add(50, -1, 10, 4701, 6200, 1);
                            vaultGearModifierTiersBuilder.add(70, -1, 10, 6201, 8000, 1);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.SOULBOUND,  "BaseSoulbound",  "base_soulbound",    List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, -1, 20, true);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.SOULBOUND,  "BaseSoulbound",  "base_no_soulbound", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, -1, 80, false);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.LIVING,     "BaseLiving",     "base_living",       List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,-1,2,true);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.LIVING,     "BaseLiving",     "base_no_living",    List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,-1,98,false);
                        });
            });

            builder.key(VaultMod.id("dagger_main")).add(VaultGearTierConfig.ModifierAffixTagGroup.IMPLICIT,        vaultGearAttributeGroupBuilder -> {
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.ATTACK_DAMAGE, "BaseAttackDamage", "base_attack_damage", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,   15, 10, 5,  8,  1);
                            vaultGearModifierTiersBuilder.add(10,  47, 10, 9,  12, 1);
                            vaultGearModifierTiersBuilder.add(16,  57, 10, 13, 16, 1);
                            vaultGearModifierTiersBuilder.add(25,  63, 10, 17, 20, 1);
                            vaultGearModifierTiersBuilder.add(36,  71, 10, 21, 24, 1);
                            vaultGearModifierTiersBuilder.add(48,  82, 10, 25, 28, 1);
                            vaultGearModifierTiersBuilder.add(58,  89, 10, 29, 32, 1);
                            vaultGearModifierTiersBuilder.add(64,  -1, 10, 33, 36, 1);
                            vaultGearModifierTiersBuilder.add(72,  -1, 10, 37, 40, 1);
                            vaultGearModifierTiersBuilder.add(83,  -1, 10, 41, 44, 1);
                            vaultGearModifierTiersBuilder.add(90,  -1, 10, 45, 48, 1);
                            vaultGearModifierTiersBuilder.add(95,  -1, 10, 49, 52, 1);
                            vaultGearModifierTiersBuilder.add(100, -1, 10, 53, 56, 1);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.ATTACK_SPEED,  "BaseAttackSpeed",  "base_attack_speed",  List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, -1, 10, -2.1,  -2.0, 1);
                            vaultGearModifierTiersBuilder.add(50,-1, 10, -1.99, -1.9, 1);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.ATTACK_RANGE,  "BaseReducedRange", "base_reduced_range", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,  -1, 10, -0.50,-0.30,1);
                            vaultGearModifierTiersBuilder.add(50, -1, 10, -0.29,-0.10,1);
                        });
            });

            builder.key(VaultMod.id("dagger_main")).add(VaultGearTierConfig.ModifierAffixTagGroup.PREFIX,          vaultGearAttributeGroupBuilder -> {
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.SOUL_QUANTITY,    "ModSoulChance",     "mod_soul_chance",     List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,   70, 10, 0.20, 0.40, 1);
                            vaultGearModifierTiersBuilder.add(28,  -1, 10, 0.41, 0.80, 1);
                            vaultGearModifierTiersBuilder.add(45,  -1, 10, 0.81, 1.20, 1);
                            vaultGearModifierTiersBuilder.add(75,  -1, 10, 1.21, 1.60, 1);
                            vaultGearModifierTiersBuilder.add(101, -1, 10, 1.60, 2.20, 1);
                            vaultGearModifierTiersBuilder.add(102, -1, 10, 2.20, 3.00, 1);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.DAMAGE_CHAMPION,  "ModChampionDamage", "mod_champion_damage", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,   79, 10, 0.05, 0.10, 1);
                            vaultGearModifierTiersBuilder.add(36,  -1, 10, 0.11, 0.20, 1);
                            vaultGearModifierTiersBuilder.add(62,  -1, 10, 0.21, 0.25, 1);
                            vaultGearModifierTiersBuilder.add(80,  -1, 10, 0.26, 0.30, 1);
                            vaultGearModifierTiersBuilder.add(101, -1, 10, 0.31, 0.40, 1);
                            vaultGearModifierTiersBuilder.add(102, -1, 10, 0.41, 0.50, 1);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.DAMAGE_DUNGEON,   "ModDungeonDamage",  "mod_dungeon_damage",  List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,   79, 10, 0.05, 0.10, 1);
                            vaultGearModifierTiersBuilder.add(36,  -1, 10, 0.11, 0.20, 1);
                            vaultGearModifierTiersBuilder.add(62,  -1, 10, 0.21, 0.25, 1);
                            vaultGearModifierTiersBuilder.add(80,  -1, 10, 0.26, 0.30, 1);
                            vaultGearModifierTiersBuilder.add(101, -1, 10, 0.31, 0.40, 1);
                            vaultGearModifierTiersBuilder.add(102, -1, 10, 0.41, 0.50, 1);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.DAMAGE_TANK,      "ModTankDamage",     "mod_tank_damage",     List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,   79, 10, 0.05, 0.10, 1);
                            vaultGearModifierTiersBuilder.add(36,  -1, 10, 0.11, 0.20, 1);
                            vaultGearModifierTiersBuilder.add(62,  -1, 10, 0.21, 0.25, 1);
                            vaultGearModifierTiersBuilder.add(80,  -1, 10, 0.26, 0.30, 1);
                            vaultGearModifierTiersBuilder.add(101, -1, 10, 0.31, 0.40, 1);
                            vaultGearModifierTiersBuilder.add(102, -1, 10, 0.41, 0.50, 1);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.DAMAGE_DWELLER,   "ModDwellerDamage",  "mod_dweller_damage",  List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,   79, 10, 0.05, 0.10, 1);
                            vaultGearModifierTiersBuilder.add(36,  -1, 10, 0.11, 0.20, 1);
                            vaultGearModifierTiersBuilder.add(62,  -1, 10, 0.21, 0.25, 1);
                            vaultGearModifierTiersBuilder.add(80,  -1, 10, 0.26, 0.30, 1);
                            vaultGearModifierTiersBuilder.add(101, -1, 10, 0.31, 0.40, 1);
                            vaultGearModifierTiersBuilder.add(102, -1, 10, 0.41, 0.50, 1);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.DAMAGE_ASSASSIN,  "ModAssassinDamage", "mod_assassin_damage", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,   79, 10, 0.05, 0.10, 1);
                            vaultGearModifierTiersBuilder.add(36,  -1, 10, 0.11, 0.20, 1);
                            vaultGearModifierTiersBuilder.add(62,  -1, 10, 0.21, 0.25, 1);
                            vaultGearModifierTiersBuilder.add(80,  -1, 10, 0.26, 0.30, 1);
                            vaultGearModifierTiersBuilder.add(101, -1, 10, 0.31, 0.40, 1);
                            vaultGearModifierTiersBuilder.add(102, -1, 10, 0.41, 0.50, 1);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.DAMAGE_HORDE,     "ModHordeDamage",    "mod_horde_damage",    List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,   79, 10, 0.05, 0.10, 1);
                            vaultGearModifierTiersBuilder.add(36,  -1, 10, 0.11, 0.20, 1);
                            vaultGearModifierTiersBuilder.add(62,  -1, 10, 0.21, 0.25, 1);
                            vaultGearModifierTiersBuilder.add(80,  -1, 10, 0.26, 0.30, 1);
                            vaultGearModifierTiersBuilder.add(101, -1, 10, 0.31, 0.40, 1);
                            vaultGearModifierTiersBuilder.add(102, -1, 10, 0.41, 0.50, 1);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.LUCKY_HIT_CHANCE, "ModLuckyHit",       "mod_lucky_hit",       List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(25,  79, 10, 0.03, 0.05, 1);
                            vaultGearModifierTiersBuilder.add(40,  -1, 10, 0.06, 0.08, 1);
                            vaultGearModifierTiersBuilder.add(60,  -1, 10, 0.09, 0.11, 1);
                            vaultGearModifierTiersBuilder.add(80,  -1, 10, 0.12, 0.15, 1);
                            vaultGearModifierTiersBuilder.add(101, -1, 10, 0.16, 0.20, 1);
                            vaultGearModifierTiersBuilder.add(102, -1, 10, 0.21, 0.25, 1);
                        });
            });

            builder.key(VaultMod.id("dagger_main")).add(VaultGearTierConfig.ModifierAffixTagGroup.SUFFIX,          vaultGearAttributeGroupBuilder -> {
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.DURABILITY,           "ModDurability",     "mod_durability",     List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(11,  -1, 10, 500,  900,  1);
                            vaultGearModifierTiersBuilder.add(28,  -1, 10, 901,  1300, 1);
                            vaultGearModifierTiersBuilder.add(46,  -1, 10, 1301, 1700, 1);
                            vaultGearModifierTiersBuilder.add(67,  -1, 10, 1701, 2100, 1);
                            vaultGearModifierTiersBuilder.add(89,  -1, 10, 2101, 2500, 1);
                            vaultGearModifierTiersBuilder.add(101, -1, 10, 2501, 3700, 1);
                            vaultGearModifierTiersBuilder.add(102, -1, 10, 3701, 5000, 1);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.ATTACK_DAMAGE,        "ModAttackEcho",     "mod_attack_damage",  List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,   49, 10, 1,  4,  1);
                            vaultGearModifierTiersBuilder.add(25,  74, 10, 5,  8,  1);
                            vaultGearModifierTiersBuilder.add(50,  -1, 10, 9,  12, 1);
                            vaultGearModifierTiersBuilder.add(75,  -1, 10, 13, 16, 1);
                            vaultGearModifierTiersBuilder.add(100, -1, 10, 17, 20, 1);
                            vaultGearModifierTiersBuilder.add(101, -1, 10, 21, 25, 1);
                            vaultGearModifierTiersBuilder.add(102, -1, 10, 26, 30, 1);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(                    ModGearAttributes.ECHOING_CHANCE,       "ModAttackEcho",     "mod_echoing_chance", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(50,   85, 10, 0.05,  0.10,  0.01);
                            vaultGearModifierTiersBuilder.add(60,   -1, 10, 0.11,  0.14,  0.01);
                            vaultGearModifierTiersBuilder.add(70,   -1, 10, 0.15,  0.18,  0.01);
                            vaultGearModifierTiersBuilder.add(90,   -1, 10, 0.19,  0.22,  0.01);
                            vaultGearModifierTiersBuilder.add(101,  -1, 10, 0.23,  0.26,  0.01);
                            vaultGearModifierTiersBuilder.add(102,  -1, 10, 0.27,  0.30,  0.01);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.ATTACK_RANGE,         "ModRangeSpeed",     "mod_attack_range",   List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,   -1, 10, 0.10, 0.20, 1);
                            vaultGearModifierTiersBuilder.add(20,  -1, 10, 0.21, 0.30, 1);
                            vaultGearModifierTiersBuilder.add(45,  -1, 10, 0.31, 0.40, 1);
                            vaultGearModifierTiersBuilder.add(75,  -1, 10, 0.41, 0.50, 1);
                            vaultGearModifierTiersBuilder.add(101, -1, 10, 0.51, 0.75, 1);
                            vaultGearModifierTiersBuilder.add(102, -1, 10, 0.76, 1.00, 1);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.ATTACK_SPEED_PERCENT, "ModRangeSpeed",     "mod_attack_speed",   List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,   -1, 10, 0.05, 0.10, 1);
                            vaultGearModifierTiersBuilder.add(36,  -1, 10, 0.11, 0.15, 1);
                            vaultGearModifierTiersBuilder.add(65,  -1, 10, 0.16, 0.20, 1);
                            vaultGearModifierTiersBuilder.add(96,  -1, 10, 0.21, 0.25, 1);
                            vaultGearModifierTiersBuilder.add(101, -1, 10, 0.26, 0.30, 1);
                            vaultGearModifierTiersBuilder.add(102, -1, 10, 0.31, 0.35, 1);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.EFFECT_CLOUD,         "ModHealBleedCloud", "mod_bleed_cloud",    List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,  -1, 4, "Bleed I",   ResourceLocation.withDefaultNamespace("empty"), 80,  4.0f, ModEffects.BLEED.getColor(), false, 0.03F, ModEffects.BLEED.getRegistryName(), 40, 0);
                            vaultGearModifierTiersBuilder.add(32, -1, 4, "Bleed II",  ResourceLocation.withDefaultNamespace("empty"), 120, 4.0f, ModEffects.BLEED.getColor(), false, 0.03F, ModEffects.BLEED.getRegistryName(), 40, 1);
                            vaultGearModifierTiersBuilder.add(64, -1, 4, "Bleed III", ResourceLocation.withDefaultNamespace("empty"), 160, 5.0f, ModEffects.BLEED.getColor(), false, 0.03F, ModEffects.BLEED.getRegistryName(), 40, 2);
                            vaultGearModifierTiersBuilder.add(101,-1, 4, "Bleed IV",  ResourceLocation.withDefaultNamespace("empty"), 200, 5.0f, ModEffects.BLEED.getColor(), false, 0.03F, ModEffects.BLEED.getRegistryName(), 40, 3);
                            vaultGearModifierTiersBuilder.add(102,-1, 4, "Bleed V",   ResourceLocation.withDefaultNamespace("empty"), 240, 5.0f, ModEffects.BLEED.getColor(), false, 0.03F, ModEffects.BLEED.getRegistryName(), 40, 4);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.EFFECT_CLOUD,         "ModHealBleedCloud", "mod_heal_cloud",     List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,  -1, 4, "Healing I",   ResourceLocation.withDefaultNamespace("empty"), 80,  4.0f, MobEffects.HEAL.getColor(), true, 0.03F, MobEffects.HEAL.getRegistryName(), 20, 0);
                            vaultGearModifierTiersBuilder.add(32, -1, 4, "Healing II",  ResourceLocation.withDefaultNamespace("empty"), 120, 4.0f, MobEffects.HEAL.getColor(), true, 0.03F, MobEffects.HEAL.getRegistryName(), 20, 0);
                            vaultGearModifierTiersBuilder.add(64, -1, 4, "Healing III", ResourceLocation.withDefaultNamespace("empty"), 160, 5.0f, MobEffects.HEAL.getColor(), true, 0.03F, MobEffects.HEAL.getRegistryName(), 20, 0);
                            vaultGearModifierTiersBuilder.add(101,-1, 4, "Healing IV",  ResourceLocation.withDefaultNamespace("empty"), 200, 5.0f, MobEffects.HEAL.getColor(), true, 0.03F, MobEffects.HEAL.getRegistryName(), 20, 1);
                            vaultGearModifierTiersBuilder.add(102,-1, 4, "Healing V",   ResourceLocation.withDefaultNamespace("empty"), 240, 5.0f, MobEffects.HEAL.getColor(), true, 0.03F, MobEffects.HEAL.getRegistryName(), 20, 2);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.EFFECT_CLOUD,         "ModPoisonCloud",    "mod_poison_cloud",   List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,  -1, 10, "Poison I",   ResourceLocation.withDefaultNamespace("empty"), 120, 4.0f, MobEffects.POISON.getColor(), false, 0.075F, MobEffects.POISON.getRegistryName(), 60, 3);
                            vaultGearModifierTiersBuilder.add(32, -1, 10, "Poison II",  ResourceLocation.withDefaultNamespace("empty"), 160, 4.0f, MobEffects.POISON.getColor(), false, 0.075F, MobEffects.POISON.getRegistryName(), 60, 5);
                            vaultGearModifierTiersBuilder.add(64, -1, 10, "Poison III", ResourceLocation.withDefaultNamespace("empty"), 200, 4.0f, MobEffects.POISON.getColor(), false, 0.075F, MobEffects.POISON.getRegistryName(), 60, 7);
                            vaultGearModifierTiersBuilder.add(101,-1, 10, "Poison IV",  ResourceLocation.withDefaultNamespace("empty"), 240, 4.0f, MobEffects.POISON.getColor(), false, 0.075F, MobEffects.POISON.getRegistryName(), 60, 9);
                            vaultGearModifierTiersBuilder.add(102,-1, 10, "Poison V",   ResourceLocation.withDefaultNamespace("empty"), 300, 4.0f, MobEffects.POISON.getColor(), false, 0.075F, MobEffects.POISON.getRegistryName(), 60, 12);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.EFFECT_CLOUD,         "ModSlownessCloud",  "mod_slowness_cloud", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,  -1, 10, "Slowness I",   ResourceLocation.withDefaultNamespace("empty"), 120, 4.0f, MobEffects.MOVEMENT_SLOWDOWN.getColor(), false, 0.075F, MobEffects.MOVEMENT_SLOWDOWN.getRegistryName(), 120, 0);
                            vaultGearModifierTiersBuilder.add(32, -1, 10, "Slowness II",  ResourceLocation.withDefaultNamespace("empty"), 160, 4.0f, MobEffects.MOVEMENT_SLOWDOWN.getColor(), false, 0.075F, MobEffects.MOVEMENT_SLOWDOWN.getRegistryName(), 140, 1);
                            vaultGearModifierTiersBuilder.add(64, -1, 10, "Slowness III", ResourceLocation.withDefaultNamespace("empty"), 200, 4.0f, MobEffects.MOVEMENT_SLOWDOWN.getColor(), false, 0.075F, MobEffects.MOVEMENT_SLOWDOWN.getRegistryName(), 160, 2);
                            vaultGearModifierTiersBuilder.add(101,-1, 10, "Slowness IV",  ResourceLocation.withDefaultNamespace("empty"), 240, 4.0f, MobEffects.MOVEMENT_SLOWDOWN.getColor(), false, 0.075F, MobEffects.MOVEMENT_SLOWDOWN.getRegistryName(), 180, 3);
                            vaultGearModifierTiersBuilder.add(102,-1, 10, "Slowness V",   ResourceLocation.withDefaultNamespace("empty"), 300, 4.0f, MobEffects.MOVEMENT_SLOWDOWN.getColor(), false, 0.075F, MobEffects.MOVEMENT_SLOWDOWN.getRegistryName(), 200, 4);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.EFFECT_CLOUD,         "ModChillCloud",     "mod_chill_cloud",    List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,  -1, 10, "Chilling I",   ResourceLocation.withDefaultNamespace("empty"), 120, 4.0f, ModEffects.CHILLED.getColor(), false, 0.075F, ModEffects.CHILLED.getRegistryName(), 140, 0);
                            vaultGearModifierTiersBuilder.add(32, -1, 10, "Chilling II",  ResourceLocation.withDefaultNamespace("empty"), 160, 4.0f, ModEffects.CHILLED.getColor(), false, 0.075F, ModEffects.CHILLED.getRegistryName(), 140, 0);
                            vaultGearModifierTiersBuilder.add(64, -1, 10, "Chilling III", ResourceLocation.withDefaultNamespace("empty"), 200, 4.0f, ModEffects.CHILLED.getColor(), false, 0.075F, ModEffects.CHILLED.getRegistryName(), 140, 0);
                            vaultGearModifierTiersBuilder.add(101,-1, 10, "Chilling IV",  ResourceLocation.withDefaultNamespace("empty"), 240, 4.0f, ModEffects.CHILLED.getColor(), false, 0.075F, ModEffects.CHILLED.getRegistryName(), 140, 0);
                            vaultGearModifierTiersBuilder.add(102,-1, 10, "Chilling V",   ResourceLocation.withDefaultNamespace("empty"), 300, 4.0f, ModEffects.CHILLED.getColor(), false, 0.075F, ModEffects.CHILLED.getRegistryName(), 140, 0);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.EFFECT_CLOUD,         "ModFearCloud",      "mod_fear_cloud",     List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,  -1, 10, "Fear I",   ResourceLocation.withDefaultNamespace("empty"), 120, 4.0f, ModEffects.TAUNT_REPEL_MOB.getColor(), false, 0.075F, ModEffects.TAUNT_REPEL_MOB.getRegistryName(), 60,  0);
                            vaultGearModifierTiersBuilder.add(32, -1, 10, "Fear II",  ResourceLocation.withDefaultNamespace("empty"), 160, 4.0f, ModEffects.TAUNT_REPEL_MOB.getColor(), false, 0.075F, ModEffects.TAUNT_REPEL_MOB.getRegistryName(), 60,  0);
                            vaultGearModifierTiersBuilder.add(64, -1, 10, "Fear III", ResourceLocation.withDefaultNamespace("empty"), 200, 4.0f, ModEffects.TAUNT_REPEL_MOB.getColor(), false, 0.075F, ModEffects.TAUNT_REPEL_MOB.getRegistryName(), 60,  0);
                            vaultGearModifierTiersBuilder.add(101,-1, 10, "Fear IV",  ResourceLocation.withDefaultNamespace("empty"), 240, 4.0f, ModEffects.TAUNT_REPEL_MOB.getColor(), false, 0.075F, ModEffects.TAUNT_REPEL_MOB.getRegistryName(), 120, 0);
                            vaultGearModifierTiersBuilder.add(102,-1, 10, "Fear V",   ResourceLocation.withDefaultNamespace("empty"), 300, 4.0f, ModEffects.TAUNT_REPEL_MOB.getColor(), false, 0.075F, ModEffects.TAUNT_REPEL_MOB.getRegistryName(), 120, 0);
                        });
            });
        });

        add("dagger_mythic", builder -> {
            builder.key(VaultMod.id("dagger_main_mythic")).add(VaultGearTierConfig.ModifierAffixTagGroup.BASE_ATTRIBUTES, vaultGearAttributeGroupBuilder -> {
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.DURABILITY, "BaseDurability", "base_durability",   List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(100, -1, 10, 6201, 8000, 1);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.SOULBOUND,  "BaseSoulbound",  "base_soulbound",    List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(100, -1, 50, true);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.SOULBOUND,  "BaseSoulbound",  "base_no_soulbound", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(100, -1, 50, false);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(                    ModGearAttributes.DIVINE,     "BaseDivine",     "base_divine",       List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(100,-1,4,true);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(                    ModGearAttributes.DIVINE,     "BaseDivine",     "base_no_divine",    List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(100,-1,96,false);
                        });
            });

            builder.key(VaultMod.id("dagger_main_mythic")).add(VaultGearTierConfig.ModifierAffixTagGroup.IMPLICIT, vaultGearAttributeGroupBuilder -> {
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.ATTACK_DAMAGE, "BaseAttackDamage", "base_attack_damage", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(100, -1, 10, 60, 75, 1);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.ATTACK_SPEED,  "BaseAttackSpeed",  "base_attack_speed",  List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(100,-1, 10, -1.95, -1.85, 1);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.ATTACK_RANGE,  "BaseReducedRange", "base_reduced_range", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(100, -1, 10, -0.15,0,1);
                        });
            });

            builder.key(VaultMod.id("dagger_main_mythic")).add(VaultGearTierConfig.ModifierAffixTagGroup.PREFIX, vaultGearAttributeGroupBuilder -> {
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.SOUL_QUANTITY,    "ModSoulChance",     "mod_soul_chance",     List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(100, -1, 10, 1.50, 2.00, 1);
                            vaultGearModifierTiersBuilder.add(101, -1, 10, 2.01, 3.50, 1);
                            vaultGearModifierTiersBuilder.add(102, -1, 10, 3.51, 5.00, 1);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.DAMAGE_CHAMPION,  "ModChampionDamage", "mod_champion_damage", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(100, -1, 10, 0.75, 1.25, 1);
                            vaultGearModifierTiersBuilder.add(101, -1, 10, 1.40, 1.80, 1);
                            vaultGearModifierTiersBuilder.add(102, -1, 10, 1.80, 2.40, 1);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.DAMAGE_DUNGEON,   "ModDungeonDamage",  "mod_dungeon_damage",  List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(100, -1, 10, 0.75, 1.25, 1);
                            vaultGearModifierTiersBuilder.add(101, -1, 10, 1.40, 1.80, 1);
                            vaultGearModifierTiersBuilder.add(102, -1, 10, 1.80, 2.40, 1);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.DAMAGE_TANK,      "ModTankDamage",     "mod_tank_damage",     List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(100, -1, 10, 0.75, 1.25, 1);
                            vaultGearModifierTiersBuilder.add(101, -1, 10, 1.40, 1.80, 1);
                            vaultGearModifierTiersBuilder.add(102, -1, 10, 1.80, 2.40, 1);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.DAMAGE_DWELLER,   "ModDwellerDamage",  "mod_dweller_damage",  List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(100, -1, 10, 0.75, 1.25, 1);
                            vaultGearModifierTiersBuilder.add(101, -1, 10, 1.40, 1.80, 1);
                            vaultGearModifierTiersBuilder.add(102, -1, 10, 1.80, 2.40, 1);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.DAMAGE_ASSASSIN,  "ModAssassinDamage", "mod_assassin_damage", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(100, -1, 10, 0.75, 1.25, 1);
                            vaultGearModifierTiersBuilder.add(101, -1, 10, 1.40, 1.80, 1);
                            vaultGearModifierTiersBuilder.add(102, -1, 10, 1.80, 2.40, 1);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.DAMAGE_HORDE,     "ModHordeDamage",    "mod_horde_damage",    List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(100, -1, 10, 0.75, 1.25, 1);
                            vaultGearModifierTiersBuilder.add(101, -1, 10, 1.40, 1.80, 1);
                            vaultGearModifierTiersBuilder.add(102, -1, 10, 1.80, 2.40, 1);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.LUCKY_HIT_CHANCE, "ModLuckyHit",       "mod_lucky_hit",       List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(100, -1, 10, 0.15, 0.19, 1);
                            vaultGearModifierTiersBuilder.add(101, -1, 10, 0.20, 0.24, 1);
                            vaultGearModifierTiersBuilder.add(102, -1, 10, 0.25, 0.30, 1);
                        });
            });

            builder.key(VaultMod.id("dagger_main_mythic")).add(VaultGearTierConfig.ModifierAffixTagGroup.SUFFIX, vaultGearAttributeGroupBuilder -> {
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.DURABILITY,           "ModDurability",     "mod_durability",     List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(100, -1, 10, 4200, 5000,  1);
                            vaultGearModifierTiersBuilder.add(101, -1, 10, 5001, 7400,  1);
                            vaultGearModifierTiersBuilder.add(102, -1, 10, 7401, 10000, 1);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.ATTACK_DAMAGE,        "ModAttackEcho",     "mod_attack_damage",  List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(100, -1, 10, 20, 25, 1);
                            vaultGearModifierTiersBuilder.add(101, -1, 10, 26, 30, 1);
                            vaultGearModifierTiersBuilder.add(102, -1, 10, 31, 40, 1);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(                    ModGearAttributes.ECHOING_CHANCE,       "ModAttackEcho",     "mod_echoing_chance", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(100,  -1, 10, 0.20,  0.26,  0.01);
                            vaultGearModifierTiersBuilder.add(101,  -1, 10, 0.27,  0.33,  0.01);
                            vaultGearModifierTiersBuilder.add(102,  -1, 10, 0.34,  0.40,  0.01);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.ATTACK_RANGE,         "ModRangeSpeed",     "mod_attack_range",   List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(100, -1, 10, 0.50, 0.75, 1);
                            vaultGearModifierTiersBuilder.add(101, -1, 10, 0.76, 1.00, 1);
                            vaultGearModifierTiersBuilder.add(102, -1, 10, 1.01, 1.25, 1);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.ATTACK_SPEED_PERCENT, "ModRangeSpeed",     "mod_attack_speed",   List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(100, -1, 10, 0.26, 0.30, 1);
                            vaultGearModifierTiersBuilder.add(101, -1, 10, 0.31, 0.35, 1);
                            vaultGearModifierTiersBuilder.add(102, -1, 10, 0.36, 0.40, 1);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.EFFECT_CLOUD,         "ModHealBleedCloud", "mod_bleed_cloud",    List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(100,-1, 4, "Bleed IV",  ResourceLocation.withDefaultNamespace("empty"), 160, 5.0f, ModEffects.BLEED.getColor(), false, 0.03F, ModEffects.BLEED.getRegistryName(), 40, 3);
                            vaultGearModifierTiersBuilder.add(101,-1, 4, "Bleed V",   ResourceLocation.withDefaultNamespace("empty"), 200, 5.0f, ModEffects.BLEED.getColor(), false, 0.03F, ModEffects.BLEED.getRegistryName(), 40, 4);
                            vaultGearModifierTiersBuilder.add(102,-1, 4, "Bleed VI",  ResourceLocation.withDefaultNamespace("empty"), 240, 5.0f, ModEffects.BLEED.getColor(), false, 0.03F, ModEffects.BLEED.getRegistryName(), 40, 5);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.EFFECT_CLOUD,         "ModHealBleedCloud", "mod_heal_cloud",     List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(100,-1, 4, "Healing IV",  ResourceLocation.withDefaultNamespace("empty"), 200, 5.0f, MobEffects.HEAL.getColor(), true, 0.03F, MobEffects.HEAL.getRegistryName(), 20, 0);
                            vaultGearModifierTiersBuilder.add(101,-1, 4, "Healing V",   ResourceLocation.withDefaultNamespace("empty"), 240, 5.0f, MobEffects.HEAL.getColor(), true, 0.03F, MobEffects.HEAL.getRegistryName(), 20, 1);
                            vaultGearModifierTiersBuilder.add(102,-1, 4, "Healing VI",  ResourceLocation.withDefaultNamespace("empty"), 280, 5.0f, MobEffects.HEAL.getColor(), true, 0.03F, MobEffects.HEAL.getRegistryName(), 20, 2);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.EFFECT_CLOUD,         "ModPoisonCloud",    "mod_poison_cloud",   List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(100,-1, 10, "Poison IV",  ResourceLocation.withDefaultNamespace("empty"), 240, 4.0f, MobEffects.POISON.getColor(), false, 0.075F, MobEffects.POISON.getRegistryName(), 60, 9);
                            vaultGearModifierTiersBuilder.add(101,-1, 10, "Poison V",   ResourceLocation.withDefaultNamespace("empty"), 300, 4.0f, MobEffects.POISON.getColor(), false, 0.075F, MobEffects.POISON.getRegistryName(), 60, 12);
                            vaultGearModifierTiersBuilder.add(102,-1, 10, "Poison VI",  ResourceLocation.withDefaultNamespace("empty"), 300, 4.0f, MobEffects.POISON.getColor(), false, 0.075F, MobEffects.POISON.getRegistryName(), 60, 12);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.EFFECT_CLOUD,         "ModSlownessCloud",  "mod_slowness_cloud", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(100,-1, 10, "Slowness IV",  ResourceLocation.withDefaultNamespace("empty"), 240, 4.0f, MobEffects.MOVEMENT_SLOWDOWN.getColor(), false, 0.075F, MobEffects.MOVEMENT_SLOWDOWN.getRegistryName(), 180, 3);
                            vaultGearModifierTiersBuilder.add(101,-1, 10, "Slowness V",   ResourceLocation.withDefaultNamespace("empty"), 300, 4.0f, MobEffects.MOVEMENT_SLOWDOWN.getColor(), false, 0.075F, MobEffects.MOVEMENT_SLOWDOWN.getRegistryName(), 200, 4);
                            vaultGearModifierTiersBuilder.add(102,-1, 10, "Slowness VI",  ResourceLocation.withDefaultNamespace("empty"), 360, 4.0f, MobEffects.MOVEMENT_SLOWDOWN.getColor(), false, 0.075F, MobEffects.MOVEMENT_SLOWDOWN.getRegistryName(), 240, 5);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.EFFECT_CLOUD,         "ModChillCloud",     "mod_chill_cloud",    List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(100,-1, 10, "Chilling IV",  ResourceLocation.withDefaultNamespace("empty"), 240, 4.0f, ModEffects.CHILLED.getColor(), false, 0.075F, ModEffects.CHILLED.getRegistryName(), 140, 1);
                            vaultGearModifierTiersBuilder.add(101,-1, 10, "Chilling V",   ResourceLocation.withDefaultNamespace("empty"), 300, 4.0f, ModEffects.CHILLED.getColor(), false, 0.075F, ModEffects.CHILLED.getRegistryName(), 140, 1);
                            vaultGearModifierTiersBuilder.add(102,-1, 10, "Chilling VI",  ResourceLocation.withDefaultNamespace("empty"), 360, 4.0f, ModEffects.CHILLED.getColor(), false, 0.075F, ModEffects.CHILLED.getRegistryName(), 140, 1);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.EFFECT_CLOUD,         "ModFearCloud",      "mod_fear_cloud",     List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(101,-1, 10, "Fear IV",  ResourceLocation.withDefaultNamespace("empty"), 240, 4.0f, ModEffects.TAUNT_REPEL_MOB.getColor(), false, 0.075F, ModEffects.TAUNT_REPEL_MOB.getRegistryName(), 120, 0);
                            vaultGearModifierTiersBuilder.add(102,-1, 10, "Fear V",   ResourceLocation.withDefaultNamespace("empty"), 300, 4.0f, ModEffects.TAUNT_REPEL_MOB.getColor(), false, 0.075F, ModEffects.TAUNT_REPEL_MOB.getRegistryName(), 120, 0);
                            vaultGearModifierTiersBuilder.add(102,-1, 10, "Fear VI",  ResourceLocation.withDefaultNamespace("empty"), 360, 4.0f, ModEffects.TAUNT_REPEL_MOB.getColor(), false, 0.075F, ModEffects.TAUNT_REPEL_MOB.getRegistryName(), 120, 0);
                        });
            });
        });

        add("dagger_sub", builder -> {
            builder.key(VaultMod.id("dagger_sub")).add(VaultGearTierConfig.ModifierAffixTagGroup.BASE_ATTRIBUTES, vaultGearAttributeGroupBuilder -> {
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.DURABILITY, "BaseDurability", "base_durability",   List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,  -1, 10, 2400, 4800, 1);
                            vaultGearModifierTiersBuilder.add(21, -1, 10, 3600, 5400, 1);
                            vaultGearModifierTiersBuilder.add(60, -1, 10, 4800, 6000, 1);
                            vaultGearModifierTiersBuilder.add(90, -1, 10, 6000, 8000, 1);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.SOULBOUND,  "BaseSoulbound",  "base_soulbound",    List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, -1, 20, true);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.SOULBOUND,  "BaseSoulbound",  "base_no_soulbound", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, -1, 80, false);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.LIVING,     "BaseLiving",     "base_living",       List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,-1,2,true);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.LIVING,     "BaseLiving",     "base_no_living",    List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,-1,98,false);
                        });
            });

            builder.key(VaultMod.id("dagger_sub")).add(VaultGearTierConfig.ModifierAffixTagGroup.IMPLICIT,        vaultGearAttributeGroupBuilder -> {
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.ATTACK_DAMAGE, "BaseAttackDamage", "base_attack_damage", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,   49, 10, 5,  7,  1);
                            vaultGearModifierTiersBuilder.add(20,  69, 10, 8,  10, 1);
                            vaultGearModifierTiersBuilder.add(40,  89, 10, 11, 13, 1);
                            vaultGearModifierTiersBuilder.add(50,  -1, 10, 14, 16, 1);
                            vaultGearModifierTiersBuilder.add(60,  -1, 10, 17, 19, 1);
                            vaultGearModifierTiersBuilder.add(70,  -1, 10, 20, 22, 1);
                            vaultGearModifierTiersBuilder.add(80,  -1, 10, 23, 25, 1);
                            vaultGearModifierTiersBuilder.add(90,  -1, 10, 26, 30, 1);
                            vaultGearModifierTiersBuilder.add(100, -1, 10, 31, 35, 1);
                        });
            });

            builder.key(VaultMod.id("dagger_sub")).add(VaultGearTierConfig.ModifierAffixTagGroup.PREFIX,          vaultGearAttributeGroupBuilder -> {
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.SOUL_QUANTITY,        "ModSoulChance",     "mod_soul_chance",     List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,   70, 200, 0.20, 0.40, 1);
                            vaultGearModifierTiersBuilder.add(28,  -1, 200, 0.41, 0.80, 1);
                            vaultGearModifierTiersBuilder.add(45,  -1, 200, 0.81, 1.20, 1);
                            vaultGearModifierTiersBuilder.add(75,  -1, 200, 1.21, 1.60, 1);
                            vaultGearModifierTiersBuilder.add(101, -1, 200, 1.60, 2.20, 1);
                            vaultGearModifierTiersBuilder.add(102, -1, 200, 2.20, 3.00, 1);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.ON_HIT_CHAIN,         "ModChaining",       "mod_chaining",        List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,   -1, 10, 1, 1, 1);
                            vaultGearModifierTiersBuilder.add(40,  -1, 10, 2, 2, 1);
                            vaultGearModifierTiersBuilder.add(80,  -1, 10, 3, 3, 1);
                            vaultGearModifierTiersBuilder.add(101, -1, 10, 4, 4, 1);
                            vaultGearModifierTiersBuilder.add(102, -1, 10, 5, 5, 1);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(                    ModGearAttributes.ECHOING_CHANCE,       "ModEchoingChance",  "mod_echoing_chance",  List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(50,   85, 10, 0.05,  0.10,  0.01);
                            vaultGearModifierTiersBuilder.add(60,   -1, 10, 0.11,  0.14,  0.01);
                            vaultGearModifierTiersBuilder.add(70,   -1, 10, 0.15,  0.18,  0.01);
                            vaultGearModifierTiersBuilder.add(90,   -1, 10, 0.19,  0.22,  0.01);
                            vaultGearModifierTiersBuilder.add(101,  -1, 10, 0.23,  0.26,  0.01);
                            vaultGearModifierTiersBuilder.add(102,  -1, 10, 0.27,  0.30,  0.01);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(                    ModGearAttributes.REAVING_DAMAGE,       "ModReaving",        "mod_reaving",         List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,    70, 10, 0.08,  0.18,  0.01);
                            vaultGearModifierTiersBuilder.add(28,   -1, 10, 0.19,  0.21,  0.01);
                            vaultGearModifierTiersBuilder.add(45,   -1, 10, 0.22,  0.24,  0.01);
                            vaultGearModifierTiersBuilder.add(75,   -1, 10, 0.25,  0.28,  0.01);
                            vaultGearModifierTiersBuilder.add(101,  -1, 10, 0.29,  0.37,  0.01);
                            vaultGearModifierTiersBuilder.add(102,  -1, 10, 0.38,  0.50,  0.01);
                        });
            });

            builder.key(VaultMod.id("dagger_sub")).add(VaultGearTierConfig.ModifierAffixTagGroup.SUFFIX,          vaultGearAttributeGroupBuilder -> {
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.DURABILITY,           "ModDurability",     "mod_durability",     List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(11,  66, 200, 400,  800,  1);
                            vaultGearModifierTiersBuilder.add(28,  -1, 200, 801,  1600, 1);
                            vaultGearModifierTiersBuilder.add(46,  -1, 200, 1601, 2400, 1);
                            vaultGearModifierTiersBuilder.add(67,  -1, 200, 2401, 3200, 1);
                            vaultGearModifierTiersBuilder.add(89,  -1, 200, 3201, 4000, 1);
                            vaultGearModifierTiersBuilder.add(101, -1, 100, 4001, 6000, 1);
                            vaultGearModifierTiersBuilder.add(102, -1, 100, 6001, 8000, 1);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.LUCKY_HIT_CHANCE,     "ModLuckyHit",       "mod_lucky_hit",       List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,   -1, 10, 0.05, 0.07, 1);
                            vaultGearModifierTiersBuilder.add(50,  -1, 10, 0.08, 0.11, 1);
                            vaultGearModifierTiersBuilder.add(75,  -1, 10, 0.12, 0.14, 1);
                            vaultGearModifierTiersBuilder.add(101, -1, 10, 0.15, 0.17, 1);
                            vaultGearModifierTiersBuilder.add(102, -1, 10, 0.18, 0.20, 1);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.EFFECT_CLOUD,         "ModHealBleedStorm", "mod_bleed_storm",    List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,  -1, 4, "Blood Storm I",   ResourceLocation.withDefaultNamespace("empty"), 5, 24.0f, ModEffects.BLEED.getColor(), false, 0.03F, ModEffects.BLEED.getRegistryName(), 40, 0);
                            vaultGearModifierTiersBuilder.add(32, -1, 4, "Blood Storm II",  ResourceLocation.withDefaultNamespace("empty"), 5, 28.0f, ModEffects.BLEED.getColor(), false, 0.03F, ModEffects.BLEED.getRegistryName(), 40, 1);
                            vaultGearModifierTiersBuilder.add(64, -1, 4, "Blood Storm III", ResourceLocation.withDefaultNamespace("empty"), 5, 32.0f, ModEffects.BLEED.getColor(), false, 0.03F, ModEffects.BLEED.getRegistryName(), 40, 2);
                            vaultGearModifierTiersBuilder.add(101,-1, 4, "Blood Storm IV",  ResourceLocation.withDefaultNamespace("empty"), 5, 47.0f, ModEffects.BLEED.getColor(), false, 0.03F, ModEffects.BLEED.getRegistryName(), 40, 3);
                            vaultGearModifierTiersBuilder.add(102,-1, 4, "Blood Storm V",   ResourceLocation.withDefaultNamespace("empty"), 5, 94.0f, ModEffects.BLEED.getColor(), false, 0.03F, ModEffects.BLEED.getRegistryName(), 40, 4);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.EFFECT_CLOUD,         "ModHealBleedStorm", "mod_heal_storm",     List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,  -1, 4, "Healing Storm I",   ResourceLocation.withDefaultNamespace("empty"), 5, 24.0f, MobEffects.HEAL.getColor(), true, 0.03F, MobEffects.HEAL.getRegistryName(), 20, 0);
                            vaultGearModifierTiersBuilder.add(32, -1, 4, "Healing Storm II",  ResourceLocation.withDefaultNamespace("empty"), 5, 28.0f, MobEffects.HEAL.getColor(), true, 0.03F, MobEffects.HEAL.getRegistryName(), 20, 0);
                            vaultGearModifierTiersBuilder.add(64, -1, 4, "Healing Storm III", ResourceLocation.withDefaultNamespace("empty"), 5, 32.0f, MobEffects.HEAL.getColor(), true, 0.03F, MobEffects.HEAL.getRegistryName(), 20, 1);
                            vaultGearModifierTiersBuilder.add(101,-1, 4, "Healing Storm IV",  ResourceLocation.withDefaultNamespace("empty"), 5, 47.0f, MobEffects.HEAL.getColor(), true, 0.03F, MobEffects.HEAL.getRegistryName(), 20, 1);
                            vaultGearModifierTiersBuilder.add(102,-1, 4, "Healing Storm V",   ResourceLocation.withDefaultNamespace("empty"), 5, 94.0f, MobEffects.HEAL.getColor(), true, 0.03F, MobEffects.HEAL.getRegistryName(), 20, 2);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.EFFECT_CLOUD,         "ModPoisonStorm",    "mod_poison_storm",   List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,  -1, 10, "Poison Storm I",   ResourceLocation.withDefaultNamespace("empty"), 5, 24.0f, MobEffects.POISON.getColor(), false, 0.075F, MobEffects.POISON.getRegistryName(), 60, 3);
                            vaultGearModifierTiersBuilder.add(32, -1, 10, "Poison Storm II",  ResourceLocation.withDefaultNamespace("empty"), 5, 28.0f, MobEffects.POISON.getColor(), false, 0.075F, MobEffects.POISON.getRegistryName(), 60, 5);
                            vaultGearModifierTiersBuilder.add(64, -1, 10, "Poison Storm III", ResourceLocation.withDefaultNamespace("empty"), 5, 32.0f, MobEffects.POISON.getColor(), false, 0.075F, MobEffects.POISON.getRegistryName(), 60, 7);
                            vaultGearModifierTiersBuilder.add(101,-1, 10, "Poison Storm IV",  ResourceLocation.withDefaultNamespace("empty"), 5, 47.0f, MobEffects.POISON.getColor(), false, 0.075F, MobEffects.POISON.getRegistryName(), 60, 9);
                            vaultGearModifierTiersBuilder.add(102,-1, 10, "Poison Storm V",   ResourceLocation.withDefaultNamespace("empty"), 5, 94.0f, MobEffects.POISON.getColor(), false, 0.075F, MobEffects.POISON.getRegistryName(), 60, 12);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.EFFECT_CLOUD,         "ModSlownessStorm",  "mod_slowness_storm", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,  -1, 10, "Slowness Storm I",   ResourceLocation.withDefaultNamespace("empty"), 5, 24.0f, MobEffects.MOVEMENT_SLOWDOWN.getColor(), false, 0.075F, MobEffects.MOVEMENT_SLOWDOWN.getRegistryName(), 120, 0);
                            vaultGearModifierTiersBuilder.add(32, -1, 10, "Slowness Storm II",  ResourceLocation.withDefaultNamespace("empty"), 5, 28.0f, MobEffects.MOVEMENT_SLOWDOWN.getColor(), false, 0.075F, MobEffects.MOVEMENT_SLOWDOWN.getRegistryName(), 140, 1);
                            vaultGearModifierTiersBuilder.add(64, -1, 10, "Slowness Storm III", ResourceLocation.withDefaultNamespace("empty"), 5, 32.0f, MobEffects.MOVEMENT_SLOWDOWN.getColor(), false, 0.075F, MobEffects.MOVEMENT_SLOWDOWN.getRegistryName(), 160, 2);
                            vaultGearModifierTiersBuilder.add(101,-1, 10, "Slowness Storm IV",  ResourceLocation.withDefaultNamespace("empty"), 5, 47.0f, MobEffects.MOVEMENT_SLOWDOWN.getColor(), false, 0.075F, MobEffects.MOVEMENT_SLOWDOWN.getRegistryName(), 180, 3);
                            vaultGearModifierTiersBuilder.add(102,-1, 10, "Slowness Storm V",   ResourceLocation.withDefaultNamespace("empty"), 5, 4.0f, MobEffects.MOVEMENT_SLOWDOWN.getColor(), false, 0.075F, MobEffects.MOVEMENT_SLOWDOWN.getRegistryName(), 200, 4);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.EFFECT_CLOUD,         "ModChillStorm",     "mod_chill_storm",    List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,  -1, 10, "Chilling Storm I",   ResourceLocation.withDefaultNamespace("empty"), 5, 24.0f, ModEffects.CHILLED.getColor(), false, 0.075F, ModEffects.CHILLED.getRegistryName(), 60, 1);
                            vaultGearModifierTiersBuilder.add(32, -1, 10, "Chilling Storm II",  ResourceLocation.withDefaultNamespace("empty"), 5, 28.0f, ModEffects.CHILLED.getColor(), false, 0.075F, ModEffects.CHILLED.getRegistryName(), 60, 2);
                            vaultGearModifierTiersBuilder.add(64, -1, 10, "Chilling Storm III", ResourceLocation.withDefaultNamespace("empty"), 5, 32.0f, ModEffects.CHILLED.getColor(), false, 0.075F, ModEffects.CHILLED.getRegistryName(), 60, 3);
                            vaultGearModifierTiersBuilder.add(101,-1, 10, "Chilling Storm IV",  ResourceLocation.withDefaultNamespace("empty"), 5, 47.0f, ModEffects.CHILLED.getColor(), false, 0.075F, ModEffects.CHILLED.getRegistryName(), 60, 4);
                            vaultGearModifierTiersBuilder.add(102,-1, 10, "Chilling Storm V",   ResourceLocation.withDefaultNamespace("empty"), 5, 94.0f, ModEffects.CHILLED.getColor(), false, 0.075F, ModEffects.CHILLED.getRegistryName(), 60, 5);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.EFFECT_CLOUD,         "ModFearStorm",      "mod_fear_storm",     List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,  -1, 10, "Fear Storm I",   ResourceLocation.withDefaultNamespace("empty"), 5, 24.0f, ModEffects.TAUNT_REPEL_MOB.getColor(), false, 0.075F, ModEffects.TAUNT_REPEL_MOB.getRegistryName(), 60,  0);
                            vaultGearModifierTiersBuilder.add(32, -1, 10, "Fear Storm II",  ResourceLocation.withDefaultNamespace("empty"), 5, 28.0f, ModEffects.TAUNT_REPEL_MOB.getColor(), false, 0.075F, ModEffects.TAUNT_REPEL_MOB.getRegistryName(), 60,  0);
                            vaultGearModifierTiersBuilder.add(64, -1, 10, "Fear Storm III", ResourceLocation.withDefaultNamespace("empty"), 5, 32.0f, ModEffects.TAUNT_REPEL_MOB.getColor(), false, 0.075F, ModEffects.TAUNT_REPEL_MOB.getRegistryName(), 60,  0);
                            vaultGearModifierTiersBuilder.add(101,-1, 10, "Fear Storm IV",  ResourceLocation.withDefaultNamespace("empty"), 5, 47.0f, ModEffects.TAUNT_REPEL_MOB.getColor(), false, 0.075F, ModEffects.TAUNT_REPEL_MOB.getRegistryName(), 120, 0);
                            vaultGearModifierTiersBuilder.add(102,-1, 10, "Fear Storm V",   ResourceLocation.withDefaultNamespace("empty"), 5, 94.0f, ModEffects.TAUNT_REPEL_MOB.getColor(), false, 0.075F, ModEffects.TAUNT_REPEL_MOB.getRegistryName(), 120, 0);
                        });
            });
        });

        // TODO: Configure Mythic values
        add("dagger_sub_mythic", builder -> {
            builder.key(VaultMod.id("dagger_sub_mythic")).add(VaultGearTierConfig.ModifierAffixTagGroup.BASE_ATTRIBUTES, vaultGearAttributeGroupBuilder -> {
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.DURABILITY, "BaseDurability", "base_durability",   List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,  -1, 10, 2400, 4800, 1);
                            vaultGearModifierTiersBuilder.add(21, -1, 10, 3600, 5400, 1);
                            vaultGearModifierTiersBuilder.add(60, -1, 10, 4800, 6000, 1);
                            vaultGearModifierTiersBuilder.add(90, -1, 10, 6000, 8000, 1);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.SOULBOUND,  "BaseSoulbound",  "base_soulbound",    List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, -1, 20, true);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.SOULBOUND,  "BaseSoulbound",  "base_no_soulbound", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0, -1, 80, false);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.LIVING,     "BaseLiving",     "base_living",       List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,-1,2,true);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.LIVING,     "BaseLiving",     "base_no_living",    List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,-1,98,false);
                        });
            });

            builder.key(VaultMod.id("dagger_sub_mythic")).add(VaultGearTierConfig.ModifierAffixTagGroup.IMPLICIT,        vaultGearAttributeGroupBuilder -> {
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.ATTACK_DAMAGE, "BaseAttackDamage", "base_attack_damage", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,   49, 10, 5,  7,  1);
                            vaultGearModifierTiersBuilder.add(20,  69, 10, 8,  10, 1);
                            vaultGearModifierTiersBuilder.add(40,  89, 10, 11, 13, 1);
                            vaultGearModifierTiersBuilder.add(50,  -1, 10, 14, 16, 1);
                            vaultGearModifierTiersBuilder.add(60,  -1, 10, 17, 19, 1);
                            vaultGearModifierTiersBuilder.add(70,  -1, 10, 20, 22, 1);
                            vaultGearModifierTiersBuilder.add(80,  -1, 10, 23, 25, 1);
                            vaultGearModifierTiersBuilder.add(90,  -1, 10, 26, 30, 1);
                            vaultGearModifierTiersBuilder.add(100, -1, 10, 31, 35, 1);
                        });
            });

            builder.key(VaultMod.id("dagger_sub_mythic")).add(VaultGearTierConfig.ModifierAffixTagGroup.PREFIX,          vaultGearAttributeGroupBuilder -> {
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.SOUL_QUANTITY,        "ModSoulChance",     "mod_soul_chance",     List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,   70, 200, 0.20, 0.40, 1);
                            vaultGearModifierTiersBuilder.add(28,  -1, 200, 0.41, 0.80, 1);
                            vaultGearModifierTiersBuilder.add(45,  -1, 200, 0.81, 1.20, 1);
                            vaultGearModifierTiersBuilder.add(75,  -1, 200, 1.21, 1.60, 1);
                            vaultGearModifierTiersBuilder.add(101, -1, 200, 1.60, 2.20, 1);
                            vaultGearModifierTiersBuilder.add(102, -1, 200, 2.20, 3.00, 1);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.ON_HIT_CHAIN,         "ModChaining",       "mod_chaining",        List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,   -1, 10, 1, 1, 1);
                            vaultGearModifierTiersBuilder.add(40,  -1, 10, 2, 2, 1);
                            vaultGearModifierTiersBuilder.add(80,  -1, 10, 3, 3, 1);
                            vaultGearModifierTiersBuilder.add(101, -1, 10, 4, 4, 1);
                            vaultGearModifierTiersBuilder.add(102, -1, 10, 5, 5, 1);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(                    ModGearAttributes.ECHOING_CHANCE,       "ModEchoingChance",  "mod_echoing_chance",  List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(50,   85, 10, 0.05,  0.10,  0.01);
                            vaultGearModifierTiersBuilder.add(60,   -1, 10, 0.11,  0.14,  0.01);
                            vaultGearModifierTiersBuilder.add(70,   -1, 10, 0.15,  0.18,  0.01);
                            vaultGearModifierTiersBuilder.add(90,   -1, 10, 0.19,  0.22,  0.01);
                            vaultGearModifierTiersBuilder.add(101,  -1, 10, 0.23,  0.26,  0.01);
                            vaultGearModifierTiersBuilder.add(102,  -1, 10, 0.27,  0.30,  0.01);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(                    ModGearAttributes.REAVING_DAMAGE,       "ModReaving",        "mod_reaving",         List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,    70, 10, 0.08,  0.18,  0.01);
                            vaultGearModifierTiersBuilder.add(28,   -1, 10, 0.19,  0.21,  0.01);
                            vaultGearModifierTiersBuilder.add(45,   -1, 10, 0.22,  0.24,  0.01);
                            vaultGearModifierTiersBuilder.add(75,   -1, 10, 0.25,  0.28,  0.01);
                            vaultGearModifierTiersBuilder.add(101,  -1, 10, 0.29,  0.37,  0.01);
                            vaultGearModifierTiersBuilder.add(102,  -1, 10, 0.38,  0.50,  0.01);
                        });
            });

            builder.key(VaultMod.id("dagger_sub_mythic")).add(VaultGearTierConfig.ModifierAffixTagGroup.SUFFIX,          vaultGearAttributeGroupBuilder -> {
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.DURABILITY,           "ModDurability",     "mod_durability",     List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(11,  66, 200, 400,  800,  1);
                            vaultGearModifierTiersBuilder.add(28,  -1, 200, 801,  1600, 1);
                            vaultGearModifierTiersBuilder.add(46,  -1, 200, 1601, 2400, 1);
                            vaultGearModifierTiersBuilder.add(67,  -1, 200, 2401, 3200, 1);
                            vaultGearModifierTiersBuilder.add(89,  -1, 200, 3201, 4000, 1);
                            vaultGearModifierTiersBuilder.add(101, -1, 100, 4001, 6000, 1);
                            vaultGearModifierTiersBuilder.add(102, -1, 100, 6001, 8000, 1);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.LUCKY_HIT_CHANCE,     "ModLuckyHit",       "mod_lucky_hit",       List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,   -1, 10, 0.05, 0.07, 1);
                            vaultGearModifierTiersBuilder.add(50,  -1, 10, 0.08, 0.11, 1);
                            vaultGearModifierTiersBuilder.add(75,  -1, 10, 0.12, 0.14, 1);
                            vaultGearModifierTiersBuilder.add(101, -1, 10, 0.15, 0.17, 1);
                            vaultGearModifierTiersBuilder.add(102, -1, 10, 0.18, 0.20, 1);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.EFFECT_CLOUD,         "ModHealBleedStorm", "mod_bleed_storm",    List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,  -1, 4, "Blood Storm I",   ResourceLocation.withDefaultNamespace("empty"), 5, 24.0f, ModEffects.BLEED.getColor(), false, 0.03F, ModEffects.BLEED.getRegistryName(), 40, 0);
                            vaultGearModifierTiersBuilder.add(32, -1, 4, "Blood Storm II",  ResourceLocation.withDefaultNamespace("empty"), 5, 28.0f, ModEffects.BLEED.getColor(), false, 0.03F, ModEffects.BLEED.getRegistryName(), 40, 1);
                            vaultGearModifierTiersBuilder.add(64, -1, 4, "Blood Storm III", ResourceLocation.withDefaultNamespace("empty"), 5, 32.0f, ModEffects.BLEED.getColor(), false, 0.03F, ModEffects.BLEED.getRegistryName(), 40, 2);
                            vaultGearModifierTiersBuilder.add(101,-1, 4, "Blood Storm IV",  ResourceLocation.withDefaultNamespace("empty"), 5, 47.0f, ModEffects.BLEED.getColor(), false, 0.03F, ModEffects.BLEED.getRegistryName(), 40, 3);
                            vaultGearModifierTiersBuilder.add(102,-1, 4, "Blood Storm V",   ResourceLocation.withDefaultNamespace("empty"), 5, 94.0f, ModEffects.BLEED.getColor(), false, 0.03F, ModEffects.BLEED.getRegistryName(), 40, 4);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.EFFECT_CLOUD,         "ModHealBleedStorm", "mod_heal_storm",     List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,  -1, 4, "Healing Storm I",   ResourceLocation.withDefaultNamespace("empty"), 5, 24.0f, MobEffects.HEAL.getColor(), true, 0.03F, MobEffects.HEAL.getRegistryName(), 20, 0);
                            vaultGearModifierTiersBuilder.add(32, -1, 4, "Healing Storm II",  ResourceLocation.withDefaultNamespace("empty"), 5, 28.0f, MobEffects.HEAL.getColor(), true, 0.03F, MobEffects.HEAL.getRegistryName(), 20, 0);
                            vaultGearModifierTiersBuilder.add(64, -1, 4, "Healing Storm III", ResourceLocation.withDefaultNamespace("empty"), 5, 32.0f, MobEffects.HEAL.getColor(), true, 0.03F, MobEffects.HEAL.getRegistryName(), 20, 1);
                            vaultGearModifierTiersBuilder.add(101,-1, 4, "Healing Storm IV",  ResourceLocation.withDefaultNamespace("empty"), 5, 47.0f, MobEffects.HEAL.getColor(), true, 0.03F, MobEffects.HEAL.getRegistryName(), 20, 1);
                            vaultGearModifierTiersBuilder.add(102,-1, 4, "Healing Storm V",   ResourceLocation.withDefaultNamespace("empty"), 5, 94.0f, MobEffects.HEAL.getColor(), true, 0.03F, MobEffects.HEAL.getRegistryName(), 20, 2);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.EFFECT_CLOUD,         "ModPoisonStorm",    "mod_poison_storm",   List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,  -1, 10, "Poison Storm I",   ResourceLocation.withDefaultNamespace("empty"), 5, 24.0f, MobEffects.POISON.getColor(), false, 0.075F, MobEffects.POISON.getRegistryName(), 60, 3);
                            vaultGearModifierTiersBuilder.add(32, -1, 10, "Poison Storm II",  ResourceLocation.withDefaultNamespace("empty"), 5, 28.0f, MobEffects.POISON.getColor(), false, 0.075F, MobEffects.POISON.getRegistryName(), 60, 5);
                            vaultGearModifierTiersBuilder.add(64, -1, 10, "Poison Storm III", ResourceLocation.withDefaultNamespace("empty"), 5, 32.0f, MobEffects.POISON.getColor(), false, 0.075F, MobEffects.POISON.getRegistryName(), 60, 7);
                            vaultGearModifierTiersBuilder.add(101,-1, 10, "Poison Storm IV",  ResourceLocation.withDefaultNamespace("empty"), 5, 47.0f, MobEffects.POISON.getColor(), false, 0.075F, MobEffects.POISON.getRegistryName(), 60, 9);
                            vaultGearModifierTiersBuilder.add(102,-1, 10, "Poison Storm V",   ResourceLocation.withDefaultNamespace("empty"), 5, 94.0f, MobEffects.POISON.getColor(), false, 0.075F, MobEffects.POISON.getRegistryName(), 60, 12);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.EFFECT_CLOUD,         "ModSlownessStorm",  "mod_slowness_storm", List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,  -1, 10, "Slowness Storm I",   ResourceLocation.withDefaultNamespace("empty"), 5, 24.0f, MobEffects.MOVEMENT_SLOWDOWN.getColor(), false, 0.075F, MobEffects.MOVEMENT_SLOWDOWN.getRegistryName(), 120, 0);
                            vaultGearModifierTiersBuilder.add(32, -1, 10, "Slowness Storm II",  ResourceLocation.withDefaultNamespace("empty"), 5, 28.0f, MobEffects.MOVEMENT_SLOWDOWN.getColor(), false, 0.075F, MobEffects.MOVEMENT_SLOWDOWN.getRegistryName(), 140, 1);
                            vaultGearModifierTiersBuilder.add(64, -1, 10, "Slowness Storm III", ResourceLocation.withDefaultNamespace("empty"), 5, 32.0f, MobEffects.MOVEMENT_SLOWDOWN.getColor(), false, 0.075F, MobEffects.MOVEMENT_SLOWDOWN.getRegistryName(), 160, 2);
                            vaultGearModifierTiersBuilder.add(101,-1, 10, "Slowness Storm IV",  ResourceLocation.withDefaultNamespace("empty"), 5, 47.0f, MobEffects.MOVEMENT_SLOWDOWN.getColor(), false, 0.075F, MobEffects.MOVEMENT_SLOWDOWN.getRegistryName(), 180, 3);
                            vaultGearModifierTiersBuilder.add(102,-1, 10, "Slowness Storm V",   ResourceLocation.withDefaultNamespace("empty"), 5, 4.0f, MobEffects.MOVEMENT_SLOWDOWN.getColor(), false, 0.075F, MobEffects.MOVEMENT_SLOWDOWN.getRegistryName(), 200, 4);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.EFFECT_CLOUD,         "ModChillStorm",     "mod_chill_storm",    List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,  -1, 10, "Chilling Storm I",   ResourceLocation.withDefaultNamespace("empty"), 5, 24.0f, ModEffects.CHILLED.getColor(), false, 0.075F, ModEffects.CHILLED.getRegistryName(), 60, 1);
                            vaultGearModifierTiersBuilder.add(32, -1, 10, "Chilling Storm II",  ResourceLocation.withDefaultNamespace("empty"), 5, 28.0f, ModEffects.CHILLED.getColor(), false, 0.075F, ModEffects.CHILLED.getRegistryName(), 60, 2);
                            vaultGearModifierTiersBuilder.add(64, -1, 10, "Chilling Storm III", ResourceLocation.withDefaultNamespace("empty"), 5, 32.0f, ModEffects.CHILLED.getColor(), false, 0.075F, ModEffects.CHILLED.getRegistryName(), 60, 3);
                            vaultGearModifierTiersBuilder.add(101,-1, 10, "Chilling Storm IV",  ResourceLocation.withDefaultNamespace("empty"), 5, 47.0f, ModEffects.CHILLED.getColor(), false, 0.075F, ModEffects.CHILLED.getRegistryName(), 60, 4);
                            vaultGearModifierTiersBuilder.add(102,-1, 10, "Chilling Storm V",   ResourceLocation.withDefaultNamespace("empty"), 5, 94.0f, ModEffects.CHILLED.getColor(), false, 0.075F, ModEffects.CHILLED.getRegistryName(), 60, 5);
                        });
                vaultGearAttributeGroupBuilder
                        .addModifier(iskallia.vault.init.ModGearAttributes.EFFECT_CLOUD,         "ModFearStorm",      "mod_fear_storm",     List.of(), vaultGearModifierTiersBuilder -> {
                            vaultGearModifierTiersBuilder.add(0,  -1, 10, "Fear Storm I",   ResourceLocation.withDefaultNamespace("empty"), 5, 24.0f, ModEffects.TAUNT_REPEL_MOB.getColor(), false, 0.075F, ModEffects.TAUNT_REPEL_MOB.getRegistryName(), 60,  0);
                            vaultGearModifierTiersBuilder.add(32, -1, 10, "Fear Storm II",  ResourceLocation.withDefaultNamespace("empty"), 5, 28.0f, ModEffects.TAUNT_REPEL_MOB.getColor(), false, 0.075F, ModEffects.TAUNT_REPEL_MOB.getRegistryName(), 60,  0);
                            vaultGearModifierTiersBuilder.add(64, -1, 10, "Fear Storm III", ResourceLocation.withDefaultNamespace("empty"), 5, 32.0f, ModEffects.TAUNT_REPEL_MOB.getColor(), false, 0.075F, ModEffects.TAUNT_REPEL_MOB.getRegistryName(), 60,  0);
                            vaultGearModifierTiersBuilder.add(101,-1, 10, "Fear Storm IV",  ResourceLocation.withDefaultNamespace("empty"), 5, 47.0f, ModEffects.TAUNT_REPEL_MOB.getColor(), false, 0.075F, ModEffects.TAUNT_REPEL_MOB.getRegistryName(), 120, 0);
                            vaultGearModifierTiersBuilder.add(102,-1, 10, "Fear Storm V",   ResourceLocation.withDefaultNamespace("empty"), 5, 94.0f, ModEffects.TAUNT_REPEL_MOB.getColor(), false, 0.075F, ModEffects.TAUNT_REPEL_MOB.getRegistryName(), 120, 0);
                        });
            });
        });
    }
}
