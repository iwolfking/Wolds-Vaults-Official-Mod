package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import xyz.iwolfking.woldsvaults.config.GreedChampionConfig;
import xyz.iwolfking.woldsvaults.config.GreedChampionConfig.Rank;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractWoldsTableProvider;
import xyz.iwolfking.woldsvaults.milestones.MilestoneRankLadder;

import java.util.LinkedHashMap;

/**
 * The Vault Champion. Ranks are keyed by the summoning medallion's rank index (health pool, then damage, movement
 * speed and attack speed multipliers); a medallion with no entry never summons one. Every rank shares one health pool;
 * the spawner multiplies pool and damage by the medallion's mob multiplier. The other blocks set level
 * scaling, the hunt and leash, the forced lightning storm, the flat true damage of Champions and assassins, the
 * effect-piercing aura, rage accrual and the arming roll, and the per-rank rewards.
 */
public class ModGreedChampionProvider extends AbstractWoldsTableProvider {

    private static final int FIRST_RANK = MilestoneRankLadder.MASTER_1;
    private static final double HEALTH_POOL = 5_000_000.0D;
    private static final double LEGEND_DAMAGE_MULTIPLIER = 250.0D / 3.0D;
    private static final double[] MOVEMENT_SPEED_MULTIPLIER = {1.5D, 1.8D, 2.1D, 2.5D, 3.0D, 3.5D, 4.0D};
    private static final double[] ATTACK_SPEED_MULTIPLIER = {2.0D, 2.4D, 2.8D, 3.3D, 3.9D, 4.4D, 5.0D};

    public ModGreedChampionProvider(DataGenerator generator) {
        super(generator, "greed/champion");
    }

    @Override
    public void registerConfigs() {
        GreedChampionConfig table = new GreedChampionConfig();
        table.ranks = new LinkedHashMap<>();
        for (int rank = FIRST_RANK; rank <= MilestoneRankLadder.LEGEND_RANK; rank++) {
            int i = rank - FIRST_RANK;
            table.ranks.put(String.valueOf(rank), Rank.of(HEALTH_POOL, damageMultiplier(rank),
                    MOVEMENT_SPEED_MULTIPLIER[i], ATTACK_SPEED_MULTIPLIER[i]));
        }

        table.scaling = new GreedChampionConfig.Scaling();
        table.scaling.baseAttackDamage = 24.0D;
        table.scaling.levelAnchor = 100;
        table.scaling.movementSpeedCap = 1.0D;
        table.scaling.scaleTeleportCooldown = false;
        table.scaling.inheritVaultModifiers = true;
        table.scaling.sizeMultiplier = 1.5F;

        table.hud = new GreedChampionConfig.Hud();
        table.hud.offsetX = 0;
        table.hud.offsetY = 30;

        table.hunt = new GreedChampionConfig.Hunt();
        table.hunt.aggroRadius = 40.0D;
        table.hunt.leashDistance = 35.0D;
        table.hunt.spawnRingMax = 8.0D;
        table.hunt.dormantTicks = 40;
        table.hunt.orphanTicks = 200;
        table.hunt.managerIntervalTicks = 20;

        table.lightning = new GreedChampionConfig.Lightning();
        table.lightning.aboveBlocks = 1.5D;
        table.lightning.belowBlocks = 4.0D;
        table.lightning.farBlocks = 25.0D;
        table.lightning.chargeTicks = 40;

        table.trueDamage = new GreedChampionConfig.TrueDamage();
        table.trueDamage.champion = 15.0F;
        table.trueDamage.assassin = 10.0F;

        table.aura = new GreedChampionConfig.Aura();
        table.aura.radius = 10.0D;
        table.aura.assassinsCarryIt = true;

        table.rage = new GreedChampionConfig.Rage();
        table.rage.decayPerTick = 6.0D;
        table.rage.chestLooted = 200.0D;
        table.rage.oreMined = 200.0D;
        table.rage.hordeKill = 100.0D;
        table.rage.assassinKill = 300.0D;
        table.rage.tankKill = 700.0D;
        table.rage.eliteChampionMultiplier = 2.0D;
        table.rage.waveMobKillMultiplier = 0.2D;
        table.rage.baseThreshold = 100_000.0D;
        table.rage.thresholdMultiplierPerKill = 4.0D;
        table.rage.rollIntervalTicks = 20;
        table.rage.baseChance = 0.001D;
        table.rage.legendBaseChance = 0.0025D;

        table.rewards = new GreedChampionConfig.Rewards();
        table.rewards.greedCoinsPerRank = 5;
        table.rewards.greedyTicketsPerRank = 2;
        this.table("greed_champion", table);
    }

    /** Half the Legend damage multiplier at the first Champion rank, rising linearly to the full value at Legend. */
    private static double damageMultiplier(int rank) {
        double share = (rank - FIRST_RANK) / (double) (MilestoneRankLadder.LEGEND_RANK - FIRST_RANK);
        return LEGEND_DAMAGE_MULTIPLIER * (0.5D + 0.5D * share);
    }
}
