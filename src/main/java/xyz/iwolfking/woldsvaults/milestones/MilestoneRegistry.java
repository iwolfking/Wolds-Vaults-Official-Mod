package xyz.iwolfking.woldsvaults.milestones;

import iskallia.vault.core.vault.stat.VaultChestType;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.GreedMilestonesConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * The one place a milestone's contract lives: which milestones exist, what each one counts, what
 * every tier of it costs and what it pays. The structure - id, category, counter, the challenge
 * crystal a milestone tracks - is code; every number behind it is pack data in
 * {@code config/the_vault/greed_milestones.json}, installed by {@code ModConfigs.register}.
 *
 * <p>That includes the two composite "in one vault" milestones, whose real requirements are not
 * expressible as the single pass/fail tier their counter carries. {@link MilestoneVaultState}
 * judges them, but it asks the registry what they need rather than tabling its own copy.</p>
 */
public class MilestoneRegistry {
    private static final Map<String, MilestoneDefinition> BY_ID = new LinkedHashMap<>();
    private static final Map<MilestoneCategory, List<MilestoneDefinition>> BY_CATEGORY = new LinkedHashMap<>();
    private static final Map<String, MilestoneDefinition> BY_CHALLENGE_CRYSTAL = new LinkedHashMap<>();
    private static List<MilestoneDefinition> ORDERED = List.of();

    private static final List<VaultChestType> DEFAULT_DEDICATED_ORDER =
            List.of(VaultChestType.WOODEN, VaultChestType.GILDED, VaultChestType.ORNATE, VaultChestType.LIVING);

    private static GreedMilestonesConfig config;
    private static List<VaultChestType> dedicatedOrder = DEFAULT_DEDICATED_ORDER;

    /**
     * Vault objective keys that "Seen It All" requires: exactly the objectives that award god
     * experience. The key is the crystal objective type written into {@code Objectives.KEY};
     * anything outside this set is ignored so that new or internal objective types cannot inflate
     * the counter.
     */
    public static final Set<String> SEEN_IT_ALL_TYPES = Collections.unmodifiableSet(new LinkedHashSet<>(List.of(
            "elixir", "scavenger", "unhinged_scavenger", "brutal_bosses", "rune_boss",
            "enchanted_elixir", "hyper", "alchemy", "bingo", "scavenger_bingo",
            "unhinged_scavenger_bingo", "chaos", "corrupted", "ballistic_bingo", "zealot"
    )));

    private static final Map<String, String> SEEN_IT_ALL_ALIASES = Map.of(
            "scaling_scavenger_bingo", "scavenger_bingo",
            "scaling_unhinged_scavenger_bingo", "unhinged_scavenger_bingo",
            "scaling_ballistic_bingo", "ballistic_bingo");

    /**
     * Resolves a live {@code Objectives.KEY} to the "Seen It All" token it counts for, or null
     * when that objective is not one of the counted ones. Three of the counted objectives are held
     * in {@code CrystalData.OBJECTIVE} under two ids for the same class - the addon registers a
     * {@code scaling_} id from its mod constructor and the vhapi objective registry re-registers
     * the same class under the plain id during the registry event - so the alias table keeps the
     * token stable regardless of which id the type lookup ends up returning.
     */
    public static String seenItAllToken(String objectiveKey) {
        if (objectiveKey == null) {
            return null;
        }
        String canonical = SEEN_IT_ALL_ALIASES.getOrDefault(objectiveKey, objectiveKey);
        return SEEN_IT_ALL_TYPES.contains(canonical) ? canonical : null;
    }

    private MilestoneRegistry() {
    }

    static {
        load(GreedMilestonesConfig.defaults());
    }

    /**
     * Rebuilds every definition from a config read off disk. Called once from
     * {@code ModConfigs.register} and again on every config reload; the registry starts on the
     * shipped defaults so that anything touching it before configs are read - item registration,
     * datagen - still sees the real progression.
     */
    public static void load(GreedMilestonesConfig loaded) {
        config = loaded;
        dedicatedOrder = readDedicatedOrder(loaded);
        BY_ID.clear();
        BY_CATEGORY.clear();
        BY_CHALLENGE_CRYSTAL.clear();
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

    /**
     * The milestone that tracks a greed challenge crystal, or null when that crystal has none.
     * {@code ultra_hard} is the one offered crystal without a milestone.
     */
    public static MilestoneDefinition getByChallengeCrystal(String challengeCrystalId) {
        return challengeCrystalId == null ? null : BY_CHALLENGE_CRYSTAL.get(challengeCrystalId);
    }

    /**
     * Greed rank a challenge crystal is gated behind, from the rank tag on its milestone. Crystals
     * with no milestone return 0, which leaves them on their config {@code minTier} gate.
     */
    public static int getChallengeRequiredRank(String challengeCrystalId) {
        MilestoneDefinition definition = getByChallengeCrystal(challengeCrystalId);
        return definition == null ? 0 : definition.getRequiredRank();
    }

    /**
     * Mob kills "Vault of Vaults" needs inside a single vault.
     */
    public static long getVaultOfVaultsMobs() {
        return vaultOfVaults().mobKills;
    }

    /**
     * Chests of each of the four normal types "Vault of Vaults" needs inside a single vault.
     */
    public static long getVaultOfVaultsChestsPerType() {
        return vaultOfVaults().chestsPerType;
    }

    /**
     * Doors of each type - treasure, vendoor, dungeon - "Vault of Vaults" needs inside a single
     * vault.
     */
    public static long getVaultOfVaultsDoorsPerType() {
        return vaultOfVaults().doorsPerType;
    }

    /**
     * Ores "Vault of Vaults" needs inside a single vault.
     */
    public static long getVaultOfVaultsOres() {
        return vaultOfVaults().ores;
    }

    /**
     * Chests of one type "Dedicated Looter" needs before the next type in its order starts
     * counting.
     */
    public static long getDedicatedLooterTarget() {
        GreedMilestonesConfig.DedicatedLooterEntry entry = config.getDedicatedLooter();
        if (entry != null && entry.perChestType > 0L) {
            return entry.perChestType;
        }
        WoldsVaults.LOGGER.error("greed_milestones.json has no usable dedicatedLooter.perChestType; falling back to the shipped target");
        return GreedMilestonesConfig.defaults().getDedicatedLooter().perChestType;
    }

    /**
     * The chest types "Dedicated Looter" must be worked through, in order. Its size is the number
     * of phases the milestone runs for.
     */
    public static List<VaultChestType> getDedicatedLooterOrder() {
        return dedicatedOrder;
    }

    private static GreedMilestonesConfig.VaultOfVaultsEntry vaultOfVaults() {
        GreedMilestonesConfig.VaultOfVaultsEntry entry = config.getVaultOfVaults();
        if (entry != null) {
            return entry;
        }
        WoldsVaults.LOGGER.error("greed_milestones.json has no vaultOfVaults block; falling back to the shipped requirements");
        return GreedMilestonesConfig.defaults().getVaultOfVaults();
    }

    /**
     * Resolves the configured chest order once at load. An unknown or empty order is reported and
     * answered with the shipped order, because a half-parsed order would leave the milestone
     * permanently stuck on the phase whose chest type went missing.
     */
    private static List<VaultChestType> readDedicatedOrder(GreedMilestonesConfig loaded) {
        GreedMilestonesConfig.DedicatedLooterEntry entry = loaded.getDedicatedLooter();
        if (entry == null || entry.chestOrder == null || entry.chestOrder.isEmpty()) {
            WoldsVaults.LOGGER.error("greed_milestones.json has no dedicatedLooter.chestOrder; falling back to the shipped order {}", DEFAULT_DEDICATED_ORDER);
            return DEFAULT_DEDICATED_ORDER;
        }
        List<VaultChestType> order = new ArrayList<>(entry.chestOrder.size());
        for (String name : entry.chestOrder) {
            try {
                order.add(VaultChestType.valueOf(name.trim().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException e) {
                WoldsVaults.LOGGER.error("greed_milestones.json dedicatedLooter.chestOrder names '{}', which is not a vault chest type; falling back to the shipped order {}", name, DEFAULT_DEDICATED_ORDER);
                return DEFAULT_DEDICATED_ORDER;
            }
        }
        return List.copyOf(order);
    }

    private static MilestoneDefinition register(MilestoneDefinition definition) {
        if (BY_ID.put(definition.getId(), definition) != null) {
            throw new IllegalStateException("Duplicate milestone id " + definition.getId());
        }
        BY_CATEGORY.computeIfAbsent(definition.getCategory(), c -> new ArrayList<>()).add(definition);
        if (definition.getChallengeCrystalId() != null
                && BY_CHALLENGE_CRYSTAL.put(definition.getChallengeCrystalId(), definition) != null) {
            throw new IllegalStateException("Duplicate challenge crystal id " + definition.getChallengeCrystalId());
        }
        return definition;
    }

    /**
     * The configured numbers for a milestone. A missing or malformed entry is reported by id and
     * answered from the shipped defaults, because dropping the milestone would silently delete a
     * player's progress in it and stubbing it would corrupt the tier the save already holds.
     */
    private static GreedMilestonesConfig.Entry entry(String id) {
        GreedMilestonesConfig.Entry entry = config.getMilestone(id);
        if (entry != null && entry.isUsable()) {
            return entry;
        }
        WoldsVaults.LOGGER.error("Milestone '{}' has no usable entry in greed_milestones.json; falling back to its shipped thresholds and reputation", id);
        GreedMilestonesConfig.Entry fallback = GreedMilestonesConfig.defaults().getMilestone(id);
        if (fallback == null) {
            throw new IllegalStateException("Milestone " + id + " has no shipped default either");
        }
        return fallback;
    }

    private static void count(String id, MilestoneCategory category) {
        register(id, category, MilestoneCounter.ACCUMULATE, null);
    }

    private static void highest(String id, MilestoneCategory category) {
        register(id, category, MilestoneCounter.HIGHEST, null);
    }

    private static void distinct(String id, MilestoneCategory category) {
        register(id, category, MilestoneCounter.DISTINCT, null);
    }

    /**
     * A pass/fail milestone. Its counter is a high-water mark rather than an accumulator because
     * {@link Milestones#complete} raises it to 1 through {@link Milestones#reach}, and the counter
     * a definition declares is enforced against the mutator that touches it.
     */
    private static void oneShot(String id, MilestoneCategory category) {
        register(id, category, MilestoneCounter.HIGHEST, null);
    }

    /**
     * Registers a challenge-crystal milestone: one tier, reputation and rank tag from config, and
     * the crystal id that both completes it and is gated by that same rank.
     */
    private static void challenge(String id, String challengeCrystalId) {
        register(id, MilestoneCategory.CHALLENGE, MilestoneCounter.HIGHEST, challengeCrystalId);
    }

    private static void register(String id, MilestoneCategory category, MilestoneCounter counter, String challengeCrystalId) {
        GreedMilestonesConfig.Entry entry = entry(id);
        register(new MilestoneDefinition(id, category, counter, entry.thresholds(), entry.reputation(),
                challengeCrystalId, entry.requiredRank()));
    }

    private static void bootstrap() {
        count(MilestoneIds.LIVING_LOOTER, MilestoneCategory.LOOTING);
        count(MilestoneIds.ORNATE_LOOTER, MilestoneCategory.LOOTING);
        count(MilestoneIds.GILDED_LOOTER, MilestoneCategory.LOOTING);
        count(MilestoneIds.WOODEN_LOOTER, MilestoneCategory.LOOTING);
        count(MilestoneIds.TREASURE_HUNTER, MilestoneCategory.LOOTING);
        count(MilestoneIds.SHOP_HUNTER, MilestoneCategory.LOOTING);
        count(MilestoneIds.DIGGY_DIGGY_JEWEL, MilestoneCategory.LOOTING);
        count(MilestoneIds.AMAZON_WORKER, MilestoneCategory.LOOTING);
        count(MilestoneIds.NULLIFIED, MilestoneCategory.LOOTING);
        oneShot(MilestoneIds.DEDICATED_LOOTER, MilestoneCategory.LOOTING);

        count(MilestoneIds.BINGO, MilestoneCategory.THEME);
        count(MilestoneIds.DRINK_UP, MilestoneCategory.THEME);
        count(MilestoneIds.BRUTALIZED, MilestoneCategory.THEME);
        count(MilestoneIds.SCAVINGO, MilestoneCategory.THEME);
        count(MilestoneIds.CHAOS_OBJECTIVES, MilestoneCategory.THEME);
        count(MilestoneIds.HYPERION, MilestoneCategory.THEME);
        count(MilestoneIds.ALCHEMIST, MilestoneCategory.THEME);
        count(MilestoneIds.VAULTS_HUNTED, MilestoneCategory.THEME);
        count(MilestoneIds.LIGHT_THE_FLAME, MilestoneCategory.THEME);
        count(MilestoneIds.I_WILL_SURVIVE, MilestoneCategory.THEME);
        count(MilestoneIds.RUNIC_RITUAL, MilestoneCategory.THEME);
        count(MilestoneIds.ETERNAL_DARKNESS, MilestoneCategory.THEME);
        count(MilestoneIds.ROYALE_PAINE, MilestoneCategory.THEME);
        count(MilestoneIds.STAIRWAY_TO_HEAVEN, MilestoneCategory.THEME);
        distinct(MilestoneIds.SEEN_IT_ALL, MilestoneCategory.THEME);

        count(MilestoneIds.SLAYERRR, MilestoneCategory.COMBAT);
        count(MilestoneIds.HACK_N_SLASH, MilestoneCategory.COMBAT);
        count(MilestoneIds.ARCHMAGE, MilestoneCategory.COMBAT);
        count(MilestoneIds.SPELL_SPAMMER, MilestoneCategory.COMBAT);
        count(MilestoneIds.ELECTRIC_CONDUIT, MilestoneCategory.COMBAT);
        count(MilestoneIds.MASTER_OF_CHAINS, MilestoneCategory.COMBAT);
        count(MilestoneIds.DEFENSE, MilestoneCategory.COMBAT);
        count(MilestoneIds.WOOSH, MilestoneCategory.COMBAT);
        count(MilestoneIds.FIVE_LEAF_CLOVER, MilestoneCategory.COMBAT);
        count(MilestoneIds.BOOM, MilestoneCategory.COMBAT);
        count(MilestoneIds.DUNGEONEER, MilestoneCategory.COMBAT);
        count(MilestoneIds.VILLAIN, MilestoneCategory.COMBAT);
        count(MilestoneIds.FLAWLESS_VICTORY, MilestoneCategory.COMBAT);

        count(MilestoneIds.FAIL_VAULTS, MilestoneCategory.MISC);
        oneShot(MilestoneIds.MASTER_SMITH, MilestoneCategory.MISC);
        highest(MilestoneIds.IDONAS_CHAMPION, MilestoneCategory.MISC);
        highest(MilestoneIds.PRIEST_OF_VELARA, MilestoneCategory.MISC);
        highest(MilestoneIds.TENOS_RIGHT_HAND, MilestoneCategory.MISC);
        highest(MilestoneIds.WENDARRS_TIMEKEEPER, MilestoneCategory.MISC);
        count(MilestoneIds.EXPLORER, MilestoneCategory.MISC);
        count(MilestoneIds.I_LIVE_HERE_NOW, MilestoneCategory.MISC);
        count(MilestoneIds.LEGENDARY, MilestoneCategory.MISC);
        count(MilestoneIds.CHALLENGED, MilestoneCategory.MISC);
        oneShot(MilestoneIds.VAULT_OF_VAULTS, MilestoneCategory.MISC);
        count(MilestoneIds.WANTED_CRIMINAL, MilestoneCategory.MISC);
        count(MilestoneIds.ARCHEOLOGIST, MilestoneCategory.MISC);
        count(MilestoneIds.SEND_A_PRAYER, MilestoneCategory.MISC);
        count(MilestoneIds.BORN_AGAIN, MilestoneCategory.MISC);
        distinct(MilestoneIds.PAL_TRAINER, MilestoneCategory.MISC);
        highest(MilestoneIds.VAULT_VETERAN, MilestoneCategory.MISC);

        challenge(MilestoneIds.UNSTABLE_SHUFFLE, "t1_shuffle_grounds");
        challenge(MilestoneIds.PITCH_BLACK, "pitch_black");
        challenge(MilestoneIds.TRAPPED, "scav_rolled");
        challenge(MilestoneIds.BALLISTIC_BLACKOUT, "ballistic_bingo_blackout");
        challenge(MilestoneIds.RUNE_MASTER, "rune_master");
        challenge(MilestoneIds.ELIXIR_OF_DOOM, "elixir_of_doom");
        challenge(MilestoneIds.COLLECTATHON, "blackout_scavingo");
        challenge(MilestoneIds.ROYALE_KING, "royale_king");
        challenge(MilestoneIds.GONE_IN_60_SECONDS, "gone_in_60_seconds");
        challenge(MilestoneIds.SURVIVAL_OF_THE_FITTEST, "survival_of_the_fittest");
        challenge(MilestoneIds.THE_PACIFIST, "the_pacifist");
        challenge(MilestoneIds.GODS_CHALLENGE, "gods_challenge");
        challenge(MilestoneIds.LUCKIEST_WIN, "luckiest_win");
        challenge(MilestoneIds.BIG_BAD_BREW, "big_bad_brew");
        challenge(MilestoneIds.THE_SPEEDRUNNER, "the_speedrunner");
        challenge(MilestoneIds.TIME_YOU_SAY, "time_you_say");
        challenge(MilestoneIds.CHAOS_CHAOS_CHAOS, "chaos_chaos_chaos");
        challenge(MilestoneIds.THE_SPEED_BLACKOUT, "speed_blackout_bingo");
        challenge(MilestoneIds.FRENZY_ADVENTURES, "one_shot_frenzy");
        challenge(MilestoneIds.RAGE_CAGE, "rage_cage");
        challenge(MilestoneIds.CELEBRATIONS, "celebrations");

        MilestoneDefinition seenItAll = BY_ID.get(MilestoneIds.SEEN_IT_ALL);
        if (seenItAll != null && seenItAll.getFinalThreshold() != SEEN_IT_ALL_TYPES.size()) {
            WoldsVaults.LOGGER.error("Milestone '{}' is configured to finish at {} objective types but {} are counted in code; it cannot be completed as configured",
                    MilestoneIds.SEEN_IT_ALL, seenItAll.getFinalThreshold(), SEEN_IT_ALL_TYPES.size());
        }

        ORDERED = List.copyOf(BY_ID.values());
        BY_CATEGORY.replaceAll((category, list) -> List.copyOf(list));
    }
}
