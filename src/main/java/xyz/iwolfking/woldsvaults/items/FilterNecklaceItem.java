package xyz.iwolfking.woldsvaults.items;

import com.simibubi.create.content.logistics.filter.FilterItem;
import iskallia.vault.item.BasicItem;
import net.joseph.vaultfilters.VFTests;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import xyz.iwolfking.woldsvaults.gui.menus.FilterNecklaceMenu;

import static iskallia.vault.init.ModItems.VAULT_MOD_GROUP;

public class FilterNecklaceItem extends BasicItem implements ICurioItem {

    private final int slotCount;

    public FilterNecklaceItem(ResourceLocation id, int slotCount) {
        super(id, new Properties().stacksTo(1).tab(VAULT_MOD_GROUP));
        this.slotCount = slotCount;
    }

    public IItemHandler getInventory(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        ItemStackHandler handler = new ItemStackHandler(slotCount) {
            @Override
            protected void onContentsChanged(int slot) {
                saveToStack(stack, this);
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack incoming) {
                return incoming.getItem() instanceof FilterItem;
            }
        };

        if(tag.contains("Inventory")) {
            handler.deserializeNBT(tag.getCompound("Inventory"));
        }

        return handler;
    }

    private void saveToStack(ItemStack stack, ItemStackHandler handler) {
        CompoundTag tag = stack.getOrCreateTag();
        CompoundTag newInvTag = handler.serializeNBT();
        if(!tag.contains("Inventory") || !tag.getCompound("Inventory").equals(newInvTag)) {
            tag.put("Inventory", handler.serializeNBT());
        }
    }

    public boolean stackMatchesFilter(ItemStack stack, ItemStack necklace) {
        IItemHandler handler = this.getInventory(necklace);
        for(int i = 0; i < handler.getSlots(); i++) {
            ItemStack slotStack = handler.getStackInSlot(i);
            if(slotStack.getItem() instanceof FilterItem) {
                return VFTests.checkFilter(stack, slotStack, true, null);
            }
            else if (slotStack.getItem().equals(stack.getItem())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if(!level.isClientSide) {
            NetworkHooks.openGui((ServerPlayer) player, new SimpleMenuProvider((id, inv, ply) -> new FilterNecklaceMenu(id, inv, player.getItemInHand(hand)),
            new TextComponent("Filter Container")));
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
}
