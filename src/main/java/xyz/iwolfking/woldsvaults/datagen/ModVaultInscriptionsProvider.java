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
        addPool("woldsvaults:challenge_rooms").level(0)
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/challenge/crystal_caves").color(15769088).count(1).model(5, 5).weight(8).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/challenge/factory").color(15769088).count(1).model(8, 8).weight(8).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/challenge/village").color(15769088).count(1).model(13, 13).weight(12).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/challenge/wildwest").color(15769088).count(1).model(14, 14).weight(8).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/challenge/x-mark").color(15769088).count(1).model(15, 15).weight(8).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/challenge/temple").color(15769088).count(1).model(22, 22).weight(8).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/challenge/memory").color(15769088).count(1).model(23, 23).weight(8).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/challenge/pirate_cave").color(15769088).count(1).model(97, 97).weight(6).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/challenge/raid/rooms").color(15769088).count(1).model(99, 99).weight(4).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/challenge/laboratory").color(15769088).count(1).model(100, 100).weight(4).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/challenge/labyrinth").color(15769088).count(1).model(103, 103).weight(1).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/challenge/graveyard").color(15769088).count(1).model(104, 104).weight(4).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/challenge/aquarium").color(15769088).count(1).model(102, 102).weight(5).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/challenge/dragon").color(15769088).count(1).model(7, 7).weight(4).size(0, 0));
        addPool("woldsvaults:omega_rooms").level(0)
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/blacksmith").color(7012096).count(1).model(1, 1).weight(8).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/mush_room").color(7012096).count(1).model(2, 2).weight(10).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/library").color(7012096).count(1).model(3, 3).weight(12).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/cove").color(7012096).count(1).model(4, 4).weight(4).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/digsite").color(7012096).count(1).model(6, 6).weight(1).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/mine").color(7012096).count(1).model(9, 9).weight(2).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/painting").color(7012096).count(1).model(10, 10).weight(8).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/trove").color(7012096).count(1).model(11, 11).weight(1).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/vendor").color(7012096).count(1).model(12, 12).weight(10).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/pixel").color(7012096).count(1).model(21, 21).weight(10).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/playzone").color(7012096).count(1).model(94, 94).weight(8).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/arcade").color(7012096).count(1).model(95, 95).weight(3).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/hellish_digsite").color(7012096).count(1).model(96, 96).weight(1).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/iskallian_garden").color(7012096).count(1).model(98, 98).weight(2).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/cube").color(7012096).count(1).model(105, 105).weight(8).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/comet_observatory").color(7012096).count(1).model(106, 106).weight(4).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/wolds_dinner").color(7012096).count(1).model(107, 107).weight(4).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/wardens_garden").color(7012096).count(1).model(108, 108).weight(4).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/warehouse").color(15769088).count(1).model(109, 109).weight(1).size(0, 0));
        addPool("woldsvaults:resource_rooms").level(0)
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/challenge/factory").color(15769088).count(1).model(8, 8).weight(2).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/raw/chromatic_caves").color(3118792).count(1).model(16, 16).weight(16).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/raw/farm").color(3118792).count(1).model(17, 17).weight(16).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/raw/emerald_caves").color(3118792).count(1).model(18, 18).weight(16).size(0, 0));
        addPool("woldsvaults:all_rooms").level(0)
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/challenge/crystal_caves").color(15769088).count(1).model(5, 5).weight(10).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/challenge/factory").color(15769088).count(1).model(8, 8).weight(8).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/challenge/village").color(15769088).count(1).model(13, 13).weight(12).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/challenge/wildwest").color(15769088).count(1).model(14, 14).weight(10).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/challenge/x-mark").color(15769088).count(1).model(15, 15).weight(10).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/challenge/temple").color(15769088).count(1).model(22, 22).weight(10).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/challenge/memory").color(15769088).count(1).model(23, 23).weight(10).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/challenge/pirate_cave").color(15769088).count(1).model(97, 97).weight(6).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/challenge/raid/rooms").color(15769088).count(1).model(99, 99).weight(6).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/challenge/laboratory").color(15769088).count(1).model(100, 100).weight(6).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/challenge/labyrinth").color(15769088).count(1).model(103, 103).weight(1).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/challenge/graveyard").color(15769088).count(1).model(104, 104).weight(5).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/challenge/aquarium").color(15769088).count(1).model(102, 102).weight(5).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/challenge/dragon").color(15769088).count(1).model(7, 7).weight(5).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/raw/chromatic_caves").color(3118792).count(1).model(16, 16).weight(16).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/raw/farm").color(3118792).count(1).model(17, 17).weight(16).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/raw/emerald_caves").color(3118792).count(1).model(18, 18).weight(16).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/blacksmith").color(7012096).count(1).model(1, 1).weight(4).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/mush_room").color(7012096).count(1).model(2, 2).weight(5).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/library").color(7012096).count(1).model(3, 3).weight(6).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/cove").color(7012096).count(1).model(4, 4).weight(2).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/digsite").color(7012096).count(1).model(6, 6).weight(1).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/mine").color(7012096).count(1).model(9, 9).weight(1).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/painting").color(7012096).count(1).model(10, 10).weight(4).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/trove").color(7012096).count(1).model(11, 11).weight(1).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/vendor").color(7012096).count(1).model(12, 12).weight(5).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/pixel").color(7012096).count(1).model(21, 21).weight(5).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/playzone").color(7012096).count(1).model(94, 94).weight(4).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/arcade").color(7012096).count(1).model(95, 95).weight(1).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/hellish_digsite").color(7012096).count(1).model(96, 96).weight(1).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/iskallian_garden").color(7012096).count(1).model(98, 98).weight(1).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/cube").color(7012096).count(1).model(105, 105).weight(4).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/comet_observatory").color(7012096).count(1).model(106, 106).weight(2).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/wolds_dinner").color(7012096).count(1).model(107, 107).weight(2).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/wardens_garden").color(7012096).count(1).model(108, 108).weight(2).size(0, 0))
                .entry(new PoolEntryBuilder("the_vault:vault/rooms/omega/warehouse").color(15769088).count(1).model(109, 109).weight(1).size(0, 0));

        mapModel("the_vault:vault/rooms/omega/digsite2", 6);
        mapModel("the_vault:vault/rooms/omega/cube", 105);
        mapModel("the_vault:vault/rooms/omega/comet_observatory", 106);
        mapModel("the_vault:vault/rooms/omega/wolds_dinner", 107);
        mapModel("the_vault:vault/rooms/omega/wardens_garden", 108);
        mapModel("the_vault:vault/rooms/omega/warehouse", 109);
    }
}
