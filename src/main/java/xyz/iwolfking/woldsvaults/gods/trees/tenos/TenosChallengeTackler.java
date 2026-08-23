package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import iskallia.vault.config.sigil.SigilConfig;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultLevel;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.VaultModifierUtils;

/**
 * Challenge Tackler: recomputes the sigil's crate tiers on join and adds a further share under
 * {@link #CRATE_TIER}, an id nothing else writes, so it can never apply twice.
 */
public final class TenosChallengeTackler {
    public static final ResourceLocation CRATE_TIER = WoldsVaults.id("tenos_challenge_tackler");

    private static final ResourceLocation SIGIL_CRATE_TIER = new ResourceLocation("the_vault", "crate_tier");

    private TenosChallengeTackler() {
    }

    /** Grants the node's share of the sigil's crate tiers to a vault that lacks them. */
    public static void boost(Vault vault, TenosNodeHandlers.ChallengeTacklerParams params) {
        if (vault == null) {
            return;
        }
        String sigil = vault.has(Vault.SIGIL) ? vault.get(Vault.SIGIL) : null;
        VaultLevel level = vault.get(Vault.LEVEL);
        if (sigil == null || level == null) {
            return;
        }
        SigilConfig.getConfig(sigil).ifPresent(config -> {
            SigilConfig.LevelEntry entry = config.getLevel(level.get());
            int sigilTiers = entry.getExtraCrateTiers();
            int extra = Math.round(sigilTiers * params.extra_crate_tier_ratio());
            if (extra <= 0) {
                return;
            }
            ResourceLocation modifierId = entry.getCrateTierModifierId();
            if (modifierId == null) {
                WoldsVaults.LOGGER.error("Challenge Tackler found sigil {} with {} crate tiers but no crate tier modifier id.",
                        sigil, sigilTiers);
                return;
            }
            if (!SIGIL_CRATE_TIER.equals(modifierId)) {
                WoldsVaults.LOGGER.error("Challenge Tackler expected sigil {} to grant crate tiers through {}, but it "
                        + "uses {}; the node did nothing rather than guess what one stack of that is worth.",
                        sigil, SIGIL_CRATE_TIER, modifierId);
                return;
            }
            if (VaultModifierUtils.getCountOfModifiers(vault, CRATE_TIER) > 0) {
                return;
            }
            if (TenosVaultUtil.resolveModifier(CRATE_TIER, "Challenge Tackler") == null) {
                return;
            }
            VaultModifierUtils.addModifier(vault, CRATE_TIER, extra);
        });
    }
}
