package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractTalentProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;

/**
 * The talent-content leg of the node datagen pipeline. Registers no talents; anything added through
 * {@code add(name, builder -> ...)} overlays the base mod's {@code talents.json} at load.
 */
public class ModTalentsProvider extends AbstractTalentProvider {
    protected ModTalentsProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
    }
}
