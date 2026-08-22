package xyz.iwolfking.woldsvaults.medallions.champion;

import iskallia.vault.VaultMod;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.entity.boss.TheVesselEntity;
import iskallia.vault.entity.champion.ChampionLogic;
import iskallia.vault.init.ModConfigs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import xyz.iwolfking.woldsvaults.config.GreedChampionConfig;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionTier;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionVaultState;
import xyz.iwolfking.woldsvaults.medallions.assassins.GreedAssassinSpawner;
import xyz.iwolfking.woldsvaults.medallions.assassins.GreedAssassins;

/**
 * Rage: the per-player, per-vault counter that decides when a Vault Champion turns up.
 *
 * <p>It rises on the things a greedy run is actually made of - chests, ores and dangerous kills - and
 * bleeds away every tick, so it measures pace rather than total progress. A slow, careful run never
 * reaches the threshold; a run that keeps pushing does.
 *
 * <p>Passing the roll does not spawn anything. It arms a spawn, and the Champion arrives on the very
 * next chest, ore or kill. That is deliberate: a boss that materialises out of nothing reads as the
 * game picking on you, while one that arrives the instant you crack another chest reads as the
 * consequence of what you just did.</p>
 */
public final class VaultChampionRage {
    private static final ResourceLocation GROUP_HORDE = VaultMod.id("horde");
    private static final ResourceLocation GROUP_TANK = VaultMod.id("tank");
    private static final Object OWNER = new Object();

    private VaultChampionRage() {
    }

    static void register() {
        CommonEvents.CHEST_LOOT_GENERATION.post().register(OWNER, data ->
                award(data.getPlayer(), config -> config.getRage().chestLooted));
        CommonEvents.ORE_LOOT_GENERATION_EVENT.register(OWNER, data ->
                award(data.getPlayer(), config -> config.getRage().oreMined));
    }

    /** Rage for a kill, doubled when what died was an elite champion. */
    public static void onKill(LivingEntity killed, LivingDeathEvent event, ServerLevel level) {
        ServerPlayer killer = GreedAssassinSpawner.resolveKiller(event);
        if (killer == null || killed instanceof TheVesselEntity) {
            return;
        }
        award(killer, config -> {
            GreedChampionConfig.Rage rage = config.getRage();
            double value = killValue(killed, rage);
            if (value <= 0.0D) {
                return 0.0D;
            }
            return ChampionLogic.isChampion(killed) ? value * rage.eliteChampionMultiplier : value;
        });
    }

    private static double killValue(LivingEntity killed, GreedChampionConfig.Rage rage) {
        if (GreedAssassins.isAssassin(killed)) {
            return rage.assassinKill;
        }
        if (ModConfigs.ENTITY_GROUPS.isInGroup(GROUP_TANK, killed)) {
            return rage.tankKill;
        }
        if (ModConfigs.ENTITY_GROUPS.isInGroup(GROUP_HORDE, killed)) {
            return rage.hordeKill;
        }
        return 0.0D;
    }

    /** One source of rage for one player, followed immediately by the armed-spawn check. */
    private static void award(ServerPlayer player, java.util.function.ToDoubleFunction<GreedChampionConfig> amount) {
        if (player == null || player.level.isClientSide) {
            return;
        }
        Vault vault = VaultUtils.getVault(player.level).orElse(null);
        if (vault == null || !vault.has(Vault.ID)) {
            return;
        }
        GreedMedallionTier tier = GreedMedallionVaultState.get(vault).orElse(null);
        if (tier == null || !tier.vaultChampionHunts()) {
            return;
        }
        if (VaultUtils.isHeraldVault(vault) || VaultUtils.isRebirthVault(vault)) {
            return;
        }
        VaultChampionState.PlayerState state = VaultChampionState.get(vault, player.getUUID());
        if (state == null) {
            return;
        }
        GreedChampionConfig config = VaultChampion.config();
        double value = amount.applyAsDouble(config);
        if (value <= 0.0D) {
            return;
        }
        state.addRage(value);
        fireArmedSpawn(vault, tier, state, player);
    }

    /**
     * Spends an armed spawn. Blocked by the same rune-boss and hyper-boss check the roll is, so a
     * spawn armed before a boss fight started simply waits it out rather than dropping a second boss
     * into the arena.
     */
    private static void fireArmedSpawn(Vault vault, GreedMedallionTier tier,
                                       VaultChampionState.PlayerState state, ServerPlayer player) {
        if (!state.isArmed() || state.getLiveChampion() != null) {
            return;
        }
        if (!(player.level instanceof ServerLevel level) || VaultChampionManager.bossFightInProgress(level)) {
            return;
        }
        TheVesselEntity champion = VaultChampionSpawner.spawn(vault, tier, level, player);
        if (champion == null) {
            return;
        }
        state.setArmed(false);
        state.setLiveChampion(champion.getUUID());
        VaultChampionKills.openBar(vault, champion);
    }
}
