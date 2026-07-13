package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractVaultModifierPoolsProvider;
import xyz.iwolfking.vhapi.api.datagen.lib.modifier_pools.ModifierPoolBuilder;
import xyz.iwolfking.woldsvaults.objectives.hyper.HyperModifierPolicy;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ModVaultModifierPoolsProvider extends AbstractVaultModifierPoolsProvider {
    public ModVaultModifierPoolsProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }


    /**
     * The six source pools below are defined once as data so the derived woldsvaults
     * hyper pools (bottom of addFiles) can be computed from them minus
     * {@link HyperModifierPolicy#BANNED} at datagen time — an upstream edit to a source
     * pool propagates into the hyper pools at the next runData instead of drifting.
     */
    private record PoolMember(String id, int weight) {
    }

    private static PoolMember m(String id, int weight) {
        return new PoolMember(id, weight);
    }

    private static final List<PoolMember> BASIC_NEGATIVE = List.of(
            m("the_vault:trapped", 1),
            m("the_vault:inert", 1),
            m("the_vault:antiheal", 1),
            m("the_vault:chunky_mobs", 1),
            m("the_vault:wild", 1),
            m("the_vault:furious_mobs", 1),
            m("the_vault:ruthless_mobs", 1),
            m("the_vault:infuriated_mobs", 1),
            m("the_vault:draining", 1),
            m("the_vault:injured", 1),
            m("the_vault:frail", 1),
            m("the_vault:weakened", 1));

    private static final List<PoolMember> MEDIUM_NEGATIVE = List.of(
            m("the_vault:bingo_infernal", 1),
            m("the_vault:companion_challenge", 1),
            m("the_vault:dangerous", 1),
            m("the_vault:rending", 1),
            m("the_vault:weakened_t2", 1),
            m("the_vault:acidic", 1),
            m("the_vault:bingo_drained", 1),
            m("the_vault:slowed", 1),
            m("the_vault:hunger", 1),
            m("the_vault:injured", 1),
            m("the_vault:draining", 1),
            m("the_vault:mob_increase", 1),
            m("the_vault:brutal_mobs", 1),
            m("the_vault:ruthless_mobs", 1),
            m("the_vault:chunky_mobs2", 1),
            m("the_vault:critical_mobs", 1),
            m("the_vault:archaic", 1),
            m("the_vault:wild", 1),
            m("the_vault:trapped", 1),
            m("the_vault:drought", 1),
            m("the_vault:haunting", 1),
            m("the_vault:stunning", 1),
            m("the_vault:poisonous", 1),
            m("woldsvaults:fleet_footed_mobs", 1),
            m("woldsvaults:phantasmal_mobs", 1),
            m("woldsvaults:resistant_mobs", 1),
            m("the_vault:weakened_powers", 1));

    private static final List<PoolMember> OMEGA_NEGATIVE = List.of(
            m("the_vault:corroded_veins", 1),
            m("the_vault:daycare_effect", 1),
            m("the_vault:haunted_mansion", 1),
            m("the_vault:classic_retro_mini", 1),
            m("the_vault:sweet_retro", 1),
            m("the_vault:surprise_boxes", 1),
            m("the_vault:armed_chest", 1),
            m("the_vault:piercing", 1),
            m("the_vault:nullifying", 1),
            m("the_vault:corrosive", 1),
            m("the_vault:mana_void", 1),
            m("the_vault:lost_quantity", 1),
            m("the_vault:hunger", 1),
            m("the_vault:abusive_mobs", 1),
            m("the_vault:ethereal", 1),
            m("the_vault:soulless", 1),
            m("the_vault:fading", 1),
            m("the_vault:enervated", 1),
            m("the_vault:vulnerable", 1),
            m("the_vault:weakened_t3", 1),
            m("the_vault:frenzy", 1),
            m("woldsvaults:witch_party", 1),
            m("woldsvaults:ghost_party", 1));

    private static final List<PoolMember> MOB_ONHITS = List.of(
            m("the_vault:stunning", 1),
            m("the_vault:dark", 1),
            m("the_vault:poisonous", 1),
            m("the_vault:toxic", 1),
            m("the_vault:wither", 1),
            m("the_vault:haunting", 1),
            m("the_vault:freezing", 1),
            m("woldsvaults:bleeding_mobs", 1),
            m("the_vault:voiding", 1));

    private static final List<PoolMember> CONCEALED_CHAOS = List.of(
            m("the_vault:coin_cascade", 8),
            m("the_vault:super_coin_cascade", 4),
            m("the_vault:gilded_cascade", 8),
            m("the_vault:ornate_cascade", 8),
            m("the_vault:living_cascade", 8),
            m("the_vault:wooden_cascade", 8),
            m("the_vault:super_gilded_cascade", 4),
            m("the_vault:super_ornate_cascade", 4),
            m("the_vault:super_living_cascade", 4),
            m("the_vault:super_wooden_cascade", 4),
            m("the_vault:item_quantity2", 8),
            m("the_vault:pristine", 8),
            m("the_vault:pandoras_box", 4),
            m("woldsvaults:cardboard_boxes", 4),
            m("the_vault:gilded", 6),
            m("the_vault:living", 6),
            m("the_vault:ornate", 6),
            m("the_vault:wooden", 6),
            m("the_vault:coin_pile", 6),
            m("the_vault:prosperous", 4),
            m("the_vault:prismatic", 4),
            m("the_vault:hoard", 2),
            m("the_vault:treasure", 2),
            m("the_vault:soul_surge", 2),
            m("the_vault:super_crate_tier", 1),
            m("the_vault:crate_tier", 4),
            m("the_vault:unhinged_mob_increase", 1),
            m("the_vault:super_stronk", 2),
            m("the_vault:phoenix", 3),
            m("the_vault:leeching", 1),
            m("the_vault:extended", 8),
            m("the_vault:champion_chance", 4),
            m("the_vault:door_hunter", 1),
            m("the_vault:mildly_enchanted", 1),
            m("the_vault:omega_cascade", 2),
            m("the_vault:omega_bonus", 1),
            m("the_vault:bronze_nuke", 1),
            m("the_vault:goblin_quantity", 1),
            m("the_vault:overpower", 3),
            m("the_vault:cull", 1),
            m("the_vault:champions_realm", 1),
            m("the_vault:no_crit_mobs", 1),
            m("the_vault:weak_mobs_damage", 1),
            m("the_vault:more_mobs", 4),
            m("the_vault:mana_regen", 1),
            m("the_vault:mega_regen", 1),
            m("the_vault:opulent_ores", 3),
            m("the_vault:perfect_ores", 1),
            m("the_vault:plentiful", 6),
            m("the_vault:super_plentiful", 2),
            m("the_vault:enlighted", 1),
            m("the_vault:objective_hunter", 1),
            m("the_vault:unchallenge_stack", 3),
            m("the_vault:daycare", 1),
            m("the_vault:more_champ_drops", 1),
            m("the_vault:companion_hunt", 1));

    private static final List<PoolMember> CONCEALED_CHAOS_BACKFIRE = List.of(
            m("the_vault:wild", 8),
            m("the_vault:ruthless_mobs", 8),
            m("the_vault:chunky_mobs2", 8),
            m("the_vault:trapped", 3),
            m("the_vault:coin_cascade", 4),
            m("the_vault:super_coin_cascade", 2),
            m("the_vault:gilded_cascade", 4),
            m("the_vault:ornate_cascade", 4),
            m("the_vault:living_cascade", 4),
            m("the_vault:wooden_cascade", 4),
            m("the_vault:super_gilded_cascade", 2),
            m("the_vault:super_ornate_cascade", 2),
            m("the_vault:super_living_cascade", 2),
            m("the_vault:super_wooden_cascade", 2),
            m("the_vault:item_quantity", 6),
            m("the_vault:item_rarity", 6),
            m("the_vault:gilded", 4),
            m("the_vault:living", 4),
            m("the_vault:ornate", 4),
            m("the_vault:wooden", 4),
            m("the_vault:coin_pile", 6),
            m("the_vault:prosperous", 4),
            m("the_vault:prismatic", 4),
            m("the_vault:hoard", 1),
            m("the_vault:treasure", 1),
            m("the_vault:soul_surge", 2),
            m("the_vault:volcanic", 6),
            m("the_vault:void_pools", 6),
            m("the_vault:fungal", 6),
            m("the_vault:safari", 6),
            m("the_vault:winter", 6),
            m("the_vault:electric", 6),
            m("the_vault:explosive", 6),
            m("the_vault:super_crate_tier", 1),
            m("the_vault:critical_mobs", 8),
            m("the_vault:enraged_mobs", 8),
            m("the_vault:unhinged_mob_increase", 8),
            m("the_vault:drought", 8),
            m("the_vault:acidic", 6),
            m("the_vault:super_stronk", 2),
            m("the_vault:phoenix", 8),
            m("the_vault:locked", 4),
            m("the_vault:rotten", 4),
            m("the_vault:rending", 8),
            m("the_vault:dangerous", 8),
            m("the_vault:orematic", 8),
            m("the_vault:leeching", 1),
            m("the_vault:extended", 2),
            m("the_vault:champion_chance", 1),
            m("the_vault:door_hunter", 1),
            m("the_vault:chemical_bath", 6),
            m("the_vault:abusive_mobs", 8),
            m("the_vault:mildly_enchanted", 2),
            m("the_vault:armed_chest", 4),
            m("the_vault:surprise_boxes", 4),
            m("the_vault:sweet_retro", 2),
            m("the_vault:classic_retro", 2),
            m("the_vault:haunted_mansion", 2),
            m("the_vault:bingo_infernal", 4),
            m("the_vault:omega_cascade", 4),
            m("the_vault:bronze_nuke", 1),
            m("the_vault:goblin_quantity", 1),
            m("the_vault:chunky_mobs4", 6),
            m("the_vault:rigged", 6),
            m("the_vault:wounded", 6),
            m("the_vault:frenzy", 3),
            m("the_vault:mini_mobs", 2),
            m("the_vault:big_mobs", 2),
            m("the_vault:champions_realm", 1),
            m("the_vault:challenge_stack", 6),
            m("the_vault:no_companion", 2),
            m("the_vault:creeping_doom", 1),
            m("the_vault:ethereal_mobs", 3),
            m("woldsvaults:phantasmal_mobs", 3),
            m("woldsvaults:regenerating_mobs", 3),
            m("woldsvaults:resistant_mobs", 4),
            m("the_vault:no_champ_drops", 1),
            m("the_vault:no_temporal_shard", 1),
            m("the_vault:weakened_powers", 4),
            m("the_vault:thiccening", 3),
            m("the_vault:abusive_mobs", 3),
            m("the_vault:ticking_clock", 3),
            m("the_vault:weak_limbs", 1),
            m("the_vault:weak_heart", 4),
            m("the_vault:spicy_chili_speed", 4),
            m("the_vault:ice_cold_essence", 4),
            m("the_vault:bubbling_trouble", 3),
            m("the_vault:curse", 1),
            m("the_vault:idona_challenge", 4),
            m("the_vault:wendarr_challenge", 4),
            m("the_vault:tenos_challenge", 4),
            m("the_vault:velara_challenge", 4),
            m("the_vault:nerfed", 4),
            m("the_vault:companion_shortened", 4),
            m("the_vault:super_unextension", 1),
            m("the_vault:no_ores", 1),
            m("the_vault:no_heal", 1),
            m("the_vault:no_souls", 1),
            m("the_vault:true_noxp", 1),
            m("the_vault:corroded_veins", 4),
            m("the_vault:piercing", 3),
            m("the_vault:rigged", 4),
            m("the_vault:slowed", 4),
            m("the_vault:mana_leak", 2),
            m("the_vault:catastrophic_brew", 3),
            m("the_vault:mob_levitate", 2),
            m("the_vault:crit_mobs", 1),
            m("the_vault:unlucky", 4),
            m("the_vault:lost_quantity", 4));

    @SafeVarargs
    private static void addAll(ModifierPoolBuilder.PoolValueListBuilder e, List<PoolMember>... lists) {
        for (List<PoolMember> list : lists) {
            for (PoolMember member : list) {
                e.add(member.id(), member.weight());
            }
        }
    }

    @SafeVarargs
    private static void addAllExceptBanned(ModifierPoolBuilder.PoolValueListBuilder e, List<PoolMember>... lists) {
        for (List<PoolMember> list : lists) {
            for (PoolMember member : list) {
                if (!HyperModifierPolicy.isBanned(member.id())) {
                    e.add(member.id(), member.weight());
                }
            }
        }
    }

    @Override
    public void addFiles(Map<String, Consumer<ModifierPoolBuilder>> map) {
        map.put("wolds_builtin_modifier_pools", b -> {

            b.pool(WoldsVaults.id("haunted_brazier").toString(), pool ->
                    pool.level(0, entries -> {
                        entries.entry(1, 1, e -> {
                            e.add("the_vault:exorcising", 60);
                            e.add("the_vault:super_stronk", 12);
                            e.add("the_vault:item_quantity2", 12);
                            e.add("the_vault:pristine", 12);
                            e.add("the_vault:orematic", 12);
                            e.add("the_vault:champion_chance", 6);
                            e.add("the_vault:disarming", 12);
                            e.add("the_vault:crate_tier", 2);
                            e.add("the_vault:soul_surge", 2);
                            e.add("the_vault:hoard", 1);
                            e.add("the_vault:fortuitous", 2);
                            e.add("the_vault:extended", 8);
                            e.add("the_vault:treasure", 1);
                        });
                        entries.entry(1, 1, e -> {
                            e.add("woldsvaults:bleeding_mobs", 35);
                            e.add("woldsvaults:resistant_mobs", 35);
                            e.add("woldsvaults:phantasmal_mobs", 20);
                            e.add("woldsvaults:fleet_footed_mobs", 30);
                            e.add("woldsvaults:witch_party", 45);
                            e.add("woldsvaults:ghost_party", 60);
                            e.add("woldsvaults:ghost_town", 90);
                            e.add("woldsvaults:ghost_city", 30);
                            e.add("the_vault:haunted_mansion", 35);
                            e.add("the_vault:critical_mobs", 40);
                            e.add("the_vault:brutal_mobs", 40);
                            e.add("the_vault:enraged_mobs", 40);
                            e.add("the_vault:dark", 40);
                            e.add("the_vault:wither", 40);
                            e.add("the_vault:voiding", 40);
                            e.add("the_vault:unhinged_mob_increase", 20);
                            e.add("the_vault:drought", 40);
                            e.add("the_vault:wounded", 40);
                            e.add("the_vault:rotten", 10);
                            e.add("the_vault:rigged", 10);
                            e.add("the_vault:rending", 40);
                            e.add("the_vault:dangerous", 30);
                        });
                    })
            );

            b.pool(WoldsVaults.id("haunted_brazier_pillage").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:abusive_mobs", 20);
                                e.add("the_vault:curse", 10);
                                e.add("the_vault:crit_mobs", 20);
                                e.add("the_vault:chunky_mobs4", 40);
                                e.add("the_vault:enraged_mobs", 40);
                                e.add("the_vault:mana_leak", 20);
                                e.add("the_vault:wounded", 30);
                                e.add("the_vault:corrosive", 10);
                                e.add("the_vault:slowed_t2", 30);
                                e.add("the_vault:weakened_t2", 30);
                                e.add("the_vault:rotten", 10);
                                e.add("the_vault:nullifying", 30);
                                e.add("the_vault:piercing", 25);
                                e.add("the_vault:enervated", 15);
                                e.add("the_vault:chemical_bath", 25);
                                e.add("woldsvaults:ghost_party", 50);
                            })
                    )
            );

            b.pool(WoldsVaults.id("brown_ghost").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:critical_mobs", 10);
                                e.add("the_vault:draining", 12);
                                e.add("the_vault:rotten", 1);
                                e.add("the_vault:dangerous", 4);
                                e.add("the_vault:trapped", 12);
                                e.add("the_vault:chunky_mobs2", 12);
                                e.add("the_vault:ruthless_mobs", 12);
                                e.add("the_vault:mob_increase", 12);
                                e.add("the_vault:inert", 12);
                                e.add("the_vault:frail", 4);
                                e.add("the_vault:archaic", 8);
                                e.add("the_vault:injured", 16);
                                e.add("the_vault:hunger", 4);
                                e.add("the_vault:grievous_wounds", 8);
                                e.add("the_vault:infuriated_mobs", 16);
                                e.add("the_vault:weakened", 8);
                                e.add("the_vault:brutal_mobs", 15);
                                e.add("the_vault:slowed", 4);
                                e.add("the_vault:empty", 240);
                            })
                    )
            );

            b.pool(VaultMod.id("unhinged").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(30, 40, e -> {
                                e.add("the_vault:wild", 8);
                                e.add("the_vault:ruthless_mobs", 8);
                                e.add("the_vault:chunky_mobs2", 8);
                                e.add("the_vault:trapped", 3);
                                e.add("the_vault:coin_cascade", 8);
                                e.add("the_vault:super_coin_cascade", 4);
                                e.add("the_vault:gilded_cascade", 8);
                                e.add("the_vault:ornate_cascade", 8);
                                e.add("the_vault:living_cascade", 8);
                                e.add("the_vault:wooden_cascade", 8);
                                e.add("the_vault:super_gilded_cascade", 4);
                                e.add("the_vault:super_ornate_cascade", 4);
                                e.add("the_vault:super_living_cascade", 4);
                                e.add("the_vault:super_wooden_cascade", 4);
                                e.add("the_vault:item_quantity", 8);
                                e.add("the_vault:item_rarity", 8);
                                e.add("the_vault:gilded", 6);
                                e.add("the_vault:living", 6);
                                e.add("the_vault:ornate", 6);
                                e.add("the_vault:wooden", 6);
                                e.add("the_vault:coin_pile", 6);
                                e.add("the_vault:prosperous", 4);
                                e.add("the_vault:prismatic", 4);
                                e.add("the_vault:hoard", 4);
                                e.add("the_vault:treasure", 4);
                                e.add("the_vault:soul_surge", 4);
                                e.add("the_vault:volcanic", 6);
                                e.add("the_vault:void_pools", 6);
                                e.add("the_vault:fungal", 6);
                                e.add("the_vault:safari", 6);
                                e.add("the_vault:winter", 6);
                                e.add("the_vault:electric", 6);
                                e.add("the_vault:explosive", 6);
                                e.add("the_vault:super_crate_tier", 4);
                                e.add("the_vault:critical_mobs", 8);
                                e.add("the_vault:enraged_mobs", 8);
                                e.add("the_vault:unhinged_mob_increase", 8);
                                e.add("woldsvaults:cardboard_boxes", 4);
                                e.add("the_vault:drought", 8);
                                e.add("the_vault:acidic", 6);
                                e.add("the_vault:super_stronk", 4);
                                e.add("the_vault:phoenix", 8);
                                e.add("the_vault:locked", 4);
                                e.add("the_vault:rotten", 4);
                                e.add("the_vault:rending", 8);
                                e.add("the_vault:dangerous", 8);
                                e.add("the_vault:orematic", 8);
                                e.add("the_vault:leeching", 1);
                                e.add("the_vault:extended", 8);
                                e.add("the_vault:champion_chance", 4);
                                e.add("the_vault:door_hunter", 4);
                                e.add("the_vault:chemical_bath", 4);
                                e.add("the_vault:abusive_mobs", 8);
                                e.add("the_vault:mildly_enchanted", 2);
                                e.add("the_vault:armed_chest", 4);
                                e.add("the_vault:surprise_boxes", 4);
                                e.add("the_vault:sweet_retro", 2);
                                e.add("the_vault:classic_retro", 2);
                                e.add("the_vault:haunted_mansion", 2);
                                e.add("the_vault:bingo_infernal", 4);
                                e.add("the_vault:omega_cascade", 4);
                                e.add("the_vault:bronze_nuke", 1);
                                e.add("the_vault:goblin_quantity", 1);
                                e.add("the_vault:chunky_mobs4", 6);
                                e.add("the_vault:rigged", 6);
                                e.add("the_vault:wounded", 6);
                                e.add("the_vault:frenzy", 1);
                                e.add("the_vault:mini_mobs", 2);
                                e.add("the_vault:big_mobs", 2);
                                e.add("the_vault:champions_realm", 1);
                            })
                    )
            );

            b.pool(VaultMod.id("bingo_task_modifiers_bad").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:mob_increase", 1);
                                e.add("the_vault:harder_mobs", 1);
                                e.add("the_vault:vulnerable", 1);
                                e.add("the_vault:champion_paradox", 1);
                                e.add("the_vault:inert", 1);
                                e.add("the_vault:bingo_trapped", 1);
                                e.add("the_vault:frail", 1);
                                e.add("the_vault:bingo_drained", 1);
                                e.add("the_vault:dark", 1);
                                e.add("the_vault:ruthless_mobs", 1);
                                e.add("the_vault:chunky_mobs2", 1);
                                e.add("the_vault:bingo_critical_mobs", 1);
                                e.add("the_vault:infuriated_mobs", 1);
                                e.add("the_vault:grievous_wounds", 1);
                                e.add("the_vault:infuriated_mobs", 1);
                                e.add("the_vault:archaic", 1);
                                e.add("the_vault:brutal_mobs", 1);
                            })
                    )
            );

            b.pool(VaultMod.id("bingo_task_modifiers").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:energizing", 1);
                                e.add("the_vault:swift", 1);
                                e.add("the_vault:soul_boost", 1);
                                e.add("the_vault:copiously", 1);
                                e.add("the_vault:strength", 1);
                                e.add("the_vault:kill_hunter", 1);
                                e.add("the_vault:bingo_quantity", 1);
                                e.add("the_vault:bingo_rarity", 1);
                                e.add("the_vault:bingo_kill_nuke", 1);
                                e.add("the_vault:bingo_kill_charm", 1);
                                e.add("the_vault:healthy", 1);
                            })
                    )
            );

            b.pool(VaultMod.id("enchanted_cascade").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:gilded_cascade", 4);
                                e.add("the_vault:super_gilded_cascade", 1);
                                e.add("the_vault:living_cascade", 4);
                                e.add("the_vault:super_living_cascade", 1);
                                e.add("the_vault:ornate_cascade", 4);
                                e.add("the_vault:super_ornate_cascade", 1);
                                e.add("the_vault:wooden_cascade", 4);
                                e.add("the_vault:super_wooden_cascade", 1);
                                e.add("the_vault:coin_cascade", 4);
                                e.add("the_vault:super_coin_cascade", 1);
                            })
                    )
            );

            b.pool(VaultMod.id("bb_wither").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:regeneration", 1);
                                e.add("the_vault:energizing", 1);
                                e.add("the_vault:healthy", 1);
                            })
                    )
            );

            b.pool(VaultMod.id("bb_webber").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:swift", 1);
                                e.add("the_vault:springy", 1);
                                e.add("the_vault:disarming", 1);
                            })
                    )
            );

            b.pool(VaultMod.id("bb_weakness").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:strength", 1);
                            })
                    )
            );

            b.pool(VaultMod.id("bb_vengeance").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:reinforced", 1);
                                e.add("the_vault:plated", 1);
                            })
                    )
            );

            b.pool(VaultMod.id("bb_storm").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:item_quantity", 1);
                                e.add("the_vault:item_rarity", 1);
                                e.add("the_vault:plentiful", 1);
                            })
                    )
            );

            b.pool(VaultMod.id("bb_sprint").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:swift", 19);
                                e.add("the_vault:tailwind", 1);
                            })
                    )
            );

            b.pool(VaultMod.id("bb_sapper").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:extended", 2);
                                e.add("the_vault:cherry", 8);
                            })
                    )
            );

            b.pool(VaultMod.id("bb_regen").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:regeneration", 1);
                            })
                    )
            );

            b.pool(VaultMod.id("bb_quicksand").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:item_rarity", 3);
                                e.add("the_vault:copiously", 6);
                                e.add("the_vault:orematic", 1);
                            })
                    )
            );

            b.pool(VaultMod.id("bb_poisonous").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:item_rarity", 3);
                                e.add("the_vault:item_rarity", 6);
                            })
                    )
            );

            b.pool(VaultMod.id("bb_gravity").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:swift", 1);
                                e.add("the_vault:energizing", 1);
                                e.add("the_vault:strength", 1);
                                e.add("the_vault:springy", 1);
                            })
                    )
            );

            b.pool(VaultMod.id("bb_ghastly").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:item_quantity", 1);
                                e.add("the_vault:item_rarity", 1);
                                e.add("the_vault:copiously", 1);
                            })
                    )
            );

            b.pool(VaultMod.id("bb_fiery").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:item_quantity", 1);
                                e.add("the_vault:item_rarity", 1);
                                e.add("the_vault:cherry", 1);
                            })
                    )
            );

            b.pool(VaultMod.id("bb_exhaust").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:strength", 1);
                                e.add("the_vault:swift", 1);
                                e.add("the_vault:cherry", 1);
                            })
                    )
            );

            b.pool(VaultMod.id("bb_darkness").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:copiously", 1);
                                e.add("the_vault:disarming", 1);
                            })
                    )
            );

            b.pool(VaultMod.id("bb_cloaking").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:exorcising", 1);
                            })
                    )
            );

            b.pool(VaultMod.id("bb_choke").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:energizing", 2);
                                e.add("the_vault:serendipitous", 1);
                            })
                    )
            );

            b.pool(VaultMod.id("bb_blastoff").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:plentiful", 1);
                                e.add("the_vault:copiously", 1);
                                e.add("the_vault:cherry", 1);
                            })
                    )
            );

            b.pool(VaultMod.id("bb_berserk").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:strength", 1);
                            })
                    )
            );

            b.pool(VaultMod.id("bb_alchemist").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:energizing", 1);
                                e.add("the_vault:xp_gain", 1);
                            })
                    )
            );

            b.pool(VaultMod.id("bb_1up").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:phoenix", 19);
                                e.add("the_vault:oneup", 1);
                            })
                    )
            );

            b.pool(VaultMod.id("bb_bulwark").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:strength", 1);
                                e.add("the_vault:reinforced", 1);
                                e.add("the_vault:plated", 1);
                            })
                    )
            );

            b.pool(VaultMod.id("curses").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:curse_ethereal", 1);
                                e.add("the_vault:soulless", 1);
                                e.add("the_vault:grievous_wounds", 1);
                                e.add("the_vault:rotten", 1);
                                e.add("the_vault:hunger", 1);
                                e.add("the_vault:mana_leak", 1);
                                e.add("the_vault:voiding", 1);
                                e.add("the_vault:unlucky", 1);
                                e.add("the_vault:lost_quantity", 1);
                                e.add("the_vault:void_pools", 1);
                                e.add("the_vault:explosive", 1);
                            })
                    )
            );

            b.pool(VaultMod.id("omega_positive").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:tailwind", 1);
                                e.add("the_vault:hoard", 1);
                                e.add("the_vault:treasure", 1);
                                e.add("the_vault:soul_surge", 1);
                                e.add("the_vault:prismatic", 1);
                                e.add("the_vault:champion_chance", 1);
                                e.add("the_vault:soul_boost", 1);
                                e.add("the_vault:looters_dream", 1);
                                e.add("the_vault:phoenix", 1);
                                e.add("the_vault:fortuitous", 1);
                                e.add("the_vault:ultimate_regeneration", 1);
                                e.add("the_vault:opulent_ores", 1);
                                e.add("the_vault:perfect_ores", 1);
                            })
                    )
            );

            b.pool(VaultMod.id("medium_positive").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:orematic", 1);
                                e.add("the_vault:plated", 1);
                                e.add("the_vault:healthy", 1);
                                e.add("the_vault:swift", 1);
                                e.add("the_vault:item_rarity", 1);
                                e.add("the_vault:item_quantity", 1);
                                e.add("the_vault:soul_boost", 1);
                                e.add("the_vault:pristine", 1);
                                e.add("the_vault:strength", 1);
                                e.add("the_vault:copiously", 1);
                                e.add("the_vault:xp_gain", 1);
                                e.add("the_vault:champion_paradox", 1);
                                e.add("the_vault:exorcising", 1);
                                e.add("the_vault:low_item_quantity", 1);
                                e.add("the_vault:pogging", 1);
                                e.add("the_vault:sparkling", 1);
                                e.add("the_vault:baby_mobs2", 1);
                            })
                    )
            );

            b.pool(VaultMod.id("omega_negative").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> addAll(e, OMEGA_NEGATIVE))
                    )
            );

            b.pool(VaultMod.id("medium_negative").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> addAll(e, MEDIUM_NEGATIVE))
                    )
            );

            b.pool(VaultMod.id("basic_negative").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> addAll(e, BASIC_NEGATIVE))
                    )
            );

            b.pool(VaultMod.id("basic_positive").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:low_item_quantity", 1);
                                e.add("the_vault:low_item_rarity", 1);
                                e.add("the_vault:energizing", 1);
                                e.add("the_vault:reinforced", 1);
                                e.add("the_vault:strength", 1);
                                e.add("the_vault:copiously", 1);
                                e.add("the_vault:cherry", 1);
                                e.add("the_vault:soul_xp", 1);
                                e.add("the_vault:soul_shards", 1);
                                e.add("the_vault:swift", 1);
                            })
                    )
            );

            b.pool(VaultMod.id("hunters_enchanted").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:all_seeing_eye", 1);
                            })
                    )
            );


            b.pool(VaultMod.id("hunters_enchanted_random").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:vendoor_hunter", 1);
                                e.add("the_vault:treasure_doors", 1);
                                e.add("the_vault:dungeon_doors", 1);
                                e.add("the_vault:living_hunter", 1);
                                e.add("the_vault:coin_hunter", 1);
                                e.add("the_vault:gilded_hunter", 1);
                                e.add("the_vault:ornate_hunter", 1);
                                e.add("the_vault:wooden_hunter", 1);
                                e.add("the_vault:altar_hunter", 1);
                                e.add("the_vault:pylon_hunter", 1);
                            })
                    )
            );

            b.pool(VaultMod.id("chaos_enchanted").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(5, 10, e -> {
                                e.add("the_vault:soul_boost", 1);
                                e.add("the_vault:low_item_quantity", 1);
                                e.add("the_vault:soul_rarity", 1);
                                e.add("the_vault:critical_mobs", 1);
                                e.add("the_vault:chunky_mobs", 1);
                                e.add("the_vault:furious_mobs", 1);
                                e.add("the_vault:wild", 1);
                                e.add("the_vault:infuriated_mobs", 1);
                                e.add("the_vault:draining", 1);
                                e.add("the_vault:frail", 1);
                                e.add("the_vault:inert", 1);
                                e.add("the_vault:strength", 1);
                                e.add("the_vault:copiously", 1);
                                e.add("the_vault:antiheal", 1);
                                e.add("the_vault:cherry", 1);
                                e.add("the_vault:mob_increase", 1);
                                e.add("the_vault:pristine", 1);
                                e.add("the_vault:item_quantity", 1);
                            })
                    )
            );

            b.pool(VaultMod.id("mob_onhits").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> addAll(e, MOB_ONHITS))
                    )
            );

            b.pool(VaultMod.id("leeching").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:leeching", 1);
                            })
                    )
            );

            b.pool(VaultMod.id("gods_omega_blessing").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:idona_dmg", 1);
                                e.add("the_vault:velara_favour_1", 1);
                                e.add("the_vault:tenos_favour_2", 1);
                                e.add("the_vault:wendarr_favour", 1);
                            })
                    )
            );

            b.pool(VaultMod.id("bingos_enchanted").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:safari_bingo", 1);
                                e.add("the_vault:challenge_bingo", 1);
                                e.add("the_vault:boinging_bingo", 1);
                                e.add("the_vault:retro_bingo", 1);
                                e.add("the_vault:surprise_bingo", 1);
                                e.add("the_vault:enchanted_bingo", 1);
                                e.add("the_vault:cursed_bingo", 1);
                                e.add("the_vault:spooky_bingo", 1);
                                e.add("the_vault:infernal_bingo", 1);
                                e.add("the_vault:sweet_bingo", 1);
                                e.add("the_vault:fungal_bingo", 1);
                                e.add("the_vault:electrifying_bingo", 1);
                                e.add("the_vault:freezing_bingo", 1);
                                e.add("the_vault:burning_bingo", 1);
                                e.add("the_vault:mini_bingo", 1);
                                e.add("the_vault:big_bingo", 1);
                                e.add("the_vault:bonus_bingo", 1);
                                e.add("the_vault:bingo", 4);
                                e.add("the_vault:bingos", 1);
                            })
                    )
            );

            b.pool(VaultMod.id("unhinged_bingos").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:safari_bingo", 1);
                                e.add("the_vault:challenge_bingo", 1);
                                e.add("the_vault:boinging_bingo", 1);
                                e.add("the_vault:retro_bingo", 1);
                                e.add("the_vault:surprise_bingo", 1);
                                e.add("the_vault:enchanted_bingo", 1);
                                e.add("the_vault:cursed_bingo", 1);
                                e.add("the_vault:spooky_bingo", 1);
                                e.add("the_vault:infernal_bingo", 1);
                                e.add("the_vault:sweet_bingo", 1);
                                e.add("the_vault:fungal_bingo", 1);
                                e.add("the_vault:electrifying_bingo", 1);
                                e.add("the_vault:freezing_bingo", 1);
                                e.add("the_vault:burning_bingo", 1);
                                e.add("the_vault:mini_bingo", 1);
                                e.add("the_vault:big_bingo", 1);
                                e.add("the_vault:bonus_bingo", 1);
                            }).entry(1, 1, e -> {
                               e.add("the_vault:crate_tier", 1);
                            }
                    )
            ));

            b.pool(VaultMod.id("random_positive").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:gilded", 2);
                                e.add("the_vault:ornate", 2);
                                e.add("the_vault:living", 2);
                                e.add("the_vault:wooden", 2);
                                e.add("the_vault:coin_pile", 2);
                                e.add("the_vault:plentiful", 4);
                                e.add("the_vault:gilded_cascade", 8);
                                e.add("the_vault:ornate_cascade", 8);
                                e.add("the_vault:living_cascade", 8);
                                e.add("the_vault:coin_cascade", 8);
                                e.add("the_vault:wooden_cascade", 8);
                                e.add("the_vault:energizing", 4);
                                e.add("the_vault:item_quantity", 8);
                                e.add("the_vault:item_rarity", 8);
                                e.add("the_vault:refined_experience", 4);
                                e.add("the_vault:richer_ores", 8);
                                e.add("the_vault:soul_boost", 8);
                                e.add("the_vault:item_quantity_2", 4);
                                e.add("the_vault:pristine", 4);
                                e.add("the_vault:disarming", 8);
                                e.add("the_vault:protected", 4);
                            })
                    )
            );

            b.pool(VaultMod.id("corrupted_modifier_pool").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("woldsvaults:bleeding_mobs", 5);
                                e.add("woldsvaults:resistant_mobs", 35);
                                e.add("woldsvaults:phantasmal_mobs", 1);
                                e.add("woldsvaults:fleet_footed_mobs", 30);
                                e.add("woldsvaults:witch_party", 3);
                                e.add("woldsvaults:ghost_party", 3);
                                e.add("the_vault:haunted_mansion", 35);
                                e.add("the_vault:critical_mobs", 40);
                                e.add("the_vault:brutal_mobs", 40);
                                e.add("the_vault:enraged_mobs", 40);
                                e.add("the_vault:dark", 5);
                                e.add("the_vault:wither", 5);
                                e.add("the_vault:voiding", 5);
                                e.add("the_vault:unhinged_mob_increase", 5);
                                e.add("the_vault:drought", 40);
                                e.add("the_vault:rigged", 40);
                                e.add("the_vault:rending", 40);
                                e.add("the_vault:dangerous", 40);
                                e.add("the_vault:wild", 40);
                                e.add("the_vault:collapsing", 40);
                                e.add("the_vault:poor", 40);
                                e.add("the_vault:lost_quantity", 20);
                                e.add("the_vault:critical_mobs", 20);
                                e.add("the_vault:chunky_mobs2", 30);
                                e.add("the_vault:chunky_mobs4", 10);
                                e.add("the_vault:ruthless_mobs", 20);
                                e.add("the_vault:brutal_mobs", 10);
                                e.add("the_vault:infuriated_mobs", 25);
                                e.add("the_vault:enraged_mobs", 20);
                                e.add("the_vault:fatiguing", 5);
                                e.add("the_vault:freezing", 5);
                                e.add("the_vault:poisonous", 5);
                                e.add("the_vault:stunning", 5);
                                e.add("the_vault:mob_increase", 20);
                                e.add("the_vault:crowded", 10);
                                e.add("the_vault:drought", 20);
                                e.add("the_vault:mana_void", 10);
                                e.add("the_vault:frail", 20);
                                e.add("the_vault:hunger", 10);
                                e.add("the_vault:slowed", 10);
                                e.add("the_vault:weakened_t2", 10);
                                e.add("the_vault:weakened_t3", 5);
                                e.add("the_vault:weakened", 15);
                                e.add("the_vault:grievous_wounds", 15);
                                e.add("the_vault:challenge_stack", 30);
                                e.add("the_vault:curse_collapsing", 5);
                                e.add("the_vault:curse", 5);
                                e.add("the_vault:harder_mobs", 20);
                                e.add("the_vault:abusive_mobs", 10);
                                e.add("the_vault:classic_retro_mini", 2);
                                e.add("the_vault:raging", 15);
                            })
                    )
            );

            b.pool(VaultMod.id("alchemy_positive").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:energizing", 4);
                                e.add("the_vault:sparkling", 1);
                                e.add("the_vault:orematic", 2);
                                e.add("the_vault:soul_xp", 4);
                                e.add("the_vault:item_quantity", 4);
                                e.add("the_vault:item_rarity", 4);
                                e.add("the_vault:copiously", 4);
                                e.add("the_vault:soul_boost", 4);
                            })
                    )
            );

            b.pool(VaultMod.id("alchemy_strong_positive").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:refined_experience", 4);
                                e.add("the_vault:opulent_ores", 4);
                                e.add("the_vault:soul_surge", 2);
                                e.add("the_vault:baby_mobs4", 1);
                                e.add("the_vault:swift", 4);
                                e.add("the_vault:protected", 4);
                                e.add("the_vault:super_stronk", 4);
                                e.add("the_vault:regeneration", 4);
                                e.add("the_vault:looters_lair", 4);
                                e.add("the_vault:pristine", 6);
                                e.add("the_vault:luckier_chests", 6);
                                e.add("the_vault:fortuitous", 1);
                                e.add("the_vault:hoard", 1);
                            })
                    )
            );

            b.pool(VaultMod.id("alchemy_negative").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:ruthless_mobs", 4);
                                e.add("the_vault:chunky_mobs", 4);
                                e.add("the_vault:chunky_mobs2", 2);
                                e.add("the_vault:trapped", 1);
                                e.add("the_vault:infuriated_mobs", 4);
                                e.add("the_vault:draining", 4);
                                e.add("the_vault:mob_increase", 4);
                                e.add("the_vault:weakened_powers", 4);
                                e.add("the_vault:critical_mobs", 4);
                                e.add("woldsvaults:resistant_mobs", 1);
                                e.add("woldsvaults:bleeding_mobs", 2);
                                e.add("the_vault:stunning", 1);
                                e.add("the_vault:dark", 1);
                            })
                    )
            );

            b.pool(VaultMod.id("alchemy_strong_negative").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:brutal_mobs", 4);
                                e.add("the_vault:chunky_mobs4", 4);
                                e.add("the_vault:rigged", 4);
                                e.add("the_vault:lost_quantity", 1);
                                e.add("the_vault:poor", 2);
                                e.add("the_vault:brutal_faster_mobs", 4);
                                e.add("the_vault:ticking_clock", 2);
                                e.add("the_vault:bubbling_trouble", 4);
                                e.add("the_vault:weak_heart", 4);
                                e.add("woldsvaults:bleeding_mobs", 4);
                                e.add("the_vault:rending", 4);
                                e.add("the_vault:mediocre", 4);
                                e.add("the_vault:hearty_mobs", 2);
                                e.add("the_vault:wounded", 4);
                                e.add("the_vault:spicy_chili_speed", 4);
                                e.add("the_vault:ice_cold_essence", 4);
                                e.add("the_vault:chemical_bath", 1);
                                e.add("the_vault:catastrophic_brew", 1);
                                e.add("the_vault:more_mobs2", 4);
                                e.add("the_vault:weakened_t3", 2);
                            })
                    )
            );

            b.pool(WoldsVaults.id("concealed_chaos_backfire").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(15, 15, e -> addAll(e, CONCEALED_CHAOS_BACKFIRE))
                    )
            );

            b.pool(WoldsVaults.id("concealed_chaos").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(15, 15, e -> addAll(e, CONCEALED_CHAOS))
                    )
            );

            // ---- HYPER (derived) ------------------------------------------------------
            // The hyper objective's chaos pools: the source lists above minus
            // HyperModifierPolicy.BANNED, derived here so bans live in exactly one place.
            // hyper_mixed feeds the 25-per-kill dumps, hyper_all_bad the brutal-mini kills,
            // hyper_bad_timer_events the periodic ambient events (and the enchanted-event
            // redirect). To ban a modifier from hyper: add it to the policy, re-run runData.
            b.pool(WoldsVaults.id("hyper_mixed").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(25, 25, e -> addAllExceptBanned(e, CONCEALED_CHAOS, CONCEALED_CHAOS_BACKFIRE))
                    )
            );
            b.pool(WoldsVaults.id("hyper_all_bad").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> addAllExceptBanned(e, CONCEALED_CHAOS_BACKFIRE))
                    )
            );
            b.pool(WoldsVaults.id("hyper_bad_timer_events").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> addAllExceptBanned(e, BASIC_NEGATIVE, MEDIUM_NEGATIVE, OMEGA_NEGATIVE, MOB_ONHITS))
                    )
            );
        });

        map.put("default_override", modifierPoolBuilder -> {
           modifierPoolBuilder.pool(VaultMod.id("default").toString(), poolLevelBuilder -> {
               poolLevelBuilder.level(0, entries -> {
                   entries.entry(1, 1, e -> {
                       e.add("the_vault:beginners_grace", 1);
                   });
                   entries.entry(1, 1, e -> {
                       e.add("the_vault:beginners_insurance", 1);
                   });
               });
               poolLevelBuilder.level(10, entries -> {
                   entries.entry(1, 1, e -> {
                       e.add("the_vault:beginners_grace", 1);
                   });
               });
               poolLevelBuilder.level(20, entries -> {
                   entries.entry(1, 1, e -> {
                       e.add("the_vault:enlighted", 1);
                       e.add("the_vault:hoard", 1);
                       e.add("the_vault:treasure", 1);
                       e.add("the_vault:soul_surge", 1);
                       e.add("the_vault:prismatic", 1);
                       e.add("the_vault:champion_chance", 1);
                       e.add("the_vault:dummy", 12);
                   });
               });
               poolLevelBuilder.level(50, entries -> {
                   entries.entry(1, 2, e -> {
                       e.add("the_vault:enlighted", 1);
                       e.add("the_vault:hoard", 1);
                       e.add("the_vault:treasure", 1);
                       e.add("the_vault:soul_surge", 1);
                       e.add("the_vault:prismatic", 1);
                       e.add("the_vault:champion_chance", 1);
                       e.add("the_vault:prosperous", 1);
                       e.add("the_vault:sparkling", 1);
                       e.add("the_vault:coin_pile", 1);
                       e.add("the_vault:gilded", 1);
                       e.add("the_vault:living", 1);
                       e.add("the_vault:ornate", 1);
                       e.add("the_vault:plated", 1);
                       e.add("the_vault:orematic", 1);
                       e.add("the_vault:plentiful", 1);
                       e.add("the_vault:champion_chance", 1);
                       e.add("the_vault:serendipitous", 1);
                       e.add("the_vault:looters_lair", 1);
                       e.add("the_vault:xp_gain", 1);
                       e.add("the_vault:oneup", 1);
                       e.add("the_vault:dummy", 100);
                   });
               });
               poolLevelBuilder.level(100, entries -> {
                   entries.entry(1, 2, e -> {
                       e.add("the_vault:hoard", 1);
                       e.add("the_vault:treasure", 1);
                       e.add("the_vault:soul_surge", 1);
                       e.add("the_vault:prismatic", 1);
                       e.add("the_vault:champion_chance", 1);
                       e.add("the_vault:prosperous", 1);
                       e.add("the_vault:sparkling", 1);
                       e.add("the_vault:coin_pile", 1);
                       e.add("the_vault:gilded", 1);
                       e.add("the_vault:living", 1);
                       e.add("the_vault:ornate", 1);
                       e.add("the_vault:plated", 1);
                       e.add("the_vault:orematic", 1);
                       e.add("the_vault:plentiful", 1);
                       e.add("the_vault:champion_chance", 1);
                       e.add("the_vault:serendipitous", 1);
                       e.add("the_vault:looters_lair", 1);
                       e.add("the_vault:oneup", 1);
                       e.add("the_vault:dummy", 102);
                   });
               });
           });
        });
    }
}
