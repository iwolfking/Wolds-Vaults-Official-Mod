package xyz.iwolfking.woldsvaults.items;

import com.simibubi.create.AllKeys;
import com.simibubi.create.content.logistics.filter.FilterItem;
import com.simibubi.create.foundation.utility.Components;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.BasicItem;
import iskallia.vault.item.gear.VoidStoneItem;
import iskallia.vault.research.StageManager;
import iskallia.vault.world.data.PlayerVaultStatsData;
import iskallia.vault.world.data.ServerVaults;
import net.joseph.vaultfilters.VFTests;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import xyz.iwolfking.woldsvaults.gui.menus.FilterNecklaceMenu;
import xyz.iwolfking.woldsvaults.init.ModCreativeTabs;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.mixins.create.accessors.FilterItemAccessor;

import java.util.List;

import static iskallia.vault.init.ModItems.VAULT_MOD_GROUP;

public class FilterNecklaceItem extends BasicItem implements ICurioItem {

    private final int slotCount;

    public FilterNecklaceItem(ResourceLocation id, int slotCount) {
        super(id, new Properties().stacksTo(1).tab(ModCreativeTabs.WOLDS_VAULTS));
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
                if(VFTests.checkFilter(stack, slotStack, true, null)) {
                    return true;
                }
            }
            else if (slotStack.getItem().equals(stack.getItem())) {
                return true;
            }
        }

        return false;
    }

    public static ItemStack getNecklace(Player player) {
        SlotResult result = CuriosApi.getCuriosHelper().findFirstCurio(player, ModItems.FILTER_NECKLACE).orElse(null);

        if(result == null) {
            return ItemStack.EMPTY;
        }

        return result.stack();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if(!level.isClientSide) {
            NetworkHooks.openGui((ServerPlayer) player, new SimpleMenuProvider((id, inv, ply) -> new FilterNecklaceMenu(id, inv, player.getItemInHand(hand)),
            new TextComponent("Filter Necklace")));
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        LivingEntity livingEntity = slotContext.entity();
        if (livingEntity instanceof ServerPlayer player) {
            if (!StageManager.getResearchTree(player).isResearched(ModConfigs.RESEARCHES.getByName("Junk Management"))) {
                return false;
            }
        }

        return ICurioItem.super.canEquip(slotContext, stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        if (ModConfigs.isInitialized() && !StageManager.RESEARCH_TREE.isResearched(ModConfigs.RESEARCHES.getByName("Junk Management"))) {
            tooltip.add((new TextComponent("Unlock \"Junk Management\" to equip")).withStyle(ChatFormatting.RED));
            tooltip.add(TextComponent.EMPTY);
            return;
        }

        if (Screen.hasShiftDown()) {
            IItemHandler inventory = getInventory(stack);
            tooltip.add(new TextComponent("Hold down [").withStyle(ChatFormatting.GRAY).append(new TextComponent("Ctrl").withStyle(ChatFormatting.YELLOW)).append(new TextComponent("] to show nested filters.").withStyle(ChatFormatting.GRAY)));
            for(int i = 0; i < inventory.getSlots(); i++) {
                ItemStack slotStack = inventory.getStackInSlot(i);
                if(inventory.getStackInSlot(i).getItem() instanceof FilterItem filterItem) {
                    List<Component> makeSummary = ((FilterItemAccessor)filterItem).runMakeSummary(slotStack);
                    if (makeSummary.isEmpty()) {
                        continue;
                    }
                    tooltip.add(new TextComponent(""));
                    tooltip.addAll(makeSummary);
                }
            }
        }
        else {
            tooltip.add(new TextComponent("Hold down [").withStyle(ChatFormatting.GRAY).append(new TextComponent("Shift").withStyle(ChatFormatting.YELLOW)).append(new TextComponent("] to show details.").withStyle(ChatFormatting.GRAY)));
            tooltip.add(new TextComponent(""));
            tooltip.add(new TextComponent("Insert Attribute Filters inside and any matching items will be voided inside vaults while wearing this!"));
        }
    }
}
