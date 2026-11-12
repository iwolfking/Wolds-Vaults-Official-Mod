package xyz.iwolfking.woldsvaults.init;

import iskallia.vault.container.inventory.VaultKeyringContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.RegistryEvent;
import xyz.iwolfking.woldsvaults.blocks.containers.*;
import xyz.iwolfking.woldsvaults.gods.container.GodTreeContainer;
import xyz.iwolfking.woldsvaults.items.filter_necklace.menus.FilterNecklaceMenu;
import xyz.iwolfking.woldsvaults.items.scavenger_pouch.menu.ScavengerPouchContainer;
import xyz.iwolfking.woldsvaults.milestones.container.GreedMilestonesContainer;
import xyz.iwolfking.woldsvaults.items.trinket_pouch.menu.PouchMenu;
import iskallia.vault.container.NBTElementContainer;
import iskallia.vault.skill.tree.AbilityTree;
import iskallia.vault.skill.tree.ExpertiseTree;
import iskallia.vault.skill.tree.PrestigeTree;
import iskallia.vault.skill.tree.TalentTree;
import xyz.iwolfking.woldsvaults.armory.menu.ArmoryGodContainer;
import xyz.iwolfking.woldsvaults.armory.menu.ArmoryMenu;
import xyz.iwolfking.woldsvaults.armory.menu.ArmoryTreeContainer;

public class ModContainers {

    public static MenuType<VaultSalvagerContainer> VAULT_SALVAGER_CONTAINER;
    public static MenuType<AugmentCraftingTableContainer> AUGMENT_CRAFTING_TABLE_CONTAINER;
    public static MenuType<VaultInfuserContainer> VAULT_INFUSER_CONTAINER;
    public static MenuType<ModBoxWorkstationContainer> MOD_BOX_WORKSTATION_CONTAINER;
    public static MenuType<WeavingStationContainer> WEAVING_STATION_CONTAINER;
    public static MenuType<FilterNecklaceMenu> FILTER_NECKLACE_CONTAINER;
    public static MenuType<ScavengerPouchContainer> SCAVENGER_POUCH_CONTAINER;
    public static MenuType<TrinketFusionContainer> TRINKET_FUSION_CONTAINER;
    public static MenuType<GreedMilestonesContainer> GREED_MILESTONES_CONTAINER;
    public static MenuType<GodTreeContainer> GOD_TREE_CONTAINER;
    public static MenuType<SuppressorContainer> SUPPRESSOR_CONTAINER;
    public static MenuType<ExtractorAssemblerContainer> EXTRACTOR_ASSEMBLER_CONTAINER;
    public static MenuType<ExtractorForgeContainer> EXTRACTOR_FORGE_CONTAINER;
    public static MenuType<xyz.iwolfking.woldsvaults.blocks.containers.VaultFabricatorContainer> VAULT_FABRICATOR_CONTAINER;
    public static MenuType<ExtractorCardMenu> EXTRACTOR_CARD_CONTAINER;
    public static MenuType<PouchMenu> TRINKET_POUCH_CONTAINER;
    public static MenuType<ArmoryMenu> ARMORY_CONTAINER;
    public static MenuType<NBTElementContainer<AbilityTree>> ARMORY_ABILITIES_CONTAINER;
    public static MenuType<NBTElementContainer<TalentTree>> ARMORY_TALENTS_CONTAINER;
    public static MenuType<NBTElementContainer<ExpertiseTree>> ARMORY_EXPERTISES_CONTAINER;
    public static MenuType<NBTElementContainer<PrestigeTree>> ARMORY_PRESTIGE_CONTAINER;
    public static MenuType<ArmoryGodContainer> ARMORY_GODS_CONTAINER;

    public static void register(RegistryEvent.Register<MenuType<?>> event) {
        VAULT_SALVAGER_CONTAINER = IForgeMenuType.create((windowId, inventory, buffer) -> {
            Level world = inventory.player.getCommandSenderWorld();
            BlockPos pos = buffer.readBlockPos();
            return new VaultSalvagerContainer(windowId, world, pos, inventory);
        });
        AUGMENT_CRAFTING_TABLE_CONTAINER = IForgeMenuType.create((windowId, inventory, buffer) -> {
            Level world = inventory.player.getCommandSenderWorld();
            BlockPos pos = buffer.readBlockPos();
            return new AugmentCraftingTableContainer(windowId, world, pos, inventory);
        });
        MOD_BOX_WORKSTATION_CONTAINER = IForgeMenuType.create((windowId, inventory, buffer) -> {
            Level world = inventory.player.getCommandSenderWorld();
            BlockPos pos = buffer.readBlockPos();
            return new ModBoxWorkstationContainer(windowId, world, pos, inventory);
        });
        WEAVING_STATION_CONTAINER = IForgeMenuType.create((windowId, inventory, buffer) -> {
            Level world = inventory.player.getCommandSenderWorld();
            BlockPos pos = buffer.readBlockPos();
            return new WeavingStationContainer(windowId, world, pos, inventory);
        });
     
        VAULT_INFUSER_CONTAINER = IForgeMenuType.create(VaultInfuserContainer::create);
        FILTER_NECKLACE_CONTAINER = IForgeMenuType.create(FilterNecklaceMenu::new);
        TRINKET_FUSION_CONTAINER = IForgeMenuType.create(TrinketFusionContainer::new);
        SUPPRESSOR_CONTAINER = IForgeMenuType.create(SuppressorContainer::new);
        TRINKET_POUCH_CONTAINER = IForgeMenuType.create(PouchMenu::client);
        SCAVENGER_POUCH_CONTAINER = IForgeMenuType.create((windowId, inventory, data) -> {
            int slot = data.readInt();
            return new ScavengerPouchContainer(windowId, inventory, slot);
        });
        GREED_MILESTONES_CONTAINER = IForgeMenuType.create((windowId, inventory, data) -> new GreedMilestonesContainer(windowId, inventory.player));
        GOD_TREE_CONTAINER = IForgeMenuType.create((windowId, inventory, data) -> new GodTreeContainer(windowId, inventory.player));
        EXTRACTOR_ASSEMBLER_CONTAINER = IForgeMenuType.create(ExtractorAssemblerContainer::create);
        EXTRACTOR_FORGE_CONTAINER = IForgeMenuType.create(ExtractorForgeContainer::create);
        VAULT_FABRICATOR_CONTAINER = IForgeMenuType.create(xyz.iwolfking.woldsvaults.blocks.containers.VaultFabricatorContainer::create);
        EXTRACTOR_CARD_CONTAINER = IForgeMenuType.create(ExtractorCardMenu::create);
        ARMORY_CONTAINER = IForgeMenuType.create(ArmoryMenu::client);
        ARMORY_ABILITIES_CONTAINER = IForgeMenuType.create((windowId, inventory, buf) ->
                ArmoryTreeContainer.client(() -> ARMORY_ABILITIES_CONTAINER, windowId, inventory, buf, new AbilityTree()));
        ARMORY_TALENTS_CONTAINER = IForgeMenuType.create((windowId, inventory, buf) ->
                ArmoryTreeContainer.client(() -> ARMORY_TALENTS_CONTAINER, windowId, inventory, buf, new TalentTree()));
        ARMORY_EXPERTISES_CONTAINER = IForgeMenuType.create((windowId, inventory, buf) ->
                ArmoryTreeContainer.client(() -> ARMORY_EXPERTISES_CONTAINER, windowId, inventory, buf, new ExpertiseTree()));
        ARMORY_PRESTIGE_CONTAINER = IForgeMenuType.create((windowId, inventory, buf) ->
                ArmoryTreeContainer.client(() -> ARMORY_PRESTIGE_CONTAINER, windowId, inventory, buf, new PrestigeTree()));
        ARMORY_GODS_CONTAINER = IForgeMenuType.create(ArmoryGodContainer::client);
        event.getRegistry().registerAll(new MenuType[]{VAULT_SALVAGER_CONTAINER.setRegistryName("vault_salvager_container"), AUGMENT_CRAFTING_TABLE_CONTAINER.setRegistryName("augment_crafting_table"), VAULT_INFUSER_CONTAINER.setRegistryName("vault_infuser"), MOD_BOX_WORKSTATION_CONTAINER.setRegistryName("mod_box_workstation"), WEAVING_STATION_CONTAINER.setRegistryName("weaving_station_container"), FILTER_NECKLACE_CONTAINER.setRegistryName("filter_necklace_container"), SCAVENGER_POUCH_CONTAINER.setRegistryName("scavenger_pouch_container"), TRINKET_FUSION_CONTAINER.setRegistryName("trinket_fusion_container"), GREED_MILESTONES_CONTAINER.setRegistryName("greed_milestones_container"), GOD_TREE_CONTAINER.setRegistryName("god_tree_container"),
                EXTRACTOR_ASSEMBLER_CONTAINER.setRegistryName("extractor_assembler_container"), EXTRACTOR_FORGE_CONTAINER.setRegistryName("extractor_forge_container"),
                VAULT_FABRICATOR_CONTAINER.setRegistryName("vault_fabricator_container"),
                EXTRACTOR_CARD_CONTAINER.setRegistryName("extractor_card_container"),
                TRINKET_POUCH_CONTAINER.setRegistryName("trinket_pouch"), SUPPRESSOR_CONTAINER.setRegistryName("suppressor_container"),
                ARMORY_CONTAINER.setRegistryName("armory"), ARMORY_ABILITIES_CONTAINER.setRegistryName("armory_abilities"),
                ARMORY_TALENTS_CONTAINER.setRegistryName("armory_talents"), ARMORY_EXPERTISES_CONTAINER.setRegistryName("armory_expertises"),
                ARMORY_PRESTIGE_CONTAINER.setRegistryName("armory_prestige"), ARMORY_GODS_CONTAINER.setRegistryName("armory_gods")});


    }
}
