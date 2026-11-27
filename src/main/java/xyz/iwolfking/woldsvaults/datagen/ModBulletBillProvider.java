package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import xyz.iwolfking.woldsvaults.config.BulletBillConfig;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractWoldsTableProvider;

/** The Bullet Bill set mechanic tuning. */
public class ModBulletBillProvider extends AbstractWoldsTableProvider {
    public ModBulletBillProvider(DataGenerator generator) {
        super(generator, "gear/bullet_bill");
    }

    /** The tuning table; {@link ModGearSetsProvider} reads the same values. */
    public static BulletBillConfig bulletBill() {
        BulletBillConfig table = new BulletBillConfig();
        table.cooldownTicks = 400;
        table.jetUpgrades = 5;
        table.jetPower = 1.0F;
        table.flightStabilizers = false;
        table.smartHover = false;
        table.boostTicks = 20;
        table.boostSpeed = 1.75F;
        table.spinTicks = 20;
        table.detonationNoDashPercent = 0.25F;
        table.detonationMinSpeed = 0.5F;
        table.detonationRadius = 4.0F;
        table.detonationRadiusPerSpeed = 1.5F;
        table.detonationMaxRadius = 14.0F;
        table.detonationOvertimeTicks = 200;
        table.banzaiScale = 1.5F;
        table.kingScale = 2.0F;
        table.giantReachShare = 1.0F;
        table.cruiseSpeed = 0.7F;
        table.kingHeatRadius = 12.0F;
        table.kingHeatDamage = 0.5F;
        table.kingHeatIntervalTicks = 10;
        return table;
    }

    @Override
    public void registerConfigs() {
        this.table("bullet_bill", bulletBill());
    }
}
