package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractVaultRoomProvider;

public class ModVaultRoomsProvider extends AbstractVaultRoomProvider {
    public ModVaultRoomsProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, existingFileHelper, WoldsVaults.MOD_ID);
    }

    @Override
    protected void registerRooms() {
        // 3x3 mythic rooms; no theme uses the mythic_rooms pool yet
        add(WoldsVaults.id("mythic/magical_forest"), room -> room.name("Magical Forest").mythic(1));
        add(WoldsVaults.id("mythic/fortress"), room -> room.name("Fortress").mythic(1));
        add(WoldsVaults.id("mythic/babel"), room -> room.name("Babel").mythic(1));
        add(WoldsVaults.id("mythic/dragon_den"), room -> room.name("Dragon Den").mythic(1));
        add(WoldsVaults.id("mythic/inverted_pyramid"), room -> room.name("Inverted Pyramid").mythic(1)
                .decor("ruins", "ruins1", "ruins2", "ruins3")
                .decor("relic", "relic1", "relic2", "relic3", "relic4")
                .decor("interior", "interior1"));
        add(WoldsVaults.id("mythic/inverted_pyramid_ruins"), room -> room.name("Inverted Pyramid Ruins").mythic(1));
        add(WoldsVaults.id("mythic/grand_canyon"), room -> room.name("Grand Canyon").mythic(1));
        add(WoldsVaults.id("mythic/hellscape"), room -> room.name("Hellscape").mythic(1));

        // only reached through the /wvpockets test crystal
        add(WoldsVaults.id("test/pocket_hub"), room -> room.name("Pocket Test Hub"));
    }
}
