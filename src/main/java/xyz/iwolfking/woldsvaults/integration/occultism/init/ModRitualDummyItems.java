package xyz.iwolfking.woldsvaults.integration.occultism.init;

import com.github.klikli_dev.occultism.Occultism;
import com.github.klikli_dev.occultism.common.item.DummyTooltipItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.RegistryEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.datagen.ModLanguageProvider;

public class ModRitualDummyItems {
    public static DummyTooltipItem CRAFT_IDONA_BRICKS = new DummyTooltipItem(defaultProperties());
    public static DummyTooltipItem CRAFT_VELARA_BRICKS = new DummyTooltipItem(defaultProperties());
    public static DummyTooltipItem CRAFT_TENOS_BRICKS = new DummyTooltipItem(defaultProperties());
    public static DummyTooltipItem CRAFT_WENDARR_BRICKS = new DummyTooltipItem(defaultProperties());
    public static DummyTooltipItem SACRIFICE_COMPANION = new DummyTooltipItem(defaultProperties());
    public static DummyTooltipItem SPAWN_EGG_INFUSION = new DummyTooltipItem(defaultProperties());
    public static DummyTooltipItem CRYSTAL_TIME_EXTENSION = new DummyTooltipItem(defaultProperties());

    public static void registerItems(RegistryEvent.Register<Item> event) {
        register(event, WoldsVaults.id("ritual_dummy/craft_idona_bricks"), CRAFT_IDONA_BRICKS, "Convert Bricks - Idona", WoldsVaults.id("idona_bricks"),  "A way to craft Idona's lovely decorative bricks without vault delving!");
        register(event, WoldsVaults.id("ritual_dummy/craft_velara_bricks"), CRAFT_VELARA_BRICKS, "Convert Bricks - Velara", WoldsVaults.id("velara_bricks"),  "A way to craft Velara's lovely decorative bricks without vault delving!");
        register(event, WoldsVaults.id("ritual_dummy/craft_tenos_bricks"), CRAFT_TENOS_BRICKS, "Convert Bricks - Tenos", WoldsVaults.id("tenos_bricks"),  "A way to craft Tenos' lovely decorative bricks without vault delving!");
        register(event, WoldsVaults.id("ritual_dummy/craft_wendarr_bricks"), CRAFT_WENDARR_BRICKS, "Convert Bricks - Wendarr", WoldsVaults.id("wendarr_bricks"),  "A way to craft Wendarr's lovely decorative bricks without vault delving!");
        register(event, WoldsVaults.id("ritual_dummy/sacrifice_companion"), SACRIFICE_COMPANION, "Sacrifice Companion", WoldsVaults.id("companion_sacrifice"),  "Sacrifice a Companion to Idona and he will reward you with a random Companion Relic!");
        register(event, WoldsVaults.id("ritual_dummy/spawn_egg_infusion"), SPAWN_EGG_INFUSION, "Imbue Spawn Egg", WoldsVaults.id("infuse_pig_spawn_egg"), "Imbue a Mystery Egg with life to transform it into a specific type of spawn egg!");
        register(event, WoldsVaults.id("ritual_dummy/crystal_time_extension"), CRYSTAL_TIME_EXTENSION, "Wendarr Time Extension", WoldsVaults.id("wendarr_crystal_time_extension"), "Make an offering to Wendarr for them to bless your vault with Extended time... and a curse! After 7 Extended have been added this way, makes crystal unmodifiable.");
    }

    private static void register(RegistryEvent.Register<Item> event, ResourceLocation id, Item item, String name, ResourceLocation ritualId, String ritualDescription) {
        event.getRegistry().register(item.setRegistryName(id));
        registerRitualLangWithDummy(item, ritualDescription, ritualId, name);
    }

    public static void registerRitualLangWithDummy(Item dummyItem, String ritualTooltip, ResourceLocation ritualId, String ritualName) {
        ModLanguageProvider.register("ritual." + ritualId.getNamespace() + "." + ritualId.getPath() + ".started", ritualName + " initiated!");
        ModLanguageProvider.register("ritual." + ritualId.getNamespace() + "." + ritualId.getPath() + ".finished", ritualName + " completed!");
        ModLanguageProvider.register(dummyItem, "Ritual: " + ritualName);
        ModLanguageProvider.register("item." + dummyItem.getRegistryName().getNamespace() + ".ritual_dummy." + dummyItem.getRegistryName().getPath().replace("ritual_dummy/", "") + ".tooltip", ritualTooltip);
    }

    public static Item.Properties defaultProperties() {
        return (new Item.Properties()).tab(Occultism.ITEM_GROUP);
    }

}