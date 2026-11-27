package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tuning for the Ten Magatsuhi set mechanic, generated as {@code vault_configs/gear/magatsuhi/magatsuhi.json}. Ten
 * Magatsuhi tuning (Daikyo and Shokyo, two katanas dual wielded; a set katana in either hand counts). magatsuhi_combo
 * grant = the finisher's extra damage: every comboHits-th melee hit in a row deals that much more, and the count
 * starts over after comboResetTicks without a hit. magatsuhi_cut grant = the swap slash's damage as a share of attack
 * damage. Right click with either katana in the main hand cuts a 1 wide, 2 tall square out of the block or enemy aimed
 * at within cutRange, or out of the air airDistance ahead; blocks with a tile entity, unbreakable blocks and fluids
 * can't be cut. The cut waits markTicks for the second right click, which swaps: an enemy trades places with the
 * wearer; a wall's blocks move to where the wearer stood (into air only; anything that can't go there waits to be put
 * back) and the wearer steps into the hole; the air just takes them there. The swap slashes every enemy within
 * slashRadius of where the wearer lands. The third right click, or returnTicks after the swap, puts everything back:
 * moved blocks return to the hole (one broken meanwhile stays gone), a swapped enemy within swapBackRange trades
 * places again, and the wearer returns to where they started. Then cooldownTicks before the next cut.
 */
public class MagatsuhiConfig extends Config implements WoldsTable {
    private static final AtomicBoolean WARNED = new AtomicBoolean();

    @Expose public int comboHits;
    @Expose public int comboResetTicks;
    @Expose public float cutRange;
    @Expose public float airDistance;
    @Expose public int markTicks;
    @Expose public int returnTicks;
    @Expose public int cooldownTicks;
    @Expose public float slashRadius;
    @Expose public float swapBackRange;

    /** The installed table, or null (logged once) until it is installed; callers treat null as the mechanic being off. */
    @Nullable
    public static MagatsuhiConfig installed() {
        MagatsuhiConfig config = ModConfigs.MAGATSUHI;
        if (config == null && WARNED.compareAndSet(false, true)) {
            WoldLogger.error("Ten Magatsuhi was used before the gear/magatsuhi/magatsuhi table was installed; it does nothing until it is");
        }
        return config;
    }

    @Override
    public String getName() {
        return "magatsuhi";
    }

    @Override
    protected void reset() {
    }

    @Nullable
    @Override
    public String validate() {
        if (this.comboHits < 1 || this.cutRange <= 0.0F) {
            return "comboHits is below 1 or the cut range is not positive";
        }
        return null;
    }
}
