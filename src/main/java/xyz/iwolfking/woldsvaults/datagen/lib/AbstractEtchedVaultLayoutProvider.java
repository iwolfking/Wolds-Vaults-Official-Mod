package xyz.iwolfking.woldsvaults.datagen.lib;

import iskallia.vault.config.VaultCrystalConfig;
import iskallia.vault.config.entry.LevelEntryList;
import iskallia.vault.item.crystal.layout.CrystalLayout;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractVaultConfigDataProvider;
import xyz.iwolfking.vhapi.api.datagen.lib.VaultConfigBuilder;
import xyz.iwolfking.vhapi.api.datagen.lib.WeightedLevelEntryListBuilder;
import xyz.iwolfking.woldsvaults.config.EtchedVaultLayoutConfig;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class AbstractEtchedVaultLayoutProvider extends AbstractVaultConfigDataProvider<AbstractEtchedVaultLayoutProvider.Builder> {
    protected AbstractEtchedVaultLayoutProvider(DataGenerator generator, String modid) {
        super(generator, modid, "etched_vault_layouts", Builder::new);
    }

    @Override
    public String getName() {
        return modid + " Etched Vault Layout Data Provider";
    }

    public static class Builder extends VaultConfigBuilder<EtchedVaultLayoutConfig> {

        private final Map<String, LevelEntryList<VaultCrystalConfig.LayoutEntry>> etchedVaultLayouts = new LinkedHashMap<>();

        public Builder() {
            super(EtchedVaultLayoutConfig::new);
        }

        public Builder addLayouts(String pool, Consumer<WeightedLevelEntryListBuilder<VaultCrystalConfig.LayoutEntry, CrystalLayout>> builderConsumer) {
            WeightedLevelEntryListBuilder<VaultCrystalConfig.LayoutEntry, CrystalLayout> builder = new WeightedLevelEntryListBuilder<>(VaultCrystalConfig.LayoutEntry::new);
            builderConsumer.accept(builder);
            etchedVaultLayouts.put(pool, builder.build());
            return this;
        }

        @Override
        protected void configureConfig(EtchedVaultLayoutConfig etchedVaultLayoutConfig) {
            etchedVaultLayoutConfig.ETCHED_VAULT_LAYOUTS.putAll(etchedVaultLayouts);
        }
    }
}
