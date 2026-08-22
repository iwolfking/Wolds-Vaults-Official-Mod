package xyz.iwolfking.woldsvaults.medallions.champion;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.entity.boss.TheVesselEntity;
import iskallia.vault.entity.boss.VaultBossBaseEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.entity.EntityTypeTest;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.GreedChampionConfig;
import xyz.iwolfking.woldsvaults.gods.GodVaultUtil;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionTier;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionVaultState;
import xyz.iwolfking.woldsvaults.medallions.assassins.GreedAssassinRegistry;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The server-side heartbeat of the Vault Champion: rage decay, the spawn roll, the leash, hunt-target
 * re-binding, the boss bar and the aura membership set.
 *
 * <p>It drives everything from a single server tick rather than from the entity, and that is what
 * makes the hunt work at all. A vault is many rooms across, nothing forces chunks beyond the one the
 * player stands in, and an entity outside a loaded region does not tick - so a Champion that relied
 * on its own AI to chase would stop dead the moment its quarry walked away. The leash here runs on
 * the server tick and teleports the Champion back into range regardless, which turns the whole
 * problem into a non-issue and closes the airborne escape the base mod's own teleport gate leaves
 * open.</p>
 */
public final class VaultChampionManager {
    private static final String ORPHAN_KEY = "woldsvaults:greed_champion_orphan";
    private static final int AURA_INTERVAL_TICKS = 10;

    private VaultChampionManager() {
    }

    public static void tick(MinecraftServer server) {
        GreedChampionConfig config = VaultChampion.config();
        int tick = server.getTickCount();
        if (tick % AURA_INTERVAL_TICKS == 0) {
            recomputeAura(server, config);
        }
        int interval = Math.max(1, config.getHunt().managerIntervalTicks);
        if (tick % interval != 0) {
            return;
        }
        for (ServerLevel level : server.getAllLevels()) {
            Vault vault = VaultUtils.getVault(level).orElse(null);
            if (vault == null || !vault.has(Vault.ID)) {
                continue;
            }
            Map<UUID, VaultChampionState.PlayerState> players = VaultChampionState.players(vault.get(Vault.ID));
            if (players.isEmpty()) {
                continue;
            }
            boolean bossFight = bossFightInProgress(level);
            for (Map.Entry<UUID, VaultChampionState.PlayerState> entry : players.entrySet()) {
                VaultChampionState.PlayerState state = entry.getValue();
                state.decay(config.getRage().decayPerTick * interval);
                tickChampion(vault, level, state, config);
                if (!bossFight) {
                    rollArm(vault, level, entry.getKey(), state, config);
                }
            }
        }
    }

    /**
     * Whether a rune boss or artifact boss is alive in this level. Both resolve their entity type from
     * config rather than a fixed id, so the shared base class is the only stable handle: hyper vaults
     * escalate the rune boss, and the rune boss and the artifact boss both extend it.
     */
    public static boolean bossFightInProgress(ServerLevel level) {
        List<? extends VaultBossBaseEntity> bosses = level.getEntities(
                EntityTypeTest.<Entity, VaultBossBaseEntity>forClass(VaultBossBaseEntity.class),
                LivingEntity::isAlive);
        return !bosses.isEmpty();
    }

    private static void tickChampion(Vault vault, ServerLevel level, VaultChampionState.PlayerState state,
                                     GreedChampionConfig config) {
        UUID championId = state.getLiveChampion();
        if (championId == null) {
            return;
        }
        Entity entity = level.getEntity(championId);
        if (!(entity instanceof TheVesselEntity champion) || !champion.isAlive()) {
            VaultChampionKills.removeBar(championId);
            state.setLiveChampion(null);
            return;
        }
        ServerPlayer hunted = resolveHuntTarget(level, champion);
        if (hunted == null) {
            hunted = rebind(vault, champion);
        }
        if (hunted == null) {
            tickOrphan(champion, config);
            return;
        }
        champion.getPersistentData().putInt(ORPHAN_KEY, 0);
        champion.setTarget(hunted);
        leash(level, champion, hunted, config);
        VaultChampionKills.syncBar(vault, champion);
    }

    private static ServerPlayer resolveHuntTarget(ServerLevel level, TheVesselEntity champion) {
        UUID targetId = VaultChampion.getHuntTarget(champion);
        if (targetId == null) {
            return null;
        }
        Entity entity = level.getEntity(targetId);
        return entity instanceof ServerPlayer player && player.isAlive() && !player.isSpectator() ? player : null;
    }

    /**
     * Hands the Champion a new victim when its current one dies, disconnects or extracts. The stamp it
     * rewrites is the hunt target only: the summoner keeps the rage bookkeeping, so the player who
     * brought this on themselves is still the one whose counter resets when it goes down.
     */
    private static ServerPlayer rebind(Vault vault, TheVesselEntity champion) {
        ServerPlayer nearest = null;
        double best = Double.MAX_VALUE;
        for (ServerPlayer runner : GodVaultUtil.runners(vault)) {
            if (!runner.isAlive() || runner.isSpectator() || runner.level != champion.level) {
                continue;
            }
            double distance = runner.distanceToSqr(champion);
            if (distance < best) {
                best = distance;
                nearest = runner;
            }
        }
        if (nearest == null) {
            return null;
        }
        VaultChampion.setHuntTarget(champion, nearest.getUUID());
        nearest.displayClientMessage(new TextComponent("The Vault Champion turns on you.")
                .withStyle(ChatFormatting.DARK_PURPLE), false);
        return nearest;
    }

    private static void tickOrphan(TheVesselEntity champion, GreedChampionConfig config) {
        int orphaned = champion.getPersistentData().getInt(ORPHAN_KEY) + config.getHunt().managerIntervalTicks;
        champion.getPersistentData().putInt(ORPHAN_KEY, orphaned);
        if (orphaned >= config.getHunt().orphanTicks) {
            WoldsVaults.LOGGER.debug("Discarding a Vault Champion with no runner left to hunt.");
            VaultChampionKills.removeBar(champion.getUUID());
            champion.discard();
        }
    }

    /**
     * Closes the distance the moment its quarry gets too far, ignoring every cooldown the Vessel's own
     * teleport is gated behind. Those gates include the target standing on the ground, which is how an
     * elytra escapes the arena fight; the whole point of a free-roaming hunter is that it does not.
     */
    private static void leash(ServerLevel level, TheVesselEntity champion, ServerPlayer hunted,
                              GreedChampionConfig config) {
        double leash = config.getHunt().leashDistance;
        if (champion.distanceToSqr(hunted) <= leash * leash) {
            return;
        }
        level.sendParticles(ParticleTypes.LARGE_SMOKE, champion.getX(), champion.getY() + 1.0D, champion.getZ(),
                24, 0.4D, 0.8D, 0.4D, 0.02D);
        champion.teleportInFrontOfPlayer(hunted, 3.0D);
        champion.setTarget(hunted);
        level.playSound(null, champion.getX(), champion.getY(), champion.getZ(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.5F, 0.6F);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, champion.getX(), champion.getY() + 1.0D, champion.getZ(),
                24, 0.4D, 0.8D, 0.4D, 0.02D);
    }

    /**
     * Rolls once per interval to arm a spawn. It never spawns directly: the roll only sets a flag, and
     * the Champion actually arrives on the player's next chest, ore or kill. That keeps the arrival
     * tied to something the player did rather than to a timer they cannot see.
     */
    private static void rollArm(Vault vault, ServerLevel level, UUID playerId,
                                VaultChampionState.PlayerState state, GreedChampionConfig config) {
        if (state.isArmed() || state.getLiveChampion() != null) {
            return;
        }
        if (VaultUtils.isHeraldVault(vault) || VaultUtils.isRebirthVault(vault)) {
            return;
        }
        GreedMedallionTier tier = GreedMedallionVaultState.get(vault).orElse(null);
        if (tier == null || !tier.vaultChampionHunts() || config.getRank(tier.getRankIndex()) == null) {
            return;
        }
        GreedChampionConfig.Rage rage = config.getRage();
        double threshold = state.threshold(rage.baseThreshold, rage.thresholdMultiplierPerKill);
        if (threshold <= 0.0D || state.getRage() < threshold) {
            return;
        }
        double base = tier.vaultChampionEnraged() ? rage.legendBaseChance : rage.baseChance;
        double chance = base * (state.getRage() / threshold);
        if (level.random.nextDouble() < chance) {
            state.setArmed(true);
            WoldsVaults.LOGGER.debug("Vault Champion spawn armed for {} at {} rage.", playerId, (long) state.getRage());
        }
    }

    /**
     * Rebuilds the set of players standing inside a Champion's or an assassin's aura. Recomputed on a
     * cadence and read as a set membership test, so the per-effect hook stays a hash lookup rather than
     * an entity sweep.
     */
    private static void recomputeAura(MinecraftServer server, GreedChampionConfig config) {
        GreedChampionConfig.Aura aura = config.getAura();
        double radiusSq = aura.radius * aura.radius;
        Set<UUID> next = new HashSet<>();
        for (ServerLevel level : server.getAllLevels()) {
            Vault vault = VaultUtils.getVault(level).orElse(null);
            if (vault == null || !vault.has(Vault.ID)) {
                continue;
            }
            List<ServerPlayer> runners = GodVaultUtil.runners(vault);
            if (runners.isEmpty()) {
                continue;
            }
            for (VaultChampionState.PlayerState state : VaultChampionState.players(vault.get(Vault.ID)).values()) {
                UUID championId = state.getLiveChampion();
                if (championId == null) {
                    continue;
                }
                Entity champion = level.getEntity(championId);
                if (champion != null && champion.isAlive()) {
                    collect(next, runners, champion, radiusSq);
                }
            }
        }
        if (aura.assassinsCarryIt) {
            GreedAssassinRegistry.forEach(server, (level, assassin) -> {
                Vault vault = VaultUtils.getVault(level).orElse(null);
                if (vault != null) {
                    collect(next, GodVaultUtil.runners(vault), assassin, radiusSq);
                }
            });
        }
        VaultChampionAura.setMembers(next);
    }

    private static void collect(Set<UUID> into, List<ServerPlayer> runners, Entity source, double radiusSq) {
        for (ServerPlayer runner : runners) {
            if (runner.level == source.level && runner.distanceToSqr(source) <= radiusSq) {
                into.add(runner.getUUID());
            }
        }
    }

    /** Drops every bar and every aura member. Called on server stop and on vault end. */
    public static void releaseVault(Vault vault, MinecraftServer server) {
        if (vault == null || !vault.has(Vault.ID)) {
            return;
        }
        UUID vaultId = vault.get(Vault.ID);
        for (VaultChampionState.PlayerState state : VaultChampionState.players(vaultId).values()) {
            if (state.getLiveChampion() != null) {
                VaultChampionKills.removeBar(state.getLiveChampion());
                discardIfPresent(server, state.getLiveChampion());
            }
        }
        VaultChampionState.release(vaultId);
    }

    private static void discardIfPresent(MinecraftServer server, UUID championId) {
        if (server == null) {
            return;
        }
        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(championId);
            if (entity != null) {
                entity.discard();
                return;
            }
        }
    }
}
