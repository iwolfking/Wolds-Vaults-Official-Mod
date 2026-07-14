package xyz.iwolfking.woldsvaults.loot;

/**
 * O(pools) normal approximation of the tiered-loot rarity percentile
 * (TieredLootTableGenerator.CDF), used above {@link #EXACT_ROLL_LIMIT} rolls where the exact
 * composition enumeration balloons (a 5-pool CDF at 54 rolls is ~424k cached BigDecimal terms).
 * The rarity heuristic H = -sum((total/w_i) * freq_i) is linear in the multinomial counts, so
 * E[H] = -n*P and Var[H] = n*(sum(score_i) - P^2) in closed form. Ultra-light pools (the 0.00445
 * mythic pool) are excluded from the fit - their huge score would dominate the variance despite
 * ~never being hit - and a hit in one is treated as the rarest possible outcome, matching what
 * the exact enumeration yields for such a draw.
 */
public final class TieredCdfApprox {
    /** Highest roll count that still uses the exact CDF enumeration on 5-pool tables. */
    public static final int EXACT_ROLL_LIMIT = 30;

    /** Pool probability below which a pool is excluded from the normal fit. */
    private static final double NEGLIGIBLE = 0.001;

    private TieredCdfApprox() {
    }

    /**
     * {@code key} = [rollCount, baseWeight_0 .. baseWeight_{P-1}], exactly as built in
     * generate(). A degenerate fit (single active pool, or all weights equal) resolves to
     * 0 or 1 directly.
     */
    public static double cdf(double[] key, int[] frequencies) {
        int pools = key.length - 1;
        double n = key[0];
        double total = 0.0;
        for (int i = 1; i <= pools; i++) {
            total += key[i];
        }
        double sumScores = 0.0;
        double hObs = 0.0;
        int active = 0;
        for (int i = 0; i < pools; i++) {
            if (key[i + 1] / total < NEGLIGIBLE) {
                if (frequencies[i] > 0) {
                    return 0.0;
                }
                continue;
            }
            double score = total / key[i + 1];
            sumScores += score;
            hObs -= score * frequencies[i];
            active++;
        }
        double mean = -n * active;
        double var = n * (sumScores - (double) active * active);
        if (var <= 0.0) {
            return hObs <= mean ? 0.0 : 1.0;
        }
        return phi((hObs - mean) / Math.sqrt(var));
    }

    /** Standard normal CDF, Abramowitz-Stegun 7.1.26 (|error| < 7.5e-8). */
    private static double phi(double z) {
        double t = 1.0 / (1.0 + 0.2316419 * Math.abs(z));
        double d = 0.3989422804014327 * Math.exp(-0.5 * z * z);
        double p = d * t * (0.319381530 + t * (-0.356563782 + t * (1.781477937
                + t * (-1.821255978 + t * 1.330274429))));
        return z >= 0.0 ? 1.0 - p : p;
    }
}
