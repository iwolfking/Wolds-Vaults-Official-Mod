package xyz.iwolfking.woldsvaults.integration.vhapi.loaders.core;

import iskallia.vault.config.VaultCrystalConfig;
import xyz.iwolfking.vhapi.api.events.VaultConfigEvent;
import xyz.iwolfking.vhapi.api.loaders.lib.core.VaultConfigProcessor;
import xyz.iwolfking.vhapi.api.util.LevelEntryListHelper;
import xyz.iwolfking.woldsvaults.config.EtchedVaultLayoutConfig;
import xyz.iwolfking.woldsvaults.config.ImplicitDeckModifiersConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

public class ImplicitDeckModifiersConfigLoader extends VaultConfigProcessor<ImplicitDeckModifiersConfig> {

    public ImplicitDeckModifiersConfigLoader() {
        super(new ImplicitDeckModifiersConfig(), "implicit_deck_modifiers");
    }

    @Override
    public void afterConfigsLoad(VaultConfigEvent.End event) {
        this.CUSTOM_CONFIGS.forEach(((resourceLocation, implicitDeckModifiersConfig) -> {
            ModConfigs.IMPLICIT_DECK_MODIFIERS.DECKS_TO_MODIFIERS_MAP.putAll(implicitDeckModifiersConfig.DECKS_TO_MODIFIERS_MAP);
        }));
    }
}
