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
 * Focus augments: per-glyph strengths and caps, the socket rolls on wands and focuses, recipe limits, tuning, and the
 * ability trait list that decides which augments fit which ability. Strengths and tuning start from Vault Grimoire's
 * defaults; the roll weights, socket tiers and recipe limits are first-pass values awaiting balance.
 */
public class ModFocusAugmentsProvider extends AbstractWoldsTableProvider {

    public ModFocusAugmentsProvider(DataGenerator generator) {
        super(generator, "focus_augments");
    }

    @Override
    public void registerConfigs() {
        FocusAugmentsConfig table = new FocusAugmentsConfig();
        table.worksOutsideVaults = true;

        FocusAugmentsConfig.Limits limits = new FocusAugmentsConfig.Limits();
        limits.maxRecipeLength = 9;
        limits.maxCastsPerTrigger = 32;
        limits.maxRecipesPerItem = 3;
        limits.maxDelaysPerRecipe = 5;
        limits.maxEchoesPerRecipe = 1;
        limits.maxBundleDepth = 2;
        table.limits = limits;

        FocusAugmentsConfig.Tuning tuning = new FocusAugmentsConfig.Tuning();
        tuning.echoDelaySeconds = 0.5F;
        tuning.echoKeepsCharge = false;
        tuning.shotgunSpreadDegrees = 15.0F;
        tuning.shotgunCopyManaShare = 0.0F;
        tuning.homingRange = 16.0F;
        tuning.homingTurnRate = 0.2F;
        tuning.homingRetargetTicks = 5;
        tuning.orbitRadius = 2.5F;
        tuning.orbitMaxSeconds = 30;
        tuning.etherealMaxSeconds = 10;
        tuning.bundleMaxSeconds = 30;
        tuning.bundleBaseSpeed = 1.0F;
        tuning.chargeSecondsPerGlyph = 1.0F;
        tuning.railRange = 128.0F;
        tuning.sizeUpMaxScale = 3.0F;
        tuning.sizeDownMinScale = 0.25F;
        table.tuning = tuning;

        table.augments = new LinkedHashMap<>();
        augment(table, AugmentType.AMPLIFY_AREA, 0.25F, 4, 10, 0, 2);
        augment(table, AugmentType.QUICKEN, 0.10F, 5, 10, 0, 2);
        augment(table, AugmentType.DISCOUNT, 0.10F, 5, 10, 0, 2);
        augment(table, AugmentType.ECHO, 0.0F, 1, 2, 60, 1);
        augment(table, AugmentType.STRENGTHEN, 0.20F, 4, 6, 30, 2);
        augment(table, AugmentType.ACCELERATE, 0.25F, 4, 10, 0, 2);
        augment(table, AugmentType.HOMING, 0.0F, 1, 6, 20, 1);
        augment(table, AugmentType.ORBIT, 0.0F, 1, 4, 20, 1);
        augment(table, AugmentType.DELAY, 1.0F, 5, 6, 0, 2);
        augment(table, AugmentType.SIZE_UP, 0.5F, 3, 8, 0, 2);
        augment(table, AugmentType.SIZE_DOWN, 0.25F, 4, 6, 0, 2);
        augment(table, AugmentType.ETHEREAL, 0.0F, 1, 2, 60, 1);
        augment(table, AugmentType.SHOTGUN, 0.5F, 4, 3, 50, 1);
        augment(table, AugmentType.LOW_GRAVITY, 0.5F, 2, 8, 0, 1);
        augment(table, AugmentType.CHARGE, 9.0F, 1, 2, 60, 1);
        augment(table, AugmentType.BUNDLED_SPELL, 0.0F, 1, 3, 40, 1);

        table.sockets = new ArrayList<>();
        table.sockets.add(sockets(0, 1, 1));
        table.sockets.add(sockets(35, 1, 2));
        table.sockets.add(sockets(70, 2, 3));

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

    private static void augment(FocusAugmentsConfig table, AugmentType type, float strength, int maxPerCast, int rollWeight,
                                int minLevel, int maxTokensPerRoll) {
        FocusAugmentsConfig.AugmentEntry entry = new FocusAugmentsConfig.AugmentEntry();
        entry.strength = strength;
        entry.maxPerCast = maxPerCast;
        entry.rollWeight = rollWeight;
        entry.minLevel = minLevel;
        entry.maxTokensPerRoll = maxTokensPerRoll;
        table.augments.put(type.id(), entry);
    }

    private static FocusAugmentsConfig.SocketTier sockets(int minLevel, int minSockets, int maxSockets) {
        FocusAugmentsConfig.SocketTier tier = new FocusAugmentsConfig.SocketTier();
        tier.minLevel = minLevel;
        tier.minSockets = minSockets;
        tier.maxSockets = maxSockets;
        return tier;
    }
}
