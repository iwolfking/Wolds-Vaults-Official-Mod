package xyz.iwolfking.woldsvaults.items;

import iskallia.vault.container.oversized.OverSizedInvWrapper;
import iskallia.vault.container.oversized.OverSizedInventory;
import iskallia.vault.container.oversized.OverSizedItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import iskallia.vault.item.BasicScavengerItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;
import xyz.iwolfking.woldsvaults.init.ModCreativeTabs;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.items.scavenger_pouch.provider.ScavengerPouchProvider;

public class ItemScavengerPouch extends Item {

    public ItemScavengerPouch(ResourceLocation id) {
        super(new Properties().stacksTo(1).tab(ModCreativeTabs.WOLDS_VAULTS));
        this.setRegistryName(id);
    }

    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> items) {
        if (this.allowdedIn(tab)) {
            items.add(new ItemStack(this));
        }
    }

    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack inHand = player.getItemInHand(hand);
        if (player instanceof ServerPlayer sPlayer) {
            int slot = hand == InteractionHand.MAIN_HAND ? sPlayer.getInventory().selected : 40;
            openPouch(sPlayer, slot, inHand);
            return InteractionResultHolder.success(inHand);
        } else {
            return InteractionResultHolder.pass(inHand);
        }
    }

    public static void openPouch(ServerPlayer player, int slot, ItemStack pouchStack) {
        ScavengerPouchProvider provider = new ScavengerPouchProvider(player, slot, pouchStack);
        NetworkHooks.openGui(player, provider, provider.extraDataWriter());
    }

    public static OverSizedInventory getInventory(ItemStack stack) {
        return getInventory(stack, player -> true);
    }

    public static OverSizedInventory getInventory(ItemStack stack, Predicate<Player> stillValid) {
        if (!(stack.getItem() instanceof ItemScavengerPouch)) {
            return OverSizedInventory.EMPTY;
        }

        List<Item> scavItems = getActiveScavengerItems();
        if (scavItems.isEmpty()) {
            return OverSizedInventory.EMPTY;
        }

        NonNullList<OverSizedItemStack> contents =
            NonNullList.withSize(scavItems.size(), OverSizedItemStack.EMPTY);

        for (OverSizedItemStack stored : getStoredStacks(stack)) {
            if (!stored.isEmpty()) {
                OptionalInt slot = findSlot(scavItems, stored.stack().getItem());
                if (slot.isPresent()) {
                    int i = slot.getAsInt();
                    OverSizedItemStack existing = contents.get(i);
                    contents.set(
                        i,
                        existing.isEmpty()
                            ? stored
                            : new OverSizedItemStack(
                                existing.stack(),
                                existing.amount() + stored.amount()
                            )
                    );
                }
            }
        }

        BiPredicate<Integer, ItemStack> canInsert =
            (slot, insert) -> insert.is(scavItems.get(slot));

        return new ScavengerInventory(
            contents,
            scavItems,
            stacks -> setStoredStacks(stack, stacks),
            stillValid,
            canInsert
        );
    }

    public static boolean interceptPlayerInventoryItemAddition(Inventory playerInventory, ItemStack toAdd) {
        if (!(toAdd.getItem() instanceof BasicScavengerItem)) {
            return false;
        } else {
            Player player = playerInventory.player;
            ItemStack scavPouch = ItemStack.EMPTY;

            for (int slot = 0; slot < playerInventory.getContainerSize(); slot++) {
                ItemStack invStack = playerInventory.getItem(slot);
                if (invStack.is(ModItems.SCAVENGER_POUCH_ITEM)) {
                    scavPouch = invStack;
                    break;
                }
            }

            return !scavPouch.isEmpty() && scavPouch.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).map(handler -> {
                for (int slotx = 0; slotx < handler.getSlots(); slotx++) {
                    if (handler.insertItem(slotx, toAdd, true).isEmpty()) {
                        ItemStack remainder = handler.insertItem(slotx, toAdd, false);
                        toAdd.setCount(remainder.getCount());
                        break;
                    }
                }

                return toAdd.isEmpty();
            }).orElse(false);
        }
    }

    public static List<OverSizedItemStack> getStoredStacks(ItemStack stack) {
        List<OverSizedItemStack> result = new ArrayList<>();
        if (!(stack.getItem() instanceof ItemScavengerPouch)) return result;

        CompoundTag tag = stack.getOrCreateTag();
        ListTag list = tag.getList("stacks", 10);

        int size = Math.max(list.size(), getActiveScavengerItems().size());
        for (int i = 0; i < size; i++) {
            result.add(OverSizedItemStack.deserialize(list.getCompound(i)));
        }

        return result;
    }

    public static void setStoredStacks(ItemStack stack, List<OverSizedItemStack> stacks) {
        if (!(stack.getItem() instanceof ItemScavengerPouch)) return;

        ListTag list = new ListTag();
        stacks.forEach(s -> list.add(s.serialize()));
        stack.getOrCreateTag().put("stacks", list);
    }

    private static List<Item> getActiveScavengerItems() {
        return ForgeRegistries.ITEMS.getValues().stream()
            .filter(item -> item instanceof BasicScavengerItem)
            .sorted(Comparator.comparing(ForgeRegistryEntry::getRegistryName))
            .map(Item.class::cast)
            .toList();
    }

    private static OptionalInt findSlot(List<Item> items, Item item) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) == item) return OptionalInt.of(i);
        }
        return OptionalInt.empty();
    }

    public static NonNullSupplier<IItemHandler> getInventorySupplier(ItemStack stack) {
        return () -> {
            OverSizedInventory inv = getInventory(stack);
            return inv instanceof ScavengerInventory si
                ? new ScavengerInvWrapper(si)
                : new OverSizedInvWrapper(inv);
        };
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(
                @Nonnull Capability<T> cap,
                @Nullable Direction side
            ) {
                return cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
                    ? LazyOptional.of(getInventorySupplier(stack)).cast()
                    : LazyOptional.empty();
            }
        };
    }

    @Override
    public void appendHoverText(
        ItemStack stack,
        @Nullable Level level,
        List<Component> tooltip,
        TooltipFlag flag
    ) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (!stack.hasTag()) return;

        tooltip.add(TextComponent.EMPTY);
        tooltip.add(new TextComponent("Scavenger Items:").withStyle(ChatFormatting.GRAY));

        for (OverSizedItemStack s : getStoredStacks(stack)) {
            if (!s.isEmpty()) {
                tooltip.add(
                    new TextComponent(
                        s.amount() + "x "
                            + s.stack().getHoverName().getString()
                    ).withStyle(ChatFormatting.DARK_GRAY)
                );
            }
        }
    }

    private static class ScavengerInvWrapper extends OverSizedInvWrapper {
        private final ScavengerInventory inv;

        public ScavengerInvWrapper(ScavengerInventory inv) {
            super(inv);
            this.inv = inv;
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (!stack.is(inv.getItems().get(slot))) return stack;
            return super.insertItem(slot, stack, simulate);
        }
    }

    private static class ScavengerInventory extends OverSizedInventory.FilteredInsert {
        private final List<Item> items;

        public ScavengerInventory(
            NonNullList<OverSizedItemStack> contents,
            List<Item> items,
            Consumer<NonNullList<OverSizedItemStack>> onChange,
            Predicate<Player> stillValid,
            BiPredicate<Integer, ItemStack> canInsert
        ) {
            super(items.size(), onChange, stillValid, canInsert);
            this.items = items;
            for (int i = 0; i < contents.size(); i++) {
                this.setOverSizedStack(i, contents.get(i));
            }
        }

        public List<Item> getItems() {
            return items;
        }
    }
}
