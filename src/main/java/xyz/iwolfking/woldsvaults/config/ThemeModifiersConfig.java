package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import com.mojang.datafixers.util.Pair;
import iskallia.vault.config.Config;
import iskallia.vault.config.entry.LevelEntryList;
import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.core.world.roll.IntRoll;
import iskallia.vault.world.VaultDifficulty;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.ThemeHelper;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public class ThemeModifiersConfig extends Config {

    @Expose
    public Map<ResourceLocation, ModifierPoolInfusionEntry> THEME_SPECIFIC_ENTRIES = new HashMap<>();

    @Expose
    public Map<String, ModifierPoolInfusionEntry> THEME_GROUP_ENTRIES = new HashMap<>();

    @Override
    public String getName() {
        return "theme_modifiers";
    }

    @Override
    protected void reset() {
        LevelEntryList<InfuseChanceEntry> lowChance = new LevelEntryList<>();
        LevelEntryList<InfuseChanceEntry> moderateChance = new LevelEntryList<>();
        LevelEntryList<InfuseChanceEntry> highChance = new LevelEntryList<>();
        lowChance.add(new InfuseChanceEntry(0, 0F));
        moderateChance.add(new InfuseChanceEntry(0, 0F));
        highChance.add(new InfuseChanceEntry(0, 0F));
        lowChance.add(new InfuseChanceEntry(50, 0.05F));
        lowChance.add(new InfuseChanceEntry(90, 0.075F));
        moderateChance.add(new InfuseChanceEntry(50, 0.15F));
        moderateChance.add(new InfuseChanceEntry(90, 0.2F));
        highChance.add(new InfuseChanceEntry(50, 0.3F));
        highChance.add(new InfuseChanceEntry(90, 0.35F));
        THEME_GROUP_ENTRIES.put("Saltshade", new ModifierPoolInfusionEntry(moderateChance, WoldsVaults.id("saltshade_infusion_modifiers")));
        THEME_GROUP_ENTRIES.put("Deepcrag", new ModifierPoolInfusionEntry(moderateChance, WoldsVaults.id("deepcrag_infusion_modifiers")));
        THEME_GROUP_ENTRIES.put("Bloodveil", new ModifierPoolInfusionEntry(moderateChance, WoldsVaults.id("bloodveil_infusion_modifiers")));
        THEME_GROUP_ENTRIES.put("Snowfell", new ModifierPoolInfusionEntry(moderateChance, WoldsVaults.id("snowfell_infusion_modifiers")));
        THEME_GROUP_ENTRIES.put("Glimmergrove", new ModifierPoolInfusionEntry(moderateChance, WoldsVaults.id("glimmergrove_infusion_modifiers")));
        THEME_GROUP_ENTRIES.put("Sporegrove", new ModifierPoolInfusionEntry(moderateChance, WoldsVaults.id("sporegrove_infusion_modifiers")));
        THEME_GROUP_ENTRIES.put("Nether", new ModifierPoolInfusionEntry(lowChance, WoldsVaults.id("nether_infusion_modifiers")));
        THEME_GROUP_ENTRIES.put("Void", new ModifierPoolInfusionEntry(lowChance, WoldsVaults.id("void_infusion_modifiers")));
        THEME_GROUP_ENTRIES.put("Candoria", new ModifierPoolInfusionEntry(moderateChance, WoldsVaults.id("candoria_infusion_modifiers")));
        THEME_GROUP_ENTRIES.put("Sandsworn", new ModifierPoolInfusionEntry(moderateChance, WoldsVaults.id("sandsworn_infusion_modifiers")));
        THEME_GROUP_ENTRIES.put("Plastic", new ModifierPoolInfusionEntry(moderateChance, WoldsVaults.id("plastic_infusion_modifiers")));
        THEME_GROUP_ENTRIES.put("Spooky", new ModifierPoolInfusionEntry(moderateChance, WoldsVaults.id("spooky_infusion_modifiers")));
        THEME_GROUP_ENTRIES.put("Chaos", new ModifierPoolInfusionEntry(lowChance, WoldsVaults.id("chaos_infusion_modifiers")));
        THEME_GROUP_ENTRIES.put("Holidays", new ModifierPoolInfusionEntry(moderateChance, WoldsVaults.id("holidays_infusion_modifiers")));
        THEME_GROUP_ENTRIES.put("FleshPit", new ModifierPoolInfusionEntry(moderateChance, WoldsVaults.id("fleshpit_infusion_modifiers")));
        THEME_GROUP_ENTRIES.put("Gods", new ModifierPoolInfusionEntry(moderateChance, WoldsVaults.id("gods_infusion_modifiers")));
        THEME_GROUP_ENTRIES.put("Underwater", new ModifierPoolInfusionEntry(highChance, WoldsVaults.id("underwater_infusion_modifiers")));
        THEME_GROUP_ENTRIES.put("Tropics", new ModifierPoolInfusionEntry(moderateChance, WoldsVaults.id("tropics_infusion_modifiers")));
        THEME_GROUP_ENTRIES.put("Grimdark", new ModifierPoolInfusionEntry(lowChance, WoldsVaults.id("grimdark_infusion_modifiers")));
        THEME_GROUP_ENTRIES.put("LostDepths", new ModifierPoolInfusionEntry(lowChance, WoldsVaults.id("lostdepths_infusion_modifiers")));
        THEME_GROUP_ENTRIES.put("Barnyard", new ModifierPoolInfusionEntry(moderateChance, WoldsVaults.id("barnyard_infusion_modifiers")));
        THEME_GROUP_ENTRIES.put("Harvest", new ModifierPoolInfusionEntry(moderateChance, WoldsVaults.id("harvest_infusion_modifiers")));
        THEME_GROUP_ENTRIES.put("Industrial", new ModifierPoolInfusionEntry(moderateChance, WoldsVaults.id("industrial_infusion_modifiers")));
        THEME_GROUP_ENTRIES.put("Astral", new ModifierPoolInfusionEntry(highChance, WoldsVaults.id("astral_infusion_modifiers")));
        THEME_GROUP_ENTRIES.put("Occult", new ModifierPoolInfusionEntry(highChance, WoldsVaults.id("occult_infusion_modifiers")));
        THEME_GROUP_ENTRIES.put("Arcane", new ModifierPoolInfusionEntry(highChance, WoldsVaults.id("arcane_infusion_modifiers")));
    }

    public static Optional<ResourceLocation> getModifierPoolForInfusedTheme(ResourceLocation themeId) {
        if(ModConfigs.THEME_MODIFIERS.THEME_SPECIFIC_ENTRIES.containsKey(themeId)) {
            return Optional.ofNullable(ModConfigs.THEME_MODIFIERS.THEME_SPECIFIC_ENTRIES.get(themeId).getModifierPool());
        }

        String themeGroup = ThemeHelper.getAugmentForTheme(themeId).orElse(null);

        if(themeGroup != null) {
            if(ModConfigs.THEME_MODIFIERS.THEME_GROUP_ENTRIES.containsKey(themeGroup)) {
                return Optional.ofNullable(ModConfigs.THEME_MODIFIERS.THEME_GROUP_ENTRIES.get(themeGroup).getModifierPool());
            }
        }


        return Optional.empty();
    }

    public static boolean shouldRandomlyInfuseVaultTheme(ResourceLocation themeId, int vaultLevel, VaultDifficulty difficulty) {
        Random random = new Random();
        if(ModConfigs.THEME_MODIFIERS.THEME_SPECIFIC_ENTRIES.containsKey(themeId)) {
            return random.nextFloat() <= ModConfigs.THEME_MODIFIERS.THEME_SPECIFIC_ENTRIES.get(themeId).getInfuseChance(vaultLevel, difficulty);
        }

        String themeGroup = ThemeHelper.getAugmentForTheme(themeId).orElse(null);

        if(themeGroup != null) {
            if(ModConfigs.THEME_MODIFIERS.THEME_GROUP_ENTRIES.containsKey(themeGroup)) {
                return random.nextFloat() <= ModConfigs.THEME_MODIFIERS.THEME_GROUP_ENTRIES.get(themeGroup).getInfuseChance(vaultLevel, difficulty);
            }
        }

        return false;

    }

    public static class ModifierPoolInfusionEntry {
        @Expose
        private LevelEntryList<InfuseChanceEntry> infuseChance;

        @Expose
        private ResourceLocation modifierPool;

        public ModifierPoolInfusionEntry(LevelEntryList<InfuseChanceEntry> infuseChance, ResourceLocation modifierPool) {
            this.infuseChance = infuseChance;
            this.modifierPool = modifierPool;
        }

        public ResourceLocation getModifierPool() {
            return this.modifierPool;
        }

        public float getInfuseChance(int level, VaultDifficulty difficulty) {
            return this.infuseChance.getForLevel(level).orElse(new InfuseChanceEntry(0, 0F)).chance * getDifficultyMultiplierIncreaseFor(difficulty);
        }

        public float getDifficultyMultiplierIncreaseFor(VaultDifficulty difficulty) {
            switch (difficulty) {
                case PIECE_OF_CAKE, EASY, NORMAL -> {
                    return 0F;
                }
                case HARD -> {
                    return 1F;
                }
                case IMPOSSIBLE -> {
                    return 2F;
                }
                case FRAGGED -> {
                    return 4F;
                }
            }

            return 1F;
        }
    }

    public static class InfuseChanceEntry implements LevelEntryList.ILevelEntry {
        @Expose
        private int level;
        @Expose
        private float chance;

        public InfuseChanceEntry(int level, float chance) {
            this.level = level;
            this.chance = chance;
        }

        @Override
        public int getLevel() {
            return this.level;
        }

        public float getChance() {
            return this.chance;
        }
    }
}
