package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import xyz.iwolfking.woldsvaults.augments.AugmentType;
import xyz.iwolfking.woldsvaults.augments.spell.GlyphStacking;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractWoldsTableProvider;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        glyph(table, AugmentType.CHAINING, "modifier", "unique", "power", 0, 0, List.of(List.of("instant", "damage", "direct")), List.of("linger"),
                List.of(), List.of(),
                potency("chains", "none", List.of(tier(0, 0.0F, 0.0F)), List.of()),
                List.of(),
                List.of(),
                1, List.of(100), 1, 10, 10, true);
        checkRoles(table);

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
}
