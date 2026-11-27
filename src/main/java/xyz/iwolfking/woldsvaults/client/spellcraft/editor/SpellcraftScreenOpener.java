package xyz.iwolfking.woldsvaults.client.spellcraft.editor;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.ScreenEvent;
import xyz.iwolfking.woldsvaults.spellcraft.item.SpellcraftItemData;

/**
 * Opens the spell editor when a spellcraft item in a player inventory slot is right-clicked with nothing on the cursor.
 * The creative inventory is skipped because its slots do not map to player inventory indices.
 */
public final class SpellcraftScreenOpener {
    private SpellcraftScreenOpener() {
    }

    /** Cancels the click and opens the editor when it is a right-click on a spellcraft item; returns whether it did. */
    public static boolean tryOpen(ScreenEvent.MouseClickedEvent.Pre event) {
        if (event.getButton() != 1 || !(event.getScreen() instanceof AbstractContainerScreen<?> screen)
                || screen instanceof CreativeModeInventoryScreen) {
            return false;
        }
        Slot slot = screen.getSlotUnderMouse();
        if (slot == null || !(slot.container instanceof Inventory) || !screen.getMenu().getCarried().isEmpty()) {
            return false;
        }
        ItemStack stack = slot.getItem();
        if (!SpellcraftItemData.canCarry(stack) || !SpellcraftItemData.has(stack)) {
            return false;
        }
        event.setCanceled(true);
        SpellcraftScreen.open(screen, slot.getContainerSlot());
        return true;
    }
}
