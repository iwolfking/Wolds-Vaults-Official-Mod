package xyz.iwolfking.woldsvaults.blocks.containers;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.entities.extraction.ExtractorEntity;
import xyz.iwolfking.woldsvaults.extraction.jobs.GreedCardUpgrades;
import xyz.iwolfking.woldsvaults.init.ModContainers;

import javax.annotation.Nullable;

/**
 * The Greed Infuser's insert screen: the armed rig's card slots (only upgradable greed cards go in), the player's
 * inventory and a Start button that the server turns into a running job.
 */
public class ExtractorCardMenu extends AbstractContainerMenu {
    public static final int BUTTON_START = 0;
    public static final int COLUMNS = 9;
    public static final int LEFT = 8;
    public static final int CARD_TOP = 34;
    public static final int FOOTER = 52;

    private final Container cards;
    private final int cardSlots;
    private final String targetClass;
    @Nullable
    private final ExtractorEntity entity;
    private final int playerTop;

    private ExtractorCardMenu(int id, Inventory playerInventory, Container cards, int cardSlots, String targetClass, @Nullable ExtractorEntity entity) {
        super(ModContainers.EXTRACTOR_CARD_CONTAINER, id);
        this.cards = cards;
        this.cardSlots = Math.max(1, Math.min(cards.getContainerSize(), cardSlots));
        this.targetClass = targetClass;
        this.entity = entity;
        for (int i = 0; i < this.cardSlots; i++) {
            this.addSlot(new CardSlot(cards, i, LEFT + (i % COLUMNS) * 18, CARD_TOP + (i / COLUMNS) * 18, targetClass));
        }
        this.playerTop = CARD_TOP + this.getCardRows() * 18 + FOOTER;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, LEFT + col * 18, this.playerTop + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, LEFT + col * 18, this.playerTop + 58));
        }
    }

    public static ExtractorCardMenu create(int id, Inventory playerInventory, FriendlyByteBuf buffer) {
        buffer.readVarInt();
        int slots = buffer.readVarInt();
        String target = buffer.readUtf();
        return new ExtractorCardMenu(id, playerInventory, new SimpleContainer(ExtractorEntity.CARD_CONTAINER_SIZE), slots, target, null);
    }

    public static ExtractorCardMenu server(int id, Inventory playerInventory, ExtractorEntity entity) {
        return new ExtractorCardMenu(id, playerInventory, entity.getCards(), entity.getCardSlots(), entity.getCardTargetClass(), entity);
    }

    public boolean isFor(ExtractorEntity extractor) {
        return this.entity == extractor;
    }

    public int getCardSlots() {
        return this.cardSlots;
    }

    public int getCardRows() {
        return (this.cardSlots + COLUMNS - 1) / COLUMNS;
    }

    public String getTargetClass() {
        return this.targetClass;
    }

    public int getPlayerTop() {
        return this.playerTop;
    }

    public int filledSlots() {
        int filled = 0;
        for (int i = 0; i < this.cardSlots; i++) {
            if (!this.cards.getItem(i).isEmpty()) {
                filled++;
            }
        }
        return filled;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != BUTTON_START || this.entity == null || !(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        return this.entity.start(serverPlayer);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < this.cardSlots) {
            if (!this.moveItemStackTo(stack, this.cardSlots, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (GreedCardUpgrades.refusal(stack, this.targetClass) != null || !this.moveItemStackTo(stack, 0, this.cardSlots, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.entity == null || this.entity.isUsableBy(player);
    }

    static class CardSlot extends Slot {
        private final String targetClass;

        CardSlot(Container container, int index, int x, int y, String targetClass) {
            super(container, index, x, y);
            this.targetClass = targetClass;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return GreedCardUpgrades.refusal(stack, this.targetClass) == null;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
