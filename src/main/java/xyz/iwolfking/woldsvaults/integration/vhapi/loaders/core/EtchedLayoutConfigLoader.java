package xyz.iwolfking.woldsvaults.integration.vhapi.loaders.core;

import iskallia.vault.config.VaultCrystalConfig;
import xyz.iwolfking.vhapi.api.events.VaultConfigEvent;
import xyz.iwolfking.vhapi.api.loaders.lib.core.VaultConfigProcessor;
import xyz.iwolfking.vhapi.api.util.LevelEntryListHelper;
import xyz.iwolfking.woldsvaults.config.EtchedVaultLayoutConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

public class EtchedLayoutConfigLoader extends VaultConfigProcessor<EtchedVaultLayoutConfig> {

    public EtchedLayoutConfigLoader() {
        super(new EtchedVaultLayoutConfig(), "etched_vault_layouts");
    }

    @Override
    public void afterConfigsLoad(VaultConfigEvent.End event) {
        this.CUSTOM_CONFIGS.forEach(((resourceLocation, etchedVaultLayoutConfig) -> {
            if(etchedVaultLayoutConfig.ETCHED_VAULT_LAYOUTS != null) {
                LevelEntryListHelper.mergeMap(etchedVaultLayoutConfig.ETCHED_VAULT_LAYOUTS, ModConfigs.ETCHED_VAULT_LAYOUT.ETCHED_VAULT_LAYOUTS, VaultCrystalConfig.LayoutEntry::getLevel, layoutEntry -> layoutEntry.pool);
            }
        }));
    }
}
