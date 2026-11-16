package xyz.iwolfking.woldsvaults.loot.themedrops;

import iskallia.vault.core.util.WeightedTree;
import iskallia.vault.core.world.loot.LootPool;
import iskallia.vault.core.world.loot.entry.LootEntry;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * The theme drops resolved for one chest: per tier index, the payload pools grafted into that tier and the share of
 * the tier's draws each takes.
 */
public final class ThemeDropInjections {
    private final Map<Integer, List<Injection>> byTier;

    /** One payload pool and the share of its tier's draws it takes. */
    public record Injection(double share, LootPool payload) {
    }

    ThemeDropInjections(Map<Integer, List<Injection>> byTier) {
        this.byTier = Map.copyOf(byTier);
    }

    /**
     * A new pool holding {@code tier} at its own total weight {@code S} and each payload at {@code S * p / (1 - P)},
     * where {@code p} is the payload's share and {@code P} the sum of the shares in this tier, so each payload takes
     * exactly its share of the tier's draws. Null when nothing is grafted into this tier. Neither {@code tier} nor
     * any payload pool is changed.
     */
    @Nullable
    public LootPool wrap(int tierIndex, WeightedTree<LootEntry> tier) {
        List<Injection> injections = this.byTier.get(tierIndex);
        if (injections == null || injections.isEmpty()) {
            return null;
        }
        double tierWeight = tier.getChildren().getTotalWeight();
        if (tierWeight <= 0.0D) {
            WoldLogger.errorOnce("theme-drops-empty-tier-" + tierIndex,
                    "Theme drops skip tier {} of a chest table because it has no weight to share", tierIndex);
            return null;
        }
        double sharedTotal = 0.0D;
        for (Injection injection : injections) {
            sharedTotal += injection.share();
        }
        LootPool wrapper = new LootPool();
        wrapper.addTree(tier, tierWeight);
        for (Injection injection : injections) {
            wrapper.addTree(injection.payload(), tierWeight * injection.share() / (1.0D - sharedTotal));
        }
        return wrapper;
    }
}
