package xyz.iwolfking.woldsvaults.config;

import iskallia.vault.config.Config;
import iskallia.vault.init.ModConfigs;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * A config the pack owner is expected to hand-edit, with the one guarantee base's {@link Config}
 * does not give such a file: an edit is never destroyed silently.
 *
 * <p>{@code Config#readConfig} treats every read failure identically - including a JSON syntax
 * error in a file that is plainly present - by logging {@code "not found, generating new"} and
 * calling {@code generateConfig()}, which writes the shipped defaults straight over the file. A
 * pack owner who mistypes a brace therefore loses the whole edit and is told the file was
 * missing. That is tolerable for a config nobody touches and wrong for one whose entire purpose
 * is being touched.
 *
 * <p>Overriding {@code generateConfig()} is enough to fix it because base calls that method from
 * exactly one place, the read-failure branch. A file about to be overwritten is moved aside to
 * {@code <name>.json.invalid} first, the real reason is logged at error, and the file is named
 * through {@code ModConfigs.INVALID_CONFIGS} so it also reaches the in-game
 * "Configs reloaded, with errors!" report rather than only the log.
 *
 * <p>{@link Config#isValid()} is the other half and belongs to each subclass: it is base's hook
 * for "this file parsed but says something structurally unusable", and failing it falls back to
 * the shipped defaults <em>without</em> writing over the file. Intra-file mistakes should be
 * caught there and reported; cross-file ones cannot be seen from a single config and stay fatal
 * where the whole set is assembled.
 */
public abstract class PackAuthoredConfig extends Config {

    /**
     * Preserves the rejected file before the shipped defaults are written over it. Called by base
     * only when a read has already failed, so an existing file here is always one that could not
     * be parsed.
     */
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
