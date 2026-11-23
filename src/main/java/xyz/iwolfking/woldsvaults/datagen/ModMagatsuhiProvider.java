package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import xyz.iwolfking.woldsvaults.config.MagatsuhiConfig;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractWoldsTableProvider;

/** The Ten Magatsuhi set mechanic tuning. */
public class ModMagatsuhiProvider extends AbstractWoldsTableProvider {
    public ModMagatsuhiProvider(DataGenerator generator) {
        super(generator, "gear/magatsuhi");
    }

    /** The tuning table; {@link ModGearSetsProvider} reads the same values. */
    public static MagatsuhiConfig magatsuhi() {
        MagatsuhiConfig table = new MagatsuhiConfig();
        table.comboHits = 3;
        table.comboResetTicks = 60;
        table.cutRange = 16.0F;
        table.airDistance = 6.0F;
        table.markTicks = 200;
        table.returnTicks = 400;
        table.cooldownTicks = 40;
        table.slashRadius = 4.0F;
        table.swapBackRange = 32.0F;
        return table;
    }

    @Override
    public void registerConfigs() {
        this.table("magatsuhi", magatsuhi());
    }
}
