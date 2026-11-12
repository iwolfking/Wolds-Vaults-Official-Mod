package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import iskallia.vault.core.vault.Vault;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.ObjectiveDifficulty;
import xyz.iwolfking.woldsvaults.api.util.VaultModifierUtils;

/**
 * Challenge Tackler: recomputes the sigil's crate tiers on join and adds a further share under
 * {@link #CRATE_TIER}, an id nothing else writes, so it can never apply twice.
 */
public final class TenosChallengeTackler {
    public static final ResourceLocation CRATE_TIER = WoldsVaults.id("tenos_challenge_tackler");

    private TenosChallengeTackler() {
    }

    /** Grants the node's share of the sigil's crate tiers to a vault that lacks them. */
    public static void boost(Vault vault, TenosNodeHandlers.ChallengeTacklerParams params) {
        if (vault == null) {
            return;
        }
        String sigil = vault.has(Vault.SIGIL) ? vault.get(Vault.SIGIL) : null;
        if (sigil == null) {
            return;
        }
        int extra = Math.round(ObjectiveDifficulty.crateTiers(sigil) * params.extra_crate_tier_ratio());
        if (extra <= 0) {
            return;
        }
        if (VaultModifierUtils.getCountOfModifiers(vault, CRATE_TIER) > 0) {
            return;
        }
        if (TenosVaultUtil.resolveModifier(CRATE_TIER, "Challenge Tackler") == null) {
            return;
        }
        VaultModifierUtils.addModifier(vault, CRATE_TIER, extra);
    }
}
