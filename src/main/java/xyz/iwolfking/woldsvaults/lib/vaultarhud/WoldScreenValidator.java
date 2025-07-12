package xyz.iwolfking.woldsvaults.lib.vaultarhud;

import io.iridium.vaultarhud.util.Point;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import xyz.iwolfking.woldsvaults.init.ModVaultarHudScreenRegistry;
import xyz.iwolfking.woldsvaults.lib.vaultarhud.screens.*;
import xyz.iwolfking.woldsvaults.util.ConditionalModUtils;

import java.util.List;

public class WoldScreenValidator {

    public WoldScreenValidator() {
    }

    public static boolean isValidScreen(Screen screen) {
        return isScreenInList(screen, ModVaultarHudScreenRegistry.getScreens());
    }

    public static boolean isScreenInList(Screen screen, List<Class<? extends Screen>> screenList) {
        return screenList.stream().anyMatch(cls -> cls.isInstance(screen));
    }

    public static Point getScreenHUDCoordinates(Screen screen, Point offset) {
        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        double x = 0.0;
        double y = 0.0;
        if (isScreenInList(screen, VanillaScreens.SCREENS)) {
            x = ((screenWidth - 176) / 2) - offset.getX();
            y = ((screenHeight + 166) / 2) - offset.getY();
        }

        AbstractContainerScreen<?> gs;
        if (ConditionalModUtils.isModPresent("sophisticatedbackpacks") && isScreenInList(screen, SophisticatedBackpacksScreens.SCREENS)) {
            gs = (AbstractContainerScreen<?>)screen;
            x = ((screenWidth - 176) / 2) - offset.getX();
            y = ((screenHeight + gs.getYSize()) / 2) - offset.getY();
        }

        else if (ConditionalModUtils.isModPresent("refinedstorage") && isScreenInList(screen, RefinedStorageScreens.SCREENS)) {
            gs = (AbstractContainerScreen)screen;
            x = ((screenWidth - gs.getXSize()) / 2) - offset.getX();
            y = ((screenHeight + gs.getYSize()) / 2) - offset.getY() - 1.0;
        }

        else {
            gs = (AbstractContainerScreen)screen;
            x = ((screenWidth - gs.getXSize()) / 2) - offset.getX();
            y = ((screenHeight + gs.getYSize()) / 2) - offset.getY();
        }

        return new Point(x, y);
    }
}
