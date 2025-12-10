package xyz.iwolfking.woldsvaults.api.util.datagen;

import com.google.gson.JsonArray;
import iskallia.vault.config.PrestigePowersConfig;
import iskallia.vault.config.SkillDescriptionsConfig;
import iskallia.vault.config.TalentsConfig;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.skill.base.LearnableSkill;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.TieredSkill;
import iskallia.vault.skill.prestige.*;
import xyz.iwolfking.vhapi.api.datagen.AbstractSkillDescriptionsProvider;
import xyz.iwolfking.vhapi.api.util.builder.description.JsonDescription;
import xyz.iwolfking.vhapi.mixin.accessors.SkillDescriptionsConfigAccessor;
import xyz.iwolfking.woldsvaults.api.util.ComponentUtils;
import xyz.iwolfking.woldsvaults.prestige.ReachPrestigePower;

public class PrestigePowersDescriptionsHelper {

    public static void generateDescriptions(AbstractSkillDescriptionsProvider.Builder builder, SkillDescriptionsConfig prestigeDescriptions) {
        ModConfigs.PRESTIGE_POWERS = new PrestigePowersConfig().readConfig();
        ModConfigs.PRESTIGE_POWERS.tree.skills.forEach(skill -> {
                createDescriptionsForPrestige(skill, prestigeDescriptions, builder);
        });
    }

    private static void createDescriptionsForPrestige(Skill skill, SkillDescriptionsConfig prestigeDescriptions, AbstractSkillDescriptionsProvider.Builder builder) {
        if(!((SkillDescriptionsConfigAccessor)prestigeDescriptions).getDescriptions().containsKey(skill.getId())) {
            return;
        }

        builder.addDescription(skill.getId(), jsonElements -> {
            ComponentUtils.toJsonArray(prestigeDescriptions.getDescriptionFor(skill.getId())).forEach(jsonElements::add);
            jsonElements.add(JsonDescription.simple("\n\n"));
            processPrestigeDescriptionByType(skill, jsonElements);
        });
    }

    private static void processPrestigeDescriptionByType(Skill skill, JsonArray jsonElements) {
        if(skill instanceof TieredSkill tieredSkill) {
            for (int i = 0; i < tieredSkill.getTiers().size(); i++) {
                LearnableSkill tier = tieredSkill.getTiers().get(i);

                if (tier instanceof MagnetMasteryPrestigePower) {
                    jsonElements.add(JsonDescription.simple(i + 1 + " "));
                    jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", (1.0 + (i * 0.25) * 100)) + "%\n", "#FFD700"));
                }

                if (tier instanceof ShieldedPrestigePower shieldedPrestigePower) {
                    jsonElements.add(JsonDescription.simple(i + 1 + " "));
                    jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", (shieldedPrestigePower.getPercentageOfHearts() * 100)) + "%\n", "#FFD700"));
                }

                if (tier instanceof ChampionDamagePrestigePower championDamagePrestigePower) {
                    jsonElements.add(JsonDescription.simple(i + 1 + " "));
                    jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", (championDamagePrestigePower.getValue() * 100)) + "%\n", "#FFD700"));
                }

                if (tier instanceof GearAttributePrestigePower gearAttributePrestigePower) {
                    jsonElements.add(JsonDescription.simple(i + 1 + " "));
                    jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", (gearAttributePrestigePower.getValue() * 100)) + "%\n", "#FFD700"));
                }

                if (tier instanceof SuperCrystalsPrestigePower superCrystalsPrestigePower) {
                    jsonElements.add(JsonDescription.simple(i + 1 + " "));
                    jsonElements.add(JsonDescription.simple("+" + superCrystalsPrestigePower.getExtraCapacity() + " extra capacity\n", "#FFD700"));
                }

                if (tier instanceof TemporalShardChancePrestigePower temporalShardChancePrestigePower) {
                    jsonElements.add(JsonDescription.simple(i + 1 + " "));
                    jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", (temporalShardChancePrestigePower.getChance() * 100)) + "%\n", "#FFD700"));
                }

                if (tier instanceof OriginalWardPrestigePower originalWardPrestigePower) {
                    jsonElements.add(JsonDescription.simple(i + 1 + " "));
                    jsonElements.add(JsonDescription.simple("+" + originalWardPrestigePower.getThresholdTicks() / 20 + " wear-off time\n", "#FFD700"));
                }

                if (tier instanceof TreasureHunterPrestigePower treasureHunterPrestigePower) {
                    jsonElements.add(JsonDescription.simple(i + 1 + " "));
                    jsonElements.add(JsonDescription.simple("+" + treasureHunterPrestigePower.getMaxStacks() + " max stacks, " + String.format("%.1f", treasureHunterPrestigePower.getBonusPerStack() * 100) + " bonus per stack\n", "#FFD700"));
                }


                if (tier instanceof CommanderPrestigePower commanderPrestigePower) {
                    jsonElements.add(JsonDescription.simple(i + 1 + " "));
                    jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", (commanderPrestigePower.getDurationMultiplier() * 100)) + "%\n", "#FFD700"));
                }

                if (tier instanceof ReachPrestigePower reachPrestigePower) {
                    jsonElements.add(JsonDescription.simple(i + 1 + " "));
                    jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", (reachPrestigePower.getReachIncrease() * 100)) + "%\n", "#FFD700"));
                }
            }
        }
    }

}
