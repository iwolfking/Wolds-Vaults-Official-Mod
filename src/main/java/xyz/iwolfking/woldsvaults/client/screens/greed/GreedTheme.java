package xyz.iwolfking.woldsvaults.client.screens.greed;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import xyz.iwolfking.woldsvaults.milestones.MilestoneRankLadder;

import java.util.Locale;

/** Palette, rank naming and number formatting shared by every greed screen widget. */
public final class GreedTheme {
    public static final int GOLD = 0xFFFFC44D;
    public static final int GOLD_DIM = 0xFF8A6A22;
    public static final int GOLD_DEEP = 0xFF6B4E12;
    public static final int PLATE = 0xFF2B2B2B;
    public static final int PLATE_DARK = 0xFF1E1E1E;
    public static final int PLATE_LIGHT = 0xFF3A3A3A;
    public static final int TRACK = 0xFF0F0F0F;
    public static final int TRACK_SHADOW = 0xFF000000;
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

    /** Roman numeral for a completed tier count; tier 0 renders blank. */
    public static String roman(int tier) {
        if (tier <= 0) {
            return "";
        }
        return tier < ROMAN.length ? ROMAN[tier] : String.valueOf(tier);
    }

    /** Compact number for threshold lists: 1200 becomes 1.2K, 50000 becomes 50K, 1e12 becomes 1T. */
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

    /** Compact elapsed time from ticks: 36000 becomes 30m, 180000 becomes 2h30m. */
    public static String duration(long ticks) {
        long minutes = Math.max(0L, ticks) / (20L * 60L);
        long hours = minutes / 60L;
        long remainder = minutes % 60L;
        if (hours <= 0L) {
            return remainder + "m";
        }
        return remainder == 0L ? hours + "h" : hours + "h" + remainder + "m";
    }

    /** Elapsed time from ticks: hours and minutes once past an hour, minutes and seconds below it. */
    public static String durationExact(long ticks) {
        long seconds = Math.max(0L, ticks) / 20L;
        long hours = seconds / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        if (hours > 0L) {
            return hours + "h " + minutes + "m";
        }
        if (minutes > 0L) {
            return minutes + "m " + (seconds % 60L) + "s";
        }
        return (seconds % 60L) + "s";
    }

    public static Component durationProgress(long current, long target) {
        long ceiling = Math.max(target, 0L);
        long shown = Math.min(Math.max(current, 0L), ceiling);
        int percent = ceiling <= 0L ? 0 : (int) Math.min(100L, shown * 100L / ceiling);
        return text(durationExact(shown) + "/" + durationExact(ceiling) + " (" + percent + "%)", GOLD);
    }

    /** Full precision with thousands separators. */
    public static String exact(long value) {
        return String.format(Locale.ROOT, "%,d", value);
    }

    /** The shared bar hover readout {@code 6,283/20,000 (31%)}; current is clamped to target, percent floored. */
    public static Component progress(long current, long target) {
        long ceiling = Math.max(target, 0L);
        long shown = Math.min(Math.max(current, 0L), ceiling);
        int percent = ceiling <= 0L ? 0 : (int) Math.min(100L, shown * 100L / ceiling);
        return text(exact(shown) + "/" + exact(ceiling) + " (" + percent + "%)", GOLD);
    }

    private static String trim(double value) {
        if (value >= 100.0D || value == Math.floor(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(Math.round(value * 10.0D) / 10.0D);
    }

    /** Short rank badge: S1 through C3, then Leg and Leg+N for every rank past Legend. */
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
        String band = MilestoneRankLadder.getBandName(rank);
        if (band.isEmpty()) {
            return "-";
        }
        return Character.toUpperCase(band.charAt(0)) + String.valueOf(MilestoneRankLadder.getTierInBand(rank));
    }

    /** The rank medallion's label: the band initial and tier for named ranks, {@code L} from Legend up. */
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

    public static String rankUnlockKey(int rank) {
        if (rank > MilestoneRankLadder.LEGEND_RANK) {
            return LANG_ROOT + "unlock.legend_plus";
        }
        return LANG_ROOT + "unlock." + Math.max(rank, MilestoneRankLadder.FIRST_RANK);
    }
}
