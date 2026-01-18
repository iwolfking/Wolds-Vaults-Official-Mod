package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import com.ibm.icu.impl.Pair;
import iskallia.vault.config.Config;
import iskallia.vault.config.entry.LevelEntryList;
import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.core.world.data.entity.EntityPredicate;
import iskallia.vault.core.world.roll.IntRoll;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.vault_events.VaultEvent;
import xyz.iwolfking.woldsvaults.api.core.vault_events.VaultEventSystem;
import xyz.iwolfking.woldsvaults.init.ModEntities;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class SurvivalObjectiveConfig extends Config {
    @Expose
    public LinkedHashMap<String, LevelEntryList<SurvivalSpawnsEntry>> SURVIVAL_SPAWNS = new LinkedHashMap<>();

    @Expose
    public LevelEntryList<SurvivalTimeEntry> SURVIVAL_TIME = new LevelEntryList<>();

    @Expose
    public LevelEntryList<SurvivalRewardEntry> SURVIVAL_REWARDS = new LevelEntryList<>();

    @Expose
    public LevelEntryList<SurvivalChallengeEntry> SURVIVAL_CHALLENGES = new LevelEntryList<>();

    public SurvivalObjectiveConfig() {
    }


    @Override
    public String getName() {
        return "survival_objective";
    }

    @Override
    protected void reset() {
        LevelEntryList<SurvivalSpawnsEntry> spawnsEntries = new LevelEntryList<>();
        WeightedList<ResourceLocation> spawnIds = new WeightedList<>();
        WeightedList<ResourceLocation> rewardEventIds = new WeightedList<>();
        WeightedList<ResourceLocation> challengeEventIds = new WeightedList<>();
        spawnIds.put(ModEntities.BLACK_GHOST.getRegistryName(), 1);
        spawnIds.put(ModEntities.BLUE_GHOST.getRegistryName(), 1);
        spawnIds.put(ModEntities.GREEN_GHOST.getRegistryName(), 1);
        spawnIds.put(ModEntities.YELLOW_GHOST.getRegistryName(), 1);
        spawnsEntries.add(new SurvivalSpawnsEntry(0, IntRoll.ofConstant(1), spawnIds, IntRoll.ofConstant(20)));
        SURVIVAL_SPAWNS.put("default", spawnsEntries);
        SURVIVAL_TIME.add(new SurvivalTimeEntry(0, IntRoll.ofUniform(20, 60)));
        SURVIVAL_TIME.add(new SurvivalTimeEntry(0, IntRoll.ofUniform(20, 60)));
        rewardEventIds.add(WoldsVaults.id("crate_tier"), 1);
        SURVIVAL_REWARDS.add(new SurvivalRewardEntry(0, rewardEventIds));
        challengeEventIds.add(WoldsVaults.id("survival_mob_buff"), 1);
        challengeEventIds.add(WoldsVaults.id("survival_player_nerf"), 1);
        SURVIVAL_CHALLENGES.add(new SurvivalChallengeEntry(0, challengeEventIds));
    }

    public int getTimeRewardFor(int level, Entity entity) {
        SurvivalTimeEntry timeEntry = SURVIVAL_TIME.getForLevel(level).orElse(null);
        if(timeEntry == null) {
            return 0;
        }

        int timeRoll = timeEntry.timeAdded.get(ChunkRandom.ofNanoTime());
        for(EntityPredicate predicate : timeEntry.entityTypeTimeScaling.keySet()) {
            if(predicate.test(entity)) {
                return (int) (timeRoll * timeEntry.entityTypeTimeScaling.get(predicate));
            }
        }

        return timeRoll;

    }

    public static class SurvivalSpawnsEntry implements LevelEntryList.ILevelEntry {
        @Expose
        private final int level;

        @Expose
        private WeightedList<ResourceLocation> entityIds = new WeightedList<>();

        @Expose
        private final IntRoll spawnAmounts;

        @Expose
        private final IntRoll spawnTimer;

        public SurvivalSpawnsEntry(int level, IntRoll spawnAmounts, IntRoll spawnTimer) {
            this.level = level;
            this.spawnAmounts = spawnAmounts;
            this.spawnTimer = spawnTimer;
        }

        public SurvivalSpawnsEntry(int level, IntRoll spawnAmounts, WeightedList<ResourceLocation> entityIds, IntRoll spawnTimer) {
            this.level = level;
            this.spawnAmounts = spawnAmounts;
            this.entityIds = entityIds;
            this.spawnTimer = spawnTimer;
        }

        @Override
        public int getLevel() {
            return this.level;
        }

        public Pair<EntityType<?>, Integer> getRandomSpawn() {
            ResourceLocation id = entityIds.getRandom(ChunkRandom.ofNanoTime()).orElse(ResourceLocation.fromNamespaceAndPath("minecraft", "pig"));
            if(ForgeRegistries.ENTITIES.containsKey(id)) {
                EntityType<?> entityType = ForgeRegistries.ENTITIES.getValue(id);
                if(entityType == null) {
                    return Pair.of(EntityType.PIG, 1);
                }
                return Pair.of(entityType, spawnAmounts.get(ChunkRandom.ofNanoTime()));
            }

            return Pair.of(EntityType.PIG, 1);
        }
    }

    public static class SurvivalTimeEntry implements LevelEntryList.ILevelEntry {
        @Expose
        private final int level;

        @Expose
        private IntRoll timeAdded;

        @Expose
        private Map<EntityPredicate, Float> entityTypeTimeScaling = new HashMap<>();

        public SurvivalTimeEntry(int level) {
            this.level = level;
        }

        public SurvivalTimeEntry(int level, IntRoll timeAddedRoll) {
            this.level = level;
            this.timeAdded = timeAddedRoll;
        }

        public SurvivalTimeEntry(int level, IntRoll timeAddedRoll, Map<EntityPredicate, Float> entityTypeToTimeScalingMap) {
            this.level = level;
            this.timeAdded = timeAddedRoll;
            this.entityTypeTimeScaling = entityTypeToTimeScalingMap;
        }

        @Override
        public int getLevel() {
            return this.level;
        }

        public IntRoll getTimeAdded() {
            return timeAdded;
        }
    }

    public static class SurvivalRewardEntry implements LevelEntryList.ILevelEntry {
        @Expose
        private final int level;

        @Expose
        private WeightedList<ResourceLocation> possibleRewardEvents;

        public SurvivalRewardEntry(int level) {
            this.level = level;
            this.possibleRewardEvents = WeightedList.empty();
        }

        public SurvivalRewardEntry(int level, WeightedList<ResourceLocation> possibleRewardEvents) {
            this.level = level;
            this.possibleRewardEvents = possibleRewardEvents;
        }

        @Override
        public int getLevel() {
            return this.level;
        }

        public Optional<VaultEvent> getRandomRewardEvent() {
            ResourceLocation eventId = possibleRewardEvents.getRandom(ChunkRandom.ofNanoTime()).orElse(null);
            VaultEvent event = VaultEventSystem.getEventById(eventId);

            if(event == null) {
                WoldsVaults.LOGGER.warn("Attempted to activate an invalid Vault Event id: {}", eventId);
                return Optional.empty();
            }

            return Optional.of(event);
        }
    }

    public static class SurvivalChallengeEntry implements LevelEntryList.ILevelEntry {
        @Expose
        private final int level;

        @Expose
        private WeightedList<ResourceLocation> possibleChallengeEvents;

        public SurvivalChallengeEntry(int level) {
            this.level = level;
            this.possibleChallengeEvents = WeightedList.empty();
        }

        public SurvivalChallengeEntry(int level, WeightedList<ResourceLocation> possibleChallengeEvents) {
            this.level = level;
            this.possibleChallengeEvents = possibleChallengeEvents;
        }

        @Override
        public int getLevel() {
            return this.level;
        }

        public Optional<VaultEvent> getRandomChallengeEvent() {
            ResourceLocation eventId = possibleChallengeEvents.getRandom(ChunkRandom.ofNanoTime()).orElse(null);
            VaultEvent event = VaultEventSystem.getEventById(eventId);

            if(event == null) {
                WoldsVaults.LOGGER.warn("Attempted to activate an invalid Vault Event id: {}", eventId);
                return Optional.empty();
            }

            return Optional.of(event);
        }
    }
}
