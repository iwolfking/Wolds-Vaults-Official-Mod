package xyz.iwolfking.woldsvaults.loot;

import iskallia.vault.util.VaultRarity;

/**
 * Per-map-tier loot scaling for the 5-pool mapped STRONGBOX tables (living/ornate/gilded).
 * Tier 0 (display tier 1) reproduces the {@link MythicLootScaling} chest curve exactly; each
 * tier above steepens the pool-3 (omega) and pool-4 (mythic) ramp so their share at +20000%
 * item rarity rises ~+0.72pp / ~+0.234pp per tier. The boost tables are precomputed calibration
 * output chosen to hit those targets, not hand-tuned values. tier = 0..5.
 */
public final class StrongboxTierScaling {
    private static final double LREF = Math.log10(201.0); // ramp normalized to 1 at itemRarity 200 (+20000%)
    private static final double OMEGA_AMP = 0.12;
    private static final double MYTHIC_AMP = 0.151;
    private static final double MYTHIC_OFFSET = 0.70;
    private static final double[] OMEGA_TIER_BOOST = {0.000, 0.013, 0.025, 0.037, 0.048, 0.059};
    private static final double[] MYTHIC_TIER_BOOST = {0.000, 0.033, 0.061, 0.086, 0.109, 0.130};

    private StrongboxTierScaling() {
    }

    /**
     * Multiplier for sub-pool {@code index}'s base weight; {@code itemRarity} is the rarity
     * fraction (2.0 == +200%). Non-positive rarity uses the vanilla linear scaling, which also
     * keeps pow() off negative bases.
     */
    public static double poolScale(int index, float itemRarity, int tier) {
        double ir = itemRarity;
        if (index == 0) {
            return 1.0;
        }
        if (ir <= 0.0) {
            return 1.0 + ir;
        }
        int t = Math.max(0, Math.min(tier, OMEGA_TIER_BOOST.length - 1));
        double ramp = Math.pow(Math.log10(1.0 + ir) / LREF, 1.4);
        switch (index) {
            case 3:
                return Math.pow(1.0 + ir, 1.0 + (OMEGA_AMP + OMEGA_TIER_BOOST[t]) * ramp);
            case 4:
                return Math.pow(1.0 + ir, 1.0 + (MYTHIC_AMP + MYTHIC_TIER_BOOST[t]) * ramp + MYTHIC_OFFSET);
            default:
                return 1.0 + ir;
        }
    }

    /** Max loot rolls at this tier: 54 + 18 per display tier (T1 = 72 .. T6 = 162). */
    public static int maxRolls(int tier) {
        return 54 + 18 * (tier + 1);
    }

    /** Extra base rolls before item-quantity scaling (~1.3 per display tier); shifts the IQ needed to cap. */
    public static int baseRollBonus(int tier) {
        return Math.round(1.3f * (tier + 1));
    }

    /**
     * Displayed rarity by tier. Strongboxes skip the CDF percentile entirely (locked block, the
     * value is never shown in a GUI), so the cosmetic name/particles track the map tier instead.
     */
    public static VaultRarity rarityForTier(int tier) {
        if (tier >= 4) {
            return VaultRarity.OMEGA;
        }
        if (tier >= 2) {
            return VaultRarity.EPIC;
        }
        return VaultRarity.RARE;
    }
}
