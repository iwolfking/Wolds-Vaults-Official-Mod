package xyz.iwolfking.woldsvaults.init;

import iskallia.vault.config.Config;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import xyz.iwolfking.vhapi.api.LoaderRegistry;
import xyz.iwolfking.vhapi.api.events.VaultConfigEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.WoldsTable;
import xyz.iwolfking.woldsvaults.config.gods.GodLevelsConfig;
import xyz.iwolfking.woldsvaults.integration.vhapi.loaders.core.WoldsTableLoader;

import javax.annotation.Nullable;

/**
 * Wold's Vaults' generated balance tables. Each one is written by a datagen provider into
 * {@code data/woldsvaults/vault_configs/}, shipped in the jar, gathered by a {@link WoldsTableLoader} and installed
 * here after every config load, on the server and again on the client once VHAPI has synced the tables. There is no
 * fallback copy: a missing or unusable table logs an error and leaves its feature unloaded.
 */
public final class WoldsTables {
    public static final WoldsTableLoader<GodLevelsConfig> GOD_LEVELS = new WoldsTableLoader<>(new GodLevelsConfig(), "gods/levels");

    private WoldsTables() {
    }

    /** Registers the loaders with VHAPI; called from the processor registration event. */
    public static void registerLoaders() {
        LoaderRegistry.addConfigProcessor(GOD_LEVELS);
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
        WoldsVaults.LOGGER.info("Installed the generated Wold's Vaults tables");
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
