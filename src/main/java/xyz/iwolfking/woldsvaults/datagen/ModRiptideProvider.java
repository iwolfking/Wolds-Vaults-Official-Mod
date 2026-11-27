package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import xyz.iwolfking.woldsvaults.config.RiptideConfig;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractWoldsTableProvider;

/** The Tidebreaker riptide dash tuning. */
public class ModRiptideProvider extends AbstractWoldsTableProvider {
    public ModRiptideProvider(DataGenerator generator) {
        super(generator, "gear/riptide");
    }

    /** The tuning table; {@link ModGearSetsProvider} formats its set tooltips from the same values. */
    public static RiptideConfig riptide() {
        RiptideConfig table = new RiptideConfig();
        table.hoverHorizontalFriction = 0.8F;
        table.hoverVerticalDamping = 0.5F;
        table.shockConeAhead = 2.0F;
        table.shockConeBehind = 3.0F;
        table.shockConeRadius = 4.0F;
        table.stormVisualBoltsPerDash = 3;
        table.contactInflate = 0.5F;
        table.stallSpeed = 0.1F;
        table.launchGraceTicks = 3;
        return table;
    }

    @Override
    public void registerConfigs() {
        this.table("riptide", riptide());
    }
}
