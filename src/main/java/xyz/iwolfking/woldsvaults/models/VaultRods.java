package xyz.iwolfking.woldsvaults.models;

import iskallia.vault.VaultMod;
import iskallia.vault.dynamodel.DynamicModelProperties;
import iskallia.vault.dynamodel.model.item.HandHeldModel;
import iskallia.vault.dynamodel.registry.DynamicModelRegistry;
import xyz.iwolfking.woldsvaults.models.lib.HandheldRodModel;

public class VaultRods {
    public static final DynamicModelRegistry<HandheldRodModel> REGISTRY = new DynamicModelRegistry<>();

    public static final HandheldRodModel VANILLA;

    public VaultRods() {
    }

    static {
        VANILLA = REGISTRY.register(new HandheldRodModel(VaultMod.id("gear/rod/vanilla"), "Classic Rod")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
    }
}