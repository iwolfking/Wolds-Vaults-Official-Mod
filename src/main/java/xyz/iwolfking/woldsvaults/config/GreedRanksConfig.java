package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The greed rank ladder - {@code config/the_vault/gods/greed_ranks.json}. Holds the reputation
 * threshold of every rank, the size and names of the rank bands, and the god alignment level each
 * band jump is gated behind.
 *
 * <p>{@link xyz.iwolfking.woldsvaults.milestones.MilestoneRankLadder} is the only reader; the
 * medallion tier table, the greed screens and the trial gates all ask the ladder rather than
 * re-deriving any of this. The defaults below are the shipped ladder, so a missing file
 * regenerates to exactly the values the addon has always used.</p>
 *
 * <p>The ladder itself rejects a band size that cannot divide the ladder and a band-name list too
 * short to cover it, logging both and running on the shipped ladder, so there is nothing left for
 * {@link #isValid()} to add; {@link PackAuthoredConfig} is here for the kept-on-parse-failure
 * guarantee.</p>
 */
public class GreedRanksConfig extends PackAuthoredConfig {
    @Expose private Map<String, String> documentation;
    @Expose private int bandSize;
    @Expose private List<String> bandNames;
    @Expose private List<Integer> thresholds;
    @Expose private Map<String, Integer> godLevelGates;

    /**
     * A config instance holding the shipped defaults without touching the disk. This is what the
     * ladder runs on until {@code ModConfigs.register} hands it the file-backed copy, which
     * matters because item registration names the greed medallions from the ladder and runs
     * before configs are read.
     */
    public static GreedRanksConfig defaults() {
        GreedRanksConfig config = new GreedRanksConfig();
        config.reset();
        return config;
    }

    /**
     * A config instance holding an explicit ladder. The greed login sync puts the server's live
     * ladder on the wire in this shape so the client can run it through exactly the same validation
     * a ladder read off disk goes through, rather than trusting a packet blindly.
     */
    public static GreedRanksConfig of(int bandSize, List<String> bandNames, List<Integer> thresholds,
                                      Map<String, Integer> godLevelGates) {
        GreedRanksConfig config = defaults();
        config.bandSize = bandSize;
        config.bandNames = new ArrayList<>(bandNames);
        config.thresholds = new ArrayList<>(thresholds);
        config.godLevelGates = new LinkedHashMap<>(godLevelGates);
        return config;
    }

    @Override
    public String getName() {
        return "gods" + File.separator + "greed_ranks";
    }

    @Override
    protected void reset() {
        this.documentation = new LinkedHashMap<>();
        this.documentation.put("bandSize", "Ranks per band. A rank-up that lands on the first rank of a band is a hyper trial; every other rank-up is a vessel trial");
        this.documentation.put("bandNames", "Band names in ladder order, one per band. Medallion registry paths, rank badges and band colours all derive from these");
        this.documentation.put("thresholds", "Greed reputation needed to hold each rank, rank 1 upwards. The list must hold one entry per rank of the ladder");
        this.documentation.put("godLevelGates", "God alignment level needed with any single god before the rank-up trial for that rank may be taken, keyed by the rank being climbed to. Ranks with no gate are omitted");
        this.bandSize = 3;
        this.bandNames = new ArrayList<>(List.of("scavenger", "looter", "hunter", "master", "champion", "legend"));
        this.thresholds = new ArrayList<>(List.of(0, 75, 100, 125, 150, 175, 200, 250, 300, 400, 500, 550, 600, 800, 900, 1000));
        this.godLevelGates = new LinkedHashMap<>();
        this.godLevelGates.put("7", 2);
        this.godLevelGates.put("10", 4);
        this.godLevelGates.put("13", 6);
        this.godLevelGates.put("16", 8);
    }

    public int getBandSize() {
        return this.bandSize;
    }

    public List<String> getBandNames() {
        return this.bandNames;
    }

    public List<Integer> getThresholds() {
        return this.thresholds;
    }

    public Map<String, Integer> getGodLevelGates() {
        return this.godLevelGates;
    }
}
