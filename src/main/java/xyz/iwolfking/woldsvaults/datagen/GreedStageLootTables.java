package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.vhapi.api.datagen.gen.AbstractLootTableProvider.EntryBuilder;
import xyz.iwolfking.vhapi.api.datagen.gen.AbstractLootTableProvider.LootBuilder;
import xyz.iwolfking.vhapi.api.datagen.gen.AbstractLootTableProvider.PoolBuilder;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * The loot tables of the greed stages, registered into {@link ModVaultLootTablesProvider}. Stage 1 holds the
 * four-pool enigma chest. Stage 2 holds the chest, strongbox, enigma, dungeon barrel, dungeon boss and dungeon
 * pedestal tables. Stage 3 holds the treasure chest, god treasure chest and treasure pillar tables. The stage-2
 * chests share a five-pool shape whose last pool is the mythic pool.
 */
public final class GreedStageLootTables {
    /** The roll-type pool of stage chest gear; added to the base roll-type pools by a config patch. */
    public static final String STAGE_LOOT_ROLLS = "greed_stage_loot";
    /** The roll-type pool of stage dungeon gear. */
    public static final String STAGE_DUNGEON_ROLLS = "greed_stage_dungeon";
    /** The roll-type pool of stage treasure gear. */
    public static final String STAGE_TREASURE_ROLLS = "greed_stage_treasure";

    private static final String ROLL_TYPE_POOL = "the_vault:gear_roll_type_pool";
    private static final double MYTHIC_POOL_WEIGHT = 0.00445D;

    private GreedStageLootTables() {
    }

    public static void register(BiConsumer<ResourceLocation, Consumer<LootBuilder>> add) {
        lootOrnateChest(add);
        lootGildedChest(add);
        lootLivingChest(add);
        lootWoodenChest(add);
        lootEnigmaChests(add);
        lootOrnateStrongbox(add);
        lootGildedStrongbox(add);
        lootLivingStrongbox(add);
        lootDungeonBoss(add);
        lootDungeonPedestal(add);
        lootWoodenDungeonBarrel(add);
        lootGildedDungeonBarrel(add);
        lootLivingDungeonBarrel(add);
        lootOrnateDungeonBarrel(add);
        lootTreasureChest(add);
        lootTreasureChestIdona(add);
        lootTreasureChestTenos(add);
        lootTreasureChestVelara(add);
        lootTreasureChestWendarr(add);
        lootTreasurePillar(add);
    }

    private static void lootOrnateChest(BiConsumer<ResourceLocation, Consumer<LootBuilder>> add) {
        add.accept(GreedStageIds.id(2, "ornate_chest"), loot -> loot.entry(entry -> entry
                .rolls(12, 15)
                .pool(80, pool -> pool
                        .item(2, "woldsvaults:supply_box", 1, 1)
                        .item(1, "the_vault:vault_alloy", 1, 1)
                        .item(12, "the_vault:ornate_chain", 1, 1)
                        .item(12, "the_vault:ornate_ingot", 1, 2)
                        .item(8, "the_vault:velvet", 1, 1)
                        .item(16, "the_vault:soot", 1, 2)
                        .item(6, "the_vault:carbon_nugget", 3, 5))
                .pool(20, pool -> pool
                        .item(10, "the_vault:wild_focus", 1, 1)
                        .item(8, "the_vault:carbon_nugget", 3, 6)
                        .item(2, "the_vault:carbon", 1, 1)
                        .item(2, "the_vault:vault_scrap", 1, 1))
                .pool(8, pool -> pool
                        .item(24, "the_vault:amplifying_focus", 1, 1)
                        .item(16, "the_vault:nullifying_focus", 1, 1)
                        .item(8, "the_vault:chaotic_focus", 1, 1)
                        .item(1, "woldsvaults:supply_box", 1, 1))
                .pool(2, pool -> blueprints(pool, 2.0D, "power_core", "radiator", "shield_array", "scanner", "drill")
                        .item(36, "the_vault:waning_focus", 1, 1)
                        .item(36, "the_vault:waxing_focus", 1, 1)
                        .item(18, "the_vault:fundamental_focus", 1, 1)
                        .item(4, "the_vault:opportunistic_focus", 1, 1)
                        .item(20, "the_vault:ornate_chest_scroll", 1, 1)
                        .itemNbt(44, "the_vault:trident", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(44, "the_vault:battlestaff", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(40, "the_vault:scepter", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, "ornate0"))
                        .itemNbt(40, "the_vault:dagger", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, "ornate0"))
                        .itemNbt(56, "the_vault:boots", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(56, "the_vault:leggings", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(56, "the_vault:chestplate", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(56, "the_vault:helmet", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(36, "the_vault:sword", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(36, "the_vault:axe", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(36, "the_vault:shield", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(36, "the_vault:wand", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(36, "the_vault:focus", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .item(8, "the_vault:vault_necklace", 1, 1))
                .pool(MYTHIC_POOL_WEIGHT, pool -> blueprints(pool, 0.56D, "power_core", "radiator", "shield_array", "scanner", "drill")
                        .item(30, "the_vault:carbon", 10, 20)
                        .item(15, "the_vault:fundamental_focus", 1, 3)
                        .item(10, "the_vault:vault_necklace", 1, 1)
                        .item(10, "the_vault:opportunistic_focus", 1, 2)
                        .itemNbt(10, "the_vault:boots", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(10, "the_vault:leggings", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(10, "the_vault:chestplate", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(10, "the_vault:helmet", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(10, "the_vault:trident", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(10, "the_vault:battlestaff", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(8, "the_vault:scepter", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, "ornate0"))
                        .itemNbt(8, "the_vault:dagger", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, "ornate0"))
                        .itemNbt(10, "the_vault:sword", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(10, "the_vault:axe", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(10, "the_vault:shield", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(10, "the_vault:wand", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(10, "the_vault:focus", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS)))));
    }

    private static void lootGildedChest(BiConsumer<ResourceLocation, Consumer<LootBuilder>> add) {
        add.accept(GreedStageIds.id(2, "gilded_chest"), loot -> loot.entry(entry -> entry
                .rolls(12, 15)
                .pool(70, pool -> pool
                        .item(8, "the_vault:gilded_sconce", 1, 1)
                        .item(12, "the_vault:gilded_ingot", 1, 2)
                        .item(8, "the_vault:topaz_shard", 1, 1)
                        .item(10, "minecraft:experience_bottle", 1, 1)
                        .item(8, "the_vault:vault_essence", 1, 4))
                .pool(20, pool -> pool
                        .item(16, "the_vault:magic_silk", 1, 2)
                        .item(10, "the_vault:vault_essence", 3, 5)
                        .item(10, "the_vault:ancient_copper_ingot", 1, 2))
                .pool(4, pool -> pool
                        .item(3, "the_vault:jewel_pouch", 1, 1)
                        .item(14, "the_vault:vault_diamond", 1, 1)
                        .item(6, "the_vault:dreamstone", 1, 1))
                .pool(2, pool -> pool
                        .item(75, "the_vault:jewel_pouch", 1, 2)
                        .item(450, "the_vault:vault_diamond", 1, 1)
                        .item(250, "the_vault:dreamstone", 1, 1)
                        .item(25, "the_vault:gilded_chest_scroll", 1, 1)
                        .item(25, "the_vault:key_piece", 1, 1)
                        .itemNbt(125, "the_vault:magnet", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(75, "the_vault:plushie", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(40, "the_vault:loot_sack", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .item(5, "the_vault:trinket", 1, 1))
                .pool(MYTHIC_POOL_WEIGHT, pool -> pool
                        .item(100, "the_vault:vault_diamond", 4, 8)
                        .item(50, "the_vault:jewel_pouch", 6, 8)
                        .item(50, "the_vault:dreamstone", 6, 8)
                        .item(10, "the_vault:key_piece", 4, 6))));
    }

    private static void lootLivingChest(BiConsumer<ResourceLocation, Consumer<LootBuilder>> add) {
        add.accept(GreedStageIds.id(2, "living_chest"), loot -> loot.entry(entry -> entry
                .rolls(12, 15)
                .pool(70, pool -> pool
                        .item(16, "the_vault:living_rock", 1, 2)
                        .item(8, "the_vault:mossy_bone", 1, 1)
                        .item(10, "the_vault:overgrown_wooden_chunk", 1, 2)
                        .item(16, "the_vault:rotten_meat", 1, 2)
                        .item(8, "the_vault:knowledge_star_essence", 1, 1)
                        .item(4, "the_vault:vault_meat", 1, 3))
                .pool(20, pool -> pool
                        .item(30, "the_vault:burger_patty", 1, 1)
                        .item(30, "the_vault:burger_bun", 1, 1)
                        .item(30, "the_vault:burger_cheese", 1, 1)
                        .item(29, "the_vault:burger_tomato", 1, 1)
                        .item(28, "the_vault:burger_lettuce", 1, 1)
                        .item(27, "the_vault:burger_pickles", 1, 1)
                        .item(26, "the_vault:burger_sauce", 1, 1)
                        .item(100, "the_vault:knowledge_star_essence", 1, 2)
                        .item(120, "the_vault:vault_meat", 1, 2))
                .pool(8, pool -> pool
                        .item(60, "the_vault:living_rock", 4, 8)
                        .item(80, "the_vault:knowledge_star_essence", 1, 2))
                .pool(2, pool -> mythicCharm(pool, 0.12D)
                        .item(50, "the_vault:sweet_kiwi", 1, 2)
                        .item(220, "the_vault:knowledge_star_essence", 2, 3)
                        .item(90, "the_vault:mystery_egg", 1, 1)
                        .item(60, "the_vault:vault_moss", 1, 1)
                        .item(24, "the_vault:living_chest_scroll", 1, 1)
                        .item(16, "the_vault:inscription_piece", 1, 1)
                        .itemNbt(4, "the_vault:vault_god_charm", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, "charms"))
                        .itemNbt(6, "the_vault:inscription", 1, 1, nbt -> { nbt.put("pool", "the_vault:random"); })
                        .item(8, "the_vault:burger_chili", 1, 1)
                        .item(32, "woldsvaults:soul_ichor", 1, 1))
                .pool(MYTHIC_POOL_WEIGHT, pool -> mythicCharm(pool, 0.03D)
                        .item(100, "the_vault:knowledge_star_essence", 16, 32)
                        .item(25, "woldsvaults:soul_ichor", 1, 3)
                        .item(20, "the_vault:burger_chili", 1, 2)
                        .itemNbt(13, "the_vault:inscription", 1, 1, nbt -> { nbt.put("pool", "the_vault:random"); }))));
    }

    private static void lootWoodenChest(BiConsumer<ResourceLocation, Consumer<LootBuilder>> add) {
        add.accept(GreedStageIds.id(2, "wooden_chest"), loot -> loot.entry(entry -> entry
                .rolls(12, 15)
                .pool(70, pool -> pool
                        .item(14, "the_vault:wooden_chunk", 1, 3)
                        .item(12, "the_vault:sandy_rocks", 1, 2)
                        .item(10, "the_vault:vault_sweets", 1, 1)
                        .item(4, "the_vault:driftwood", 1, 2)
                        .item(4, "the_vault:vault_plating", 1, 1))
                .pool(20, pool -> pool
                        .item(4, "the_vault:magic_silk", 1, 1)
                        .item(8, "the_vault:carbon_nugget", 1, 3)
                        .item(14, "the_vault:driftwood", 2, 3)
                        .item(3, "woldsvaults:vault_diamond_nugget", 1, 3)
                        .item(4, "the_vault:vault_plating", 1, 1))
                .pool(8, pool -> pool
                        .item(2, "minecraft:shulker_shell", 1, 1)
                        .item(10, "the_vault:vault_essence", 1, 2)
                        .item(10, "the_vault:silver_scrap", 1, 1))
                .pool(2, pool -> blueprints(pool, 0.36D, "chassis", "legs", "attachment", "drill")
                        .item(6, "the_vault:wooden_chest_scroll", 1, 1)
                        .item(5, "the_vault:mod_box", 1, 1)
                        .item(2, "woldsvaults:augment_box", 1, 1)
                        .item(85, "the_vault:bounty_pearl", 1, 1))
                .pool(MYTHIC_POOL_WEIGHT, pool -> blueprints(pool, 0.31D, "chassis", "legs", "attachment", "drill")
                        .item(40, "the_vault:vault_plating", 16, 24)
                        .item(30, "the_vault:bounty_pearl", 3, 5)
                        .item(10, "the_vault:mod_box", 3, 6)
                        .item(5, "woldsvaults:augment_box", 1, 2))));
    }

    private static void lootEnigmaChests(BiConsumer<ResourceLocation, Consumer<LootBuilder>> add) {
        add.accept(GreedStageIds.id(1, "enigma_chest"), loot -> loot.entry(GreedStageLootTables::enigmaPools));
        add.accept(GreedStageIds.id(2, "enigma_chest"), loot -> loot.entry(entry -> enigmaPools(entry)
                .pool(MYTHIC_POOL_WEIGHT, pool -> vaultMaps(pool, 10, 3, 5)
                        .item(30, "the_vault:chaotic_focus", 1, 2)
                        .item(10, "the_vault:enigma_chest_scroll", 1, 1)
                        .item(20, "the_vault:vault_platinum", 3, 6)
                        .item(10, "woldsvaults:omega_box", 1, 3)
                        .item(20, "woldsvaults:chunk_of_power", 1, 1))));
    }

    /** The four pools both enigma chests share; the stage-2 chest adds a mythic pool on top. Gear sits in the third. */
    private static EntryBuilder enigmaPools(EntryBuilder entry) {
        return entry
                .rolls(6, 9)
                .pool(70, pool -> pool
                        .item(6, "the_vault:gemstone", 1, 2)
                        .item(6, "the_vault:key_piece", 1, 2)
                        .item(16, "the_vault:silver_scrap", 1, 3)
                        .item(16, "the_vault:carbon", 1, 3)
                        .item(8, "the_vault:eternal_soul", 1, 1)
                        .item(8, "the_vault:dreamstone", 1, 1)
                        .item(16, "the_vault:vault_essence", 4, 8)
                        .item(16, "the_vault:vault_diamond", 1, 3)
                        .item(12, "the_vault:magic_silk", 1, 3)
                        .item(6, "woldsvaults:chromatic_gold_ingot", 1, 1)
                        .item(6, "the_vault:chromatic_steel_ingot", 1, 1)
                        .item(6, "the_vault:chromatic_iron_ingot", 1, 3)
                        .item(4, "the_vault:mod_box", 1, 1)
                        .item(4, "woldsvaults:augment_box", 1, 1)
                        .item(4, "woldsvaults:supply_box", 1, 1)
                        .item(5, "woldsvaults:gem_box", 1, 1)
                        .item(3, "woldsvaults:catalyst_box", 1, 1)
                        .item(1, "woldsvaults:omega_box", 1, 1)
                        .item(2, "woldsvaults:inscription_box", 1, 1)
                        .item(3, "woldsvaults:enigma_egg", 1, 1)
                        .item(3, "woldsvaults:arcane_essence", 1, 2)
                        .item(8, "woldsvaults:soul_ichor", 1, 2)
                        .item(2, "woldsvaults:chroma_core", 1, 1)
                        .item(3, "the_vault:gem_pog", 1, 1))
                .pool(20, pool -> pool
                        .item(4, "the_vault:bitter_lemon", 1, 1)
                        .item(1, "the_vault:mango", 1, 1)
                        .item(6, "the_vault:grapes", 1, 1)
                        .item(9, "the_vault:sweet_kiwi", 1, 1))
                .pool(8, pool -> pool
                        .item(8, "the_vault:knowledge_star", 1, 1)
                        .itemNbt(36, "the_vault:trident", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(36, "the_vault:rang", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(36, "the_vault:battlestaff", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(36, "the_vault:scepter", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(36, "the_vault:dagger", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(48, "the_vault:boots", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(48, "the_vault:leggings", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(48, "the_vault:chestplate", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(48, "the_vault:helmet", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(24, "the_vault:sword", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(24, "the_vault:axe", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(24, "the_vault:shield", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(24, "the_vault:wand", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(24, "the_vault:focus", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(12, "the_vault:map", 1, 1, nbt -> { nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS); nbt.put("the_vault:map_tier", -1); }))
                .pool(2, pool -> pool
                        .item(4, "the_vault:trinket", 1, 1)
                        .item(4, "the_vault:vault_platinum", 1, 1)
                        .item(10, "the_vault:vault_catalyst_chaos", 1, 1)
                        .item(10, "the_vault:chaotic_focus", 1, 1)
                        .item(4, "woldsvaults:chunk_of_power", 1, 1)
                        .item(1, "the_vault:enigma_chest_scroll", 1, 1)
                        .item(1, "woldsvaults:general_decor_scroll", 1, 1));
    }

    private static void lootOrnateStrongbox(BiConsumer<ResourceLocation, Consumer<LootBuilder>> add) {
        add.accept(GreedStageIds.id(2, "ornate_strongbox"), loot -> loot.entry(entry -> entry
                .rolls(24, 26)
                .pool(80, pool -> pool
                        .item(8, "the_vault:ornate_chain", 1, 1)
                        .item(8, "the_vault:ornate_ingot", 1, 2)
                        .item(5, "the_vault:velvet", 1, 1)
                        .item(12, "the_vault:soot", 1, 2)
                        .item(12, "the_vault:carbon_nugget", 5, 8))
                .pool(20, pool -> pool
                        .item(10, "the_vault:wild_focus", 1, 2)
                        .item(4, "the_vault:carbon", 2, 4)
                        .item(4, "the_vault:vault_scrap", 2, 4))
                .pool(8, pool -> pool
                        .item(24, "the_vault:amplifying_focus", 1, 2)
                        .item(16, "the_vault:nullifying_focus", 1, 2)
                        .item(12, "the_vault:chaotic_focus", 1, 2)
                        .item(1, "the_vault:vorpal_focus", 1, 1)
                        .item(4, "woldsvaults:supply_box", 1, 1)
                        .item(2, "the_vault:vault_alloy", 1, 1))
                .pool(2, pool -> blueprints(pool, 6.4D, "chassis", "power_core", "drill", "legs", "shield_array", "radiator", "scanner", "attachment")
                        .item(2, "the_vault:cryonic_focus", 1, 1)
                        .item(2, "the_vault:pyretic_focus", 1, 1)
                        .item(1, "the_vault:empowered_chaotic_focus", 1, 1)
                        .item(3, "woldsvaults:blazing_focus", 1, 1)
                        .item(3, "the_vault:resilient_focus", 1, 1)
                        .item(3, "woldsvaults:repair_augmenter", 1, 1)
                        .item(15, "the_vault:waning_focus", 1, 2)
                        .item(15, "the_vault:waxing_focus", 1, 2)
                        .item(21, "the_vault:fundamental_focus", 1, 2)
                        .item(4, "woldsvaults:inscribing_focus", 1, 1)
                        .item(15, "the_vault:ornate_chest_scroll", 1, 1)
                        .itemNbt(48, "the_vault:trident", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(48, "the_vault:battlestaff", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(46, "the_vault:scepter", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(46, "the_vault:dagger", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(48, "the_vault:rang", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(48, "the_vault:boots", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(48, "the_vault:leggings", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(48, "the_vault:chestplate", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(48, "the_vault:helmet", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(24, "the_vault:sword", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(24, "the_vault:axe", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(24, "the_vault:shield", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(24, "the_vault:wand", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(24, "the_vault:focus", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(1, "the_vault:map", 1, 1, nbt -> { nbt.put(ROLL_TYPE_POOL, "gear_completion"); nbt.put("the_vault:map_tier", -1); })
                        .item(4, "the_vault:vault_necklace", 1, 1))
                .pool(MYTHIC_POOL_WEIGHT, pool -> blueprints(pool, 4.5D, "chassis", "power_core", "drill", "legs", "shield_array", "radiator", "scanner", "attachment")
                        .item(50, "the_vault:vault_alloy", 7, 14)
                        .item(10, "woldsvaults:blazing_focus", 2, 2)
                        .item(10, "the_vault:resilient_focus", 2, 2)
                        .item(10, "the_vault:cryonic_focus", 2, 2)
                        .item(10, "the_vault:pyretic_focus", 2, 2)
                        .item(10, "the_vault:empowered_chaotic_focus", 2, 2)
                        .itemNbt(1, "the_vault:map", 1, 1, nbt -> { nbt.put(ROLL_TYPE_POOL, "gear_completion"); nbt.put("the_vault:map_tier", -1); })
                        .item(10, "woldsvaults:repair_augmenter", 2, 2)
                        .item(80, "the_vault:fundamental_focus", 3, 6)
                        .item(200, "the_vault:carbon", 10, 20))));
    }

    private static void lootGildedStrongbox(BiConsumer<ResourceLocation, Consumer<LootBuilder>> add) {
        add.accept(GreedStageIds.id(2, "gilded_strongbox"), loot -> loot.entry(entry -> entry
                .rolls(23, 26)
                .pool(70, pool -> pool
                        .item(6, "the_vault:gilded_sconce", 1, 1)
                        .item(8, "the_vault:gilded_ingot", 1, 2)
                        .item(6, "the_vault:topaz_shard", 1, 1)
                        .item(6, "minecraft:experience_bottle", 1, 1)
                        .item(8, "the_vault:magic_silk", 1, 1)
                        .item(2, "woldsvaults:vault_diamond_nugget", 1, 3))
                .pool(20, pool -> pool
                        .item(12, "the_vault:magic_silk", 2, 3)
                        .item(8, "the_vault:vault_essence", 3, 6))
                .pool(6, pool -> pool
                        .item(2, "the_vault:jewel_pouch", 2, 3)
                        .item(10, "the_vault:vault_diamond", 2, 2)
                        .item(6, "the_vault:dreamstone", 1, 1)
                        .itemNbt(3, "the_vault:magnet", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS)))
                .pool(2, pool -> pool
                        .item(60, "the_vault:jewel_pouch", 2, 3)
                        .item(160, "the_vault:vault_diamond", 1, 2)
                        .item(25, "the_vault:gilded_chest_scroll", 1, 2)
                        .item(16, "woldsvaults:catalyst_box", 1, 1)
                        .item(3, "the_vault:gem_pog", 1, 1)
                        .item(50, "woldsvaults:altar_recatalyzer", 1, 1)
                        .itemNbt(80, "the_vault:plushie", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .itemNbt(40, "the_vault:loot_sack", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS))
                        .item(15, "the_vault:trinket", 1, 1)
                        .itemNbt(1, "the_vault:map", 1, 1, nbt -> { nbt.put(ROLL_TYPE_POOL, "gear_completion"); nbt.put("the_vault:map_tier", -1); }))
                .pool(MYTHIC_POOL_WEIGHT, pool -> pool
                        .item(200, "the_vault:vault_diamond", 8, 12)
                        .item(100, "the_vault:jewel_pouch", 5, 10)
                        .item(33, "woldsvaults:altar_recatalyzer", 1, 1)
                        .item(20, "the_vault:trinket", 1, 1)
                        .item(20, "woldsvaults:catalyst_box", 2, 2)
                        .itemNbt(1, "the_vault:map", 1, 1, nbt -> { nbt.put(ROLL_TYPE_POOL, "gear_completion"); nbt.put("the_vault:map_tier", -1); }))));
    }

    private static void lootLivingStrongbox(BiConsumer<ResourceLocation, Consumer<LootBuilder>> add) {
        add.accept(GreedStageIds.id(2, "living_strongbox"), loot -> loot.entry(entry -> entry
                .rolls(24, 26)
                .pool(70, pool -> pool
                        .item(16, "the_vault:living_rock", 1, 2)
                        .item(8, "the_vault:mossy_bone", 1, 1)
                        .item(10, "the_vault:overgrown_wooden_chunk", 1, 2)
                        .item(16, "the_vault:rotten_meat", 1, 2)
                        .item(8, "the_vault:knowledge_star_essence", 1, 1)
                        .item(4, "the_vault:vault_meat", 1, 3))
                .pool(20, pool -> pool
                        .item(30, "the_vault:burger_patty", 1, 1)
                        .item(30, "the_vault:burger_bun", 1, 1)
                        .item(30, "the_vault:burger_cheese", 1, 1)
                        .item(29, "the_vault:burger_tomato", 1, 1)
                        .item(28, "the_vault:burger_lettuce", 1, 1)
                        .item(27, "the_vault:burger_pickles", 1, 1)
                        .item(26, "the_vault:burger_sauce", 1, 1)
                        .item(25, "the_vault:burger_chili", 1, 1)
                        .item(100, "the_vault:knowledge_star_essence", 1, 2)
                        .item(120, "the_vault:vault_meat", 1, 2))
                .pool(8, pool -> pool
                        .item(60, "the_vault:living_rock", 4, 8)
                        .item(80, "the_vault:knowledge_star_essence", 1, 2))
                .pool(2, pool -> mythicCharm(pool, 0.106D)
                        .item(30, "the_vault:sweet_kiwi", 2, 3)
                        .item(200, "the_vault:knowledge_star_essence", 2, 3)
                        .item(84, "the_vault:mystery_egg", 1, 1)
                        .item(56, "the_vault:vault_moss", 1, 1)
                        .item(16, "the_vault:living_chest_scroll", 1, 1)
                        .itemNbt(3, "the_vault:vault_god_charm", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, "charms"))
                        .itemNbt(4, "the_vault:inscription", 1, 1, nbt -> { nbt.put("pool", "the_vault:random"); })
                        .item(16, "woldsvaults:soul_ichor", 1, 1)
                        .item(8, "woldsvaults:unidentified_gateway_pearl", 1, 1)
                        .item(1, "the_vault:knowledge_star", 1, 1)
                        .itemNbt(1, "the_vault:map", 1, 1, nbt -> { nbt.put(ROLL_TYPE_POOL, "gear_completion"); nbt.put("the_vault:map_tier", -1); }))
                .pool(MYTHIC_POOL_WEIGHT, pool -> mythicCharm(pool, 0.018D)
                        .itemNbt(1, "the_vault:map", 1, 1, nbt -> { nbt.put(ROLL_TYPE_POOL, "gear_completion"); nbt.put("the_vault:map_tier", -1); })
                        .item(50, "the_vault:knowledge_star_essence", 16, 32)
                        .item(20, "woldsvaults:soul_ichor", 4, 8)
                        .item(6, "woldsvaults:unidentified_gateway_pearl", 1, 1)
                        .item(20, "the_vault:burger_chili", 5, 10))));
    }

    private static void lootDungeonBoss(BiConsumer<ResourceLocation, Consumer<LootBuilder>> add) {
        add.accept(GreedStageIds.id(2, "dungeon_boss"), loot -> loot.entry(entry -> entry
                .rolls(1, 2)
                .pool(1, pool -> pool
                        .item(80, "minecraft:air", 1, 1)
                        .itemNbt(5, "the_vault:helmet", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, "Chaotic"))
                        .itemNbt(5, "the_vault:chestplate", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, "Chaotic"))
                        .itemNbt(5, "the_vault:leggings", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, "Chaotic"))
                        .itemNbt(5, "the_vault:boots", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, "Chaotic"))
                        .itemNbt(5, "the_vault:sword", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, "Chaotic"))
                        .itemNbt(5, "the_vault:axe", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, "Chaotic"))
                        .itemNbt(5, "the_vault:shield", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, "Chaotic"))
                        .itemNbt(5, "the_vault:focus", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, "Chaotic"))
                        .itemNbt(5, "the_vault:wand", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, "Chaotic"))
                        .itemNbt(5, "the_vault:trident", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, "Chaotic"))
                        .itemNbt(5, "the_vault:battlestaff", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, "Chaotic"))
                        .itemNbt(5, "the_vault:scepter", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, "Chaotic"))
                        .itemNbt(5, "the_vault:dagger", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, "Chaotic"))
                        .itemNbt(5, "the_vault:plushie", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, "Chaotic"))
                        .itemNbt(5, "the_vault:loot_sack", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, "Chaotic"))
                        .itemNbt(5, "the_vault:rang", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, "Chaotic"))
                        .item(4, "woldsvaults:concealed_chaos", 1, 1)
                        .itemNbt(4, "the_vault:deck_socket", 1, 1, nbt -> { nbt.put("Modifier", "@dungeon_boss"); nbt.put("ModifierRoll", "greater"); })
                        .itemNbt(8, "the_vault:deck_socket", 1, 1, nbt -> { nbt.put("Modifier", "@dungeon_boss"); })
                        .item(8, "the_vault:vault_catalyst_chaos", 1, 1))));
    }

    private static void lootDungeonPedestal(BiConsumer<ResourceLocation, Consumer<LootBuilder>> add) {
        add.accept(GreedStageIds.id(2, "dungeon_pedestal"), loot -> loot.entry(entry -> entry
                .rolls(1, 1)
                .pool(70, pool -> pool
                        .item(40, "the_vault:gem_echo", 1, 1)
                        .item(120, "the_vault:perfect_black_opal", 1, 2)
                        .item(25, "woldsvaults:blue_vault_essence", 1, 4)
                        .itemSnbt(8, "the_vault:booster_pack", 1, 1, "{\"id\": \"the_vault:library_pack\"}")
                        .item(40, "woldsvaults:gem_box", 1, 1)
                        .item(40, "woldsvaults:augment_box", 1, 1)
                        .item(10, "woldsvaults:omega_box", 1, 1)
                        .item(10, "woldsvaults:inscription_box", 1, 1)
                        .item(20, "woldsvaults:catalyst_box", 1, 1)
                        .item(40, "woldsvaults:supply_box", 1, 1)
                        .item(40, "the_vault:mod_box", 1, 1)
                        .item(20, "woldsvaults:repair_augmenter", 1, 1)
                        .item(20, "woldsvaults:altar_recatalyzer", 1, 1)
                        .item(10, "woldsvaults:capstone_prosperous", 1, 1)
                        .item(10, "woldsvaults:capstone_frenzy", 1, 1)
                        .item(8, "woldsvaults:capstone_enchanted", 1, 1)
                        .item(16, "the_vault:capstone_vendoor_hunter", 1, 1)
                        .item(8, "the_vault:capstone_pylon_hunter", 1, 1)
                        .item(5, "the_vault:unidentified_treasure_key", 1, 1)
                        .item(40, "the_vault:gem_pog", 1, 1)
                        .item(25, "the_vault:pyretic_focus", 1, 1)
                        .item(15, "woldsvaults:eccentric_focus", 1, 1)
                        .item(5, "the_vault:empowered_chaotic_focus", 1, 1)
                        .item(10, "the_vault:opportunistic_focus", 1, 1)
                        .item(60, "the_vault:fundamental_focus", 1, 1)
                        .item(80, "the_vault:chaotic_focus", 1, 1)
                        .item(60, "the_vault:waxing_focus", 1, 1)
                        .item(60, "the_vault:waning_focus", 1, 1)
                        .itemNbt(4, "the_vault:magnet", 1, 1, nbt -> { nbt.put("the_vault:gear_unique_pool", "woldsvaults:bloodseeking_magnet"); nbt.put("the_vault:gear_roll_type", "Unique"); })
                        .itemNbt(2, "woldsvaults:recipe_blueprint", 1, 1, nbt -> { nbt.put("recipe", "woldsvaults:heavy_trinket_pouch"); }))));
    }

    private static void lootWoodenDungeonBarrel(BiConsumer<ResourceLocation, Consumer<LootBuilder>> add) {
        add.accept(GreedStageIds.id(2, "wooden_dungeon_barrel"), loot -> loot.entry(entry -> entry
                .rolls(1, 1)
                .pool(1, pool -> pool
                        .item(45, "the_vault:vault_plating", 10, 18)
                        .item(15, "the_vault:mod_box", 1, 2)
                        .item(3, "woldsvaults:augment_box", 1, 1)
                        .item(25, "woldsvaults:vault_diamond_nugget", 4, 12)
                        .item(30, "the_vault:bounty_pearl", 2, 4)
                        .itemNbt(1, "the_vault:deck_socket", 1, 1, nbt -> { nbt.put("Modifier", "@colour_cores"); nbt.put("ModifierRoll", "greater"); })
                        .itemNbt(2, "the_vault:deck_socket", 1, 1, nbt -> { nbt.put("Modifier", "@colour_cores"); })
                        .item(70, "the_vault:carbon_nugget", 12, 20))));
    }

    private static void lootGildedDungeonBarrel(BiConsumer<ResourceLocation, Consumer<LootBuilder>> add) {
        add.accept(GreedStageIds.id(2, "gilded_dungeon_barrel"), loot -> loot.entry(entry -> entry
                .rolls(1, 1)
                .pool(1, pool -> pool
                        .item(70, "the_vault:jewel_pouch", 1, 4)
                        .item(10, "the_vault:key_piece", 1, 2)
                        .item(20, "the_vault:vault_diamond", 1, 4)
                        .itemNbt(20, "the_vault:magnet", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, "dungeon"))
                        .item(2, "the_vault:trinket", 1, 1)
                        .itemNbt(4, "the_vault:plushie", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_DUNGEON_ROLLS))
                        .itemNbt(4, "the_vault:loot_sack", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_DUNGEON_ROLLS))
                        .itemNbt(1, "the_vault:deck_socket", 1, 1, nbt -> { nbt.put("Modifier", "@colour_cores"); nbt.put("ModifierRoll", "greater"); })
                        .itemNbt(2, "the_vault:deck_socket", 1, 1, nbt -> { nbt.put("Modifier", "@colour_cores"); }))));
    }

    private static void lootLivingDungeonBarrel(BiConsumer<ResourceLocation, Consumer<LootBuilder>> add) {
        add.accept(GreedStageIds.id(2, "living_dungeon_barrel"), loot -> loot.entry(entry -> entry
                .rolls(1, 1)
                .pool(1, pool -> pool
                        .item(15, "the_vault:knowledge_star_essence", 10, 20)
                        .itemNbt(2, "the_vault:inscription", 1, 1, nbt -> { nbt.put("pool", "the_vault:random"); })
                        .item(15, "the_vault:mystery_egg", 1, 3)
                        .item(5, "woldsvaults:soul_ichor", 1, 1)
                        .item(3, "the_vault:burger_chili", 1, 1)
                        .item(80, "the_vault:inscription_piece", 1, 2)
                        .itemNbt(2, "the_vault:vault_god_charm", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, "charms_dungeons"))
                        .itemNbt(1, "the_vault:deck_socket", 1, 1, nbt -> { nbt.put("Modifier", "@colour_cores"); nbt.put("ModifierRoll", "greater"); })
                        .itemNbt(2, "the_vault:deck_socket", 1, 1, nbt -> { nbt.put("Modifier", "@colour_cores"); }))));
    }

    private static void lootOrnateDungeonBarrel(BiConsumer<ResourceLocation, Consumer<LootBuilder>> add) {
        add.accept(GreedStageIds.id(2, "ornate_dungeon_barrel"), loot -> loot.entry(entry -> entry
                .rolls(1, 1)
                .pool(1, pool -> pool
                        .itemNbt(7, "the_vault:helmet", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_DUNGEON_ROLLS))
                        .itemNbt(7, "the_vault:chestplate", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_DUNGEON_ROLLS))
                        .itemNbt(7, "the_vault:leggings", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_DUNGEON_ROLLS))
                        .itemNbt(7, "the_vault:boots", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_DUNGEON_ROLLS))
                        .itemNbt(7, "the_vault:sword", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_DUNGEON_ROLLS))
                        .itemNbt(7, "the_vault:axe", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_DUNGEON_ROLLS))
                        .itemNbt(7, "the_vault:shield", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_DUNGEON_ROLLS))
                        .itemNbt(7, "the_vault:focus", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_DUNGEON_ROLLS))
                        .itemNbt(7, "the_vault:wand", 1, 1, nbt -> { nbt.put("the_vault:gear_roll_t ype_pool", STAGE_DUNGEON_ROLLS); })
                        .itemNbt(7, "the_vault:trident", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_DUNGEON_ROLLS))
                        .itemNbt(7, "the_vault:battlestaff", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_DUNGEON_ROLLS))
                        .itemNbt(7, "the_vault:scepter", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_DUNGEON_ROLLS))
                        .itemNbt(7, "the_vault:dagger", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_DUNGEON_ROLLS))
                        .itemNbt(7, "the_vault:rang", 1, 1, nbt -> nbt.put(ROLL_TYPE_POOL, STAGE_DUNGEON_ROLLS))
                        .item(20, "the_vault:waning_focus", 1, 1)
                        .item(20, "the_vault:waxing_focus", 1, 1)
                        .item(4, "the_vault:fundamental_focus", 1, 1)
                        .item(1, "the_vault:opportunistic_focus", 1, 1)
                        .item(1, "the_vault:vault_necklace", 1, 1)
                        .itemNbt(1, "the_vault:deck_socket", 1, 1, nbt -> { nbt.put("Modifier", "@colour_cores"); nbt.put("ModifierRoll", "greater"); })
                        .itemNbt(2, "the_vault:deck_socket", 1, 1, nbt -> { nbt.put("Modifier", "@colour_cores"); }))));
    }

    private static void lootTreasureChest(BiConsumer<ResourceLocation, Consumer<LootBuilder>> add) {
        add.accept(GreedStageIds.id(3, "treasure_chest"), loot -> loot.entry(entry -> entry
                .rolls(9, 9)
                .pool(0, pool -> pool
                        .item(25, "minecraft:air", 1, 1))
                .pool(60, pool -> pool
                        .item(4, "the_vault:mod_box", 1, 1)
                        .itemSnbt(4, "the_vault:vault_catalyst_infused", 1, 1, "{\"pool\":\"the_vault:treasure_chest\"}")
                        .item(4, "the_vault:sour_orange", 1, 1)
                        .item(2, "the_vault:capstone_pylon_hunter", 1, 1)
                        .item(2, "the_vault:capstone_vendoor_hunter", 1, 1)
                        .item(4, "the_vault:vault_catalyst_chaos", 1, 1))
                .pool(30, pool -> pool
                        .itemSnbt(4, "the_vault:vault_catalyst_infused", 1, 1, "{\"pool\":\"the_vault:treasure_bonus\"}")
                        .item(4, "the_vault:resilient_focus", 1, 1)
                        .item(4, "the_vault:recharge_core", 1, 1)
                        .item(4, "the_vault:crystal_seal_raid_infinite", 1, 1)
                        .item(4, "the_vault:pyretic_focus", 1, 1)
                        .item(4, "the_vault:empowered_chaotic_focus", 1, 1)
                        .item(4, "the_vault:lost_bounty", 1, 1)
                        .item(4, "the_vault:unique_shard", 1, 1)
                        .item(4, "the_vault:capstone_dungeon_hunter", 1, 1)
                        .item(2, "the_vault:trinket", 1, 1))
                .pool(10, pool -> pool
                        .item(2, "the_vault:temporal_shard", 1, 1)
                        .item(2, "the_vault:opportunistic_focus", 1, 1)
                        .itemNbt(4, "the_vault:sigil", 1, 1, nbt -> { nbt.put("SigilPool", "treasure"); })
                        .item(4, "the_vault:treasure_chest_scroll", 1, 1)
                        .item(4, "woldsvaults:wold_star_chunk", 1, 1)
                        .item(4, "the_vault:mystic_pear", 1, 1)
                        .item(4, "woldsvaults:inscription_box", 1, 1)
                        .item(4, "woldsvaults:omega_box", 1, 1)
                        .itemNbt(5, "the_vault:inscription", 1, 1, nbt -> { nbt.put("pool", "the_vault:treasure"); })
                        .item(4, "the_vault:knowledge_star", 1, 1)
                        .itemNbt(4, "the_vault:deck_socket", 1, 1, nbt -> { nbt.put("Modifier", "@treasure_sand"); })
                        .itemNbt(2, "the_vault:deck_socket", 1, 1, nbt -> { nbt.put("Modifier", "@treasure_sand"); nbt.put("ModifierRoll", "greater"); })
                        .itemNbt(1, "the_vault:map", 1, 1, nbt -> { nbt.put(ROLL_TYPE_POOL, STAGE_TREASURE_ROLLS); nbt.put("the_vault:map_tier", -1); }))));
    }

    private static void lootTreasureChestIdona(BiConsumer<ResourceLocation, Consumer<LootBuilder>> add) {
        add.accept(GreedStageIds.id(3, "treasure_chest_idona"), loot -> loot.entry(entry -> entry
                .rolls(9, 9)
                .pool(0, pool -> pool
                        .item(25, "minecraft:air", 1, 1))
                .pool(60, pool -> pool
                        .item(4, "the_vault:mod_box", 2, 4)
                        .item(4, "the_vault:red_vault_essence", 4, 8)
                        .itemSnbt(4, "the_vault:vault_catalyst_infused", 1, 1, "{\"pool\":\"the_vault:treasure_bonus\"}")
                        .itemSnbt(4, "the_vault:booster_pack", 1, 1, "{\"id\": \"the_vault:hellish_pack\"}")
                        .item(2, "the_vault:sour_orange", 1, 1)
                        .item(2, "the_vault:capstone_pylon_hunter", 1, 1)
                        .item(2, "the_vault:capstone_vendoor_hunter", 1, 1)
                        .item(2, "the_vault:vault_catalyst_chaos", 1, 1))
                .pool(30, pool -> pool
                        .itemSnbt(4, "the_vault:vault_catalyst_infused", 1, 1, "{\"pool\":\"the_vault:idona_bonus\"}")
                        .item(3, "the_vault:resilient_focus", 1, 1)
                        .item(2, "the_vault:recharge_core", 1, 1)
                        .item(1, "the_vault:crystal_seal_raid_infinite", 1, 1)
                        .item(4, "the_vault:pyretic_focus", 1, 1)
                        .item(4, "the_vault:empowered_chaotic_focus", 1, 1)
                        .item(4, "the_vault:lost_bounty", 1, 1)
                        .item(4, "the_vault:unique_shard", 1, 1)
                        .item(4, "the_vault:capstone_dungeon_hunter", 1, 1)
                        .item(4, "the_vault:trinket", 1, 1))
                .pool(10, pool -> pool
                        .item(4, "the_vault:temporal_shard", 1, 1)
                        .item(4, "the_vault:opportunistic_focus", 1, 1)
                        .itemNbt(4, "the_vault:sigil", 1, 1, nbt -> { nbt.put("SigilPool", "treasure"); })
                        .item(5, "the_vault:treasure_chest_scroll", 1, 1)
                        .item(3, "woldsvaults:wold_star_chunk", 1, 1)
                        .item(4, "the_vault:mystic_pear", 1, 1)
                        .item(5, "woldsvaults:inscription_box", 1, 1)
                        .item(5, "woldsvaults:omega_box", 1, 1)
                        .itemNbt(2, "the_vault:inscription", 1, 1, nbt -> { nbt.put("pool", "the_vault:idona"); })
                        .itemNbt(2, "the_vault:inscription", 1, 1, nbt -> { nbt.put("pool", "the_vault:treasure"); })
                        .item(4, "the_vault:knowledge_star", 1, 1)
                        .itemNbt(4, "the_vault:deck_socket", 1, 1, nbt -> { nbt.put("Modifier", "@treasure_sand"); })
                        .itemNbt(2, "the_vault:deck_socket", 1, 1, nbt -> { nbt.put("Modifier", "@treasure_sand"); })
                        .itemNbt(2, "the_vault:deck_socket", 1, 1, nbt -> { nbt.put("Modifier", "@treasure_sand"); nbt.put("ModifierRoll", "greater"); })
                        .itemNbt(1, "the_vault:map", 1, 1, nbt -> { nbt.put(ROLL_TYPE_POOL, STAGE_TREASURE_ROLLS); nbt.put("the_vault:map_tier", -1); }))));
    }

    private static void lootTreasureChestTenos(BiConsumer<ResourceLocation, Consumer<LootBuilder>> add) {
        add.accept(GreedStageIds.id(3, "treasure_chest_tenos"), loot -> loot.entry(entry -> entry
                .rolls(9, 9)
                .pool(0, pool -> pool
                        .item(25, "minecraft:air", 1, 1))
                .pool(60, pool -> pool
                        .item(4, "the_vault:mod_box", 1, 1)
                        .item(4, "woldsvaults:blue_vault_essence", 4, 8)
                        .itemSnbt(4, "the_vault:vault_catalyst_infused", 1, 1, "{\"pool\":\"the_vault:treasure_bonus\"}")
                        .itemSnbt(4, "the_vault:booster_pack", 1, 1, "{\"id\": \"the_vault:library_pack\"}")
                        .item(2, "the_vault:sour_orange", 1, 1)
                        .item(2, "the_vault:capstone_pylon_hunter", 1, 1)
                        .item(2, "the_vault:capstone_vendoor_hunter", 1, 1)
                        .item(2, "the_vault:vault_catalyst_chaos", 1, 1))
                .pool(30, pool -> pool
                        .itemSnbt(4, "the_vault:vault_catalyst_infused", 1, 1, "{\"pool\":\"the_vault:tenos_bonus\"}")
                        .item(3, "the_vault:resilient_focus", 1, 1)
                        .item(2, "the_vault:recharge_core", 1, 1)
                        .item(1, "the_vault:crystal_seal_raid_infinite", 1, 1)
                        .item(4, "the_vault:pyretic_focus", 1, 1)
                        .item(4, "the_vault:empowered_chaotic_focus", 1, 1)
                        .item(4, "the_vault:lost_bounty", 1, 1)
                        .item(4, "the_vault:unique_shard", 1, 1)
                        .item(4, "the_vault:capstone_dungeon_hunter", 1, 1)
                        .item(4, "the_vault:trinket", 1, 1))
                .pool(10, pool -> pool
                        .item(4, "the_vault:temporal_shard", 1, 1)
                        .item(4, "the_vault:opportunistic_focus", 1, 1)
                        .itemNbt(4, "the_vault:sigil", 1, 1, nbt -> { nbt.put("SigilPool", "treasure"); })
                        .item(5, "the_vault:treasure_chest_scroll", 1, 1)
                        .item(3, "woldsvaults:wold_star_chunk", 1, 1)
                        .item(4, "the_vault:mystic_pear", 1, 1)
                        .item(5, "woldsvaults:inscription_box", 1, 1)
                        .item(5, "woldsvaults:omega_box", 1, 1)
                        .itemNbt(2, "the_vault:inscription", 1, 1, nbt -> { nbt.put("pool", "the_vault:tenos"); })
                        .item(4, "the_vault:knowledge_star", 1, 1)
                        .itemNbt(4, "the_vault:deck_socket", 1, 1, nbt -> { nbt.put("Modifier", "@treasure_sand"); })
                        .itemNbt(2, "the_vault:deck_socket", 1, 1, nbt -> { nbt.put("Modifier", "@treasure_sand"); })
                        .itemNbt(2, "the_vault:deck_socket", 1, 1, nbt -> { nbt.put("Modifier", "@treasure_sand"); nbt.put("ModifierRoll", "greater"); })
                        .itemNbt(1, "the_vault:map", 1, 1, nbt -> { nbt.put(ROLL_TYPE_POOL, STAGE_TREASURE_ROLLS); nbt.put("the_vault:map_tier", -1); }))));
    }

    private static void lootTreasureChestVelara(BiConsumer<ResourceLocation, Consumer<LootBuilder>> add) {
        add.accept(GreedStageIds.id(3, "treasure_chest_velara"), loot -> loot.entry(entry -> entry
                .rolls(9, 9)
                .pool(0, pool -> pool
                        .item(25, "minecraft:air", 1, 1))
                .pool(60, pool -> pool
                        .item(4, "the_vault:mod_box", 2, 4)
                        .item(4, "woldsvaults:green_vault_essence", 4, 8)
                        .itemSnbt(4, "the_vault:vault_catalyst_infused", 1, 1, "{\"pool\":\"the_vault:treasure_bonus\"}")
                        .itemSnbt(4, "the_vault:booster_pack", 1, 1, "{\"id\": \"the_vault:grove_pack\"}")
                        .item(2, "the_vault:sour_orange", 1, 1)
                        .item(2, "the_vault:capstone_pylon_hunter", 1, 1)
                        .item(2, "the_vault:capstone_vendoor_hunter", 1, 1)
                        .item(2, "the_vault:vault_catalyst_chaos", 1, 1))
                .pool(30, pool -> pool
                        .itemSnbt(4, "the_vault:vault_catalyst_infused", 1, 1, "{\"pool\":\"the_vault:velara_bonus\"}")
                        .item(3, "the_vault:resilient_focus", 1, 1)
                        .item(2, "the_vault:recharge_core", 1, 1)
                        .item(1, "the_vault:crystal_seal_raid_infinite", 1, 1)
                        .item(4, "the_vault:pyretic_focus", 1, 1)
                        .item(4, "the_vault:empowered_chaotic_focus", 1, 1)
                        .item(4, "the_vault:lost_bounty", 1, 1)
                        .item(4, "the_vault:unique_shard", 1, 1)
                        .item(4, "the_vault:capstone_dungeon_hunter", 1, 1)
                        .item(4, "the_vault:trinket", 1, 1))
                .pool(10, pool -> pool
                        .item(4, "the_vault:temporal_shard", 1, 1)
                        .item(4, "the_vault:opportunistic_focus", 1, 1)
                        .itemNbt(4, "the_vault:sigil", 1, 1, nbt -> { nbt.put("SigilPool", "treasure"); })
                        .item(5, "the_vault:treasure_chest_scroll", 1, 1)
                        .item(3, "woldsvaults:wold_star_chunk", 1, 1)
                        .item(4, "the_vault:mystic_pear", 1, 1)
                        .item(5, "woldsvaults:inscription_box", 1, 1)
                        .item(5, "woldsvaults:omega_box", 1, 1)
                        .itemNbt(2, "the_vault:inscription", 1, 1, nbt -> { nbt.put("pool", "the_vault:velara"); })
                        .item(4, "the_vault:knowledge_star", 1, 1)
                        .itemNbt(4, "the_vault:deck_socket", 1, 1, nbt -> { nbt.put("Modifier", "@treasure_sand"); })
                        .itemNbt(2, "the_vault:deck_socket", 1, 1, nbt -> { nbt.put("Modifier", "@treasure_sand"); })
                        .itemNbt(2, "the_vault:deck_socket", 1, 1, nbt -> { nbt.put("Modifier", "@treasure_sand"); nbt.put("ModifierRoll", "greater"); })
                        .itemNbt(1, "the_vault:map", 1, 1, nbt -> { nbt.put(ROLL_TYPE_POOL, STAGE_TREASURE_ROLLS); nbt.put("the_vault:map_tier", -1); }))));
    }

    private static void lootTreasureChestWendarr(BiConsumer<ResourceLocation, Consumer<LootBuilder>> add) {
        add.accept(GreedStageIds.id(3, "treasure_chest_wendarr"), loot -> loot.entry(entry -> entry
                .rolls(9, 9)
                .pool(0, pool -> pool
                        .item(25, "minecraft:air", 1, 1))
                .pool(60, pool -> pool
                        .item(4, "the_vault:mod_box", 2, 4)
                        .item(4, "woldsvaults:yellow_vault_essence", 4, 8)
                        .itemSnbt(4, "the_vault:booster_pack", 1, 1, "{\"id\": \"the_vault:museum_pack\"}")
                        .itemSnbt(4, "the_vault:vault_catalyst_infused", 1, 1, "{\"pool\":\"the_vault:treasure_bonus\"}")
                        .item(2, "the_vault:sour_orange", 1, 1)
                        .item(2, "the_vault:capstone_pylon_hunter", 1, 1)
                        .item(2, "the_vault:capstone_vendoor_hunter", 1, 1)
                        .item(2, "the_vault:vault_catalyst_chaos", 1, 1))
                .pool(30, pool -> pool
                        .itemSnbt(4, "the_vault:vault_catalyst_infused", 1, 1, "{\"pool\":\"the_vault:wendarr_bonus\"}")
                        .item(3, "the_vault:resilient_focus", 1, 1)
                        .item(2, "the_vault:recharge_core", 1, 1)
                        .item(1, "the_vault:crystal_seal_raid_infinite", 1, 1)
                        .item(4, "the_vault:pyretic_focus", 1, 1)
                        .item(4, "the_vault:empowered_chaotic_focus", 1, 1)
                        .item(4, "the_vault:lost_bounty", 1, 1)
                        .item(4, "the_vault:unique_shard", 1, 1)
                        .item(4, "the_vault:capstone_dungeon_hunter", 1, 1)
                        .item(4, "the_vault:trinket", 1, 1))
                .pool(10, pool -> pool
                        .item(4, "the_vault:temporal_shard", 1, 1)
                        .item(4, "the_vault:opportunistic_focus", 1, 1)
                        .itemNbt(4, "the_vault:sigil", 1, 1, nbt -> { nbt.put("SigilPool", "treasure"); })
                        .item(5, "the_vault:treasure_chest_scroll", 1, 1)
                        .item(3, "woldsvaults:wold_star_chunk", 1, 1)
                        .item(4, "the_vault:mystic_pear", 1, 1)
                        .item(5, "woldsvaults:inscription_box", 1, 1)
                        .item(5, "woldsvaults:omega_box", 1, 1)
                        .itemNbt(2, "the_vault:inscription", 1, 1, nbt -> { nbt.put("pool", "the_vault:wendarr"); })
                        .item(4, "the_vault:knowledge_star", 1, 1)
                        .itemNbt(4, "the_vault:deck_socket", 1, 1, nbt -> { nbt.put("Modifier", "@treasure_sand"); })
                        .itemNbt(2, "the_vault:deck_socket", 1, 1, nbt -> { nbt.put("Modifier", "@treasure_sand"); })
                        .itemNbt(2, "the_vault:deck_socket", 1, 1, nbt -> { nbt.put("Modifier", "@treasure_sand"); nbt.put("ModifierRoll", "greater"); })
                        .itemNbt(1, "the_vault:map", 1, 1, nbt -> { nbt.put(ROLL_TYPE_POOL, STAGE_TREASURE_ROLLS); nbt.put("the_vault:map_tier", -1); }))));
    }

    private static void lootTreasurePillar(BiConsumer<ResourceLocation, Consumer<LootBuilder>> add) {
        add.accept(GreedStageIds.id(3, "treasure_pillar"), loot -> loot.entry(entry -> entry
                .rolls(1, 1)
                .pool(70, pool -> pool
                        .itemNbt(10, "the_vault:magnet", 1, 1, nbt -> { nbt.put("the_vault:gear_unique_pool", "woldsvaults:treasure_magnet"); nbt.put("the_vault:gear_roll_type", "Unique"); })
                        .itemNbt(100, "the_vault:sword", 1, 1, nbt -> { nbt.put("the_vault:is_legendary", "true"); nbt.put(ROLL_TYPE_POOL, STAGE_TREASURE_ROLLS); })
                        .itemNbt(100, "the_vault:axe", 1, 1, nbt -> { nbt.put("the_vault:is_legendary", "true"); nbt.put(ROLL_TYPE_POOL, STAGE_TREASURE_ROLLS); })
                        .itemNbt(100, "the_vault:helmet", 1, 1, nbt -> { nbt.put("the_vault:is_legendary", "true"); nbt.put(ROLL_TYPE_POOL, STAGE_TREASURE_ROLLS); })
                        .itemNbt(100, "the_vault:chestplate", 1, 1, nbt -> { nbt.put("the_vault:is_legendary", "true"); nbt.put(ROLL_TYPE_POOL, STAGE_TREASURE_ROLLS); })
                        .itemNbt(100, "the_vault:leggings", 1, 1, nbt -> { nbt.put("the_vault:is_legendary", "true"); nbt.put(ROLL_TYPE_POOL, STAGE_TREASURE_ROLLS); })
                        .itemNbt(100, "the_vault:boots", 1, 1, nbt -> { nbt.put("the_vault:is_legendary", "true"); nbt.put(ROLL_TYPE_POOL, STAGE_TREASURE_ROLLS); })
                        .itemNbt(80, "the_vault:shield", 1, 1, nbt -> { nbt.put("the_vault:is_legendary", "true"); nbt.put(ROLL_TYPE_POOL, STAGE_TREASURE_ROLLS); })
                        .itemNbt(80, "the_vault:wand", 1, 1, nbt -> { nbt.put("the_vault:is_legendary", "true"); nbt.put(ROLL_TYPE_POOL, STAGE_TREASURE_ROLLS); })
                        .itemNbt(80, "the_vault:focus", 1, 1, nbt -> { nbt.put("the_vault:is_legendary", "true"); nbt.put(ROLL_TYPE_POOL, STAGE_TREASURE_ROLLS); })
                        .itemNbt(100, "the_vault:loot_sack", 1, 1, nbt -> { nbt.put("the_vault:is_legendary", "true"); nbt.put(ROLL_TYPE_POOL, STAGE_TREASURE_ROLLS); })
                        .itemNbt(100, "the_vault:plushie", 1, 1, nbt -> { nbt.put("the_vault:is_legendary", "true"); nbt.put(ROLL_TYPE_POOL, STAGE_TREASURE_ROLLS); })
                        .itemNbt(100, "the_vault:rang", 1, 1, nbt -> { nbt.put("the_vault:is_legendary", "true"); nbt.put(ROLL_TYPE_POOL, STAGE_TREASURE_ROLLS); })
                        .itemNbt(100, "the_vault:battlestaff", 1, 1, nbt -> { nbt.put("the_vault:is_legendary", "true"); nbt.put(ROLL_TYPE_POOL, STAGE_TREASURE_ROLLS); })
                        .itemNbt(100, "the_vault:scepter", 1, 1, nbt -> { nbt.put(ROLL_TYPE_POOL, STAGE_TREASURE_ROLLS); nbt.put("the_vault:is_legendary", "true"); })
                        .itemNbt(100, "the_vault:dagger", 1, 1, nbt -> { nbt.put(ROLL_TYPE_POOL, STAGE_TREASURE_ROLLS); nbt.put("the_vault:is_legendary", "true"); })
                        .itemNbt(100, "the_vault:trident", 1, 1, nbt -> { nbt.put("the_vault:is_legendary", "true"); nbt.put(ROLL_TYPE_POOL, STAGE_TREASURE_ROLLS); }))));
    }

    /** Adds the Mythic-rolled vault charm to a pool. */
    private static PoolBuilder mythicCharm(PoolBuilder pool, double weight) {
        return pool.itemNbt(weight, "woldsvaults:mythic_vault_charm", 1, 1, nbt -> nbt.put("the_vault:gear_roll_type", "Mythic"));
    }

    /** Adds one blueprint per extractor part to a pool, each at {@code weight}. */
    private static PoolBuilder blueprints(PoolBuilder pool, double weight, String... parts) {
        for (String part : parts) {
            pool.item(weight, "woldsvaults:" + part + "_blueprint", 1, 1);
        }
        return pool;
    }

    /** Adds vault maps of the stored tiers {@code from} to {@code to}, sharing {@code weight} evenly. */
    private static PoolBuilder vaultMaps(PoolBuilder pool, int weight, int from, int to) {
        double each = (double) weight / (to - from + 1);
        for (int tier = from; tier <= to; tier++) {
            int stored = tier;
            pool.itemNbt(each, "the_vault:map", 1, 1, nbt -> {
                nbt.put(ROLL_TYPE_POOL, STAGE_LOOT_ROLLS);
                nbt.put("the_vault:map_tier", stored);
            });
        }
        return pool;
    }
}
