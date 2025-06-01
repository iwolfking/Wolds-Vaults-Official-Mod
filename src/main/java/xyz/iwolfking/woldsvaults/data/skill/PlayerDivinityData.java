package xyz.iwolfking.woldsvaults.data.skill;

import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.skill.base.LearnableSkill;
import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import xyz.iwolfking.woldsvaults.gui.menus.divinity.DivinityTree;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.util.DivinityUtils;

import java.util.*;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerDivinityData extends SavedData {
    private static final String DATA_NAME = "woldsvaults_divinity_data";
    private Map<UUID, DivinityTree> playerMap = new HashMap<>();
    private final Set<UUID> scheduledMerge = new HashSet<>();
    private DivinityTree previous;

    public PlayerDivinityData() {
    }

    public DivinityTree getDivinityTree(Player player) {
        return this.getDivinityTree(player.getUUID());
    }

    public DivinityTree getDivinityTree(UUID uuid) {
        return this.playerMap.computeIfAbsent(uuid, (uuid1) -> {
            return ModConfigs.DIVINITY_CONFIG.getAll().copy();
        });
    }

    public void resetAllPlayerDivinityTrees(ServerLevel level) {
        this.playerMap.clear();
        this.setDirty();

        for (ServerPlayer player : level.players()) {
            this.getDivinityTree(player).sync(SkillContext.of(player));
        }

    }

    public void resetDivinityTree(ServerPlayer player) {
        this.getDivinityTree(player).iterate(LearnableSkill.class, (skill) -> {
            skill.onRemove(SkillContext.of(player));
        });
        this.playerMap.remove(player.getUUID());
        this.setDirty();
        this.getDivinityTree(player).sync(SkillContext.of(player));
    }

    public boolean isDirty() {
        return true;
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            if (event.side.isServer()) {
                PlayerDivinityData data = get((ServerLevel) event.world);

                if (data.previous != ModConfigs.DIVINITY_CONFIG.getAll()) {
                    data.previous = ModConfigs.DIVINITY_CONFIG.getAll();
                    data.scheduledMerge.addAll(data.playerMap.keySet());
                }
            }

        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            if (event.side.isServer()) {
                Player var2 = event.player;
                if (var2 instanceof ServerPlayer) {
                    ServerPlayer player = (ServerPlayer)var2;
                    PlayerDivinityData data = get(player.getLevel());
                    if (data.scheduledMerge.remove(player.getUUID())) {
                        SkillContext context = SkillContext.of(player);
                        data.playerMap.put(player.getUUID(), (DivinityTree) (data.playerMap.get(player.getUUID())).mergeFrom(ModConfigs.DIVINITY_CONFIG.getAll().copy(), context));
                        SkillContext ctx = DivinityUtils.ofDivinity(player);
                        PlayerDivinityStats stats2 = PlayerDivinityStatData.get((ServerLevel)player.level).getVaultStats(player);
                        stats2.setDivinityPoints(ctx.getLearnPoints());

                        AttributeSnapshotHelper.getInstance().refreshSnapshotDelayed(player);
                    }
                    data.getDivinityTree(player).onTick(SkillContext.of(player));
                }


            }
        }
    }


    private static PlayerDivinityData create(CompoundTag tag) {
        PlayerDivinityData data = new PlayerDivinityData();
        data.load(tag);
        return data;
    }

    public void load(CompoundTag nbt) {
        this.playerMap.clear();
        this.scheduledMerge.clear();
        ListTag playerList = nbt.getList("Players", 8);
        ListTag talentList = nbt.getList("Divinity", 10);
        if (playerList.size() != talentList.size()) {
            throw new IllegalStateException("Map doesn't have the same amount of keys as values");
        } else {
            for(int i = 0; i < playerList.size(); ++i) {
                UUID playerUUID = UUID.fromString(playerList.getString(i));
                Adapters.SKILL.readNbt(talentList.getCompound(i)).ifPresent((tree) -> {
                    this.playerMap.put(playerUUID, (DivinityTree) tree);
                    this.scheduledMerge.add(playerUUID);
                });
            }

            this.setDirty();
        }
    }

    public CompoundTag save(CompoundTag nbt) {
        ListTag playerList = new ListTag();
        ListTag talentList = new ListTag();
        this.playerMap.forEach((uuid, tree) -> {
            Adapters.SKILL.writeNbt(tree).ifPresent((tag) -> {
                playerList.add(StringTag.valueOf(uuid.toString()));
                talentList.add(tag);
            });
        });
        nbt.put("Players", playerList);
        nbt.put("Divinity", talentList);
        return nbt;
    }

    public static PlayerDivinityData getServer() {
        return get(ServerLifecycleHooks.getCurrentServer());
    }

    public static PlayerDivinityData get(ServerLevel world) {
        return get(world.getServer());
    }

    public static PlayerDivinityData get(MinecraftServer srv) {
        return srv.overworld().getDataStorage().computeIfAbsent(PlayerDivinityData::create, PlayerDivinityData::new, DATA_NAME);
    }
}
