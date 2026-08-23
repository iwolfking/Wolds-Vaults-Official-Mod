package xyz.iwolfking.woldsvaults.medallions;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.core.vault.objective.BingoObjective;
import iskallia.vault.core.vault.objective.ChaosObjective;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.vault.objective.Objectives;
import iskallia.vault.core.vault.objective.RuneBossObjective;
import iskallia.vault.core.vault.objective.ScavengerBingoObjective;
import iskallia.vault.core.vault.objective.rune.RuneBossFight;
import iskallia.vault.core.vault.objective.rune.RuneBossFights;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.VaultModifierUtils;
import xyz.iwolfking.woldsvaults.objectives.AlchemyObjective;
import xyz.iwolfking.woldsvaults.objectives.BallisticBingoObjective;
import xyz.iwolfking.woldsvaults.objectives.BrutalBossesObjective;
import xyz.iwolfking.woldsvaults.objectives.CorruptedObjective;
import xyz.iwolfking.woldsvaults.objectives.EnchantedElixirObjective;
import xyz.iwolfking.woldsvaults.objectives.HauntedBraziersObjective;
import xyz.iwolfking.woldsvaults.objectives.HyperVaultObjective;
import xyz.iwolfking.woldsvaults.objectives.UnhingedScavengerObjective;
import xyz.iwolfking.woldsvaults.objectives.ZealotObjective;

import java.util.List;

/**
 * The greed crate loot (GCL) system: which bonus loot table a completion crate rolls and how many greed
 * coins ride along. The table comes from the crystal's greed medallion
 * ({@code woldsvaults:greed_crate_loot_1} through {@code _7}, pack-side in
 * {@code config/the_vault/gen/1.0/loot_tables/}); the objective is only a scalar coin multiplier.
 */
public final class GreedCrateLoot {
    private static final String TABLE_PREFIX = "greed_crate_loot_";
    private static final int MAX_TABLE = 7;
    private static final double DEFAULT_MULTIPLIER = 1.0D;
    private static final double MINOR_MULTIPLIER = 1.2D;
    private static final double MAJOR_MULTIPLIER = 1.5D;
    private static final double BALLISTIC_MULTIPLIER = 1.6D;
    private static final double DOUBLE_MULTIPLIER = 2.0D;
    private static final double BINGO_LINE_GROWTH = 1.025D;
    private static final double BALLISTIC_LINE_GROWTH = 1.03D;
    private static final ResourceLocation GREEDY_CRATE_TIER = WoldsVaults.id("greedy_crate_tier");

    private GreedCrateLoot() {
    }

    /**
     * The loot table key for a medallion's GCL tier; tier 0 (Scavenger 1 and 2) has no table and pays greed coins
     * only.
     */
    public static ResourceLocation tableId(int greedCrateLootTier) {
        return WoldsVaults.id(TABLE_PREFIX + Math.min(MAX_TABLE, greedCrateLootTier));
    }

    public static boolean isExcludedVault(Vault vault) {
        return VaultUtils.isRoyaleVault(vault)
                || VaultUtils.isBrazierVault(vault)
                || VaultUtils.isCakeVault(vault)
                || VaultUtils.isSpecialVault(vault)
                || !objectives(vault, HauntedBraziersObjective.class).isEmpty();
    }

    /**
     * Greed coins for one crate: {@code floor(baseGreedCoins * objectiveMultiplier)}, then the greedy-crate-tier
     * bonus, which only hyperboss kills grant.
     */
    public static int coinsForCrate(Vault vault, GreedMedallionTier medallion) {
        double multiplier = objectiveMultiplier(vault);
        int coins = (int) Math.floor(medallion.getBaseGreedCoins() * multiplier);
        long greedyCrateTiers = VaultModifierUtils.getCountOfModifiers(vault, GREEDY_CRATE_TIER);
        if (greedyCrateTiers > 0) {
            coins = Math.round(coins * (1.0F
                    + HyperVaultObjective.cfg().getGreedyCoinBonusPerStack() * greedyCrateTiers));
        }
        return Math.max(0, coins);
    }

    /**
     * The objective's coin multiplier; an unlisted objective falls back to 1x, and several qualifying objectives
     * resolve to the largest of the set.
     */
    public static double objectiveMultiplier(Vault vault) {
        double multiplier = DEFAULT_MULTIPLIER;
        if (HyperVaultObjective.get(vault).isPresent()) {
            multiplier = Math.max(multiplier, DOUBLE_MULTIPLIER);
        }
        if (!objectives(vault, CorruptedObjective.class).isEmpty()) {
            multiplier = Math.max(multiplier, DOUBLE_MULTIPLIER);
        }
        if (!objectives(vault, AlchemyObjective.class).isEmpty()
                || !objectives(vault, ChaosObjective.class).isEmpty()) {
            multiplier = Math.max(multiplier, MAJOR_MULTIPLIER);
        }
        if (!objectives(vault, BrutalBossesObjective.class).isEmpty()
                || !objectives(vault, ZealotObjective.class).isEmpty()
                || !objectives(vault, UnhingedScavengerObjective.class).isEmpty()
                || !objectives(vault, EnchantedElixirObjective.class).isEmpty()) {
            multiplier = Math.max(multiplier, MINOR_MULTIPLIER);
        }
        for (BallisticBingoObjective objective : objectives(vault, BallisticBingoObjective.class)) {
            multiplier = Math.max(multiplier,
                    BALLISTIC_MULTIPLIER * Math.pow(BALLISTIC_LINE_GROWTH, Math.max(0, objective.getBingos())));
        }
        for (BingoObjective objective : objectives(vault, BingoObjective.class)) {
            multiplier = Math.max(multiplier,
                    MAJOR_MULTIPLIER * Math.pow(BINGO_LINE_GROWTH, Math.max(0, objective.getBingos())));
        }
        for (ScavengerBingoObjective objective : objectives(vault, ScavengerBingoObjective.class)) {
            multiplier = Math.max(multiplier,
                    MAJOR_MULTIPLIER * Math.pow(BINGO_LINE_GROWTH, Math.max(0, objective.getCompletedBingos())));
        }
        int runes = collectedRunes(vault);
        if (runes >= 0) {
            multiplier = Math.max(multiplier, MINOR_MULTIPLIER * Math.max(1.0D, Math.log10(Math.max(1, runes))));
        }
        return multiplier;
    }

    /**
     * Runes consumed at this vault's boss pillars, summed over every fight; -1 when the vault has no rune boss
     * objective.
     */
    private static int collectedRunes(Vault vault) {
        List<RuneBossObjective> bosses = objectives(vault, RuneBossObjective.class);
        if (bosses.isEmpty()) {
            return -1;
        }
        int runes = 0;
        for (RuneBossObjective objective : bosses) {
            RuneBossFights fights = objective.getOptional(RuneBossObjective.FIGHTS).orElse(null);
            if (fights == null) {
                WoldsVaults.LOGGER.warn("Rune boss objective has no fight list; its greed coin bonus falls back to the 1.2x floor.");
                continue;
            }
            for (RuneBossFight fight : fights.getFights()) {
                runes += fight.getRuneCount();
            }
        }
        return runes;
    }

    private static <T extends Objective> List<T> objectives(Vault vault, Class<T> type) {
        if (vault == null || !vault.has(Vault.OBJECTIVES)) {
            return List.of();
        }
        Objectives objectives = vault.get(Vault.OBJECTIVES);
        return objectives == null ? List.of() : objectives.getAll(type);
    }
}
