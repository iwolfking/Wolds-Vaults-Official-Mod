package xyz.iwolfking.woldsvaults.client.screens.armory;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.iwolfking.woldsvaults.armory.ArmoryRegistry;
import xyz.iwolfking.woldsvaults.armory.menu.ArmoryGodMenus;
import xyz.iwolfking.woldsvaults.armory.menu.ArmoryTreeMenus;

/** Registers the armory's screens and block render layers. */
@OnlyIn(Dist.CLIENT)
public final class ArmoryScreens {
    private ArmoryScreens() {
    }

    public static void register() {
        MenuScreens.register(ArmoryRegistry.ARMORY_MENU, ArmoryScreen::new);
        MenuScreens.register(ArmoryTreeMenus.ABILITIES, ArmoryAbilitiesScreen::new);
        MenuScreens.register(ArmoryTreeMenus.TALENTS, ArmoryTalentsScreen::new);
        MenuScreens.register(ArmoryTreeMenus.EXPERTISES, ArmoryExpertisesScreen::new);
        MenuScreens.register(ArmoryTreeMenus.PRESTIGE, ArmoryPrestigeScreen::new);
        MenuScreens.register(ArmoryGodMenus.ARMORY_GODS, ArmoryGodTreeScreen::new);
        ItemBlockRenderTypes.setRenderLayer(ArmoryRegistry.ARMORY, RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ArmoryRegistry.CREATIVE_ARMORY, RenderType.cutout());
    }
}
