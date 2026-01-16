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
        entry.baseTicks = 60;
        SURVIVAL_TIME.add(new SurvivalTimeEntry(0));
    }

    public int getTimeRewardFor(Entity entity) {
        return 60;
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

        public WeightedList<EntityType<?>> getEntities() {
            WeightedList<EntityType<?>> entityTypeWeightedList = new WeightedList<>();
            entityTypeWeightedList.add(ModEntities.HATURKIN, 1);
            return entityTypeWeightedList;
        }
    }

    public static class SurvivalTimeEntry implements LevelEntryList.ILevelEntry {
        @Expose
        private final int level;

        @Expose
        private int baseTicks;

        @Expose
        private Map<EntityPredicate, Float> ENTITY_TYPE_TO_TIME_SCALING = new HashMap<>();

        public SurvivalTimeEntry(int level) {
            this.level = level;
        }

        @Override
        public int getLevel() {
            return this.level;
        }

        public int getBaseTicks() {
            return baseTicks;
        }

        public Map<EntityPredicate, Float> getEntityTypeToTimeScalingMap() {
            return ENTITY_TYPE_TO_TIME_SCALING;
        }
    }
}
