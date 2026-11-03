package xyz.iwolfking.woldsvaults.init;

import iskallia.vault.config.Config;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import xyz.iwolfking.vhapi.api.LoaderRegistry;
import xyz.iwolfking.vhapi.api.events.VaultConfigEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.GreedChampionConfig;
import xyz.iwolfking.woldsvaults.config.GreedMedallionsConfig;
import xyz.iwolfking.woldsvaults.config.GreedMilestonesConfig;
import xyz.iwolfking.woldsvaults.config.GreedRanksConfig;
import xyz.iwolfking.woldsvaults.config.WoldsTable;
import xyz.iwolfking.woldsvaults.config.gods.GodLevelsConfig;
import xyz.iwolfking.woldsvaults.integration.vhapi.loaders.core.WoldsTableLoader;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionTier;
import xyz.iwolfking.woldsvaults.milestones.MilestoneRankLadder;
import xyz.iwolfking.woldsvaults.milestones.MilestoneRegistry;

import javax.annotation.Nullable;

/**
 * Wold's Vaults' generated balance tables. Each one is written by a datagen provider into
 * {@code data/woldsvaults/vault_configs/}, shipped in the jar, gathered by a {@link WoldsTableLoader} and installed
 * here after every config load, on the server and again on the client once VHAPI has synced the tables. There is no
 * fallback copy: a missing or unusable table logs an error and leaves its feature unloaded.
 */
public final class WoldsTables {
    public static final WoldsTableLoader<GodLevelsConfig> GOD_LEVELS = new WoldsTableLoader<>(new GodLevelsConfig(), "gods/levels");
    public static final WoldsTableLoader<GreedRanksConfig> GREED_RANKS = new WoldsTableLoader<>(new GreedRanksConfig(), "greed/ranks");
    public static final WoldsTableLoader<GreedMilestonesConfig> GREED_MILESTONES = new WoldsTableLoader<>(new GreedMilestonesConfig(), "greed/milestones");
    public static final WoldsTableLoader<GreedMedallionsConfig> GREED_MEDALLIONS = new WoldsTableLoader<>(new GreedMedallionsConfig(), "greed/medallions");
    public static final WoldsTableLoader<GreedChampionConfig> GREED_CHAMPION = new WoldsTableLoader<>(new GreedChampionConfig(), "greed/champion");

    private WoldsTables() {
    }

    /** Registers the loaders with VHAPI; called from the processor registration event. */
    public static void registerLoaders() {
        LoaderRegistry.addConfigProcessor(GOD_LEVELS);
        LoaderRegistry.addConfigProcessor(GREED_RANKS);
        LoaderRegistry.addConfigProcessor(GREED_MILESTONES);
        LoaderRegistry.addConfigProcessor(GREED_MEDALLIONS);
        LoaderRegistry.addConfigProcessor(GREED_CHAMPION);
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
        installGreedProgression();
        WoldsVaults.LOGGER.info("Installed the generated Wold's Vaults tables");
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
