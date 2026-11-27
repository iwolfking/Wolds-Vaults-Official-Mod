package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import iskallia.vault.config.Config;
import xyz.iwolfking.woldsvaults.spellcraft.ability.AbilityTrait;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphStacking;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Spellcraft glyphs, generated as {@code vault_configs/spellcraft/spellcraft.json}: every glyph's rolls, cost, effects
 * and limits, how focuses and wands roll, the spell rules, mechanics tuning and which abilities take which glyphs.
 * Ability mana costs and cooldowns stay VH's, and glyphs only multiply them.
 */
public class SpellcraftConfig extends Config implements WoldsTable {
    @Expose public boolean worksOutsideVaults;
    @Expose public Rules rules;
    @Expose public Tuning tuning;
    @Expose public Map<String, GlyphDefinition> glyphs;
    @Expose public List<Combo> combos;
    @Expose public FocusRolls focus;
    @Expose public WandRolls wand;
    @Expose public Map<String, List<String>> abilityTraits;
    @Expose public List<String> disabledAbilities;
    @Expose public List<GearGlyph> gearGlyphs;
    @Expose public Map<String, UniqueSet> uniqueSets;
    @Expose public List<EtchingGlyph> etchingGlyphs;
    @Expose public List<String> removedEtchings;

    public static class Rules {
        @Expose public float escalationPerGlyph;
        @Expose public int abilityStepCost;
        @Expose public float chainCooldownShare;
        @Expose public float manaFactorFloor;
        @Expose public float glyphHasteHeadroom;
        @Expose public int maxGlyphsPerSpell;
        @Expose public int maxSpellsPerItem;
        @Expose public int maxCastsPerTrigger;
        @Expose public int maxBundleNesting;
        @Expose public int maxNameLength;
        @Expose public int defaultPriority;
        @Expose public int priorities;
        @Expose public int uniqueMixCost;
        @Expose public List<CostNoise> costNoise;
    }

    public static class CostNoise {
        @Expose public int offset;
        @Expose public int weight;
    }

    /** Mechanics numbers that are not rolled. */
    public static class Tuning {
        @Expose public float echoDelaySeconds;
        @Expose public float shotgunSpreadDegrees;
        @Expose public int homingRetargetTicks;
        @Expose public float homingRange;
        @Expose public float homingAimRange;
        @Expose public float homingAimConeDegrees;
        @Expose public float orbitRadius;
        @Expose public float orbitHeight;
        @Expose public int orbitVolleyExplosionGapTicks;
        @Expose public float bundleSpeed;
        @Expose public float railRange;
        @Expose public float sizeUpMaxScale;
        @Expose public float sizeDownMinScale;
        @Expose public float chargeSeconds;
        @Expose public float chargeMinShare;
        @Expose public float chargeMoveFactor;
    }

    /**
     * One glyph. {@code kind} is {@code modifier}, {@code delay} or {@code bundle}, and {@code class} ({@code common},
     * {@code special}, {@code unique} or {@code utility}) decides how its effects stack ({@link GlyphStacking}). A
     * modifier fits a target that has every trait of one {@code requires} set (a Bundled Spell orb has the single trait
     * {@code bundle}), none of the {@code forbids} traits and, when {@code specs} is not empty, one of those
     * specializations. {@code excludes} lists glyphs it never shares a step with. Step effects change the modified step
     * and spell effects the whole slot. With {@code scope} {@code step} the step effects reach the ability or orb the
     * glyph follows, with {@code spell} every live ability step and orb of the spell it fits.
     */
    public static class GlyphDefinition {
        public static final String SCOPE_STEP = "step";
        public static final String SCOPE_SPELL = "spell";

        @Expose public String kind;
        @Expose @SerializedName("class") public String glyphClass;
        @Expose public String category;
        @Expose public int minLevel;
        @Expose public int weight;
        @Expose public List<List<String>> requires;
        @Expose public List<String> forbids;
        @Expose public List<String> specs;
        @Expose public List<String> excludes;
        @Expose public Potency potency;
        @Expose public List<Effect> step;
        @Expose public List<Effect> spell;
        @Expose public int maxUses;
        @Expose public List<Integer> useWeights;
        @Expose public int capPerSpell;
        @Expose public int costMin;
        @Expose public int costMax;
        @Expose public boolean noEscalation;
        @Expose public String description;
        @Expose public String scope;

        /** Whether the step effects reach every step of the spell the glyph fits, not only the step it follows. */
        public boolean spellWide() {
            return SCOPE_SPELL.equals(this.scope);
        }

        public boolean isModifier() {
            return "modifier".equals(this.kind);
        }

        public boolean isDelay() {
            return "delay".equals(this.kind);
        }

        public boolean isBundle() {
            return "bundle".equals(this.kind);
        }

        public boolean isCommon() {
            return "common".equals(this.glyphClass);
        }

        public boolean isSpecial() {
            return "special".equals(this.glyphClass);
        }

        public boolean isUnique() {
            return "unique".equals(this.glyphClass);
        }

        public boolean isUtility() {
            return "utility".equals(this.glyphClass);
        }
    }

    /** The rolled main value: a range per item-level tier, plus values rolled at the same quality. */
    public static class Potency {
        @Expose public String label;
        @Expose public String format;
        @Expose public List<PotencyTier> tiers;
        @Expose public List<Linked> linked;
    }

    public static class PotencyTier {
        @Expose public int minLevel;
        @Expose public float min;
        @Expose public float max;
    }

    public static class Linked {
        @Expose public String name;
        @Expose public float min;
        @Expose public float max;
    }

    /**
     * A multiplier on a stat: {@code base + k * potency} ({@code base} defaults to 1), a {@code fixed} factor, or
     * {@code 1 + perUse} once per use. {@code role} is {@code upside}, {@code downside} or {@code cost}; with the
     * glyph's class it decides how the effect stacks ({@link GlyphStacking}). Step stats are {@link
     * GlyphStacking#STEP_STATS}, spell stats {@link GlyphStacking#SPELL_STATS}.
     */
    public static class Effect {
        @Expose public String stat;
        @Expose public String role;
        @Expose public Float base;
        @Expose public Float k;
        @Expose public Float fixed;
        @Expose public Float perUse;

        public float factor(float potency) {
            if (this.k != null) {
                return (this.base != null ? this.base : 1.0F) + this.k * potency;
            }
            if (this.fixed != null) {
                return this.fixed;
            }
            return this.perUse != null ? 1.0F + this.perUse : 1.0F;
        }
    }

    /** A flat arcana surcharge when a spell holds both glyphs. */
    public static class Combo {
        @Expose public String a;
        @Expose public String b;
        @Expose public int cost;
        @Expose public String reason;
    }

    public static class FocusRolls {
        @Expose public float budgetBase;
        @Expose public float budgetPerLevel;
        @Expose public float budgetRollMin;
        @Expose public float budgetRollMax;
        @Expose public Map<String, RarityRolls> rarities;
        @Expose public List<DepthTier> depth;
        @Expose public Map<String, DepthTier> depthBonus;
    }

    /** One rarity's rolls; {@code focusBudgetMult} multiplies the arcana budget on focuses only (wands keep theirs). */
    public static class RarityRolls {
        @Expose public float budget;
        @Expose public Float focusBudgetMult;
        @Expose public int linesMin;
        @Expose public int linesMax;
        @Expose public Float budgetRollMin;
        @Expose public Float budgetRollMax;
    }

    /** Depth: the most glyphs one spell on the item may hold. */
    public static class DepthTier {
        @Expose public int minLevel;
        @Expose public int min;
        @Expose public int max;
    }

    public static class WandRolls {
        @Expose public float budgetFactor;
        @Expose public int linesDelta;
        @Expose public int minLines;
        @Expose public int maxDepth;
    }

    /**
     * A gear affix that grants a unique glyph instead of acting on its own. {@code attribute} is the gear attribute id;
     * {@code key} the ability key of a keyed ability attribute or the special modification id of an {@code
     * ability_special_modification}, null for a plain attribute. The glyph's potency is the affix's rolled value,
     * negated when {@code orientation} is {@code lower} (the affix is stronger the lower it rolls), times {@code
     * scale}; a flag attribute grants potency {@code scale}. With {@code linked} set, the affix grants no line of its
     * own: it is the second half of the glyph another affix on the same item grants, its value places that line's
     * quality in the glyph's linked value of that name, and the cast reads it back from there.
     */
    public static class GearGlyph {
        @Expose public String attribute;
        @Expose public String key;
        @Expose public String glyph;
        @Expose public float scale;
        @Expose public String orientation;
        @Expose public String linked;

        public boolean lower() {
            return "lower".equals(this.orientation);
        }

        /** The affix value of the linked value at a line's quality, signed like the affix. */
        public float linkedValue(Linked linked, float quality) {
            float magnitude = (linked.min + (linked.max - linked.min) * quality) * this.scale;
            return lower() ? -magnitude : magnitude;
        }

        /** The glyph potency of a rolled affix value. */
        public float potency(float value) {
            return (lower() ? -value : value) * this.scale;
        }

        /** The affix value a glyph potency stands for (the inverse of {@link #potency}). */
        public float value(float potency) {
            float value = this.scale == 0.0F ? 0.0F : potency / this.scale;
            return lower() ? -value : value;
        }
    }

    /**
     * An etching that grants a unique glyph instead of acting on its own (the etching's effect runs from the glyph).
     * Its value is read part by part in the order of the attribute's value ({@code parts}, one for a plain value, two
     * for a pair, three for a throuple): the {@code potency} part becomes the glyph's potency ({@code offset + value *
     * scale}), a {@code linked} part places the line's quality in the glyph's linked value of that name, and a {@code
     * choice} part is one of {@code choices} (with more than one, the line's quality picks it). A {@code flag} etching
     * grants potency {@code offset + scale}.
     */
    public static class EtchingGlyph {
        public static final String LINGERING_CAST = "cast";
        public static final String LINGERING_TOTEM = "totem";

        @Expose public String attribute;
        @Expose public String glyph;
        @Expose public float scale;
        @Expose public float offset;
        @Expose public List<EtchingPart> parts;
        @Expose public List<String> choices;
        @Expose public String lingering;

        /** The glyph potency of a rolled value. */
        public float potency(float value) {
            return this.offset + value * this.scale;
        }

        /** The rolled value a glyph potency stands for (the inverse of {@link #potency}). */
        public float value(float potency) {
            return this.scale == 0.0F ? 0.0F : (potency - this.offset) / this.scale;
        }
    }

    /**
     * One part of an etching's value: its role ({@code potency}, {@code linked}, {@code choice}, {@code flag}), number
     * type ({@code int}, {@code float}) and linked value name.
     */
    public static class EtchingPart {
        @Expose public String role;
        @Expose public String type;
        @Expose public String linked;

        public boolean isInt() {
            return "int".equals(this.type);
        }
    }

    /**
     * A unique focus's or wand's fixed glyph set. Budget and depth roll from {@code rarityRow} ({@code
     * ancientRarityRow} for an ancient), with the wand rules for wands; the lines are every {@code always} glyph plus a
     * draw of {@code drawMin}-{@code drawMax} distinct glyphs from {@code draw} at equal odds.
     */
    public static class UniqueSet {
        @Expose public String rarityRow;
        @Expose public String ancientRarityRow;
        @Expose public List<SetGlyph> always;
        @Expose public List<String> draw;
        @Expose public int drawMin;
        @Expose public int drawMax;
    }

    /**
     * A signature glyph of a unique set; {@code uses} and {@code cap}, when set, replace the rolled uses and the type's
     * cap.
     */
    public static class SetGlyph {
        @Expose public String glyph;
        @Expose public Integer uses;
        @Expose public Integer cap;
    }

    @Override
    public String getName() {
        return "spellcraft";
    }

    @Override
    protected void reset() {
    }

    public GlyphDefinition glyph(GlyphType type) {
        return this.glyphs.get(type.id());
    }

    public boolean isDisabled(String abilityId) {
        return this.disabledAbilities.contains(abilityId);
    }

    /** The potency tier for an item level: the highest tier whose minimum level the item reaches. */
    @Nullable
    public static PotencyTier potencyTier(GlyphDefinition glyph, int itemLevel) {
        PotencyTier best = null;
        for (PotencyTier tier : glyph.potency.tiers) {
            if (tier.minLevel <= itemLevel && (best == null || tier.minLevel > best.minLevel)) {
                best = tier;
            }
        }
        return best;
    }

    @Nullable
    public static DepthTier depthTier(List<DepthTier> tiers, int itemLevel) {
        DepthTier best = null;
        for (DepthTier tier : tiers) {
            if (tier.minLevel <= itemLevel && (best == null || tier.minLevel > best.minLevel)) {
                best = tier;
            }
        }
        return best;
    }

    @Nullable
    @Override
    public String validate() {
        if (this.rules == null || this.tuning == null || this.glyphs == null || this.combos == null || this.focus == null
                || this.wand == null || this.abilityTraits == null || this.disabledAbilities == null) {
            return "a section is missing";
        }
        if (this.rules.costNoise == null || this.rules.costNoise.isEmpty()) {
            return "the cost noise table is empty";
        }
        if (this.rules.maxGlyphsPerSpell < 1 || this.rules.maxSpellsPerItem < 1 || this.rules.maxCastsPerTrigger < 1
                || this.rules.maxBundleNesting < 0 || this.rules.manaFactorFloor <= 0 || this.rules.maxNameLength < 1
                || this.rules.glyphHasteHeadroom < 1.0F) {
            return "a spell rule is out of range";
        }
        if (this.rules.priorities < 1 || this.rules.defaultPriority < 1 || this.rules.defaultPriority > this.rules.priorities
                || this.rules.uniqueMixCost < 0) {
            return "a priority or unique glyph rule is out of range";
        }
        if (this.tuning.sizeUpMaxScale < 1 || this.tuning.sizeDownMinScale <= 0 || this.tuning.sizeDownMinScale > 1
                || this.tuning.chargeSeconds <= 0 || this.tuning.homingRange <= 0 || this.tuning.homingAimRange <= 0
                || this.tuning.homingAimConeDegrees <= 0 || this.tuning.chargeMoveFactor <= 0 || this.tuning.chargeMoveFactor > 1
                || this.tuning.orbitVolleyExplosionGapTicks < 1) {
            return "a tuning value is out of range";
        }
        for (GlyphType type : GlyphType.values()) {
            GlyphDefinition glyph = this.glyphs.get(type.id());
            if (glyph == null) {
                return "glyph " + type.id() + " has no entry";
            }
            String problem = validateGlyph(glyph);
            if (problem != null) {
                return "glyph " + type.id() + " " + problem;
            }
        }
        for (GlyphType type : GlyphType.values()) {
            for (String other : this.glyphs.get(type.id()).excludes) {
                if (this.glyph(GlyphType.byId(other)).spellWide()) {
                    return "glyph " + type.id() + " excludes the spell-wide glyph " + other;
                }
            }
        }
        if (this.glyphs.size() != GlyphType.values().length) {
            return "it has entries for glyphs that do not exist";
        }
        for (Combo combo : this.combos) {
            if (GlyphType.byId(combo.a) == null || GlyphType.byId(combo.b) == null || combo.cost < 0) {
                return "combo " + combo.a + " + " + combo.b + " is invalid";
            }
        }
        if (this.focus.rarities == null || this.focus.rarities.isEmpty() || this.focus.depth == null
                || depthTier(this.focus.depth, 0) == null || this.focus.depthBonus == null) {
            return "the focus rolls are incomplete";
        }
        for (Map.Entry<String, RarityRolls> rarity : this.focus.rarities.entrySet()) {
            RarityRolls rolls = rarity.getValue();
            if (rolls.linesMin < 0 || rolls.linesMax < rolls.linesMin || rolls.budget < 0
                    || (rolls.focusBudgetMult != null && rolls.focusBudgetMult <= 0)) {
                return "rarity " + rarity.getKey() + " has an invalid roll";
            }
        }
        if (this.wand.minLines < 0 || this.wand.maxDepth < 1 || this.wand.budgetFactor < 0) {
            return "the wand rolls are invalid";
        }
        String gear = validateGearGlyphs();
        if (gear != null) {
            return gear;
        }
        String etchings = validateEtchingGlyphs();
        if (etchings != null) {
            return etchings;
        }
        String sets = validateUniqueSets();
        if (sets != null) {
            return sets;
        }
        for (Map.Entry<String, List<String>> ability : this.abilityTraits.entrySet()) {
            for (String trait : ability.getValue()) {
                try {
                    AbilityTrait.valueOf(trait.toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException e) {
                    return "ability " + ability.getKey() + " has an unknown trait " + trait;
                }
            }
        }
        return null;
    }

    @Nullable
    private String validateGearGlyphs() {
        if (this.gearGlyphs == null) {
            return null;
        }
        for (GearGlyph entry : this.gearGlyphs) {
            GlyphType type = entry.glyph == null ? null : GlyphType.byId(entry.glyph);
            if (entry.attribute == null || type == null) {
                return "gear glyph " + entry.attribute + " names no attribute or an unknown glyph " + entry.glyph;
            }
            if (!this.glyphs.get(type.id()).isUnique()) {
                return "gear glyph " + entry.attribute + " grants " + type.id() + ", which is not a unique glyph";
            }
            if (entry.scale < 0.0F || (entry.orientation != null && !List.of("higher", "lower").contains(entry.orientation))) {
                return "gear glyph " + entry.attribute + " has an invalid scale or orientation";
            }
            if (entry.linked != null) {
                GlyphDefinition glyph = this.glyphs.get(type.id());
                boolean found = glyph.potency != null && glyph.potency.linked != null
                        && glyph.potency.linked.stream().anyMatch(linked -> entry.linked.equals(linked.name));
                if (!found) {
                    return "gear glyph " + entry.attribute + " fills linked value " + entry.linked + ", which " + type.id() + " does not have";
                }
            }
        }
        return null;
    }

    @Nullable
    private String validateEtchingGlyphs() {
        if (this.etchingGlyphs == null) {
            return null;
        }
        for (EtchingGlyph entry : this.etchingGlyphs) {
            GlyphType type = entry.glyph == null ? null : GlyphType.byId(entry.glyph);
            String name = "etching glyph " + entry.attribute;
            if (entry.attribute == null || type == null || entry.parts == null || entry.parts.isEmpty() || entry.parts.size() > 3) {
                return name + " names no attribute, an unknown glyph " + entry.glyph + " or no value parts";
            }
            GlyphDefinition glyph = this.glyphs.get(type.id());
            if (!glyph.isUnique()) {
                return name + " grants " + type.id() + ", which is not a unique glyph";
            }
            if (entry.lingering != null) {
                if (!List.of(EtchingGlyph.LINGERING_CAST, EtchingGlyph.LINGERING_TOTEM).contains(entry.lingering)) {
                    return name + " has an unknown lingering source " + entry.lingering;
                }
                if (glyph.specs == null || glyph.specs.size() != 1) {
                    return name + " is lingering, so " + type.id() + " must be locked to exactly one ability";
                }
            }
            int potency = 0;
            int quality = 0;
            for (EtchingPart part : entry.parts) {
                if (part.role == null || !List.of("potency", "linked", "choice", "flag").contains(part.role)) {
                    return name + " has a part with an unknown role " + part.role;
                }
                if (("potency".equals(part.role) || "linked".equals(part.role)) && !List.of("int", "float").contains(part.type)) {
                    return name + " has a number part without an int or float type";
                }
                if ("potency".equals(part.role) || "flag".equals(part.role)) {
                    potency++;
                }
                if ("linked".equals(part.role)) {
                    quality++;
                    if (part.linked == null || glyph.potency.linked == null
                            || glyph.potency.linked.stream().noneMatch(linked -> part.linked.equals(linked.name))) {
                        return name + " links " + part.linked + ", which " + type.id() + " has no linked value for";
                    }
                }
                if ("choice".equals(part.role)) {
                    if (entry.choices == null || entry.choices.isEmpty()) {
                        return name + " has a choice part but no choices";
                    }
                    if (entry.choices.size() > 1) {
                        quality++;
                    }
                }
            }
            if (potency != 1 || quality > 1 || entry.scale == 0.0F) {
                return name + " needs exactly one potency or flag part, at most one quality part and a non-zero scale";
            }
        }
        if (this.removedEtchings == null) {
            return "the removed etchings list is missing";
        }
        return null;
    }

    @Nullable
    private String validateUniqueSets() {
        if (this.uniqueSets == null) {
            return null;
        }
        for (Map.Entry<String, UniqueSet> entry : this.uniqueSets.entrySet()) {
            UniqueSet set = entry.getValue();
            String name = "unique set " + entry.getKey();
            if (set.always == null || set.draw == null || set.rarityRow == null || set.ancientRarityRow == null) {
                return name + " is missing a field";
            }
            if (this.focus != null && this.focus.rarities != null
                    && (!this.focus.rarities.containsKey(set.rarityRow) || !this.focus.rarities.containsKey(set.ancientRarityRow))) {
                return name + " names a rarity row the focus rolls lack";
            }
            if (set.drawMin < 0 || set.drawMax < set.drawMin || set.drawMax > set.draw.size()) {
                return name + " draws " + set.drawMin + "-" + set.drawMax + " from a list of " + set.draw.size();
            }
            for (SetGlyph always : set.always) {
                if (always.glyph == null || GlyphType.byId(always.glyph) == null || (always.uses != null && always.uses < 1)
                        || (always.cap != null && always.cap < 1)) {
                    return name + " has an invalid signature glyph " + always.glyph;
                }
                if (set.draw.contains(always.glyph)) {
                    return name + " both always grants and draws " + always.glyph;
                }
            }
            if (set.draw.stream().anyMatch(id -> GlyphType.byId(id) == null) || set.draw.stream().distinct().count() != set.draw.size()) {
                return name + " draws an unknown or repeated glyph";
            }
        }
        return null;
    }

    @Nullable
    private static String validateGlyph(GlyphDefinition glyph) {
        if (glyph.kind == null || !List.of("modifier", "delay", "bundle").contains(glyph.kind)) {
            return "has an unknown kind";
        }
        if (glyph.glyphClass == null || !GlyphStacking.CLASSES.contains(glyph.glyphClass)) {
            return "has an unknown class";
        }
        if (glyph.potency == null || glyph.potency.tiers == null || glyph.potency.tiers.isEmpty() || glyph.step == null
                || glyph.spell == null || glyph.requires == null || glyph.forbids == null || glyph.specs == null
                || glyph.excludes == null || glyph.useWeights == null) {
            return "is missing a field";
        }
        for (List<String> set : glyph.requires) {
            for (String trait : set) {
                if (!GlyphType.BUNDLE_TRAIT.equals(trait) && !isTrait(trait)) {
                    return "requires an unknown trait " + trait;
                }
            }
        }
        for (String trait : glyph.forbids) {
            if (!isTrait(trait)) {
                return "forbids an unknown trait " + trait;
            }
        }
        if (glyph.maxUses < 1 || glyph.useWeights.size() != glyph.maxUses || glyph.capPerSpell < 1
                || glyph.costMin < 0 || glyph.costMax < glyph.costMin || glyph.weight < 0) {
            return "has an invalid roll";
        }
        for (PotencyTier tier : glyph.potency.tiers) {
            if (tier.max < tier.min) {
                return "has a potency tier with max below min";
            }
        }
        for (Effect effect : glyph.step) {
            if (!GlyphStacking.STEP_STATS.contains(effect.stat)) {
                return "has an unknown step stat " + effect.stat;
            }
            String problem = validateEffect(glyph, effect);
            if (problem != null) {
                return problem;
            }
        }
        for (Effect effect : glyph.spell) {
            if (!GlyphStacking.SPELL_STATS.contains(effect.stat)) {
                return "has an unknown spell stat " + effect.stat;
            }
            String problem = validateEffect(glyph, effect);
            if (problem != null) {
                return problem;
            }
        }
        for (String other : glyph.excludes) {
            if (GlyphType.byId(other) == null) {
                return "excludes an unknown glyph " + other;
            }
        }
        if (!GlyphDefinition.SCOPE_STEP.equals(glyph.scope) && !GlyphDefinition.SCOPE_SPELL.equals(glyph.scope)) {
            return "has an unknown scope " + glyph.scope;
        }
        if (glyph.spellWide() && (!glyph.isModifier() || !glyph.excludes.isEmpty())) {
            return "is spell-wide but is not a modifier or excludes other glyphs";
        }
        return null;
    }

    @Nullable
    private static String validateEffect(GlyphDefinition glyph, Effect effect) {
        if (effect.role == null || !GlyphStacking.ROLES.contains(effect.role)) {
            return "has an effect on " + effect.stat + " with an unknown role";
        }
        int modes = (effect.k != null ? 1 : 0) + (effect.fixed != null ? 1 : 0) + (effect.perUse != null ? 1 : 0);
        if (modes != 1 || (effect.base != null && effect.k == null)) {
            return "has an effect on " + effect.stat + " that is not exactly one of k, fixed or perUse";
        }
        if (glyph.isUtility() && "upside".equals(effect.role)) {
            return "is a utility glyph with an upside on " + effect.stat;
        }
        return null;
    }

    private static boolean isTrait(String trait) {
        try {
            AbilityTrait.valueOf(trait.toUpperCase(Locale.ROOT));
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
