package xyz.iwolfking.woldsvaults.augments.item;

import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.item.gear.FocusItem;
import iskallia.vault.item.gear.WandItem;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.augments.AugmentType;
import xyz.iwolfking.woldsvaults.augments.FocusAugments;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Rolls a focus's or wand's spellcraft implicits when it is identified: arcana from item level and rarity, glyph lines
 * by rarity (distinct types, weighted, level-gated), each with a potency in its level tier, a cost that follows the
 * potency roll plus noise, and a number of uses; and depth (glyphs per spell) by level. Wands roll a smaller budget,
 * fewer lines and a lower depth cap.
 */
public final class SpellcraftRoller {
    private static UniqueSets uniqueSets;

    private SpellcraftRoller() {
    }

    /**
     * The unique-set hook (R30, loadout spec sec. 6.1): a unique focus or wand with a set rolls its lines from the set and
     * its budget and depth from the set's rarity row, and a unique rolled before sets existed converts on first read.
     * Until a provider is installed uniques roll like any other carrier.
     */
    public interface UniqueSets {
        /** Rolls a unique's implicits from its set; false when the unique has no set, so it rolls as any carrier. */
        boolean roll(FocusAugmentsConfig config, ItemStack stack, VaultGearData data, Random random);

        /**
         * Converts a unique whose implicits predate its set: new lines, budget and depth through
         * {@link SpellcraftItemData#replaceImplicits}, spells kept. Returns true when it changed the item.
         */
        boolean convert(FocusAugmentsConfig config, ItemStack stack, VaultGearData data, Random random);
    }

    /** Installs the unique-set provider; call during common setup. */
    public static void setUniqueSets(UniqueSets sets) {
        uniqueSets = sets;
    }

    public static boolean canCarry(ItemStack stack) {
        return stack.getItem() instanceof FocusItem || stack.getItem() instanceof WandItem;
    }

    public static boolean isWand(ItemStack stack) {
        return stack.getItem() instanceof WandItem;
    }

    public static void rollIfMissing(ItemStack stack, Random random) {
        if (canCarry(stack) && !SpellcraftItemData.has(stack)) {
            roll(stack, random);
        }
    }

    /**
     * The server's inventory scan for one stack: rolls implicits for an identified carrier that has none (identified
     * before spellcraft existed, or through a path that skips the roll hook) and lets the unique-set hook convert old
     * uniques.
     */
    public static void upkeep(ItemStack stack, Random random) {
        if (!canCarry(stack)) {
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
        FocusAugmentsConfig config = FocusAugments.config();
        if (uniqueSets != null && config != null && data.getRarity() == VaultGearRarity.UNIQUE) {
            uniqueSets.convert(config, stack, data, random);
        }
    }

    /** Rolls fresh implicits, replacing any spells on the item. Logs and leaves the item alone if it can't. */
    public static void roll(ItemStack stack, Random random) {
        FocusAugmentsConfig config = FocusAugments.config();
        if (config == null || !canCarry(stack)) {
            return;
        }
        VaultGearData data = VaultGearData.read(stack);
        if (uniqueSets != null && data.getRarity() == VaultGearRarity.UNIQUE && uniqueSets.roll(config, stack, data, random)) {
            return;
        }
        int level = data.getItemLevel();
        String rarity = data.getRarity().name();
        FocusAugmentsConfig.FocusRolls focus = config.focus;
        FocusAugmentsConfig.RarityRolls rolls = focus.rarities.get(rarity);
        if (rolls == null) {
            WoldsVaults.LOGGER.error("Spellcraft: no focus roll for rarity {}; {} rolls no spellcraft implicits", rarity, stack.getItem());
            return;
        }
        float rollMin = rolls.budgetRollMin != null ? rolls.budgetRollMin : focus.budgetRollMin;
        float rollMax = rolls.budgetRollMax != null ? rolls.budgetRollMax : focus.budgetRollMax;
        float budget = (focus.budgetBase + focus.budgetPerLevel * level) * rolls.budget * between(random, rollMin, rollMax);
        int lineCount = rolls.linesMin + random.nextInt(rolls.linesMax - rolls.linesMin + 1);
        FocusAugmentsConfig.DepthTier depthTier = FocusAugmentsConfig.depthTier(focus.depth, level);
        FocusAugmentsConfig.DepthTier bonus = focus.depthBonus.get(rarity);
        if (bonus != null && level >= bonus.minLevel) {
            depthTier = bonus;
        }
        int depth = depthTier == null ? 1 : depthTier.min + random.nextInt(depthTier.max - depthTier.min + 1);
        if (isWand(stack)) {
            budget *= config.wand.budgetFactor;
            lineCount = Math.max(config.wand.minLines, lineCount + config.wand.linesDelta);
            depth = Math.min(depth, config.wand.maxDepth);
        } else if (rolls.focusBudgetMult != null) {
            budget *= rolls.focusBudgetMult;
        }
        List<AugmentType> pool = new ArrayList<>();
        for (AugmentType type : AugmentType.values()) {
            FocusAugmentsConfig.Glyph glyph = config.glyph(type);
            if (glyph.minLevel <= level && glyph.weight > 0 && FocusAugmentsConfig.potencyTier(glyph, level) != null) {
                pool.add(type);
            }
        }
        List<SpellcraftItemData.Line> lines = new ArrayList<>();
        for (int i = 0; i < lineCount && !pool.isEmpty(); i++) {
            AugmentType type = draw(config, pool, random);
            pool.remove(type);
            lines.add(rollLine(config, type, level, random));
        }
        SpellcraftItemData.writeImplicits(stack, random.nextLong(), Math.max(0, Math.round(budget)), Math.max(1, depth), lines);
    }

    private static SpellcraftItemData.Line rollLine(FocusAugmentsConfig config, AugmentType type, int level, Random random) {
        FocusAugmentsConfig.Glyph glyph = config.glyph(type);
        FocusAugmentsConfig.PotencyTier tier = FocusAugmentsConfig.potencyTier(glyph, level);
        float quality = random.nextFloat();
        float potency = tier.min + (tier.max - tier.min) * quality;
        int cost = Math.round(glyph.costMin + (glyph.costMax - glyph.costMin) * quality);
        if (glyph.costMax > glyph.costMin) {
            cost += noise(config, random);
        }
        int uses = 1 + weighted(glyph.useWeights, random);
        return new SpellcraftItemData.Line(type, potency, quality, Math.max(1, cost), uses);
    }

    private static AugmentType draw(FocusAugmentsConfig config, List<AugmentType> pool, Random random) {
        int total = pool.stream().mapToInt(type -> config.glyph(type).weight).sum();
        int pick = random.nextInt(Math.max(1, total));
        for (AugmentType type : pool) {
            pick -= config.glyph(type).weight;
            if (pick < 0) {
                return type;
            }
        }
        return pool.get(pool.size() - 1);
    }

    private static int noise(FocusAugmentsConfig config, Random random) {
        int total = config.rules.costNoise.stream().mapToInt(noise -> noise.weight).sum();
        int pick = random.nextInt(Math.max(1, total));
        for (FocusAugmentsConfig.CostNoise noise : config.rules.costNoise) {
            pick -= noise.weight;
            if (pick < 0) {
                return noise.offset;
            }
        }
        return 0;
    }

    private static int weighted(List<Integer> weights, Random random) {
        int total = weights.stream().mapToInt(Integer::intValue).sum();
        int pick = random.nextInt(Math.max(1, total));
        for (int i = 0; i < weights.size(); i++) {
            pick -= weights.get(i);
            if (pick < 0) {
                return i;
            }
        }
        return 0;
    }

    private static float between(Random random, float min, float max) {
        return min + (max - min) * random.nextFloat();
    }
}
