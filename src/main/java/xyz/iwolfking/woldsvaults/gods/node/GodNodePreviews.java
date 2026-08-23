package xyz.iwolfking.woldsvaults.gods.node;

import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * The god nodes whose description carries a live formula rather than a fixed number, and the
 * server-side resolvers that turn that formula into the bonus the player has right now.
 *
 * <p>The gods tab asks the server for the selected node's preview and the reply carries the
 * formula text to swap out of the description, the multiplier it currently resolves to and the
 * worked math shown when the value is hovered. Each tree registers its own previews beside its
 * handler types in {@code GodNodeHandlerTypes.bootstrap()}; the formula constants here are what
 * the descriptions datagen emits, so the text the screen looks for and the text the description
 * holds cannot drift apart.
 */
public final class GodNodePreviews {
    public static final String MALEDICTION_FORMULA = "cubeRoot(1 + healing efficiency)";
    public static final String INDIANA_JONES_FORMULA = "cubeRoot((1000 + trap disarm) / 1000)";
    public static final String LOOTING_ENGINE_FORMULA = "cubeRoot((1200 + c) / 1200)";
    public static final String SACK_OF_MOBS_FORMULA = "log1000(1000 + kills)";
    public static final String PACED_STRIKES_FORMULA = "sqrt((50 + t) / 50)";

    /** A resolved preview: the multiplier the formula gives right now and the math behind it, top line first. */
    public record Preview(double multiplier, List<Component> lines) {
    }

    @FunctionalInterface
    public interface Resolver {
        Preview resolve(ServerPlayer player);
    }

    private record Entry(String formulaText, Resolver resolver) {
    }

    private static final Map<String, Entry> ENTRIES = new LinkedHashMap<>();

    private GodNodePreviews() {
    }

    /**
     * Registers the live formula of one effect. Registering an effect twice is a programming error
     * and is fatal, the same way a duplicate handler type is.
     */
    public static synchronized void register(String effectId, String formulaText, Resolver resolver) {
        if (effectId == null || effectId.isBlank() || formulaText == null || formulaText.isBlank() || resolver == null) {
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

    @Nullable
    public static synchronized String formulaText(String effectId) {
        Entry entry = ENTRIES.get(effectId);
        return entry == null ? null : entry.formulaText();
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

    /** A number for the worked math: whole when it is whole, otherwise up to two decimals. */
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

    /** A multiplier for the worked math, always to three decimals so small bonuses do not vanish. */
    public static String multiplier(double value) {
        return String.format(Locale.ROOT, "%.3f", value);
    }

    /** The hover body for a preview: its lines joined with line breaks into one component. */
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

    /**
     * Builds the hover lines of a preview in the one shape every formula node shares: the formula,
     * each input with its meaning and current value, the worked result, and a note when the star
     * is not currently applying.
     */
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

        /** Adds the standard note when the star is not applying to the player right now. */
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
