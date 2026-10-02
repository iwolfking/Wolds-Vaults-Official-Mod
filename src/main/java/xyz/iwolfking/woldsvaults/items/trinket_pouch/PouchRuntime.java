package xyz.iwolfking.woldsvaults.items.trinket_pouch;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import xyz.iwolfking.woldsvaults.api.util.PouchHelper;

public final class PouchRuntime {
    private static final Map<Player, Map<ItemStack, WornEntry>> WORN = new WeakHashMap<>();

    private PouchRuntime() {}

    public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            PouchMigration.equipped(event.player);
            if (event.player instanceof ServerPlayer player) {
                PouchStartRoom.trackDeparture(player);
            }
        } else {
            update(event.player);
        }
    }

    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        clear(event.getPlayer());
    }

    public static synchronized void clear(Player player) {
        Map<ItemStack, WornEntry> previous = WORN.remove(player);
        if (previous != null) {
            previous.values().forEach(entry -> unequip(player, entry.snapshot(), entry.index()));
        }
    }

    public static synchronized void update(Player player) {
        ItemStack pouch = PouchHelper.equipped(player);
        Map<ItemStack, WornEntry> previous = WORN.getOrDefault(player, Map.of());
        if (previous.isEmpty() && (!player.isAlive() || !PouchHelper.isPouch(pouch))) {
            return;
        }
        Map<ItemStack, WornEntry> next = new IdentityHashMap<>();
        if (player.isAlive() && PouchHelper.isPouch(pouch)) {
            PouchContents contents = PouchCapability.get(pouch);
            List<Integer> requested = contents.activeIndices();
            List<Integer> selected = PouchHelper.validSelection(pouch, contents, requested);
            if (!player.level.isClientSide) {
                if (!selected.equals(requested)) {
                    contents.setActive(selected);
                }
                if (contents.autoReplace() && !selected.isEmpty() && !PouchHelper.locked(player)) {
                    selected = replaceExhausted(pouch, contents, player, selected);
                }
            }
            for (int index : selected) {
                ItemStack stack = contents.getStackInSlot(index);
                WornEntry entry = previous.get(stack);
                next.put(stack, entry == null ? new WornEntry(stack.copy(), index) : entry);
            }
        }
        previous.forEach((stack, entry) -> {
            if (!next.containsKey(stack)) {
                unequip(player, entry.snapshot(), entry.index());
            }
        });
        next.forEach((stack, entry) -> {
            ICurioItem item = (ICurioItem) stack.getItem();
            if (!previous.containsKey(stack)) {
                item.onEquip(PouchHelper.context(player, stack, entry.index()), ItemStack.EMPTY, stack);
            }
            item.curioTick(PouchHelper.context(player, stack, entry.index()), stack);
        });
        if (player instanceof ServerPlayer serverPlayer && !previous.keySet().equals(next.keySet())) {
            AttributeSnapshotHelper.getInstance().refreshSnapshotDelayed(serverPlayer);
        }
        if (next.isEmpty()) {
            WORN.remove(player);
        } else {
            WORN.put(player, next);
        }
    }

    private record WornEntry(ItemStack snapshot, int index) {}

    private static void unequip(Player player, ItemStack stack, int index) {
        if (stack.getItem() instanceof ICurioItem item) {
            item.onUnequip(PouchHelper.context(player, stack, index), ItemStack.EMPTY, stack);
        }
    }

    private static List<Integer> replaceExhausted(ItemStack pouch, PouchContents contents, Player player, List<Integer> selected) {
        List<Integer> replacement = selected;
        for (int position = 0; position < replacement.size(); position++) {
            ItemStack exhausted = contents.getStackInSlot(replacement.get(position));
            if (PouchHelper.remainingUses(exhausted) > 0) {
                continue;
            }
            String effect = PouchHelper.effectKey(exhausted);
            for (int reserve = 0; reserve < PouchContents.SIZE; reserve++) {
                ItemStack candidate = contents.getStackInSlot(reserve);
                if (!replacement.contains(reserve) && PouchHelper.remainingUses(candidate) > 0
                        && effect.equals(PouchHelper.effectKey(candidate))) {
                    List<Integer> trial = new ArrayList<>(replacement);
                    trial.set(position, reserve);
                    if (PouchHelper.validate(pouch, contents, trial, player).isEmpty()) {
                        replacement = trial;
                        break;
                    }
                }
            }
        }
        if (replacement != selected) {
            contents.setActive(replacement);
        }
        return replacement;
    }
}
