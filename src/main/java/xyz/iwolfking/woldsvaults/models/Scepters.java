package xyz.iwolfking.woldsvaults.models;

import iskallia.vault.VaultMod;
import iskallia.vault.dynamodel.DynamicModelProperties;
import iskallia.vault.dynamodel.model.item.HandHeldModel;
import iskallia.vault.dynamodel.registry.DynamicModelRegistry;

public class Scepters {
    public static final DynamicModelRegistry<HandHeldModel> REGISTRY = new DynamicModelRegistry<>();
    public static final HandHeldModel SCEPTER_0;


    public Scepters() {
    }

    static {
        SCEPTER_0 = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/scepter/scepter_0"), "Scepter_0")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
    }
}
