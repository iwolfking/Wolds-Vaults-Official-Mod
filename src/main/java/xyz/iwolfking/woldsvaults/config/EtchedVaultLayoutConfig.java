package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.block.entity.VaultForgeTileEntity;
import iskallia.vault.config.Config;
import iskallia.vault.config.VaultCrystalConfig;
import iskallia.vault.config.entry.LevelEntryList;
import java.util.LinkedHashMap;
import java.util.Map;

public class EtchedVaultLayoutConfig extends Config {
    @Expose
    public Map<String, LevelEntryList<VaultCrystalConfig.LayoutEntry>> ETCHED_VAULT_LAYOUTS = new LinkedHashMap<>();

    @Override
    public String getName() {
        return "etched_vault_layouts";
    }

    @Override
    protected void reset() {
    }
}
