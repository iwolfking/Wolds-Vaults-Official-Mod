package xyz.iwolfking.woldsvaults.medallions.champion;

import iskallia.vault.core.vault.Vault;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-vault, per-player rage and champion bookkeeping.
 *
 * <p>Nothing is shared between players and nothing is shared between vaults: every runner has their
 * own rage counter, their own escalating threshold and their own Champion, and all of it is
 * discarded when the vault ends. That is the same in-memory contract
 * {@code GreedMedallionVaultState} and {@code VaultClockRate} keep - a vault that outlives a server
 * restart simply starts its rage over, which is the right failure for a within-run counter.
 *
 * <p>The Champion itself is not in-memory in that sense. It persists in the world, so a restart that
 * empties this map leaves a live boss with no record; {@code VaultChampionManager} re-adopts it from
 * the entity's own NBT rather than letting the vault treat it as gone and arm another.
 *
 * <p>The entries are held by vault id rather than by level so a finished vault's
 * {@code VirtualWorld} is never kept alive by this map.</p>
 */
public final class VaultChampionState {
    private static final Map<UUID, Map<UUID, PlayerState>> VAULTS = new ConcurrentHashMap<>();

    private VaultChampionState() {
    }

    /** One player's standing in one vault. */
    public static final class PlayerState {
        private double rage;
        private int championKills;
        private boolean armed;
        private UUID liveChampion;
        private int missingTicks;

        public double getRage() {
            return this.rage;
        }

        public int getChampionKills() {
            return this.championKills;
        }

        public boolean isArmed() {
            return this.armed;
        }

        public void setArmed(boolean armed) {
            this.armed = armed;
        }

        public UUID getLiveChampion() {
            return this.liveChampion;
        }

        public void setLiveChampion(UUID liveChampion) {
            this.liveChampion = liveChampion;
            this.missingTicks = 0;
        }

        /**
         * Books one manager tick on which the live Champion did not resolve to an entity, and answers
         * how long that has been true for. An unloaded chunk is the ordinary reason: the record is
         * kept so the vault cannot arm a second Champion while the first is simply out of sight.
         */
        public int noteChampionMissing(int ticks) {
            this.missingTicks += Math.max(0, ticks);
            return this.missingTicks;
        }

        public void noteChampionPresent() {
            this.missingTicks = 0;
        }

        public void addRage(double amount) {
            this.rage = Math.max(0.0D, this.rage + amount);
        }

        public void decay(double amount) {
            this.rage = Math.max(0.0D, this.rage - amount);
        }

        /**
         * Ends one Champion: the summoner's rage returns to zero and their next Champion costs more.
         * Called on defeat regardless of who landed the killing blow - the pool belongs to whoever
         * summoned it, not to whoever finished it.
         */
        public void onChampionDefeated() {
            this.rage = 0.0D;
            this.armed = false;
            this.liveChampion = null;
            this.missingTicks = 0;
            this.championKills++;
        }

        public double threshold(double base, double multiplierPerKill) {
            return base * Math.pow(multiplierPerKill, this.championKills);
        }
    }

    public static PlayerState get(UUID vaultId, UUID playerId) {
        if (vaultId == null || playerId == null) {
            return null;
        }
        return VAULTS.computeIfAbsent(vaultId, id -> new ConcurrentHashMap<>())
                .computeIfAbsent(playerId, id -> new PlayerState());
    }

    public static PlayerState get(Vault vault, UUID playerId) {
        return vault == null || !vault.has(Vault.ID) ? null : get(vault.get(Vault.ID), playerId);
    }

    /** The live entries for one vault, or an empty map. Never creates. */
    public static Map<UUID, PlayerState> players(UUID vaultId) {
        Map<UUID, PlayerState> players = vaultId == null ? null : VAULTS.get(vaultId);
        return players == null ? Map.of() : players;
    }

    public static Iterable<Map.Entry<UUID, Map<UUID, PlayerState>>> all() {
        return VAULTS.entrySet();
    }

    public static void release(UUID vaultId) {
        if (vaultId != null) {
            VAULTS.remove(vaultId);
        }
    }

    public static void clearAll() {
        VAULTS.clear();
    }

    /** The vault a level belongs to, or null when the level is not a vault at all. */
    public static Vault vaultOf(Level level) {
        return iskallia.vault.core.vault.VaultUtils.getVault(level).orElse(null);
    }
}
