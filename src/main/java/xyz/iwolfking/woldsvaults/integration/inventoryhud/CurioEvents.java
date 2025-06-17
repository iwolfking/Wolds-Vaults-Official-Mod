package xyz.iwolfking.woldsvaults.integration.inventoryhud;


import dlovin.inventoryhud.InventoryHUD;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.event.SlotModifiersUpdatedEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;

@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, value = Dist.CLIENT)
public class CurioEvents {
    @SubscribeEvent
    public static void onCurioModifiersUpdated(SlotModifiersUpdatedEvent event) {
        try {
            if (InventoryHUD.isCuriosMod) {
                InventoryHUD.getInstance().getInventoryGui().disableCurios();
            }
        } catch (Exception ignored) {
            // invhud not loaded
        }
    }
}
