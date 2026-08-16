package xyz.iwolfking.woldsvaults.gods;

import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.world.data.PlayerReputationData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
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
import xyz.iwolfking.woldsvaults.gods.network.GodNetwork;
import xyz.iwolfking.woldsvaults.prestige.GodExperiencePrestigePower;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * World SavedData holding every player's alignment with each of the four Vault Gods: accumulated
 * XP (level is always derived from it, never stored), the ledger of god points spent per tree
 * node, granted bonus points, lifetime god-altar completions and the player's minor-transfer-slot
 * selections. Changes on the server are pushed to the owning player with
 * {@link GodAlignmentSyncMessage}; {@link ClientGodAlignmentData} mirrors them client-side.
 */
public class GodAlignmentData extends SavedData {
    protected static final String DATA_NAME = "woldsvaults_GodAlignment";

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

    /**
     * Piety with a god: ten per point of base god reputation plus twenty per god alignment level.
     * Reputation is read from the base mod's {@link PlayerReputationData}, whose cap the addon
     * already raises per player through God's Mastery. Server side only — this reads world saved
     * data, so client display must be fed by a synced value rather than by calling this.
     */
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

    public GodState getState(UUID playerId, VaultGod god) {
        return this.players.computeIfAbsent(playerId, id -> new EnumMap<>(VaultGod.class))
                .computeIfAbsent(god, g -> new GodState());
    }

    public long getXp(UUID playerId, VaultGod god) {
        return this.getState(playerId, god).xp;
    }

    public int getLevel(UUID playerId, VaultGod god) {
        return GodLevels.levelForXp(this.getState(playerId, god).xp);
    }

    public int getSpentPoints(UUID playerId, VaultGod god) {
        int spent = 0;
        for (int points : this.getState(playerId, god).spentPoints.values()) {
            spent += points;
        }
        return spent;
    }

    public int getTotalPoints(UUID playerId, VaultGod god) {
        return GodLevels.totalPointsForLevel(this.getLevel(playerId, god)) + this.getState(playerId, god).bonusPoints;
    }

    public int getUnspentPoints(UUID playerId, VaultGod god) {
        return this.getTotalPoints(playerId, god) - this.getSpentPoints(playerId, god);
    }

    /**
     * The spent-point ledger for a tree, node id to points invested. Wave-2 node content reads
     * this to decide which nodes are unlocked and at what tier.
     */
    public Map<String, Integer> getSpentLedger(UUID playerId, VaultGod god) {
        return Collections.unmodifiableMap(this.getState(playerId, god).spentPoints);
    }

    public int getPointsIn(UUID playerId, VaultGod god, String nodeId) {
        return this.getState(playerId, god).spentPoints.getOrDefault(nodeId, 0);
    }

    public int getMinorTransferSlots(UUID playerId, VaultGod god) {
        return GodLevels.minorTransferSlots(this.getLevel(playerId, god));
    }

    public List<String> getMinorTransfers(UUID playerId, VaultGod god) {
        return Collections.unmodifiableList(this.getState(playerId, god).minorTransfers);
    }

    public int getAltarCompletions(UUID playerId, VaultGod god) {
        return this.getState(playerId, god).altarCompletions;
    }

    /**
     * Lifetime god-altar completions across all four gods — the {@code n} of the altar XP formula.
     */
    public int getTotalAltarCompletions(UUID playerId) {
        int total = 0;
        for (VaultGod god : VaultGod.values()) {
            total += this.getAltarCompletions(playerId, god);
        }
        return total;
    }

    /**
     * Adds god XP, promoting through as many levels as the amount covers and firing one
     * {@link GodLevelUpEvent} per level gained. Returns the number of levels gained. The amount is
     * scaled by the player's God's Disciple prestige powers before it is banked, so every award
     * path that funnels through here picks the bonus up.
     */
    public int addGodXp(ServerPlayer player, VaultGod god, long amount) {
        if (amount <= 0L) {
            return 0;
        }
        amount = applyGodExperiencePowers(player, amount);
        GodState state = this.getState(player.getUUID(), god);
        int before = GodLevels.levelForXp(state.xp);
        state.xp += amount;
        int after = GodLevels.levelForXp(state.xp);
        this.setDirty();
        for (int level = before + 1; level <= after; level++) {
            MinecraftForge.EVENT_BUS.post(new GodLevelUpEvent(player, god, level));
        }
        this.sync(player);
        return after - before;
    }

    /**
     * Scales a god XP award by the owned God's Disciple prestige powers. The powers add together
     * before the multiply, so the shipped +20% and +15% ranks give +35% rather than +38%. Rounds up
     * so that small awards still move by at least the full base amount.
     */
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

    /**
     * Forces a god level by rewriting accumulated XP to that level's threshold. Fires
     * {@link GodLevelUpEvent} for every level crossed upwards; downgrades fire nothing.
     */
    public void setLevel(ServerPlayer player, VaultGod god, int level) {
        GodState state = this.getState(player.getUUID(), god);
        int before = GodLevels.levelForXp(state.xp);
        state.xp = GodLevels.xpForLevel(Math.max(level, 0));
        this.setDirty();
        for (int gained = before + 1; gained <= level; gained++) {
            MinecraftForge.EVENT_BUS.post(new GodLevelUpEvent(player, god, gained));
        }
        this.sync(player);
    }

    public void addBonusPoints(ServerPlayer player, VaultGod god, int amount) {
        GodState state = this.getState(player.getUUID(), god);
        state.bonusPoints = Math.max(state.bonusPoints + amount, -GodLevels.totalPointsForLevel(GodLevels.levelForXp(state.xp)));
        this.setDirty();
        this.sync(player);
    }

    /**
     * Invests points into a node. Fails without side effects when the player has fewer unspent
     * points than requested.
     */
    public boolean spendPoints(ServerPlayer player, VaultGod god, String nodeId, int points) {
        if (points <= 0 || this.getUnspentPoints(player.getUUID(), god) < points) {
            return false;
        }
        GodState state = this.getState(player.getUUID(), god);
        state.spentPoints.merge(nodeId, points, Integer::sum);
        this.setDirty();
        this.sync(player);
        return true;
    }

    /**
     * Returns points invested in a node back to the player's pool. Fails without side effects
     * when the node holds fewer points than requested.
     */
    public boolean refundPoints(ServerPlayer player, VaultGod god, String nodeId, int points) {
        GodState state = this.getState(player.getUUID(), god);
        int invested = state.spentPoints.getOrDefault(nodeId, 0);
        if (points <= 0 || invested < points) {
            return false;
        }
        if (invested == points) {
            state.spentPoints.remove(nodeId);
        } else {
            state.spentPoints.put(nodeId, invested - points);
        }
        this.setDirty();
        this.sync(player);
        return true;
    }

    public void refundAll(ServerPlayer player, VaultGod god) {
        this.getState(player.getUUID(), god).spentPoints.clear();
        this.setDirty();
        this.sync(player);
    }

    /**
     * Binds a foreign minor node to one of this god's minor-transfer slots. Fails when every slot
     * this god has unlocked is already taken, or when the node is already bound.
     */
    public boolean addMinorTransfer(ServerPlayer player, VaultGod god, String nodeId) {
        GodState state = this.getState(player.getUUID(), god);
        if (state.minorTransfers.contains(nodeId)) {
            return false;
        }
        if (state.minorTransfers.size() >= this.getMinorTransferSlots(player.getUUID(), god)) {
            return false;
        }
        state.minorTransfers.add(nodeId);
        this.setDirty();
        this.sync(player);
        return true;
    }

    public boolean removeMinorTransfer(ServerPlayer player, VaultGod god, String nodeId) {
        if (!this.getState(player.getUUID(), god).minorTransfers.remove(nodeId)) {
            return false;
        }
        this.setDirty();
        this.sync(player);
        return true;
    }

    public int incrementAltarCompletions(UUID playerId, VaultGod god) {
        GodState state = this.getState(playerId, god);
        state.altarCompletions++;
        this.setDirty();
        return state.altarCompletions;
    }

    public void sync(ServerPlayer player) {
        GodNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                new GodAlignmentSyncMessage(this.players.getOrDefault(player.getUUID(), new EnumMap<>(VaultGod.class))));
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
        return tag;
    }

    /** One player's alignment with one god. Level is derived from {@link #xp}, never stored. */
    public static class GodState {
        public long xp;
        public int bonusPoints;
        public int altarCompletions;
        public final Map<String, Integer> spentPoints = new LinkedHashMap<>();
        public final List<String> minorTransfers = new ArrayList<>();

        public static GodState fromNbt(CompoundTag tag) {
            GodState state = new GodState();
            state.xp = tag.getLong("xp");
            state.bonusPoints = tag.getInt("bonus_points");
            state.altarCompletions = tag.getInt("altar_completions");
            ListTag spent = tag.getList("spent", Tag.TAG_COMPOUND);
            for (int i = 0; i < spent.size(); i++) {
                CompoundTag entry = spent.getCompound(i);
                state.spentPoints.put(entry.getString("node"), entry.getInt("points"));
            }
            ListTag transfers = tag.getList("minor_transfers", Tag.TAG_STRING);
            for (int i = 0; i < transfers.size(); i++) {
                state.minorTransfers.add(transfers.getString(i));
            }
            return state;
        }

        public CompoundTag toNbt() {
            CompoundTag tag = new CompoundTag();
            tag.putLong("xp", this.xp);
            tag.putInt("bonus_points", this.bonusPoints);
            tag.putInt("altar_completions", this.altarCompletions);
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
            return tag;
        }
    }
}
