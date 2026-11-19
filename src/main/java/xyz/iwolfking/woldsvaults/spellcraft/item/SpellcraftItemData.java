package xyz.iwolfking.woldsvaults.spellcraft.item;

import iskallia.vault.item.gear.FocusItem;
import iskallia.vault.item.gear.WandItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Spellcraft data on a focus or wand, in NBT {@code woldsvaults_spellcraft}: the rolled implicits (arcana budget,
 * depth, glyph lines) and the spells built on it. Spell tokens are ability group ids and glyph ids, and the
 * first token is the trigger ability. An item that still holds an old {@code bar} list has its spell entries read as
 * slots and the rest dropped with one warning per item, and the next edit writes the current form.
 */
public final class SpellcraftItemData {
    public static final int SLOTS = 32;
    public static final String ROOT = "woldsvaults_spellcraft";
    public static final int UNASSIGNED = -1;
    private static final String LEGACY_BAR = "bar";
    private static final String LEGACY_SPELL_PREFIX = "s:";
    private static final String UNIQUE_SET = "set";
    private static final Set<Long> WARNED = Collections.synchronizedSet(new HashSet<>());
    private static int revision;

    private SpellcraftItemData() {
    }

    public static boolean canCarry(ItemStack stack) {
        return stack.getItem() instanceof FocusItem || stack.getItem() instanceof WandItem;
    }

    /** Bumped on every spellcraft write on this side, so cached views and bars know an item changed. */
    public static int revision() {
        return revision;
    }

    public static boolean has(ItemStack stack) {
        return stack.getTag() != null && stack.getTag().contains(ROOT, Tag.TAG_COMPOUND);
    }

    @Nullable
    private static CompoundTag root(ItemStack stack) {
        return has(stack) ? stack.getTag().getCompound(ROOT) : null;
    }

    private static CompoundTag rootForWrite(ItemStack stack) {
        revision++;
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(ROOT, Tag.TAG_COMPOUND)) {
            tag.put(ROOT, new CompoundTag());
        }
        return tag.getCompound(ROOT);
    }

    /** A random id rolled with the implicits; spells on the bar are known by this id and their spell id. */
    public static long id(ItemStack stack) {
        CompoundTag root = root(stack);
        return root == null ? 0L : root.getLong("id");
    }

    /** The item's identity as a glyph source and bar occupant: its id in decimal. */
    public static String key(ItemStack stack) {
        return Long.toString(id(stack));
    }

    public static int budget(ItemStack stack) {
        CompoundTag root = root(stack);
        return root == null ? 0 : root.getInt("budget");
    }

    public static int depth(ItemStack stack) {
        CompoundTag root = root(stack);
        return root == null ? 0 : root.getInt("depth");
    }

    public static List<GlyphLine> lines(ItemStack stack) {
        CompoundTag root = root(stack);
        if (root == null) {
            return List.of();
        }
        List<GlyphLine> lines = new ArrayList<>();
        for (Tag tag : root.getList("lines", Tag.TAG_COMPOUND)) {
            CompoundTag line = (CompoundTag) tag;
            GlyphType type = GlyphType.byId(line.getString("g"));
            if (type != null) {
                Integer cap = line.contains("cap", Tag.TAG_ANY_NUMERIC) ? (int) line.getByte("cap") : null;
                lines.add(new GlyphLine(type, line.getFloat("p"), line.getFloat("q"), line.getInt("c"), line.getInt("u"), line.getBoolean("x"), cap));
            }
        }
        return Collections.unmodifiableList(lines);
    }

    public static Optional<GlyphLine> line(ItemStack stack, GlyphType type) {
        return lines(stack).stream().filter(line -> line.type() == type).findFirst();
    }

    public static List<StoredSpell> spells(ItemStack stack) {
        CompoundTag root = root(stack);
        return root == null ? List.of() : Collections.unmodifiableList(readSpells(root));
    }

    public static Optional<StoredSpell> spell(ItemStack stack, int id) {
        return spells(stack).stream().filter(spell -> spell.id() == id).findFirst();
    }

    private static List<StoredSpell> readSpells(CompoundTag root) {
        Map<Integer, Integer> legacySlots = root.contains(LEGACY_BAR, Tag.TAG_LIST) ? legacySlots(root) : Map.of();
        List<StoredSpell> spells = new ArrayList<>();
        for (Tag tag : root.getList("spells", Tag.TAG_COMPOUND)) {
            CompoundTag spell = (CompoundTag) tag;
            List<String> tokens = new ArrayList<>();
            for (Tag token : spell.getList("t", Tag.TAG_STRING)) {
                tokens.add(token.getAsString());
            }
            int id = spell.getInt("id");
            int slot = spell.contains("slot", Tag.TAG_ANY_NUMERIC) ? spell.getByte("slot") : legacySlots.getOrDefault(id, UNASSIGNED);
            if (slot < UNASSIGNED || slot >= SLOTS) {
                WoldLogger.error("Spellcraft: spell {} on item {} names slot {}, which does not exist; it stays off the bar", id,
                        root.getLong("id"), slot);
                slot = UNASSIGNED;
            }
            spells.add(new StoredSpell(id, spell.getString("name"), Collections.unmodifiableList(tokens), slot, spell.getByte("prio")));
        }
        return spells;
    }

    private static Map<Integer, Integer> legacySlots(CompoundTag root) {
        Set<Integer> ids = new HashSet<>();
        for (Tag tag : root.getList("spells", Tag.TAG_COMPOUND)) {
            ids.add(((CompoundTag) tag).getInt("id"));
        }
        Map<Integer, Integer> slots = new HashMap<>();
        List<String> dropped = new ArrayList<>();
        ListTag bar = root.getList(LEGACY_BAR, Tag.TAG_STRING);
        for (int i = 0; i < Math.min(SLOTS, bar.size()); i++) {
            String entry = bar.getString(i);
            if (entry.isEmpty()) {
                continue;
            }
            Integer id = null;
            if (entry.startsWith(LEGACY_SPELL_PREFIX)) {
                try {
                    id = Integer.parseInt(entry.substring(LEGACY_SPELL_PREFIX.length()));
                } catch (NumberFormatException e) {
                    id = null;
                }
            }
            if (id != null && ids.contains(id) && !slots.containsKey(id)) {
                slots.put(id, i);
            } else {
                dropped.add("slot " + (i + 1) + " '" + entry + "'");
            }
        }
        if (!dropped.isEmpty() && WARNED.add(root.getLong("id"))) {
            WoldLogger.warn("Spellcraft: item {} still holds an old item bar; its spells keep their slots, but these entries have no "
                    + "place any more and are dropped: {}", root.getLong("id"), String.join(", ", dropped));
        }
        return slots;
    }

    private static void writeSpells(CompoundTag root, List<StoredSpell> spells) {
        ListTag list = new ListTag();
        for (StoredSpell spell : spells) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("id", spell.id());
            tag.putString("name", spell.name());
            ListTag tokens = new ListTag();
            spell.tokens().forEach(token -> tokens.add(StringTag.valueOf(token)));
            tag.put("t", tokens);
            tag.putByte("slot", (byte) spell.slot());
            tag.putByte("prio", (byte) spell.prio());
            list.add(tag);
        }
        root.put("spells", list);
        root.remove(LEGACY_BAR);
    }

    private static ListTag writeLines(List<GlyphLine> lines) {
        ListTag list = new ListTag();
        for (GlyphLine line : lines) {
            CompoundTag tag = new CompoundTag();
            tag.putString("g", line.type().id());
            tag.putFloat("p", line.potency());
            tag.putFloat("q", line.quality());
            tag.putInt("c", line.cost());
            tag.putInt("u", line.uses());
            if (line.transferable()) {
                tag.putBoolean("x", true);
            }
            if (line.cap() != null) {
                tag.putByte("cap", (byte) (int) line.cap());
            }
            list.add(tag);
        }
        return list;
    }

    /** Writes freshly rolled implicits and clears the spells. */
    public static void writeImplicits(ItemStack stack, long id, int budget, int depth, List<GlyphLine> lines) {
        CompoundTag root = rootForWrite(stack);
        root.putLong("id", id);
        root.putInt("budget", budget);
        root.putInt("depth", depth);
        root.put("lines", writeLines(lines));
        root.put("spells", new ListTag());
        root.putInt("nextSpell", 1);
        root.remove(LEGACY_BAR);
        root.remove(UNIQUE_SET);
    }

    /** The unique set (unique id and rarity row) the item's implicits were last rolled from, or "" when none. */
    public static String uniqueSet(ItemStack stack) {
        CompoundTag root = root(stack);
        return root == null ? "" : root.getString(UNIQUE_SET);
    }

    /** Records the unique set the implicits were just rolled from, so they are not converted again. */
    public static void markUniqueSet(ItemStack stack, String set) {
        rootForWrite(stack).putString(UNIQUE_SET, set);
    }

    /**
     * Replaces budget, depth and lines but keeps the item id and its spells (a converted unique keeps its spells;
     * glyphs the new lines can't back go inactive on their own).
     */
    public static void replaceImplicits(ItemStack stack, int budget, int depth, List<GlyphLine> lines) {
        CompoundTag root = rootForWrite(stack);
        List<StoredSpell> spells = readSpells(root);
        root.putInt("budget", budget);
        root.putInt("depth", depth);
        root.put("lines", writeLines(lines));
        writeSpells(root, spells);
    }

    /**
     * Replaces the spell with the same id, or adds it; a spell id of 0 gets the next free id. Returns the stored id.
     */
    public static int putSpell(ItemStack stack, StoredSpell spell) {
        CompoundTag root = rootForWrite(stack);
        int id = spell.id();
        if (id <= 0) {
            id = Math.max(1, root.getInt("nextSpell"));
            root.putInt("nextSpell", id + 1);
        }
        StoredSpell stored = new StoredSpell(id, spell.name(), spell.tokens(), spell.slot(), spell.prio());
        List<StoredSpell> spells = new ArrayList<>(readSpells(root));
        boolean replaced = false;
        for (int i = 0; i < spells.size(); i++) {
            if (spells.get(i).id() == id) {
                spells.set(i, stored);
                replaced = true;
            }
        }
        if (!replaced) {
            spells.add(stored);
        }
        writeSpells(root, spells);
        return id;
    }

    public static void removeSpell(ItemStack stack, int id) {
        CompoundTag root = rootForWrite(stack);
        List<StoredSpell> spells = new ArrayList<>(readSpells(root));
        spells.removeIf(spell -> spell.id() == id);
        writeSpells(root, spells);
    }
}
