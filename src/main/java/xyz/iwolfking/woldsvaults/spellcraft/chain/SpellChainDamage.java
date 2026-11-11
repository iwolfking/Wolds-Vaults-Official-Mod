package xyz.iwolfking.woldsvaults.spellcraft.chain;

/**
 * The WV chain scope: open while a Chaining glyph's chain deals its links. A chain hit is pure damage, so every WV
 * on-hit handler and attacker-side damage increase (and the VH ones WV switches off through mixins) returns early
 * while it is open. VH's own {@code IS_CHAINING_ATTACKING} flag is set around the same hits. Server thread only.
 */
public final class SpellChainDamage {
    private static int depth;

    private SpellChainDamage() {
    }

    public static boolean active() {
        return depth > 0;
    }

    public static void run(Runnable action) {
        depth++;
        try {
            action.run();
        } finally {
            depth--;
        }
    }
}
