package xyz.iwolfking.woldsvaults.milestones;

import iskallia.vault.core.vault.stat.VaultChestType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-vault, per-player scratch state for the composite milestones, Flawless Victory and the
 * polled-objective baselines. Serialised inside {@link MilestoneData}; dropped when the run ends.
 */
public class MilestoneVaultState {
    private static final Map<UUID, Map<UUID, MilestoneVaultState>> STATES = new ConcurrentHashMap<>();
    private static final Map<UUID, UUID> ACTIVE = new ConcurrentHashMap<>();

    private static volatile boolean scratchDirty;

    private final long[] chests = new long[VaultChestType.values().length];
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

    /** The scratch state of the vault this player is registered against, or null for none. */
    public static MilestoneVaultState current(UUID playerId) {
        UUID vaultId = ACTIVE.get(playerId);
        return vaultId == null ? null : peek(vaultId, playerId);
    }

    /** Whether this player is registered against a vault; outlives the teleport out of it. */
    public static boolean isTracked(UUID playerId) {
        return ACTIVE.containsKey(playerId);
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
        scratchDirty = true;
        Map<UUID, MilestoneVaultState> vault = STATES.remove(vaultId);
        if (vault != null) {
            vault.keySet().forEach(playerId -> ACTIVE.remove(playerId, vaultId));
        }
    }

    /** Drops the registration but keeps the scratch, so rejoining picks the state back up. */
    public static void unregisterPlayer(UUID playerId) {
        scratchDirty = true;
        ACTIVE.remove(playerId);
    }

    /** Replaces every live scratch state with the contents of a milestone save. */
    static void loadAll(ListTag list) {
        STATES.clear();
        ACTIVE.clear();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            if (!tag.hasUUID("vault") || !tag.hasUUID("player")) {
                continue;
            }
            MilestoneVaultState state = new MilestoneVaultState();
            state.load(tag);
            STATES.computeIfAbsent(tag.getUUID("vault"), id -> new ConcurrentHashMap<>())
                    .put(tag.getUUID("player"), state);
        }
    }

    static void reset() {
        STATES.clear();
        ACTIVE.clear();
    }

    static ListTag saveAll() {
        ListTag list = new ListTag();
        STATES.forEach((vaultId, players) -> players.forEach((playerId, state) -> {
            CompoundTag tag = state.save();
            tag.putUUID("vault", vaultId);
            tag.putUUID("player", playerId);
            list.add(tag);
        }));
        return list;
    }

    private CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putLongArray("chests", this.chests);
        tag.putLong("mobKills", this.mobKills);
        tag.putLong("ores", this.ores);
        tag.putLong("treasureDoors", this.treasureDoors);
        tag.putLong("vendoors", this.vendoors);
        tag.putLong("dungeonDoors", this.dungeonDoors);
        tag.putBoolean("damaged", this.damaged);
        tag.putInt("dedicatedPhase", this.dedicatedPhase);
        tag.putLong("dedicatedProgress", this.dedicatedProgress);
        tag.putInt("bingoLines", this.bingoLines);
        tag.putInt("scavengerBingoLines", this.scavengerBingoLines);
        tag.putInt("chaosCompleted", this.chaosCompleted);
        tag.putInt("elixirCollected", this.elixirCollected);
        tag.putInt("runeBossKills", this.runeBossKills);
        tag.putBoolean("baselinesReady", this.baselinesReady);
        return tag;
    }

    private void load(CompoundTag tag) {
        long[] saved = tag.getLongArray("chests");
        System.arraycopy(saved, 0, this.chests, 0, Math.min(saved.length, this.chests.length));
        this.mobKills = tag.getLong("mobKills");
        this.ores = tag.getLong("ores");
        this.treasureDoors = tag.getLong("treasureDoors");
        this.vendoors = tag.getLong("vendoors");
        this.dungeonDoors = tag.getLong("dungeonDoors");
        this.damaged = tag.getBoolean("damaged");
        this.dedicatedPhase = tag.getInt("dedicatedPhase");
        this.dedicatedProgress = tag.getLong("dedicatedProgress");
        this.bingoLines = tag.getInt("bingoLines");
        this.scavengerBingoLines = tag.getInt("scavengerBingoLines");
        this.chaosCompleted = tag.getInt("chaosCompleted");
        this.elixirCollected = tag.getInt("elixirCollected");
        this.runeBossKills = tag.getInt("runeBossKills");
        this.baselinesReady = tag.getBoolean("baselinesReady");
    }

    /** Records one looted vault chest; a chest flagged {@code box} counts for nothing. */
    public void onChestLooted(VaultChestType type, boolean box) {
        if (box) {
            return;
        }
        scratchDirty = true;
        this.chests[type.ordinal()]++;
        this.advanceDedicated(type);
    }

    private void advanceDedicated(VaultChestType type) {
        scratchDirty = true;
        List<VaultChestType> order = MilestoneRegistry.getDedicatedLooterOrder();
        if (this.dedicatedPhase >= order.size() || order.get(this.dedicatedPhase) != type) {
            return;
        }
        this.dedicatedProgress++;
        if (this.dedicatedProgress >= MilestoneRegistry.getDedicatedLooterTarget()) {
            this.dedicatedPhase++;
            this.dedicatedProgress = 0L;
        }
    }

    public boolean isDedicatedLooterDone() {
        return this.dedicatedPhase >= MilestoneRegistry.getDedicatedLooterOrder().size();
    }

    public void onMobKill() {
        scratchDirty = true;
        this.mobKills++;
    }

    public void onOreMined() {
        scratchDirty = true;
        this.ores++;
    }

    public void onTreasureDoor() {
        scratchDirty = true;
        this.treasureDoors++;
    }

    public void onVendoor() {
        scratchDirty = true;
        this.vendoors++;
    }

    public void onDungeonDoor() {
        scratchDirty = true;
        this.dungeonDoors++;
    }

    public void onDamaged() {
        scratchDirty = true;
        this.damaged = true;
    }

    public boolean isFlawless() {
        return !this.damaged;
    }

    /** Whether this run met every "Vault of Vaults" quota. The caller checks vault completion. */
    public boolean isVaultOfVaultsDone() {
        long chestsPerType = MilestoneRegistry.getVaultOfVaultsChestsPerType();
        long doorsPerType = MilestoneRegistry.getVaultOfVaultsDoorsPerType();
        return this.mobKills >= MilestoneRegistry.getVaultOfVaultsMobs()
                && this.chests[VaultChestType.WOODEN.ordinal()] >= chestsPerType
                && this.chests[VaultChestType.GILDED.ordinal()] >= chestsPerType
                && this.chests[VaultChestType.ORNATE.ordinal()] >= chestsPerType
                && this.chests[VaultChestType.LIVING.ordinal()] >= chestsPerType
                && this.treasureDoors >= doorsPerType
                && this.vendoors >= doorsPerType
                && this.dungeonDoors >= doorsPerType
                && this.ores >= MilestoneRegistry.getVaultOfVaultsOres();
    }

    public boolean areBaselinesReady() {
        return this.baselinesReady;
    }

    public void markBaselinesReady() {
        scratchDirty = true;
        this.baselinesReady = true;
    }

    public int takeBingoDelta(int total) {
        scratchDirty = true;
        int delta = Math.max(0, total - this.bingoLines);
        this.bingoLines = Math.max(this.bingoLines, total);
        return delta;
    }

    public int takeScavengerBingoDelta(int total) {
        scratchDirty = true;
        int delta = Math.max(0, total - this.scavengerBingoLines);
        this.scavengerBingoLines = Math.max(this.scavengerBingoLines, total);
        return delta;
    }

    public int takeChaosDelta(int total) {
        scratchDirty = true;
        int delta = Math.max(0, total - this.chaosCompleted);
        this.chaosCompleted = Math.max(this.chaosCompleted, total);
        return delta;
    }

    public int takeElixirDelta(int total) {
        scratchDirty = true;
        int delta = Math.max(0, total - this.elixirCollected);
        this.elixirCollected = Math.max(this.elixirCollected, total);
        return delta;
    }

    public int takeRuneBossDelta(int total) {
        scratchDirty = true;
        int delta = Math.max(0, total - this.runeBossKills);
        this.runeBossKills = Math.max(this.runeBossKills, total);
        return delta;
    }

    /** Whether the scratch has changed since this was last called, clearing the flag. */
    static boolean consumeDirty() {
        boolean dirty = scratchDirty;
        scratchDirty = false;
        return dirty;
    }
}
