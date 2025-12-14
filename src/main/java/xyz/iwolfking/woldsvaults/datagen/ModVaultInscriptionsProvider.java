package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractInscriptionProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class ModVaultInscriptionsProvider extends AbstractInscriptionProvider {
    public ModVaultInscriptionsProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    protected void build() {
        mapModel("the_vault:vault/rooms/omega/digsite2", 6);
        mapModel("the_vault:vault/rooms/omega/cube", 105);
        mapModel("the_vault:vault/rooms/omega/comet_observatory", 106);
        mapModel("the_vault:vault/rooms/omega/wolds_dinner", 107);
        mapModel("the_vault:vault/rooms/omega/wardens_garden", 108);
    }
}
