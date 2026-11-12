package xyz.iwolfking.woldsvaults.mixins.vaulthunters.greed;

import iskallia.vault.greed.GreedNodeHelper;
import iskallia.vault.skill.base.Skill;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Makes every {@link GreedNodeHelper} payout poll read greed nodes as locked, records untouched. */
@Mixin(value = GreedNodeHelper.class, remap = false)
public abstract class MixinGreedNodeHelperNodesDisabled {
    @Redirect(method = {
            "getAdditionalVaultTimeTicks",
            "getXpGainMultiplier",
            "getImbuementChanceBonus",
            "getGreedReputationMultiplier(Liskallia/vault/greed/GreedTree;)F",
            "getClientXpGainMultiplier",
            "hasCryonicFocusFreeze"
    }, at = @At(value = "INVOKE", target = "Liskallia/vault/skill/base/Skill;isUnlocked()Z", ordinal = 0))
    private static boolean woldsvaults$treatGreedNodesAsLocked(Skill skill) {
        return false;
    }
}
