package xyz.iwolfking.woldsvaults.lib.vaultarhud.screens;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.FurnaceScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

import java.util.Arrays;
import java.util.List;

public class VanillaScreens  {
    public static final List<Class<? extends Screen>> SCREENS = Arrays.asList(InventoryScreen.class, ContainerScreen.class, CraftingScreen.class, FurnaceScreen.class);

}
