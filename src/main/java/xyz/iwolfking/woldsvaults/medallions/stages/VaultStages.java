package xyz.iwolfking.woldsvaults.medallions.stages;

import iskallia.vault.core.vault.Vault;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.config.GreedVaultStagesConfig;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionTier;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionVaultState;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Picks the palette or pool id a vault reads. In a vault whose medallion rank is at or above a stage's unlock rank,
 * each normal id that stage pairs resolves to its stage id; every other id, and every id in a vault without a
 * medallion, resolves to itself. Stage ids resolve to themselves.
 */
public final class VaultStages {
    private static volatile Lookup lookup;

    private VaultStages() {
    }

    private record Redirect(ResourceLocation stageId, int unlockRank) {
    }

    private record Lookup(Map<ResourceLocation, Redirect> palettes, Map<ResourceLocation, Redirect> pools) {
    }

    /** Rebuilds the lookup from the installed stage table; a null table leaves every id unchanged. */
    public static void load(@Nullable GreedVaultStagesConfig table) {
        if (table == null) {
            lookup = null;
            return;
        }
        Map<ResourceLocation, Redirect> palettes = new HashMap<>();
        Map<ResourceLocation, Redirect> pools = new HashMap<>();
        for (GreedVaultStagesConfig.Stage stage : table.stages.values()) {
            collect(stage.palettes, stage.unlockRank, palettes);
            collect(stage.pools, stage.unlockRank, pools);
        }
        lookup = new Lookup(Map.copyOf(palettes), Map.copyOf(pools));
    }

    private static void collect(Map<String, String> pairs, int unlockRank, Map<ResourceLocation, Redirect> into) {
        for (Map.Entry<String, String> pair : pairs.entrySet()) {
            into.put(ResourceLocation.parse(pair.getKey()), new Redirect(ResourceLocation.parse(pair.getValue()), unlockRank));
        }
    }

    public static ResourceLocation resolvePalette(@Nullable Vault vault, ResourceLocation id) {
        Lookup current = lookup;
        return resolve(vault, id, current == null ? null : current.palettes());
    }

    public static ResourceLocation resolvePool(@Nullable Vault vault, ResourceLocation id) {
        Lookup current = lookup;
        return resolve(vault, id, current == null ? null : current.pools());
    }

    private static ResourceLocation resolve(@Nullable Vault vault, ResourceLocation id, @Nullable Map<ResourceLocation, Redirect> redirects) {
        if (vault == null || id == null) {
            return id;
        }
        if (redirects == null) {
            if (GreedMedallionVaultState.get(vault).isPresent()) {
                WoldLogger.errorOnce("greed-vault-stages-missing",
                        "The greed stage table is not installed; medallion vaults read normal palettes and pools.");
            }
            return id;
        }
        Redirect redirect = redirects.get(id);
        if (redirect == null) {
            return id;
        }
        Optional<GreedMedallionTier> tier = GreedMedallionVaultState.get(vault);
        return tier.isPresent() && tier.get().getRankIndex() >= redirect.unlockRank() ? redirect.stageId() : id;
    }
}
