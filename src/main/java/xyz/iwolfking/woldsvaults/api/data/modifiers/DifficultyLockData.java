package xyz.iwolfking.woldsvaults.api.data.modifiers;

import iskallia.vault.world.VaultDifficulty;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DifficultyLockData extends SavedData {
    private static final String DATA_NAME = "woldsVaults_DifficultyLockData";

    private final Map<UUID, VaultDifficulty> difficultyMap = new HashMap<>();

    public DifficultyLockData() {}

    public static DifficultyLockData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
            DifficultyLockData::load,
            DifficultyLockData::new,
            DATA_NAME
        );
    }

    public static DifficultyLockData load(CompoundTag tag) {
        DifficultyLockData data = new DifficultyLockData();
        CompoundTag mapTag = tag.getCompound("DifficultyMap");
        for (String key : mapTag.getAllKeys()) {
            try {
                UUID playerId = UUID.fromString(key);
                VaultDifficulty difficulty = VaultDifficulty.valueOf(mapTag.getString(key));
                data.difficultyMap.put(playerId, difficulty);
            } catch (Exception ignored) {}
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag mapTag = new CompoundTag();
        for (Map.Entry<UUID, VaultDifficulty> entry : difficultyMap.entrySet()) {
            mapTag.putString(entry.getKey().toString(), entry.getValue().name());
        }
        tag.put("DifficultyMap", mapTag);
        return tag;
    }

    public void setDifficulty(UUID playerId, VaultDifficulty difficulty) {
        difficultyMap.put(playerId, difficulty);
        setDirty();
    }

    public VaultDifficulty getDifficulty(UUID playerId) {
        return difficultyMap.get(playerId);
    }

    public boolean hasDifficulty(UUID playerId) {
        return difficultyMap.containsKey(playerId);
    }

    public void removeDifficulty(UUID playerId) {
        if (difficultyMap.remove(playerId) != null) {
            setDirty();
        }
    }
}