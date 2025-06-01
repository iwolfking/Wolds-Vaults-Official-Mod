package xyz.iwolfking.woldsvaults.data.skill;

import iskallia.vault.skill.base.GroupedSkill;
import iskallia.vault.skill.base.LearnableSkill;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.SkillContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.server.ServerLifecycleHooks;
import xyz.iwolfking.woldsvaults.gui.menus.divinity.DivinityTree;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDivinityStatData extends SavedData {
    private static final String DATA_NAME = "woldsvaults_divinity_stat_data";
    private final Map<UUID, PlayerDivinityStats> playerMap = new HashMap<>();

    public PlayerDivinityStats getVaultStats(Player player) {
        return this.getVaultStats(player.getUUID());
    }

    public PlayerDivinityStats getVaultStats(UUID uuid) {
        return this.playerMap.computeIfAbsent(uuid, PlayerDivinityStats::new);
    }

    public PlayerDivinityStatData spendDivinityPoints(ServerPlayer player, int amount) {
        this.getVaultStats(player).spendDivinityPoints(player.getServer(), amount);
        this.setDirty();
        return this;
    }

    public PlayerDivinityStatData addDivinityPoints(ServerPlayer player, int amount) {
        this.getVaultStats(player).addDivinityPoints(amount).sync(player.getLevel().getServer());
        this.setDirty();
        return this;
    }

    public PlayerDivinityStatData refundDivinityPoints(ServerPlayer player, int amount) {
        this.getVaultStats(player).refundDivinityPoints(amount).sync(player.getLevel().getServer());
        this.setDirty();
        return this;
    }

    public PlayerDivinityStatData resetDivinity(ServerPlayer player) {
        this.getVaultStats(player).resetDivinity(player.getLevel().getServer()).sync(player.getLevel().getServer());
        PlayerDivinityStatData statsData = this.get(player.getLevel());
        PlayerDivinityData divinityData = PlayerDivinityData.get(player.getLevel());
        DivinityTree divinityTree = divinityData.getDivinityTree(player);
        PlayerDivinityStats stats = statsData.getVaultStats(player);

        for (Skill skill : divinityTree.getAll(LearnableSkill.class, Skill::isUnlocked)) {
            while (skill.isUnlocked()) {
                SkillContext context = SkillContext.of(player);
                if (skill.getParent() instanceof GroupedSkill grouped) {
                    grouped.select(skill.getId());
                    skill = grouped;
                }

                if (skill instanceof LearnableSkill learnable && learnable.canRegret(context)) {
                    learnable.regret(context);
                    divinityTree.sync(context);
                }
            }
        }

        stats.setDivinityPoints(0);
        stats.sync(player.getLevel().getServer());
        this.setDirty();
        return this;
    }

    public PlayerDivinityStatData resetAndReturnDivinityPoints(ServerPlayer player) {
        this.getVaultStats(player).resetAndReturnDivinityPoints().sync(player.getLevel().getServer());
        this.setDirty();
        return this;
    }

    public static PlayerDivinityStatData getServer() {
        return get(ServerLifecycleHooks.getCurrentServer());
    }

    public static PlayerDivinityStatData get(ServerLevel world) {
        return get(world.getServer());
    }

    public static PlayerDivinityStatData get(MinecraftServer srv) {
        return srv.overworld().getDataStorage().computeIfAbsent(PlayerDivinityStatData::create, PlayerDivinityStatData::new, DATA_NAME);
    }

    private static PlayerDivinityStatData create(CompoundTag tag) {
        PlayerDivinityStatData data = new PlayerDivinityStatData();
        data.load(tag);
        return data;
    }

    public void load(CompoundTag nbt) {
        ListTag playerList = nbt.getList("PlayerEntries", 8);
        ListTag statEntries = nbt.getList("StatEntries", 10);
        if (playerList.size() != statEntries.size()) {
            throw new IllegalStateException("Map doesn't have the same amount of keys as values");
        } else {

            for(int i = 0; i < playerList.size(); ++i) {
                UUID playerUUID = UUID.fromString(playerList.getString(i));
                this.getVaultStats(playerUUID).deserializeNBT(statEntries.getCompound(i));
            }
        }
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        ListTag playerList = new ListTag();
        ListTag statsList = new ListTag();
        this.playerMap.forEach((uuid, stats) -> {
            playerList.add(StringTag.valueOf(uuid.toString()));
            statsList.add(stats.serializeNBT());
        });
        nbt.put("PlayerEntries", playerList);
        nbt.put("StatEntries", statsList);
        return nbt;
    }
}
