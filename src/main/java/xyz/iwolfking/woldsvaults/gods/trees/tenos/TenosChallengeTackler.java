package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import iskallia.vault.config.sigil.SigilConfig;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultLevel;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.VaultModifierUtils;

/**
 * Challenge Tackler (r118): sigils provide 50% more crate tiers.
 *
 * <p>The base mod stamps sigil crate tiers at crystal application, before anyone is inside the
 * vault, which would have made the node's effect depend on who started the run rather than who is
 * in it. Instead the sigil's own contribution is recomputed on join from
 * {@code SigilConfig.getExtraCrateTiers} and half of it is added again - so only the sigil's tiers
 * are boosted, never crate tiers from other sources, and any runner carrying the node is enough to
 * trigger it.
 *
 * <p>The extra tiers are added under an id of this node's own, a one-tier crate quantity modifier
 * identical in effect to {@code the_vault:crate_tier}. That is what makes applying twice
 * detectable: {@code the_vault:crate_tier} is shared by the sigil, by Ballistic Bingo's rewards and
 * by a prestige power, so counting it on the vault says nothing about who put it there, and a guard
 * built on that count would have made the node stand down whenever something else had already
 * granted tiers. Counting an id nobody else writes is exact.
 *
 * <p>That id is declared in {@code vault_modifiers.json} and only looked up here. Registering it
 * from Java instead would have broken the guard rather than helped it: {@code
 * Modifiers.getModifiers()} drops entries whose id is missing from the modifier registry, and the
 * registry is cleared and rebuilt from config on every reload, so the node would have stopped
 * seeing its own tiers and granted them again. Declared, the count is carried in the vault's own
 * saved modifier list, so the guard holds for a second player joining, for a relog and across a
 * server restart.
 */
public final class TenosChallengeTackler {
    public static final ResourceLocation CRATE_TIER = WoldsVaults.id("tenos_challenge_tackler");

    /** The crate tier modifier every shipped sigil uses, and the one this node mirrors. */
    private static final ResourceLocation SIGIL_CRATE_TIER = new ResourceLocation("the_vault", "crate_tier");

    private TenosChallengeTackler() {
    }

    /**
     * Grants the node's share of the sigil's own crate tiers to a vault that has not been granted
     * them yet. The caller has already established that a runner holds the node.
     */
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
