package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.config.LegacyLootTablesConfig;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractLegacyLootTableProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.*;

public class ModLegacyLootTablesProvider extends AbstractLegacyLootTableProvider {
    protected ModLegacyLootTablesProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        add("wold_objectives", builder -> {
            createStandardLevelsForObjective("survival", "the_vault:base_crate_0").forEach(builder::addLevel);
        });
    }

    private static final int[] standardLevels = new int[]{0, 5, 20, 50, 100};

    public List<LegacyLootTablesConfig.Level> createStandardLevelsForObjective(String objectiveName, String baseCrate) {
        List<LegacyLootTablesConfig.Level> levels = new ArrayList<>();
        Arrays.stream(standardLevels).forEach(value -> {
            LegacyLootTablesConfig.Level level = new LegacyLootTablesConfig.Level(value);
            level.COMPLETION_CRATE = new LinkedHashMap<>();
            if(value != 0 && value != 5) {
                String crateName = baseCrate.replace("0", String.valueOf(value));
                level.COMPLETION_CRATE.put(objectiveName, crateName);
            }
            else {
                level.COMPLETION_CRATE.put(objectiveName, baseCrate);
            }
            levels.add(level);
        });

        return levels;

    }
}
