package xyz.iwolfking.woldsvaults.integration.vhapi.loaders.core;

import xyz.iwolfking.vhapi.api.events.VaultConfigEvent;
import xyz.iwolfking.vhapi.api.loaders.lib.core.VaultConfigProcessor;
import xyz.iwolfking.woldsvaults.config.EtchedVaultLayoutConfig;

public class EtchedLayoutConfigLoader extends VaultConfigProcessor<EtchedVaultLayoutConfig> {

    public EtchedLayoutConfigLoader(EtchedVaultLayoutConfig instance, String directory) {
        super(instance, directory);
    }

    @Override
    public void afterConfigsLoad(VaultConfigEvent.End event) {
        //this.CUSTOM_CONFIGS.forEach()
    }
}
