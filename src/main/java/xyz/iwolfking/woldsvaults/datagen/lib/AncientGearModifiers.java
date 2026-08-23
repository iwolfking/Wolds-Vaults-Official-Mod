package xyz.iwolfking.woldsvaults.datagen.lib;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The ancient unique gear modifier tables, one per ancient-eligible unique. Plain Gson and no Minecraft
 * types, so {@link #build()} runs on its own.
 */
public final class AncientGearModifiers {
    private AncientGearModifiers() {
    }

    /** Every ancient config keyed by its file name, in the order the provider writes them. */
    public static Map<String, JsonObject> build() {
        Map<String, JsonObject> configs = new LinkedHashMap<>();
        put(configs, "unique_ancient_annmari", AncientGearModifiers::annmari);
        put(configs, "unique_ancient_aural_magnet", AncientGearModifiers::auralMagnet);
        put(configs, "unique_ancient_aurora_scissors", AncientGearModifiers::auroraScissors);
        put(configs, "unique_ancient_baguette", AncientGearModifiers::baguette);
        put(configs, "unique_ancient_bamboo_fightstick", AncientGearModifiers::bambooFightstick);
        put(configs, "unique_ancient_bloodseeking_magnet", AncientGearModifiers::bloodseekingMagnet);
        put(configs, "unique_ancient_broodmother", AncientGearModifiers::broodmother);
        put(configs, "unique_ancient_butcher_axe", AncientGearModifiers::butcherAxe);
        put(configs, "unique_ancient_castle", AncientGearModifiers::castle);
        put(configs, "unique_ancient_chainlash", AncientGearModifiers::chainlash);
        put(configs, "unique_ancient_chonknet", AncientGearModifiers::chonknet);
        put(configs, "unique_ancient_chroma_brew", AncientGearModifiers::chromaBrew);
        put(configs, "unique_ancient_crashguards", AncientGearModifiers::crashguards);
        put(configs, "unique_ancient_crystal_double_blade", AncientGearModifiers::crystalDoubleBlade);
        put(configs, "unique_ancient_crystalplate", AncientGearModifiers::crystalplate);
        put(configs, "unique_ancient_echoflare", AncientGearModifiers::echoflare);
        put(configs, "unique_ancient_ender_rings", AncientGearModifiers::enderRings);
        put(configs, "unique_ancient_eternal_stella", AncientGearModifiers::eternalStella);
        put(configs, "unique_ancient_everflame", AncientGearModifiers::everflame);
        put(configs, "unique_ancient_everfrost", AncientGearModifiers::everfrost);
        put(configs, "unique_ancient_fork_of_the_glutton", AncientGearModifiers::forkOfTheGlutton);
        put(configs, "unique_ancient_frostguards", AncientGearModifiers::frostguards);
        put(configs, "unique_ancient_frostwarden", AncientGearModifiers::frostwarden);
        put(configs, "unique_ancient_frozen_heart", AncientGearModifiers::frozenHeart);
        put(configs, "unique_ancient_frozen_orb", AncientGearModifiers::frozenOrb);
        put(configs, "unique_ancient_frozen_throne", AncientGearModifiers::frozenThrone);
        put(configs, "unique_ancient_gladiator_buckler", AncientGearModifiers::gladiatorBuckler);
        put(configs, "unique_ancient_grass_sword", AncientGearModifiers::grassSword);
        put(configs, "unique_ancient_grim", AncientGearModifiers::grim);
        put(configs, "unique_ancient_hexblade", AncientGearModifiers::hexblade);
        put(configs, "unique_ancient_honey_stick", AncientGearModifiers::honeyStick);
        put(configs, "unique_ancient_inferno_reach", AncientGearModifiers::infernoReach);
        put(configs, "unique_ancient_inflated_justice", AncientGearModifiers::inflatedJustice);
        put(configs, "unique_ancient_iskallibur", AncientGearModifiers::iskallibur);
        put(configs, "unique_ancient_ivy", AncientGearModifiers::ivy);
        put(configs, "unique_ancient_jester", AncientGearModifiers::jester);
        put(configs, "unique_ancient_kaleidoscope", AncientGearModifiers::kaleidoscope);
        put(configs, "unique_ancient_lava_chicken_sword", AncientGearModifiers::lavaChickenSword);
        put(configs, "unique_ancient_leviathan", AncientGearModifiers::leviathan);
        put(configs, "unique_ancient_manabloom", AncientGearModifiers::manabloom);
        put(configs, "unique_ancient_mineral_greatsword", AncientGearModifiers::mineralGreatsword);
        put(configs, "unique_ancient_ocean_current", AncientGearModifiers::oceanCurrent);
        put(configs, "unique_ancient_pacifist_sandals", AncientGearModifiers::pacifistSandals);
        put(configs, "unique_ancient_pax", AncientGearModifiers::pax);
        put(configs, "unique_ancient_pestilence_wall", AncientGearModifiers::pestilenceWall);
        put(configs, "unique_ancient_plague_steppers", AncientGearModifiers::plagueSteppers);
        put(configs, "unique_ancient_pocket_penguin", AncientGearModifiers::pocketPenguin);
        put(configs, "unique_ancient_quickstone", AncientGearModifiers::quickstone);
        put(configs, "unique_ancient_safer_spaces", AncientGearModifiers::saferSpaces);
        put(configs, "unique_ancient_shattering_jewel", AncientGearModifiers::shatteringJewel);
        put(configs, "unique_ancient_starforge", AncientGearModifiers::starforge);
        put(configs, "unique_ancient_stormcrown", AncientGearModifiers::stormcrown);
        put(configs, "unique_ancient_swarmwalkers", AncientGearModifiers::swarmwalkers);
        put(configs, "unique_ancient_sweetheart", AncientGearModifiers::sweetheart);
        put(configs, "unique_ancient_treasure_jewel", AncientGearModifiers::treasureJewel);
        put(configs, "unique_ancient_treasure_magnet", AncientGearModifiers::treasureMagnet);
        put(configs, "unique_ancient_trirang", AncientGearModifiers::trirang);
        put(configs, "unique_ancient_vitalis", AncientGearModifiers::vitalis);
        put(configs, "unique_ancient_warbound_helmet", AncientGearModifiers::warboundHelmet);
        put(configs, "unique_ancient_wicked_witch", AncientGearModifiers::wickedWitch);
        put(configs, "unique_ancient_young_kitsune", AncientGearModifiers::youngKitsune);
        put(configs, "unique_ancient_zombie_horse_axe", AncientGearModifiers::zombieHorseAxe);
        return configs;
    }

    private static void put(Map<String, JsonObject> configs, String key, Consumer<Config> table) {
        Config config = new Config();
        table.accept(config);
        configs.put(key, config.toJson());
    }

    private static final class Config {
        private final Map<String, JsonArray> groups = new LinkedHashMap<>();

        private void base(String attribute, String group, String identifier, List<String> tags, JsonObject... tiers) {
            this.add("BASE_ATTRIBUTES", attribute, group, identifier, tags, tiers);
        }

        private void implicit(String attribute, String group, String identifier, List<String> tags, JsonObject... tiers) {
            this.add("IMPLICIT", attribute, group, identifier, tags, tiers);
        }

        private void prefix(String attribute, String group, String identifier, List<String> tags, JsonObject... tiers) {
            this.add("PREFIX", attribute, group, identifier, tags, tiers);
        }

        private void suffix(String attribute, String group, String identifier, List<String> tags, JsonObject... tiers) {
            this.add("SUFFIX", attribute, group, identifier, tags, tiers);
        }

        private void add(String affixGroup, String attribute, String group, String identifier,
                         List<String> tags, JsonObject[] tiers) {
            JsonObject entry = new JsonObject();
            entry.addProperty("attribute", attribute);
            entry.addProperty("group", group);
            entry.addProperty("identifier", identifier);
            JsonArray tagArray = new JsonArray();
            tags.forEach(tagArray::add);
            entry.add("tags", tagArray);
            JsonArray tierArray = new JsonArray();
            for (JsonObject tier : tiers) {
                tierArray.add(tier);
            }
            entry.add("tiers", tierArray);
            this.groups.computeIfAbsent(affixGroup, key -> new JsonArray()).add(entry);
        }

        private JsonObject toJson() {
            JsonObject modifierGroup = new JsonObject();
            this.groups.forEach(modifierGroup::add);
            JsonObject root = new JsonObject();
            root.add("modifierGroup", modifierGroup);
            return root;
        }
    }

    private static List<String> tags(String... tags) {
        return List.of(tags);
    }

    private static JsonObject tier(int minLevel, int maxLevel, int weight, JsonObject value) {
        JsonObject tier = new JsonObject();
        tier.addProperty("minLevel", minLevel);
        tier.addProperty("maxLevel", maxLevel);
        tier.addProperty("weight", weight);
        tier.add("value", value);
        return tier;
    }

    /** A tier value from alternating key/value arguments; the Java type decides whole vs decimal JSON. */
    private static JsonObject obj(Object... keyValues) {
        if (keyValues.length % 2 != 0) {
            throw new IllegalArgumentException("Tier value needs an even number of arguments");
        }
        JsonObject value = new JsonObject();
        for (int index = 0; index < keyValues.length; index += 2) {
            String key = (String) keyValues[index];
            Object raw = keyValues[index + 1];
            if (raw instanceof JsonArray array) {
                value.add(key, array);
            } else if (raw instanceof JsonObject nested) {
                value.add(key, nested);
            } else if (raw instanceof Number number) {
                value.addProperty(key, number);
            } else if (raw instanceof Boolean flag) {
                value.addProperty(key, flag);
            } else {
                value.addProperty(key, (String) raw);
            }
        }
        return value;
    }

    private static JsonArray list(Object... values) {
        JsonArray array = new JsonArray();
        for (Object raw : values) {
            if (raw instanceof JsonObject nested) {
                array.add(nested);
            } else if (raw instanceof Number number) {
                array.add(new JsonPrimitive(number));
            } else if (raw instanceof Boolean flag) {
                array.add(new JsonPrimitive(flag));
            } else {
                array.add(new JsonPrimitive((String) raw));
            }
        }
        return array;
    }

    private static void annmari(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.implicit("the_vault:armor", "BaseModAbility", "the_vault:base_armor", tags(),
                tier(0, -1, 100, obj("min", 30, "max", 40, "step", 1)));
        config.implicit("the_vault:critical_hit_mitigation", "uCritHitImplicit", "the_vault:u_base_crit_hit", tags(),
                tier(0, -1, 100, obj("min", 0.3D, "max", 0.4D, "step", 0.01D)));
        config.implicit("the_vault:ability_power", "BaseBonusPool", "the_vault:base_ap", tags(),
                tier(0, -1, 100, obj("min", 50.0D, "max", 80.0D, "step", 1.0D)));
        config.prefix("the_vault:armor", "ModArmor", "the_vault:u_mod_armor", tags("focusArmor"),
                tier(0, -1, 600, obj("min", 10, "max", 14, "step", 1)));
        config.prefix("the_vault:health", "ModHealthMana", "the_vault:mod_health", tags("focusHealth"),
                tier(0, -1, 500, obj("min", 12, "max", 16, "step", 1)));
        config.prefix("the_vault:added_ability_level", "ModAPLvl", "the_vault:mod_added_ability_level_groupheal", tags("focusAbilityPower", "focusAbilityPowerFireball"),
                tier(0, -1, 60, obj("abilityKey", "Heal_Group", "levelChange", 3)));
        config.suffix("the_vault:ability_cooldown_percent", "ModEnhancement", "the_vault:heal_cooldown", tags(),
                tier(0, -1, 5, obj("min", -0.8D, "max", -0.6D, "step", 0.01D, "abilityKey", "Heal_Group")));
        config.suffix("the_vault:phoenix", "ModPhoenix", "the_vault:u_phoenix", tags(),
                tier(0, -1, 100, obj("min", 1, "max", 1, "step", 1)));
    }

    private static void auralMagnet(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_magnet_durability", tags("resilientFocusTarget"),
                tier(0, -1, 10, obj("min", 60000, "max", 75000, "step", 1)));
        config.implicit("the_vault:range", "BaseRange", "the_vault:u_base_magnet_range", tags(),
                tier(0, -1, 100, obj("min", 25.0D, "max", 33.25D, "step", 0.1D)));
        config.implicit("the_vault:endergized", "BaseEndergized", "the_vault:base_endergized", tags(),
                tier(0, -1, 10, obj("flag", true)));
        config.prefix("the_vault:added_talent_level", "ModPrimeAmpLevel", "the_vault:mod_prime_amp_level", tags(),
                tier(0, -1, 10, obj("talentKey", "Prime_Amplification", "levelChange", 2)));
        config.prefix("the_vault:added_ability_level", "ModEmpowerLevel", "the_vault:mod_empower_level", tags(),
                tier(0, -1, 10, obj("abilityKey", "Empower", "levelChange", 2)));
        config.prefix("the_vault:added_ability_level", "ModNovaLevel", "the_vault:mod_nova_level", tags(),
                tier(0, -1, 10, obj("abilityKey", "Nova", "levelChange", 2)));
        config.prefix("the_vault:area_of_effect", "ModEffectRadius", "the_vault:unique_area_of_effect", tags("focusEffectRadius"),
                tier(0, -1, 100, obj("min", 0.2D, "max", 0.27D, "step", 0.01D)));
        config.prefix("the_vault:ability_power_percent", "ModAP", "the_vault:mod_ability_increase", tags("focusAbilityDamage", "antiqueAnyAbilityPower"),
                tier(0, -1, 200, obj("min", 0.2D, "max", 0.27D, "step", 0.01D)));
        config.suffix("the_vault:mana_regen", "ModManaRegen", "the_vault:mod_mana_regen", tags("focusManaRegen"),
                tier(0, -1, 100, obj("min", 1.0D, "max", 1.33D, "step", 0.01D)));
    }

    private static void auroraScissors(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.implicit("the_vault:attack_damage", "BaseAttackDamage", "the_vault:base_attack_damage", tags(),
                tier(0, -1, 10, obj("min", 100.0D, "max", 133.0D, "step", 1.0D)));
        config.implicit("the_vault:attack_speed", "BaseAttackSpeed", "the_vault:base_attack_speed", tags(),
                tier(0, -1, 10, obj("min", -2.29D, "max", -2.2D, "step", 0.01D)));
        config.prefix("the_vault:ability_power", "BaseBonusPool", "the_vault:mod_ap", tags(),
                tier(0, -1, 100, obj("min", 50.0D, "max", 66.0D, "step", 1.0D)));
        config.prefix("the_vault:soul_chance", "ModSoulChance", "the_vault:mod_soul_chance_scissors", tags("focusSoulChance"),
                tier(0, -1, 10, obj("min", 1.0D, "max", 1.33D, "step", 0.01D)));
        config.suffix("the_vault:ap_scaling_damage", "BaseScaling", "the_vault:ap_scaling_damage_scissors", tags(),
                tier(0, -1, 10, obj("min", 0.35D, "max", 0.47D, "step", 0.01D)));
        config.suffix("the_vault:attack_speed_percent", "uAttackSpeed", "the_vault:mod_attack_speed_percent", tags("focusAttackSpeedPercent", "noLegendary"),
                tier(0, -1, 10, obj("min", 0.25D, "max", 0.33D, "step", 0.01D)));
    }

    private static void baguette(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.implicit("the_vault:effect", "uSaturation", "the_vault:u_saturation", tags(),
                tier(0, -1, 100, obj("effectKey", "minecraft:saturation", "amplifier", 1)));
        config.implicit("the_vault:effect", "uRegeneration", "the_vault:u_regeneration", tags(),
                tier(0, -1, 100, obj("effectKey", "minecraft:regeneration", "amplifier", 6)));
        config.suffix("the_vault:healing_effectiveness", "ModHealth", "the_vault:u_baguette_healingeff", tags("focusHealingEff", "noLegendary"),
                tier(0, -1, 10, obj("min", 0.5D, "max", 0.67D, "step", 0.01D)));
    }

    private static void bambooFightstick(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.implicit("the_vault:attack_damage", "BaseAttackDamage", "the_vault:base_attack_damage_battlestaff_stick", tags(),
                tier(0, -1, 10, obj("min", 15.0D, "max", 19.0D, "step", 1.0D)));
        config.implicit("the_vault:lucky_hit_chance", "ModStaffBonusOffensive", "the_vault:base_lucky_hit_battlestaff_stick", tags(),
                tier(0, -1, 10, obj("min", 0.55D, "max", 0.73D, "step", 0.01D)));
        config.implicit("the_vault:attack_speed", "BaseAttackSpeed", "the_vault:base_attack_speed_battlestaff_stick", tags(),
                tier(0, -1, 10, obj("min", -1.9D, "max", -1.65D, "step", 0.01D)));
        config.implicit("the_vault:attack_range", "ModAttackType", "the_vault:base_attack_range_battlestaff_stick", tags("focusAttackRange"),
                tier(0, -1, 10, obj("min", 0.5D, "max", 0.67D, "step", 0.01D)));
        config.prefix("the_vault:lucky_hit_chance_percentile", "ModLuckyHitChance", "the_vault:u_bamboo_lucky_hit_percentile", tags("focusLuckyHitChance"),
                tier(0, -1, 10, obj("min", 0.1D, "max", 0.4D, "step", 0.01D)));
    }

    private static void bloodseekingMagnet(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_magnet_durability", tags("resilientFocusTarget"),
                tier(0, -1, 10, obj("min", 60000, "max", 75000, "step", 1)));
        config.implicit("the_vault:range", "BaseRange", "the_vault:u_base_magnet_range", tags(),
                tier(0, -1, 100, obj("min", 25.0D, "max", 33.25D, "step", 0.1D)));
        config.implicit("the_vault:endergized", "BaseEndergized", "the_vault:base_endergized", tags(),
                tier(0, -1, 10, obj("flag", true)));
        config.prefix("the_vault:damage_increase", "ModResistance", "the_vault:u_bloody_magnet_increase", tags("focusDamage"),
                tier(0, -1, 600, obj("min", 0.55D, "max", 0.73D, "step", 0.01D)));
        config.prefix("the_vault:leech", "ModLeech", "the_vault:u_bloody_magnet_leech", tags("focusDamage"),
                tier(0, -1, 600, obj("min", 0.04D, "max", 0.06D, "step", 0.01D)));
        config.prefix("the_vault:effect", "ModEffect", "the_vault:u_cursed_unlucky", tags(),
                tier(0, -1, 10, obj("effectKey", "minecraft:unluck", "amplifier", 1)));
        config.suffix("the_vault:trap_disarming", "ModTrapDisarm", "the_vault:u_bloody_magnet_trap_disarm", tags(),
                tier(0, -1, 100, obj("min", -0.9D, "max", -0.75D, "step", 0.05D)));
        config.suffix("the_vault:mana_regen", "ModManaRegen", "the_vault:u_bloody_magnet_mana_regen", tags("focusHealingEff"),
                tier(0, -1, 100, obj("min", -0.9D, "max", -0.75D, "step", 0.05D)));
    }

    private static void broodmother(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.implicit("the_vault:block", "BaseBlock", "the_vault:u_base_block", tags(),
                tier(0, -1, 10, obj("min", 0.4D, "max", 0.53D, "step", 0.01D)));
        config.prefix("the_vault:damage_increase", "ModResistance", "the_vault:mod_damage_increase", tags("focusDamage"),
                tier(0, -1, 600, obj("min", 0.35D, "max", 0.47D, "step", 0.01D)));
        config.prefix("the_vault:resistance", "ModResistance", "the_vault:mod_resistance", tags("focusResistance"),
                tier(0, -1, 600, obj("min", 0.16D, "max", 0.21D, "step", 0.01D)));
        config.prefix("the_vault:broodmother_web", "ModEnhancement", "the_vault:u_broodmother_web", tags("noLegendary", "noImbuement"),
                tier(0, -1, 100, obj("chance", obj("min", 0.6D, "max", 0.8D, "step", 0.05D), "percentAttackDamage", obj("min", 1.2D, "max", 1.6D, "step", 0.05D))));
    }

    private static void butcherAxe(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.implicit("the_vault:attack_damage", "BaseAttackDamage", "the_vault:base_attack_damage", tags(),
                tier(0, -1, 10, obj("min", 100.0D, "max", 133.0D, "step", 1.0D)));
        config.implicit("the_vault:attack_speed", "BaseAttackSpeed", "the_vault:base_attack_speed", tags(),
                tier(0, -1, 10, obj("min", -2.29D, "max", -2.2D, "step", 0.01D)));
        config.prefix("the_vault:double_hit_chance", "BaseDoubleHitChance", "the_vault:base_double_hit_chance", tags(),
                tier(0, -1, 10, obj("min", 0.81D, "max", 1.08D, "step", 0.05D)));
        config.prefix("the_vault:attack_damage", "ModOnHitType", "the_vault:mod_attack_damage", tags("focusAttackDamage"),
                tier(0, -1, 10, obj("min", 55.0D, "max", 73.0D, "step", 1.0D)));
        config.suffix("the_vault:on_kill_heal", "ModEnhancement", "the_vault:u_on_kill_heal", tags("noImbuement"),
                tier(0, -1, 100, obj("chance", obj("min", 0.2D, "max", 0.25D, "step", 0.01D), "amount", obj("min", 2.0D, "max", 2.5D, "step", 0.0D))));
    }

    private static void castle(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.implicit("the_vault:armor", "BaseModAbility", "the_vault:base_armor", tags(),
                tier(0, -1, 100, obj("min", 30, "max", 40, "step", 1)));
        config.implicit("the_vault:critical_hit_mitigation", "uCritHitImplicit", "the_vault:u_critical_hit_mitigation", tags(),
                tier(0, -1, 100, obj("min", 0.5D, "max", 0.67D, "step", 0.01D)));
        config.prefix("the_vault:armor", "ModArmor", "the_vault:u_mod_armor", tags("focusArmor"),
                tier(0, -1, 600, obj("min", 10, "max", 13, "step", 1)));
        config.prefix("the_vault:resistance", "ModResistance", "the_vault:mod_resistance", tags("focusResistance"),
                tier(0, -1, 600, obj("min", 0.16D, "max", 0.21D, "step", 0.01D)));
        config.suffix("the_vault:knockback_resistance", "UniqueCastleKnockbackResistance", "the_vault:u_castle_knockback_resistance", tags("focusKnockbackRes", "noLegendary", "noImbuement"),
                tier(0, -1, 100, obj("min", 1.0D, "max", 1.0D, "step", 0.01D)));
        config.suffix("the_vault:castle_bastion", "UniqueCastleBastion", "the_vault:u_castle_bastion", tags("noLegendary", "noImbuement"),
                tier(0, -1, 100, obj("min", 0.1D, "max", 0.13D, "step", 0.01D)));
    }

    private static void chainlash(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:offhand_base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 10, obj("min", 21002, "max", 26252, "step", 1)));
        config.prefix("the_vault:on_hit_chain", "ModOnHitType", "the_vault:u_chaining", tags("focusChaining"),
                tier(0, -1, 10, obj("min", 5, "max", 7, "step", 1)));
        config.prefix("the_vault:chaining_damage", "ModOnHitType", "the_vault:u_chaining_damage", tags(),
                tier(0, -1, 10, obj("min", 0.96D, "max", 1.0D, "step", 0.01D)));
        config.prefix("the_vault:area_of_effect", "ModEffectRadius", "the_vault:unique_area_of_effect", tags("focusEffectRadius"),
                tier(0, -1, 100, obj("min", 0.2D, "max", 0.27D, "step", 0.01D)));
    }

    private static void chonknet(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_magnet_durability", tags("resilientFocusTarget"),
                tier(0, -1, 10, obj("min", 60000, "max", 75000, "step", 1)));
        config.implicit("the_vault:range", "BaseRange", "the_vault:u_base_magnet_range", tags(),
                tier(0, -1, 100, obj("min", 25.0D, "max", 33.25D, "step", 0.1D)));
        config.implicit("the_vault:endergized", "BaseEndergized", "the_vault:base_endergized", tags(),
                tier(0, -1, 10, obj("flag", true)));
        config.prefix("the_vault:health_percentile", "ModHealthMana", "the_vault:u_chunky_magnet_health", tags(),
                tier(0, -1, 600, obj("min", 0.25D, "max", 0.33D, "step", 0.01D)));
        config.prefix("the_vault:healing_effectiveness", "ModHealthEff", "the_vault:u_chunky_magnet_healing", tags("focusHealingEff"),
                tier(0, -1, 100, obj("min", 0.65D, "max", 0.86D, "step", 0.01D)));
        config.suffix("the_vault:item_quantity", "ModItemQuantity", "the_vault:u_chunky_magnet_item_quantity", tags("focusItemQuantity"),
                tier(0, -1, 100, obj("min", -0.15D, "max", -0.1D, "step", 0.01D)));
        config.suffix("the_vault:item_rarity", "ModItemRarity", "the_vault:u_chunky_magnet_item_rarity", tags("focusItemRarity"),
                tier(0, -1, 100, obj("min", -0.15D, "max", -0.1D, "step", 0.01D)));
    }

    private static void chromaBrew(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.prefix("the_vault:effect_cloud", "ModEffectCloud1", "the_vault:mod_healing_cloud_brew", tags(),
                tier(0, -1, 10, obj("tooltipDisplayName", "Healing VI", "potion", "minecraft:empty", "additionalEffects", list(obj("effect", "minecraft:instant_health", "duration", 20, "amplifier", 3)), "duration", 240, "radius", 4.0D, "color", 16262179, "affectsOwner", true, "triggerChance", 0.05D)));
        config.prefix("the_vault:effect_cloud", "ModEffectCloud2", "the_vault:mod_wither_cloud_brew", tags(),
                tier(0, -1, 10, obj("tooltipDisplayName", "Wither VI", "potion", "minecraft:empty", "additionalEffects", list(obj("effect", "minecraft:wither", "duration", 200, "amplifier", 5)), "duration", 300, "radius", 4.0D, "color", 3484199, "affectsOwner", false, "triggerChance", 0.05D)));
        config.prefix("the_vault:effect_cloud", "ModEffectCloud2", "the_vault:mod_bleed_cloud_brew", tags(),
                tier(0, -1, 10, obj("tooltipDisplayName", "Bleed VI", "potion", "minecraft:empty", "additionalEffects", list(obj("effect", "the_vault:bleed", "duration", 200, "amplifier", 5)), "duration", 300, "radius", 4.0D, "color", 16711680, "affectsOwner", false, "triggerChance", 0.05D)));
        config.prefix("the_vault:effect_cloud", "ModEffectCloud2", "the_vault:mod_poison_cloud_brew", tags(),
                tier(0, -1, 10, obj("tooltipDisplayName", "Poison VI", "potion", "minecraft:empty", "additionalEffects", list(obj("effect", "minecraft:poison", "duration", 200, "amplifier", 5)), "duration", 300, "radius", 4.0D, "color", 5149489, "affectsOwner", false, "triggerChance", 0.05D)));
        config.prefix("the_vault:effect_cloud", "ModEffectCloud3", "the_vault:mod_slowness_cloud_brew", tags(),
                tier(0, -1, 10, obj("tooltipDisplayName", "Slowness VI", "potion", "minecraft:empty", "additionalEffects", list(obj("effect", "minecraft:slowness", "duration", 200, "amplifier", 4)), "duration", 360, "radius", 4.0D, "color", 5926017, "affectsOwner", false, "triggerChance", 0.05D)));
        config.prefix("the_vault:effect_cloud", "ModEffectCloud3", "the_vault:mod_weakness_cloud_brew", tags(),
                tier(0, -1, 10, obj("tooltipDisplayName", "Weakness VI", "potion", "minecraft:empty", "additionalEffects", list(obj("effect", "minecraft:weakness", "duration", 300, "amplifier", 1)), "duration", 300, "radius", 4.0D, "color", 4738376, "affectsOwner", false, "triggerChance", 0.05D)));
        config.suffix("the_vault:effect_cloud_chance_additive", "ModEffectCloudChance", "the_vault:mod_effect_cloud_chance", tags(),
                tier(0, -1, 10, obj("min", 0.12D, "max", 0.16D, "step", 0.01D)));
        config.suffix("the_vault:added_ability_level", "ModDiffuseLevel", "the_vault:mod_diffuse_level", tags(),
                tier(0, -1, 10, obj("abilityKey", "Expunge_Base", "levelChange", 6)));
    }

    private static void crashguards(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.implicit("the_vault:armor", "BaseModAbility", "the_vault:base_armor", tags(),
                tier(0, -1, 100, obj("min", 30, "max", 40, "step", 1)));
        config.implicit("the_vault:mana_additive", "BaseBonusPool", "the_vault:base_mana_additive", tags(),
                tier(0, -1, 100, obj("min", 60, "max", 80, "step", 1)));
        config.prefix("the_vault:armor", "ModArmor", "the_vault:u_mod_armor", tags("focusArmor"),
                tier(0, -1, 600, obj("min", 10, "max", 14, "step", 1)));
        config.prefix("the_vault:resistance", "ModResistance", "the_vault:mod_resistance", tags("focusResistance"),
                tier(0, -1, 600, obj("min", 0.16D, "max", 0.21D, "step", 0.01D)));
        config.prefix("the_vault:knockback_resistance", "ModKnockbackResistance", "the_vault:u_knockback_resistance", tags("focusKnockbackRes"),
                tier(0, -1, 600, obj("min", 1.0D, "max", 1.0D, "step", 0.01D)));
        config.suffix("the_vault:kinetic_immunity", "UniqueKineticImmunity", "the_vault:mod_kinetic_immunity", tags(),
                tier(0, -1, 20, obj("flag", true)));
        config.suffix("the_vault:item_rarity", "ModItemRarity", "the_vault:u_item_rarity", tags("focusItemRarity"),
                tier(0, -1, 100, obj("min", 0.3D, "max", 0.4D, "step", 0.01D)));
        config.suffix("the_vault:crashwave", "UniqueCrashwave", "the_vault:u_crashwave", tags(),
                tier(0, -1, 10, obj("min", 1, "max", 4, "step", 1)));
    }

    private static void crystalDoubleBlade(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.implicit("the_vault:attack_damage", "BaseAttackDamage", "the_vault:base_attack_damage", tags(),
                tier(0, -1, 10, obj("min", 100.0D, "max", 150.0D, "step", 1.0D)));
        config.implicit("the_vault:attack_speed", "BaseAttackSpeed", "the_vault:base_attack_speed", tags(),
                tier(0, -1, 10, obj("min", -2.29D, "max", -2.2D, "step", 0.01D)));
        config.prefix("the_vault:ability_power", "BaseBonusPool", "the_vault:mod_ap", tags(),
                tier(0, -1, 100, obj("min", 50.0D, "max", 80.0D, "step", 1.0D)));
        config.suffix("the_vault:attack_range", "uAttackRange", "the_vault:mod_attack_range", tags("focusAttackRange"),
                tier(0, -1, 10, obj("min", 0.5D, "max", 0.67D, "step", 0.01D)));
        config.suffix("the_vault:attack_speed_percent", "uAttackSpeed", "the_vault:mod_attack_speed_percent", tags("focusAttackSpeedPercent", "noLegendary"),
                tier(0, -1, 10, obj("min", 0.4D, "max", 0.5D, "step", 0.01D)));
    }

    private static void crystalplate(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.implicit("the_vault:ability_power", "BaseBonusPool", "the_vault:base_ap", tags(),
                tier(0, -1, 100, obj("min", 50.0D, "max", 80.0D, "step", 1.0D)));
        config.prefix("the_vault:added_ability_level", "ModAbility", "the_vault:u_all_skills", tags(),
                tier(0, -1, 20, obj("levelChange", 3, "abilityKey", "all_abilities")));
        config.prefix("the_vault:ability_power", "BaseBonusPool", "the_vault:mod_ap", tags(),
                tier(0, -1, 100, obj("min", 50.0D, "max", 80.0D, "step", 1.0D)));
        config.suffix("the_vault:mana_regen", "ModManaRegen", "the_vault:mod_mana_regen", tags("focusManaRegen"),
                tier(0, -1, 100, obj("min", 1.0D, "max", 1.33D, "step", 0.01D)));
        config.suffix("the_vault:area_of_effect", "ModEffectRadius", "the_vault:mod_area_of_effect", tags("focusEffectRadius"),
                tier(0, -1, 100, obj("min", 0.2D, "max", 0.27D, "step", 0.01D)));
    }

    private static void echoflare(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:offhand_base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 10, obj("min", 21002, "max", 26252, "step", 1)));
        config.implicit("the_vault:mana_additive", "BaseBonusPool", "the_vault:base_offhand_mana_additive", tags(),
                tier(0, -1, 10, obj("min", 100, "max", 133, "step", 1)));
        config.suffix("the_vault:mana_regen", "ModManaRegen", "the_vault:mod_mana_regen", tags("focusManaRegen"),
                tier(0, -1, 100, obj("min", 1.0D, "max", 1.33D, "step", 0.01D)));
        config.suffix("the_vault:ability_cooldown_skip", "ModEnhancement", "the_vault:mod_ability_cooldown_skip", tags("nolegendary", "noImbuement"),
                tier(0, -1, 100, obj("min", 0.2D, "max", 0.27D, "step", 0.01D)));
    }

    private static void enderRings(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.prefix("the_vault:added_ability_level", "ModAbility", "the_vault:mod_added_ability_level_dashwarp", tags("focusAbilityPower", "focusAbilityPowerDashWarp"),
                tier(0, -1, 30, obj("abilityKey", "Dash_Warp", "levelChange", 2)));
        config.prefix("the_vault:ability_cooldown_percent", "ModEnhancement", "the_vault:warp_cooldown", tags("noImbuement"),
                tier(0, -1, 5, obj("min", -0.6D, "max", -0.5D, "step", 0.01D, "abilityKey", "Dash_Warp")));
        config.suffix("the_vault:warp_projectile_speed", "ModWarpSpeed", "the_vault:mod_warp_projectile_speed", tags("noImbuement"),
                tier(0, -1, 100, obj("min", 0.25D, "max", 0.33D, "step", 0.01D)));
        config.suffix("the_vault:mana_regen", "ModManaRegen", "the_vault:mod_mana_regen", tags("focusManaRegen"),
                tier(0, -1, 100, obj("min", 1.0D, "max", 1.33D, "step", 0.01D)));
    }

    private static void eternalStella(Config config) {
        config.implicit("the_vault:jewel_size", "ModHealthMana", "the_vault:u_jewel_size_stella", tags("focusHealth"),
                tier(0, -1, 500, obj("min", 5, "max", 10, "step", 5)));
        config.prefix("the_vault:immortality", "ModImmortality", "the_vault:u_immortality", tags(),
                tier(0, -1, 100, obj("min", 1.0D, "max", 1.0D, "step", 0.01D)));
    }

    private static void everflame(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.implicit("the_vault:attack_damage", "BaseAttackDamage", "the_vault:base_attack_damage", tags(),
                tier(0, -1, 10, obj("min", 100.0D, "max", 133.0D, "step", 1.0D)));
        config.implicit("the_vault:attack_speed", "BaseAttackSpeed", "the_vault:base_attack_speed", tags(),
                tier(0, -1, 10, obj("min", -2.29D, "max", -2.2D, "step", 0.01D)));
        config.prefix("the_vault:attack_damage", "ModOnHitType", "the_vault:mod_attack_damage", tags("focusAttackDamage"),
                tier(0, -1, 10, obj("min", 55.0D, "max", 73.0D, "step", 1.0D)));
        config.prefix("the_vault:added_ability_level", "ModAbility", "the_vault:mod_everflame_fireball", tags(),
                tier(0, -1, 10, obj("abilityKey", "Fireball_Base", "levelChange", 4)));
        config.suffix("the_vault:area_of_effect", "ModEffectRadius", "the_vault:mod_area_of_effect", tags("focusEffectRadius"),
                tier(0, -1, 100, obj("min", 0.2D, "max", 0.27D, "step", 0.01D)));
        config.suffix("the_vault:ability_special_modification", "ModEnhancement", "the_vault:unique_everflame_modification", tags(),
                tier(0, -1, 5, obj("specialModificationKey", "the_vault:fireball_special_modification", "abilityKey", "Fireball_Base", "textColor", 14076214, "highlightColor", 6082075, "min", 0.65D, "max", 0.86D, "step", 0.01D)));
    }

    private static void everfrost(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.implicit("the_vault:attack_damage", "BaseAttackDamage", "the_vault:base_attack_damage", tags(),
                tier(0, -1, 10, obj("min", 100.0D, "max", 133.0D, "step", 1.0D)));
        config.implicit("the_vault:attack_speed", "BaseAttackSpeed", "the_vault:base_attack_speed", tags(),
                tier(0, -1, 10, obj("min", -2.29D, "max", -2.2D, "step", 0.01D)));
        config.prefix("the_vault:attack_damage", "ModOnHitType", "the_vault:mod_attack_damage", tags("focusAttackDamage"),
                tier(0, -1, 10, obj("min", 55.0D, "max", 73.0D, "step", 1.0D)));
        config.prefix("the_vault:added_ability_level", "ModAbility", "the_vault:mod_everfrost_blast", tags(),
                tier(0, -1, 10, obj("abilityKey", "Ice_Bolt_Blast", "levelChange", 4)));
        config.suffix("the_vault:area_of_effect", "ModEffectRadius", "the_vault:mod_area_of_effect", tags("focusEffectRadius"),
                tier(0, -1, 100, obj("min", 0.2D, "max", 0.27D, "step", 0.01D)));
        config.suffix("the_vault:ability_special_modification", "ModEnhancement", "the_vault:unique_glacial_hypothermia", tags(),
                tier(0, -1, 5, obj("specialModificationKey", "the_vault:glacial_blast_hypothermia", "abilityKey", "Ice_Bolt_Blast", "textColor", 14076214, "highlightColor", 6082075, "min", 6, "max", 8, "step", 1)));
    }

    private static void forkOfTheGlutton(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.implicit("the_vault:attack_damage", "BaseDamage", "the_vault:trident_damage", tags(),
                tier(0, -1, 10, obj("min", 115.0D, "max", 152.0D, "step", 1.0D)));
        config.implicit("the_vault:trident_loyalty", "BaseLoyalty", "the_vault:trident_loyalty", tags(),
                tier(0, -1, 10, obj("min", 4, "max", 5, "step", 1)));
        config.implicit("the_vault:attack_speed", "BaseAttackSpeed", "the_vault:base_attack_speed_axe", tags(),
                tier(0, -1, 10, obj("min", -2.85D, "max", -2.6D, "step", 0.01D)));
        config.implicit("the_vault:lucky_hit_chance", "BaseBonusPool", "the_vault:jester_lucky_hit", tags(),
                tier(0, -1, 10, obj("min", 0.15D, "max", 0.2D, "step", 0.01D)));
        config.prefix("the_vault:hit_hearts", "ModHitHearts", "the_vault:mod_hit_hearts_fork", tags(),
                tier(0, -1, 10, obj("min", 0.75D, "max", 1.0D, "step", 0.01D)));
        config.prefix("the_vault:damage_tank", "ModTankDamage", "the_vault:mod_tank_damage_fork", tags(),
                tier(0, -1, 10, obj("min", 1.0D, "max", 1.33D, "step", 0.01D)));
        config.prefix("the_vault:soul_chance_percentile", "ModSoulQuantity", "the_vault:mod_soul_quantity_fork", tags(),
                tier(0, -1, 10, obj("min", 0.25D, "max", 0.33D, "step", 0.01D)));
        config.suffix("the_vault:effect_cloud", "ModEffectCloud", "the_vault:mod_healing_cloud_fork", tags(),
                tier(0, -1, 10, obj("tooltipDisplayName", "Healing II", "potion", "minecraft:empty", "additionalEffects", list(obj("effect", "minecraft:instant_health", "duration", 20, "amplifier", 0)), "duration", 80, "radius", 4.0D, "color", 16262179, "affectsOwner", true, "triggerChance", 0.05D)));
    }

    private static void frostguards(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.implicit("the_vault:armor", "BaseModAbility", "the_vault:base_armor", tags(),
                tier(0, -1, 100, obj("min", 30, "max", 40, "step", 1)));
        config.prefix("the_vault:added_ability_level", "ModAbility", "the_vault:u_coldsnap", tags("focusAbilityPower", "focusAbilityPowerStonefall"),
                tier(0, -1, 20, obj("abilityKey", "Stonefall_Cold", "levelChange", 2)));
        config.prefix("the_vault:armor", "ModArmor", "the_vault:u_mod_armor", tags("focusArmor"),
                tier(0, -1, 600, obj("min", 10, "max", 13, "step", 1)));
        config.suffix("the_vault:effect_trail", "ModEnhancement", "the_vault:u_chilled_effect_trail", tags(),
                tier(0, -1, 10, obj("effectId", "the_vault:chilled", "durationTicks", obj("min", 160, "max", 240, "step", 20))));
    }

    private static void frostwarden(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.implicit("the_vault:block", "BaseBlock", "the_vault:u_base_block", tags(),
                tier(0, -1, 10, obj("min", 0.4D, "max", 0.53D, "step", 0.01D)));
        config.suffix("the_vault:block_glacial_prison", "ModEnhancement", "the_vault:u_block_glacial_prison", tags("noLegendary", "noImbuement"),
                tier(0, -1, 100, obj("min", 0.5D, "max", 0.6D, "step", 0.01D)));
    }

    private static void frozenHeart(Config config) {
        config.implicit("the_vault:jewel_size", "ModHealthMana", "the_vault:u_jewel_size", tags("focusHealth"),
                tier(0, -1, 500, obj("min", 5, "max", 10, "step", 1)));
        config.suffix("the_vault:ability_cast_on_loot", "ModAbilityCastOnLoot", "the_vault:u_ability_cast_on_loot", tags("noImbuement"),
                tier(0, -1, 100, obj("abilityId", "Nova_Slow", "tileEntityGroupId", "the_vault:chest", "displayName", "Omega Chest", "chance", obj("min", 2, "max", 0.25D, "step", 0.05D), "level", obj("min", 8, "max", 12, "step", 1))));
    }

    private static void frozenOrb(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:offhand_base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 10, obj("min", 21002, "max", 26252, "step", 1)));
        config.implicit("the_vault:mana_additive", "BaseBonusPool", "the_vault:base_offhand_mana_additive", tags(),
                tier(0, -1, 10, obj("min", 100, "max", 133, "step", 1)));
        config.prefix("the_vault:added_ability_level", "ModLvlDefensive", "the_vault:unique_added_ability_level_frostnova", tags("focusAbilityPower", "focusAbilityPowerNova"),
                tier(0, -1, 60, obj("abilityKey", "Nova_Slow", "levelChange", 3)));
        config.prefix("the_vault:ability_special_modification", "ModEnhancement", "the_vault:unique_frost_nova_vulnerability", tags(),
                tier(0, -1, 5, obj("specialModificationKey", "the_vault:frost_nova_vulnerability", "abilityKey", "Nova_Slow", "textColor", 14076214, "highlightColor", 6082075, "min", 6, "max", 8, "step", 1)));
        config.suffix("the_vault:lucky_hit_chance", "ModOnHit", "the_vault:unique_lucky_hit_chance", tags("focusLuckyHitChance"),
                tier(0, -1, 100, obj("min", 0.12D, "max", 0.16D, "step", 0.01D)));
    }

    private static void frozenThrone(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.implicit("the_vault:armor", "BaseModAbility", "the_vault:base_armor", tags(),
                tier(0, -1, 100, obj("min", 30, "max", 40, "step", 1)));
        config.implicit("the_vault:critical_hit_mitigation", "uCritHitImplicit", "the_vault:u_base_crit_hit", tags(),
                tier(0, -1, 100, obj("min", 0.3D, "max", 0.4D, "step", 0.01D)));
        config.prefix("the_vault:added_ability_level", "ModLvlDefensive", "the_vault:unique_added_ability_level_frostnova", tags("focusAbilityPower", "focusAbilityPowerNova"),
                tier(0, -1, 60, obj("abilityKey", "Nova_Slow", "levelChange", 3)));
        config.prefix("the_vault:ability_on_damage", "ModOnDamageType", "the_vault:u_frost_nova_on_damage", tags("noLegendary"),
                tier(0, -1, 20, obj("abilityId", "Nova_Slow", "chance", obj("min", 0.2D, "max", 0.25D, "step", 0.01D), "level", obj("min", 5, "max", 10, "step", 1))));
    }

    private static void gladiatorBuckler(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.implicit("the_vault:block", "BaseBlock", "the_vault:u_base_block", tags(),
                tier(0, -1, 10, obj("min", 0.4D, "max", 0.53D, "step", 0.01D)));
        config.prefix("the_vault:third_attack", "ModThirdAttack", "the_vault:u_third_attack", tags("noLegendary"),
                tier(0, -1, 20, obj("min", 1.2D, "max", 1.6D, "step", 0.01D)));
        config.suffix("the_vault:attack_speed_percent", "uAttackSpeed", "the_vault:mod_attack_speed_percent", tags("focusAttackSpeedPercent", "noLegendary"),
                tier(0, -1, 10, obj("min", 0.25D, "max", 0.33D, "step", 0.01D)));
    }

    private static void grassSword(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.implicit("the_vault:attack_damage", "BaseAttackDamage", "the_vault:base_attack_damage", tags(),
                tier(0, -1, 10, obj("min", 100.0D, "max", 133.0D, "step", 1.0D)));
        config.implicit("the_vault:attack_speed", "BaseAttackSpeed", "the_vault:base_attack_speed", tags(),
                tier(0, -1, 10, obj("min", -2.29D, "max", -2.2D, "step", 0.01D)));
        config.prefix("the_vault:thorns_damage_flat", "BaseBlock", "the_vault:base_thorns_damage_flat_sword", tags(),
                tier(0, -1, 10, obj("min", 45.0D, "max", 59.85D, "step", 0.5D)));
        config.prefix("the_vault:healing_effectiveness", "ModHealthEff", "the_vault:u_grass_sword_healing_eff", tags("focusHealingEff"),
                tier(0, -1, 100, obj("min", 0.25D, "max", 0.33D, "step", 0.01D)));
        config.suffix("the_vault:thorns_scaling_damage", "BaseScaling", "the_vault:thorns_scaling_damage_grass_sword", tags(),
                tier(0, -1, 10, obj("min", 0.5D, "max", 0.67D, "step", 0.01D)));
    }

    private static void grim(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.prefix("the_vault:added_ability_level", "ModAbility", "the_vault:mod_fear", tags(),
                tier(0, -1, 8, obj("abilityKey", "Taunt_Repel", "levelChange", 11)));
        config.prefix("the_vault:ability_area_of_effect_percent", "ModEnhancement", "the_vault:fear_effect_increase", tags(),
                tier(0, -1, 5, obj("min", 1.0D, "max", 1.33D, "step", 0.01D, "abilityKey", "Taunt_Repel")));
    }

    private static void hexblade(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.implicit("the_vault:attack_damage", "BaseAttackDamage", "the_vault:base_attack_damage", tags(),
                tier(0, -1, 10, obj("min", 100.0D, "max", 133.0D, "step", 1.0D)));
        config.implicit("the_vault:attack_speed", "BaseAttackSpeed", "the_vault:base_attack_speed", tags(),
                tier(0, -1, 10, obj("min", -2.29D, "max", -2.2D, "step", 0.01D)));
        config.prefix("the_vault:hexing_chance", "ModOnHitType", "the_vault:mod_hexing_chance", tags("focusHexing"),
                tier(0, -1, 600, obj("min", 0.35D, "max", 0.47D, "step", 0.01D)));
        config.prefix("the_vault:ability_power_percent", "ModAP", "the_vault:mod_ability_increase", tags("focusAbilityDamage", "antiqueAnyAbilityPower"),
                tier(0, -1, 200, obj("min", 0.2D, "max", 0.27D, "step", 0.01D)));
        config.suffix("the_vault:attack_speed_percent", "uAttackSpeed", "the_vault:mod_attack_speed_percent", tags("focusAttackSpeedPercent", "noLegendary"),
                tier(0, -1, 10, obj("min", 0.25D, "max", 0.33D, "step", 0.01D)));
        config.suffix("the_vault:attack_range", "uAttackRange", "the_vault:mod_attack_range", tags("focusAttackRange"),
                tier(0, -1, 10, obj("min", 0.5D, "max", 0.67D, "step", 0.01D)));
    }

    private static void honeyStick(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.implicit("the_vault:attack_damage", "BaseAttackDamage", "the_vault:base_attack_damage", tags(),
                tier(0, -1, 10, obj("min", 100.0D, "max", 133.0D, "step", 1.0D)));
        config.implicit("the_vault:attack_speed", "BaseAttackSpeed", "the_vault:base_attack_speed", tags(),
                tier(0, -1, 10, obj("min", -2.29D, "max", -2.2D, "step", 0.01D)));
        config.implicit("the_vault:sweeping_hit_damage", "baseSweepingHit", "the_vault:base_sweeping_hit_damage", tags(),
                tier(0, -1, 10, obj("min", 0.8D, "max", 1.06D, "step", 0.01D)));
        config.suffix("the_vault:attack_speed_percent", "uAttackSpeed", "the_vault:mod_attack_speed_percent", tags("focusAttackSpeedPercent", "noLegendary"),
                tier(0, -1, 10, obj("min", 0.25D, "max", 0.33D, "step", 0.01D)));
        config.suffix("the_vault:shocking_hit_chance", "ModOnHitAddition", "the_vault:u_shocking_hit", tags("focusShocking"),
                tier(0, -1, 10, obj("min", 1.0D, "max", 1.0D, "step", 0.01D)));
        config.suffix("the_vault:on_hit_stun", "ModOnHitAddition", "the_vault:u_stun_hit", tags("focusStun"),
                tier(0, -1, 10, obj("min", 0.3D, "max", 0.4D, "step", 0.01D)));
    }

    private static void infernoReach(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:offhand_base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 10, obj("min", 21002, "max", 26252, "step", 1)));
        config.implicit("the_vault:ability_power", "BaseBonusPool", "the_vault:base_ap", tags(),
                tier(0, -1, 100, obj("min", 50.0D, "max", 90.0D, "step", 1.0D)));
        config.prefix("the_vault:ability_power_percent", "ModAP", "the_vault:mod_ability_increase", tags("focusAbilityDamage", "antiqueAnyAbilityPower"),
                tier(0, -1, 200, obj("min", 0.2D, "max", 0.27D, "step", 0.01D)));
        config.prefix("the_vault:added_ability_level", "ModAPLvl", "the_vault:mod_added_ability_level_fireball", tags("focusAbilityPower", "focusAbilityPowerFireball"),
                tier(0, -1, 60, obj("abilityKey", "Fireball_Base", "levelChange", 3)));
        config.prefix("the_vault:ability_area_of_effect_percent", "ModEnhancement", "the_vault:fireball_aoe_increase", tags(),
                tier(0, -1, 5, obj("min", 0.5D, "max", 0.67D, "step", 0.01D, "abilityKey", "Fireball_Base")));
        config.prefix("the_vault:cooldown_reduction", "ModCooldownReduction", "the_vault:mod_cooldown_reduction", tags("focusCooldown"),
                tier(0, -1, 100, obj("min", 0.28D, "max", 0.37D, "step", 0.01D)));
        config.prefix("the_vault:area_of_effect", "ModEffectRadius", "the_vault:unique_area_of_effect", tags("focusEffectRadius"),
                tier(0, -1, 100, obj("min", 0.2D, "max", 0.27D, "step", 0.01D)));
    }

    private static void inflatedJustice(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.implicit("the_vault:attack_damage", "BaseAttackDamage", "the_vault:base_attack_damage", tags(),
                tier(0, -1, 10, obj("min", 100.0D, "max", 133.0D, "step", 1.0D)));
        config.implicit("the_vault:attack_speed", "BaseAttackSpeed", "the_vault:base_attack_speed", tags(),
                tier(0, -1, 10, obj("min", -2.29D, "max", -2.2D, "step", 0.01D)));
        config.implicit("the_vault:sweeping_hit_damage", "baseSweepingHit", "the_vault:base_sweeping_hit_damage", tags(),
                tier(0, -1, 10, obj("min", 0.8D, "max", 1.06D, "step", 0.01D)));
        config.prefix("the_vault:attack_damage", "ModOnHitType", "the_vault:mod_attack_damage", tags("focusAttackDamage"),
                tier(0, -1, 10, obj("min", 55.0D, "max", 73.0D, "step", 1.0D)));
        config.prefix("the_vault:damage_baby", "uDamageBaby", "the_vault:mod_damage_baby", tags("focusDamageToddler"),
                tier(0, -1, 10, obj("min", 1.0D, "max", 1.33D, "step", 0.01D)));
        config.suffix("the_vault:lucky_hit_aoe", "ModOnHitType", "the_vault:u_on_hit_aoe", tags("focusChaining"),
                tier(0, -1, 2, obj("min", 5, "max", 5, "step", 1)));
    }

    private static void iskallibur(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.implicit("the_vault:attack_damage", "BaseAttackDamage", "the_vault:base_attack_damage", tags(),
                tier(0, -1, 10, obj("min", 100.0D, "max", 133.0D, "step", 1.0D)));
        config.implicit("the_vault:attack_speed", "BaseAttackSpeed", "the_vault:base_attack_speed", tags(),
                tier(0, -1, 10, obj("min", -2.29D, "max", -2.2D, "step", 0.01D)));
        config.implicit("the_vault:sweeping_hit_damage", "baseSweepingHit", "the_vault:base_sweeping_hit_damage", tags(),
                tier(0, -1, 10, obj("min", 0.8D, "max", 1.06D, "step", 0.01D)));
        config.prefix("the_vault:attack_damage", "ModOnHitType", "the_vault:mod_attack_damage", tags("focusAttackDamage"),
                tier(0, -1, 10, obj("min", 55.0D, "max", 73.0D, "step", 1.0D)));
        config.prefix("the_vault:relentless_strike", "ModRelentlessStrike", "the_vault:u_relentless_strike", tags("noLegendary"),
                tier(0, -1, 20, obj("min", 0.16D, "max", 0.21D, "step", 0.01D)));
        config.suffix("the_vault:attack_speed_percent", "uAttackSpeed", "the_vault:mod_attack_speed_percent", tags("focusAttackSpeedPercent", "noLegendary"),
                tier(0, -1, 10, obj("min", 0.25D, "max", 0.33D, "step", 0.01D)));
    }

    private static void ivy(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.implicit("the_vault:thorns_damage_flat", "BaseBlock", "the_vault:base_thorns_damage_flat", tags(),
                tier(0, -1, 10, obj("min", 50.0D, "max", 66.5D, "step", 0.5D)));
        config.prefix("the_vault:thorns_damage_flat", "ModBlockThorns", "the_vault:u_thorns_damage", tags("focusThornsDamage"),
                tier(0, -1, 10, obj("min", 50.0D, "max", 66.5D, "step", 0.5D)));
        config.prefix("the_vault:lucky_thorns", "ModLuckyThorns", "the_vault:u_lucky_thorns", tags("noLegendary"),
                tier(0, -1, 20, obj("flag", true)));
        config.prefix("the_vault:damage_increase", "ModResistance", "the_vault:mod_damage_increase", tags("focusDamage"),
                tier(0, -1, 600, obj("min", 0.35D, "max", 0.47D, "step", 0.01D)));
        config.suffix("the_vault:lucky_hit_chance", "ModOnHit", "the_vault:u_lucky_hit_chance", tags("focusLuckyHitChance"),
                tier(0, -1, 100, obj("min", 0.1D, "max", 0.13D, "step", 0.01D)));
    }

    private static void jester(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.implicit("the_vault:armor", "BaseModAbility", "the_vault:base_armor", tags(),
                tier(0, -1, 100, obj("min", 30, "max", 40, "step", 1)));
        config.implicit("the_vault:lucky_hit_chance", "BaseBonusPool", "the_vault:base_lucky_hit_chance", tags(),
                tier(0, -1, 10, obj("min", 0.07D, "max", 0.09D, "step", 0.01D)));
        config.prefix("the_vault:jester_lucky_hit_chance_percentile", "UniqueLuckyHit", "the_vault:jester_lucky_hit_percentile", tags("focusLuckyHitChance"),
                tier(0, -1, 10, obj("min", 0.9D, "max", 1, "step", 0.01D)));
        config.suffix("the_vault:item_rarity", "ModItemRarity", "the_vault:u_item_rarity", tags("focusItemRarity"),
                tier(0, -1, 100, obj("min", 0.3D, "max", 0.4D, "step", 0.01D)));
    }

    private static void kaleidoscope(Config config) {
        config.implicit("the_vault:jewel_size", "ModHealthMana", "the_vault:u_jewel_size", tags("focusHealth"),
                tier(0, -1, 500, obj("min", 5, "max", 5, "step", 1)));
        config.prefix("the_vault:wooden_affinity", "ModSoulbound", "the_vault:u_wooden_affinity", tags("noLegendary"),
                tier(0, -1, 20, obj("flag", true)));
        config.prefix("the_vault:coin_affinity", "ModSoulbound", "the_vault:u_coin_affinity", tags("noLegendary"),
                tier(0, -1, 20, obj("flag", true)));
        config.prefix("the_vault:gilded_affinity", "ModSoulbound", "the_vault:u_gilded_affinity", tags("noLegendary"),
                tier(0, -1, 20, obj("flag", true)));
        config.prefix("the_vault:ornate_affinity", "ModSoulbound", "the_vault:u_ornate_affinity", tags("noLegendary"),
                tier(0, -1, 20, obj("flag", true)));
        config.prefix("the_vault:living_affinity", "ModSoulbound", "the_vault:u_living_affinity", tags("noLegendary"),
                tier(0, -1, 20, obj("flag", true)));
    }

    private static void lavaChickenSword(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.implicit("the_vault:attack_damage", "BaseAttackDamage", "the_vault:base_attack_damage", tags(),
                tier(0, -1, 10, obj("min", 100.0D, "max", 133.0D, "step", 1.0D)));
        config.implicit("the_vault:attack_speed", "BaseAttackSpeed", "the_vault:base_attack_speed", tags(),
                tier(0, -1, 10, obj("min", -2.29D, "max", -2.2D, "step", 0.01D)));
        config.implicit("the_vault:lucky_hit_chance", "BaseBonusPool", "the_vault:jester_lucky_hit", tags(),
                tier(0, -1, 10, obj("min", 0.15D, "max", 0.2D, "step", 0.01D)));
        config.prefix("the_vault:burning_hit_chance", "ModBurningHit", "the_vault:mod_burning_hit_lava_chicken", tags(),
                tier(0, -1, 10, obj("min", 0.2D, "max", 0.27D, "step", 0.01D)));
        config.prefix("the_vault:dripping_lava", "ModDrippingLava", "the_vault:mod_dripping_lava", tags(),
                tier(0, -1, 10, obj("flag", true)));
        config.prefix("the_vault:on_hit_aoe", "ModOnHitType", "the_vault:u_on_hit_aoe", tags("focusChaining"),
                tier(0, -1, 2, obj("min", 5, "max", 7, "step", 1)));
        config.suffix("the_vault:on_hit_stun", "ModOnHitAddition", "the_vault:u_stun_hit", tags("focusStun"),
                tier(0, -1, 10, obj("min", 0.3D, "max", 0.4D, "step", 0.01D)));
    }

    private static void leviathan(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.implicit("the_vault:attack_damage", "BaseAttackDamage", "the_vault:u_leviathan_attack_damage", tags(),
                tier(0, -1, 10, obj("min", 132.0D, "max", 175.0D, "step", 1.0D)));
        config.implicit("the_vault:attack_speed", "BaseAttackSpeed", "the_vault:u_leviathan_attack_speed", tags(),
                tier(0, -1, 10, obj("min", -3.5D, "max", -3.35D, "step", 0.01D)));
        config.prefix("the_vault:reaving_damage", "ModOnHitType", "the_vault:u_leviathan_reaving_damage", tags("focusReavingDamage"),
                tier(0, -1, 10, obj("min", 0.75D, "max", 0.85D, "step", 0.01D)));
    }

    private static void manabloom(Config config) {
        config.implicit("the_vault:jewel_size", "ModHealthMana", "the_vault:u_jewel_size_manabloom", tags("focusJewel"),
                tier(0, -1, 500, obj("min", 5, "max", 10, "step", 1)));
        config.prefix("the_vault:mana_per_looted_tile", "uLootedTile", "the_vault:u_mana_looting", tags(),
                tier(0, -1, 1000, obj("tileEntityGroupId", "the_vault:chest", "displayName", "Chests", "manaGenerationChance", obj("min", 0.5D, "max", 0.75D, "step", 0.01D), "manaGenerated", obj("min", 3, "max", 5, "step", 1))));
    }

    private static void mineralGreatsword(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.implicit("the_vault:attack_damage", "BaseAttackDamage", "the_vault:base_attack_damage", tags(),
                tier(0, -1, 10, obj("min", 100.0D, "max", 133.0D, "step", 1.0D)));
        config.implicit("the_vault:attack_speed", "BaseAttackSpeed", "the_vault:base_attack_speed_greatsword", tags(),
                tier(0, -1, 10, obj("min", -2.7D, "max", -2.65D, "step", 0.01D)));
        config.prefix("the_vault:attack_damage", "ModOnHitType", "the_vault:mod_attack_damage_mineral_greatsword", tags("focusAttackDamage"),
                tier(0, -1, 10, obj("min", 70.0D, "max", 93.0D, "step", 1.0D)));
        config.suffix("the_vault:on_hit_stun", "ModStunning", "the_vault:mineral_greatsword_stun", tags("focusStun"),
                tier(0, -1, 10, obj("min", 0.5D, "max", 0.67D, "step", 0.01D)));
        config.suffix("the_vault:attack_range", "uAttackRange", "the_vault:mod_attack_range", tags("focusAttackRange"),
                tier(0, -1, 10, obj("min", 0.5D, "max", 0.67D, "step", 0.01D)));
        config.suffix("the_vault:quaking_hit", "UniqueQuakingHit", "the_vault:u_quaking_hit", tags(),
                tier(0, -1, 10, obj("min", 1, "max", 4, "step", 1)));
    }

    private static void oceanCurrent(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.implicit("the_vault:attack_damage", "BaseDamage", "the_vault:trident_damage_low", tags(),
                tier(0, -1, 10, obj("min", 80.0D, "max", 106.0D, "step", 1.0D)));
        config.implicit("the_vault:trident_loyalty", "BaseLoyalty", "the_vault:trident_loyalty_zeus", tags(),
                tier(0, -1, 10, obj("min", 6, "max", 8, "step", 1)));
        config.implicit("the_vault:trident_channeling", "BaseChanneling", "the_vault:trident_channeling", tags(),
                tier(0, -1, 10, obj("flag", true)));
        config.implicit("the_vault:attack_speed", "BaseAttackSpeed", "the_vault:base_attack_speed_axe", tags(),
                tier(0, -1, 10, obj("min", -2.85D, "max", -2.6D, "step", 0.01D)));
        config.prefix("the_vault:trident_channeling_chance", "ModChannelingChance", "the_vault:channeling_chance_zeus", tags(),
                tier(0, -1, 10, obj("min", 1.0D, "max", 1.0D, "step", 0.01D)));
        config.prefix("the_vault:second_judgement", "ModSecondJudgement", "the_vault:second_judgement_zeus", tags(),
                tier(0, -1, 10, obj("min", 0.75D, "max", 1.0D, "step", 0.01D)));
        config.prefix("the_vault:trident_wind_up_percent", "ModTridentWindup", "the_vault:windup_time_zeus", tags(),
                tier(0, -1, 10, obj("min", 0.75D, "max", 0.85D, "step", 0.01D)));
        config.suffix("the_vault:shocking_hit_chance", "ModShockingHit", "the_vault:shocking_hit_zeus", tags(),
                tier(0, -1, 10, obj("min", 0.25D, "max", 0.33D, "step", 0.01D)));
        config.suffix("the_vault:effect_cloud", "ModEffectCloud", "the_vault:slowness_cloud_zeus", tags(),
                tier(0, -1, 10, obj("tooltipDisplayName", "Slowness II", "potion", "minecraft:empty", "additionalEffects", list(obj("effect", "minecraft:slowness", "duration", 140, "amplifier", 0)), "duration", 120, "radius", 5.0D, "color", 5926017, "affectsOwner", false, "triggerChance", 0.075D)));
    }

    private static void pacifistSandals(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.prefix("the_vault:movement_speed", "uMovementSpeed", "the_vault:u_pacifist_movement", tags(),
                tier(0, -1, 100, obj("min", 0.35D, "max", 0.47D, "step", 0.01D)));
        config.prefix("the_vault:knockback_resistance", "ModKnockbackResistance", "the_vault:u_knockback_resistance", tags("focusKnockbackRes"),
                tier(0, -1, 600, obj("min", 1.0D, "max", 1.0D, "step", 0.01D)));
        config.prefix("the_vault:added_ability_level", "ModAbility", "the_vault:u_entropic_bind", tags(),
                tier(0, -1, 8, obj("abilityKey", "Empower_Slowness_Aura", "levelChange", 2)));
    }

    private static void pax(Config config) {
        config.implicit("the_vault:jewel_size", "ModHealthMana", "the_vault:u_jewel_size_pax", tags("focusHealth"),
                tier(0, -1, 500, obj("min", 3, "max", 5, "step", 5)));
        config.prefix("the_vault:picking", "ModSoulbound", "the_vault:u_picking", tags("noLegendary"),
                tier(0, -1, 20, obj("flag", true)));
        config.prefix("the_vault:axing", "ModSoulbound", "the_vault:u_axing", tags("noLegendary"),
                tier(0, -1, 20, obj("flag", true)));
        config.prefix("the_vault:shovelling", "ModSoulbound", "the_vault:u_shovelling", tags("noLegendary"),
                tier(0, -1, 20, obj("flag", true)));
        config.prefix("the_vault:reaping", "ModSoulbound", "the_vault:u_reaping", tags("noLegendary"),
                tier(0, -1, 20, obj("flag", true)));
    }

    private static void pestilenceWall(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.implicit("the_vault:block", "BaseBlock", "the_vault:u_base_block", tags(),
                tier(0, -1, 10, obj("min", 0.4D, "max", 0.53D, "step", 0.01D)));
        config.prefix("the_vault:knockback_resistance", "ModKnockbackResistance", "the_vault:u_knockback_resistance", tags("focusKnockbackRes"),
                tier(0, -1, 600, obj("min", 1.0D, "max", 1.0D, "step", 0.01D)));
        config.suffix("the_vault:ability_special_modification", "ModEnhancement", "the_vault:unique_entropic_poison", tags(),
                tier(0, -1, 5, obj("specialModificationKey", "the_vault:entropic_bind_poison", "abilityKey", "Empower_Slowness_Aura", "textColor", 14076214, "highlightColor", 6082075, "min", 12, "max", 16, "step", 1)));
    }

    private static void plagueSteppers(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.implicit("the_vault:armor", "BaseModAbility", "the_vault:base_armor", tags(),
                tier(0, -1, 100, obj("min", 30, "max", 40, "step", 1)));
        config.prefix("the_vault:armor", "ModArmor", "the_vault:u_mod_armor", tags("focusArmor"),
                tier(0, -1, 600, obj("min", 10, "max", 14, "step", 1)));
        config.prefix("the_vault:added_ability_level", "ModPoisonNovaLevel", "the_vault:mod_poison_nova_level", tags(),
                tier(0, -1, 10, obj("abilityKey", "Nova_Dot", "levelChange", 5)));
        config.suffix("the_vault:effect_trail", "ModEffectTrail", "the_vault:mod_poison_trail", tags(),
                tier(0, -1, 10, obj("durationTicks", obj("step", 20, "min", 300, "max", 360), "effectId", "minecraft:poison")));
        config.suffix("the_vault:radiation_wave", "UniqueRadiationWave", "the_vault:u_radiation_wave", tags(),
                tier(0, -1, 10, obj("min", 4.0D, "max", 6.0D, "step", 0.5D)));
    }

    private static void pocketPenguin(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.prefix("the_vault:added_ability_level", "ModJavelinLevel", "the_vault:mod_javelin_base_level", tags(),
                tier(0, -1, 10, obj("abilityKey", "Javelin_Base", "levelChange", 2)));
        config.prefix("the_vault:added_ability_level", "ModImplodeLevel", "the_vault:mod_implode_level", tags(),
                tier(0, -1, 10, obj("abilityKey", "Implode", "levelChange", 2)));
        config.prefix("the_vault:javelin_implode", "ModImplodingJavelin", "the_vault:mod_imploding_javelin", tags(),
                tier(0, -1, 10, obj("flag", true)));
        config.prefix("the_vault:ability_mana_cost_percent", "ModJavelinManaIncrease", "the_vault:javelin_mana_cost", tags(),
                tier(0, -1, 10, obj("min", 1.5D, "max", 2.0D, "step", 0.01D, "abilityKey", "Javelin_Base")));
        config.prefix("the_vault:ability_cooldown_percent", "ModJavelinCooldownIncrease", "the_vault:javelin_cooldown_increase", tags(),
                tier(0, -1, 10, obj("min", 10.0D, "max", 14.0D, "step", 0.5D, "abilityKey", "Javelin_Base")));
        config.suffix("the_vault:mana_regen", "ModManaRegen", "the_vault:mod_mana_regen", tags("focusManaRegen"),
                tier(0, -1, 100, obj("min", 1.0D, "max", 1.33D, "step", 0.01D)));
    }

    private static void quickstone(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:magnet_base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 10, obj("min", 60000, "max", 75000, "step", 1)));
        config.prefix("the_vault:range", "BaseRange", "the_vault:base_range", tags(),
                tier(0, -1, 100, obj("min", 25.0D, "max", 33.25D, "step", 0.1D)));
        config.prefix("the_vault:velocity", "BaseVelocity", "the_vault:base_velocity", tags(),
                tier(0, -1, 100, obj("min", 0.25D, "max", 0.33D, "step", 0.01D)));
        config.suffix("the_vault:copiously", "ModCopiously", "the_vault:copiously", tags("focusCopiously"),
                tier(0, -1, 100, obj("min", 0.25D, "max", 0.33D, "step", 0.001D)));
        config.suffix("the_vault:mining_speed_percent", "ModCopiously", "the_vault:u_mining_speed_percent", tags("focusMiningSpeedPercent"),
                tier(0, -1, 100, obj("min", 0.5D, "max", 0.6D, "step", 0.05D)));
    }

    private static void saferSpaces(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.implicit("the_vault:unique_effect", "ModSaferSpaceEffect", "the_vault:saferspaceeffect", tags("noLegendary"),
                tier(0, -1, 5, obj("effectKey", "woldsvaults:safer_space", "amplifier", 4)));
        config.implicit("the_vault:block", "BaseBlock", "the_vault:u_safer_block", tags("noLegendary"),
                tier(0, -1, 10, obj("min", 0.25D, "max", 0.28D, "step", 0.01D)));
    }

    private static void shatteringJewel(Config config) {
        config.implicit("the_vault:jewel_size", "ModHealthMana", "the_vault:u_jewel_size_stella", tags("focusHealth"),
                tier(0, -1, 500, obj("min", 10, "max", 15, "step", 5)));
        config.prefix("woldsvaults:breaching", "ModBreaching", "the_vault:breaching_jewel", tags("noLegendary"),
                tier(0, -1, 20, obj("flag", true)));
    }

    private static void starforge(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.implicit("the_vault:attack_damage", "BaseAttackDamage", "the_vault:base_attack_damage", tags(),
                tier(0, -1, 10, obj("min", 100.0D, "max", 133.0D, "step", 1.0D)));
        config.implicit("the_vault:attack_speed", "BaseAttackSpeed", "the_vault:base_attack_speed", tags(),
                tier(0, -1, 10, obj("min", -2.29D, "max", -2.2D, "step", 0.01D)));
        config.prefix("the_vault:ability_power", "BaseBonusPool", "the_vault:mod_ap", tags(),
                tier(0, -1, 100, obj("min", 50.0D, "max", 66.0D, "step", 1.0D)));
        config.suffix("the_vault:attack_speed_percent", "uAttackSpeed", "the_vault:mod_attack_speed_percent", tags("focusAttackSpeedPercent", "noLegendary"),
                tier(0, -1, 10, obj("min", 0.25D, "max", 0.33D, "step", 0.01D)));
        config.suffix("the_vault:arcane_nova_on_hit", "ModEnhancement", "the_vault:u_arcane_nova_on_hit", tags("noImbuement"),
                tier(0, -1, 100, obj("hitsRequired", obj("min", 5, "max", 6, "step", 1), "radius", obj("min", 6.0D, "max", 7.0D, "step", 1), "percentAbilityPower", obj("min", 2.0D, "max", 2.5D, "step", 0.05D))));
    }

    private static void stormcrown(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.implicit("the_vault:ability_power", "BaseBonusPool", "the_vault:base_ap", tags(),
                tier(0, -1, 100, obj("min", 50.0D, "max", 66.0D, "step", 1.0D)));
        config.prefix("the_vault:added_ability_level", "ModAPLvl", "the_vault:unique_storm_arrow_level", tags("focusAbilityPower", "focusAbilityPowerStormArrow"),
                tier(0, -1, 30, obj("abilityKey", "Storm_Arrow_Base", "levelChange", 3)));
        config.prefix("the_vault:ability_area_of_effect_percent", "ModEnhancement", "the_vault:storm_aoe_reduce", tags(),
                tier(0, -1, 5, obj("min", -0.6D, "max", -0.4D, "step", 0.01D, "abilityKey", "Storm_Arrow_Base")));
        config.prefix("the_vault:ability_cooldown_percent", "ModEnhancement", "the_vault:storm_cooldown", tags(),
                tier(0, -1, 5, obj("min", -0.8D, "max", -0.75D, "step", 0.01D, "abilityKey", "Storm_Arrow_Base")));
        config.suffix("the_vault:mana_regen", "ModManaRegen", "the_vault:mod_mana_regen", tags("focusManaRegen"),
                tier(0, -1, 100, obj("min", 1.0D, "max", 1.33D, "step", 0.01D)));
    }

    private static void swarmwalkers(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.implicit("the_vault:armor", "BaseModAbility", "the_vault:base_armor", tags(),
                tier(0, -1, 100, obj("min", 30, "max", 40, "step", 1)));
        config.prefix("the_vault:movement_speed", "uMovementSpeed", "the_vault:u_movement", tags(),
                tier(0, -1, 100, obj("min", 0.3D, "max", 0.4D, "step", 0.01D)));
        config.prefix("the_vault:knockback_resistance", "ModKnockbackResistance", "the_vault:u_knockback_resistance", tags("focusKnockbackRes"),
                tier(0, -1, 600, obj("min", 1.0D, "max", 1.33D, "step", 0.01D)));
        config.suffix("the_vault:effect_list_avoidance", "ModEffectAvoidance", "the_vault:u_effect_avoidance", tags("focusEffAvoidance"),
                tier(0, -1, 120, obj("effectKeys", list("minecraft:poison", "minecraft:wither", "minecraft:levitation", "minecraft:slowness", "minecraft:blindness", "minecraft:hunger", "the_vault:bleed"), "name", "the_vault.gear_attribute.effect_avoidance.avoidance.bad_effects", "minChance", 0.9D, "maxChance", 1.0D, "step", 0.01D)));
        config.suffix("the_vault:mana_regen", "ModManaRegen", "the_vault:mod_mana_regen", tags("focusManaRegen"),
                tier(0, -1, 100, obj("min", 1.0D, "max", 1.33D, "step", 0.01D)));
    }

    private static void sweetheart(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.implicit("the_vault:health", "ModHealthMana", "the_vault:base_health", tags("focusHealth"),
                tier(0, -1, 500, obj("min", 12, "max", 16, "step", 1)));
        config.prefix("the_vault:health", "ModHealthMana", "the_vault:mod_health", tags("focusHealth"),
                tier(0, -1, 500, obj("min", 12, "max", 16, "step", 1)));
        config.suffix("the_vault:health_boost", "UniqueHealthBoost", "the_vault:u_health_boost", tags("focusHealth"),
                tier(0, -1, 10, obj("min", 0.1D, "max", 0.15D, "step", 0.01D)));
    }

    private static void treasureJewel(Config config) {
        config.implicit("the_vault:jewel_size", "ModHealthMana", "the_vault:u_jewel_size_treasure", tags("focusHealth"),
                tier(0, -1, 500, obj("min", 3, "max", 5, "step", 1)));
        config.prefix("the_vault:treasure_affinity", "ModAffinity", "the_vault:u_treasure_affinity", tags("noLegendary"),
                tier(0, -1, 20, obj("flag", true)));
    }

    private static void treasureMagnet(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_magnet_durability", tags("resilientFocusTarget"),
                tier(0, -1, 10, obj("min", 60000, "max", 75000, "step", 1)));
        config.implicit("the_vault:range", "BaseRange", "the_vault:u_base_magnet_range", tags(),
                tier(0, -1, 100, obj("min", 25.0D, "max", 33.25D, "step", 0.1D)));
        config.implicit("the_vault:endergized", "BaseEndergized", "the_vault:base_endergized", tags(),
                tier(0, -1, 10, obj("flag", true)));
        config.prefix("the_vault:item_rarity", "ModItemQuantity", "the_vault:u_treasure_magnet_item_rarity", tags("focusItemQuantity"),
                tier(0, -1, 100, obj("min", 2.0D, "max", 3.0D, "step", 0.01D)));
        config.prefix("the_vault:item_quantity", "ModItemQuantity", "the_vault:u_treasure_magnet_item_quantity", tags("focusItemQuantity"),
                tier(0, -1, 100, obj("min", 2.0D, "max", 3.0D, "step", 0.01D)));
        config.prefix("the_vault:effect", "ModEffect", "the_vault:u_lucky_lucky_tm", tags(),
                tier(0, -1, 10, obj("effectKey", "minecraft:luck", "amplifier", 2)));
        config.suffix("the_vault:health_percentile", "ModHealthMana", "the_vault:u_treasure_magnet_health", tags(),
                tier(0, -1, 600, obj("min", -0.5D, "max", -0.5D, "step", 0.01D)));
        config.suffix("the_vault:mana_additive_percentile", "ModMana", "the_vault:u_treasure_magnet_mana", tags(),
                tier(0, -1, 600, obj("min", -0.5D, "max", -0.5D, "step", 0.01D)));
    }

    private static void trirang(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.implicit("the_vault:attack_speed", "BaseAttackSpeed", "the_vault:rang_standard_attack_speed", tags(),
                tier(0, -1, 10, obj("min", -2.5D, "max", -2.4D, "step", 0.01D)));
        config.implicit("the_vault:attack_damage", "BaseAttackDamage", "the_vault:rang_standard_attack_damage", tags(),
                tier(0, -1, 10, obj("min", 65.0D, "max", 86.0D, "step", 1.0D)));
        config.implicit("the_vault:range", "BaseRange", "the_vault:rang_standard_range", tags(),
                tier(0, -1, 100, obj("min", 30.0D, "max", 40.0D, "step", 0.1D)));
        config.implicit("the_vault:velocity", "BaseVelocity", "the_vault:rang_standard_velocity", tags(),
                tier(0, -1, 100, obj("min", 0.08D, "max", 0.11D, "step", 0.01D)));
        config.prefix("the_vault:on_hit_chain", "ModChaining", "the_vault:trirang_chaining", tags("focusChaining"),
                tier(0, -1, 10, obj("min", 3, "max", 4, "step", 1)));
        config.prefix("the_vault:piercing", "ModUniquePiercing", "the_vault:trirang_piercing", tags("focusPiercing"),
                tier(0, -1, 1000, obj("min", 3, "max", 4, "step", 1)));
        config.prefix("the_vault:returning_damage", "ModReturningDamage", "the_vault:trirang_returning", tags("focusReturnDamage"),
                tier(0, -1, 1000, obj("min", 0.33D, "max", 0.44D, "step", 0.01D)));
        config.suffix("the_vault:lucky_hit_chance", "ModLuckyHit", "the_vault:trirang_lucky_hit", tags("focusLuckyHitChance"),
                tier(0, -1, 10, obj("min", 0.03D, "max", 0.04D, "step", 0.01D)));
        config.suffix("the_vault:on_hit_stun", "ModStunning", "the_vault:trirang_stun", tags("focusStun"),
                tier(0, -1, 10, obj("min", 0.03D, "max", 0.04D, "step", 0.01D)));
        config.suffix("the_vault:shocking_hit_chance", "ModShocking", "the_vault:trirang_shock", tags("focusShocking"),
                tier(0, -1, 10, obj("min", 0.03D, "max", 0.04D, "step", 0.01D)));
    }

    private static void vitalis(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.implicit("the_vault:armor", "BaseModAbility", "the_vault:base_armor", tags(),
                tier(0, -1, 100, obj("min", 30, "max", 40, "step", 1)));
        config.prefix("the_vault:health", "ModHealthMana", "the_vault:mod_health", tags("focusHealth"),
                tier(0, -1, 500, obj("min", 12, "max", 16, "step", 1)));
        config.prefix("the_vault:armor", "ModArmor", "the_vault:u_mod_armor", tags("focusArmor"),
                tier(0, -1, 600, obj("min", 10, "max", 14, "step", 1)));
        config.prefix("the_vault:resistance", "ModResistance", "the_vault:mod_resistance", tags("focusResistance"),
                tier(0, -1, 600, obj("min", 0.16D, "max", 0.21D, "step", 0.01D)));
        config.prefix("the_vault:added_ability_level", "ModAPLvl", "the_vault:mod_added_ability_level_heal", tags("focusAbilityPower", "focusAbilityPowerHeal"),
                tier(0, -1, 1, obj("abilityKey", "Heal_Base", "levelChange", 3)));
        config.suffix("the_vault:mana_regen", "ModManaRegen", "the_vault:mod_mana_regen", tags("focusManaRegen"),
                tier(0, -1, 100, obj("min", 1.0D, "max", 1.33D, "step", 0.01D)));
        config.suffix("the_vault:block_heal", "ModEnhancement", "the_vault:u_block_heal", tags("noLegendary", "noImbuement"),
                tier(0, -1, 100, obj("chance", obj("min", 0.4D, "max", 0.6D, "step", 0.05D), "amount", obj("min", 3.0D, "max", 3.0D, "step", 0.0D))));
    }

    private static void warboundHelmet(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.implicit("the_vault:armor", "BaseModAbility", "the_vault:base_armor", tags(),
                tier(0, -1, 100, obj("min", 30, "max", 40, "step", 1)));
        config.prefix("the_vault:added_ability_level", "ModAbility", "the_vault:mod_jav_scatter", tags(),
                tier(0, -1, 10, obj("abilityKey", "Javelin_Scatter", "levelChange", 4)));
        config.prefix("the_vault:damage_increase", "ModResistance", "the_vault:mod_damage_increase", tags("focusDamage"),
                tier(0, -1, 600, obj("min", 0.35D, "max", 0.47D, "step", 0.01D)));
        config.prefix("the_vault:cooldown_reduction", "ModCooldownReduction", "the_vault:mod_cooldown_reduction", tags("focusCooldown"),
                tier(0, -1, 100, obj("min", 0.28D, "max", 0.37D, "step", 0.01D)));
        config.suffix("the_vault:mana_regen", "ModManaRegen", "the_vault:mod_mana_regen", tags("focusManaRegen"),
                tier(0, -1, 100, obj("min", 1.0D, "max", 1.33D, "step", 0.01D)));
        config.suffix("the_vault:splitting_javelins", "UniqueSplittingJavelins", "the_vault:u_splitting_javelins", tags(),
                tier(0, -1, 10, obj("min", 2, "max", 3, "step", 1)));
    }

    private static void wickedWitch(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.prefix("the_vault:effect_duration", "uEffectDuration", "the_vault:u_witch_effect_duration", tags("focusDuration"),
                tier(0, -1, 100, obj("min", 1.0D, "max", 1.33D, "step", 0.01D)));
        config.prefix("the_vault:cooldown_reduction", "uCooldownReduction", "the_vault:u_witch_cooldown_reduction", tags("focusCooldown"),
                tier(0, -1, 100, obj("min", -0.4D, "max", -0.27D, "step", 0.05D)));
        config.suffix("the_vault:effect_list_avoidance", "uEffectAvoidance", "the_vault:u_witch_effect_avoidance", tags("focusEffAvoidance"),
                tier(0, -1, 250, obj("effectKeys", list("minecraft:poison", "minecraft:wither", "minecraft:levitation", "minecraft:slowness", "minecraft:blindness", "minecraft:hunger", "the_vault:bleed", "the_vault:chilled", "the_vault:corruption"), "name", "the_vault.gear_attribute.effect_avoidance.avoidance.bad_effects", "minChance", 0.8D, "maxChance", 1.0D, "step", 0.05D)));
    }

    private static void youngKitsune(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.implicit("the_vault:attack_damage", "BaseAttackDamage", "the_vault:base_attack_damage", tags(),
                tier(0, -1, 10, obj("min", 100.0D, "max", 133.0D, "step", 1.0D)));
        config.implicit("the_vault:attack_speed", "BaseAttackSpeed", "the_vault:base_attack_speed", tags(),
                tier(0, -1, 10, obj("min", -2.29D, "max", -2.2D, "step", 0.01D)));
        config.prefix("the_vault:execution_damage", "ModOnHitType", "the_vault:mod_execution_young_kitsune", tags("focusResistance"),
                tier(0, -1, 600, obj("min", 0.1D, "max", 0.13D, "step", 0.01D)));
        config.suffix("the_vault:attack_speed_percent", "uAttackSpeed", "the_vault:mod_attack_speed_percent", tags("focusAttackSpeedPercent", "noLegendary"),
                tier(0, -1, 10, obj("min", 0.25D, "max", 0.33D, "step", 0.01D)));
    }

    private static void zombieHorseAxe(Config config) {
        config.base("the_vault:durability", "BaseDurability", "the_vault:base_durability", tags("resilientFocusTarget"),
                tier(0, -1, 100, obj("min", 29076, "max", 36345, "step", 1)));
        config.implicit("the_vault:attack_damage", "BaseAttackDamage", "the_vault:u_leviathan_attack_damage", tags(),
                tier(0, -1, 10, obj("min", 132.0D, "max", 175.0D, "step", 1.0D)));
        config.implicit("the_vault:attack_speed", "BaseAttackSpeed", "the_vault:base_attack_speed_axe", tags(),
                tier(0, -1, 10, obj("min", -2.85D, "max", -2.6D, "step", 0.01D)));
        config.prefix("the_vault:movement_speed", "uMovementSpeed", "the_vault:u_movement_horse", tags(),
                tier(0, -1, 100, obj("min", 0.4D, "max", 0.53D, "step", 0.01D)));
        config.suffix("the_vault:attack_range", "uAttackRange", "the_vault:mod_attack_range", tags("focusAttackRange"),
                tier(0, -1, 10, obj("min", 0.5D, "max", 0.67D, "step", 0.01D)));
        config.suffix("the_vault:effect_cloud", "ModOnHitAddition", "the_vault:mod_poison_cloud_zombie_horse", tags("focusPoisonCloud"),
                tier(0, -1, 10, obj("tooltipDisplayName", "Poison X", "potion", "minecraft:empty", "additionalEffects", list(obj("effect", "minecraft:poison", "duration", 100, "amplifier", 22, "showParticles", true, "showIcon", true)), "duration", 300, "radius", 7.0D, "color", 4236591, "affectsOwner", false, "triggerChance", 0.2D)));
    }
}
