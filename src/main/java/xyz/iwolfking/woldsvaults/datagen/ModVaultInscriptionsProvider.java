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
        addPool("the_vault:warehouse").level(0).entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/warehouse").color(7012096).count(1).model(109, 109).weight(1).size(10, 10));
        addPool("the_vault:treasure").level(0)
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/warehouse").color(7012096).count(1).model(109, 109).weight(1).size(10, 10))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/trove").color(7012096).count(1).model(11, 11).weight(5).size(10, 10))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/mine").color(7012096).count(1).model(9, 9).weight(5).size(10, 10))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/digsite2").color(7012096).count(1).model(6, 6).weight(2).size(10, 10))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/iskallian_garden").color(7012096).count(1).model(98, 98).weight(3).size(5, 5))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/comet_observatory").color(7012096).count(1).model(106, 106).weight(5).size(5, 5));

        mapModel("the_vault:vault/rooms/omega/digsite2", 6);
        mapModel("the_vault:vault/rooms/omega/cube", 105);
        mapModel("the_vault:vault/rooms/omega/comet_observatory", 106);
        mapModel("the_vault:vault/rooms/omega/wolds_dinner", 107);
        mapModel("the_vault:vault/rooms/omega/wardens_garden", 108);
        mapModel("the_vault:vault/rooms/omega/warehouse", 109);
    }
}
