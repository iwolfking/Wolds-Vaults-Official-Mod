package xyz.iwolfking.woldsvaults.datagen.patches;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractConfigPatchProvider.Node;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractConfigPatchProvider.Patch;

import static xyz.iwolfking.woldsvaults.datagen.lib.AbstractConfigPatchProvider.id;
import static xyz.iwolfking.woldsvaults.datagen.lib.AbstractConfigPatchProvider.where;

/**
 * The greed rework's greed trader: every tier's offer table (tiers 13 to 16 are new), the pools those offers draw
 * from, the companion relic and catalyst gates the rework opens up, and the challenge unlock tiers.
 */
public final class GreedTraderPatches {
    private GreedTraderPatches() {
    }

    public static void register(Patch trader) {
        Node tierPools = trader.at("tierPools");
        tierPools.set("1", rows(
                offer("random_etching", 200, 10, 20),
                pool(150, "super_inscriptions", 20, 40),
                pool(150, "catalyst_t1", 10, 20),
                pool(60, "greed_catalysts_t1", 20, 35),
                pool(42, "random_crystals_weak", 15, 20)));
        tierPools.set("2", rows(
                offer("random_etching", 200, 10, 20),
                pool(150, "super_inscriptions", 20, 40),
                pool(150, "catalyst_t1", 10, 20),
                pool(68, "greed_catalysts_t1", 20, 35),
                pool(68, "greed_cards_weak", 20, 30),
                pool(48, "random_crystals_weak", 15, 20)));
        tierPools.set("3", rows(
                offer("random_etching", 200, 10, 20),
                pool(150, "super_inscriptions", 20, 40),
                pool(150, "catalyst_t1", 10, 20),
                pool(75, "greed_catalysts_t1", 20, 35),
                pool(75, "greed_cards_weak", 20, 30),
                pool(53, "random_crystals_weak", 15, 20),
                pool(50, "greed_maps", 35, 50)));
        tierPools.set("4", rows(
                offer("random_etching", 200, 15, 25),
                pool(150, "super_inscriptions", 20, 40),
                pool(150, "catalyst_t1", 10, 20),
                pool(87, "greed_catalysts_t1", 20, 35),
                pool(87, "greed_cards_weak", 20, 30),
                pool(61, "random_crystals_weak", 15, 20),
                pool(52, "greed_companion_relics_t1", 50, 90),
                pool(50, "greed_maps", 35, 50)));
        tierPools.set("5", rows(
                offer("random_etching", 200, 15, 25),
                pool(150, "super_inscriptions", 25, 45),
                pool(150, "catalyst_t1", 10, 20),
                pool(94, "greed_catalysts_t1", 20, 35),
                pool(94, "greed_cards_weak", 20, 30),
                pool(66, "random_crystals_weak", 15, 20),
                pool(56, "greed_companion_relics_t1", 50, 90),
                pool(50, "greed_maps", 35, 50),
                pool(50, "greater_cores_basic", 50, 75)));
        tierPools.set("6", rows(
                offer("random_etching", 200, 15, 25),
                pool(150, "super_inscriptions", 25, 45),
                pool(150, "catalyst_t1", 10, 20),
                pool(99, "greed_catalysts_t1", 20, 35),
                pool(99, "greed_cards_medium", 25, 45),
                pool(69, "random_crystals_weak", 15, 20),
                pool(59, "greed_companion_relics_t1", 50, 90),
                pool(50, "greed_maps", 35, 50),
                pool(50, "greater_cores_basic", 50, 75),
                pool(30, "catalyst_t2", 30, 50)));
        tierPools.set("7", rows(
                offer("random_etching", 200, 20, 30),
                pool(150, "super_inscriptions", 25, 45),
                pool(91, "catalyst_t1", 10, 20),
                pool(68, "greed_catalysts_t1", 20, 35),
                pool(60, "greed_cards_medium", 25, 45),
                pool(59, "random_crystals_medium", 15, 20),
                pool(51, "greed_companion_relics_t1", 50, 90),
                pool(50, "greed_maps", 35, 50),
                pool(50, "greater_cores_basic", 50, 75),
                pool(36, "catalyst_t2", 30, 50),
                pool(3, "ancient_uniques", 150, 200)));
        tierPools.set("8", rows(
                offer("random_etching", 200, 20, 30),
                pool(150, "super_inscriptions", 25, 45),
                pool(96, "catalyst_t1", 10, 20),
                pool(72, "greed_catalysts_t1", 20, 35),
                pool(63, "random_crystals_medium", 15, 20),
                pool(60, "greed_cards_medium", 25, 45),
                pool(54, "greed_companion_relics_t1", 50, 90),
                pool(50, "greed_maps", 35, 50),
                pool(50, "greater_cores_basic", 50, 75),
                pool(38, "catalyst_t2", 30, 50),
                pool(30, "greater_cores_advanced", 80, 100),
                pool(3, "ancient_uniques", 150, 200)));
        tierPools.set("9", rows(
                offer("random_etching", 200, 20, 30),
                pool(150, "super_inscriptions", 25, 45),
                pool(96, "catalyst_t1", 10, 20),
                pool(72, "greed_catalysts_t1", 20, 35),
                pool(63, "random_crystals_medium", 15, 20),
                pool(60, "greed_cards_strong", 30, 50),
                pool(54, "greed_companion_relics_t2", 90, 160),
                pool(50, "greed_maps", 35, 50),
                pool(50, "greater_cores_basic", 50, 75),
                pool(38, "catalyst_t2", 30, 50),
                pool(30, "greater_cores_advanced", 80, 100),
                pool(3, "ancient_uniques", 150, 200)));
        tierPools.set("10", rows(
                offer("random_etching", 200, 25, 35),
                pool(150, "super_inscriptions", 25, 45),
                pool(95, "catalyst_t1", 10, 20),
                pool(81, "greed_catalysts_t1", 20, 35),
                offer("powerful_etching", 75, 40, 50),
                pool(71, "random_crystals_strong", 15, 20),
                pool(61, "greed_companion_relics_t2", 90, 160),
                pool(60, "greed_cards_strong", 30, 50),
                pool(50, "greed_maps", 35, 50),
                pool(50, "greater_cores_basic", 50, 75),
                pool(38, "catalyst_t2", 30, 50),
                pool(30, "greater_cores_advanced", 80, 100),
                pool(19, "catalyst_t3", 40, 50),
                pool(3, "ancient_uniques", 150, 200)));
        tierPools.set("11", rows(
                offer("random_etching", 200, 25, 35),
                pool(150, "super_inscriptions", 25, 45),
                pool(95, "catalyst_t1", 10, 20),
                pool(81, "greed_catalysts_t2", 40, 50),
                offer("powerful_etching", 75, 40, 50),
                pool(71, "random_crystals_strong", 15, 20),
                pool(61, "greed_companion_relics_t2", 90, 160),
                pool(60, "greed_cards_strong", 30, 50),
                pool(50, "greed_maps", 35, 50),
                pool(50, "greater_cores_basic", 50, 75),
                pool(38, "catalyst_t2", 30, 50),
                pool(30, "greater_cores_advanced", 80, 100),
                pool(19, "catalyst_t3", 40, 50),
                pool(3, "ancient_uniques", 150, 200)));
        tierPools.set("12", rows(
                offer("random_etching", 200, 25, 35),
                pool(150, "super_inscriptions", 25, 45),
                pool(95, "catalyst_t1", 10, 20),
                pool(81, "greed_catalysts_t2", 40, 50),
                offer("powerful_etching", 75, 40, 50),
                pool(71, "random_crystals_strong", 15, 20),
                pool(61, "greed_companion_relics_t3", 120, 190),
                pool(60, "greed_cards_strong", 30, 50),
                pool(50, "greed_maps", 35, 50),
                pool(50, "greater_cores_basic", 50, 75),
                pool(38, "catalyst_t2", 30, 50),
                pool(30, "greater_cores_advanced", 80, 100),
                pool(19, "catalyst_t3", 40, 50),
                pool(3, "ancient_uniques", 150, 200)));
        tierPools.add("13", rows(
                offer("random_etching", 200, 25, 35),
                pool(150, "super_inscriptions", 25, 45),
                pool(95, "catalyst_t1", 10, 20),
                pool(81, "greed_catalysts_t2", 40, 50),
                offer("powerful_etching", 75, 40, 50),
                pool(71, "random_crystals_strong", 15, 20),
                pool(61, "greed_companion_relics_t3", 120, 190),
                pool(60, "greed_cards_elite", 40, 60),
                pool(50, "greed_maps", 35, 50),
                pool(50, "greater_cores_basic", 50, 75),
                pool(38, "catalyst_t2", 30, 50),
                pool(30, "greater_cores_advanced", 80, 100),
                pool(19, "catalyst_t3", 40, 50),
                pool(6, "ancient_uniques", 150, 200)));
        tierPools.add("14", lateTier(64, 7));
        tierPools.add("15", lateTier(65, 8));
        tierPools.add("16", lateTier(65, 10));

        Node pools = trader.at("pools");
        for (String retired : new String[]{"random_crystals", "greater_cores", "greed_booster_packs", "general_items",
                "companion_super_relics", "modified_super_inscriptions", "greed_companion_relics"}) {
            pools.remove(retired);
        }
        pools.at("catalyst_t2", 0).set("minGreedTier", 0);
        pools.at("catalyst_t2", 0, "catalyst", "size").set("count", 10);
        pools.at("catalyst_t3", 0).set("minGreedTier", 0);
        pools.at("catalyst_t3", 0, "catalyst", "size").set("count", 10);
        pools.at("greed_catalysts_t1", 0).set("maxGreedTier", -1);
        pools.at("greed_catalysts_t2", 0).set("minGreedTier", 0);
        pools.at("greed_catalysts_t2", 0, "catalyst").set("isGreedy", true);
        for (int i = 0; i < 6; i++) {
            pools.at("greed_companion_relics_t1", i).set("minGreedTier", 0).set("maxGreedTier", -1);
            pools.at("greed_companion_relics_t2", i).set("minGreedTier", 0).set("maxGreedTier", -1);
            pools.at("greed_companion_relics_t3", i).set("minGreedTier", 0);
        }
        pools.add("random_crystals_weak", entries(crystal(4, 8, 1, 1)));
        pools.add("random_crystals_medium", entries(crystal(6, 12, 2, 2)));
        pools.add("random_crystals_strong", entries(crystal(8, 20, 3, 2)));
        for (String size : new String[]{"weak", "medium", "strong", "elite"}) {
            pools.add("greed_cards_" + size, entries(
                    item(10, 0, "the_vault:booster_pack", "{id:\"the_vault:" + size + "_greed_pack\"}"),
                    item(6, 0, "the_vault:booster_pack", "{id:\"the_vault:mega_" + size + "_greed_pack\"}")));
        }
        pools.add("greater_cores_basic", entries(item(10, 0, "the_vault:deck_socket", "{Modifier:\"@greed_basic\",ModifierRoll:\"greater\"}")));
        pools.add("greater_cores_advanced", entries(item(10, 0, "the_vault:deck_socket", "{Modifier:\"@greed\",ModifierRoll:\"greater\"}")));
        JsonObject ancientUnique = new JsonObject();
        ancientUnique.addProperty("type", "ancient_unique");
        ancientUnique.addProperty("weight", 10);
        ancientUnique.addProperty("minGreedTier", 0);
        ancientUnique.addProperty("maxGreedTier", -1);
        pools.add("ancient_uniques", entries(ancientUnique));
        pools.set("greed_maps", entries(
                map(40, 0, "gear_completion", 0), map(40, 0, "gear_completion", 1), map(40, 0, "gear_completion", 2),
                map(32, 5, "gear_completion", 1), map(32, 5, "gear_completion", 2), map(32, 5, "gear_completion", 3),
                map(32, 7, "gear_completion", 2), map(32, 7, "gear_completion", 3), map(32, 7, "gear_completion", 4),
                map(32, 10, "gear_completion", 3), map(32, 10, "gear_completion", 4), map(32, 10, "gear_completion", 5),
                map(15, 13, "gear_epic", 2), map(15, 13, "gear_epic", 3), map(15, 13, "gear_epic", 4), map(15, 13, "gear_epic", 5)));

        trader.at("quests", id("the_vault:mega_multikill"), "task", "counter").set("window", 5);

        challengeTier(trader, "gone_in_60_seconds", 4);
        challengeTier(trader, "the_pacifist", 5);
        challengeTier(trader, "luckiest_win", 5);
        challengeTier(trader, "the_speedrunner", 5);
        challengeTier(trader, "time_you_say", 8);
        challengeTier(trader, "speed_blackout_bingo", 8);
        challengeTier(trader, "one_shot_frenzy", 9);
    }

    private static JsonArray lateTier(int companionRelicWeight, int ancientUniqueWeight) {
        return rows(
                offer("random_etching", 200, 25, 35),
                pool(150, "super_inscriptions", 25, 45),
                pool(101, "catalyst_t1", 10, 20),
                pool(86, "greed_catalysts_t2", 40, 50),
                offer("powerful_etching", 75, 40, 50),
                pool(75, "greed_cards_elite", 40, 60),
                pool(75, "random_crystals_strong", 15, 20),
                pool(companionRelicWeight, "greed_companion_relics_t3", 120, 190),
                pool(60, "greater_cores_basic", 50, 75),
                pool(50, "greed_maps", 35, 50),
                pool(40, "catalyst_t2", 30, 50),
                pool(40, "greater_cores_advanced", 80, 100),
                pool(20, "catalyst_t3", 40, 50),
                pool(ancientUniqueWeight, "ancient_uniques", 150, 200));
    }

    private static void challengeTier(Patch trader, String challengeCrystalId, int minTier) {
        trader.at("challenges", where("challengeCrystalId", challengeCrystalId)).set("minTier", minTier);
    }

    private static JsonArray rows(JsonObject... rows) {
        return entries(rows);
    }

    private static JsonArray entries(JsonObject... entries) {
        JsonArray array = new JsonArray();
        for (JsonObject entry : entries) {
            array.add(entry);
        }
        return array;
    }

    private static JsonObject offer(String type, int weight, int minCoinCost, int maxCoinCost) {
        JsonObject row = new JsonObject();
        row.addProperty("type", type);
        row.addProperty("weight", weight);
        row.addProperty("minCoinCost", minCoinCost);
        row.addProperty("maxCoinCost", maxCoinCost);
        return row;
    }

    private static JsonObject pool(int weight, String poolId, int minCoinCost, int maxCoinCost) {
        JsonObject row = new JsonObject();
        row.addProperty("type", "pool");
        row.addProperty("weight", weight);
        row.addProperty("poolId", poolId);
        row.addProperty("minCoinCost", minCoinCost);
        row.addProperty("maxCoinCost", maxCoinCost);
        return row;
    }

    private static JsonObject item(int weight, int minGreedTier, String item, String nbt) {
        JsonObject entry = new JsonObject();
        entry.addProperty("weight", weight);
        entry.addProperty("minGreedTier", minGreedTier);
        entry.addProperty("maxGreedTier", -1);
        JsonObject stack = new JsonObject();
        stack.addProperty("item", item);
        stack.addProperty("nbt", nbt);
        entry.add("itemStack", stack);
        return entry;
    }

    private static JsonObject map(int weight, int minGreedTier, String rollPool, int mapTier) {
        return item(weight, minGreedTier, "the_vault:map",
                "{\"the_vault:gear_roll_type_pool\": \"" + rollPool + "\",\"the_vault:map_tier\": " + mapTier + "}");
    }

    private static JsonObject crystal(int minModifiers, int maxModifiers, int maxTreasure, int maxTemporal) {
        JsonObject entry = new JsonObject();
        entry.addProperty("type", "random_crystal");
        entry.addProperty("weight", 10);
        entry.addProperty("minGreedTier", 0);
        entry.addProperty("maxGreedTier", -1);
        JsonObject crystal = new JsonObject();
        crystal.addProperty("modifierPool", "normal_modifiers");
        crystal.addProperty("minModifiers", minModifiers);
        crystal.addProperty("maxModifiers", maxModifiers);
        crystal.addProperty("modifierPoolTwo", "treasure_modifiers");
        crystal.addProperty("minModifiersTwo", 0);
        crystal.addProperty("maxModifiersTwo", maxTreasure);
        crystal.addProperty("modifierPoolThree", "temporal_modifiers");
        crystal.addProperty("minModifiersThree", 0);
        crystal.addProperty("maxModifiersThree", maxTemporal);
        entry.add("crystal", crystal);
        return entry;
    }
}
