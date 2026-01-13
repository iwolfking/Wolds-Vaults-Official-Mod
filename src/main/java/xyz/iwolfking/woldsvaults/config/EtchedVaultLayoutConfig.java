package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.block.entity.VaultForgeTileEntity;
import iskallia.vault.config.Config;
import iskallia.vault.config.VaultCrystalConfig;
import iskallia.vault.config.entry.LevelEntryList;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.item.crystal.layout.CrystalLayout;
import xyz.iwolfking.woldsvaults.api.core.layout.impl.ClassicRingsCrystalLayout;
import xyz.iwolfking.woldsvaults.api.core.layout.impl.ClassicRingsLayout;

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
        LevelEntryList<VaultCrystalConfig.LayoutEntry> layoutEntries = new LevelEntryList<>();
        WeightedList<CrystalLayout> defaultLayouts = new WeightedList<>();
        defaultLayouts.add(new ClassicRingsCrystalLayout(1, 12, 2), 1);
        layoutEntries.put(new VaultCrystalConfig.LayoutEntry(0, defaultLayouts));
        ETCHED_VAULT_LAYOUTS.put("default", layoutEntries);
    }
}
