package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.ability.special.EntropyPoisonModification;
import iskallia.vault.gear.attribute.ability.special.FrostNovaVulnerabilityModification;
import iskallia.vault.gear.etching.EtchingGearAttributes;
import iskallia.vault.init.ModGearAttributes;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractWoldsTableProvider;
import xyz.iwolfking.woldsvaults.datagen.lib.AncientGearModifiers;
import xyz.iwolfking.woldsvaults.datagen.spellcraft.EtchingGlyphBuilder;
import xyz.iwolfking.woldsvaults.datagen.spellcraft.FocusRarityBuilder;
import xyz.iwolfking.woldsvaults.datagen.spellcraft.GlyphBuilder;
import xyz.iwolfking.woldsvaults.init.ModEtchingGearAttributes;
import xyz.iwolfking.woldsvaults.modifiers.gear.special.FireballModification;
import xyz.iwolfking.woldsvaults.modifiers.gear.special.GlacialBlastModification;
import xyz.iwolfking.woldsvaults.modifiers.gear.special.ImplodingJavelinModification;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphStacking;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Generates the spellcraft table: glyph definitions, combo surcharges, focus and wand rolls, spell rules and tuning,
 * ability traits, the gear and etching glyph grants and the unique glyph sets.
 */
public class ModSpellcraftProvider extends AbstractWoldsTableProvider {
    private static final String UP = "upside";
    private static final String DOWN = "downside";
    private static final String COST = "cost";
    private static final String HIGHER = "higher";
    private static final String LOWER = "lower";
    private static final String REGULAR_UNIQUE_ROW = "OMEGA";
    private static final String ANCIENT_UNIQUE_ROW = "MYTHIC";
    private static final float TICK = 0.05F;
    public static final float RAIL_SIZE_MIN = 1.1F;

    public ModSpellcraftProvider(DataGenerator generator) {
        super(generator, "spellcraft");
    }

    @Override
    public void registerConfigs() {
        SpellcraftConfig table = new SpellcraftConfig();
        table.worksOutsideVaults = true;
        table.rules = rules();
        table.tuning = tuning();
        table.glyphs = new LinkedHashMap<>();
        powerAndFlowGlyphs(table);
        pathGlyphs(table);
        formGlyphs(table);
        chaosGlyphs(table);
        buffGlyphs(table);
        chainingGlyph(table);
        combos(table);
        table.focus = focusRolls();
        table.wand = wandRolls();
        table.abilityTraits = abilityTraits();
        table.disabledAbilities = new ArrayList<>();
        gearGlyphs(table);
        gearAffixes(table);
        etchingGlyphs(table);
        uniqueSets(table);
        checkDescriptions(table);
        checkRoles(table);
        table("spellcraft", table);
    }

    private static SpellcraftConfig.Rules rules() {
        SpellcraftConfig.Rules rules = new SpellcraftConfig.Rules();
        rules.escalationPerGlyph = 0.12F;
        rules.abilityStepCost = 7;
        rules.chainCooldownShare = 0.25F;
        rules.manaFactorFloor = 0.5F;
        rules.glyphHasteHeadroom = 1.0F;
        rules.maxGlyphsPerSpell = 12;
        rules.maxSpellsPerItem = 6;
        rules.maxCastsPerTrigger = 16;
        rules.maxBundleNesting = 2;
        rules.maxNameLength = 24;
        rules.defaultPriority = 3;
        rules.priorities = 5;
        rules.uniqueMixCost = 10;
        rules.costNoise = List.of(noise(-1, 25), noise(0, 50), noise(1, 25));
        return rules;
    }

    private static SpellcraftConfig.Tuning tuning() {
        SpellcraftConfig.Tuning tuning = new SpellcraftConfig.Tuning();
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
        return tuning;
    }

    private static void powerAndFlowGlyphs(SpellcraftConfig table) {
        glyph(GlyphType.AMPLIFY_AREA).glyphClass("common").category("power").weight(10)
                .requires(List.of("instant", "area"))
                .potency(potency("area", "pct", List.of(tier(0, 0.12F, 0.216F), tier(35, 0.168F, 0.288F), tier(70, 0.216F, 0.36F)), List.of()))
                .step(k("area", UP, 1.0F), k("damage", DOWN, -0.25F / 1.2F))
                .spell(k("mana", COST, 0.4F / 1.2F))
                .uses(3, List.of(60, 30, 10)).cap(3).cost(4, 8).spellWide()
                .description("<gray>Increases area by <#5FD068>%.0fpvalue% <gray>on every ability in the spell it fits")
                .register(table);
        glyph(GlyphType.STRENGTHEN).glyphClass("common").category("power").minLevel(15).weight(8)
                .requires(List.of("instant", "damage"))
                .potency(potency("damage", "pct", List.of(tier(0, 0.096F, 0.168F), tier(35, 0.132F, 0.228F), tier(70, 0.18F, 0.3F)), List.of()))
                .step(k("damage", UP, 1.0F), k("area", DOWN, -0.3F / 1.2F))
                .spell(k("mana", COST, 0.6F / 1.2F))
                .uses(3, List.of(60, 30, 10)).cap(3).cost(5, 11).spellWide()
                .description("<gray>Increases damage by <#E0564A>%.0fpvalue% <gray>on every ability in the spell it fits")
                .register(table);
        glyph(GlyphType.QUICKEN).glyphClass("common").category("flow").weight(10)
                .requires(List.of("instant"), List.of("toggle"))
                .potency(potency("haste", "pct", List.of(tier(0, 0.072F, 0.12F), tier(35, 0.096F, 0.168F), tier(70, 0.12F, 0.216F)), List.of()))
                .step(k("damage", DOWN, -0.2F / 1.2F))
                .spell(k("haste", UP, 1.0F), k("mana", COST, 0.8F / 1.2F))
                .uses(3, List.of(60, 30, 10)).cap(3).cost(5, 11)
                .description("<gray>Increases spell haste by <#4AC6E0>%.0fpvalue%<gray>, offsetting cooldown penalties even at the haste cap")
                .register(table);
        glyph(GlyphType.DISCOUNT).glyphClass("common").category("flow").weight(10)
                .requires(List.of("mana"))
                .potency(potency("mana efficiency", "pct", List.of(tier(0, 0.077F, 0.133F), tier(35, 0.104F, 0.195F), tier(70, 0.133F, 0.263F)), List.of()))
                .spell(k("efficiency", UP, 1.0F), k("haste", DOWN, -0.2F))
                .uses(3, List.of(60, 30, 10)).cap(3).cost(4, 8)
                .description("<gray>Increases mana efficiency by <#6FA8FF>%.0fpvalue%")
                .register(table);
        glyph(GlyphType.DELAY).kind("delay").glyphClass("utility").category("flow").weight(6)
                .potency(potency("pause", "seconds", List.of(tier(0, 0.25F, 1.0F)), List.of()))
                .spell(fixed("mana", COST, 1.02F))
                .uses(4, List.of(40, 30, 20, 10)).cap(4).cost(1, 1).noEscalation()
                .description("<gray>Pauses the spell for <yellow>%.2fvalue <gray>seconds")
                .register(table);
    }

    private static void pathGlyphs(SpellcraftConfig table) {
        glyph(GlyphType.ACCELERATE).glyphClass("common").category("path").weight(10)
                .requires(List.of("instant", "path"), List.of("instant", "projectile"), List.of("bundle"))
                .potency(potency("speed", "pct", List.of(tier(0, 0.18F, 0.3F), tier(35, 0.24F, 0.384F), tier(70, 0.3F, 0.48F)), List.of()))
                .step(k("speed", UP, 1.0F), k("damage", DOWN, -0.08F / 1.2F))
                .spell(k("mana", COST, 0.2F / 1.2F))
                .uses(3, List.of(60, 30, 10)).cap(3).cost(2, 6).spellWide()
                .description("<gray>Increases projectile speed by <yellow>%.0fpvalue% <gray>on every ability or orb in the spell it fits")
                .register(table);
        glyph(GlyphType.HOMING).glyphClass("utility").category("path").minLevel(20).weight(6)
                .requires(List.of("instant", "path"), List.of("instant", "projectile"), List.of("bundle"))
                .excludes("orbit")
                .potency(potency("turn rate", "deg_s", List.of(tier(20, 60.0F, 120.0F), tier(50, 80.0F, 150.0F), tier(80, 100.0F, 180.0F)), List.of()))
                .step(fixed("damage", DOWN, 0.9F), fixed("speed", DOWN, 0.9F))
                .spell(fixed("mana", COST, 1.1F))
                .uses(1, List.of(100)).cap(1).cost(7, 12)
                .description("<gray>Projectiles home onto targets, turning <yellow>%.0fvalue<gray> degrees per second")
                .register(table);
        glyph(GlyphType.ORBIT).glyphClass("utility").category("path").minLevel(20).weight(4)
                .requires(List.of("instant", "projectile"), List.of("bundle"))
                .excludes("homing")
                .potency(potency("duration", "seconds", List.of(tier(20, 6.0F, 12.0F)), List.of()))
                .step(fixed("damage", DOWN, 0.85F))
                .spell(fixed("mana", COST, 1.15F), fixed("cooldown", COST, 1.1F))
                .uses(1, List.of(100)).cap(1).cost(7, 12)
                .description("<gray>Projectiles orbit you for <yellow>%.1fvalue <gray>seconds")
                .register(table);
        glyph(GlyphType.LOW_GRAVITY).glyphClass("utility").category("path").weight(8)
                .requires(List.of("instant", "gravity"), List.of("bundle"))
                .potency(potency("gravity", "neg_pct", List.of(tier(0, 0.4F, 0.7F)), List.of()))
                .step(fixed("speed", DOWN, 0.95F))
                .spell(fixed("mana", COST, 1.04F))
                .uses(1, List.of(100)).cap(1).cost(1, 4)
                .description("<gray>Projectiles fall <yellow>%.0fpvalue% <gray>slower")
                .register(table);
        glyph(GlyphType.ETHEREAL).glyphClass("utility").category("path").minLevel(60).weight(2)
                .requires(List.of("instant", "path"), List.of("instant", "projectile"), List.of("bundle"))
                .potency(potency("lifetime", "seconds", List.of(tier(60, 4.0F, 8.0F)), List.of()))
                .step(fixed("damage", DOWN, 0.85F))
                .spell(fixed("mana", COST, 1.2F))
                .uses(1, List.of(100)).cap(1).cost(11, 16)
                .description("<gray>Projectiles pass through walls for <yellow>%.1fvalue <gray>seconds")
                .register(table);
    }

    private static void formGlyphs(SpellcraftConfig table) {
        glyph(GlyphType.SIZE_UP).glyphClass("common").category("form").weight(8)
                .requires(List.of("instant", "path"), List.of("instant", "projectile"), List.of("instant", "bolt"), List.of("bundle"))
                .potency(potency("size", "pct", List.of(tier(0, 0.24F, 0.36F), tier(35, 0.3F, 0.48F), tier(70, 0.36F, 0.6F)), List.of()))
                .step(k("scale", UP, 1.0F), k("speed", DOWN, -0.3F / 1.2F))
                .spell(k("mana", COST, 0.3F / 1.2F))
                .uses(2, List.of(70, 30)).cap(2).cost(4, 8).spellWide()
                .description("<gray>Increases projectile size by <yellow>%.0fpvalue% <gray>on every ability or orb in the spell it fits")
                .register(table);
        glyph(GlyphType.SIZE_DOWN).glyphClass("common").category("form").weight(6)
                .requires(List.of("instant", "path"), List.of("instant", "projectile"), List.of("bundle"))
                .potency(potency("size", "neg_pct", List.of(tier(0, 0.15F, 0.22F), tier(35, 0.18F, 0.26F), tier(70, 0.2F, 0.3F)), List.of()))
                .step(k("scale", DOWN, -1.0F), k("damage", UP, 0.72F), k("speed", UP, 0.6F))
                .spell(fixed("mana", COST, 1.05F))
                .uses(2, List.of(70, 30)).cap(2).cost(4, 8).spellWide()
                .description("<gray>Reduces projectile size by <yellow>%.0fpvalue% <gray>on every ability or orb in the spell it fits, hitting harder and flying faster")
                .register(table);
    }

    private static void chaosGlyphs(SpellcraftConfig table) {
        glyph(GlyphType.ECHO).glyphClass("special").category("chaos").minLevel(60).weight(2)
                .requires(List.of("instant", "damage"))
                .potency(potency("echo power", "pct_plain", List.of(tier(60, 0.45F, 0.65F)), List.of()))
                .spell(fixed("mana", COST, 1.5F), fixed("cooldown", COST, 1.3F))
                .uses(1, List.of(100)).cap(1).cost(14, 22)
                .description("<gray>Recasts the ability after a short delay at <#E0564A>%.0fpvalue% <gray>power")
                .register(table);
        glyph(GlyphType.SHOTGUN).glyphClass("special").category("chaos").minLevel(50).weight(3)
                .requires(List.of("instant", "path"), List.of("instant", "projectile"), List.of("instant", "bolt"), List.of("bundle"))
                .potency(potency("copy damage", "pct_plain", List.of(tier(50, 0.7F, 0.8F), tier(70, 0.75F, 0.85F)), List.of()))
                .spell(perUse("mana", COST, 0.3F), perUse("cooldown", COST, 0.1F))
                .uses(3, List.of(50, 35, 15)).cap(3).cost(12, 19)
                .description("<gray>Fires an extra copy dealing <#E0564A>%.0fpvalue% <gray>damage")
                .register(table);
        glyph(GlyphType.CHARGE).glyphClass("special").category("chaos").minLevel(60).weight(2)
                .requires(List.of("instant", "damage", "path"), List.of("instant", "damage", "projectile"), List.of("instant", "damage", "area"),
                        List.of("instant", "damage", "bolt"))
                .forbids("linger")
                .potency(potency("full charge", "pct", List.of(tier(60, 0.9F, 1.5F)), List.of()))
                .spell(fixed("mana", COST, 1.35F), fixed("cooldown", COST, 1.25F))
                .uses(1, List.of(100)).cap(1).cost(12, 18).spellWide()
                .description("<gray>Hold the key to charge; a full charge deals <#E0564A>%.0fpvalue% <gray>more damage with every ability in the spell it fits")
                .register(table);
        glyph(GlyphType.BUNDLED_SPELL).kind("bundle").glyphClass("special").category("chaos").minLevel(40).weight(3)
                .potency(potency("flight", "seconds", List.of(tier(40, 3.0F, 6.0F)), List.of()))
                .spell(fixed("mana", COST, 1.25F), fixed("cooldown", COST, 1.15F))
                .uses(1, List.of(100)).cap(1).cost(12, 19)
                .description("<gray>Throws the rest of the spell as an orb that flies for <yellow>%.1fvalue <gray>seconds")
                .register(table);
    }

    private static void buffGlyphs(SpellcraftConfig table) {
        glyph(GlyphType.INTENSIFY).glyphClass("common").category("power").minLevel(20).weight(8)
                .requires(List.of("instant", "intensify"), List.of("toggle", "intensify"))
                .potency(potency("intensity", "pct", List.of(tier(0, 0.1F, 0.14F), tier(35, 0.12F, 0.16F), tier(70, 0.14F, 0.18F)), List.of()))
                .step(k("intensity", UP, 1.0F), fixed("duration", DOWN, 0.85F), k("drain", DOWN, 1.5F), k("heal_mana", DOWN, 1.5F))
                .spell(fixed("mana", COST, 1.15F))
                .uses(2, List.of(70, 30)).cap(2).cost(6, 10).spellWide()
                .description("<gray>Increases buff strength by <gold>%.0fpvalue% <gray>on every ability in the spell it fits")
                .register(table);
        glyph(GlyphType.EXTEND).glyphClass("common").category("flow").minLevel(20).weight(8)
                .requires(List.of("instant", "extend"), List.of("toggle", "extend"))
                .potency(potency("duration", "pct", List.of(tier(0, 0.2F, 0.275F), tier(35, 0.25F, 0.325F), tier(70, 0.3F, 0.4F)), List.of()))
                .step(k("duration", UP, 1.0F), kBase("intensity", DOWN, 0.98F, -0.2F), k("drain", UP, -0.5F))
                .spell(fixed("mana", COST, 1.1F))
                .uses(2, List.of(70, 30)).cap(2).cost(5, 9).spellWide()
                .description("<gray>Increases buff duration by <yellow>%.0fpvalue% <gray>on every ability in the spell it fits")
                .register(table);
    }

    private static void chainingGlyph(SpellcraftConfig table) {
        glyph(GlyphType.CHAINING).glyphClass("unique").category("unique").weight(0)
                .requires(List.of("instant", "damage", "direct"))
                .forbids("linger")
                .potency(potency("procs chaining", "none", List.of(tier(0, 0.0F, 0.0F)), List.of()))
                .uses(1, List.of(100)).cap(1).cost(10, 10).noEscalation()
                .description("<gray>Direct hits now proc <yellow>Chaining <gray>to nearby mobs")
                .register(table);
    }

    private static void combos(SpellcraftConfig table) {
        table.combos = new ArrayList<>();
        combo(table, "shotgun", "echo", 10, "every copy echoes");
        combo(table, "shotgun", "homing", 5, "every copy finds a target");
        combo(table, "shotgun", "orbit", 5, "a ring of orbiting projectiles");
        combo(table, "shotgun", "bundled_spell", 10, "a Shotgun on the bundle multiplies every payload");
        combo(table, "ethereal", "homing", 5, "seeks through walls");
        combo(table, "ethereal", "bundled_spell", 7, "payloads land behind walls");
        combo(table, "echo", "bundled_spell", 7, "an Echo inside a payload repeats at every landing");
    }

    private static SpellcraftConfig.FocusRolls focusRolls() {
        SpellcraftConfig.FocusRolls focus = new SpellcraftConfig.FocusRolls();
        focus.budgetBase = 14.4F;
        focus.budgetPerLevel = 0.72F;
        focus.budgetRollMin = 0.9F;
        focus.budgetRollMax = 1.1F;
        focus.rarities = new LinkedHashMap<>();
        rarity("SCRAPPY", 0.6F).lines(2, 3).register(focus);
        rarity("COMMON", 0.8F).lines(3, 4).register(focus);
        rarity("RARE", 1.0F).lines(4, 5).register(focus);
        rarity("EPIC", 1.15F).lines(5, 6).register(focus);
        rarity("UNIQUE", 1.15F).lines(5, 6).register(focus);
        rarity("SPECIAL", 1.15F).lines(5, 6).register(focus);
        rarity("OMEGA", 1.3F).lines(6, 7).register(focus);
        rarity("MYTHIC", 1.5F).focusBudgetMult(1.3F).lines(8, 9).register(focus);
        rarity("CHAOTIC", 1.3F).lines(4, 8).budgetRoll(0.75F, 1.25F).register(focus);
        focus.depth = List.of(depth(0, 3, 3), depth(40, 3, 4), depth(70, 4, 4));
        focus.depthBonus = new LinkedHashMap<>();
        focus.depthBonus.put("OMEGA", depth(70, 4, 5));
        focus.depthBonus.put("MYTHIC", depth(70, 6, 6));
        return focus;
    }

    private static SpellcraftConfig.WandRolls wandRolls() {
        SpellcraftConfig.WandRolls wand = new SpellcraftConfig.WandRolls();
        wand.budgetFactor = 0.6F;
        wand.linesDelta = -2;
        wand.minLines = 1;
        wand.maxDepth = 4;
        return wand;
    }

    private static Map<String, List<String>> abilityTraits() {
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
        return traits;
    }

    private static void gearGlyphs(SpellcraftConfig table) {
        gearGlyph(GlyphType.RIME, List.of("Nova_Slow"))
                .potency(potency("vulnerability level", "level", List.of(tier(0, 1.0F, 6.0F)), List.of()))
                .description("<gray>Frost Nova applies level <yellow>%.0fvalue <gray>Vulnerability")
                .register(table);
        gearGlyph(GlyphType.WARP_HASTE, List.of("Dash_Warp"))
                .potency(potency("warp cooldown", "neg_pct", List.of(tier(0, 0.1F, 0.5F)), List.of()))
                .spell(kBase("cooldown", UP, 1.0F, -1.0F))
                .description("<gray>Reduces Warp's cooldown by <#4AC6E0>%.0fpvalue%")
                .register(table);
        gearGlyph(GlyphType.WARP_VELOCITY, List.of("Dash_Warp"))
                .potency(potency("warp arrow speed", "pct", List.of(tier(0, 0.1F, 0.5F)), List.of()))
                .description("<gray>Increases Warp's arrow speed by <yellow>%.0fpvalue%")
                .register(table);
        gearGlyph(GlyphType.STEEL_CORE, List.of("Fireball_Base", "Fireball_Volley"))
                .potency(potency("centre bonus", "pct",
                        List.of(tier(0, ModVaultGearTiersProvider.STEEL_INFERNO_CENTRE_MIN, (float) AncientGearModifiers.STEEL_INFERNO_CENTRE_MAX)),
                        List.of(linked("radius", ModVaultGearTiersProvider.STEEL_INFERNO_RADIUS_MIN, (float) AncientGearModifiers.STEEL_INFERNO_RADIUS_MAX))))
                .description("<gray>Fireball's explosion is tighter, dealing up to <#E0564A>%.2fvalue<gray>x damage at its centre")
                .register(table);
        gearGlyph(GlyphType.TWINFIRE, List.of("Fireball_Base"))
                .potency(potency("second fireball chance", "pct_plain", List.of(tier(0, 0.65F, 0.86F)), List.of()))
                .description("<gray>Fireball has a <yellow>%.0fpvalue% <gray>chance to fire a second fireball")
                .register(table);
        gearGlyph(GlyphType.DEEP_CHILL, List.of("Ice_Bolt_Blast"))
                .potency(potency("hypothermia", "level", List.of(tier(0, 2.0F, 8.0F)), List.of()))
                .description("<gray>Glacial Blast applies level <yellow>%.0fvalue <#2FE1FA>Hypothermia")
                .register(table);
        gearGlyph(GlyphType.IMPLOSION, List.of("Javelin_Base", "Javelin_Piercing", "Javelin_Scatter", "Javelin_Sight"))
                .potency(potency("implode damage", "pct", List.of(tier(0, 0.5F, 0.9F)), List.of()))
                .description("<gray>Javelins implode on impact, dealing <#E0564A>%.0fpvalue% <gray>of their damage")
                .register(table);
        gearGlyph(GlyphType.ENTROPIC_BIND, List.of("Empower_Slowness_Aura"))
                .potency(potency("poison level", "level", List.of(tier(0, 1.0F, 16.0F)), List.of()))
                .description("<gray>Slowness Aura poisons mobs at level <yellow>%.0fvalue")
                .register(table);
        gearGlyph(GlyphType.WIDE_BLAST, List.of("Fireball_Base"))
                .potency(potency("fireball area", "pct", List.of(tier(0, 0.1F, 0.24F)), List.of()))
                .description("<gray>Increases Fireball's explosion area by <#5FD068>%.0fpvalue%")
                .register(table);
        gearGlyph(GlyphType.SWIFT_STORM, List.of("Storm_Arrow_Base"))
                .potency(potency("storm cooldown", "neg_pct", List.of(tier(0, 0.5F, 0.75F)), List.of(linked("area", 0.6F, 0.8F))))
                .spell(kBase("cooldown", UP, 1.0F, -1.0F))
                .description("<gray>Reduces Storm Arrow's cooldown by <#4AC6E0>%.0fpvalue% <gray>and its area by <#5FD068>{area}%")
                .register(table);
        gearGlyph(GlyphType.STORM_TRACKING, List.of("Storm_Arrow_Base"))
                .potency(potency("storm follow speed", "default",
                        List.of(tier(0, ModVaultGearTiersProvider.STORM_FOLLOW_SPEED, ModVaultGearTiersProvider.STORM_FOLLOW_SPEED)), List.of()))
                .description("<gray>Storm Arrow's storm locks onto a mob and follows it at <yellow>%.0fvalue <gray>blocks per second")
                .register(table);
        gearGlyph(GlyphType.DREAD, List.of("Taunt_Repel"))
                .potency(potency("fear area", "pct", List.of(tier(0, 0.2F, 1.33F)), List.of()))
                .description("<gray>Increases Repel's fear area by <#5FD068>%.0fpvalue%")
                .register(table);
        gearGlyph(GlyphType.QUICK_MEND, List.of("Heal_Group"))
                .potency(potency("group heal cooldown", "neg_pct", List.of(tier(0, 0.25F, 0.75F)), List.of()))
                .spell(kBase("cooldown", UP, 1.0F, -1.0F))
                .description("<gray>Reduces Group Heal's cooldown by <#4AC6E0>%.0fpvalue%")
                .register(table);
    }

    private static void gearAffixes(SpellcraftConfig table) {
        String special = ModGearAttributes.ABILITY_SPECIAL_MODIFICATION.getRegistryName().toString();
        String area = ModGearAttributes.ABILITY_AREA_OF_EFFECT_PERCENT.getRegistryName().toString();
        String cooldown = ModGearAttributes.ABILITY_COOLDOWN_PERCENT.getRegistryName().toString();
        table.gearGlyphs = new ArrayList<>();
        gearAffix(table, special, FrostNovaVulnerabilityModification.ID.toString(), GlyphType.RIME, 1.0F, HIGHER);
        gearAffix(table, cooldown, "Dash_Warp", GlyphType.WARP_HASTE, 1.0F, LOWER);
        gearAffix(table, ModGearAttributes.WARP_PROJECTILE_SPEED.getRegistryName().toString(), null, GlyphType.WARP_VELOCITY, 1.0F, HIGHER);
        gearAffix(table, xyz.iwolfking.woldsvaults.init.ModGearAttributes.STEEL_INFERNO.getRegistryName().toString(), null, GlyphType.STEEL_CORE,
                1.0F, HIGHER);
        gearAffix(table, special, FireballModification.ID.toString(), GlyphType.TWINFIRE, 1.0F, HIGHER);
        gearAffix(table, special, GlacialBlastModification.ID.toString(), GlyphType.DEEP_CHILL, 1.0F, HIGHER);
        gearAffix(table, special, ImplodingJavelinModification.ID.toString(), GlyphType.IMPLOSION, 1.0F, HIGHER);
        gearAffix(table, xyz.iwolfking.woldsvaults.init.ModGearAttributes.IMPLODING_JAVELIN.getRegistryName().toString(), null, GlyphType.IMPLOSION,
                0.0F, HIGHER);
        gearAffix(table, special, EntropyPoisonModification.ID.toString(), GlyphType.ENTROPIC_BIND, 1.0F, HIGHER);
        gearAffix(table, area, "Fireball_Base", GlyphType.WIDE_BLAST, 1.0F, HIGHER);
        gearAffix(table, cooldown, "Storm_Arrow_Base", GlyphType.SWIFT_STORM, 1.0F, LOWER);
        linkedAffix(table, area, "Storm_Arrow_Base", GlyphType.SWIFT_STORM, "area", LOWER);
        gearAffix(table, xyz.iwolfking.woldsvaults.init.ModGearAttributes.STORM_TRACKING.getRegistryName().toString(), "Storm_Arrow_Base",
                GlyphType.STORM_TRACKING, 1.0F, HIGHER);
        gearAffix(table, area, "Taunt_Repel", GlyphType.DREAD, 1.0F, HIGHER);
        gearAffix(table, cooldown, "Heal_Group", GlyphType.QUICK_MEND, 1.0F, LOWER);
    }

    private static void etchingGlyphs(SpellcraftConfig table) {
        table.etchingGlyphs = new ArrayList<>();
        List<String> novas = List.of("Nova_Base", "Nova_Slow", "Nova_Dot");
        List<String> iceBolts = List.of("Ice_Bolt_Base", "Ice_Bolt_Blast");
        List<String> blizzards = List.of("Shard_Blizzard", "Storm_Arrow_Blizzard");
        List<String> smites = List.of("Smite_Base", "Smite_Archon", "Smite_Thunderstorm");
        List<String> orbs = List.of("Chain_Lightning_Orbs");
        List<String> quakes = List.of("Earthquake_Base", "Earthquake_Singularity", "Earthquake_Tremor");
        List<String> novaChoices = List.of("Nova_Slow", "Nova_Base", "Nova_Dot");
        etching(GlyphType.GLACIAL_NOVA, List.of("Nova_Slow"), EtchingGearAttributes.FROST_NOVA_FREEZE)
                .potency(potency("freeze chance", "pct_plain", List.of(tier(0, 0.1F, 0.75F)), List.of()))
                .parts(part("potency", "float"))
                .register(table);
        etching(GlyphType.NOVA_RECAST, novas, EtchingGearAttributes.NOVA_RECAST)
                .potency(potency("recast after", "seconds", List.of(tier(0, 1.0F, 5.0F)), List.of()))
                .scale(TICK)
                .parts(part("potency", "int"))
                .register(table);
        etching(GlyphType.LOW_MANA_NOVA, List.of("Nova_Base"), EtchingGearAttributes.NOVA_LOW_MANA_DAMAGE)
                .potency(potency("damage below half mana", "pct", List.of(tier(0, 0.4F, 1.2F)), List.of(linked("threshold", 0.5F, 0.5F))))
                .parts(part("potency", "float"), linkedPart("threshold", "float"))
                .register(table);
        etching(GlyphType.GLACIAL_DAMAGE, List.of("Ice_Bolt_Blast"), EtchingGearAttributes.ICE_BOLT_CHUNK_DAMAGE)
                .potency(potency("attack damage on the blast", "pct_plain", List.of(tier(0, 0.1F, 1.0F)), List.of()))
                .parts(part("potency", "float"))
                .register(table);
        etching(GlyphType.SHATTERING_BOLT, iceBolts, EtchingGearAttributes.ICE_BOLT_EFFECT_ETCHING)
                .potency(potency("glacial shatter chance", "pct_plain", List.of(tier(0, 0.2F, 0.9F)), List.of(linked("duration", 2.0F, 5.0F))))
                .choices(List.of("the_vault:glacial_shatter"))
                .parts(part("choice", null), part("potency", "float"), linkedPart("duration", "int"))
                .register(table);
        etching(GlyphType.FROZEN_BARRAGE, iceBolts, EtchingGearAttributes.ICE_BOLT_MULTICAST)
                .potency(potency("extra casts", "default", List.of(tier(0, 4.0F, 10.0F)), List.of()))
                .parts(part("potency", "int"))
                .register(table);
        etching(GlyphType.PIERCING_BOLT, List.of("Ice_Bolt_Base", "Ice_Bolt_Blast", "Shard_Blizzard", "Storm_Arrow_Blizzard"),
                EtchingGearAttributes.ICE_BOLT_PIERCE)
                .potency(potency("pierce", "default", List.of(tier(0, 4.0F, 15.0F)), List.of()))
                .parts(part("potency", "int"))
                .register(table);
        etching(GlyphType.BLIZZARD_DAMAGE, blizzards, EtchingGearAttributes.STORM_ARROW_BLIZZARD_DAMAGE)
                .potency(potency("ability power per hit", "pct_plain", List.of(tier(0, 0.1F, 0.6F)), List.of()))
                .parts(part("potency", "float"))
                .register(table);
        etching(GlyphType.BLIZZARD_VULNERABILITY, blizzards, EtchingGearAttributes.STORM_ARROW_BLIZZARD_EFFECT)
                .potency(potency("vulnerable stacks", "level", List.of(tier(0, 1.0F, 10.0F)), List.of(linked("duration", 20.0F, 120.0F))))
                .choices(List.of("the_vault:vulnerable"))
                .parts(part("choice", null), linkedPart("duration", "int"), part("potency", "int"))
                .register(table);
        etching(GlyphType.STUNNING_SMITE, smites, EtchingGearAttributes.SMITE_SUN)
                .potency(potency("stun chance", "pct_plain", List.of(tier(0, 0.5F, 1.0F)), List.of()))
                .parts(part("potency", "float"))
                .register(table);
        etching(GlyphType.ECHOING_SMITE, smites, EtchingGearAttributes.SMITE_ECHO)
                .potency(potency("echo bolt damage", "pct_plain", List.of(tier(0, 0.2F, 1.2F)), List.of()))
                .parts(part("potency", "float"))
                .register(table);
        etching(GlyphType.SECOND_STRIKE, List.of("Smite_Base", "Smite_Archon", "Smite_Thunderstorm", "Chain_Lightning_Base"),
                EtchingGearAttributes.LIGHTNING_STRIKE_DELAY)
                .potency(potency("strikes again after", "seconds", List.of(tier(0, 1.0F, 4.0F)), List.of()))
                .scale(TICK)
                .parts(part("potency", "float"))
                .register(table);
        etching(GlyphType.GRAND_ORB, orbs, EtchingGearAttributes.LIGHTNING_ORB_SIZE)
                .potency(potency("orb size", "pct", List.of(tier(0, 0.5F, 2.5F)), List.of()))
                .parts(part("potency", "float"))
                .register(table);
        etching(GlyphType.ORB_REFUND, orbs, EtchingGearAttributes.LIGHTNING_ORB_KILL_COOLDOWN)
                .potency(potency("cooldown refunded per kill", "pct_plain", List.of(tier(0, 0.2F, 1.0F)), List.of()))
                .parts(part("potency", "float"))
                .register(table);
        etching(GlyphType.TRISTORM_ORBS, orbs, EtchingGearAttributes.LIGHTNING_ORB_TRIPLE_DAMAGE)
                .potency(potency("damage per orb (3 orbs)", "pct_plain", List.of(tier(0, 0.4F, 0.8F)), List.of()))
                .scale(-1.0F).offset(1.0F)
                .parts(part("potency", "float"))
                .register(table);
        etching(GlyphType.ARCANE_PIERCE, List.of("Arcane_Base"), EtchingGearAttributes.ARCANE_PIERCE)
                .potency(potency("pierce", "default", List.of(tier(0, 2.0F, 5.0F)), List.of()))
                .parts(part("potency", "int"))
                .register(table);
        etching(GlyphType.RAIL_PIERCE, List.of("Arcane_Rail"), EtchingGearAttributes.RAIL_PIERCE)
                .potency(potency("pierce", "default", List.of(tier(0, 10.0F, 25.0F)), List.of()))
                .parts(part("potency", "int"))
                .register(table);
        etching(GlyphType.WIDE_RAIL, List.of("Arcane_Rail"), EtchingGearAttributes.ARCANE_RAIL_SIZE)
                .potency(potency("hit width (blocks)", "default", List.of(tier(0, RAIL_SIZE_MIN, 5.0F)), List.of()))
                .parts(part("potency", "float"))
                .register(table);
        etching(GlyphType.PRISM_ANCHOR, List.of("Arcane_Prism"), EtchingGearAttributes.PRISM_ANCHOR)
                .potency(potency("anchors to blocks on a miss", "none", List.of(tier(0, 1.0F, 1.0F)), List.of()))
                .parts(part("flag", null))
                .register(table);
        etching(GlyphType.PRISM_SPLIT, List.of("Arcane_Prism"), EtchingGearAttributes.PRISM_SPLIT)
                .potency(potency("also fires the other pattern", "none", List.of(tier(0, 1.0F, 1.0F)), List.of()))
                .parts(part("flag", null))
                .register(table);
        etching(GlyphType.SCATTER_PIERCE, List.of("Javelin_Piercing"), EtchingGearAttributes.EXTRA_PIERCING_JAVELIN)
                .potency(potency("side javelin damage", "pct_plain", List.of(tier(0, 0.3F, 1.2F)), List.of()))
                .parts(part("potency", "float"))
                .register(table);
        etching(GlyphType.PIERCING_RICOCHET, List.of("Javelin_Piercing"), EtchingGearAttributes.PIERCING_JAVELIN_RICOCHET)
                .potency(potency("ricochet damage", "pct_plain", List.of(tier(0, 0.3F, 0.8F)), List.of()))
                .scale(-1.0F).offset(1.0F)
                .parts(part("potency", "float"))
                .register(table);
        etching(GlyphType.SCATTER_MANA, List.of("Javelin_Scatter"), EtchingGearAttributes.SCATTER_MANA)
                .potency(potency("mana per hit", "default", List.of(tier(0, 1.0F, 8.0F)), List.of()))
                .parts(part("potency", "int"))
                .register(table);
        etching(GlyphType.AFTERSHOCK, quakes, EtchingGearAttributes.EARTHQUAKE_RECAST_DELAY)
                .potency(potency("quakes again after", "seconds", List.of(tier(0, 1.0F, 3.5F)), List.of()))
                .scale(TICK)
                .parts(part("potency", "int"))
                .register(table);
        etching(GlyphType.ARCANE_WRATH, List.of("Totem_Player_Damage"), EtchingGearAttributes.TOTEM_PLAYER_DAMAGE_AP)
                .potency(potency("buff also raises ability power", "none", List.of(tier(0, 1.0F, 1.0F)), List.of()))
                .lingering(SpellcraftConfig.EtchingGlyph.LINGERING_TOTEM)
                .parts(part("flag", null))
                .register(table);
        etching(GlyphType.VULNERABLE_TOTEM, List.of("Totem_Player_Damage"), EtchingGearAttributes.TOTEM_PLAYER_DAMAGE_EFFECT)
                .potency(potency("vulnerable stacks", "level", List.of(tier(0, 1.0F, 10.0F)), List.of(linked("duration", 20.0F, 120.0F))))
                .choices(List.of("the_vault:vulnerable")).lingering(SpellcraftConfig.EtchingGlyph.LINGERING_TOTEM)
                .parts(part("choice", null), linkedPart("duration", "float"), part("potency", "int"))
                .register(table);
        etching(GlyphType.SHOCKWAVE, List.of("Stonefall_Base"), EtchingGearAttributes.STONE_FALL_STUN)
                .potency(potency("stun chance", "pct_plain", List.of(tier(0, 0.2F, 1.0F)), List.of(linked("duration", 40.0F, 120.0F))))
                .choices(List.of("the_vault:noai"))
                .parts(part("potency", "float"), linkedPart("duration", "int"), part("choice", null))
                .register(table);
        etching(GlyphType.BASH_FORCE, List.of("Shield_Bash"), EtchingGearAttributes.SHIELD_BASH_DAMAGE)
                .potency(potency("attack damage", "pct_plain", List.of(tier(0, 0.1F, 0.5F)), List.of()))
                .parts(part("potency", "float"))
                .register(table);
        etching(GlyphType.HEAL_CLEANSE, List.of("Heal_Base"), EtchingGearAttributes.HEAL_CLEANSE)
                .potency(potency("cleanse level", "level", List.of(tier(0, 1.0F, 10.0F)), List.of()))
                .parts(part("potency", "int"))
                .register(table);
        etching(GlyphType.RAMPAGING_LUCK, List.of("Rampage_Base"), EtchingGearAttributes.RAMPAGE_LUCKY_HIT)
                .potency(potency("lucky hit", "pct", List.of(tier(0, 0.02F, 0.12F)), List.of()))
                .lingering(SpellcraftConfig.EtchingGlyph.LINGERING_CAST)
                .parts(part("potency", "float"))
                .register(table);
        etching(GlyphType.TAP_DAMAGE, List.of("Implode_Life_Tap"), EtchingGearAttributes.LIFE_TAP_EXTRA_DAMAGE)
                .potency(potency("damage per health drained", "default", List.of(tier(0, 1.0F, 8.0F)), List.of()))
                .parts(part("potency", "int"))
                .register(table);
        etching(GlyphType.LUCKY_TAP, List.of("Implode_Life_Tap"), EtchingGearAttributes.LIFE_TAP_LUCKY_HIT)
                .potency(potency("% lucky hit per kill", "default", List.of(tier(0, 0.5F, 2.5F)), List.of(linked("duration", 160.0F, 220.0F))))
                .scale(100.0F)
                .parts(part("potency", "float"), linkedPart("duration", "int"))
                .register(table);
        etching(GlyphType.IMPLODING_REVERSAL, List.of("Mana_Shield_Implode"), EtchingGearAttributes.IMPLODE_MANA_REGEN)
                .potency(potency("mana regen per kill", "pct", List.of(tier(0, 0.01F, 0.06F)), List.of()))
                .parts(part("potency", "float"))
                .register(table);
        etching(GlyphType.NOVA_SHIELD, List.of("Mana_Shield_Base"), EtchingGearAttributes.MANA_SHIELD_NOVA)
                .potency(potency("nova level", "level", List.of(tier(0, 2.0F, 20.0F)), List.of()))
                .choices(novaChoices).lingering(SpellcraftConfig.EtchingGlyph.LINGERING_CAST)
                .parts(part("choice", null), part("potency", "int"))
                .register(table);
        etching(GlyphType.NOVA_DASH, List.of("Dash_Warp"), EtchingGearAttributes.DASH_WARP_NOVA)
                .potency(potency("nova level", "level", List.of(tier(0, 2.0F, 20.0F)), List.of()))
                .choices(novaChoices)
                .parts(part("choice", null), part("potency", "int"))
                .register(table);
        etching(GlyphType.SPIRIT_NOVA, List.of("Ghost_Walk_Spirit"), EtchingGearAttributes.SPIRIT_WALK_ABILITY)
                .potency(potency("frost nova level", "level", List.of(tier(0, 1.0F, 10.0F)), List.of()))
                .choices(List.of("Nova_Slow")).lingering(SpellcraftConfig.EtchingGlyph.LINGERING_CAST)
                .parts(part("choice", null), part("potency", "int"))
                .register(table);
        etching(GlyphType.CONCENTRATED_DRAIN, List.of("Concentrate_Base"), ModEtchingGearAttributes.CONCENTRATE_DRAIN)
                .potency(potency("current health drained per effect", "pct_plain", List.of(tier(0, 0.02F, 0.1F)), List.of()))
                .parts(part("potency", "float"))
                .register(table);
        etching(GlyphType.GREEDBALL, List.of("Fireball_Base"), ModEtchingGearAttributes.FIREBALL_GREEDBALL)
                .potency(potency("of quantity, rarity and copiously as damage, minimum radius", "pct_plain", List.of(tier(0, 0.1F, 0.4F)), List.of()))
                .parts(part("potency", "float"))
                .register(table);
        etching(GlyphType.VOLLEY_MITOSIS, List.of("Fireball_Volley"), ModEtchingGearAttributes.FIREBALL_VOLLEY_MITOSIS)
                .potency(potency("fireballs on first bounce", "default", List.of(tier(0, 1.0F, 3.0F)), List.of()))
                .parts(part("potency", "int"))
                .register(table);
        etching(GlyphType.COLOSSUS_TITAN, List.of("Colossus_Base"), ModEtchingGearAttributes.COLOSSUS_TITAN_RESISTANCE)
                .potency(potency("resistance cap", "pct", List.of(tier(0, 0.04F, 0.08F)), List.of()))
                .lingering(SpellcraftConfig.EtchingGlyph.LINGERING_CAST)
                .parts(part("potency", "float"))
                .register(table);
        etching(GlyphType.NINJA_GETAWAY, List.of("Sneaky_Getaway"), ModEtchingGearAttributes.SNEAKY_GETAWAY_NINJA)
                .potency(potency("dodge", "pct", List.of(tier(0, 0.25F, 0.4F)), List.of()))
                .lingering(SpellcraftConfig.EtchingGlyph.LINGERING_CAST)
                .parts(part("potency", "float"))
                .register(table);
        etching(GlyphType.CHEMICAL_BARRAGE, List.of("Expunge_Base"), ModEtchingGearAttributes.DIFFUSE_CHEMICAL_BOMB)
                .potency(potency("chaos grenades", "default", List.of(tier(0, 3.0F, 9.0F)), List.of()))
                .parts(part("potency", "int"))
                .register(table);
        etching(GlyphType.IMPLODING_BARRIER, List.of("UltimateShield_Base"), ModEtchingGearAttributes.IMPLODING_BARRIER)
                .potency(potency("chance to implode when hit", "pct_plain", List.of(tier(0, 0.03F, 0.15F)), List.of()))
                .lingering(SpellcraftConfig.EtchingGlyph.LINGERING_CAST)
                .parts(part("potency", "float"))
                .register(table);
        etching(GlyphType.RAVENOUS_FANGS, List.of("Fangs_Base"), ModEtchingGearAttributes.RAVENOUS_FANGS)
                .potency(potency("75% less damage, double execute threshold", "none", List.of(tier(0, 1.0F, 1.0F)), List.of()))
                .parts(part("flag", null))
                .register(table);

        table.removedEtchings = new ArrayList<>(List.of(EtchingGearAttributes.ICE_BOLT_LUCKY.getRegistryName().toString()));
    }

    private static void uniqueSets(SpellcraftConfig table) {
        table.uniqueSets = new LinkedHashMap<>();
        uniqueSet(table, "the_vault:inferno_reach", List.of(always(GlyphType.STEEL_CORE)),
                List.of(GlyphType.STRENGTHEN, GlyphType.SHOTGUN, GlyphType.INTENSIFY, GlyphType.SIZE_DOWN, GlyphType.ACCELERATE), 3, 5);
        uniqueSet(table, "the_vault:frozen_orb", List.of(always(GlyphType.RIME)),
                List.of(GlyphType.DELAY, GlyphType.ECHO, GlyphType.BUNDLED_SPELL, GlyphType.SIZE_UP, GlyphType.AMPLIFY_AREA), 3, 5);
        uniqueSet(table, "the_vault:ender_rings", List.of(always(GlyphType.WARP_HASTE), always(GlyphType.WARP_VELOCITY)),
                List.of(GlyphType.ECHO, GlyphType.QUICKEN, GlyphType.INTENSIFY, GlyphType.EXTEND), 2, 4);
        uniqueSet(table, "the_vault:echoflare", List.of(),
                List.of(GlyphType.SHOTGUN, GlyphType.BUNDLED_SPELL, GlyphType.ECHO, GlyphType.HOMING, GlyphType.ORBIT, GlyphType.ACCELERATE,
                        GlyphType.LOW_GRAVITY), 5, 7);
        uniqueSet(table, "woldsvaults:chroma_brew", List.of(),
                List.of(GlyphType.SIZE_UP, GlyphType.AMPLIFY_AREA, GlyphType.SIZE_DOWN, GlyphType.INTENSIFY, GlyphType.CHARGE,
                        GlyphType.BUNDLED_SPELL), 4, 6);
        uniqueSet(table, "the_vault:chainlash", List.of(always(GlyphType.CHAINING)),
                List.of(GlyphType.STRENGTHEN, GlyphType.ECHO, GlyphType.HOMING, GlyphType.ACCELERATE, GlyphType.BUNDLED_SPELL), 3, 5);
        uniqueSet(table, "the_vault:baguette", List.of(always(GlyphType.INTENSIFY, 4, 4)),
                List.of(GlyphType.EXTEND, GlyphType.ORBIT, GlyphType.CHARGE, GlyphType.BUNDLED_SPELL), 2, 4);
    }

    private static GlyphBuilder glyph(GlyphType type) {
        return new GlyphBuilder(type).kind("modifier");
    }

    private static GlyphBuilder gearGlyph(GlyphType type, List<String> specs) {
        return glyph(type).glyphClass("unique").category("unique").weight(0).requires(List.of()).specs(specs)
                .uses(1, List.of(100)).cap(1).cost(10, 10).noEscalation();
    }

    private static EtchingGlyphBuilder etching(GlyphType type, List<String> specs, VaultGearAttribute<?> attribute) {
        return new EtchingGlyphBuilder(gearGlyph(type, specs), attribute);
    }

    private static FocusRarityBuilder rarity(String rarity, float budget) {
        return new FocusRarityBuilder(rarity, budget);
    }

    private static void gearAffix(SpellcraftConfig table, String attribute, String key, GlyphType glyph, float scale, String orientation) {
        SpellcraftConfig.GearGlyph entry = new SpellcraftConfig.GearGlyph();
        entry.attribute = attribute;
        entry.key = key;
        entry.glyph = glyph.id();
        entry.scale = scale;
        entry.orientation = orientation;
        table.gearGlyphs.add(entry);
    }

    private static void linkedAffix(SpellcraftConfig table, String attribute, String key, GlyphType glyph, String linked, String orientation) {
        gearAffix(table, attribute, key, glyph, 1.0F, orientation);
        table.gearGlyphs.get(table.gearGlyphs.size() - 1).linked = linked;
    }

    private static SpellcraftConfig.SetGlyph always(GlyphType type) {
        SpellcraftConfig.SetGlyph glyph = new SpellcraftConfig.SetGlyph();
        glyph.glyph = type.id();
        return glyph;
    }

    private static SpellcraftConfig.SetGlyph always(GlyphType type, int uses, int cap) {
        SpellcraftConfig.SetGlyph glyph = always(type);
        glyph.uses = uses;
        glyph.cap = cap;
        return glyph;
    }

    private static void uniqueSet(SpellcraftConfig table, String uniqueId, List<SpellcraftConfig.SetGlyph> always, List<GlyphType> draw,
                                  int drawMin, int drawMax) {
        if (drawMin < 0 || drawMax < drawMin || drawMax > draw.size()) {
            throw new IllegalStateException("Unique set " + uniqueId + " draws " + drawMin + "-" + drawMax + " from " + draw.size() + " glyphs");
        }
        SpellcraftConfig.UniqueSet set = new SpellcraftConfig.UniqueSet();
        set.rarityRow = REGULAR_UNIQUE_ROW;
        set.ancientRarityRow = ANCIENT_UNIQUE_ROW;
        set.always = always;
        set.draw = draw.stream().map(GlyphType::id).toList();
        set.drawMin = drawMin;
        set.drawMax = drawMax;
        table.uniqueSets.put(uniqueId, set);
    }

    private static SpellcraftConfig.Potency potency(String label, String format, List<SpellcraftConfig.PotencyTier> tiers,
                                                       List<SpellcraftConfig.Linked> linked) {
        SpellcraftConfig.Potency potency = new SpellcraftConfig.Potency();
        potency.label = label;
        potency.format = format;
        potency.tiers = tiers;
        potency.linked = linked;
        return potency;
    }

    private static SpellcraftConfig.PotencyTier tier(int minLevel, float min, float max) {
        SpellcraftConfig.PotencyTier tier = new SpellcraftConfig.PotencyTier();
        tier.minLevel = minLevel;
        tier.min = min;
        tier.max = max;
        return tier;
    }

    private static SpellcraftConfig.Linked linked(String name, float min, float max) {
        SpellcraftConfig.Linked linked = new SpellcraftConfig.Linked();
        linked.name = name;
        linked.min = min;
        linked.max = max;
        return linked;
    }

    private static SpellcraftConfig.Effect k(String stat, String role, float k) {
        SpellcraftConfig.Effect effect = effect(stat, role);
        effect.k = k;
        return effect;
    }

    private static SpellcraftConfig.Effect kBase(String stat, String role, float base, float k) {
        SpellcraftConfig.Effect effect = effect(stat, role);
        effect.base = base;
        effect.k = k;
        return effect;
    }

    private static SpellcraftConfig.Effect fixed(String stat, String role, float fixed) {
        SpellcraftConfig.Effect effect = effect(stat, role);
        effect.fixed = fixed;
        return effect;
    }

    private static SpellcraftConfig.Effect perUse(String stat, String role, float perUse) {
        SpellcraftConfig.Effect effect = effect(stat, role);
        effect.perUse = perUse;
        return effect;
    }

    private static SpellcraftConfig.Effect effect(String stat, String role) {
        SpellcraftConfig.Effect effect = new SpellcraftConfig.Effect();
        effect.stat = stat;
        effect.role = role;
        return effect;
    }

    /** Etching glyphs show their etching's text, so every other glyph needs a description template. */
    private static void checkDescriptions(SpellcraftConfig table) {
        Set<String> etchings = new HashSet<>();
        table.etchingGlyphs.forEach(entry -> etchings.add(entry.glyph));
        for (GlyphType type : GlyphType.values()) {
            SpellcraftConfig.GlyphDefinition glyph = table.glyph(type);
            if (glyph != null && !etchings.contains(type.id()) && (glyph.description == null || glyph.description.isBlank())) {
                throw new IllegalStateException("Glyph " + type.id() + " has no description template");
            }
        }
    }

    /**
     * Refuses an effect whose direction contradicts its role: an upside must move its stat in the player's favour, a
     * downside or cost against it.
     */
    private static void checkRoles(SpellcraftConfig table) {
        for (Map.Entry<String, SpellcraftConfig.GlyphDefinition> entry : table.glyphs.entrySet()) {
            List<SpellcraftConfig.Effect> effects = new ArrayList<>(entry.getValue().step);
            effects.addAll(entry.getValue().spell);
            for (SpellcraftConfig.Effect effect : effects) {
                float direction = effect.k != null ? effect.k : effect.factor(0.0F) - 1.0F;
                boolean favourable = GlyphStacking.lowerIsBetter(effect.stat) ? direction < 0.0F : direction > 0.0F;
                if (direction == 0.0F || favourable != UP.equals(effect.role)) {
                    throw new IllegalStateException("Glyph " + entry.getKey() + ": the " + effect.role + " on " + effect.stat
                            + " moves the stat the wrong way");
                }
            }
        }
    }

    private static void combo(SpellcraftConfig table, String a, String b, int cost, String reason) {
        SpellcraftConfig.Combo combo = new SpellcraftConfig.Combo();
        combo.a = a;
        combo.b = b;
        combo.cost = cost;
        combo.reason = reason;
        table.combos.add(combo);
    }

    private static SpellcraftConfig.CostNoise noise(int offset, int weight) {
        SpellcraftConfig.CostNoise noise = new SpellcraftConfig.CostNoise();
        noise.offset = offset;
        noise.weight = weight;
        return noise;
    }

    private static SpellcraftConfig.DepthTier depth(int minLevel, int min, int max) {
        SpellcraftConfig.DepthTier tier = new SpellcraftConfig.DepthTier();
        tier.minLevel = minLevel;
        tier.min = min;
        tier.max = max;
        return tier;
    }

    private static SpellcraftConfig.EtchingPart part(String role, String type) {
        SpellcraftConfig.EtchingPart part = new SpellcraftConfig.EtchingPart();
        part.role = role;
        part.type = type;
        return part;
    }

    private static SpellcraftConfig.EtchingPart linkedPart(String linked, String type) {
        SpellcraftConfig.EtchingPart part = part("linked", type);
        part.linked = linked;
        return part;
    }
}
