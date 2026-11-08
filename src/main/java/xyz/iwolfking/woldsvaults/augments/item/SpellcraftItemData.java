package xyz.iwolfking.woldsvaults.augments.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.augments.AugmentType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Spellcraft data on a focus or wand, in NBT {@code woldsvaults_spellcraft}: the rolled implicits (arcana budget,
 * depth, glyph lines), the augmented spells built on it, and its spell bar. A bar entry is empty (inherit the default
 * bar), {@code -} (blank), {@code a:<ability group>} or {@code s:<spell id>}. Spell tokens are ability group ids and
 * glyph ids; the first token is the trigger ability.
 */
public final class SpellcraftItemData {
    public static final int SLOTS = 16;
    public static final String ROOT = "woldsvaults_spellcraft";
    public static final String INHERIT = "";
    public static final String BLANK = "-";
    public static final String ABILITY_PREFIX = "a:";
    public static final String SPELL_PREFIX = "s:";

    private SpellcraftItemData() {
    }

    /** One rolled glyph line: its type, rolled potency, arcana cost per placement and how many placements it allows. */
    public record Line(AugmentType type, float potency, float quality, int cost, int uses) {
    }

    /** One augmented spell: a per-item id (kept across edits so bar slots follow it), a name and its tokens. */
    public record Spell(int id, String name, List<String> tokens) {
        public String trigger() {
            return this.tokens.isEmpty() ? "" : this.tokens.get(0);
        }
    }

    public static boolean has(ItemStack stack) {
        return stack.getTag() != null && stack.getTag().contains(ROOT, Tag.TAG_COMPOUND);
    }

    @Nullable
    private static CompoundTag root(ItemStack stack) {
        return has(stack) ? stack.getTag().getCompound(ROOT) : null;
    }

    private static CompoundTag rootForWrite(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(ROOT, Tag.TAG_COMPOUND)) {
            tag.put(ROOT, new CompoundTag());
        }
        return tag.getCompound(ROOT);
    }

    /** A random id rolled with the implicits; bar keys use it so two copies of a spell on different items differ. */
    public static long id(ItemStack stack) {
        CompoundTag root = root(stack);
        return root == null ? 0L : root.getLong("id");
    }

    public static int budget(ItemStack stack) {
        CompoundTag root = root(stack);
        return root == null ? 0 : root.getInt("budget");
    }

    public static int depth(ItemStack stack) {
        CompoundTag root = root(stack);
        return root == null ? 0 : root.getInt("depth");
    }

    public static List<Line> lines(ItemStack stack) {
        CompoundTag root = root(stack);
        if (root == null) {
            return List.of();
        }
        List<Line> lines = new ArrayList<>();
        for (Tag tag : root.getList("lines", Tag.TAG_COMPOUND)) {
            CompoundTag line = (CompoundTag) tag;
            AugmentType type = AugmentType.byId(line.getString("g"));
            if (type != null) {
                lines.add(new Line(type, line.getFloat("p"), line.getFloat("q"), line.getInt("c"), line.getInt("u")));
            }
        }
        return Collections.unmodifiableList(lines);
    }

    public static Optional<Line> line(ItemStack stack, AugmentType type) {
        return lines(stack).stream().filter(line -> line.type() == type).findFirst();
    }

    public static List<Spell> spells(ItemStack stack) {
        CompoundTag root = root(stack);
        if (root == null) {
            return List.of();
        }
        List<Spell> spells = new ArrayList<>();
        for (Tag tag : root.getList("spells", Tag.TAG_COMPOUND)) {
            CompoundTag spell = (CompoundTag) tag;
            List<String> tokens = new ArrayList<>();
            for (Tag token : spell.getList("t", Tag.TAG_STRING)) {
                tokens.add(token.getAsString());
            }
            spells.add(new Spell(spell.getInt("id"), spell.getString("name"), Collections.unmodifiableList(tokens)));
        }
        return Collections.unmodifiableList(spells);
    }

    public static Optional<Spell> spell(ItemStack stack, int id) {
        return spells(stack).stream().filter(spell -> spell.id() == id).findFirst();
    }

    /** The item's bar, always {@link #SLOTS} entries. */
    public static String[] bar(ItemStack stack) {
        String[] bar = new String[SLOTS];
        java.util.Arrays.fill(bar, INHERIT);
        CompoundTag root = root(stack);
        if (root != null) {
            ListTag list = root.getList("bar", Tag.TAG_STRING);
            for (int i = 0; i < Math.min(SLOTS, list.size()); i++) {
                bar[i] = list.getString(i);
            }
        }
        return bar;
    }

    /** Writes freshly rolled implicits and clears spells and bar. */
    public static void writeImplicits(ItemStack stack, long id, int budget, int depth, List<Line> lines) {
        CompoundTag root = rootForWrite(stack);
        root.putLong("id", id);
        root.putInt("budget", budget);
        root.putInt("depth", depth);
        ListTag list = new ListTag();
        for (Line line : lines) {
            CompoundTag tag = new CompoundTag();
            tag.putString("g", line.type().id());
            tag.putFloat("p", line.potency());
            tag.putFloat("q", line.quality());
            tag.putInt("c", line.cost());
            tag.putInt("u", line.uses());
            list.add(tag);
        }
        root.put("lines", list);
        root.put("spells", new ListTag());
        root.putInt("nextSpell", 1);
        root.remove("bar");
    }

    /** Replaces the spell with the same id, or adds it; a spell id of 0 gets the next free id. Returns the stored id. */
    public static int putSpell(ItemStack stack, Spell spell) {
        CompoundTag root = rootForWrite(stack);
        int id = spell.id();
        if (id <= 0) {
            id = Math.max(1, root.getInt("nextSpell"));
            root.putInt("nextSpell", id + 1);
        }
        CompoundTag tag = new CompoundTag();
        tag.putInt("id", id);
        tag.putString("name", spell.name());
        ListTag tokens = new ListTag();
        spell.tokens().forEach(token -> tokens.add(StringTag.valueOf(token)));
        tag.put("t", tokens);
        ListTag spells = root.getList("spells", Tag.TAG_COMPOUND);
        boolean replaced = false;
        for (int i = 0; i < spells.size(); i++) {
            if (spells.getCompound(i).getInt("id") == id) {
                spells.set(i, tag);
                replaced = true;
            }
        }
        if (!replaced) {
            spells.add(tag);
        }
        root.put("spells", spells);
        return id;
    }

    /** Removes a spell and clears any bar slot that pointed at it. */
    public static void removeSpell(ItemStack stack, int id) {
        CompoundTag root = rootForWrite(stack);
        ListTag spells = root.getList("spells", Tag.TAG_COMPOUND);
        spells.removeIf(tag -> ((CompoundTag) tag).getInt("id") == id);
        root.put("spells", spells);
        String[] bar = bar(stack);
        for (int i = 0; i < SLOTS; i++) {
            if (bar[i].equals(SPELL_PREFIX + id)) {
                setBarEntry(stack, i, INHERIT);
            }
        }
    }

    public static void setBarEntry(ItemStack stack, int slot, String value) {
        if (slot < 0 || slot >= SLOTS) {
            return;
        }
        String[] bar = bar(stack);
        bar[slot] = value;
        ListTag list = new ListTag();
        for (String entry : bar) {
            list.add(StringTag.valueOf(entry));
        }
        rootForWrite(stack).put("bar", list);
    }
}
