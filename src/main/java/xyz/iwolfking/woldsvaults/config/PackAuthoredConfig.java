package xyz.iwolfking.woldsvaults.config;

import iskallia.vault.config.Config;
import iskallia.vault.init.ModConfigs;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * A {@link Config} whose file is never silently overwritten: one that fails to parse is moved aside to
 * {@code <name>.json.invalid}, logged and named in {@code ModConfigs.INVALID_CONFIGS} before the
 * defaults regenerate. Structural checks go in {@link Config#isValid()}, which leaves the file alone.
 */
public abstract class PackAuthoredConfig extends Config {

    /** Preserves the rejected file before the shipped defaults are written over it. */
    @Override
    public void generateConfig() {
        this.preserveRejectedFile();
        super.generateConfig();
    }

    private void preserveRejectedFile() {
        File file = this.getConfigFile();
        if (!file.exists()) {
            return;
        }
        File kept = new File(file.getParentFile(), file.getName() + ".invalid");
        try {
            Files.move(file.toPath(), kept.toPath(), StandardCopyOption.REPLACE_EXISTING);
            WoldsVaults.LOGGER.error("Could not parse {}; the file has been kept as {} and regenerated from the "
                    + "shipped defaults. Fix the JSON in the kept copy and restore it.",
                    file.getName(), kept.getName());
        } catch (IOException e) {
            WoldsVaults.LOGGER.error("Could not parse {}, and it could not be moved aside either; it is about to be "
                    + "overwritten with the shipped defaults.", file.getName(), e);
        }
        ModConfigs.INVALID_CONFIGS.add(file.getName() + " - could not be parsed; kept as " + kept.getName()
                + " and regenerated from defaults.");
    }
}
