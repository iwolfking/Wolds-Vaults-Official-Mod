package xyz.iwolfking.woldsvaults.competition;

import iskallia.vault.core.vault.objective.TimeTrialObjective;
import iskallia.vault.init.ModBlocks;
import iskallia.vault.init.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import xyz.iwolfking.vhapi.api.util.MessageUtils;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TimeTrialCompetition extends SavedData {
    private static final String DATA_NAME = WoldsVaults.MOD_ID + "_time_trial_competition";
    private static TimeTrialCompetition instance;
    private static final long WEEK_IN_TICKS = 168000; // 7 days in minecraft time (20 ticks per second * 60 seconds * 60 minutes * 24 hours * 7 days)
    
    private String currentObjective;
    private long endTime;
    private final Map<UUID, Long> playerTimes = new ConcurrentHashMap<>();
    private final Map<UUID, String> playerNames = new ConcurrentHashMap<>();
    
    public TimeTrialCompetition() {
        // Set competition to end at next Monday 00:00
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMonday = now.with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                .toLocalDate().atStartOfDay();
        this.endTime = nextMonday.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
    
    public static TimeTrialCompetition get() {
        return instance;
    }
    
    public static void setInstance(TimeTrialCompetition instance) {
        TimeTrialCompetition.instance = instance;
    }

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        MinecraftServer server = event.getServer();
        DimensionDataStorage storage = server.overworld().getDataStorage();
        instance = storage.computeIfAbsent(TimeTrialCompetition::load, TimeTrialCompetition::new, DATA_NAME);
        instance.setDirty();
    }
    
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        
        if (server != null && instance != null && System.currentTimeMillis() >= instance.endTime) {
            instance.endCompetition(server);
        }
    }
    
    public void recordTime(UUID playerId, String playerName, long time, String objective) {
        if (!objective.equals(currentObjective)) {
            // New objective, reset competition
            currentObjective = objective;
            playerTimes.clear();
            playerNames.clear();
            setDirty();
        }
        
        playerTimes.merge(playerId, time, Math::min);
        playerNames.put(playerId, playerName);
        setDirty();
    }
    
    public Map<UUID, Long> getLeaderboard() {
        // Return a sorted map of player times (fastest first)
        Map<UUID, Long> sorted = new LinkedHashMap<>();
        playerTimes.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .forEachOrdered(entry -> sorted.put(entry.getKey(), entry.getValue()));
        return sorted;
    }
    
    public String getPlayerName(UUID playerId) {
        return playerNames.getOrDefault(playerId, "Unknown");
    }
    
    public String getCurrentObjective() {
        return currentObjective != null ? currentObjective : "None";
    }
    
    public long getTimeRemaining() {
        return Math.max(0, endTime - System.currentTimeMillis());
    }
    
    private void endCompetition(MinecraftServer server) {
        if (playerTimes.isEmpty()) {
            // No participants, just reset
            resetCompetition();
            return;
        }
        
        // Find winner
        Map.Entry<UUID, Long> winner = playerTimes.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .orElse(null);
                
        if (winner != null) {
            ServerPlayer player = server.getPlayerList().getPlayer(winner.getKey());
            if (player != null) {
                // Award the winner
                ItemStack trophy = new ItemStack(ModBlocks.HERALD_TROPHY_BLOCK_ITEM);
                if (!player.addItem(trophy)) {
                    // Drop at player's feet if inventory is full
                    player.drop(trophy, false);
                }
                
                // Broadcast winner
                String message = String.format("§6§l[Weekly Time Trial] §e%s §6won this week's Time Trial with a time of §e%.2f seconds§6! The objective was: §e%s",
                        player.getDisplayName().getString(),
                        winner.getValue() / 20.0,
                        currentObjective);
                MessageUtils.broadcastMessage(player.getLevel(), new TextComponent(message));
            }
        }
        
        // Reset for next week
        resetCompetition();
    }
    
    private void resetCompetition() {
        currentObjective = null;
        playerTimes.clear();
        playerNames.clear();
        
        // Set new end time (next Monday 00:00)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMonday = now.with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                .toLocalDate().atStartOfDay();
        this.endTime = nextMonday.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        
        setDirty();
    }
    
    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putString("objective", currentObjective != null ? currentObjective : "");
        tag.putLong("endTime", endTime);
        
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
