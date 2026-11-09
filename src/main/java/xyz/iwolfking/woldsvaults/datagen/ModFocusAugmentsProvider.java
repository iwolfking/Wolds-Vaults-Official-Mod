package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import xyz.iwolfking.woldsvaults.augments.AugmentType;
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
 * the 2026-11-08 prototype with depth +2 and arcana x1.2 over the spec.
 */
public class ModFocusAugmentsProvider extends AbstractWoldsTableProvider {

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
        rules.costNoise = List.of(noise(-1, 25), noise(0, 50), noise(1, 25));
        table.rules = rules;

        FocusAugmentsConfig.Tuning tuning = new FocusAugmentsConfig.Tuning();
        tuning.echoDelaySeconds = 0.5F;
        tuning.shotgunSpreadDegrees = 12.0F;
        tuning.homingRetargetTicks = 5;
        tuning.orbitRadius = 2.5F;
        tuning.orbitHeight = 1.2F;
        tuning.bundleSpeed = 0.8F;
        tuning.railRange = 128.0F;
        tuning.sizeUpMaxScale = 3.0F;
        tuning.sizeDownMinScale = 0.4F;
        tuning.chargeSeconds = 1.0F;
        tuning.chargeMinShare = 0.25F;
        table.tuning = tuning;

        table.glyphs = new LinkedHashMap<>();
        glyph(table, AugmentType.AMPLIFY_AREA, "modifier", "power", 0, 10, List.of(List.of("instant", "area"), List.of("instant", "projectile")), List.of(),
                potency("area", "pct", List.of(tier(0, 0.1F, 0.18F), tier(35, 0.14F, 0.24F), tier(70, 0.18F, 0.3F)), List.of()),
                List.of(k("area", 1.0F), k("damage", -0.25F)),
                List.of(k("mana", 0.4F)),
                3, List.of(60, 30, 10), 3, 4, 8, false);
        glyph(table, AugmentType.STRENGTHEN, "modifier", "power", 15, 8, List.of(List.of("instant")), List.of(),
                potency("damage", "pct", List.of(tier(0, 0.08F, 0.14F), tier(35, 0.11F, 0.19F), tier(70, 0.15F, 0.25F)), List.of()),
                List.of(k("damage", 1.0F), k("area", -0.3F)),
                List.of(k("mana", 0.6F)),
                3, List.of(60, 30, 10), 3, 5, 11, false);
        glyph(table, AugmentType.QUICKEN, "modifier", "flow", 0, 10, List.of(List.of("instant"), List.of("toggle")), List.of(),
                potency("haste", "pct", List.of(tier(0, 0.06F, 0.1F), tier(35, 0.08F, 0.14F), tier(70, 0.1F, 0.18F)), List.of()),
                List.of(k("damage", -0.2F)),
                List.of(k("haste", 1.0F), k("mana", 0.8F)),
                3, List.of(60, 30, 10), 3, 5, 11, false);
        glyph(table, AugmentType.DISCOUNT, "modifier", "flow", 0, 10, List.of(List.of("mana")), List.of(),
                potency("mana", "neg_pct", List.of(tier(0, 0.06F, 0.1F), tier(35, 0.08F, 0.14F), tier(70, 0.1F, 0.18F)), List.of()),
                List.of(),
                List.of(k("mana", -1.0F), k("haste", -0.3F)),
                3, List.of(60, 30, 10), 3, 4, 8, false);
        glyph(table, AugmentType.DELAY, "delay", "flow", 0, 6, List.of(), List.of(),
                potency("pause", "seconds", List.of(tier(0, 0.25F, 1.0F)), List.of()),
                List.of(),
                List.of(fixed("mana", 1.02F)),
                4, List.of(40, 30, 20, 10), 4, 1, 1, true);
        glyph(table, AugmentType.ACCELERATE, "modifier", "path", 0, 10, List.of(List.of("instant", "projectile"), List.of("bundle")), List.of(),
                potency("speed", "pct", List.of(tier(0, 0.15F, 0.25F), tier(35, 0.2F, 0.32F), tier(70, 0.25F, 0.4F)), List.of()),
                List.of(k("speed", 1.0F), k("damage", -0.08F)),
                List.of(k("mana", 0.2F)),
                3, List.of(60, 30, 10), 3, 2, 6, false);
        glyph(table, AugmentType.HOMING, "modifier", "path", 20, 6, List.of(List.of("instant", "projectile"), List.of("bundle")), List.of("orbit"),
                potency("range", "blocks", List.of(tier(20, 10.0F, 16.0F)), List.of(linked("turn rate", 0.12F, 0.22F))),
                List.of(fixed("damage", 0.9F), fixed("speed", 0.9F)),
                List.of(fixed("mana", 1.1F)),
                1, List.of(100), 1, 7, 12, false);
        glyph(table, AugmentType.ORBIT, "modifier", "path", 20, 4, List.of(List.of("instant", "projectile"), List.of("bundle")), List.of("homing"),
                potency("duration", "seconds", List.of(tier(20, 6.0F, 12.0F)), List.of()),
                List.of(fixed("damage", 0.85F)),
                List.of(fixed("mana", 1.15F), fixed("cooldown", 1.1F)),
                1, List.of(100), 1, 7, 12, false);
        glyph(table, AugmentType.LOW_GRAVITY, "modifier", "path", 0, 8, List.of(List.of("instant", "projectile"), List.of("bundle")), List.of(),
                potency("gravity", "neg_pct", List.of(tier(0, 0.4F, 0.7F)), List.of()),
                List.of(fixed("speed", 0.95F)),
                List.of(fixed("mana", 1.04F)),
                1, List.of(100), 1, 1, 4, false);
        glyph(table, AugmentType.ETHEREAL, "modifier", "path", 60, 2, List.of(List.of("instant", "projectile"), List.of("bundle")), List.of(),
                potency("lifetime", "seconds", List.of(tier(60, 4.0F, 8.0F)), List.of()),
                List.of(fixed("damage", 0.85F)),
                List.of(fixed("mana", 1.2F)),
                1, List.of(100), 1, 11, 16, false);
        glyph(table, AugmentType.SIZE_UP, "modifier", "form", 0, 8, List.of(List.of("instant", "projectile"), List.of("instant", "bolt"), List.of("bundle")), List.of(),
                potency("size", "pct", List.of(tier(0, 0.2F, 0.3F), tier(35, 0.25F, 0.4F), tier(70, 0.3F, 0.5F)), List.of()),
                List.of(k("scale", 1.0F), k("speed", -0.3F)),
                List.of(k("mana", 0.3F)),
                2, List.of(70, 30), 2, 4, 8, false);
        glyph(table, AugmentType.SIZE_DOWN, "modifier", "form", 0, 6, List.of(List.of("instant", "projectile"), List.of("bundle")), List.of(),
                potency("size", "neg_pct", List.of(tier(0, 0.15F, 0.22F), tier(35, 0.18F, 0.26F), tier(70, 0.2F, 0.3F)), List.of()),
                List.of(k("scale", -1.0F), k("damage", 0.6F), k("speed", 0.5F)),
                List.of(fixed("mana", 1.05F)),
                2, List.of(70, 30), 2, 4, 8, false);
        glyph(table, AugmentType.ECHO, "modifier", "chaos", 60, 2, List.of(List.of("instant")), List.of(),
                potency("echo power", "pct_plain", List.of(tier(60, 0.45F, 0.65F)), List.of()),
                List.of(),
                List.of(fixed("mana", 1.5F), fixed("cooldown", 1.3F)),
                1, List.of(100), 1, 14, 22, false);
        glyph(table, AugmentType.SHOTGUN, "modifier", "chaos", 50, 3, List.of(List.of("instant", "projectile"), List.of("instant", "bolt"), List.of("bundle")), List.of(),
                potency("copy damage", "pct_plain", List.of(tier(50, 0.7F, 0.8F), tier(70, 0.75F, 0.85F)), List.of()),
                List.of(),
                List.of(perUse("mana", 0.3F), perUse("cooldown", 0.1F)),
                3, List.of(50, 35, 15), 3, 12, 19, false);
        glyph(table, AugmentType.CHARGE, "modifier", "chaos", 60, 2, List.of(List.of("instant", "projectile"), List.of("instant", "area"), List.of("instant", "bolt")), List.of(),
                potency("full charge", "pct", List.of(tier(60, 0.6F, 1.0F)), List.of()),
                List.of(),
                List.of(fixed("mana", 1.35F), fixed("cooldown", 1.25F)),
                1, List.of(100), 1, 12, 18, false);
        glyph(table, AugmentType.BUNDLED_SPELL, "bundle", "chaos", 40, 3, List.of(), List.of(),
                potency("flight", "seconds", List.of(tier(40, 3.0F, 6.0F)), List.of()),
                List.of(),
                List.of(fixed("mana", 1.25F), fixed("cooldown", 1.15F)),
                1, List.of(100), 2, 12, 19, false);

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
        rarity(focus, "SCRAPPY", 0.6F, 2, 3, null, null);
        rarity(focus, "COMMON", 0.8F, 3, 4, null, null);
        rarity(focus, "RARE", 1.0F, 4, 5, null, null);
        rarity(focus, "EPIC", 1.15F, 5, 6, null, null);
        rarity(focus, "UNIQUE", 1.15F, 5, 6, null, null);
        rarity(focus, "SPECIAL", 1.15F, 5, 6, null, null);
        rarity(focus, "OMEGA", 1.3F, 6, 7, null, null);
        rarity(focus, "MYTHIC", 1.5F, 8, 9, null, null);
        rarity(focus, "CHAOTIC", 1.3F, 4, 8, 0.75F, 1.25F);
        focus.depth = List.of(depth(0, 3, 3), depth(40, 3, 4), depth(70, 4, 4));
        focus.depthBonus = new LinkedHashMap<>();
        focus.depthBonus.put("OMEGA", depth(70, 4, 5));
        focus.depthBonus.put("MYTHIC", depth(70, 4, 5));
        table.focus = focus;

        FocusAugmentsConfig.WandRolls wand = new FocusAugmentsConfig.WandRolls();
        wand.budgetFactor = 0.6F;
        wand.linesDelta = -2;
        wand.minLines = 1;
        wand.maxDepth = 4;
        table.wand = wand;

        Map<String, List<String>> traits = new LinkedHashMap<>();
        traits.put("UltimateShield_Base", List.of("TOGGLE", "MANA"));
        traits.put("Fangs_Base", List.of("INSTANT", "AREA", "MANA"));
        traits.put("Fangs_Maw", List.of("INSTANT", "AREA", "MANA"));
        traits.put("Expunge_Base", List.of("INSTANT", "AREA", "MANA"));
        traits.put("Concentrate_Base", List.of("INSTANT", "AREA", "MANA"));
        traits.put("Colossus_Base", List.of("INSTANT", "MANA"));
        traits.put("Sneaky_Getaway", List.of("INSTANT", "MANA"));
        traits.put("Heal_Base", List.of("INSTANT", "MANA"));
        traits.put("Heal_Group", List.of("INSTANT", "AREA", "MANA"));
        traits.put("Heal_Cleanse", List.of("INSTANT", "MANA"));
        traits.put("Dash_Base", List.of("INSTANT", "MANA"));
        traits.put("Dash_Damage", List.of("INSTANT", "AREA", "MANA"));
        traits.put("Dash_Warp", List.of("INSTANT", "MANA"));
        traits.put("Nova_Base", List.of("INSTANT", "AREA", "MANA"));
        traits.put("Nova_Slow", List.of("INSTANT", "AREA", "MANA"));
        traits.put("Nova_Dot", List.of("INSTANT", "AREA", "MANA"));
        traits.put("Execute_Base", List.of("INSTANT"));
        traits.put("Ghost_Walk_Base", List.of("INSTANT", "MANA"));
        traits.put("Ghost_Walk_Spirit", List.of("INSTANT", "MANA"));
        traits.put("Mega_Jump_Base", List.of("INSTANT", "AREA", "MANA"));
        traits.put("Mega_Jump_Break_Up", List.of("INSTANT", "AREA", "MANA"));
        traits.put("Mega_Jump_Break_Down", List.of("INSTANT", "AREA", "MANA"));
        traits.put("Levitate", List.of("HOLD", "MANA"));
        traits.put("Rampage_Base", List.of("TOGGLE", "MANA"));
        traits.put("Rampage_Bloodlust", List.of("TOGGLE", "MANA"));
        traits.put("Rampage_Berserker", List.of("TOGGLE", "MANA"));
        traits.put("Rampage_Instinct", List.of("TOGGLE", "MANA"));
        traits.put("Rampage_Leech", List.of("TOGGLE", "MANA"));
        traits.put("Rampage_Chain", List.of("TOGGLE", "MANA"));
        traits.put("Smite_Base", List.of("TOGGLE", "AREA", "MANA"));
        traits.put("Smite_Archon", List.of("TOGGLE", "AREA", "MANA"));
        traits.put("Smite_Blast_Wave", List.of("TOGGLE", "AREA", "MANA"));
        traits.put("Smite_Thunderstorm", List.of("TOGGLE", "AREA", "MANA"));
        traits.put("Summon_Eternal_Base", List.of("INSTANT", "MANA"));
        traits.put("Empower_Base", List.of("TOGGLE", "AREA", "MANA"));
        traits.put("Empower_Ice_Armor", List.of("TOGGLE", "MANA"));
        traits.put("Empower_Slowness_Aura", List.of("TOGGLE", "AREA", "MANA"));
        traits.put("Vein_Miner_Base", List.of("HOLD", "AREA"));
        traits.put("Vein_Miner_Fortune", List.of("HOLD", "AREA"));
        traits.put("Vein_Miner_Durability", List.of("HOLD", "AREA"));
        traits.put("Vein_Miner_Void", List.of("HOLD", "AREA"));
        traits.put("Vein_Miner_Chain", List.of("HOLD", "AREA"));
        traits.put("Hunter_Base", List.of("INSTANT", "MANA"));
        traits.put("Javelin_Sight", List.of("INSTANT", "AREA", "PROJECTILE", "MANA"));
        traits.put("Farmer_Base", List.of("TOGGLE", "AREA", "MANA"));
        traits.put("Farmer_Melon", List.of("TOGGLE", "AREA", "MANA"));
        traits.put("Farmer_Cactus", List.of("TOGGLE", "AREA", "MANA"));
        traits.put("Farmer_Animal", List.of("TOGGLE", "AREA", "MANA"));
        traits.put("Mana_Shield_Base", List.of("INSTANT", "MANA"));
        traits.put("Mana_Barrier", List.of("INSTANT", "AREA", "MANA"));
        traits.put("Shield_Bash", List.of("INSTANT", "AREA", "MANA"));
        traits.put("Shield_Bash_Earthshatter", List.of("INSTANT", "AREA", "MANA"));
        traits.put("Shield_Bash_Battering_Ram", List.of("INSTANT", "AREA", "MANA"));
        traits.put("Mana_Shield_Retribution", List.of("INSTANT", "AREA", "MANA"));
        traits.put("Mana_Shield_Implode", List.of("INSTANT", "AREA", "MANA"));
        traits.put("Implode_Life_Tap", List.of("INSTANT", "AREA"));
        traits.put("Taunt_Base", List.of("INSTANT", "AREA", "MANA"));
        traits.put("Taunt_Repel", List.of("INSTANT", "AREA", "MANA"));
        traits.put("Taunt_Decoy", List.of("INSTANT", "AREA", "MANA"));
        traits.put("Taunt_Charm", List.of("INSTANT", "AREA", "MANA"));
        traits.put("Stonefall_Base", List.of("INSTANT", "AREA", "MANA"));
        traits.put("Stonefall_Snow", List.of("INSTANT", "AREA", "MANA"));
        traits.put("Stonefall_Cold", List.of("INSTANT", "AREA", "MANA"));
        traits.put("Totem_Base", List.of("INSTANT", "AREA", "MANA"));
        traits.put("Totem_Player_Damage", List.of("INSTANT", "AREA", "MANA"));
        traits.put("Totem_Mana_Regen", List.of("INSTANT", "AREA", "MANA"));
        traits.put("Totem_Mob_Damage", List.of("INSTANT", "AREA", "MANA"));
        traits.put("Javelin_Base", List.of("INSTANT", "PROJECTILE", "MANA"));
        traits.put("Javelin_Piercing", List.of("INSTANT", "PROJECTILE", "MANA"));
        traits.put("Javelin_Scatter", List.of("INSTANT", "PROJECTILE", "MANA"));
        traits.put("Shell_Base", List.of("TOGGLE", "MANA"));
        traits.put("Shell_Porcupine", List.of("TOGGLE", "MANA"));
        traits.put("Shell_Quill", List.of("TOGGLE", "MANA"));
        traits.put("Fireball_Base", List.of("INSTANT", "PROJECTILE", "MANA"));
        traits.put("Fireball_Volley", List.of("INSTANT", "PROJECTILE", "MANA"));
        traits.put("Fireball_Fireshot", List.of("INSTANT", "PROJECTILE", "MANA"));
        traits.put("Storm_Arrow_Base", List.of("INSTANT", "AREA", "PROJECTILE", "MANA"));
        traits.put("Storm_Arrow_Blizzard", List.of("INSTANT", "AREA", "MANA"));
        traits.put("Battle_Cry_Base", List.of("INSTANT", "AREA", "MANA"));
        traits.put("Battle_Cry_Spectral_Strike", List.of("INSTANT", "AREA", "MANA"));
        traits.put("Battle_Cry_Lucky_Strike", List.of("INSTANT", "AREA", "MANA"));
        traits.put("Ice_Bolt_Base", List.of("INSTANT", "PROJECTILE", "MANA"));
        traits.put("Ice_Bolt_Blast", List.of("INSTANT", "AREA", "PROJECTILE", "MANA"));
        traits.put("Shard_Blizzard", List.of("INSTANT", "AREA", "PROJECTILE", "MANA"));
        traits.put("Arcane_Base", List.of("HOLD", "BEAM", "MANA"));
        traits.put("Arcane_Rail", List.of("INSTANT", "BOLT", "MANA"));
        traits.put("Arcane_Prism", List.of("INSTANT", "MANA"));
        traits.put("Earthquake_Base", List.of("INSTANT", "AREA", "MANA"));
        traits.put("Earthquake_Singularity", List.of("INSTANT", "AREA", "MANA"));
        traits.put("Earthquake_Tremor", List.of("INSTANT", "AREA", "MANA"));
        traits.put("Earthquake_Landmine", List.of("INSTANT", "AREA", "MANA"));
        traits.put("Chain_Lightning_Base", List.of("INSTANT", "MANA"));
        traits.put("Chain_Lightning_Orbs", List.of("INSTANT", "AREA", "PROJECTILE", "MANA"));
        traits.put("Chain_Lightning_Charged_Bolts", List.of("INSTANT", "MANA"));
        traits.put("Necromancy_Base", List.of("INSTANT", "MANA"));
        traits.put("Necromancy_Archer", List.of("INSTANT", "MANA"));
        traits.put("Necromancy_Golem", List.of("INSTANT", "MANA"));
        traits.put("Grenade_Base", List.of("INSTANT", "AREA", "PROJECTILE", "MANA"));
        traits.put("Grenade_Sticky", List.of("INSTANT", "AREA", "PROJECTILE", "MANA"));
        traits.put("Toxic_Grenade", List.of("INSTANT", "AREA", "PROJECTILE", "MANA"));
        table.abilityTraits = traits;
        table.disabledAbilities = new ArrayList<>();

        table("focus_augments", table);
    }

    private static void glyph(FocusAugmentsConfig table, AugmentType type, String kind, String category, int minLevel, int weight,
                              List<List<String>> requires, List<String> excludes, FocusAugmentsConfig.Potency potency,
                              List<FocusAugmentsConfig.Effect> step, List<FocusAugmentsConfig.Effect> spell, int maxUses,
                              List<Integer> useWeights, int capPerSpell, int costMin, int costMax, boolean noEscalation) {
        FocusAugmentsConfig.Glyph glyph = new FocusAugmentsConfig.Glyph();
        glyph.kind = kind;
        glyph.category = category;
        glyph.minLevel = minLevel;
        glyph.weight = weight;
        glyph.requires = requires;
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

    private static FocusAugmentsConfig.Effect k(String stat, float k) {
        FocusAugmentsConfig.Effect effect = new FocusAugmentsConfig.Effect();
        effect.stat = stat;
        effect.k = k;
        return effect;
    }

    private static FocusAugmentsConfig.Effect fixed(String stat, float fixed) {
        FocusAugmentsConfig.Effect effect = new FocusAugmentsConfig.Effect();
        effect.stat = stat;
        effect.fixed = fixed;
        return effect;
    }

    private static FocusAugmentsConfig.Effect perUse(String stat, float perUse) {
        FocusAugmentsConfig.Effect effect = new FocusAugmentsConfig.Effect();
        effect.stat = stat;
        effect.perUse = perUse;
        return effect;
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

    private static void rarity(FocusAugmentsConfig.FocusRolls focus, String rarity, float budget, int linesMin, int linesMax,
                               Float budgetRollMin, Float budgetRollMax) {
        FocusAugmentsConfig.RarityRolls rolls = new FocusAugmentsConfig.RarityRolls();
        rolls.budget = budget;
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
