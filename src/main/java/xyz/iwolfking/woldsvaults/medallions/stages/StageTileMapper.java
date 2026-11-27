package xyz.iwolfking.woldsvaults.medallions.stages;

import iskallia.vault.core.vault.Vault;

import javax.annotation.Nullable;

/**
 * Implemented on the base mod's {@code TileMapper} by mixin: the vault of the placement the mapper was built for, so
 * the palette references it inlines resolve through {@link VaultStages} for that vault.
 */
public interface StageTileMapper {
    @Nullable
    Vault woldsvaults$getStageVault();

    void woldsvaults$setStageVault(@Nullable Vault vault);
}
