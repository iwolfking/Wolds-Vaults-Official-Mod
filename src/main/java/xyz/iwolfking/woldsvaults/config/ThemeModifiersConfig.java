package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import com.mojang.datafixers.util.Pair;
import iskallia.vault.config.Config;
import iskallia.vault.config.entry.LevelEntryList;
import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.core.world.roll.IntRoll;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.iwolfking.woldsvaults.api.util.ThemeHelper;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ThemeModifiersConfig extends Config {

    @Expose
    public Map<ResourceLocation, ThemeModifierEntry> THEME_SPECIFIC_ENTRIES = new HashMap<>();

    @Expose
    public Map<String, ThemeGroupModifierEntry> THEME_GROUP_ENTRIES = new HashMap<>();

    @Override
    public String getName() {
        return "theme_modifiers";
    }

    @Override
    protected void reset() {
    }

    public static Optional<ResourceLocation> getModifierPoolForInfusedTheme(ResourceLocation themeId, int level) {
        if(ModConfigs.THEME_MODIFIERS.THEME_SPECIFIC_ENTRIES.containsKey(themeId)) {
            return Optional.ofNullable(ModConfigs.THEME_MODIFIERS.THEME_SPECIFIC_ENTRIES.get(themeId).modifierPool);
        }

        String themeGroup = ThemeHelper.getAugmentForTheme(themeId).orElse(null);

        if(themeGroup != null) {
            if(ModConfigs.THEME_MODIFIERS.THEME_GROUP_ENTRIES.containsKey(themeGroup)) {
                return Optional.ofNullable(ModConfigs.THEME_MODIFIERS.THEME_GROUP_ENTRIES.get(themeGroup).modifierPool);
            }
        }


        return Optional.empty();
    }

    public static class ThemeModifierEntry {

        @Expose
        private ResourceLocation modifierPool;

        @Expose
        private float infuseChance;

        public ThemeModifierEntry(ResourceLocation modifierPoolId, float infuseChance) {
            this.modifierPool = modifierPoolId;
            this.infuseChance = infuseChance;
        }

        public ResourceLocation getModifierPool() {
            return this.modifierPool;
        }

        public float getInfuseChance() {
            return this.infuseChance;
        }
    }

    public static class ThemeGroupModifierEntry {

        @Expose
        private String themeGroupId;

        @Expose
        private ResourceLocation modifierPool;

        @Expose
        private float infuseChance;

        public ThemeGroupModifierEntry(String themeId, ResourceLocation modifierPoolId, float infuseChance) {
            this.themeGroupId = themeId;
            this.modifierPool = modifierPoolId;
            this.infuseChance = infuseChance;
        }

        public String getThemeGroupId() {
            return this.themeGroupId;
        }

        public ResourceLocation getModifierPool() {
            return this.modifierPool;
        }

        public float getInfuseChance() {
            return this.infuseChance;
        }
    }
}
