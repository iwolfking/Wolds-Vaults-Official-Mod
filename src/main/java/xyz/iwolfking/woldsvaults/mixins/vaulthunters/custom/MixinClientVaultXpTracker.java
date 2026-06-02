package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.client.data.ClientExpertiseData;
import iskallia.vault.client.data.ClientVaultXpTracker;
import iskallia.vault.skill.base.TieredSkill;
import iskallia.vault.skill.expertise.type.ExperiencedExpertise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ClientVaultXpTracker.class, remap = false)
public class MixinClientVaultXpTracker {

    @WrapOperation(
        method = "updateBreakdown",
        at = @At(
            value = "INVOKE",
            target = "Liskallia/vault/greed/GreedNodeHelper;getClientXpGainMultiplier()F"
        )
    )
    private float redirectBonusExpMultiplier(Operation<Float> original) {
        float increase = 0.0F;

        for (TieredSkill learnedTalentNode : ClientExpertiseData.getLearnedTalentNodes()) {
            if (learnedTalentNode.getChild() instanceof ExperiencedExpertise experiencedExpertise) {
                increase += experiencedExpertise.getIncreasedExpPercentage();
            }
        }

        return original.call() + increase;
    }
}