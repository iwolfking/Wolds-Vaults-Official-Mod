package xyz.iwolfking.woldsvaults.items.scavenger_pouch.menu;

import iskallia.vault.container.oversized.OverSizedInventory;
import iskallia.vault.container.oversized.OverSizedItemStack;
import iskallia.vault.container.oversized.OverSizedSlotContainer;
import iskallia.vault.container.oversized.OverSizedTabSlot;
import iskallia.vault.container.slot.ConditionalReadSlot;
import iskallia.vault.item.BasicScavengerItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

import org.jetbrains.annotations.NotNull;
import xyz.iwolfking.woldsvaults.init.ModContainers;
import xyz.iwolfking.woldsvaults.items.ItemScavengerPouch;

public class ScavengerPouchContainer extends OverSizedSlotContainer {
    private static final int SCAV_COLUMNS = 9;
    private static final int SLOT_SIZE = 18;
    private static final int SCAV_START_X = 8;
    private static final int SCAV_START_Y = 25;
    private static final int SCAV_PADDING_BOTTOM = 6;

    private int scavRows = 1;
    private final Inventory inventory;
    private final int scavPouchSlot;

    private final List<Runnable> slotListeners = new ArrayList<>();
    private final List<ScavengerItemSlot> scavItemSlots = new ArrayList<>();

    public ScavengerPouchContainer(int id, Inventory playerInventory, int slot) {
        super(ModContainers.SCAVENGER_POUCH_CONTAINER, id, playerInventory.player);
        this.inventory = playerInventory;
        this.scavPouchSlot = slot;

        if (!playerInventory.player.level.isClientSide) {
            this.slotListeners.add(this::updateScavengerItems);
        }

        this.getScavengerPouchStack().ifPresent(stack ->
            playerInventory.player
                .getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                .ifPresent(inv -> {
                    this.initPlayerSlots(inv);
                    this.initScavengerSlots(stack);
                })
        );
    }

    public List<ScavengerItemSlot> getScavItemSlots() {
        return this.scavItemSlots;
    }

    private void initPlayerSlots(IItemHandler playerInventory) {
        int invStartY = getPlayerInventoryStartY() + 112;

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new ConditionalReadSlot(
                        playerInventory,
                        col + row * 9 + 9,
                        8 + col * 18,
                        invStartY + (row * 18),
                        this::canAccess
                ));
            }
        }

        int hotbarY = invStartY + 54;

        for (int hotbar = 0; hotbar < 9; ++hotbar) {
            this.addSlot(new ConditionalReadSlot(
                    playerInventory,
                    hotbar,
                    8 + hotbar * 18,
                    hotbarY,
                    this::canAccess
            ));
        }
    }


    public int getScavengerRows() {
        return this.scavRows;
    }

    public int getPlayerInventoryStartY() {
        return SCAV_START_Y + scavRows * SLOT_SIZE + SCAV_PADDING_BOTTOM + 4;
    }

    private void initScavengerSlots(ItemStack pouchStack) {
        List<OverSizedItemStack> stored = ItemScavengerPouch.getStoredStacks(pouchStack);

        List<Item> scavItems = ForgeRegistries.ITEMS.getValues().stream()
                .filter(item -> item instanceof BasicScavengerItem)
                .sorted(Comparator.comparing(ForgeRegistryEntry::getRegistryName))
                .toList();

        this.scavRows = (int) Math.ceil(scavItems.size() / (double) SCAV_COLUMNS);

        OverSizedInventory buffer = new OverSizedInventory(
                scavItems.size(),
                stacks -> {},
                player -> true
        );

        for (int index = 0; index < scavItems.size(); index++) {
            Item scavItem = scavItems.get(index);

            OverSizedItemStack existing =
                    stored.stream()
                            .filter(s -> s.stack().is(scavItem))
                            .findFirst()
                            .orElse(OverSizedItemStack.EMPTY);

            buffer.setOverSizedStack(index, existing);

            int col = index % SCAV_COLUMNS;
            int row = index / SCAV_COLUMNS;

            int x = SCAV_START_X + col * SLOT_SIZE;
            int y = SCAV_START_Y + row * SLOT_SIZE;

            this.addScavSlot(buffer, new ItemStack(scavItem), index, x, y);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        var slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();

            if (scavItemSlots.contains(slot)) {
                ScavengerItemSlot scavSlot = (ScavengerItemSlot) slot;
                OverSizedInventory inventory = (OverSizedInventory) scavSlot.container;

                OverSizedItemStack stored = inventory.getOverSizedContents().get(slot.index);
                if (stored.isEmpty() || stored.amount() <= 0) return ItemStack.EMPTY;

                int giveAmount = Math.min(stack.getMaxStackSize(), stored.amount());

                ItemStack out = stack.copy();
                out.setCount(giveAmount);

                if (!this.moveItemStackTo(out, 0, player.getInventory().items.size(), true)) {
                    return ItemStack.EMPTY;
                }

                OverSizedItemStack updated = new OverSizedItemStack(stored.stack(), stored.amount() - giveAmount);
                inventory.setOverSizedStack(slot.index, updated);

                slot.set(stack);
                return out;
            }

            boolean moved = false;
            for (ScavengerItemSlot scavSlot : scavItemSlots) {
                if (scavSlot.mayPlace(stack)) {
                    moved = this.moveItemStackTo(stack, scavSlot.index, scavSlot.index + 1, false);
                    if (moved) break;
                }
            }

            if (!moved) return ItemStack.EMPTY;

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return result;
    }





    private void addScavSlot(Container container, ItemStack keyType, int index, int x, int y) {
        ScavengerItemSlot slot = new ScavengerItemSlot(container, index, x, y, keyType);
        slot.addListener(() -> this.slotListeners.forEach(Runnable::run));
        this.scavItemSlots.add(slot);
        this.addSlot(slot);
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return this.hasScavPouch();
    }

    public boolean canAccess(int slot, ItemStack stack) {
        return this.hasScavPouch() && !(stack.getItem() instanceof ItemScavengerPouch);
    }

    public boolean hasScavPouch() {
        ItemStack stack = this.inventory.getItem(this.scavPouchSlot);
        return !stack.isEmpty() && stack.getItem() instanceof ItemScavengerPouch;
    }

    public Optional<ItemStack> getScavengerPouchStack() {
        return this.hasScavPouch()
            ? Optional.of(this.inventory.getItem(this.scavPouchSlot))
            : Optional.empty();
    }

    private void updateScavengerItems() {
        this.getScavengerPouchStack().ifPresent(pouch -> {
            List<OverSizedItemStack> stacks = new ArrayList<>();
            for (ScavengerItemSlot slot : this.scavItemSlots) {
                stacks.add(OverSizedItemStack.of(slot.getItem()));
            }
            ItemScavengerPouch.setStoredStacks(pouch, stacks);
        });
    }

    public static class ScavengerItemSlot extends OverSizedTabSlot {
        private final ItemStack scavStack;

        public ScavengerItemSlot(Container container, int index, int x, int y, ItemStack scavStack) {
            super(container, index, x, y);
            this.scavStack = scavStack;
            this.setFilter(stack -> stack.sameItem(this.scavStack));
        }

        public ItemStack getScavStack() {
            return scavStack;
        }
    }
}
