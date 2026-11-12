package xyz.iwolfking.woldsvaults.spellcraft.gear;

import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.init.ModGearAttributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.api.util.AncientUniqueHelper;
import xyz.iwolfking.woldsvaults.augments.AugmentType;
import xyz.iwolfking.woldsvaults.augments.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.augments.item.SpellcraftRoller;
import xyz.iwolfking.woldsvaults.augments.spell.SpellModel;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/**
 * Unique focuses and wands with a fixed glyph set (loadout spec sec. 6.1, table section {@code uniqueSets}). Budget and
 * depth roll on the set's rarity row (omega for a regular unique, mythic for an ancient), with the wand rules for wands;
 * the lines are every signature glyph plus a draw of distinct glyphs at equal odds, each rolled as on any carrier of
 * the item's level. A signature glyph the item's own gear affix already grants (Steel Core on Steel Inferno, Rime on
 * Frozen Orb, the warp glyphs on Ender Rings) comes from that affix with its rolled value and is not stored twice.
 * A unique rolled before its set existed (or on another row) converts on the server's inventory scan: new lines, budget
 * and depth, spells kept, one INFO line per item. A unique with no set rolls like any carrier and logs one WARN per id.
 */
public final class UniqueSetRoller implements SpellcraftRoller.UniqueSets {
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    private record Plan(String uniqueId, String row, FocusAugmentsConfig.UniqueSet set, FocusAugmentsConfig.RarityRolls rolls) {
        String mark() {
            return this.uniqueId + "|" + this.row;
        }
    }

    /** What a set roll produces, before it is written. */
    public record Rolled(int budget, int depth, List<SpellcraftItemData.Line> lines) {
    }

    @Override
    public boolean roll(FocusAugmentsConfig config, ItemStack stack, VaultGearData data, Random random) {
        Plan plan = plan(config, data);
        if (plan == null) {
            return false;
        }
        Rolled rolled = rollSet(config, stack, data.getItemLevel(), plan.set(), plan.row(), plan.rolls(), random);
        SpellcraftItemData.writeImplicits(stack, random.nextLong(), rolled.budget(), rolled.depth(), rolled.lines());
        SpellcraftItemData.markUniqueSet(stack, plan.mark());
        return true;
    }

    @Override
    public boolean convert(FocusAugmentsConfig config, ItemStack stack, VaultGearData data, Random random) {
        Plan plan = plan(config, data);
        if (plan == null || plan.mark().equals(SpellcraftItemData.uniqueSet(stack))) {
            return false;
        }
        int oldBudget = SpellcraftItemData.budget(stack);
        int oldDepth = SpellcraftItemData.depth(stack);
        int oldLines = SpellcraftItemData.lines(stack).size();
        Rolled rolled = rollSet(config, stack, data.getItemLevel(), plan.set(), plan.row(), plan.rolls(), random);
        int savedDepth = deepestSpell(config, stack);
        int depth = Math.max(rolled.depth(), savedDepth);
        SpellcraftItemData.replaceImplicits(stack, rolled.budget(), depth, rolled.lines());
        SpellcraftItemData.markUniqueSet(stack, plan.mark());
        WoldLogger.info("Spellcraft: unique {} (item {}) converted to its {} glyph set: arcana {} -> {}, depth {} -> {}, {} stored lines -> {}; "
                        + "its {} spells are kept", plan.uniqueId(), SpellcraftItemData.key(stack), plan.row(), oldBudget, rolled.budget(), oldDepth,
                depth, oldLines, rolled.lines().size(), SpellcraftItemData.spells(stack).size());
        if (depth > rolled.depth()) {
            WoldLogger.info("Spellcraft: unique {} (item {}) keeps depth {} instead of the rolled {} so its saved spells stay castable",
                    plan.uniqueId(), SpellcraftItemData.key(stack), depth, rolled.depth());
        }
        return true;
    }

    /**
     * The most depth-counting glyphs any saved spell on the item holds (unique glyphs don't count toward depth), so a
     * converted unique never turns a saved spell into a depth error.
     */
    private static int deepestSpell(FocusAugmentsConfig config, ItemStack stack) {
        int deepest = 0;
        for (SpellcraftItemData.Spell spell : SpellcraftItemData.spells(stack)) {
            int count = 0;
            for (String token : spell.tokens()) {
                AugmentType type = AugmentType.byId(token);
                if (type != null && !SpellModel.isUnique(config, type)) {
                    count++;
                }
            }
            deepest = Math.max(deepest, count);
        }
        return deepest;
    }

    @Nullable
    private static Plan plan(FocusAugmentsConfig config, VaultGearData data) {
        ResourceLocation key = data.getFirstValue(ModGearAttributes.UNIQUE_ITEM_KEY).orElse(null);
        if (key == null) {
            errorOnce("nokey", "Spellcraft: a unique focus or wand has no unique id; it rolls like any carrier");
            return null;
        }
        String uniqueId = key.toString();
        if (config.uniqueSets == null) {
            if (WARNED.add("missing")) {
                WoldLogger.error("Spellcraft: the focus augments table has no uniqueSets section; unique focuses and wands roll like any carrier");
            }
            return null;
        }
        FocusAugmentsConfig.UniqueSet set = config.uniqueSets.get(uniqueId);
        if (set == null) {
            errorOnce("set:" + uniqueId, "Spellcraft: unique " + uniqueId + " has no glyph set; it rolls like any carrier");
            return null;
        }
        String row = AncientUniqueHelper.isAncient(data) ? set.ancientRarityRow : set.rarityRow;
        FocusAugmentsConfig.RarityRolls rolls = config.focus.rarities.get(row);
        if (rolls == null) {
            if (WARNED.add("row:" + row)) {
                WoldLogger.error("Spellcraft: unique set {} rolls on rarity row {}, which the focus rolls lack; it rolls like any carrier",
                        uniqueId, row);
            }
            return null;
        }
        return new Plan(uniqueId, row, set, rolls);
    }

    /**
     * Rolls a set for an item: budget and depth on the row, the signature lines not already granted by the item's gear
     * affixes, and the draw. Public for the offline check.
     */
    public static Rolled rollSet(FocusAugmentsConfig config, ItemStack stack, int level, FocusAugmentsConfig.UniqueSet set, String row,
                                 FocusAugmentsConfig.RarityRolls rolls, Random random) {
        int budget = Math.max(0, Math.round(SpellcraftRoller.budget(config, stack, level, rolls, random)));
        int depth = Math.max(1, SpellcraftRoller.depth(config, stack, level, row, random));
        Set<AugmentType> fromAffixes = EnumSet.noneOf(AugmentType.class);
        GearGlyphLines.lines(stack).forEach(line -> fromAffixes.add(line.type()));
        return new Rolled(budget, depth, lines(config, set, level, fromAffixes, random));
    }

    /** The stored lines of a set roll, given the glyph types the item's own affixes already grant. */
    public static List<SpellcraftItemData.Line> lines(FocusAugmentsConfig config, FocusAugmentsConfig.UniqueSet set, int level,
                                                      Set<AugmentType> fromAffixes, Random random) {
        List<SpellcraftItemData.Line> lines = new ArrayList<>();
        for (FocusAugmentsConfig.SetGlyph always : set.always) {
            AugmentType type = AugmentType.byId(always.glyph);
            if (type == null) {
                continue;
            }
            if (fromAffixes.contains(type) && always.uses == null && always.cap == null) {
                continue;
            }
            if (isGearGlyph(config, type)) {
                errorOnce("affix:" + type.id(), "Spellcraft: a unique set's signature glyph " + type.id()
                        + " has no granting affix on the item; it is stored as a rolled line instead");
            }
            SpellcraftItemData.Line rolled = SpellcraftRoller.rollLine(config, type, level, random);
            lines.add(new SpellcraftItemData.Line(type, rolled.potency(), rolled.quality(), rolled.cost(),
                    always.uses != null ? always.uses : rolled.uses(), false, always.cap));
        }
        List<String> pool = new ArrayList<>(set.draw);
        Collections.shuffle(pool, random);
        int count = set.drawMin + random.nextInt(set.drawMax - set.drawMin + 1);
        for (String id : pool.subList(0, Math.min(count, pool.size()))) {
            AugmentType type = AugmentType.byId(id);
            if (type != null) {
                lines.add(SpellcraftRoller.rollLine(config, type, level, random));
            }
        }
        return lines;
    }

    private static boolean isGearGlyph(FocusAugmentsConfig config, AugmentType type) {
        if (config.gearGlyphs == null) {
            return false;
        }
        for (FocusAugmentsConfig.GearGlyph entry : config.gearGlyphs) {
            if (type.id().equals(entry.glyph)) {
                return true;
            }
        }
        return false;
    }

    private static void errorOnce(String key, String message) {
        if (WARNED.add(key)) {
            WoldLogger.error(message);
        }
    }
}
