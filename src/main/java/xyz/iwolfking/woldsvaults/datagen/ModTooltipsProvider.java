package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractTooltipProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModItems;

public class ModTooltipsProvider extends AbstractTooltipProvider {
    protected ModTooltipsProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        add("test", builder -> {
            builder.addTooltipEntry(ModItems.CHROMA_CORE.getRegistryName(), "test")
                    .addTooltipEntry(ModItems.CHROMATIC_GOLD_INGOT.getRegistryName(), "test")
                    .build();
        });
    }
}
