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

import java.util.ArrayList;
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
    private static final int ADOPTION_INTERVAL_TICKS = 100;
    private static final int MISSING_GRACE_TICKS = 6000;

    private static boolean rankCoverageChecked;

    private VaultChampionManager() {
    }

    public static void tick(MinecraftServer server) {
        GreedChampionConfig config = VaultChampion.config();
        verifyRankCoverage(config);
        int tick = server.getTickCount();
        if (tick % AURA_INTERVAL_TICKS == 0) {
            recomputeAura(server, config);
        }
        int manageInterval = Math.max(1, config.getHunt().managerIntervalTicks);
        int rollInterval = Math.max(1, config.getRage().rollIntervalTicks);
        boolean manage = tick % manageInterval == 0;
        boolean roll = tick % rollInterval == 0;
        boolean adopt = tick % ADOPTION_INTERVAL_TICKS == 0;
        if (!manage && !roll && !adopt) {
            return;
        }
        for (ServerLevel level : server.getAllLevels()) {
            Vault vault = VaultUtils.getVault(level).orElse(null);
            if (vault == null || !vault.has(Vault.ID)) {
                continue;
            }
            UUID vaultId = vault.get(Vault.ID);
            if (adopt) {
                adoptUnmanaged(vault, vaultId, level);
            }
            Map<UUID, VaultChampionState.PlayerState> players = VaultChampionState.players(vaultId);
            if (players.isEmpty()) {
                continue;
            }
            boolean bossFight = roll && bossFightInProgress(level);
            for (Map.Entry<UUID, VaultChampionState.PlayerState> entry : players.entrySet()) {
                VaultChampionState.PlayerState state = entry.getValue();
                if (manage) {
                    state.decay(config.getRage().decayPerTick * manageInterval);
                    tickChampion(vault, level, state, config, manageInterval);
                }
                if (roll && !bossFight) {
                    rollArm(vault, level, entry.getKey(), state, config);
                }
            }
        }
    }

    /**
     * Warns once a session when the two spawn gates disagree. {@code greed_medallions.json} decides
     * which ranks may summon a Champion and {@code gods/greed_champion.json} decides what one is made
     * of; a rank that passes the first and is missing from the second builds rage to its threshold and
     * then rolls forever against a spawn that can never happen, with nothing said in play.
     */
    private static void verifyRankCoverage(GreedChampionConfig config) {
        if (rankCoverageChecked) {
            return;
        }
        rankCoverageChecked = true;
        List<Integer> missing = new ArrayList<>();
        for (GreedMedallionTier tier : GreedMedallionTier.values()) {
            if (tier.vaultChampionHunts() && config.getRank(tier.getRankIndex()) == null) {
                missing.add(tier.getRankIndex());
            }
        }
        if (!missing.isEmpty()) {
            WoldsVaults.LOGGER.error("greed_medallions.json lets medallion ranks {} summon a Vault Champion but "
                    + "gods/greed_champion.json has no stat block for them - those ranks build rage and never spawn "
                    + "anything. Add a ranks entry for each, or raise gates.vaultChampionHunts.", missing);
        }
    }

    /** Clears the state that only holds for one server session. */
    public static void resetSessionState() {
        rankCoverageChecked = false;
    }

    /**
     * Re-adopts any Champion standing in the vault that no player state points at.
     *
     * <p>A Champion outlives the map that tracks it. It is persistent and it is saved with the world,
     * so a server restart - or anything else that empties {@code VaultChampionState} - leaves a live
     * boss in a vault with no record of it: no bar, no leash, no orphan countdown, and nothing to stop
     * the next roll arming a second one. Everything needed to rebuild that record rides the entity, so
     * the fix is to read it back rather than to write a saved-data file for what is otherwise a purely
     * within-run counter.
     *
     * <p>Run on its own slow cadence and only while somebody is in the level, because a Champion in an
     * unloaded chunk is not there to be found: the sweep has to keep coming back until the region
     * around it loads.</p>
     */
    private static void adoptUnmanaged(Vault vault, UUID vaultId, ServerLevel level) {
        if (level.players().isEmpty()) {
            return;
        }
        List<? extends TheVesselEntity> loose = level.getEntities(
                EntityTypeTest.<Entity, TheVesselEntity>forClass(TheVesselEntity.class),
                champion -> champion.isAlive() && VaultChampion.isChampion(champion));
        if (loose.isEmpty()) {
            return;
        }
        Set<UUID> tracked = new HashSet<>();
        for (VaultChampionState.PlayerState state : VaultChampionState.players(vaultId).values()) {
            if (state.getLiveChampion() != null) {
                tracked.add(state.getLiveChampion());
            }
        }
        for (TheVesselEntity champion : loose) {
            if (!tracked.contains(champion.getUUID())) {
                adopt(vault, vaultId, champion, tracked);
            }
        }
    }

    /**
     * Rebuilds one Champion's bookkeeping from its own NBT: the summoner owns the record, the rank and
     * the damage pool are already on the entity, and the hunt target is re-resolved by the next manager
     * tick like any other. What cannot be recovered is the summoner's champion-kill count, so their
     * next threshold escalation starts over - the same thing a restart does to rage itself.
     *
     * <p>Anything that cannot be bookkept is discarded rather than left roaming. A Champion with no
     * damage pool can never be defeated, and one with no summoner has nobody to settle rage against;
     * both would be permanent unmanaged bosses.</p>
     */
    private static void adopt(Vault vault, UUID vaultId, TheVesselEntity champion, Set<UUID> tracked) {
        UUID stampedVault = VaultChampion.getVaultId(champion);
        if (stampedVault != null && !stampedVault.equals(vaultId)) {
            WoldsVaults.LOGGER.warn("Discarding a Vault Champion stamped for vault {} that turned up in vault {}.",
                    stampedVault, vaultId);
            champion.discard();
            return;
        }
        UUID summoner = VaultChampion.getSummoner(champion);
        if (summoner == null || VaultChampion.getTier(champion).isEmpty()
                || VaultChampionKills.poolOf(champion) <= 0.0D) {
            WoldsVaults.LOGGER.error("Discarding an unmanageable Vault Champion in vault {}: it carries no summoner, "
                    + "no medallion rank or no damage pool, so it could never be defeated.", vaultId);
            champion.discard();
            return;
        }
        VaultChampionState.PlayerState state = VaultChampionState.get(vaultId, summoner);
        if (state == null) {
            return;
        }
        if (state.getLiveChampion() != null) {
            WoldsVaults.LOGGER.warn("Discarding a duplicate Vault Champion in vault {}: {} already has a live one.",
                    vaultId, summoner);
            champion.discard();
            return;
        }
        state.setLiveChampion(champion.getUUID());
        tracked.add(champion.getUUID());
        VaultChampionSpawner.applySize(champion, VaultChampion.config().getScaling().sizeMultiplier);
        VaultChampionKills.syncBar(vault, champion);
        WoldsVaults.LOGGER.info("Re-adopted a Vault Champion in vault {} summoned by {}: {} of {} damage already "
                        + "dealt to it.", vaultId, summoner, (long) VaultChampionKills.dealtOf(champion),
                (long) VaultChampionKills.poolOf(champion));
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

    /**
     * One manager tick for one player's Champion.
     *
     * <p>An entity that does not resolve is not a fight that ended. The usual reason is that its chunk
     * is not loaded, and the record is what keeps the vault from arming a second Champion while the
     * first is merely out of sight; the grace period is only there so a record whose entity is
     * genuinely gone - removed by an operator, lost with a corrupt region file - does not lock the
     * player out of Champions for the rest of the run. One that comes back after its record is dropped
     * is re-adopted by the next sweep.</p>
     */
    private static void tickChampion(Vault vault, ServerLevel level, VaultChampionState.PlayerState state,
                                     GreedChampionConfig config, int interval) {
        UUID championId = state.getLiveChampion();
        if (championId == null) {
            return;
        }
        Entity entity = level.getEntity(championId);
        if (!(entity instanceof TheVesselEntity champion) || !champion.isAlive()) {
            int missing = state.noteChampionMissing(interval);
            if (missing == interval) {
                WoldsVaults.LOGGER.debug("Vault Champion {} is not loaded; holding its record until it is.",
                        championId);
            }
            if (missing >= MISSING_GRACE_TICKS) {
                WoldsVaults.LOGGER.warn("Vault Champion {} has not been loaded for {} ticks - dropping its record. "
                        + "It is re-adopted if the entity turns up again.", championId, missing);
                VaultChampionKills.closeBar(vault);
                state.setLiveChampion(null);
            }
            return;
        }
        state.noteChampionPresent();
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
     * Rolls once every {@code rage.rollIntervalTicks} to arm a spawn. It never spawns directly: the roll only sets a flag, and
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

    /**
     * Drops every bar and every aura member, and takes every Champion in the vault with it. The sweep
     * is by tag rather than by record: a Champion the state lost track of - one that was unloaded when
     * its record aged out, or that a restart orphaned - is exactly the one that must not be left behind
     * in a world that is about to be deleted.
     */
    public static void releaseVault(Vault vault, MinecraftServer server) {
        if (vault == null || !vault.has(Vault.ID)) {
            return;
        }
        UUID vaultId = vault.get(Vault.ID);
        Set<UUID> discarded = sweepChampions(server, vaultId);
        for (VaultChampionState.PlayerState state : VaultChampionState.players(vaultId).values()) {
            UUID championId = state.getLiveChampion();
            if (championId != null && !discarded.contains(championId) && discardIfPresent(server, championId)) {
                WoldsVaults.LOGGER.warn("Vault Champion {} had to be discarded by id: no loaded level resolved back "
                        + "to vault {} for the sweep.", championId, vaultId);
            }
        }
        VaultChampionKills.closeBar(vault);
        VaultChampionState.release(vaultId);
    }

    /** Discards every tagged Champion standing in the vault's level, and answers which ones those were. */
    private static Set<UUID> sweepChampions(MinecraftServer server, UUID vaultId) {
        Set<UUID> discarded = new HashSet<>();
        if (server == null) {
            return discarded;
        }
        for (ServerLevel level : server.getAllLevels()) {
            Vault levelVault = VaultUtils.getVault(level).orElse(null);
            if (levelVault == null || !levelVault.has(Vault.ID) || !vaultId.equals(levelVault.get(Vault.ID))) {
                continue;
            }
            for (TheVesselEntity champion : level.getEntities(
                    EntityTypeTest.<Entity, TheVesselEntity>forClass(TheVesselEntity.class),
                    VaultChampion::isChampion)) {
                champion.discard();
                discarded.add(champion.getUUID());
            }
        }
        return discarded;
    }

    private static boolean discardIfPresent(MinecraftServer server, UUID championId) {
        if (server == null) {
            return false;
        }
        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(championId);
            if (entity != null) {
                entity.discard();
                return true;
            }
        }
        return false;
    }
}
