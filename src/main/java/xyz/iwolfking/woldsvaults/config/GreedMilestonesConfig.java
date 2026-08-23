package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.milestones.MilestoneIds;
import xyz.iwolfking.woldsvaults.milestones.MilestoneRankLadder;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Every greed milestone's numbers - {@code config/the_vault/greed_milestones.json}. Each entry
 * holds the tier thresholds its counter is measured against and the reputation each tier banks,
 * plus the greed rank tag that gates a challenge crystal.
 *
 * <p>The two composite "in one vault" milestones cannot be expressed as a single counter, so their
 * requirements sit alongside the table: Vault of Vaults needs all four of its sub-totals inside one
 * run, and Dedicated Looter needs one chest type looted to target before the next type counts. Both
 * are read through {@code MilestoneRegistry}, which is what makes the registry able to answer what
 * a milestone actually needs rather than reporting the {@code 1} of its pass/fail counter.</p>
 *
 * <p>{@code MilestoneRegistry} is the only reader. The defaults below are the shipped balance, so a
 * missing file regenerates to exactly the progression the addon has always had.</p>
 */
public class GreedMilestonesConfig extends PackAuthoredConfig {
    private static final long MINUTE = 20L * 60L;
    private static final long HOUR = MINUTE * 60L;

    public static class Entry {
        @Expose public List<Long> thresholds;
        @Expose public List<Integer> reputation;
        @Expose @Nullable public Integer requiredRank;

        /**
         * Whether this entry describes a milestone at all: at least one tier, one reputation value
         * per tier, and thresholds that strictly ascend. {@code MilestoneDefinition} would throw on
         * the first two, and a config typo should not take the milestone engine down with it.
         *
         * <p>The ordering check is here because nothing downstream enforces it and the failure is
         * silent: tier completion is counted by walking the list until a threshold is not met, so a
         * table that dips - or repeats a value - retires every tier past the dip permanently and the
         * reputation banked behind them can never be claimed. {@code MilestoneRegistry} answers a
         * refusal by falling back to that milestone's shipped table and naming it in the log.</p>
         */
        public boolean isUsable() {
            if (this.thresholds == null || this.thresholds.isEmpty()
                    || this.reputation == null || this.reputation.size() != this.thresholds.size()) {
                return false;
            }
            for (int index = 1; index < this.thresholds.size(); index++) {
                if (this.thresholds.get(index) <= this.thresholds.get(index - 1)) {
                    WoldsVaults.LOGGER.error("A greed_milestones.json entry puts tier {} at {} and tier {} at {}; thresholds must strictly ascend, so the entry is refused",
                            index, this.thresholds.get(index), index - 1, this.thresholds.get(index - 1));
                    return false;
                }
            }
            return true;
        }

        public long[] thresholds() {
            long[] values = new long[this.thresholds.size()];
            for (int index = 0; index < values.length; index++) {
                values[index] = this.thresholds.get(index);
            }
            return values;
        }

        public int[] reputation() {
            int[] values = new int[this.reputation.size()];
            for (int index = 0; index < values.length; index++) {
                values[index] = this.reputation.get(index);
            }
            return values;
        }

        public int requiredRank() {
            return this.requiredRank == null ? 0 : this.requiredRank;
        }
    }

    public static class VaultOfVaultsEntry {
        @Expose public long mobKills;
        @Expose public long chestsPerType;
        @Expose public long doorsPerType;
        @Expose public long ores;
    }

    public static class DedicatedLooterEntry {
        @Expose public long perChestType;
        @Expose public List<String> chestOrder;
    }

    @Expose private Map<String, String> documentation;
    @Expose private Map<String, Entry> milestones;
    @Expose private VaultOfVaultsEntry vaultOfVaults;
    @Expose private DedicatedLooterEntry dedicatedLooter;

    /**
     * A config instance holding the shipped defaults without touching the disk, for the window
     * between the milestone registry being first touched and {@code ModConfigs.register} handing
     * it the file-backed copy.
     */
    public static GreedMilestonesConfig defaults() {
        GreedMilestonesConfig config = new GreedMilestonesConfig();
        config.reset();
        return config;
    }

    @Override
    public String getName() {
        return "greed_milestones";
    }

    @Nullable
    public Entry getMilestone(String id) {
        return this.milestones == null ? null : this.milestones.get(id);
    }

    @Nullable
    public VaultOfVaultsEntry getVaultOfVaults() {
        return this.vaultOfVaults;
    }

    @Nullable
    public DedicatedLooterEntry getDedicatedLooter() {
        return this.dedicatedLooter;
    }

    @Override
    protected void reset() {
        this.documentation = new LinkedHashMap<>();
        this.documentation.put("milestones", "One entry per milestone id: thresholds are the counter values each tier is reached at, reputation is what each tier banks for collection at Mr. Greedy");
        this.documentation.put("requiredRank", "Greed rank a challenge crystal's milestone is tagged with; it gates the crystal in the shop. Omitted for every milestone that is not a challenge");
        this.documentation.put("challenge reputation", "Shipped challenge reputation is 15% of the greed_ranks threshold of the rank above the tag, floored; edit it here to break that link");
        this.documentation.put("vaultOfVaults", "All four totals must be met inside a single vault, on top of completing its objective. Deliberately one run, not a lifetime tally");
        this.documentation.put("dedicatedLooter", "Chest types must be looted to perChestType in chestOrder, one type at a time, inside a single vault. Names are VaultChestType values");
        this.documentation.put("seen_it_all", "Its final threshold must equal the number of counted objective types in code; a mismatch is logged at load and makes the milestone uncompletable");
        this.milestones = new LinkedHashMap<>();
        tiers(MilestoneIds.LIVING_LOOTER,
                new long[]{2000L, 10_000L, 50_000L, 200_000L, 1_000_000L}, new int[]{20, 45, 70, 120, 300});
        tiers(MilestoneIds.ORNATE_LOOTER,
                new long[]{2000L, 10_000L, 50_000L, 200_000L, 1_000_000L}, new int[]{20, 45, 70, 120, 300});
        tiers(MilestoneIds.GILDED_LOOTER,
                new long[]{2000L, 10_000L, 50_000L, 200_000L, 1_000_000L}, new int[]{20, 45, 70, 120, 300});
        tiers(MilestoneIds.WOODEN_LOOTER,
                new long[]{2000L, 10_000L, 50_000L, 200_000L, 1_000_000L}, new int[]{20, 45, 70, 120, 300});
        tiers(MilestoneIds.TREASURE_HUNTER, new long[]{20L, 100L, 400L, 1000L, 3000L}, new int[]{15, 25, 50, 100, 225});
        tiers(MilestoneIds.SHOP_HUNTER, new long[]{20L, 100L, 400L, 1000L, 3000L}, new int[]{15, 25, 50, 100, 175});
        tiers(MilestoneIds.DIGGY_DIGGY_JEWEL,
                new long[]{1000L, 10_000L, 50_000L, 250_000L, 800_000L}, new int[]{10, 30, 55, 85, 160});
        tiers(MilestoneIds.AMAZON_WORKER, new long[]{200L, 1000L, 3000L, 10_000L}, new int[]{20, 30, 50, 75});
        tiers(MilestoneIds.NULLIFIED, new long[]{25L, 100L, 400L, 1500L}, new int[]{10, 20, 35, 50});
        tiers(MilestoneIds.DEDICATED_LOOTER, new long[]{1L}, new int[]{75});
        tiers(MilestoneIds.BINGO, new long[]{30L, 90L, 220L, 500L, 1000L}, new int[]{15, 30, 60, 115, 190});
        tiers(MilestoneIds.DRINK_UP,
                new long[]{20_000L, 100_000L, 250_000L, 500_000L, 1_000_000L}, new int[]{15, 40, 75, 130, 190});
        tiers(MilestoneIds.BRUTALIZED, new long[]{25L, 100L, 300L, 700L, 1500L}, new int[]{15, 25, 50, 80, 120});
        tiers(MilestoneIds.SCAVINGO, new long[]{30L, 90L, 220L, 500L, 1000L}, new int[]{15, 30, 60, 115, 190});
        tiers(MilestoneIds.CHAOS_OBJECTIVES, new long[]{30L, 90L, 220L, 500L, 1000L}, new int[]{15, 30, 60, 115, 190});
        tiers(MilestoneIds.HYPERION, new long[]{150L, 500L, 2000L, 5000L, 10_000L}, new int[]{15, 25, 50, 80, 130});
        tiers(MilestoneIds.ALCHEMIST, new long[]{700L, 3000L, 10_000L, 25_000L, 50_000L}, new int[]{15, 25, 50, 80, 130});
        tiers(MilestoneIds.VAULTS_HUNTED, new long[]{10L, 40L, 100L, 200L, 500L}, new int[]{10, 30, 65, 100, 250});
        tiers(MilestoneIds.LIGHT_THE_FLAME, new long[]{15L, 50L, 125L, 375L, 800L}, new int[]{10, 25, 40, 70, 120});
        tiers(MilestoneIds.I_WILL_SURVIVE,
                new long[]{30L * MINUTE, (5L * HOUR) / 2L, 6L * HOUR, 10L * HOUR, 16L * HOUR}, new int[]{10, 25, 40, 70, 125});
        tiers(MilestoneIds.RUNIC_RITUAL, new long[]{3L, 10L, 25L, 50L, 100L}, new int[]{10, 25, 40, 70, 125});
        tiers(MilestoneIds.ETERNAL_DARKNESS, new long[]{50L, 200L, 500L, 1000L, 2000L}, new int[]{15, 25, 50, 80, 135});
        tiers(MilestoneIds.ROYALE_PAINE, new long[]{1L, 5L, 10L, 25L, 50L}, new int[]{10, 20, 35, 50, 80});
        tiers(MilestoneIds.STAIRWAY_TO_HEAVEN, new long[]{3L, 10L, 25L, 50L, 100L}, new int[]{15, 30, 50, 100, 150});
        tiers(MilestoneIds.SEEN_IT_ALL, new long[]{15L}, new int[]{50});
        tiers(MilestoneIds.SLAYERRR, new long[]{5000L, 20_000L, 100_000L, 500_000L, 1_500_000L}, new int[]{10, 30, 75, 115, 190});
        tiers(MilestoneIds.HACK_N_SLASH,
                new long[]{50_000_000L, 1_000_000_000L, 100_000_000_000L, 1_000_000_000_000L, 10_000_000_000_000L}, new int[]{5, 15, 35, 65, 100});
        tiers(MilestoneIds.ARCHMAGE,
                new long[]{50_000_000L, 1_000_000_000L, 100_000_000_000L, 1_000_000_000_000L, 10_000_000_000_000L}, new int[]{5, 15, 35, 65, 100});
        tiers(MilestoneIds.SPELL_SPAMMER,
                new long[]{5000L, 50_000L, 175_000L, 750_000L, 2_000_000L}, new int[]{10, 15, 25, 50, 75});
        tiers(MilestoneIds.ELECTRIC_CONDUIT,
                new long[]{1000L, 5000L, 35_000L, 125_000L, 400_000L}, new int[]{10, 15, 25, 50, 100});
        tiers(MilestoneIds.MASTER_OF_CHAINS,
                new long[]{1000L, 5000L, 35_000L, 125_000L, 400_000L}, new int[]{10, 15, 25, 50, 100});
        tiers(MilestoneIds.DEFENSE, new long[]{500L, 2000L, 10_000L, 25_000L, 50_000L}, new int[]{10, 15, 25, 45, 70});
        tiers(MilestoneIds.WOOSH, new long[]{500L, 2000L, 10_000L, 25_000L, 50_000L}, new int[]{10, 15, 25, 45, 70});
        tiers(MilestoneIds.FIVE_LEAF_CLOVER, new long[]{500L, 2000L, 10_000L, 25_000L, 50_000L}, new int[]{10, 15, 25, 45, 70});
        tiers(MilestoneIds.BOOM, new long[]{10_000L, 75_000L, 225_000L, 700_000L}, new int[]{10, 25, 40, 70});
        tiers(MilestoneIds.DUNGEONEER, new long[]{5L, 15L, 50L, 125L}, new int[]{10, 20, 40, 60});
        tiers(MilestoneIds.VILLAIN, new long[]{1L, 15L, 60L, 250L}, new int[]{5, 10, 20, 30});
        tiers(MilestoneIds.FLAWLESS_VICTORY, new long[]{3L, 10L, 25L, 50L}, new int[]{15, 20, 30, 60});
        tiers(MilestoneIds.ASSASSIN_ASSASSINATOR,
                new long[]{10L, 50L, 150L, 400L, 1000L}, new int[]{20, 40, 75, 100, 125});
        tiers(MilestoneIds.FAIL_VAULTS, new long[]{5L, 15L, 40L, 100L, 200L}, new int[]{10, 20, 40, 70, 90});
        tiers(MilestoneIds.MASTER_SMITH, new long[]{1L}, new int[]{75});
        tiers(MilestoneIds.IDONAS_CHAMPION,
                new long[]{1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L}, new int[]{10, 20, 30, 40, 50, 60, 70, 80, 90, 100});
        tiers(MilestoneIds.PRIEST_OF_VELARA,
                new long[]{1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L}, new int[]{10, 20, 30, 40, 50, 60, 70, 80, 90, 100});
        tiers(MilestoneIds.TENOS_RIGHT_HAND,
                new long[]{1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L}, new int[]{10, 20, 30, 40, 50, 60, 70, 80, 90, 100});
        tiers(MilestoneIds.WENDARRS_TIMEKEEPER,
                new long[]{1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L}, new int[]{10, 20, 30, 40, 50, 60, 70, 80, 90, 100});
        tiers(MilestoneIds.EXPLORER, new long[]{3L, 15L, 40L, 100L, 200L}, new int[]{10, 25, 60, 90, 150});
        tiers(MilestoneIds.I_LIVE_HERE_NOW,
                new long[]{2L * HOUR, 10L * HOUR, 25L * HOUR, 50L * HOUR, 100L * HOUR}, new int[]{10, 40, 80, 140, 280});
        tiers(MilestoneIds.LEGENDARY, new long[]{5L, 25L, 100L, 250L}, new int[]{10, 25, 75, 150});
        tiers(MilestoneIds.CHALLENGED, new long[]{5L, 25L, 100L, 250L}, new int[]{10, 25, 75, 150});
        tiers(MilestoneIds.VAULT_OF_VAULTS, new long[]{1L}, new int[]{50});
        tiers(MilestoneIds.WANTED_CRIMINAL, new long[]{100L, 500L, 2000L, 10_000L, 50_000L}, new int[]{15, 20, 30, 50, 75});
        tiers(MilestoneIds.ARCHEOLOGIST, new long[]{2L, 5L, 25L, 100L}, new int[]{10, 15, 25, 50});
        tiers(MilestoneIds.SEND_A_PRAYER, new long[]{25L, 75L, 200L, 400L, 750L}, new int[]{10, 20, 35, 50, 75});
        tiers(MilestoneIds.BORN_AGAIN, new long[]{3L, 10L, 25L, 100L}, new int[]{5, 10, 20, 30});
        tiers(MilestoneIds.PAL_TRAINER, new long[]{1L, 5L, 10L, 25L}, new int[]{10, 20, 30, 50});
        tiers(MilestoneIds.VAULT_VETERAN, new long[]{1L, 2L, 3L, 4L, 5L}, new int[]{50, 100, 200, 300, 500});
        challenge(MilestoneIds.UNSTABLE_SHUFFLE, MilestoneRankLadder.LOOTER_1);
        challenge(MilestoneIds.PITCH_BLACK, MilestoneRankLadder.LOOTER_1);
        challenge(MilestoneIds.TRAPPED, MilestoneRankLadder.LOOTER_3);
        challenge(MilestoneIds.BALLISTIC_BLACKOUT, MilestoneRankLadder.MASTER_1);
        challenge(MilestoneIds.RUNE_MASTER, MilestoneRankLadder.MASTER_1);
        challenge(MilestoneIds.ELIXIR_OF_DOOM, MilestoneRankLadder.HUNTER_1);
        challenge(MilestoneIds.COLLECTATHON, MilestoneRankLadder.MASTER_2);
        challenge(MilestoneIds.ROYALE_KING, MilestoneRankLadder.CHAMPION_2);
        challenge(MilestoneIds.GONE_IN_60_SECONDS, MilestoneRankLadder.LOOTER_1);
        challenge(MilestoneIds.SURVIVAL_OF_THE_FITTEST, MilestoneRankLadder.MASTER_3);
        challenge(MilestoneIds.THE_PACIFIST, MilestoneRankLadder.LOOTER_2);
        challenge(MilestoneIds.GODS_CHALLENGE, MilestoneRankLadder.HUNTER_3);
        challenge(MilestoneIds.LUCKIEST_WIN, MilestoneRankLadder.LOOTER_2);
        challenge(MilestoneIds.BIG_BAD_BREW, MilestoneRankLadder.HUNTER_1);
        challenge(MilestoneIds.THE_SPEEDRUNNER, MilestoneRankLadder.LOOTER_2);
        challenge(MilestoneIds.TIME_YOU_SAY, MilestoneRankLadder.HUNTER_2);
        challenge(MilestoneIds.CHAOS_CHAOS_CHAOS, MilestoneRankLadder.CHAMPION_1);
        challenge(MilestoneIds.THE_SPEED_BLACKOUT, MilestoneRankLadder.HUNTER_2);
        challenge(MilestoneIds.FRENZY_ADVENTURES, MilestoneRankLadder.HUNTER_3);
        challenge(MilestoneIds.RAGE_CAGE, MilestoneRankLadder.CHAMPION_1);
        challenge(MilestoneIds.CELEBRATIONS, MilestoneRankLadder.CHAMPION_3);

        this.vaultOfVaults = new VaultOfVaultsEntry();
        this.vaultOfVaults.mobKills = 10_000L;
        this.vaultOfVaults.chestsPerType = 1_000L;
        this.vaultOfVaults.doorsPerType = 10L;
        this.vaultOfVaults.ores = 1_000L;

        this.dedicatedLooter = new DedicatedLooterEntry();
        this.dedicatedLooter.perChestType = 25_000L;
        this.dedicatedLooter.chestOrder = new ArrayList<>(List.of("WOODEN", "GILDED", "ORNATE", "LIVING"));
    }

    private void tiers(String id, long[] thresholds, int[] reputation) {
        Entry entry = new Entry();
        entry.thresholds = new ArrayList<>(thresholds.length);
        for (long threshold : thresholds) {
            entry.thresholds.add(threshold);
        }
        entry.reputation = new ArrayList<>(reputation.length);
        for (int value : reputation) {
            entry.reputation.add(value);
        }
        this.milestones.put(id, entry);
    }

    private void challenge(String id, int taggedRank) {
        this.tiers(id, new long[]{1L}, new int[]{MilestoneRankLadder.getChallengeReputation(taggedRank)});
        this.milestones.get(id).requiredRank = taggedRank;
    }
}
