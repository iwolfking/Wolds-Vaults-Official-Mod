package xyz.iwolfking.woldsvaults.api.core.competition;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import xyz.iwolfking.woldsvaults.api.core.competition.lib.RewardBundle;

import java.util.*;

public class PlayerRewardStorage extends SavedData {

    private static final String DATA_NAME = "woldsvaults_player_rewards";
    private static PlayerRewardStorage instance;


    private final Map<UUID, List<RewardBundle>> rewards = new HashMap<>();

    public static PlayerRewardStorage get(MinecraftServer server) {
        if (instance == null) {
            instance = server.overworld()
                    .getDataStorage()
                    .computeIfAbsent(
                            PlayerRewardStorage::load,
                            PlayerRewardStorage::new,
                            DATA_NAME
                    );
        }
        return instance;
    }

    public void addReward(UUID player, RewardBundle bundle) {
        rewards.computeIfAbsent(player, k -> new ArrayList<>()).add(bundle);
        setDirty();
    }

    public List<RewardBundle> getRewards(UUID player) {
        return rewards.getOrDefault(player, Collections.emptyList());
    }

    public boolean hasRewards(UUID player) {
        return rewards.containsKey(player) && !rewards.get(player).isEmpty();
    }

    public boolean claimItem(UUID player, RewardBundle bundle, ItemStack stack) {
        List<RewardBundle> bundles = rewards.get(player);
        if (bundles == null) return false;

        if (!bundles.contains(bundle)) return false;

        boolean removed = bundle.remove(stack);
        if (!removed) return false;

        if (bundle.getItems().isEmpty()) {
            bundles.remove(bundle);
        }

        if (bundles.isEmpty()) {
            rewards.remove(player);
        }

        setDirty();
        return true;
    }


    public void clearRewards(UUID player) {
        rewards.remove(player);
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag players = new ListTag();

        rewards.forEach((uuid, bundles) -> {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("Player", uuid);

            ListTag bundleList = new ListTag();
            for (RewardBundle bundle : bundles) {
                bundleList.add(bundle.save());
            }

            playerTag.put("Bundles", bundleList);
            players.add(playerTag);
        });

        tag.put("Players", players);
        return tag;
    }

    public static PlayerRewardStorage load(CompoundTag tag) {
        PlayerRewardStorage storage = new PlayerRewardStorage();

        ListTag players = tag.getList("Players", Tag.TAG_COMPOUND);
        for (int i = 0; i < players.size(); i++) {
            CompoundTag playerTag = players.getCompound(i);
            UUID uuid = playerTag.getUUID("Player");

            ListTag bundleList = playerTag.getList("Bundles", Tag.TAG_COMPOUND);
            List<RewardBundle> bundles = new ArrayList<>();
            for (int j = 0; j < bundleList.size(); j++) {
                bundles.add(RewardBundle.load(bundleList.getCompound(j)));
            }

            storage.rewards.put(uuid, bundles);
        }

        return storage;
    }
}
