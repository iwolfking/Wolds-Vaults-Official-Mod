package xyz.iwolfking.woldsvaults.gods.ultimates;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Which players Savior has already revived, one per person per vault; entries are dropped when their vault ends. */
public class UltimateReviveData extends SavedData {
    protected static final String DATA_NAME = "woldsvaults_GodUltimateRevives";

    private final Map<UUID, Set<UUID>> revivedByVault = new HashMap<>();

    private UltimateReviveData() {
    }

    private UltimateReviveData(CompoundTag tag) {
        this.load(tag);
    }

    public static UltimateReviveData get(MinecraftServer server) {
        return server.overworld()
                .getDataStorage()
                .computeIfAbsent(UltimateReviveData::new, UltimateReviveData::new, DATA_NAME);
    }

    /**
     * Claims this player's one Savior revive in this vault; {@code false} means it was already spent and they must
     * not be revived.
     */
    public boolean claim(UUID vaultId, UUID playerId) {
        if (!this.revivedByVault.computeIfAbsent(vaultId, id -> new HashSet<>()).add(playerId)) {
            return false;
        }
        this.setDirty();
        return true;
    }

    public boolean hasClaimed(UUID vaultId, UUID playerId) {
        return this.revivedByVault.getOrDefault(vaultId, Set.of()).contains(playerId);
    }

    public void forgetVault(UUID vaultId) {
        if (this.revivedByVault.remove(vaultId) != null) {
            this.setDirty();
        }
    }

    private void load(CompoundTag tag) {
        ListTag vaults = tag.getList("vaults", Tag.TAG_COMPOUND);
        for (int i = 0; i < vaults.size(); i++) {
            CompoundTag entry = vaults.getCompound(i);
            Set<UUID> revived = new HashSet<>();
            ListTag players = entry.getList("players", Tag.TAG_INT_ARRAY);
            for (int j = 0; j < players.size(); j++) {
                revived.add(net.minecraft.nbt.NbtUtils.loadUUID(players.get(j)));
            }
            this.revivedByVault.put(entry.getUUID("vault"), revived);
        }
    }

    @Override
    @Nonnull
    public CompoundTag save(@Nonnull CompoundTag tag) {
        ListTag vaults = new ListTag();
        this.revivedByVault.forEach((vaultId, revived) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("vault", vaultId);
            ListTag players = new ListTag();
            revived.forEach(playerId -> players.add(net.minecraft.nbt.NbtUtils.createUUID(playerId)));
            entry.put("players", players);
            vaults.add(entry);
        });
        tag.put("vaults", vaults);
        return tag;
    }
}
