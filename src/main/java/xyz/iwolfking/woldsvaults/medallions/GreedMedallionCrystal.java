package xyz.iwolfking.woldsvaults.medallions;

import java.util.Optional;

/**
 * Implemented on the base mod's {@code CrystalData} by mixin so a crystal can carry one greed medallion.
 * Only the rank index is stored (0 = none); a tier's numbers are re-derived from the table.
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
