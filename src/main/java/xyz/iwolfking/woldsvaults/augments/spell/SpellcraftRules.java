package xyz.iwolfking.woldsvaults.augments.spell;

import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.SpecializedSkill;
import iskallia.vault.skill.tree.AbilityTree;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.augments.AugmentType;
import xyz.iwolfking.woldsvaults.augments.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Item-wide spellcraft rules: every spell on a focus or wand together must fit its arcana budget and its glyph lines'
 * uses, within the table's spell count and name length. Used before any edit is stored and to show the arcana bar.
 */
public final class SpellcraftRules {
    private SpellcraftRules() {
    }

    /** The result of checking an item: its problems, total arcana, glyph placements and each spell's model by id. */
    public record ItemCheck(List<String> errors, float arcana, Map<AugmentType, Integer> uses, Map<Integer, SpellModel> models) {
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

    public static ItemCheck check(FocusAugmentsConfig config, ItemStack stack, Function<String, String> specOf, boolean strict) {
        return check(config, stack, SpellcraftItemData.spells(stack), specOf, strict);
    }

    /** Checks the item as if it held {@code spells} instead of its own. */
    public static ItemCheck check(FocusAugmentsConfig config, ItemStack stack, List<SpellcraftItemData.Spell> spells,
                                  Function<String, String> specOf, boolean strict) {
        List<SpellcraftItemData.Line> lines = SpellcraftItemData.lines(stack);
        int depth = SpellcraftItemData.depth(stack);
        List<String> errors = new ArrayList<>();
        Map<AugmentType, Integer> uses = new EnumMap<>(AugmentType.class);
        Map<Integer, SpellModel> models = new LinkedHashMap<>();
        float arcana = 0.0F;
        if (spells.size() > config.rules.maxSpellsPerItem) {
            errors.add("an item holds at most " + config.rules.maxSpellsPerItem + " spells");
        }
        for (SpellcraftItemData.Spell spell : spells) {
            SpellModel model = SpellModel.build(config, lines, depth, spell.tokens(), specOf, strict);
            models.put(spell.id(), model);
            arcana += model.arcana();
            model.uses().forEach((type, count) -> uses.merge(type, count, Integer::sum));
            for (String error : model.errors()) {
                errors.add(spell.name() + ": " + error);
            }
            if (spell.name().length() > config.rules.maxNameLength) {
                errors.add(spell.name() + ": the name is longer than " + config.rules.maxNameLength + " characters");
            }
        }
        for (Map.Entry<AugmentType, Integer> use : uses.entrySet()) {
            int allowed = lines.stream().filter(line -> line.type() == use.getKey()).mapToInt(SpellcraftItemData.Line::uses).sum();
            if (use.getValue() > allowed) {
                errors.add(use.getKey().displayName() + " is placed " + use.getValue() + " times; the item allows " + allowed);
            }
        }
        int budget = SpellcraftItemData.budget(stack);
        if (Math.round(arcana) > budget) {
            errors.add("the spells cost " + Math.round(arcana) + " arcana; the item has " + budget);
        }
        return new ItemCheck(errors, arcana, uses, models);
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
