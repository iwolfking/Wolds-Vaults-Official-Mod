package xyz.iwolfking.woldsvaults.spellcraft.scope;

/**
 * The WV chain scope: open while a Chaining glyph's chain deals its links. A chain hit is pure damage, so every WV
 * on-hit handler and attacker-side damage increase, and the VH ones WV switches off through mixins, returns early while
 * it is open. Server thread only.
 */
public final class ChainScope {
    private static int depth;

    private ChainScope() {
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
