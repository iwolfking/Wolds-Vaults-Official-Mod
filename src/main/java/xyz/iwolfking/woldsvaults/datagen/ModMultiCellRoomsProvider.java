package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import xyz.iwolfking.woldsvaults.config.MultiCellRoomsConfig;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractWoldsTableProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Multi-cell room pools. Each entry names a template pool, its span in grid cells (3 = a room covering 2x2 room
 * slots, 141 blocks wide) and every template that pool can pick, so the client can size a discovered room.
 */
public class ModMultiCellRoomsProvider extends AbstractWoldsTableProvider {
    private static final int MYTHIC_HEIGHT = 144;
    private static final List<String> MYTHIC_ROOMS = List.of("magical_forest", "fortress", "babel", "dragon_den", "inverted_pyramid",
            "inverted_pyramid_ruins", "grand_canyon", "hellscape");

    public ModMultiCellRoomsProvider(DataGenerator generator) {
        super(generator, "layout/multi_cell_rooms");
    }

    @Override
    public void registerConfigs() {
        MultiCellRoomsConfig table = new MultiCellRoomsConfig();
        table.rooms = new ArrayList<>();
        table.rooms.add(MultiCellRoomsConfig.Entry.of("woldsvaults:vault/rooms/special/extraction_3x3", 3,
                List.of("woldsvaults:vault/rooms/special/extraction_3x3_1")));
        List<String> mythic = MYTHIC_ROOMS.stream().map(room -> "woldsvaults:vault/rooms/mythic/" + room).toList();
        table.rooms.add(MultiCellRoomsConfig.Entry.of("woldsvaults:vault/rooms/mythic_rooms", 3, MYTHIC_HEIGHT, mythic));
        for (String template : mythic) {
            table.rooms.add(MultiCellRoomsConfig.Entry.of(template, 3, MYTHIC_HEIGHT, List.of(template)));
        }
        this.table("multi_cell_rooms", table);
    }
}
