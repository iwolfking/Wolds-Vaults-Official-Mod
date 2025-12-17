package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractVaultModifierPoolsProvider;
import xyz.iwolfking.vhapi.api.datagen.lib.modifier_pools.ModifierPoolBuilder;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.Map;
import java.util.function.Consumer;

public class ModVaultModifierPoolsProvider extends AbstractVaultModifierPoolsProvider {
    public ModVaultModifierPoolsProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
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
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:corroded_veins", 1);
                                e.add("the_vault:daycare", 1);
                                e.add("the_vault:haunted_mansion", 1);
                                e.add("the_vault:classic_retro_mini", 1);
                                e.add("the_vault:sweet_retro", 1);
                                e.add("the_vault:surprise_boxes", 1);
                                e.add("the_vault:armed_chest", 1);
                                e.add("the_vault:piercing", 1);
                                e.add("the_vault:nullifying", 1);
                                e.add("the_vault:corrosive", 1);
                                e.add("the_vault:mana_void", 1);
                                e.add("the_vault:lost_quantity", 1);
                                e.add("the_vault:hunger", 1);
                                e.add("the_vault:abusive_mobs", 1);
                                e.add("the_vault:ethereal", 1);
                                e.add("the_vault:soulless", 1);
                                e.add("the_vault:fading", 1);
                                e.add("the_vault:enervated", 1);
                                e.add("the_vault:vulnerable", 1);
                                e.add("the_vault:weakened_t3", 1);
                                e.add("the_vault:frenzy", 1);
                                e.add("woldsvaults:witch_party", 1);
                                e.add("woldsvaults:ghost_party", 1);
                            })
                    )
            );

            b.pool(VaultMod.id("medium_negative").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:bingo_infernal", 1);
                                e.add("the_vault:companion_challenge", 1);
                                e.add("the_vault:dangerous", 1);
                                e.add("the_vault:rending", 1);
                                e.add("the_vault:weakened_t2", 1);
                                e.add("the_vault:acidic", 1);
                                e.add("the_vault:bingo_drained", 1);
                                e.add("the_vault:slowed", 1);
                                e.add("the_vault:hunger", 1);
                                e.add("the_vault:injured", 1);
                                e.add("the_vault:draining", 1);
                                e.add("the_vault:mob_increase", 1);
                                e.add("the_vault:brutal_mobs", 1);
                                e.add("the_vault:ruthless_mobs", 1);
                                e.add("the_vault:chunky_mobs2", 1);
                                e.add("the_vault:critical_mobs", 1);
                                e.add("the_vault:archaic", 1);
                                e.add("the_vault:wild", 1);
                                e.add("the_vault:trapped", 1);
                                e.add("the_vault:drought", 1);
                                e.add("the_vault:haunting", 1);
                                e.add("the_vault:stunning", 1);
                                e.add("the_vault:poisonous", 1);
                                e.add("woldsvaults:fleet_footed_mobs", 1);
                                e.add("woldsvaults:phantasmal_mobs", 1);
                                e.add("woldsvaults:resistant_mobs", 1);
                                e.add("the_vault:weakened_powers", 1);
                            })
                    )
            );

            b.pool(VaultMod.id("basic_negative").toString(), pool ->
                    pool.level(0, entries ->
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:trapped", 1);
                                e.add("the_vault:inert", 1);
                                e.add("the_vault:antiheal", 1);
                                e.add("the_vault:chunky_mobs", 1);
                                e.add("the_vault:wild", 1);
                                e.add("the_vault:furious_mobs", 1);
                                e.add("the_vault:ruthless_mobs", 1);
                                e.add("the_vault:infuriated_mobs", 1);
                                e.add("the_vault:draining", 1);
                                e.add("the_vault:injured", 1);
                                e.add("the_vault:frail", 1);
                                e.add("the_vault:weakened", 1);
                            })
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
                            entries.entry(1, 1, e -> {
                                e.add("the_vault:stunning", 1);
                                e.add("the_vault:dark", 1);
                                e.add("the_vault:poisonous", 1);
                                e.add("the_vault:toxic", 1);
                                e.add("the_vault:wither", 1);
                                e.add("the_vault:haunting", 1);
                                e.add("the_vault:freezing", 1);
                                e.add("woldsvaults:bleeding_mobs", 1);
                                e.add("the_vault:voiding", 1);
                            })
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
                                e.add("the_vault:xp_gain", 4);
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
