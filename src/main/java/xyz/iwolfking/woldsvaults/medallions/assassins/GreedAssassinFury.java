package xyz.iwolfking.woldsvaults.medallions.assassins;

import iskallia.vault.VaultMod;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.entity.champion.ChampionLogic;
import iskallia.vault.init.ModConfigs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.VaultMobUtils;
import xyz.iwolfking.woldsvaults.config.GreedMedallionsConfig;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionTier;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionVaultState;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Assassin fury: the per-player, per-vault meter that decides when greed assassins spawn. Kills fill it, weighted by
 * mob class and scaled by the medallion rank and by a boost that favours a slow pace; past the threshold a roll every
 * {@code rollIntervalTicks} arms a squad, which arrives on the player's next kill and spends one threshold of fury per
 * assassin. Every assassin spawned adds slowdown debt that cuts further fury gain and drains away exponentially over the
 * rank's recovery time. All state is keyed by vault and dropped when the vault ends.
 */
public final class GreedAssassinFury {
    private static final ResourceLocation GROUP_TANK = VaultMod.id("tank");
    private static final ResourceLocation GROUP_HORDE = VaultMod.id("horde");
    private static final ResourceLocation GROUP_ASSASSIN = VaultMod.id("assassin");
    private static final double TICKS_PER_SECOND = 20.0D;
    private static final Map<UUID, Map<UUID, State>> VAULTS = new ConcurrentHashMap<>();

    private GreedAssassinFury() {
    }

    /** One runner's meter in one vault. */
    static final class State {
        private final GreedMedallionTier tier;
        private double fury;
        private double debt;
        private double pace;
        private long lastTick = -1L;
        private long readySinceTick = -1L;
        private boolean armed;

        State(GreedMedallionTier tier) {
            this.tier = tier;
        }

        private void advance(long now, GreedMedallionsConfig.Fury curve) {
            if (this.lastTick >= 0L && now > this.lastTick) {
                double elapsed = now - this.lastTick;
                this.debt *= Math.exp(-elapsed / (this.tier.getAssassinRecoverySeconds() * TICKS_PER_SECOND));
                this.pace *= Math.exp(-elapsed / (curve.rateWindowSeconds * TICKS_PER_SECOND));
            }
            this.lastTick = now;
        }

        private void gain(double weight, long now, GreedMedallionsConfig.Fury curve) {
            this.advance(now, curve);
            double boost = 1.0D + curve.boostAmount / (1.0D + Math.pow(this.pace / curve.boostKneeRate, curve.boostExponent));
            this.fury += this.tier.getAssassinFuryGain() * weight * boost / (1.0D + curve.slowdownPerAssassin * this.debt);
            this.pace += weight * 60.0D / curve.rateWindowSeconds;
        }

        private void spend(int assassins, long now, GreedMedallionsConfig.Fury curve) {
            this.advance(now, curve);
            this.fury = Math.max(0.0D, this.fury - assassins * curve.threshold);
            this.debt += assassins;
            this.armed = false;
            this.readySinceTick = -1L;
        }

        private void roll(long now, GreedMedallionsConfig.Fury curve, Random random) {
            if (this.armed) {
                return;
            }
            if (this.fury < curve.threshold) {
                this.readySinceTick = -1L;
                return;
            }
            if (this.readySinceTick < 0L) {
                this.readySinceTick = now;
            }
            double waited = (now - this.readySinceTick) / TICKS_PER_SECOND + curve.rollIntervalTicks / TICKS_PER_SECOND;
            double progress = Math.max(this.fury / (this.tier.getAssassinSquadTarget() * curve.threshold),
                    waited / curve.timeoutSeconds);
            double chance = 1.0D - Math.exp(-curve.rollRate * Math.pow(progress, curve.rollShape));
            if (random.nextDouble() < chance) {
                this.armed = true;
            }
        }
    }

    /** Adds a kill's fury to its killer's meter and, when a squad is armed, spawns it. */
    public static void onKill(LivingEntity killed, LivingDeathEvent event, ServerLevel level) {
        if (killed instanceof Player || GreedAssassins.isAssassin(killed)) {
            return;
        }
        ServerPlayer player = GreedAssassinSpawner.resolveKiller(event);
        if (player == null) {
            return;
        }
        Vault vault = VaultUtils.getVault(killed.level).orElse(null);
        if (vault == null || !vault.has(Vault.ID)) {
            return;
        }
        GreedMedallionTier tier = GreedMedallionVaultState.get(vault).orElse(null);
        if (tier == null || VaultUtils.isHeraldVault(vault) || VaultUtils.isRebirthVault(vault)) {
            return;
        }
        GreedMedallionsConfig.Fury curve = GreedMedallionTier.furyCurve();
        if (curve == null) {
            return;
        }
        long now = level.getGameTime();
        State state = VAULTS.computeIfAbsent(vault.get(Vault.ID), id -> new ConcurrentHashMap<>())
                .computeIfAbsent(player.getUUID(), id -> new State(tier));
        double weight = killWeight(killed, curve);
        if (weight > 0.0D) {
            state.gain(weight, now, curve);
        }
        if (state.armed) {
            fireSquad(vault, level, player, state, curve, now);
        }
    }

    private static void fireSquad(Vault vault, ServerLevel level, ServerPlayer player, State state,
                                  GreedMedallionsConfig.Fury curve, long now) {
        int wanted = (int) Math.min(Math.floor(state.fury / curve.threshold), curve.maxSquad);
        if (wanted < 1) {
            WoldsVaults.LOGGER.error("Greed assassin squad armed for {} with only {} fury of a {} threshold - disarming.",
                    player.getGameProfile().getName(), (long) state.fury, (long) curve.threshold);
            state.armed = false;
            return;
        }
        int spawned = 0;
        for (int i = 0; i < wanted; i++) {
            if (GreedAssassinSpawner.spawn(vault, state.tier, level, player)) {
                spawned++;
            }
        }
        if (spawned == 0) {
            WoldsVaults.LOGGER.warn("No spawn position found for any of {} greed assassins near {} - the squad stays armed "
                    + "for their next kill.", wanted, player.getGameProfile().getName());
            return;
        }
        state.spend(spawned, now, curve);
        GreedAssassinSpawner.announce(level, player, spawned);
    }

    private static double killWeight(LivingEntity killed, GreedMedallionsConfig.Fury curve) {
        if (VaultMobUtils.isEliteMob(killed) || ChampionLogic.isChampion(killed)) {
            return curve.eliteKill;
        }
        if (ModConfigs.ENTITY_GROUPS.isInGroup(GROUP_TANK, killed)) {
            return curve.tankKill;
        }
        if (ModConfigs.ENTITY_GROUPS.isInGroup(GROUP_ASSASSIN, killed)) {
            return curve.assassinKill;
        }
        if (ModConfigs.ENTITY_GROUPS.isInGroup(GROUP_HORDE, killed)) {
            return curve.hordeKill;
        }
        return 0.0D;
    }

    /** Runs the arming roll for every meter once every {@code rollIntervalTicks}. */
    public static void tick(MinecraftServer server) {
        GreedMedallionsConfig.Fury curve = GreedMedallionTier.furyCurve();
        if (curve == null || VAULTS.isEmpty() || server.getTickCount() % curve.rollIntervalTicks != 0) {
            return;
        }
        ServerLevel overworld = server.overworld();
        long now = overworld.getGameTime();
        for (Map<UUID, State> players : VAULTS.values()) {
            for (State state : players.values()) {
                state.roll(now, curve, overworld.random);
            }
        }
    }

    public static void release(UUID vaultId) {
        if (vaultId != null) {
            VAULTS.remove(vaultId);
        }
    }

    public static void clearAll() {
        VAULTS.clear();
    }
}
