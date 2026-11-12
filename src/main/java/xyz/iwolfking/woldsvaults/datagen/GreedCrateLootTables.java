package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.vhapi.api.datagen.gen.AbstractLootTableProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * The seven greed crate loot tables ({@code woldsvaults:greed_crate_loot_1} to {@code _7}) a completion crate rolls
 * by its medallion's tier. Each entry is one roll group; each row is one item, with one cell per tier written
 * {@code weight/count} or {@code weight/min-max}, and {@code -} where the item is not in that tier's pool.
 */
public final class GreedCrateLootTables {
    public static final int TIERS = 7;

    private static final List<Entry> ENTRIES = List.of(
        new Entry("focus", tiers("3", "3", "3", "3", "3", "3", "3"), List.of(
                row("the_vault:vorpal_focus", "2/2", "2/2", "2/2", "2/2", "2/2", "2/2", "2/2"),
                row("the_vault:waning_focus", "2/2", "2/2", "2/2", "2/2", "2/2", "2/2", "2/2"),
                row("the_vault:waxing_focus", "2/2", "2/2", "2/2", "2/2", "2/2", "2/2", "2/2"),
                row("woldsvaults:eccentric_focus", "1/2", "1/2", "1/2", "1/2", "1/2", "1/2", "1/2"),
                row("the_vault:cryonic_focus", "1/1", "1/1", "1/1", "1/1", "1/1", "1/1", "1/1"),
                row("the_vault:pyretic_focus", "1/1", "1/1", "1/1", "1/1", "1/1", "1/1", "1/1"),
                row("the_vault:opportunistic_focus", "1/1", "1/1", "1/1", "1/1", "1/1", "1/1", "1/1"),
                row("the_vault:empowered_chaotic_focus", "1/1", "1/1", "1/1", "1/1", "1/1", "1/1", "1/1"))),
        new Entry("currency", tiers("2", "2", "2", "2", "2", "2", "2"), List.of(
                row("the_vault:vault_gold", "15/3-6", "15/3-8", "15/5-8", "15/5-8", "15/6-12", "15/6-12", "15/6-12"),
                row("the_vault:vault_platinum", "5/2-3", "5/2-4", "6/3-4", "8/4-5", "8/4-6", "8/4-6", "10/5-8"),
                row("woldsvaults:greedy_ticket", "4/1-2", "4/1-2", "6/2-3", "6/2-3", "10/3-4", "10/3-4", "12/5"))),
        new Entry("maps", tiers("1", "1", "1", "1", "1", "1", "1"), List.of(
                row("minecraft:air", "15/1", "20/1", "20/1", "20/1", "25/1", "20/1", "20/1"),
                row("the_vault:map", nbt("the_vault:gear_roll_type", "Epic+", "the_vault:map_tier", 2), "-", "-", "-", "-", "-", "-", "6/1"),
                row("the_vault:map", nbt("the_vault:gear_roll_type", "Epic+", "the_vault:map_tier", 3), "-", "-", "-", "-", "-", "-", "6/1"),
                row("the_vault:map", nbt("the_vault:gear_roll_type", "Epic+", "the_vault:map_tier", 4), "-", "-", "-", "-", "-", "-", "6/1"),
                row("the_vault:map", nbt("the_vault:gear_roll_type", "Epic+", "the_vault:map_tier", 5), "-", "-", "-", "-", "-", "-", "6/1"),
                row("the_vault:map", nbt("the_vault:gear_roll_type_pool", "gear_completion", "the_vault:map_tier", 0), "5/1", "5/1", "-", "-", "-", "-", "-"),
                row("the_vault:map", nbt("the_vault:gear_roll_type_pool", "gear_completion", "the_vault:map_tier", 1), "5/1", "5/1", "5/1", "6/1", "6/1", "-", "-"),
                row("the_vault:map", nbt("the_vault:gear_roll_type_pool", "gear_completion", "the_vault:map_tier", 2), "5/1", "5/1", "5/1", "6/1", "6/1", "6/1", "-"),
                row("the_vault:map", nbt("the_vault:gear_roll_type_pool", "gear_completion", "the_vault:map_tier", 3), "-", "5/1", "5/1", "6/1", "6/1", "6/1", "-"),
                row("the_vault:map", nbt("the_vault:gear_roll_type_pool", "gear_completion", "the_vault:map_tier", 4), "-", "-", "5/1", "6/1", "6/1", "6/1", "-"),
                row("the_vault:map", nbt("the_vault:gear_roll_type_pool", "gear_completion", "the_vault:map_tier", 5), "-", "-", "-", "-", "6/1", "6/1", "-"))),
        new Entry("common", tiers("2", "2", "2", "2", "2", "3", "3"), List.of(
                row("the_vault:soul_vortex", "16/1", "16/1", "16/1", "16/1", "12/2", "12/2", "12/2"),
                row("the_vault:spicy_hearty_burger", "16/1-3", "16/1-3", "16/1-3", "16/3-6", "16/3-6", "16/3-6", "16/5-10"),
                row("the_vault:gem_pog", "16/1-2", "16/2-3", "16/2-3", "16/4-5", "16/6", "16/6", "16/8"),
                row("the_vault:bitter_lemon", "12/2", "12/3", "12/4", "10/6", "10/6", "10/6", "8/6"),
                row("the_vault:sour_orange", "-", "12/2", "12/2", "14/3", "14/4", "14/4", "16/4"),
                row("woldsvaults:soul_ichor", "12/1", "12/2", "12/2", "12/3", "14/3", "14/3", "16/4"),
                row("the_vault:unidentified_treasure_key", "12/1", "12/1", "12/1", "12/2", "12/4", "12/4", "12/4"),
                row("the_vault:recharge_core", "12/1", "12/1", "12/1", "12/1", "10/1", "10/1", "10/1"),
                row("woldsvaults:altar_recatalyzer", "12/2", "12/2", "12/4", "12/6", "12/6", "12/6", "12/6"),
                row("the_vault:card_juice", "10/10", "10/16", "10/24", "12/30", "14/32", "14/32", "14/48"),
                row("the_vault:jewel_pouch", "8/6", "8/6", "8/12", "10/14", "10/16", "10/16", "12/16"),
                row("woldsvaults:companion_reroller", "-", "-", "-", "-", "-", "8/1", "8/1"),
                row("woldsvaults:augment_box", "8/1-2", "8/1-2", "8/2-3", "8/2-3", "8/2-4", "8/2-4", "8/2-4"),
                row("woldsvaults:hasty_pomegranate", "8/1", "8/1", "8/1", "8/1", "8/3", "8/3", "8/3"),
                row("woldsvaults:inscription_box", "6/1-2", "6/1-2", "6/2-3", "6/2-3", "6/2-4", "6/2-4", "6/2-4"),
                row("woldsvaults:catalyst_box", "6/1-2", "6/1-2", "6/2-3", "6/2-3", "6/2-4", "6/2-4", "6/2-4"),
                row("woldsvaults:repair_augmenter", "4/1", "4/1", "5/1", "5/1", "5/1", "5/1", "5/1"),
                row("the_vault:deck_socket", nbt("Modifier", "@completion_crate"), "4/1", "4/1", "5/1", "6/1", "6/1", "6/1", "7/1"))),
        new Entry("mythic", tiers("1", "1", "1", "1", "1", "1", "1"), List.of(
                row("minecraft:air", "150/1", "150/1", "150/1", "140/1", "140/1", "140/1", "130/1"),
                row("woldsvaults:omega_box", "-", "-", "-", "5/1-4", "5/2-4", "5/2-4", "8/2-4"),
                row("woldsvaults:concealed_chaos", "2/1", "2/1", "2/1", "2/1", "3/1", "3/1", "3/1"),
                row("the_vault:companion_egg", "1/1", "2/1", "2/1", "2/1", "2/1", "2/1", "2/1"),
                row("woldsvaults:uber_chaos_catalyst", "1/1", "1/1", "1/1", "2/1", "2/1", "2/1", "2/1"),
                row("the_vault:deck_socket", nbt("Modifier", "@completion_crate", "ModifierRoll", "greater"), "-", "-", "-", "1/1", "1/1", "1/1", "2/1"),
                row("woldsvaults:capstone_all_seeing_eye", "1/1", "1/1", "1/1", "1/1", "1/1", "1/1", "1/1"),
                row("the_vault:mystic_pear", "-", "-", "1/1", "1/1", "1/1", "1/1", "1/1"))),
        new Entry("mystery_deck", tiers("1", "1", "1", "1", "1", "1", "2"), List.of(
                row("minecraft:air", "10/1", "7/1", "7/1", "6/1", "6/1", "5/1", "5/1"),
                row("the_vault:card_deck", nbt("id", "mystery"), "0/1", "3/1", "3/1", "4/1", "4/1", "5/1", "5/1"))));

    private GreedCrateLootTables() {
    }

    /** Adds every tier's table through the loot table provider's {@code add}. */
    public static void register(BiConsumer<ResourceLocation, Consumer<AbstractLootTableProvider.LootBuilder>> add) {
        for (int tier = 1; tier <= TIERS; tier++) {
            int column = tier - 1;
            add.accept(WoldsVaults.id("greed_crate_loot_" + tier), loot -> ENTRIES.forEach(entry -> entry.addTo(loot, column)));
        }
    }

    private record Row(String id, Map<String, Object> nbt, String[] cells) {
        void addTo(AbstractLootTableProvider.PoolBuilder pool, int column) {
            String cell = this.cells[column];
            if (cell.equals("-")) {
                return;
            }
            String[] parts = cell.split("/");
            int weight = Integer.parseInt(parts[0]);
            int[] count = range(parts[1]);
            if (this.nbt == null) {
                pool.item(weight, this.id, count[0], count[1]);
            } else {
                pool.itemNbt(weight, this.id, count[0], count[1], tag -> tag.putAll(this.nbt));
            }
        }
    }

    private record Entry(String name, String[] rolls, List<Row> rows) {
        void addTo(AbstractLootTableProvider.LootBuilder loot, int column) {
            int[] roll = range(this.rolls[column]);
            loot.entry(entry -> entry.rolls(roll[0], roll[1]).pool(1, pool -> this.rows.forEach(row -> row.addTo(pool, column))));
        }
    }

    private static Row row(String id, String... cells) {
        return new Row(id, null, check(id, cells));
    }

    private static Row row(String id, Map<String, Object> nbt, String... cells) {
        return new Row(id, nbt, check(id, cells));
    }

    private static String[] tiers(String... rolls) {
        return check("rolls", rolls);
    }

    private static Map<String, Object> nbt(Object... pairs) {
        Map<String, Object> nbt = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            nbt.put((String) pairs[i], pairs[i + 1]);
        }
        return nbt;
    }

    private static String[] check(String what, String[] cells) {
        if (cells.length != TIERS) {
            throw new IllegalStateException(what + " lists " + cells.length + " tiers, not " + TIERS);
        }
        return cells;
    }

    private static int[] range(String text) {
        String[] bounds = text.split("-");
        int min = Integer.parseInt(bounds[0]);
        return new int[]{min, bounds.length > 1 ? Integer.parseInt(bounds[1]) : min};
    }
}
