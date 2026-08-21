package xyz.iwolfking.woldsvaults.config.gods;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Locale;

/**
 * The shipped value of every god node effect, in the shape {@code god_node_effects_<god>.json}
 * stores it. This is the default state {@link GodNodeEffectsConfig#reset()} writes, so a fresh
 * install with no pack file regenerates the same numbers the trees shipped with, and it is the
 * fallback {@link xyz.iwolfking.woldsvaults.gods.GodNodeValues} reads when a configured file is
 * missing an effect or a field.
 *
 * <p>{@code values} is the per-point table; every other key is a parameter of that effect's
 * handler type. Both are transcribed one-for-one from the Java constants the god modules used to
 * carry. An effect still bound to {@link #LEGACY_HANDLER} is one whose god has not been ported
 * yet, and that binding disappears a tree at a time.
 */
public final class GodNodeEffectDefaults {
    /** Handler type of an effect whose values are configured but whose behaviour is still its god module's. */
    public static final String LEGACY_HANDLER = "legacy";

    private static final String GEAR_ATTRIBUTE_SCALED = "gear_attribute_scaled";
    private static final String PIETY = "piety";

    private GodNodeEffectDefaults() {
    }

    /**
     * The default effect table of one god, by lower-case god name. An unknown name yields an
     * empty table rather than throwing, because the caller is a config reset that must not be
     * able to fail.
     */
    public static GodNodeEffectsConfig.EffectMap effects(String god) {
        GodNodeEffectsConfig.EffectMap map = new GodNodeEffectsConfig.EffectMap();
        switch (god == null ? "" : god.toLowerCase(Locale.ROOT)) {
            case "idona" -> idona(map);
            case "velara" -> velara(map);
            case "wendarr" -> wendarr(map);
            case "tenos" -> tenos(map);
            default -> {
            }
        }
        return map;
    }

    /**
     * Builds one entry of an effect that has not been ported onto a handler yet. {@code fields}
     * alternate name and value, and land on the entry object exactly as they appear in the file,
     * so the written config and this table are the same document.
     */
    private static void put(GodNodeEffectsConfig.EffectMap map, String id, float[] values, Object... fields) {
        put(map, id, LEGACY_HANDLER, values, fields);
    }

    /** As {@link #put}, for an effect bound to a real handler type. */
    private static void put(GodNodeEffectsConfig.EffectMap map, String id, String handler, float[] values,
                            Object... fields) {
        JsonObject json = new JsonObject();
        json.addProperty("handler", handler);
        JsonArray table = new JsonArray();
        for (float value : values) {
            table.add(value);
        }
        json.add("values", table);
        for (int i = 0; i + 1 < fields.length; i += 2) {
            String name = (String) fields[i];
            Object value = fields[i + 1];
            if (value instanceof Number number) {
                json.addProperty(name, number);
            } else {
                json.addProperty(name, String.valueOf(value));
            }
        }
        map.put(id, new GodNodeEffectsConfig.Entry(handler, values, json));
    }

    /**
     * Idona, ported off {@link #LEGACY_HANDLER}. Its seven plain stat effects bind the shared
     * {@code gear_attribute_scaled} type and Pious Devotion the shared {@code piety} type, so both
     * are config alone; every effect Java has to know about binds a type named after itself, so an
     * unbound or misspelt effect fails the load instead of falling through to the catch-all.
     */
    private static void idona(GodNodeEffectsConfig.EffectMap map) {
        put(map, "idona_hard_hitter", GEAR_ATTRIBUTE_SCALED, new float[]{0.25F}, "attribute", "the_vault:damage_increase");
        put(map, "idona_hard_hitter_ii", GEAR_ATTRIBUTE_SCALED, new float[]{0.5F}, "attribute", "the_vault:damage_increase");
        put(map, "idona_elite_caster", GEAR_ATTRIBUTE_SCALED, new float[]{0.25F}, "attribute", "the_vault:ability_power_percent");
        put(map, "idona_elite_caster_ii", GEAR_ATTRIBUTE_SCALED, new float[]{0.5F}, "attribute", "the_vault:ability_power_percent");
        put(map, "idona_pious_devotion", PIETY, new float[]{10.0F});
        put(map, "idona_full_of_soul", GEAR_ATTRIBUTE_SCALED, new float[]{0.5F}, "attribute", "the_vault:soul_chance_percentile");
        put(map, "idona_fortunate", GEAR_ATTRIBUTE_SCALED, new float[]{0.025F}, "attribute", "the_vault:lucky_hit_chance");
        put(map, "idona_fortunate_ii", GEAR_ATTRIBUTE_SCALED, new float[]{0.04F}, "attribute", "the_vault:lucky_hit_chance");
        put(map, "idona_stack_stack_stack", "idona_stack_stack_stack", new float[]{1.0F});
        put(map, "idona_king_hunter", "idona_king_hunter", new float[]{0.75F});
        put(map, "idona_enforcer", "idona_enforcer", new float[]{0.75F});
        put(map, "idona_kinetic_impact", "idona_kinetic_impact", new float[]{}, "per_percent", 0.001F);
        put(map, "idona_surrounded", "idona_surrounded", new float[]{}, "per_mob", 0.025F, "radius", 20.0D);
        put(map, "idona_grand_archmage", "idona_grand_archmage", new float[]{},
                "mana_percentile", 1.0F, "mana_flat", 200, "mana_regen", 8.0F, "ability_damage", 2.0F,
                "cooldown_reduction_cap", 0.05F);
        put(map, "idona_ultra_rampaging", "idona_ultra_rampaging", new float[]{});
        put(map, "idona_overcrit", "idona_overcrit", new float[]{});
        put(map, "idona_true_rage", "idona_true_rage", new float[]{}, "efficiency", 0.5F);
        put(map, "idona_crushing_blows", "idona_crushing_blows", new float[]{}, "multiplier", 1.15F);
        put(map, "idona_under_pressure", "idona_under_pressure", new float[]{}, "window_ticks", 18000, "max", 2.0F);
        put(map, "idona_pincushion", "idona_pincushion", new float[]{}, "per_hit", 0.005F);
        put(map, "idona_banked_anger", "idona_banked_anger", new float[]{}, "base", 100000.0D);
        put(map, "idona_soulstealer", "idona_soulstealer", new float[]{}, "multiplier", 1.5F);
        put(map, "idona_luckiest_hit", "idona_luckiest_hit", new float[]{}, "chance_scale", 0.1F);
        put(map, "idona_thwack", "idona_thwack", new float[]{}, "multiplier", 2.0F);
        put(map, "idona_weaponmaster", "idona_weaponmaster", new float[]{},
                "two_handed", 1.5F, "dual_wield_attack_speed", 0.3D);
        put(map, "idona_cleave_expert", "idona_cleave_expert", new float[]{}, "efficiency", 0.2F);
        put(map, "idona_sneaky_advantage", "idona_sneaky_advantage", new float[]{}, "per_effect", 0.05F);
        put(map, "idona_super_stacker", "idona_super_stacker", new float[]{}, "multiplier", 1.25F);
        put(map, "idona_stack_hoarder", "idona_stack_hoarder", new float[]{}, "multiplier", 1.5F);
        put(map, "idona_prison_warden", "idona_prison_warden", new float[]{}, "multiplier", 2.0F, "duration_ticks", 600);
        put(map, "idona_greedbane", "idona_greedbane", new float[]{}, "multiplier", 1.5F);
        put(map, "idona_power_dump", "idona_power_dump", new float[]{},
                "per_mana", 0.0025F, "surplus_ttl_ticks", 900, "continuous_grace_ticks", 30);
    }

    /**
     * Velara, the first tree ported off {@link #LEGACY_HANDLER}. Its twelve plain stat effects
     * bind the shared {@code gear_attribute_scaled} type and are config alone; every effect with
     * behaviour binds a type named after itself, so an unbound or misspelt effect fails the load
     * instead of falling through to the catch-all.
     */
    private static void velara(GodNodeEffectsConfig.EffectMap map) {
        put(map, "velara_tough", GEAR_ATTRIBUTE_SCALED, new float[]{0.25F}, "attribute", "the_vault:health_percentile");
        put(map, "velara_tough_ii", GEAR_ATTRIBUTE_SCALED, new float[]{0.5F}, "attribute", "the_vault:health_percentile");
        put(map, "velara_armored", GEAR_ATTRIBUTE_SCALED, new float[]{0.25F}, "attribute", "the_vault:armor_percentile");
        put(map, "velara_armored_ii", GEAR_ATTRIBUTE_SCALED, new float[]{0.5F}, "attribute", "the_vault:armor_percentile");
        put(map, "velara_immune", "velara_effect_avoidance", new float[]{0.05F});
        put(map, "velara_immune_ii", "velara_effect_avoidance", new float[]{0.1F});
        put(map, "velara_healthy", GEAR_ATTRIBUTE_SCALED, new float[]{0.2F}, "attribute", "the_vault:healing_effectiveness");
        put(map, "velara_healthy_ii", GEAR_ATTRIBUTE_SCALED, new float[]{0.4F}, "attribute", "the_vault:healing_effectiveness");
        put(map, "velara_fast_reflexes", GEAR_ATTRIBUTE_SCALED, new float[]{0.02F}, "attribute", "woldsvaults:dodge_percent");
        put(map, "velara_fast_reflexes_ii", GEAR_ATTRIBUTE_SCALED, new float[]{0.05F}, "attribute", "woldsvaults:dodge_percent");
        put(map, "velara_guarded", GEAR_ATTRIBUTE_SCALED, new float[]{0.02F}, "attribute", "the_vault:block");
        put(map, "velara_guarded_ii", GEAR_ATTRIBUTE_SCALED, new float[]{0.05F}, "attribute", "the_vault:block");
        put(map, "velara_thorny", GEAR_ATTRIBUTE_SCALED, new float[]{25.0F}, "attribute", "the_vault:thorns_damage_flat");
        put(map, "velara_thorny_ii", GEAR_ATTRIBUTE_SCALED, new float[]{50.0F}, "attribute", "the_vault:thorns_damage_flat");
        put(map, "velara_pious_devotion", PIETY, new float[]{10.0F});
        put(map, "velara_counterstrike", "velara_counterstrike", new float[]{}, "chance", 0.5F);
        put(map, "velara_magic_armor", "velara_magic_armor", new float[]{}, "efficiency", 0.5F);
        put(map, "velara_defender_of_the_faith", "velara_defender_of_the_faith", new float[]{}, "per_charm", 0.05F);
        put(map, "velara_sacrifice", "velara_sacrifice", new float[]{}, "syphon", 0.66F, "resistance", 0.33F);
        put(map, "velara_perserverence", "velara_perserverence", new float[]{}, "timer_bonus", 0.5F);
        put(map, "velara_adaptive_armor", "velara_adaptive_armor", new float[]{}, "per_stack", 0.1F, "max_stacks", 6);
        put(map, "velara_bounce_back", "velara_bounce_back", new float[]{}, "multiplier", 2.0F, "health_threshold", 0.1F);
        put(map, "velara_indomitable", "velara_indomitable", new float[]{}, "regeneration_levels", 3);
        put(map, "velara_field_medic", "velara_field_medic", new float[]{}, "multiplier", 1.5F);
        put(map, "velara_the_stonewall", "velara_the_stonewall", new float[]{},
                "speed_multiplier", 0.33F, "armor_multiplier", 1.5F);
        put(map, "velara_cactus", "velara_cactus", new float[]{}, "armor_multiplier", 0.33F, "thorns_multiplier", 2.0F);
        put(map, "velara_malediction", "velara_malediction", new float[]{}, "forced_healing", 0.5F);
        put(map, "velara_immortal", "velara_immortal", new float[]{},
                "health_multiplier", 1.33F, "armor_multiplier", 1.33F, "healing_multiplier", 1.5F,
                "flat_health", 20.0F, "flat_armor", 50.0F, "regeneration_levels", 10, "damage_multiplier", 0.5F,
                "revive_cooldown_ticks", 6000);
        put(map, "velara_fleeting_physicality", "velara_fleeting_physicality", new float[]{},
                "immune_ticks", 200, "vulnerable_ticks", 400, "damage_multiplier", 3.0F);
        put(map, "velara_steadfast", "velara_steadfast", new float[]{}, "knockback_floor", 1.0F, "armor_per_excess", 1.0F);
        put(map, "velara_sanitation", "velara_sanitation", new float[]{}, "radius", 50.0F, "duration_divisor", 3);
        put(map, "velara_presence", "velara_presence", new float[]{},
                "radius", 100.0F, "resistance", 0.1F, "healing", 1.0F, "regeneration_levels", 2);
        put(map, "velara_healing_flow", "velara_healing_flow", new float[]{}, "per_mana_regen", 0.04F);
        put(map, "velara_utilized", "velara_utilized", new float[]{}, "ability_levels", 3);
    }

    private static void wendarr(GodNodeEffectsConfig.EffectMap map) {
        put(map, "wendarr_fruit_conissour", new float[]{0.01F});
        put(map, "wendarr_speedy", new float[]{0.05F});
        put(map, "wendarr_heavily_effected", new float[]{0.1F});
        put(map, "wendarr_speedy_caster", new float[]{0.05F});
        put(map, "wendarr_fruity", new float[]{0.25F});
        put(map, "wendarr_pious_devotion", new float[]{10.0F});
        put(map, "wendarr_master_imbuer", new float[]{});
        put(map, "wendarr_extraction_superviser", new float[]{});
        put(map, "wendarr_expert_eater", new float[]{}, "save_chance", 0.1F);
        put(map, "wendarr_pristine_condition", new float[]{}, "rot_multiplier", 0.85F);
        put(map, "wendarr_efficient_steps", new float[]{}, "ratio", 0.25F);
        put(map, "wendarr_legend_of_the_pear", new float[]{});
        put(map, "wendarr_glutton", new float[]{}, "time_multiplier", 0.33F, "rot_multiplier", 0.2F);
        put(map, "wendarr_paced_strikes", new float[]{}, "reference_minutes", 50.0F);
        put(map, "wendarr_edge_of_time", new float[]{},
                "multiplier", 10.0F, "drain_min_ticks", 20, "drain_max_ticks", 60);
        put(map, "wendarr_extender", new float[]{}, "ticks", 4800);
        put(map, "wendarr_temporal_breaking", new float[]{});
        put(map, "wendarr_plushie_lover", new float[]{});
        put(map, "wendarr_temporal_shielding", new float[]{}, "reduction", 0.1F);
        put(map, "wendarr_tough_stomach", new float[]{}, "health_scaling", 0.86D);
        put(map, "wendarr_clock_artificier", new float[]{}, "multiplier", 1.5F);
        put(map, "wendarr_pylon_whisperer", new float[]{}, "boost", 1.5F);
        put(map, "wendarr_gardener", new float[]{}, "extra_fruit_chance", 0.5F, "starfruit_upgrade_chance", 0.05F);
        put(map, "wendarr_armored_extractors", new float[]{});
        put(map, "wendarr_the_deckless", new float[]{},
                "fruit_per_slot", 0.01F, "speed_per_slot", 0.05F, "cooldown_per_slot", 0.05F);
        put(map, "wendarr_speed_demon", new float[]{}, "rate", 0.75F, "stat_multiplier", 1.1F);
        put(map, "wendarr_quick_search", new float[]{}, "rate", 0.7F);
    }

    private static void tenos(GodNodeEffectsConfig.EffectMap map) {
        put(map, "tenos_hoarder", new float[]{0.25F});
        put(map, "tenos_hoarder_ii", new float[]{0.5F});
        put(map, "tenos_treasurer", new float[]{0.25F});
        put(map, "tenos_treasurer_ii", new float[]{0.5F});
        put(map, "tenos_magical", new float[]{0.4F});
        put(map, "tenos_magical_ii", new float[]{0.8F});
        put(map, "tenos_reserves", new float[]{0.1F});
        put(map, "tenos_reserves_ii", new float[]{0.2F});
        put(map, "tenos_careful", new float[]{0.15F});
        put(map, "tenos_careful_ii", new float[]{0.3F});
        put(map, "tenos_wide_influence", new float[]{0.1F});
        put(map, "tenos_wide_influence_ii", new float[]{0.2F});
        put(map, "tenos_advanced_extraction", new float[]{0.2F});
        put(map, "tenos_advanced_extraction_ii", new float[]{0.4F});
        put(map, "tenos_pious_devotion", new float[]{10.0F});
        put(map, "tenos_omega_vault", new float[]{}, "weight_multiplier", 2.0D);
        put(map, "tenos_looting_engine", new float[]{}, "reference", 1200.0F);
        put(map, "tenos_mana_starved", new float[]{}, "max_bonus", 1.0F, "threshold", 0.5F);
        put(map, "tenos_deep_reserves", new float[]{}, "multiplier", 0.5F);
        put(map, "tenos_barter_expert", new float[]{}, "rich_palette_chance", 0.5F);
        put(map, "tenos_nose_for_treasure", new float[]{}, "treasure_door_bonus", 0.25D);
        put(map, "tenos_domain_expansion", new float[]{}, "per_cell", 0.025F, "cap", 1.5F);
        put(map, "tenos_massive_chests", new float[]{}, "rolls", 20, "rarity_multiplier", 0.5F);
        put(map, "tenos_indiana_jones", new float[]{}, "reference", 1000.0F);
        put(map, "tenos_expert_looter", new float[]{}, "rolls", 4);
        put(map, "tenos_master_of_chests", new float[]{}, "cascading_stacks", 15);
        put(map, "tenos_global_veins", new float[]{}, "levels", 8);
        put(map, "tenos_drillmaster", new float[]{}, "raised_fortune_cap", 7);
        put(map, "tenos_sacked", new float[]{});
        put(map, "tenos_sack_of_mobs", new float[]{}, "log_base", 1000.0F);
        put(map, "tenos_unstoppable_greed", new float[]{}, "ratio", 0.1F);
        put(map, "tenos_wealthy_patron", new float[]{}, "per_unique", 0.03F);
        put(map, "tenos_gold_plating", new float[]{},
                "damage_multiplier", 0.25F, "cost_coefficient", 2.0F, "cost_offset", 10.0F);
        put(map, "tenos_cash_hunter", new float[]{}, "efficiency", 0.25F);
        put(map, "tenos_challenge_tackler", new float[]{}, "extra_crate_tier_ratio", 0.5F, "tiers_per_stack", 1.0F);
    }
}
