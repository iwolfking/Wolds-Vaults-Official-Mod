package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tuning for the Tidebreaker riptide dash, generated as {@code vault_configs/gear/riptide/riptide.json}: hover, shock
 * cone, storm bolts and wall contact.
 */
public class RiptideConfig extends Config implements WoldsTable {
    private static final AtomicBoolean WARNED = new AtomicBoolean();

    @Expose public float hoverHorizontalFriction;
    @Expose public float hoverVerticalDamping;
    @Expose public float shockConeAhead;
    @Expose public float shockConeBehind;
    @Expose public float shockConeRadius;
    @Expose public int stormVisualBoltsPerDash;
    @Expose public float contactInflate;
    @Expose public float stallSpeed;
    @Expose public int launchGraceTicks;

    /** Blocks the swept box that finds riptide contacts grows by on every side. */
    public float contactInflate() {
        return this.contactInflate;
    }

    /** Horizontal blocks per tick below which a dash that runs into a wall ends; faster dashes slide along it. */
    public float stallSpeed() {
        return this.stallSpeed;
    }

    /** Ticks after a launch during which a wall never ends the dash. */
    public int launchGraceTicks() {
        return this.launchGraceTicks;
    }

    /** The installed table, or null (logged once) until it is installed; callers treat null as the mechanic being off. */
    @Nullable
    public static RiptideConfig installed() {
        RiptideConfig config = ModConfigs.RIPTIDE;
        if (config == null && WARNED.compareAndSet(false, true)) {
            WoldLogger.error("Riptide was used before the gear/riptide/riptide table was installed; it does nothing until it is");
        }
        return config;
    }

    @Override
    public String getName() {
        return "riptide";
    }

    @Override
    protected void reset() {
    }

    @Nullable
    @Override
    public String validate() {
        if (this.shockConeAhead < 0.0F || this.shockConeBehind < 0.0F || this.shockConeRadius < 0.0F || this.contactInflate < 0.0F) {
            return "a shock cone or contact distance is negative";
        }
        return null;
    }
}
