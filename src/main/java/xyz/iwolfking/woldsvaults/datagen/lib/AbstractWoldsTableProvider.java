package xyz.iwolfking.woldsvaults.datagen.lib;

import iskallia.vault.config.Config;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractVaultConfigDataProvider;
import xyz.iwolfking.vhapi.api.datagen.lib.VaultConfigBuilder;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.WoldsTable;

/**
 * Writes one of Wold's Vaults' own balance tables to {@code vault_configs/<directory>/<name>.json}. The provider
 * is the table's only source: it builds the complete config and {@link #table} refuses to emit one that
 * {@link WoldsTable#validate()} rejects, so a broken table fails datagen instead of shipping.
 */
public abstract class AbstractWoldsTableProvider extends AbstractVaultConfigDataProvider<AbstractWoldsTableProvider.NoBuilder> {
    private final String directory;

    protected AbstractWoldsTableProvider(DataGenerator generator, String directory) {
        super(generator, WoldsVaults.MOD_ID, directory, NoBuilder::new);
        this.directory = directory;
    }

    protected <T extends Config & WoldsTable> void table(String name, T table) {
        String problem = table.validate();
        if (problem != null) {
            throw new IllegalStateException("Generated table " + this.directory + "/" + name + " is invalid: " + problem);
        }
        this.addConfig(name, table);
    }

    @Override
    public String getName() {
        return "Wolds Vaults " + this.directory + " table";
    }

    /** Table providers hand over finished configs through {@link #table}; this builder is never used. */
    public static final class NoBuilder extends VaultConfigBuilder<Config> {
        private NoBuilder() {
            super(() -> {
                throw new UnsupportedOperationException("Wold's Vaults table providers add finished configs");
            });
        }

        @Override
        protected void configureConfig(Config config) {
        }
    }
}
