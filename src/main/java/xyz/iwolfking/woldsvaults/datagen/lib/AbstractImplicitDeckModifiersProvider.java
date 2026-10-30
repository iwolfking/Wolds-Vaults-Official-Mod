package xyz.iwolfking.woldsvaults.datagen.lib;

import iskallia.vault.config.VaultCrystalConfig;
import iskallia.vault.config.entry.LevelEntryList;
import iskallia.vault.item.crystal.layout.CrystalLayout;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractVaultConfigDataProvider;
import xyz.iwolfking.vhapi.api.datagen.lib.VaultConfigBuilder;
import xyz.iwolfking.vhapi.api.datagen.lib.WeightedLevelEntryListBuilder;
import xyz.iwolfking.woldsvaults.config.EtchedVaultLayoutConfig;
import xyz.iwolfking.woldsvaults.config.ImplicitDeckModifiersConfig;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class AbstractImplicitDeckModifiersProvider extends AbstractVaultConfigDataProvider<AbstractImplicitDeckModifiersProvider.Builder> {
    protected AbstractImplicitDeckModifiersProvider(DataGenerator generator, String modid) {
        super(generator, modid, "implicit_deck_modifiers", Builder::new);
    }

    @Override
    public String getName() {
        return modid + " Implicit Deck Modifiers Data Provider";
    }

    public static class Builder extends VaultConfigBuilder<ImplicitDeckModifiersConfig> {

        private final Map<String, String> entries = new LinkedHashMap<>();

        public Builder() {
            super(ImplicitDeckModifiersConfig::new);
        }

        public Builder add(String deckId, String deckModifierId) {
            entries.put(deckId, deckModifierId);
            return this;
        }

        @Override
        protected void configureConfig(ImplicitDeckModifiersConfig implicitDeckModifiersConfig) {
            implicitDeckModifiersConfig.DECKS_TO_MODIFIERS_MAP.putAll(entries);
        }
    }
}
