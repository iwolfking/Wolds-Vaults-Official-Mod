package xyz.iwolfking.woldsvaults.client.init;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import shadows.gateways.gate.Gateway;
import xyz.iwolfking.woldsvaults.blocks.containers.AugmentCraftingTableScreen;
import xyz.iwolfking.woldsvaults.blocks.containers.VaultInfuserScreen;
import xyz.iwolfking.woldsvaults.blocks.containers.VaultSalvagerScreen;
import xyz.iwolfking.woldsvaults.init.ModContainers;

@OnlyIn(Dist.CLIENT)
public class ModScreens {
    public static void register() {
        MenuScreens.register(ModContainers.VAULT_SALVAGER_CONTAINER, VaultSalvagerScreen::new);
        MenuScreens.register(ModContainers.AUGMENT_CRAFTING_TABLE_CONTAINER, AugmentCraftingTableScreen::new);
        MenuScreens.register(ModContainers.VAULT_INFUSER_CONTAINER, VaultInfuserScreen::new);
    }
}
