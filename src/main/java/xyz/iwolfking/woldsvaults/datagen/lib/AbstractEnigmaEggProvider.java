package xyz.iwolfking.woldsvaults.datagen.lib;

import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractVaultConfigDataProvider;
import xyz.iwolfking.vhapi.api.datagen.lib.loot.LootableConfigBuilder;
import xyz.iwolfking.woldsvaults.config.lib.GenericLootableConfig;
import java.util.function.Supplier;

public class AbstractEnigmaEggProvider extends AbstractVaultConfigDataProvider<AbstractEnigmaEggProvider.Builder> {

    protected AbstractEnigmaEggProvider(DataGenerator generator, String modid) {
        super(generator, modid, "loot_box/enigma_egg", Builder::new);
    }

    @Override
    public void registerConfigs() {

    }

    @Override
    public String getName() {
        return modid + " Enigma Egg Data Provider";
    }

    public static class Builder extends LootableConfigBuilder<GenericLootableConfig> {

        public Builder() {
            super(() -> new GenericLootableConfig("enigma_egg"));
        }


        @Override
        protected void configureConfig(GenericLootableConfig genericLootableConfig) {
            config.POOL.addAll(entries);
        }
    }

}
