package xyz.iwolfking.woldsvaults.init;

import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.RegistryEvent;
import xyz.iwolfking.woldsvaults.blocks.containers.AugmentCraftingTableContainer;
import xyz.iwolfking.woldsvaults.blocks.containers.VaultInfuserContainer;
import xyz.iwolfking.woldsvaults.blocks.containers.VaultSalvagerContainer;

public class ModContainers {

    public static MenuType<VaultSalvagerContainer> VAULT_SALVAGER_CONTAINER;
    public static MenuType<AugmentCraftingTableContainer> AUGMENT_CRAFTING_TABLE_CONTAINER;
    public static MenuType<VaultInfuserContainer> VAULT_INFUSER_CONTAINER;

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
     
        VAULT_INFUSER_CONTAINER = IForgeMenuType.create(VaultInfuserContainer::create);
        event.getRegistry().registerAll(new MenuType[]{VAULT_SALVAGER_CONTAINER.setRegistryName("vault_salvager_container"), AUGMENT_CRAFTING_TABLE_CONTAINER.setRegistryName("augment_crafting_table"), VAULT_INFUSER_CONTAINER.setRegistryName("vault_infuser")});


    }
}
