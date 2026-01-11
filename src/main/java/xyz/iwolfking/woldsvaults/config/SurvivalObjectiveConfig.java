package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;
import iskallia.vault.config.entry.LevelEntryList;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.core.world.data.entity.EntityPredicate;
import iskallia.vault.core.world.data.entity.PartialEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import xyz.iwolfking.woldsvaults.init.ModEntities;

import java.util.HashMap;
import java.util.Map;

public class SurvivalObjectiveConfig extends Config {
    @Expose
    public LevelEntryList<SurvivalSpawnsEntry> SURVIVAL_SPAWNS = new LevelEntryList<>();

    @Expose
    public LevelEntryList<SurvivalTimeEntry> SURVIVAL_TIME = new LevelEntryList<>();

    public SurvivalObjectiveConfig() {
        SURVIVAL_SPAWNS.add(new SurvivalSpawnsEntry(0));
    }


    @Override
    public String getName() {
        return "survival_objective";
    }

    @Override
    protected void reset() {
        SURVIVAL_SPAWNS.add(new SurvivalSpawnsEntry(0));
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
        private final int spawnAmount = 1;

        public SurvivalSpawnsEntry(int level) {
            this.level = level;
        }

        @Override
        public int getLevel() {
            return this.level;
        }

        public WeightedList<EntityType<?>> getEntities() {
            WeightedList<EntityType<?>> entityTypeWeightedList = new WeightedList<>();
            entityTypeWeightedList.add(ModEntities.HATURKIN, 1);
            return entityTypeWeightedList;
        }

        public int getSpawnAmount() {
            return 1;
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
