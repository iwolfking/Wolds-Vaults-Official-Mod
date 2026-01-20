package xyz.iwolfking.woldsvaults.init;

import iskallia.vault.item.crystal.CrystalData;
import xyz.iwolfking.woldsvaults.objectives.ScalingBallisticBingoCrystalObjective;
import xyz.iwolfking.woldsvaults.objectives.speedrun.SpeedrunCrystalObjective;

public class ModCrystalObjectives {
    public static void init() {
        CrystalData.OBJECTIVE.register("brb_speedrun", SpeedrunCrystalObjective.class, SpeedrunCrystalObjective::new);
        CrystalData.OBJECTIVE.register("scaling_ballistic_bingo", ScalingBallisticBingoCrystalObjective.class, ScalingBallisticBingoCrystalObjective::new);
    }
}
