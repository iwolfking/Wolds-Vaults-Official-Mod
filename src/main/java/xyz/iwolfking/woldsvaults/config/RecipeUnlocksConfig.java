package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeUnlocksConfig extends Config {

    @Expose
    public Map<ResourceLocation, Entry> RECIPE_UNLOCKS = new HashMap<>();

    @Override
    public String getName() {
        return "recipe_unlocks";
    }

    @Override
    protected void reset() {
        RECIPE_UNLOCKS = new HashMap<>();
        RECIPE_UNLOCKS.put(WoldsVaults.id("default"), new Entry("Test Recipe", "This should never be seen in normal gameplay."));
    }

    public static class Entry {
        @Expose
        public String NAME;

        @Expose
        public String DESCRIPTION;

        public Entry(String NAME, String DESCRIPTION) {
            this.NAME = NAME;
            this.DESCRIPTION = DESCRIPTION;
        }
    }
}
