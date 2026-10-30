package xyz.iwolfking.woldsvaults.models;

import iskallia.vault.VaultMod;
import iskallia.vault.dynamodel.DynamicModelProperties;
import iskallia.vault.dynamodel.model.item.PlainItemModel;
import iskallia.vault.dynamodel.registry.DynamicModelRegistry;

public class Jewels {
    public static final DynamicModelRegistry<PlainItemModel> REGISTRY = new DynamicModelRegistry<>();
    public static final PlainItemModel STELLA;
    public static final PlainItemModel TREASURE;
    public static final PlainItemModel MANABLOOM;
    public static final PlainItemModel KALEIDOSCOPE;
    public static final PlainItemModel PAX;
    public static final PlainItemModel BREACHING;
    public static final PlainItemModel FROST;

    static {
        STELLA = REGISTRY.register(new PlainItemModel(VaultMod.id("gear/jewel/stella"), "Eternal Stella")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        TREASURE = REGISTRY.register(new PlainItemModel(VaultMod.id("gear/jewel/treasure"), "Treasured Jewel")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        MANABLOOM = REGISTRY.register(new PlainItemModel(VaultMod.id("gear/jewel/manabloom"), "Manabloom")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        KALEIDOSCOPE = REGISTRY.register(new PlainItemModel(VaultMod.id("gear/jewel/kaleidoscope"), "Kaleidoscope")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        PAX = REGISTRY.register(new PlainItemModel(VaultMod.id("gear/jewel/pax"), "Pax")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        BREACHING = REGISTRY.register(new PlainItemModel(VaultMod.id("gear/jewel/breaching"), "Breaching")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        FROST = REGISTRY.register(new PlainItemModel(VaultMod.id("gear/jewel/frost"), "Frost")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
    }
}
