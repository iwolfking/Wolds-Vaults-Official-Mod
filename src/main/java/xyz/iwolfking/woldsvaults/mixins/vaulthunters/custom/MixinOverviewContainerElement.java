package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.client.data.ClientExpertiseData;
import iskallia.vault.client.gui.screen.summary.element.OverviewContainerElement;
import iskallia.vault.core.vault.stat.StatCollector;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.TieredSkill;
import iskallia.vault.skill.expertise.type.ExperiencedExpertise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = OverviewContainerElement.class, remap = false)
public class MixinOverviewContainerElement {

    @Unique
    private static float woldsVaults$getClientExperiencedMultiplier() {
        float increase = 0.0F;
        for (TieredSkill learnedTalentNode : ClientExpertiseData.getLearnedTalentNodes()) {
            if ((Object) learnedTalentNode instanceof Skill skill) {
                for (ExperiencedExpertise expertise : skill.getAll(ExperiencedExpertise.class, Skill::isUnlocked)) {
                    increase += expertise.getIncreasedExpPercentage();
                }
            }
        }
        return increase;
    }

    @WrapOperation(
        method = "<init>(Liskallia/vault/client/gui/framework/spatial/spi/ISpatial;Liskallia/vault/client/gui/screen/summary/VaultExitContainerScreenData;)V",
        at = @At(
            value = "INVOKE",
            target = "Liskallia/vault/core/vault/stat/StatCollector;getExpMultiplier()F"
        )
    )
    private float compoundOverviewExperienceMultiplier(StatCollector instance, Operation<Float> original) {
        float baseMultiplier = original.call(instance);
        float experiencedBoost = woldsVaults$getClientExperiencedMultiplier();
        
        return baseMultiplier + experiencedBoost;
    }
}