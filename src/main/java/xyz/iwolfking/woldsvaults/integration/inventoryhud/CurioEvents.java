package xyz.iwolfking.woldsvaults.integration.inventoryhud;


import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.event.SlotModifiersUpdatedEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;

@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, value = Dist.CLIENT)
public class CurioEvents {
    @SubscribeEvent
    public static void onCurioModifiersUpdated(SlotModifiersUpdatedEvent event) {
        if (ModList.get().isLoaded("inventoryhud")) {
            InvHudEvent.onCurioModifiersUpdated(event);
        }
    }
}
