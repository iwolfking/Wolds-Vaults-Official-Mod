package xyz.iwolfking.woldsvaults.medallions;

import java.util.Optional;

/**
 * Implemented on the base mod's {@code CrystalData} by mixin so a crystal can carry exactly one
 * greed medallion. Only the rank index is stored (absent = 0); the tier's numbers are always
 * re-derived from {@link GreedMedallionTier#byRankIndex(int)} so a rebalance of the table never
 * needs a crystal migration.
 */
public interface GreedMedallionCrystal {
    int woldsvaults$getMedallionRank();

    void woldsvaults$setMedallionRank(int rank);

    default Optional<GreedMedallionTier> woldsvaults$getMedallion() {
        int rank = this.woldsvaults$getMedallionRank();
        return rank <= 0 ? Optional.empty() : GreedMedallionTier.byRankIndex(rank);
    }

    default boolean woldsvaults$hasMedallion() {
        return this.woldsvaults$getMedallionRank() > 0;
    }
}
