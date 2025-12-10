package xyz.iwolfking.woldsvaults.api.util.datagen;

import com.google.gson.JsonArray;
import iskallia.vault.config.ExpertisesConfig;
import iskallia.vault.config.SkillDescriptionsConfig;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.skill.base.LearnableSkill;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.TieredSkill;
import iskallia.vault.skill.expertise.type.*;
import iskallia.vault.skill.prestige.*;
import iskallia.vault.skill.talent.GearAttributeSkill;
import iskallia.vault.skill.talent.type.GearAttributeTalent;
import xyz.iwolfking.vhapi.api.datagen.AbstractSkillDescriptionsProvider;
import xyz.iwolfking.vhapi.api.util.builder.description.JsonDescription;
import xyz.iwolfking.vhapi.mixin.accessors.SkillDescriptionsConfigAccessor;
import xyz.iwolfking.woldsvaults.api.util.ComponentUtils;
import xyz.iwolfking.woldsvaults.expertises.*;
import xyz.iwolfking.woldsvaults.prestige.ReachPrestigePower;

public class ExpertisesDescriptionHelper {

    public static void generateDescriptions(AbstractSkillDescriptionsProvider.Builder builder, SkillDescriptionsConfig expertiseDescriptions) {
        ModConfigs.EXPERTISES = new ExpertisesConfig().readConfig();
        ModConfigs.EXPERTISES.tree.skills.forEach(skill -> {
                createDescriptionForExpertise(skill, expertiseDescriptions, builder);
        });
    }

    private static void createDescriptionForExpertise(Skill skill, SkillDescriptionsConfig expertiseDescriptions, AbstractSkillDescriptionsProvider.Builder builder) {
        if(!((SkillDescriptionsConfigAccessor)expertiseDescriptions).getDescriptions().containsKey(skill.getId())) {
            return;
        }

        builder.addDescription(skill.getId(), jsonElements -> {
            ComponentUtils.toJsonArray(expertiseDescriptions.getDescriptionFor(skill.getId())).forEach(jsonElements::add);
            jsonElements.add(JsonDescription.simple("\n\n"));
            getExpertiseDescriptionByType(skill, jsonElements);
        });
    }

    private static void getExpertiseDescriptionByType(Skill skill, JsonArray jsonElements) {
        if(skill instanceof TieredSkill tieredSkill) {
            for (int i = 0; i < tieredSkill.getTiers().size(); i++) {
                LearnableSkill tier = tieredSkill.getTiers().get(i);

                if (tier instanceof CompanionCooldownExpertise companionCooldownExpertise) {
                    jsonElements.add(JsonDescription.simple(i + 1 + " "));
                    jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", companionCooldownExpertise.getCooldownReduction() * 100) + "%\n", "#FFD700"));
                }

                if (tier instanceof GraveInsurance graveInsurance) {
                    jsonElements.add(JsonDescription.simple(i + 1 + " "));
                    jsonElements.add(JsonDescription.simple("-" + String.format("%.1f", graveInsurance.getCostReduction() * 100) + "%\n", "#FFD700"));
                }

                if (tier instanceof EclecticGearExpertise eclecticGearExpertise) {
                    jsonElements.add(JsonDescription.simple(i + 1 + " "));
                    jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", (eclecticGearExpertise.getIncreasedChance() * 100)) + "%\n", "#FFD700"));
                }

                if (tier instanceof BlessedExpertise blessedExpertise) {
                    jsonElements.add(JsonDescription.simple(i + 1 + " "));
                    jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", (blessedExpertise.affinityIncrease * 100)) + "%\n", "#FFD700"));
                }

                if (tier instanceof LuckyAltarExpertise luckyAltarExpertise) {
                    jsonElements.add(JsonDescription.simple(i + 1 + " "));
                    jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", (luckyAltarExpertise.getLuckyAltarChance() * 100)) + "%\n", "#FFD700"));
                }

                if (tier instanceof BarteringExpertise barteringExpertise) {
                    jsonElements.add(JsonDescription.simple(i + 1 + " "));
                    jsonElements.add(JsonDescription.simple("-" + String.format("%.1f", (barteringExpertise.getCostReduction() * 100)) + "%\n", "#FFD700"));
                }

                if (tier instanceof ArtisanExpertise artisanExpertise) {
                    jsonElements.add(JsonDescription.simple(i + 1 + " "));
                    jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", (artisanExpertise.getPotentialReduction() * 100)) + "%\n", "#FFD700"));
                }

                if (tier instanceof GearAttributeTalent gearAttributeSkill) {
                    jsonElements.add(JsonDescription.simple(i + 1 + " "));
                    jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", (gearAttributeSkill.getValue() * 100)) + "%\n", "#FFD700"));
                }

                if (tier instanceof ShopRerollExpertise shopRerollExpertise) {
                    jsonElements.add(JsonDescription.simple(i + 1 + " "));
                    jsonElements.add(JsonDescription.simple(shopRerollExpertise.getRerollTimeout() / 20 + " seconds cooldown\n", "#FFD700"));
                }

                if (tier instanceof DivineExpertise divineExpertise) {
                    jsonElements.add(JsonDescription.simple(i + 1 + " "));
                    jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", (divineExpertise.getAffinityIncrease() * 100)) + "%\n", "#FFD700"));
                }

                if (tier instanceof PylonPilfererExpertise pylonPilfererExpertise) {
                    jsonElements.add(JsonDescription.simple(i + 1 + " "));
                    jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", (pylonPilfererExpertise.getChanceIncrease() * 100)) + "%\n", "#FFD700"));
                }

                if (tier instanceof TrinketerExpertise trinketerExpertise) {
                    jsonElements.add(JsonDescription.simple(i + 1 + " "));
                    jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", (trinketerExpertise.getDamageAvoidanceChance() * 100)) + "%\n", "#FFD700"));
                }

                if (tier instanceof MysticExpertise mysticExpertise) {
                    jsonElements.add(JsonDescription.simple(i + 1 + " "));
                    jsonElements.add(JsonDescription.simple("+" + mysticExpertise.getAdditionalCrystalVolume() + " capacity\n", "#FFD700"));
                }

                if (tier instanceof SurpriseModifiersExpertise surpriseModifiersExpertise) {
                    jsonElements.add(JsonDescription.simple(i + 1 + " "));
                    jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", (surpriseModifiersExpertise.getExpertiseLevel() * .1F * 100)) + "%\n", "#FFD700"));
                }

                if (tier instanceof InfuserExpertise infuserExpertise) {
                    jsonElements.add(JsonDescription.simple(i + 1 + " "));
                    jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", (infuserExpertise.getNegativeModifierRemovalChance() * 100)) + "%\n", "#FFD700"));
                }

                if (tier instanceof ExperiencedExpertise experiencedExpertise) {
                    jsonElements.add(JsonDescription.simple(i + 1 + " "));
                    jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", (experiencedExpertise.getIncreasedExpPercentage() * 100)) + "%\n", "#FFD700"));
                }

                if (tier instanceof FortunateExpertise fortunateExpertise) {
                    jsonElements.add(JsonDescription.simple(i + 1 + " "));
                    jsonElements.add(JsonDescription.simple("+" + fortunateExpertise.getAdditionalFortuneLevels() + " levels\n", "#FFD700"));
                }

                if (tier instanceof BlackMarketExpertise) {
                    jsonElements.add(JsonDescription.simple(i + 1 + " "));
                    jsonElements.add(JsonDescription.simple("+" + (i + 1) + " slot\n", "#FFD700"));
                }

                if (tier instanceof LegendaryExpertise legendaryExpertise) {
                    jsonElements.add(JsonDescription.simple(i + 1 + " "));
                    jsonElements.add(JsonDescription.simple("+" + String.format("%.1f", (legendaryExpertise.getExtraLegendaryChance() * 100)) + "%\n", "#FFD700"));
                }
            }
        }
    }

}
