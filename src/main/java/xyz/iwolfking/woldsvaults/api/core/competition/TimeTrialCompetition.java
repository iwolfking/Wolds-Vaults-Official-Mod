package xyz.iwolfking.woldsvaults.api.core.competition;

import iskallia.vault.block.VaultCrateBlock;
import iskallia.vault.core.Version;
import iskallia.vault.core.data.key.LootTableKey;
import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultRegistry;
import iskallia.vault.core.world.loot.LootTable;
import iskallia.vault.core.world.loot.generator.LootTableGenerator;
import iskallia.vault.init.ModBlocks;
import iskallia.vault.util.calc.ItemQuantityHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import xyz.iwolfking.vhapi.api.util.MessageUtils;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.competition.lib.RewardBundle;
import xyz.iwolfking.woldsvaults.blocks.tiles.TimeTrialTrophyBlockEntity;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.items.TimeTrialTrophyItem;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TimeTrialCompetition extends SavedData {
    private static final String DATA_NAME = WoldsVaults.MOD_ID + "_time_trial_competition";
    private static TimeTrialCompetition instance;

    private String currentObjective;
    private long endTime;
    private long currentSeed;
    private final Map<UUID, Long> playerTimes = new ConcurrentHashMap<>();
    private final Map<UUID, String> playerNames = new ConcurrentHashMap<>();
    
    public TimeTrialCompetition() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = now.with(TemporalAdjusters.next(ModConfigs.TIME_TRIAL_COMPETITION.RESET_DAY_OF_WEEK))
                .toLocalDate().atStartOfDay();
        this.endTime = endTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        currentObjective = ModConfigs.TIME_TRIAL_COMPETITION.getRandomObjective();
        this.currentSeed = new Random().nextLong();
    }
    
    public static TimeTrialCompetition get() {
        return instance;
    }
    
    public static void setInstance(TimeTrialCompetition instance) {
        TimeTrialCompetition.instance = instance;
    }

    @SubscribeEvent
    public static void onServerAboutToStart(ServerStartingEvent event) {
        MinecraftServer server = event.getServer();

        if(!isCompetitionEnabled(server)) {
            instance = null;
            return;
        }

        DimensionDataStorage storage = server.overworld().getDataStorage();
        instance = storage.computeIfAbsent(TimeTrialCompetition::load, TimeTrialCompetition::new, DATA_NAME);
        instance.setDirty();
    }
    
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

        if (!isCompetitionEnabled(server) || instance == null) return;
        
        if (System.currentTimeMillis() >= instance.endTime) {
            instance.endCompetition(server);
        }
    }
    
    public void recordTime(UUID playerId, String playerName, long time, String objective) {
        if(!objective.equals(currentObjective)) {
            return;
        }

        playerTimes.merge(playerId, time, Math::min);
        playerNames.put(playerId, playerName);
        setDirty();
    }

    public Map<UUID, Long> getLeaderboard() {
        Map<UUID, Long> sorted = new LinkedHashMap<>();
        playerTimes.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .forEachOrdered(entry -> sorted.put(entry.getKey(), entry.getValue()));
        return sorted;
    }

    public Map.Entry<UUID, Long> getBestTime() {
        if(getLeaderboard().isEmpty()) {
            return null;
        }
        return getLeaderboard().entrySet().iterator().next();
    }

    public String getPlayerName(UUID playerId) {
        return playerNames.getOrDefault(playerId, "Unknown");
    }
    
    public String getCurrentObjective() {
        return currentObjective != null ? currentObjective : null;
    }

    public long getSeed() {
        return currentSeed;
    }

    public void setCurrentObjective(String objectiveId) {
        currentObjective = objectiveId;
        this.setDirty();
    }
    
    public long getTimeRemaining() {
        return Math.max(0, endTime - System.currentTimeMillis());
    }
    
    public void endCompetition(MinecraftServer server) {
        if (playerTimes.isEmpty()) {
            resetCompetition();
            return;
        }

        Map.Entry<UUID, Long> winner = playerTimes.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .orElse(null);
                
        if (winner != null) {
            ServerPlayer player = server.getPlayerList().getPlayer(winner.getKey());
            if (player != null) {
                awardWinner(player);
                String message = String.format("§6§l[Weekly Time Trial] §e%s §6won this week's Time Trial with a time of §e%.2f seconds§6! The objective was: §e%s",
                        player.getDisplayName().getString(),
                        winner.getValue() / 20.0,
                        currentObjective);
                MessageUtils.broadcastMessage(player.getLevel(), new TextComponent(message));
            }
            else {
                awardWinner(winner.getKey(), server);
                String message = String.format("§6§l[Weekly Time Trial] §e%s §6won this week's Time Trial with a time of §e%.2f seconds§6! The objective was: §e%s",
                        UsernameCache.getMap().getOrDefault(winner.getKey(), "Player"),
                        winner.getValue() / 20.0,
                        currentObjective);
                server.getPlayerList().getPlayers().forEach(player1 -> {
                    MessageUtils.sendMessage(player1, new TextComponent(message));
                });
            }
        }

        resetCompetition();
    }

    private void awardWinner(UUID playerUUID, MinecraftServer server) {
        PlayerRewardStorage rewards = PlayerRewardStorage.get(server);

        if(ModConfigs.TIME_TRIAL_COMPETITION.REWARD_CRATE_LOOT_TABLE != null) {
            LootTableKey lootTableKey = VaultRegistry.LOOT_TABLE.getKey(ModConfigs.TIME_TRIAL_COMPETITION.REWARD_CRATE_LOOT_TABLE);

            NonNullList<ItemStack> loot = NonNullList.create();
            LootTableGenerator generator = new LootTableGenerator(
                    Version.latest(),
                    lootTableKey,
                    0.0F
            );
            generator.generate(ChunkRandom.ofNanoTime());
            generator.getItems().forEachRemaining(loot::add);

            ItemStack trophyStack = TimeTrialTrophyItem.create(new TimeTrialTrophyBlockEntity.TrophyData(UsernameCache.getMap().getOrDefault(playerUUID, "Player"), playerUUID, currentObjective, LocalDateTime.now().toString(), getBestTime().getValue()));
            loot.add(trophyStack);
            ItemStack crate = VaultCrateBlock.getCrateWithLoot(VaultCrateBlock.Type.valueOf("TIME_TRIAL_REWARD"), loot);

            rewards.addReward(
                    playerUUID,
                    new RewardBundle(List.of(crate))
            );
        }
    }

    private void awardWinner(ServerPlayer player) {
        awardWinner(player.getUUID(), player.getServer());
        player.sendMessage(
                new TextComponent("§6You earned a Time Trial reward! Use /timetrial rewards to claim it."),
                player.getUUID()
        );
    }


    public void resetCompetition() {
        currentObjective = ModConfigs.TIME_TRIAL_COMPETITION.getRandomObjective();
        playerTimes.clear();
        playerNames.clear();

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime endTime = now.with(TemporalAdjusters.next(ModConfigs.TIME_TRIAL_COMPETITION.RESET_DAY_OF_WEEK))
                .toLocalDate().atStartOfDay();
        this.endTime = endTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        this.currentSeed = new Random().nextLong();
        setDirty();
    }

    public static boolean isCompetitionEnabled(MinecraftServer server) {
        return true;
        //return server != null && server.isDedicatedServer() && !ModConfigs.TIME_TRIAL_COMPETITION.enabled;
    }


    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putString("objective", currentObjective != null ? currentObjective : "");
        tag.putLong("endTime", endTime);
        tag.putLong("seed", currentSeed);
        
        ListTag timesList = new ListTag();
        playerTimes.forEach((id, time) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("id", id);
            entry.putLong("time", time);
            entry.putString("name", playerNames.getOrDefault(id, ""));
            timesList.add(entry);
        });
        tag.put("times", timesList);
        
        return tag;
    }
    
    public static TimeTrialCompetition load(CompoundTag tag) {
        TimeTrialCompetition competition = new TimeTrialCompetition();
        competition.currentObjective = tag.getString("objective");
        if (competition.currentObjective.isEmpty()) {
            competition.currentObjective = null;
        }
        competition.endTime = tag.getLong("endTime");
        competition.currentSeed = tag.getLong("seed");
        
        ListTag timesList = tag.getList("times", Tag.TAG_COMPOUND);
        for (int i = 0; i < timesList.size(); i++) {
            CompoundTag entry = timesList.getCompound(i);
            UUID id = entry.getUUID("id");
            competition.playerTimes.put(id, entry.getLong("time"));
            competition.playerNames.put(id, entry.getString("name"));
        }
        
        return competition;
    }
}
