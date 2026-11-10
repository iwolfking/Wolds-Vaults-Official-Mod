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
import xyz.iwolfking.woldsvaults.augments.spell.GlyphStacking;
import xyz.iwolfking.woldsvaults.augments.spell.SpellModel;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;
import xyz.iwolfking.woldsvaults.spellcraft.bar.SpellCooldowns;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
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

    /** One more number VH labels for the ability: the label's lang key (under {@code bar.stat.}) and its formatted value. */
    public record Extra(String key, String value) {
    }

    /** An ability's numbers before glyphs; negative radius, velocity or duration means the ability has none. */
    public record Base(@Nullable Hit hit, @Nullable Hit splinter, @Nullable Hit splash, float radius, boolean aoe, float splashRadius,
                       float velocity, int durationTicks, List<Extra> extras) {
    }

    /** A row of the spell's stats: an ability step or a Bundled Spell orb, at its payload depth. */
    public sealed interface Row permits Step, Orb {
        int depth();
    }

    /**
     * One ability step after its glyphs. {@code casts} counts Shotgun copies and the orbs carrying it; {@code total} is
     * one hit from every cast plus the Echo replay, or null when the step casts once with no Echo; the charged values are
     * at full Charge, or null without Charge.
     */
    public record Step(String group, int depth, @Nullable Hit hit, @Nullable Hit charged, @Nullable Hit splinter, @Nullable Hit splash,
                       int casts, float echoPower, @Nullable Hit total, @Nullable Hit chargedTotal, float radius, float splashRadius,
                       float velocity, int durationTicks, List<Extra> extras, @Nullable GlyphStacking.Pool glyphs) implements Row {
        public boolean hasStats() {
            return this.hit != null || this.splinter != null || this.splash != null || this.radius >= 0.0F || this.splashRadius >= 0.0F
                    || this.velocity >= 0.0F || this.durationTicks >= 0 || !this.extras.isEmpty();
        }
    }

    /** A Bundled Spell orb: how many orbs fly, their velocity in blocks per second, and the flight time in seconds. */
    public record Orb(int depth, int copies, float velocity, float flightSeconds, GlyphStacking.Pool glyphs) implements Row {
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
        return step(player, group, base(player, ability), CastModifiers.NONE, 1.0F, 1, false, 0, null);
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
                Base base = ability == null ? null : base(player, ability);
                rows.add(step(player, cast.group(), base, CastModifiers.of(config, cast.glyphs()), damageFactor, multiplicity, root, depth,
                        model.stepPool(cast)));
            } else if (step instanceof SpellModel.Bundle bundle) {
                CastModifiers orb = CastModifiers.of(config, bundle.glyphs());
                int copies = 1 + orb.shotgunCopies();
                rows.add(new Orb(depth, copies * multiplicity, (float) (config.tuning.bundleSpeed * orb.speed() * 20.0), bundle.glyph().potency(),
                        model.stepPool(bundle)));
                walk(player, config, tree, model, bundle.payload(), damageFactor * orb.damage(), multiplicity * copies, false, depth + 1, rows);
            }
        }
    }

    private static Step step(LocalPlayer player, String group, @Nullable Base base, CastModifiers modifiers, float damageFactor, int multiplicity,
                             boolean root, int depth, @Nullable GlyphStacking.Pool glyphs) {
        int casts = (1 + modifiers.shotgunCopies()) * multiplicity;
        float echo = modifiers.echoPower();
        if (base == null) {
            return new Step(group, depth, null, null, null, null, casts, echo, null, null, -1.0F, -1.0F, -1.0F, -1, List.of(), glyphs);
        }
        float factor = modifiers.damage() * damageFactor;
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
        return new Step(group, depth, hit, charged, splinter, splash, casts, echo, total, chargedTotal, radius, base.splashRadius(), velocity,
                base.durationTicks(), base.extras(), glyphs);
    }

    /** VH's numbers for one ability against the player's current stats, before any glyph. */
    public static Base base(LocalPlayer player, Ability ability) {
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
        Integer adjusted = ticks(ability, "adjustedDuration");
        Integer plainDuration = ticks(ability, "duration");
        int duration = adjusted != null ? EffectDurationHelper.adjustEffectDurationFloor(player, adjusted) : plainDuration != null ? plainDuration : -1;
        return new Base(hit, splinter, splash, radius, aoe, splashRadius, velocity, duration, extras(ability));
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

    private static List<Extra> extras(Ability ability) {
        List<Extra> extras = new ArrayList<>();
        Float distance = number(ability, "distance");
        if (distance != null) {
            extras.add(new Extra("distance", SpellcraftGui.tr("fmt.blocks", compact(distance))));
        }
        for (String key : INTERVAL_KEYS) {
            Integer ticks = ticks(ability, key);
            if (ticks != null) {
                extras.add(new Extra("interval", SpellcraftGui.tr("fmt.seconds", SpellcraftGui.seconds(ticks))));
                break;
            }
        }
        for (String key : COUNT_KEYS) {
            Float count = number(ability, key);
            if (count != null) {
                extras.add(new Extra(key, compact(count)));
            }
        }
        for (String key : AMOUNT_KEYS) {
            Float amount = number(ability, key);
            if (amount != null) {
                extras.add(new Extra(key, compact(amount)));
            }
        }
        return extras;
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
