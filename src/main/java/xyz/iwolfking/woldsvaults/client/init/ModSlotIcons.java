package xyz.iwolfking.woldsvaults.client.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.LinkedList;
import java.util.List;

@Mod.EventBusSubscriber(
        value = {Dist.CLIENT},
        bus = Mod.EventBusSubscriber.Bus.MOD,
        modid = WoldsVaults.MOD_ID
)
public class ModSlotIcons {
    public static final List<ResourceLocation> REGISTRY = new LinkedList<>();
    public static final ResourceLocation LAYOUT_NO_ITEM = register("gui/slot/layout_no_item");
    public static final ResourceLocation PLACEHOLDER_NO_ITEM = register("gui/slot/placeholder_no_item");

    private static ResourceLocation register(String path) {
        ResourceLocation icon = WoldsVaults.id(path);
        REGISTRY.add(icon);
        return icon;
    }

    @SubscribeEvent
    public static void stitchIcons(TextureStitchEvent.Pre event) {
        if (event.getAtlas().location() == InventoryMenu.BLOCK_ATLAS) {
            for(ResourceLocation icon : REGISTRY) {
                event.addSprite(icon);
            }
        }
    }
}
