package xyz.iwolfking.woldsvaults.client.screens.greed;

import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;

/**
 * The looks a greed milestone row can take, kept in the client config (Icon Chip unless set otherwise there).
 * Each has its row height (0 keeps the screen size's own), whether it sits in a rarity frame, and whether that frame
 * wears its flourishes. Holds no client-only classes, since the config that names it is built on both sides.
 */
public enum GreedRowStyle {
    CLASSIC("classic", 0, true, true),
    NAVY("navy", 40, true, true),
    REWARD_CHIP("reward_chip", 40, true, true),
    ICON_CHIP("icon_chip", 44, true, true),
    TIER_LADDER("tier_ladder", 44, true, true),
    TIER_BADGE("tier_badge", 40, true, true),
    COMPACT("compact", 18, true, false),
    BIG_CLAIM("big_claim", 40, true, true),
    TIER_CHIPS("tier_chips", 42, true, true),
    ADVANCEMENT("advancement", 36, true, true),
    LEDGER("ledger", 24, false, false),
    FOLDER_CARD("folder_card", 48, true, true);

    private final String key;
    private final int height;
    private final boolean framed;
    private final boolean ornaments;

    GreedRowStyle(String key, int height, boolean framed, boolean ornaments) {
        this.key = key;
        this.height = height;
        this.framed = framed;
        this.ornaments = ornaments;
    }

    public String key() {
        return this.key;
    }

    public int height(int screenRowHeight) {
        return this.height > 0 ? this.height : screenRowHeight;
    }

    public boolean framed() {
        return this.framed;
    }

    public boolean ornaments() {
        return this.ornaments;
    }

    public boolean navy() {
        return this != CLASSIC;
    }

    /** The style in the client config, or {@link #ICON_CHIP} before it has loaded. */
    public static GreedRowStyle current() {
        try {
            return WoldsVaultsConfig.CLIENT.greedRowStyle.get();
        } catch (IllegalStateException | NullPointerException e) {
            WoldLogger.errorOnce("greed_row_style", "Greed row style config is not loaded; using {}", ICON_CHIP, e);
            return ICON_CHIP;
        }
    }
}
