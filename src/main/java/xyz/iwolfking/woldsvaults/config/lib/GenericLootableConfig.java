package xyz.iwolfking.woldsvaults.config.lib;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;
import iskallia.vault.config.entry.vending.ProductEntry;
import iskallia.vault.util.data.WeightedList;

public class GenericLootableConfig extends Config {
    private final String name;

    @Expose
    public WeightedList<ProductEntry> POOL = new WeightedList<>();

    public GenericLootableConfig(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    protected void reset() {

    }
}
