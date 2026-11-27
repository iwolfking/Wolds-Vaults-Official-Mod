package xyz.iwolfking.woldsvaults.spellcraft.roll;

import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.item.gear.WandItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphType;
import xyz.iwolfking.woldsvaults.spellcraft.item.GlyphLine;
import xyz.iwolfking.woldsvaults.spellcraft.item.SpellcraftItemData;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.ToIntFunction;

/**
 * Rolls a focus's or wand's spellcraft implicits when it is identified: arcana from item level and rarity, distinct
 * weighted glyph lines by rarity with a potency in their level tier, a cost that follows the potency roll plus noise
 * and a number of uses, and depth by level. Wands roll a smaller budget, fewer lines and a lower depth cap.
 */
public final class SpellcraftRoller {
    private static UniqueSets uniqueSets;

    private SpellcraftRoller() {
    }

    /**
     * The unique-set hook: a unique focus or wand with a set rolls its lines from the set and its budget and depth from
     * the set's rarity row. Until a provider is installed uniques roll like any other carrier.
     */
    public interface UniqueSets {
        /** Rolls a unique's implicits from its set; false when the unique has no set, so it rolls as any carrier. */
        boolean roll(SpellcraftConfig config, ItemStack stack, VaultGearData data, Random random);

        /**
         * Converts a unique whose implicits predate its set: new lines, budget and depth through {@link
         * SpellcraftItemData#replaceImplicits}, spells kept. Returns true when it changed the item.
         */
        boolean convert(SpellcraftConfig config, ItemStack stack, VaultGearData data, Random random);
    }

    /** Installs the unique-set provider; call during common setup. */
    public static void setUniqueSets(UniqueSets sets) {
        uniqueSets = sets;
    }

    public static boolean isWand(ItemStack stack) {
        return stack.getItem() instanceof WandItem;
    }

    public static void rollIfMissing(ItemStack stack, Random random) {
        if (SpellcraftItemData.canCarry(stack) && !SpellcraftItemData.has(stack)) {
            roll(stack, random);
        }
    }

    /**
     * The server's inventory scan for one stack: rolls implicits for an identified carrier that has none (identified
     * before spellcraft existed, or through a path that skips the roll hook) and lets the unique-set hook convert old
     * uniques.
     */
    public static void upkeep(ItemStack stack, Random random) {
        if (!SpellcraftItemData.canCarry(stack)) {
            return;
        }
        VaultGearData data = VaultGearData.read(stack);
        if (data.getState() != VaultGearState.IDENTIFIED) {
            return;
        }
        if (!SpellcraftItemData.has(stack)) {
            roll(stack, random);
            return;
        }
        SpellcraftConfig config = ModConfigs.SPELLCRAFT;
        if (uniqueSets != null && config != null && data.getRarity() == VaultGearRarity.UNIQUE) {
            uniqueSets.convert(config, stack, data, random);
        }
    }

    /**
     * Gives spellcraft implicits to any identified focus or wand in the inventory that has none, and lets the
     * unique-set hook convert old uniques.
     */
    public static void rollMissingSpellcraft(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            upkeep(player.getInventory().getItem(i), player.getRandom());
        }
    }

    /** Rolls fresh implicits, replacing any spells on the item. Logs and leaves the item alone if it can't. */
    public static void roll(ItemStack stack, Random random) {
        SpellcraftConfig config = ModConfigs.SPELLCRAFT;
        if (config == null || !SpellcraftItemData.canCarry(stack)) {
            return;
        }
        VaultGearData data = VaultGearData.read(stack);
        if (uniqueSets != null && data.getRarity() == VaultGearRarity.UNIQUE && uniqueSets.roll(config, stack, data, random)) {
            return;
        }
        int level = data.getItemLevel();
        String rarity = data.getRarity().name();
        SpellcraftConfig.FocusRolls focus = config.focus;
        SpellcraftConfig.RarityRolls rolls = focus.rarities.get(rarity);
        if (rolls == null) {
            WoldLogger.error("Spellcraft: no focus roll for rarity {}; {} rolls no spellcraft implicits", rarity, stack.getItem());
            return;
        }
        float budget = budget(config, stack, level, rolls, random);
        int lineCount = rolls.linesMin + random.nextInt(rolls.linesMax - rolls.linesMin + 1);
        int depth = depth(config, stack, level, rarity, random);
        if (isWand(stack)) {
            lineCount = Math.max(config.wand.minLines, lineCount + config.wand.linesDelta);
        }
        List<GlyphType> pool = new ArrayList<>();
        for (GlyphType type : GlyphType.values()) {
            SpellcraftConfig.GlyphDefinition glyph = config.glyph(type);
            if (glyph.minLevel <= level && glyph.weight > 0 && SpellcraftConfig.potencyTier(glyph, level) != null) {
                pool.add(type);
            }
        }
        List<GlyphLine> lines = new ArrayList<>();
        for (int i = 0; i < lineCount && !pool.isEmpty(); i++) {
            GlyphType type = draw(config, pool, random);
            pool.remove(type);
            lines.add(rollLine(config, type, level, random));
        }
        SpellcraftItemData.writeImplicits(stack, random.nextLong(), Math.max(0, Math.round(budget)), Math.max(1, depth), lines);
    }

    /**
     * The arcana budget a carrier of this level rolls on a rarity row: base plus per level, times the row's budget and
     * a roll, then the wand factor for wands or the row's focus multiplier for focuses.
     */
    public static float budget(SpellcraftConfig config, ItemStack stack, int level, SpellcraftConfig.RarityRolls rolls, Random random) {
        SpellcraftConfig.FocusRolls focus = config.focus;
        float rollMin = rolls.budgetRollMin != null ? rolls.budgetRollMin : focus.budgetRollMin;
        float rollMax = rolls.budgetRollMax != null ? rolls.budgetRollMax : focus.budgetRollMax;
        float budget = (focus.budgetBase + focus.budgetPerLevel * level) * rolls.budget * between(random, rollMin, rollMax);
        if (isWand(stack)) {
            budget *= config.wand.budgetFactor;
        } else if (rolls.focusBudgetMult != null) {
            budget *= rolls.focusBudgetMult;
        }
        return budget;
    }

    /**
     * The depth a carrier of this level rolls on a rarity row: the level tier, or the row's bonus tier once reached;
     * wands cap it.
     */
    public static int depth(SpellcraftConfig config, ItemStack stack, int level, String rarity, Random random) {
        SpellcraftConfig.DepthTier depthTier = SpellcraftConfig.depthTier(config.focus.depth, level);
        SpellcraftConfig.DepthTier bonus = config.focus.depthBonus.get(rarity);
        if (bonus != null && level >= bonus.minLevel) {
            depthTier = bonus;
        }
        int depth = depthTier == null ? 1 : depthTier.min + random.nextInt(depthTier.max - depthTier.min + 1);
        return isWand(stack) ? Math.min(depth, config.wand.maxDepth) : depth;
    }

    /**
     * One rolled line of a glyph at an item level: potency within the level's tier at a random quality, a cost that
     * follows the quality plus noise, and uses from the glyph's weights. A level below every tier rolls in the lowest
     * tier (unique sets grant their glyphs whatever the level).
     */
    public static GlyphLine rollLine(SpellcraftConfig config, GlyphType type, int level, Random random) {
        SpellcraftConfig.GlyphDefinition glyph = config.glyph(type);
        SpellcraftConfig.PotencyTier tier = SpellcraftConfig.potencyTier(glyph, level);
        if (tier == null) {
            tier = lowestTier(glyph);
        }
        float quality = random.nextFloat();
        float potency = tier.min + (tier.max - tier.min) * quality;
        int cost = Math.round(glyph.costMin + (glyph.costMax - glyph.costMin) * quality);
        if (glyph.costMax > glyph.costMin) {
            cost += noise(config, random);
        }
        int uses = 1 + weighted(glyph.useWeights, random);
        return new GlyphLine(type, potency, quality, Math.max(1, cost), uses);
    }

    private static SpellcraftConfig.PotencyTier lowestTier(SpellcraftConfig.GlyphDefinition glyph) {
        SpellcraftConfig.PotencyTier lowest = glyph.potency.tiers.get(0);
        for (SpellcraftConfig.PotencyTier tier : glyph.potency.tiers) {
            if (tier.minLevel < lowest.minLevel) {
                lowest = tier;
            }
        }
        return lowest;
    }

    private static GlyphType draw(SpellcraftConfig config, List<GlyphType> pool, Random random) {
        int index = pick(pool, type -> config.glyph(type).weight, random);
        return index < 0 ? pool.get(pool.size() - 1) : pool.get(index);
    }

    private static int noise(SpellcraftConfig config, Random random) {
        int index = pick(config.rules.costNoise, noise -> noise.weight, random);
        return index < 0 ? 0 : config.rules.costNoise.get(index).offset;
    }

    private static int weighted(List<Integer> weights, Random random) {
        int index = pick(weights, Integer::intValue, random);
        return index < 0 ? 0 : index;
    }

    private static <T> int pick(List<T> entries, ToIntFunction<T> weight, Random random) {
        int total = entries.stream().mapToInt(weight).sum();
        int pick = random.nextInt(Math.max(1, total));
        for (int i = 0; i < entries.size(); i++) {
            pick -= weight.applyAsInt(entries.get(i));
            if (pick < 0) {
                return i;
            }
        }
        return -1;
    }

    private static float between(Random random, float min, float max) {
        return min + (max - min) * random.nextFloat();
    }
}
