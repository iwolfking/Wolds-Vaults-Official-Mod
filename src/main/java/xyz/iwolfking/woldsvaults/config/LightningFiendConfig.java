package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tuning for the Lightning Fiend set mechanic, generated as {@code vault_configs/gear/lightning_fiend/lightning_fiend.json}:
 * chaining, lightning form, its dash, arc lash and rod, the thunderstrike and its charge.
 */
public class LightningFiendConfig extends Config implements WoldsTable {
    private static final AtomicBoolean WARNED = new AtomicBoolean();

    @Expose public float chainRange;
    @Expose public int formDurationTicks;
    @Expose public float dashDistance;
    @Expose public float dashWidth;
    @Expose public int dashCooldownTicks;
    @Expose public int dashTravelTicks;
    @Expose public float arcLashRange;
    @Expose public float arcLashDamage;
    @Expose public float arcLashStrikeDamage;
    @Expose public float arcLashConeAngle;
    @Expose public int arcLashBolts;
    @Expose public float arcLashSpeed;
    @Expose public int arcLashCooldownTicks;
    @Expose public float rodRange;
    @Expose public float rodLockAngle;
    @Expose public float rodOvershoot;
    @Expose public float rodDamageMultiplier;
    @Expose public int rodCooldownTicks;
    @Expose public float thunderBaseDamage;
    @Expose public float thunderMaxMultiplier;
    @Expose public float thunderBaseRadius;
    @Expose public float thunderMaxRadius;
    @Expose public float chargeMax;
    @Expose public float chargePerThrowHit;
    @Expose public float chargePerArcLash;
    @Expose public float chargePerDash;
    @Expose public float chargePerRod;
    @Expose public float chargeBossMultiplier;
    @Expose public float formResistanceCapBonus;

    /** The installed table, or null (logged once) until it is installed; callers treat null as the mechanic being off. */
    @Nullable
    public static LightningFiendConfig installed() {
        LightningFiendConfig config = ModConfigs.LIGHTNING_FIEND;
        if (config == null && WARNED.compareAndSet(false, true)) {
            WoldLogger.error("Lightning Fiend was used before the gear/lightning_fiend/lightning_fiend table was installed; it does nothing until it is");
        }
        return config;
    }

    @Override
    public String getName() {
        return "lightning_fiend";
    }

    @Override
    protected void reset() {
    }

    @Nullable
    @Override
    public String validate() {
        if (this.formDurationTicks <= 0 || this.chargeMax <= 0.0F) {
            return "the form duration or maximum charge is not positive";
        }
        return null;
    }
}
