package xyz.iwolfking.woldsvaults.api.util.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.p3pp3rf1y.sophisticatedbackpacks.client.gui.BackpackScreen;

public class ClientBackpackUtil {

    public static boolean isSupportedDelootingScreen(Screen screen) {
        if(screen instanceof BackpackScreen || screen instanceof CreativeModeInventoryScreen) {
            return false;
        }

        return screen instanceof AbstractContainerScreen<?> abstractContainerScreen && !abstractContainerScreen.getMenu().slots.isEmpty();
    }
}
