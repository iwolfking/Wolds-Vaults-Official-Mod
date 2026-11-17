package xyz.iwolfking.woldsvaults.armory.service;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.armory.SlotKeys;
import xyz.iwolfking.woldsvaults.integration.curios.ArmoryCurioSlots;

/** Reads and writes a player's slot by armory slot key. */
final class PlayerSlots {
    private PlayerSlots() {
    }

    static ItemStack get(ServerPlayer player, String key) {
        if (key.startsWith("armor:")) {
            return player.getItemBySlot(EquipmentSlot.byName(key.substring("armor:".length())));
        }
        if (key.equals(SlotKeys.OFFHAND)) {
            return player.getItemBySlot(EquipmentSlot.OFFHAND);
        }
        if (key.startsWith("hotbar:")) {
            return player.getInventory().getItem(Integer.parseInt(key.substring("hotbar:".length())));
        }
        if (key.startsWith("inv:")) {
            return player.getInventory().getItem(9 + Integer.parseInt(key.substring("inv:".length())));
        }
        if (SlotKeys.isCurio(key)) {
            return ArmoryCurioSlots.get(player, key);
        }
        return ItemStack.EMPTY;
    }

    /** Sets the slot; returns false when the player has no such slot or the curio slot refused the item. */
    static boolean set(ServerPlayer player, String key, ItemStack stack) {
        if (key.startsWith("armor:")) {
            player.setItemSlot(EquipmentSlot.byName(key.substring("armor:".length())), stack);
            return true;
        }
        if (key.equals(SlotKeys.OFFHAND)) {
            player.setItemSlot(EquipmentSlot.OFFHAND, stack);
            return true;
        }
        if (key.startsWith("hotbar:")) {
            player.getInventory().setItem(Integer.parseInt(key.substring("hotbar:".length())), stack);
            return true;
        }
        if (key.startsWith("inv:")) {
            player.getInventory().setItem(9 + Integer.parseInt(key.substring("inv:".length())), stack);
            return true;
        }
        if (SlotKeys.isCurio(key)) {
            return ArmoryCurioSlots.set(player, key, stack);
        }
        return false;
    }

}
