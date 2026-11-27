package xyz.iwolfking.woldsvaults.medallions.stages;

import iskallia.vault.core.vault.Vault;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.config.GreedVaultStagesConfig;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionTier;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionVaultState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Picks the palette or pool id a vault reads. In a vault whose medallion rank is at or above a stage's unlock rank,
 * each normal id that stage pairs resolves to its stage id, the highest such stage winning; every other id, and every id in a vault without a
 * medallion, resolves to itself. Stage ids resolve to themselves.
 */
public final class VaultStages {
    private static volatile Lookup lookup;

    private VaultStages() {
    }

    private record Redirect(ResourceLocation stageId, int unlockRank) {
    }

    private record Lookup(Map<ResourceLocation, List<Redirect>> palettes, Map<ResourceLocation, List<Redirect>> pools) {
    }

    /** Rebuilds the lookup from the installed stage table; a null table leaves every id unchanged. */
    public static void load(@Nullable GreedVaultStagesConfig table) {
        if (table == null) {
            lookup = null;
            return;
        }
        Map<ResourceLocation, List<Redirect>> palettes = new HashMap<>();
        Map<ResourceLocation, List<Redirect>> pools = new HashMap<>();
        for (GreedVaultStagesConfig.Stage stage : table.stages.values()) {
            collect(stage.palettes, stage.unlockRank, palettes);
            collect(stage.pools, stage.unlockRank, pools);
        }
        lookup = new Lookup(highestFirst(palettes), highestFirst(pools));
    }

    private static void collect(Map<String, String> pairs, int unlockRank, Map<ResourceLocation, List<Redirect>> into) {
        for (Map.Entry<String, String> pair : pairs.entrySet()) {
            into.computeIfAbsent(ResourceLocation.parse(pair.getKey()), key -> new ArrayList<>())
                    .add(new Redirect(ResourceLocation.parse(pair.getValue()), unlockRank));
        }
    }

    private static Map<ResourceLocation, List<Redirect>> highestFirst(Map<ResourceLocation, List<Redirect>> redirects) {
        Map<ResourceLocation, List<Redirect>> sorted = new HashMap<>();
        redirects.forEach((id, list) -> sorted.put(id, list.stream()
                .sorted((first, second) -> Integer.compare(second.unlockRank(), first.unlockRank()))
                .toList()));
        return Map.copyOf(sorted);
    }

    public static ResourceLocation resolvePalette(@Nullable Vault vault, ResourceLocation id) {
        Lookup current = lookup;
        return resolve(vault, id, current == null ? null : current.palettes());
    }

    public static ResourceLocation resolvePool(@Nullable Vault vault, ResourceLocation id) {
        Lookup current = lookup;
        return resolve(vault, id, current == null ? null : current.pools());
    }

    private static ResourceLocation resolve(@Nullable Vault vault, ResourceLocation id, @Nullable Map<ResourceLocation, List<Redirect>> redirects) {
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
        List<Redirect> candidates = redirects.get(id);
        if (candidates == null) {
            return id;
        }
        Optional<GreedMedallionTier> tier = GreedMedallionVaultState.get(vault);
        if (tier.isEmpty()) {
            return id;
        }
        int rank = tier.get().getRankIndex();
        for (Redirect redirect : candidates) {
            if (rank >= redirect.unlockRank()) {
                return redirect.stageId();
            }
        }
        return id;
    }
}
