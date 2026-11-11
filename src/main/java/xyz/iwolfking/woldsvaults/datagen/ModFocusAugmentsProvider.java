package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.gear.attribute.ability.special.EntropyPoisonModification;
import iskallia.vault.gear.attribute.ability.special.FrostNovaVulnerabilityModification;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.etching.EtchingGearAttributes;
import iskallia.vault.init.ModGearAttributes;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.woldsvaults.augments.AugmentType;
import xyz.iwolfking.woldsvaults.augments.spell.GlyphStacking;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractWoldsTableProvider;
import xyz.iwolfking.woldsvaults.datagen.lib.AncientGearModifiers;
import xyz.iwolfking.woldsvaults.init.ModEtchingGearAttributes;
import xyz.iwolfking.woldsvaults.modifiers.gear.special.FireballModification;
import xyz.iwolfking.woldsvaults.modifiers.gear.special.GlacialBlastModification;
import xyz.iwolfking.woldsvaults.modifiers.gear.special.ImplodingJavelinModification;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Spellcraft glyphs (redesign/spellcraft/SPEC.md): each glyph's potency tiers, side effects, uses, per-spell cap and
 * arcana cost; the combo surcharges; how focuses and wands roll arcana, glyph lines and depth (glyphs per spell); the
 * spell rules and mechanics tuning; and the ability trait list that decides which glyphs fit which ability. Numbers are
 * the 2026-11-08 prototype with depth +2 and arcana x1.2 over the spec, with the round 4 changes
 * (redesign/spellcraft/BALANCE_PLAN_2026-11-10.md, GLYPH_STACKING_2026-11-10.md): common upsides x1.2 with their
 * downside and cost coefficients divided by 1.2 (written as {@code value / 1.2F}), Discount as mana efficiency, Charge
 * x1.5, Intensify, Extend and Chaining, Bundled Spell once per spell, Mythic focus depth 6 and arcana x1.3.
 */
public class ModFocusAugmentsProvider extends AbstractWoldsTableProvider {
    private static final String UP = "upside";
    private static final String DOWN = "downside";
    private static final String COST = "cost";
    private static final String HIGHER = "higher";
    private static final String LOWER = "lower";
    private static final String REGULAR_UNIQUE_ROW = "OMEGA";
    private static final String ANCIENT_UNIQUE_ROW = "MYTHIC";
    private static final float TICK = 0.05F;
    public static final float RAIL_SIZE_MIN = 1.1F;

    public ModFocusAugmentsProvider(DataGenerator generator) {
        super(generator, "focus_augments");
    }

    @Override
    public void registerConfigs() {
        FocusAugmentsConfig table = new FocusAugmentsConfig();
        table.worksOutsideVaults = true;

        FocusAugmentsConfig.Rules rules = new FocusAugmentsConfig.Rules();
        rules.escalationPerGlyph = 0.12F;
        rules.abilityStepCost = 7;
        rules.chainCooldownShare = 0.25F;
        rules.manaFactorFloor = 0.5F;
        rules.minCooldownSeconds = 0.5F;
        rules.maxGlyphsPerSpell = 12;
        rules.maxSpellsPerItem = 6;
        rules.maxCastsPerTrigger = 16;
        rules.maxBundleNesting = 2;
        rules.maxNameLength = 24;
        rules.defaultPriority = 3;
        rules.priorities = 5;
        rules.uniqueMixCost = 10;
        rules.costNoise = List.of(noise(-1, 25), noise(0, 50), noise(1, 25));
        table.rules = rules;

        FocusAugmentsConfig.Tuning tuning = new FocusAugmentsConfig.Tuning();
        tuning.echoDelaySeconds = 0.5F;
        tuning.shotgunSpreadDegrees = 12.0F;
        tuning.homingRetargetTicks = 5;
        tuning.homingRange = 6.0F;
        tuning.homingAimRange = 48.0F;
        tuning.homingAimConeDegrees = 25.0F;
        tuning.orbitRadius = 2.5F;
        tuning.orbitHeight = 1.2F;
        tuning.orbitVolleyExplosionGapTicks = 10;
        tuning.bundleSpeed = 0.8F;
        tuning.railRange = 128.0F;
        tuning.sizeUpMaxScale = 3.0F;
        tuning.sizeDownMinScale = 0.4F;
        tuning.chargeSeconds = 1.0F;
        tuning.chargeMinShare = 0.25F;
        tuning.chargeMoveFactor = 0.5F;
        table.tuning = tuning;

        table.glyphs = new LinkedHashMap<>();
        glyph(table, AugmentType.AMPLIFY_AREA, "modifier", "common", "power", 0, 10, List.of(List.of("instant", "area")), List.of(), List.of(), List.of(),
                potency("area", "pct", List.of(tier(0, 0.12F, 0.216F), tier(35, 0.168F, 0.288F), tier(70, 0.216F, 0.36F)), List.of()),
                List.of(k("area", UP, 1.0F), k("damage", DOWN, -0.25F / 1.2F)),
                List.of(k("mana", COST, 0.4F / 1.2F)),
                3, List.of(60, 30, 10), 3, 4, 8, false);
        glyph(table, AugmentType.STRENGTHEN, "modifier", "common", "power", 15, 8, List.of(List.of("instant", "damage")), List.of(), List.of(),
                List.of("intensify"),
                potency("damage", "pct", List.of(tier(0, 0.096F, 0.168F), tier(35, 0.132F, 0.228F), tier(70, 0.18F, 0.3F)), List.of()),
                List.of(k("damage", UP, 1.0F), k("area", DOWN, -0.3F / 1.2F)),
                List.of(k("mana", COST, 0.6F / 1.2F)),
                3, List.of(60, 30, 10), 3, 5, 11, false);
        glyph(table, AugmentType.QUICKEN, "modifier", "common", "flow", 0, 10, List.of(List.of("instant"), List.of("toggle")), List.of(), List.of(), List.of(),
                potency("haste", "pct", List.of(tier(0, 0.072F, 0.12F), tier(35, 0.096F, 0.168F), tier(70, 0.12F, 0.216F)), List.of()),
                List.of(k("damage", DOWN, -0.2F / 1.2F)),
                List.of(k("haste", UP, 1.0F), k("mana", COST, 0.8F / 1.2F)),
                3, List.of(60, 30, 10), 3, 5, 11, false);
        glyph(table, AugmentType.DISCOUNT, "modifier", "common", "flow", 0, 10, List.of(List.of("mana")), List.of(), List.of(), List.of(),
                potency("mana efficiency", "pct", List.of(tier(0, 0.077F, 0.133F), tier(35, 0.104F, 0.195F), tier(70, 0.133F, 0.263F)), List.of()),
                List.of(),
                List.of(k("efficiency", UP, 1.0F), k("haste", DOWN, -0.2F)),
                3, List.of(60, 30, 10), 3, 4, 8, false);
        glyph(table, AugmentType.DELAY, "delay", "utility", "flow", 0, 6, List.of(), List.of(), List.of(), List.of(),
                potency("pause", "seconds", List.of(tier(0, 0.25F, 1.0F)), List.of()),
                List.of(),
                List.of(fixed("mana", COST, 1.02F)),
                4, List.of(40, 30, 20, 10), 4, 1, 1, true);
        glyph(table, AugmentType.ACCELERATE, "modifier", "common", "path", 0, 10,
                List.of(List.of("instant", "path"), List.of("instant", "projectile"), List.of("bundle")), List.of(), List.of(), List.of(),
                potency("speed", "pct", List.of(tier(0, 0.18F, 0.3F), tier(35, 0.24F, 0.384F), tier(70, 0.3F, 0.48F)), List.of()),
                List.of(k("speed", UP, 1.0F), k("damage", DOWN, -0.08F / 1.2F)),
                List.of(k("mana", COST, 0.2F / 1.2F)),
                3, List.of(60, 30, 10), 3, 2, 6, false);
        glyph(table, AugmentType.HOMING, "modifier", "utility", "path", 20, 6,
                List.of(List.of("instant", "path"), List.of("instant", "projectile"), List.of("bundle")), List.of(), List.of(), List.of("orbit"),
                potency("turn rate", "deg_s", List.of(tier(20, 60.0F, 120.0F), tier(50, 80.0F, 150.0F), tier(80, 100.0F, 180.0F)), List.of()),
                List.of(fixed("damage", DOWN, 0.9F), fixed("speed", DOWN, 0.9F)),
                List.of(fixed("mana", COST, 1.1F)),
                1, List.of(100), 1, 7, 12, false);
        glyph(table, AugmentType.ORBIT, "modifier", "utility", "path", 20, 4, List.of(List.of("instant", "projectile"), List.of("bundle")), List.of(),
                List.of(), List.of("homing"),
                potency("duration", "seconds", List.of(tier(20, 6.0F, 12.0F)), List.of()),
                List.of(fixed("damage", DOWN, 0.85F)),
                List.of(fixed("mana", COST, 1.15F), fixed("cooldown", COST, 1.1F)),
                1, List.of(100), 1, 7, 12, false);
        glyph(table, AugmentType.LOW_GRAVITY, "modifier", "utility", "path", 0, 8, List.of(List.of("instant", "gravity"), List.of("bundle")), List.of(),
                List.of(), List.of(),
                potency("gravity", "neg_pct", List.of(tier(0, 0.4F, 0.7F)), List.of()),
                List.of(fixed("speed", DOWN, 0.95F)),
                List.of(fixed("mana", COST, 1.04F)),
                1, List.of(100), 1, 1, 4, false);
        glyph(table, AugmentType.ETHEREAL, "modifier", "utility", "path", 60, 2,
                List.of(List.of("instant", "path"), List.of("instant", "projectile"), List.of("bundle")), List.of(), List.of(), List.of(),
                potency("lifetime", "seconds", List.of(tier(60, 4.0F, 8.0F)), List.of()),
                List.of(fixed("damage", DOWN, 0.85F)),
                List.of(fixed("mana", COST, 1.2F)),
                1, List.of(100), 1, 11, 16, false);
        glyph(table, AugmentType.SIZE_UP, "modifier", "common", "form", 0, 8,
                List.of(List.of("instant", "path"), List.of("instant", "projectile"), List.of("instant", "bolt"), List.of("bundle")), List.of(), List.of(),
                List.of(),
                potency("size", "pct", List.of(tier(0, 0.24F, 0.36F), tier(35, 0.3F, 0.48F), tier(70, 0.36F, 0.6F)), List.of()),
                List.of(k("scale", UP, 1.0F), k("speed", DOWN, -0.3F / 1.2F)),
                List.of(k("mana", COST, 0.3F / 1.2F)),
                2, List.of(70, 30), 2, 4, 8, false);
        glyph(table, AugmentType.SIZE_DOWN, "modifier", "common", "form", 0, 6,
                List.of(List.of("instant", "path"), List.of("instant", "projectile"), List.of("bundle")), List.of(), List.of(), List.of(),
                potency("size", "neg_pct", List.of(tier(0, 0.15F, 0.22F), tier(35, 0.18F, 0.26F), tier(70, 0.2F, 0.3F)), List.of()),
                List.of(k("scale", DOWN, -1.0F), k("damage", UP, 0.72F), k("speed", UP, 0.6F)),
                List.of(fixed("mana", COST, 1.05F)),
                2, List.of(70, 30), 2, 4, 8, false);
        glyph(table, AugmentType.ECHO, "modifier", "special", "chaos", 60, 2, List.of(List.of("instant", "damage")), List.of(), List.of(), List.of(),
                potency("echo power", "pct_plain", List.of(tier(60, 0.45F, 0.65F)), List.of()),
                List.of(),
                List.of(fixed("mana", COST, 1.5F), fixed("cooldown", COST, 1.3F)),
                1, List.of(100), 1, 14, 22, false);
        glyph(table, AugmentType.SHOTGUN, "modifier", "special", "chaos", 50, 3,
                List.of(List.of("instant", "path"), List.of("instant", "projectile"), List.of("instant", "bolt"), List.of("bundle")), List.of(), List.of(),
                List.of(),
                potency("copy damage", "pct_plain", List.of(tier(50, 0.7F, 0.8F), tier(70, 0.75F, 0.85F)), List.of()),
                List.of(),
                List.of(perUse("mana", COST, 0.3F), perUse("cooldown", COST, 0.1F)),
                3, List.of(50, 35, 15), 3, 12, 19, false);
        glyph(table, AugmentType.CHARGE, "modifier", "special", "chaos", 60, 2,
                List.of(List.of("instant", "damage", "path"), List.of("instant", "damage", "projectile"), List.of("instant", "damage", "area"),
                        List.of("instant", "damage", "bolt")), List.of("linger"), List.of(), List.of(),
                potency("full charge", "pct", List.of(tier(60, 0.9F, 1.5F)), List.of()),
                List.of(),
                List.of(fixed("mana", COST, 1.35F), fixed("cooldown", COST, 1.25F)),
                1, List.of(100), 1, 12, 18, false);
        glyph(table, AugmentType.BUNDLED_SPELL, "bundle", "special", "chaos", 40, 3, List.of(), List.of(), List.of(), List.of(),
                potency("flight", "seconds", List.of(tier(40, 3.0F, 6.0F)), List.of()),
                List.of(),
                List.of(fixed("mana", COST, 1.25F), fixed("cooldown", COST, 1.15F)),
                1, List.of(100), 1, 12, 19, false);
        glyph(table, AugmentType.INTENSIFY, "modifier", "common", "power", 20, 8, List.of(List.of("instant", "intensify"), List.of("toggle", "intensify")),
                List.of(), List.of(), List.of("strengthen"),
                potency("intensity", "pct", List.of(tier(0, 0.1F, 0.14F), tier(35, 0.12F, 0.16F), tier(70, 0.14F, 0.18F)), List.of()),
                List.of(k("intensity", UP, 1.0F), fixed("duration", DOWN, 0.85F), k("drain", DOWN, 1.5F), k("heal_mana", DOWN, 1.5F)),
                List.of(fixed("mana", COST, 1.15F)),
                2, List.of(70, 30), 2, 6, 10, false);
        glyph(table, AugmentType.EXTEND, "modifier", "common", "flow", 20, 8, List.of(List.of("instant", "extend"), List.of("toggle", "extend")),
                List.of(), List.of(), List.of(),
                potency("duration", "pct", List.of(tier(0, 0.2F, 0.275F), tier(35, 0.25F, 0.325F), tier(70, 0.3F, 0.4F)), List.of()),
                List.of(k("duration", UP, 1.0F), kBase("intensity", DOWN, 0.98F, -0.2F), k("drain", UP, -0.5F)),
                List.of(fixed("mana", COST, 1.1F)),
                2, List.of(70, 30), 2, 5, 9, false);
        glyph(table, AugmentType.CHAINING, "modifier", "unique", "unique", 0, 0, List.of(List.of("instant", "damage", "direct")), List.of("linger"),
                List.of(), List.of(),
                potency("procs chaining", "none", List.of(tier(0, 0.0F, 0.0F)), List.of()),
                List.of(),
                List.of(),
                1, List.of(100), 1, 10, 10, true);
        gearGlyph(table, AugmentType.RIME, "unique", List.of("Nova_Slow"), potency("vulnerability level", "level", List.of(tier(0, 1.0F, 6.0F)), List.of()),
                List.of(), List.of());
        gearGlyph(table, AugmentType.WARP_HASTE, "unique", List.of("Dash_Warp"), potency("warp cooldown", "neg_pct", List.of(tier(0, 0.1F, 0.5F)), List.of()),
                List.of(), List.of(kBase("cooldown", UP, 1.0F, -1.0F)));
        gearGlyph(table, AugmentType.WARP_VELOCITY, "unique", List.of("Dash_Warp"), potency("warp arrow speed", "pct", List.of(tier(0, 0.1F, 0.5F)), List.of()),
                List.of(), List.of());
        gearGlyph(table, AugmentType.STEEL_CORE, "unique", List.of("Fireball_Base", "Fireball_Volley"),
                potency("centre bonus", "pct", List.of(tier(0, ModVaultGearTiersProvider.STEEL_INFERNO_CENTRE_MIN, (float) AncientGearModifiers.STEEL_INFERNO_CENTRE_MAX)),
                        List.of(linked("radius", ModVaultGearTiersProvider.STEEL_INFERNO_RADIUS_MIN, (float) AncientGearModifiers.STEEL_INFERNO_RADIUS_MAX))),
                List.of(), List.of());
        gearGlyph(table, AugmentType.TWINFIRE, "unique", List.of("Fireball_Base"), potency("second fireball chance", "pct_plain", List.of(tier(0, 0.65F, 0.86F)),
                List.of()), List.of(), List.of());
        gearGlyph(table, AugmentType.DEEP_CHILL, "unique", List.of("Ice_Bolt_Blast"), potency("hypothermia", "level", List.of(tier(0, 2.0F, 8.0F)), List.of()),
                List.of(), List.of());
        gearGlyph(table, AugmentType.IMPLOSION, "unique", List.of("Javelin_Base", "Javelin_Piercing", "Javelin_Scatter", "Javelin_Sight"),
                potency("implode damage", "pct", List.of(tier(0, 0.5F, 0.9F)), List.of()), List.of(), List.of());
        gearGlyph(table, AugmentType.ENTROPIC_BIND, "unique", List.of("Empower_Slowness_Aura"), potency("poison level", "level", List.of(tier(0, 1.0F, 16.0F)),
                List.of()), List.of(), List.of());
        gearGlyph(table, AugmentType.WIDE_BLAST, "unique", List.of("Fireball_Base"), potency("fireball area", "pct", List.of(tier(0, 0.1F, 0.24F)), List.of()),
                List.of(), List.of());
        gearGlyph(table, AugmentType.NARROW_STORM, "unique", List.of("Storm_Arrow_Base"), potency("storm area", "neg_pct", List.of(tier(0, 0.6F, 0.8F)), List.of()),
                List.of(), List.of());
        gearGlyph(table, AugmentType.SWIFT_STORM, "unique", List.of("Storm_Arrow_Base"), potency("storm cooldown", "neg_pct", List.of(tier(0, 0.5F, 0.75F)),
                List.of()), List.of(), List.of(kBase("cooldown", UP, 1.0F, -1.0F)));
        gearGlyph(table, AugmentType.DREAD, "unique", List.of("Taunt_Repel"), potency("fear area", "pct", List.of(tier(0, 0.2F, 1.33F)), List.of()),
                List.of(), List.of());
        gearGlyph(table, AugmentType.QUICK_MEND, "unique", List.of("Heal_Group"), potency("group heal cooldown", "neg_pct", List.of(tier(0, 0.25F, 0.75F)),
                List.of()), List.of(), List.of(kBase("cooldown", UP, 1.0F, -1.0F)));
        etchingGlyphs(table);
        describe(table, AugmentType.AMPLIFY_AREA, "<gray>Increases area by <#5FD068>%.0fpvalue%");
        describe(table, AugmentType.STRENGTHEN, "<gray>Increases damage by <#E0564A>%.0fpvalue%");
        describe(table, AugmentType.QUICKEN, "<gray>Increases spell haste by <#4AC6E0>%.0fpvalue%");
        describe(table, AugmentType.DISCOUNT, "<gray>Increases mana efficiency by <#6FA8FF>%.0fpvalue%");
        describe(table, AugmentType.DELAY, "<gray>Pauses the spell for <yellow>%.2fvalue <gray>seconds");
        describe(table, AugmentType.ACCELERATE, "<gray>Increases projectile speed by <yellow>%.0fpvalue%");
        describe(table, AugmentType.HOMING, "<gray>Projectiles home onto targets, turning <yellow>%.0fvalue<gray> degrees per second");
        describe(table, AugmentType.ORBIT, "<gray>Projectiles orbit you for <yellow>%.1fvalue <gray>seconds");
        describe(table, AugmentType.LOW_GRAVITY, "<gray>Projectiles fall <yellow>%.0fpvalue% <gray>slower");
        describe(table, AugmentType.ETHEREAL, "<gray>Projectiles pass through walls for <yellow>%.1fvalue <gray>seconds");
        describe(table, AugmentType.SIZE_UP, "<gray>Increases projectile size by <yellow>%.0fpvalue%");
        describe(table, AugmentType.SIZE_DOWN, "<gray>Reduces projectile size by <yellow>%.0fpvalue%<gray>, hitting harder and flying faster");
        describe(table, AugmentType.ECHO, "<gray>Recasts the ability after a short delay at <#E0564A>%.0fpvalue% <gray>power");
        describe(table, AugmentType.SHOTGUN, "<gray>Fires an extra copy dealing <#E0564A>%.0fpvalue% <gray>damage");
        describe(table, AugmentType.CHARGE, "<gray>Hold the key to charge; a full charge deals <#E0564A>%.0fpvalue% <gray>more damage");
        describe(table, AugmentType.BUNDLED_SPELL, "<gray>Throws the rest of the spell as an orb that flies for <yellow>%.1fvalue <gray>seconds");
        describe(table, AugmentType.INTENSIFY, "<gray>Increases the buff's strength by <gold>%.0fpvalue%");
        describe(table, AugmentType.EXTEND, "<gray>Increases the buff's duration by <yellow>%.0fpvalue%");
        describe(table, AugmentType.CHAINING, "<gray>Direct hits now proc <yellow>Chaining <gray>to nearby mobs");
        describe(table, AugmentType.RIME, "<gray>Frost Nova applies level <yellow>%.0fvalue <gray>Vulnerability");
        describe(table, AugmentType.WARP_HASTE, "<gray>Reduces Warp's cooldown by <#4AC6E0>%.0fpvalue%");
        describe(table, AugmentType.WARP_VELOCITY, "<gray>Increases Warp's arrow speed by <yellow>%.0fpvalue%");
        describe(table, AugmentType.STEEL_CORE, "<gray>Fireball's explosion is tighter, dealing up to <#E0564A>%.2fvalue<gray>x damage at its centre");
        describe(table, AugmentType.TWINFIRE, "<gray>Fireball has a <yellow>%.0fpvalue% <gray>chance to fire a second fireball");
        describe(table, AugmentType.DEEP_CHILL, "<gray>Glacial Blast applies level <yellow>%.0fvalue <#2FE1FA>Hypothermia");
        describe(table, AugmentType.IMPLOSION, "<gray>Javelins implode on impact, dealing <#E0564A>%.0fpvalue% <gray>of their damage");
        describe(table, AugmentType.ENTROPIC_BIND, "<gray>Slowness Aura poisons mobs at level <yellow>%.0fvalue");
        describe(table, AugmentType.WIDE_BLAST, "<gray>Increases Fireball's explosion area by <#5FD068>%.0fpvalue%");
        describe(table, AugmentType.NARROW_STORM, "<gray>Reduces Storm Arrow's area by <#5FD068>%.0fpvalue%");
        describe(table, AugmentType.SWIFT_STORM, "<gray>Reduces Storm Arrow's cooldown by <#4AC6E0>%.0fpvalue%");
        describe(table, AugmentType.DREAD, "<gray>Increases Repel's fear area by <#5FD068>%.0fpvalue%");
        describe(table, AugmentType.QUICK_MEND, "<gray>Reduces Group Heal's cooldown by <#4AC6E0>%.0fpvalue%");
        checkDescriptions(table);
        checkRoles(table);

        String special = ModGearAttributes.ABILITY_SPECIAL_MODIFICATION.getRegistryName().toString();
        String area = ModGearAttributes.ABILITY_AREA_OF_EFFECT_PERCENT.getRegistryName().toString();
        String cooldown = ModGearAttributes.ABILITY_COOLDOWN_PERCENT.getRegistryName().toString();
        table.gearGlyphs = new ArrayList<>();
        gearAffix(table, special, FrostNovaVulnerabilityModification.ID.toString(), AugmentType.RIME, 1.0F, HIGHER);
        gearAffix(table, cooldown, "Dash_Warp", AugmentType.WARP_HASTE, 1.0F, LOWER);
        gearAffix(table, ModGearAttributes.WARP_PROJECTILE_SPEED.getRegistryName().toString(), null, AugmentType.WARP_VELOCITY, 1.0F, HIGHER);
        gearAffix(table, xyz.iwolfking.woldsvaults.init.ModGearAttributes.STEEL_INFERNO.getRegistryName().toString(), null, AugmentType.STEEL_CORE,
                1.0F, HIGHER);
        gearAffix(table, special, FireballModification.ID.toString(), AugmentType.TWINFIRE, 1.0F, HIGHER);
        gearAffix(table, special, GlacialBlastModification.ID.toString(), AugmentType.DEEP_CHILL, 1.0F, HIGHER);
        gearAffix(table, special, ImplodingJavelinModification.ID.toString(), AugmentType.IMPLOSION, 1.0F, HIGHER);
        gearAffix(table, xyz.iwolfking.woldsvaults.init.ModGearAttributes.IMPLODING_JAVELIN.getRegistryName().toString(), null, AugmentType.IMPLOSION,
                0.0F, HIGHER);
        gearAffix(table, special, EntropyPoisonModification.ID.toString(), AugmentType.ENTROPIC_BIND, 1.0F, HIGHER);
        gearAffix(table, area, "Fireball_Base", AugmentType.WIDE_BLAST, 1.0F, HIGHER);
        gearAffix(table, area, "Storm_Arrow_Base", AugmentType.NARROW_STORM, 1.0F, LOWER);
        gearAffix(table, cooldown, "Storm_Arrow_Base", AugmentType.SWIFT_STORM, 1.0F, LOWER);
        gearAffix(table, area, "Taunt_Repel", AugmentType.DREAD, 1.0F, HIGHER);
        gearAffix(table, cooldown, "Heal_Group", AugmentType.QUICK_MEND, 1.0F, LOWER);

        table.uniqueSets = new LinkedHashMap<>();
        uniqueSet(table, "the_vault:inferno_reach", List.of(always(AugmentType.STEEL_CORE)),
                List.of(AugmentType.STRENGTHEN, AugmentType.SHOTGUN, AugmentType.INTENSIFY, AugmentType.SIZE_DOWN, AugmentType.ACCELERATE), 3, 5);
        uniqueSet(table, "the_vault:frozen_orb", List.of(always(AugmentType.RIME)),
                List.of(AugmentType.DELAY, AugmentType.ECHO, AugmentType.BUNDLED_SPELL, AugmentType.SIZE_UP, AugmentType.AMPLIFY_AREA), 3, 5);
        uniqueSet(table, "the_vault:ender_rings", List.of(always(AugmentType.WARP_HASTE), always(AugmentType.WARP_VELOCITY)),
                List.of(AugmentType.ECHO, AugmentType.QUICKEN, AugmentType.INTENSIFY, AugmentType.EXTEND), 2, 4);
        uniqueSet(table, "the_vault:echoflare", List.of(),
                List.of(AugmentType.SHOTGUN, AugmentType.BUNDLED_SPELL, AugmentType.ECHO, AugmentType.HOMING, AugmentType.ORBIT, AugmentType.ACCELERATE,
                        AugmentType.LOW_GRAVITY), 5, 7);
        uniqueSet(table, "woldsvaults:chroma_brew", List.of(),
                List.of(AugmentType.SIZE_UP, AugmentType.AMPLIFY_AREA, AugmentType.SIZE_DOWN, AugmentType.INTENSIFY, AugmentType.CHARGE,
                        AugmentType.BUNDLED_SPELL), 4, 6);
        uniqueSet(table, "the_vault:chainlash", List.of(always(AugmentType.CHAINING)),
                List.of(AugmentType.STRENGTHEN, AugmentType.ECHO, AugmentType.HOMING, AugmentType.ACCELERATE, AugmentType.BUNDLED_SPELL), 3, 5);
        uniqueSet(table, "the_vault:baguette", List.of(always(AugmentType.INTENSIFY, 4, 4)),
                List.of(AugmentType.EXTEND, AugmentType.ORBIT, AugmentType.CHARGE, AugmentType.BUNDLED_SPELL), 2, 4);

        table.combos = new ArrayList<>();
        combo(table, "shotgun", "echo", 10, "every copy echoes");
        combo(table, "shotgun", "homing", 5, "every copy finds a target");
        combo(table, "shotgun", "orbit", 5, "a ring of orbiting projectiles");
        combo(table, "shotgun", "bundled_spell", 10, "a Shotgun on the bundle multiplies every payload");
        combo(table, "ethereal", "homing", 5, "seeks through walls");
        combo(table, "ethereal", "bundled_spell", 7, "payloads land behind walls");
        combo(table, "echo", "bundled_spell", 7, "an Echo inside a payload repeats at every landing");

        FocusAugmentsConfig.FocusRolls focus = new FocusAugmentsConfig.FocusRolls();
        focus.budgetBase = 14.4F;
        focus.budgetPerLevel = 0.72F;
        focus.budgetRollMin = 0.9F;
        focus.budgetRollMax = 1.1F;
        focus.rarities = new LinkedHashMap<>();
        rarity(focus, "SCRAPPY", 0.6F, null, 2, 3, null, null);
        rarity(focus, "COMMON", 0.8F, null, 3, 4, null, null);
        rarity(focus, "RARE", 1.0F, null, 4, 5, null, null);
        rarity(focus, "EPIC", 1.15F, null, 5, 6, null, null);
        rarity(focus, "UNIQUE", 1.15F, null, 5, 6, null, null);
        rarity(focus, "SPECIAL", 1.15F, null, 5, 6, null, null);
        rarity(focus, "OMEGA", 1.3F, null, 6, 7, null, null);
        rarity(focus, "MYTHIC", 1.5F, 1.3F, 8, 9, null, null);
        rarity(focus, "CHAOTIC", 1.3F, null, 4, 8, 0.75F, 1.25F);
        focus.depth = List.of(depth(0, 3, 3), depth(40, 3, 4), depth(70, 4, 4));
        focus.depthBonus = new LinkedHashMap<>();
        focus.depthBonus.put("OMEGA", depth(70, 4, 5));
        focus.depthBonus.put("MYTHIC", depth(70, 6, 6));
        table.focus = focus;

        FocusAugmentsConfig.WandRolls wand = new FocusAugmentsConfig.WandRolls();
        wand.budgetFactor = 0.6F;
        wand.linesDelta = -2;
        wand.minLines = 1;
        wand.maxDepth = 4;
        table.wand = wand;

        Map<String, List<String>> traits = new LinkedHashMap<>();
        traits.put("UltimateShield_Base", List.of("TOGGLE", "INTENSIFY", "EXTEND"));
        traits.put("Fangs_Base", List.of("INSTANT", "AREA", "MANA", "DAMAGE"));
        traits.put("Fangs_Maw", List.of("INSTANT", "AREA", "MANA", "DAMAGE"));
        traits.put("Expunge_Base", List.of("INSTANT", "AREA", "MANA", "DAMAGE", "LINGER"));
        traits.put("Concentrate_Base", List.of("INSTANT", "AREA", "MANA", "DAMAGE", "LINGER", "INTENSIFY", "EXTEND"));
        traits.put("Colossus_Base", List.of("INSTANT", "MANA", "INTENSIFY", "EXTEND"));
        traits.put("Sneaky_Getaway", List.of("INSTANT", "MANA", "INTENSIFY", "EXTEND"));
        traits.put("Heal_Base", List.of("INSTANT", "MANA", "INTENSIFY"));
        traits.put("Heal_Group", List.of("INSTANT", "AREA", "MANA", "INTENSIFY"));
        traits.put("Heal_Cleanse", List.of("INSTANT", "MANA"));
        traits.put("Dash_Base", List.of("INSTANT", "MANA"));
        traits.put("Dash_Damage", List.of("INSTANT", "MANA", "DAMAGE"));
        traits.put("Dash_Warp", List.of("INSTANT", "MANA"));
        traits.put("Nova_Base", List.of("INSTANT", "AREA", "MANA", "DAMAGE", "PAYLOAD"));
        traits.put("Nova_Slow", List.of("INSTANT", "AREA", "MANA", "DAMAGE", "PAYLOAD", "INTENSIFY", "EXTEND"));
        traits.put("Nova_Dot", List.of("INSTANT", "AREA", "MANA", "DAMAGE", "LINGER", "PAYLOAD"));
        traits.put("Ghost_Walk_Base", List.of("INSTANT", "MANA", "EXTEND"));
        traits.put("Ghost_Walk_Spirit", List.of("INSTANT", "MANA", "EXTEND"));
        traits.put("Mega_Jump_Base", List.of("INSTANT", "AREA", "MANA"));
        traits.put("Mega_Jump_Break_Up", List.of("INSTANT", "AREA", "MANA"));
        traits.put("Mega_Jump_Break_Down", List.of("INSTANT", "AREA", "MANA"));
        traits.put("Levitate", List.of("HOLD", "MANA"));
        traits.put("Rampage_Base", List.of("TOGGLE", "MANA", "INTENSIFY", "EXTEND"));
        traits.put("Rampage_Bloodlust", List.of("TOGGLE", "MANA", "INTENSIFY", "EXTEND"));
        traits.put("Rampage_Berserker", List.of("TOGGLE", "MANA", "INTENSIFY", "EXTEND"));
        traits.put("Rampage_Instinct", List.of("TOGGLE", "MANA", "INTENSIFY", "EXTEND"));
        traits.put("Rampage_Leech", List.of("TOGGLE", "MANA", "INTENSIFY", "EXTEND"));
        traits.put("Rampage_Chain", List.of("TOGGLE", "MANA", "INTENSIFY", "EXTEND"));
        traits.put("Smite_Base", List.of("TOGGLE", "MANA", "INTENSIFY", "EXTEND"));
        traits.put("Smite_Archon", List.of("TOGGLE", "MANA", "INTENSIFY", "EXTEND"));
        traits.put("Smite_Blast_Wave", List.of("TOGGLE", "MANA", "INTENSIFY", "EXTEND"));
        traits.put("Smite_Thunderstorm", List.of("TOGGLE"));
        traits.put("Summon_Eternal_Base", List.of("INSTANT", "MANA", "EXTEND"));
        traits.put("Empower_Base", List.of("TOGGLE", "MANA", "INTENSIFY", "EXTEND"));
        traits.put("Empower_Ice_Armor", List.of("TOGGLE", "MANA", "INTENSIFY", "EXTEND"));
        traits.put("Empower_Slowness_Aura", List.of("TOGGLE", "MANA", "INTENSIFY", "EXTEND"));
        traits.put("Vein_Miner_Base", List.of("HOLD"));
        traits.put("Vein_Miner_Fortune", List.of("HOLD"));
        traits.put("Vein_Miner_Durability", List.of("HOLD"));
        traits.put("Vein_Miner_Void", List.of("HOLD"));
        traits.put("Vein_Miner_Chain", List.of("HOLD"));
        traits.put("Hunter_Base", List.of("INSTANT", "MANA", "EXTEND"));
        traits.put("Javelin_Sight", List.of("INSTANT", "AREA", "PROJECTILE", "MANA", "DAMAGE", "DIRECT", "GRAVITY", "PAYLOAD"));
        traits.put("Farmer_Base", List.of("TOGGLE", "AREA", "MANA"));
        traits.put("Farmer_Melon", List.of("TOGGLE", "AREA", "MANA"));
        traits.put("Farmer_Cactus", List.of("TOGGLE", "AREA", "MANA"));
        traits.put("Farmer_Animal", List.of("TOGGLE", "AREA", "MANA"));
        traits.put("Mana_Shield_Base", List.of("INSTANT", "MANA", "INTENSIFY", "EXTEND"));
        traits.put("Mana_Barrier", List.of("INSTANT", "AREA", "MANA", "EXTEND"));
        traits.put("Shield_Bash", List.of("INSTANT", "AREA", "MANA", "DAMAGE"));
        traits.put("Shield_Bash_Earthshatter", List.of("INSTANT", "AREA", "MANA", "DAMAGE"));
        traits.put("Shield_Bash_Battering_Ram", List.of("INSTANT", "AREA", "MANA", "DAMAGE"));
        traits.put("Mana_Shield_Retribution", List.of("INSTANT", "AREA", "MANA", "DAMAGE", "LINGER"));
        traits.put("Mana_Shield_Implode", List.of("INSTANT", "AREA", "DAMAGE", "PAYLOAD"));
        traits.put("Implode_Life_Tap", List.of("INSTANT", "AREA", "DAMAGE", "PAYLOAD"));
        traits.put("Taunt_Base", List.of("INSTANT", "AREA", "MANA", "INTENSIFY", "EXTEND"));
        traits.put("Taunt_Repel", List.of("INSTANT", "AREA", "MANA", "INTENSIFY", "EXTEND"));
        traits.put("Taunt_Decoy", List.of("INSTANT", "AREA", "PROJECTILE", "MANA", "GRAVITY", "PAYLOAD", "EXTEND"));
        traits.put("Taunt_Charm", List.of("INSTANT", "AREA", "MANA", "INTENSIFY", "EXTEND"));
        traits.put("Stonefall_Base", List.of("INSTANT", "MANA", "INTENSIFY", "EXTEND"));
        traits.put("Stonefall_Snow", List.of("INSTANT", "MANA", "DAMAGE", "INTENSIFY", "EXTEND"));
        traits.put("Stonefall_Cold", List.of("INSTANT", "MANA", "INTENSIFY", "EXTEND"));
        traits.put("Totem_Base", List.of("INSTANT", "AREA", "MANA", "INTENSIFY", "EXTEND"));
        traits.put("Totem_Player_Damage", List.of("INSTANT", "AREA", "MANA", "INTENSIFY", "EXTEND"));
        traits.put("Totem_Mana_Regen", List.of("INSTANT", "AREA", "MANA", "INTENSIFY", "EXTEND"));
        traits.put("Totem_Mob_Damage", List.of("INSTANT", "AREA", "MANA", "DAMAGE", "LINGER", "EXTEND"));
        traits.put("Javelin_Base", List.of("INSTANT", "PROJECTILE", "MANA", "DAMAGE", "DIRECT", "GRAVITY", "PAYLOAD"));
        traits.put("Javelin_Piercing", List.of("INSTANT", "PROJECTILE", "MANA", "DAMAGE", "DIRECT", "GRAVITY", "PAYLOAD"));
        traits.put("Javelin_Scatter", List.of("INSTANT", "PROJECTILE", "MANA", "DAMAGE", "DIRECT", "GRAVITY", "PAYLOAD"));
        traits.put("Shell_Base", List.of("TOGGLE", "MANA", "INTENSIFY", "EXTEND"));
        traits.put("Shell_Porcupine", List.of("TOGGLE", "MANA", "INTENSIFY", "EXTEND"));
        traits.put("Shell_Quill", List.of("TOGGLE", "MANA", "INTENSIFY", "EXTEND"));
        traits.put("Fireball_Base", List.of("INSTANT", "AREA", "PROJECTILE", "MANA", "DAMAGE", "GRAVITY", "PAYLOAD"));
        traits.put("Fireball_Volley", List.of("INSTANT", "AREA", "PROJECTILE", "MANA", "DAMAGE", "GRAVITY", "PAYLOAD"));
        traits.put("Fireball_Fireshot", List.of("INSTANT", "PROJECTILE", "MANA", "DAMAGE", "DIRECT", "GRAVITY", "PAYLOAD"));
        traits.put("Storm_Arrow_Base", List.of("INSTANT", "AREA", "PROJECTILE", "MANA", "DAMAGE", "GRAVITY", "PAYLOAD"));
        traits.put("Storm_Arrow_Blizzard", List.of("INSTANT", "AREA", "PROJECTILE", "MANA", "DAMAGE", "GRAVITY", "PAYLOAD"));
        traits.put("Battle_Cry_Base", List.of("INSTANT", "AREA", "MANA", "PAYLOAD", "INTENSIFY", "EXTEND"));
        traits.put("Battle_Cry_Spectral_Strike", List.of("INSTANT", "AREA", "MANA", "PAYLOAD", "INTENSIFY", "EXTEND"));
        traits.put("Battle_Cry_Lucky_Strike", List.of("INSTANT", "AREA", "MANA", "PAYLOAD", "INTENSIFY", "EXTEND"));
        traits.put("Ice_Bolt_Base", List.of("INSTANT", "PROJECTILE", "MANA", "DAMAGE", "DIRECT", "PAYLOAD"));
        traits.put("Ice_Bolt_Blast", List.of("INSTANT", "AREA", "PROJECTILE", "MANA", "DAMAGE", "DIRECT", "PAYLOAD"));
        traits.put("Shard_Blizzard", List.of("INSTANT", "AREA", "PROJECTILE", "MANA", "DAMAGE", "GRAVITY", "PAYLOAD"));
        traits.put("Arcane_Base", List.of("HOLD", "BEAM", "MANA", "DAMAGE"));
        traits.put("Arcane_Rail", List.of("INSTANT", "BOLT", "MANA", "DAMAGE"));
        traits.put("Arcane_Prism", List.of("INSTANT", "MANA", "DAMAGE"));
        traits.put("Earthquake_Base", List.of("INSTANT", "AREA", "MANA", "DAMAGE", "PAYLOAD"));
        traits.put("Earthquake_Singularity", List.of("INSTANT", "AREA", "MANA", "DAMAGE", "PAYLOAD"));
        traits.put("Earthquake_Tremor", List.of("INSTANT", "AREA", "MANA", "DAMAGE", "PAYLOAD"));
        traits.put("Earthquake_Landmine", List.of("INSTANT", "AREA", "MANA", "DAMAGE", "LINGER", "PAYLOAD"));
        traits.put("Chain_Lightning_Base", List.of("INSTANT", "PATH", "MANA", "DAMAGE", "PAYLOAD"));
        traits.put("Chain_Lightning_Orbs", List.of("INSTANT", "AREA", "PROJECTILE", "MANA", "DAMAGE", "PAYLOAD"));
        traits.put("Chain_Lightning_Charged_Bolts", List.of("INSTANT", "PATH", "MANA", "DAMAGE"));
        traits.put("Necromancy_Base", List.of("INSTANT", "MANA", "DAMAGE", "GRAVITY", "LINGER", "PAYLOAD"));
        traits.put("Necromancy_Archer", List.of("INSTANT", "MANA", "DAMAGE", "GRAVITY", "LINGER", "PAYLOAD"));
        traits.put("Necromancy_Golem", List.of("INSTANT", "MANA", "DAMAGE", "GRAVITY", "LINGER", "PAYLOAD"));
        traits.put("Grenade_Base", List.of("INSTANT", "AREA", "PROJECTILE", "MANA", "DAMAGE", "GRAVITY", "PAYLOAD"));
        traits.put("Grenade_Sticky", List.of("INSTANT", "AREA", "PROJECTILE", "MANA", "DAMAGE", "GRAVITY", "PAYLOAD"));
        traits.put("Toxic_Grenade", List.of("INSTANT", "AREA", "PROJECTILE", "MANA", "DAMAGE", "GRAVITY", "PAYLOAD"));
        traits.put("Stirrings_Of_Power", List.of("INSTANT", "MANA"));
        traits.put("Stirrings_Of_Power_Dormant", List.of("INSTANT", "MANA"));
        traits.put("Cope_De_Grace", List.of("INSTANT", "MANA"));
        traits.put("Savior", List.of("INSTANT", "MANA"));
        traits.put("Eyes_Of_God", List.of("INSTANT", "MANA"));
        traits.put("Bullet_Time", List.of("INSTANT", "MANA"));
        traits.put("Life_Block_Base", List.of("INSTANT", "MANA", "EXTEND"));
        traits.put("Mana_Block", List.of("INSTANT", "MANA", "EXTEND"));
        table.abilityTraits = traits;
        table.disabledAbilities = new ArrayList<>();

        table("focus_augments", table);
    }

    private static void glyph(FocusAugmentsConfig table, AugmentType type, String kind, String glyphClass, String category, int minLevel,
                              int weight, List<List<String>> requires, List<String> forbids, List<String> specs, List<String> excludes,
                              FocusAugmentsConfig.Potency potency, List<FocusAugmentsConfig.Effect> step, List<FocusAugmentsConfig.Effect> spell,
                              int maxUses, List<Integer> useWeights, int capPerSpell, int costMin, int costMax, boolean noEscalation) {
        FocusAugmentsConfig.Glyph glyph = new FocusAugmentsConfig.Glyph();
        glyph.kind = kind;
        glyph.glyphClass = glyphClass;
        glyph.category = category;
        glyph.minLevel = minLevel;
        glyph.weight = weight;
        glyph.requires = requires;
        glyph.forbids = forbids;
        glyph.specs = specs;
        glyph.excludes = excludes;
        glyph.potency = potency;
        glyph.step = step;
        glyph.spell = spell;
        glyph.maxUses = maxUses;
        glyph.useWeights = useWeights;
        glyph.capPerSpell = capPerSpell;
        glyph.costMin = costMin;
        glyph.costMax = costMax;
        glyph.noEscalation = noEscalation;
        table.glyphs.put(type.id(), glyph);
    }

    /**
     * A gear glyph: a unique glyph locked to the specs its affix acts on, fitting any of them, weight 0 (it never rolls
     * as a random line), one copy per spell, free on its own (the unique glyph rule) and never escalating.
     */
    private static void gearGlyph(FocusAugmentsConfig table, AugmentType type, String category, List<String> specs,
                                  FocusAugmentsConfig.Potency potency, List<FocusAugmentsConfig.Effect> step,
                                  List<FocusAugmentsConfig.Effect> spell) {
        glyph(table, type, "modifier", "unique", category, 0, 0, List.of(List.of()), List.of(), specs, List.of(), potency, step, spell,
                1, List.of(100), 1, 10, 10, true);
    }

    private static void gearAffix(FocusAugmentsConfig table, String attribute, String key, AugmentType glyph, float scale, String orientation) {
        FocusAugmentsConfig.GearGlyph entry = new FocusAugmentsConfig.GearGlyph();
        entry.attribute = attribute;
        entry.key = key;
        entry.glyph = glyph.id();
        entry.scale = scale;
        entry.orientation = orientation;
        table.gearGlyphs.add(entry);
    }

    private static FocusAugmentsConfig.SetGlyph always(AugmentType type) {
        FocusAugmentsConfig.SetGlyph glyph = new FocusAugmentsConfig.SetGlyph();
        glyph.glyph = type.id();
        return glyph;
    }

    private static FocusAugmentsConfig.SetGlyph always(AugmentType type, int uses, int cap) {
        FocusAugmentsConfig.SetGlyph glyph = always(type);
        glyph.uses = uses;
        glyph.cap = cap;
        return glyph;
    }

    /** A unique set rolled from the omega row (regular) or the mythic row (ancient); refuses a draw count its list cannot fill. */
    private static void uniqueSet(FocusAugmentsConfig table, String uniqueId, List<FocusAugmentsConfig.SetGlyph> always, List<AugmentType> draw,
                                  int drawMin, int drawMax) {
        if (drawMin < 0 || drawMax < drawMin || drawMax > draw.size()) {
            throw new IllegalStateException("Unique set " + uniqueId + " draws " + drawMin + "-" + drawMax + " from " + draw.size() + " glyphs");
        }
        FocusAugmentsConfig.UniqueSet set = new FocusAugmentsConfig.UniqueSet();
        set.rarityRow = REGULAR_UNIQUE_ROW;
        set.ancientRarityRow = ANCIENT_UNIQUE_ROW;
        set.always = always;
        set.draw = draw.stream().map(AugmentType::id).toList();
        set.drawMin = drawMin;
        set.drawMax = drawMax;
        table.uniqueSets.put(uniqueId, set);
    }

    private static FocusAugmentsConfig.Potency potency(String label, String format, List<FocusAugmentsConfig.PotencyTier> tiers,
                                                       List<FocusAugmentsConfig.Linked> linked) {
        FocusAugmentsConfig.Potency potency = new FocusAugmentsConfig.Potency();
        potency.label = label;
        potency.format = format;
        potency.tiers = tiers;
        potency.linked = linked;
        return potency;
    }

    private static FocusAugmentsConfig.PotencyTier tier(int minLevel, float min, float max) {
        FocusAugmentsConfig.PotencyTier tier = new FocusAugmentsConfig.PotencyTier();
        tier.minLevel = minLevel;
        tier.min = min;
        tier.max = max;
        return tier;
    }

    private static FocusAugmentsConfig.Linked linked(String name, float min, float max) {
        FocusAugmentsConfig.Linked linked = new FocusAugmentsConfig.Linked();
        linked.name = name;
        linked.min = min;
        linked.max = max;
        return linked;
    }

    private static FocusAugmentsConfig.Effect k(String stat, String role, float k) {
        FocusAugmentsConfig.Effect effect = effect(stat, role);
        effect.k = k;
        return effect;
    }

    private static FocusAugmentsConfig.Effect kBase(String stat, String role, float base, float k) {
        FocusAugmentsConfig.Effect effect = effect(stat, role);
        effect.base = base;
        effect.k = k;
        return effect;
    }

    private static FocusAugmentsConfig.Effect fixed(String stat, String role, float fixed) {
        FocusAugmentsConfig.Effect effect = effect(stat, role);
        effect.fixed = fixed;
        return effect;
    }

    private static FocusAugmentsConfig.Effect perUse(String stat, String role, float perUse) {
        FocusAugmentsConfig.Effect effect = effect(stat, role);
        effect.perUse = perUse;
        return effect;
    }

    private static FocusAugmentsConfig.Effect effect(String stat, String role) {
        FocusAugmentsConfig.Effect effect = new FocusAugmentsConfig.Effect();
        effect.stat = stat;
        effect.role = role;
        return effect;
    }

    /**
     * Refuses an effect whose direction contradicts its role: an upside must move its stat in the player's favour
     * (a positive k on damage, a negative k on mana), a downside or cost against it. For k effects the sign of k
     * decides; fixed and per-use effects are checked by their factor.
     */
    private static void describe(FocusAugmentsConfig table, AugmentType type, String description) {
        table.glyph(type).description = description;
    }

    /** Every glyph that is not an etching glyph needs a description template (etching glyphs show their etching's text). */
    private static void checkDescriptions(FocusAugmentsConfig table) {
        Set<String> etchings = new HashSet<>();
        table.etchingGlyphs.forEach(entry -> etchings.add(entry.glyph));
        for (AugmentType type : AugmentType.values()) {
            FocusAugmentsConfig.Glyph glyph = table.glyph(type);
            if (glyph != null && !etchings.contains(type.id()) && (glyph.description == null || glyph.description.isBlank())) {
                throw new IllegalStateException("Glyph " + type.id() + " has no description template");
            }
        }
    }

    private static void checkRoles(FocusAugmentsConfig table) {
        for (Map.Entry<String, FocusAugmentsConfig.Glyph> entry : table.glyphs.entrySet()) {
            List<FocusAugmentsConfig.Effect> effects = new ArrayList<>(entry.getValue().step);
            effects.addAll(entry.getValue().spell);
            for (FocusAugmentsConfig.Effect effect : effects) {
                float direction = effect.k != null ? effect.k : effect.factor(0.0F) - 1.0F;
                boolean favourable = GlyphStacking.lowerIsBetter(effect.stat) ? direction < 0.0F : direction > 0.0F;
                if (direction == 0.0F || favourable != UP.equals(effect.role)) {
                    throw new IllegalStateException("Glyph " + entry.getKey() + ": the " + effect.role + " on " + effect.stat
                            + " moves the stat the wrong way");
                }
            }
        }
    }

    private static void combo(FocusAugmentsConfig table, String a, String b, int cost, String reason) {
        FocusAugmentsConfig.Combo combo = new FocusAugmentsConfig.Combo();
        combo.a = a;
        combo.b = b;
        combo.cost = cost;
        combo.reason = reason;
        table.combos.add(combo);
    }

    private static FocusAugmentsConfig.CostNoise noise(int offset, int weight) {
        FocusAugmentsConfig.CostNoise noise = new FocusAugmentsConfig.CostNoise();
        noise.offset = offset;
        noise.weight = weight;
        return noise;
    }

    private static void rarity(FocusAugmentsConfig.FocusRolls focus, String rarity, float budget, Float focusBudgetMult, int linesMin,
                               int linesMax, Float budgetRollMin, Float budgetRollMax) {
        FocusAugmentsConfig.RarityRolls rolls = new FocusAugmentsConfig.RarityRolls();
        rolls.budget = budget;
        rolls.focusBudgetMult = focusBudgetMult;
        rolls.linesMin = linesMin;
        rolls.linesMax = linesMax;
        rolls.budgetRollMin = budgetRollMin;
        rolls.budgetRollMax = budgetRollMax;
        focus.rarities.put(rarity, rolls);
    }

    private static FocusAugmentsConfig.DepthTier depth(int minLevel, int min, int max) {
        FocusAugmentsConfig.DepthTier tier = new FocusAugmentsConfig.DepthTier();
        tier.minLevel = minLevel;
        tier.min = min;
        tier.max = max;
        return tier;
    }

    /**
     * The etchings that grant unique glyphs: each glyph is locked to the specs its etching acts on, and its entry reads
     * the etching's value into the glyph's potency (and quality, for a second number or a choice). Delays given in
     * ticks show as seconds. Lucky Bolt is removed: existing copies grant nothing and do nothing.
     */
    private static void etchingGlyphs(FocusAugmentsConfig table) {
        table.etchingGlyphs = new ArrayList<>();
        List<String> novas = List.of("Nova_Base", "Nova_Slow", "Nova_Dot");
        List<String> iceBolts = List.of("Ice_Bolt_Base", "Ice_Bolt_Blast");
        List<String> blizzards = List.of("Shard_Blizzard", "Storm_Arrow_Blizzard");
        List<String> smites = List.of("Smite_Base", "Smite_Archon", "Smite_Thunderstorm");
        List<String> orbs = List.of("Chain_Lightning_Orbs");
        List<String> quakes = List.of("Earthquake_Base", "Earthquake_Singularity", "Earthquake_Tremor");
        List<String> novaChoices = List.of("Nova_Slow", "Nova_Base", "Nova_Dot");

        etching(table, AugmentType.GLACIAL_NOVA, "unique", List.of("Nova_Slow"), potency("freeze chance", "pct_plain", List.of(tier(0, 0.1F, 0.75F)), List.of()),
                EtchingGearAttributes.FROST_NOVA_FREEZE, 1.0F, 0.0F, List.of(), part("potency", "float"));
        etching(table, AugmentType.NOVA_RECAST, "unique", novas, potency("recast after", "seconds", List.of(tier(0, 1.0F, 5.0F)), List.of()),
                EtchingGearAttributes.NOVA_RECAST, TICK, 0.0F, List.of(), part("potency", "int"));
        etching(table, AugmentType.LOW_MANA_NOVA, "unique", List.of("Nova_Base"),
                potency("damage below half mana", "pct", List.of(tier(0, 0.4F, 1.2F)), List.of(linked("threshold", 0.5F, 0.5F))),
                EtchingGearAttributes.NOVA_LOW_MANA_DAMAGE, 1.0F, 0.0F, List.of(), part("potency", "float"), linkedPart("threshold", "float"));
        etching(table, AugmentType.GLACIAL_DAMAGE, "unique", List.of("Ice_Bolt_Blast"),
                potency("attack damage on the blast", "pct_plain", List.of(tier(0, 0.1F, 1.0F)), List.of()),
                EtchingGearAttributes.ICE_BOLT_CHUNK_DAMAGE, 1.0F, 0.0F, List.of(), part("potency", "float"));
        etching(table, AugmentType.SHATTERING_BOLT, "unique", iceBolts,
                potency("glacial shatter chance", "pct_plain", List.of(tier(0, 0.2F, 0.9F)), List.of(linked("duration", 2.0F, 5.0F))),
                EtchingGearAttributes.ICE_BOLT_EFFECT_ETCHING, 1.0F, 0.0F, List.of("the_vault:glacial_shatter"),
                part("choice", null), part("potency", "float"), linkedPart("duration", "int"));
        etching(table, AugmentType.FROZEN_BARRAGE, "unique", iceBolts, potency("extra casts", "default", List.of(tier(0, 4.0F, 10.0F)), List.of()),
                EtchingGearAttributes.ICE_BOLT_MULTICAST, 1.0F, 0.0F, List.of(), part("potency", "int"));
        etching(table, AugmentType.PIERCING_BOLT, "unique", List.of("Ice_Bolt_Base", "Ice_Bolt_Blast", "Shard_Blizzard", "Storm_Arrow_Blizzard"),
                potency("pierce", "default", List.of(tier(0, 4.0F, 15.0F)), List.of()),
                EtchingGearAttributes.ICE_BOLT_PIERCE, 1.0F, 0.0F, List.of(), part("potency", "int"));
        etching(table, AugmentType.BLIZZARD_DAMAGE, "unique", blizzards, potency("ability power per hit", "pct_plain", List.of(tier(0, 0.1F, 0.6F)), List.of()),
                EtchingGearAttributes.STORM_ARROW_BLIZZARD_DAMAGE, 1.0F, 0.0F, List.of(), part("potency", "float"));
        etching(table, AugmentType.BLIZZARD_VULNERABILITY, "unique", blizzards,
                potency("vulnerable stacks", "level", List.of(tier(0, 1.0F, 10.0F)), List.of(linked("duration", 20.0F, 120.0F))),
                EtchingGearAttributes.STORM_ARROW_BLIZZARD_EFFECT, 1.0F, 0.0F, List.of("the_vault:vulnerable"),
                part("choice", null), linkedPart("duration", "int"), part("potency", "int"));
        etching(table, AugmentType.STUNNING_SMITE, "unique", smites, potency("stun chance", "pct_plain", List.of(tier(0, 0.5F, 1.0F)), List.of()),
                EtchingGearAttributes.SMITE_SUN, 1.0F, 0.0F, List.of(), part("potency", "float"));
        etching(table, AugmentType.ECHOING_SMITE, "unique", smites, potency("echo bolt damage", "pct_plain", List.of(tier(0, 0.2F, 1.2F)), List.of()),
                EtchingGearAttributes.SMITE_ECHO, 1.0F, 0.0F, List.of(), part("potency", "float"));
        etching(table, AugmentType.SECOND_STRIKE, "unique", List.of("Smite_Base", "Smite_Archon", "Smite_Thunderstorm", "Chain_Lightning_Base"),
                potency("strikes again after", "seconds", List.of(tier(0, 1.0F, 4.0F)), List.of()),
                EtchingGearAttributes.LIGHTNING_STRIKE_DELAY, TICK, 0.0F, List.of(), part("potency", "float"));
        etching(table, AugmentType.GRAND_ORB, "unique", orbs, potency("orb size", "pct", List.of(tier(0, 0.5F, 2.5F)), List.of()),
                EtchingGearAttributes.LIGHTNING_ORB_SIZE, 1.0F, 0.0F, List.of(), part("potency", "float"));
        etching(table, AugmentType.ORB_REFUND, "unique", orbs, potency("cooldown refunded per kill", "pct_plain", List.of(tier(0, 0.2F, 1.0F)), List.of()),
                EtchingGearAttributes.LIGHTNING_ORB_KILL_COOLDOWN, 1.0F, 0.0F, List.of(), part("potency", "float"));
        etching(table, AugmentType.TRISTORM_ORBS, "unique", orbs, potency("damage per orb (3 orbs)", "pct_plain", List.of(tier(0, 0.4F, 0.8F)), List.of()),
                EtchingGearAttributes.LIGHTNING_ORB_TRIPLE_DAMAGE, -1.0F, 1.0F, List.of(), part("potency", "float"));
        etching(table, AugmentType.ARCANE_PIERCE, "unique", List.of("Arcane_Base"), potency("pierce", "default", List.of(tier(0, 2.0F, 5.0F)), List.of()),
                EtchingGearAttributes.ARCANE_PIERCE, 1.0F, 0.0F, List.of(), part("potency", "int"));
        etching(table, AugmentType.RAIL_PIERCE, "unique", List.of("Arcane_Rail"), potency("pierce", "default", List.of(tier(0, 10.0F, 25.0F)), List.of()),
                EtchingGearAttributes.RAIL_PIERCE, 1.0F, 0.0F, List.of(), part("potency", "int"));
        etching(table, AugmentType.WIDE_RAIL, "unique", List.of("Arcane_Rail"),
                potency("hit width (blocks)", "default", List.of(tier(0, RAIL_SIZE_MIN, 5.0F)), List.of()),
                EtchingGearAttributes.ARCANE_RAIL_SIZE, 1.0F, 0.0F, List.of(), part("potency", "float"));
        etching(table, AugmentType.PRISM_ANCHOR, "unique", List.of("Arcane_Prism"), potency("anchors to blocks on a miss", "none", List.of(tier(0, 1.0F, 1.0F)), List.of()),
                EtchingGearAttributes.PRISM_ANCHOR, 1.0F, 0.0F, List.of(), part("flag", null));
        etching(table, AugmentType.PRISM_SPLIT, "unique", List.of("Arcane_Prism"), potency("also fires the other pattern", "none", List.of(tier(0, 1.0F, 1.0F)), List.of()),
                EtchingGearAttributes.PRISM_SPLIT, 1.0F, 0.0F, List.of(), part("flag", null));
        etching(table, AugmentType.SCATTER_PIERCE, "unique", List.of("Javelin_Piercing"),
                potency("side javelin damage", "pct_plain", List.of(tier(0, 0.3F, 1.2F)), List.of()),
                EtchingGearAttributes.EXTRA_PIERCING_JAVELIN, 1.0F, 0.0F, List.of(), part("potency", "float"));
        etching(table, AugmentType.PIERCING_RICOCHET, "unique", List.of("Javelin_Piercing"), potency("ricochet damage", "pct_plain", List.of(tier(0, 0.3F, 0.8F)), List.of()),
                EtchingGearAttributes.PIERCING_JAVELIN_RICOCHET, -1.0F, 1.0F, List.of(), part("potency", "float"));
        etching(table, AugmentType.SCATTER_MANA, "unique", List.of("Javelin_Scatter"), potency("mana per hit", "default", List.of(tier(0, 1.0F, 8.0F)), List.of()),
                EtchingGearAttributes.SCATTER_MANA, 1.0F, 0.0F, List.of(), part("potency", "int"));
        etching(table, AugmentType.AFTERSHOCK, "unique", quakes, potency("quakes again after", "seconds", List.of(tier(0, 1.0F, 3.5F)), List.of()),
                EtchingGearAttributes.EARTHQUAKE_RECAST_DELAY, TICK, 0.0F, List.of(), part("potency", "int"));
        etching(table, AugmentType.ARCANE_WRATH, "unique", List.of("Totem_Player_Damage"),
                potency("buff also raises ability power", "none", List.of(tier(0, 1.0F, 1.0F)), List.of()),
                EtchingGearAttributes.TOTEM_PLAYER_DAMAGE_AP, 1.0F, 0.0F, List.of(), part("flag", null));
        etching(table, AugmentType.VULNERABLE_TOTEM, "unique", List.of("Totem_Player_Damage"),
                potency("vulnerable stacks", "level", List.of(tier(0, 1.0F, 10.0F)), List.of(linked("duration", 20.0F, 120.0F))),
                EtchingGearAttributes.TOTEM_PLAYER_DAMAGE_EFFECT, 1.0F, 0.0F, List.of("the_vault:vulnerable"),
                part("choice", null), linkedPart("duration", "float"), part("potency", "int"));
        etching(table, AugmentType.SHOCKWAVE, "unique", List.of("Stonefall_Base"),
                potency("stun chance", "pct_plain", List.of(tier(0, 0.2F, 1.0F)), List.of(linked("duration", 40.0F, 120.0F))),
                EtchingGearAttributes.STONE_FALL_STUN, 1.0F, 0.0F, List.of("the_vault:noai"),
                part("potency", "float"), linkedPart("duration", "int"), part("choice", null));
        etching(table, AugmentType.BASH_FORCE, "unique", List.of("Shield_Bash"), potency("attack damage", "pct_plain", List.of(tier(0, 0.1F, 0.5F)), List.of()),
                EtchingGearAttributes.SHIELD_BASH_DAMAGE, 1.0F, 0.0F, List.of(), part("potency", "float"));
        etching(table, AugmentType.HEAL_CLEANSE, "unique", List.of("Heal_Base"), potency("cleanse level", "level", List.of(tier(0, 1.0F, 10.0F)), List.of()),
                EtchingGearAttributes.HEAL_CLEANSE, 1.0F, 0.0F, List.of(), part("potency", "int"));
        etching(table, AugmentType.RAMPAGING_LUCK, "unique", List.of("Rampage_Base"), potency("lucky hit", "pct", List.of(tier(0, 0.02F, 0.12F)), List.of()),
                EtchingGearAttributes.RAMPAGE_LUCKY_HIT, 1.0F, 0.0F, List.of(), part("potency", "float"));
        etching(table, AugmentType.TAP_DAMAGE, "unique", List.of("Implode_Life_Tap"),
                potency("damage per health drained", "default", List.of(tier(0, 1.0F, 8.0F)), List.of()),
                EtchingGearAttributes.LIFE_TAP_EXTRA_DAMAGE, 1.0F, 0.0F, List.of(), part("potency", "int"));
        etching(table, AugmentType.LUCKY_TAP, "unique", List.of("Implode_Life_Tap"),
                potency("% lucky hit per kill", "default", List.of(tier(0, 0.5F, 2.5F)), List.of(linked("duration", 160.0F, 220.0F))),
                EtchingGearAttributes.LIFE_TAP_LUCKY_HIT, 100.0F, 0.0F, List.of(), part("potency", "float"), linkedPart("duration", "int"));
        etching(table, AugmentType.IMPLODING_REVERSAL, "unique", List.of("Mana_Shield_Implode"),
                potency("mana regen per kill", "pct", List.of(tier(0, 0.01F, 0.06F)), List.of()),
                EtchingGearAttributes.IMPLODE_MANA_REGEN, 1.0F, 0.0F, List.of(), part("potency", "float"));
        etching(table, AugmentType.NOVA_SHIELD, "unique", List.of("Mana_Shield_Base"), potency("nova level", "level", List.of(tier(0, 2.0F, 20.0F)), List.of()),
                EtchingGearAttributes.MANA_SHIELD_NOVA, 1.0F, 0.0F, novaChoices, part("choice", null), part("potency", "int"));
        etching(table, AugmentType.NOVA_DASH, "unique", List.of("Dash_Warp"), potency("nova level", "level", List.of(tier(0, 2.0F, 20.0F)), List.of()),
                EtchingGearAttributes.DASH_WARP_NOVA, 1.0F, 0.0F, novaChoices, part("choice", null), part("potency", "int"));
        etching(table, AugmentType.SPIRIT_NOVA, "unique", List.of("Ghost_Walk_Spirit"),
                potency("frost nova level", "level", List.of(tier(0, 1.0F, 10.0F)), List.of()),
                EtchingGearAttributes.SPIRIT_WALK_ABILITY, 1.0F, 0.0F, List.of("Nova_Slow"), part("choice", null), part("potency", "int"));
        etching(table, AugmentType.CONCENTRATED_DRAIN, "unique", List.of("Concentrate_Base"),
                potency("current health drained per effect", "pct_plain", List.of(tier(0, 0.02F, 0.1F)), List.of()),
                ModEtchingGearAttributes.CONCENTRATE_DRAIN, 1.0F, 0.0F, List.of(), part("potency", "float"));
        etching(table, AugmentType.GREEDBALL, "unique", List.of("Fireball_Base"),
                potency("of quantity, rarity and copiously as damage, minimum radius", "pct_plain", List.of(tier(0, 0.1F, 0.4F)), List.of()),
                ModEtchingGearAttributes.FIREBALL_GREEDBALL, 1.0F, 0.0F, List.of(), part("potency", "float"));
        etching(table, AugmentType.VOLLEY_MITOSIS, "unique", List.of("Fireball_Volley"),
                potency("fireballs on first bounce", "default", List.of(tier(0, 1.0F, 3.0F)), List.of()),
                ModEtchingGearAttributes.FIREBALL_VOLLEY_MITOSIS, 1.0F, 0.0F, List.of(), part("potency", "int"));
        etching(table, AugmentType.COLOSSUS_TITAN, "unique", List.of("Colossus_Base"), potency("resistance cap", "pct", List.of(tier(0, 0.04F, 0.08F)), List.of()),
                ModEtchingGearAttributes.COLOSSUS_TITAN_RESISTANCE, 1.0F, 0.0F, List.of(), part("potency", "float"));
        etching(table, AugmentType.NINJA_GETAWAY, "unique", List.of("Sneaky_Getaway"), potency("dodge", "pct", List.of(tier(0, 0.25F, 0.4F)), List.of()),
                ModEtchingGearAttributes.SNEAKY_GETAWAY_NINJA, 1.0F, 0.0F, List.of(), part("potency", "float"));
        etching(table, AugmentType.CHEMICAL_BARRAGE, "unique", List.of("Expunge_Base"), potency("chaos grenades", "default", List.of(tier(0, 3.0F, 9.0F)), List.of()),
                ModEtchingGearAttributes.DIFFUSE_CHEMICAL_BOMB, 1.0F, 0.0F, List.of(), part("potency", "int"));
        etching(table, AugmentType.IMPLODING_BARRIER, "unique", List.of("UltimateShield_Base"),
                potency("chance to implode when hit", "pct_plain", List.of(tier(0, 0.03F, 0.15F)), List.of()),
                ModEtchingGearAttributes.IMPLODING_BARRIER, 1.0F, 0.0F, List.of(), part("potency", "float"));
        etching(table, AugmentType.RAVENOUS_FANGS, "unique", List.of("Fangs_Base"),
                potency("75% less damage, double execute threshold", "none", List.of(tier(0, 1.0F, 1.0F)), List.of()),
                ModEtchingGearAttributes.RAVENOUS_FANGS, 1.0F, 0.0F, List.of(), part("flag", null));

        table.removedEtchings = new ArrayList<>(List.of(EtchingGearAttributes.ICE_BOLT_LUCKY.getRegistryName().toString()));
    }

    private static void etching(FocusAugmentsConfig table, AugmentType type, String category, List<String> specs, FocusAugmentsConfig.Potency potency,
                                VaultGearAttribute<?> attribute, float scale, float offset, List<String> choices, FocusAugmentsConfig.EtchingPart... parts) {
        gearGlyph(table, type, category, specs, potency, List.of(), List.of());
        FocusAugmentsConfig.EtchingGlyph entry = new FocusAugmentsConfig.EtchingGlyph();
        entry.attribute = attribute.getRegistryName().toString();
        entry.glyph = type.id();
        entry.scale = scale;
        entry.offset = offset;
        entry.parts = List.of(parts);
        entry.choices = choices;
        table.etchingGlyphs.add(entry);
    }

    private static FocusAugmentsConfig.EtchingPart part(String role, String type) {
        FocusAugmentsConfig.EtchingPart part = new FocusAugmentsConfig.EtchingPart();
        part.role = role;
        part.type = type;
        return part;
    }

    private static FocusAugmentsConfig.EtchingPart linkedPart(String linked, String type) {
        FocusAugmentsConfig.EtchingPart part = part("linked", type);
        part.linked = linked;
        return part;
    }
}
