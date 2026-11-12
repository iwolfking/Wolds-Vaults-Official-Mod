package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;
import xyz.iwolfking.woldsvaults.milestones.MilestoneRankLadder;

import javax.annotation.Nullable;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * The tunable numbers of the greed rank ladder, generated as {@code vault_configs/greed/ranks/greed_ranks.json}:
 * every rank's reputation threshold, the god alignment level each gated rank-up needs, the reputation step past
 * Legend and the greed coins a rank-up trial pays per rank. The ladder's shape (rank count, bands, band names) is
 * structure and lives in {@link MilestoneRankLadder}.
 */
public class GreedRanksConfig extends Config implements WoldsTable {
    @Expose public List<Integer> thresholds;
    @Expose public Map<String, Integer> godLevelGates;
    @Expose public int legendPlusStep;
    @Expose public int trialCoinsPerRank;

    @Override
    public String getName() {
        return "gods" + File.separator + "greed_ranks";
    }

    @Override
    protected void reset() {
    }

    @Nullable
    @Override
    public String validate() {
        if (this.thresholds == null || this.thresholds.size() != MilestoneRankLadder.LEGEND_RANK) {
            return "it lists " + (this.thresholds == null ? 0 : this.thresholds.size()) + " rank thresholds for a "
                    + MilestoneRankLadder.LEGEND_RANK + " rank ladder";
        }
        for (int index = 1; index < this.thresholds.size(); index++) {
            if (this.thresholds.get(index) <= this.thresholds.get(index - 1)) {
                return "rank thresholds must strictly ascend, but rank " + (index + 1) + " sits at " + this.thresholds.get(index);
            }
        }
        if (this.godLevelGates == null) {
            return "godLevelGates is missing";
        }
        for (Map.Entry<String, Integer> gate : this.godLevelGates.entrySet()) {
            int rank;
            try {
                rank = Integer.parseInt(gate.getKey().trim());
            } catch (NumberFormatException e) {
                return "god level gate key '" + gate.getKey() + "' is not a rank number";
            }
            if (rank < MilestoneRankLadder.FIRST_RANK || rank > MilestoneRankLadder.LEGEND_RANK || gate.getValue() == null) {
                return "god level gate for rank " + gate.getKey() + " is outside the ladder or empty";
            }
        }
        if (this.legendPlusStep <= 0) {
            return "legendPlusStep must be positive";
        }
        if (this.trialCoinsPerRank < 0) {
            return "trialCoinsPerRank is negative";
        }
        return null;
    }
}
