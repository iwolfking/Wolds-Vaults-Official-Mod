package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.config.customisation.VaultLevelDiscovery;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractCustomisationDiscoveryProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class ModCustomisationDiscoveryProvider extends AbstractCustomisationDiscoveryProvider {
    protected ModCustomisationDiscoveryProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        add("aura_unlocks", builder -> {
           builder.putTrail("dweller", new VaultLevelDiscovery(10));
           builder.putTrail("cheeser", new VaultLevelDiscovery(20));
           builder.putTrail("goblin", new VaultLevelDiscovery(30));
           builder.putTrail("champion", new VaultLevelDiscovery(40));
           builder.putTrail("legend", new VaultLevelDiscovery(50));
        });
    }
}
