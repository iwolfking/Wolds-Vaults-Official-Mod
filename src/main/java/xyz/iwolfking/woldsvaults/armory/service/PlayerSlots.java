package xyz.iwolfking.woldsvaults.armory.service;

import iskallia.vault.integration.IntegrationCurios;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.Tuple;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import xyz.iwolfking.woldsvaults.armory.SlotKeys;

import java.util.List;
import java.util.Map;

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
        if (key.startsWith("curio:") && hasCurioSlot(player, key)) {
            return IntegrationCurios.getCurioItemStack(player, SlotKeys.curioId(key), SlotKeys.curioIndex(key));
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
        if (key.startsWith("curio:") && hasCurioSlot(player, key)) {
            return IntegrationCurios.setCurioItemStack(player, stack, SlotKeys.curioId(key), SlotKeys.curioIndex(key));
        }
        return false;
    }

    static boolean hasCurioSlot(ServerPlayer player, String key) {
        Map<String, List<Tuple<ItemStack, Integer>>> curios = IntegrationCurios.getCuriosItemStacks(player);
        List<Tuple<ItemStack, Integer>> slots = curios.get(SlotKeys.curioId(key));
        if (slots == null) {
            return false;
        }
        int index = SlotKeys.curioIndex(key);
        return slots.stream().anyMatch(tuple -> tuple.getB() == index);
    }

    /** Whether the curio's own equip rule (deck level minimum, etc.) lets {@code player} wear {@code stack}. */
    static boolean canEquipCurio(ServerPlayer player, String key, ItemStack stack) {
        SlotContext context = new SlotContext(SlotKeys.curioId(key), player, SlotKeys.curioIndex(key), false, true);
        return CuriosApi.getCuriosHelper().getCurio(stack).map(curio -> curio.canEquip(context)).orElse(true);
    }
}
