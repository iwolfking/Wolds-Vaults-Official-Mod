package xyz.iwolfking.woldsvaults.augments.spell;

import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.SpecializedSkill;
import iskallia.vault.skill.tree.AbilityTree;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.augments.AugmentType;
import xyz.iwolfking.woldsvaults.augments.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;
import xyz.iwolfking.woldsvaults.spellcraft.pool.GlyphView;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Item-wide spellcraft rules. Every spell on a focus or wand is read against the item's glyph view; together the
 * active glyphs must fit the item's arcana budget, so when they don't (a pricier line arrived, a source came back) the
 * budget pass greys glyphs one at a time from the end (newest spell first, last token first), recomputing every cost
 * after each step, until they fit. Editing adds the table's limits on spell count, names and slots, refuses new glyphs
 * the view can't back, and refuses an edit that would grey more glyphs than the item had greyed before it.
 */
public final class SpellcraftRules {
    private SpellcraftRules() {
    }

    /**
     * The result of checking an item: its problems, total active arcana, glyph placements, each spell's model by id
     * (after the budget pass), and how many glyphs the budget pass greyed.
     */
    public record ItemCheck(List<String> errors, float arcana, Map<AugmentType, Integer> uses, Map<Integer, SpellModel> models,
                            int budgetInactive, int budget) {
        public boolean isValid() {
            return this.errors.isEmpty();
        }
    }

    /** Maps an ability group to the tree's current specialization id, or null when the tree has no such group. */
    public static Function<String, String> specOf(AbilityTree tree) {
        return group -> {
            Skill skill = tree.getForId(group).orElse(null);
            if (skill instanceof SpecializedSkill specialized && specialized.getSpecialization() != null) {
                return specialized.getSpecialization().getId();
            }
            return null;
        };
    }

    /** A spell's priority, with 0 read as the table's default. */
    public static int priority(FocusAugmentsConfig config, SpellcraftItemData.Spell spell) {
        return spell.prio() <= 0 ? config.rules.defaultPriority : Math.min(spell.prio(), config.rules.priorities);
    }

    /** Reads the item's stored spells for casting and display. */
    public static ItemCheck check(FocusAugmentsConfig config, ItemStack stack, GlyphView view, Function<String, String> specOf) {
        return check(config, SpellcraftItemData.budget(stack), SpellcraftItemData.depth(stack), view, SpellcraftItemData.spells(stack), specOf,
                SpellModel.Mode.LENIENT, Map.of());
    }

    /**
     * Checks spells against an item's budget, depth and view. {@code saved} gives, per spell id, the glyph copies the
     * stored spell already holds (used by {@link SpellModel.Mode#STRICT_NEW}). In the lenient mode only spell structure
     * is an error; the editing modes add the item's limits.
     */
    public static ItemCheck check(FocusAugmentsConfig config, int budget, int depth, GlyphView view, List<SpellcraftItemData.Spell> spells,
                                  Function<String, String> specOf, SpellModel.Mode mode, Map<Integer, Map<AugmentType, Integer>> saved) {
        List<String> errors = new ArrayList<>();
        Map<Integer, Map<Integer, SpellModel.State>> grey = new HashMap<>();
        Map<Integer, SpellModel> models = new LinkedHashMap<>();
        for (SpellcraftItemData.Spell spell : spells) {
            models.put(spell.id(), build(config, view, depth, spell, specOf, mode, grey, saved));
        }
        List<SpellcraftItemData.Spell> newestFirst = new ArrayList<>(spells);
        newestFirst.sort(Comparator.comparingInt(SpellcraftItemData.Spell::id).reversed());
        int greyed = 0;
        while (Math.round(total(models)) > budget) {
            boolean stepped = false;
            for (SpellcraftItemData.Spell spell : newestFirst) {
                SpellModel model = models.get(spell.id());
                int last = lastActive(model, spell.tokens().size());
                if (last >= 0) {
                    grey.computeIfAbsent(spell.id(), id -> new HashMap<>()).put(last, SpellModel.State.INACTIVE_BUDGET);
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
        Map<AugmentType, Integer> uses = new EnumMap<>(AugmentType.class);
        for (SpellcraftItemData.Spell spell : spells) {
            SpellModel model = models.get(spell.id());
            model.uses().forEach((type, count) -> uses.merge(type, count, Integer::sum));
            for (String error : model.errors()) {
                errors.add(spell.name() + ": " + error);
            }
        }
        if (mode != SpellModel.Mode.LENIENT) {
            editingErrors(config, spells, errors);
        }
        return new ItemCheck(errors, total(models), uses, models, greyed, budget);
    }

    private static void editingErrors(FocusAugmentsConfig config, List<SpellcraftItemData.Spell> spells, List<String> errors) {
        if (spells.size() > config.rules.maxSpellsPerItem) {
            errors.add("an item holds at most " + config.rules.maxSpellsPerItem + " spells");
        }
        Map<Integer, String> slots = new HashMap<>();
        for (SpellcraftItemData.Spell spell : spells) {
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

    private static SpellModel build(FocusAugmentsConfig config, GlyphView view, int depth, SpellcraftItemData.Spell spell,
                                    Function<String, String> specOf, SpellModel.Mode mode, Map<Integer, Map<Integer, SpellModel.State>> grey,
                                    Map<Integer, Map<AugmentType, Integer>> saved) {
        return SpellModel.build(config, view, depth, spell.tokens(), specOf, mode, grey.getOrDefault(spell.id(), Map.of()),
                saved.getOrDefault(spell.id(), Map.of()));
    }

    private static float total(Map<Integer, SpellModel> models) {
        float total = 0.0F;
        for (SpellModel model : models.values()) {
            total += model.arcana();
        }
        return total;
    }

    private static int lastActive(SpellModel model, int tokens) {
        for (int i = tokens - 1; i >= 0; i--) {
            SpellModel.Glyph glyph = model.glyphAt(i);
            if (glyph != null && glyph.active()) {
                return i;
            }
        }
        return -1;
    }

    /** The glyph copies of each type a stored spell holds, by spell id. */
    public static Map<Integer, Map<AugmentType, Integer>> savedCounts(List<SpellcraftItemData.Spell> spells) {
        Map<Integer, Map<AugmentType, Integer>> saved = new HashMap<>();
        for (SpellcraftItemData.Spell spell : spells) {
            Map<AugmentType, Integer> counts = new EnumMap<>(AugmentType.class);
            for (String token : spell.tokens()) {
                AugmentType type = AugmentType.byId(token);
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
     * the editor and the server must: {@link SpellModel.Mode#STRICT_NEW} against the stored spells, plus a refusal when
     * the edit makes the budget pass grey more glyphs than the stored spells do.
     */
    public static ItemCheck checkEdit(FocusAugmentsConfig config, ItemStack stack, GlyphView view, List<SpellcraftItemData.Spell> edited,
                                      Function<String, String> specOf) {
        List<SpellcraftItemData.Spell> stored = SpellcraftItemData.spells(stack);
        int budget = SpellcraftItemData.budget(stack);
        int depth = SpellcraftItemData.depth(stack);
        ItemCheck before = check(config, budget, depth, view, stored, specOf, SpellModel.Mode.LENIENT, Map.of());
        ItemCheck after = check(config, budget, depth, view, edited, specOf, SpellModel.Mode.STRICT_NEW, savedCounts(stored));
        if (after.budgetInactive() > before.budgetInactive()) {
            List<String> errors = new ArrayList<>(after.errors());
            errors.add("over the item's arcana budget of " + budget + ": this would switch off "
                    + (after.budgetInactive() - before.budgetInactive()) + " more glyph(s)");
            return new ItemCheck(errors, after.arcana(), after.uses(), after.models(), after.budgetInactive(), budget);
        }
        return after;
    }

    /** The item's spells with one spell replaced or added ({@code edited}), or removed ({@code edited} null). */
    public static List<SpellcraftItemData.Spell> withEdit(ItemStack stack, int spellId, @Nullable SpellcraftItemData.Spell edited) {
        List<SpellcraftItemData.Spell> spells = new ArrayList<>();
        boolean replaced = false;
        for (SpellcraftItemData.Spell spell : SpellcraftItemData.spells(stack)) {
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
