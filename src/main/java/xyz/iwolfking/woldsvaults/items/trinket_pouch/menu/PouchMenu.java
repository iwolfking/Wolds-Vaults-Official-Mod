package xyz.iwolfking.woldsvaults.items.trinket_pouch.menu;

import xyz.iwolfking.woldsvaults.init.ModContainers;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.network.packets.ClientboundTrinketPouchStatePacket;
import xyz.iwolfking.woldsvaults.items.trinket_pouch.PouchCapability;
import xyz.iwolfking.woldsvaults.items.trinket_pouch.PouchContents;
import xyz.iwolfking.woldsvaults.items.trinket_pouch.PouchMigration;
import xyz.iwolfking.woldsvaults.api.util.PouchHelper;
import xyz.iwolfking.woldsvaults.items.trinket_pouch.PouchRuntime;
import xyz.iwolfking.woldsvaults.items.trinket_pouch.PouchStartRoom;
import iskallia.vault.core.vault.Vault;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.network.NetworkHooks;

public final class PouchMenu extends AbstractContainerMenu {
    public static final int EQUIPPED = -1;
    public static final int SAVE_PRESET = 0;
    public static final int APPLY_PRESET = SAVE_PRESET + PouchContents.PRESET_COUNT;
    public static final int AUTO_REPLACE = APPLY_PRESET + PouchContents.PRESET_COUNT;
    public static final int TOGGLE_TRINKET = 100;
    private final Player owner;
    private final int pouchSlot;
    private final ItemStack pouch;
    private final PouchContents contents;
    private CompoundTag lastState;
    private String status = "";
    private Feedback feedback = Feedback.NONE;
    private int statusPreset = -1;
    private int statusRevision;
    private long statusReceivedAt;

    public enum Feedback { NONE, SAVED, APPLIED, RENAMED, ERROR }
    private boolean storedView;
    private boolean locked;
    private boolean startRoom;
    private boolean receivingSlotSync;

    public static void open(ServerPlayer player, int pouchSlot) {
        if (player.containerMenu instanceof PouchMenu || !player.containerMenu.getCarried().isEmpty()
                || !player.containerMenu.stillValid(player) || pouchSlot < EQUIPPED
                || pouchSlot >= player.getInventory().getContainerSize()) {
            return;
        }
        if (pouchSlot != EQUIPPED && player.containerMenu.slots.stream().noneMatch(slot ->
                slot.container == player.getInventory() && slot.getSlotIndex() == pouchSlot && slot.mayPickup(player))) {
            return;
        }
        ItemStack pouch = locate(player, pouchSlot);
        if (!PouchHelper.isPouch(pouch)) {
            player.displayClientMessage(new TextComponent("Equip a trinket pouch or hold one to open it."), true);
            return;
        }
        if (!PouchCapability.get(pouch).isReadable()) {
            player.displayClientMessage(new TranslatableComponent("gui.woldsvaults.pouch.unreadable"), true);
            return;
        }
        PouchMigration.stored(pouch);
        PouchMigration.equipped(player);
        NetworkHooks.openGui(player, new SimpleMenuProvider((id, inventory, user) -> new PouchMenu(id, inventory, pouchSlot, pouch),
                pouch.getHoverName()), buffer -> {
                    buffer.writeInt(pouchSlot);
                    buffer.writeItem(pouch);
                });
    }

    public static PouchMenu client(int id, Inventory inventory, FriendlyByteBuf buffer) {
        return new PouchMenu(id, inventory, buffer.readInt(), buffer.readItem());
    }

    private PouchMenu(int id, Inventory inventory, int pouchSlot, ItemStack pouch) {
        super(ModContainers.TRINKET_POUCH_CONTAINER, id);
        this.owner = inventory.player;
        this.pouchSlot = pouchSlot;
        this.pouch = pouch;
        this.contents = PouchCapability.get(pouch);
        this.locked = PouchHelper.locked(owner);
        for (int index = 0; index < PouchContents.SIZE; index++) {
            addSlot(new SlotItemHandler(contents, index, PouchLayout.GRID_X + 1 + index % 9 * 18,
                    PouchLayout.GRID_Y + 1 + index / 9 * 18) {
                @Override
                public void set(@Nonnull ItemStack stack) {
                    if (receivingSlotSync) {
                        contents.synchronizeStackInSlot(getSlotIndex(), stack);
                        setChanged();
                    } else {
                        super.set(stack);
                    }
                }

                @Override
                public boolean mayPlace(ItemStack stack) {
                    return !isLocked() && super.mayPlace(stack);
                }

                @Override
                public boolean mayPickup(Player player) {
                    return !isLocked() && super.mayPickup(player);
                }

                @Override
                public int getMaxStackSize(ItemStack stack) {
                    return 1;
                }

                @Override
                public boolean isActive() {
                    return !owner.level.isClientSide || storedView;
                }
            });
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addInventorySlot(inventory, column + row * 9 + 9, PouchLayout.GRID_X + 1 + column * 18, PouchLayout.INVENTORY_Y + 1 + row * 18);
            }
        }
        for (int column = 0; column < 9; column++) {
            addInventorySlot(inventory, column, PouchLayout.GRID_X + 1 + column * 18, PouchLayout.HOTBAR_Y + 1);
        }
    }

    private void addInventorySlot(Inventory inventory, int index, int x, int y) {
        addSlot(new Slot(inventory, index, x, y) {
            @Override
            public boolean isActive() {
                return !owner.level.isClientSide || storedView;
            }

            @Override
            public boolean mayPickup(Player player) {
                return index != pouchSlot && !isLocked() && super.mayPickup(player);
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return index != pouchSlot && !isLocked() && super.mayPlace(stack);
            }
        });
    }

    private static ItemStack locate(Player player, int slot) {
        if (slot == EQUIPPED) {
            return PouchHelper.equipped(player);
        }
        return slot >= 0 && slot < player.getInventory().getContainerSize() ? player.getInventory().getItem(slot) : ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player == owner && player.isAlive() && (owner.level.isClientSide || locate(player, pouchSlot) == pouch);
    }

    @Override
    public void clicked(int slot, int button, ClickType click, Player player) {
        if (!stillValid(player) || isLocked() || click == ClickType.SWAP && button == pouchSlot) {
            return;
        }
        super.clicked(slot, button, click, player);
        if (!owner.level.isClientSide) {
            PouchRuntime.update(owner);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (!stillValid(player) || isLocked() || index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot source = slots.get(index);
        if (!source.hasItem() || !source.mayPickup(player)) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = source.getItem();
        ItemStack previous = stack.copy();
        boolean moved = index < PouchContents.SIZE
                ? moveItemStackTo(stack, PouchContents.SIZE, slots.size(), true)
                : moveItemStackTo(stack, 0, PouchContents.SIZE, false);
        if (!moved) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            source.set(ItemStack.EMPTY);
        } else {
            source.setChanged();
        }
        source.onTake(player, stack);
        return previous;
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        boolean loadoutButton = button >= TOGGLE_TRINKET && button < TOGGLE_TRINKET + PouchContents.SIZE
                || button >= SAVE_PRESET && button < APPLY_PRESET + PouchContents.PRESET_COUNT;
        if (!stillValid(player) || player.level.isClientSide || isLocked() && !(loadoutButton && inStartRoom())) {
            return false;
        }
        beginStatus(button >= SAVE_PRESET && button < APPLY_PRESET + PouchContents.PRESET_COUNT
                ? button % PouchContents.PRESET_COUNT : -1);
        if (button >= TOGGLE_TRINKET && button < TOGGLE_TRINKET + PouchContents.SIZE) {
            int index = button - TOGGLE_TRINKET;
            List<Integer> active = new ArrayList<>(contents.activeIndices());
            if (!active.remove(Integer.valueOf(index))) {
                active.add(index);
            }
            apply(active);
        } else if (button >= SAVE_PRESET && button < SAVE_PRESET + PouchContents.PRESET_COUNT) {
            contents.savePreset(button - SAVE_PRESET);
            feedback = Feedback.SAVED;
        } else if (button >= APPLY_PRESET && button < APPLY_PRESET + PouchContents.PRESET_COUNT) {
            int preset = button - APPLY_PRESET;
            List<Integer> available = contents.resolvePreset(preset);
            apply(isLocked() ? PouchStartRoom.keepTimeTrinkets(contents, available) : available);
            if (status.isEmpty()) {
                contents.markPresetApplied(preset);
                feedback = Feedback.APPLIED;
            }
        } else if (button == AUTO_REPLACE) {
            contents.toggleAutoReplace();
        } else {
            return false;
        }
        PouchRuntime.update(player);
        broadcastChanges();
        return true;
    }

    private void apply(List<Integer> active) {
        if (isLocked()) {
            Optional<Vault> vault = isEquipped() ? PouchHelper.startRoomVault(owner) : Optional.empty();
            status = vault.map(startRoomVault -> PouchStartRoom.apply((ServerPlayer) owner, startRoomVault, pouch, contents, active))
                    .orElse("This pouch is locked inside vaults");
        } else {
            status = PouchHelper.validate(pouch, contents, active, owner);
            if (status.isEmpty()) {
                contents.setActive(active);
            }
        }
        if (!status.isEmpty()) {
            feedback = Feedback.ERROR;
        }
    }

    private void beginStatus(int preset) {
        status = "";
        feedback = Feedback.NONE;
        statusPreset = preset;
        statusRevision++;
    }

    public boolean renamePreset(Player player, int preset, String name) {
        if (!stillValid(player) || isLocked() && !inStartRoom() || player.level.isClientSide || preset < 0 || preset >= PouchContents.PRESET_COUNT) {
            return false;
        }
        beginStatus(preset);
        try {
            contents.renamePreset(preset, name);
            feedback = Feedback.RENAMED;
        } catch (IllegalArgumentException exception) {
            status = exception.getMessage();
            feedback = Feedback.ERROR;
        }
        broadcastChanges();
        return true;
    }

    @Override
    public void setItem(int slot, int stateId, @Nonnull ItemStack stack) {
        receivingSlotSync = owner.level.isClientSide;
        try {
            super.setItem(slot, stateId, stack);
        } finally {
            receivingSlotSync = false;
        }
    }

    @Override
    public void initializeContents(int stateId, @Nonnull List<ItemStack> stacks, @Nonnull ItemStack carried) {
        receivingSlotSync = owner.level.isClientSide;
        try {
            super.initializeContents(stateId, stacks, carried);
        } finally {
            receivingSlotSync = false;
        }
    }

    @Override
    public void sendAllDataToRemote() {
        super.sendAllDataToRemote();
        sendPouchState(true);
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        sendPouchState(false);
    }

    private void sendPouchState(boolean force) {
        if (owner instanceof ServerPlayer player) {
            CompoundTag state = contents.serializeNBT();
            state.putBoolean("Locked", PouchHelper.locked(owner));
            state.putBoolean("StartRoom", inStartRoom());
            state.putString("Status", status);
            state.putInt("StatusPreset", statusPreset);
            state.putInt("StatusRevision", statusRevision);
            state.putInt("Feedback", feedback.ordinal());
            if (force || !state.equals(lastState)) {
                lastState = state;
                ModNetwork.sendToClient(new ClientboundTrinketPouchStatePacket(containerId, state), player);
            }
        }
    }

    public void receiveState(CompoundTag state) {
        if (!owner.level.isClientSide) {
            throw new IllegalStateException("Client pouch state must not mutate server storage");
        }
        contents.deserializeNBT(state);
        locked = state.getBoolean("Locked");
        startRoom = state.getBoolean("StartRoom");
        status = state.getString("Status");
        statusPreset = state.getInt("StatusPreset");
        feedback = Feedback.values()[state.getInt("Feedback")];
        int revision = state.getInt("StatusRevision");
        if (revision != statusRevision) {
            statusRevision = revision;
            statusReceivedAt = System.nanoTime();
        }
    }

    public PouchContents contents() { return contents; }
    public ItemStack pouch() { return pouch; }
    public boolean isEquipped() { return pouchSlot == EQUIPPED; }
    public String status() { return status; }
    public Feedback feedback() { return feedback; }
    public int statusPreset() { return statusPreset; }
    public int statusRevision() { return statusRevision; }
    public boolean hasRecentFeedback() {
        return feedback != Feedback.NONE && System.nanoTime() - statusReceivedAt < 3_000_000_000L;
    }
    public boolean isLocked() { return owner.level.isClientSide ? locked : PouchHelper.locked(owner); }
    public boolean inStartRoom() { return owner.level.isClientSide ? startRoom : isEquipped() && PouchHelper.startRoomVault(owner).isPresent(); }
    public boolean canChangeLoadout() { return !isLocked() || inStartRoom(); }
    public boolean storedView() { return storedView; }
    public void setStoredView(boolean value) { storedView = value; }
}
