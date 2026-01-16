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
import xyz.iwolfking.woldsvaults.init.ModEntities;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class SurvivalObjectiveConfig extends Config {
    @Expose
    public LinkedHashMap<String, LevelEntryList<SurvivalSpawnsEntry>> SURVIVAL_SPAWNS = new LinkedHashMap<>();

    @Expose
    public LevelEntryList<SurvivalTimeEntry> SURVIVAL_TIME = new LevelEntryList<>();

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
        spawnIds.put(ModEntities.BLACK_GHOST.getRegistryName(), 1);
        spawnIds.put(ModEntities.BLUE_GHOST.getRegistryName(), 1);
        spawnIds.put(ModEntities.GREEN_GHOST.getRegistryName(), 1);
        spawnIds.put(ModEntities.YELLOW_GHOST.getRegistryName(), 1);
        spawnsEntries.add(new SurvivalSpawnsEntry(0, IntRoll.ofConstant(1), spawnIds));
        SURVIVAL_SPAWNS.put("default", spawnsEntries);
        SurvivalTimeEntry entry = new SurvivalTimeEntry(0);
        entry.timeAdded = IntRoll.ofUniform(20, 40);
        SURVIVAL_TIME.add(new SurvivalTimeEntry(0));
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

        public SurvivalSpawnsEntry(int level, IntRoll spawnAmounts) {
            this.level = level;
            this.spawnAmounts = spawnAmounts;
        }

        public SurvivalSpawnsEntry(int level, IntRoll spawnAmounts, WeightedList<ResourceLocation> entityIds) {
            this.level = level;
            this.spawnAmounts = spawnAmounts;
            this.entityIds = entityIds;
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
}
