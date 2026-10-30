package xyz.iwolfking.woldsvaults.datagen.lib;

import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractVaultConfigDataProvider;
import xyz.iwolfking.vhapi.api.datagen.lib.loot.LootableConfigBuilder;
import xyz.iwolfking.woldsvaults.config.lib.GenericLootableConfig;

public class AbstractAugmentBoxProvider extends AbstractVaultConfigDataProvider<AbstractAugmentBoxProvider.Builder> {

    protected AbstractAugmentBoxProvider(DataGenerator generator, String modid) {
        super(generator, modid, "loot_box/augment_box", Builder::new);
    }

    @Override
    public void registerConfigs() {

    }

    @Override
    public String getName() {
        return modid + " Augment Box Data Provider";
    }

    public static class Builder extends LootableConfigBuilder<GenericLootableConfig> {

        public Builder() {
            super(() -> new GenericLootableConfig("augment_box"));
        }


        @Override
        protected void configureConfig(GenericLootableConfig genericLootableConfig) {
            config.POOL.addAll(entries);
        }
    }

}
