package xyz.iwolfking.woldsvaults.gods.sacrifice;

import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * World SavedData for the Greed Cauldron: each player's selected god and, per god, the item counts fed
 * toward that god's current gate. Deposits are one-way, and completing a sacrifice clears that god's
 * progress. Which gate it counts toward comes from {@code GodAlignmentData}'s completed-sacrifice count.
 */
public class GodSacrificeData extends SavedData {
    protected static final String DATA_NAME = "woldsvaults_GodSacrifices";

    private final Map<UUID, PlayerProgress> players = new HashMap<>();

    private GodSacrificeData() {
    }

    private GodSacrificeData(CompoundTag tag) {
        this.load(tag);
    }

    public static GodSacrificeData get(MinecraftServer server) {
        return server.overworld().getDataStorage()
                .computeIfAbsent(GodSacrificeData::new, GodSacrificeData::new, DATA_NAME);
    }

    public static GodSacrificeData get(ServerLevel level) {
        return get(level.getServer());
    }

    @Nullable
    public VaultGod getSelectedGod(UUID playerId) {
        PlayerProgress progress = this.players.get(playerId);
        return progress == null ? null : progress.selectedGod;
    }

    public void setSelectedGod(UUID playerId, VaultGod god) {
        this.progress(playerId).selectedGod = god;
        this.setDirty();
    }

    public Map<ResourceLocation, Integer> getProgress(UUID playerId, VaultGod god) {
        return Collections.unmodifiableMap(this.deposits(playerId, god));
    }

    public int getDeposited(UUID playerId, VaultGod god, ResourceLocation item) {
        return this.deposits(playerId, god).getOrDefault(item, 0);
    }

    /**
     * Feeds {@code count} of an item toward a god's gate, clamped to what it still needs; returns how many were
     * consumed.
     */
    public int deposit(UUID playerId, VaultGod god, GodSacrifices.Gate gate, ResourceLocation item, int count) {
        int required = 0;
        for (GodSacrifices.Entry entry : gate.entries()) {
            if (entry.item().equals(item)) {
                required = entry.count();
                break;
            }
        }
        if (required <= 0 || count <= 0) {
            return 0;
        }
        Map<ResourceLocation, Integer> deposits = this.progress(playerId).deposits
                .computeIfAbsent(god, g -> new LinkedHashMap<>());
        int have = deposits.getOrDefault(item, 0);
        int accepted = Math.min(count, required - have);
        if (accepted <= 0) {
            return 0;
        }
        deposits.put(item, have + accepted);
        this.setDirty();
        return accepted;
    }

    public boolean isGateComplete(UUID playerId, VaultGod god, GodSacrifices.Gate gate) {
        Map<ResourceLocation, Integer> deposits = this.deposits(playerId, god);
        for (GodSacrifices.Entry entry : gate.entries()) {
            if (deposits.getOrDefault(entry.item(), 0) < entry.count()) {
                return false;
            }
        }
        return true;
    }

    public void clearProgress(UUID playerId, VaultGod god) {
        PlayerProgress progress = this.players.get(playerId);
        if (progress != null && progress.deposits.remove(god) != null) {
            this.setDirty();
        }
    }

    private PlayerProgress progress(UUID playerId) {
        return this.players.computeIfAbsent(playerId, id -> new PlayerProgress());
    }

    private Map<ResourceLocation, Integer> deposits(UUID playerId, VaultGod god) {
        PlayerProgress progress = this.players.get(playerId);
        if (progress == null) {
            return Map.of();
        }
        return progress.deposits.getOrDefault(god, Map.of());
    }

    public void load(CompoundTag tag) {
        this.players.clear();
        ListTag playerList = tag.getList("players", Tag.TAG_COMPOUND);
        for (int i = 0; i < playerList.size(); i++) {
            CompoundTag playerTag = playerList.getCompound(i);
            PlayerProgress progress = new PlayerProgress();
            if (playerTag.contains("selected", Tag.TAG_STRING)) {
                progress.selectedGod = VaultGod.fromName(playerTag.getString("selected"));
            }
            ListTag godList = playerTag.getList("gods", Tag.TAG_COMPOUND);
            for (int j = 0; j < godList.size(); j++) {
                CompoundTag godTag = godList.getCompound(j);
                VaultGod god = VaultGod.fromName(godTag.getString("god"));
                if (god == null) {
                    WoldsVaults.LOGGER.error("Dropping sacrifice progress for unknown god '{}'.", godTag.getString("god"));
                    continue;
                }
                Map<ResourceLocation, Integer> deposits = new LinkedHashMap<>();
                ListTag items = godTag.getList("items", Tag.TAG_COMPOUND);
                for (int k = 0; k < items.size(); k++) {
                    CompoundTag itemTag = items.getCompound(k);
                    deposits.put(ResourceLocation.parse(itemTag.getString("item")), itemTag.getInt("count"));
                }
                progress.deposits.put(god, deposits);
            }
            this.players.put(playerTag.getUUID("player"), progress);
        }
    }

    @Nonnull
    @Override
    public CompoundTag save(@Nonnull CompoundTag tag) {
        ListTag playerList = new ListTag();
        this.players.forEach((playerId, progress) -> {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("player", playerId);
            if (progress.selectedGod != null) {
                playerTag.putString("selected", progress.selectedGod.getName());
            }
            ListTag godList = new ListTag();
            progress.deposits.forEach((god, deposits) -> {
                CompoundTag godTag = new CompoundTag();
                godTag.putString("god", god.getName());
                ListTag items = new ListTag();
                deposits.forEach((item, count) -> {
                    CompoundTag itemTag = new CompoundTag();
                    itemTag.putString("item", item.toString());
                    itemTag.putInt("count", count);
                    items.add(itemTag);
                });
                godTag.put("items", items);
                godList.add(godTag);
            });
            playerTag.put("gods", godList);
            playerList.add(playerTag);
        });
        tag.put("players", playerList);
        return tag;
    }

    private static class PlayerProgress {
        @Nullable
        private VaultGod selectedGod;
        private final EnumMap<VaultGod, Map<ResourceLocation, Integer>> deposits = new EnumMap<>(VaultGod.class);
    }
}
