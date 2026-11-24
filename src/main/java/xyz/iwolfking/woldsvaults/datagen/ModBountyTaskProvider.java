package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractBountyBoardTasksProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class ModBountyTaskProvider extends AbstractBountyBoardTasksProvider {
    protected ModBountyTaskProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
//        add("wold_bounty_additions", builder -> {
//            builder.addTask("")
//        });
    }
}
