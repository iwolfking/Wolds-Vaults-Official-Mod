package xyz.iwolfking.woldsvaults.milestones.trials;

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

/** Persisted table of which live vaults are rank-up trials, and for which rank, by {@code Vault.ID}. */
public class GreedTrialData extends SavedData {
    protected static final String DATA_NAME = "woldsvaults_GreedTrials";

    private final Map<UUID, Integer> trials = new HashMap<>();

    private GreedTrialData() {
    }

    private GreedTrialData(CompoundTag tag) {
        this.load(tag);
    }

    public static GreedTrialData get(MinecraftServer server) {
        return server.overworld()
                .getDataStorage()
                .computeIfAbsent(GreedTrialData::new, GreedTrialData::new, DATA_NAME);
    }

    public void mark(UUID vaultId, int targetRank) {
        this.trials.put(vaultId, targetRank);
        this.setDirty();
    }

    /** The rank this vault is a trial for, or 0 when it is an ordinary vault. */
    public int getTargetRank(UUID vaultId) {
        return vaultId == null ? 0 : this.trials.getOrDefault(vaultId, 0);
    }

    /** A copy of every marked vault id, safe to iterate while clearing entries. */
    public Set<UUID> getMarkedVaults() {
        return new HashSet<>(this.trials.keySet());
    }

    public void clear(UUID vaultId) {
        if (vaultId != null && this.trials.remove(vaultId) != null) {
            this.setDirty();
        }
    }

    public void load(CompoundTag tag) {
        this.trials.clear();
        ListTag list = tag.getList("trials", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            this.trials.put(entry.getUUID("vault"), entry.getInt("rank"));
        }
    }

    @Nonnull
    @Override
    public CompoundTag save(@Nonnull CompoundTag tag) {
        ListTag list = new ListTag();
        this.trials.forEach((vaultId, rank) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("vault", vaultId);
            entry.putInt("rank", rank);
            list.add(entry);
        });
        tag.put("trials", list);
        return tag;
    }
}
