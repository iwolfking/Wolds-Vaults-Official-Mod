package xyz.iwolfking.woldsvaults.init;

import iskallia.vault.config.Config;
import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import xyz.iwolfking.vhapi.api.LoaderRegistry;
import xyz.iwolfking.vhapi.api.events.VaultConfigEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.ExtractionObjectiveConfig;
import xyz.iwolfking.woldsvaults.config.ExtractorCostsConfig;
import xyz.iwolfking.woldsvaults.config.ExtractorPartsConfig;
import xyz.iwolfking.woldsvaults.config.GearSetConfig;
import xyz.iwolfking.woldsvaults.config.GreedChampionConfig;
import xyz.iwolfking.woldsvaults.config.GreedMedallionsConfig;
import xyz.iwolfking.woldsvaults.config.GreedMilestonesConfig;
import xyz.iwolfking.woldsvaults.config.GreedRanksConfig;
import xyz.iwolfking.woldsvaults.config.HyperObjectiveConfig;
import xyz.iwolfking.woldsvaults.config.MultiCellRoomsConfig;
import xyz.iwolfking.woldsvaults.config.ObjectiveDifficultyConfig;
import xyz.iwolfking.woldsvaults.config.StatScreenConfig;
import xyz.iwolfking.woldsvaults.config.WoldsTable;
import xyz.iwolfking.woldsvaults.config.gods.GodLevelsConfig;
import xyz.iwolfking.woldsvaults.config.gods.GodNodeEffectsConfig;
import xyz.iwolfking.woldsvaults.config.gods.GodTreeConfig;
import xyz.iwolfking.woldsvaults.config.gods.GodTreeGuiStylesConfig;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeRegistry;
import xyz.iwolfking.woldsvaults.gods.node.GodTreeConfigException;
import xyz.iwolfking.woldsvaults.integration.vhapi.loaders.core.WoldsTableLoader;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionTier;
import xyz.iwolfking.woldsvaults.milestones.MilestoneRankLadder;
import xyz.iwolfking.woldsvaults.milestones.MilestoneRegistry;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * Wold's Vaults' generated balance tables. Each one is written by a datagen provider into
 * {@code data/woldsvaults/vault_configs/}, shipped in the jar, gathered by a {@link WoldsTableLoader} and installed
 * here after every config load, on the server and again on the client once VHAPI has synced the tables. There is no
 * fallback copy: a missing or unusable table logs an error and leaves its feature unloaded.
 */
public final class WoldsTables {
    public static final WoldsTableLoader<GodLevelsConfig> GOD_LEVELS = new WoldsTableLoader<>(new GodLevelsConfig(), "gods/levels");
    public static final WoldsTableLoader<GodTreeConfig> GOD_TREES = new WoldsTableLoader<>(new GodTreeConfig(), "gods/trees");
    public static final WoldsTableLoader<GodTreeGuiStylesConfig> GOD_TREE_STYLES = new WoldsTableLoader<>(new GodTreeGuiStylesConfig(), "gods/styles");
    public static final WoldsTableLoader<GodNodeEffectsConfig> GOD_NODE_EFFECTS = new WoldsTableLoader<>(new GodNodeEffectsConfig(), "gods/effects");
    public static final WoldsTableLoader<GearSetConfig> GEAR_SETS = new WoldsTableLoader<>(new GearSetConfig(), "gear/sets");
    public static final WoldsTableLoader<ObjectiveDifficultyConfig> OBJECTIVE_DIFFICULTY = new WoldsTableLoader<>(new ObjectiveDifficultyConfig(), "objectives/difficulty");
    public static final WoldsTableLoader<HyperObjectiveConfig> HYPER_OBJECTIVE = new WoldsTableLoader<>(new HyperObjectiveConfig(), "objectives/hyper");
    public static final WoldsTableLoader<MultiCellRoomsConfig> MULTI_CELL_ROOMS = new WoldsTableLoader<>(new MultiCellRoomsConfig(), "layout/multi_cell_rooms");
    public static final WoldsTableLoader<ExtractionObjectiveConfig> EXTRACTION_OBJECTIVE = new WoldsTableLoader<>(new ExtractionObjectiveConfig(), "objectives/extraction");
    public static final WoldsTableLoader<ExtractorPartsConfig> EXTRACTOR_PARTS = new WoldsTableLoader<>(new ExtractorPartsConfig(), "extraction/parts");
    public static final WoldsTableLoader<ExtractorCostsConfig> EXTRACTOR_COSTS = new WoldsTableLoader<>(new ExtractorCostsConfig(), "extraction/costs");
    public static final WoldsTableLoader<GreedRanksConfig> GREED_RANKS = new WoldsTableLoader<>(new GreedRanksConfig(), "greed/ranks");
    public static final WoldsTableLoader<GreedMilestonesConfig> GREED_MILESTONES = new WoldsTableLoader<>(new GreedMilestonesConfig(), "greed/milestones");
    public static final WoldsTableLoader<GreedMedallionsConfig> GREED_MEDALLIONS = new WoldsTableLoader<>(new GreedMedallionsConfig(), "greed/medallions");
    public static final WoldsTableLoader<GreedChampionConfig> GREED_CHAMPION = new WoldsTableLoader<>(new GreedChampionConfig(), "greed/champion");
    public static final WoldsTableLoader<StatScreenConfig> STAT_SCREEN = new WoldsTableLoader<>(new StatScreenConfig(), "gui/stat_screen");

    private WoldsTables() {
    }

    /** Registers the loaders with VHAPI; called from the processor registration event. */
    public static void registerLoaders() {
        LoaderRegistry.addConfigProcessor(GOD_LEVELS);
        LoaderRegistry.addConfigProcessor(GOD_TREES);
        LoaderRegistry.addConfigProcessor(GOD_TREE_STYLES);
        LoaderRegistry.addConfigProcessor(GOD_NODE_EFFECTS);
        LoaderRegistry.addConfigProcessor(GEAR_SETS);
        LoaderRegistry.addConfigProcessor(OBJECTIVE_DIFFICULTY);
        LoaderRegistry.addConfigProcessor(HYPER_OBJECTIVE);
        LoaderRegistry.addConfigProcessor(MULTI_CELL_ROOMS);
        LoaderRegistry.addConfigProcessor(EXTRACTION_OBJECTIVE);
        LoaderRegistry.addConfigProcessor(EXTRACTOR_PARTS);
        LoaderRegistry.addConfigProcessor(EXTRACTOR_COSTS);
        LoaderRegistry.addConfigProcessor(GREED_RANKS);
        LoaderRegistry.addConfigProcessor(GREED_MILESTONES);
        LoaderRegistry.addConfigProcessor(GREED_MEDALLIONS);
        LoaderRegistry.addConfigProcessor(GREED_CHAMPION);
        LoaderRegistry.addConfigProcessor(STAT_SCREEN);
    }

    /** Installs the tables after the loaders have run; lowest priority so every loader has gathered first. */
    public static void registerInstaller() {
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, WoldsTables::install);
    }

    private static void install(VaultConfigEvent.End event) {
        if (LoaderRegistry.VHAPI_DATA_LOADER.JSON_DATA.isEmpty()) {
            return;
        }
        ModConfigs.GOD_LEVELS = require(GOD_LEVELS, "god_levels");
        installGodTrees();
        installGreedProgression();
        ModConfigs.GEAR_SETS = require(GEAR_SETS, "gear_sets");
        ModConfigs.OBJECTIVE_DIFFICULTY = require(OBJECTIVE_DIFFICULTY, "objective_difficulty");
        ModConfigs.HYPER_OBJECTIVE = require(HYPER_OBJECTIVE, "hyper_objective");
        MultiCellRoomsConfig multiCell = require(MULTI_CELL_ROOMS, "multi_cell_rooms");
        if (multiCell != null) {
            ModConfigs.MULTI_CELL_ROOMS = multiCell;
        }
        installExtraction();
        ModConfigs.STAT_SCREEN = require(STAT_SCREEN, "player_stats");
        WoldsVaults.LOGGER.info("Installed the generated Wold's Vaults tables");
    }

    /**
     * Installs all four god trees together or none of them: a missing table or one the node registry rejects logs
     * an error and leaves the previously installed trees in place.
     */
    private static void installGodTrees() {
        Map<VaultGod, GodTreeConfig> trees = new EnumMap<>(VaultGod.class);
        Map<VaultGod, GodTreeGuiStylesConfig> styles = new EnumMap<>(VaultGod.class);
        Map<VaultGod, GodNodeEffectsConfig> effects = new EnumMap<>(VaultGod.class);
        for (VaultGod god : VaultGod.values()) {
            String name = god.getName().toLowerCase(Locale.ROOT);
            GodTreeConfig tree = require(GOD_TREES, name);
            GodTreeGuiStylesConfig style = require(GOD_TREE_STYLES, name);
            GodNodeEffectsConfig effect = require(GOD_NODE_EFFECTS, name);
            if (tree == null || style == null || effect == null) {
                WoldsVaults.LOGGER.error("God trees were not installed because {}'s tables are incomplete", name);
                return;
            }
            trees.put(god, tree);
            styles.put(god, style);
            effects.put(god, effect);
        }
        try {
            GodNodeRegistry.load(trees, styles, effects);
        } catch (GodTreeConfigException e) {
            WoldsVaults.LOGGER.error("The generated god tree tables were rejected; the previously installed trees stay in place", e);
            iskallia.vault.init.ModConfigs.INVALID_CONFIGS.add("woldsvaults gods/* - " + e.getMessage());
            return;
        }
        ModConfigs.GOD_TREES.clear();
        ModConfigs.GOD_TREES.putAll(trees);
        ModConfigs.GOD_TREE_GUI_STYLES.clear();
        ModConfigs.GOD_TREE_GUI_STYLES.putAll(styles);
        ModConfigs.GOD_NODE_EFFECTS.clear();
        ModConfigs.GOD_NODE_EFFECTS.putAll(effects);
    }

    /** The extraction holders start on inert tables, so a table that is missing or rejected keeps the last good one. */
    private static void installExtraction() {
        ExtractionObjectiveConfig objective = require(EXTRACTION_OBJECTIVE, "extraction_objective");
        if (objective != null) {
            ModConfigs.EXTRACTION_OBJECTIVE = objective;
        }
        ExtractorPartsConfig parts = require(EXTRACTOR_PARTS, "extractor_parts");
        if (parts != null) {
            ModConfigs.EXTRACTOR_PARTS = parts;
        }
        ExtractorCostsConfig costs = require(EXTRACTOR_COSTS, "extractor_costs");
        if (costs != null) {
            ModConfigs.EXTRACTOR_COSTS = costs;
        }
    }

    /** The rank ladder installs before the milestones, which are tagged with its ranks. */
    private static void installGreedProgression() {
        ModConfigs.GREED_RANKS = require(GREED_RANKS, "greed_ranks");
        if (ModConfigs.GREED_RANKS != null) {
            MilestoneRankLadder.load(ModConfigs.GREED_RANKS);
        }
        ModConfigs.GREED_MILESTONES = require(GREED_MILESTONES, "greed_milestones");
        if (ModConfigs.GREED_MILESTONES != null) {
            MilestoneRegistry.load(ModConfigs.GREED_MILESTONES);
        }
        ModConfigs.GREED_MEDALLIONS = require(GREED_MEDALLIONS, "greed_medallions");
        if (ModConfigs.GREED_MEDALLIONS != null) {
            GreedMedallionTier.load(ModConfigs.GREED_MEDALLIONS);
        }
        ModConfigs.GREED_CHAMPION = require(GREED_CHAMPION, "greed_champion");
    }

    @Nullable
    private static <T extends Config & WoldsTable> T require(WoldsTableLoader<T> loader, String name) {
        T table = loader.table(name);
        if (table == null) {
            WoldsVaults.LOGGER.error("Generated table woldsvaults:{}/{} is missing; its feature stays unloaded",
                    loader.directory, name);
            return null;
        }
        String problem = table.validate();
        if (problem != null) {
            WoldsVaults.LOGGER.error("Generated table woldsvaults:{}/{} is unusable ({}); its feature stays unloaded",
                    loader.directory, name, problem);
            return null;
        }
        return table;
    }
}
