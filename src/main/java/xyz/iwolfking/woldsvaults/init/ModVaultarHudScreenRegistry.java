package xyz.iwolfking.woldsvaults.init;

import net.minecraft.client.gui.screens.Screen;
import xyz.iwolfking.woldsvaults.lib.vaultarhud.screens.*;
import xyz.iwolfking.woldsvaults.util.ConditionalModUtils;

import java.util.ArrayList;
import java.util.List;

public class ModVaultarHudScreenRegistry {
    private static final List<Class<? extends Screen>> SCREENS = new ArrayList<>();


    public static void init() {
        if(ConditionalModUtils.isModPresent("ae2")) {
            SCREENS.addAll(AE2Screens.SCREENS);
        }
        if(ConditionalModUtils.isModPresent("colossalchests")) {
            SCREENS.addAll(CollosalChestScreens.SCREENS);
        }
        if(ConditionalModUtils.isModPresent("integrateddynamics")) {
            SCREENS.addAll(IntegratedDynamicsScreens.SCREENS);
        }
        if(ConditionalModUtils.isModPresent("refinedstorage")) {
            SCREENS.addAll(RefinedStorageScreens.SCREENS);
        }
        if(ConditionalModUtils.isModPresent("thermal")) {
            SCREENS.addAll(ThermalScreens.SCREENS);
        }
        if(ConditionalModUtils.isModPresent("toms_storage")) {
            SCREENS.addAll(TomsScreens.SCREENS);
        }
        if(ConditionalModUtils.isModPresent("sophisticatedbackpacks")) {
            SCREENS.addAll(SophisticatedBackpacksScreens.SCREENS);
        }

        SCREENS.addAll(VanillaScreens.SCREENS);

    }

    public static void registerScreens(List<Class<? extends Screen>> screens) {
        SCREENS.addAll(screens);
    }

    public static List<Class<? extends Screen>> getScreens() {
        return SCREENS;
    }
}
