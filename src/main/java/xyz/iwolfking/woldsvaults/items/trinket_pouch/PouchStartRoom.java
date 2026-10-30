package xyz.iwolfking.woldsvaults.items.trinket_pouch;

import iskallia.vault.core.vault.ClassicPortalLogic;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.WorldManager;
import iskallia.vault.core.vault.player.Listeners;
import iskallia.vault.item.gear.VaultUsesHelper;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.expertise.type.TrinketerExpertise;
import iskallia.vault.world.data.PlayerExpertisesData;
import iskallia.vault.world.data.ServerVaults;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import xyz.iwolfking.woldsvaults.api.util.PouchHelper;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.ClassicListenersLogicAccessor;

public final class PouchStartRoom {
    public static final String TIME_TRINKET_FIXED = "Time trinkets stay as entered in the start room";
    private static final String REFUNDS = "WoldsStartRoomRefunds";
    private static final String REFUNDS_VAULT = "Vault";
    private static final String REFUNDS_SLOTS = "Slots";
    private static final String LEFT_START_ROOM = "WoldsLeftStartRoom";
    private static final double START_AREA_RADIUS_SQUARED = 15 * 15;

    private PouchStartRoom() {}

    public static void trackDeparture(ServerPlayer player) {
        if (!player.level.dimension().location().getNamespace().equals("the_vault")) {
            return;
        }
        ServerVaults.get(player.level).ifPresent(vault -> {
            if (!hasLeft(player, vault) && !insideStartArea(player, vault)) {
                player.getPersistentData().putUUID(LEFT_START_ROOM, vault.get(Vault.ID));
            }
        });
    }

    public static boolean isOpen(ServerPlayer player, Vault vault) {
        return !hasLeft(player, vault) && insideStartArea(player, vault);
    }

    private static boolean hasLeft(ServerPlayer player, Vault vault) {
        CompoundTag data = player.getPersistentData();
        return data.hasUUID(LEFT_START_ROOM) && data.getUUID(LEFT_START_ROOM).equals(vault.get(Vault.ID));
    }

    private static boolean insideStartArea(ServerPlayer player, Vault vault) {
        return vault.has(Vault.WORLD) && vault.get(Vault.WORLD).get(WorldManager.PORTAL_LOGIC) instanceof ClassicPortalLogic logic
                && logic.getPlayerStartPos(vault)
                        .map(start -> player.distanceToSqr(Vec3.atCenterOf(start)) <= START_AREA_RADIUS_SQUARED)
                        .orElse(false);
    }

    public static String apply(ServerPlayer player, Vault vault, ItemStack pouch, PouchContents contents, List<Integer> requested) {
        Set<Integer> before = new HashSet<>(contents.activeIndices());
        Set<Integer> after = new HashSet<>(requested);
        for (int index : changedIndices(before, after)) {
            if (PouchHelper.isTimeExtension(contents.getStackInSlot(index))) {
                return TIME_TRINKET_FIXED;
            }
        }
        String error = PouchHelper.validate(pouch, contents, requested, player, false);
        if (!error.isEmpty()) {
            return error;
        }
        UUID vaultId = vault.get(Vault.ID);
        CompoundTag refunds = refundsFor(pouch, vaultId);
        for (int index : before) {
            if (!after.contains(index)) {
                refund(contents.getStackInSlot(index), vaultId, refunds.getCompound(REFUNDS_SLOTS), index);
            }
        }
        for (int index : requested) {
            if (!before.contains(index)) {
                charge(player, vault, contents.getStackInSlot(index), vaultId, refunds.getCompound(REFUNDS_SLOTS), index);
            }
        }
        pouch.getOrCreateTag().put(REFUNDS, refunds);
        contents.setActive(requested);
        return "";
    }

    public static List<Integer> keepTimeTrinkets(PouchContents contents, List<Integer> requested) {
        List<Integer> kept = new ArrayList<>();
        for (int index : requested) {
            if (!PouchHelper.isTimeExtension(contents.getStackInSlot(index)) || contents.isActive(index)) {
                kept.add(index);
            }
        }
        for (int index : contents.activeIndices()) {
            if (PouchHelper.isTimeExtension(contents.getStackInSlot(index)) && !kept.contains(index)) {
                kept.add(index);
            }
        }
        return kept;
    }

    private static Set<Integer> changedIndices(Set<Integer> before, Set<Integer> after) {
        Set<Integer> changed = new HashSet<>(before);
        changed.addAll(after);
        Set<Integer> unchanged = new HashSet<>(before);
        unchanged.retainAll(after);
        changed.removeAll(unchanged);
        return changed;
    }

    private static CompoundTag refundsFor(ItemStack pouch, UUID vaultId) {
        CompoundTag stored = pouch.getOrCreateTag().getCompound(REFUNDS);
        if (stored.hasUUID(REFUNDS_VAULT) && stored.getUUID(REFUNDS_VAULT).equals(vaultId)) {
            return stored.copy();
        }
        CompoundTag fresh = new CompoundTag();
        fresh.putUUID(REFUNDS_VAULT, vaultId);
        fresh.put(REFUNDS_SLOTS, new CompoundTag());
        return fresh;
    }

    private static void refund(ItemStack stack, UUID vaultId, CompoundTag refundedSlots, int index) {
        if (removeVaultEntry(stack, "usedVaults", vaultId)) {
            refundedSlots.putBoolean(String.valueOf(index), false);
        } else if (removeVaultEntry(stack, "freeUsedVaults", vaultId)) {
            refundedSlots.putBoolean(String.valueOf(index), true);
        }
    }

    private static void charge(ServerPlayer player, Vault vault, ItemStack stack, UUID vaultId, CompoundTag refundedSlots, int index) {
        if (VaultUsesHelper.isUsableInVault(stack, vaultId)) {
            return;
        }
        String key = String.valueOf(index);
        boolean free = refundedSlots.contains(key) ? refundedSlots.getBoolean(key) : rollFree(player, vault, stack);
        refundedSlots.remove(key);
        if (free) {
            VaultUsesHelper.addFreeUsedVault(stack, vaultId);
        } else {
            VaultUsesHelper.addUsedVault(stack, vaultId);
        }
    }

    private static boolean rollFree(ServerPlayer player, Vault vault, ItemStack stack) {
        ClassicListenersLogicAccessor logic = (ClassicListenersLogicAccessor) vault.get(Vault.LISTENERS).get(Listeners.LOGIC);
        if (logic.woldsvaults$shouldAddFree(player, vault, stack)) {
            return true;
        }
        double freeChance = PlayerExpertisesData.get(player.getLevel()).getExpertises(player)
                .getAll(TrinketerExpertise.class, Skill::isUnlocked).stream()
                .mapToDouble(TrinketerExpertise::getDamageAvoidanceChance).sum();
        return player.level.random.nextDouble() < freeChance;
    }

    private static boolean removeVaultEntry(ItemStack stack, String listKey, UUID vaultId) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(listKey, Tag.TAG_LIST)) {
            return false;
        }
        ListTag entries = tag.getList(listKey, Tag.TAG_COMPOUND);
        for (int entry = 0; entry < entries.size(); entry++) {
            CompoundTag vaultEntry = entries.getCompound(entry);
            if (vaultEntry.hasUUID("id") && vaultEntry.getUUID("id").equals(vaultId)) {
                entries.remove(entry);
                tag.put(listKey, entries);
                return true;
            }
        }
        return false;
    }
}
