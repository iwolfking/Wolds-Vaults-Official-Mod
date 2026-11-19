package xyz.iwolfking.woldsvaults.client.spellcraft.editor;

import iskallia.vault.skill.tree.AbilityTree;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftDraw;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftNames;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftPalette;
import xyz.iwolfking.woldsvaults.client.spellcraft.state.ClientGlyphViews;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.network.packets.SpellEditMessage;
import xyz.iwolfking.woldsvaults.network.packets.SpellPlaceMessage;
import xyz.iwolfking.woldsvaults.spellcraft.ability.AbilityLookup;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphType;
import xyz.iwolfking.woldsvaults.spellcraft.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.spellcraft.item.StoredSpell;
import xyz.iwolfking.woldsvaults.spellcraft.source.GlyphView;
import xyz.iwolfking.woldsvaults.spellcraft.spell.SpellcraftRules;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * The spell editor's local drafts of an item's spells and the selection within them. A draft is sent to the server a
 * moment after its last change, and only when the whole item passes the spellcraft check with it.
 */
final class SpellDrafts {
    static final int END = Integer.MAX_VALUE;
    private static final int SAVE_DELAY = 8;
    private static final int SERVER_WAIT = 60;
    private static final int DELETE_WINDOW = 60;
    private static final int SAVED_TEXT = 0xFF1E7A1E;

    /** A spell being edited, whose id stays 0 until the server stores it as a new spell. */
    static final class Draft {
        int id;
        String name;
        List<String> tokens;
        boolean dirty;
        boolean pendingNew;
        int sentAt = -1;
        Set<Integer> knownIds = Set.of();

        Draft(int id, String name, List<String> tokens) {
            this.id = id;
            this.name = name;
            this.tokens = new ArrayList<>(tokens);
        }

        boolean noTrigger() {
            return this.tokens.isEmpty() || GlyphType.byId(this.tokens.get(0)) != null;
        }
    }

    record Status(String text, int color) {
    }

    private final int inventorySlot;
    private final Supplier<AbilityTree> tree;
    private final Runnable reloadName;
    private final List<Draft> drafts = new ArrayList<>();
    private int selected;
    private int insertAt = END;
    private int weaveScroll;
    private int saveIn = -1;
    private int ticks;
    private int deleteArmed = -1;
    private int deleteArmedAt;

    SpellDrafts(int inventorySlot, Supplier<AbilityTree> tree, Runnable reloadName) {
        this.inventorySlot = inventorySlot;
        this.tree = tree;
        this.reloadName = reloadName;
        for (StoredSpell spell : SpellcraftItemData.spells(stack())) {
            this.drafts.add(new Draft(spell.id(), spell.name(), spell.tokens()));
        }
        if (this.drafts.isEmpty()) {
            this.drafts.add(new Draft(0, "", List.of()));
        }
    }

    ItemStack stack() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || this.inventorySlot < 0 || this.inventorySlot >= minecraft.player.getInventory().getContainerSize()) {
            return ItemStack.EMPTY;
        }
        return minecraft.player.getInventory().getItem(this.inventorySlot);
    }

    boolean stackValid() {
        ItemStack stack = stack();
        return SpellcraftItemData.canCarry(stack) && SpellcraftItemData.has(stack);
    }

    @Nullable
    Draft draft() {
        return this.selected >= 0 && this.selected < this.drafts.size() ? this.drafts.get(this.selected) : null;
    }

    int size() {
        return this.drafts.size();
    }

    Draft get(int index) {
        return this.drafts.get(index);
    }

    int selected() {
        return this.selected;
    }

    int insertAt() {
        return this.insertAt;
    }

    void setInsertAt(int insertAt) {
        this.insertAt = insertAt;
    }

    int weaveScroll() {
        return this.weaveScroll;
    }

    void setWeaveScroll(int weaveScroll) {
        this.weaveScroll = weaveScroll;
    }

    int deleteArmed() {
        return this.deleteArmed;
    }

    void countTick() {
        this.ticks++;
    }

    void countDownSave() {
        if (this.saveIn > 0 && --this.saveIn == 0) {
            saveDirty();
        }
    }

    void expireDelete() {
        if (this.deleteArmed >= 0 && this.ticks - this.deleteArmedAt > DELETE_WINDOW) {
            this.deleteArmed = -1;
        }
    }

    boolean savePending() {
        return this.saveIn > 0;
    }

    void markDirty(Draft draft) {
        draft.dirty = true;
        draft.sentAt = -1;
        this.saveIn = SAVE_DELAY;
    }

    /** The name the server will store: stripped, cut to the table's limit, and the trigger's name when blank. */
    String storedName(Draft draft) {
        String name = draft.name.strip();
        if (name.isEmpty()) {
            String trigger = draft.noTrigger() ? "" : draft.tokens.get(0);
            name = trigger.isEmpty() ? SpellcraftNames.tr("screen.new_spell") : SpellcraftNames.groupName(this.tree.get(), trigger);
        }
        SpellcraftConfig config = ModConfigs.SPELLCRAFT;
        return config != null && name.length() > config.rules.maxNameLength ? name.substring(0, config.rules.maxNameLength).strip() : name;
    }

    private List<StoredSpell> withDraft(Draft draft) {
        int checkId = draft.id > 0 ? draft.id : StoredSpell.UNSAVED_ID;
        StoredSpell stored = draft.id > 0 ? SpellcraftItemData.spell(stack(), draft.id).orElse(null) : null;
        int slot = stored == null ? SpellcraftItemData.UNASSIGNED : stored.slot();
        int prio = stored == null ? 0 : stored.prio();
        return SpellcraftRules.withEdit(stack(), checkId, new StoredSpell(checkId, storedName(draft), List.copyOf(draft.tokens), slot, prio));
    }

    GlyphView view(SpellcraftConfig config) {
        return ClientGlyphViews.view(config, stack());
    }

    @Nullable
    SpellcraftRules.ItemCheck check(Draft draft) {
        SpellcraftConfig config = ModConfigs.SPELLCRAFT;
        if (config == null) {
            return null;
        }
        return SpellcraftRules.checkEdit(config, stack(), view(config), withDraft(draft), AbilityLookup.specOf(this.tree.get()));
    }

    boolean matchesStored(Draft draft) {
        if (draft.id <= 0) {
            return false;
        }
        StoredSpell stored = SpellcraftItemData.spell(stack(), draft.id).orElse(null);
        return stored != null && stored.name().equals(storedName(draft)) && stored.tokens().equals(draft.tokens);
    }

    void saveDirty() {
        for (Draft draft : this.drafts) {
            if (!draft.dirty || draft.pendingNew || matchesStored(draft)) {
                draft.dirty = draft.dirty && draft.pendingNew;
                continue;
            }
            SpellcraftRules.ItemCheck check = check(draft);
            if (check == null || !check.isValid()) {
                continue;
            }
            ModNetwork.sendToServer(new SpellEditMessage(this.inventorySlot, Math.max(0, draft.id), storedName(draft), List.copyOf(draft.tokens), false));
            draft.dirty = false;
            draft.sentAt = this.ticks;
            if (draft.id <= 0) {
                draft.pendingNew = true;
                Set<Integer> ids = new HashSet<>();
                SpellcraftItemData.spells(stack()).forEach(spell -> ids.add(spell.id()));
                draft.knownIds = ids;
            }
        }
    }

    void adoptNewSpells() {
        Set<Integer> claimed = new HashSet<>();
        this.drafts.forEach(draft -> claimed.add(draft.id));
        for (Draft draft : this.drafts) {
            if (!draft.pendingNew) {
                continue;
            }
            for (StoredSpell spell : SpellcraftItemData.spells(stack())) {
                if (!draft.knownIds.contains(spell.id()) && !claimed.contains(spell.id())) {
                    draft.id = spell.id();
                    draft.pendingNew = false;
                    claimed.add(spell.id());
                    if (draft.dirty) {
                        this.saveIn = SAVE_DELAY;
                    }
                    break;
                }
            }
            if (draft.pendingNew && this.ticks - draft.sentAt > SERVER_WAIT) {
                draft.pendingNew = false;
                WoldLogger.error("Spellcraft screen: the server did not store new spell '{}' (inventory slot {}); left as an unsaved draft",
                        draft.name, this.inventorySlot);
            }
        }
    }

    @Nullable
    StoredSpell stored(@Nullable Draft draft) {
        return draft == null || draft.id <= 0 ? null : SpellcraftItemData.spell(stack(), draft.id).orElse(null);
    }

    /**
     * Steps the selected spell's bar slot, skipping slots that other spells of this item hold, or its priority, and
     * sends the result to the server.
     */
    void stepPlacement(boolean slot, int direction) {
        SpellcraftConfig config = ModConfigs.SPELLCRAFT;
        StoredSpell stored = stored(draft());
        if (config == null || stored == null) {
            return;
        }
        if (slot) {
            Set<Integer> taken = new HashSet<>();
            SpellcraftItemData.spells(stack()).stream().filter(spell -> spell.id() != stored.id() && spell.assigned())
                    .forEach(spell -> taken.add(spell.slot()));
            int next = stored.slot();
            for (int i = 0; i <= SpellcraftItemData.SLOTS; i++) {
                next = Math.floorMod(next + 1 + direction, SpellcraftItemData.SLOTS + 1) - 1;
                if (next < 0 || !taken.contains(next)) {
                    break;
                }
            }
            ModNetwork.sendToServer(new SpellPlaceMessage(this.inventorySlot, stored.id(), next, 0));
        } else {
            int prio = SpellcraftRules.priority(config, stored);
            int next = Math.floorMod(prio - 1 + direction, config.rules.priorities) + 1;
            ModNetwork.sendToServer(new SpellPlaceMessage(this.inventorySlot, stored.id(), stored.slot(), next));
        }
        SpellcraftDraw.click();
    }

    /**
     * Turns the selected spell on or off in classic mode. On writes the first bar slot no other spell of this item
     * holds (slot 1 when every slot is taken), so the spell keeps a real slot in slot mode; off unassigns it.
     */
    void toggleActive() {
        StoredSpell stored = stored(draft());
        if (stored == null) {
            return;
        }
        int slot = SpellcraftItemData.UNASSIGNED;
        if (!stored.assigned()) {
            Set<Integer> taken = new HashSet<>();
            SpellcraftItemData.spells(stack()).stream().filter(spell -> spell.id() != stored.id() && spell.assigned())
                    .forEach(spell -> taken.add(spell.slot()));
            slot = 0;
            while (slot < SpellcraftItemData.SLOTS && taken.contains(slot)) {
                slot++;
            }
            if (slot == SpellcraftItemData.SLOTS) {
                slot = 0;
            }
        }
        ModNetwork.sendToServer(new SpellPlaceMessage(this.inventorySlot, stored.id(), slot, 0));
        SpellcraftDraw.click();
    }

    void setPriority(int priority) {
        SpellcraftConfig config = ModConfigs.SPELLCRAFT;
        StoredSpell stored = stored(draft());
        if (config == null || stored == null || priority < 1 || priority > config.rules.priorities) {
            return;
        }
        if (priority != SpellcraftRules.priority(config, stored)) {
            ModNetwork.sendToServer(new SpellPlaceMessage(this.inventorySlot, stored.id(), stored.slot(), priority));
        }
        SpellcraftDraw.click();
    }

    List<String> errors(@Nullable Draft draft, @Nullable SpellcraftRules.ItemCheck check) {
        if (draft == null || check == null || draft.tokens.isEmpty()) {
            return List.of();
        }
        String prefix = storedName(draft) + ": ";
        List<String> errors = new ArrayList<>();
        for (String error : check.errors()) {
            errors.add(error.startsWith(prefix) ? error.substring(prefix.length()) : error);
        }
        return errors;
    }

    Status status(Draft draft, @Nullable SpellcraftRules.ItemCheck check) {
        if (draft.tokens.isEmpty()) {
            return new Status(SpellcraftNames.tr("screen.status.empty"), SpellcraftPalette.DARK);
        }
        if (draft.pendingNew) {
            return new Status(SpellcraftNames.tr("screen.status.saving"), StatsPanel.COOLDOWN_TEXT);
        }
        if (matchesStored(draft)) {
            return new Status(SpellcraftNames.tr("screen.status.saved"), SAVED_TEXT);
        }
        if (check == null || !check.isValid()) {
            return new Status(SpellcraftNames.tr("screen.status.unsaved"), SpellcraftPalette.BAD_TEXT);
        }
        if (!draft.dirty && draft.sentAt >= 0 && this.ticks - draft.sentAt > SERVER_WAIT) {
            return new Status(SpellcraftNames.tr("screen.status.refused"), SpellcraftPalette.BAD_TEXT);
        }
        return new Status(SpellcraftNames.tr("screen.status.saving"), StatsPanel.COOLDOWN_TEXT);
    }

    void addDraft() {
        this.drafts.add(new Draft(0, "", List.of()));
        select(this.drafts.size() - 1);
    }

    void select(int index) {
        if (this.saveIn > 0) {
            saveDirty();
            this.saveIn = -1;
        }
        this.selected = index;
        this.insertAt = END;
        this.weaveScroll = 0;
        this.deleteArmed = -1;
        this.reloadName.run();
    }

    void deleteOrArm(int index) {
        if (this.deleteArmed == index) {
            delete(index);
        } else {
            this.deleteArmed = index;
            this.deleteArmedAt = this.ticks;
        }
    }

    private void delete(int index) {
        Draft draft = this.drafts.get(index);
        if (draft.id > 0) {
            ModNetwork.sendToServer(new SpellEditMessage(this.inventorySlot, draft.id, "", List.of(), true));
        }
        this.drafts.remove(index);
        if (this.drafts.isEmpty()) {
            this.drafts.add(new Draft(0, "", List.of()));
        }
        this.deleteArmed = -1;
        if (index < this.selected) {
            this.selected--;
        }
        this.selected = Math.min(this.selected, this.drafts.size() - 1);
        this.insertAt = END;
        this.weaveScroll = 0;
        this.reloadName.run();
    }

    void removeToken(Draft draft, int index) {
        draft.tokens.remove(index);
        if (this.insertAt != END && this.insertAt > index) {
            this.insertAt--;
        }
        markDirty(draft);
    }

    /** Inserts after the selected weave cell, or first when the token is an ability and the spell has no trigger. */
    void insert(String token) {
        Draft draft = draft();
        if (draft == null) {
            return;
        }
        boolean ability = GlyphType.byId(token) == null;
        boolean noTrigger = draft.noTrigger();
        int at = ability && noTrigger ? 0 : Math.min(this.insertAt, draft.tokens.size());
        draft.tokens.add(at, token);
        if (this.insertAt != END) {
            this.insertAt = at + 1;
        }
        if (ability && at == 0 && draft.name.isBlank()) {
            draft.name = SpellcraftNames.groupName(this.tree.get(), token);
            this.reloadName.run();
        }
        markDirty(draft);
        SpellcraftDraw.click();
    }
}
