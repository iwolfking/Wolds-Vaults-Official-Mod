package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import xyz.iwolfking.woldsvaults.config.LightningFiendConfig;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractWoldsTableProvider;

/** The Lightning Fiend set mechanic tuning. */
public class ModLightningFiendProvider extends AbstractWoldsTableProvider {
    public ModLightningFiendProvider(DataGenerator generator) {
        super(generator, "gear/lightning_fiend");
    }

    /** The tuning table; {@link ModGearSetsProvider} formats its set tooltips from the same values. */
    public static LightningFiendConfig lightningFiend() {
        LightningFiendConfig table = new LightningFiendConfig();
        table.chainRange = 6.0F;
        table.formDurationTicks = 200;
        table.dashDistance = 7.0F;
        table.dashWidth = 1.5F;
        table.dashCooldownTicks = 10;
        table.dashTravelTicks = 2;
        table.arcLashRange = 12.0F;
        table.arcLashDamage = 0.5F;
        table.arcLashStrikeDamage = 0.05F;
        table.arcLashConeAngle = 30.0F;
        table.arcLashBolts = 5;
        table.arcLashSpeed = 1.2F;
        table.arcLashCooldownTicks = 10;
        table.rodRange = 24.0F;
        table.rodLockAngle = 60.0F;
        table.rodOvershoot = 0.5F;
        table.rodDamageMultiplier = 1.5F;
        table.rodCooldownTicks = 10;
        table.thunderBaseDamage = 5.0F;
        table.thunderMaxMultiplier = 30.0F;
        table.thunderBaseRadius = 3.0F;
        table.thunderMaxRadius = 7.0F;
        table.chargeMax = 500.0F;
        table.chargePerThrowHit = 5.0F;
        table.chargePerArcLash = 5.0F;
        table.chargePerDash = 10.0F;
        table.chargePerRod = 15.0F;
        table.chargeBossMultiplier = 3.0F;
        table.formResistanceCapBonus = 0.1F;
        return table;
    }

    @Override
    public void registerConfigs() {
        this.table("lightning_fiend", lightningFiend());
    }
}
