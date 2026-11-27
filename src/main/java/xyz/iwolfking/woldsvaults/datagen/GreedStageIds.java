package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.milestones.MilestoneRankLadder;

import java.util.List;

/** Builds the ids of greed stage content ({@code woldsvaults:greed/stage_<n>/<path>}) and holds each stage's unlock rank. */
public final class GreedStageIds {
    private static final List<Integer> UNLOCK_RANKS = List.of(
            MilestoneRankLadder.LOOTER_1, MilestoneRankLadder.LOOTER_3, MilestoneRankLadder.HUNTER_2);

    private GreedStageIds() {
    }

    public static ResourceLocation id(int stage, String path) {
        return WoldsVaults.id("greed/stage_" + stage + "/" + path);
    }

    /** The same id as a string, for references written inside palette and loot entries. */
    public static String ref(int stage, String path) {
        return id(stage, path).toString();
    }

    /** The number of greed stages. */
    public static int stageCount() {
        return UNLOCK_RANKS.size();
    }

    /** The medallion rank that unlocks a 1-based stage; refuses a stage that does not exist. */
    public static int unlockRank(int stage) {
        if (stage < 1 || stage > UNLOCK_RANKS.size()) {
            throw new IllegalArgumentException("Greed stage " + stage + " has no unlock rank");
        }
        return UNLOCK_RANKS.get(stage - 1);
    }
}
