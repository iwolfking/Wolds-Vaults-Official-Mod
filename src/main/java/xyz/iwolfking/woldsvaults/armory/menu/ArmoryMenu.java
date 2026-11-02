package xyz.iwolfking.woldsvaults.armory.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import iskallia.vault.container.slot.spi.IGhostSlot;
import iskallia.vault.container.spi.AbstractElementContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import top.theillusivec4.curios.api.CuriosApi;
import xyz.iwolfking.woldsvaults.armory.ArmoryPage;
import xyz.iwolfking.woldsvaults.armory.ArmoryRegistry;
import xyz.iwolfking.woldsvaults.armory.ArmoryTab;
import xyz.iwolfking.woldsvaults.armory.ArmoryViewInfo;
import xyz.iwolfking.woldsvaults.armory.SlotKeys;
import xyz.iwolfking.woldsvaults.armory.block.ArmoryBlockEntity;
import xyz.iwolfking.woldsvaults.armory.service.ArmoryService;

import javax.annotation.Nullable;

/**
 * The armory's container menu for the Gear, Inventory and Excess tabs. Slot order: the 54 page slots
 * ({@link SlotKeys} order), 128 excess slots, 2 upgrade slots, then the player's 27 inventory and 9 hotbar slots.
 * The tab is fixed per opening (tabs reopen the menu, as the H screen does), so slots not shown on this tab are
 * parked off screen and skipped by shift-click.
 */
public class ArmoryMenu extends AbstractElementContainer {
    public static final int PAGE_START = 0;
    public static final int EXCESS_START = PAGE_START + 54;
    public static final int UPGRADE_START = EXCESS_START + ArmoryBlockEntity.EXCESS_SLOTS;
    public static final int PLAYER_START = UPGRADE_START + 2;
    public static final int HOTBAR_START = PLAYER_START + 27;
    public static final int END = HOTBAR_START + 9;
    public static final int EXCESS_COLUMNS = 16;
    public static final int EXCESS_VISIBLE_ROWS = 5;
    public static final int CURIO_VISIBLE = 8;
    public static final int BUTTON_STOW = 11;
    public static final int BUTTON_LOAD = 12;
    public static final int BUTTON_MARK = 20;
    public static final int HIDDEN = -10_000;

    private final BlockPos pos;
    private final int page;
    private final ArmoryTab tab;
    @Nullable
    private final ArmoryBlockEntity armory;
    private ArmoryViewInfo info;
    private ArmoryClientState state = ArmoryClientState.EMPTY;
    private int curioScroll;
    private int excessScroll;
    private int slotOffsetY;

    private ArmoryMenu(int windowId, Inventory inventory, ArmoryViewInfo info, @Nullable ArmoryBlockEntity armory,
                       Container pageContainer, IItemHandlerModifiable excess, IItemHandlerModifiable upgrades) {
        super(ArmoryRegistry.ARMORY_MENU, windowId, inventory.player);
        this.pos = info.pos();
        this.page = info.page();
        this.tab = info.tab();
        this.armory = armory;
        this.info = info;
        for (int i = 0; i < SlotKeys.size(); i++) {
            this.addSlot(new PageSlot(pageContainer, i));
        }
        for (int i = 0; i < ArmoryBlockEntity.EXCESS_SLOTS; i++) {
            this.addSlot(new ViewSlot(excess, i, ArmoryTab.EXCESS));
        }
        for (int i = 0; i < 2; i++) {
            this.addSlot(new ViewSlot(upgrades, i, ArmoryTab.INVENTORY));
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(inventory, 9 + row * 9 + column, 8 + column * 18, 102 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(inventory, column, 8 + column * 18, 160));
        }
        this.layout();
    }

    /** Server side: slots read and write the block entity's page, excess and upgrades directly. */
    public static ArmoryMenu server(int windowId, Inventory inventory, ArmoryBlockEntity armory, ArmoryViewInfo info) {
        return new ArmoryMenu(windowId, inventory, info, armory, new PageContainer(armory, info.page()), armory.excess(), armory.upgrades());
    }

    /** Client side: plain containers filled by the vanilla slot sync. */
    public static ArmoryMenu client(int windowId, Inventory inventory, FriendlyByteBuf buf) {
        ArmoryViewInfo info = ArmoryViewInfo.read(buf);
        return new ArmoryMenu(windowId, inventory, info, null, new SimpleContainer(SlotKeys.size()),
                new ItemStackHandler(ArmoryBlockEntity.EXCESS_SLOTS), new ItemStackHandler(2));
    }

    public BlockPos pos() {
        return this.pos;
    }

    public int page() {
        return this.page;
    }

    public ArmoryTab tab() {
        return this.tab;
    }

    public ArmoryViewInfo info() {
        return this.info;
    }

    public ArmoryClientState state() {
        return this.state;
    }

    public void acceptState(ArmoryViewInfo info, ArmoryClientState state) {
        this.info = info;
        this.state = state;
    }

    public int curioScroll() {
        return this.curioScroll;
    }

    public int excessScroll() {
        return this.excessScroll;
    }

    public void setCurioScroll(int scroll) {
        this.curioScroll = Math.max(0, Math.min(SlotKeys.CURIO_IDS.size() - CURIO_VISIBLE, scroll));
        this.layout();
    }

    /** Shifts every shown slot down by {@code offsetY}, as the H screen does to sit slots under its title window. */
    public void setSlotOffsetY(int offsetY) {
        this.slotOffsetY = offsetY;
        this.layout();
    }

    public void setExcessScroll(int scroll) {
        int rows = ArmoryBlockEntity.EXCESS_SLOTS / EXCESS_COLUMNS;
        this.excessScroll = Math.max(0, Math.min(rows - EXCESS_VISIBLE_ROWS, scroll));
        this.layout();
    }

    /** Places every slot for this menu's tab and scroll positions; slots not on the tab go to {@link #HIDDEN}. */
    private void layout() {
        for (int i = 0; i < SlotKeys.size(); i++) {
            Slot slot = this.slots.get(PAGE_START + i);
            int x = HIDDEN;
            int y = HIDDEN;
            if (this.tab == ArmoryTab.GEAR) {
                if (i < SlotKeys.OFFHAND_INDEX) {
                    x = 8;
                    y = 8 + i * 18;
                } else if (i == SlotKeys.OFFHAND_INDEX) {
                    x = 8;
                    y = 80;
                } else if (i >= SlotKeys.CURIO_START) {
                    int row = i - SlotKeys.CURIO_START - this.curioScroll;
                    if (row >= 0 && row < CURIO_VISIBLE) {
                        x = -20;
                        y = 8 + row * 18;
                    }
                }
            } else if (this.tab == ArmoryTab.INVENTORY) {
                if (i >= SlotKeys.INV_START && i < SlotKeys.CURIO_START) {
                    int index = i - SlotKeys.INV_START;
                    x = 8 + (index % 9) * 18;
                    y = 8 + (index / 9) * 18;
                } else if (i >= SlotKeys.HOTBAR_START && i < SlotKeys.INV_START) {
                    x = 8 + (i - SlotKeys.HOTBAR_START) * 18;
                    y = 70;
                }
            }
            slot.x = x;
            slot.y = y == HIDDEN ? HIDDEN : y + this.slotOffsetY;
        }
        for (int i = 0; i < ArmoryBlockEntity.EXCESS_SLOTS; i++) {
            Slot slot = this.slots.get(EXCESS_START + i);
            int row = i / EXCESS_COLUMNS - this.excessScroll;
            boolean shown = this.tab == ArmoryTab.EXCESS && row >= 0 && row < EXCESS_VISIBLE_ROWS;
            slot.x = shown ? 8 + (i % EXCESS_COLUMNS) * 18 : HIDDEN;
            slot.y = shown ? 8 + row * 18 + this.slotOffsetY : HIDDEN;
        }
        for (int i = 0; i < 2; i++) {
            Slot slot = this.slots.get(UPGRADE_START + i);
            slot.x = this.tab == ArmoryTab.INVENTORY ? 306 + i * 20 : HIDDEN;
            slot.y = this.tab == ArmoryTab.INVENTORY ? 36 + this.slotOffsetY : HIDDEN;
        }
        for (int i = PLAYER_START; i < END; i++) {
            Slot slot = this.slots.get(i);
            int index = i - PLAYER_START;
            slot.y = (index < 27 ? 102 + (index / 9) * 18 : 160) + this.slotOffsetY;
        }
    }

    public boolean isShown(Slot slot) {
        return slot.x != HIDDEN;
    }

    /** Whether a slot belongs to this menu's tab, wherever scrolling has put it. */
    public boolean onTab(int index) {
        if (index >= PLAYER_START) {
            return true;
        }
        if (index >= UPGRADE_START) {
            return this.tab == ArmoryTab.INVENTORY;
        }
        if (index >= EXCESS_START) {
            return this.tab == ArmoryTab.EXCESS;
        }
        int i = index - PAGE_START;
        if (this.tab == ArmoryTab.GEAR) {
            return i <= SlotKeys.OFFHAND_INDEX || i >= SlotKeys.CURIO_START;
        }
        return this.tab == ArmoryTab.INVENTORY && i >= SlotKeys.HOTBAR_START && i < SlotKeys.CURIO_START;
    }

    public static String keyOf(int slotIndex) {
        return SlotKeys.ALL.get(slotIndex - PAGE_START);
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.armory == null) {
            return true;
        }
        return !this.armory.isRemoved() && player.distanceToSqr(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5) <= 64.0;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!(player instanceof ServerPlayer serverPlayer) || this.armory == null) {
            return false;
        }
        if (id == BUTTON_STOW) {
            ArmoryService.stow(serverPlayer, this.armory, this.page);
        } else if (id == BUTTON_LOAD) {
            ArmoryService.load(serverPlayer, this.armory, this.page);
        } else if (id >= BUTTON_MARK && id < BUTTON_MARK + SlotKeys.size()) {
            this.armory.page(this.page).toggleMark(SlotKeys.ALL.get(id - BUTTON_MARK));
            this.armory.setChanged();
        } else {
            return false;
        }
        this.broadcastChanges();
        ArmoryNavigation.sendState(serverPlayer, this, this.armory);
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem() || !this.onTab(index)) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < PLAYER_START) {
            if (!this.moveItemStackTo(stack, PLAYER_START, END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (this.tab == ArmoryTab.EXCESS) {
            if (!this.moveShown(stack, EXCESS_START, UPGRADE_START)) {
                return ItemStack.EMPTY;
            }
        } else if (this.tab == ArmoryTab.INVENTORY) {
            if (!this.moveShown(stack, UPGRADE_START, PLAYER_START) && !this.moveShown(stack, PAGE_START, EXCESS_START)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveShown(stack, PAGE_START, EXCESS_START)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return stack.getCount() == original.getCount() ? ItemStack.EMPTY : original;
    }

    private boolean moveShown(ItemStack stack, int start, int end) {
        boolean moved = false;
        for (int i = start; i < end && !stack.isEmpty(); i++) {
            if (this.onTab(i) && this.moveItemStackTo(stack, i, i + 1, false)) {
                moved = true;
            }
        }
        return moved;
    }

    private class ViewSlot extends SlotItemHandler {
        private final ArmoryTab shownOn;

        ViewSlot(IItemHandlerModifiable handler, int index, ArmoryTab shownOn) {
            super(handler, index, HIDDEN, HIDDEN);
            this.shownOn = shownOn;
        }

        @Override
        public boolean isActive() {
            return ArmoryMenu.this.tab == this.shownOn && this.x != HIDDEN;
        }
    }

    /** A page slot: accepts only what the mirrored player slot would accept. */
    public class PageSlot extends Slot implements IGhostSlot {
        private final String key;

        PageSlot(Container container, int index) {
            super(container, index, HIDDEN, HIDDEN);
            this.key = SlotKeys.ALL.get(index);
        }

        public String key() {
            return this.key;
        }

        @Override
        public boolean isActive() {
            return this.x != HIDDEN;
        }

        @Override
        public int getMaxStackSize() {
            return this.key.startsWith("armor:") || this.key.startsWith("curio:") ? 1 : super.getMaxStackSize();
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return accepts(this.key, stack, ArmoryMenu.this.player);
        }

        @Nullable
        @Override
        public ItemStack getGhostItemStack() {
            if (this.hasItem()) {
                return null;
            }
            ItemStack expected = ArmoryMenu.this.state.expected(this.key);
            return expected == null || expected.isEmpty() ? null : expected;
        }
    }

    public static boolean accepts(String key, ItemStack stack, Player player) {
        if (key.startsWith("armor:")) {
            EquipmentSlot slot = EquipmentSlot.byName(key.substring("armor:".length()));
            return stack.canEquip(slot, player);
        }
        if (key.startsWith("curio:")) {
            return CuriosApi.getCuriosHelper().getCurioTags(stack.getItem()).contains(SlotKeys.curioId(key));
        }
        return true;
    }

    /**
     * The server's view of one page as a container. Taking an item out leaves the page's saved layout alone (the
     * slot then shows a ghost); putting one in records it as the slot's saved item.
     */
    private static final class PageContainer implements Container {
        private final ArmoryBlockEntity armory;
        private final int page;

        PageContainer(ArmoryBlockEntity armory, int page) {
            this.armory = armory;
            this.page = page;
        }

        private ArmoryPage page() {
            return this.armory.page(this.page);
        }

        @Override
        public int getContainerSize() {
            return SlotKeys.size();
        }

        @Override
        public boolean isEmpty() {
            return this.page().storedMap().isEmpty();
        }

        @Override
        public ItemStack getItem(int index) {
            return this.page().stored(SlotKeys.ALL.get(index));
        }

        @Override
        public ItemStack removeItem(int index, int count) {
            ItemStack current = this.getItem(index);
            if (current.isEmpty()) {
                return ItemStack.EMPTY;
            }
            ItemStack taken = current.split(count);
            if (current.isEmpty()) {
                this.page().setStored(SlotKeys.ALL.get(index), ItemStack.EMPTY);
            }
            this.setChanged();
            return taken;
        }

        @Override
        public ItemStack removeItemNoUpdate(int index) {
            ItemStack current = this.getItem(index);
            this.page().setStored(SlotKeys.ALL.get(index), ItemStack.EMPTY);
            return current;
        }

        @Override
        public void setItem(int index, ItemStack stack) {
            String key = SlotKeys.ALL.get(index);
            this.page().setStored(key, stack);
            if (!stack.isEmpty()) {
                this.page().setExpected(key, stack);
            }
            this.setChanged();
        }

        @Override
        public void setChanged() {
            this.armory.setChanged();
        }

        @Override
        public boolean stillValid(Player player) {
            return !this.armory.isRemoved();
        }

        @Override
        public void clearContent() {
            this.page().storedMap().clear();
            this.setChanged();
        }
    }
}
