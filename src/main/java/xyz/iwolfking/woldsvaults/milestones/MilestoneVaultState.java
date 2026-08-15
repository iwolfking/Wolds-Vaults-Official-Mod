package xyz.iwolfking.woldsvaults.milestones;

import iskallia.vault.core.vault.stat.VaultChestType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-vault, per-player scratch state for the milestones that cannot be expressed as a plain
 * global counter: the two composite "in one vault" milestones, Flawless Victory, and the
 * baselines used to turn polled objective totals into deltas.
 */
public class MilestoneVaultState {
    public static final long VAULT_OF_VAULTS_MOBS = 10_000L;
    public static final long VAULT_OF_VAULTS_CHESTS = 1_000L;
    public static final long VAULT_OF_VAULTS_DOORS = 10L;
    public static final long VAULT_OF_VAULTS_ORES = 1_000L;
    public static final long DEDICATED_LOOTER_TARGET = 25_000L;

    private static final Map<UUID, Map<UUID, MilestoneVaultState>> STATES = new ConcurrentHashMap<>();
    private static final Map<UUID, UUID> ACTIVE = new ConcurrentHashMap<>();

    private final long[] chests = new long[VaultChestType.values().length];
    private long woodenBoxes;
    private long mobKills;
    private long ores;
    private long treasureDoors;
    private long vendoors;
    private long dungeonDoors;
    private boolean damaged;

    private int dedicatedPhase;
    private long dedicatedProgress;

    private int bingoLines;
    private int scavengerBingoLines;
    private int chaosCompleted;
    private int elixirCollected;
    private int runeBossKills;
    private boolean baselinesReady;

    public static MilestoneVaultState get(UUID vaultId, UUID playerId) {
        ACTIVE.put(playerId, vaultId);
        return STATES.computeIfAbsent(vaultId, id -> new ConcurrentHashMap<>())
                .computeIfAbsent(playerId, id -> new MilestoneVaultState());
    }

    public static MilestoneVaultState peek(UUID vaultId, UUID playerId) {
        Map<UUID, MilestoneVaultState> vault = STATES.get(vaultId);
        return vault == null ? null : vault.get(playerId);
    }

    /**
     * O(1) "is this player inside a vault right now" lookup, and the vault scratch state if so.
     * Used as the cheap gate on the hot per-kill and per-damage paths so they never have to walk
     * the server's vault list.
     */
    public static MilestoneVaultState current(UUID playerId) {
        UUID vaultId = ACTIVE.get(playerId);
        return vaultId == null ? null : peek(vaultId, playerId);
    }

    public static void release(UUID vaultId, UUID playerId) {
        ACTIVE.remove(playerId, vaultId);
        Map<UUID, MilestoneVaultState> vault = STATES.get(vaultId);
        if (vault != null) {
            vault.remove(playerId);
            if (vault.isEmpty()) {
                STATES.remove(vaultId);
            }
        }
    }

    public static void release(UUID vaultId) {
        Map<UUID, MilestoneVaultState> vault = STATES.remove(vaultId);
        if (vault != null) {
            vault.keySet().forEach(playerId -> ACTIVE.remove(playerId, vaultId));
        }
    }

    public static void releasePlayer(UUID playerId) {
        UUID vaultId = ACTIVE.remove(playerId);
        if (vaultId != null) {
            Map<UUID, MilestoneVaultState> vault = STATES.get(vaultId);
            if (vault != null) {
                vault.remove(playerId);
            }
        }
    }

    public void onChestLooted(VaultChestType type, boolean box) {
        if (box) {
            this.woodenBoxes++;
            return;
        }
        this.chests[type.ordinal()]++;
        this.advanceDedicated(type);
    }

    private void advanceDedicated(VaultChestType type) {
        VaultChestType expected = dedicatedOrder(this.dedicatedPhase);
        if (expected == null || expected != type) {
            return;
        }
        this.dedicatedProgress++;
        if (this.dedicatedProgress >= DEDICATED_LOOTER_TARGET) {
            this.dedicatedPhase++;
            this.dedicatedProgress = 0L;
        }
    }

    private static VaultChestType dedicatedOrder(int phase) {
        return switch (phase) {
            case 0 -> VaultChestType.WOODEN;
            case 1 -> VaultChestType.GILDED;
            case 2 -> VaultChestType.ORNATE;
            case 3 -> VaultChestType.LIVING;
            default -> null;
        };
    }

    public boolean isDedicatedLooterDone() {
        return this.dedicatedPhase >= 4;
    }

    public void onMobKill() {
        this.mobKills++;
    }

    public void onOreMined() {
        this.ores++;
    }

    public void onTreasureDoor() {
        this.treasureDoors++;
    }

    public void onVendoor() {
        this.vendoors++;
    }

    public void onDungeonDoor() {
        this.dungeonDoors++;
    }

    public void onDamaged() {
        this.damaged = true;
    }

    public boolean isFlawless() {
        return !this.damaged;
    }

    /**
     * "Complete an Impressive Vault": 10k mobs, 1k of each of the four normal chests, 10 of every
     * door type, 1000 ores. The objective completion itself is checked by the caller.
     */
    public boolean isVaultOfVaultsDone() {
        return this.mobKills >= VAULT_OF_VAULTS_MOBS
                && this.chests[VaultChestType.WOODEN.ordinal()] >= VAULT_OF_VAULTS_CHESTS
                && this.chests[VaultChestType.GILDED.ordinal()] >= VAULT_OF_VAULTS_CHESTS
                && this.chests[VaultChestType.ORNATE.ordinal()] >= VAULT_OF_VAULTS_CHESTS
                && this.chests[VaultChestType.LIVING.ordinal()] >= VAULT_OF_VAULTS_CHESTS
                && this.treasureDoors >= VAULT_OF_VAULTS_DOORS
                && this.vendoors >= VAULT_OF_VAULTS_DOORS
                && this.dungeonDoors >= VAULT_OF_VAULTS_DOORS
                && this.ores >= VAULT_OF_VAULTS_ORES;
    }

    public long getWoodenBoxes() {
        return this.woodenBoxes;
    }

    public boolean areBaselinesReady() {
        return this.baselinesReady;
    }

    public void markBaselinesReady() {
        this.baselinesReady = true;
    }

    public int takeBingoDelta(int total) {
        int delta = Math.max(0, total - this.bingoLines);
        this.bingoLines = Math.max(this.bingoLines, total);
        return delta;
    }

    public int takeScavengerBingoDelta(int total) {
        int delta = Math.max(0, total - this.scavengerBingoLines);
        this.scavengerBingoLines = Math.max(this.scavengerBingoLines, total);
        return delta;
    }

    public int takeChaosDelta(int total) {
        int delta = Math.max(0, total - this.chaosCompleted);
        this.chaosCompleted = Math.max(this.chaosCompleted, total);
        return delta;
    }

    public int takeElixirDelta(int total) {
        int delta = Math.max(0, total - this.elixirCollected);
        this.elixirCollected = Math.max(this.elixirCollected, total);
        return delta;
    }

    public int takeRuneBossDelta(int total) {
        int delta = Math.max(0, total - this.runeBossKills);
        this.runeBossKills = Math.max(this.runeBossKills, total);
        return delta;
    }
}
