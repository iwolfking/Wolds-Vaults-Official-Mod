package xyz.iwolfking.woldsvaults.integration.vhapi.loaders.core;

import iskallia.vault.config.Config;
import xyz.iwolfking.vhapi.api.loaders.lib.core.VaultConfigProcessor;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import javax.annotation.Nullable;

/**
 * Collects one of Wold's Vaults' own generated tables from {@code data/woldsvaults/vault_configs/<directory>/}.
 * It installs nothing itself; {@link xyz.iwolfking.woldsvaults.init.WoldsTables} reads every loader once all of
 * them have gathered, so tables that depend on each other install in order.
 */
public class WoldsTableLoader<T extends Config> extends VaultConfigProcessor<T> {

    public WoldsTableLoader(T instance, String directory) {
        super(instance, directory);
    }

    /** The table generated as {@code vault_configs/<directory>/<name>.json}, or null when no datapack carries it. */
    @Nullable
    public T table(String name) {
        return this.CUSTOM_CONFIGS.get(WoldsVaults.id(name));
    }
}
