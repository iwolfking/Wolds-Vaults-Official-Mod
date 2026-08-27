package xyz.iwolfking.woldsvaults.maps;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.core.vault.objective.BingoObjective;
import iskallia.vault.core.vault.objective.Objectives;
import iskallia.vault.core.vault.objective.ScavengerBingoObjective;
import iskallia.vault.gear.data.AttributeGearData;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.modification.GearModification;
import iskallia.vault.init.ModGearModifications;
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
 * The numbers behind god-experience vault maps: the god a map is bound to, how big its bonus-XP
 * implicit rolls, and what the mapped vault pays out on completion.
 */
public final class MapGodXp {
    /** Lowest and highest bonus-XP percent per map tier, index 0 = tier 1 ... index 5 = tier 6. */
    private static final int[][] UNROLLED_BAND = {
            {80, 125}, {115, 150}, {140, 165}, {150, 175}, {160, 190}, {175, 200}
    };

    private static final int[][] ROLLED_BAND = {
            {0, 25}, {0, 35}, {0, 45}, {0, 55}, {0, 65}, {0, 75}
    };

    /** Base god experience per {@code Objectives.KEY}; an objective outside this table pays nothing. */
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

    /** Stored map tier is 0-5; this is the display tier 1-6 the bands are tabled against. */
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
     * Whether an artisan modification leaves a map's Bonus XP alone. Adding a modifier - the
     * Amplifying Focus, the only item bound to that operation - is free, so a map can be filled
     * with vault modifiers without paying the implicit; every other focus still re-rolls it.
     */
    public static boolean preservesBonusXp(GearModification modification) {
        return modification == ModGearModifications.ADD_MODIFIER;
    }

    /** Re-rolls a map's Bonus XP into its tier's "rolled" band, on any artisan modification. */
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

    /** The god a map is bound to, falling back to one derived from its gear uuid. */
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

    /** Base god xp for a completed mapped vault; hyper and bingo scale, unlisted objectives give 0. */
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

    /** Completed bingo lines across whichever bingo objective the vault is running. */
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

    /** One player's award: {@code base(objective) * cbrt(difficulty) * (1 + bonus%)}, rounded. */
    public static long award(Vault vault, String objectiveKey, double difficultyMultiplier, int bonusPercent) {
        double base = baseXp(vault, objectiveKey);
        if (base <= 0.0D) {
            return 0L;
        }
        double difficulty = Math.cbrt(Math.max(difficultyMultiplier, 1.0D));
        return Math.round(base * difficulty * (1.0D + bonusPercent / 100.0D));
    }
}
