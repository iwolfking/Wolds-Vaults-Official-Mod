package xyz.iwolfking.woldsvaults.builds.state;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.gear.data.AttributeGearData;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.integration.IntegrationCurios;
import iskallia.vault.item.CompanionItem;
import iskallia.vault.item.gear.VaultCharmItem;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.builds.BuildException;
import xyz.iwolfking.woldsvaults.builds.BuildReport;
import xyz.iwolfking.woldsvaults.builds.Json;
import xyz.iwolfking.woldsvaults.builds.codec.ItemCodec;
import xyz.iwolfking.woldsvaults.gods.GodPiety;
import xyz.iwolfking.woldsvaults.gods.charms.MythicCharmRolls;
import xyz.iwolfking.woldsvaults.items.gear.MythicVaultCharmItem;
import xyz.iwolfking.woldsvaults.items.trinket_pouch.PouchRuntime;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * The item half of a build: armor, offhand, mainhand (the selected hotbar slot), hotbar, the rest of the inventory
 * and every curio slot. Items are built and every slot is checked before the player is touched.
 */
public final class EquipmentCodec {
    public static final int HOTBAR_SIZE = 9;
    private static final Map<String, EquipmentSlot> ARMOR = new LinkedHashMap<>();
    private static final Set<String> EQUIPMENT_KEYS = Set.of("head", "chest", "legs", "feet", "offhand", "mainhand",
            "selected_slot");

    static {
        ARMOR.put("head", EquipmentSlot.HEAD);
        ARMOR.put("chest", EquipmentSlot.CHEST);
        ARMOR.put("legs", EquipmentSlot.LEGS);
        ARMOR.put("feet", EquipmentSlot.FEET);
    }

    private EquipmentCodec() {
    }

    /** Writes the player's equipment, hotbar, curios and (with {@code withInventory}) the rest of the inventory. */
    public static void capture(ServerPlayer player, boolean withInventory, JsonObject out, BuildReport report) {
        Inventory inventory = player.getInventory();
        int selected = inventory.selected;
        JsonObject equipment = new JsonObject();
        for (Map.Entry<String, EquipmentSlot> entry : ARMOR.entrySet()) {
            equipment.add(entry.getKey(), item(player.getItemBySlot(entry.getValue()), "equipment." + entry.getKey(), report));
        }
        equipment.add("offhand", item(player.getOffhandItem(), "equipment.offhand", report));
        equipment.add("mainhand", item(inventory.getItem(selected), "equipment.mainhand", report));
        equipment.addProperty("selected_slot", selected);
        out.add("equipment", equipment);

        JsonArray hotbar = new JsonArray();
        for (int slot = 0; slot < HOTBAR_SIZE; slot++) {
            if (slot != selected && !inventory.getItem(slot).isEmpty()) {
                hotbar.add(slotEntry(slot, inventory.getItem(slot), "hotbar[" + slot + "]", report));
            }
        }
        out.add("hotbar", hotbar);
        if (withInventory) {
            JsonArray rest = new JsonArray();
            for (int slot = HOTBAR_SIZE; slot < inventory.items.size(); slot++) {
                if (!inventory.getItem(slot).isEmpty()) {
                    rest.add(slotEntry(slot, inventory.getItem(slot), "inventory[" + slot + "]", report));
                }
            }
            out.add("inventory", rest);
        }

        JsonObject curios = new JsonObject();
        Map<String, List<Tuple<ItemStack, Integer>>> worn = new TreeMap<>(IntegrationCurios.getCuriosItemStacks(player));
        for (Map.Entry<String, List<Tuple<ItemStack, Integer>>> entry : worn.entrySet()) {
            JsonArray slots = new JsonArray();
            boolean any = false;
            for (Tuple<ItemStack, Integer> stack : entry.getValue()) {
                slots.add(item(stack.getA(), "curios." + entry.getKey() + "[" + stack.getB() + "]", report));
                any |= !stack.getA().isEmpty();
            }
            while (slots.size() > 0 && slots.get(slots.size() - 1).isJsonNull()) {
                slots.remove(slots.size() - 1);
            }
            if (any) {
                curios.add(entry.getKey(), slots);
            }
        }
        out.add("curios", curios);
    }

    private static JsonElement item(ItemStack stack, String path, BuildReport report) {
        return stack.isEmpty() ? JsonNull.INSTANCE : ItemCodec.write(stack, path, report);
    }

    private static JsonObject slotEntry(int slot, ItemStack stack, String path, BuildReport report) {
        JsonObject json = new JsonObject();
        json.addProperty("slot", slot);
        json.add("stack", ItemCodec.write(stack, path, report));
        return json;
    }

    /** True when the build has any item section, so loading it replaces the player's items. */
    public static boolean hasItems(JsonObject build) {
        return build.has("equipment") || build.has("hotbar") || build.has("inventory") || build.has("curios");
    }

    /** Builds every item of the build and checks that each slot exists on {@code player}. */
    public static Plan plan(JsonObject build, ServerPlayer player, int targetLevel, int targetGreedTier, BuildReport report) {
        Plan plan = new Plan();
        Inventory inventory = player.getInventory();
        JsonObject equipment = Json.optObject(build, "equipment", "build");
        if (equipment != null) {
            Json.onlyKeys(equipment, "equipment", EQUIPMENT_KEYS);
            for (String key : ARMOR.keySet()) {
                ItemStack stack = readOptional(equipment, key, "equipment", targetLevel, report);
                if (stack != null) {
                    plan.armor.put(ARMOR.get(key), stack);
                }
            }
            plan.offhand = readOptional(equipment, "offhand", "equipment", targetLevel, report);
            plan.mainhand = readOptional(equipment, "mainhand", "equipment", targetLevel, report);
            Integer selected = Json.optInt(equipment, "selected_slot", "equipment");
            plan.selectedSlot = selected == null ? 0 : selected;
            if (plan.selectedSlot < 0 || plan.selectedSlot >= HOTBAR_SIZE) {
                throw new BuildException("equipment.selected_slot: must be 0 to " + (HOTBAR_SIZE - 1));
            }
        }
        readSlots(build, "hotbar", 0, HOTBAR_SIZE, plan.hotbar, targetLevel, report);
        readSlots(build, "inventory", HOTBAR_SIZE, inventory.items.size(), plan.inventory, targetLevel, report);
        if (plan.mainhand != null && plan.hotbar.containsKey(plan.selectedSlot)) {
            throw new BuildException("hotbar: slot " + plan.selectedSlot + " is the selected slot, which holds the mainhand item");
        }
        JsonObject curios = Json.optObject(build, "curios", "build");
        if (curios != null) {
            Map<String, List<Tuple<ItemStack, Integer>>> slots = IntegrationCurios.getCuriosItemStacks(player);
            for (Map.Entry<String, JsonElement> entry : curios.entrySet()) {
                String id = entry.getKey();
                String path = "curios." + id;
                if (!entry.getValue().isJsonArray()) {
                    throw new BuildException(path + ": expected a list of items (null for an empty slot)");
                }
                JsonArray list = entry.getValue().getAsJsonArray();
                int size = slots.containsKey(id) ? slots.get(id).size() : 0;
                Map<Integer, ItemStack> placed = new TreeMap<>();
                for (int index = 0; index < list.size(); index++) {
                    JsonElement element = list.get(index);
                    if (element.isJsonNull()) {
                        continue;
                    }
                    if (index >= size) {
                        throw new BuildException(path + "[" + index + "]: this player has " + size + " '" + id
                                + "' curio slot(s), so index " + index + " does not exist");
                    }
                    placed.put(index, ItemCodec.read(Json.asObject(element, path + "[" + index + "]"),
                            path + "[" + index + "]", targetLevel, report));
                }
                plan.curios.put(id, placed);
            }
            plan.clearCurios = true;
        }
        plan.all().forEach((path, stack) -> checkGates(path, stack, targetLevel, targetGreedTier, report));
        return plan;
    }

    @Nullable
    private static ItemStack readOptional(JsonObject parent, String key, String path, int targetLevel, BuildReport report) {
        JsonElement element = parent.get(key);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        return ItemCodec.read(Json.asObject(element, path + "." + key), path + "." + key, targetLevel, report);
    }

    private static void readSlots(JsonObject build, String key, int from, int to, Map<Integer, ItemStack> into,
                                  int targetLevel, BuildReport report) {
        JsonArray array = Json.optArray(build, key, "build");
        if (array == null) {
            return;
        }
        for (int i = 0; i < array.size(); i++) {
            String path = key + "[" + i + "]";
            JsonObject entry = Json.asObject(array.get(i), path);
            Json.onlyKeys(entry, path, Set.of("slot", "stack"));
            int slot = Json.intValue(entry, "slot", path);
            if (slot < from || slot >= to) {
                throw new BuildException(path + ".slot: " + key + " slots run " + from + " to " + (to - 1));
            }
            if (into.containsKey(slot)) {
                throw new BuildException(path + ".slot: slot " + slot + " is listed twice");
            }
            into.put(slot, ItemCodec.read(Json.object(entry, "stack", path), path + ".stack", targetLevel, report));
        }
    }

    private static void checkGates(String path, ItemStack stack, int targetLevel, int targetGreedTier, BuildReport report) {
        if (isPinnedMythic(stack)) {
            report.bypassed(path + ": mythic charm pinned at " + MythicCharmRolls.storedUnits(stack) + " piety units; it "
                    + "keeps the build's values and never rescales to the wearer's piety");
        }
        if (!AttributeGearData.hasData(stack) || !(AttributeGearData.read(stack.copy()) instanceof VaultGearData data)) {
            return;
        }
        if (data.getItemLevel() > targetLevel) {
            report.bypassed(path + ": item level " + data.getItemLevel() + " is above the player's level " + targetLevel
                    + "; the game ignores its stats until the player catches up");
        }
        int greedTier = data.get(ModGearAttributes.GREED_TIER, VaultGearAttributeTypeMerger.intSum());
        if (greedTier > targetGreedTier) {
            report.bypassed(path + ": needs greed tier " + greedTier + " (player greed tier " + targetGreedTier
                    + "); the game blocks attacks with it");
        }
    }

    private static boolean isPinnedMythic(ItemStack stack) {
        return MythicVaultCharmItem.isMythic(stack) && stack.hasTag()
                && stack.getTag().getBoolean(MythicCharmRolls.PINNED_TAG);
    }

    /** The items of one build and where they go. */
    public static final class Plan {
        final Map<EquipmentSlot, ItemStack> armor = new LinkedHashMap<>();
        @Nullable ItemStack offhand;
        @Nullable ItemStack mainhand;
        int selectedSlot;
        final Map<Integer, ItemStack> hotbar = new TreeMap<>();
        final Map<Integer, ItemStack> inventory = new TreeMap<>();
        final Map<String, Map<Integer, ItemStack>> curios = new LinkedHashMap<>();
        boolean clearCurios;

        Map<String, ItemStack> all() {
            Map<String, ItemStack> all = new LinkedHashMap<>();
            this.armor.forEach((slot, stack) -> all.put("equipment." + slot.getName(), stack));
            if (this.offhand != null) {
                all.put("equipment.offhand", this.offhand);
            }
            if (this.mainhand != null) {
                all.put("equipment.mainhand", this.mainhand);
            }
            this.hotbar.forEach((slot, stack) -> all.put("hotbar slot " + slot, stack));
            this.inventory.forEach((slot, stack) -> all.put("inventory slot " + slot, stack));
            this.curios.forEach((id, slots) -> slots.forEach((index, stack) -> all.put("curios." + id + "[" + index + "]", stack)));
            return all;
        }

        /**
         * Checks that a keep-inventory load has room for the items it displaces, before anything moves. Returns an
         * error message, or null when there is room.
         */
        @Nullable
        public String checkKeepRoom(ServerPlayer player) {
            List<ItemStack> displaced = this.displaced(player);
            int free = 0;
            Inventory inventory = player.getInventory();
            for (int slot = 0; slot < inventory.items.size(); slot++) {
                boolean target = this.hotbar.containsKey(slot) || this.inventory.containsKey(slot)
                        || (this.mainhand != null && slot == this.selectedSlot);
                if (!target && inventory.getItem(slot).isEmpty()) {
                    free++;
                }
            }
            if (displaced.size() > free) {
                return "keep_inventory needs " + displaced.size() + " free inventory slots for the items the build replaces, "
                        + "but only " + free + " are free";
            }
            return null;
        }

        private List<ItemStack> displaced(ServerPlayer player) {
            List<ItemStack> displaced = new ArrayList<>();
            Inventory inventory = player.getInventory();
            this.armor.keySet().forEach(slot -> addIfPresent(displaced, player.getItemBySlot(slot)));
            if (this.offhand != null) {
                addIfPresent(displaced, player.getOffhandItem());
            }
            if (this.mainhand != null) {
                addIfPresent(displaced, inventory.getItem(this.selectedSlot));
            }
            this.hotbar.keySet().forEach(slot -> addIfPresent(displaced, inventory.getItem(slot)));
            this.inventory.keySet().forEach(slot -> addIfPresent(displaced, inventory.getItem(slot)));
            if (this.clearCurios) {
                IntegrationCurios.getCuriosItemStacks(player).values().forEach(list ->
                        list.forEach(stack -> addIfPresent(displaced, stack.getA())));
            }
            return displaced;
        }

        private static void addIfPresent(List<ItemStack> list, ItemStack stack) {
            if (!stack.isEmpty()) {
                list.add(stack);
            }
        }

        /**
         * Clears (or with {@code keepInventory} moves aside) the player's items and places the build's. Charms are
         * stamped with the player's piety and companions lose their old owner, so neither changes after equipping.
         */
        public void apply(ServerPlayer player, boolean keepInventory, BuildReport report) {
            Inventory inventory = player.getInventory();
            List<ItemStack> displaced = keepInventory ? this.displaced(player).stream().map(ItemStack::copy).toList() : List.of();
            if (keepInventory) {
                this.armor.keySet().forEach(slot -> player.setItemSlot(slot, ItemStack.EMPTY));
                if (this.offhand != null) {
                    player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                }
                if (this.mainhand != null) {
                    inventory.setItem(this.selectedSlot, ItemStack.EMPTY);
                }
                this.hotbar.keySet().forEach(slot -> inventory.setItem(slot, ItemStack.EMPTY));
                this.inventory.keySet().forEach(slot -> inventory.setItem(slot, ItemStack.EMPTY));
            } else {
                inventory.clearContent();
            }
            if (this.clearCurios || !keepInventory) {
                IntegrationCurios.getCuriosItemStacks(player).forEach((id, list) -> list.forEach(stack -> {
                    if (!stack.getA().isEmpty()) {
                        IntegrationCurios.setCurioItemStack(player, ItemStack.EMPTY, id, stack.getB());
                    }
                }));
            }
            this.all().forEach((path, stack) -> fixUp(stack, player, path, report));
            this.armor.forEach(player::setItemSlot);
            if (this.offhand != null) {
                player.setItemSlot(EquipmentSlot.OFFHAND, this.offhand);
            }
            this.hotbar.forEach(inventory::setItem);
            this.inventory.forEach(inventory::setItem);
            if (this.mainhand != null) {
                inventory.setItem(this.selectedSlot, this.mainhand);
                inventory.selected = this.selectedSlot;
                player.connection.send(new ClientboundSetCarriedItemPacket(this.selectedSlot));
            }
            for (Map.Entry<String, Map<Integer, ItemStack>> entry : this.curios.entrySet()) {
                for (Map.Entry<Integer, ItemStack> slot : entry.getValue().entrySet()) {
                    if (!IntegrationCurios.setCurioItemStack(player, slot.getValue(), entry.getKey(), slot.getKey())) {
                        throw new BuildException("curios." + entry.getKey() + "[" + slot.getKey() + "]: the curio slot "
                                + "refused the item");
                    }
                }
            }
            for (ItemStack stack : displaced) {
                if (!inventory.add(stack)) {
                    player.drop(stack, false);
                    report.bypassed("keep_inventory: no room left for " + ItemCodec.describe(stack) + "; dropped at the player's feet");
                }
            }
            player.inventoryMenu.broadcastChanges();
            PouchRuntime.update(player);
        }

        private static void fixUp(ItemStack stack, ServerPlayer player, String path, BuildReport report) {
            if (stack.getItem() instanceof VaultCharmItem) {
                VaultGod god = VaultCharmItem.getGod(stack).orElse(null);
                if (god != null && isPinnedMythic(stack)) {
                    report.note(path + ": pinned mythic charm keeps its " + MythicCharmRolls.storedUnits(stack)
                            + " piety units (this player's " + god.getName() + " piety gives "
                            + GodPiety.scaleUnits(player, god) + ")");
                } else if (god != null) {
                    int units = GodPiety.scaleUnits(player, god);
                    if (MythicVaultCharmItem.isMythic(stack)) {
                        stack.getOrCreateTag().putInt(MythicCharmRolls.UNITS_TAG, units);
                    }
                    VaultCharmItem.setGodReputation(stack, units);
                    report.note(path + ": charm stamped with this player's " + god.getName() + " piety (" + units
                            + " units) so its saved values are not rescaled");
                }
            }
            if (stack.getItem() instanceof CompanionItem && stack.hasTag()) {
                stack.getTag().remove("Owner");
                stack.getTag().remove("OwnerName");
                report.note(path + ": companion owner cleared so it binds to this player; its companion id is shared "
                        + "with the saved original");
            }
        }
    }
}
