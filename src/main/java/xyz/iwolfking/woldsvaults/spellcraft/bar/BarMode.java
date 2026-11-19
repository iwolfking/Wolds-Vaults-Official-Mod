package xyz.iwolfking.woldsvaults.spellcraft.bar;

import javax.annotation.Nullable;

/**
 * How a player's bar is laid out and cast. Classic mode derives one slot per learned ability and casts it with Vault
 * Hunters' per-ability quick-fire keys; slot mode uses the saved default bar and the spell slot keys.
 */
public enum BarMode {
    CLASSIC,
    SLOT;

    @Nullable
    public static BarMode byOrdinal(int ordinal) {
        BarMode[] modes = values();
        return ordinal >= 0 && ordinal < modes.length ? modes[ordinal] : null;
    }
}
