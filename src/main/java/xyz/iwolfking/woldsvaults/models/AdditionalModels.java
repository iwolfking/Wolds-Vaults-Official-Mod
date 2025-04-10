package xyz.iwolfking.woldsvaults.models;

import iskallia.vault.VaultMod;
import iskallia.vault.dynamodel.DynamicModelProperties;
import iskallia.vault.dynamodel.model.item.HandHeldModel;
import iskallia.vault.dynamodel.model.item.PlainItemModel;
import iskallia.vault.dynamodel.model.item.shield.ShieldModel;
import iskallia.vault.init.ModDynamicModels;
import xyz.iwolfking.vhapi.VHAPI;


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
    public static final HandHeldModel ARROGANTE;
    public static final HandHeldModel WOLDIANCHOR;
    public static final HandHeldModel YOUNG_KITSUNE;
    public static final HandHeldModel MINERAL_GREATSWORD;
    public static final HandHeldModel GRASS_BLADE;
    public static final HandHeldModel MYTHIC_ZEKE_SWORD;
    public static final HandHeldModel SACRED_ZEKE_SWORD;
    public static final HandHeldModel PRETTY_SCISSORS;
    public static final HandHeldModel OLD_WORLD_AXE;
    public static final HandHeldModel FOUL_BLADE_AXE;
    public static final HandHeldModel KLK_SCISSOR_1;
    public static final HandHeldModel KLK_SCISSOR_2;
    public static final ShieldModel END_CRYSTAL_SHIELD;
    public static final PlainItemModel PURPLE_LIGHTNING;
    public static final PlainItemModel PURPLE_SOUL_WAND;
    public static final PlainItemModel HAUNTED_GRIMOIRE;
    public static final PlainItemModel WOLDS_TOME;
    public static final HandHeldModel HOTDOG_BAT;
    public static final HandHeldModel HYPER_CRYSTAL_SWORD;
    public static final HandHeldModel BRRYS_LUTE;
    public static final HandHeldModel CHEESEBLADE;
    public static final PlainItemModel CAT_WAND;
    public static final PlainItemModel MUSTARD;
    public static final PlainItemModel TINKERS_TANKARD;

    static {
       LEVIATHAN_AXE = ModDynamicModels.Axes.REGISTRY.register(new HandHeldModel(VaultMod.id("gear/axe/leviathan"), "Leviathan Axe")).properties(new DynamicModelProperties());
       BLOODSEEKING_MAGNET = ModDynamicModels.Magnets.REGISTRY_MAGNETS.register(new PlainItemModel(VaultMod.id("magnets/bloody_magnet"), "Bloodseeking Magnet")).properties(new DynamicModelProperties());
       OTHERWORLDLY_MAGNET = ModDynamicModels.Magnets.REGISTRY_MAGNETS.register(new PlainItemModel(VaultMod.id("magnets/ender_magnet"), "Otherworldly Magnet")).properties(new DynamicModelProperties());
       HEART_MAGNET = ModDynamicModels.Magnets.REGISTRY_MAGNETS.register(new PlainItemModel(VaultMod.id("magnets/heart_magnet"), "Heart Magnet")).properties(new DynamicModelProperties());
       TREASURE_MAGNET = ModDynamicModels.Magnets.REGISTRY_MAGNETS.register(new PlainItemModel(VaultMod.id("magnets/treasure_magnet"), "Treasure Seeker's Magnet")).properties(new DynamicModelProperties());
       EVERFROST = ModDynamicModels.Swords.REGISTRY.register(new HandHeldModel(VaultMod.id("gear/sword/everfrost"), "Everfrost Sword")).properties(new DynamicModelProperties());
       EVERFLAME = ModDynamicModels.Swords.REGISTRY.register(new HandHeldModel(VaultMod.id("gear/sword/everflame"), "Everflame Sword")).properties(new DynamicModelProperties());
       HEXBLADE = ModDynamicModels.Swords.REGISTRY.register(new HandHeldModel(VaultMod.id("gear/sword/hexblade"), "Hexblade")).properties(new DynamicModelProperties());
       HORSE_AXE = ModDynamicModels.Axes.REGISTRY.register(new HandHeldModel(VaultMod.id("gear/axe/zombie_horse"), "Zombie-Horse")).properties(new DynamicModelProperties());
       OLD_WORLD_AXE = ModDynamicModels.Axes.REGISTRY.register(new HandHeldModel(VaultMod.id("gear/axe/old_world_axe"), "Old World Axe")).properties(new DynamicModelProperties());
       FOUL_BLADE_AXE = ModDynamicModels.Axes.REGISTRY.register(new HandHeldModel(VaultMod.id("gear/axe/foul_blade_axe"), "Foul Blade Axe")).properties(new DynamicModelProperties());
       YOUNG_KITSUNE = ModDynamicModels.Swords.REGISTRY.register(new HandHeldModel(VaultMod.id("gear/sword/young_kitsune"), "Young Kitsune")).properties(new DynamicModelProperties());
       ARROGANTE = ModDynamicModels.Axes.REGISTRY.register(new HandHeldModel(VaultMod.id("gear/axe/arrogante"), "Arrogante Axe")).properties(new DynamicModelProperties());
       WOLDIANCHOR = ModDynamicModels.Axes.REGISTRY.register(new HandHeldModel(VHAPI.of("gear/axe/iskallianchor"), "Woldianchor")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
       YOUNG_KITSUNE = ModDynamicModels.Swords.REGISTRY.register(new HandHeldModel(VaultMod.id("gear/sword/young_kitsune"), "Young Kitsune Blade")).properties(new DynamicModelProperties());
       MINERAL_GREATSWORD = ModDynamicModels.Swords.REGISTRY.register(new HandHeldModel(VaultMod.id("gear/sword/mineral_greatsword"), "Mineral Greatsword")).properties(new DynamicModelProperties());
       GRASS_BLADE = ModDynamicModels.Swords.REGISTRY.register(new HandHeldModel(VaultMod.id("gear/sword/grass_blade"), "Leaf Sword")).properties(new DynamicModelProperties());
       MYTHIC_ZEKE_SWORD = ModDynamicModels.Swords.REGISTRY.register(new HandHeldModel(VaultMod.id("gear/sword/mythic_zeke_sword"), "Mythical Blade")).properties(new DynamicModelProperties());
       SACRED_ZEKE_SWORD = ModDynamicModels.Swords.REGISTRY.register(new HandHeldModel(VaultMod.id("gear/sword/sacred_zeke_sword"), "Sacred Blade")).properties(new DynamicModelProperties());
       PRETTY_SCISSORS = ModDynamicModels.Swords.REGISTRY.register(new HandHeldModel(VaultMod.id("gear/sword/aurora_scissors"), "Aurora Scissors")).properties(new DynamicModelProperties());
       KLK_SCISSOR_1 = ModDynamicModels.Swords.REGISTRY.register(new HandHeldModel(VaultMod.id("gear/sword/klk_scissor_1"), "Red Scissor Half")).properties(new DynamicModelProperties());
       KLK_SCISSOR_2 = ModDynamicModels.Swords.REGISTRY.register(new HandHeldModel(VaultMod.id("gear/sword/klk_scissor_2"), "Purple Scissor Half")).properties(new DynamicModelProperties());
       PURPLE_LIGHTNING = ModDynamicModels.Wands.REGISTRY.register(new PlainItemModel(VaultMod.id("gear/wand/purple_lightning"), "Arcane Lightning Wand")).properties(new DynamicModelProperties());
       PURPLE_SOUL_WAND = ModDynamicModels.Wands.REGISTRY.register(new PlainItemModel(VaultMod.id("gear/wand/purple_soul_wand"), "Arcane Soul Wand")).properties(new DynamicModelProperties());
       HAUNTED_GRIMOIRE = ModDynamicModels.Focus.REGISTRY.register(new PlainItemModel(VaultMod.id("gear/focus/haunted_grimoire"), "Haunted Grimoire")).properties(new DynamicModelProperties());
       WOLDS_TOME = ModDynamicModels.Focus.REGISTRY.register(new PlainItemModel(VaultMod.id("gear/focus/wolds_tome"), "Wold's Tome")).properties(new DynamicModelProperties());
       END_CRYSTAL_SHIELD = ModDynamicModels.Shields.REGISTRY.register(new ShieldModel(VaultMod.id("gear/shield/end_crystal_shield"), "End Crystal Shield")).properties(new DynamicModelProperties());
       HOTDOG_BAT = ModDynamicModels.Swords.REGISTRY.register(new HandHeldModel(VHAPI.of("gear/sword/hotdog"), "Hotdog Bat")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
       HYPER_CRYSTAL_SWORD = ModDynamicModels.Swords.REGISTRY.register(new HandHeldModel(VHAPI.of("gear/sword/crystal"), "Hyper Crystal Sword")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
       BRRYS_LUTE = ModDynamicModels.Swords.REGISTRY.register(new HandHeldModel(VHAPI.of("gear/sword/lute"), "Brry's Lute")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
       CHEESEBLADE = ModDynamicModels.Swords.REGISTRY.register(new HandHeldModel(VHAPI.of("gear/sword/cheese"), "Cheeseblade")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
       CAT_WAND = ModDynamicModels.Wands.REGISTRY.register(new PlainItemModel(VHAPI.of("gear/wand/cat"), "Cat Wand")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
       MUSTARD = ModDynamicModels.Wands.REGISTRY.register(new PlainItemModel(VHAPI.of("gear/wand/mustard"), "Mustard Bottle")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
       TINKERS_TANKARD = ModDynamicModels.Focus.REGISTRY.register(new PlainItemModel(VaultMod.id("gear/focus/tinkers_tankard"), "Tinker's Tankard")).properties(new DynamicModelProperties().allowTransmogrification().discoverOnRoll());
    }

}
