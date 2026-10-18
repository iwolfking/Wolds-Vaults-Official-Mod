package xyz.iwolfking.woldsvaults.models;

import iskallia.vault.VaultMod;
import iskallia.vault.dynamodel.DynamicModelProperties;
import iskallia.vault.dynamodel.model.item.HandHeldModel;
import iskallia.vault.dynamodel.registry.DynamicModelRegistry;

public class Scepters {
    public static final DynamicModelRegistry<HandHeldModel> REGISTRY = new DynamicModelRegistry<>();
    public static final HandHeldModel ARCANE_SCEPTER;
    public static final HandHeldModel BLAZING_SCEPTER;
    public static final HandHeldModel BRIGHT_SCEPTER;
    public static final HandHeldModel DARK_SCEPTER;
    public static final HandHeldModel DULL_SCEPTER;
    public static final HandHeldModel LIFE_SCEPTER;
    public static final HandHeldModel MYTHIC_SCEPTER;
    public static final HandHeldModel STANDARD_SCEPTER;
    public static final HandHeldModel VERDANT_SCEPTER;


    public Scepters() {
    }

    static {
        ARCANE_SCEPTER = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/scepter/arcane_scepter"), "Arcane Scepter")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        BLAZING_SCEPTER = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/scepter/blazing_scepter"), "Blazing Scepter")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        BRIGHT_SCEPTER = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/scepter/bright_scepter"), "Bright Scepter")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        DARK_SCEPTER = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/scepter/dark_scepter"), "Dark Scepter")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        DULL_SCEPTER = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/scepter/dull_scepter"), "Dull Scepter")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        LIFE_SCEPTER = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/scepter/life_scepter"), "Life Scepter")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        MYTHIC_SCEPTER = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/scepter/mythic_scepter"), "Mythic Scepter")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        STANDARD_SCEPTER = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/scepter/standard_scepter"), "Standard Scepter")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        VERDANT_SCEPTER = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/scepter/verdant_scepter"), "Verdant Scepter")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
    }
}
