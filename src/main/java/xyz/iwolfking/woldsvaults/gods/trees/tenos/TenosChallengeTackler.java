package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import iskallia.vault.config.sigil.SigilConfig;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultLevel;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.VaultModifierUtils;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Challenge Tackler (r118): sigils provide 50% more crate tiers.
 *
 * <p>The base mod stamps sigil crate tiers at crystal application, before anyone is inside the
 * vault, which would have made the node's effect depend on who started the run rather than who is
 * in it. Instead the sigil's own contribution is recomputed on join from
 * {@code SigilConfig.getExtraCrateTiers} and the extra half is added as more stacks of the very
 * modifier the sigil used - so only the sigil's tiers are boosted, never crate tiers from other
 * sources, and any runner carrying the node is enough to trigger it.
 */
public final class TenosChallengeTackler {
    public static final float EXTRA_CRATE_TIER_RATIO = 0.5F;

    private static final Set<UUID> BOOSTED_VAULTS = ConcurrentHashMap.newKeySet();
    private static final Object OWNER = new Object();

    private TenosChallengeTackler() {
    }

    static void register() {
        CommonEvents.LISTENER_JOIN.register(OWNER, data -> boost(data.getVault()));
        CommonEvents.VAULT_END.register(OWNER, data -> BOOSTED_VAULTS.remove(data.getVault().get(Vault.ID)));
    }

    private static void boost(Vault vault) {
        if (vault == null || !vault.has(Vault.ID)) {
            return;
        }
        UUID vaultId = vault.get(Vault.ID);
        if (BOOSTED_VAULTS.contains(vaultId)) {
            return;
        }
        if (!TenosVaultUtil.anyRunnerHasMinor(vault, TenosNodes.CHALLENGE_TACKLER)) {
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
            int extra = Math.round(sigilTiers * EXTRA_CRATE_TIER_RATIO);
            if (extra <= 0) {
                return;
            }
            ResourceLocation modifierId = entry.getCrateTierModifierId();
            if (modifierId == null) {
                WoldsVaults.LOGGER.error("Challenge Tackler found sigil {} with {} crate tiers but no crate tier modifier id.",
                        sigil, sigilTiers);
                return;
            }
            BOOSTED_VAULTS.add(vaultId);
            VaultModifierUtils.addModifier(vault, modifierId, extra);
        });
    }
}
