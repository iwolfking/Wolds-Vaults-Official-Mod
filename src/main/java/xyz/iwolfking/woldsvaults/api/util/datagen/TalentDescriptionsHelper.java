package xyz.iwolfking.woldsvaults.api.util.datagen;

import com.google.gson.JsonArray;
import iskallia.vault.config.AbilitiesConfig;
import iskallia.vault.config.SkillDescriptionsConfig;
import iskallia.vault.config.TalentsConfig;
import iskallia.vault.gear.attribute.type.VaultGearAttributeType;
import iskallia.vault.init.ModConfigs;
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
import iskallia.vault.skill.talent.type.mana.HighManaGearAttributeTalent;
import iskallia.vault.skill.talent.type.mana.LowManaDamageTalent;
import iskallia.vault.skill.talent.type.mana.LowManaHealingEfficiencyTalent;
import iskallia.vault.skill.talent.type.onhit.CastOnHitTalent;
import iskallia.vault.skill.talent.type.onhit.DamageOnHitTalent;
import iskallia.vault.skill.talent.type.onhit.EffectOnHitTalent;
import iskallia.vault.skill.talent.type.onkill.CastOnKillTalent;
import xyz.iwolfking.vhapi.api.datagen.AbstractSkillDescriptionsProvider;
import xyz.iwolfking.vhapi.api.util.builder.description.JsonDescription;
import xyz.iwolfking.woldsvaults.api.util.ComponentUtils;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.*;

public class TalentDescriptionsHelper {

    public static void generateTalentDescriptions(AbstractSkillDescriptionsProvider.Builder builder, SkillDescriptionsConfig talentsDescriptions) {
        ModConfigs.TALENTS = new TalentsConfig().readConfig();
        ModConfigs.ABILITIES = new AbilitiesConfig().readConfig();
        ModConfigs.TALENTS.tree.skills.forEach(skill -> {
            if(skill instanceof GroupedSkill groupedSkill) {
                groupedSkill.getChildren().forEach(child -> {
                    generateDescriptions(child, talentsDescriptions, builder);
                });
            }
            else {
                generateDescriptions(skill, talentsDescriptions, builder);
            }
        });
    }

    private static void generateDescriptions(Skill skill, SkillDescriptionsConfig talentsDescriptions, AbstractSkillDescriptionsProvider.Builder builder) {
        builder.addDescription(skill.getId(), jsonElements -> {
            ComponentUtils.toJsonArray(talentsDescriptions.getDescriptionFor(skill.getId())).forEach(jsonElements::add);
            jsonElements.add(JsonDescription.simple("\n\n"));
            processTalentDescriptionsByType(skill, jsonElements);
        });
    }

    private static void processTalentDescriptionsByType(Skill skill, JsonArray jsonElements) {
        if(skill instanceof TieredSkill tieredSkill) {
            for(int i = 0; i < tieredSkill.getTiers().size(); i++) {
                LearnableSkill tier = tieredSkill.getTiers().get(i);
                jsonElements.add(JsonDescription.simple(i + 1 + " "));
                if(tier instanceof EffectTalent effectTalent) {
                    jsonElements.add(JsonDescription.simple("+" + effectTalent.getAmplifier()+ "\n", "green"));
                }
                else if(tier instanceof PuristTalent puristTalent) {
                    jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", (puristTalent.getDamageIncrease() * 100)) + "% damage per piece\n", "#C23627"));
                }
                else if(tier instanceof VanillaAttributeTalent vanillaAttributeTalent) {
                    jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", (((VanillaAttributeTalentAccessor)vanillaAttributeTalent).getAmount() * 100)) + "%\n", "#90FF00"));
                }
                else if(tier instanceof LowHealthResistanceTalent lowHealthResistanceTalent) {
                    jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", lowHealthResistanceTalent.getAdditionalResistance() * 100) + "%\n", "#90FF00"));
                }
                else if(tier instanceof LowHealthDamageTalent lowHealthDamageTalent) {
                    jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", ((LowHealthDamageTalentAccessor)lowHealthDamageTalent).getDamageIncrease() * 100) + "%\n", "#90FF00"));
                }
                else if(tier instanceof HighHealthGearAttributeTalent highHealthGearAttributeTalent) {
                    jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", ((HighHealthGearAttributeTalentAccessor)highHealthGearAttributeTalent).getValue() * 100) + "%\n", "#90FF00"));
                }
                else if(tier instanceof LowHealthGearAttributeTalent lowHealthGearAttributeTalent) {
                    jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", ((LowHealthGearAttributeTalentAccessor)lowHealthGearAttributeTalent).getValue() * 100) + "%\n", "#90FF00"));
                }
                else if(tier instanceof LightningDamageTalent lightningDamageTalent) {
                    jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", lightningDamageTalent.getIncreasedDamageDealtPercentage() * 100) + "%\n", "#90FF00"));
                }
                else if(tier instanceof LightningStunTalent lightningStunTalent) {
                    jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", lightningStunTalent.getStunChance() * 100) + "%\n", "#90FF00"));
                }
                else if(tier instanceof HighManaGearAttributeTalent highManaGearAttributeTalent) {
                    jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", ((HighManaGearAttributeTalentAccessor)highManaGearAttributeTalent).getValue() * 100) + "%\n", "#90FF00"));
                }
                else if(tier instanceof LowManaHealingEfficiencyTalent lowManaHealingEfficiencyTalent) {
                    jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", lowManaHealingEfficiencyTalent.getAdditionalHealingEfficiency() * 100) + "%\n", "#90FF00"));
                }
                else if(tier instanceof LowManaDamageTalent lowManaDamageTalent) {
                    jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", ((LowManaDamageTalentAccessor)lowManaDamageTalent).getDamageIncrease() * 100) + "%\n", "#90FF00"));
                }
                else if(tier instanceof DamageLuckyHitTalent damageLuckyHitTalent) {
                    jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", damageLuckyHitTalent.getDamageIncrease() * 100) + "%\n", "#90FF00"));
                }
                else if(tier instanceof ManaLeechLuckyHitTalent manaLeechLuckyHitTalent) {
                    jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", ((ManaLeechLuckyHitTalentAccessor)manaLeechLuckyHitTalent).getMaxManaPercentage() * 100) + "%\n", "#90FF00"));
                }
                else if(tier instanceof HealthLeechLuckyHitTalent healthLeechLuckyHitTalent) {
                    jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", ((HealthLeechLuckyHitTalentAccessor)healthLeechLuckyHitTalent).getMaxHealthPercentage() * 100) + "%\n", "#90FF00"));
                }
                else if(tier instanceof PrudentTalent prudentTalent) {
                    jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", prudentTalent.getProbability() * 100) + "%\n", "#90FF00"));
                }
                else if(tier instanceof EffectOnHitTalent effectOnHitTalent) {
                    jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", ((EffectOnHitTalentAccessor)effectOnHitTalent).getProbability() * 100) + "% chance\n", "#90FF00"));
                    jsonElements.add(JsonDescription.simple(" - " + effectOnHitTalent.getEffect().getDisplayName().getString() + " Level " + ((EffectOnHitTalentAccessor)effectOnHitTalent).getAmplifier() + ", " + (effectOnHitTalent.getUnmodifiedDuration() / 20) + " seconds\n", "#90FF00"));
                }
                else if(tier instanceof CastOnHitTalent castOnHitTalent) {
                    jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", ((CastOnHitTalentAccessor)castOnHitTalent).getProbability() * 100) + "%", "#90FF00"));
                    Skill ability = ModConfigs.ABILITIES.getAbilityById(((CastOnHitTalentAccessor)castOnHitTalent).getAbilityName()).orElse(null);
                    if(ability != null) {
                        jsonElements.add(JsonDescription.simple(" - " + ability.getName() + "\n", "#90FF00"));
                    }
                    else {
                        jsonElements.add(JsonDescription.simple(" - " + ((CastOnHitTalentAccessor)castOnHitTalent).getAbilityName() + "\n", "#90FF00"));
                    }
                }
                else if(tier instanceof CastOnKillTalent castOnKillTalent) {
                    jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", ((CastOnKillTalentAccessor)castOnKillTalent).getProbability() * 100) + "%", "#90FF00"));
                    Skill ability = ModConfigs.ABILITIES.getAbilityById(((CastOnKillTalentAccessor)castOnKillTalent).getAbilityName()).orElse(null);
                    if(ability != null) {
                        jsonElements.add(JsonDescription.simple(" - " + ability.getName() + "\n", "#90FF00"));
                    }
                    else {
                        jsonElements.add(JsonDescription.simple(" - " + ((CastOnKillTalentAccessor)castOnKillTalent).getAbilityName() + "\n", "#90FF00"));
                    }
                }
                else if(tier instanceof DamageOnHitTalent damageOnHitTalent) {
                    jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", ((DamageOnHitTalentAccessor)damageOnHitTalent).getDamageIncrease() * 100) + "%", "#90FF00"));
                }
                else if(tier instanceof StackingGearAttributeTalent stackingGearAttributeTalent) {
                    jsonElements.add(JsonDescription.simple(((StackingGearAttributeTalentAccessor)stackingGearAttributeTalent).getMaxStacks() + " max stacks", "#90FF00"));
                    jsonElements.add(JsonDescription.simple(", "));
                    jsonElements.add(JsonDescription.simple(stackingGearAttributeTalent.getUnmodifiedDurationTicks() / 20 + " second wear-off time\n", "#90FF00"));
                }
                else if(tier instanceof JavelinDamageTalent javelinDamageTalent) {
                    jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", javelinDamageTalent.getIncreasedDamage() * 100) + "%\n", "#FF00CB"));
                }
                else if(tier instanceof JavelinFrugalTalent javelinFrugalTalent) {
                    jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", javelinFrugalTalent.getFrugalChance() * 100) + "%\n", "#FF00CB"));
                }
                else if(tier instanceof JavelinThrowPowerTalent javelinThrowPowerTalent) {
                    jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", javelinThrowPowerTalent.getThrowPower() * 100) + "%\n", "#FF00CB"));
                }
                else if(tier instanceof FarmerTwerker farmerTwerker) {
                    jsonElements.add(JsonDescription.simple("+" + farmerTwerker.getHorizontalRange() + " Radius\n", "#FF00CB"));
                    jsonElements.add(JsonDescription.simple(", "));
                    jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", (1.0 + ((20 - farmerTwerker.getTickDelay()) / 10))) + "% Growth Speed\n", "#FF00CB"));
                }
                else if(tier instanceof GearAttributeTalent gearAttributeTalent) {
                    if(gearAttributeTalent.getAttribute().getType().equals(VaultGearAttributeType.floatType())) {
                        jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", gearAttributeTalent.getValue() * 100) + "%\n", "#FF00CB"));
                    }
                    else if(gearAttributeTalent.getAttribute().getType().equals(VaultGearAttributeType.doubleType())) {
                        jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", gearAttributeTalent.getValue() * 100) + "%\n", "#FF00CB"));
                    }
                    else if(gearAttributeTalent.getAttribute().getType().equals(VaultGearAttributeType.intType())) {
                        jsonElements.add(JsonDescription.simple("+" + gearAttributeTalent.getValue() + "\n", "#FF00CB"));
                    }
                }
                else if(i + 1 == tieredSkill.getMaxLearnableTier() && tieredSkill.getTiers().size() != tieredSkill.getMaxLearnableTier()) {
                    jsonElements.add(JsonDescription.simple("Overlevels\n", "#EFBF04"));
                    jsonElements.add(JsonDescription.simple("------------", "#EFBF04"));
                    jsonElements.add(JsonDescription.simple("\n"));
                }
            }
        }
        if(skill instanceof GroupedSkill groupedSkill) {
            groupedSkill.getChildren().forEach(learnableSkill -> {
                processTalentDescriptionsByType(learnableSkill, jsonElements);
            });
        }
    }

}
