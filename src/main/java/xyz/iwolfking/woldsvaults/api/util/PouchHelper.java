package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.core.vault.player.ClassicListenersLogic;
import iskallia.vault.core.vault.player.Listeners;
import iskallia.vault.core.vault.player.Runner;
import iskallia.vault.core.vault.time.TickClock;
import iskallia.vault.gear.trinket.TrinketEffect;
import iskallia.vault.gear.trinket.TrinketHelper;
import iskallia.vault.gear.trinket.effects.VaultTimeExtensionTrinket;
import iskallia.vault.integration.IntegrationCurios;
import iskallia.vault.item.gear.TrinketItem;
import iskallia.vault.item.gear.VaultUsesHelper;
import iskallia.vault.world.data.ServerVaults;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import xyz.iwolfking.woldsvaults.items.TrinketPouchItem;
import xyz.iwolfking.woldsvaults.items.trinket_pouch.PouchContents;
import xyz.iwolfking.woldsvaults.items.trinket_pouch.PouchStartRoom;

public final class PouchHelper {
    public static final List<String> COLORS = List.of("red_trinket", "blue_trinket", "green_trinket");

    private PouchHelper() {}

    public static boolean isPouch(ItemStack stack) {
        return stack.getItem() instanceof TrinketPouchItem;
    }

    public static boolean isStoredItem(ItemStack stack) {
        return stack.getItem() instanceof TrinketItem;
    }

    public static boolean isTrinket(ItemStack stack) {
        return isStoredItem(stack) && TrinketItem.isIdentified(stack) && COLORS.contains(color(stack));
    }

    public static String color(ItemStack stack) {
        return TrinketItem.getSlotIdentifier(stack).orElse("");
    }

    public static int remainingUses(ItemStack stack) {
        return Math.max(0, VaultUsesHelper.getUses(stack) - VaultUsesHelper.getUsedVaults(stack).size());
    }

    public static ItemStack equipped(Player player) {
        return IntegrationCurios.getCurioItemStack(player, "trinket_pouch", 0);
    }

    public static boolean locked(Player player) {
        if (player.level.dimension().location().getNamespace().equals("the_vault")) return true;
        if (player.level.isClientSide) return false;
        if (ServerVaults.get(player.level).isPresent()) return true;
        return ServerVaults.getAll().stream().anyMatch(vault -> vault.has(Vault.LISTENERS)
                && vault.get(Vault.LISTENERS).contains(player.getUUID()));
    }

    public static Optional<Vault> startRoomVault(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return Optional.empty();
        }
        return ServerVaults.get(player.level).filter(vault -> !VaultUtils.isRoyaleVault(vault)
                && vault.has(Vault.LISTENERS) && vault.get(Vault.LISTENERS).get(player.getUUID()) instanceof Runner
                && vault.get(Vault.LISTENERS).get(Listeners.LOGIC) instanceof ClassicListenersLogic
                && vault.has(Vault.CLOCK) && vault.get(Vault.CLOCK).get(TickClock.LOGICAL_TIME) == 0
                && PouchStartRoom.isOpen(serverPlayer, vault));
    }

    public static boolean isTimeExtension(ItemStack stack) {
        return effects(stack).stream().anyMatch(effect -> effect.trinket() instanceof VaultTimeExtensionTrinket);
    }

    public static Map<String, Integer> capacities(ItemStack pouch) {
        return TrinketPouchItem.getPouchConfigFor(pouch).SLOT_ENTRIES;
    }

    public static SlotContext context(Player player, ItemStack stack, int index) {
        return new SlotContext(color(stack), player, index, false, true);
    }

    public static List<TrinketHelper.TrinketStack<TrinketEffect<?>>> effects(ItemStack stack) {
        return TrinketHelper.getTrinkets(Map.of(color(stack), List.of(new Tuple<>(stack, 0))), TrinketEffect.class);
    }

    public static String effectKey(ItemStack stack) {
        return effects(stack).stream().map(entry -> String.valueOf(entry.trinket().getRegistryName())).distinct().sorted()
                .reduce((first, second) -> first + "+" + second).orElse("");
    }

    public static String validate(ItemStack pouch, PouchContents contents, List<Integer> indices, Player player) {
        return validate(pouch, contents, indices, player, true);
    }

    public static String validate(ItemStack pouch, PouchContents contents, List<Integer> indices, Player player, boolean checkEquipRules) {
        SelectionValidator validator = new SelectionValidator(pouch, contents, player, checkEquipRules);
        for (int index : indices) {
            String error = validator.accept(index);
            if (!error.isEmpty()) {
                return error;
            }
        }
        return "";
    }

    public static List<Integer> validSelection(ItemStack pouch, PouchContents contents, List<Integer> requested) {
        List<Integer> accepted = new ArrayList<>();
        SelectionValidator validator = new SelectionValidator(pouch, contents, null, true);
        for (int index : requested) {
            if (validator.accept(index).isEmpty()) {
                accepted.add(index);
            }
        }
        return accepted;
    }

    private static final class SelectionValidator {
        private final PouchContents contents;
        private final Player player;
        private final boolean checkEquipRules;
        private final Map<String, Integer> capacities;
        private final Map<String, Integer> counts = new HashMap<>();
        private final Set<TrinketEffect<?>> effects = new HashSet<>();

        private SelectionValidator(ItemStack pouch, PouchContents contents, Player player, boolean checkEquipRules) {
            this.contents = contents;
            this.player = player;
            this.checkEquipRules = checkEquipRules;
            this.capacities = capacities(pouch);
        }

        private String accept(int index) {
            if (index < 0 || index >= PouchContents.SIZE) {
                return "Invalid stored item";
            }
            ItemStack stack = contents.getStackInSlot(index);
            String color = color(stack);
            if (!isStoredItem(stack) || !TrinketItem.isIdentified(stack) || !COLORS.contains(color)) {
                return "Only identified colored trinkets can be activated";
            }
            if (player != null && !contents.isActive(index) && remainingUses(stack) == 0) {
                return "This trinket has no remaining uses";
            }
            int count = counts.getOrDefault(color, 0) + 1;
            if (count > capacities.getOrDefault(color, 0)) {
                return "No free " + color.replace("_trinket", "") + " capacity in this pouch";
            }
            Set<TrinketEffect<?>> candidateEffects = new HashSet<>();
            for (TrinketHelper.TrinketStack<TrinketEffect<?>> effect : effects(stack)) {
                if (effects.contains(effect.trinket()) || !candidateEffects.add(effect.trinket())) {
                    return "This effect is already active";
                }
            }
            if (player != null && checkEquipRules && !((ICurioItem) stack.getItem()).canEquip(context(player, stack, index), stack)) {
                return "This trinket cannot be equipped right now";
            }
            counts.put(color, count);
            effects.addAll(candidateEffects);
            return "";
        }
    }
}
