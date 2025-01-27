package xyz.iwolfking.woldsvaults.models;

import iskallia.vault.VaultMod;
import iskallia.vault.dynamodel.DynamicModelProperties;
import iskallia.vault.dynamodel.model.item.PlainItemModel;
import iskallia.vault.dynamodel.registry.DynamicModelRegistry;

public class Maps {
    public static final DynamicModelRegistry<PlainItemModel> REGISTRY = new DynamicModelRegistry<>();
    //public static final PlainItemModel STANDARD_MAP;
    public static final PlainItemModel SCRAPPY_MAP;
    public static final PlainItemModel COMMON_MAP;
    public static final PlainItemModel RARE_MAP;
    public static final PlainItemModel EPIC_MAP;
    public static final PlainItemModel OMEGA_MAP;
    public static final PlainItemModel MYTHIC_MAP;
    public static final PlainItemModel SACRED_MAP;
    public static final PlainItemModel CORRUPTED_MAP;

    static {
        //STANDARD_MAP = (PlainItemModel)REGISTRY.register((new PlainItemModel(VaultMod.id("gear/map/standard"), "Standard Map")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll()));
        SCRAPPY_MAP = (PlainItemModel)REGISTRY.register((new PlainItemModel(VaultMod.id("gear/map/scrappy_map"), "Scrappy Map")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll()));
        COMMON_MAP = (PlainItemModel)REGISTRY.register((new PlainItemModel(VaultMod.id("gear/map/common_map"), "Common Map")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll()));
        RARE_MAP = (PlainItemModel)REGISTRY.register((new PlainItemModel(VaultMod.id("gear/map/rare_map"), "Rare Map")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll()));
        EPIC_MAP = (PlainItemModel)REGISTRY.register((new PlainItemModel(VaultMod.id("gear/map/epic_map"), "Epic Map")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll()));
        OMEGA_MAP = (PlainItemModel)REGISTRY.register((new PlainItemModel(VaultMod.id("gear/map/omega_map"), "Omega Map")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll()));
        MYTHIC_MAP = (PlainItemModel)REGISTRY.register((new PlainItemModel(VaultMod.id("gear/map/mythic_map"), "Mythic Map")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll()));
        SACRED_MAP = (PlainItemModel)REGISTRY.register((new PlainItemModel(VaultMod.id("gear/map/sacred_map"), "Sacred Map")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll()));
        CORRUPTED_MAP = (PlainItemModel)REGISTRY.register((new PlainItemModel(VaultMod.id("gear/map/corrupted_map"), "Corrupted Map")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll()));
    }
}
