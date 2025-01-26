package xyz.iwolfking.woldsvaults.models;

import iskallia.vault.VaultMod;
import iskallia.vault.dynamodel.DynamicModelProperties;
import iskallia.vault.dynamodel.model.item.HandHeldModel;
import iskallia.vault.dynamodel.registry.DynamicModelRegistry;

public class Battlestaffs {
    public static final DynamicModelRegistry<HandHeldModel> REGISTRY = new DynamicModelRegistry<>();
    public static final HandHeldModel BATTLESTAFF_0;
    public static final HandHeldModel BATTLESTAFF_COPPER;
    public static final HandHeldModel BATTLESTAFF_LAPIS;
    public static final HandHeldModel BATTLESTAFF_REDSTONE;
    public static final HandHeldModel BATTLESTAFF_GROWING;
    public static final HandHeldModel BATTLESTAFF_GOLDEN;
    public static final HandHeldModel BATTLESTAFF_EMERALD;
    public static final HandHeldModel BATTLESTAFF_DIAMOND;
    public static final HandHeldModel BATTLESTAFF_PURPLE;
    public static final HandHeldModel BATTLESTAFF_TERROR;
    public static final HandHeldModel BATTLESTAFF_LIGHT_BLUE;
    public static final HandHeldModel BATTLESTAFF_LIGHTSABER;
    public static final HandHeldModel BAMBOO_FIGHTSTICK;
    public static final HandHeldModel NINJA_BATON;


    public Battlestaffs() {
    }

    static {
        BATTLESTAFF_0 = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/battlestaff/battlestaff_0"), "Battlestaff_0")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        BATTLESTAFF_COPPER = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/battlestaff/battlestaff_copper"), "Copper Battlestaff")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        BATTLESTAFF_LAPIS = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/battlestaff/battlestaff_lapis"), "Lapis Battlestaff")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        BATTLESTAFF_REDSTONE = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/battlestaff/battlestaff_redstone"), "Redstone Battlestaff")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        BATTLESTAFF_GROWING = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/battlestaff/battlestaff_growing"), "Growing Battlestaff")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        BATTLESTAFF_GOLDEN = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/battlestaff/battlestaff_golden"), "Golden Battlestaff")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        BATTLESTAFF_EMERALD = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/battlestaff/battlestaff_emerald"), "Emerald Battlestaff")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        BATTLESTAFF_DIAMOND = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/battlestaff/battlestaff_diamond"), "Diamond Battlestaff")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        BATTLESTAFF_PURPLE = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/battlestaff/battlestaff_purple"), "Purple Battlestaff")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        BATTLESTAFF_TERROR = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/battlestaff/battlestaff_terror"), "Terror Battlestaff")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        BATTLESTAFF_LIGHT_BLUE = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/battlestaff/battlestaff_light_blue"), "Arcane Battlestaff")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        BATTLESTAFF_LIGHTSABER = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/battlestaff/lightsaber"), "Cosmonite's Battlesaber")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        BAMBOO_FIGHTSTICK = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/battlestaff/bamboo_fightstick"), "Bamboo Fightstick")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
        NINJA_BATON = REGISTRY.register(new HandHeldModel(VaultMod.id("gear/battlestaff/ninja_baton"), "Ninja Baton")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
    }
}
