package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import xyz.iwolfking.woldsvaults.config.GreedMilestonesConfig;
import xyz.iwolfking.woldsvaults.config.GreedRanksConfig;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractWoldsTableProvider;
import xyz.iwolfking.woldsvaults.milestones.MilestoneIds;
import xyz.iwolfking.woldsvaults.milestones.MilestoneRankLadder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Every greed milestone's tier thresholds and reputation, the rank tags that gate challenge crystals, and the real
 * requirements of the two pass/fail milestones. Challenge reputation is derived from the rank ladder in
 * {@link ModGreedRanksProvider#ladder()}, so retuning a rank threshold retunes the challenges tagged below it.
 */
public class ModGreedMilestonesProvider extends AbstractWoldsTableProvider {
    private static final long MINUTE = 20L * 60L;
    private static final long HOUR = MINUTE * 60L;

    public ModGreedMilestonesProvider(DataGenerator generator) {
        super(generator, "greed/milestones");
    }

    @Override
    public void registerConfigs() {
        GreedRanksConfig ladder = ModGreedRanksProvider.ladder();
        GreedMilestonesConfig table = new GreedMilestonesConfig();
        Map<String, GreedMilestonesConfig.Entry> milestones = new LinkedHashMap<>();
        table.milestones = milestones;
        tiers(milestones, MilestoneIds.LIVING_LOOTER,
                new long[]{2000L, 10_000L, 50_000L, 200_000L, 1_000_000L}, new int[]{20, 45, 70, 120, 300});
        tiers(milestones, MilestoneIds.ORNATE_LOOTER,
                new long[]{2000L, 10_000L, 50_000L, 200_000L, 1_000_000L}, new int[]{20, 45, 70, 120, 300});
        tiers(milestones, MilestoneIds.GILDED_LOOTER,
                new long[]{2000L, 10_000L, 50_000L, 200_000L, 1_000_000L}, new int[]{20, 45, 70, 120, 300});
        tiers(milestones, MilestoneIds.WOODEN_LOOTER,
                new long[]{2000L, 10_000L, 50_000L, 200_000L, 1_000_000L}, new int[]{20, 45, 70, 120, 300});
        tiers(milestones, MilestoneIds.TREASURE_HUNTER, new long[]{20L, 100L, 400L, 1000L, 3000L}, new int[]{15, 25, 50, 100, 225});
        tiers(milestones, MilestoneIds.SHOP_HUNTER, new long[]{20L, 100L, 400L, 1000L, 3000L}, new int[]{15, 25, 50, 100, 175});
        tiers(milestones, MilestoneIds.DIGGY_DIGGY_JEWEL,
                new long[]{1000L, 10_000L, 50_000L, 250_000L, 800_000L}, new int[]{10, 30, 55, 85, 160});
        tiers(milestones, MilestoneIds.AMAZON_WORKER, new long[]{200L, 1000L, 3000L, 10_000L}, new int[]{20, 30, 50, 75});
        tiers(milestones, MilestoneIds.NULLIFIED, new long[]{25L, 100L, 400L, 1500L}, new int[]{10, 20, 35, 50});
        tiers(milestones, MilestoneIds.DEDICATED_LOOTER, new long[]{1L}, new int[]{75});
        tiers(milestones, MilestoneIds.BINGO, new long[]{30L, 90L, 220L, 500L, 1000L}, new int[]{15, 30, 60, 115, 190});
        tiers(milestones, MilestoneIds.DRINK_UP,
                new long[]{20_000L, 100_000L, 250_000L, 500_000L, 1_000_000L}, new int[]{15, 40, 75, 130, 190});
        tiers(milestones, MilestoneIds.BRUTALIZED, new long[]{25L, 100L, 300L, 700L, 1500L}, new int[]{15, 25, 50, 80, 120});
        tiers(milestones, MilestoneIds.SCAVINGO, new long[]{30L, 90L, 220L, 500L, 1000L}, new int[]{15, 30, 60, 115, 190});
        tiers(milestones, MilestoneIds.CHAOS_OBJECTIVES, new long[]{30L, 90L, 220L, 500L, 1000L}, new int[]{15, 30, 60, 115, 190});
        tiers(milestones, MilestoneIds.HYPERION, new long[]{150L, 500L, 2000L, 5000L, 10_000L}, new int[]{15, 25, 50, 80, 130});
        tiers(milestones, MilestoneIds.ALCHEMIST, new long[]{700L, 3000L, 10_000L, 25_000L, 50_000L}, new int[]{15, 25, 50, 80, 130});
        tiers(milestones, MilestoneIds.VAULTS_HUNTED, new long[]{10L, 40L, 100L, 200L, 500L}, new int[]{10, 30, 65, 100, 250});
        tiers(milestones, MilestoneIds.LIGHT_THE_FLAME, new long[]{15L, 50L, 125L, 375L, 800L}, new int[]{10, 25, 40, 70, 120});
        tiers(milestones, MilestoneIds.I_WILL_SURVIVE,
                new long[]{30L * MINUTE, (5L * HOUR) / 2L, 6L * HOUR, 10L * HOUR, 16L * HOUR}, new int[]{10, 25, 40, 70, 125});
        tiers(milestones, MilestoneIds.RUNIC_RITUAL, new long[]{3L, 10L, 25L, 50L, 100L}, new int[]{10, 25, 40, 70, 125});
        tiers(milestones, MilestoneIds.ETERNAL_DARKNESS, new long[]{50L, 200L, 500L, 1000L, 2000L}, new int[]{15, 25, 50, 80, 135});
        tiers(milestones, MilestoneIds.ROYALE_PAINE, new long[]{1L, 5L, 10L, 25L, 50L}, new int[]{10, 20, 35, 50, 80});
        tiers(milestones, MilestoneIds.STAIRWAY_TO_HEAVEN, new long[]{3L, 10L, 25L, 50L, 100L}, new int[]{15, 30, 50, 100, 150});
        tiers(milestones, MilestoneIds.SEEN_IT_ALL, new long[]{15L}, new int[]{50});
        tiers(milestones, MilestoneIds.SLAYERRR, new long[]{5000L, 20_000L, 100_000L, 500_000L, 1_500_000L}, new int[]{10, 30, 75, 115, 190});
        tiers(milestones, MilestoneIds.HACK_N_SLASH,
                new long[]{50_000_000L, 1_000_000_000L, 100_000_000_000L, 1_000_000_000_000L, 10_000_000_000_000L}, new int[]{5, 15, 35, 65, 100});
        tiers(milestones, MilestoneIds.ARCHMAGE,
                new long[]{50_000_000L, 1_000_000_000L, 100_000_000_000L, 1_000_000_000_000L, 10_000_000_000_000L}, new int[]{5, 15, 35, 65, 100});
        tiers(milestones, MilestoneIds.SPELL_SPAMMER,
                new long[]{5000L, 50_000L, 175_000L, 750_000L, 2_000_000L}, new int[]{10, 15, 25, 50, 75});
        tiers(milestones, MilestoneIds.ELECTRIC_CONDUIT,
                new long[]{1000L, 5000L, 35_000L, 125_000L, 400_000L}, new int[]{10, 15, 25, 50, 100});
        tiers(milestones, MilestoneIds.MASTER_OF_CHAINS,
                new long[]{1000L, 5000L, 35_000L, 125_000L, 400_000L}, new int[]{10, 15, 25, 50, 100});
        tiers(milestones, MilestoneIds.DEFENSE, new long[]{500L, 2000L, 10_000L, 25_000L, 50_000L}, new int[]{10, 15, 25, 45, 70});
        tiers(milestones, MilestoneIds.WOOSH, new long[]{500L, 2000L, 10_000L, 25_000L, 50_000L}, new int[]{10, 15, 25, 45, 70});
        tiers(milestones, MilestoneIds.FIVE_LEAF_CLOVER, new long[]{500L, 2000L, 10_000L, 25_000L, 50_000L}, new int[]{10, 15, 25, 45, 70});
        tiers(milestones, MilestoneIds.BOOM, new long[]{10_000L, 75_000L, 225_000L, 700_000L}, new int[]{10, 25, 40, 70});
        tiers(milestones, MilestoneIds.DUNGEONEER, new long[]{5L, 15L, 50L, 125L}, new int[]{10, 20, 40, 60});
        tiers(milestones, MilestoneIds.VILLAIN, new long[]{1L, 15L, 60L, 250L}, new int[]{5, 10, 20, 30});
        tiers(milestones, MilestoneIds.FLAWLESS_VICTORY, new long[]{3L, 10L, 25L, 50L}, new int[]{15, 20, 30, 60});
        tiers(milestones, MilestoneIds.ASSASSIN_ASSASSINATOR,
                new long[]{10L, 50L, 150L, 400L, 1000L}, new int[]{20, 40, 75, 100, 125});
        tiers(milestones, MilestoneIds.FAIL_VAULTS, new long[]{5L, 15L, 40L, 100L, 200L}, new int[]{10, 20, 40, 70, 90});
        tiers(milestones, MilestoneIds.MASTER_SMITH, new long[]{1L}, new int[]{75});
        tiers(milestones, MilestoneIds.IDONAS_CHAMPION,
                new long[]{1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L}, new int[]{10, 20, 30, 40, 50, 60, 70, 80, 90, 100});
        tiers(milestones, MilestoneIds.PRIEST_OF_VELARA,
                new long[]{1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L}, new int[]{10, 20, 30, 40, 50, 60, 70, 80, 90, 100});
        tiers(milestones, MilestoneIds.TENOS_RIGHT_HAND,
                new long[]{1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L}, new int[]{10, 20, 30, 40, 50, 60, 70, 80, 90, 100});
        tiers(milestones, MilestoneIds.WENDARRS_TIMEKEEPER,
                new long[]{1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L}, new int[]{10, 20, 30, 40, 50, 60, 70, 80, 90, 100});
        tiers(milestones, MilestoneIds.EXPLORER, new long[]{3L, 15L, 40L, 100L, 200L}, new int[]{10, 25, 60, 90, 150});
        tiers(milestones, MilestoneIds.I_LIVE_HERE_NOW,
                new long[]{2L * HOUR, 10L * HOUR, 25L * HOUR, 50L * HOUR, 100L * HOUR}, new int[]{10, 40, 80, 140, 280});
        tiers(milestones, MilestoneIds.LEGENDARY, new long[]{5L, 25L, 100L, 250L}, new int[]{10, 25, 75, 150});
        tiers(milestones, MilestoneIds.CHALLENGED, new long[]{5L, 25L, 100L, 250L}, new int[]{10, 25, 75, 150});
        tiers(milestones, MilestoneIds.VAULT_OF_VAULTS, new long[]{1L}, new int[]{50});
        tiers(milestones, MilestoneIds.WANTED_CRIMINAL, new long[]{100L, 500L, 2000L, 10_000L, 50_000L}, new int[]{15, 20, 30, 50, 75});
        tiers(milestones, MilestoneIds.ARCHEOLOGIST, new long[]{2L, 5L, 25L, 100L}, new int[]{10, 15, 25, 50});
        tiers(milestones, MilestoneIds.SEND_A_PRAYER, new long[]{25L, 75L, 200L, 400L, 750L}, new int[]{10, 20, 35, 50, 75});
        tiers(milestones, MilestoneIds.BORN_AGAIN, new long[]{3L, 10L, 25L, 100L}, new int[]{5, 10, 20, 30});
        tiers(milestones, MilestoneIds.PAL_TRAINER, new long[]{1L, 5L, 10L, 25L}, new int[]{10, 20, 30, 50});
        tiers(milestones, MilestoneIds.VAULT_VETERAN, new long[]{1L, 2L, 3L, 4L, 5L}, new int[]{50, 100, 200, 300, 500});
        challenge(milestones, ladder, MilestoneIds.UNSTABLE_SHUFFLE, MilestoneRankLadder.LOOTER_1);
        challenge(milestones, ladder, MilestoneIds.PITCH_BLACK, MilestoneRankLadder.LOOTER_1);
        challenge(milestones, ladder, MilestoneIds.TRAPPED, MilestoneRankLadder.LOOTER_3);
        challenge(milestones, ladder, MilestoneIds.BALLISTIC_BLACKOUT, MilestoneRankLadder.MASTER_1);
        challenge(milestones, ladder, MilestoneIds.RUNE_MASTER, MilestoneRankLadder.MASTER_1);
        challenge(milestones, ladder, MilestoneIds.ELIXIR_OF_DOOM, MilestoneRankLadder.HUNTER_1);
        challenge(milestones, ladder, MilestoneIds.COLLECTATHON, MilestoneRankLadder.MASTER_2);
        challenge(milestones, ladder, MilestoneIds.ROYALE_KING, MilestoneRankLadder.CHAMPION_2);
        challenge(milestones, ladder, MilestoneIds.GONE_IN_60_SECONDS, MilestoneRankLadder.LOOTER_1);
        challenge(milestones, ladder, MilestoneIds.SURVIVAL_OF_THE_FITTEST, MilestoneRankLadder.MASTER_3);
        challenge(milestones, ladder, MilestoneIds.THE_PACIFIST, MilestoneRankLadder.LOOTER_2);
        challenge(milestones, ladder, MilestoneIds.GODS_CHALLENGE, MilestoneRankLadder.HUNTER_3);
        challenge(milestones, ladder, MilestoneIds.LUCKIEST_WIN, MilestoneRankLadder.LOOTER_2);
        challenge(milestones, ladder, MilestoneIds.BIG_BAD_BREW, MilestoneRankLadder.HUNTER_1);
        challenge(milestones, ladder, MilestoneIds.THE_SPEEDRUNNER, MilestoneRankLadder.LOOTER_2);
        challenge(milestones, ladder, MilestoneIds.TIME_YOU_SAY, MilestoneRankLadder.HUNTER_2);
        challenge(milestones, ladder, MilestoneIds.CHAOS_CHAOS_CHAOS, MilestoneRankLadder.CHAMPION_1);
        challenge(milestones, ladder, MilestoneIds.THE_SPEED_BLACKOUT, MilestoneRankLadder.HUNTER_2);
        challenge(milestones, ladder, MilestoneIds.FRENZY_ADVENTURES, MilestoneRankLadder.HUNTER_3);
        challenge(milestones, ladder, MilestoneIds.RAGE_CAGE, MilestoneRankLadder.CHAMPION_1);
        challenge(milestones, ladder, MilestoneIds.CELEBRATIONS, MilestoneRankLadder.CHAMPION_3);

        table.vaultOfVaults = new GreedMilestonesConfig.VaultOfVaultsEntry();
        table.vaultOfVaults.mobKills = 10_000L;
        table.vaultOfVaults.chestsPerType = 1_000L;
        table.vaultOfVaults.doorsPerType = 10L;
        table.vaultOfVaults.ores = 1_000L;

        table.dedicatedLooter = new GreedMilestonesConfig.DedicatedLooterEntry();
        table.dedicatedLooter.perChestType = 25_000L;
        table.dedicatedLooter.chestOrder = new ArrayList<>(List.of("WOODEN", "GILDED", "ORNATE", "LIVING"));
        this.table("greed_milestones", table);
    }

    private static void tiers(Map<String, GreedMilestonesConfig.Entry> milestones, String id, long[] thresholds, int[] reputation) {
        GreedMilestonesConfig.Entry entry = new GreedMilestonesConfig.Entry();
        entry.thresholds = new ArrayList<>(thresholds.length);
        for (long threshold : thresholds) {
            entry.thresholds.add(threshold);
        }
        entry.reputation = new ArrayList<>(reputation.length);
        for (int value : reputation) {
            entry.reputation.add(value);
        }
        milestones.put(id, entry);
    }

    private static void challenge(Map<String, GreedMilestonesConfig.Entry> milestones, GreedRanksConfig ladder, String id, int taggedRank) {
        tiers(milestones, id, new long[]{1L}, new int[]{ModGreedRanksProvider.challengeReputation(ladder, taggedRank)});
        milestones.get(id).requiredRank = taggedRank;
    }
}
