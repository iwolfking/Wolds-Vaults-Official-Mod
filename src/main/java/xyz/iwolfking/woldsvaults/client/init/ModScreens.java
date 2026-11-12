package xyz.iwolfking.woldsvaults.client.init;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.iwolfking.woldsvaults.blocks.containers.*;
import xyz.iwolfking.woldsvaults.client.screens.FilterNecklaceContainerScreen;
import xyz.iwolfking.woldsvaults.client.screens.PouchScreen;
import xyz.iwolfking.woldsvaults.client.screens.ScavengerPouchScreen;
import xyz.iwolfking.woldsvaults.client.screens.gods.GodTreeScreen;
import xyz.iwolfking.woldsvaults.client.screens.greed.GreedMilestonesScreen;
import xyz.iwolfking.woldsvaults.client.screens.SuppressorScreen;
import xyz.iwolfking.woldsvaults.init.ModContainers;
import xyz.iwolfking.woldsvaults.client.screens.armory.ArmoryScreen;
import xyz.iwolfking.woldsvaults.client.screens.armory.ArmoryAbilitiesScreen;
import xyz.iwolfking.woldsvaults.client.screens.armory.ArmoryTalentsScreen;
import xyz.iwolfking.woldsvaults.client.screens.armory.ArmoryExpertisesScreen;
import xyz.iwolfking.woldsvaults.client.screens.armory.ArmoryPrestigeScreen;
import xyz.iwolfking.woldsvaults.client.screens.armory.ArmoryGodTreeScreen;
import xyz.iwolfking.woldsvaults.blocks.containers.VaultFabricatorScreen;

@OnlyIn(Dist.CLIENT)
public class ModScreens {
    public static void register() {
        MenuScreens.register(ModContainers.VAULT_SALVAGER_CONTAINER, VaultSalvagerScreen::new);
        MenuScreens.register(ModContainers.AUGMENT_CRAFTING_TABLE_CONTAINER, AugmentCraftingTableScreen::new);
        MenuScreens.register(ModContainers.VAULT_INFUSER_CONTAINER, VaultInfuserScreen::new);
        MenuScreens.register(ModContainers.MOD_BOX_WORKSTATION_CONTAINER, ModBoxWorkstationScreen::new);
        MenuScreens.register(ModContainers.WEAVING_STATION_CONTAINER, WeavingStationScreen::new);
        MenuScreens.register(ModContainers.FILTER_NECKLACE_CONTAINER, FilterNecklaceContainerScreen::new);
        MenuScreens.register(ModContainers.SCAVENGER_POUCH_CONTAINER, ScavengerPouchScreen::new);
        MenuScreens.register(ModContainers.TRINKET_FUSION_CONTAINER, TrinketFusionScreen::new);
        MenuScreens.register(ModContainers.GREED_MILESTONES_CONTAINER, GreedMilestonesScreen::new);
        MenuScreens.register(ModContainers.GOD_TREE_CONTAINER, GodTreeScreen::new);
        MenuScreens.register(ModContainers.EXTRACTOR_ASSEMBLER_CONTAINER, ExtractorAssemblerScreen::new);
        MenuScreens.register(ModContainers.EXTRACTOR_FORGE_CONTAINER, ExtractorForgeScreen::new);
        MenuScreens.register(ModContainers.VAULT_FABRICATOR_CONTAINER, VaultFabricatorScreen::new);
        MenuScreens.register(ModContainers.EXTRACTOR_CARD_CONTAINER, ExtractorCardScreen::new);
        MenuScreens.register(ModContainers.TRINKET_POUCH_CONTAINER, PouchScreen::new);
        MenuScreens.register(ModContainers.SUPPRESSOR_CONTAINER, SuppressorScreen::new);
        MenuScreens.register(ModContainers.ARMORY_CONTAINER, ArmoryScreen::new);
        MenuScreens.register(ModContainers.ARMORY_ABILITIES_CONTAINER, ArmoryAbilitiesScreen::new);
        MenuScreens.register(ModContainers.ARMORY_TALENTS_CONTAINER, ArmoryTalentsScreen::new);
        MenuScreens.register(ModContainers.ARMORY_EXPERTISES_CONTAINER, ArmoryExpertisesScreen::new);
        MenuScreens.register(ModContainers.ARMORY_PRESTIGE_CONTAINER, ArmoryPrestigeScreen::new);
        MenuScreens.register(ModContainers.ARMORY_GODS_CONTAINER, ArmoryGodTreeScreen::new);
    }
}
