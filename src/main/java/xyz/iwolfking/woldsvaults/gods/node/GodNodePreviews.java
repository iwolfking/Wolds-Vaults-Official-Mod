package xyz.iwolfking.woldsvaults.gods.node;

import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * The god nodes whose description carries a live formula rather than a fixed number, plus the
 * server-side resolvers behind them. The formula functions here build the text both the descriptions datagen
 * (from the effect table) and the runtime previews (from the loaded effect) emit, so the two always match.
 */
public final class GodNodePreviews {
    public static final String MALEDICTION_FORMULA = "cubeRoot(1 + healing efficiency)";

    public record Preview(double multiplier, List<Component> lines) {
    }

    @FunctionalInterface
    public interface Resolver {
        Preview resolve(ServerPlayer player);
    }

    private record Entry(Supplier<String> formulaText, Resolver resolver) {
    }

    private static final Map<String, Entry> ENTRIES = new LinkedHashMap<>();

    private GodNodePreviews() {
    }

    /** Indiana Jones' multiplier, {@code cbrt((reference + disarm%) / reference)}. */
    public static String indianaJonesFormula(double reference) {
        String referenceText = number(reference);
        return "cubeRoot((" + referenceText + " + trap disarm) / " + referenceText + ")";
    }

    /** Looting Engine's multiplier, {@code cbrt((reference + c) / reference)}. */
    public static String lootingEngineFormula(double reference) {
        String referenceText = number(reference);
        return "cubeRoot((" + referenceText + " + c) / " + referenceText + ")";
    }

    /** Sack of Mobs' multiplier, {@code log_base(base + kills)}. */
    public static String sackOfMobsFormula(double logBase) {
        String baseText = number(logBase);
        return "log" + baseText + "(" + baseText + " + kills)";
    }

    /** Paced Strikes' multiplier, {@code sqrt((reference + t) / reference)}. */
    public static String pacedStrikesFormula(double referenceMinutes) {
        String referenceText = number(referenceMinutes);
        return "sqrt((" + referenceText + " + t) / " + referenceText + ")";
    }

    /**
     * Registers the live formula of one effect; registering an effect twice is fatal. The formula text is
     * resolved on every query, so it can read the effect's parameters once the god tables have loaded.
     */
    public static synchronized void register(String effectId, Supplier<String> formulaText, Resolver resolver) {
        if (effectId == null || effectId.isBlank() || formulaText == null || resolver == null) {
            throw GodTreeConfigException.fail("Refusing to register a god node preview with a blank effect id or formula");
        }
        if (ENTRIES.containsKey(effectId)) {
            throw GodTreeConfigException.fail("God node preview for '" + effectId + "' is already registered");
        }
        ENTRIES.put(effectId, new Entry(formulaText, resolver));
    }

    public static synchronized boolean isDynamic(String effectId) {
        return ENTRIES.containsKey(effectId);
    }

    /**
     * The formula text of an effect from its loaded parameters, or null when it has no live formula. A formula
     * that cannot be resolved (its effect is not loaded) logs an error and reads as null, so the node shows as static.
     */
    @Nullable
    public static String formulaText(String effectId) {
        Entry entry;
        synchronized (GodNodePreviews.class) {
            entry = ENTRIES.get(effectId);
        }
        if (entry == null) {
            return null;
        }
        try {
            String text = entry.formulaText().get();
            if (text == null || text.isBlank()) {
                WoldsVaults.LOGGER.error("God node preview for '{}' resolved a blank formula; showing it as static.", effectId);
                return null;
            }
            return text;
        } catch (RuntimeException e) {
            WoldsVaults.LOGGER.error("God node preview for '{}' could not resolve its formula; showing it as static.",
                    effectId, e);
            return null;
        }
    }

    /** The player's current preview for an effect, or empty when the effect has no live formula. */
    public static Optional<Preview> resolve(ServerPlayer player, String effectId) {
        Entry entry;
        synchronized (GodNodePreviews.class) {
            entry = ENTRIES.get(effectId);
        }
        if (entry == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(entry.resolver().resolve(player));
    }

    /** The bonus a multiplier amounts to, one decimal and an explicit sign: 1.042 reads {@code +4.2%}. */
    public static String percentText(double multiplier) {
        double percent = Math.round((multiplier - 1.0D) * 1000.0D) / 10.0D;
        return (percent < 0.0D ? "-" : "+") + number(Math.abs(percent)) + "%";
    }

    /** A signed percentage for an input that is stored as a fraction: 1.2 reads {@code +120%}. */
    public static String signedPercent(double fraction) {
        double percent = Math.round(fraction * 1000.0D) / 10.0D;
        return (percent < 0.0D ? "-" : "+") + number(Math.abs(percent)) + "%";
    }

    public static String number(double value) {
        String text = String.format(Locale.ROOT, "%.2f", value);
        if (text.indexOf('.') >= 0) {
            text = text.replaceAll("0+$", "");
            if (text.endsWith(".")) {
                text = text.substring(0, text.length() - 1);
            }
        }
        return text.equals("-0") ? "0" : text;
    }

    public static String multiplier(double value) {
        return String.format(Locale.ROOT, "%.3f", value);
    }

    public static MutableComponent hoverText(List<Component> lines) {
        MutableComponent hover = new TextComponent("");
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) {
                hover.append(new TextComponent("\n"));
            }
            hover.append(lines.get(i));
        }
        return hover;
    }

    /** Builds the hover lines of a preview: formula, inputs with their values, worked result, notes. */
    public static final class Working {
        private final VaultGod god;
        private final List<Component> lines = new ArrayList<>();

        public Working(VaultGod god) {
            this.god = god;
        }

        public Working formula(String quantity, String formula) {
            this.lines.add(new TextComponent(quantity + " = ").withStyle(ChatFormatting.WHITE)
                    .append(new TextComponent(formula).setStyle(this.accent())));
            return this;
        }

        public Working input(String symbol, String meaning, String value) {
            this.lines.add(new TextComponent(symbol).setStyle(this.accent())
                    .append(new TextComponent(" = " + meaning + " = ").withStyle(ChatFormatting.GRAY))
                    .append(new TextComponent(value).withStyle(ChatFormatting.WHITE)));
            return this;
        }

        public Working result(String worked, double multiplier) {
            this.lines.add(new TextComponent("= " + worked + " = " + multiplier(multiplier)).withStyle(ChatFormatting.WHITE)
                    .append(new TextComponent("  (" + percentText(multiplier) + ")").setStyle(this.accent())));
            return this;
        }

        public Working note(String text) {
            this.lines.add(new TextComponent(text).withStyle(ChatFormatting.GRAY));
            return this;
        }

        public Working inactive(boolean inactive) {
            if (inactive) {
                this.note("This star is not active for you right now; this is what it would give.");
            }
            return this;
        }

        public Preview build(double multiplier) {
            return new Preview(multiplier, List.copyOf(this.lines));
        }

        private Style accent() {
            return Style.EMPTY.withColor(TextColor.fromRgb(this.god.getColor() & 0xFFFFFF));
        }
    }
}
