package xyz.iwolfking.woldsvaults.models;

import iskallia.vault.VaultMod;
import iskallia.vault.dynamodel.DynamicModelProperties;
import iskallia.vault.dynamodel.model.item.PlainItemModel;
import iskallia.vault.dynamodel.registry.DynamicModelRegistry;

public class Maps {
    public static final DynamicModelRegistry<PlainItemModel> REGISTRY = new DynamicModelRegistry<>();
    public static final PlainItemModel STANDARD_MAP;

    static {
        STANDARD_MAP = (PlainItemModel)REGISTRY.register((new PlainItemModel(VaultMod.id("gear/map/standard"), "Standard Map")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll()));
    }
}
