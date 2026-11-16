package xyz.iwolfking.woldsvaults.mixins.vaulthunters.medallions;

import iskallia.vault.core.Version;
import iskallia.vault.core.data.DataObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.data.key.FieldKey;
import iskallia.vault.core.data.key.registry.FieldRegistry;
import iskallia.vault.core.vault.Vault;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionVault;

@Mixin(value = Vault.class, remap = false)
public abstract class MixinVaultMedallionRank extends DataObject<Vault> implements GreedMedallionVault {
    @Shadow @Final public static FieldRegistry FIELDS;

    /** The medallion rank, saved to disk with the vault and never sent to clients. */
    @Unique
    private static final FieldKey<Integer> woldsvaults$MEDALLION_RANK = FieldKey.of(WoldsVaults.id("greed_medallion_rank"), Integer.class)
            .with(Version.v1_0, Adapters.INT_SEGMENTED_7, DISK.all()).register(FIELDS);

    @Override
    public int woldsvaults$getMedallionRank() {
        Integer rank = this.get(woldsvaults$MEDALLION_RANK);
        return rank == null ? 0 : rank;
    }

    @Override
    public void woldsvaults$setMedallionRank(int rank) {
        this.set(woldsvaults$MEDALLION_RANK, rank);
    }
}
