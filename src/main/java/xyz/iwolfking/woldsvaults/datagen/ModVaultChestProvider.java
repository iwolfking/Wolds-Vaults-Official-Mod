package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractVaultChestProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class ModVaultChestProvider extends AbstractVaultChestProvider {
    protected ModVaultChestProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID, "vault_chest");
    }

    @Override
    public void registerConfigs() {
        add("wolds_traps", builder -> {
            builder.addLevel(0, 0.05F, pool -> {
                pool.add("Shockwave", 2);
                pool.add("Zoomies", 2);
                pool.add("Plating", 4);
            });
        });
    }
}
