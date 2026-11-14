package xyz.iwolfking.woldsvaults.spellcraft.item;

import java.util.List;

/**
 * One spell: a per-item id (kept across edits), a name, its tokens, the bar slot it overlays ({@link
 * SpellcraftItemData#UNASSIGNED} for none) and its priority (0 for the table's default).
 */
public record StoredSpell(int id, String name, List<String> tokens, int slot, int prio) {
    public static final int UNSAVED_ID = Integer.MAX_VALUE;

    public StoredSpell(int id, String name, List<String> tokens) {
        this(id, name, tokens, SpellcraftItemData.UNASSIGNED, 0);
    }

    public String trigger() {
        return this.tokens.isEmpty() ? "" : this.tokens.get(0);
    }

    public StoredSpell withSlot(int slot) {
        return new StoredSpell(this.id, this.name, this.tokens, slot, this.prio);
    }

    public StoredSpell withPrio(int prio) {
        return new StoredSpell(this.id, this.name, this.tokens, this.slot, prio);
    }

    public boolean assigned() {
        return this.slot >= 0 && this.slot < SpellcraftItemData.SLOTS;
    }
}
