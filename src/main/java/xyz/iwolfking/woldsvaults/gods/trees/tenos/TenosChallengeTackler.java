package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import iskallia.vault.config.sigil.SigilConfig;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultLevel;
import iskallia.vault.core.vault.modifier.modifier.CrateItemQuantityModifier;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import net.minecraft.network.chat.TextColor;
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
 * <p>The extra tiers are added under an id of this node's own, a one-tier
 * {@code CrateItemQuantityModifier} identical in effect to {@code the_vault:crate_tier}. That is
 * what makes applying twice detectable: {@code the_vault:crate_tier} is shared by the sigil, by
 * Ballistic Bingo's rewards and by a prestige power, so counting it on the vault says nothing
 * about who put it there, and a guard built on that count would have made the node stand down
 * whenever something else had already granted tiers. Counting an id nobody else writes is exact.
 * The vault's modifier list is saved with the vault, so the guard holds for a second player
 * joining, for a relog and across a server restart, which a static set would not.
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
            if (resolveTierModifier(params.tiers_per_stack()) == null) {
                return;
            }
            VaultModifierUtils.addModifier(vault, CRATE_TIER, extra);
        });
    }

    /**
     * One crate tier, under this node's id. Registered lazily rather than at startup because the
     * modifier registry is cleared on every config reload, the same reason Omega Vault's modifier
     * is built this way.
     */
    private static VaultModifier<?> resolveTierModifier(float tiersPerStack) {
        VaultModifier<?> existing = VaultModifierRegistry.get(CRATE_TIER);
        if (existing != null) {
            return existing;
        }
        try {
            CrateItemQuantityModifier modifier = new CrateItemQuantityModifier(
                    CRATE_TIER,
                    new CrateItemQuantityModifier.Properties(tiersPerStack),
                    new VaultModifier.Display("Crate Tier", TextColor.parseColor("#38C9C0"),
                            "Increases the reward crate tier once per modifier"));
            VaultModifierRegistry.register(CRATE_TIER, modifier);
            return modifier;
        } catch (Exception e) {
            WoldsVaults.LOGGER.error("Challenge Tackler could not register its crate tier modifier {}; "
                    + "the node does nothing.", CRATE_TIER, e);
            return null;
        }
    }
}
