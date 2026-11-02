package xyz.iwolfking.woldsvaults.armory.service;

import iskallia.vault.item.CompanionItem;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.PouchHelper;
import xyz.iwolfking.woldsvaults.armory.ArmoryPage;
import xyz.iwolfking.woldsvaults.armory.SavedBuild;
import xyz.iwolfking.woldsvaults.armory.SlotKeys;
import xyz.iwolfking.woldsvaults.armory.block.ArmoryBlockEntity;
import xyz.iwolfking.woldsvaults.builds.BuildTasks;
import xyz.iwolfking.woldsvaults.items.trinket_pouch.PouchRuntime;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Save, Stow and Load. Each action first builds its whole move list (and, for Load, the tree plan), checks it
 * (excess room, curio equip rules, tree gates) and only then moves anything: a refusal leaves the player, the page
 * and the excess untouched.
 */
public final class ArmoryService {
    private ArmoryService() {
    }

    /** Refusal reasons for Load (empty when Load would run) and the notes it would produce. */
    public record Preview(List<String> refusals, List<String> notes) {
    }

    private record Move(String key, ItemStack incoming, ItemStack outgoing) {
    }

    private static final class LoadPlan {
        final List<String> refusals = new ArrayList<>();
        final List<String> notes = new ArrayList<>();
        final List<Move> moves = new ArrayList<>();
        @Nullable TreePlanner.Plan trees;
        @Nullable CreativeRaise raise;
    }

    public static Preview preview(ServerPlayer player, ArmoryBlockEntity armory, int page) {
        try {
            LoadPlan plan = planLoad(player, armory, page);
            return new Preview(plan.refusals, plan.notes);
        } catch (RuntimeException e) {
            WoldsVaults.LOGGER.error("Armory: Load preview failed for {}", player.getGameProfile().getName(), e);
            return new Preview(List.of("Load check failed: " + e.getClass().getSimpleName()), List.of());
        }
    }

    /** Saves the player's trees and the layout of every marked slot. Items do not move (normal armory). */
    public static void save(ServerPlayer player, ArmoryBlockEntity armory, int pageIndex, CompoundTag stats, boolean overwrite) {
        ArmoryPage page = armory.page(pageIndex);
        if (PouchHelper.locked(player)) {
            refuse(player, "Save", List.of("The armory cannot be used from inside a vault"));
            return;
        }
        if (page.hasSave() && !overwrite) {
            refuse(player, "Save", List.of("Build " + (pageIndex + 1) + " already has a save; confirm the overwrite"));
            return;
        }
        CompoundTag snapshot = stats.copy();
        snapshot.putInt("selected", player.getInventory().selected);
        page.setBuild(SavedBuild.capture(player, snapshot));
        int recorded = 0;
        for (String key : SlotKeys.ALL) {
            if (!page.isMarked(key)) {
                page.setExpected(key, null);
                continue;
            }
            ItemStack current = PlayerSlots.get(player, key);
            page.setExpected(key, current.isEmpty() ? ItemStack.EMPTY : current);
            if (armory.isCreative()) {
                page.setStored(key, current.isEmpty() ? ItemStack.EMPTY : current.copy());
            }
            if (!current.isEmpty()) {
                recorded++;
            }
        }
        armory.setChanged();
        tell(player, ChatFormatting.GREEN, "Saved Build " + (pageIndex + 1) + ": trees and " + recorded + " items recorded.");
    }

    /** Moves every marked slot's item into the page; page items it replaces go to excess. */
    public static void stow(ServerPlayer player, ArmoryBlockEntity armory, int pageIndex) {
        if (armory.isCreative()) {
            refuse(player, "Stow", List.of("The Creative Armory hands out copies; it does not store items"));
            return;
        }
        if (PouchHelper.locked(player)) {
            refuse(player, "Stow", List.of("The armory cannot be used from inside a vault"));
            return;
        }
        ArmoryPage page = armory.page(pageIndex);
        List<ItemStack> toExcess = new ArrayList<>();
        List<String> keys = new ArrayList<>();
        for (String key : SlotKeys.ALL) {
            if (!page.isMarked(key)) {
                continue;
            }
            ItemStack current = PlayerSlots.get(player, key);
            if (!current.isEmpty() && !page.stored(key).isEmpty()) {
                toExcess.add(page.stored(key));
            }
            keys.add(key);
        }
        int shortfall = excessShortfall(armory.excess(), toExcess);
        if (shortfall > 0) {
            refuse(player, "Stow", List.of("Excess needs " + shortfall + " more free slots"));
            return;
        }
        int moved = 0;
        for (String key : keys) {
            ItemStack current = PlayerSlots.get(player, key).copy();
            if (current.isEmpty()) {
                page.setExpected(key, ItemStack.EMPTY);
                continue;
            }
            if (!PlayerSlots.set(player, key, ItemStack.EMPTY)) {
                WoldsVaults.LOGGER.warn("Armory: could not take {} out of {}'s {} during Stow; left in place",
                        current.getHoverName().getString(), player.getGameProfile().getName(), key);
                continue;
            }
            ItemStack old = page.stored(key);
            if (!old.isEmpty()) {
                toExcessOrPlayer(player, armory, old);
            }
            page.setStored(key, current);
            page.setExpected(key, current);
            moved++;
        }
        armory.setChanged();
        afterMove(player);
        tell(player, ChatFormatting.GREEN, "Stowed " + moved + " items in Build " + (pageIndex + 1) + ".");
    }

    /** Equips the page's items, then applies the saved trees the upgrades allow; all or nothing. */
    public static void load(ServerPlayer player, ArmoryBlockEntity armory, int pageIndex) {
        LoadPlan plan;
        try {
            plan = planLoad(player, armory, pageIndex);
        } catch (RuntimeException e) {
            WoldsVaults.LOGGER.error("Armory: Load check failed for {}; nothing moved", player.getGameProfile().getName(), e);
            refuse(player, "Load", List.of("Load check failed (" + e.getClass().getSimpleName() + "); nothing moved"));
            return;
        }
        if (!plan.refusals.isEmpty()) {
            refuse(player, "Load", plan.refusals);
            return;
        }
        ArmoryPage page = armory.page(pageIndex);
        boolean creative = armory.isCreative();
        int moved = 0;
        for (Move move : plan.moves) {
            ItemStack outgoing = PlayerSlots.get(player, move.key()).copy();
            if (!PlayerSlots.set(player, move.key(), move.incoming().copy())) {
                WoldsVaults.LOGGER.warn("Armory: {}'s {} refused {} during Load; it stays on the page",
                        player.getGameProfile().getName(), move.key(), move.incoming().getHoverName().getString());
                plan.notes.add("Skipped " + move.incoming().getHoverName().getString() + ": the slot refused it");
                continue;
            }
            if (!creative && !move.incoming().isEmpty()) {
                page.setStored(move.key(), ItemStack.EMPTY);
            }
            if (!outgoing.isEmpty()) {
                toExcessOrPlayer(player, armory, outgoing);
            }
            moved++;
        }
        armory.setChanged();
        afterMove(player);
        if (plan.raise != null) {
            plan.raise.apply(player);
        }
        if (plan.trees != null) {
            TreePlanner.Plan trees = plan.trees;
            if (plan.raise != null) {
                trees = TreePlanner.plan(player, page.build(), armory.hasSkillsUpgrade(), armory.hasGodsUpgrade(),
                        TreePlanner.Budget.of(player));
                if (!trees.ok()) {
                    WoldsVaults.LOGGER.error("Armory: after the creative raises {}'s trees still do not fit: {}",
                            player.getGameProfile().getName(), trees.refusals);
                    refuse(player, "Load", trees.refusals);
                    trees = null;
                }
            }
            if (trees != null) {
                try {
                    trees.commit(player);
                } catch (RuntimeException | LinkageError e) {
                    WoldsVaults.LOGGER.error("Armory: applying the trees failed part way for {}", player.getGameProfile().getName(), e);
                    tell(player, ChatFormatting.RED, "Applying the saved trees failed part way; check your trees. Gear was loaded.");
                }
            }
        }
        tell(player, ChatFormatting.GREEN, "Loaded Build " + (pageIndex + 1) + ": " + moved + " slots set.");
        for (String note : plan.notes) {
            tell(player, ChatFormatting.GRAY, note);
        }
    }

    private static LoadPlan planLoad(ServerPlayer player, ArmoryBlockEntity armory, int pageIndex) {
        LoadPlan plan = new LoadPlan();
        ArmoryPage page = armory.page(pageIndex);
        boolean creative = armory.isCreative();
        if (!page.hasSave() && page.storedMap().isEmpty()) {
            plan.refusals.add("Nothing is saved on this page");
            return plan;
        }
        if (PouchHelper.locked(player)) {
            plan.refusals.add("The armory cannot be used from inside a vault");
        }
        List<ItemStack> toExcess = new ArrayList<>();
        int missing = 0;
        for (String key : SlotKeys.ALL) {
            if (!page.isMarked(key)) {
                continue;
            }
            ItemStack expected = page.expected(key);
            ItemStack incoming = creative ? (expected == null ? ItemStack.EMPTY : expected.copy()) : page.stored(key);
            boolean clear = incoming.isEmpty() && expected != null && expected.isEmpty();
            if (incoming.isEmpty() && !clear) {
                if (expected != null) {
                    missing++;
                }
                continue;
            }
            if (key.startsWith("curio:") && !PlayerSlots.hasCurioSlot(player, key)) {
                if (!incoming.isEmpty()) {
                    plan.notes.add("No " + SlotKeys.curioId(key) + " slot: " + incoming.getHoverName().getString() + " stays on the page");
                }
                continue;
            }
            if (!incoming.isEmpty() && key.startsWith("curio:") && !PlayerSlots.canEquipCurio(player, key, incoming)) {
                plan.notes.add(incoming.getHoverName().getString() + " cannot be worn yet; it stays on the page");
                continue;
            }
            if (creative && !incoming.isEmpty()) {
                if (isBackpackWithContents(incoming)) {
                    plan.notes.add("Skipped " + incoming.getHoverName().getString() + ": a copy would share its contents");
                    continue;
                }
                stripOwner(incoming);
            }
            ItemStack outgoing = PlayerSlots.get(player, key);
            if (incoming.isEmpty() && outgoing.isEmpty()) {
                continue;
            }
            if (!outgoing.isEmpty()) {
                toExcess.add(outgoing);
            }
            plan.moves.add(new Move(key, incoming, outgoing));
        }
        if (missing > 0) {
            plan.notes.add(missing + " saved item" + (missing == 1 ? " is" : "s are") + " not in the armory; those slots are skipped");
        }
        int shortfall = excessShortfall(armory.excess(), toExcess);
        if (shortfall > 0) {
            plan.refusals.add("Excess needs " + shortfall + " more free slots for the gear Load replaces");
        }
        SavedBuild build = page.build();
        if (build != null) {
            boolean skills = armory.hasSkillsUpgrade();
            boolean gods = armory.hasGodsUpgrade();
            if (!skills) {
                plan.notes.add("Skills: off (no skills upgrade)");
            }
            if (!gods) {
                plan.notes.add("Gods: off (no gods upgrade)");
            }
            if (skills || gods) {
                TreePlanner.Budget budget = TreePlanner.Budget.of(player);
                if (creative) {
                    plan.raise = CreativeRaise.of(player, build);
                    budget = plan.raise.budget();
                    plan.notes.addAll(plan.raise.notes());
                }
                plan.trees = TreePlanner.plan(player, build, skills, gods, budget);
                plan.refusals.addAll(plan.trees.refusals);
                plan.notes.addAll(plan.trees.notes);
            }
        }
        return plan;
    }

    /** How many more empty excess slots {@code stacks} would need, counting merges into partial stacks. */
    private static int excessShortfall(ItemStackHandler excess, List<ItemStack> stacks) {
        ItemStackHandler copy = new ItemStackHandler(excess.getSlots());
        for (int i = 0; i < excess.getSlots(); i++) {
            copy.setStackInSlot(i, excess.getStackInSlot(i).copy());
        }
        int shortfall = 0;
        for (ItemStack stack : stacks) {
            if (!ItemHandlerHelper.insertItemStacked(copy, stack.copy(), false).isEmpty()) {
                shortfall++;
            }
        }
        return shortfall;
    }

    private static void toExcessOrPlayer(ServerPlayer player, ArmoryBlockEntity armory, ItemStack stack) {
        ItemStack rest = ItemHandlerHelper.insertItemStacked(armory.excess(), stack, false);
        if (!rest.isEmpty()) {
            WoldsVaults.LOGGER.error("Armory: excess was full despite the check; {} went to {} instead",
                    rest.getHoverName().getString(), player.getGameProfile().getName());
            if (!player.getInventory().add(rest)) {
                player.drop(rest, false);
            }
        }
    }

    private static boolean isBackpackWithContents(ItemStack stack) {
        return "sophisticatedbackpacks".equals(ForgeRegistries.ITEMS.getKey(stack.getItem()).getNamespace())
                && stack.hasTag() && stack.getTag().contains("contentsUuid");
    }

    private static void stripOwner(ItemStack stack) {
        if (stack.getItem() instanceof CompanionItem && stack.hasTag()) {
            stack.getTag().remove("Owner");
            stack.getTag().remove("OwnerName");
        }
    }

    private static void afterMove(ServerPlayer player) {
        player.inventoryMenu.broadcastChanges();
        PouchRuntime.update(player);
        BuildTasks.later(2, () -> {
            if (!player.isRemoved()) {
                PouchRuntime.update(player);
                AttributeSnapshotHelper.getInstance().refreshSnapshot(player);
            }
        });
    }

    private static void refuse(ServerPlayer player, String action, List<String> reasons) {
        tell(player, ChatFormatting.RED, action + " refused; nothing moved:");
        for (String reason : reasons) {
            tell(player, ChatFormatting.RED, "  " + reason);
        }
    }

    private static void tell(ServerPlayer player, ChatFormatting color, String text) {
        player.displayClientMessage(new TextComponent("[Armory] " + text).withStyle(color), false);
    }
}
