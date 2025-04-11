package xyz.iwolfking.woldsvaults.config.lib;

import iskallia.vault.config.ShopPedestalConfig;

public class GenericShopPedestalConfig extends ShopPedestalConfig {
    private final String name;

    public GenericShopPedestalConfig(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
