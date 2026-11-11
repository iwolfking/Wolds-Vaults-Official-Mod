package xyz.iwolfking.woldsvaults.client.spellcraft;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.init.ModEffects;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.mana.Mana;
import iskallia.vault.skill.ability.component.AbilityLabelBindingRegistry;
import iskallia.vault.skill.ability.effect.ShieldBashAbility;
import iskallia.vault.skill.ability.effect.TauntCharmAbility;
import iskallia.vault.skill.ability.effect.TotemPlayerDamageAbility;
import iskallia.vault.skill.ability.effect.spi.AbstractIceBoltAbility;
import iskallia.vault.skill.ability.effect.spi.AbstractRampageAbility;
import iskallia.vault.skill.ability.effect.spi.core.Ability;
import iskallia.vault.skill.tree.AbilityTree;
import iskallia.vault.snapshot.AttributeSnapshot;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import iskallia.vault.util.calc.AbilityPowerHelper;
import iskallia.vault.util.calc.AreaOfEffectHelper;
import iskallia.vault.util.calc.AttributeLimitHelper;
import iskallia.vault.util.calc.BlockChanceHelper;
import iskallia.vault.util.calc.EffectDurationHelper;
import iskallia.vault.util.calc.PlayerStat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.augments.cast.CastModifiers;
import xyz.iwolfking.woldsvaults.augments.effect.BuffGlyphs;
import xyz.iwolfking.woldsvaults.augments.spell.GlyphRelevance;
import xyz.iwolfking.woldsvaults.augments.spell.GlyphStacking;
import xyz.iwolfking.woldsvaults.augments.spell.SpellModel;
import xyz.iwolfking.woldsvaults.augments.spell.SpellcraftRules;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;
import xyz.iwolfking.woldsvaults.spellcraft.bar.SpellCooldowns;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * What one press of a slot does at a training dummy, for the spell bar tooltip and the focus screen. The per-ability
 * numbers are VH's own description labels ({@code AbilityLabelBindingRegistry}, the values behind {@code $damage},
 * {@code $radius} ...), read against the player's live stats the way VH's label factory reads them; the step's glyphs
 * are then applied as the caster applies them.
 * <p>
 * Damage per hit is flat + attack damage x percent x (1 + Damage Increase) + ability power x percent + current mana x
 * percent. Damage Increase is counted because every attack-damage ability hit receives it (baked into the ability or in
 * the hurt event) and no ability-power hit does. Target-dependent multipliers (mob type, gods, armour, crits, distance
 * falloff) and temporary buffs are left out. Radius is VH's area-of-effect radius for the ability, with the step's area
 * factor multiplied into the AoE factor before VH's AoE limit clamps it; velocity is VH's throw power (blocks per tick,
 * shown per second) times the step's speed; duration is VH's effect-duration-adjusted duration.
 */
public final class ClientAbilityStats {
    private static final String NO_BINDING = "NO BINDING";
    private static final String[] AREA_KEYS = {"adjustedRadius", "radius"};
    private static final String[] INTERVAL_KEYS = {"damageInterval", "stormInterval", "shockInterval"};
    private static final String[] COUNT_KEYS = {"shockCount", "boltCount", "numberOfJavelins", "numberOfBounces", "piercing", "maxTargets"};
    private static final String[] AMOUNT_KEYS = {"heal", "barrierPoints"};
    private static final Set<String> LOGGED = new HashSet<>();
    private static final Map<String, Effects> EFFECTS = effects();

    private ClientAbilityStats() {
    }

    /** A damage value, or a min-max range for abilities that roll a random percent. */
    public record Hit(float min, float max) {
        public Hit times(float factor) {
            return new Hit(this.min * factor, this.max * factor);
        }

        public Hit plus(Hit other) {
            return new Hit(this.min + other.min, this.max + other.max);
        }

        public boolean isRange() {
            return this.max - this.min > Math.max(1.0E-3F, Math.abs(this.max) * 1.0E-3F);
        }
    }

    /**
     * One more number VH labels for the ability: the line's lang key (under {@code bar.stat.}), its formatted value and
     * the value's colour.
     */
    public record Extra(String key, String value, int color) {
    }

    /** Which buff factor of a step a value takes, as the WV buff mixins apply it on the server. */
    private enum Factor {
        NONE,
        INTENSITY,
        DURATION,
        UPSIDE,
        DRAIN
    }

    /** How a VH label reads and how its value is shown. */
    private enum Format {
        PERCENT,
        NUMBER,
        INTEGER,
        BLOCKS,
        TICKS,
        PER_SECOND
    }

    /** One main effect line: the VH label it reads, the line's lang key, the label's format and the factor it takes. */
    private record Main(String label, String key, Format format, Factor factor) {
    }

    /**
     * What a spec's glyphs change beyond damage, area and speed: its main effect lines (shown first), the factor on its
     * hit and on its duration, and whether VH binds its duration as a percent label (Mana Shield).
     */
    private record Effects(List<Main> mains, Factor hit, Factor duration, boolean percentDuration) {
    }

    /** A main effect's raw label value: a fraction for percents, ticks for times, else the number. */
    private record MainValue(Main main, float raw) {
    }

    /** An ability's numbers before glyphs; negative radius, velocity or duration means the ability has none. */
    public record Base(@Nullable Hit hit, @Nullable Hit splinter, @Nullable Hit splash, float radius, boolean aoe, float splashRadius,
                       float velocity, int durationTicks, List<Extra> extras, @Nullable Effects effects, List<MainValue> mains) {
    }

    /** A row of the spell's stats: an ability step or a Bundled Spell orb, at its payload depth. */
    public sealed interface Row permits Step, Orb {
        int depth();
    }

    /**
     * One ability step after its glyphs. {@code casts} counts Shotgun copies and the orbs carrying it; {@code total} is
     * one hit from every cast plus the Echo replay, or null when the step casts once with no Echo; the charged values are
     * at full Charge, or null without Charge. {@code mains} are the ability's main effect lines after the step's buff
     * glyphs; {@code relevance} says which of the step's glyph stats apply to it.
     */
    public record Step(String group, int depth, @Nullable Hit hit, @Nullable Hit charged, @Nullable Hit splinter, @Nullable Hit splash,
                       int casts, float echoPower, @Nullable Hit total, @Nullable Hit chargedTotal, float radius, float splashRadius,
                       float velocity, int durationTicks, List<Extra> mains, List<Extra> extras, @Nullable GlyphStacking.Pool glyphs,
                       GlyphRelevance.Filter relevance) implements Row {
        public boolean hasStats() {
            return this.hit != null || this.splinter != null || this.splash != null || this.radius >= 0.0F || this.splashRadius >= 0.0F
                    || this.velocity >= 0.0F || this.durationTicks >= 0 || !this.mains.isEmpty() || !this.extras.isEmpty();
        }
    }

    /** A Bundled Spell orb: how many orbs fly, their velocity in blocks per second, and the flight time in seconds. */
    public record Orb(int depth, int copies, float velocity, float flightSeconds, GlyphStacking.Pool glyphs,
                      GlyphRelevance.Filter relevance) implements Row {
    }

    /** Every row of a spell, and the whole press's damage when more than one step deals damage (else null). */
    public record SpellStats(List<Row> rows, @Nullable Hit total) {
    }

    @Nullable
    private static LocalPlayer player() {
        return Minecraft.getInstance().player;
    }

    /** A plain ability's stats as one step without glyphs, or null when it isn't learned or there is no player. */
    @Nullable
    public static Step plain(AbilityTree tree, String group) {
        LocalPlayer player = player();
        Ability ability = SpellCooldowns.ability(tree, group);
        if (player == null || ability == null) {
            return null;
        }
        String spec = SpellcraftRules.specOf(tree).apply(group);
        return step(player, group, base(player, ability, spec), CastModifiers.NONE, BuffGlyphs.Factors.NONE, 1.0F, 1, false, 0, null,
                GlyphRelevance.ALL);
    }

    /** An augmented spell's rows: each ability step with its glyphs, each Bundled Spell orb and its payload. */
    @Nullable
    public static SpellStats spell(FocusAugmentsConfig config, AbilityTree tree, SpellModel model) {
        LocalPlayer player = player();
        if (player == null) {
            return null;
        }
        List<Row> rows = new ArrayList<>();
        walk(player, config, tree, model, model.steps(), 1.0F, 1, true, 0, rows);
        Hit total = null;
        int damaging = 0;
        for (Row row : rows) {
            if (row instanceof Step step && step.hit() != null) {
                Hit press = step.total() != null ? step.total() : step.hit().times(step.casts());
                total = total == null ? press : total.plus(press);
                damaging++;
            }
        }
        return new SpellStats(rows, damaging > 1 ? total : null);
    }

    private static void walk(LocalPlayer player, FocusAugmentsConfig config, AbilityTree tree, SpellModel model, List<SpellModel.Step> steps,
                             float damageFactor, int multiplicity, boolean root, int depth, List<Row> rows) {
        for (SpellModel.Step step : steps) {
            if (step instanceof SpellModel.Cast cast) {
                Ability ability = SpellCooldowns.ability(tree, cast.group());
                Base base = ability == null ? null : base(player, ability, cast.spec());
                CastModifiers modifiers = CastModifiers.of(config, cast.glyphs());
                BuffGlyphs.Factors factors = ability == null ? BuffGlyphs.Factors.NONE : BuffGlyphs.factorsOf(ability, modifiers);
                rows.add(step(player, cast.group(), base, modifiers, factors, damageFactor, multiplicity, root, depth, model.stepPool(cast),
                        GlyphRelevance.of(config, model, cast)));
            } else if (step instanceof SpellModel.Bundle bundle) {
                CastModifiers orb = CastModifiers.of(config, bundle.glyphs());
                int copies = 1 + orb.shotgunCopies();
                rows.add(new Orb(depth, copies * multiplicity, (float) (config.tuning.bundleSpeed * orb.speed() * 20.0), bundle.glyph().potency(),
                        model.stepPool(bundle), GlyphRelevance.of(config, model, bundle)));
                walk(player, config, tree, model, bundle.payload(), damageFactor * orb.damage(), multiplicity * copies, false, depth + 1, rows);
            }
        }
    }

    private static Step step(LocalPlayer player, String group, @Nullable Base base, CastModifiers modifiers, BuffGlyphs.Factors factors,
                             float damageFactor, int multiplicity, boolean root, int depth, @Nullable GlyphStacking.Pool glyphs,
                             GlyphRelevance.Filter relevance) {
        int casts = (1 + modifiers.shotgunCopies()) * multiplicity;
        float echo = modifiers.echoPower();
        if (base == null) {
            return new Step(group, depth, null, null, null, null, casts, echo, null, null, -1.0F, -1.0F, -1.0F, -1, List.of(), List.of(), glyphs,
                    relevance);
        }
        Effects effects = base.effects();
        float factor = modifiers.damage() * damageFactor * (effects == null ? 1.0F : value(effects.hit(), factors));
        Hit hit = base.hit() == null ? null : base.hit().times(factor);
        Hit charged = hit != null && root && modifiers.chargeBonus() > 0.0F ? hit.times(1.0F + modifiers.chargeBonus()) : null;
        Hit total = null;
        Hit chargedTotal = null;
        if (hit != null && (casts > 1 || echo > 0.0F)) {
            Hit echoed = hit.times(echo * casts);
            total = hit.times(casts).plus(echoed);
            chargedTotal = charged == null ? null : charged.times(casts).plus(echoed);
        }
        Hit splinter = base.splinter() == null ? null : base.splinter().times(factor);
        Hit splash = base.splash() == null ? null : base.splash().times(factor);
        float radius = base.radius() >= 0.0F && base.aoe() ? base.radius() * areaRatio(player, modifiers.area()) : base.radius();
        float velocity = base.velocity() >= 0.0F ? base.velocity() * modifiers.speed() : -1.0F;
        int duration = base.durationTicks() >= 0 && effects != null
                ? BuffGlyphs.scale(base.durationTicks(), value(effects.duration(), factors)) : base.durationTicks();
        List<Extra> mains = new ArrayList<>();
        for (MainValue main : base.mains()) {
            mains.add(format(main, value(main.main().factor(), factors)));
        }
        return new Step(group, depth, hit, charged, splinter, splash, casts, echo, total, chargedTotal, radius, base.splashRadius(), velocity,
                duration, mains, base.extras(), glyphs, relevance);
    }

    private static float value(Factor factor, BuffGlyphs.Factors factors) {
        return switch (factor) {
            case NONE -> 1.0F;
            case INTENSITY -> factors.intensity();
            case DURATION -> factors.duration();
            case UPSIDE -> factors.durationUpside();
            case DRAIN -> factors.drain();
        };
    }

    /** A main effect line at a factor, rounded as the server rounds it (integer stats and ticks to nearest, minimum 1). */
    private static Extra format(MainValue main, float factor) {
        Main def = main.main();
        int color = def.format() == Format.TICKS || def.factor() == Factor.DRAIN ? SpellcraftGui.STAT_YELLOW : SpellcraftGui.STAT_INTENSITY;
        String value = switch (def.format()) {
            case PERCENT -> SpellcraftGui.tr("fmt.percent", compact(main.raw() * factor * 100.0F));
            case NUMBER -> compact(main.raw() * factor);
            case INTEGER -> String.valueOf(BuffGlyphs.scale(Math.round(main.raw()), factor));
            case BLOCKS -> SpellcraftGui.tr("fmt.blocks", compact(main.raw() * factor));
            case TICKS -> SpellcraftGui.tr("fmt.seconds", SpellcraftGui.seconds(BuffGlyphs.scale(Math.round(main.raw()), factor)));
            case PER_SECOND -> SpellcraftGui.tr("fmt.per_second", compact(main.raw() * factor));
        };
        return new Extra(def.key(), value, color);
    }

    /** VH's numbers for one ability against the player's current stats, before any glyph; {@code spec} picks its main effects. */
    public static Base base(LocalPlayer player, Ability ability, @Nullable String spec) {
        AttributeSnapshot snapshot = AttributeSnapshotHelper.getInstance().getSnapshot(player);
        float damageIncrease = snapshot.getAttributeValue(ModGearAttributes.DAMAGE_INCREASE, VaultGearAttributeTypeMerger.floatSum());
        float attackDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE) * (1.0F + damageIncrease);
        Hit hit = hit(player, ability, attackDamage);
        Float splinterPercent = percent(ability, "splinterDamage");
        Float splashPercent = percent(ability, "splashDamage");
        Hit splinter = hit == null || splinterPercent == null ? null : hit.times(splinterPercent);
        Hit splash = hit == null || splashPercent == null ? null : hit.times(splashPercent);
        float radius = -1.0F;
        boolean aoe = false;
        for (String key : AREA_KEYS) {
            Float value = number(ability, key);
            if (value != null) {
                aoe = key.equals("adjustedRadius");
                radius = aoe ? AreaOfEffectHelper.adjustAreaOfEffect(player, ability, value) : value;
                break;
            }
        }
        Float splashBase = number(ability, "splashRadius");
        float splashRadius = splashBase == null ? -1.0F : AreaOfEffectHelper.adjustAreaOfEffect(player, ability, splashBase);
        Float throwPower = number(ability, "throwPower");
        float velocity = throwPower == null ? -1.0F : throwPower * 20.0F;
        Effects effects = spec == null ? null : EFFECTS.get(spec);
        Integer adjusted = ticks(ability, "adjustedDuration");
        Integer plainDuration = ticks(ability, "duration");
        int duration = adjusted != null ? EffectDurationHelper.adjustEffectDurationFloor(player, adjusted) : plainDuration != null ? plainDuration : -1;
        if (duration < 0 && effects != null && effects.percentDuration()) {
            Float bound = percent(ability, "duration");
            duration = bound == null ? -1 : Math.round(bound * 100.0F);
        }
        List<MainValue> mains = new ArrayList<>();
        Set<String> covered = new HashSet<>();
        if (effects != null) {
            for (Main main : effects.mains()) {
                Float raw = raw(ability, main);
                if (raw != null) {
                    mains.add(new MainValue(main, raw));
                    covered.add(main.label());
                } else {
                    missingLabel(ability, spec, main.label());
                }
            }
        }
        return new Base(hit, splinter, splash, radius, aoe, splashRadius, velocity, duration, extras(ability, covered), effects, mains);
    }

    @Nullable
    private static Float raw(Ability ability, Main main) {
        return switch (main.format()) {
            case PERCENT -> percent(ability, main.label());
            case NUMBER, INTEGER, BLOCKS -> number(ability, main.label());
            case TICKS -> {
                Integer ticks = ticks(ability, main.label());
                yield ticks == null ? null : ticks.floatValue();
            }
            case PER_SECOND -> {
                String value = binding(ability, main.label());
                yield value == null ? null : parse(ability, main.label(), value.endsWith("/s") ? value.substring(0, value.length() - 2) : value);
            }
        };
    }

    private static void missingLabel(Ability ability, String spec, String label) {
        String id = spec + "." + label;
        if (LOGGED.add(id)) {
            WoldsVaults.LOGGER.warn("Spellcraft stats: {} ({}) has no VH label '{}'; its main effect line is left out", spec,
                    ability.getClass().getSimpleName(), label);
        }
    }

    @Nullable
    private static Hit hit(LocalPlayer player, Ability ability, float attackDamage) {
        float flat = 0.0F;
        float adPercent = 0.0F;
        boolean any = false;
        if (ability instanceof AbstractIceBoltAbility bolt) {
            flat += (float) bolt.getDamagePerBolt();
            adPercent += bolt.getPercentAttackDamageDealt();
            any = true;
        } else if (ability instanceof ShieldBashAbility) {
            Float scalar = percent(ability, "damage");
            if (scalar != null) {
                adPercent += 1.0F + BlockChanceHelper.getBlockChance(player) * scalar;
                any = true;
            }
        } else if (!(ability instanceof AbstractRampageAbility || ability instanceof TotemPlayerDamageAbility || ability instanceof TauntCharmAbility)) {
            Float percent = percent(ability, "damage");
            if (percent != null) {
                adPercent += percent;
                any = true;
            }
        }
        for (String key : new String[]{"damageMultiplier", "shardAttackDamage"}) {
            Float percent = percent(ability, key);
            if (percent != null) {
                adPercent += percent;
                any = true;
            }
        }
        for (String key : new String[]{"baseDamage", "shardDamage"}) {
            Float value = number(ability, key);
            if (value != null) {
                flat += value;
                any = true;
            }
        }
        float apMin = 0.0F;
        float apMax = 0.0F;
        for (String key : new String[]{"ability_power", "minionDamage"}) {
            Float percent = percent(ability, key);
            if (percent != null) {
                apMin += percent;
                apMax += percent;
                any = true;
            }
        }
        Float rangeMin = percent(ability, "ability_power_min");
        Float rangeMax = percent(ability, "ability_power_max");
        if (rangeMin != null && rangeMax != null) {
            apMin += rangeMin;
            apMax += rangeMax;
            any = true;
        }
        Float manaPercent = percent(ability, "manaDamage");
        float mana = manaPercent == null ? 0.0F : manaPercent * Mana.get(player);
        any |= manaPercent != null;
        if (!any) {
            return null;
        }
        float abilityPower = apMin != 0.0F || apMax != 0.0F ? AbilityPowerHelper.getAbilityPower(player) : 0.0F;
        float fixed = flat + attackDamage * adPercent + mana;
        return new Hit(fixed + abilityPower * apMin, fixed + abilityPower * apMax);
    }

    private static List<Extra> extras(Ability ability, Set<String> covered) {
        List<Extra> extras = new ArrayList<>();
        Float distance = covered.contains("distance") ? null : number(ability, "distance");
        if (distance != null) {
            extras.add(new Extra("distance", SpellcraftGui.tr("fmt.blocks", compact(distance)), SpellcraftGui.STAT_PLAIN));
        }
        for (String key : INTERVAL_KEYS) {
            Integer ticks = ticks(ability, key);
            if (ticks != null) {
                extras.add(new Extra("interval", SpellcraftGui.tr("fmt.seconds", SpellcraftGui.seconds(ticks)), SpellcraftGui.STAT_YELLOW));
                break;
            }
        }
        for (String key : COUNT_KEYS) {
            Float count = covered.contains(key) ? null : number(ability, key);
            if (count != null) {
                extras.add(new Extra(key, compact(count), SpellcraftGui.STAT_PLAIN));
            }
        }
        for (String key : AMOUNT_KEYS) {
            Float amount = covered.contains(key) ? null : number(ability, key);
            if (amount != null) {
                extras.add(new Extra(key, compact(amount), SpellcraftGui.STAT_INTENSITY));
            }
        }
        return extras;
    }

    /**
     * The main effect table (redesign/spellcraft/REFINEMENT_PASS_2026-11-10.md sec. 2.3): per spec, the VH labels its
     * buff glyphs reach and the factor each takes, transcribed from the WV buff mixins
     * ({@code mixins/vaulthunters/skills/buffs}, {@code MixinAbstractRampageAbility}, {@code BloodlustSpeed} and the WV
     * Colossus, Sneaky Getaway and Concentrate abilities). The label getters themselves read factor 1 on the client, so
     * the step's factors are applied here.
     */
    private static Map<String, Effects> effects() {
        Map<String, Effects> table = new HashMap<>();
        Main speed = new Main("speed", "buff_speed", Format.PERCENT, Factor.INTENSITY);
        Main rampage = new Main("damage", "damage_bonus", Format.PERCENT, Factor.INTENSITY);
        Main heal = new Main("heal", "heal", Format.NUMBER, Factor.INTENSITY);
        Main hitCost = new Main("additionalManaPerHit", "mana_per_hit", Format.NUMBER, Factor.DRAIN);
        Main boltCost = new Main("additionalManaPerBolt", "mana_per_bolt", Format.NUMBER, Factor.DRAIN);
        Main thorns = new Main("additional_thorns_damage", "thorns", Format.PERCENT, Factor.INTENSITY);
        Main slowness = new Main("slowness", "slowness", Format.INTEGER, Factor.INTENSITY);
        Main reduction = new Main("damageReduction", "damage_reduction", Format.PERCENT, Factor.NONE);
        table.put("Empower_Base", effects(Factor.NONE, Factor.NONE, speed));
        table.put("Empower_Ice_Armor", effects(Factor.NONE, Factor.INTENSITY, new Main("chilled", "chilled", Format.INTEGER, Factor.INTENSITY), hitCost));
        table.put("Empower_Slowness_Aura", effects(Factor.NONE, Factor.NONE, slowness));
        table.put("Rampage_Base", effects(Factor.NONE, Factor.NONE, rampage));
        table.put("Rampage_Bloodlust", effects(Factor.NONE, Factor.UPSIDE, rampage,
                new Main("manaRefund", "mana_refund", Format.NUMBER, Factor.INTENSITY), new Main("speed", "stack_speed", Format.PERCENT, Factor.INTENSITY)));
        table.put("Rampage_Berserker", effects(Factor.NONE, Factor.NONE, rampage, new Main("manaRamp", "mana_ramp", Format.PER_SECOND, Factor.DRAIN)));
        table.put("Rampage_Instinct", effects(Factor.NONE, Factor.NONE, rampage,
                new Main("luckyHitChance", "lucky_hit", Format.PERCENT, Factor.INTENSITY)));
        table.put("Rampage_Leech", effects(Factor.NONE, Factor.NONE, new Main("leech", "leech", Format.PERCENT, Factor.INTENSITY)));
        table.put("Rampage_Chain", effects(Factor.NONE, Factor.NONE, new Main("chains", "chains", Format.INTEGER, Factor.INTENSITY)));
        table.put("Heal_Base", effects(Factor.NONE, Factor.NONE, heal));
        table.put("Heal_Group", effects(Factor.NONE, Factor.NONE, heal));
        table.put("Totem_Base", effects(Factor.NONE, Factor.DURATION, new Main("heal", "heal_per_second", Format.NUMBER, Factor.INTENSITY)));
        table.put("Totem_Mana_Regen", effects(Factor.NONE, Factor.DURATION, new Main("manaRegen", "mana_regen", Format.PERCENT, Factor.INTENSITY)));
        table.put("Totem_Player_Damage", effects(Factor.NONE, Factor.DURATION, new Main("damage", "damage_bonus", Format.PERCENT, Factor.INTENSITY)));
        table.put("Totem_Mob_Damage", effects(Factor.NONE, Factor.DURATION));
        table.put("Mana_Shield_Base", new Effects(List.of(new Main("barrierPoints", "barrierPoints", Format.INTEGER, Factor.INTENSITY)),
                Factor.NONE, Factor.DURATION, true));
        table.put("Mana_Barrier", effects(Factor.NONE, Factor.DURATION));
        table.put("Stonefall_Base", effects(Factor.NONE, Factor.DURATION, reduction, new Main("knockback", "knockback", Format.NUMBER, Factor.INTENSITY)));
        table.put("Stonefall_Snow", effects(Factor.NONE, Factor.DURATION,
                new Main("damageReduction", "damage_reduction", Format.PERCENT, Factor.INTENSITY)));
        table.put("Stonefall_Cold", effects(Factor.NONE, Factor.DURATION, reduction, new Main("freezeDuration", "freeze", Format.TICKS, Factor.INTENSITY)));
        table.put("Taunt_Base", effects(Factor.NONE, Factor.DURATION, new Main("vulnerable", "vulnerable", Format.INTEGER, Factor.INTENSITY)));
        table.put("Taunt_Repel", effects(Factor.NONE, Factor.DURATION, new Main("distance", "distance", Format.BLOCKS, Factor.INTENSITY)));
        table.put("Taunt_Charm", effects(Factor.NONE, Factor.DURATION, new Main("damage", "charm_damage", Format.PERCENT, Factor.INTENSITY)));
        table.put("Taunt_Decoy", effects(Factor.NONE, Factor.DURATION));
        table.put("Battle_Cry_Base", effects(Factor.NONE, Factor.DURATION,
                new Main("attackDamagePerStack", "attack_per_stack", Format.PERCENT, Factor.INTENSITY)));
        table.put("Battle_Cry_Spectral_Strike", effects(Factor.NONE, Factor.DURATION,
                new Main("abilityPowerPerStack", "power_per_stack", Format.PERCENT, Factor.INTENSITY)));
        table.put("Battle_Cry_Lucky_Strike", effects(Factor.NONE, Factor.DURATION,
                new Main("luckyHitPerStack", "lucky_per_stack", Format.PERCENT, Factor.INTENSITY)));
        table.put("Shell_Base", effects(Factor.NONE, Factor.NONE, new Main("stunChance", "stun_chance", Format.PERCENT, Factor.INTENSITY),
                new Main("stunDuration", "stun_duration", Format.TICKS, Factor.INTENSITY), hitCost));
        table.put("Shell_Porcupine", effects(Factor.NONE, Factor.NONE, thorns, hitCost));
        table.put("Shell_Quill", effects(Factor.NONE, Factor.NONE, thorns, hitCost));
        table.put("Smite_Base", effects(Factor.INTENSITY, Factor.NONE, boltCost));
        table.put("Smite_Archon", effects(Factor.INTENSITY, Factor.NONE, boltCost));
        table.put("Smite_Thunderstorm", effects(Factor.INTENSITY, Factor.NONE, boltCost));
        table.put("Smite_Blast_Wave", effects(Factor.INTENSITY, Factor.NONE));
        table.put("Hunter_Base", effects(Factor.NONE, Factor.DURATION));
        table.put("Ghost_Walk_Base", effects(Factor.NONE, Factor.DURATION));
        table.put("Ghost_Walk_Spirit", effects(Factor.NONE, Factor.DURATION));
        table.put("Summon_Eternal_Base", effects(Factor.NONE, Factor.DURATION));
        table.put("Nova_Slow", effects(Factor.NONE, Factor.DURATION, slowness));
        table.put("Colossus_Base", effects(Factor.NONE, Factor.DURATION,
                new Main("additionalResistance", "resistance", Format.PERCENT, Factor.INTENSITY), new Main("size", "size", Format.PERCENT, Factor.INTENSITY)));
        table.put("Sneaky_Getaway", effects(Factor.NONE, Factor.DURATION, speed));
        table.put("Concentrate_Base", effects(Factor.NONE, Factor.DURATION, new Main("baseAmplitude", "amplitude", Format.INTEGER, Factor.INTENSITY)));
        return Map.copyOf(table);
    }

    private static Effects effects(Factor hit, Factor duration, Main... mains) {
        return new Effects(List.of(mains), hit, duration, false);
    }

    /**
     * The step's area factor as VH would apply it: the AoE factor (1 + Area of Effect, after VH's area stat listeners)
     * times the glyph factor, clamped to VH's AoE limit, relative to the same factor without the glyph.
     */
    private static float areaRatio(LocalPlayer player, float factor) {
        if (factor == 1.0F || player.hasEffect(ModEffects.NULLIFY_AREA_OF_EFFECT)) {
            return 1.0F;
        }
        AttributeSnapshot snapshot = AttributeSnapshotHelper.getInstance().getSnapshot(player);
        float multiplier = 1.0F + snapshot.getAttributeValue(ModGearAttributes.AREA_OF_EFFECT, VaultGearAttributeTypeMerger.floatSum());
        multiplier = CommonEvents.PLAYER_STAT.invoke(PlayerStat.AREA_OF_EFFECT, player, multiplier).getValue();
        float limit = 1.0F + AttributeLimitHelper.getAreaOfEffectLimit(player);
        float plain = Mth.clamp(multiplier, 0.0F, limit);
        return plain <= 0.0F ? 0.0F : Mth.clamp(multiplier * factor, 0.0F, limit) / plain;
    }

    @Nullable
    private static String binding(Ability ability, String key) {
        String value;
        try {
            value = AbilityLabelBindingRegistry.getBindingValue(ability, key);
        } catch (RuntimeException e) {
            String id = ability.getClass().getSimpleName() + "." + key;
            if (LOGGED.add(id)) {
                WoldsVaults.LOGGER.error("Spellcraft stats: VH label {} threw while reading; the stat is left out of the tooltip", id, e);
            }
            return null;
        }
        return value == null || NO_BINDING.equals(value) ? null : value.trim();
    }

    @Nullable
    private static Float percent(Ability ability, String key) {
        String value = binding(ability, key);
        if (value == null) {
            return null;
        }
        if (!value.endsWith("%")) {
            unreadable(ability, key, value);
            return null;
        }
        Float parsed = parse(ability, key, value.substring(0, value.length() - 1));
        return parsed == null ? null : parsed / 100.0F;
    }

    @Nullable
    private static Float number(Ability ability, String key) {
        String value = binding(ability, key);
        return value == null ? null : parse(ability, key, value);
    }

    /**
     * A VH time label ("1.5s") in ticks. Only {@code adjustedDuration} also takes a bare number as seconds, as VH's label
     * factory does; any other form (VH binds some "duration" labels to percentages) counts as no duration.
     */
    @Nullable
    private static Integer ticks(Ability ability, String key) {
        String value = binding(ability, key);
        boolean suffixed = value != null && value.endsWith("s");
        if (value == null || (!suffixed && !key.equals("adjustedDuration")) || value.endsWith("%")) {
            return null;
        }
        Float seconds = parse(ability, key, suffixed ? value.substring(0, value.length() - 1) : value);
        return seconds == null ? null : (int) (seconds * 20.0F);
    }

    @Nullable
    private static Float parse(Ability ability, String key, String text) {
        try {
            return Float.parseFloat(text.trim());
        } catch (NumberFormatException e) {
            unreadable(ability, key, text);
            return null;
        }
    }

    private static void unreadable(Ability ability, String key, String value) {
        String id = ability.getClass().getSimpleName() + "." + key;
        if (LOGGED.add(id)) {
            WoldsVaults.LOGGER.error("Spellcraft stats: VH label {} reads '{}', which is not a number; the stat is left out of the tooltip", id, value);
        }
    }

    /** A number in at most four characters plus a suffix: 7.5, 85, 850, 1.2k, 34.6k, 560k, 3.4M, 1.1B, 2T. */
    public static String compact(double value) {
        String[] units = {"", "k", "M", "B", "T"};
        double scaled = value;
        int unit = 0;
        while (unit < units.length - 1 && Math.abs(round(scaled)) >= 1000.0) {
            scaled /= 1000.0;
            unit++;
        }
        return trimmed(round(scaled)) + units[unit];
    }

    private static double round(double value) {
        double magnitude = Math.abs(value);
        return magnitude < 100.0 ? Math.round(value * 10.0) / 10.0 : Math.round(value);
    }

    private static String trimmed(double value) {
        String text = String.format(Locale.ROOT, "%.1f", value);
        return text.endsWith(".0") ? text.substring(0, text.length() - 2) : text;
    }

    /** A hit in compact form, as "min-max" when it is a range. */
    public static String hit(Hit hit) {
        return hit.isRange() ? SpellcraftGui.tr("fmt.range", compact(hit.min()), compact(hit.max())) : compact(hit.max());
    }
}
