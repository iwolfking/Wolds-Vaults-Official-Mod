package xyz.iwolfking.woldsvaults.integration.arsnouveau.init;

import iskallia.vault.item.BasicMobEggItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.RegistryEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModCreativeTabs;

public class ArsSpawnEggItems {
    public static final BasicMobEggItem DRYGMY_SPAWN_EGG = new BasicMobEggItem(WoldsVaults.id("drygmy_spawn_egg"), () -> com.hollingsworth.arsnouveau.common.entity.ModEntities.ENTITY_DRYGMY, 25088, DyeColor.GREEN.getId(), (new Item.Properties()).tab(ModCreativeTabs.WOLDS_VAULTS));

    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(DRYGMY_SPAWN_EGG);
    }
}
