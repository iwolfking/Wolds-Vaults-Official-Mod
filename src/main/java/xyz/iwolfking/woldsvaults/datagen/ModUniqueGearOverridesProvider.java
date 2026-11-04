package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.init.ModItems;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractUniqueGearOverridesProvider;

public class ModUniqueGearOverridesProvider extends AbstractUniqueGearOverridesProvider {
    public ModUniqueGearOverridesProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        add("greed_unique_gear", builder -> builder
                .entry("the_vault:stormcrown", "Stormcrown", ModItems.HELMET, entry -> entry
                        .model("the_vault:gear/armor/stormcrown/helmet")
                        .model("the_vault:gear/armor/thundercrown_silver/helmet")
                        .model("the_vault:gear/armor/thundercrown_gold/helmet")
                        .base("the_vault:base_durability")
                        .implicit("the_vault:base_ap")
                        .prefix("the_vault:unique_storm_arrow_level", "the_vault:storm_aoe_reduce", "the_vault:storm_cooldown", "the_vault:u_storm_tracking")
                        .suffix("the_vault:mod_mana_regen"))
                .entry("the_vault:warbound_helmet", "Helm of the Warbound", ModItems.HELMET, entry -> entry
                        .model("the_vault:gear/armor/spiral_guardian/helmet")
                        .model("the_vault:gear/armor/helm_of_the_warbound_aquatic/helmet")
                        .model("the_vault:gear/armor/helm_of_the_warbound_amythyst/helmet")
                        .base("the_vault:base_durability")
                        .implicit("the_vault:base_armor")
                        .prefix("the_vault:mod_jav_scatter", "the_vault:mod_damage_increase", "the_vault:u_javelin_multi_hit")
                        .suffix("the_vault:mod_cooldown_reduction", "the_vault:mod_mana_regen", "the_vault:u_splitting_javelins"))
                .entry("the_vault:sweetheart", "The Sweetheart", ModItems.CHESTPLATE, entry -> entry
                        .model("the_vault:gear/armor/sweetheart/chestplate")
                        .base("the_vault:base_durability")
                        .implicit("the_vault:base_health")
                        .prefix("the_vault:mod_health", "the_vault:mod_health")
                        .suffix("the_vault:u_health_boost")
                        .lore("Woven with devotion, its embrace strengthens the body, filling the heart with vitality beyond measure. Few armours guard life as fiercely as this."))
                .entry("the_vault:crashguards", "Crashguards", ModItems.LEGGINGS, entry -> entry
                        .model("the_vault:gear/armor/crashguards/leggings")
                        .model("the_vault:gear/armor/crashguards_safety/leggings")
                        .model("the_vault:gear/armor/crashguards_chute/leggings")
                        .base("the_vault:base_durability")
                        .implicit("the_vault:base_armor", "the_vault:base_mana_additive")
                        .prefix("the_vault:u_mod_armor", "the_vault:mod_resistance", "the_vault:u_knockback_resistance")
                        .suffix("the_vault:mod_kinetic_immunity", "the_vault:u_item_rarity", "the_vault:u_crashwave")
                        .lore("Made for skyborn adventurers, they brace against impacts that would break lesser armour. Mana, resistance, and fortune flow through them, ensuring the fall never ends in ruin."))
                .entry("the_vault:inferno_reach", "Steel Inferno", ModItems.WAND, entry -> entry
                        .model("the_vault:gear/wand/infernos_reach")
                        .model("the_vault:gear/wand/infernos_reach_soul")
                        .model("the_vault:gear/wand/infernos_reach_diorite")
                        .base("the_vault:offhand_base_durability")
                        .implicit("the_vault:base_ap")
                        .prefix("the_vault:mod_ability_increase", "the_vault:mod_added_ability_level_fireball")
                        .suffix("the_vault:u_steel_inferno", "the_vault:mod_cooldown_reduction", "the_vault:unique_area_of_effect")));
    }
}
