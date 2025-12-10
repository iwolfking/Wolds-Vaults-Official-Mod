package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import iskallia.vault.config.LegacyLootTablesConfig;
import iskallia.vault.init.ModConfigs;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.system.CallbackI;
import xyz.iwolfking.vhapi.api.datagen.AbstractLootInfoProvider;
import xyz.iwolfking.vhapi.api.datagen.AbstractTooltipProvider;
import xyz.iwolfking.vhapi.api.util.ResourceLocUtils;
import xyz.iwolfking.vhapi.mixin.accessors.LegacyLootTablesConfigAccessor;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModItems;

public class ModLootInfoProvider extends AbstractLootInfoProvider {
    protected ModLootInfoProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        ModConfigs.LOOT_TABLES = new LegacyLootTablesConfig().readConfig();

        add("god_altars", builder -> {
            godAltar("Idona", builder);
            godAltar("Tenos", builder);
            godAltar("Velara", builder);
            godAltar("Wendarr", builder);
        });

        add("wolds_tables", builder -> {
            genericSingleton("Tombstone", VaultMod.id("tombstone_loot"), 50, builder);
            genericSingleton("Iskallian Leaves", VaultMod.id("iskallian_leaves"), 40, builder);
            genericSingleton("Hellish Sand", VaultMod.id("hellish_sand"), 30, builder);

            builder.lootInfo(VaultMod.id("dungeon_pedestal"), "Dungeon Pedestal", map -> {
                map.put(VaultMod.id("dungeon_pedestal_lvl50"), 50);
                map.put(VaultMod.id("dungeon_pedestal_lvl75"), 75);
            });

            mapChest("Enigma", builder);
            mapChest("Wooden", builder);
            mapChest("Living", builder);
            mapChest("Ornate", builder);
            mapChest("Gilded", builder);
            mapChest("Treasure", builder);

            completionCrate("brutal_bosses", builder);
            completionCrate("unhinged_scavenger", builder);
            completionCrate("haunted_braziers", builder);
            completionCrate("enchanted_elixir", builder);
            completionCrate("zealot", builder);
            completionCrate("ballistic_bingo", builder);
            completionCrate("ballistic_bingo_full", builder);
            completionCrate("corrupted", builder);
            completionCrate("alchemy", builder);

        });

        add("vanilla_completion_overrides", builder -> {
           completionCrate("monolith",  builder);
           completionCrate("boss",  builder);
           completionCrate("cake",  builder);
           completionCrate("elixir", builder);
           completionCrate("paradox", builder);
           completionCrate("bingo", builder);
           completionCrate("bingo_full", builder);
           completionCrate("rune_boss", builder);
           completionCrate("scavenger", builder);
        });
    }

    public void godAltar(String god, Builder builder) {
        builder.lootInfo(VaultMod.id("god_altar_" + god.toLowerCase()), "God Altar (" + god + ")", map -> {
            map.put(VaultMod.id("god_altar_" + god.toLowerCase()), 40);
        });
    }

    public void mapChest(String chestType, Builder builder) {
        builder.lootInfo(VaultMod.id(chestType.toLowerCase() + "_chest_map"), chestType + " Chests (Map)", map -> {
            map.put(VaultMod.id(chestType.toLowerCase() + "_chest_map"), 100);
        });
    }

    public void genericSingleton(String name, ResourceLocation lootTableId, int level, Builder builder) {
        builder.lootInfo(VaultMod.id(name.toLowerCase().replace(" ", "_")), name, map -> {
            map.put(lootTableId, level);
        });
    }

    public ResourceLocation getLootTableForCrateForLevel(String objectiveId, int level) {
        LegacyLootTablesConfig.Level lootTables = ModConfigs.LOOT_TABLES.getForLevel(level);
        if(lootTables != null) {
            if(lootTables.getCompletionCrate(objectiveId) != null) {
                return lootTables.getCompletionCrate(objectiveId);
            }
        }

        return WoldsVaults.id("empty");

    }

    public void completionCrate(String objectiveId, Builder builder) {
        builder.lootInfo(VaultMod.id("completion_crate_" + objectiveId), ResourceLocUtils.formatReadableName(VaultMod.id("completion_crate_" + objectiveId)), map -> {
            map.put(getLootTableForCrateForLevel(objectiveId, 0), 0);
            map.put(getLootTableForCrateForLevel(objectiveId, 20), 20);
            map.put(getLootTableForCrateForLevel(objectiveId, 50), 50);
            map.put(getLootTableForCrateForLevel(objectiveId, 100), 100);
        });
    }
}
