package xyz.iwolfking.woldsvaults.init;

import iskallia.vault.container.inventory.VaultKeyringContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.RegistryEvent;
import xyz.iwolfking.woldsvaults.blocks.containers.*;
import xyz.iwolfking.woldsvaults.items.filter_necklace.menus.FilterNecklaceMenu;
import xyz.iwolfking.woldsvaults.items.scavenger_pouch.menu.ScavengerPouchContainer;

public class ModContainers {

    public static MenuType<VaultSalvagerContainer> VAULT_SALVAGER_CONTAINER;
    public static MenuType<AugmentCraftingTableContainer> AUGMENT_CRAFTING_TABLE_CONTAINER;
    public static MenuType<VaultInfuserContainer> VAULT_INFUSER_CONTAINER;
    public static MenuType<ModBoxWorkstationContainer> MOD_BOX_WORKSTATION_CONTAINER;
    public static MenuType<WeavingStationContainer> WEAVING_STATION_CONTAINER;
    public static MenuType<FilterNecklaceMenu> FILTER_NECKLACE_CONTAINER;
    public static MenuType<ScavengerPouchContainer> SCAVENGER_POUCH_CONTAINER;

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
        SCAVENGER_POUCH_CONTAINER = IForgeMenuType.create((windowId, inventory, data) -> {
            int slot = data.readInt();
            return new ScavengerPouchContainer(windowId, inventory, slot);
        });
        event.getRegistry().registerAll(new MenuType[]{VAULT_SALVAGER_CONTAINER.setRegistryName("vault_salvager_container"), AUGMENT_CRAFTING_TABLE_CONTAINER.setRegistryName("augment_crafting_table"), VAULT_INFUSER_CONTAINER.setRegistryName("vault_infuser"), MOD_BOX_WORKSTATION_CONTAINER.setRegistryName("mod_box_workstation"), WEAVING_STATION_CONTAINER.setRegistryName("weaving_station_container"), FILTER_NECKLACE_CONTAINER.setRegistryName("filter_necklace_container"), SCAVENGER_POUCH_CONTAINER.setRegistryName("scavenger_pouch_container")});


    }
}
