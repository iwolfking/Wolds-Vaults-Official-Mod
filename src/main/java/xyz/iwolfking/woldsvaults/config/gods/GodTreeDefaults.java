package xyz.iwolfking.woldsvaults.config.gods;

import java.util.Locale;

/**
 * The shipped constellation topology and layout of all four god trees, in the shape
 * {@code god_tree_<god>.json} and {@code god_tree_<god>_gui_styles.json} store them.
 *
 * <p>This is the tree half of what {@link GodNodeEffectDefaults} is for effect values, and it
 * exists for the same reason. The base mod's config loader catches every exception from a failed
 * read, calls {@code generateConfig()} and writes the result back over the file - so a config
 * whose {@code reset()} produced nothing turned one mistyped character in a hand-edited tree into
 * the permanent loss of that tree, followed by a boot crash on every restart, because validation
 * then found effects with no nodes to place them on. Regenerating the shipped tree here makes the
 * same mistake self-healing and leaves a fresh install with working trees before the pack's files
 * are ever read.
 *
 * <p>The layouts are the drafts exported from {@code redesign/greed-rework/tree-drafts/} in the
 * pack repo; coordinates are screen pixels with +y down, and edges are the undirected lattice
 * connections. {@code ModGodTreesProvider} writes these same builders out as the pack's files, so
 * the shipped default and the generated file are one source.
 *
 * <p>What a node's effect DOES is not written here - that is {@code god_node_effects_<god>.json},
 * which is hand-authored balance data.
 */
public final class GodTreeDefaults {

    private GodTreeDefaults() {
    }

    /** The shipped tree of {@code god}, by lower-case god name, or null if there is no such god. */
    public static GodTreeBuilder forGod(String god) {
        return switch (god == null ? "" : god.toLowerCase(Locale.ROOT)) {
            case "idona" -> idona();
            case "wendarr" -> wendarr();
            case "velara" -> velara();
            case "tenos" -> tenos();
            default -> null;
        };
    }

    public static GodTreeBuilder idona() {
        GodTreeBuilder tree = new GodTreeBuilder("idona");
        sword(tree);
        staff(tree);
        gambler(tree);
        hoard(tree);
        reaper(tree);
        bounty(tree);
        tree.label("The Sword", -199, 271);
        tree.label("The Staff", 293, 365);
        tree.label("The Gambler", -137, -573);
        tree.label("The Hoard", 137, -573);
        tree.label("The Reaper", -388, -462);
        tree.label("The Bounty", 388, -462);
        return tree;
    }

    private static void sword(GodTreeBuilder tree) {
        tree.root("idona_start_sw", "The Sword", -199, 235);
        hardHitter(tree, "idona_sw_1_0_hard_hitter", -136, 226);
        piousDevotion(tree, "idona_sw_1_1_pious_devotion", -190, 172);
        piousDevotion(tree, "idona_sw_2_0_pious_devotion", -99, 190);
        hardHitter(tree, "idona_sw_2_1_hard_hitter", -127, 163);
        hardHitter(tree, "idona_sw_2_2_hard_hitter", -154, 135);
        hardHitter(tree, "idona_sw_3_1_hard_hitter", -27, 190);
        hardHitter(tree, "idona_sw_3_2_hard_hitter", -63, 153);
        kingHunter(tree, "idona_sw_3_3_king_hunter", -90, 126);
        hardHitter(tree, "idona_sw_3_4_hard_hitter", -117, 99);
        hardHitter(tree, "idona_sw_3_5_hard_hitter", -154, 63);
        tree.minor("idona_crushing_blows", "Crushing Blows", "idona_crushing_blows", icon("overpower"), -186, 31);
        hardHitter(tree, "idona_sw_4_0_hard_hitter", 9, 153);
        hardHitter(tree, "idona_sw_4_1_hard_hitter", -27, 117);
        piousDevotion(tree, "idona_sw_4_2_pious_devotion", -54, 90);
        hardHitter(tree, "idona_sw_4_3_hard_hitter", -81, 63);
        hardHitter(tree, "idona_sw_4_4_hard_hitter", -117, 27);
        tree.minor("idona_weaponmaster", "Weaponmaster", "idona_weaponmaster", icon("attack_speed"), 5, 222);
        piousDevotion(tree, "idona_sw_5_0_pious_devotion", 45, 117);
        hardHitter(tree, "idona_sw_5_1_hard_hitter", 9, 81);
        hardHitter(tree, "idona_sw_5_2_hard_hitter", -45, 27);
        hardHitter(tree, "idona_sw_5_3_hard_hitter", -81, -9);
        tree.minor("idona_kinetic_impact", "Kinetic Impact", "idona_kinetic_impact", icon("movement_speed"), 45, 45);
        tree.minor("idona_surrounded", "Surrounded", "idona_surrounded", icon("taunt"), -9, -9);
        hardHitterII(tree, "idona_sw_6_0_hard_hitter_ii", 81, 81);
        piousDevotion(tree, "idona_sw_6_2_pious_devotion", 18, 18);
        hardHitterII(tree, "idona_sw_6_4_hard_hitter_ii", -45, -45);
        hardHitterII(tree, "idona_sw_7_0_hard_hitter_ii", 117, 45);
        hardHitterII(tree, "idona_sw_7_1_hard_hitter_ii", 81, 9);
        hardHitterII(tree, "idona_sw_7_2_hard_hitter_ii", 27, -45);
        piousDevotion(tree, "idona_sw_7_3_pious_devotion", -9, -81);
        piousDevotion(tree, "idona_sw_8_0_pious_devotion", 154, 9);
        hardHitterII(tree, "idona_sw_8_1_hard_hitter_ii", 117, -27);
        hardHitterII(tree, "idona_sw_8_2_hard_hitter_ii", 90, -54);
        hardHitterII(tree, "idona_sw_8_3_hard_hitter_ii", 63, -81);
        hardHitterII(tree, "idona_sw_8_4_hard_hitter_ii", 27, -118);
        tree.minor("idona_cleave_expert", "Cleave Expert", "idona_cleave_expert", icon("aoe"), 99, -118);
        hardHitterII(tree, "idona_sw_9_0_hard_hitter_ii", 190, -27);
        piousDevotion(tree, "idona_sw_9_2_pious_devotion", 127, -91);
        hardHitterII(tree, "idona_sw_9_4_hard_hitter_ii", 63, -154);
        tree.minor("idona_true_rage", "True Rage", "idona_true_rage", icon("rampage_chain"), 154, -63);
        tree.minor("idona_banked_anger", "Banked Anger", "idona_banked_anger", icon("retribution"), 226, -63);
        hardHitterII(tree, "idona_sw_10_1_hard_hitter_ii", 190, -100);
        hardHitterII(tree, "idona_sw_10_2_hard_hitter_ii", 136, -154);
        tree.minor("idona_under_pressure", "Under Pressure", "idona_under_pressure", icon("effect_duration"), 99, -190);
        hardHitterII(tree, "idona_sw_11_0_hard_hitter_ii", 248, -114);
        hardHitterII(tree, "idona_sw_11_1_hard_hitter_ii", 215, -147);
        piousDevotion(tree, "idona_sw_11_2_pious_devotion", 183, -179);
        hardHitterII(tree, "idona_sw_11_3_hard_hitter_ii", 150, -212);
        tree.major("idona_ultra_rampaging", "Ultra Rampaging", "idona_ultra_rampaging", icon("rampage"), 235, -199);
        tree.edge("idona_banked_anger", "idona_sw_11_0_hard_hitter_ii");
        tree.edge("idona_banked_anger", "idona_sw_9_0_hard_hitter_ii");
        tree.edge("idona_cleave_expert", "idona_sw_10_2_hard_hitter_ii");
        tree.edge("idona_cleave_expert", "idona_sw_8_3_hard_hitter_ii");
        tree.edge("idona_crushing_blows", "idona_sw_3_5_hard_hitter");
        tree.edge("idona_kinetic_impact", "idona_sw_5_1_hard_hitter");
        tree.edge("idona_kinetic_impact", "idona_sw_7_1_hard_hitter_ii");
        tree.edge("idona_start_sw", "idona_sw_1_0_hard_hitter");
        tree.edge("idona_start_sw", "idona_sw_1_1_pious_devotion");
        tree.edge("idona_surrounded", "idona_sw_5_2_hard_hitter");
        tree.edge("idona_surrounded", "idona_sw_7_2_hard_hitter_ii");
        tree.edge("idona_sw_10_1_hard_hitter_ii", "idona_sw_11_1_hard_hitter_ii");
        tree.edge("idona_sw_10_1_hard_hitter_ii", "idona_sw_9_2_pious_devotion");
        tree.edge("idona_sw_10_1_hard_hitter_ii", "idona_true_rage");
        tree.edge("idona_sw_10_2_hard_hitter_ii", "idona_sw_11_2_pious_devotion");
        tree.edge("idona_sw_10_2_hard_hitter_ii", "idona_sw_11_3_hard_hitter_ii");
        tree.edge("idona_sw_10_2_hard_hitter_ii", "idona_sw_9_2_pious_devotion");
        tree.edge("idona_sw_10_2_hard_hitter_ii", "idona_sw_9_4_hard_hitter_ii");
        tree.edge("idona_sw_11_0_hard_hitter_ii", "idona_sw_11_1_hard_hitter_ii");
        tree.edge("idona_sw_11_1_hard_hitter_ii", "idona_sw_11_2_pious_devotion");
        tree.edge("idona_sw_11_1_hard_hitter_ii", "idona_ultra_rampaging");
        tree.edge("idona_sw_11_2_pious_devotion", "idona_sw_11_3_hard_hitter_ii");
        tree.edge("idona_sw_11_2_pious_devotion", "idona_ultra_rampaging");
        tree.edge("idona_sw_11_3_hard_hitter_ii", "idona_under_pressure");
        tree.edge("idona_sw_1_0_hard_hitter", "idona_sw_2_0_pious_devotion");
        tree.edge("idona_sw_1_0_hard_hitter", "idona_sw_2_1_hard_hitter");
        tree.edge("idona_sw_1_1_pious_devotion", "idona_sw_2_1_hard_hitter");
        tree.edge("idona_sw_1_1_pious_devotion", "idona_sw_2_2_hard_hitter");
        tree.edge("idona_sw_2_0_pious_devotion", "idona_sw_3_1_hard_hitter");
        tree.edge("idona_sw_2_0_pious_devotion", "idona_sw_3_2_hard_hitter");
        tree.edge("idona_sw_2_0_pious_devotion", "idona_sw_3_3_king_hunter");
        tree.edge("idona_sw_2_1_hard_hitter", "idona_sw_3_3_king_hunter");
        tree.edge("idona_sw_2_1_hard_hitter", "idona_sw_3_4_hard_hitter");
        tree.edge("idona_sw_2_2_hard_hitter", "idona_sw_3_4_hard_hitter");
        tree.edge("idona_sw_2_2_hard_hitter", "idona_sw_3_5_hard_hitter");
        tree.edge("idona_sw_3_1_hard_hitter", "idona_sw_3_2_hard_hitter");
        tree.edge("idona_sw_3_1_hard_hitter", "idona_sw_4_0_hard_hitter");
        tree.edge("idona_sw_3_1_hard_hitter", "idona_weaponmaster");
        tree.edge("idona_sw_3_2_hard_hitter", "idona_sw_3_3_king_hunter");
        tree.edge("idona_sw_3_2_hard_hitter", "idona_sw_4_0_hard_hitter");
        tree.edge("idona_sw_3_2_hard_hitter", "idona_sw_4_1_hard_hitter");
        tree.edge("idona_sw_3_3_king_hunter", "idona_sw_3_4_hard_hitter");
        tree.edge("idona_sw_3_3_king_hunter", "idona_sw_4_1_hard_hitter");
        tree.edge("idona_sw_3_3_king_hunter", "idona_sw_4_2_pious_devotion");
        tree.edge("idona_sw_3_4_hard_hitter", "idona_sw_3_5_hard_hitter");
        tree.edge("idona_sw_3_4_hard_hitter", "idona_sw_4_2_pious_devotion");
        tree.edge("idona_sw_3_4_hard_hitter", "idona_sw_4_3_hard_hitter");
        tree.edge("idona_sw_3_5_hard_hitter", "idona_sw_4_3_hard_hitter");
        tree.edge("idona_sw_3_5_hard_hitter", "idona_sw_4_4_hard_hitter");
        tree.edge("idona_sw_4_0_hard_hitter", "idona_sw_4_1_hard_hitter");
        tree.edge("idona_sw_4_0_hard_hitter", "idona_sw_5_0_pious_devotion");
        tree.edge("idona_sw_4_0_hard_hitter", "idona_sw_5_1_hard_hitter");
        tree.edge("idona_sw_4_1_hard_hitter", "idona_sw_4_2_pious_devotion");
        tree.edge("idona_sw_4_1_hard_hitter", "idona_sw_5_1_hard_hitter");
        tree.edge("idona_sw_4_2_pious_devotion", "idona_sw_4_3_hard_hitter");
        tree.edge("idona_sw_4_2_pious_devotion", "idona_sw_5_1_hard_hitter");
        tree.edge("idona_sw_4_2_pious_devotion", "idona_sw_5_2_hard_hitter");
        tree.edge("idona_sw_4_3_hard_hitter", "idona_sw_4_4_hard_hitter");
        tree.edge("idona_sw_4_3_hard_hitter", "idona_sw_5_2_hard_hitter");
        tree.edge("idona_sw_4_3_hard_hitter", "idona_sw_5_3_hard_hitter");
        tree.edge("idona_sw_4_4_hard_hitter", "idona_sw_5_3_hard_hitter");
        tree.edge("idona_sw_5_0_pious_devotion", "idona_sw_6_0_hard_hitter_ii");
        tree.edge("idona_sw_5_1_hard_hitter", "idona_sw_6_0_hard_hitter_ii");
        tree.edge("idona_sw_5_1_hard_hitter", "idona_sw_6_2_pious_devotion");
        tree.edge("idona_sw_5_2_hard_hitter", "idona_sw_6_2_pious_devotion");
        tree.edge("idona_sw_5_3_hard_hitter", "idona_sw_6_4_hard_hitter_ii");
        tree.edge("idona_sw_6_0_hard_hitter_ii", "idona_sw_7_0_hard_hitter_ii");
        tree.edge("idona_sw_6_0_hard_hitter_ii", "idona_sw_7_1_hard_hitter_ii");
        tree.edge("idona_sw_6_2_pious_devotion", "idona_sw_7_1_hard_hitter_ii");
        tree.edge("idona_sw_6_2_pious_devotion", "idona_sw_7_2_hard_hitter_ii");
        tree.edge("idona_sw_6_4_hard_hitter_ii", "idona_sw_7_3_pious_devotion");
        tree.edge("idona_sw_7_0_hard_hitter_ii", "idona_sw_7_1_hard_hitter_ii");
        tree.edge("idona_sw_7_0_hard_hitter_ii", "idona_sw_8_0_pious_devotion");
        tree.edge("idona_sw_7_1_hard_hitter_ii", "idona_sw_8_0_pious_devotion");
        tree.edge("idona_sw_7_1_hard_hitter_ii", "idona_sw_8_1_hard_hitter_ii");
        tree.edge("idona_sw_7_1_hard_hitter_ii", "idona_sw_8_2_hard_hitter_ii");
        tree.edge("idona_sw_7_2_hard_hitter_ii", "idona_sw_7_3_pious_devotion");
        tree.edge("idona_sw_7_2_hard_hitter_ii", "idona_sw_8_2_hard_hitter_ii");
        tree.edge("idona_sw_7_2_hard_hitter_ii", "idona_sw_8_3_hard_hitter_ii");
        tree.edge("idona_sw_7_3_pious_devotion", "idona_sw_8_3_hard_hitter_ii");
        tree.edge("idona_sw_7_3_pious_devotion", "idona_sw_8_4_hard_hitter_ii");
        tree.edge("idona_sw_8_0_pious_devotion", "idona_sw_9_0_hard_hitter_ii");
        tree.edge("idona_sw_8_1_hard_hitter_ii", "idona_sw_9_2_pious_devotion");
        tree.edge("idona_sw_8_1_hard_hitter_ii", "idona_true_rage");
        tree.edge("idona_sw_8_2_hard_hitter_ii", "idona_sw_9_2_pious_devotion");
        tree.edge("idona_sw_8_3_hard_hitter_ii", "idona_sw_9_4_hard_hitter_ii");
        tree.edge("idona_sw_8_4_hard_hitter_ii", "idona_sw_9_4_hard_hitter_ii");
        tree.edge("idona_sw_9_4_hard_hitter_ii", "idona_under_pressure");
    }

    private static void staff(GodTreeBuilder tree) {
        tree.root("idona_start_st", "The Staff", 293, 329);
        eliteCaster(tree, "idona_st_1_0_elite_caster", 285, 264);
        piousDevotion(tree, "idona_st_1_1_pious_devotion", 228, 321);
        eliteCaster(tree, "idona_st_2_0_elite_caster", 249, 228);
        eliteCaster(tree, "idona_st_2_1_elite_caster", 220, 256);
        eliteCaster(tree, "idona_st_2_2_elite_caster", 192, 285);
        eliteCaster(tree, "idona_st_3_1_elite_caster", 213, 192);
        fullOfSoul(tree, "idona_st_3_2_full_of_soul", 184, 220);
        eliteCaster(tree, "idona_st_3_3_elite_caster", 156, 249);
        tree.minor("idona_pincushion", "Pincushion", "idona_pincushion", icon("thorns"), 123, 282);
        piousDevotion(tree, "idona_st_4_0_pious_devotion", 176, 156);
        eliteCaster(tree, "idona_st_4_1_elite_caster", 148, 184);
        eliteCaster(tree, "idona_st_4_2_elite_caster", 120, 212);
        tree.minor("idona_thwack", "Thwack", "idona_thwack", icon("stun_chance"), 246, 159);
        eliteCaster(tree, "idona_st_5_0_elite_caster", 112, 148);
        piousDevotion(tree, "idona_st_6_0_pious_devotion", -112, -76);
        eliteCasterII(tree, "idona_st_7_0_elite_caster_ii", -120, -140);
        piousDevotion(tree, "idona_st_7_1_pious_devotion", -176, -84);
        tree.minor("idona_prison_warden", "Prison Warden", "idona_prison_warden", icon("ice_blast"), -156, -177);
        tree.minor("idona_sneaky_advantage", "Sneaky Advantage", "idona_sneaky_advantage", icon("toxic_grenade"), -213, -120);
        eliteCasterII(tree, "idona_st_8_1_elite_caster_ii", -184, -148);
        eliteCasterII(tree, "idona_st_9_0_elite_caster_ii", -192, -213);
        piousDevotion(tree, "idona_st_9_1_pious_devotion", -249, -156);
        eliteCasterII(tree, "idona_st_10_0_elite_caster_ii", -228, -249);
        eliteCasterII(tree, "idona_st_10_1_elite_caster_ii", -257, -221);
        eliteCasterII(tree, "idona_st_10_2_elite_caster_ii", -285, -192);
        eliteCasterII(tree, "idona_st_11_1_elite_caster_ii", -265, -285);
        piousDevotion(tree, "idona_st_11_2_pious_devotion", -293, -257);
        eliteCasterII(tree, "idona_st_11_3_elite_caster_ii", -321, -229);
        stackStackStack(tree, "idona_st_11_4_stack_stack_stack", -354, -195);
        tree.major("idona_grand_archmage", "Grand Archmage", "idona_grand_archmage", icon("arcane"), -329, -293);
        tree.minor("idona_power_dump", "Power Dump", "idona_power_dump", icon("mana"), -231, -318);
        tree.edge("idona_grand_archmage", "idona_st_11_1_elite_caster_ii");
        tree.edge("idona_grand_archmage", "idona_st_11_2_pious_devotion");
        tree.edge("idona_grand_archmage", "idona_st_11_3_elite_caster_ii");
        tree.edge("idona_pincushion", "idona_st_3_3_elite_caster");
        tree.edge("idona_power_dump", "idona_st_11_1_elite_caster_ii");
        tree.edge("idona_prison_warden", "idona_st_7_0_elite_caster_ii");
        tree.edge("idona_prison_warden", "idona_st_9_0_elite_caster_ii");
        tree.edge("idona_sneaky_advantage", "idona_st_7_1_pious_devotion");
        tree.edge("idona_sneaky_advantage", "idona_st_9_1_pious_devotion");
        tree.edge("idona_st_10_0_elite_caster_ii", "idona_st_10_1_elite_caster_ii");
        tree.edge("idona_st_10_0_elite_caster_ii", "idona_st_11_1_elite_caster_ii");
        tree.edge("idona_st_10_0_elite_caster_ii", "idona_st_11_2_pious_devotion");
        tree.edge("idona_st_10_0_elite_caster_ii", "idona_st_9_0_elite_caster_ii");
        tree.edge("idona_st_10_1_elite_caster_ii", "idona_st_10_2_elite_caster_ii");
        tree.edge("idona_st_10_1_elite_caster_ii", "idona_st_11_2_pious_devotion");
        tree.edge("idona_st_10_1_elite_caster_ii", "idona_st_11_3_elite_caster_ii");
        tree.edge("idona_st_10_1_elite_caster_ii", "idona_st_9_0_elite_caster_ii");
        tree.edge("idona_st_10_1_elite_caster_ii", "idona_st_9_1_pious_devotion");
        tree.edge("idona_st_10_2_elite_caster_ii", "idona_st_11_3_elite_caster_ii");
        tree.edge("idona_st_10_2_elite_caster_ii", "idona_st_11_4_stack_stack_stack");
        tree.edge("idona_st_10_2_elite_caster_ii", "idona_st_9_1_pious_devotion");
        tree.edge("idona_st_11_1_elite_caster_ii", "idona_st_11_2_pious_devotion");
        tree.edge("idona_st_11_2_pious_devotion", "idona_st_11_3_elite_caster_ii");
        tree.edge("idona_st_11_3_elite_caster_ii", "idona_st_11_4_stack_stack_stack");
        tree.edge("idona_st_1_0_elite_caster", "idona_st_2_0_elite_caster");
        tree.edge("idona_st_1_0_elite_caster", "idona_st_2_1_elite_caster");
        tree.edge("idona_st_1_0_elite_caster", "idona_start_st");
        tree.edge("idona_st_1_1_pious_devotion", "idona_st_2_1_elite_caster");
        tree.edge("idona_st_1_1_pious_devotion", "idona_st_2_2_elite_caster");
        tree.edge("idona_st_1_1_pious_devotion", "idona_start_st");
        tree.edge("idona_st_2_0_elite_caster", "idona_st_3_1_elite_caster");
        tree.edge("idona_st_2_0_elite_caster", "idona_st_3_2_full_of_soul");
        tree.edge("idona_st_2_1_elite_caster", "idona_st_3_2_full_of_soul");
        tree.edge("idona_st_2_1_elite_caster", "idona_st_3_3_elite_caster");
        tree.edge("idona_st_2_2_elite_caster", "idona_st_3_3_elite_caster");
        tree.edge("idona_st_3_1_elite_caster", "idona_st_3_2_full_of_soul");
        tree.edge("idona_st_3_1_elite_caster", "idona_st_4_0_pious_devotion");
        tree.edge("idona_st_3_1_elite_caster", "idona_thwack");
        tree.edge("idona_st_3_2_full_of_soul", "idona_st_3_3_elite_caster");
        tree.edge("idona_st_3_2_full_of_soul", "idona_st_4_0_pious_devotion");
        tree.edge("idona_st_3_2_full_of_soul", "idona_st_4_1_elite_caster");
        tree.edge("idona_st_3_3_elite_caster", "idona_st_4_1_elite_caster");
        tree.edge("idona_st_3_3_elite_caster", "idona_st_4_2_elite_caster");
        tree.edge("idona_st_4_0_pious_devotion", "idona_st_4_1_elite_caster");
        tree.edge("idona_st_4_0_pious_devotion", "idona_st_5_0_elite_caster");
        tree.edge("idona_st_4_1_elite_caster", "idona_st_4_2_elite_caster");
        tree.edge("idona_st_4_1_elite_caster", "idona_st_5_0_elite_caster");
        tree.edge("idona_st_4_2_elite_caster", "idona_st_5_0_elite_caster");
        tree.edge("idona_st_5_0_elite_caster", "idona_st_6_0_pious_devotion");
        tree.edge("idona_st_5_0_elite_caster", "idona_sw_6_0_hard_hitter_ii");
        tree.edge("idona_st_6_0_pious_devotion", "idona_st_7_0_elite_caster_ii");
        tree.edge("idona_st_6_0_pious_devotion", "idona_st_7_1_pious_devotion");
        tree.edge("idona_st_6_0_pious_devotion", "idona_sw_6_4_hard_hitter_ii");
        tree.edge("idona_st_7_0_elite_caster_ii", "idona_st_8_1_elite_caster_ii");
        tree.edge("idona_st_7_1_pious_devotion", "idona_st_8_1_elite_caster_ii");
        tree.edge("idona_st_8_1_elite_caster_ii", "idona_st_9_0_elite_caster_ii");
        tree.edge("idona_st_8_1_elite_caster_ii", "idona_st_9_1_pious_devotion");
    }

    private static void gambler(GodTreeBuilder tree) {
        tree.root("idona_start_gambler", "The Gambler", -137, -609);
        fortunate(tree, "idona_gambler_a1_fortunate", -151, -674);
        piousDevotion(tree, "idona_gambler_a2_pious_devotion", -165, -739);
        fortunate(tree, "idona_gambler_s1_fortunate", -120, -736);
        fortunate(tree, "idona_gambler_s2_fortunate", -205, -718);
        fortunateII(tree, "idona_gambler_g1_fortunate_ii", -67, -783);
        fortunateII(tree, "idona_gambler_g2_fortunate_ii", -272, -740);
        fortunateII(tree, "idona_gambler_head_fortunate_ii", -178, -802);
        tree.minor("idona_luckiest_hit", "Luckiest Hit", "idona_luckiest_hit", icon("battle_cry_lucky_strike"), -279, -822);
        tree.minor("idona_overcrit", "Overcrit", "idona_overcrit", icon("double_damage"), -94, -861);
        tree.edge("idona_gambler_a1_fortunate", "idona_gambler_a2_pious_devotion");
        tree.edge("idona_gambler_a1_fortunate", "idona_gambler_s1_fortunate");
        tree.edge("idona_gambler_a1_fortunate", "idona_gambler_s2_fortunate");
        tree.edge("idona_gambler_a1_fortunate", "idona_start_gambler");
        tree.edge("idona_gambler_a2_pious_devotion", "idona_gambler_head_fortunate_ii");
        tree.edge("idona_gambler_a2_pious_devotion", "idona_gambler_s1_fortunate");
        tree.edge("idona_gambler_a2_pious_devotion", "idona_gambler_s2_fortunate");
        tree.edge("idona_gambler_g1_fortunate_ii", "idona_gambler_s1_fortunate");
        tree.edge("idona_gambler_g1_fortunate_ii", "idona_overcrit");
        tree.edge("idona_gambler_g2_fortunate_ii", "idona_gambler_s2_fortunate");
        tree.edge("idona_gambler_g2_fortunate_ii", "idona_luckiest_hit");
    }

    private static void hoard(GodTreeBuilder tree) {
        tree.root("idona_start_hoard", "The Hoard", 137, -609);
        stackStackStack(tree, "idona_hoard_l1_stack_stack_stack", 197, -657);
        stackStackStack(tree, "idona_hoard_r1_stack_stack_stack", 102, -677);
        hardHitter(tree, "idona_hoard_c1_hard_hitter", 159, -712);
        stackStackStack(tree, "idona_hoard_l2_stack_stack_stack", 242, -710);
        stackStackStack(tree, "idona_hoard_r2_stack_stack_stack", 82, -744);
        stackStackStack(tree, "idona_hoard_c2_stack_stack_stack", 172, -774);
        stackStackStack(tree, "idona_hoard_l3_stack_stack_stack", 273, -768);
        stackStackStack(tree, "idona_hoard_r3_stack_stack_stack", 78, -810);
        hardHitterII(tree, "idona_hoard_top_hard_hitter_ii", 185, -837);
        tree.minor("idona_stack_hoarder", "Stack Hoarder", "idona_stack_hoarder", icon("empower_entropy"), 99, -873);
        tree.minor("idona_super_stacker", "Super Stacker", "idona_super_stacker", icon("empower"), 279, -835);
        tree.edge("idona_hoard_c1_hard_hitter", "idona_hoard_c2_stack_stack_stack");
        tree.edge("idona_hoard_c1_hard_hitter", "idona_hoard_l1_stack_stack_stack");
        tree.edge("idona_hoard_c1_hard_hitter", "idona_hoard_r1_stack_stack_stack");
        tree.edge("idona_hoard_c2_stack_stack_stack", "idona_hoard_l2_stack_stack_stack");
        tree.edge("idona_hoard_c2_stack_stack_stack", "idona_hoard_r2_stack_stack_stack");
        tree.edge("idona_hoard_c2_stack_stack_stack", "idona_hoard_top_hard_hitter_ii");
        tree.edge("idona_hoard_l1_stack_stack_stack", "idona_hoard_l2_stack_stack_stack");
        tree.edge("idona_hoard_l1_stack_stack_stack", "idona_start_hoard");
        tree.edge("idona_hoard_l2_stack_stack_stack", "idona_hoard_l3_stack_stack_stack");
        tree.edge("idona_hoard_l3_stack_stack_stack", "idona_hoard_top_hard_hitter_ii");
        tree.edge("idona_hoard_l3_stack_stack_stack", "idona_super_stacker");
        tree.edge("idona_hoard_r1_stack_stack_stack", "idona_hoard_r2_stack_stack_stack");
        tree.edge("idona_hoard_r1_stack_stack_stack", "idona_start_hoard");
        tree.edge("idona_hoard_r2_stack_stack_stack", "idona_hoard_r3_stack_stack_stack");
        tree.edge("idona_hoard_r3_stack_stack_stack", "idona_hoard_top_hard_hitter_ii");
        tree.edge("idona_hoard_r3_stack_stack_stack", "idona_stack_hoarder");
    }

    private static void reaper(GodTreeBuilder tree) {
        tree.root("idona_start_reaper", "The Reaper", -388, -498);
        fullOfSoul(tree, "idona_reaper_h1_full_of_soul", -431, -558);
        fullOfSoul(tree, "idona_reaper_x1_full_of_soul", -420, -620);
        piousDevotion(tree, "idona_reaper_x2_pious_devotion", -494, -566);
        fullOfSoul(tree, "idona_reaper_b1_full_of_soul", -440, -687);
        fullOfSoul(tree, "idona_reaper_h2_full_of_soul", -484, -630);
        piousDevotion(tree, "idona_reaper_b2_pious_devotion", -420, -755);
        fullOfSoul(tree, "idona_reaper_fin_full_of_soul", -536, -671);
        tree.minor("idona_soulstealer", "Soulstealer", "idona_soulstealer", icon("soul_chance"), -359, -749);
        tree.edge("idona_reaper_b1_full_of_soul", "idona_reaper_b2_pious_devotion");
        tree.edge("idona_reaper_b1_full_of_soul", "idona_reaper_h2_full_of_soul");
        tree.edge("idona_reaper_b1_full_of_soul", "idona_reaper_x1_full_of_soul");
        tree.edge("idona_reaper_b1_full_of_soul", "idona_soulstealer");
        tree.edge("idona_reaper_b2_pious_devotion", "idona_soulstealer");
        tree.edge("idona_reaper_fin_full_of_soul", "idona_reaper_h2_full_of_soul");
        tree.edge("idona_reaper_h1_full_of_soul", "idona_reaper_x1_full_of_soul");
        tree.edge("idona_reaper_h1_full_of_soul", "idona_reaper_x2_pious_devotion");
        tree.edge("idona_reaper_h1_full_of_soul", "idona_start_reaper");
        tree.edge("idona_reaper_h2_full_of_soul", "idona_reaper_x1_full_of_soul");
        tree.edge("idona_reaper_h2_full_of_soul", "idona_reaper_x2_pious_devotion");
    }

    private static void bounty(GodTreeBuilder tree) {
        tree.root("idona_start_bounty", "The Bounty", 388, -498);
        enforcer(tree, "idona_bounty_h1_enforcer", 430, -556);
        kingHunter(tree, "idona_bounty_h2_king_hunter", 470, -611);
        enforcer(tree, "idona_bounty_a1_enforcer", 442, -660);
        kingHunter(tree, "idona_bounty_a3_king_hunter", 528, -642);
        enforcer(tree, "idona_bounty_a2_enforcer", 492, -715);
        tree.minor("idona_greedbane", "Greedbane", "idona_greedbane", icon("greed_assassin_dmg"), 420, -724);
        tree.edge("idona_bounty_a1_enforcer", "idona_bounty_a2_enforcer");
        tree.edge("idona_bounty_a1_enforcer", "idona_bounty_h2_king_hunter");
        tree.edge("idona_bounty_a1_enforcer", "idona_greedbane");
        tree.edge("idona_bounty_a2_enforcer", "idona_bounty_a3_king_hunter");
        tree.edge("idona_bounty_a2_enforcer", "idona_greedbane");
        tree.edge("idona_bounty_a3_king_hunter", "idona_bounty_h2_king_hunter");
        tree.edge("idona_bounty_h1_enforcer", "idona_bounty_h2_king_hunter");
        tree.edge("idona_bounty_h1_enforcer", "idona_start_bounty");
    }
    private static void hardHitter(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Hard Hitter", "idona_hard_hitter", icon("attack_damage_percentile"), x, y);
    }

    private static void hardHitterII(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Hard Hitter II", "idona_hard_hitter_ii", icon("attack_damage_percentile"), x, y);
    }

    private static void eliteCaster(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Elite Caster", "idona_elite_caster", icon("ability_power_percentile"), x, y);
    }

    private static void eliteCasterII(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Elite Caster II", "idona_elite_caster_ii", icon("ability_power_percentile"), x, y);
    }

    private static void piousDevotion(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Pious Devotion", "idona_pious_devotion", icon("smite"), x, y);
    }

    private static void fullOfSoul(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Full of Soul", "idona_full_of_soul", icon("soul_chance"), x, y);
    }

    private static void fortunate(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Fortunate", "idona_fortunate", icon("lucky_hit"), x, y);
    }

    private static void fortunateII(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Fortunate II", "idona_fortunate_ii", icon("lucky_hit"), x, y);
    }

    private static void stackStackStack(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Stack Stack Stack", "idona_stack_stack_stack", icon("skill_point"), x, y);
    }

    private static void kingHunter(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "King Hunter", "idona_king_hunter", icon("champion_dmg"), x, y);
    }

    private static void enforcer(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Enforcer", "idona_enforcer", icon("horde_dmg"), x, y);
    }

    public static GodTreeBuilder wendarr() {
        GodTreeBuilder tree = new GodTreeBuilder("wendarr");
        glassFrame(tree);
        feast(tree);
        harvest(tree);
        runner(tree);
        clockwork(tree);
        swift(tree);
        deck(tree);
        spanner(tree);
        bough(tree);
        wendarrBridges(tree);
        tree.label("The Feast", -305, -234);
        tree.label("The Harvest", 305, -234);
        tree.label("The Runner", -305, 234);
        tree.label("The Clockwork", 305, 234);
        tree.label("The Swift", 333, -148);
        tree.label("The Deck", 333, 148);
        tree.label("The Spanner", -333, -148);
        tree.label("The Bough", -333, 148);
        return tree;
    }

    private static void glassFrame(GodTreeBuilder tree) {
        tree.root("wendarr_start_top", "Top of the Glass", 0, -439);
        tree.root("wendarr_start_bot", "Bottom of the Glass", 0, 439);
        tree.major("wendarr_legend_of_the_pear", "Legend of the Pear", "wendarr_legend_of_the_pear", icon("fruit_effectiveness"), 0, -63);
        tree.major("wendarr_edge_of_time", "Edge of Time", "wendarr_edge_of_time", icon("double_damage"), 0, 63);
        masterImbuer(tree, "wendarr_fr_pl_master_imbuer", -338, 0);
        masterImbuer(tree, "wendarr_fr_pr_master_imbuer", 338, 0);
    }

    private static void feast(GodTreeBuilder tree) {
        fruitConissour(tree, "wendarr_tl_0_0_fruit_conissour", -57, -439);
        fruitConissour(tree, "wendarr_tl_0_1_fruit_conissour", -57, -477);
        heavilyEffected(tree, "wendarr_tl_1_0_heavily_effected", -115, -459);
        wendarrPiousDevotion(tree, "wendarr_tl_1_1_pious_devotion", -115, -497);
        fruitConissour(tree, "wendarr_tl_2_0_fruit_conissour", -173, -459);
        tree.minor("wendarr_pristine_condition", "Pristine Condition", "wendarr_pristine_condition", icon("cleanse"), -175, -497);
        fruitConissour(tree, "wendarr_tl_3_0_fruit_conissour", -237, -428);
        heavilyEffected(tree, "wendarr_tl_3_1_heavily_effected", -265, -453);
        wendarrPiousDevotion(tree, "wendarr_tl_4_0_pious_devotion", -259, -360);
        fruitConissour(tree, "wendarr_tl_4_1_fruit_conissour", -297, -358);
        fruitConissour(tree, "wendarr_tl_5_0_fruit_conissour", -198, -325);
        heavilyEffected(tree, "wendarr_tl_5_1_heavily_effected", -230, -299);
        fruitConissour(tree, "wendarr_tl_5_2_fruit_conissour", -259, -274);
        wendarrPiousDevotion(tree, "wendarr_tl_6_0_pious_devotion", -161, -282);
        fruitConissour(tree, "wendarr_tl_6_1_fruit_conissour", -193, -255);
        heavilyEffected(tree, "wendarr_tl_6_2_heavily_effected", -221, -230);
        tree.minor("wendarr_tough_stomach", "Tough Stomach", "wendarr_tough_stomach", icon("health"), -124, -238);
        fruitConissour(tree, "wendarr_tl_7_1_fruit_conissour", -155, -211);
        tree.minor("wendarr_expert_eater", "Expert Eater", "wendarr_expert_eater", icon("copiously"), -184, -187);
        heavilyEffected(tree, "wendarr_tl_8_0_heavily_effected", -87, -195);
        fruitConissour(tree, "wendarr_tl_8_1_fruit_conissour", -118, -168);
        fruitConissour(tree, "wendarr_tl_8_2_fruit_conissour", -147, -143);
        fruitConissour(tree, "wendarr_tl_9_0_fruit_conissour", -50, -151);
        heavilyEffected(tree, "wendarr_tl_9_1_heavily_effected", -81, -124);
        wendarrPiousDevotion(tree, "wendarr_tl_9_2_pious_devotion", -110, -100);
        fruitConissour(tree, "wendarr_tl_10_0_fruit_conissour", -29, -94);
        tree.edge("wendarr_expert_eater", "wendarr_tl_6_2_heavily_effected");
        tree.edge("wendarr_expert_eater", "wendarr_tl_8_2_fruit_conissour");
        tree.edge("wendarr_pristine_condition", "wendarr_tl_1_1_pious_devotion");
        tree.edge("wendarr_pristine_condition", "wendarr_tl_3_1_heavily_effected");
        tree.edge("wendarr_tl_0_0_fruit_conissour", "wendarr_tl_0_1_fruit_conissour");
        tree.edge("wendarr_tl_0_0_fruit_conissour", "wendarr_tl_1_0_heavily_effected");
        tree.edge("wendarr_tl_0_1_fruit_conissour", "wendarr_tl_1_0_heavily_effected");
        tree.edge("wendarr_tl_0_1_fruit_conissour", "wendarr_tl_1_1_pious_devotion");
        tree.edge("wendarr_tl_10_0_fruit_conissour", "wendarr_tl_9_0_fruit_conissour");
        tree.edge("wendarr_tl_10_0_fruit_conissour", "wendarr_tl_9_1_heavily_effected");
        tree.edge("wendarr_tl_1_0_heavily_effected", "wendarr_tl_1_1_pious_devotion");
        tree.edge("wendarr_tl_1_0_heavily_effected", "wendarr_tl_2_0_fruit_conissour");
        tree.edge("wendarr_tl_1_1_pious_devotion", "wendarr_tl_2_0_fruit_conissour");
        tree.edge("wendarr_tl_2_0_fruit_conissour", "wendarr_tl_3_0_fruit_conissour");
        tree.edge("wendarr_tl_2_0_fruit_conissour", "wendarr_tl_3_1_heavily_effected");
        tree.edge("wendarr_tl_3_0_fruit_conissour", "wendarr_tl_4_0_pious_devotion");
        tree.edge("wendarr_tl_3_1_heavily_effected", "wendarr_tl_4_0_pious_devotion");
        tree.edge("wendarr_tl_3_1_heavily_effected", "wendarr_tl_4_1_fruit_conissour");
        tree.edge("wendarr_tl_4_0_pious_devotion", "wendarr_tl_5_1_heavily_effected");
        tree.edge("wendarr_tl_4_0_pious_devotion", "wendarr_tl_5_2_fruit_conissour");
        tree.edge("wendarr_tl_4_1_fruit_conissour", "wendarr_tl_5_2_fruit_conissour");
        tree.edge("wendarr_tl_5_0_fruit_conissour", "wendarr_tl_5_1_heavily_effected");
        tree.edge("wendarr_tl_5_0_fruit_conissour", "wendarr_tl_6_0_pious_devotion");
        tree.edge("wendarr_tl_5_1_heavily_effected", "wendarr_tl_5_2_fruit_conissour");
        tree.edge("wendarr_tl_5_1_heavily_effected", "wendarr_tl_6_1_fruit_conissour");
        tree.edge("wendarr_tl_5_2_fruit_conissour", "wendarr_tl_6_1_fruit_conissour");
        tree.edge("wendarr_tl_5_2_fruit_conissour", "wendarr_tl_6_2_heavily_effected");
        tree.edge("wendarr_tl_6_0_pious_devotion", "wendarr_tough_stomach");
        tree.edge("wendarr_tl_6_1_fruit_conissour", "wendarr_tl_7_1_fruit_conissour");
        tree.edge("wendarr_tl_7_1_fruit_conissour", "wendarr_tl_8_1_fruit_conissour");
        tree.edge("wendarr_tl_8_0_heavily_effected", "wendarr_tl_9_0_fruit_conissour");
        tree.edge("wendarr_tl_8_0_heavily_effected", "wendarr_tough_stomach");
        tree.edge("wendarr_tl_8_1_fruit_conissour", "wendarr_tl_9_1_heavily_effected");
        tree.edge("wendarr_tl_8_1_fruit_conissour", "wendarr_tl_9_2_pious_devotion");
        tree.edge("wendarr_tl_8_2_fruit_conissour", "wendarr_tl_9_2_pious_devotion");
        tree.edge("wendarr_tl_9_0_fruit_conissour", "wendarr_tl_9_1_heavily_effected");
        tree.edge("wendarr_tl_9_1_heavily_effected", "wendarr_tl_9_2_pious_devotion");
    }

    private static void harvest(GodTreeBuilder tree) {
        fruitConissour(tree, "wendarr_tr_0_0_fruit_conissour", 57, -459);
        fruitConissour(tree, "wendarr_tr_0_1_fruit_conissour", 57, -497);
        wendarrPiousDevotion(tree, "wendarr_tr_1_0_pious_devotion", 115, -459);
        masterImbuer(tree, "wendarr_tr_1_1_master_imbuer", 115, -497);
        masterImbuer(tree, "wendarr_tr_2_0_master_imbuer", 173, -459);
        tree.minor("wendarr_clock_artificier", "Clock Artificier", "wendarr_clock_artificier", icon("effect_duration"), 175, -497);
        fruitConissour(tree, "wendarr_tr_3_0_fruit_conissour", 237, -428);
        fruitConissour(tree, "wendarr_tr_3_1_fruit_conissour", 265, -453);
        fruitConissour(tree, "wendarr_tr_4_0_fruit_conissour", 259, -360);
        wendarrPiousDevotion(tree, "wendarr_tr_4_1_pious_devotion", 297, -358);
        fruitConissour(tree, "wendarr_tr_5_0_fruit_conissour", 198, -325);
        masterImbuer(tree, "wendarr_tr_5_1_master_imbuer", 230, -299);
        fruitConissour(tree, "wendarr_tr_5_2_fruit_conissour", 259, -274);
        masterImbuer(tree, "wendarr_tr_6_0_master_imbuer", 161, -282);
        fruitConissour(tree, "wendarr_tr_6_1_fruit_conissour", 193, -255);
        wendarrPiousDevotion(tree, "wendarr_tr_6_2_pious_devotion", 221, -230);
        tree.minor("wendarr_glutton", "Glutton", "wendarr_glutton", icon("fruit_effectiveness"), 124, -238);
        fruitConissour(tree, "wendarr_tr_7_1_fruit_conissour", 155, -211);
        tree.minor("wendarr_fruity", "Fruity", "wendarr_fruity", icon("fruit_effectiveness"), 184, -187);
        fruitConissour(tree, "wendarr_tr_8_0_fruit_conissour", 87, -195);
        masterImbuer(tree, "wendarr_tr_8_1_master_imbuer", 118, -168);
        heavilyEffected(tree, "wendarr_tr_8_2_heavily_effected", 147, -143);
        wendarrPiousDevotion(tree, "wendarr_tr_9_0_pious_devotion", 50, -151);
        fruitConissour(tree, "wendarr_tr_9_1_fruit_conissour", 81, -124);
        masterImbuer(tree, "wendarr_tr_9_2_master_imbuer", 110, -100);
        fruitConissour(tree, "wendarr_tr_10_0_fruit_conissour", 29, -94);
        tree.edge("wendarr_clock_artificier", "wendarr_tr_1_1_master_imbuer");
        tree.edge("wendarr_clock_artificier", "wendarr_tr_3_1_fruit_conissour");
        tree.edge("wendarr_fruity", "wendarr_tr_6_2_pious_devotion");
        tree.edge("wendarr_fruity", "wendarr_tr_8_2_heavily_effected");
        tree.edge("wendarr_glutton", "wendarr_tr_6_0_master_imbuer");
        tree.edge("wendarr_glutton", "wendarr_tr_8_0_fruit_conissour");
        tree.edge("wendarr_tr_0_0_fruit_conissour", "wendarr_tr_0_1_fruit_conissour");
        tree.edge("wendarr_tr_0_0_fruit_conissour", "wendarr_tr_1_0_pious_devotion");
        tree.edge("wendarr_tr_0_0_fruit_conissour", "wendarr_tr_1_1_master_imbuer");
        tree.edge("wendarr_tr_0_1_fruit_conissour", "wendarr_tr_1_1_master_imbuer");
        tree.edge("wendarr_tr_10_0_fruit_conissour", "wendarr_tr_9_0_pious_devotion");
        tree.edge("wendarr_tr_10_0_fruit_conissour", "wendarr_tr_9_1_fruit_conissour");
        tree.edge("wendarr_tr_1_0_pious_devotion", "wendarr_tr_1_1_master_imbuer");
        tree.edge("wendarr_tr_1_0_pious_devotion", "wendarr_tr_2_0_master_imbuer");
        tree.edge("wendarr_tr_1_1_master_imbuer", "wendarr_tr_2_0_master_imbuer");
        tree.edge("wendarr_tr_2_0_master_imbuer", "wendarr_tr_3_0_fruit_conissour");
        tree.edge("wendarr_tr_2_0_master_imbuer", "wendarr_tr_3_1_fruit_conissour");
        tree.edge("wendarr_tr_3_0_fruit_conissour", "wendarr_tr_4_0_fruit_conissour");
        tree.edge("wendarr_tr_3_1_fruit_conissour", "wendarr_tr_4_0_fruit_conissour");
        tree.edge("wendarr_tr_3_1_fruit_conissour", "wendarr_tr_4_1_pious_devotion");
        tree.edge("wendarr_tr_4_0_fruit_conissour", "wendarr_tr_5_1_master_imbuer");
        tree.edge("wendarr_tr_4_0_fruit_conissour", "wendarr_tr_5_2_fruit_conissour");
        tree.edge("wendarr_tr_4_1_pious_devotion", "wendarr_tr_5_2_fruit_conissour");
        tree.edge("wendarr_tr_5_0_fruit_conissour", "wendarr_tr_5_1_master_imbuer");
        tree.edge("wendarr_tr_5_0_fruit_conissour", "wendarr_tr_6_0_master_imbuer");
        tree.edge("wendarr_tr_5_1_master_imbuer", "wendarr_tr_5_2_fruit_conissour");
        tree.edge("wendarr_tr_5_1_master_imbuer", "wendarr_tr_6_1_fruit_conissour");
        tree.edge("wendarr_tr_5_2_fruit_conissour", "wendarr_tr_6_1_fruit_conissour");
        tree.edge("wendarr_tr_5_2_fruit_conissour", "wendarr_tr_6_2_pious_devotion");
        tree.edge("wendarr_tr_6_1_fruit_conissour", "wendarr_tr_7_1_fruit_conissour");
        tree.edge("wendarr_tr_7_1_fruit_conissour", "wendarr_tr_8_1_master_imbuer");
        tree.edge("wendarr_tr_8_0_fruit_conissour", "wendarr_tr_9_0_pious_devotion");
        tree.edge("wendarr_tr_8_1_master_imbuer", "wendarr_tr_9_1_fruit_conissour");
        tree.edge("wendarr_tr_8_1_master_imbuer", "wendarr_tr_9_2_master_imbuer");
        tree.edge("wendarr_tr_8_2_heavily_effected", "wendarr_tr_9_2_master_imbuer");
        tree.edge("wendarr_tr_9_0_pious_devotion", "wendarr_tr_9_1_fruit_conissour");
        tree.edge("wendarr_tr_9_1_fruit_conissour", "wendarr_tr_9_2_master_imbuer");
    }

    private static void runner(GodTreeBuilder tree) {
        speedy(tree, "wendarr_bl_0_0_speedy", -57, 439);
        speedy(tree, "wendarr_bl_0_1_speedy", -57, 477);
        wendarrPiousDevotion(tree, "wendarr_bl_1_0_pious_devotion", -115, 459);
        speedyCaster(tree, "wendarr_bl_1_1_speedy_caster", -115, 497);
        speedyCaster(tree, "wendarr_bl_2_0_speedy_caster", -173, 459);
        tree.minor("wendarr_efficient_steps", "Efficient Steps", "wendarr_efficient_steps", icon("movement_speed"), -175, 497);
        speedy(tree, "wendarr_bl_3_0_speedy", -237, 428);
        speedy(tree, "wendarr_bl_3_1_speedy", -265, 453);
        speedy(tree, "wendarr_bl_4_0_speedy", -259, 360);
        wendarrPiousDevotion(tree, "wendarr_bl_4_1_pious_devotion", -297, 358);
        speedy(tree, "wendarr_bl_5_0_speedy", -198, 325);
        speedyCaster(tree, "wendarr_bl_5_1_speedy_caster", -230, 299);
        speedy(tree, "wendarr_bl_5_2_speedy", -259, 274);
        wendarrPiousDevotion(tree, "wendarr_bl_6_0_pious_devotion", -161, 282);
        speedy(tree, "wendarr_bl_6_1_speedy", -193, 255);
        speedyCaster(tree, "wendarr_bl_6_2_speedy_caster", -221, 230);
        tree.minor("wendarr_temporal_shielding", "Temporal Shielding", "wendarr_temporal_shielding", icon("resistance"), -124, 238);
        speedy(tree, "wendarr_bl_7_1_speedy", -155, 211);
        tree.minor("wendarr_paced_strikes", "Paced Strikes", "wendarr_paced_strikes", icon("double_damage"), -184, 187);
        speedyCaster(tree, "wendarr_bl_8_0_speedy_caster", -87, 195);
        speedy(tree, "wendarr_bl_8_1_speedy", -118, 168);
        speedy(tree, "wendarr_bl_8_2_speedy", -147, 143);
        speedy(tree, "wendarr_bl_9_0_speedy", -50, 151);
        speedyCaster(tree, "wendarr_bl_9_1_speedy_caster", -81, 124);
        wendarrPiousDevotion(tree, "wendarr_bl_9_2_pious_devotion", -110, 100);
        speedy(tree, "wendarr_bl_10_0_speedy", -29, 94);
        tree.edge("wendarr_bl_0_0_speedy", "wendarr_bl_0_1_speedy");
        tree.edge("wendarr_bl_0_0_speedy", "wendarr_bl_1_0_pious_devotion");
        tree.edge("wendarr_bl_0_1_speedy", "wendarr_bl_1_0_pious_devotion");
        tree.edge("wendarr_bl_0_1_speedy", "wendarr_bl_1_1_speedy_caster");
        tree.edge("wendarr_bl_10_0_speedy", "wendarr_bl_9_0_speedy");
        tree.edge("wendarr_bl_10_0_speedy", "wendarr_bl_9_1_speedy_caster");
        tree.edge("wendarr_bl_1_0_pious_devotion", "wendarr_bl_1_1_speedy_caster");
        tree.edge("wendarr_bl_1_0_pious_devotion", "wendarr_bl_2_0_speedy_caster");
        tree.edge("wendarr_bl_1_1_speedy_caster", "wendarr_bl_2_0_speedy_caster");
        tree.edge("wendarr_bl_1_1_speedy_caster", "wendarr_efficient_steps");
        tree.edge("wendarr_bl_2_0_speedy_caster", "wendarr_bl_3_0_speedy");
        tree.edge("wendarr_bl_2_0_speedy_caster", "wendarr_bl_3_1_speedy");
        tree.edge("wendarr_bl_3_0_speedy", "wendarr_bl_4_0_speedy");
        tree.edge("wendarr_bl_3_1_speedy", "wendarr_bl_4_0_speedy");
        tree.edge("wendarr_bl_3_1_speedy", "wendarr_bl_4_1_pious_devotion");
        tree.edge("wendarr_bl_3_1_speedy", "wendarr_efficient_steps");
        tree.edge("wendarr_bl_4_0_speedy", "wendarr_bl_5_1_speedy_caster");
        tree.edge("wendarr_bl_4_0_speedy", "wendarr_bl_5_2_speedy");
        tree.edge("wendarr_bl_4_1_pious_devotion", "wendarr_bl_5_2_speedy");
        tree.edge("wendarr_bl_5_0_speedy", "wendarr_bl_5_1_speedy_caster");
        tree.edge("wendarr_bl_5_0_speedy", "wendarr_bl_6_0_pious_devotion");
        tree.edge("wendarr_bl_5_1_speedy_caster", "wendarr_bl_5_2_speedy");
        tree.edge("wendarr_bl_5_1_speedy_caster", "wendarr_bl_6_1_speedy");
        tree.edge("wendarr_bl_5_2_speedy", "wendarr_bl_6_1_speedy");
        tree.edge("wendarr_bl_5_2_speedy", "wendarr_bl_6_2_speedy_caster");
        tree.edge("wendarr_bl_6_0_pious_devotion", "wendarr_temporal_shielding");
        tree.edge("wendarr_bl_6_1_speedy", "wendarr_bl_7_1_speedy");
        tree.edge("wendarr_bl_6_2_speedy_caster", "wendarr_paced_strikes");
        tree.edge("wendarr_bl_7_1_speedy", "wendarr_bl_8_1_speedy");
        tree.edge("wendarr_bl_8_0_speedy_caster", "wendarr_bl_9_0_speedy");
        tree.edge("wendarr_bl_8_0_speedy_caster", "wendarr_temporal_shielding");
        tree.edge("wendarr_bl_8_1_speedy", "wendarr_bl_9_1_speedy_caster");
        tree.edge("wendarr_bl_8_1_speedy", "wendarr_bl_9_2_pious_devotion");
        tree.edge("wendarr_bl_8_2_speedy", "wendarr_bl_9_2_pious_devotion");
        tree.edge("wendarr_bl_8_2_speedy", "wendarr_paced_strikes");
        tree.edge("wendarr_bl_9_0_speedy", "wendarr_bl_9_1_speedy_caster");
        tree.edge("wendarr_bl_9_1_speedy_caster", "wendarr_bl_9_2_pious_devotion");
    }

    private static void clockwork(GodTreeBuilder tree) {
        speedyCaster(tree, "wendarr_br_0_0_speedy_caster", 57, 439);
        speedyCaster(tree, "wendarr_br_0_1_speedy_caster", 57, 477);
        wendarrPiousDevotion(tree, "wendarr_br_1_0_pious_devotion", 115, 459);
        heavilyEffected(tree, "wendarr_br_1_1_heavily_effected", 115, 497);
        heavilyEffected(tree, "wendarr_br_2_0_heavily_effected", 173, 459);
        tree.disabledMinor("wendarr_armored_extractors", "Armored Extractors", "wendarr_armored_extractors", icon("armour"), 175, 497);
        extractionSuperviser(tree, "wendarr_br_3_0_extraction_superviser", 237, 428);
        speedyCaster(tree, "wendarr_br_3_1_speedy_caster", 265, 453);
        extractionSuperviser(tree, "wendarr_br_4_0_extraction_superviser", 259, 360);
        wendarrPiousDevotion(tree, "wendarr_br_4_1_pious_devotion", 297, 358);
        speedyCaster(tree, "wendarr_br_5_0_speedy_caster", 198, 325);
        heavilyEffected(tree, "wendarr_br_5_1_heavily_effected", 230, 299);
        speedyCaster(tree, "wendarr_br_5_2_speedy_caster", 259, 274);
        speedyCaster(tree, "wendarr_br_6_0_speedy_caster", 161, 282);
        wendarrPiousDevotion(tree, "wendarr_br_6_1_pious_devotion", 193, 255);
        heavilyEffected(tree, "wendarr_br_6_2_heavily_effected", 221, 230);
        tree.minor("wendarr_temporal_breaking", "Temporal Breaking", "wendarr_temporal_breaking", icon("stun_chance"), 124, 238);
        speedyCaster(tree, "wendarr_br_7_1_speedy_caster", 155, 211);
        tree.minor("wendarr_extender", "Extender", "wendarr_extender", icon("utility"), 184, 187);
        heavilyEffected(tree, "wendarr_br_8_0_heavily_effected", 87, 195);
        speedyCaster(tree, "wendarr_br_8_1_speedy_caster", 118, 168);
        speedyCaster(tree, "wendarr_br_8_2_speedy_caster", 147, 143);
        speedyCaster(tree, "wendarr_br_9_0_speedy_caster", 50, 151);
        heavilyEffected(tree, "wendarr_br_9_1_heavily_effected", 81, 124);
        wendarrPiousDevotion(tree, "wendarr_br_9_2_pious_devotion", 110, 100);
        speedyCaster(tree, "wendarr_br_10_0_speedy_caster", 29, 94);
        tree.edge("wendarr_armored_extractors", "wendarr_br_1_1_heavily_effected");
        tree.edge("wendarr_armored_extractors", "wendarr_br_3_1_speedy_caster");
        tree.edge("wendarr_br_0_0_speedy_caster", "wendarr_br_0_1_speedy_caster");
        tree.edge("wendarr_br_0_0_speedy_caster", "wendarr_br_1_0_pious_devotion");
        tree.edge("wendarr_br_0_1_speedy_caster", "wendarr_br_1_0_pious_devotion");
        tree.edge("wendarr_br_0_1_speedy_caster", "wendarr_br_1_1_heavily_effected");
        tree.edge("wendarr_br_10_0_speedy_caster", "wendarr_br_9_0_speedy_caster");
        tree.edge("wendarr_br_10_0_speedy_caster", "wendarr_br_9_1_heavily_effected");
        tree.edge("wendarr_br_1_0_pious_devotion", "wendarr_br_1_1_heavily_effected");
        tree.edge("wendarr_br_1_0_pious_devotion", "wendarr_br_2_0_heavily_effected");
        tree.edge("wendarr_br_1_1_heavily_effected", "wendarr_br_2_0_heavily_effected");
        tree.edge("wendarr_br_2_0_heavily_effected", "wendarr_br_3_0_extraction_superviser");
        tree.edge("wendarr_br_2_0_heavily_effected", "wendarr_br_3_1_speedy_caster");
        tree.edge("wendarr_br_3_0_extraction_superviser", "wendarr_br_4_0_extraction_superviser");
        tree.edge("wendarr_br_3_1_speedy_caster", "wendarr_br_4_0_extraction_superviser");
        tree.edge("wendarr_br_3_1_speedy_caster", "wendarr_br_4_1_pious_devotion");
        tree.edge("wendarr_br_4_0_extraction_superviser", "wendarr_br_5_1_heavily_effected");
        tree.edge("wendarr_br_4_0_extraction_superviser", "wendarr_br_5_2_speedy_caster");
        tree.edge("wendarr_br_4_1_pious_devotion", "wendarr_br_5_2_speedy_caster");
        tree.edge("wendarr_br_5_0_speedy_caster", "wendarr_br_5_1_heavily_effected");
        tree.edge("wendarr_br_5_0_speedy_caster", "wendarr_br_6_0_speedy_caster");
        tree.edge("wendarr_br_5_1_heavily_effected", "wendarr_br_5_2_speedy_caster");
        tree.edge("wendarr_br_5_1_heavily_effected", "wendarr_br_6_1_pious_devotion");
        tree.edge("wendarr_br_5_2_speedy_caster", "wendarr_br_6_1_pious_devotion");
        tree.edge("wendarr_br_5_2_speedy_caster", "wendarr_br_6_2_heavily_effected");
        tree.edge("wendarr_br_6_0_speedy_caster", "wendarr_temporal_breaking");
        tree.edge("wendarr_br_6_1_pious_devotion", "wendarr_br_7_1_speedy_caster");
        tree.edge("wendarr_br_6_2_heavily_effected", "wendarr_extender");
        tree.edge("wendarr_br_7_1_speedy_caster", "wendarr_br_8_1_speedy_caster");
        tree.edge("wendarr_br_8_0_heavily_effected", "wendarr_br_9_0_speedy_caster");
        tree.edge("wendarr_br_8_0_heavily_effected", "wendarr_temporal_breaking");
        tree.edge("wendarr_br_8_1_speedy_caster", "wendarr_br_9_1_heavily_effected");
        tree.edge("wendarr_br_8_1_speedy_caster", "wendarr_br_9_2_pious_devotion");
        tree.edge("wendarr_br_8_2_speedy_caster", "wendarr_br_9_2_pious_devotion");
        tree.edge("wendarr_br_8_2_speedy_caster", "wendarr_extender");
        tree.edge("wendarr_br_9_0_speedy_caster", "wendarr_br_9_1_heavily_effected");
        tree.edge("wendarr_br_9_1_heavily_effected", "wendarr_br_9_2_pious_devotion");
    }

    private static void swift(GodTreeBuilder tree) {
        tree.root("wendarr_start_swift", "The Swift", 377, -168);
        speedy(tree, "wendarr_swift_b1_speedy", 420, -187);
        speedy(tree, "wendarr_swift_wl1_speedy", 447, -153);
        speedyCaster(tree, "wendarr_swift_wr1_speedy_caster", 413, -229);
        speedyCaster(tree, "wendarr_swift_head_speedy_caster", 466, -207);
        speedyCaster(tree, "wendarr_swift_wl2_speedy_caster", 489, -125);
        speedy(tree, "wendarr_swift_wr2_speedy", 420, -280);
        tree.minor("wendarr_speed_demon", "Speed Demon", "wendarr_speed_demon", icon("attack_speed"), 528, -103);
        tree.minor("wendarr_quick_search", "Quick Search", "wendarr_quick_search", icon("door_hunter"), 430, -323);
        tree.edge("wendarr_quick_search", "wendarr_swift_wr2_speedy");
        tree.edge("wendarr_speed_demon", "wendarr_swift_wl2_speedy_caster");
        tree.edge("wendarr_start_swift", "wendarr_swift_b1_speedy");
        tree.edge("wendarr_swift_b1_speedy", "wendarr_swift_head_speedy_caster");
        tree.edge("wendarr_swift_b1_speedy", "wendarr_swift_wl1_speedy");
        tree.edge("wendarr_swift_b1_speedy", "wendarr_swift_wr1_speedy_caster");
        tree.edge("wendarr_swift_head_speedy_caster", "wendarr_swift_wl1_speedy");
        tree.edge("wendarr_swift_head_speedy_caster", "wendarr_swift_wr1_speedy_caster");
        tree.edge("wendarr_swift_wl1_speedy", "wendarr_swift_wl2_speedy_caster");
        tree.edge("wendarr_swift_wr1_speedy_caster", "wendarr_swift_wr2_speedy");
    }

    private static void deck(GodTreeBuilder tree) {
        tree.root("wendarr_start_deck", "The Deck", 377, 168);
        fruitConissour(tree, "wendarr_deck_c0_fruit_conissour", 410, 183);
        heavilyEffected(tree, "wendarr_deck_cl1_heavily_effected", 394, 215);
        fruitConissour(tree, "wendarr_deck_cr1_fruit_conissour", 423, 149);
        heavilyEffected(tree, "wendarr_deck_c1_heavily_effected", 461, 205);
        fruitConissour(tree, "wendarr_deck_cl2_fruit_conissour", 404, 263);
        heavilyEffected(tree, "wendarr_deck_cr2_heavily_effected", 465, 124);
        tree.minor("wendarr_plushie_lover", "Plushie Lover", "wendarr_plushie_lover", icon("taunt_charm"), 420, 307);
        tree.minor("wendarr_the_deckless", "The Deckless", "wendarr_the_deckless", icon("chaos_cube"), 509, 106);
        tree.edge("wendarr_deck_c0_fruit_conissour", "wendarr_deck_c1_heavily_effected");
        tree.edge("wendarr_deck_c0_fruit_conissour", "wendarr_deck_cl1_heavily_effected");
        tree.edge("wendarr_deck_c0_fruit_conissour", "wendarr_deck_cr1_fruit_conissour");
        tree.edge("wendarr_deck_c0_fruit_conissour", "wendarr_start_deck");
        tree.edge("wendarr_deck_c1_heavily_effected", "wendarr_deck_cl1_heavily_effected");
        tree.edge("wendarr_deck_c1_heavily_effected", "wendarr_deck_cr1_fruit_conissour");
        tree.edge("wendarr_deck_cl1_heavily_effected", "wendarr_deck_cl2_fruit_conissour");
        tree.edge("wendarr_deck_cl2_fruit_conissour", "wendarr_plushie_lover");
        tree.edge("wendarr_deck_cr1_fruit_conissour", "wendarr_deck_cr2_heavily_effected");
        tree.edge("wendarr_deck_cr2_heavily_effected", "wendarr_the_deckless");
    }

    private static void spanner(GodTreeBuilder tree) {
        tree.root("wendarr_start_spanner", "The Spanner", -377, -168);
        masterImbuer(tree, "wendarr_spanner_h1_master_imbuer", -418, -186);
        wendarrPiousDevotion(tree, "wendarr_spanner_h2_pious_devotion", -458, -204);
        masterImbuer(tree, "wendarr_spanner_jl_master_imbuer", -472, -246);
        masterImbuer(tree, "wendarr_spanner_jr_master_imbuer", -499, -187);
        tree.minor("wendarr_pylon_whisperer", "Pylon Whisperer", "wendarr_pylon_whisperer", icon("totem"), -515, -229);
        tree.edge("wendarr_pylon_whisperer", "wendarr_spanner_jl_master_imbuer");
        tree.edge("wendarr_pylon_whisperer", "wendarr_spanner_jr_master_imbuer");
        tree.edge("wendarr_spanner_h1_master_imbuer", "wendarr_spanner_h2_pious_devotion");
        tree.edge("wendarr_spanner_h1_master_imbuer", "wendarr_start_spanner");
        tree.edge("wendarr_spanner_h2_pious_devotion", "wendarr_spanner_jl_master_imbuer");
        tree.edge("wendarr_spanner_h2_pious_devotion", "wendarr_spanner_jr_master_imbuer");
        tree.edge("wendarr_spanner_jl_master_imbuer", "wendarr_spanner_jr_master_imbuer");
    }

    private static void bough(GodTreeBuilder tree) {
        tree.root("wendarr_start_bough", "The Bough", -377, 168);
        fruitConissour(tree, "wendarr_bough_b1_fruit_conissour", -418, 186);
        fruitConissour(tree, "wendarr_bough_ll_fruit_conissour", -461, 166);
        wendarrPiousDevotion(tree, "wendarr_bough_lr_pious_devotion", -427, 229);
        fruitConissour(tree, "wendarr_bough_b2_fruit_conissour", -474, 211);
        tree.minor("wendarr_gardener", "Gardener", "wendarr_gardener", icon("item_quantity"), -509, 226);
        tree.edge("wendarr_bough_b1_fruit_conissour", "wendarr_bough_ll_fruit_conissour");
        tree.edge("wendarr_bough_b1_fruit_conissour", "wendarr_bough_lr_pious_devotion");
        tree.edge("wendarr_bough_b1_fruit_conissour", "wendarr_start_bough");
        tree.edge("wendarr_bough_b2_fruit_conissour", "wendarr_bough_ll_fruit_conissour");
        tree.edge("wendarr_bough_b2_fruit_conissour", "wendarr_bough_lr_pious_devotion");
        tree.edge("wendarr_bough_b2_fruit_conissour", "wendarr_gardener");
    }

    private static void wendarrBridges(GodTreeBuilder tree) {
        tree.edge("wendarr_bl_0_0_speedy", "wendarr_start_bot");
        tree.edge("wendarr_bl_0_1_speedy", "wendarr_br_0_1_speedy_caster");
        tree.edge("wendarr_bl_0_1_speedy", "wendarr_start_bot");
        tree.edge("wendarr_bl_10_0_speedy", "wendarr_edge_of_time");
        tree.edge("wendarr_bl_4_1_pious_devotion", "wendarr_fr_pl_master_imbuer");
        tree.edge("wendarr_bl_9_0_speedy", "wendarr_br_10_0_speedy_caster");
        tree.edge("wendarr_br_0_0_speedy_caster", "wendarr_start_bot");
        tree.edge("wendarr_br_0_1_speedy_caster", "wendarr_start_bot");
        tree.edge("wendarr_br_10_0_speedy_caster", "wendarr_edge_of_time");
        tree.edge("wendarr_br_4_1_pious_devotion", "wendarr_fr_pr_master_imbuer");
        tree.edge("wendarr_fr_pl_master_imbuer", "wendarr_tl_4_1_fruit_conissour");
        tree.edge("wendarr_fr_pr_master_imbuer", "wendarr_tr_4_1_pious_devotion");
        tree.edge("wendarr_legend_of_the_pear", "wendarr_tl_10_0_fruit_conissour");
        tree.edge("wendarr_legend_of_the_pear", "wendarr_tr_10_0_fruit_conissour");
        tree.edge("wendarr_start_top", "wendarr_tl_0_0_fruit_conissour");
        tree.edge("wendarr_start_top", "wendarr_tl_0_1_fruit_conissour");
        tree.edge("wendarr_start_top", "wendarr_tr_0_0_fruit_conissour");
        tree.edge("wendarr_start_top", "wendarr_tr_0_1_fruit_conissour");
        tree.edge("wendarr_tl_0_1_fruit_conissour", "wendarr_tr_0_1_fruit_conissour");
        tree.edge("wendarr_tl_9_0_fruit_conissour", "wendarr_tr_10_0_fruit_conissour");
    }

    private static void fruitConissour(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Fruit Conissour", "wendarr_fruit_conissour", icon("fruit_effectiveness"), x, y);
    }

    private static void speedy(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Speedy", "wendarr_speedy", icon("movement_speed"), x, y);
    }

    private static void heavilyEffected(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Heavily Effected", "wendarr_heavily_effected", icon("effect_duration"), x, y);
    }

    private static void masterImbuer(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Master Imbuer", "wendarr_master_imbuer", icon("imbuement_change"), x, y);
    }

    private static void speedyCaster(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Speedy Caster", "wendarr_speedy_caster", icon("cooldown_reduction"), x, y);
    }

    private static void wendarrPiousDevotion(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Pious Devotion", "wendarr_pious_devotion", icon("smite"), x, y);
    }

    private static void extractionSuperviser(GodTreeBuilder tree, String id, int x, int y) {
        tree.disabledStat(id, "Extraction Superviser", "wendarr_extraction_superviser", icon("mining_speed"), x, y);
    }

    public static GodTreeBuilder velara() {
        GodTreeBuilder tree = new GodTreeBuilder("velara");
        wreathFrame(tree);
        bulwark(tree);
        mercy(tree);
        fern(tree);
        briar(tree);
        bloom(tree);
        sapling(tree);
        velaraBridges(tree);
        tree.label("The Bulwark", 113, 860);
        tree.label("The Mercy", -113, -860);
        tree.label("The Fern", 0, -520);
        tree.label("The Briar", -144, 360);
        tree.label("The Bloom", -274, 231);
        tree.label("The Sapling", 396, 250);
        return tree;
    }

    private static void wreathFrame(GodTreeBuilder tree) {
        tree.root("velara_start_bulwark", "The Bulwark", 666, 0);
        tree.major("velara_immortal", "Immortal", "velara_immortal", icon("defensive"), -643, 172);
        tree.root("velara_start_mercy", "The Mercy", -666, 0);
        tree.major("velara_sacrifice", "Sacrifice", "velara_sacrifice", icon("taunt"), 643, -172);
    }

    private static void bulwark(GodTreeBuilder tree) {
        armored(tree, "velara_east_0_0_armored", 725, 177);
        tree.minor("velara_magic_armor", "Magic Armor", "velara_magic_armor", icon("mana_barrier"), 788, 193);
        guarded(tree, "velara_east_1_0_guarded", 576, 300);
        armored(tree, "velara_east_1_1_armored", 662, 345);
        velaraPiousDevotion(tree, "velara_east_1_2_pious_devotion", 720, 375);
        thorny(tree, "velara_east_2_0_thorny", 403, 354);
        velaraPiousDevotion(tree, "velara_east_2_1_pious_devotion", 460, 404);
        tough(tree, "velara_east_2_2_tough", 533, 468);
        tough(tree, "velara_east_2_3_tough", 582, 510);
        tree.minor("velara_the_stonewall", "The Stonewall", "velara_the_stonewall", icon("stonefall"), 286, 409);
        armored(tree, "velara_east_3_1_armored", 330, 471);
        guarded(tree, "velara_east_3_2_guarded", 385, 550);
        tree.minor("velara_cactus", "Cactus", "velara_cactus", icon("shell_quill"), 422, 603);
        thornyII(tree, "velara_east_4_0_thorny_ii", 181, 465);
        velaraPiousDevotion(tree, "velara_east_4_1_pious_devotion", 208, 536);
        armored(tree, "velara_east_4_2_armored", 243, 626);
        armored(tree, "velara_east_4_3_armored", 267, 687);
        armoredII(tree, "velara_east_5_0_armored_ii", 80, 607);
        toughII(tree, "velara_east_5_1_tough_ii", 93, 703);
        guardedII(tree, "velara_east_5_2_guarded_ii", 101, 767);
        thornyII(tree, "velara_east_6_0_thorny_ii", -62, 570);
        armoredII(tree, "velara_east_6_1_armored_ii", -71, 646);
        velaraPiousDevotion(tree, "velara_east_6_2_pious_devotion", -81, 742);
        tree.minor("velara_steadfast", "Steadfast", "velara_steadfast", icon("knockback_resistance"), -88, 807);
        armoredII(tree, "velara_east_7_0_armored_ii", -196, 539);
        guardedII(tree, "velara_east_7_1_guarded_ii", -222, 610);
        armoredII(tree, "velara_east_7_2_armored_ii", -255, 702);
        toughII(tree, "velara_east_7_3_tough_ii", -278, 762);
        tree.minor("velara_adaptive_armor", "Adaptive Armor", "velara_adaptive_armor", icon("resistance"), -298, 446);
        velaraPiousDevotion(tree, "velara_east_8_1_pious_devotion", -340, 509);
        armoredII(tree, "velara_east_8_2_armored_ii", -394, 590);
        tree.minor("velara_fleeting_physicality", "Fleeting Physicality", "velara_fleeting_physicality", icon("mana_shield_implode"), -430, 644);
        thornyII(tree, "velara_east_9_0_thorny_ii", -368, 337);
        armoredII(tree, "velara_east_9_1_armored_ii", -424, 388);
        guardedII(tree, "velara_east_9_2_guarded_ii", -495, 454);
        velaraPiousDevotion(tree, "velara_east_9_3_pious_devotion", -543, 498);
        armoredII(tree, "velara_east_10_0_armored_ii", -504, 276);
        velaraPiousDevotion(tree, "velara_east_10_1_pious_devotion", -589, 323);
        tree.edge("velara_adaptive_armor", "velara_east_7_0_armored_ii");
        tree.edge("velara_adaptive_armor", "velara_east_9_0_thorny_ii");
        tree.edge("velara_cactus", "velara_east_2_3_tough");
        tree.edge("velara_cactus", "velara_east_4_3_armored");
        tree.edge("velara_east_0_0_armored", "velara_east_1_1_armored");
        tree.edge("velara_east_0_0_armored", "velara_east_1_2_pious_devotion");
        tree.edge("velara_east_10_0_armored_ii", "velara_east_10_1_pious_devotion");
        tree.edge("velara_east_10_0_armored_ii", "velara_east_9_0_thorny_ii");
        tree.edge("velara_east_10_0_armored_ii", "velara_east_9_1_armored_ii");
        tree.edge("velara_east_10_1_pious_devotion", "velara_east_9_2_guarded_ii");
        tree.edge("velara_east_10_1_pious_devotion", "velara_east_9_3_pious_devotion");
        tree.edge("velara_east_1_0_guarded", "velara_east_1_1_armored");
        tree.edge("velara_east_1_0_guarded", "velara_east_2_0_thorny");
        tree.edge("velara_east_1_0_guarded", "velara_east_2_1_pious_devotion");
        tree.edge("velara_east_1_1_armored", "velara_east_1_2_pious_devotion");
        tree.edge("velara_east_1_1_armored", "velara_east_2_2_tough");
        tree.edge("velara_east_1_2_pious_devotion", "velara_east_2_2_tough");
        tree.edge("velara_east_1_2_pious_devotion", "velara_east_2_3_tough");
        tree.edge("velara_east_1_2_pious_devotion", "velara_magic_armor");
        tree.edge("velara_east_2_0_thorny", "velara_east_3_1_armored");
        tree.edge("velara_east_2_0_thorny", "velara_the_stonewall");
        tree.edge("velara_east_2_1_pious_devotion", "velara_east_3_1_armored");
        tree.edge("velara_east_2_2_tough", "velara_east_3_2_guarded");
        tree.edge("velara_east_3_1_armored", "velara_east_4_0_thorny_ii");
        tree.edge("velara_east_3_1_armored", "velara_east_4_1_pious_devotion");
        tree.edge("velara_east_3_2_guarded", "velara_east_4_2_armored");
        tree.edge("velara_east_4_0_thorny_ii", "velara_east_5_0_armored_ii");
        tree.edge("velara_east_4_0_thorny_ii", "velara_the_stonewall");
        tree.edge("velara_east_4_1_pious_devotion", "velara_east_5_0_armored_ii");
        tree.edge("velara_east_4_2_armored", "velara_east_5_1_tough_ii");
        tree.edge("velara_east_4_2_armored", "velara_east_5_2_guarded_ii");
        tree.edge("velara_east_4_3_armored", "velara_east_5_2_guarded_ii");
        tree.edge("velara_east_5_0_armored_ii", "velara_east_5_1_tough_ii");
        tree.edge("velara_east_5_0_armored_ii", "velara_east_6_0_thorny_ii");
        tree.edge("velara_east_5_0_armored_ii", "velara_east_6_1_armored_ii");
        tree.edge("velara_east_5_1_tough_ii", "velara_east_5_2_guarded_ii");
        tree.edge("velara_east_5_1_tough_ii", "velara_east_6_2_pious_devotion");
        tree.edge("velara_east_5_2_guarded_ii", "velara_east_6_2_pious_devotion");
        tree.edge("velara_east_5_2_guarded_ii", "velara_steadfast");
        tree.edge("velara_east_6_0_thorny_ii", "velara_east_7_0_armored_ii");
        tree.edge("velara_east_6_0_thorny_ii", "velara_east_7_1_guarded_ii");
        tree.edge("velara_east_6_1_armored_ii", "velara_east_7_1_guarded_ii");
        tree.edge("velara_east_6_2_pious_devotion", "velara_east_7_2_armored_ii");
        tree.edge("velara_east_6_2_pious_devotion", "velara_east_7_3_tough_ii");
        tree.edge("velara_east_7_1_guarded_ii", "velara_east_8_1_pious_devotion");
        tree.edge("velara_east_7_2_armored_ii", "velara_east_8_2_armored_ii");
        tree.edge("velara_east_7_3_tough_ii", "velara_east_8_2_armored_ii");
        tree.edge("velara_east_7_3_tough_ii", "velara_fleeting_physicality");
        tree.edge("velara_east_7_3_tough_ii", "velara_steadfast");
        tree.edge("velara_east_8_1_pious_devotion", "velara_east_9_1_armored_ii");
        tree.edge("velara_east_8_2_armored_ii", "velara_east_9_2_guarded_ii");
        tree.edge("velara_east_8_2_armored_ii", "velara_east_9_3_pious_devotion");
        tree.edge("velara_east_9_3_pious_devotion", "velara_fleeting_physicality");
    }

    private static void mercy(GodTreeBuilder tree) {
        healthy(tree, "velara_west_0_0_healthy", -725, -177);
        immune(tree, "velara_west_0_1_immune", -788, -193);
        fastReflexes(tree, "velara_west_1_0_fast_reflexes", -576, -300);
        healthy(tree, "velara_west_1_1_healthy", -662, -345);
        velaraPiousDevotion(tree, "velara_west_1_2_pious_devotion", -720, -375);
        tough(tree, "velara_west_2_0_tough", -403, -354);
        velaraPiousDevotion(tree, "velara_west_2_1_pious_devotion", -460, -404);
        healthy(tree, "velara_west_2_2_healthy", -533, -468);
        healthy(tree, "velara_west_2_3_healthy", -582, -510);
        tree.minor("velara_malediction", "Malediction", "velara_malediction", icon("life_tap"), -286, -409);
        healthy(tree, "velara_west_3_1_healthy", -330, -471);
        fastReflexes(tree, "velara_west_3_2_fast_reflexes", -385, -550);
        tree.minor("velara_healing_flow", "Healing Flow", "velara_healing_flow", icon("mana_regeneration"), -422, -603);
        toughII(tree, "velara_west_4_0_tough_ii", -181, -465);
        velaraPiousDevotion(tree, "velara_west_4_1_pious_devotion", -208, -536);
        healthy(tree, "velara_west_4_2_healthy", -243, -626);
        immune(tree, "velara_west_4_3_immune", -267, -687);
        healthyII(tree, "velara_west_5_0_healthy_ii", -80, -607);
        immuneII(tree, "velara_west_5_1_immune_ii", -93, -703);
        fastReflexesII(tree, "velara_west_5_2_fast_reflexes_ii", -101, -767);
        healthyII(tree, "velara_west_6_0_healthy_ii", 62, -570);
        toughII(tree, "velara_west_6_1_tough_ii", 71, -646);
        velaraPiousDevotion(tree, "velara_west_6_2_pious_devotion", 81, -742);
        tree.minor("velara_defender_of_the_faith", "Defender of the Faith", "velara_defender_of_the_faith", icon("smite_hand2"), 88, -807);
        immuneII(tree, "velara_west_7_0_immune_ii", 196, -539);
        fastReflexesII(tree, "velara_west_7_1_fast_reflexes_ii", 222, -610);
        healthyII(tree, "velara_west_7_2_healthy_ii", 255, -702);
        toughII(tree, "velara_west_7_3_tough_ii", 278, -762);
        tree.minor("velara_bounce_back", "Bounce Back", "velara_bounce_back", icon("heal"), 298, -446);
        velaraPiousDevotion(tree, "velara_west_8_1_pious_devotion", 340, -509);
        healthyII(tree, "velara_west_8_2_healthy_ii", 394, -590);
        tree.minor("velara_field_medic", "Field Medic", "velara_field_medic", icon("heal_group"), 430, -644);
        toughII(tree, "velara_west_9_0_tough_ii", 368, -337);
        healthyII(tree, "velara_west_9_1_healthy_ii", 424, -388);
        immuneII(tree, "velara_west_9_2_immune_ii", 495, -454);
        velaraPiousDevotion(tree, "velara_west_9_3_pious_devotion", 543, -498);
        healthyII(tree, "velara_west_10_0_healthy_ii", 504, -276);
        velaraPiousDevotion(tree, "velara_west_10_1_pious_devotion", 589, -323);
        tree.edge("velara_bounce_back", "velara_west_7_0_immune_ii");
        tree.edge("velara_bounce_back", "velara_west_9_0_tough_ii");
        tree.edge("velara_defender_of_the_faith", "velara_west_5_2_fast_reflexes_ii");
        tree.edge("velara_defender_of_the_faith", "velara_west_7_3_tough_ii");
        tree.edge("velara_field_medic", "velara_west_7_3_tough_ii");
        tree.edge("velara_field_medic", "velara_west_9_3_pious_devotion");
        tree.edge("velara_healing_flow", "velara_west_2_3_healthy");
        tree.edge("velara_healing_flow", "velara_west_4_3_immune");
        tree.edge("velara_malediction", "velara_west_2_0_tough");
        tree.edge("velara_malediction", "velara_west_4_0_tough_ii");
        tree.edge("velara_west_0_0_healthy", "velara_west_1_1_healthy");
        tree.edge("velara_west_0_0_healthy", "velara_west_1_2_pious_devotion");
        tree.edge("velara_west_0_1_immune", "velara_west_1_2_pious_devotion");
        tree.edge("velara_west_10_0_healthy_ii", "velara_west_10_1_pious_devotion");
        tree.edge("velara_west_10_0_healthy_ii", "velara_west_9_0_tough_ii");
        tree.edge("velara_west_10_0_healthy_ii", "velara_west_9_1_healthy_ii");
        tree.edge("velara_west_10_1_pious_devotion", "velara_west_9_2_immune_ii");
        tree.edge("velara_west_10_1_pious_devotion", "velara_west_9_3_pious_devotion");
        tree.edge("velara_west_1_0_fast_reflexes", "velara_west_1_1_healthy");
        tree.edge("velara_west_1_0_fast_reflexes", "velara_west_2_0_tough");
        tree.edge("velara_west_1_0_fast_reflexes", "velara_west_2_1_pious_devotion");
        tree.edge("velara_west_1_1_healthy", "velara_west_1_2_pious_devotion");
        tree.edge("velara_west_1_1_healthy", "velara_west_2_2_healthy");
        tree.edge("velara_west_1_2_pious_devotion", "velara_west_2_2_healthy");
        tree.edge("velara_west_1_2_pious_devotion", "velara_west_2_3_healthy");
        tree.edge("velara_west_2_0_tough", "velara_west_3_1_healthy");
        tree.edge("velara_west_2_1_pious_devotion", "velara_west_3_1_healthy");
        tree.edge("velara_west_2_2_healthy", "velara_west_3_2_fast_reflexes");
        tree.edge("velara_west_3_1_healthy", "velara_west_4_0_tough_ii");
        tree.edge("velara_west_3_1_healthy", "velara_west_4_1_pious_devotion");
        tree.edge("velara_west_3_2_fast_reflexes", "velara_west_4_2_healthy");
        tree.edge("velara_west_4_0_tough_ii", "velara_west_5_0_healthy_ii");
        tree.edge("velara_west_4_1_pious_devotion", "velara_west_5_0_healthy_ii");
        tree.edge("velara_west_4_2_healthy", "velara_west_5_1_immune_ii");
        tree.edge("velara_west_4_2_healthy", "velara_west_5_2_fast_reflexes_ii");
        tree.edge("velara_west_4_3_immune", "velara_west_5_2_fast_reflexes_ii");
        tree.edge("velara_west_5_0_healthy_ii", "velara_west_5_1_immune_ii");
        tree.edge("velara_west_5_0_healthy_ii", "velara_west_6_0_healthy_ii");
        tree.edge("velara_west_5_0_healthy_ii", "velara_west_6_1_tough_ii");
        tree.edge("velara_west_5_1_immune_ii", "velara_west_5_2_fast_reflexes_ii");
        tree.edge("velara_west_5_1_immune_ii", "velara_west_6_2_pious_devotion");
        tree.edge("velara_west_5_2_fast_reflexes_ii", "velara_west_6_2_pious_devotion");
        tree.edge("velara_west_6_0_healthy_ii", "velara_west_7_0_immune_ii");
        tree.edge("velara_west_6_0_healthy_ii", "velara_west_7_1_fast_reflexes_ii");
        tree.edge("velara_west_6_1_tough_ii", "velara_west_7_1_fast_reflexes_ii");
        tree.edge("velara_west_6_2_pious_devotion", "velara_west_7_2_healthy_ii");
        tree.edge("velara_west_6_2_pious_devotion", "velara_west_7_3_tough_ii");
        tree.edge("velara_west_7_1_fast_reflexes_ii", "velara_west_8_1_pious_devotion");
        tree.edge("velara_west_7_2_healthy_ii", "velara_west_8_2_healthy_ii");
        tree.edge("velara_west_7_3_tough_ii", "velara_west_8_2_healthy_ii");
        tree.edge("velara_west_8_1_pious_devotion", "velara_west_9_1_healthy_ii");
        tree.edge("velara_west_8_2_healthy_ii", "velara_west_9_2_immune_ii");
        tree.edge("velara_west_8_2_healthy_ii", "velara_west_9_3_pious_devotion");
    }

    private static void fern(GodTreeBuilder tree) {
        tree.root("velara_start_fern", "The Fern", 104, -124);
        healthy(tree, "velara_fern_s1_healthy", 51, -174);
        immune(tree, "velara_fern_ll1_immune", -19, -113);
        healthy(tree, "velara_fern_lr1_healthy", 104, -250);
        velaraPiousDevotion(tree, "velara_fern_s2_pious_devotion", -1, -226);
        healthyII(tree, "velara_fern_ll2_healthy_ii", -96, -143);
        immuneII(tree, "velara_fern_lr2_immune_ii", 67, -326);
        healthyII(tree, "velara_fern_s3_healthy_ii", -55, -272);
        tree.minor("velara_sanitation", "Sanitation", "velara_sanitation", icon("effect_duration"), -160, -195);
        tree.minor("velara_presence", "Presence", "velara_presence", icon("battle_cry"), 10, -391);
        tree.edge("velara_fern_ll1_immune", "velara_fern_ll2_healthy_ii");
        tree.edge("velara_fern_ll1_immune", "velara_fern_s1_healthy");
        tree.edge("velara_fern_ll1_immune", "velara_fern_s2_pious_devotion");
        tree.edge("velara_fern_ll2_healthy_ii", "velara_fern_s3_healthy_ii");
        tree.edge("velara_fern_ll2_healthy_ii", "velara_sanitation");
        tree.edge("velara_fern_lr1_healthy", "velara_fern_lr2_immune_ii");
        tree.edge("velara_fern_lr1_healthy", "velara_fern_s1_healthy");
        tree.edge("velara_fern_lr1_healthy", "velara_fern_s2_pious_devotion");
        tree.edge("velara_fern_lr2_immune_ii", "velara_fern_s3_healthy_ii");
        tree.edge("velara_fern_lr2_immune_ii", "velara_presence");
        tree.edge("velara_fern_s1_healthy", "velara_fern_s2_pious_devotion");
        tree.edge("velara_fern_s1_healthy", "velara_start_fern");
        tree.edge("velara_fern_s2_pious_devotion", "velara_fern_s3_healthy_ii");
    }

    private static void briar(GodTreeBuilder tree) {
        tree.root("velara_start_briar", "The Briar", -40, 172);
        tough(tree, "velara_briar_s1_tough", 10, 223);
        thorny(tree, "velara_briar_tl1_thorny", 81, 166);
        tough(tree, "velara_briar_tr1_tough", -28, 286);
        velaraPiousDevotion(tree, "velara_briar_s2_pious_devotion", 75, 258);
        toughII(tree, "velara_briar_tl2_tough_ii", 152, 171);
        thornyII(tree, "velara_briar_tr2_thorny_ii", 15, 371);
        toughII(tree, "velara_briar_s3_tough_ii", 123, 307);
        tree.minor("velara_indomitable", "Indomitable", "velara_indomitable", icon("heal_effect"), 219, 204);
        tree.minor("velara_perserverence", "Perserverence", "velara_perserverence", icon("totem"), 63, 428);
        tree.edge("velara_briar_s1_tough", "velara_briar_s2_pious_devotion");
        tree.edge("velara_briar_s1_tough", "velara_briar_tl1_thorny");
        tree.edge("velara_briar_s1_tough", "velara_briar_tr1_tough");
        tree.edge("velara_briar_s1_tough", "velara_start_briar");
        tree.edge("velara_briar_s2_pious_devotion", "velara_briar_s3_tough_ii");
        tree.edge("velara_briar_s2_pious_devotion", "velara_briar_tl1_thorny");
        tree.edge("velara_briar_s2_pious_devotion", "velara_briar_tr1_tough");
        tree.edge("velara_briar_s3_tough_ii", "velara_briar_tl2_tough_ii");
        tree.edge("velara_briar_s3_tough_ii", "velara_briar_tr2_thorny_ii");
        tree.edge("velara_briar_tl1_thorny", "velara_briar_tl2_tough_ii");
        tree.edge("velara_briar_tl2_tough_ii", "velara_indomitable");
        tree.edge("velara_briar_tr1_tough", "velara_briar_tr2_thorny_ii");
        tree.edge("velara_briar_tr2_thorny_ii", "velara_perserverence");
    }

    private static void bloom(GodTreeBuilder tree) {
        tree.root("velara_start_bloom", "The Bloom", -140, -41);
        guarded(tree, "velara_bloom_s1_guarded", -209, -25);
        guarded(tree, "velara_bloom_ll_guarded", -205, 64);
        fastReflexes(tree, "velara_bloom_lr_fast_reflexes", -241, -95);
        fastReflexes(tree, "velara_bloom_s2_fast_reflexes", -282, 5);
        velaraPiousDevotion(tree, "velara_bloom_pl_pious_devotion", -296, 103);
        guardedII(tree, "velara_bloom_pr_guarded_ii", -350, -66);
        fastReflexesII(tree, "velara_bloom_pt_fast_reflexes_ii", -335, 22);
        tree.minor("velara_counterstrike", "Counterstrike", "velara_counterstrike", icon("retribution"), -396, 42);
        tree.edge("velara_bloom_ll_guarded", "velara_bloom_s1_guarded");
        tree.edge("velara_bloom_ll_guarded", "velara_bloom_s2_fast_reflexes");
        tree.edge("velara_bloom_lr_fast_reflexes", "velara_bloom_s1_guarded");
        tree.edge("velara_bloom_lr_fast_reflexes", "velara_bloom_s2_fast_reflexes");
        tree.edge("velara_bloom_pl_pious_devotion", "velara_bloom_s2_fast_reflexes");
        tree.edge("velara_bloom_pl_pious_devotion", "velara_counterstrike");
        tree.edge("velara_bloom_pr_guarded_ii", "velara_bloom_s2_fast_reflexes");
        tree.edge("velara_bloom_pr_guarded_ii", "velara_counterstrike");
        tree.edge("velara_bloom_pt_fast_reflexes_ii", "velara_bloom_s2_fast_reflexes");
        tree.edge("velara_bloom_pt_fast_reflexes_ii", "velara_counterstrike");
        tree.edge("velara_bloom_s1_guarded", "velara_bloom_s2_fast_reflexes");
        tree.edge("velara_bloom_s1_guarded", "velara_start_bloom");
    }

    private static void sapling(GodTreeBuilder tree) {
        tree.root("velara_start_sapling", "The Sapling", 151, 50);
        tough(tree, "velara_sapling_rl_tough", 158, -41);
        armored(tree, "velara_sapling_rr_armored", 211, 122);
        armored(tree, "velara_sapling_s1_armored", 228, 30);
        velaraPiousDevotion(tree, "velara_sapling_s2_pious_devotion", 292, 1);
        armoredII(tree, "velara_sapling_bl_armored_ii", 296, -90);
        toughII(tree, "velara_sapling_br_tough_ii", 338, 86);
        armoredII(tree, "velara_sapling_s3_armored_ii", 358, -13);
        tree.minor("velara_utilized", "Utilized", "velara_utilized", icon("utility"), 425, -33);
        tree.edge("velara_sapling_bl_armored_ii", "velara_sapling_s2_pious_devotion");
        tree.edge("velara_sapling_bl_armored_ii", "velara_sapling_s3_armored_ii");
        tree.edge("velara_sapling_br_tough_ii", "velara_sapling_s2_pious_devotion");
        tree.edge("velara_sapling_br_tough_ii", "velara_sapling_s3_armored_ii");
        tree.edge("velara_sapling_rl_tough", "velara_sapling_s1_armored");
        tree.edge("velara_sapling_rl_tough", "velara_start_sapling");
        tree.edge("velara_sapling_rr_armored", "velara_sapling_s1_armored");
        tree.edge("velara_sapling_rr_armored", "velara_start_sapling");
        tree.edge("velara_sapling_s1_armored", "velara_sapling_s2_pious_devotion");
        tree.edge("velara_sapling_s1_armored", "velara_start_sapling");
        tree.edge("velara_sapling_s2_pious_devotion", "velara_sapling_s3_armored_ii");
        tree.edge("velara_sapling_s3_armored_ii", "velara_utilized");
    }

    private static void velaraBridges(GodTreeBuilder tree) {
        tree.edge("velara_east_0_0_armored", "velara_start_bulwark");
        tree.edge("velara_east_10_0_armored_ii", "velara_immortal");
        tree.edge("velara_east_10_1_pious_devotion", "velara_immortal");
        tree.edge("velara_magic_armor", "velara_start_bulwark");
        tree.edge("velara_sacrifice", "velara_west_10_0_healthy_ii");
        tree.edge("velara_sacrifice", "velara_west_10_1_pious_devotion");
        tree.edge("velara_start_mercy", "velara_west_0_0_healthy");
        tree.edge("velara_start_mercy", "velara_west_0_1_immune");
    }

    private static void tough(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Tough", "velara_tough", icon("health"), x, y);
    }

    private static void armored(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Armored", "velara_armored", icon("armour"), x, y);
    }

    private static void immune(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Immune", "velara_immune", icon("cleanse"), x, y);
    }

    private static void healthy(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Healthy", "velara_healthy", icon("healing_efficiency"), x, y);
    }

    private static void fastReflexes(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Fast Reflexes", "velara_fast_reflexes", icon("dash"), x, y);
    }

    private static void guarded(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Guarded", "velara_guarded", icon("shell"), x, y);
    }

    private static void thorny(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Thorny", "velara_thorny", icon("thorns"), x, y);
    }

    private static void velaraPiousDevotion(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Pious Devotion", "velara_pious_devotion", icon("smite"), x, y);
    }

    private static void toughII(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Tough II", "velara_tough_ii", icon("health"), x, y);
    }

    private static void armoredII(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Armored II", "velara_armored_ii", icon("armour"), x, y);
    }

    private static void immuneII(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Immune II", "velara_immune_ii", icon("cleanse"), x, y);
    }

    private static void healthyII(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Healthy II", "velara_healthy_ii", icon("healing_efficiency"), x, y);
    }

    private static void fastReflexesII(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Fast Reflexes II", "velara_fast_reflexes_ii", icon("dash"), x, y);
    }

    private static void guardedII(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Guarded II", "velara_guarded_ii", icon("shell"), x, y);
    }

    private static void thornyII(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Thorny II", "velara_thorny_ii", icon("thorns"), x, y);
    }

    public static GodTreeBuilder tenos() {
        GodTreeBuilder tree = new GodTreeBuilder("tenos");
        tenosFrame(tree);
        goldPile(tree);
        doors(tree);
        haul(tree);
        chalice(tree);
        scales(tree);
        compass(tree);
        lantern(tree);
        tenosBridges(tree);
        tree.label("The Hoard", 484, 387);
        tree.label("The Doors", -423, -238);
        tree.label("The Haul", 423, -238);
        tree.label("The Chalice", -684, 18);
        tree.label("The Scales", 684, 41);
        tree.label("The Lantern", -630, 593);
        tree.label("The Compass", 630, 591);
        return tree;
    }

    private static void tenosFrame(GodTreeBuilder tree) {
        tree.root("tenos_start_hoard", "The Hoard", -284, 529);
        tree.major("tenos_looting_engine", "Looting Engine", "tenos_looting_engine", icon("arcane_rail"), 4, 104);
        tree.root("tenos_start_chest", "The Chest", 0, -31);
        tree.major("tenos_massive_chests", "Massive Chests", "tenos_massive_chests", icon("copiously"), 0, -445);
    }

    private static void goldPile(GodTreeBuilder tree) {
        hoarder(tree, "tenos_pile_0_0_hoarder", -170, 454);
        treasurer(tree, "tenos_pile_0_1_treasurer", -170, 500);
        hoarder(tree, "tenos_pile_0_2_hoarder", -170, 558);
        treasurer(tree, "tenos_pile_0_3_treasurer", -170, 605);
        treasurer(tree, "tenos_pile_1_0_treasurer", -56, 454);
        hoarder(tree, "tenos_pile_1_1_hoarder", -56, 500);
        tenosPiousDevotion(tree, "tenos_pile_1_2_pious_devotion", -56, 558);
        hoarder(tree, "tenos_pile_1_3_hoarder", -56, 605);
        tree.minor("tenos_sacked", "Sacked", "tenos_sacked", icon("loot_goblin"), 58, 454);
        hoarder(tree, "tenos_pile_2_1_hoarder", 58, 500);
        treasurer(tree, "tenos_pile_2_2_treasurer", 58, 558);
        tree.minor("tenos_sack_of_mobs", "Sack of Mobs", "tenos_sack_of_mobs", icon("horde_dmg"), 58, 605);
        hoarder(tree, "tenos_pile_3_0_hoarder", 172, 454);
        tenosPiousDevotion(tree, "tenos_pile_3_1_pious_devotion", 172, 500);
        hoarder(tree, "tenos_pile_3_2_hoarder", 172, 558);
        treasurer(tree, "tenos_pile_3_3_treasurer", 172, 605);
        treasurer(tree, "tenos_pile_4_0_treasurer", 252, 483);
        hoarder(tree, "tenos_pile_4_1_hoarder", 299, 515);
        tree.minor("tenos_drillmaster", "Drillmaster", "tenos_drillmaster", icon("mining_speed"), 338, 541);
        tenosPiousDevotion(tree, "tenos_pile_5_0_pious_devotion", 257, 387);
        treasurerII(tree, "tenos_pile_5_1_treasurer_ii", 315, 387);
        hoarderII(tree, "tenos_pile_5_2_hoarder_ii", 380, 387);
        tree.minor("tenos_global_veins", "Global Veins", "tenos_global_veins", icon("vein_miner"), 213, 410);
        hoarderII(tree, "tenos_pile_6_1_hoarder_ii", 213, 346);
        advancedExtractionII(tree, "tenos_pile_6_2_advanced_extraction_ii", 214, 288);
        treasurerII(tree, "tenos_pile_6_3_treasurer_ii", 214, 241);
        treasurerII(tree, "tenos_pile_7_0_treasurer_ii", 99, 392);
        tenosPiousDevotion(tree, "tenos_pile_7_1_pious_devotion", 99, 346);
        hoarderII(tree, "tenos_pile_7_2_hoarder_ii", 99, 288);
        hoarderII(tree, "tenos_pile_7_3_hoarder_ii", 99, 241);
        tree.minor("tenos_gold_plating", "Gold Plating", "tenos_gold_plating", icon("greed_assasin_dmg_reduction"), -15, 392);
        hoarderII(tree, "tenos_pile_8_1_hoarder_ii", -15, 346);
        treasurerII(tree, "tenos_pile_8_2_treasurer_ii", -15, 288);
        tree.minor("tenos_cash_hunter", "Cash Hunter", "tenos_cash_hunter", icon("kill_coin"), -15, 241);
        treasurerII(tree, "tenos_pile_9_0_treasurer_ii", -173, 268);
        advancedExtractionII(tree, "tenos_pile_9_1_advanced_extraction_ii", -127, 262);
        tenosPiousDevotion(tree, "tenos_pile_9_2_pious_devotion", -70, 255);
        treasurerII(tree, "tenos_pile_10_0_treasurer_ii", -120, 134);
        tenosPiousDevotion(tree, "tenos_pile_10_1_pious_devotion", -67, 156);
        tree.edge("tenos_cash_hunter", "tenos_pile_7_3_hoarder_ii");
        tree.edge("tenos_drillmaster", "tenos_pile_3_3_treasurer");
        tree.edge("tenos_drillmaster", "tenos_pile_5_2_hoarder_ii");
        tree.edge("tenos_global_veins", "tenos_pile_7_0_treasurer_ii");
        tree.edge("tenos_gold_plating", "tenos_pile_7_0_treasurer_ii");
        tree.edge("tenos_gold_plating", "tenos_pile_9_0_treasurer_ii");
        tree.edge("tenos_pile_0_0_hoarder", "tenos_pile_0_1_treasurer");
        tree.edge("tenos_pile_0_0_hoarder", "tenos_pile_1_0_treasurer");
        tree.edge("tenos_pile_0_0_hoarder", "tenos_pile_1_1_hoarder");
        tree.edge("tenos_pile_0_1_treasurer", "tenos_pile_0_2_hoarder");
        tree.edge("tenos_pile_0_1_treasurer", "tenos_pile_1_1_hoarder");
        tree.edge("tenos_pile_0_2_hoarder", "tenos_pile_0_3_treasurer");
        tree.edge("tenos_pile_0_2_hoarder", "tenos_pile_1_2_pious_devotion");
        tree.edge("tenos_pile_0_2_hoarder", "tenos_pile_1_3_hoarder");
        tree.edge("tenos_pile_0_3_treasurer", "tenos_pile_1_3_hoarder");
        tree.edge("tenos_pile_10_0_treasurer_ii", "tenos_pile_9_0_treasurer_ii");
        tree.edge("tenos_pile_10_0_treasurer_ii", "tenos_pile_9_1_advanced_extraction_ii");
        tree.edge("tenos_pile_10_1_pious_devotion", "tenos_pile_9_2_pious_devotion");
        tree.edge("tenos_pile_1_0_treasurer", "tenos_sacked");
        tree.edge("tenos_pile_1_1_hoarder", "tenos_pile_2_1_hoarder");
        tree.edge("tenos_pile_1_2_pious_devotion", "tenos_pile_2_2_treasurer");
        tree.edge("tenos_pile_1_3_hoarder", "tenos_pile_2_2_treasurer");
        tree.edge("tenos_pile_1_3_hoarder", "tenos_sack_of_mobs");
        tree.edge("tenos_pile_2_1_hoarder", "tenos_pile_3_1_pious_devotion");
        tree.edge("tenos_pile_2_2_treasurer", "tenos_pile_3_2_hoarder");
        tree.edge("tenos_pile_2_2_treasurer", "tenos_pile_3_3_treasurer");
        tree.edge("tenos_pile_3_0_hoarder", "tenos_pile_4_0_treasurer");
        tree.edge("tenos_pile_3_0_hoarder", "tenos_sacked");
        tree.edge("tenos_pile_3_1_pious_devotion", "tenos_pile_4_0_treasurer");
        tree.edge("tenos_pile_3_2_hoarder", "tenos_pile_4_1_hoarder");
        tree.edge("tenos_pile_3_3_treasurer", "tenos_pile_4_1_hoarder");
        tree.edge("tenos_pile_3_3_treasurer", "tenos_sack_of_mobs");
        tree.edge("tenos_pile_4_0_treasurer", "tenos_pile_5_0_pious_devotion");
        tree.edge("tenos_pile_4_1_hoarder", "tenos_pile_5_1_treasurer_ii");
        tree.edge("tenos_pile_5_0_pious_devotion", "tenos_pile_5_1_treasurer_ii");
        tree.edge("tenos_pile_5_0_pious_devotion", "tenos_pile_6_1_hoarder_ii");
        tree.edge("tenos_pile_5_1_treasurer_ii", "tenos_pile_6_2_advanced_extraction_ii");
        tree.edge("tenos_pile_5_2_hoarder_ii", "tenos_pile_6_3_treasurer_ii");
        tree.edge("tenos_pile_6_1_hoarder_ii", "tenos_pile_6_2_advanced_extraction_ii");
        tree.edge("tenos_pile_6_1_hoarder_ii", "tenos_pile_7_1_pious_devotion");
        tree.edge("tenos_pile_6_2_advanced_extraction_ii", "tenos_pile_6_3_treasurer_ii");
        tree.edge("tenos_pile_6_2_advanced_extraction_ii", "tenos_pile_7_2_hoarder_ii");
        tree.edge("tenos_pile_6_2_advanced_extraction_ii", "tenos_pile_7_3_hoarder_ii");
        tree.edge("tenos_pile_6_3_treasurer_ii", "tenos_pile_7_3_hoarder_ii");
        tree.edge("tenos_pile_7_1_pious_devotion", "tenos_pile_8_1_hoarder_ii");
        tree.edge("tenos_pile_7_2_hoarder_ii", "tenos_pile_8_2_treasurer_ii");
        tree.edge("tenos_pile_7_3_hoarder_ii", "tenos_pile_8_2_treasurer_ii");
        tree.edge("tenos_pile_8_1_hoarder_ii", "tenos_pile_9_1_advanced_extraction_ii");
        tree.edge("tenos_pile_8_2_treasurer_ii", "tenos_pile_9_2_pious_devotion");
    }

    private static void doors(GodTreeBuilder tree) {
        wideInfluence(tree, "tenos_chest_l_0_0_wide_influence", -60, -31);
        careful(tree, "tenos_chest_l_0_1_careful", -60, 23);
        careful(tree, "tenos_chest_l_1_0_careful", -120, -31);
        tenosPiousDevotion(tree, "tenos_chest_l_1_1_pious_devotion", -120, 23);
        wideInfluence(tree, "tenos_chest_l_2_0_wide_influence", -181, -31);
        tree.minor("tenos_challenge_tackler", "Challenge Tackler", "tenos_challenge_tackler", icon("greed_assassin_rep"), -181, 23);
        careful(tree, "tenos_chest_l_3_0_careful", -288, -91);
        wideInfluence(tree, "tenos_chest_l_3_1_wide_influence", -337, -68);
        wideInfluence(tree, "tenos_chest_l_4_0_wide_influence", -297, -177);
        careful(tree, "tenos_chest_l_4_1_careful", -351, -177);
        magicalII(tree, "tenos_chest_l_5_0_magical_ii", -234, -238);
        tenosPiousDevotion(tree, "tenos_chest_l_5_1_pious_devotion", -297, -238);
        wideInfluenceII(tree, "tenos_chest_l_5_2_wide_influence_ii", -351, -238);
        wideInfluenceII(tree, "tenos_chest_l_6_0_wide_influence_ii", -234, -298);
        carefulII(tree, "tenos_chest_l_6_1_careful_ii", -297, -298);
        wideInfluenceII(tree, "tenos_chest_l_6_2_wide_influence_ii", -351, -298);
        tree.minor("tenos_omega_vault", "Omega Vault", "tenos_omega_vault", icon("chaos_cube"), -231, -358);
        wideInfluenceII(tree, "tenos_chest_l_7_1_wide_influence_ii", -289, -382);
        tree.minor("tenos_nose_for_treasure", "Nose for Treasure", "tenos_nose_for_treasure", icon("door_hunter"), -339, -402);
        carefulII(tree, "tenos_chest_l_8_0_careful_ii", -180, -382);
        magicalII(tree, "tenos_chest_l_8_1_magical_ii", -181, -445);
        wideInfluenceII(tree, "tenos_chest_l_8_2_wide_influence_ii", -181, -499);
        wideInfluenceII(tree, "tenos_chest_l_9_0_wide_influence_ii", -120, -445);
        tenosPiousDevotion(tree, "tenos_chest_l_9_1_pious_devotion", -120, -499);
        carefulII(tree, "tenos_chest_l_10_0_careful_ii", -60, -445);
        wideInfluenceII(tree, "tenos_chest_l_10_1_wide_influence_ii", -60, -499);
        tree.edge("tenos_challenge_tackler", "tenos_chest_l_1_1_pious_devotion");
        tree.edge("tenos_challenge_tackler", "tenos_chest_l_3_1_wide_influence");
        tree.edge("tenos_chest_l_0_0_wide_influence", "tenos_chest_l_1_0_careful");
        tree.edge("tenos_chest_l_0_0_wide_influence", "tenos_chest_l_1_1_pious_devotion");
        tree.edge("tenos_chest_l_0_1_careful", "tenos_chest_l_1_1_pious_devotion");
        tree.edge("tenos_chest_l_10_0_careful_ii", "tenos_chest_l_9_0_wide_influence_ii");
        tree.edge("tenos_chest_l_10_0_careful_ii", "tenos_chest_l_9_1_pious_devotion");
        tree.edge("tenos_chest_l_10_1_wide_influence_ii", "tenos_chest_l_9_1_pious_devotion");
        tree.edge("tenos_chest_l_1_0_careful", "tenos_chest_l_1_1_pious_devotion");
        tree.edge("tenos_chest_l_1_0_careful", "tenos_chest_l_2_0_wide_influence");
        tree.edge("tenos_chest_l_1_1_pious_devotion", "tenos_chest_l_2_0_wide_influence");
        tree.edge("tenos_chest_l_2_0_wide_influence", "tenos_chest_l_3_0_careful");
        tree.edge("tenos_chest_l_2_0_wide_influence", "tenos_chest_l_3_1_wide_influence");
        tree.edge("tenos_chest_l_3_0_careful", "tenos_chest_l_4_0_wide_influence");
        tree.edge("tenos_chest_l_3_1_wide_influence", "tenos_chest_l_4_0_wide_influence");
        tree.edge("tenos_chest_l_3_1_wide_influence", "tenos_chest_l_4_1_careful");
        tree.edge("tenos_chest_l_4_0_wide_influence", "tenos_chest_l_5_1_pious_devotion");
        tree.edge("tenos_chest_l_4_0_wide_influence", "tenos_chest_l_5_2_wide_influence_ii");
        tree.edge("tenos_chest_l_4_1_careful", "tenos_chest_l_5_2_wide_influence_ii");
        tree.edge("tenos_chest_l_5_0_magical_ii", "tenos_chest_l_5_1_pious_devotion");
        tree.edge("tenos_chest_l_5_0_magical_ii", "tenos_chest_l_6_0_wide_influence_ii");
        tree.edge("tenos_chest_l_5_1_pious_devotion", "tenos_chest_l_5_2_wide_influence_ii");
        tree.edge("tenos_chest_l_5_1_pious_devotion", "tenos_chest_l_6_1_careful_ii");
        tree.edge("tenos_chest_l_5_2_wide_influence_ii", "tenos_chest_l_6_1_careful_ii");
        tree.edge("tenos_chest_l_5_2_wide_influence_ii", "tenos_chest_l_6_2_wide_influence_ii");
        tree.edge("tenos_chest_l_6_0_wide_influence_ii", "tenos_omega_vault");
        tree.edge("tenos_chest_l_6_1_careful_ii", "tenos_chest_l_7_1_wide_influence_ii");
        tree.edge("tenos_chest_l_6_2_wide_influence_ii", "tenos_nose_for_treasure");
        tree.edge("tenos_chest_l_7_1_wide_influence_ii", "tenos_chest_l_8_1_magical_ii");
        tree.edge("tenos_chest_l_8_0_careful_ii", "tenos_omega_vault");
        tree.edge("tenos_chest_l_8_1_magical_ii", "tenos_chest_l_9_0_wide_influence_ii");
        tree.edge("tenos_chest_l_8_1_magical_ii", "tenos_chest_l_9_1_pious_devotion");
        tree.edge("tenos_chest_l_8_2_wide_influence_ii", "tenos_chest_l_9_1_pious_devotion");
        tree.edge("tenos_chest_l_8_2_wide_influence_ii", "tenos_nose_for_treasure");
        tree.edge("tenos_chest_l_9_0_wide_influence_ii", "tenos_chest_l_9_1_pious_devotion");
    }

    private static void haul(GodTreeBuilder tree) {
        hoarder(tree, "tenos_chest_r_0_0_hoarder", 60, -31);
        treasurer(tree, "tenos_chest_r_0_1_treasurer", 60, 23);
        treasurer(tree, "tenos_chest_r_1_0_treasurer", 120, -31);
        tenosPiousDevotion(tree, "tenos_chest_r_1_1_pious_devotion", 120, 23);
        hoarder(tree, "tenos_chest_r_2_0_hoarder", 181, -31);
        tree.minor("tenos_barter_expert", "Barter Expert", "tenos_barter_expert", icon("totem"), 181, 23);
        treasurer(tree, "tenos_chest_r_3_0_treasurer", 288, -91);
        hoarder(tree, "tenos_chest_r_3_1_hoarder", 337, -68);
        hoarder(tree, "tenos_chest_r_4_0_hoarder", 297, -177);
        treasurer(tree, "tenos_chest_r_4_1_treasurer", 351, -177);
        reservesII(tree, "tenos_chest_r_5_0_reserves_ii", 234, -238);
        tenosPiousDevotion(tree, "tenos_chest_r_5_1_pious_devotion", 297, -238);
        hoarderII(tree, "tenos_chest_r_5_2_hoarder_ii", 351, -238);
        treasurerII(tree, "tenos_chest_r_6_0_treasurer_ii", 234, -298);
        hoarderII(tree, "tenos_chest_r_6_1_hoarder_ii", 297, -298);
        treasurerII(tree, "tenos_chest_r_6_2_treasurer_ii", 351, -298);
        tree.minor("tenos_expert_looter", "Expert Looter", "tenos_expert_looter", icon("hunter"), 231, -358);
        hoarderII(tree, "tenos_chest_r_7_1_hoarder_ii", 289, -382);
        tree.minor("tenos_master_of_chests", "Master of Chests", "tenos_master_of_chests", icon("chaining"), 339, -402);
        hoarderII(tree, "tenos_chest_r_8_0_hoarder_ii", 180, -382);
        reservesII(tree, "tenos_chest_r_8_1_reserves_ii", 181, -445);
        treasurerII(tree, "tenos_chest_r_8_2_treasurer_ii", 181, -499);
        treasurerII(tree, "tenos_chest_r_9_0_treasurer_ii", 120, -445);
        tenosPiousDevotion(tree, "tenos_chest_r_9_1_pious_devotion", 120, -499);
        hoarderII(tree, "tenos_chest_r_10_0_hoarder_ii", 60, -445);
        treasurerII(tree, "tenos_chest_r_10_1_treasurer_ii", 60, -499);
        tree.edge("tenos_barter_expert", "tenos_chest_r_1_1_pious_devotion");
        tree.edge("tenos_barter_expert", "tenos_chest_r_3_1_hoarder");
        tree.edge("tenos_chest_r_0_0_hoarder", "tenos_chest_r_1_0_treasurer");
        tree.edge("tenos_chest_r_0_0_hoarder", "tenos_chest_r_1_1_pious_devotion");
        tree.edge("tenos_chest_r_0_1_treasurer", "tenos_chest_r_1_1_pious_devotion");
        tree.edge("tenos_chest_r_10_0_hoarder_ii", "tenos_chest_r_9_0_treasurer_ii");
        tree.edge("tenos_chest_r_10_0_hoarder_ii", "tenos_chest_r_9_1_pious_devotion");
        tree.edge("tenos_chest_r_10_1_treasurer_ii", "tenos_chest_r_9_1_pious_devotion");
        tree.edge("tenos_chest_r_1_0_treasurer", "tenos_chest_r_1_1_pious_devotion");
        tree.edge("tenos_chest_r_1_0_treasurer", "tenos_chest_r_2_0_hoarder");
        tree.edge("tenos_chest_r_1_1_pious_devotion", "tenos_chest_r_2_0_hoarder");
        tree.edge("tenos_chest_r_2_0_hoarder", "tenos_chest_r_3_0_treasurer");
        tree.edge("tenos_chest_r_2_0_hoarder", "tenos_chest_r_3_1_hoarder");
        tree.edge("tenos_chest_r_3_0_treasurer", "tenos_chest_r_4_0_hoarder");
        tree.edge("tenos_chest_r_3_1_hoarder", "tenos_chest_r_4_0_hoarder");
        tree.edge("tenos_chest_r_3_1_hoarder", "tenos_chest_r_4_1_treasurer");
        tree.edge("tenos_chest_r_4_0_hoarder", "tenos_chest_r_5_1_pious_devotion");
        tree.edge("tenos_chest_r_4_0_hoarder", "tenos_chest_r_5_2_hoarder_ii");
        tree.edge("tenos_chest_r_4_1_treasurer", "tenos_chest_r_5_2_hoarder_ii");
        tree.edge("tenos_chest_r_5_0_reserves_ii", "tenos_chest_r_5_1_pious_devotion");
        tree.edge("tenos_chest_r_5_0_reserves_ii", "tenos_chest_r_6_0_treasurer_ii");
        tree.edge("tenos_chest_r_5_1_pious_devotion", "tenos_chest_r_5_2_hoarder_ii");
        tree.edge("tenos_chest_r_5_1_pious_devotion", "tenos_chest_r_6_1_hoarder_ii");
        tree.edge("tenos_chest_r_5_2_hoarder_ii", "tenos_chest_r_6_1_hoarder_ii");
        tree.edge("tenos_chest_r_5_2_hoarder_ii", "tenos_chest_r_6_2_treasurer_ii");
        tree.edge("tenos_chest_r_6_0_treasurer_ii", "tenos_expert_looter");
        tree.edge("tenos_chest_r_6_1_hoarder_ii", "tenos_chest_r_7_1_hoarder_ii");
        tree.edge("tenos_chest_r_6_2_treasurer_ii", "tenos_master_of_chests");
        tree.edge("tenos_chest_r_7_1_hoarder_ii", "tenos_chest_r_8_1_reserves_ii");
        tree.edge("tenos_chest_r_8_0_hoarder_ii", "tenos_expert_looter");
        tree.edge("tenos_chest_r_8_1_reserves_ii", "tenos_chest_r_9_0_treasurer_ii");
        tree.edge("tenos_chest_r_8_1_reserves_ii", "tenos_chest_r_9_1_pious_devotion");
        tree.edge("tenos_chest_r_8_2_treasurer_ii", "tenos_chest_r_9_1_pious_devotion");
        tree.edge("tenos_chest_r_8_2_treasurer_ii", "tenos_master_of_chests");
        tree.edge("tenos_chest_r_9_0_treasurer_ii", "tenos_chest_r_9_1_pious_devotion");
    }

    private static void chalice(GodTreeBuilder tree) {
        tree.root("tenos_start_chalice", "The Chalice", -653, -77);
        magical(tree, "tenos_chalice_st_magical", -668, -126);
        reserves(tree, "tenos_chalice_bl_reserves", -727, -141);
        magical(tree, "tenos_chalice_br_magical", -628, -171);
        tenosPiousDevotion(tree, "tenos_chalice_cup_pious_devotion", -685, -182);
        magicalII(tree, "tenos_chalice_rl_magical_ii", -765, -181);
        reservesII(tree, "tenos_chalice_rr_reserves_ii", -619, -226);
        tree.minor("tenos_mana_starved", "Mana Starved", "tenos_mana_starved", icon("mana"), -790, -225);
        tree.minor("tenos_deep_reserves", "Deep Reserves", "tenos_deep_reserves", icon("mana_barrier"), -622, -277);
        tree.edge("tenos_chalice_bl_reserves", "tenos_chalice_cup_pious_devotion");
        tree.edge("tenos_chalice_bl_reserves", "tenos_chalice_rl_magical_ii");
        tree.edge("tenos_chalice_bl_reserves", "tenos_chalice_st_magical");
        tree.edge("tenos_chalice_br_magical", "tenos_chalice_cup_pious_devotion");
        tree.edge("tenos_chalice_br_magical", "tenos_chalice_rr_reserves_ii");
        tree.edge("tenos_chalice_br_magical", "tenos_chalice_st_magical");
        tree.edge("tenos_chalice_cup_pious_devotion", "tenos_chalice_st_magical");
        tree.edge("tenos_chalice_rl_magical_ii", "tenos_mana_starved");
        tree.edge("tenos_chalice_rr_reserves_ii", "tenos_deep_reserves");
        tree.edge("tenos_chalice_st_magical", "tenos_start_chalice");
    }

    private static void scales(GodTreeBuilder tree) {
        tree.root("tenos_start_scales", "The Scales", 659, -54);
        hoarder(tree, "tenos_scales_po_hoarder", 672, -106);
        tenosPiousDevotion(tree, "tenos_scales_bm_pious_devotion", 685, -158);
        treasurer(tree, "tenos_scales_al_treasurer", 619, -182);
        hoarder(tree, "tenos_scales_ar_hoarder", 751, -135);
        hoarderII(tree, "tenos_scales_hl_hoarder_ii", 617, -229);
        treasurerII(tree, "tenos_scales_hr_treasurer_ii", 775, -175);
        tree.minor("tenos_unstoppable_greed", "Unstoppable Greed", "tenos_unstoppable_greed", icon("atk_ap"), 565, -218);
        tree.minor("tenos_wealthy_patron", "Wealthy Patron", "tenos_wealthy_patron", icon("armour"), 815, -140);
        tree.edge("tenos_scales_al_treasurer", "tenos_scales_bm_pious_devotion");
        tree.edge("tenos_scales_al_treasurer", "tenos_scales_hl_hoarder_ii");
        tree.edge("tenos_scales_al_treasurer", "tenos_scales_po_hoarder");
        tree.edge("tenos_scales_ar_hoarder", "tenos_scales_bm_pious_devotion");
        tree.edge("tenos_scales_ar_hoarder", "tenos_scales_hr_treasurer_ii");
        tree.edge("tenos_scales_ar_hoarder", "tenos_scales_po_hoarder");
        tree.edge("tenos_scales_bm_pious_devotion", "tenos_scales_po_hoarder");
        tree.edge("tenos_scales_hl_hoarder_ii", "tenos_unstoppable_greed");
        tree.edge("tenos_scales_hr_treasurer_ii", "tenos_wealthy_patron");
        tree.edge("tenos_scales_po_hoarder", "tenos_start_scales");
    }

    private static void compass(GodTreeBuilder tree) {
        tree.root("tenos_start_compass", "The Compass", 665, 496);
        wideInfluence(tree, "tenos_compass_ll_wide_influence", 605, 473);
        careful(tree, "tenos_compass_lr_careful", 687, 437);
        tenosPiousDevotion(tree, "tenos_compass_mid_pious_devotion", 627, 412);
        wideInfluenceII(tree, "tenos_compass_tip_wide_influence_ii", 602, 371);
        tree.minor("tenos_domain_expansion", "Domain Expansion", "tenos_domain_expansion", icon("nova"), 588, 324);
        tree.edge("tenos_compass_ll_wide_influence", "tenos_compass_lr_careful");
        tree.edge("tenos_compass_ll_wide_influence", "tenos_compass_mid_pious_devotion");
        tree.edge("tenos_compass_ll_wide_influence", "tenos_start_compass");
        tree.edge("tenos_compass_lr_careful", "tenos_compass_mid_pious_devotion");
        tree.edge("tenos_compass_lr_careful", "tenos_start_compass");
        tree.edge("tenos_compass_mid_pious_devotion", "tenos_compass_tip_wide_influence_ii");
        tree.edge("tenos_compass_tip_wide_influence_ii", "tenos_domain_expansion");
    }

    private static void lantern(GodTreeBuilder tree) {
        tree.root("tenos_start_lantern", "The Lantern", -657, 499);
        careful(tree, "tenos_lantern_gl_careful", -688, 446);
        advancedExtraction(tree, "tenos_lantern_gr_advanced_extraction", -599, 480);
        tenosPiousDevotion(tree, "tenos_lantern_bod_pious_devotion", -630, 427);
        carefulII(tree, "tenos_lantern_top_careful_ii", -610, 382);
        tree.minor("tenos_indiana_jones", "Indiana Jones", "tenos_indiana_jones", icon("trap_disarm"), -594, 335);
        tree.edge("tenos_indiana_jones", "tenos_lantern_top_careful_ii");
        tree.edge("tenos_lantern_bod_pious_devotion", "tenos_lantern_gl_careful");
        tree.edge("tenos_lantern_bod_pious_devotion", "tenos_lantern_gr_advanced_extraction");
        tree.edge("tenos_lantern_bod_pious_devotion", "tenos_lantern_top_careful_ii");
        tree.edge("tenos_lantern_gl_careful", "tenos_lantern_gr_advanced_extraction");
        tree.edge("tenos_lantern_gl_careful", "tenos_start_lantern");
        tree.edge("tenos_lantern_gr_advanced_extraction", "tenos_start_lantern");
    }

    private static void tenosBridges(GodTreeBuilder tree) {
        tree.edge("tenos_chest_l_0_0_wide_influence", "tenos_start_chest");
        tree.edge("tenos_chest_l_0_1_careful", "tenos_start_chest");
        tree.edge("tenos_chest_l_10_0_careful_ii", "tenos_massive_chests");
        tree.edge("tenos_chest_l_10_1_wide_influence_ii", "tenos_massive_chests");
        tree.edge("tenos_chest_r_0_0_hoarder", "tenos_start_chest");
        tree.edge("tenos_chest_r_0_1_treasurer", "tenos_start_chest");
        tree.edge("tenos_chest_r_10_0_hoarder_ii", "tenos_massive_chests");
        tree.edge("tenos_chest_r_10_1_treasurer_ii", "tenos_massive_chests");
        tree.edge("tenos_looting_engine", "tenos_pile_10_0_treasurer_ii");
        tree.edge("tenos_looting_engine", "tenos_pile_10_1_pious_devotion");
        tree.edge("tenos_pile_0_0_hoarder", "tenos_start_hoard");
        tree.edge("tenos_pile_0_1_treasurer", "tenos_start_hoard");
        tree.edge("tenos_pile_0_2_hoarder", "tenos_start_hoard");
        tree.edge("tenos_pile_0_3_treasurer", "tenos_start_hoard");
    }

    private static void hoarder(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Hoarder", "tenos_hoarder", icon("item_quantity"), x, y);
    }

    private static void treasurer(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Treasurer", "tenos_treasurer", icon("item_rarity"), x, y);
    }

    private static void magical(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Magical", "tenos_magical", icon("mana_regeneration"), x, y);
    }

    private static void reserves(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Reserves", "tenos_reserves", icon("mana_percentile"), x, y);
    }

    private static void careful(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Careful", "tenos_careful", icon("trap_disarm"), x, y);
    }

    private static void wideInfluence(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Wide Influence", "tenos_wide_influence", icon("area_of_effect"), x, y);
    }

    private static void advancedExtraction(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Advanced Extraction", "tenos_advanced_extraction", icon("copiously"), x, y);
    }

    private static void tenosPiousDevotion(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Pious Devotion", "tenos_pious_devotion", icon("smite"), x, y);
    }

    private static void hoarderII(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Hoarder II", "tenos_hoarder_ii", icon("item_quantity"), x, y);
    }

    private static void treasurerII(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Treasurer II", "tenos_treasurer_ii", icon("item_rarity"), x, y);
    }

    private static void magicalII(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Magical II", "tenos_magical_ii", icon("mana_regeneration"), x, y);
    }

    private static void reservesII(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Reserves II", "tenos_reserves_ii", icon("mana_percentile"), x, y);
    }

    private static void carefulII(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Careful II", "tenos_careful_ii", icon("trap_disarm"), x, y);
    }

    private static void wideInfluenceII(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Wide Influence II", "tenos_wide_influence_ii", icon("area_of_effect"), x, y);
    }

    private static void advancedExtractionII(GodTreeBuilder tree, String id, int x, int y) {
        tree.stat(id, "Advanced Extraction II", "tenos_advanced_extraction_ii", icon("copiously"), x, y);
    }

    private static String icon(String name) {
        return "the_vault:textures/gui/greed/nodes/" + name + ".png";
    }
}
