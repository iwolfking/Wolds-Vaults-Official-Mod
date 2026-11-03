package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import xyz.iwolfking.woldsvaults.config.ObjectiveDifficultyConfig;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractWoldsTableProvider;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * How a vault's difficulty multiplier D scales objective requirements: per-objective exponents (base x D^k), bingo
 * tile classes, the challenge stack curve, crate tier stacks by sigil difficulty, scavenger goal floors, ballistic
 * bingo grid growth and how many survival wave groups stay in reserve.
 */
public class ModObjectiveDifficultyProvider extends AbstractWoldsTableProvider {

    public ModObjectiveDifficultyProvider(DataGenerator generator) {
        super(generator, "objectives/difficulty");
    }

    @Override
    public void registerConfigs() {
        ObjectiveDifficultyConfig table = new ObjectiveDifficultyConfig();
        table.exponents = new LinkedHashMap<>();
        table.exponents.put("scavenger", 1.0D);
        table.exponents.put("scavenger_bingo", 1.0D);
        table.exponents.put("unhinged_scavenger", 1.0D);
        table.exponents.put("unhinged_scavenger_bingo", 1.0D);
        table.exponents.put("elixir", 1.0D);
        table.exponents.put("enchanted_elixir", 1.0D);
        table.exponents.put("rune_boss", 1.0D);
        table.exponents.put("boss", 0.5D);
        table.exponents.put("cake", 1.0D);
        table.exponents.put("chaos", 0.5D);
        table.exponents.put("corrupted", 1.0D);
        table.exponents.put("zealot", 1.0D);
        table.exponents.put("survival", 0.5D);
        table.exponents.put("alchemy", 0.5D);

        table.bingoTileClasses = new LinkedHashMap<>();
        table.bingoTileClasses.put(ObjectiveDifficultyConfig.LINEAR, 1.0D);
        table.bingoTileClasses.put("soft", 0.7D);
        table.bingoTileClassByName = new LinkedHashMap<>();
        for (String name : List.of("Kill Dwellers", "Rapidly Kill Dwellers", "Kill Tanks", "Kill Hordes", "Defeat an Elite",
                "Free Villager", "Kill Dungeon Boss", "Multi Kill!")) {
            table.bingoTileClassByName.put(name, "soft");
        }

        table.stackLinearFactor = 3.0D;
        table.stackBreakpoint = 4.0D;
        table.stackOffset = 4.0D;
        table.stackPower = 1.5D;

        table.crateTiersBySigilDifficulty = new LinkedHashMap<>();
        table.crateTiersBySigilDifficulty.put("0.5", 2);
        table.crateTiersBySigilDifficulty.put("1.0", 3);
        table.crateTiersBySigilDifficulty.put("1.5", 4);
        table.crateTiersBySigilDifficulty.put("3.0", 5);
        table.crateTiersBySigilDifficulty.put("5.0", 6);
        table.crateTiersBySigilDifficulty.put("6.0", 8);
        table.crateTiersBySigilDifficulty.put("6.5", 9);
        table.crateTiersBySigilDifficulty.put("7.0", 10);
        table.crateTiersBySigilDifficulty.put("7.5", 11);
        table.crateTiersBySigilDifficulty.put("8.0", 12);
        table.crateTiersBySigilDifficulty.put("8.5", 13);
        table.crateTiersBySigilDifficulty.put("9.0", 14);

        table.scavengerGoalFloorLowLevel = new LinkedHashMap<>();
        table.scavengerGoalFloorLowLevel.put("0.5", 5);
        table.scavengerGoalFloorLowLevel.put("3.0", 6);
        table.scavengerGoalFloorHighLevelFrom = 20;
        table.scavengerGoalFloorHighLevel = new LinkedHashMap<>();
        table.scavengerGoalFloorHighLevel.put("0.5", 5);
        table.scavengerGoalFloorHighLevel.put("1.0", 6);
        table.scavengerGoalFloorHighLevel.put("3.0", 7);

        table.ballisticGridBonus = new LinkedHashMap<>();
        table.ballisticGridBonus.put("1.0", 1);
        table.ballisticGridBonus.put("1.5", 2);
        table.ballisticGridBonus.put("5.0", 3);
        table.ballisticGridCap = 9;

        table.survivalReservedWaveGroups = 3;
        this.table("objective_difficulty", table);
    }
}
