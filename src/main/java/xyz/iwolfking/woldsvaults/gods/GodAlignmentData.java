package xyz.iwolfking.woldsvaults.gods;

import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.world.data.PlayerReputationData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.PrestigePowerHelper;
import xyz.iwolfking.woldsvaults.gods.event.GodLevelUpEvent;
import xyz.iwolfking.woldsvaults.gods.network.GodAlignmentSyncMessage;
import xyz.iwolfking.woldsvaults.network.NetworkHandler;
import xyz.iwolfking.woldsvaults.prestige.GodExperiencePrestigePower;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * World SavedData holding every player's alignment with each Vault God: accumulated XP (level derives
 * from it, never stored), the god-point ledger, bonus points, altar completions and the positional
 * minor-transfer slots. God points come from levels, from raw reputation and from the bonus ledger.
 * Changes are pushed to the owning player with {@link GodAlignmentSyncMessage}.
 */
public class GodAlignmentData extends SavedData {
    protected static final String DATA_NAME = "woldsvaults_GodAlignment";

    /** 1 renumbered the sacrifice gates: gate 0 (Initiation) stopped granting a level of its own. */
    private static final int DATA_VERSION = 1;

    /** What a read of a player and god with no record yet returns. Shared, and never written to. */
    private static final GodState EMPTY = new GodState();

    private static final Set<String> WARNED_STALE_TRANSFERS = ConcurrentHashMap.newKeySet();

    private static final Map<String, Long> LAST_UNINITIATED_NOTICE = new ConcurrentHashMap<>();
    private static final long UNINITIATED_NOTICE_INTERVAL_MS = 30_000L;

    private final Map<UUID, EnumMap<VaultGod, GodState>> players = new HashMap<>();

    private GodAlignmentData() {
    }

    private GodAlignmentData(CompoundTag tag) {
        this.load(tag);
    }

    public static GodAlignmentData get(MinecraftServer server) {
        return server.overworld()
                .getDataStorage()
                .computeIfAbsent(GodAlignmentData::new, GodAlignmentData::new, DATA_NAME);
    }

    public static GodAlignmentData get(ServerLevel level) {
        return get(level.getServer());
    }

    /** Piety: 10 per reputation point, 20 per god level, plus {@link PietyBonusSource}. Server side only. */
    public static int piety(Player player, VaultGod god) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            WoldsVaults.LOGGER.warn("GodAlignmentData.piety called with no server available for {}; returning 0.",
                    player.getGameProfile().getName());
            return 0;
        }
        int reputation = PlayerReputationData.getReputation(player.getUUID(), god);
        return 10 * reputation + 20 * get(server).getLevel(player.getUUID(), god)
                + PietyBonusSource.total(player, god);
    }

    /** A player's standing with one god, without creating a record; the shared {@link #EMPTY} is read-only. */
    public GodState getState(UUID playerId, VaultGod god) {
        EnumMap<VaultGod, GodState> states = this.players.get(playerId);
        GodState state = states == null ? null : states.get(god);
        return state == null ? EMPTY : state;
    }

    private GodState mutableState(UUID playerId, VaultGod god) {
        return this.players.computeIfAbsent(playerId, id -> new EnumMap<>(VaultGod.class))
                .computeIfAbsent(god, g -> new GodState());
    }

    public long getXp(UUID playerId, VaultGod god) {
        return this.getState(playerId, god).xp;
    }

    public int getLevel(UUID playerId, VaultGod god) {
        GodState state = this.getState(playerId, god);
        return GodLevels.gatedLevel(state.xp, state.sacrifices);
    }

    public int getSacrifices(UUID playerId, VaultGod god) {
        return this.getState(playerId, god).sacrifices;
    }

    /** Completes one sacrifice gate, firing a {@link GodLevelUpEvent} per level the XP had already paid for. */
    public void completeSacrifice(ServerPlayer player, VaultGod god) {
        GodState state = this.mutableState(player.getUUID(), god);
        int before = GodLevels.gatedLevel(state.xp, state.sacrifices);
        state.sacrifices++;
        int after = GodLevels.gatedLevel(state.xp, state.sacrifices);
        this.setDirty();
        for (int level = before + 1; level <= after; level++) {
            MinecraftForge.EVENT_BUS.post(new GodLevelUpEvent(player, god, level));
        }
        this.sync(player);
    }

    public int getSpentPoints(UUID playerId, VaultGod god) {
        return this.getState(playerId, god).spentPoints();
    }

    public int getTotalPoints(UUID playerId, VaultGod god) {
        return this.getState(playerId, god).totalPoints(this.getLevel(playerId, god), reputationPoints(playerId, god));
    }

    public int getUnspentPoints(UUID playerId, VaultGod god) {
        return this.getState(playerId, god).unspentPoints(this.getLevel(playerId, god), reputationPoints(playerId, god));
    }

    /** The god points raw reputation with {@code god} grants on top of the ones their levels do. */
    public static int reputationPoints(UUID playerId, VaultGod god) {
        return GodLevels.reputationPoints(PlayerReputationData.getReputation(playerId, god));
    }

    /** The spent-point ledger for a tree: effect key to points invested, not tree-node positions. */
    public Map<String, Integer> getSpentLedger(UUID playerId, VaultGod god) {
        return Collections.unmodifiableMap(this.getState(playerId, god).spentPoints);
    }

    public int getPointsIn(UUID playerId, VaultGod god, String ledgerKey) {
        return this.getState(playerId, god).spentPoints.getOrDefault(ledgerKey, 0);
    }

    public int getMinorTransferSlots(UUID playerId, VaultGod god) {
        return GodLevels.minorTransferSlots(this.getLevel(playerId, god));
    }

    /** The effect ids {@code god}'s slots carry; a stale entry reads empty and is dropped on the next write. */
    public List<String> getMinorTransfers(UUID playerId, VaultGod god) {
        GodState state = this.getState(playerId, god);
        return MinorTransferSlots.liveTransfers(god, state, this.getMinorTransferSlots(playerId, god), id -> {
            if (WARNED_STALE_TRANSFERS.add(playerId + "/" + god.getName() + "/" + id)) {
                WoldsVaults.LOGGER.warn("Ignoring {}'s {} transfer slot entry '{}': it is not a learned minor star of "
                        + "that god. It will be dropped the next time those slots are written.", playerId, god.getName(), id);
            }
        });
    }

    /** The raw content of one transfer slot; empty for a hole, a slot never written, or one not granted. */
    public Optional<String> getMinorTransferSlot(UUID playerId, VaultGod god, int slot) {
        List<String> slots = this.getState(playerId, god).minorTransfers;
        if (slot < 0 || slot >= slots.size() || slots.get(slot).isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(slots.get(slot));
    }

    /** The slot index holding {@code effectId} in {@code god}'s transfer slots, or -1. */
    public int findMinorTransferSlot(UUID playerId, VaultGod god, String effectId) {
        if (effectId == null || effectId.isEmpty()) {
            return -1;
        }
        return this.getState(playerId, god).minorTransfers.indexOf(effectId);
    }

    public int getAltarCompletions(UUID playerId, VaultGod god) {
        return this.getState(playerId, god).altarCompletions;
    }

    /** Lifetime god-altar completions across all four gods - the {@code n} of the altar XP formula. */
    public int getTotalAltarCompletions(UUID playerId) {
        int total = 0;
        for (VaultGod god : VaultGod.values()) {
            total += this.getAltarCompletions(playerId, god);
        }
        return total;
    }

    /** Whether the player has completed a god's Initiation offering, the first sacrifice gate. */
    public boolean hasInitiated(UUID playerId, VaultGod god) {
        return this.getState(playerId, god).sacrifices > 0;
    }

    /** Adds god XP, refused entirely until the god's Initiation offering is made; returns levels gained. */
    public int addGodXp(ServerPlayer player, VaultGod god, long amount) {
        if (amount <= 0L) {
            return 0;
        }
        if (!this.hasInitiated(player.getUUID(), god)) {
            reportUninitiated(player, god, amount);
            return 0;
        }
        return this.grantGodXp(player, god, amount);
    }

    /** Banks god XP without the initiation gate - god altars and commands; scaled by God's Disciple powers. */
    public int grantGodXp(ServerPlayer player, VaultGod god, long amount) {
        if (amount <= 0L) {
            return 0;
        }
        amount = applyGodExperiencePowers(player, amount);
        GodState state = this.mutableState(player.getUUID(), god);
        int before = GodLevels.gatedLevel(state.xp, state.sacrifices);
        state.xp += amount;
        int after = GodLevels.gatedLevel(state.xp, state.sacrifices);
        this.setDirty();
        for (int level = before + 1; level <= after; level++) {
            MinecraftForge.EVENT_BUS.post(new GodLevelUpEvent(player, god, level));
        }
        this.sync(player);
        return after - before;
    }

    /** Tells the player why an award was dropped, at most once per player and god per 30 seconds. */
    private static void reportUninitiated(ServerPlayer player, VaultGod god, long amount) {
        String key = player.getUUID() + "/" + god.getName();
        long now = System.currentTimeMillis();
        Long last = LAST_UNINITIATED_NOTICE.get(key);
        if (last != null && now - last < UNINITIATED_NOTICE_INTERVAL_MS) {
            return;
        }
        LAST_UNINITIATED_NOTICE.put(key, now);
        player.displayClientMessage(new TextComponent(god.getName()
                + " does not know your name yet - make the Initiation offering at the Greed Cauldron.")
                .withStyle(god.getChatColor()), true);
        WoldsVaults.LOGGER.info("Dropped {} {} XP for {}: the Initiation offering has not been made.",
                amount, god.getName(), player.getGameProfile().getName());
    }

    /** The amount {@link #addGodXp} would bank for {@code amount} raw experience, without banking it. */
    public long previewScaledXp(ServerPlayer player, long amount) {
        return applyGodExperiencePowers(player, amount);
    }

    /** Scales a god XP award by the God's Disciple powers, which add before the multiply. Rounds up. */
    private static long applyGodExperiencePowers(ServerPlayer player, long amount) {
        if (player.getServer() == null) {
            WoldsVaults.LOGGER.debug("God XP awarded with no server attached; skipping God's Disciple scaling.");
            return amount;
        }
        float bonus = 0.0F;
        for (GodExperiencePrestigePower power : PrestigePowerHelper.getPrestigePowersOfType(player, GodExperiencePrestigePower.class)) {
            bonus += power.getExperienceIncrease();
        }
        if (bonus <= 0.0F) {
            return amount;
        }
        return (long) Math.ceil((double) amount * (1.0D + (double) bonus));
    }

    /** Forces a god level, granting the sacrifice gates it needs; only levels gained fire an event. */
    public void setLevel(ServerPlayer player, VaultGod god, int level) {
        GodState state = this.mutableState(player.getUUID(), god);
        int before = GodLevels.gatedLevel(state.xp, state.sacrifices);
        state.xp = GodLevels.xpForLevel(Math.max(level, 0));
        state.sacrifices = Math.max(state.sacrifices, GodLevels.sacrificesForLevel(level));
        this.setDirty();
        for (int gained = before + 1; gained <= level; gained++) {
            MinecraftForge.EVENT_BUS.post(new GodLevelUpEvent(player, god, gained));
        }
        this.sync(player);
    }

    /** Grants or removes bonus god points, floored at what the player's gated level grants. */
    public void addBonusPoints(ServerPlayer player, VaultGod god, int amount) {
        GodState state = this.mutableState(player.getUUID(), god);
        state.bonusPoints = Math.max(state.bonusPoints + amount,
                -(GodLevels.totalPointsForLevel(GodLevels.gatedLevel(state.xp, state.sacrifices))
                        + reputationPoints(player.getUUID(), god)));
        this.setDirty();
        this.sync(player);
    }

    /** Gives a god's whole constellation back: points, owned positions and the transfer slots. */
    public void refundAll(ServerPlayer player, VaultGod god) {
        GodState state = this.mutableState(player.getUUID(), god);
        state.spentPoints.clear();
        state.treeNodes.clear();
        state.minorTransfers.clear();
        this.setDirty();
        this.sync(player);
    }

    public Set<String> getPurchasedTreeNodes(UUID playerId, VaultGod god) {
        return Collections.unmodifiableSet(this.getState(playerId, god).treeNodes);
    }

    public boolean isTreeNodePurchased(UUID playerId, VaultGod god, String nodeId) {
        return this.getState(playerId, god).treeNodes.contains(nodeId);
    }

    /** Buys one tree node, banking its cost under the ledger key; graph validation belongs to the caller. */
    public boolean purchaseTreeNode(ServerPlayer player, VaultGod god, String nodeId, String ledgerKey, int cost) {
        GodState state = this.mutableState(player.getUUID(), god);
        if (state.treeNodes.contains(nodeId)) {
            return false;
        }
        if (cost <= 0 || this.getUnspentPoints(player.getUUID(), god) < cost) {
            return false;
        }
        state.treeNodes.add(nodeId);
        state.spentPoints.merge(ledgerKey, cost, Integer::sum);
        this.setDirty();
        this.sync(player);
        return true;
    }

    /**
     * Writes one of {@code god}'s transfer slots, leaving any other slot {@code effectId} occupied; null
     * or empty clears it and stale entries are dropped. Returns whether anything changed.
     */
    public boolean setMinorTransfer(ServerPlayer player, VaultGod god, int slot, @Nullable String effectId) {
        if (slot < 0 || slot >= GodLevels.maxMinorTransferSlots()) {
            return false;
        }
        String id = effectId == null ? "" : effectId;
        GodState state = this.mutableState(player.getUUID(), god);
        List<String> slots = state.minorTransfers;
        while (slots.size() <= slot) {
            slots.add("");
        }
        boolean changed = false;
        for (int i = 0; i < slots.size(); i++) {
            String current = slots.get(i);
            if (current.isEmpty()) {
                continue;
            }
            boolean duplicate = i != slot && current.equals(id);
            if (duplicate || !MinorTransferSlots.isTransferable(god, current, state.spentPoints)) {
                slots.set(i, "");
                changed = true;
            }
        }
        if (!slots.get(slot).equals(id)) {
            slots.set(slot, id);
            changed = true;
        }
        if (!changed) {
            return false;
        }
        this.setDirty();
        this.sync(player);
        return true;
    }

    public int incrementAltarCompletions(UUID playerId, VaultGod god) {
        GodState state = this.mutableState(playerId, god);
        state.altarCompletions++;
        this.setDirty();
        return state.altarCompletions;
    }

    public void sync(ServerPlayer player) {
        EnumMap<VaultGod, Integer> pietyByGod = new EnumMap<>(VaultGod.class);
        EnumMap<VaultGod, Integer> reputationByGod = new EnumMap<>(VaultGod.class);
        for (VaultGod god : VaultGod.values()) {
            pietyByGod.put(god, piety(player, god));
            reputationByGod.put(god, PlayerReputationData.getReputation(player.getUUID(), god));
        }
        NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                new GodAlignmentSyncMessage(this.players.getOrDefault(player.getUUID(), new EnumMap<>(VaultGod.class)),
                        pietyByGod, reputationByGod));
    }

    public void load(CompoundTag tag) {
        this.players.clear();
        ListTag playerList = tag.getList("players", Tag.TAG_COMPOUND);
        for (int i = 0; i < playerList.size(); i++) {
            CompoundTag playerTag = playerList.getCompound(i);
            UUID playerId = playerTag.getUUID("player");
            EnumMap<VaultGod, GodState> states = new EnumMap<>(VaultGod.class);
            ListTag godList = playerTag.getList("gods", Tag.TAG_COMPOUND);
            for (int j = 0; j < godList.size(); j++) {
                CompoundTag godTag = godList.getCompound(j);
                VaultGod god = VaultGod.fromName(godTag.getString("god"));
                if (god == null) {
                    WoldsVaults.LOGGER.error("Dropping god alignment state for unknown god '{}' on player {}.",
                            godTag.getString("god"), playerId);
                    continue;
                }
                states.put(god, GodState.fromNbt(godTag));
            }
            this.players.put(playerId, states);
        }
        this.migrate(tag.getInt("version"));
    }

    /**
     * Brings saved state up to {@link #DATA_VERSION}. Version 0 counted the Initiation as the gate that opened
     * level 1, so every player who had completed any gate is credited one more; a player who had completed none
     * still has none.
     */
    private void migrate(int version) {
        if (version >= DATA_VERSION) {
            return;
        }
        int migrated = 0;
        for (EnumMap<VaultGod, GodState> states : this.players.values()) {
            for (GodState state : states.values()) {
                if (state.sacrifices > 0) {
                    state.sacrifices++;
                    migrated++;
                }
            }
        }
        this.setDirty();
        WoldsVaults.LOGGER.info("Migrated god alignment data from version {} to {}: {} god sacrifice counts "
                + "credited one gate so the Initiation no longer grants a level of its own.",
                version, DATA_VERSION, migrated);
    }

    @Nonnull
    @Override
    public CompoundTag save(@Nonnull CompoundTag tag) {
        ListTag playerList = new ListTag();
        this.players.forEach((playerId, states) -> {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("player", playerId);
            ListTag godList = new ListTag();
            states.forEach((god, state) -> {
                CompoundTag godTag = state.toNbt();
                godTag.putString("god", god.getName());
                godList.add(godTag);
            });
            playerTag.put("gods", godList);
            playerList.add(playerTag);
        });
        tag.put("players", playerList);
        tag.putInt("version", DATA_VERSION);
        return tag;
    }

    /** One player's alignment with one god; level derives from {@link #xp} capped by {@link #sacrifices}. */
    public static class GodState {
        public long xp;
        public int bonusPoints;
        public int altarCompletions;
        public int sacrifices;
        public final Map<String, Integer> spentPoints = new LinkedHashMap<>();
        public final List<String> minorTransfers = new ArrayList<>();
        public final Set<String> treeNodes = new LinkedHashSet<>();

        public int spentPoints() {
            int spent = 0;
            for (int points : this.spentPoints.values()) {
                spent += points;
            }
            return spent;
        }

        /** Points {@code level} grants, plus the reputation-granted and bonus points on top. */
        public int totalPoints(int level, int reputationPoints) {
            return GodLevels.totalPointsForLevel(level) + reputationPoints + this.bonusPoints;
        }

        public int unspentPoints(int level, int reputationPoints) {
            return this.totalPoints(level, reputationPoints) - this.spentPoints();
        }

        public static GodState fromNbt(CompoundTag tag) {
            GodState state = new GodState();
            state.xp = tag.getLong("xp");
            state.bonusPoints = tag.getInt("bonus_points");
            state.altarCompletions = tag.getInt("altar_completions");
            state.sacrifices = tag.contains("sacrifices", Tag.TAG_ANY_NUMERIC)
                    ? tag.getInt("sacrifices")
                    : GodLevels.levelForXp(state.xp);
            ListTag spent = tag.getList("spent", Tag.TAG_COMPOUND);
            for (int i = 0; i < spent.size(); i++) {
                CompoundTag entry = spent.getCompound(i);
                state.spentPoints.put(entry.getString("node"), entry.getInt("points"));
            }
            ListTag transfers = tag.getList("minor_transfers", Tag.TAG_STRING);
            for (int i = 0; i < transfers.size(); i++) {
                state.minorTransfers.add(transfers.getString(i));
            }
            ListTag purchased = tag.getList("tree_nodes", Tag.TAG_STRING);
            for (int i = 0; i < purchased.size(); i++) {
                state.treeNodes.add(purchased.getString(i));
            }
            return state;
        }

        public CompoundTag toNbt() {
            CompoundTag tag = new CompoundTag();
            tag.putLong("xp", this.xp);
            tag.putInt("bonus_points", this.bonusPoints);
            tag.putInt("altar_completions", this.altarCompletions);
            tag.putInt("sacrifices", this.sacrifices);
            ListTag spent = new ListTag();
            this.spentPoints.forEach((node, points) -> {
                CompoundTag entry = new CompoundTag();
                entry.putString("node", node);
                entry.putInt("points", points);
                spent.add(entry);
            });
            tag.put("spent", spent);
            ListTag transfers = new ListTag();
            this.minorTransfers.forEach(node -> transfers.add(StringTag.valueOf(node)));
            tag.put("minor_transfers", transfers);
            ListTag purchased = new ListTag();
            this.treeNodes.forEach(node -> purchased.add(StringTag.valueOf(node)));
            tag.put("tree_nodes", purchased);
            return tag;
        }
    }
}
