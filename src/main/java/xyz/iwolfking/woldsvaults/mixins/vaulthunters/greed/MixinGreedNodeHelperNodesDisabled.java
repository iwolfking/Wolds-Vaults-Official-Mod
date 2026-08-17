package xyz.iwolfking.woldsvaults.mixins.vaulthunters.greed;

import iskallia.vault.greed.GreedNodeHelper;
import iskallia.vault.skill.base.Skill;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Design decision D32: the old greed tree is retired, so no greed node may pay out any more. Every
 * poll in {@code GreedNodeHelper} walks the player's tree and sums the nodes that answer
 * {@link Skill#isUnlocked()}; redirecting that one call to {@code false} makes all of them - extra
 * vault time, vault xp multiplier (server and client), imbuement chance, reputation multiplier and
 * the cryonic focus legendary-lock - compute as if the player had unlocked nothing.
 *
 * <p>The unlock records themselves are untouched. The tree's own bookkeeping still reads them
 * truthfully, which matters because {@code PlayerGreedTreeData}'s config-reload merge rebuilds a
 * player's tree from {@code GreedTree.getUnlockedNodes()} and re-stamps those ids: neutralising
 * {@code isUnlocked} at the node level instead would empty that set and permanently erase every
 * unlock on the next merge.
 *
 * <p>This redirect runs inside the loop body, so it composes with
 * {@code MixinGreedNodeHelperImbuement}, which adds the Wendarr Master Imbuer bonus to the RETURN
 * value: imbuement chance becomes base 0 plus the god contribution.
 */
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
