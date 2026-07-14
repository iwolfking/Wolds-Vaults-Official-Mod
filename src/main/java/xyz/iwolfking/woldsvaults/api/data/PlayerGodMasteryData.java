package xyz.iwolfking.woldsvaults.api.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Per-player God's Mastery counts, stored as world SavedData keyed by player UUID.
 * Each consumed God's Mastery raises that player's maximum god reputation by one above
 * the base cap of 50 (see {@link xyz.iwolfking.woldsvaults.api.util.GodMasteryHelper}).
 */
public class PlayerGodMasteryData extends SavedData {
    protected static final String DATA_NAME = "woldsvaults_God_Mastery";
    protected Map<UUID, Integer> mastery = new HashMap<>();

    private PlayerGodMasteryData() {
    }

    private PlayerGodMasteryData(CompoundTag tag) {
        this.load(tag);
    }

    public int getMastery(UUID playerId) {
        return this.mastery.getOrDefault(playerId, 0);
    }

    /**
     * Adds one mastery for the given player and returns their new total.
     */
    public int increaseMastery(UUID playerId) {
        int total = this.getMastery(playerId) + 1;
        this.mastery.put(playerId, total);
        this.setDirty();
        return total;
    }

    public void load(CompoundTag tag) {
        this.mastery.clear();
        ListTag players = tag.getList("players", Tag.TAG_COMPOUND);
        for (int i = 0; i < players.size(); i++) {
            CompoundTag playerTag = players.getCompound(i);
            this.mastery.put(playerTag.getUUID("player"), playerTag.getInt("mastery"));
        }
    }

    @Nonnull
    @Override
    public CompoundTag save(@Nonnull CompoundTag tag) {
        ListTag players = new ListTag();
        this.mastery.forEach((playerId, count) -> {
            if (count > 0) {
                CompoundTag playerTag = new CompoundTag();
                playerTag.putUUID("player", playerId);
                playerTag.putInt("mastery", count);
                players.add(playerTag);
            }
        });
        tag.put("players", players);
        return tag;
    }

    public static PlayerGodMasteryData get(ServerLevel level) {
        return get(level.getServer());
    }

    public static PlayerGodMasteryData get(MinecraftServer server) {
        return server.overworld()
                .getDataStorage()
                .computeIfAbsent(PlayerGodMasteryData::new, PlayerGodMasteryData::new, DATA_NAME);
    }
}
