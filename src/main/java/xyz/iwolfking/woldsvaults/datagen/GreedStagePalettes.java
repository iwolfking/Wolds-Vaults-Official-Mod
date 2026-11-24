package xyz.iwolfking.woldsvaults.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import iskallia.vault.block.PlaceholderBlock;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.vhapi.api.datagen.lib.gen.palette.PaletteBuilder;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractConfigPatchProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The palettes of the greed stages, registered into {@link ModVaultPalettesProvider}. Stage 1 holds the chest
 * placeholders with the stage chest and strongbox loot tables and a few enigma chests. Stage 2 holds the same chest
 * placeholders with more enigma chests, and the room base chain, its coin, ore, pylon, vendor and dungeon
 * placeholders (god altars keep the normal palette), the treasure door and the challenge and omega room palettes. Stage 3 holds the treasure
 * room and trove palettes. Palettes that equal a normal palette apart from the ids they point at are derived from
 * the seeded normal palette; the rest are written out here.
 */
public final class GreedStagePalettes {
    private static final List<String> CHESTS = List.of("ornate", "gilded", "living");
    private static final int STAGE_1_CHEST_WEIGHT = 189;
    private static final int STAGE_1_STRONGBOX_WEIGHT = 10;
    private static final int STAGE_1_ENIGMA_WEIGHT = 1;
    private static final int STAGE_2_CHEST_WEIGHT = 94;
    private static final int STAGE_2_STRONGBOX_WEIGHT = 5;
    private static final int STAGE_2_ENIGMA_WEIGHT = 1;

    /** Where the palettes go: {@link ModVaultPalettesProvider}'s {@code add}. */
    @FunctionalInterface
    public interface Sink {
        void add(ResourceLocation id, PaletteBuilder builder, Consumer<PaletteBuilder> palette);
    }

    private GreedStagePalettes() {
    }

    public static void register(Sink sink) {
        for (String chest : CHESTS) {
            chestPlaceholder(sink, 1, chest, STAGE_1_CHEST_WEIGHT, STAGE_1_STRONGBOX_WEIGHT, STAGE_1_ENIGMA_WEIGHT);
            chestPlaceholder(sink, 2, chest, STAGE_2_CHEST_WEIGHT, STAGE_2_STRONGBOX_WEIGHT, STAGE_2_ENIGMA_WEIGHT);
        }
        derived(sink, GreedStageIds.id(2, "room_base"), "generic/room_base",
                Map.of("the_vault:generic/treasure", GreedStageIds.ref(2, "treasure")));
        derived(sink, GreedStageIds.id(2, "dungeon_door_placeholder"), "generic/dungeon_door_placeholder",
                Map.of("the_vault:dungeon_rooms/gilded_chest_placeholder", GreedStageIds.ref(2, "dungeon_rooms/gilded_chest_placeholder"),
                        "the_vault:dungeon_boss", GreedStageIds.ref(2, "dungeon_boss")));
        derived(sink, GreedStageIds.id(2, "vendor/base"), "vendor/base",
                Map.of("the_vault:generic/room_base", GreedStageIds.ref(2, "room_base"),
                        "the_vault:generic/vendor_pedestal_placeholder", GreedStageIds.ref(2, "vendor_pedestal_placeholder")));
        derived(sink, GreedStageIds.id(2, "x-mark/base"), "x-mark/base",
                Map.of("the_vault:generic/treasure", GreedStageIds.ref(2, "treasure")));
        derived(sink, GreedStageIds.id(3, "treasure_rooms"), "treasure_rooms/treasure_rooms",
                Map.of("the_vault:treasure_rooms/treasure_chest", GreedStageIds.ref(3, "treasure_chest")));
        paletteTreasure(sink);
        paletteWoodenChestPlaceholder(sink);
        paletteCoinStackPlaceholder(sink);
        paletteOrePlaceholder(sink);
        palettePylonPlaceholder(sink);
        paletteVendorPedestalPlaceholder(sink);
        paletteDungeonRoomsGildedChestPlaceholder(sink);
        paletteTreasureDoorPlaceholder(sink);
        paletteVendorRoomsVendorRooms(sink);
        paletteVendorRoomsBarterExpert(sink);
        paletteCometObservatoryBase(sink);
        paletteCoveBase(sink);
        paletteGraveyardBase(sink);
        paletteLaboratoryBase(sink);
        paletteLabyrinthBase(sink);
        paletteRaidBase(sink);
        paletteWardensGardenBase(sink);
        paletteWildwestBase(sink);
        paletteWoldsDinnerBase(sink);
        paletteTreasureChest(sink);
        paletteTroveBase(sink);
    }

    private static void paletteTreasure(Sink sink) {
        sink.add(GreedStageIds.id(2, "treasure"), new PaletteBuilder(), p -> p
                .reference(GreedStageIds.ref(1, "wooden_chest_placeholder"))
                .reference(GreedStageIds.ref(2, "gilded_chest_placeholder"))
                .reference(GreedStageIds.ref(2, "living_chest_placeholder"))
                .reference(GreedStageIds.ref(2, "ornate_chest_placeholder"))
                .reference(GreedStageIds.ref(2, "coin_stack_placeholder"))
                .reference(GreedStageIds.ref(2, "ore_placeholder"))
                .reference(GreedStageIds.ref(2, "pylon_placeholder"))
                .reference("the_vault:generic/gate_placeholder")
                .reference("the_vault:generic/dungeon_discoverable_remove")
                .reference("the_vault:generic/god_altars"));
    }

    private static void paletteWoodenChestPlaceholder(Sink sink) {
        sink.add(GreedStageIds.id(1, "wooden_chest_placeholder"), new PaletteBuilder(), p -> p
                .placeholder(PlaceholderBlock.Type.WOODEN_CHEST, ph -> ph
                        .probability(0, 1.0, success -> {
                            success.put("the_vault:wooden_chest{LootTable:\"" + GreedStageIds.ref(1, "wooden_chest") + "\"}", 1);
                        }, failure -> {
                            failure.put("minecraft:air", 1);
                        }))
                .placeholder(PlaceholderBlock.Type.WOODEN_CHEST_WATERLOGGED, ph -> ph
                        .probability(0, 1.0, success -> {
                            success.put("the_vault:wooden_chest[waterlogged=true]{LootTable:\"" + GreedStageIds.ref(1, "wooden_chest") + "\"}", 1);
                        }, failure -> {
                            failure.put("minecraft:water", 1);
                        })));
    }

    /**
     * A chest placeholder that places the stage-1 chest and strongbox loot tables and an enigma chest, at the given
     * weights, at every level, dry and waterlogged.
     */
    private static void chestPlaceholder(Sink sink, int stage, String chest, int chestWeight, int strongboxWeight, int enigmaWeight) {
        PlaceholderBlock.Type dry = PlaceholderBlock.Type.valueOf(chest.toUpperCase(Locale.ROOT) + "_CHEST");
        PlaceholderBlock.Type wet = PlaceholderBlock.Type.valueOf(chest.toUpperCase(Locale.ROOT) + "_CHEST_WATERLOGGED");
        String chestTable = GreedStageIds.ref(1, chest + "_chest");
        String strongboxTable = GreedStageIds.ref(1, chest + "_strongbox");
        String enigma = "the_vault:enigma_chest{LootTable:\"" + GreedStageIds.ref(stage, "enigma_chest") + "\"}";
        sink.add(GreedStageIds.id(stage, chest + "_chest_placeholder"), new PaletteBuilder(), p -> p
                .placeholder(dry, ph -> ph
                        .probability(0, 1.0, success -> {
                            success.put("the_vault:" + chest + "_chest{LootTable:\"" + chestTable + "\"}", chestWeight);
                            success.put("the_vault:" + chest + "_strongbox{LootTable:\"" + strongboxTable + "\"}", strongboxWeight);
                            success.put(enigma, enigmaWeight);
                        }, failure -> {
                            failure.put("minecraft:air", 1);
                        }))
                .placeholder(wet, ph -> ph
                        .probability(0, 1.0, success -> {
                            success.put("the_vault:" + chest + "_chest[waterlogged=true]{LootTable:\"" + chestTable + "\"}", chestWeight);
                            success.put("the_vault:" + chest + "_strongbox[waterlogged=true]{LootTable:\"" + strongboxTable + "\"}", strongboxWeight);
                            success.put(enigma, enigmaWeight);
                        }, failure -> {
                            failure.put("minecraft:water", 1);
                        })));
    }

    private static void paletteCoinStackPlaceholder(Sink sink) {
        sink.add(GreedStageIds.id(2, "coin_stack_placeholder"), new PaletteBuilder(), p -> p
                .placeholder(PlaceholderBlock.Type.COIN_STACKS, ph -> ph
                        .probability(0, 0.9, success -> {
                            success.put("the_vault:coin_pile[size=1,waterlogged=false]{LootTable:\"the_vault:coin_pile_lvl30\"}", 2);
                            success.put("the_vault:coin_pile[size=2,waterlogged=false]{LootTable:\"the_vault:coin_pile_lvl30\"}", 14);
                            success.put("the_vault:coin_pile[size=3,waterlogged=false]{LootTable:\"the_vault:coin_pile_lvl30\"}", 30);
                            success.put("the_vault:coin_pile[size=4,waterlogged=false]{LootTable:\"the_vault:coin_pile_lvl30\"}", 48);
                            success.put("the_vault:coin_pile[size=5,waterlogged=false]{LootTable:\"the_vault:coin_pile_lvl30\"}", 26);
                            success.put("the_vault:coin_pile[size=6,waterlogged=false]{LootTable:\"the_vault:coin_pile_lvl30\"}", 18);
                            success.put("the_vault:coin_pile[size=7,waterlogged=false]{LootTable:\"the_vault:coin_pile_lvl30\"}", 6);
                        }, failure -> {
                            failure.put("minecraft:air", 1);
                        }))
                .placeholder(PlaceholderBlock.Type.COIN_STACKS_WATERLOGGED, ph -> ph
                        .probability(0, 0.9, success -> {
                            success.put("the_vault:coin_pile[size=1,waterlogged=true]{LootTable:\"the_vault:coin_pile_lvl30\"}", 2);
                            success.put("the_vault:coin_pile[size=2,waterlogged=true]{LootTable:\"the_vault:coin_pile_lvl30\"}", 14);
                            success.put("the_vault:coin_pile[size=3,waterlogged=true]{LootTable:\"the_vault:coin_pile_lvl30\"}", 30);
                            success.put("the_vault:coin_pile[size=4,waterlogged=true]{LootTable:\"the_vault:coin_pile_lvl30\"}", 48);
                            success.put("the_vault:coin_pile[size=5,waterlogged=true]{LootTable:\"the_vault:coin_pile_lvl30\"}", 26);
                            success.put("the_vault:coin_pile[size=6,waterlogged=true]{LootTable:\"the_vault:coin_pile_lvl30\"}", 18);
                            success.put("the_vault:coin_pile[size=7,waterlogged=true]{LootTable:\"the_vault:coin_pile_lvl30\"}", 6);
                        }, failure -> {
                            failure.put("minecraft:water", 1);
                        })));
    }

    private static void paletteOrePlaceholder(Sink sink) {
        sink.add(GreedStageIds.id(2, "ore_placeholder"), new PaletteBuilder(), p -> p
                .placeholder(PlaceholderBlock.Type.ORE, ph -> ph
                        .probability(0, 0.14, success -> {
                            success.put("the_vault:ore_painite[type=vault_stone,generated=true]", 100);
                            success.put("the_vault:ore_alexandrite[type=vault_stone,generated=true]", 40);
                            success.put("the_vault:ore_wutodie[type=vault_stone,generated=true]", 80);
                            success.put("the_vault:ore_benitoite[type=vault_stone,generated=true]", 120);
                            success.put("the_vault:ore_larimar[type=vault_stone,generated=true]", 300);
                            success.put("the_vault:ore_black_opal[type=vault_stone,generated=true]", 20);
                            success.put("the_vault:ore_echo[type=vault_stone,generated=true]", 1);
                            success.put("the_vault:ore_iskallium[type=vault_stone,generated=true]", 5);
                            success.put("the_vault:ore_gorginite[type=vault_stone,generated=true]", 5);
                            success.put("the_vault:ore_ashium[type=vault_stone,generated=true]", 5);
                            success.put("the_vault:ore_sparkletine[type=vault_stone,generated=true]", 5);
                            success.put("the_vault:ore_bomignite[type=vault_stone,generated=true]", 5);
                            success.put("the_vault:ore_tubium[type=vault_stone,generated=true]", 5);
                            success.put("the_vault:ore_upaline[type=vault_stone,generated=true]", 5);
                            success.put("the_vault:ore_xenium[type=vault_stone,generated=true]", 5);
                            success.put("the_vault:ore_petzanite[type=vault_stone,generated=true]", 5);
                        }, failure -> {
                            failure.put("the_vault:vault_stone", 1);
                        })));
    }

    private static void palettePylonPlaceholder(Sink sink) {
        sink.add(GreedStageIds.id(2, "pylon_placeholder"), new PaletteBuilder(), p -> p
                .placeholder(PlaceholderBlock.Type.PYLON, ph -> ph
                        .probability(0, 0.08, success -> {
                            success.put("the_vault:pylon{Config:{type:effect,effect:regeneration,amplifier:10,duration:60,color:69A3FF67 ,description:\"Regenerates all your health\"}}", 10);
                            success.put("the_vault:pylon{Config:{type:effect,effect:regeneration,amplifier:1,duration:2400,color:85bdff54,description:\"Grants +1 Regeneration for 2 minutes\"}}", 20);
                            success.put("the_vault:pylon{Config:{type:effect,effect:speed,amplifier:1,duration:3600,color:69ffe58c,description:\"Grants +1 Speed for 3 minutes\"}}", 20);
                            success.put("the_vault:pylon{Config:{type:effect,effect:absorption,amplifier:1,duration:2400,color:6900FF00,description:\"Grants 2 absorption hearts for 2 minutes\"}}", 20);
                            success.put("the_vault:pylon{Config:{type:potion,charges:2,color:69c28cff,description:\"Recharges 2 potion charges\"}}", 20);
                            success.put("the_vault:pylon{Config:{type:mana,missingManaPercent:1.0,color:692F86AE,description:\"Refills your mana\"}}", 30);
                            success.put("the_vault:pylon{Config:{type:time,ticks:900,color:8000FFFF,description:\"Adds 30 seconds to the vault timer\"}}", 0);
                            success.put("the_vault:pylon{Config:{type:time,ticks:1800,color:8020ffff,description:\"Adds 1 minute and 30 seconds to the vault timer\"}}", 0);
                            success.put("the_vault:pylon{Config:{type:stat,stat:ITEM_RARITY,addend:0.20,duration:1200,color:69EAFF00,description:\"+20% Item Rarity for 1 minute\"}}", 10);
                            success.put("the_vault:pylon{Config:{type:stat,stat:ITEM_QUANTITY,addend:0.20,duration:1200,color:69ff9c00,description:\"+20% Item Quantity for 1 minute\"}}", 10);
                            success.put("the_vault:pylon{Config:{type:stat,stat:DURABILITY_WEAR_REDUCTION,addend:0.5,duration:6000,color:69b5b5b5,description:\"+50% Durability Damage Reduction for 5 minutes\"}}", 15);
                            success.put("the_vault:pylon{Config:{type:stat,stat:ITEM_RARITY,addend:1.00,duration:6000,color:69EAFF00,description:\"+100% Item Rarity for 5 minutes\",uber:1b,uberColor:69ff8a14}}", 1);
                            success.put("the_vault:pylon{Config:{type:stat,stat:ITEM_QUANTITY,addend:1.00,duration:6000,color:69ff9c00,description:\"+100% Item Quantity for 5 minutes\",uber:1b,uberColor:69ff9c00}}", 1);
                            success.put("the_vault:pylon{Config:{type:time,ticks:3600,color:8000FFFF,description:\"Adds 3 minutes to the vault timer\",uber:1b,uberColor:692F86AE}}", 0);
                            success.put("the_vault:pylon{Config:{type:effect,effect:speed,amplifier:3,duration:6000,color:69ffe58c,description:\"Grants +3 Speed for 5 minutes\",uber:1b,uberColor:69ffff87}}", 4);
                            success.put("the_vault:pylon{Config:{type:effect,effect:strength,amplifier:10,duration:6000,color:69e21b1b,description:\"Grants +10 Strength for 5 minutes\",uber:1b,uberColor:69fa52a9}}", 4);
                            success.put("the_vault:pylon{Config:{type:effect,effect:regeneration,amplifier:2,duration:6000,color:85bdff54,description:\"Grants +2 Regeneration for 5 minutes\",uber:1b,uberColor:695fe80d}}", 4);
                            success.put("the_vault:pylon{Config:{type:stat,stat:LUCKY_HIT_CHANCE,addend:0.50,duration:6000,color:696df5a3,description:\"+50% Lucky Hit Chance for 5 minutes\",uber:1b,uberColor:69fff705}}", 2);
                            success.put("the_vault:pylon{Config:{type:stat,stat:SOUL_CHANCE,addend:1.0,duration:6000,color:696900cc,description:\"+100% Soul Shard Chance for 5 minutes\",uber:1b,uberColor:69f03232}}", 2);
                            success.put("the_vault:pylon{Config:{type:stat,stat:COPIOUSLY,addend:1.0,duration:6000,color:80ff8cc5 ,description:\"+100% Copiously Chance for 5 minutes\",uber:1b,uberColor:80ffffff}}", 4);
                            success.put("the_vault:pylon{Config:{type:potion,charges:6,color:69c28cff,description:\"Recharges your potion fully\",uber:1b,uberColor:69ffffff}}", 2);
                        }, failure -> {
                            failure.put("minecraft:air", 1);
                        })));
    }

    private static void paletteVendorPedestalPlaceholder(Sink sink) {
        sink.add(GreedStageIds.id(2, "vendor_pedestal_placeholder"), new PaletteBuilder(), p -> p
                .placeholder(PlaceholderBlock.Type.VENDOR_PEDESTAL, ph -> ph
                        .probability(0, 1.0, success -> {
                            success.put("the_vault:shop_pedestal", 4);
                            success.put("woldsvaults:rare_shop_pedestal", 4);
                            success.put("woldsvaults:omega_shop_pedestal", 2);
                        }, failure -> {
                            failure.put("minecraft:air", 1);
                        })));
    }

    private static void paletteDungeonRoomsGildedChestPlaceholder(Sink sink) {
        sink.add(GreedStageIds.id(2, "dungeon_rooms/gilded_chest_placeholder"), new PaletteBuilder(), p -> p
                .leveled(leveled -> leveled
                        .list(0, "weighted_target", "the_vault:placeholder", out -> {
                            out.put("the_vault:placeholder{ Hidden: 1b }", 1);
                        }))
                .placeholder(PlaceholderBlock.Type.WOODEN_CHEST, ph -> ph
                        .probability(50, 0.75, success -> {
                            success.put("the_vault:gilded_barrel{LootTable:\"" + GreedStageIds.ref(2, "gilded_dungeon_barrel") + "\"}", 1);
                            success.put("the_vault:living_barrel{LootTable:\"" + GreedStageIds.ref(2, "living_dungeon_barrel") + "\"}", 1);
                            success.put("the_vault:ornate_barrel{LootTable:\"" + GreedStageIds.ref(2, "ornate_dungeon_barrel") + "\"}", 1);
                            success.put("the_vault:wooden_barrel{LootTable:\"" + GreedStageIds.ref(2, "wooden_dungeon_barrel") + "\"}", 1);
                        }, failure -> {
                            failure.put("minecraft:air", 1);
                        }))
                .placeholder(PlaceholderBlock.Type.COIN_STACKS, ph -> ph
                        .probability(50, 1.0, success -> {
                            success.put("the_vault:gilded_barrel{LootTable:\"" + GreedStageIds.ref(2, "gilded_dungeon_barrel") + "\"}", 1);
                            success.put("the_vault:ornate_barrel{LootTable:\"" + GreedStageIds.ref(2, "ornate_dungeon_barrel") + "\"}", 1);
                            success.put("the_vault:living_barrel{LootTable:\"" + GreedStageIds.ref(2, "living_dungeon_barrel") + "\"}", 1);
                            success.put("the_vault:wooden_barrel{LootTable:\"" + GreedStageIds.ref(2, "wooden_dungeon_barrel") + "\"}", 1);
                        }, failure -> {
                            failure.put("minecraft:air", 1);
                        })));
    }

    private static void paletteTreasureDoorPlaceholder(Sink sink) {
        sink.add(GreedStageIds.id(2, "treasure_door_placeholder"), new PaletteBuilder(), p -> p
                .placeholder(PlaceholderBlock.Type.TREASURE_DOOR, ph -> ph
                        .probability(0, 0.0, success -> {
                            success.put("the_vault:error_block", 1);
                        }, failure -> {
                            failure.put("minecraft:stone", 1);
                        })
                        .probability(20, 0.15, success -> {
                            success.put("the_vault:vendor_door", 1);
                        }, failure -> {
                            failure.put("minecraft:stone", 1);
                        })
                        .probability(50, 0.15, success -> {
                            success.put("the_vault:vendor_door", 2);
                            success.put("the_vault:treasure_door", 1);
                        }, failure -> {
                            failure.put("minecraft:stone", 1);
                        }))
                .weightedTargetGeneric("the_vault:vendor_door", out -> {
                    out.add(door(9, GreedStageIds.ref(2, "vendor_rooms/vendor_rooms"), "the_vault:vendor_room", GreedStageIds.ref(2, "vendor_rooms/vendor_rooms")));
                    out.add(door(1, GreedStageIds.ref(2, "vendor_rooms/etching_rooms"), "the_vault:vendor_room", "the_vault:vendor_rooms/etching_rooms"));
                })
                .weightedTargetGeneric("the_vault:treasure_door", out -> {
                    out.add(door(1, "the_vault:vault/treasure_rooms/treasure_rooms", "the_vault:treasure_room", "the_vault:treasure_rooms/treasure_rooms"));
                })
                .weightedTarget("the_vault:treasure_door", w -> w
                        .add("[type=iskallium]", 1)
                        .add("[type=xenium]", 1)
                        .add("[type=petzanite]", 1)
                        .add("[type=sparkletine]", 1)
                        .add("[type=upaline]", 1)
                        .add("[type=tubium]", 1)
                        .add("[type=gorginite]", 1)
                        .add("[type=bomignite]", 1)
                        .add("[type=ashium]", 1))
                .reference(GreedStageIds.ref(2, "dungeon_door_placeholder")));
    }

    private static void paletteVendorRoomsVendorRooms(Sink sink) {
        sink.add(GreedStageIds.id(2, "vendor_rooms/vendor_rooms"), new PaletteBuilder(), p -> p
                .placeholder(PlaceholderBlock.Type.VENDOR_PEDESTAL, ph -> ph
                        .probability(0, 1.0, success -> {
                            success.put("the_vault:shop_pedestal", 6);
                            success.put("woldsvaults:blacksmith_shop_pedestal", 2);
                            success.put("woldsvaults:god_shop_pedestal", 2);
                            success.put("woldsvaults:rare_shop_pedestal", 2);
                            success.put("woldsvaults:omega_shop_pedestal", 1);
                        }, failure -> {
                            failure.put("minecraft:air", 1);
                        }))
                .weightedTarget("minecraft:chest", w -> w
                        .add("minecraft:conduit", 1)
                        .add("minecraft:air", 10))
                .weightedTarget("minecraft:acacia_planks", w -> w
                        .add("the_vault:altar_chest_placeable", 1)
                        .add("minecraft:air", 10))
                .templateStackTile("@the_vault:vault_lootables", "STRUCTURE_VENDOR_ROOM"));
    }

    private static void paletteVendorRoomsBarterExpert(Sink sink) {
        sink.add(GreedStageIds.id(2, "vendor_rooms/barter_expert"), new PaletteBuilder(), p -> p
                .placeholder(PlaceholderBlock.Type.VENDOR_PEDESTAL, ph -> ph
                        .probability(0, 1.0, success -> {
                            success.put("the_vault:shop_pedestal", 20);
                            success.put("woldsvaults:rare_shop_pedestal", 25);
                            success.put("woldsvaults:god_shop_pedestal", 18);
                            success.put("woldsvaults:omega_shop_pedestal", 17);
                            success.put("woldsvaults:blacksmith_shop_pedestal", 15);
                        }, failure -> {
                            failure.put("minecraft:air", 1);
                        })));
    }

    private static void paletteCometObservatoryBase(Sink sink) {
        sink.add(GreedStageIds.id(2, "comet_observatory/base"), new PaletteBuilder(), p -> p
                .weightedTarget("the_vault:placeholder[type=\"objective\"]", w -> w
                        .add("the_vault:block_gem_larimar", 1)
                        .add("the_vault:block_gem_benitoite", 1)
                        .add("the_vault:block_gem_wutodie", 1))
                .reference("the_vault:generic/comet_pedestal_placeholder")
                .reference("the_vault:generic/dungeon_door_placeholder_comet")
                .weightedTarget("minecraft:mycelium", w -> w
                        .add("woldsvaults:puzzle_cube[color=blue]{LootTable:\"the_vault:cube_block_lvl0\"}", 1)
                        .add("woldsvaults:puzzle_cube[color=yellow]{LootTable:\"the_vault:cube_block_lvl0\"}", 1)
                        .add("woldsvaults:puzzle_cube[color=green]{LootTable:\"the_vault:cube_block_lvl0\"}", 1)
                        .add("woldsvaults:puzzle_cube[color=red]{LootTable:\"the_vault:cube_block_lvl0\"}", 1))
                .leveled(leveled -> leveled
                        .list(0, "weighted_target", "the_vault:treasure_sand", out -> {
                            out.put("the_vault:treasure_sand{LootTable:\"the_vault:treasure_sand_lvl0\"}", 1);
                        }))
                .placeholder(PlaceholderBlock.Type.DUNGEON_DISCOVERABLE, ph -> ph
                        .probability(0, 0.4, success -> {
                            success.put("the_vault:imbuement_altar[locked=false]", 0);
                            success.put("the_vault:vault_globe[type=common,generated=true]", 6);
                            success.put("the_vault:vault_globe[type=ore,generated=true]", 6);
                            success.put("the_vault:vault_globe[type=challenge,generated=true]", 4);
                            success.put("the_vault:vault_globe[type=omega,generated=true]", 1);
                            success.put("the_vault:alchemy_archive[locked=false]", 12);
                        }, failure -> {
                            failure.put("woldsvaults:dungeon_pedestal{Generated:0b,LootTable:\"" + GreedStageIds.ref(2, "dungeon_pedestal") + "\"}", 1);
                        })
                        .probability(60, 0.5, success -> {
                            success.put("the_vault:imbuement_altar[locked=false]", 8);
                            success.put("the_vault:reclamation_altar[locked=false]", 4);
                            success.put("the_vault:vault_globe[type=common,generated=true]", 6);
                            success.put("the_vault:vault_globe[type=ore,generated=true]", 6);
                            success.put("the_vault:vault_globe[type=challenge,generated=true]", 4);
                            success.put("the_vault:vault_globe[type=omega,generated=true]", 1);
                            success.put("the_vault:alchemy_archive[locked=false]", 6);
                        }, failure -> {
                            failure.put("woldsvaults:dungeon_pedestal{Generated:0b,LootTable:\"" + GreedStageIds.ref(2, "dungeon_pedestal") + "\"}", 1);
                        }))
                .leveled(leveled -> leveled
                        .list(0, "weighted_target", "the_vault:enhancement_altar", out -> {
                            out.put("the_vault:enhancement_altar{config:{type:loot_chests,range:{type:uniform,min:15,max:25},filter:[\"WOODEN\",\"LIVING\",\"GILDED\",\"ORNATE\"],display:\"{'text':'Loot %s Chests'}\"}}", 1);
                            out.put("the_vault:enhancement_altar{config:{type:loot_chests,range:{type:uniform,min:8,max:14},filter:[\"WOODEN\"],display:\"{'text':'Loot %s Wooden Chests'}\"}}", 1);
                            out.put("the_vault:enhancement_altar{config:{type:loot_chests,range:{type:uniform,min:3,max:6},filter:[\"LIVING\"],display:\"{'text':'Loot %s Living Chests'}\"}}", 1);
                            out.put("the_vault:enhancement_altar{config:{type:loot_chests,range:{type:uniform,min:3,max:6},filter:[\"GILDED\"],display:\"{'text':'Loot %s Gilded Chests'}\"}}", 1);
                            out.put("the_vault:enhancement_altar{config:{type:loot_chests,range:{type:uniform,min:3,max:6},filter:[\"ORNATE\"],display:\"{'text':'Loot %s Ornate Chests'}\"}}", 1);
                            out.put("the_vault:enhancement_altar{config:{type:break_blocks,range:{type:uniform,min:5,max:10},filter:[\"@the_vault:ores\"],display:\"{'text':'Mine %s Ores'}\"}}", 1);
                            out.put("the_vault:enhancement_altar{config:{type:kill_mobs,range:{type:uniform,min:10,max:25},filter:[\"@the_vault:mobs\"],display:\"{'text':'Kill %s Mobs'}\"}}", 1);
                        })
                        .list(30, "weighted_target", "the_vault:enhancement_altar", out -> {
                            out.put("the_vault:enhancement_altar{config:{type:loot_chests,range:{type:uniform,min:25,max:35},filter:[\"WOODEN\",\"LIVING\",\"GILDED\",\"ORNATE\"],display:\"{'text':'Loot %s Chests'}\"}}", 1);
                            out.put("the_vault:enhancement_altar{config:{type:loot_chests,range:{type:uniform,min:14,max:20},filter:[\"WOODEN\"],display:\"{'text':'Loot %s Wooden Chests'}\"}}", 1);
                            out.put("the_vault:enhancement_altar{config:{type:loot_chests,range:{type:uniform,min:6,max:9},filter:[\"LIVING\"],display:\"{'text':'Loot %s Living Chests'}\"}}", 1);
                            out.put("the_vault:enhancement_altar{config:{type:loot_chests,range:{type:uniform,min:6,max:9},filter:[\"GILDED\"],display:\"{'text':'Loot %s Gilded Chests'}\"}}", 1);
                            out.put("the_vault:enhancement_altar{config:{type:loot_chests,range:{type:uniform,min:6,max:9},filter:[\"ORNATE\"],display:\"{'text':'Loot %s Ornate Chests'}\"}}", 1);
                            out.put("the_vault:enhancement_altar{config:{type:break_blocks,range:{type:uniform,min:10,max:15},filter:[\"@the_vault:ores\"],display:\"{'text':'Mine %s Ores'}\"}}", 1);
                            out.put("the_vault:enhancement_altar{config:{type:kill_mobs,range:{type:uniform,min:10,max:15},filter:[\"@the_vault:mobs\"],display:\"{'text':'Kill %s Mobs'}\"}}", 1);
                        })
                        .list(50, "weighted_target", "the_vault:enhancement_altar", out -> {
                            out.put("the_vault:enhancement_altar{config:{type:loot_chests,range:{type:uniform,min:35,max:45},filter:[\"WOODEN\",\"LIVING\",\"GILDED\",\"ORNATE\"],display:\"{'text':'Loot %s Chests'}\"}}", 1);
                            out.put("the_vault:enhancement_altar{config:{type:loot_chests,range:{type:uniform,min:20,max:26},filter:[\"WOODEN\"],display:\"{'text':'Loot %s Wooden Chests'}\"}}", 1);
                            out.put("the_vault:enhancement_altar{config:{type:loot_chests,range:{type:uniform,min:9,max:12},filter:[\"LIVING\"],display:\"{'text':'Loot %s Living Chests'}\"}}", 1);
                            out.put("the_vault:enhancement_altar{config:{type:loot_chests,range:{type:uniform,min:9,max:12},filter:[\"GILDED\"],display:\"{'text':'Loot %s Gilded Chests'}\"}}", 1);
                            out.put("the_vault:enhancement_altar{config:{type:loot_chests,range:{type:uniform,min:9,max:12},filter:[\"ORNATE\"],display:\"{'text':'Loot %s Ornate Chests'}\"}}", 1);
                            out.put("the_vault:enhancement_altar{config:{type:break_blocks,range:{type:uniform,min:15,max:20},filter:[\"@the_vault:ores\"],display:\"{'text':'Mine %s Ores'}\"}}", 1);
                            out.put("the_vault:enhancement_altar{config:{type:kill_mobs,range:{type:uniform,min:15,max:20},filter:[\"@the_vault:mobs\"],display:\"{'text':'Kill %s Mobs'}\"}}", 1);
                        })
                        .list(65, "weighted_target", "the_vault:enhancement_altar", out -> {
                            out.put("the_vault:enhancement_altar{config:{type:loot_chests,range:{type:uniform,min:45,max:55},filter:[\"WOODEN\",\"LIVING\",\"GILDED\",\"ORNATE\"],display:\"{'text':'Loot %s Chests'}\"}}", 1);
                            out.put("the_vault:enhancement_altar{config:{type:loot_chests,range:{type:uniform,min:26,max:32},filter:[\"WOODEN\"],display:\"{'text':'Loot %s Wooden Chests'}\"}}", 1);
                            out.put("the_vault:enhancement_altar{config:{type:loot_chests,range:{type:uniform,min:12,max:15},filter:[\"LIVING\"],display:\"{'text':'Loot %s Living Chests'}\"}}", 1);
                            out.put("the_vault:enhancement_altar{config:{type:loot_chests,range:{type:uniform,min:12,max:15},filter:[\"GILDED\"],display:\"{'text':'Loot %s Gilded Chests'}\"}}", 1);
                            out.put("the_vault:enhancement_altar{config:{type:loot_chests,range:{type:uniform,min:12,max:15},filter:[\"ORNATE\"],display:\"{'text':'Loot %s Ornate Chests'}\"}}", 1);
                            out.put("the_vault:enhancement_altar{config:{type:break_blocks,range:{type:uniform,min:20,max:25},filter:[\"@the_vault:ores\"],display:\"{'text':'Mine %s Ores'}\"}}", 1);
                            out.put("the_vault:enhancement_altar{config:{type:kill_mobs,range:{type:uniform,min:20,max:25},filter:[\"@the_vault:mobs\"],display:\"{'text':'Kill %s Mobs'}\"}}", 1);
                        }))
                .weightedTarget("minecraft:command_block", w -> w
                        .add("minecraft:air", 1))
                .reference(GreedStageIds.ref(2, "room_base")));
    }

    private static void paletteCoveBase(Sink sink) {
        sink.add(GreedStageIds.id(2, "cove/base"), new PaletteBuilder(), p -> p
                .weightedTarget("minecraft:gold_block", w -> w
                        .add("the_vault:fools_gold", 25)
                        .add("minecraft:air", 975))
                .weightedTarget("the_vault:placeholder[type=coin_stacks]", w -> w
                        .add("the_vault:placeholder[type=coin_stacks]", 35)
                        .add("minecraft:air", 65))
                .weightedTarget("the_vault:placeholder[type=coin_stacks_waterlogged]", w -> w
                        .add("the_vault:placeholder[type=coin_stacks_waterlogged]", 35)
                        .add("minecraft:water", 65))
                .placeholder(PlaceholderBlock.Type.COIN_STACKS, ph -> ph
                        .probability(0, 0.9, success -> {
                            success.put("the_vault:coin_pile[size=3,waterlogged=false]{LootTable:\"the_vault:coin_pile_lvl0\"}", 20);
                            success.put("the_vault:coin_pile[size=4,waterlogged=false]{LootTable:\"the_vault:coin_pile_lvl0\"}", 40);
                            success.put("the_vault:coin_pile[size=5,waterlogged=false]{LootTable:\"the_vault:coin_pile_lvl0\"}", 22);
                            success.put("the_vault:coin_pile[size=6,waterlogged=false]{LootTable:\"the_vault:coin_pile_lvl0\"}", 7);
                            success.put("the_vault:coin_pile[size=7,waterlogged=false]{LootTable:\"the_vault:coin_pile_lvl0\"}", 4);
                        }, failure -> {
                            failure.put("minecraft:air", 1);
                        })
                        .probability(20, 0.9, success -> {
                            success.put("the_vault:coin_pile[size=3,waterlogged=false]{LootTable:\"the_vault:coin_pile_lvl0\"}", 20);
                            success.put("the_vault:coin_pile[size=4,waterlogged=false]{LootTable:\"the_vault:coin_pile_lvl0\"}", 48);
                            success.put("the_vault:coin_pile[size=5,waterlogged=false]{LootTable:\"the_vault:coin_pile_lvl0\"}", 28);
                            success.put("the_vault:coin_pile[size=6,waterlogged=false]{LootTable:\"the_vault:coin_pile_lvl0\"}", 15);
                            success.put("the_vault:coin_pile[size=7,waterlogged=false]{LootTable:\"the_vault:coin_pile_lvl0\"}", 4);
                        }, failure -> {
                            failure.put("minecraft:air", 1);
                        })
                        .probability(30, 0.9, success -> {
                            success.put("the_vault:coin_pile[size=3,waterlogged=false]{LootTable:\"the_vault:coin_pile_lvl30\"}", 20);
                            success.put("the_vault:coin_pile[size=4,waterlogged=false]{LootTable:\"the_vault:coin_pile_lvl30\"}", 48);
                            success.put("the_vault:coin_pile[size=5,waterlogged=false]{LootTable:\"the_vault:coin_pile_lvl30\"}", 28);
                            success.put("the_vault:coin_pile[size=6,waterlogged=false]{LootTable:\"the_vault:coin_pile_lvl30\"}", 15);
                            success.put("the_vault:coin_pile[size=7,waterlogged=false]{LootTable:\"the_vault:coin_pile_lvl30\"}", 4);
                        }, failure -> {
                            failure.put("minecraft:air", 1);
                        }))
                .placeholder(PlaceholderBlock.Type.COIN_STACKS_WATERLOGGED, ph -> ph
                        .probability(0, 0.9, success -> {
                            success.put("the_vault:coin_pile[size=3,waterlogged=true]{LootTable:\"the_vault:coin_pile_lvl0\"}", 20);
                            success.put("the_vault:coin_pile[size=4,waterlogged=true]{LootTable:\"the_vault:coin_pile_lvl0\"}", 40);
                            success.put("the_vault:coin_pile[size=5,waterlogged=true]{LootTable:\"the_vault:coin_pile_lvl0\"}", 22);
                            success.put("the_vault:coin_pile[size=6,waterlogged=true]{LootTable:\"the_vault:coin_pile_lvl0\"}", 7);
                            success.put("the_vault:coin_pile[size=7,waterlogged=true]{LootTable:\"the_vault:coin_pile_lvl0\"}", 4);
                        }, failure -> {
                            failure.put("minecraft:water", 1);
                        })
                        .probability(20, 0.9, success -> {
                            success.put("the_vault:coin_pile[size=3,waterlogged=true]{LootTable:\"the_vault:coin_pile_lvl0\"}", 20);
                            success.put("the_vault:coin_pile[size=4,waterlogged=true]{LootTable:\"the_vault:coin_pile_lvl0\"}", 48);
                            success.put("the_vault:coin_pile[size=5,waterlogged=true]{LootTable:\"the_vault:coin_pile_lvl0\"}", 28);
                            success.put("the_vault:coin_pile[size=6,waterlogged=true]{LootTable:\"the_vault:coin_pile_lvl0\"}", 15);
                            success.put("the_vault:coin_pile[size=7,waterlogged=true]{LootTable:\"the_vault:coin_pile_lvl0\"}", 4);
                        }, failure -> {
                            failure.put("minecraft:water", 1);
                        })
                        .probability(30, 0.9, success -> {
                            success.put("the_vault:coin_pile[size=3,waterlogged=true]{LootTable:\"the_vault:coin_pile_lvl30\"}", 20);
                            success.put("the_vault:coin_pile[size=4,waterlogged=true]{LootTable:\"the_vault:coin_pile_lvl30\"}", 48);
                            success.put("the_vault:coin_pile[size=5,waterlogged=true]{LootTable:\"the_vault:coin_pile_lvl30\"}", 28);
                            success.put("the_vault:coin_pile[size=6,waterlogged=true]{LootTable:\"the_vault:coin_pile_lvl30\"}", 15);
                            success.put("the_vault:coin_pile[size=7,waterlogged=true]{LootTable:\"the_vault:coin_pile_lvl30\"}", 4);
                        }, failure -> {
                            failure.put("minecraft:water", 1);
                        }))
                .reference(GreedStageIds.ref(2, "room_base")));
    }

    private static void paletteGraveyardBase(Sink sink) {
        sink.add(GreedStageIds.id(2, "graveyard/base"), new PaletteBuilder(), p -> p
                .weightedTarget("the_vault:placeholder[type=\"wooden_chest\"]", w -> w
                        .add("the_vault:placeholder[type=gilded_chest]", 4)
                        .add("the_vault:placeholder[type=living_chest]", 10)
                        .add("the_vault:placeholder[type=ornate_chest]", 7)
                        .add("the_vault:placeholder[type=wooden_chest]", 10)
                        .add("minecraft:air", 4))
                .weightedTarget("the_vault:placeholder[type=\"coin_stacks\"]", w -> w
                        .add("the_vault:placeholder[type=coin_stacks]", 4)
                        .add("minecraft:air", 4))
                .weightedTarget("minecraft:player_head", w -> w
                        .add("minecraft:player_head[rotation=0]{display:{Name:\"{\\\"text\\\":\\\"Skull\\\"}\"},SkullOwner:{Id:[I;-967193132,-466204297,-1498950319,-1900097605],Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWFlMzg1NWY5NTJjZDRhMDNjMTQ4YTk0NmUzZjgxMmE1OTU1YWQzNWNiY2I1MjYyN2VhNGFjZDQ3ZDMwODEifX19\"}]}}}", 1)
                        .add("minecraft:player_head[rotation=1]{display:{Name:\"{\\\"text\\\":\\\"Skull\\\"}\"},SkullOwner:{Id:[I;-967193132,-466204297,-1498950319,-1900097605],Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWFlMzg1NWY5NTJjZDRhMDNjMTQ4YTk0NmUzZjgxMmE1OTU1YWQzNWNiY2I1MjYyN2VhNGFjZDQ3ZDMwODEifX19\"}]}}}", 1)
                        .add("minecraft:player_head[rotation=2]{display:{Name:\"{\\\"text\\\":\\\"Skull\\\"}\"},SkullOwner:{Id:[I;-967193132,-466204297,-1498950319,-1900097605],Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWFlMzg1NWY5NTJjZDRhMDNjMTQ4YTk0NmUzZjgxMmE1OTU1YWQzNWNiY2I1MjYyN2VhNGFjZDQ3ZDMwODEifX19\"}]}}}", 1)
                        .add("minecraft:player_head[rotation=3]{display:{Name:\"{\\\"text\\\":\\\"Skull\\\"}\"},SkullOwner:{Id:[I;-967193132,-466204297,-1498950319,-1900097605],Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWFlMzg1NWY5NTJjZDRhMDNjMTQ4YTk0NmUzZjgxMmE1OTU1YWQzNWNiY2I1MjYyN2VhNGFjZDQ3ZDMwODEifX19\"}]}}}", 1)
                        .add("minecraft:player_head[rotation=4]{display:{Name:\"{\\\"text\\\":\\\"Skull\\\"}\"},SkullOwner:{Id:[I;-967193132,-466204297,-1498950319,-1900097605],Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWFlMzg1NWY5NTJjZDRhMDNjMTQ4YTk0NmUzZjgxMmE1OTU1YWQzNWNiY2I1MjYyN2VhNGFjZDQ3ZDMwODEifX19\"}]}}}", 1)
                        .add("minecraft:player_head[rotation=5]{display:{Name:\"{\\\"text\\\":\\\"Skull\\\"}\"},SkullOwner:{Id:[I;-967193132,-466204297,-1498950319,-1900097605],Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWFlMzg1NWY5NTJjZDRhMDNjMTQ4YTk0NmUzZjgxMmE1OTU1YWQzNWNiY2I1MjYyN2VhNGFjZDQ3ZDMwODEifX19\"}]}}}", 1)
                        .add("minecraft:player_head[rotation=6]{display:{Name:\"{\\\"text\\\":\\\"Skull\\\"}\"},SkullOwner:{Id:[I;-967193132,-466204297,-1498950319,-1900097605],Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWFlMzg1NWY5NTJjZDRhMDNjMTQ4YTk0NmUzZjgxMmE1OTU1YWQzNWNiY2I1MjYyN2VhNGFjZDQ3ZDMwODEifX19\"}]}}}", 1)
                        .add("minecraft:player_head[rotation=7]{display:{Name:\"{\\\"text\\\":\\\"Skull\\\"}\"},SkullOwner:{Id:[I;-967193132,-466204297,-1498950319,-1900097605],Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWFlMzg1NWY5NTJjZDRhMDNjMTQ4YTk0NmUzZjgxMmE1OTU1YWQzNWNiY2I1MjYyN2VhNGFjZDQ3ZDMwODEifX19\"}]}}}", 1)
                        .add("minecraft:player_head[rotation=8]{display:{Name:\"{\\\"text\\\":\\\"Skull\\\"}\"},SkullOwner:{Id:[I;-967193132,-466204297,-1498950319,-1900097605],Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWFlMzg1NWY5NTJjZDRhMDNjMTQ4YTk0NmUzZjgxMmE1OTU1YWQzNWNiY2I1MjYyN2VhNGFjZDQ3ZDMwODEifX19\"}]}}}", 1)
                        .add("minecraft:player_head[rotation=9]{display:{Name:\"{\\\"text\\\":\\\"Skull\\\"}\"},SkullOwner:{Id:[I;-967193132,-466204297,-1498950319,-1900097605],Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWFlMzg1NWY5NTJjZDRhMDNjMTQ4YTk0NmUzZjgxMmE1OTU1YWQzNWNiY2I1MjYyN2VhNGFjZDQ3ZDMwODEifX19\"}]}}}", 1)
                        .add("minecraft:player_head[rotation=10]{display:{Name:\"{\\\"text\\\":\\\"Skull\\\"}\"},SkullOwner:{Id:[I;-967193132,-466204297,-1498950319,-1900097605],Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWFlMzg1NWY5NTJjZDRhMDNjMTQ4YTk0NmUzZjgxMmE1OTU1YWQzNWNiY2I1MjYyN2VhNGFjZDQ3ZDMwODEifX19\"}]}}}", 1)
                        .add("minecraft:player_head[rotation=11]{display:{Name:\"{\\\"text\\\":\\\"Skull\\\"}\"},SkullOwner:{Id:[I;-967193132,-466204297,-1498950319,-1900097605],Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWFlMzg1NWY5NTJjZDRhMDNjMTQ4YTk0NmUzZjgxMmE1OTU1YWQzNWNiY2I1MjYyN2VhNGFjZDQ3ZDMwODEifX19\"}]}}}", 1)
                        .add("minecraft:player_head[rotation=12]{display:{Name:\"{\\\"text\\\":\\\"Skull\\\"}\"},SkullOwner:{Id:[I;-967193132,-466204297,-1498950319,-1900097605],Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWFlMzg1NWY5NTJjZDRhMDNjMTQ4YTk0NmUzZjgxMmE1OTU1YWQzNWNiY2I1MjYyN2VhNGFjZDQ3ZDMwODEifX19\"}]}}}", 1)
                        .add("minecraft:player_head[rotation=13]{display:{Name:\"{\\\"text\\\":\\\"Skull\\\"}\"},SkullOwner:{Id:[I;-967193132,-466204297,-1498950319,-1900097605],Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWFlMzg1NWY5NTJjZDRhMDNjMTQ4YTk0NmUzZjgxMmE1OTU1YWQzNWNiY2I1MjYyN2VhNGFjZDQ3ZDMwODEifX19\"}]}}}", 1)
                        .add("minecraft:player_head[rotation=14]{display:{Name:\"{\\\"text\\\":\\\"Skull\\\"}\"},SkullOwner:{Id:[I;-967193132,-466204297,-1498950319,-1900097605],Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWFlMzg1NWY5NTJjZDRhMDNjMTQ4YTk0NmUzZjgxMmE1OTU1YWQzNWNiY2I1MjYyN2VhNGFjZDQ3ZDMwODEifX19\"}]}}}", 1)
                        .add("minecraft:player_head[rotation=15]{display:{Name:\"{\\\"text\\\":\\\"Skull\\\"}\"},SkullOwner:{Id:[I;-967193132,-466204297,-1498950319,-1900097605],Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWFlMzg1NWY5NTJjZDRhMDNjMTQ4YTk0NmUzZjgxMmE1OTU1YWQzNWNiY2I1MjYyN2VhNGFjZDQ3ZDMwODEifX19\"}]}}}", 1))
                .placeholder(PlaceholderBlock.Type.VENDOR_PEDESTAL, ph -> ph
                        .probability(0, 0.1, success -> {
                            success.put("woldsvaults:spooky_shop_pedestal", 1);
                        }, failure -> {
                            failure.put("minecraft:air", 1);
                        }))
                .weightedTarget("woldsvaults:tombstone", w -> w
                        .add("woldsvaults:tombstone{LootTable:\"the_vault:tombstone_loot\"}", 1))
                .reference(GreedStageIds.ref(2, "room_base")));
    }

    private static void paletteLaboratoryBase(Sink sink) {
        sink.add(GreedStageIds.id(2, "laboratory/base"), new PaletteBuilder(), p -> p
                .weightedTarget("the_vault:animatrix_block", w -> w
                        .add("the_vault:animatrix_block{inventory:{Items:[{Count:1b,Slot:0,id:\"the_vault:elite_drowned_egg\"}],Size:1},displayOnly:1b}", 1)
                        .add("the_vault:animatrix_block{inventory:{Items:[{Count:1b,Slot:0,id:\"the_vault:elite_husk_egg\"}],Size:1},displayOnly:1b}", 1)
                        .add("the_vault:animatrix_block{inventory:{Items:[{Count:1b,Slot:0,id:\"the_vault:elite_skeleton_egg\"}],Size:1},displayOnly:1b}", 1)
                        .add("the_vault:animatrix_block{inventory:{Items:[{Count:1b,Slot:0,id:\"the_vault:elite_witch_egg\"}],Size:1},displayOnly:1b}", 1)
                        .add("the_vault:animatrix_block{inventory:{Items:[{Count:1b,Slot:0,id:\"the_vault:elite_zombie_egg\"}],Size:1},displayOnly:1b}", 1)
                        .add("the_vault:animatrix_block{inventory:{Items:[{Count:1b,Slot:0,id:\"the_vault:elite_enderman_egg\"}],Size:1},displayOnly:1b}", 1)
                        .add("the_vault:animatrix_block{inventory:{Items:[{Count:1b,Slot:0,id:\"the_vault:elite_stray_egg\"}],Size:1},displayOnly:1b}", 1)
                        .add("the_vault:animatrix_block{inventory:{Items:[{Count:1b,Slot:0,id:\"the_vault:elite_wither_skeleton_egg\"}],Size:1},displayOnly:1b}", 1)
                        .add("the_vault:animatrix_block", 1))
                .reference(GreedStageIds.ref(2, "room_base"))
                .reference("the_vault:generic/challenge_elite_spawners")
                .weightedTargetGeneric("the_vault:placeholder[type=objective]", out -> {
                    out.add(json("""
                        {
                          "block": "the_vault:elite_controller",
                          "renderer": {
                            "coreColor": "0x4266F5",
                            "glyphColor": "0x4266F5",
                            "particleColor": "0x4266F5"
                          },
                          "zone": {
                            "min": [
                              -23,
                              -20,
                              -23
                            ],
                            "max": [
                              23,
                              27,
                              23
                            ]
                          },
                          "proxies": [
                            {
                              "pos": [
                                [
                                  3,
                                  0,
                                  0
                                ],
                                [
                                  -3,
                                  0,
                                  0
                                ]
                              ],
                              "entries": [
                                {
                                  "action": "positive_lab",
                                  "probability": 1.0,
                                  "count": 1
                                }
                              ]
                            }
                          ],
                          "elite": [
                            {
                              "id": "the_vault:elite_drowned",
                              "weight": 1,
                              "modifierImmunity": [
                                "#the_vault:modifier_type/mob_frenzy",
                                "#the_vault:modifier_type/mob_attribute"
                              ]
                            },
                            {
                              "id": "the_vault:elite_husk",
                              "weight": 1,
                              "modifierImmunity": [
                                "#the_vault:modifier_type/mob_frenzy",
                                "#the_vault:modifier_type/mob_attribute"
                              ]
                            },
                            {
                              "id": "the_vault:elite_skeleton",
                              "weight": 1,
                              "modifierImmunity": [
                                "#the_vault:modifier_type/mob_frenzy",
                                "#the_vault:modifier_type/mob_attribute"
                              ]
                            },
                            {
                              "id": "the_vault:elite_stray",
                              "weight": 1,
                              "modifierImmunity": [
                                "#the_vault:modifier_type/mob_frenzy",
                                "#the_vault:modifier_type/mob_attribute"
                              ]
                            },
                            {
                              "id": "the_vault:elite_witch",
                              "weight": 1,
                              "modifierImmunity": [
                                "#the_vault:modifier_type/mob_frenzy",
                                "#the_vault:modifier_type/mob_attribute"
                              ]
                            },
                            {
                              "id": "the_vault:elite_wither_skeleton",
                              "weight": 1,
                              "modifierImmunity": [
                                "#the_vault:modifier_type/mob_frenzy",
                                "#the_vault:modifier_type/mob_attribute"
                              ]
                            },
                            {
                              "id": "the_vault:elite_zombie",
                              "weight": 1,
                              "modifierImmunity": [
                                "#the_vault:modifier_type/mob_frenzy",
                                "#the_vault:modifier_type/mob_attribute"
                              ]
                            }
                          ],
                          "weight": 1
                        }
                        """));
                }));
    }

    private static void paletteLabyrinthBase(Sink sink) {
        sink.add(GreedStageIds.id(2, "labyrinth/base"), new PaletteBuilder(), p -> p
                .weightedTarget("the_vault:placeholder[type=\"objective\"]", w -> w
                        .add("minecraft:air", 1))
                .weightedTarget("the_vault:placeholder[type=\"ornate_chest_guaranteed\"]", w -> w
                        .add("the_vault:placeholder[type=ornate_chest]", 1))
                .weightedTarget("the_vault:placeholder[type=\"ornate_chest\"]", w -> w
                        .add("the_vault:placeholder[type=ornate_chest]", 7)
                        .add("minecraft:air", 3))
                .weightedTarget("the_vault:placeholder[type=\"living_chest_guaranteed\"]", w -> w
                        .add("the_vault:placeholder[type=living_chest]", 1))
                .weightedTarget("the_vault:placeholder[type=\"wooden_chest\"]", w -> w
                        .add("the_vault:placeholder[type=wooden_chest]", 7)
                        .add("minecraft:air", 3))
                .weightedTarget("the_vault:placeholder[type=\"wooden_chest_guaranteed\"]", w -> w
                        .add("the_vault:placeholder[type=wooden_chest]", 1))
                .weightedTarget("the_vault:placeholder[type=\"living_chest\"]", w -> w
                        .add("the_vault:placeholder[type=living_chest]", 7)
                        .add("minecraft:air", 3))
                .weightedTarget("the_vault:placeholder[type=\"gilded_chest_guaranteed\"]", w -> w
                        .add("the_vault:placeholder[type=gilded_chest]", 1))
                .weightedTarget("the_vault:placeholder[type=\"gilded_chest\"]", w -> w
                        .add("the_vault:placeholder[type=gilded_chest]", 7)
                        .add("minecraft:air", 3))
                .weightedTarget("the_vault:placeholder[type=\"coin_stacks\"]", w -> w
                        .add("the_vault:placeholder[type=coin_stacks]", 5)
                        .add("minecraft:air", 5))
                .weightedTarget("the_vault:placeholder[type=\"coin_stacks_guaranteed\"]", w -> w
                        .add("the_vault:placeholder[type=coin_stacks]", 1))
                .weightedTarget("the_vault:black_chromatic_steel_block", w -> w
                        .add("minecraft:air", 1))
                .placeholder(PlaceholderBlock.Type.ORE, ph -> ph
                        .probability(0, 0.8, success -> {
                            success.put("the_vault:ore_echo[type=vault_stone,generated=true]", 1);
                            success.put("the_vault:ore_black_opal[type=vault_stone,generated=true]", 3);
                            success.put("the_vault:ore_iskallium[type=vault_stone,generated=true]", 5);
                            success.put("the_vault:ore_gorginite[type=vault_stone,generated=true]", 5);
                            success.put("the_vault:ore_ashium[type=vault_stone,generated=true]", 5);
                            success.put("the_vault:ore_sparkletine[type=vault_stone,generated=true]", 5);
                            success.put("the_vault:ore_bomignite[type=vault_stone,generated=true]", 5);
                            success.put("the_vault:ore_tubium[type=vault_stone,generated=true]", 5);
                            success.put("the_vault:ore_upaline[type=vault_stone,generated=true]", 5);
                            success.put("the_vault:ore_xenium[type=vault_stone,generated=true]", 5);
                            success.put("the_vault:ore_petzanite[type=vault_stone,generated=true]", 5);
                        }, failure -> {
                            failure.put("the_vault:vault_stone", 1);
                        }))
                .placeholder(PlaceholderBlock.Type.DUNGEON_DISCOVERABLE, ph -> ph
                        .probability(0, 0.4, success -> {
                            success.put("the_vault:imbuement_altar[locked=false]", 0);
                            success.put("the_vault:vault_globe[type=common,generated=true]", 6);
                            success.put("the_vault:vault_globe[type=ore,generated=true]", 6);
                            success.put("the_vault:vault_globe[type=challenge,generated=true]", 4);
                            success.put("the_vault:vault_globe[type=omega,generated=true]", 1);
                            success.put("the_vault:alchemy_archive[locked=false]", 12);
                        }, failure -> {
                            failure.put("woldsvaults:dungeon_pedestal{Generated:0b,LootTable:\"the_vault:dungeon_pedestal_lvl50\"}", 1);
                        })
                        .probability(60, 0.5, success -> {
                            success.put("the_vault:imbuement_altar[locked=false]", 8);
                            success.put("the_vault:reclamation_altar[locked=false]", 4);
                            success.put("the_vault:vault_globe[type=common,generated=true]", 6);
                            success.put("the_vault:vault_globe[type=ore,generated=true]", 6);
                            success.put("the_vault:vault_globe[type=challenge,generated=true]", 4);
                            success.put("the_vault:vault_globe[type=omega,generated=true]", 1);
                            success.put("the_vault:alchemy_archive[locked=false]", 6);
                        }, failure -> {
                            failure.put("woldsvaults:dungeon_pedestal{Generated:0b,LootTable:\"the_vault:dungeon_pedestal_lvl75\"}", 1);
                        }))
                .leveled(leveled -> leveled
                        .list(0, "weighted_target", "the_vault:treasure_sand", out -> {
                            out.put("the_vault:treasure_sand{LootTable:\"the_vault:treasure_sand_lvl0\"}", 9);
                            out.put("the_vault:treasure_sand{LootTable:\"woldsvaults:trove_sand\"}", 1);
                        }))
                .weightedTarget("the_vault:cube_block[color=blue]", w -> w
                        .add("woldsvaults:puzzle_cube[color=blue]{LootTable:\"the_vault:cube_block_lvl0\"}", 1))
                .weightedTarget("the_vault:cube_block[color=yellow]", w -> w
                        .add("woldsvaults:puzzle_cube[color=yellow]{LootTable:\"the_vault:cube_block_lvl0\"}", 1))
                .weightedTarget("the_vault:cube_block[color=green]", w -> w
                        .add("woldsvaults:puzzle_cube[color=green]{LootTable:\"the_vault:cube_block_lvl0\"}", 1))
                .weightedTarget("the_vault:cube_block[color=red]", w -> w
                        .add("woldsvaults:puzzle_cube[color=red]{LootTable:\"the_vault:cube_block_lvl0\"}", 1))
                .reference("the_vault:generic/dungeon_door_placeholder_comet")
                .reference("the_vault:generic/treasure_door_placeholder")
                .reference("the_vault:generic/comet_pedestal_placeholder")
                .reference(GreedStageIds.ref(2, "room_base")));
    }

    private static void paletteRaidBase(Sink sink) {
        sink.add(GreedStageIds.id(2, "raid/base"), new PaletteBuilder(), p -> p
                .reference("the_vault:generic/hidden_placeholder")
                .reference(GreedStageIds.ref(2, "room_base"))
                .reference("the_vault:generic/challenge_elite_spawners")
                .weightedTargetGeneric("the_vault:placeholder[type=objective]", out -> {
                    out.add(json("""
                        {
                          "block": "the_vault:raid_controller",
                          "renderer": {
                            "coreColor": "0xCC0000",
                            "glyphColor": "0xCC0000",
                            "particleColor": "0xCC0000"
                          },
                          "curses": [],
                          "zone": {
                            "min": [
                              -23,
                              -20,
                              -23
                            ],
                            "max": [
                              23,
                              27,
                              23
                            ]
                          },
                          "spawners": [
                            {
                              "pos": {
                                "min": [
                                  -20,
                                  -1,
                                  -20
                                ],
                                "max": [
                                  -6,
                                  3,
                                  20
                                ]
                              },
                              "groups": [
                                "ground"
                              ]
                            },
                            {
                              "pos": {
                                "min": [
                                  6,
                                  -1,
                                  -20
                                ],
                                "max": [
                                  20,
                                  3,
                                  20
                                ]
                              },
                              "groups": [
                                "ground"
                              ]
                            },
                            {
                              "pos": {
                                "min": [
                                  -20,
                                  -1,
                                  -20
                                ],
                                "max": [
                                  20,
                                  3,
                                  -6
                                ]
                              },
                              "groups": [
                                "ground"
                              ]
                            },
                            {
                              "pos": {
                                "min": [
                                  -20,
                                  -1,
                                  6
                                ],
                                "max": [
                                  20,
                                  3,
                                  20
                                ]
                              },
                              "groups": [
                                "ground"
                              ]
                            },
                            {
                              "pos": {
                                "min": [
                                  -20,
                                  6,
                                  -20
                                ],
                                "max": [
                                  20,
                                  27,
                                  20
                                ]
                              },
                              "groups": [
                                "elevated"
                              ]
                            }
                          ],
                          "waves": {
                            "type": "uniform",
                            "min": 4,
                            "max": 4
                          },
                          "proxies": [
                            {
                              "pos": [
                                [
                                  3,
                                  0,
                                  0
                                ],
                                [
                                  -3,
                                  0,
                                  0
                                ],
                                [
                                  0,
                                  0,
                                  3
                                ]
                              ],
                              "entries": [
                                {
                                  "action": "greed_stage_positive_base",
                                  "probability": 1.0,
                                  "count": 1
                                },
                                {
                                  "action": "greed_stage_negative",
                                  "probability": 1.0,
                                  "count": 1
                                },
                                {
                                  "action": [
                                    {
                                      "type": "group",
                                      "add_pillager": 6,
                                      "weight": 5
                                    },
                                    {
                                      "type": "group",
                                      "add_vindicator": 8,
                                      "weight": 5
                                    },
                                    {
                                      "type": "group",
                                      "add_ravager": 3,
                                      "weight": 5
                                    },
                                    {
                                      "type": "group",
                                      "add_healer": 3,
                                      "weight": 5
                                    },
                                    {
                                      "type": "group",
                                      "add_evoker": 3,
                                      "weight": 5
                                    },
                                    {
                                      "type": "group",
                                      "add_vex": 5,
                                      "weight": 5
                                    },
                                    {
                                      "type": "group",
                                      "add_mage": 4,
                                      "weight": 5
                                    },
                                    {
                                      "type": "group",
                                      "add_royal_guard": 2,
                                      "weight": 4
                                    },
                                    {
                                      "type": "group",
                                      "add_geomancer": 2,
                                      "weight": 3
                                    },
                                    {
                                      "type": "group",
                                      "add_windcaller": 2,
                                      "weight": 3
                                    },
                                    {
                                      "type": "group",
                                      "add_mountaineer": 3,
                                      "weight": 3
                                    },
                                    {
                                      "type": "group",
                                      "add_iceologer": 4,
                                      "weight": 3
                                    },
                                    {
                                      "type": "group",
                                      "add_vindicator_champion": 1,
                                      "weight": 3
                                    },
                                    {
                                      "type": "group",
                                      "add_vex_champion": 1,
                                      "weight": 3
                                    },
                                    {
                                      "type": "group",
                                      "add_pillager_champion": 1,
                                      "weight": 3
                                    },
                                    {
                                      "type": "group",
                                      "add_ravager_champion": 1,
                                      "weight": 3
                                    },
                                    {
                                      "type": "group",
                                      "add_royal_guard_champion": 1,
                                      "weight": 3
                                    }
                                  ],
                                  "probability": 1.0,
                                  "count": 1
                                }
                              ]
                            },
                            {
                              "pos": [
                                0,
                                0,
                                -3
                              ],
                              "entries": [
                                {
                                  "action": "forfeit",
                                  "probability": 1.0,
                                  "count": 1
                                }
                              ]
                            }
                          ],
                          "actions": [
                            {
                              "type": "group",
                              "add_vindicator": 8
                            },
                            {
                              "type": "group",
                              "add_pillager": 4
                            }
                          ],
                          "weight": 1
                        }
                        """));
                }));
    }

    private static void paletteWardensGardenBase(Sink sink) {
        sink.add(GreedStageIds.id(2, "wardens_garden/base"), new PaletteBuilder(), p -> p
                .weightedTarget("the_vault:placeholder[type=ornate_chest]", w -> w
                        .add("the_vault:placeholder[type=ornate_chest]", 65)
                        .add("minecraft:air", 35))
                .reference(GreedStageIds.ref(2, "room_base")));
    }

    private static void paletteWildwestBase(Sink sink) {
        sink.add(GreedStageIds.id(2, "wildwest/base"), new PaletteBuilder(), p -> p
                .reference(GreedStageIds.ref(2, "room_base"))
                .weightedTarget("minecraft:smooth_quartz", w -> w
                        .add("the_vault:golden_tooth[generated=true]", 1)
                        .add("minecraft:smooth_quartz", 49))
                .reference(GreedStageIds.ref(2, "room_base"))
                .weightedTargetGeneric("the_vault:placeholder[type=objective]", out -> {
                    out.add(json("""
                        {
                          "block": "the_vault:wild_west_controller",
                          "renderer": {
                            "coreColor": "0xf5b042",
                            "glyphColor": "0xf5b042",
                            "particleColor": "0xf5b042"
                          },
                          "curses": [
                            "the_vault:disable_cat"
                          ],
                          "zone": {
                            "min": [
                              -23,
                              -20,
                              -23
                            ],
                            "max": [
                              23,
                              27,
                              23
                            ]
                          },
                          "proxies": [
                            {
                              "pos": [
                                [
                                  3,
                                  0,
                                  0
                                ],
                                [
                                  -3,
                                  0,
                                  0
                                ]
                              ],
                              "entries": [
                                {
                                  "action": "positive_wildwest",
                                  "probability": 1.0,
                                  "count": 1
                                }
                              ]
                            }
                          ],
                          "multipliers": [
                            {
                              "evasionChance": 0.6,
                              "damageMultiplier": 1.2,
                              "accuracy": 1.0,
                              "speed": 1.0
                            },
                            {
                              "evasionChance": 0.6,
                              "damageMultiplier": 1.2,
                              "accuracy": 1.0,
                              "speed": 1.0
                            },
                            {
                              "evasionChance": 0.6,
                              "damageMultiplier": 1.2,
                              "accuracy": 1.0,
                              "speed": 1.0
                            },
                            {
                              "evasionChance": 0.6,
                              "damageMultiplier": 1.2,
                              "accuracy": 1.0,
                              "speed": 1.0
                            },
                            {
                              "evasionChance": 0.6,
                              "damageMultiplier": 1.2,
                              "accuracy": 1.0,
                              "speed": 1.0
                            }
                          ],
                          "gates": [
                            {
                              "pos": [
                                23,
                                0,
                                -1
                              ],
                              "rotation": 1
                            },
                            {
                              "pos": [
                                -24,
                                0,
                                -1
                              ],
                              "rotation": 1
                            },
                            {
                              "pos": [
                                -1,
                                0,
                                23
                              ],
                              "rotation": 0
                            },
                            {
                              "pos": [
                                -1,
                                0,
                                -23
                              ],
                              "rotation": 0
                            }
                          ],
                          "maxRounds": 5,
                          "countdownTicks": 100,
                          "weight": 1
                        }
                        """));
                }));
    }

    private static void paletteWoldsDinnerBase(Sink sink) {
        sink.add(GreedStageIds.id(2, "wolds_dinner/base"), new PaletteBuilder(), p -> p
                .weightedTarget("the_vault:placeholder[type=living_chest]", w -> w
                        .add("the_vault:placeholder[type=living_chest]", 65)
                        .add("minecraft:air", 35))
                .reference(GreedStageIds.ref(2, "room_base")));
    }

    private static void paletteTreasureChest(Sink sink) {
        sink.add(GreedStageIds.id(3, "treasure_chest"), new PaletteBuilder(), p -> p
                .leveled(leveled -> leveled
                        .list(0, "weighted_target", "the_vault:treasure_container", out -> {
                            out.put("the_vault:treasure_container{Generated:0b,LootTable:\"the_vault:treasure_chest_lvl0\"}", 1);
                        })
                        .list(50, "weighted_target", "the_vault:treasure_container", out -> {
                            out.put("the_vault:treasure_container{Generated:0b,LootTable:\"" + GreedStageIds.ref(3, "treasure_chest") + "\"}", 3);
                        }))
                .leveled(leveled -> leveled
                        .list(0, "weighted_target", "the_vault:treasure_chest", out -> {
                            out.put("the_vault:treasure_container{Generated:0b,LootTable:\"" + GreedStageIds.ref(3, "treasure_chest") + "\"}", 1);
                        }))
                .leveled(leveled -> leveled
                        .list(0, "weighted_target", "minecraft:lime_concrete", out -> {
                            out.put("the_vault:treasure_container{Generated:0b,LootTable:\"" + GreedStageIds.ref(3, "treasure_chest_velara") + "\"}", 1);
                        }))
                .leveled(leveled -> leveled
                        .list(0, "weighted_target", "minecraft:blue_concrete", out -> {
                            out.put("the_vault:treasure_container{Generated:0b,LootTable:\"" + GreedStageIds.ref(3, "treasure_chest_tenos") + "\"}", 1);
                        }))
                .leveled(leveled -> leveled
                        .list(0, "weighted_target", "minecraft:yellow_concrete", out -> {
                            out.put("the_vault:treasure_container{Generated:0b,LootTable:\"" + GreedStageIds.ref(3, "treasure_chest_wendarr") + "\"}", 1);
                        }))
                .leveled(leveled -> leveled
                        .list(0, "weighted_target", "minecraft:red_concrete", out -> {
                            out.put("the_vault:treasure_container{Generated:0b,LootTable:\"" + GreedStageIds.ref(3, "treasure_chest_idona") + "\"}", 1);
                        }))
                .leveled(leveled -> leveled
                        .list(0, "weighted_target", "the_vault:treasure_pedestal", out -> {
                            out.put("the_vault:treasure_pedestal{LootTable:\"" + GreedStageIds.ref(3, "treasure_pillar") + "\"}", 7);
                            out.put("minecraft:air", 3);
                        }))
                .leveled(leveled -> leveled
                        .list(0, "weighted_target", "minecraft:light_blue_wool", out -> {
                            out.put("the_vault:treasure_pedestal{LootTable:\"" + GreedStageIds.ref(3, "treasure_pillar") + "\"}", 3);
                            out.put("minecraft:air", 7);
                        }))
                .leveled(leveled -> leveled
                        .list(0, "weighted_target", "minecraft:blue_wool", out -> {
                            out.put("the_vault:treasure_pedestal{LootTable:\"" + GreedStageIds.ref(3, "treasure_pillar") + "\"}", 1);
                        }))
                .weightedTarget("the_vault:treasure_sand", w -> w
                        .add("the_vault:treasure_sand{LootTable:\"the_vault:treasure_sand_lvl0\"}", 1))
                .weightedTarget("the_vault:vault_stone", w -> w
                        .add("the_vault:ore_painite[type=vault_stone,generated=true]", 75)
                        .add("the_vault:ore_alexandrite[type=vault_stone,generated=true]", 35)
                        .add("the_vault:ore_wutodie[type=vault_stone,generated=true]", 35)
                        .add("the_vault:ore_benitoite[type=vault_stone,generated=true]", 50)
                        .add("the_vault:ore_larimar[type=vault_stone,generated=true]", 40)
                        .add("the_vault:ore_black_opal[type=vault_stone,generated=true]", 75)
                        .add("the_vault:ore_echo[type=vault_stone,generated=true]", 5)
                        .add("the_vault:vault_stone", 70))
                .placeholder(PlaceholderBlock.Type.PYLON, ph -> ph
                        .probability(0, 0.75, success -> {
                            success.put("woldsvaults:locked_treasure_container[type=ashium]{Generated:0b,LootTable:\"" + GreedStageIds.ref(3, "treasure_chest") + "\"}", 1);
                            success.put("woldsvaults:locked_treasure_container[type=bomignite]{Generated:0b,LootTable:\"" + GreedStageIds.ref(3, "treasure_chest") + "\"}", 1);
                            success.put("woldsvaults:locked_treasure_container[type=gorginite]{Generated:0b,LootTable:\"" + GreedStageIds.ref(3, "treasure_chest") + "\"}", 1);
                            success.put("woldsvaults:locked_treasure_container[type=iskallium]{Generated:0b,LootTable:\"" + GreedStageIds.ref(3, "treasure_chest") + "\"}", 1);
                            success.put("woldsvaults:locked_treasure_container[type=petzanite]{Generated:0b,LootTable:\"" + GreedStageIds.ref(3, "treasure_chest") + "\"}", 1);
                            success.put("woldsvaults:locked_treasure_container[type=sparkletine]{Generated:0b,LootTable:\"" + GreedStageIds.ref(3, "treasure_chest") + "\"}", 1);
                            success.put("woldsvaults:locked_treasure_container[type=tubium]{Generated:0b,LootTable:\"" + GreedStageIds.ref(3, "treasure_chest") + "\"}", 1);
                            success.put("woldsvaults:locked_treasure_container[type=upaline]{Generated:0b,LootTable:\"" + GreedStageIds.ref(3, "treasure_chest") + "\"}", 1);
                            success.put("woldsvaults:locked_treasure_container[type=xenium]{Generated:0b,LootTable:\"" + GreedStageIds.ref(3, "treasure_chest") + "\"}", 1);
                        }, failure -> {
                            failure.put("minecraft:air", 1);
                        }))
                .placeholder(PlaceholderBlock.Type.ORE, ph -> ph
                        .probability(0, 0.4, success -> {
                            success.put("the_vault:ore_painite[type=vault_stone,generated=true]", 60);
                            success.put("the_vault:ore_alexandrite[type=vault_stone,generated=true]", 30);
                            success.put("the_vault:ore_wutodie[type=vault_stone,generated=true]", 30);
                            success.put("the_vault:ore_benitoite[type=vault_stone,generated=true]", 40);
                            success.put("the_vault:ore_larimar[type=vault_stone,generated=true]", 30);
                            success.put("the_vault:ore_black_opal[type=vault_stone,generated=true]", 75);
                            success.put("the_vault:ore_echo[type=vault_stone,generated=true]", 10);
                        }, failure -> {
                            failure.put("the_vault:vault_stone", 1);
                        }))
                .weightedTarget("the_vault:polished_vault_stone", w -> w
                        .add("the_vault:ore_painite[type=vault_stone,generated=true]", 60)
                        .add("the_vault:ore_black_opal[type=vault_stone,generated=true]", 20)
                        .add("the_vault:vault_stone", 20))
                .weightedTarget("woldsvaults:hellish_sand", w -> w
                        .add("woldsvaults:hellish_sand{LootTable:\"the_vault:hellish_sand_idona\"}", 1))
                .weightedTarget("woldsvaults:iskallian_leaves", w -> w
                        .add("woldsvaults:iskallian_leaves{LootTable:\"the_vault:iskallian_leaves_velara\"}", 1))
                .placeholder(PlaceholderBlock.Type.LIVING_CHEST, ph -> ph
                        .probability(50, 1.0, success -> {
                            success.put("the_vault:living_strongbox{LootTable:\"" + GreedStageIds.ref(1, "living_strongbox") + "\"}", 1);
                        }, failure -> {
                            failure.put("minecraft:air", 1);
                        })
                        .probability(60, 1.0, success -> {
                            success.put("the_vault:living_strongbox{LootTable:\"" + GreedStageIds.ref(1, "living_strongbox") + "\"}", 1);
                        }, failure -> {
                            failure.put("minecraft:air", 1);
                        })
                        .probability(70, 1.0, success -> {
                            success.put("the_vault:living_strongbox{LootTable:\"" + GreedStageIds.ref(1, "living_strongbox") + "\"}", 1);
                        }, failure -> {
                            failure.put("minecraft:air", 1);
                        })
                        .probability(80, 1.0, success -> {
                            success.put("the_vault:living_strongbox{LootTable:\"" + GreedStageIds.ref(1, "living_strongbox") + "\"}", 1);
                        }, failure -> {
                            failure.put("minecraft:air", 1);
                        })
                        .probability(100, 1.0, success -> {
                            success.put("the_vault:living_strongbox{LootTable:\"" + GreedStageIds.ref(1, "living_strongbox") + "\"}", 1);
                        }, failure -> {
                            failure.put("minecraft:air", 1);
                        }))
                .placeholder(PlaceholderBlock.Type.ORNATE_CHEST, ph -> ph
                        .probability(50, 1.0, success -> {
                            success.put("the_vault:ornate_strongbox{LootTable:\"" + GreedStageIds.ref(1, "ornate_strongbox") + "\"}", 1);
                        }, failure -> {
                            failure.put("minecraft:air", 1);
                        })
                        .probability(65, 1.0, success -> {
                            success.put("the_vault:ornate_strongbox{LootTable:\"" + GreedStageIds.ref(1, "ornate_strongbox") + "\"}", 1);
                        }, failure -> {
                            failure.put("minecraft:air", 1);
                        })
                        .probability(80, 1.0, success -> {
                            success.put("the_vault:ornate_strongbox{LootTable:\"" + GreedStageIds.ref(1, "ornate_strongbox") + "\"}", 1);
                        }, failure -> {
                            failure.put("minecraft:air", 1);
                        })
                        .probability(100, 1.0, success -> {
                            success.put("the_vault:ornate_strongbox{LootTable:\"" + GreedStageIds.ref(1, "ornate_strongbox") + "\"}", 1);
                        }, failure -> {
                            failure.put("minecraft:air", 1);
                        }))
                .placeholder(PlaceholderBlock.Type.GILDED_CHEST, ph -> ph
                        .probability(50, 1.0, success -> {
                            success.put("the_vault:gilded_strongbox{LootTable:\"" + GreedStageIds.ref(1, "gilded_strongbox") + "\"}", 1);
                        }, failure -> {
                            failure.put("minecraft:air", 1);
                        })
                        .probability(80, 1.0, success -> {
                            success.put("the_vault:gilded_strongbox{LootTable:\"" + GreedStageIds.ref(1, "gilded_strongbox") + "\"}", 1);
                        }, failure -> {
                            failure.put("minecraft:air", 1);
                        })
                        .probability(100, 1.0, success -> {
                            success.put("the_vault:gilded_strongbox{LootTable:\"" + GreedStageIds.ref(1, "gilded_strongbox") + "\"}", 1);
                        }, failure -> {
                            failure.put("minecraft:air", 1);
                        })));
    }

    private static void paletteTroveBase(Sink sink) {
        sink.add(GreedStageIds.id(3, "trove/base"), new PaletteBuilder(), p -> p
                .placeholder(PlaceholderBlock.Type.PYLON, ph -> ph
                        .probability(0, 0.75, success -> {
                            success.put("woldsvaults:locked_treasure_container[type=ashium]{Generated:0b,LootTable:\"" + GreedStageIds.ref(3, "treasure_chest") + "\"}", 1);
                            success.put("woldsvaults:locked_treasure_container[type=bomignite]{Generated:0b,LootTable:\"" + GreedStageIds.ref(3, "treasure_chest") + "\"}", 1);
                            success.put("woldsvaults:locked_treasure_container[type=gorginite]{Generated:0b,LootTable:\"" + GreedStageIds.ref(3, "treasure_chest") + "\"}", 1);
                            success.put("woldsvaults:locked_treasure_container[type=iskallium]{Generated:0b,LootTable:\"" + GreedStageIds.ref(3, "treasure_chest") + "\"}", 1);
                            success.put("woldsvaults:locked_treasure_container[type=petzanite]{Generated:0b,LootTable:\"" + GreedStageIds.ref(3, "treasure_chest") + "\"}", 1);
                            success.put("woldsvaults:locked_treasure_container[type=sparkletine]{Generated:0b,LootTable:\"" + GreedStageIds.ref(3, "treasure_chest") + "\"}", 1);
                            success.put("woldsvaults:locked_treasure_container[type=tubium]{Generated:0b,LootTable:\"" + GreedStageIds.ref(3, "treasure_chest") + "\"}", 1);
                            success.put("woldsvaults:locked_treasure_container[type=upaline]{Generated:0b,LootTable:\"" + GreedStageIds.ref(3, "treasure_chest") + "\"}", 1);
                            success.put("woldsvaults:locked_treasure_container[type=xenium]{Generated:0b,LootTable:\"" + GreedStageIds.ref(3, "treasure_chest") + "\"}", 1);
                        }, failure -> {
                            failure.put("minecraft:air", 1);
                        }))
                .weightedTarget("minecraft:yellow_wool", w -> w
                        .add("the_vault:treasure_sand{LootTable:\"the_vault:digsite_sand_lvl0\"}", 1))
                .weightedTarget("minecraft:yellow_concrete_powder", w -> w
                        .add("the_vault:treasure_sand{LootTable:\"the_vault:treasure_sand_lvl0\"}", 1)
                        .add("the_vault:treasure_sand{LootTable:\"the_vault:digsite_sand_lvl0\"}", 3))
                .weightedTarget("minecraft:yellow_concrete", w -> w
                        .add("the_vault:treasure_sand{LootTable:\"woldsvaults:trove_sand\"}", 2)
                        .add("the_vault:treasure_sand{LootTable:\"the_vault:treasure_sand_lvl0\"}", 7)
                        .add("the_vault:treasure_sand{LootTable:\"the_vault:digsite_sand_lvl0\"}", 12))
                .weightedTarget("the_vault:placeholder[type=ornate_chest]", w -> w
                        .add("the_vault:placeholder[type=ornate_chest]", 1)
                        .add("minecraft:air", 0))
                .weightedTarget("the_vault:placeholder[type=living_chest]", w -> w
                        .add("the_vault:placeholder[type=living_chest]", 1)
                        .add("minecraft:air", 0))
                .weightedTarget("the_vault:placeholder[type=gilded_chest]", w -> w
                        .add("the_vault:placeholder[type=gilded_chest]", 1)
                        .add("minecraft:air", 0))
                .weightedTarget("the_vault:placeholder[type=coin_stacks]", w -> w
                        .add("the_vault:placeholder[type=coin_stacks]", 1)
                        .add("minecraft:air", 0))
                .reference(GreedStageIds.ref(2, "room_base")));
    }

    /**
     * A stage palette that equals the seeded normal palette apart from the ids in {@code remap}. Refuses a seeded
     * palette in which any of those ids no longer appears.
     */
    private static void derived(Sink sink, ResourceLocation id, String normalPath, Map<String, String> remap) {
        Map<String, Integer> hits = new HashMap<>();
        JsonElement palette = remap(seededPalette(normalPath), remap, hits);
        for (String from : remap.keySet()) {
            if (!hits.containsKey(from)) {
                throw new IllegalStateException("Seeded " + normalPath + " no longer points at " + from);
            }
        }
        sink.add(id, new PaletteBuilder(), builder -> translate(builder, palette.getAsJsonObject(), normalPath));
    }

    private static JsonObject seededPalette(String path) {
        return AbstractConfigPatchProvider.seeded("gen/1.0/palettes/" + path + ".json").getAsJsonObject();
    }

    private static JsonElement remap(JsonElement element, Map<String, String> remap, Map<String, Integer> hits) {
        if (element.isJsonObject()) {
            JsonObject out = new JsonObject();
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                out.add(remapString(entry.getKey(), remap, hits), remap(entry.getValue(), remap, hits));
            }
            return out;
        }
        if (element.isJsonArray()) {
            JsonArray out = new JsonArray();
            for (JsonElement child : element.getAsJsonArray()) {
                out.add(remap(child, remap, hits));
            }
            return out;
        }
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            return new JsonPrimitive(remapString(element.getAsString(), remap, hits));
        }
        return element;
    }

    private static String remapString(String text, Map<String, String> remap, Map<String, Integer> hits) {
        String out = text;
        for (Map.Entry<String, String> entry : remap.entrySet()) {
            Matcher matcher = Pattern.compile(Pattern.quote(entry.getKey()) + "(?![a-z0-9_/\\-])").matcher(out);
            if (matcher.find()) {
                hits.merge(entry.getKey(), 1, Integer::sum);
                out = matcher.replaceAll(Matcher.quoteReplacement(entry.getValue()));
            }
        }
        return out;
    }

    /** Rebuilds a palette's JSON through the palette builder; refuses processors the builder cannot write. */
    private static void translate(PaletteBuilder builder, JsonObject palette, String source) {
        for (Map.Entry<String, JsonElement> entry : palette.entrySet()) {
            boolean emptyList = entry.getValue().isJsonArray() && entry.getValue().getAsJsonArray().isEmpty();
            if (!entry.getKey().equals("tile_processors") && !emptyList) {
                throw new IllegalStateException("Palette " + source + " has " + entry.getKey() + ", which stage palettes do not support");
            }
        }
        for (JsonElement element : palette.getAsJsonArray("tile_processors")) {
            JsonObject processor = element.getAsJsonObject();
            String type = processor.get("type").getAsString();
            switch (type) {
                case "reference" -> builder.reference(processor.get("id").getAsString());
                case "weighted_target" -> {
                    String target = processor.get("target").getAsString();
                    JsonElement output = processor.get("output");
                    if (output.isJsonArray()) {
                        builder.weightedTargetGeneric(target, out -> out.addAll(output.getAsJsonArray().deepCopy()));
                    } else {
                        Map<String, Integer> weights = weights(output.getAsJsonObject(), source);
                        builder.weightedTarget(target, w -> weights.forEach(w::add));
                    }
                }
                case "placeholder" -> {
                    PlaceholderBlock.Type target = PlaceholderBlock.Type.fromString(processor.get("target").getAsString());
                    if (target == null) {
                        throw new IllegalStateException("Palette " + source + " names an unknown placeholder " + processor.get("target"));
                    }
                    builder.placeholder(target, placeholder -> {
                        for (JsonElement levelElement : processor.getAsJsonArray("levels")) {
                            JsonObject level = levelElement.getAsJsonObject();
                            Map<String, Integer> success = weights(level.getAsJsonObject("success"), source);
                            Map<String, Integer> failure = weights(level.getAsJsonObject("failure"), source);
                            placeholder.probability(level.get("level").getAsInt(), level.get("probability").getAsDouble(),
                                    s -> s.putAll(success), f -> f.putAll(failure));
                        }
                    });
                }
                case "leveled" -> builder.leveled(leveled -> {
                    for (JsonElement levelElement : processor.getAsJsonArray("levels")) {
                        JsonObject level = levelElement.getAsJsonObject();
                        if (!"weighted_target".equals(level.get("type").getAsString())) {
                            throw new IllegalStateException("Palette " + source + " has a leveled " + level.get("type") + " level");
                        }
                        int at = level.get("level").getAsInt();
                        String target = level.get("target").getAsString();
                        JsonElement output = level.get("output");
                        if (output.isJsonArray()) {
                            leveled.listGeneric(at, target, out -> out.addAll(output.getAsJsonArray().deepCopy()));
                        } else {
                            Map<String, Integer> weights = weights(output.getAsJsonObject(), source);
                            leveled.list(at, "weighted_target", target, out -> out.putAll(weights));
                        }
                    }
                });
                case "template_stack_tile" -> {
                    List<String> stack = new ArrayList<>();
                    processor.getAsJsonArray("stack").forEach(tile -> stack.add(tile.getAsString()));
                    builder.templateStackTile(processor.get("target").getAsString(), stack.toArray(new String[0]));
                }
                default -> throw new IllegalStateException("Palette " + source + " has a " + type + " processor, which stage palettes do not support");
            }
        }
    }

    private static Map<String, Integer> weights(JsonObject output, String source) {
        Map<String, Integer> weights = new LinkedHashMap<>();
        output.entrySet().forEach(entry -> weights.put(entry.getKey(), wholeWeight(entry.getValue(), source)));
        return weights;
    }

    private static int wholeWeight(JsonElement weight, String source) {
        double value = weight.getAsDouble();
        if (value != Math.rint(value)) {
            throw new IllegalStateException("Palette " + source + " has the fractional weight " + value + ", which VH reads as a whole number");
        }
        return (int) value;
    }

    /** One weighted room behind a treasure or vendor door. */
    private static JsonObject door(int weight, String pool, String target, String... palettes) {
        JsonObject door = new JsonObject();
        door.addProperty("Pool", pool);
        door.addProperty("Target", target);
        JsonArray list = new JsonArray();
        for (String palette : palettes) {
            list.add(palette);
        }
        door.add("Palettes", list);
        door.addProperty("weight", weight);
        return door;
    }

    /** A tile entry that carries its own fields, such as a challenge room controller. */
    private static JsonElement json(String text) {
        return JsonParser.parseString(text);
    }
}
