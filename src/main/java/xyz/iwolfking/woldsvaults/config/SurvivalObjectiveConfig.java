package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;
import iskallia.vault.config.entry.LevelEntryList;
import iskallia.vault.core.util.WeightedList;
import net.minecraft.world.entity.EntityType;
import xyz.iwolfking.woldsvaults.init.ModEntities;

public class SurvivalObjectiveConfig extends Config {
    @Expose
    public LevelEntryList<SurvivalSpawnsEntry> SURVIVAL_SPAWNS = new LevelEntryList<>();

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
}
