package xyz.iwolfking.woldsvaults.integration.vhapi.loaders.core;

import xyz.iwolfking.vhapi.api.events.VaultConfigEvent;
import xyz.iwolfking.vhapi.api.loaders.lib.core.VaultConfigProcessor;
import xyz.iwolfking.woldsvaults.config.EtchedVaultLayoutConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

public class EtchedLayoutConfigLoader extends VaultConfigProcessor<EtchedVaultLayoutConfig> {

    public EtchedLayoutConfigLoader() {
        super(new EtchedVaultLayoutConfig(), "etched_vault_layouts");
    }

    @Override
    public void afterConfigsLoad(VaultConfigEvent.End event) {
        this.CUSTOM_CONFIGS.forEach(((resourceLocation, etchedVaultLayoutConfig) -> {
            etchedVaultLayoutConfig.ETCHED_VAULT_LAYOUTS.forEach((s, layoutEntries) -> {
                if(ModConfigs.ETCHED_VAULT_LAYOUT.ETCHED_VAULT_LAYOUTS.containsKey(s)) {
                    ModConfigs.ETCHED_VAULT_LAYOUT.ETCHED_VAULT_LAYOUTS.get(s).addAll(etchedVaultLayoutConfig.ETCHED_VAULT_LAYOUTS.get(s));
                }
                else {
                    ModConfigs.ETCHED_VAULT_LAYOUT.ETCHED_VAULT_LAYOUTS.put(s, etchedVaultLayoutConfig.ETCHED_VAULT_LAYOUTS.get(s));
                }
            });
        }));
    }
}
