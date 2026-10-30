package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.client.data.ClientExpertiseData;
import iskallia.vault.client.gui.screen.summary.VaultEndScreen;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.stat.StatCollector;
import iskallia.vault.skill.base.TieredSkill;
import iskallia.vault.skill.expertise.type.ExperiencedExpertise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = VaultEndScreen.class, remap = false)
public class MixinVaultEndScreen {

    @Unique
    private static float woldsVaults$getClientExperiencedMultiplier() {
        float increase = 0.0F;
        for (TieredSkill learnedTalentNode : ClientExpertiseData.getLearnedTalentNodes()) {
            if (learnedTalentNode.getChild() instanceof ExperiencedExpertise experiencedExpertise) {
                increase += experiencedExpertise.getIncreasedExpPercentage();
            }
        }
        return increase;
    }

    @WrapOperation(
        method = "<init>(Liskallia/vault/core/vault/stat/VaultSnapshot;Lnet/minecraft/network/chat/Component;Ljava/util/UUID;ZZ)V",
        at = @At(
            value = "INVOKE", 
            target = "Liskallia/vault/core/vault/stat/StatCollector;getExperience(Liskallia/vault/core/vault/Vault;)I"
        )
    )
    private int multiplyDisplayXpWithExperienced(StatCollector instance, Vault vault, Operation<Integer> original) {
        float experiencedBoost = woldsVaults$getClientExperiencedMultiplier();
        
        return Math.round(original.call(instance, vault) * (1.0F + experiencedBoost));
    }

    @ModifyArg(
        method = "lambda$new$23",
        at = @At(
            value = "INVOKE",
            target = "Ljava/lang/String;format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;",
            ordinal = 0
        ),
        index = 1
    )
    private static Object[] adjustShiftXpReceiptArgs(Object[] args) {
        if (args != null && args.length >= 2 && args[0] instanceof Float originalMultiplier) {
            float experiencedBoost = woldsVaults$getClientExperiencedMultiplier();

            args[0] = originalMultiplier + experiencedBoost;
        }
        return args;
    }
}