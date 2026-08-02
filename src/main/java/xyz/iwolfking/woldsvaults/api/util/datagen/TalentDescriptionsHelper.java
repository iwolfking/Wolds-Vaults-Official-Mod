package xyz.iwolfking.woldsvaults.api.util.datagen;

import com.google.gson.JsonArray;
import iskallia.vault.VaultMod;
import iskallia.vault.config.AbilitiesConfig;
import iskallia.vault.config.SkillDescriptionsConfig;
import iskallia.vault.config.TalentsConfig;
import iskallia.vault.effect.GlacialShatterEffect;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeRegistry;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.attribute.config.ConfigurableAttributeGenerator;
import iskallia.vault.gear.attribute.config.DoubleAttributeGenerator;
import iskallia.vault.gear.attribute.config.FloatAttributeGenerator;
import iskallia.vault.gear.attribute.config.IntegerAttributeGenerator;
import iskallia.vault.gear.attribute.type.VaultGearAttributeType;
import iskallia.vault.gear.reader.IntegerModifierReader;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.skill.base.GroupedSkill;
import iskallia.vault.skill.base.LearnableSkill;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.TieredSkill;
import iskallia.vault.skill.expertise.type.FarmerTwerker;
import iskallia.vault.skill.talent.type.*;
import iskallia.vault.skill.talent.type.health.HighHealthGearAttributeTalent;
import iskallia.vault.skill.talent.type.health.LowHealthDamageTalent;
import iskallia.vault.skill.talent.type.health.LowHealthGearAttributeTalent;
import iskallia.vault.skill.talent.type.health.LowHealthResistanceTalent;
import iskallia.vault.skill.talent.type.luckyhit.DamageLuckyHitTalent;
import iskallia.vault.skill.talent.type.luckyhit.HealthLeechLuckyHitTalent;
import iskallia.vault.skill.talent.type.luckyhit.ManaLeechLuckyHitTalent;
import iskallia.vault.skill.talent.type.luckyhit.SweepingLuckyHitTalent;
import iskallia.vault.skill.talent.type.mana.HighManaGearAttributeTalent;
import iskallia.vault.skill.talent.type.mana.LowManaDamageTalent;
import iskallia.vault.skill.talent.type.mana.LowManaHealingEfficiencyTalent;
import iskallia.vault.skill.talent.type.onhit.CastOnHitTalent;
import iskallia.vault.skill.talent.type.onhit.DamageOnHitTalent;
import iskallia.vault.skill.talent.type.onhit.EffectOnHitTalent;
import iskallia.vault.skill.talent.type.onkill.CastOnKillTalent;
import xyz.iwolfking.vhapi.api.datagen.AbstractSkillDescriptionsProvider;
import xyz.iwolfking.vhapi.api.util.builder.description.JsonDescription;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.ComponentUtils;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.*;

import java.util.Random;
import java.util.function.Consumer;

public class TalentDescriptionsHelper {

    public static void appendOverlevelDescription(String skillId, AbstractSkillDescriptionsProvider.Builder builder, String action, String overlevelBonusDesc, String descriptionColor) {
        if(ModConfigs.TALENTS == null) {
            ModConfigs.TALENTS = new TalentsConfig().readConfig();
        }

        if(ModConfigs.SKILL_DESCRIPTIONS == null) {
            ModConfigs.SKILL_DESCRIPTIONS = new SkillDescriptionsConfig().readConfig();
        }

        Skill skill = ModConfigs.TALENTS.tree.getForId(skillId).orElse(null);

        if(skill == null) {
            return;
        }

        builder.addDescription(skill.getId(), jsonElements -> {
            ComponentUtils.toJsonArray(ModConfigs.SKILL_DESCRIPTIONS.getDescriptionFor(skill.getId())).forEach(jsonElements::add);
            appendOverlevelDescription(skill.getName(), action, overlevelBonusDesc, descriptionColor, jsonElements);
        });
    }

    public static void appendOverlevelDescription(String talentName, String action, String overlevelDescription, String descriptionColor, JsonArray jsonElements) {
            jsonElements.add(JsonDescription.text("\n\n"));
            jsonElements.add(JsonDescription.text("Overlevels of the "));
            jsonElements.add(JsonDescription.text(talentName, "gold"));
            jsonElements.add(JsonDescription.text(" talent "));
            jsonElements.add(JsonDescription.text(action + " "));
            jsonElements.add(JsonDescription.text(overlevelDescription, descriptionColor));
    }

    public enum NumericKind { INT, FLOAT, DOUBLE, OTHER }

    public static NumericKind detectNumericKind(VaultGearAttribute<?> attr) {

        if(attr.getRegistryName().equals(VaultMod.id("ability_power"))) {
            return NumericKind.INT;
        }

        if(attr.getGenerator() instanceof FloatAttributeGenerator) {
            return NumericKind.FLOAT;
        }
        else if(attr.getGenerator() instanceof IntegerAttributeGenerator) {
            return NumericKind.INT;
        }
        else if(attr.getGenerator() instanceof DoubleAttributeGenerator) {
            return NumericKind.DOUBLE;
        }

        return NumericKind.OTHER;
    }


}
