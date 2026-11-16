package xyz.iwolfking.woldsvaults.spellcraft.roll;

import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.init.ModGearAttributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.gear.ancient.Ancients;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphType;
import xyz.iwolfking.woldsvaults.spellcraft.item.GlyphLine;
import xyz.iwolfking.woldsvaults.spellcraft.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.spellcraft.item.StoredSpell;
import xyz.iwolfking.woldsvaults.spellcraft.source.GearAffixLines;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Unique focuses and wands with a fixed glyph set (table section {@code uniqueSets}). Budget and depth roll on the
 * set's rarity row, and the lines are every signature glyph the item's own gear affixes do not already grant plus a
 * draw of distinct glyphs at equal odds. A unique rolled from another set or row converts on the server's inventory
 * scan and keeps its spells, and a unique with no set rolls like any carrier and logs one error per id.
 */
public final class UniqueSetRoller implements SpellcraftRoller.UniqueSets {
    private record Plan(String uniqueId, String row, SpellcraftConfig.UniqueSet set, SpellcraftConfig.RarityRolls rolls) {
        String mark() {
            return this.uniqueId + "|" + this.row;
        }
    }

    public record Rolled(int budget, int depth, List<GlyphLine> lines) {
    }

    @Override
    public boolean roll(SpellcraftConfig config, ItemStack stack, VaultGearData data, Random random) {
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
    public boolean convert(SpellcraftConfig config, ItemStack stack, VaultGearData data, Random random) {
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

    private static int deepestSpell(SpellcraftConfig config, ItemStack stack) {
        int deepest = 0;
        for (StoredSpell spell : SpellcraftItemData.spells(stack)) {
            int count = 0;
            for (String token : spell.tokens()) {
                GlyphType type = GlyphType.byId(token);
                if (type != null && !config.glyph(type).isUnique()) {
                    count++;
                }
            }
            deepest = Math.max(deepest, count);
        }
        return deepest;
    }

    @Nullable
    private static Plan plan(SpellcraftConfig config, VaultGearData data) {
        ResourceLocation key = data.getFirstValue(ModGearAttributes.UNIQUE_ITEM_KEY).orElse(null);
        if (key == null) {
            WoldLogger.errorOnce("UniqueSetRoller:nokey", "Spellcraft: a unique focus or wand has no unique id; it rolls like any carrier");
            return null;
        }
        String uniqueId = key.toString();
        if (config.uniqueSets == null) {
            WoldLogger.errorOnce("UniqueSetRoller:missing", "Spellcraft: the spellcraft table has no uniqueSets section; unique focuses and wands roll like any carrier");
            return null;
        }
        SpellcraftConfig.UniqueSet set = config.uniqueSets.get(uniqueId);
        if (set == null) {
            WoldLogger.errorOnce("UniqueSetRoller:set:" + uniqueId, "Spellcraft: unique " + uniqueId + " has no glyph set; it rolls like any carrier");
            return null;
        }
        String row = Ancients.isAncient(data) ? set.ancientRarityRow : set.rarityRow;
        SpellcraftConfig.RarityRolls rolls = config.focus.rarities.get(row);
        if (rolls == null) {
            WoldLogger.errorOnce("UniqueSetRoller:row:" + row, "Spellcraft: unique set {} rolls on rarity row {}, which the focus rolls lack; it rolls like any carrier",
                    uniqueId, row);
            return null;
        }
        return new Plan(uniqueId, row, set, rolls);
    }

    private static Rolled rollSet(SpellcraftConfig config, ItemStack stack, int level, SpellcraftConfig.UniqueSet set, String row,
                                 SpellcraftConfig.RarityRolls rolls, Random random) {
        int budget = Math.max(0, Math.round(SpellcraftRoller.budget(config, stack, level, rolls, random)));
        int depth = Math.max(1, SpellcraftRoller.depth(config, stack, level, row, random));
        Set<GlyphType> fromAffixes = EnumSet.noneOf(GlyphType.class);
        GearAffixLines.lines(stack).forEach(line -> fromAffixes.add(line.type()));
        return new Rolled(budget, depth, lines(config, set, level, fromAffixes, random));
    }

    /** The stored lines of a set roll, given the glyph types the item's own affixes already grant. */
    public static List<GlyphLine> lines(SpellcraftConfig config, SpellcraftConfig.UniqueSet set, int level,
                                                      Set<GlyphType> fromAffixes, Random random) {
        List<GlyphLine> lines = new ArrayList<>();
        for (SpellcraftConfig.SetGlyph always : set.always) {
            GlyphType type = GlyphType.byId(always.glyph);
            if (type == null) {
                continue;
            }
            if (fromAffixes.contains(type) && always.uses == null && always.cap == null) {
                continue;
            }
            if (isGearGlyph(config, type)) {
                WoldLogger.errorOnce("UniqueSetRoller:affix:" + type.id(), "Spellcraft: a unique set's signature glyph " + type.id()
                        + " has no granting affix on the item; it is stored as a rolled line instead");
            }
            GlyphLine rolled = SpellcraftRoller.rollLine(config, type, level, random);
            lines.add(new GlyphLine(type, rolled.potency(), rolled.quality(), rolled.cost(),
                    always.uses != null ? always.uses : rolled.uses(), false, always.cap));
        }
        List<String> pool = new ArrayList<>(set.draw);
        Collections.shuffle(pool, random);
        int count = set.drawMin + random.nextInt(set.drawMax - set.drawMin + 1);
        for (String id : pool.subList(0, Math.min(count, pool.size()))) {
            GlyphType type = GlyphType.byId(id);
            if (type != null) {
                lines.add(SpellcraftRoller.rollLine(config, type, level, random));
            }
        }
        return lines;
    }

    private static boolean isGearGlyph(SpellcraftConfig config, GlyphType type) {
        if (config.gearGlyphs == null) {
            return false;
        }
        for (SpellcraftConfig.GearGlyph entry : config.gearGlyphs) {
            if (type.id().equals(entry.glyph)) {
                return true;
            }
        }
        return false;
    }
}
