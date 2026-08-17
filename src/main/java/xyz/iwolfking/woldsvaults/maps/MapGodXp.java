package xyz.iwolfking.woldsvaults.maps;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.core.vault.objective.BingoObjective;
import iskallia.vault.core.vault.objective.Objectives;
import iskallia.vault.core.vault.objective.ScavengerBingoObjective;
import iskallia.vault.gear.data.AttributeGearData;
import iskallia.vault.gear.data.VaultGearData;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;
import xyz.iwolfking.woldsvaults.objectives.BallisticBingoObjective;
import xyz.iwolfking.woldsvaults.objectives.HyperVaultObjective;

import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

/**
 * The numbers behind god-experience vault maps: which god a map is bound to, how big its bonus-XP
 * implicit rolls, and how much god experience the mapped vault pays out on completion.
 *
 * <p>Every map drops bound to one of the four vault gods, and that binding never changes. On top of
 * it sits a "Bonus XP" implicit whose band depends on the map tier and on whether the map has been
 * touched in the artisan station: a natural, untouched map rolls the high band, and any modification
 * re-rolls it into the much lower "rolled" band, which is the design's disincentive against
 * target-farming one fast objective. Both bands come from the greed rework's God XP sheet.</p>
 *
 * <p>Award formula: {@code base(objective) * cbrt(vault difficulty) * (1 + bonus%)}, banked through
 * {@code GodAlignmentData#addGodXp} so the God's Disciple prestige powers still multiply it.</p>
 */
public final class MapGodXp {
    /** Lowest and highest bonus-XP percent per map tier, index 0 = tier 1 ... index 5 = tier 6. */
    private static final int[][] UNROLLED_BAND = {
            {80, 125}, {115, 150}, {140, 165}, {150, 175}, {160, 190}, {175, 200}
    };

    private static final int[][] ROLLED_BAND = {
            {0, 25}, {0, 35}, {0, 45}, {0, 55}, {0, 65}, {0, 75}
    };

    /**
     * Base god experience per objective, keyed by the crystal objective type written into
     * {@code Objectives.KEY}. Objectives outside this table pay nothing — braziers in particular are
     * excluded from greed rewards entirely by the design doc. Three of the entries are scaled
     * further by live vault state and are handled in {@link #baseXp(Vault, String)}.
     */
    private static final Map<String, Double> BASE_XP = Map.ofEntries(
            Map.entry("elixir", 2000.0D),
            Map.entry("scavenger", 2000.0D),
            Map.entry("unhinged_scavenger", 2000.0D),
            Map.entry("brutal_bosses", 2500.0D),
            Map.entry("rune_boss", 3000.0D),
            Map.entry("enchanted_elixir", 3000.0D),
            Map.entry("hyper", 3000.0D),
            Map.entry("alchemy", 3000.0D),
            Map.entry("bingo", 3000.0D),
            Map.entry("scavenger_bingo", 3000.0D),
            Map.entry("unhinged_scavenger_bingo", 3000.0D),
            Map.entry("chaos", 4000.0D),
            Map.entry("corrupted", 4000.0D),
            Map.entry("ballistic_bingo", 4000.0D),
            Map.entry("zealot", 5000.0D)
    );

    private static final Random SHARED_RANDOM = new Random();

    private static final Map<String, String> ALIASES = Map.of(
            "scaling_scavenger_bingo", "scavenger_bingo",
            "scaling_unhinged_scavenger_bingo", "unhinged_scavenger_bingo",
            "scaling_ballistic_bingo", "ballistic_bingo");

    private MapGodXp() {
    }

    /** Stored map tier is 0-5; this is the display tier 1-6 the sheet's bands are tabled against. */
    public static int displayTier(ItemStack stack) {
        return clampTier(VaultGearData.read(stack).getFirstValue(ModGearAttributes.MAP_TIER).orElse(0) + 1);
    }

    private static int clampTier(int displayTier) {
        return Math.min(Math.max(displayTier, 1), UNROLLED_BAND.length);
    }

    public static int rollBonusPercent(int displayTier, boolean modified, Random random) {
        int[] band = (modified ? ROLLED_BAND : UNROLLED_BAND)[clampTier(displayTier) - 1];
        return band[0] + random.nextInt(band[1] - band[0] + 1);
    }

    /** Same roll for callers with no random of their own, such as the artisan-station hook. */
    public static int rollBonusPercent(int displayTier, boolean modified) {
        return rollBonusPercent(displayTier, modified, SHARED_RANDOM);
    }

    /**
     * Re-rolls a map's Bonus XP into its tier's "rolled" band. Called for every artisan station
     * modification of a map, whatever the modification was: the design's rule is that touching the
     * map at all costs it the natural-drop bonus.
     */
    public static void onMapModified(ItemStack stack) {
        VaultGearData data = VaultGearData.read(stack);
        if (!data.hasAttribute(ModGearAttributes.MAP_BONUS_XP)) {
            return;
        }
        int displayTier = data.getFirstValue(ModGearAttributes.MAP_TIER).orElse(0) + 1;
        data.createOrReplaceAttributeValue(ModGearAttributes.MAP_BONUS_XP, rollBonusPercent(displayTier, true));
        data.write(stack);
    }

    public static VaultGod rollGod(Random random) {
        return VaultGod.values()[random.nextInt(VaultGod.values().length)];
    }

    /**
     * The god a map is bound to. Maps rolled before god binding existed carry no god attribute, so
     * they fall back to a god derived from the item's own gear uuid — stable, and identical on the
     * client tooltip and on the server award path. Maps with neither attribute nor uuid are the only
     * ones that pay nothing.
     */
    public static Optional<VaultGod> godOf(ItemStack stack) {
        VaultGearData data = VaultGearData.read(stack);
        Optional<String> stored = data.getFirstValue(ModGearAttributes.MAP_GOD);
        if (stored.isPresent()) {
            VaultGod god = VaultGod.fromName(stored.get());
            if (god != null) {
                return Optional.of(god);
            }
            WoldsVaults.LOGGER.error("Vault map carries unknown god '{}'; falling back to its uuid-derived god.", stored.get());
        }
        return AttributeGearData.readUUID(stack).map(MapGodXp::godFromUuid);
    }

    private static VaultGod godFromUuid(UUID uuid) {
        int index = Math.floorMod(Long.hashCode(uuid.getMostSignificantBits() ^ uuid.getLeastSignificantBits()),
                VaultGod.values().length);
        return VaultGod.values()[index];
    }

    public static int bonusPercentOf(ItemStack stack) {
        return VaultGearData.read(stack).getFirstValue(ModGearAttributes.MAP_BONUS_XP).orElse(0);
    }

    /**
     * Base god experience for a completed mapped vault, before difficulty and the map's bonus. Three
     * objectives scale with live vault state: hyper by cycle, the three bingo variants by completed
     * lines. Returns 0 for objectives the sheet does not pay for.
     */
    public static double baseXp(Vault vault, String objectiveKey) {
        if (objectiveKey == null) {
            return 0.0D;
        }
        String canonical = ALIASES.getOrDefault(objectiveKey, objectiveKey);
        Double base = BASE_XP.get(canonical);
        if (base == null) {
            return 0.0D;
        }
        return switch (canonical) {
            case "hyper" -> base * Math.pow(1.1D, HyperVaultObjective.getCycleCount(vault));
            case "bingo", "scavenger_bingo", "unhinged_scavenger_bingo" -> base * Math.pow(1.02D, bingoLines(vault));
            case "ballistic_bingo" -> base * Math.pow(1.025D, bingoLines(vault));
            default -> base;
        };
    }

    /**
     * Completed bingo lines across whichever bingo objective the vault is running. Ballistic bingo
     * extends the base bingo objective, so the larger of the two reads is the honest count.
     */
    private static int bingoLines(Vault vault) {
        Optional<Objectives> objectives = vault.getOptional(Vault.OBJECTIVES);
        if (objectives.isEmpty()) {
            WoldsVaults.LOGGER.warn("Mapped vault has no objectives when counting bingo lines for god xp; counting zero.");
            return 0;
        }
        Objectives all = objectives.get();
        int lines = all.findFirst(BingoObjective.class).map(BingoObjective::getBingos).orElse(0);
        Optional<BallisticBingoObjective> ballistic = all.findFirst(BallisticBingoObjective.class);
        if (ballistic.isPresent()) {
            lines = Math.max(lines, ballistic.get().getBingos());
        }
        lines = Math.max(lines, all.findFirst(ScavengerBingoObjective.class)
                .map(ScavengerBingoObjective::getCompletedBingos).orElse(0));
        return lines;
    }

    /**
     * The full award for one player completing a mapped vault. The cube root of the vault's
     * difficulty multiplier is the sheet's "benefits multiplicatively from cbrt(objective
     * difficulty)"; the difficulty itself is captured at vault build so a released medallion cannot
     * change it mid-award.
     */
    public static long award(Vault vault, String objectiveKey, double difficultyMultiplier, int bonusPercent) {
        double base = baseXp(vault, objectiveKey);
        if (base <= 0.0D) {
            return 0L;
        }
        double difficulty = Math.cbrt(Math.max(difficultyMultiplier, 1.0D));
        return Math.round(base * difficulty * (1.0D + bonusPercent / 100.0D));
    }
}
