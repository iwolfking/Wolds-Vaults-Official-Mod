package xyz.iwolfking.woldsvaults.integration.occultism;

import com.github.klikli_dev.occultism.Occultism;
import com.github.klikli_dev.occultism.common.item.DummyTooltipItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.RegistryEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class ModRitualDummyItems {
    public static DummyTooltipItem CRAFT_IDONA_BRICKS = new DummyTooltipItem(defaultProperties());
    public static DummyTooltipItem CRAFT_VELARA_BRICKS = new DummyTooltipItem(defaultProperties());
    public static DummyTooltipItem CRAFT_TENOS_BRICKS = new DummyTooltipItem(defaultProperties());
    public static DummyTooltipItem CRAFT_WENDARR_BRICKS = new DummyTooltipItem(defaultProperties());

    public static void registerItems(RegistryEvent.Register<Item> event) {
        register(event, WoldsVaults.id("ritual_dummy/craft_idona_bricks"), CRAFT_IDONA_BRICKS);
        register(event, WoldsVaults.id("ritual_dummy/craft_velara_bricks"), CRAFT_VELARA_BRICKS);
        register(event, WoldsVaults.id("ritual_dummy/craft_tenos_bricks"), CRAFT_TENOS_BRICKS);
        register(event, WoldsVaults.id("ritual_dummy/craft_wendarr_bricks"), CRAFT_WENDARR_BRICKS);
    }

    private static void register(RegistryEvent.Register<Item> event, ResourceLocation id, Item item) {
        event.getRegistry().register(item.setRegistryName(id));
    }

    public static Item.Properties defaultProperties() {
        return (new Item.Properties()).tab(Occultism.ITEM_GROUP);
    }

}