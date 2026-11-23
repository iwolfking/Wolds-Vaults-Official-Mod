package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import xyz.iwolfking.woldsvaults.config.SimonConfig;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractWoldsTableProvider;

/** The Simon's Set set mechanic tuning. */
public class ModSimonProvider extends AbstractWoldsTableProvider {
    public ModSimonProvider(DataGenerator generator) {
        super(generator, "gear/simon");
    }

    /** The tuning table; {@link ModGearSetsProvider} reads the same values. */
    public static SimonConfig simon() {
        SimonConfig table = new SimonConfig();
        table.plantCooldownTicks = 80;
        table.seedSpeed = 1.5F;
        table.seedRange = 32.0F;
        table.growRadius = 6.0F;
        table.growthPerStage = 4.0F;
        table.maxGrowthPerHit = 1.5F;
        table.shrinkTicks = 200;
        table.leashRange = 48.0F;
        table.budRange = 6.0F;
        table.budSpacing = 3.0F;
        table.resonateMinCrystals = 4;
        table.showTicks = 8;
        table.showGapTicks = 4;
        table.inputBaseTicks = 60;
        table.inputTicksPerCrystal = 30;
        table.clickRange = 24.0F;
        table.detonationValues = new float[]{0.0F, 1.0F, 2.0F, 3.5F};
        table.burstRadius = 3.0F;
        table.burstRadiusPerValue = 0.75F;
        table.failDamage = 0.5F;
        table.maxSequenceLength = 10;
        table.lengthDamage = 0.15F;
        table.facetDamage = 0.25F;
        table.facetSlowTicks = 40;
        table.shardFieldTicks = 80;
        table.shardFieldRadius = 3.0F;
        table.shardFieldDamage = 0.15F;
        table.finaleRadius = 4.0F;
        table.finaleRadiusPerValue = 0.75F;
        table.finaleMaxRadius = 14.0F;
        table.finaleMaxPrismaticMultiplier = 8.0F;
        table.brittleResistance = 0.02F;
        table.reflectRadius = 1.25F;
        table.reflectRange = 24.0F;
        table.arcaneReflectDamage = 1.0F;
        table.prismReach = 1.5F;
        return table;
    }

    @Override
    public void registerConfigs() {
        this.table("simon", simon());
    }
}
