package xyz.iwolfking.woldsvaults.models;

import iskallia.vault.VaultMod;
import iskallia.vault.dynamodel.DynamicModelProperties;
import iskallia.vault.dynamodel.model.item.PlainItemModel;
import iskallia.vault.dynamodel.registry.DynamicModelRegistry;

public class Rangs {
    public static final DynamicModelRegistry<PlainItemModel> REGISTRY = new DynamicModelRegistry<>();
    public static final PlainItemModel WOODEN_RANG;
    public static final PlainItemModel AMETHYST;
    public static final PlainItemModel CRIMSON;
    public static final PlainItemModel DIAMOND;
    public static final PlainItemModel EMERALD;
    public static final PlainItemModel GOLD;
    public static final PlainItemModel OURORANGOS;
    public static final PlainItemModel STONE;
    public static final PlainItemModel TRIRANG;
    public static final PlainItemModel MIDNIGHT;
    public static final PlainItemModel PRISMATIC;


    static {
        WOODEN_RANG = REGISTRY.register(new PlainItemModel(VaultMod.id("gear/rang/wooden"), "Wooden Rang")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        AMETHYST = REGISTRY.register(new PlainItemModel(VaultMod.id("gear/rang/amethyst"), "Amethyst Rang")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        CRIMSON = REGISTRY.register(new PlainItemModel(VaultMod.id("gear/rang/crimson"), "Crimson Rang")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        DIAMOND = REGISTRY.register(new PlainItemModel(VaultMod.id("gear/rang/diamond"), "Diamond Rang")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        EMERALD = REGISTRY.register(new PlainItemModel(VaultMod.id("gear/rang/emerald"), "Emerald Rang")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        GOLD = REGISTRY.register(new PlainItemModel(VaultMod.id("gear/rang/gold"), "Golden Rang")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        OURORANGOS = REGISTRY.register(new PlainItemModel(VaultMod.id("gear/rang/ourorangos"), "Ourorangos")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        STONE = REGISTRY.register(new PlainItemModel(VaultMod.id("gear/rang/stone"), "Stone Rang")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        PRISMATIC = REGISTRY.register(new PlainItemModel(VaultMod.id("gear/rang/prismatic"), "Prismatic Rang")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        MIDNIGHT = REGISTRY.register(new PlainItemModel(VaultMod.id("gear/rang/midnight"), "Midnight Rang")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        TRIRANG = REGISTRY.register(new PlainItemModel(VaultMod.id("gear/rang/trirang"), "Tri-Rang")).properties(new DynamicModelProperties());
    }
}
