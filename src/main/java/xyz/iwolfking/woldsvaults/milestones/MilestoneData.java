package xyz.iwolfking.woldsvaults.milestones;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The single world-level store for milestone progress. Counters are monotonic longs kept in
 * memory and only marked for persistence on a throttle (see {@link MilestoneFlusher}) or on the
 * events that must never lose progress, so that millions of increments cost no save traffic.
 */
public class MilestoneData extends SavedData {
    protected static final String DATA_NAME = "woldsvaults_Milestones";

    private static volatile MinecraftServer CACHED_SERVER;
    private static volatile MilestoneData CACHED;

    private final Map<UUID, Map<String, Long>> counters = new HashMap<>();
    private final Map<UUID, Map<String, Set<String>>> tokens = new HashMap<>();
    private final Map<UUID, Map<String, Double>> fractions = new HashMap<>();
    private final Map<UUID, Set<String>> pendingSync = new HashMap<>();
    private boolean pendingSave;

    private MilestoneData() {
    }

    private MilestoneData(CompoundTag tag) {
        this.load(tag);
    }

    public long getValue(UUID playerId, String milestoneId) {
        Map<String, Long> player = this.counters.get(playerId);
        return player == null ? 0L : player.getOrDefault(milestoneId, 0L);
    }

    public void setValue(UUID playerId, String milestoneId, long value) {
        this.counters.computeIfAbsent(playerId, id -> new HashMap<>()).put(milestoneId, value);
        this.pendingSync.computeIfAbsent(playerId, id -> new LinkedHashSet<>()).add(milestoneId);
        this.pendingSave = true;
    }

    public Map<String, Long> getAllValues(UUID playerId) {
        Map<String, Long> player = this.counters.get(playerId);
        return player == null ? Map.of() : Collections.unmodifiableMap(player);
    }

    public Set<String> getTokens(UUID playerId, String milestoneId) {
        Map<String, Set<String>> player = this.tokens.get(playerId);
        if (player == null) {
            return Set.of();
        }
        return player.getOrDefault(milestoneId, Set.of());
    }

    public boolean addToken(UUID playerId, String milestoneId, String token) {
        boolean added = this.tokens.computeIfAbsent(playerId, id -> new HashMap<>())
                .computeIfAbsent(milestoneId, id -> new LinkedHashSet<>())
                .add(token);
        if (added) {
            this.pendingSave = true;
        }
        return added;
    }

    /**
     * Adds a fractional amount and returns the whole units that have accumulated. The remainder
     * is deliberately transient: losing under one unit on a restart is irrelevant, and keeping it
     * out of NBT keeps the save shape simple.
     */
    public long addFraction(UUID playerId, String milestoneId, double amount) {
        Map<String, Double> player = this.fractions.computeIfAbsent(playerId, id -> new HashMap<>());
        double total = player.getOrDefault(milestoneId, 0.0D) + amount;
        long whole = (long) Math.floor(total);
        player.put(milestoneId, total - whole);
        return whole;
    }

    public Map<UUID, Set<String>> drainPendingSync() {
        if (this.pendingSync.isEmpty()) {
            return Map.of();
        }
        Map<UUID, Set<String>> drained = new HashMap<>(this.pendingSync);
        this.pendingSync.clear();
        return drained;
    }

    public void clearPendingSync(UUID playerId) {
        this.pendingSync.remove(playerId);
    }

    public boolean hasPendingSave() {
        return this.pendingSave;
    }

    /**
     * Marks the store for persistence and clears the throttle flag. Called on tier crossings, on
     * the periodic flush, and unconditionally on vault leave, vault end, logout and server stop.
     */
    public void flush() {
        if (this.pendingSave) {
            this.pendingSave = false;
            this.setDirty();
        }
    }

    public void markPendingSave() {
        this.pendingSave = true;
    }

    public void load(CompoundTag tag) {
        this.counters.clear();
        this.tokens.clear();
        ListTag players = tag.getList("players", Tag.TAG_COMPOUND);
        for (int i = 0; i < players.size(); i++) {
            CompoundTag playerTag = players.getCompound(i);
            UUID playerId = playerTag.getUUID("player");
            Map<String, Long> values = new HashMap<>();
            CompoundTag valuesTag = playerTag.getCompound("counters");
            for (String key : valuesTag.getAllKeys()) {
                values.put(key, valuesTag.getLong(key));
            }
            if (!values.isEmpty()) {
                this.counters.put(playerId, values);
            }
            CompoundTag tokensTag = playerTag.getCompound("tokens");
            Map<String, Set<String>> playerTokens = new HashMap<>();
            for (String key : tokensTag.getAllKeys()) {
                ListTag list = tokensTag.getList(key, Tag.TAG_STRING);
                Set<String> set = new LinkedHashSet<>();
                for (int j = 0; j < list.size(); j++) {
                    set.add(list.getString(j));
                }
                playerTokens.put(key, set);
            }
            if (!playerTokens.isEmpty()) {
                this.tokens.put(playerId, playerTokens);
            }
        }
    }

    @Nonnull
    @Override
    public CompoundTag save(@Nonnull CompoundTag tag) {
        ListTag players = new ListTag();
        Set<UUID> ids = new HashSet<>(this.counters.keySet());
        ids.addAll(this.tokens.keySet());
        for (UUID playerId : ids) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("player", playerId);
            CompoundTag valuesTag = new CompoundTag();
            this.counters.getOrDefault(playerId, Map.of()).forEach((key, value) -> {
                if (value != 0L) {
                    valuesTag.putLong(key, value);
                }
            });
            playerTag.put("counters", valuesTag);
            CompoundTag tokensTag = new CompoundTag();
            this.tokens.getOrDefault(playerId, Map.of()).forEach((key, set) -> {
                ListTag list = new ListTag();
                set.forEach(token -> list.add(StringTag.valueOf(token)));
                tokensTag.put(key, list);
            });
            playerTag.put("tokens", tokensTag);
            players.add(playerTag);
        }
        tag.put("players", players);
        return tag;
    }

    public static MilestoneData get(ServerLevel level) {
        return get(level.getServer());
    }

    public static void invalidate() {
        CACHED_SERVER = null;
        CACHED = null;
    }

    /**
     * Cached per running server: this is on the per-kill and per-damage hot paths, and the
     * overworld's data storage lives exactly as long as the server instance does.
     */
    public static MilestoneData get(MinecraftServer server) {
        MilestoneData cached = CACHED;
        if (cached != null && CACHED_SERVER == server) {
            return cached;
        }
        MilestoneData data = server.overworld()
                .getDataStorage()
                .computeIfAbsent(MilestoneData::new, MilestoneData::new, DATA_NAME);
        CACHED_SERVER = server;
        CACHED = data;
        return data;
    }
}
