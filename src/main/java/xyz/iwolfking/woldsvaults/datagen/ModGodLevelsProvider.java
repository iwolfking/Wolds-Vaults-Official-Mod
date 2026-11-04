package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import xyz.iwolfking.woldsvaults.config.gods.GodLevelsConfig;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractWoldsTableProvider;

/**
 * The god level curve: cumulative XP thresholds up to the last defined level and a flat cost per level past it,
 * god points from levels and from reputation, and the levels that open minor transfer slots and the ultimate.
 */
public class ModGodLevelsProvider extends AbstractWoldsTableProvider {

    public ModGodLevelsProvider(DataGenerator generator) {
        super(generator, "gods/levels");
    }

    @Override
    public void registerConfigs() {
        this.table("god_levels", levels());
    }

    /** The god level table, shared with the description text that quotes it. */
    public static GodLevelsConfig levels() {
        GodLevelsConfig levels = new GodLevelsConfig();
        levels.cumulativeXp = new long[]{20000L, 60000L, 120000L, 200000L, 300000L, 400000L, 500000L, 650000L, 800000L, 1000000L};
        levels.maxDefinedLevel = 10;
        levels.xpPerLevelPastMax = 150000L;
        levels.ultimateUnlockLevel = 5;
        levels.pointsAtLevelOne = 3;
        levels.pointsPerLevelToMax = 3;
        levels.pointsPerLevelPastMax = 2;
        levels.minorTransferSlotLevels = new int[]{2, 4, 7};
        levels.chartingReputation = 10;
        levels.reputationPerGodPoint = 10;
        levels.baseAltarXp = 300.0D;
        return levels;
    }
}
