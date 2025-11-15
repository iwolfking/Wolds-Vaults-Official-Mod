package xyz.iwolfking.woldsvaults.integration.inventoryhud;

import dlovin.inventoryhud.InventoryHUD;
import top.theillusivec4.curios.api.event.SlotModifiersUpdatedEvent;

public class InvHudEvent {
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
