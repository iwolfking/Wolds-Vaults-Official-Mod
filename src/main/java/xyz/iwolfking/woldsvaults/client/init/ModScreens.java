package xyz.iwolfking.woldsvaults.client.init;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import shadows.gateways.gate.Gateway;
import xyz.iwolfking.woldsvaults.blocks.containers.*;
import xyz.iwolfking.woldsvaults.client.screens.FilterNecklaceContainerScreen;
import xyz.iwolfking.woldsvaults.gui.menus.divinity.DivinityElementContainerScreen;
import xyz.iwolfking.woldsvaults.init.ModContainers;

@OnlyIn(Dist.CLIENT)
public class ModScreens {
    public static void register() {
        MenuScreens.register(ModContainers.VAULT_SALVAGER_CONTAINER, VaultSalvagerScreen::new);
        MenuScreens.register(ModContainers.AUGMENT_CRAFTING_TABLE_CONTAINER, AugmentCraftingTableScreen::new);
        MenuScreens.register(ModContainers.VAULT_INFUSER_CONTAINER, VaultInfuserScreen::new);
        MenuScreens.register(ModContainers.MOD_BOX_WORKSTATION_CONTAINER, ModBoxWorkstationScreen::new);
        MenuScreens.register(ModContainers.WEAVING_STATION_CONTAINER, WeavingStationScreen::new);
        MenuScreens.register(ModContainers.FILTER_NECKLACE_CONTAINER, FilterNecklaceContainerScreen::new);
        MenuScreens.register(ModContainers.DIVINITY_TAB_CONTAINER, DivinityElementContainerScreen::new);
    }
}
