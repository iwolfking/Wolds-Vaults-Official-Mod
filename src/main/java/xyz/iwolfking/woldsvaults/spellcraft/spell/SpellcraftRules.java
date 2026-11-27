package xyz.iwolfking.woldsvaults.spellcraft.spell;

import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphType;
import xyz.iwolfking.woldsvaults.spellcraft.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.spellcraft.item.StoredSpell;
import xyz.iwolfking.woldsvaults.spellcraft.source.GlyphView;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Item-wide spellcraft rules. Every spell on a focus or wand is read against the item's glyph view. Only spells assigned
 * to a bar slot are charged: together their active glyphs must fit the item's arcana budget, so when they don't (a
 * pricier line arrived, a source came back) the budget pass greys glyphs of those spells one at a time from the end
 * (newest spell first, last token first), recomputing every cost after each step, until they fit. Editing adds the table's limits on spell count, names and slots, refuses new glyphs the
 * view can't back, and refuses an edit that would grey more glyphs than the item had greyed before it.
 */
public final class SpellcraftRules {
    private SpellcraftRules() {
    }

    /**
     * The result of checking an item: its problems, the active arcana of the charged spells, glyph placements, each
     * spell's model by id (after the budget pass), how many glyphs the budget pass greyed, and the ids of the charged
     * spells.
     */
    public record ItemCheck(List<String> errors, float arcana, Map<GlyphType, Integer> uses, Map<Integer, SpellModel> models,
                            int budgetInactive, int budget, Set<Integer> charged) {
        public boolean isValid() {
            return this.errors.isEmpty();
        }
    }

    /** A spell's priority, with 0 read as the table's default. */
    public static int priority(SpellcraftConfig config, StoredSpell spell) {
        return spell.prio() <= 0 ? config.rules.defaultPriority : Math.min(spell.prio(), config.rules.priorities);
    }

    /** Reads the item's stored spells for casting and display. */
    public static ItemCheck check(SpellcraftConfig config, ItemStack stack, GlyphView view, Function<String, String> specOf) {
        return check(config, SpellcraftItemData.budget(stack), SpellcraftItemData.depth(stack), view, SpellcraftItemData.spells(stack), specOf,
                ReadMode.LENIENT, Map.of());
    }

    /**
     * Checks spells against an item's budget, depth and view. In the lenient mode only spell structure is an error,
     * while the editing modes add the item's limits and use {@code saved}, the glyph copies each stored spell already
     * holds, by spell id.
     */
    public static ItemCheck check(SpellcraftConfig config, int budget, int depth, GlyphView view, List<StoredSpell> spells,
                                  Function<String, String> specOf, ReadMode mode, Map<Integer, Map<GlyphType, Integer>> saved) {
        List<String> errors = new ArrayList<>();
        Map<Integer, Map<Integer, GlyphState>> grey = new HashMap<>();
        Map<Integer, SpellModel> models = new LinkedHashMap<>();
        for (StoredSpell spell : spells) {
            models.put(spell.id(), build(config, view, depth, spell, specOf, mode, grey, saved));
        }
        Set<Integer> charged = new HashSet<>();
        List<StoredSpell> newestFirst = new ArrayList<>();
        for (StoredSpell spell : spells) {
            if (spell.assigned()) {
                charged.add(spell.id());
                newestFirst.add(spell);
            }
        }
        newestFirst.sort(Comparator.comparingInt(StoredSpell::id).reversed());
        int greyed = 0;
        while (Math.round(total(models, charged)) > budget) {
            boolean stepped = false;
            for (StoredSpell spell : newestFirst) {
                SpellModel model = models.get(spell.id());
                int last = lastActive(model, spell.tokens().size());
                if (last >= 0) {
                    grey.computeIfAbsent(spell.id(), id -> new HashMap<>()).put(last, GlyphState.INACTIVE_BUDGET);
                    models.put(spell.id(), build(config, view, depth, spell, specOf, mode, grey, saved));
                    greyed++;
                    stepped = true;
                    break;
                }
            }
            if (!stepped) {
                break;
            }
        }
        Map<GlyphType, Integer> uses = new EnumMap<>(GlyphType.class);
        for (StoredSpell spell : spells) {
            SpellModel model = models.get(spell.id());
            model.uses().forEach((type, count) -> uses.merge(type, count, Integer::sum));
            for (String error : model.errors()) {
                errors.add(spell.name() + ": " + error);
            }
        }
        if (mode != ReadMode.LENIENT) {
            editingErrors(config, spells, errors);
        }
        return new ItemCheck(errors, total(models, charged), uses, models, greyed, budget, charged);
    }

    private static void editingErrors(SpellcraftConfig config, List<StoredSpell> spells, List<String> errors) {
        if (spells.size() > config.rules.maxSpellsPerItem) {
            errors.add("an item holds at most " + config.rules.maxSpellsPerItem + " spells");
        }
        Map<Integer, String> slots = new HashMap<>();
        for (StoredSpell spell : spells) {
            if (spell.name().length() > config.rules.maxNameLength) {
                errors.add(spell.name() + ": the name is longer than " + config.rules.maxNameLength + " characters");
            }
            if (spell.assigned()) {
                String other = slots.putIfAbsent(spell.slot(), spell.name());
                if (other != null) {
                    errors.add(spell.name() + ": " + other + " already uses slot " + (spell.slot() + 1) + " on this item");
                }
            }
            if (spell.prio() < 0 || spell.prio() > config.rules.priorities) {
                errors.add(spell.name() + ": priority " + spell.prio() + " is outside 1-" + config.rules.priorities);
            }
        }
    }

    private static SpellModel build(SpellcraftConfig config, GlyphView view, int depth, StoredSpell spell,
                                    Function<String, String> specOf, ReadMode mode, Map<Integer, Map<Integer, GlyphState>> grey,
                                    Map<Integer, Map<GlyphType, Integer>> saved) {
        return SpellModel.build(config, view, depth, spell.tokens(), specOf, mode, grey.getOrDefault(spell.id(), Map.of()),
                saved.getOrDefault(spell.id(), Map.of()));
    }

    private static float total(Map<Integer, SpellModel> models, Set<Integer> charged) {
        float total = 0.0F;
        for (Map.Entry<Integer, SpellModel> entry : models.entrySet()) {
            if (charged.contains(entry.getKey())) {
                total += entry.getValue().arcana();
            }
        }
        return total;
    }

    private static int lastActive(SpellModel model, int tokens) {
        for (int i = tokens - 1; i >= 0; i--) {
            PlacedGlyph glyph = model.glyphAt(i);
            if (glyph != null && glyph.active()) {
                return i;
            }
        }
        return -1;
    }

    private static Map<Integer, Map<GlyphType, Integer>> savedCounts(List<StoredSpell> spells) {
        Map<Integer, Map<GlyphType, Integer>> saved = new HashMap<>();
        for (StoredSpell spell : spells) {
            Map<GlyphType, Integer> counts = new EnumMap<>(GlyphType.class);
            for (String token : spell.tokens()) {
                GlyphType type = GlyphType.byId(token);
                if (type != null) {
                    counts.merge(type, 1, Integer::sum);
                }
            }
            saved.put(spell.id(), counts);
        }
        return saved;
    }

    /**
     * Checks an edit of the item's spells (a spell replaced, added or removed, or a slot or priority changed) the way
     * the editor and the server must: {@link ReadMode#STRICT_NEW} against the stored spells, plus a refusal when the
     * edit makes the budget pass grey more glyphs than the stored spells do.
     */
    public static ItemCheck checkEdit(SpellcraftConfig config, ItemStack stack, GlyphView view, List<StoredSpell> edited,
                                      Function<String, String> specOf) {
        List<StoredSpell> stored = SpellcraftItemData.spells(stack);
        int budget = SpellcraftItemData.budget(stack);
        int depth = SpellcraftItemData.depth(stack);
        ItemCheck before = check(config, budget, depth, view, stored, specOf, ReadMode.LENIENT, Map.of());
        ItemCheck after = check(config, budget, depth, view, edited, specOf, ReadMode.STRICT_NEW, savedCounts(stored));
        if (after.budgetInactive() > before.budgetInactive()) {
            List<String> errors = new ArrayList<>(after.errors());
            errors.add("over the item's arcana budget of " + budget + ": this would switch off "
                    + (after.budgetInactive() - before.budgetInactive()) + " more glyph(s)");
            return new ItemCheck(errors, after.arcana(), after.uses(), after.models(), after.budgetInactive(), budget, after.charged());
        }
        return after;
    }

    /** The item's spells with one spell replaced or added ({@code edited}), or removed ({@code edited} null). */
    public static List<StoredSpell> withEdit(ItemStack stack, int spellId, @Nullable StoredSpell edited) {
        List<StoredSpell> spells = new ArrayList<>();
        boolean replaced = false;
        for (StoredSpell spell : SpellcraftItemData.spells(stack)) {
            if (spell.id() == spellId) {
                replaced = true;
                if (edited != null) {
                    spells.add(edited);
                }
            } else {
                spells.add(spell);
            }
        }
        if (!replaced && edited != null) {
            spells.add(edited);
        }
        return spells;
    }
}
