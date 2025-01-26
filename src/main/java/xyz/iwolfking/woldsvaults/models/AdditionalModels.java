package xyz.iwolfking.woldsvaults.models;

import iskallia.vault.VaultMod;
import iskallia.vault.dynamodel.DynamicModelProperties;
import iskallia.vault.dynamodel.model.item.HandHeldModel;
import iskallia.vault.dynamodel.model.item.PlainItemModel;
import iskallia.vault.init.ModDynamicModels;


public class AdditionalModels {

    public static final HandHeldModel LEVIATHAN_AXE;
    public static final PlainItemModel BLOODSEEKING_MAGNET;
    public static final PlainItemModel OTHERWORLDLY_MAGNET;
    public static final PlainItemModel HEART_MAGNET;
    public static final PlainItemModel TREASURE_MAGNET;
    public static final HandHeldModel EVERFROST;
    public static final HandHeldModel EVERFLAME;
    public static final HandHeldModel HEXBLADE;
    public static final HandHeldModel HORSE_AXE;
    public static final HandHeldModel YOUNG_KITSUNE;
    public static final HandHeldModel MINERAL_GREATSWORD;
    public static final HandHeldModel GRASS_BLADE;
    public static final HandHeldModel MYTHIC_ZEKE_SWORD;
    public static final HandHeldModel SACRED_ZEKE_SWORD;
    public static final HandHeldModel PRETTY_SCISSORS;

    static {
       LEVIATHAN_AXE = ModDynamicModels.Axes.REGISTRY.register(new HandHeldModel(VaultMod.id("gear/axe/leviathan"), "Leviathan")).properties(new DynamicModelProperties());
       BLOODSEEKING_MAGNET = ModDynamicModels.Magnets.REGISTRY_MAGNETS.register(new PlainItemModel(VaultMod.id("magnets/bloody_magnet"), "Bloodseeking Magnet")).properties(new DynamicModelProperties());
       OTHERWORLDLY_MAGNET = ModDynamicModels.Magnets.REGISTRY_MAGNETS.register(new PlainItemModel(VaultMod.id("magnets/ender_magnet"), "Otherworldly Magnet")).properties(new DynamicModelProperties());
       HEART_MAGNET = ModDynamicModels.Magnets.REGISTRY_MAGNETS.register(new PlainItemModel(VaultMod.id("magnets/heart_magnet"), "Heart Magnet")).properties(new DynamicModelProperties());
       TREASURE_MAGNET = ModDynamicModels.Magnets.REGISTRY_MAGNETS.register(new PlainItemModel(VaultMod.id("magnets/treasure_magnet"), "Treasure Seeker's Magnet")).properties(new DynamicModelProperties());
       EVERFROST = ModDynamicModels.Swords.REGISTRY.register(new HandHeldModel(VaultMod.id("gear/sword/everfrost"), "Everfrost")).properties(new DynamicModelProperties());
       EVERFLAME = ModDynamicModels.Swords.REGISTRY.register(new HandHeldModel(VaultMod.id("gear/sword/everflame"), "Everflame")).properties(new DynamicModelProperties());
       HEXBLADE = ModDynamicModels.Swords.REGISTRY.register(new HandHeldModel(VaultMod.id("gear/sword/hexblade"), "Hexblade")).properties(new DynamicModelProperties());
       HORSE_AXE = ModDynamicModels.Axes.REGISTRY.register(new HandHeldModel(VaultMod.id("gear/axe/zombie_horse"), "Zombie-Horse")).properties(new DynamicModelProperties());
       YOUNG_KITSUNE = ModDynamicModels.Swords.REGISTRY.register(new HandHeldModel(VaultMod.id("gear/sword/young_kitsune"), "Young Kitsune")).properties(new DynamicModelProperties());
       MINERAL_GREATSWORD = ModDynamicModels.Swords.REGISTRY.register(new HandHeldModel(VaultMod.id("gear/sword/mineral_greatsword"), "Mineral Greatsword")).properties(new DynamicModelProperties());
       GRASS_BLADE = ModDynamicModels.Swords.REGISTRY.register(new HandHeldModel(VaultMod.id("gear/sword/grass_blade"), "Leaf Sword")).properties(new DynamicModelProperties());
       MYTHIC_ZEKE_SWORD = ModDynamicModels.Swords.REGISTRY.register(new HandHeldModel(VaultMod.id("gear/sword/mythic_zeke_sword"), "Mythical Blade")).properties(new DynamicModelProperties());
       SACRED_ZEKE_SWORD = ModDynamicModels.Swords.REGISTRY.register(new HandHeldModel(VaultMod.id("gear/sword/sacred_zeke_sword"), "Sacred Blade")).properties(new DynamicModelProperties());
       PRETTY_SCISSORS = ModDynamicModels.Swords.REGISTRY.register(new HandHeldModel(VaultMod.id("gear/sword/aurora_scissors"), "Aurora Scissors")).properties(new DynamicModelProperties());
    }

}
