package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.config.UniqueCodexConfig;
import iskallia.vault.init.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.vhapi.api.datagen.AbstractUniqueGearProvider;
import xyz.iwolfking.vhapi.api.datagen.lib.gear.UniqueGearBuilder;
import xyz.iwolfking.vhapi.api.datagen.lib.gear.UniqueGearEntry;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import java.util.function.Consumer;

public class ModUniqueGearProvider extends AbstractUniqueGearProvider {
    public ModUniqueGearProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    protected void addGear(Consumer<AbstractUniqueGearProvider.GeneratedEntry> consumer) {
        consumer.accept(entry("woldsvaults:ocean_current", new UniqueGearBuilder("Ocean Current", xyz.iwolfking.woldsvaults.init.ModItems.TRIDENT)
                .model("woldsvaults:gear/trident/ocean_current")
                .base("the_vault:base_durability")
                .implicit("the_vault:base_armor",
                        "the_vault:jester_lucky_hit",
                        "the_vault:base_lucky_hit_chance",
                        "the_vault:base_lucky_hit_chance")
                .suffix("the_vault:u_item_rarity")
                .dropLocation("Nowhere")
                .description("This is a test unique!!!", "$text")
                .description("\n\nIt is very testy", "$text")
                .modelType("TRIDENT")
                .slotType(UniqueCodexConfig.IntroductionPage.SlotType.SWORD)
                .build()
        ));

        consumer.accept(entry("woldsvaults:shattering_jewel", new UniqueGearBuilder("Shatterering Jewel", ModItems.JEWEL)
                .model("the_vault:gear/jewel/treasure")
                .implicit("the_vault:u_jewel_size_stella")
                .prefix("the_vault:breaching_jewel")
                .dropLocation("Gateways")
                .description("A very special jewel that makes all ", "$text")
                .description("sliceable", "$titleLine")
                .description("chests be broken the same way as ", "$text")
                .description("Wooden ", "$wooden")
                .description("chests (break in one go)", "$text")
                .modelType("JEWEL")
                .slotType(UniqueCodexConfig.IntroductionPage.SlotType.JEWEL)
                .build()
        ));

        consumer.accept(entry("woldsvaults:eternal_stella", new UniqueGearBuilder("Eternal Stella", ModItems.JEWEL)
                .model("the_vault:gear/jewel/stella")
                .implicit("the_vault:u_jewel_size_stella")
                .prefix("the_vault:u_immortality")
                .dropLocation("Black Market")
                .description("This lovely jewel gives your ", "$text")
                .description("Vault Tool ", "$titleLine")
                .description("100% ", "$text")
                .description("Vanilla Immortality", "$impossible")
                .description(", making it no longer take durability in the overworld.", "$text")
                .modelType("JEWEL")
                .slotType(UniqueCodexConfig.IntroductionPage.SlotType.JEWEL)
                .build()
        ));

        consumer.accept(entry("woldsvaults:treasure_jewel", new UniqueGearBuilder("Treasured Jewel", ModItems.JEWEL)
                .model("the_vault:gear/jewel/treasure")
                .implicit("the_vault:u_jewel_size_treasure")
                .prefix("the_vault:u_treasure_affinity")
                .dropLocation("Craftable")
                .description("This jewel gives your tool affinity towards ", "$text")
                .description("Treasure Chests ", "$titleLine")
                .description("allowing you to mine them instead of having to open them by hand!", "$text")
                .modelType("JEWEL")
                .slotType(UniqueCodexConfig.IntroductionPage.SlotType.JEWEL)
                .build()
        ));

        consumer.accept(entry("woldsvaults:wicked_witch", new UniqueGearBuilder("The Wicked Witch", xyz.iwolfking.woldsvaults.init.ModItems.PLUSHIE)
                .model("the_vault:gear/plushie/witch")
                .base("the_vault:base_durability")
                .prefix("the_vault:u_witch_effect_duration", "the_vault:u_witch_cooldown_reduction")
                .suffix("the_vault:u_witch_effect_avoidance")
                .dropLocation("Laboratory")
                .description("This funky Plushie gives you much greater ", "$text")
                .description("Effect Duration ", "$duration")
                .description("and ", "$text")
                .description("Effect Avoidance ", "$ethereal")
                .description("with the downside of lowering your ", "$text")
                .description("Cooldown Reduction", "$cooldown")
                .modelType("PLUSHIE")
                .slotType(UniqueCodexConfig.IntroductionPage.SlotType.OFFHAND)
                .build()
        ));

        consumer.accept(entry("woldsvaults:chonknet", new UniqueGearBuilder("Chonknet", ModItems.MAGNET)
                .model("the_vault:magnets/heart_magnet")
                .base("the_vault:base_magnet_durability")
                .implicit("the_vault:u_base_magnet_range", "the_vault:base_endergized")
                .prefix("the_vault:u_chunky_magnet_health", "the_vault:u_chunky_magnet_healing")
                .suffix("the_vault:u_chunky_magnet_item_quantity", "the_vault:u_chunky_magnet_item_rarity")
                .dropLocation("Bounty Reward")
                .description("This thick Magnet gives you much greater ", "$text")
                .description("Max Health ", "$healthPoints")
                .description("and ", "$text")
                .description("Healing Efficiency ", "$heal")
                .description("with the downside of lowering your ", "$text")
                .description("Item Quantity ", "$itemQuantity")
                .description("and ", "$text")
                .description("Item Rarity ", "$itemRarity")
                .modelType("MAGNETS")
                .slotType(UniqueCodexConfig.IntroductionPage.SlotType.MAGNET)
                .build()
        ));

        consumer.accept(entry("woldsvaults:treasure_magnet", new UniqueGearBuilder("Treasure Hunter's Magnet", ModItems.MAGNET)
                .model("the_vault:magnets/treasure_magnet")
                .base("the_vault:base_magnet_durability")
                .implicit("the_vault:u_base_magnet_range", "the_vault:base_endergized")
                .prefix("the_vault:u_treasure_magnet_item_rarity", "the_vault:u_treasure_magnet_item_quantity")
                .suffix("the_vault:u_treasure_magnet_health", "the_vault:u_treasure_magnet_mana")
                .dropLocation("Bounty Reward")
                .description("This powerful Magnet gives you much greater ", "$text")
                .description("Item Quantity ", "$itemQuantity")
                .description("and ", "$text")
                .description("Item Rarity" , "$itemRarity")
                .description("with the downside of lowering your ", "$text")
                .description("Max Health ", "$healthPoints")
                .description("and ", "$text")
                .description("Max Mana ", "$manaCost")
                .modelType("MAGNETS")
                .slotType(UniqueCodexConfig.IntroductionPage.SlotType.MAGNET)
                .build()
        ));

        consumer.accept(entry("woldsvaults:bloodseeking_magnet", new UniqueGearBuilder("Bloodfetcher", ModItems.MAGNET)
                .model("the_vault:magnets/bloody_magnet")
                .base("the_vault:base_magnet_durability")
                .implicit("the_vault:u_base_magnet_range", "the_vault:base_endergized")
                .prefix("the_vault:u_bloody_magnet_increase", "the_vault:u_bloody_magnet_leech")
                .suffix("the_vault:u_bloody_magnet_trap_disarm", "the_vault:u_bloody_magnet_mana_regen")
                .dropLocation("Bounty Reward")
                .description("This deadly Magnet gives you hit harder and leech life back on hit ", "$text")
                .description("with the downside of lowering your ", "$text")
                .description("Trap Disarm ", "$duration")
                .description("and ", "$text")
                .description("Mana Regen ", "$manaCost")
                .modelType("MAGNETS")
                .slotType(UniqueCodexConfig.IntroductionPage.SlotType.MAGNET)
                .build()
        ));

        consumer.accept(entry("woldsvaults:leviathan", new UniqueGearBuilder("Leviathan", ModItems.AXE)
                .model("the_vault:gear/axe/leviathan")
                .base("the_vault:base_durability")
                .implicit("the_vault:u_leviathan_attack_damage", "the_vault:u_leviathan_attack_speed")
                .prefix("the_vault:u_leviathan_reaving_damage")
                .dropLocation("Bounty Reward")
                .description("This heavy hitter of an Axe hits for massive max health damage ", "$text")
                .description("but is extremely slow! ", "$text")
                .modelType("AXE")
                .slotType(UniqueCodexConfig.IntroductionPage.SlotType.AXE)
                .build()
        ));

        consumer.accept(entry("woldsvaults:zombie_horse_axe", new UniqueGearBuilder("Zombie Horse Axe", ModItems.AXE)
                .model("the_vault:gear/axe/zombie_horse")
                .base("the_vault:base_durability")
                .implicit("the_vault:u_leviathan_attack_damage", "the_vault:base_attack_speed_axe")
                .prefix("the_vault:u_movement_horse", "the_vault:mod_attack_range")
                .suffix("the_vault:mod_poison_cloud_zombie_horse")
                .dropLocation("Graveyard Room")
                .description("This spooky Axe can put out extremely deadly Poison clouds and makes you move faster!", "$text")
                .modelType("AXE")
                .slotType(UniqueCodexConfig.IntroductionPage.SlotType.AXE)
                .build()
        ));

        consumer.accept(entry("woldsvaults:trirang", new UniqueGearBuilder("Tri-Rang", xyz.iwolfking.woldsvaults.init.ModItems.RANG)
                .model("the_vault:gear/rang/trirang")
                .base("the_vault:base_durability")
                .implicit("the_vault:rang_standard_attack_speed", "the_vault:rang_standard_attack_damage",  "the_vault:rang_standard_range",  "the_vault:rang_standard_velocity")
                .prefix("the_vault:trirang_chaining", "the_vault:trirang_piercing", "the_vault:trirang_returning")
                .suffix("the_vault:trirang_lucky_hit", "the_vault:trirang_stun", "the_vault:trirang_shock")
                .dropLocation("Bounty Reward")
                .description("This numeric obsessed Vaultarang can do quite a bit of damage!", "$text")
                .modelType("RANG")
                .slotType(UniqueCodexConfig.IntroductionPage.SlotType.SWORD)
                .build()
        ));

        consumer.accept(entry("woldsvaults:everfrost", new UniqueGearBuilder("Everfrost", ModItems.SWORD)
                .model("the_vault:gear/sword/everfrost")
                .base("the_vault:base_durability")
                .implicit("the_vault:base_attack_damage", "the_vault:base_attack_speed")
                .prefix("the_vault:mod_attack_damage", "the_vault:mod_everfrost_blast")
                .suffix("the_vault:mod_area_of_effect", "the_vault:unique_glacial_hypothermia")
                .dropLocation("Vendoors")
                .description("This frozen Sword boosts the power of your Glacial Blast!", "$text")
                .modelType("SWORD")
                .slotType(UniqueCodexConfig.IntroductionPage.SlotType.SWORD)
                .build()
        ));

        consumer.accept(entry("woldsvaults:everflame", new UniqueGearBuilder("Everflame", ModItems.SWORD)
                .model("the_vault:gear/sword/everflame")
                .base("the_vault:base_durability")
                .implicit("the_vault:base_attack_damage", "the_vault:base_attack_speed")
                .prefix("the_vault:mod_attack_damage", "the_vault:mod_everflame_fireball")
                .suffix("the_vault:mod_area_of_effect", "the_vault:unique_everflame_modification")
                .dropLocation("Vendoors")
                .description("This flaming Sword gives you the ability to shoot TWO fireballs!", "$text")
                .modelType("SWORD")
                .slotType(UniqueCodexConfig.IntroductionPage.SlotType.SWORD)
                .build()
        ));

        consumer.accept(entry("woldsvaults:hexblade", new UniqueGearBuilder("Hexblade", ModItems.SWORD)
                .model("the_vault:gear/sword/hexblade")
                .base("the_vault:base_durability")
                .implicit("the_vault:base_attack_damage", "the_vault:base_attack_speed")
                .prefix("the_vault:mod_hexing_chance", "the_vault:mod_ability_increase")
                .suffix("the_vault:mod_attack_speed_percent", "the_vault:mod_attack_range")
                .dropLocation("Vendoors")
                .description("This Sword can apply a myriad of negative effects to your foes!", "$text")
                .modelType("SWORD")
                .slotType(UniqueCodexConfig.IntroductionPage.SlotType.SWORD)
                .build()
        ));

        consumer.accept(entry("woldsvaults:mineral_greatsword", new UniqueGearBuilder("Mineral Greatsword", ModItems.SWORD)
                .model("the_vault:gear/sword/mineral_greatsword")
                .base("the_vault:base_durability")
                .implicit("the_vault:base_attack_damage", "the_vault:base_attack_speed_greatsword")
                .prefix("the_vault:mod_attack_damage_mineral_greatsword")
                .suffix("the_vault:mineral_greatsword_stun", "the_vault:mod_attack_range")
                .dropLocation("Vendoors")
                .description("This colossal Sword can hit hard and stun your foes!", "$text")
                .modelType("SWORD")
                .slotType(UniqueCodexConfig.IntroductionPage.SlotType.SWORD)
                .build()
        ));

        consumer.accept(entry("woldsvaults:grass_sword", new UniqueGearBuilder("Grass Sword", ModItems.SWORD)
                .model("the_vault:gear/sword/grass_blade")
                .base("the_vault:base_durability")
                .implicit("the_vault:base_attack_damage", "the_vault:base_attack_speed")
                .prefix("the_vault:base_thorns_damage_flat_sword", "the_vault:u_grass_sword_healing_eff")
                .suffix("the_vault:thorns_scaling_damage_grass_sword")
                .dropLocation("Vendoors")
                .description("This lovely Sword can use your Thorns Damage to deal additional damage on-hit!", "$text")
                .modelType("SWORD")
                .slotType(UniqueCodexConfig.IntroductionPage.SlotType.SWORD)
                .build()
        ));

        consumer.accept(entry("woldsvaults:aurora_scissors", new UniqueGearBuilder("Aurora Scissors", ModItems.SWORD)
                .model("the_vault:gear/sword/aurora_scissors")
                .base("the_vault:base_durability")
                .implicit("the_vault:base_attack_damage", "the_vault:base_attack_speed")
                .prefix("the_vault:mod_ap", "the_vault:mod_soul_chance_scissors")
                .suffix("the_vault:ap_scaling_damage_scissors", "the_vault:mod_attack_speed_percent")
                .dropLocation("Black Market")
                .description("This lovely Sword can use your Ability Power to deal additional damage on-hit!", "$text")
                .modelType("SWORD")
                .slotType(UniqueCodexConfig.IntroductionPage.SlotType.SWORD)
                .build()
        ));

        consumer.accept(entry("woldsvaults:young_kitsune", new UniqueGearBuilder("Young Kitsune Blade", ModItems.SWORD)
                .model("the_vault:gear/sword/young_kitsune")
                .base("the_vault:base_durability")
                .implicit("the_vault:base_attack_damage", "the_vault:base_attack_speed")
                .prefix("the_vault:mod_execution_young_kitsune")
                .suffix("the_vault:mod_attack_speed_percent")
                .dropLocation("Vendoors")
                .description("The weaker they are, the harder they fall!", "$text")
                .modelType("SWORD")
                .slotType(UniqueCodexConfig.IntroductionPage.SlotType.SWORD)
                .build()
        ));

        consumer.accept(entry("woldsvaults:safer_spaces", new UniqueGearBuilder("Safer Spaces", xyz.iwolfking.woldsvaults.init.ModItems.PLUSHIE)
                .model("the_vault:gear/plushie/safer_spaces")
                .base("the_vault:base_durability")
                .implicit("the_vault:saferspaceeffect", "the_vault:u_safer_block", "the_vault:u_safer_block")
                .dropLocation("Black Market")
                .description("???", "$text")
                .modelType("PLUSHIE")
                .slotType(UniqueCodexConfig.IntroductionPage.SlotType.OFFHAND)
                .build()
        ));

        consumer.accept(entry("woldsvaults:bamboo_fightstick", new UniqueGearBuilder("Bamboo Fightstick", xyz.iwolfking.woldsvaults.init.ModItems.BATTLESTAFF)
                .model("the_vault:gear/battlestaff/bamboo_fightstick")
                .base("the_vault:base_durability")
                .implicit("the_vault:base_attack_damage_battlestaff_stick", "the_vault:base_lucky_hit_battlestaff_stick", "the_vault:base_attack_speed_battlestaff_stick", "the_vault:base_attack_range_battlestaff_stick")
                .dropLocation("Vendoors")
                .description("Trigger Lucky Hits extremely often with this weak stick!", "$text")
                .modelType("BATTLESTAFF")
                .slotType(UniqueCodexConfig.IntroductionPage.SlotType.SWORD)
                .build()
        ));

        consumer.accept(entry("the_vault:chainlash", new UniqueGearBuilder("Chainlash", ModItems.WAND)
                .model("the_vault:gear/wand/chaining_wand")
                .base("the_vault:offhand_base_durability")
                .implicit("the_vault:u_chaining")
                .prefix("the_vault:u_chaining_damage")
                .suffix("the_vault:unique_area_of_effect")
                .dropLocation("The Laboratory")
                .description("This weird wand empowers your on-hit attacks by ", "$text")
                .description("chaining ", "$chains")
                .description("them, increasing your reach greatly. ", "$text")
                .description("It also decreases the amount of damage loss that ", "$text")
                .description("chaining ", "$chains")
                .description("usually entails. ", "$text")
                .modelType("WAND")
                .slotType(UniqueCodexConfig.IntroductionPage.SlotType.WAND)
                .build()
        ));

        consumer.accept(entry("the_vault:baguette", new UniqueGearBuilder("The Baguette", ModItems.WAND)
                .model("the_vault:gear/wand/baguette")
                .base("the_vault:base_durability")
                .implicit("the_vault:u_saturation", "the_vault:u_regeneration")
                .suffix("the_vault:u_baguette_healingeff")
                .dropLocation("The Laboratory")
                .description("Hungry? This freshly baked ", "$text")
                .description("Baguette ", "#7D520E")
                .description("will taste good at any time. ", "$text")
                .modelType("WAND")
                .slotType(UniqueCodexConfig.IntroductionPage.SlotType.WAND)
                .build()
        ));
    }

    private GeneratedEntry entry(String id, UniqueGearEntry data) {
        return new GeneratedEntry(new ResourceLocation(id), data);
    }
}
