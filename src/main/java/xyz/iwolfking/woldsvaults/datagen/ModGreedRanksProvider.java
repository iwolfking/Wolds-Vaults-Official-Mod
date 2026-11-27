package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import xyz.iwolfking.woldsvaults.config.GreedRanksConfig;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractWoldsTableProvider;
import xyz.iwolfking.woldsvaults.milestones.MilestoneRankLadder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * The greed rank ladder's numbers: the reputation each rank costs, the god alignment level gating the last rank of
 * each band from Hunter up, the step per rank past Legend and the greed coins a rank-up trial pays per rank. The
 * milestone table reads the same ladder for its challenge reputation, so both come from {@link #ladder()}.
 */
public class ModGreedRanksProvider extends AbstractWoldsTableProvider {
    private static final double CHALLENGE_REPUTATION_SHARE = 0.15D;

    public ModGreedRanksProvider(DataGenerator generator) {
        super(generator, "greed/ranks");
    }

    @Override
    public void registerConfigs() {
        this.table("greed_ranks", ladder());
    }

    public static GreedRanksConfig ladder() {
        GreedRanksConfig ranks = new GreedRanksConfig();
        ranks.thresholds = new ArrayList<>(List.of(0, 75, 100, 125, 150, 175, 200, 250, 300, 400, 500, 550, 600, 800, 900, 1000));
        ranks.godLevelGates = new LinkedHashMap<>();
        ranks.godLevelGates.put(String.valueOf(MilestoneRankLadder.HUNTER_1), 2);
        ranks.godLevelGates.put(String.valueOf(MilestoneRankLadder.MASTER_1), 4);
        ranks.godLevelGates.put(String.valueOf(MilestoneRankLadder.CHAMPION_1), 6);
        ranks.godLevelGates.put(String.valueOf(MilestoneRankLadder.LEGEND), 8);
        ranks.legendPlusStep = 250;
        ranks.trialCoinsPerRank = 50;
        return ranks;
    }

    /** Reputation a challenge milestone tagged with this rank pays: a fixed share of the next rank's threshold, floored. */
    public static int challengeReputation(GreedRanksConfig ladder, int taggedRank) {
        return (int) Math.floor(CHALLENGE_REPUTATION_SHARE * ladder.thresholds.get(taggedRank));
    }
}
