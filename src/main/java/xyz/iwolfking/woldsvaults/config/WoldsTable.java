package xyz.iwolfking.woldsvaults.config;

import javax.annotation.Nullable;

/**
 * A config whose only source is a generated table in {@code vault_configs}. It carries no default values: its
 * provider writes every field, and {@link #validate()} decides whether a loaded copy can be installed.
 */
public interface WoldsTable {

    /** Null when the table can be installed, otherwise what is wrong with it. */
    @Nullable
    String validate();
}
