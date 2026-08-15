package xyz.iwolfking.woldsvaults.milestones;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Code-defined source of every milestone definition. The registry is populated once, eagerly,
 * and is immutable afterwards; a config or datagen layer only has to replace
 * {@link #register(MilestoneDefinition)} calls in {@link #bootstrap()} to take over as the source.
 */
public class MilestoneRegistry {
    private static final Map<String, MilestoneDefinition> BY_ID = new LinkedHashMap<>();
    private static final Map<MilestoneCategory, List<MilestoneDefinition>> BY_CATEGORY = new LinkedHashMap<>();
    private static List<MilestoneDefinition> ORDERED = List.of();

    private static final long MINUTE = 20L * 60L;
    private static final long HOUR = MINUTE * 60L;

    /**
     * Vault objective keys that "Seen It All" requires. The key is the crystal objective type
     * written into {@code Objectives.KEY}; anything outside this set is ignored so that new or
     * internal objective types cannot inflate the counter.
     */
    public static final Set<String> SEEN_IT_ALL_TYPES = Collections.unmodifiableSet(new LinkedHashSet<>(List.of(
            "cake", "scavenger", "monolith", "elixir", "herald", "ascension", "bingo", "rune_boss",
            "raid", "vault_royale", "scavenger_bingo", "chaos", "boss",
            "brutal_bosses", "corrupted", "alchemy", "survival", "haunted_braziers"
    )));

    private MilestoneRegistry() {
    }

    static {
        bootstrap();
    }

    public static MilestoneDefinition get(String id) {
        return BY_ID.get(id);
    }

    public static boolean contains(String id) {
        return BY_ID.containsKey(id);
    }

    public static List<MilestoneDefinition> getAll() {
        return ORDERED;
    }

    public static List<MilestoneDefinition> getByCategory(MilestoneCategory category) {
        return BY_CATEGORY.getOrDefault(category, List.of());
    }

    private static MilestoneDefinition register(MilestoneDefinition definition) {
        if (BY_ID.put(definition.getId(), definition) != null) {
            throw new IllegalStateException("Duplicate milestone id " + definition.getId());
        }
        BY_CATEGORY.computeIfAbsent(definition.getCategory(), c -> new ArrayList<>()).add(definition);
        return definition;
    }

    private static void count(String id, MilestoneCategory category, long[] thresholds, int[] reputation) {
        register(new MilestoneDefinition(id, category, MilestoneCounter.ACCUMULATE, thresholds, reputation));
    }

    private static void highest(String id, MilestoneCategory category, long[] thresholds, int[] reputation) {
        register(new MilestoneDefinition(id, category, MilestoneCounter.HIGHEST, thresholds, reputation));
    }

    private static void oneShot(String id, MilestoneCategory category, int reputation) {
        register(new MilestoneDefinition(id, category, MilestoneCounter.ACCUMULATE, new long[]{1L}, new int[]{reputation}));
    }

    private static void challenge(String id, int taggedRank) {
        oneShot(id, MilestoneCategory.CHALLENGE, MilestoneRankLadder.getChallengeReputation(taggedRank));
    }

    private static void bootstrap() {
        long[] chestTiers = {2_000L, 10_000L, 50_000L, 200_000L, 1_000_000L};
        int[] chestRep = {20, 45, 70, 120, 300};
        count(MilestoneIds.LIVING_LOOTER, MilestoneCategory.LOOTING, chestTiers, chestRep);
        count(MilestoneIds.ORNATE_LOOTER, MilestoneCategory.LOOTING, chestTiers, chestRep);
        count(MilestoneIds.GILDED_LOOTER, MilestoneCategory.LOOTING, chestTiers, chestRep);
        count(MilestoneIds.WOODEN_LOOTER, MilestoneCategory.LOOTING, chestTiers, chestRep);
        count(MilestoneIds.TREASURE_HUNTER, MilestoneCategory.LOOTING,
                new long[]{20L, 100L, 400L, 1_000L, 3_000L}, new int[]{15, 25, 50, 100, 225});
        count(MilestoneIds.SHOP_HUNTER, MilestoneCategory.LOOTING,
                new long[]{20L, 100L, 400L, 1_000L, 3_000L}, new int[]{15, 25, 50, 100, 175});
        count(MilestoneIds.DIGGY_DIGGY_JEWEL, MilestoneCategory.LOOTING,
                new long[]{1_000L, 10_000L, 50_000L, 250_000L, 800_000L}, new int[]{10, 30, 55, 85, 160});
        count(MilestoneIds.AMAZON_WORKER, MilestoneCategory.LOOTING,
                new long[]{200L, 1_000L, 3_000L, 10_000L}, new int[]{20, 30, 50, 75});
        count(MilestoneIds.NULLIFIED, MilestoneCategory.LOOTING,
                new long[]{25L, 100L, 400L, 1_500L}, new int[]{10, 20, 35, 50});
        oneShot(MilestoneIds.DEDICATED_LOOTER, MilestoneCategory.LOOTING, 75);

        count(MilestoneIds.BINGO, MilestoneCategory.THEME,
                new long[]{30L, 90L, 220L, 500L, 1_000L}, new int[]{15, 30, 60, 115, 190});
        count(MilestoneIds.DRINK_UP, MilestoneCategory.THEME,
                new long[]{20_000L, 100_000L, 250_000L, 500_000L, 1_000_000L}, new int[]{15, 40, 75, 130, 190});
        count(MilestoneIds.BRUTALIZED, MilestoneCategory.THEME,
                new long[]{25L, 100L, 300L, 700L, 1_500L}, new int[]{15, 25, 50, 80, 120});
        count(MilestoneIds.SCAVINGO, MilestoneCategory.THEME,
                new long[]{30L, 90L, 220L, 500L, 1_000L}, new int[]{15, 30, 60, 115, 190});
        count(MilestoneIds.CHAOS_OBJECTIVES, MilestoneCategory.THEME,
                new long[]{30L, 90L, 220L, 500L, 1_000L}, new int[]{15, 30, 60, 115, 190});
        count(MilestoneIds.HYPERION, MilestoneCategory.THEME,
                new long[]{150L, 500L, 2_000L, 5_000L, 10_000L}, new int[]{15, 25, 50, 80, 130});
        count(MilestoneIds.ALCHEMIST, MilestoneCategory.THEME,
                new long[]{700L, 3_000L, 10_000L, 25_000L, 50_000L}, new int[]{15, 25, 50, 80, 130});
        count(MilestoneIds.VAULTS_HUNTED, MilestoneCategory.THEME,
                new long[]{10L, 40L, 100L, 200L, 500L}, new int[]{10, 30, 65, 100, 250});
        count(MilestoneIds.LIGHT_THE_FLAME, MilestoneCategory.THEME,
                new long[]{15L, 50L, 125L, 375L, 800L}, new int[]{10, 25, 40, 70, 120});
        count(MilestoneIds.I_WILL_SURVIVE, MilestoneCategory.THEME,
                new long[]{30L * MINUTE, (5L * HOUR) / 2L, 6L * HOUR, 10L * HOUR, 16L * HOUR},
                new int[]{10, 25, 40, 70, 125});
        count(MilestoneIds.RUNIC_RITUAL, MilestoneCategory.THEME,
                new long[]{3L, 10L, 25L, 50L, 100L}, new int[]{10, 25, 40, 70, 125});
        count(MilestoneIds.ETERNAL_DARKNESS, MilestoneCategory.THEME,
                new long[]{50L, 200L, 500L, 1_000L, 2_000L}, new int[]{15, 25, 50, 80, 135});
        count(MilestoneIds.ROYALE_PAINE, MilestoneCategory.THEME,
                new long[]{1L, 5L, 10L, 25L, 50L}, new int[]{10, 20, 35, 50, 80});
        count(MilestoneIds.STAIRWAY_TO_HEAVEN, MilestoneCategory.THEME,
                new long[]{3L, 10L, 25L, 50L, 100L}, new int[]{15, 30, 50, 100, 150});
        register(new MilestoneDefinition(MilestoneIds.SEEN_IT_ALL, MilestoneCategory.THEME, MilestoneCounter.DISTINCT,
                new long[]{SEEN_IT_ALL_TYPES.size()}, new int[]{50}));

        count(MilestoneIds.SLAYERRR, MilestoneCategory.COMBAT,
                new long[]{5_000L, 20_000L, 100_000L, 500_000L, 1_500_000L}, new int[]{10, 30, 75, 115, 190});
        long[] damageTiers = {50_000_000L, 1_000_000_000L, 100_000_000_000L, 1_000_000_000_000L, 10_000_000_000_000L};
        int[] damageRep = {5, 15, 35, 65, 100};
        count(MilestoneIds.HACK_N_SLASH, MilestoneCategory.COMBAT, damageTiers, damageRep);
        count(MilestoneIds.ARCHMAGE, MilestoneCategory.COMBAT, damageTiers, damageRep);
        count(MilestoneIds.SPELL_SPAMMER, MilestoneCategory.COMBAT,
                new long[]{5_000L, 50_000L, 175_000L, 750_000L, 2_000_000L}, new int[]{10, 15, 25, 50, 75});
        count(MilestoneIds.ELECTRIC_CONDUIT, MilestoneCategory.COMBAT,
                new long[]{1_000L, 5_000L, 35_000L, 125_000L, 400_000L}, new int[]{10, 15, 25, 50, 100});
        count(MilestoneIds.MASTER_OF_CHAINS, MilestoneCategory.COMBAT,
                new long[]{1_000L, 5_000L, 35_000L, 125_000L, 400_000L}, new int[]{10, 15, 25, 50, 100});
        long[] defensiveTiers = {500L, 2_000L, 10_000L, 25_000L, 50_000L};
        count(MilestoneIds.DEFENSE, MilestoneCategory.COMBAT, defensiveTiers, new int[]{10, 15, 25, 45, 70});
        count(MilestoneIds.WOOSH, MilestoneCategory.COMBAT, defensiveTiers, new int[]{10, 15, 25, 45, 70});
        count(MilestoneIds.FIVE_LEAF_CLOVER, MilestoneCategory.COMBAT, defensiveTiers, new int[]{10, 15, 25, 45, 70});
        count(MilestoneIds.BOOM, MilestoneCategory.COMBAT,
                new long[]{10_000L, 75_000L, 225_000L, 700_000L}, new int[]{10, 25, 40, 70});
        count(MilestoneIds.DUNGEONEER, MilestoneCategory.COMBAT,
                new long[]{5L, 15L, 50L, 125L}, new int[]{10, 20, 40, 60});
        count(MilestoneIds.VILLAIN, MilestoneCategory.COMBAT,
                new long[]{1L, 15L, 60L, 250L}, new int[]{5, 10, 20, 30});
        count(MilestoneIds.FLAWLESS_VICTORY, MilestoneCategory.COMBAT,
                new long[]{3L, 10L, 25L, 50L}, new int[]{15, 20, 30, 60});

        count(MilestoneIds.FAIL_VAULTS, MilestoneCategory.MISC,
                new long[]{5L, 15L, 40L, 100L, 200L}, new int[]{10, 20, 40, 70, 90});
        oneShot(MilestoneIds.MASTER_SMITH, MilestoneCategory.MISC, 75);
        long[] godTiers = {1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L};
        int[] godRep = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
        highest(MilestoneIds.IDONAS_CHAMPION, MilestoneCategory.MISC, godTiers, godRep);
        highest(MilestoneIds.PRIEST_OF_VELARA, MilestoneCategory.MISC, godTiers, godRep);
        highest(MilestoneIds.TENOS_RIGHT_HAND, MilestoneCategory.MISC, godTiers, godRep);
        highest(MilestoneIds.WENDARRS_TIMEKEEPER, MilestoneCategory.MISC, godTiers, godRep);
        count(MilestoneIds.EXPLORER, MilestoneCategory.MISC,
                new long[]{3L, 15L, 40L, 100L, 200L}, new int[]{10, 25, 60, 90, 150});
        count(MilestoneIds.I_LIVE_HERE_NOW, MilestoneCategory.MISC,
                new long[]{2L * HOUR, 10L * HOUR, 25L * HOUR, 50L * HOUR, 100L * HOUR},
                new int[]{10, 40, 80, 140, 280});
        count(MilestoneIds.LEGENDARY, MilestoneCategory.MISC,
                new long[]{5L, 25L, 100L, 250L}, new int[]{10, 25, 75, 150});
        count(MilestoneIds.CHALLENGED, MilestoneCategory.MISC,
                new long[]{5L, 25L, 100L, 250L}, new int[]{10, 25, 75, 150});
        oneShot(MilestoneIds.VAULT_OF_VAULTS, MilestoneCategory.MISC, 50);
        count(MilestoneIds.WANTED_CRIMINAL, MilestoneCategory.MISC,
                new long[]{100L, 500L, 2_000L, 10_000L, 50_000L}, new int[]{15, 20, 30, 50, 75});
        count(MilestoneIds.ARCHEOLOGIST, MilestoneCategory.MISC,
                new long[]{2L, 5L, 25L, 100L}, new int[]{10, 15, 25, 50});
        count(MilestoneIds.SEND_A_PRAYER, MilestoneCategory.MISC,
                new long[]{25L, 75L, 200L, 400L, 750L}, new int[]{10, 20, 35, 50, 75});
        count(MilestoneIds.BORN_AGAIN, MilestoneCategory.MISC,
                new long[]{3L, 10L, 25L, 100L}, new int[]{5, 10, 20, 30});
        count(MilestoneIds.PAL_TRAINER, MilestoneCategory.MISC,
                new long[]{1L, 5L, 10L, 25L}, new int[]{10, 20, 30, 50});
        highest(MilestoneIds.VAULT_VETERAN, MilestoneCategory.MISC,
                new long[]{1L, 2L, 3L, 4L, 5L}, new int[]{0, 0, 0, 0, 0});

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

        ORDERED = List.copyOf(BY_ID.values());
        BY_CATEGORY.replaceAll((category, list) -> List.copyOf(list));
    }
}
