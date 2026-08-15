package xyz.iwolfking.woldsvaults.client.screens.greed;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import xyz.iwolfking.woldsvaults.milestones.MilestoneRankLadder;

/**
 * Palette and text helpers shared by every greed screen widget: dark grey plates with gold
 * highlights, plus the rank naming and number formatting the design sheet asks for.
 */
public final class GreedTheme {
    public static final int GOLD = 0xFFFFC44D;
    public static final int GOLD_DIM = 0xFF8A6A22;
    public static final int GOLD_DEEP = 0xFF6B4E12;
    public static final int PLATE = 0xFF2B2B2B;
    public static final int PLATE_DARK = 0xFF1E1E1E;
    public static final int PLATE_LIGHT = 0xFF3A3A3A;
    public static final int BORDER = 0xFF141414;
    public static final int TEXT = 0xFFDCDCDC;
    public static final int TEXT_DIM = 0xFF8A8A8A;
    public static final int TEXT_TITLE = 0xFFFFFFFF;

    public static final String LANG_ROOT = "screen.woldsvaults.greed.";

    private static final String[] ROMAN = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};

    private GreedTheme() {
    }

    public static MutableComponent text(String value, int color) {
        return new TextComponent(value).setStyle(Style.EMPTY.withColor(color));
    }

    public static MutableComponent lang(String suffix, Object... args) {
        return new TranslatableComponent(LANG_ROOT + suffix, args);
    }

    public static MutableComponent langColored(String suffix, int color, Object... args) {
        return new TranslatableComponent(LANG_ROOT + suffix, args).setStyle(Style.EMPTY.withColor(color));
    }

    /**
     * Roman numeral for a completed tier count. Tier 0 renders blank so a milestone with nothing
     * finished shows an empty left end on its progress bar rather than a zero.
     */
    public static String roman(int tier) {
        if (tier <= 0) {
            return "";
        }
        return tier < ROMAN.length ? ROMAN[tier] : String.valueOf(tier);
    }

    /**
     * Compact number for threshold lists: 1200 becomes 1.2K, 50000 becomes 50K, 1e12 becomes 1T.
     * Threshold lists carry up to five values per row and do not fit at full precision.
     */
    public static String compact(long value) {
        if (value < 1_000L) {
            return String.valueOf(value);
        }
        if (value < 1_000_000L) {
            return trim(value / 1_000.0D) + "K";
        }
        if (value < 1_000_000_000L) {
            return trim(value / 1_000_000.0D) + "M";
        }
        if (value < 1_000_000_000_000L) {
            return trim(value / 1_000_000_000.0D) + "B";
        }
        return trim(value / 1_000_000_000_000.0D) + "T";
    }

    private static String trim(double value) {
        if (value >= 100.0D || value == Math.floor(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(Math.round(value * 10.0D) / 10.0D);
    }

    /**
     * Short rank badge shown on the flanks of the reputation bar: S1 through C3, then Leg and
     * Leg+N for every rank past Legend.
     */
    public static String rankShortLabel(int rank) {
        if (rank < MilestoneRankLadder.FIRST_RANK) {
            return "-";
        }
        if (rank > MilestoneRankLadder.LEGEND_RANK) {
            return "Leg+" + (rank - MilestoneRankLadder.LEGEND_RANK);
        }
        if (rank == MilestoneRankLadder.LEGEND_RANK) {
            return "Leg";
        }
        int band = (rank - 1) / 3;
        int step = (rank - 1) % 3 + 1;
        return switch (band) {
            case 0 -> "S" + step;
            case 1 -> "L" + step;
            case 2 -> "H" + step;
            case 3 -> "M" + step;
            default -> "C" + step;
        };
    }

    /**
     * Single glyph drawn inside the rank medallion: the band initial for named ranks, the tier
     * number for Legend and beyond.
     */
    public static String rankMedallionGlyph(int rank) {
        if (rank < MilestoneRankLadder.FIRST_RANK) {
            return "-";
        }
        if (rank >= MilestoneRankLadder.LEGEND_RANK) {
            return "L";
        }
        return rankShortLabel(rank);
    }

    public static Component rankName(int rank) {
        if (rank < MilestoneRankLadder.FIRST_RANK) {
            return lang("rank.none");
        }
        if (rank > MilestoneRankLadder.LEGEND_RANK) {
            return lang("rank.legend_plus", (Object) (rank - MilestoneRankLadder.LEGEND_RANK));
        }
        return lang("rank." + rank);
    }

    /**
     * Lang key holding the "gain on rank up" lines for a rank, authored one entry per rank from the
     * design sheet's rank list.
     */
    public static String rankUnlockKey(int rank) {
        if (rank > MilestoneRankLadder.LEGEND_RANK) {
            return LANG_ROOT + "unlock.legend_plus";
        }
        return LANG_ROOT + "unlock." + Math.max(rank, MilestoneRankLadder.FIRST_RANK);
    }
}
