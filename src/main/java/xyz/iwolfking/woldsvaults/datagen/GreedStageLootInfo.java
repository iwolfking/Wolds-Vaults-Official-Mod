package xyz.iwolfking.woldsvaults.datagen;

import java.util.List;

/** The greed stage loot tables listed in JEI's loot info, shared by the loot info and language providers. */
public final class GreedStageLootInfo {
    public static final List<Entry> ENTRIES = List.of(
            new Entry("Enigma Chest", "enigma_chest", 1),
            new Entry("Enigma Chest", "enigma_chest", 2),
            new Entry("Wooden Chest", "wooden_chest", 1),
            new Entry("Living Chest", "living_chest", 1),
            new Entry("Ornate Chest", "ornate_chest", 1),
            new Entry("Gilded Chest", "gilded_chest", 1),
            new Entry("Living Strongbox", "living_strongbox", 1),
            new Entry("Ornate Strongbox", "ornate_strongbox", 1),
            new Entry("Gilded Strongbox", "gilded_strongbox", 1),
            new Entry("Wooden Dungeon Barrel", "wooden_dungeon_barrel", 2),
            new Entry("Living Dungeon Barrel", "living_dungeon_barrel", 2),
            new Entry("Ornate Dungeon Barrel", "ornate_dungeon_barrel", 2),
            new Entry("Gilded Dungeon Barrel", "gilded_dungeon_barrel", 2),
            new Entry("Dungeon Pedestal", "dungeon_pedestal", 2),
            new Entry("Dungeon Boss", "dungeon_boss", 2),
            new Entry("Treasure Chest", "treasure_chest", 3),
            new Entry("Treasure Pillar", "treasure_pillar", 3),
            new Entry("Treasure Chest Velara", "treasure_chest_velara", 3),
            new Entry("Treasure Chest Tenos", "treasure_chest_tenos", 3),
            new Entry("Treasure Chest Wendarr", "treasure_chest_wendarr", 3),
            new Entry("Treasure Chest Idona", "treasure_chest_idona", 3));

    private GreedStageLootInfo() {
    }

    public record Entry(String name, String table, int stage) {
        /** The loot info group path, {@code <table>_stage_<n>}. */
        public String group() {
            return this.table + "_stage_" + this.stage;
        }

        /** The title shown for the group, "Name (Greed Stage n)". */
        public String title() {
            return this.name + " (Greed Stage " + this.stage + ")";
        }
    }
}
