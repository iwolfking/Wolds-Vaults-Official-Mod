package xyz.iwolfking.woldsvaults.extraction.parts;

import javax.annotation.Nullable;

/**
 * Every number an extractor carries. Flat stats sum across parts and are multiplied by attachment
 * percentages; multiplier stats compound; inert stats are stored for later systems and do nothing yet.
 * Display names and colours follow the_vault's attribute colours wherever the_vault has the same stat.
 */
public enum ExtractorStat {
    POWER("power", "Power", Kind.FLAT, Unit.FLAT, 0xFF7A3D),
    SPEED("speed", "Speed", Kind.FLAT, Unit.FLAT, 0xFFE068),
    HEALTH("health", "Health", Kind.FLAT, Unit.FLAT, 0x22FF25),
    ARMOR("armor", "Armor", Kind.FLAT, Unit.FLAT, 0x48BAF8),
    SHIELD("shield", "Shield", Kind.FLAT, Unit.FLAT, 0x5EC8FF),
    SHIELD_TOUGHNESS("shield_toughness", "Shield Toughness", Kind.FLAT, Unit.FLAT, 0x3E8FD6),
    SHIELD_REGEN("shield_regen", "Shield Regen", Kind.FLAT, Unit.RATE, 0x9BE7FF),
    ENERGY_CAPACITY("energy_capacity", "Energy Capacity", Kind.FLAT, Unit.FLAT, 0xB6FF3F),
    DRAIN("drain", "Energy Drain", Kind.FLAT, Unit.RATE, 0xFF5A5A),
    ENERGY_PER_CELL("energy_per_cell", "Cell Recharge", Kind.FLAT, Unit.FLAT, 0x7CFF9B),
    DURABILITY("durability", "Durability", Kind.FLAT, Unit.FLAT, 0xDFD0FE),
    QUANTITY("quantity", "Item Quantity", Kind.FLAT, Unit.FLAT, 0xE88A12),
    RARITY("rarity", "Item Rarity", Kind.FLAT, Unit.FLAT, 0xEAFF00),
    HEALTH_REGEN("health_regen", "Health Regen", Kind.FLAT, Unit.RATE, 0x8EFF22),
    DRAIN_MULT("drain_mult", "Energy Drain", Kind.MULTIPLIER, Unit.MULTIPLIER, 0xFF5A5A),
    DURABILITY_DRAIN_MULT("durability_drain_mult", "Durability Wear", Kind.MULTIPLIER, Unit.MULTIPLIER, 0xDFD0FE),
    EFFECT_RESISTANCE("effect_resistance", "Effect Resistance", Kind.INERT, Unit.PERCENT, 0xFEDD00),
    ELITE_RESISTANCE("elite_resistance", "Elite Resistance", Kind.INERT, Unit.PERCENT, 0xFFB000),
    HEALING_EFFICIENCY("healing_efficiency", "Healing Efficiency", Kind.INERT, Unit.PERCENT, 0x62E39A),
    TURRET("turret", "Defense Turret", Kind.INERT, Unit.FLAG, 0xFF4E9A);

    public enum Kind {
        FLAT,
        MULTIPLIER,
        INERT
    }

    /** How a value of this stat is written: a plain number, a per-second rate, a percentage, a multiplier or a yes/no flag. */
    public enum Unit {
        FLAT,
        RATE,
        PERCENT,
        MULTIPLIER,
        FLAG
    }

    private final String id;
    private final String displayName;
    private final Kind kind;
    private final Unit unit;
    private final int color;

    ExtractorStat(String id, String displayName, Kind kind, Unit unit, int color) {
        this.id = id;
        this.displayName = displayName;
        this.kind = kind;
        this.unit = unit;
        this.color = color;
    }

    public String getId() {
        return this.id;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public Kind getKind() {
        return this.kind;
    }

    public Unit getUnit() {
        return this.unit;
    }

    public int getColor() {
        return this.color;
    }

    /** Lower is better for these, so percentage affixes that "reduce" them are written as negatives. */
    public boolean isLowerBetter() {
        return this == DRAIN || this == DRAIN_MULT || this == DURABILITY_DRAIN_MULT;
    }

    @Nullable
    public static ExtractorStat byId(String id) {
        if (id == null) {
            return null;
        }
        for (ExtractorStat stat : values()) {
            if (stat.id.equalsIgnoreCase(id) || stat.name().equalsIgnoreCase(id)) {
                return stat;
            }
        }
        return null;
    }
}
