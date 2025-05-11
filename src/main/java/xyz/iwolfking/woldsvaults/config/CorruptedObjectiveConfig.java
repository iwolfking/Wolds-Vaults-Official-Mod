package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.VaultMod;
import iskallia.vault.config.Config;
import iskallia.vault.config.MonolithConfig;
import iskallia.vault.config.entry.LevelEntryList;
import net.minecraft.resources.ResourceLocation;

public class CorruptedObjectiveConfig extends Config {

    @Expose
    private LevelEntryList<CorruptedObjectiveConfig.Entry> levels;

    @Override
    public String getName() {
        return "corrupted_objective";
    }


    @Override
    protected void reset() {
        this.levels = new LevelEntryList<>();
        this.levels.put(new Entry(100, VaultMod.id("corrupted_modifier_pool")));
    }

    public ResourceLocation getModifierPool(int level) {
        return this.levels.getForLevel(level).orElseThrow().modifierPool;
    }

    private static class Entry implements LevelEntryList.ILevelEntry {
        @Expose
        private final int level;
        @Expose
        private ResourceLocation modifierPool;

        public Entry(int level, ResourceLocation modifierPool) {
            this.level = level;
            this.modifierPool = modifierPool;

        }

        public int getLevel() {
            return this.level;
        }
    }
}
