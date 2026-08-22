package xyz.iwolfking.woldsvaults.milestones;

import iskallia.vault.core.vault.stat.VaultChestType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-vault, per-player scratch state for the milestones that cannot be expressed as a plain
 * global counter: the two composite "in one vault" milestones, Flawless Victory, and the
 * baselines used to turn polled objective totals into deltas.
 *
 * <p>The scratch is keyed by (vault, player) and lives exactly as long as the run does: it is
 * created on join, kept across a logout or a restart that happens mid-run, and destroyed only
 * when the player leaves the vault or the vault ends. It rides along in {@link MilestoneData}
 * rather than in its own store, because everything it feeds is judged on the way out of a vault
 * and would otherwise be silently reset by a relog.</p>
 *
 * <p>The numbers the two composites are judged against are not held here: they belong to the
 * milestones, so they come from {@link MilestoneRegistry} and are pack config like every other
 * milestone threshold.</p>
 */
public class MilestoneVaultState {
    private static final Map<UUID, Map<UUID, MilestoneVaultState>> STATES = new ConcurrentHashMap<>();
    private static final Map<UUID, UUID> ACTIVE = new ConcurrentHashMap<>();

    private static volatile boolean scratchDirty;

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

    /**
     * Whether the engine currently has this player registered against a vault. This is the fast
     * path of the milestone vault gate, and it answers the registration rather than the scratch
     * state, so it stays true for the window in which a listener is registered but its state has
     * not been created yet.
     *
     * <p>The registration is made on {@code LISTENER_JOIN}, refreshed on every listener tick, and
     * dropped only by {@link #release} or {@link #unregisterPlayer}. It deliberately
     * outlives the teleport out of the vault dimension: {@code LISTENER_LEAVE} fires after the
     * vault's own leave logic has already sent the player home, and every end-of-run milestone -
     * Vaults Hunted, Fail Vaults, Explorer, Seen It All, Flawless Victory - is awarded from
     * there.</p>
     */
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

    /**
     * Drops the "in a vault right now" registration without touching the scratch behind it. This
     * is the logout path: the run is still going, so the damage flag Flawless Victory reads, the
     * composite tallies and the polled baselines all have to be waiting intact when the player
     * comes back. Rejoining re-registers them against the same vault and {@link #get} hands back
     * the state they left.
     */
    public static void unregisterPlayer(UUID playerId) {
        scratchDirty = true;
        ACTIVE.remove(playerId);
    }

    /**
     * Replaces every live scratch state with the contents of a milestone save. Called only from
     * {@link MilestoneData}'s load, so a restart taken mid-run resumes with the tallies it had.
     */
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
        tag.putLong("woodenBoxes", this.woodenBoxes);
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
        this.woodenBoxes = tag.getLong("woodenBoxes");
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

    public void onChestLooted(VaultChestType type, boolean box) {
        scratchDirty = true;
        if (box) {
            this.woodenBoxes++;
            return;
        }
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

    /**
     * "Complete an Impressive Vault": the configured mob kills, that many of each of the four
     * normal chests, that many of every door type and that many ores, all inside this one run. The
     * objective completion itself is checked by the caller.
     */
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

    public long getWoodenBoxes() {
        return this.woodenBoxes;
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

    /**
     * Whether the scratch has changed since this was last called, clearing the flag.
     *
     * <p>The scratch is serialised into {@link MilestoneData} but is not one of its fields, so its
     * mutators cannot set that save's own pending flag. Without this the scratch was written under
     * a dirty flag none of its writers touched, and only reached disk when an unrelated counter
     * happened to move in the same window - so a run that advanced nothing, a bail with no
     * progress, never persisted its own release and left the entry behind across a restart.
     */
    static boolean consumeDirty() {
        boolean dirty = scratchDirty;
        scratchDirty = false;
        return dirty;
    }
}
