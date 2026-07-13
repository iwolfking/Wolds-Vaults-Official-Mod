package xyz.iwolfking.woldsvaults.loot;

/**
 * Item-rarity -> sub-pool weight multipliers for the 5-pool (mapped vault) tiered chest tables.
 * Pools 0-2 behave exactly like vanilla; pool 3 (omega) gains a ramped exponent and pool 4
 * (mythic) a steeper offset one, so their share keeps growing at high rarity instead of
 * converging to the base-weight ratio.
 */
public final class MythicLootScaling {
    /** Sub-pool count of a mapped chest table (4 vanilla tiers + mythic); other tables are untouched. */
    public static final int MAPPED_POOL_COUNT = 5;

    private static final double LREF = Math.log10(201.0); // ramp normalized to 1 at itemRarity 200 (+20000%)
    private static final double OMEGA_AMP = 0.12;
    private static final double MYTHIC_AMP = 0.151;
    private static final double MYTHIC_OFFSET = 0.70;

    private MythicLootScaling() {
    }

    /**
     * Multiplier for sub-pool {@code index}'s base weight; {@code itemRarity} is the rarity
     * fraction (2.0 == +200%). Non-positive rarity uses the vanilla linear scaling, which also
     * keeps pow() off negative bases.
     */
    public static double poolScale(int index, float itemRarity) {
        double ir = itemRarity;
        if (index == 0) {
            return 1.0;
        }
        if (ir <= 0.0) {
            return 1.0 + ir;
        }
        double ramp = Math.pow(Math.log10(1.0 + ir) / LREF, 1.4);
        switch (index) {
            case 3:
                return Math.pow(1.0 + ir, 1.0 + OMEGA_AMP * ramp);
            case 4:
                return Math.pow(1.0 + ir, 1.0 + MYTHIC_AMP * ramp + MYTHIC_OFFSET);
            default:
                return 1.0 + ir;
        }
    }
}
