package xyz.iwolfking.woldsvaults.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.DataGenerator;
import iskallia.vault.util.PlayerBerserkHelper;
import xyz.iwolfking.woldsvaults.api.cast.LuckyReset;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractConfigPatchProvider;
import xyz.iwolfking.woldsvaults.datagen.patches.GreedTraderPatches;
import xyz.iwolfking.woldsvaults.datagen.patches.SpellHastePatches;
import xyz.iwolfking.woldsvaults.events.ExecuteEvents;
import xyz.iwolfking.woldsvaults.milestones.MilestoneRankLadder;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Every change Wold's Vaults makes to a base Vault Hunters config, as field-level patches the game applies when it
 * reads that config. Content Wold's Vaults adds as whole new entries goes through the VHAPI overlay providers; this
 * provider holds the edits to entries upstream owns, so upstream keeps every value it is not patched on.
 */
public class ModConfigPatchesProvider extends AbstractConfigPatchProvider {
    public static final String STEEL_INFERNO = "Steel Inferno";
    public static final int EXECUTION_STRIKE_TIERS = 29;
    public static final double WEAVER_OF_TIME_HASTE_CAP = 1.0;

    private static final int UPSTREAM_PRESTIGE_HEADERS = 12;
    private static final int PRESTIGE_HEADER_COLUMNS = 3;
    private static final int PRESTIGE_HEADER_LEFT = -150;
    private static final int PRESTIGE_HEADER_TOP = -22;
    private static final int PRESTIGE_HEADER_COLUMN_STEP = 130;
    private static final int PRESTIGE_HEADER_ROW_STEP = 72;
    private static final int PRESTIGE_HEADER_COLOR = 0xFF808080;
    private static final int PRESTIGE_HEADER_TEXT_COLOR = 0xFFFFFFFF;

    public ModConfigPatchesProvider(DataGenerator generator) {
        super(generator);
    }

    @Override
    public String getName() {
        return "Wolds Vaults base config patches";
    }

    /** How a boss's execute threshold reads next to a player's: "half" at 0.5, otherwise a percent and {@code of}. */
    public static String bossThresholdShare() {
        float factor = ExecuteEvents.BOSS_THRESHOLD_FACTOR;
        return factor == 0.5F ? "half" : Math.round(factor * 100) + "% of";
    }

    /** Executing Strike's execute threshold at a talent tier (0-based): 4% at the first tier, then 2% per tier. */
    public static double executionThreshold(int tier) {
        return (4 + 2 * tier) / 100.0;
    }

    @Override
    protected void registerPatches() {
        talents();
        prestigePowers();
        prestigeScreen();
        abilities();
        descriptions();
        gearModifiers();
        uniqueGear();
        etchings();
        deckCores();
        mapChestLoot();
        sigils();
        misc();
        GreedTraderPatches.register(patch("greed/greed_trader.json"));
        SpellHastePatches.register(this::patch);
    }

    private void talents() {
        Patch talents = patch("talents.json");
        for (int tier = 0; tier < EXECUTION_STRIKE_TIERS; tier++) {
            talents.at("tree", "skills", id("Execution_Strike"), "tiers", tier)
                    .set("type", "execution_lucky_hit")
                    .set("damageIncrease", executionThreshold(tier));
        }
    }

    private void prestigePowers() {
        Patch prestige = patch("prestige_powers.json");
        prestigeRank(prestige, "StepUpPower", "step_up_1", 3, 200);
        prestigeRank(prestige, "BlackMarketRerolls", "black_market_rerolls_1", 4, 400);
        prestigeRank(prestige, "CrateTier", "crate_tier_1", 5, 600);
        prestigeRank(prestige, "NoTrinketUses", "trinket_uses_1", 8, 1200);
        prestigeRank(prestige, "BlackMarketer", "black_marketer_1", 10, 1600);
        prestigeRank(prestige, "HealthIncrease", "health_increase_1", 11, 1800);
        prestigeRank(prestige, "PrismaticPouch", "prism_possessor_1", 13, 2400);
        prestigeRank(prestige, "MasterfulPower", "masterful_power_1", 13, 2400);
        prestigeRank(prestige, "BarrierOfResilience", "barrier_of_resilience_1", 13, 2400);
        prestigeRank(prestige, "ShieldOfLastingGuard", "shield_of_lasting_guard_1", 14, 2800);
        prestigeRank(prestige, "WeaverOfTime", "weaver_of_time_1", 14, 2800);
        prestige.at("tree", "skills", id("WeaverOfTime"), "tiers", id("weaver_of_time_1")).set("value", WEAVER_OF_TIME_HASTE_CAP);
        prestige.at("tree", "skills", id("Toolsmith"), "tiers", 0).set("capacityIncrease", 50);
        prestige.at("tree", "skills", id("SuperCrystals"), "tiers", id("super_crystals_1")).set("extraCapacity", 75);
        prestige.at("tree", "skills", id("Fruity"), "tiers", id("fruity_1")).set("value", 0.5);
    }

    private static void prestigeRank(Patch prestige, String power, String tierId, int greedRank, int knowledgeCost) {
        prestige.at("tree", "skills", id(power), "tiers", id(tierId))
                .set("learnKnowledgeCost", knowledgeCost)
                .set("tier", greedRank)
                .set("requiredGreedTier", greedRank);
    }

    private void prestigeScreen() {
        Patch screen = patch("prestige_powers_gui_styles.json");
        Node headers = screen.at("headers");
        for (int rank = MilestoneRankLadder.FIRST_RANK; rank <= MilestoneRankLadder.LEGEND_RANK; rank++) {
            int index = rank - MilestoneRankLadder.FIRST_RANK;
            if (index < UPSTREAM_PRESTIGE_HEADERS) {
                headers.at(index).set("text", ModLanguageProvider.rankName(rank));
                continue;
            }
            JsonObject header = new JsonObject();
            header.addProperty("text", ModLanguageProvider.rankName(rank));
            header.addProperty("x", PRESTIGE_HEADER_LEFT + PRESTIGE_HEADER_COLUMN_STEP * (index % PRESTIGE_HEADER_COLUMNS));
            header.addProperty("y", PRESTIGE_HEADER_TOP + PRESTIGE_HEADER_ROW_STEP * (index / PRESTIGE_HEADER_COLUMNS));
            header.addProperty("width", 100);
            header.addProperty("height", 16);
            header.addProperty("headerColor", PRESTIGE_HEADER_COLOR);
            header.addProperty("textColor", PRESTIGE_HEADER_TEXT_COLOR);
            header.addProperty("containerHeight", 50);
            headers.append(header);
        }

        Node styles = screen.at("styles");
        styles.at("BountyCooldownReduction").set("x", -130);
        styles.at("EnderAnchorPower").set("x", -100);
        styles.at("StepUpPower").set("x", 160).set("y", 10);
        styles.at("BlackMarketRerolls").set("x", -70).set("y", 82);
        styles.at("CrateTier").set("x", 30);
        styles.at("NoTrinketUses").set("x", 30).set("y", 154);
        styles.at("BlackMarketer").set("x", -130).set("y", 226);
        styles.at("MovementSpeedMultiplier").set("x", -70);
        styles.at("HealthIncrease").set("x", 60).set("y", 226);
        styles.at("PrismaticPouch").set("x", -130).set("y", 298);
        styles.at("BarrierOfResilience").set("x", -100).set("y", 298);
        styles.at("MasterfulPower").set("x", -70).set("y", 298);
        styles.at("WeaverOfTime").set("x", 0).set("y", 298);
        styles.at("ShieldOfLastingGuard").set("x", 60).set("y", 298);
        styles.add("GodsDisciple", prestigeStyle(160, 82, "the_spark"));
        styles.add("ToolsmithII", prestigeStyle(0, 154, "toolsmith"));
        styles.add("SuperCrystalsII", prestigeStyle(130, 154, "super_crystals"));
        styles.add("GodsDiscipleII", prestigeStyle(160, 226, "the_petal"));
        styles.add("RelicHunter", prestigeStyle(190, 226, "the_carapace"));
        styles.add("SpiritsHandII", prestigeStyle(30, 298, "spirits_hand"));
        styles.add("FruityII", prestigeStyle(130, 298, "fruity"));
        styles.add("SuperCrystalsIII", prestigeStyle(160, 298, "super_crystals"));
        styles.add("RelicHunterII", prestigeStyle(190, 298, "the_shadow"));
        styles.add("LegendOfTime", prestigeStyle(-130, 370, "the_wings"));
        styles.add("LegendOfPower", prestigeStyle(-100, 370, "triple_jump"));
        styles.add("LegendOfRiches", prestigeStyle(-70, 370, "chain_miner"));
    }

    private static JsonObject prestigeStyle(int x, int y, String icon) {
        JsonObject style = new JsonObject();
        style.addProperty("x", x);
        style.addProperty("y", y);
        style.addProperty("frameType", "RECTANGULAR");
        style.addProperty("icon", "the_vault:gui/prestige/" + icon);
        style.addProperty("inactiveIcon", "the_vault:gui/prestige/" + icon + "_inactive");
        return style;
    }

    private void abilities() {
        Patch abilities = patch("abilities.json");
        double[] manaPerDamage = {8.0, 7.5, 7.0, 6.5, 6.0, 5.5, 5.0, 4.5, 4.2, 3.9, 3.6, 3.3, 3.0, 2.7, 2.4, 2.2, 1.9, 1.8, 1.7,
                1.6, 1.5, 1.4, 1.3, 1.2, 1.1, 1.0, 0.9, 0.8, 0.7, 0.6};
        for (int tier = 1; tier <= manaPerDamage.length; tier++) {
            abilityTier(abilities, "UltimateShield", "UltimateShield_Base", "ultimate_shield_" + tier).set("manaPerDamageScalar", manaPerDamage[tier - 1]);
        }
        for (int tier = 1; tier <= 30; tier++) {
            abilityTier(abilities, "Expunge", "Concentrate_Base", "concentrate_" + tier).add("maxChainProcs", 10 + tier / 3);
            abilityTier(abilities, "Battle_Cry", "Battle_Cry_Spectral_Strike", "battle_cry_spectral_strike_" + tier).set("abilityPowerPerStack", 0.1);
        }
        Node chainMiner = abilities.at("tree", "skills", id("Vein_Miner"), "specializations", id("Vein_Miner_Chain"), "tiers");
        for (int tier = 31; tier <= 38; tier++) {
            JsonObject entry = new JsonObject();
            entry.addProperty("type", "vein_miner_chain");
            entry.addProperty("blockLimit", tier + 2);
            entry.addProperty("range", 6);
            entry.addProperty("learnPointCost", 1);
            entry.addProperty("regretPointCost", 1);
            entry.addProperty("cooldownTicks", 0);
            entry.addProperty("unlockLevel", 0);
            entry.addProperty("id", "chain_miner_" + tier);
            chainMiner.append(entry);
        }
        abilityTier(abilities, "Ice_Bolt", "Ice_Bolt_Base", "ice_bolt_arrow_12")
                .set("damagePerBolt", 17)
                .add("percentAttackDamageDealt", 0.7)
                .add("splinterChance", 0.53)
                .add("splinterDamageMultiplier", 0.5)
                .add("splinterRange", 4);

        JsonObject stirrings = new JsonObject();
        stirrings.addProperty("type", "specialized");
        stirrings.addProperty("id", "Stirrings_Of_Power");
        stirrings.addProperty("name", "Stirrings of Power");
        stirrings.addProperty("maxLearnableTier", 1);
        JsonArray specializations = new JsonArray();
        specializations.add(singleTierAbility("Stirrings_Of_Power_Dormant", "Stirrings of Power", "stirrings_of_power", 0));
        specializations.add(singleTierAbility("Cope_De_Grace", "Cope de Grace", "cope_de_grace", 6000));
        specializations.add(singleTierAbility("Savior", "Savior", "savior", 6000));
        specializations.add(singleTierAbility("Eyes_Of_God", "Eyes of God", "eyes_of_god", 6000));
        specializations.add(singleTierAbility("Bullet_Time", "Bullet Time", "bullet_time", 6000));
        stirrings.add("specializations", specializations);
        abilities.at("tree", "skills").append(stirrings);
    }

    private static Node abilityTier(Patch abilities, String ability, String specialization, String tierId) {
        return abilities.at("tree", "skills", id(ability), "specializations", id(specialization), "tiers", id(tierId));
    }

    private static JsonObject singleTierAbility(String id, String name, String type, int cooldownTicks) {
        JsonObject tier = new JsonObject();
        tier.addProperty("type", type);
        tier.addProperty("manaCost", 0.0);
        tier.addProperty("learnPointCost", 0);
        tier.addProperty("regretPointCost", 0);
        tier.addProperty("cooldownTicks", cooldownTicks);
        tier.addProperty("unlockLevel", 0);
        tier.addProperty("id", type + "_1");
        JsonArray tiers = new JsonArray();
        tiers.add(tier);
        JsonObject specialization = new JsonObject();
        specialization.addProperty("type", "tiered");
        specialization.addProperty("id", id);
        specialization.addProperty("name", name);
        specialization.addProperty("maxLearnableTier", 1);
        specialization.add("tiers", tiers);
        return specialization;
    }

    private void descriptions() {
        Node berserk = patch("skill_descriptions.json").at("descriptions", "BerserkPower");
        berserk.at(3).set("text", Math.round(berserkDamagePerStack() * 100) + "%");
        berserk.at(4).set("text", " every time you kill a mob, this effect can stack ");
        berserk.at(5).set("text", PlayerBerserkHelper.MAX_STACKS + " times");
        berserk.at(7).set("text", PlayerBerserkHelper.DURATION_TICKS / 20 + " seconds");

        Node spectralStrike = patch("abilities_descriptions.json").at("data", "Battle_Cry_Spectral_Strike", "description");
        spectralStrike.at(4).set("text", "of stacks on each qualifying hit: swings and fang hits add ");
        spectralStrike.at(6).set("text", "(a share of your base ability power per stack) as damage, and ability hits gain the same share per stack.\nThe stacks will fizzle after a ");
    }

    /** Berserk's attack damage per stack, a private constant of the base mod's berserk effect. */
    private static float berserkDamagePerStack() {
        try {
            Field field = PlayerBerserkHelper.BerserkEffect.class.getDeclaredField("MULTIPLIER_PER_STACK");
            field.setAccessible(true);
            return field.getFloat(null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Berserk's per-stack multiplier is no longer readable; update the BerserkPower text patch", e);
        }
    }

    private void gearModifiers() {
        List<String> weapons = List.of("axe", "battlestaff", "focus", "loot_sack", "magnet", "plushie", "rang", "sword", "trident", "wand");
        List<String> armour = List.of("boots", "chestplate", "helmet", "leggings");
        for (String gear : weapons) {
            removeCorruptedImplicits(patch("gear_modifiers/" + gear + ".json"), "the_vault:u_frost_nova_on_damage", "the_vault:storm_aoe_reduce", "the_vault:storm_cooldown");
            removeCorruptedImplicits(patch("gear_modifiers/" + gear + "_mythic.json"), "the_vault:u_frost_nova_on_damage", "the_vault:storm_aoe_reduce", "the_vault:storm_cooldown");
        }
        removeCorruptedImplicits(patch("gear_modifiers/shield_mythic.json"), "the_vault:u_frost_nova_on_damage", "the_vault:storm_aoe_reduce", "the_vault:storm_cooldown");
        for (String gear : armour) {
            removeCorruptedImplicits(patch("gear_modifiers/" + gear + ".json"), "the_vault:storm_aoe_reduce", "the_vault:storm_cooldown");
            removeCorruptedImplicits(patch("gear_modifiers/" + gear + "_mythic.json"), "the_vault:storm_aoe_reduce", "the_vault:storm_cooldown");
        }

        Patch unique = patch("gear_modifiers/unique.json");
        removeCorruptedImplicits(unique, "the_vault:seal_storm_aoe_reduce", "the_vault:seal_storm_cooldown");
        unique.at("modifierGroup", "PREFIX").remove(where("identifier", "the_vault:fireball_aoe_increase"));
        Node kitsune = unique.at("modifierGroup", "PREFIX", where("identifier", "the_vault:mod_execution_young_kitsune"), "tiers");
        kitsune.at(0, "value").set("min", 0.015).set("max", 0.025).set("step", 0.005);
        kitsune.at(1, "value").set("min", 0.03).set("max", 0.05).set("step", 0.005);

        patch("gear_model_roll_rarities.json").at("ARMOR_MODEL_ROLLS", "OMEGA").remove(value(null));

        for (String gear : UNSHIPPED_GEAR_TABLES) {
            patch("gear_modifiers/" + gear + ".json").create(emptyGearTable());
        }
    }

    /**
     * Gear tier tables the game registers but upstream ships no file for. The base mod writes its placeholder table
     * (armor, attack damage, poison avoidance, regen cloud, fire) for a missing file and VHAPI appends the addon's
     * rows onto it, so each of these is created empty instead.
     */
    private static final List<String> UNSHIPPED_GEAR_TABLES = List.of(
            "scepter_mythic", "dagger_mythic", "dagger_sub", "dagger_sub_mythic", "unique_mythic", "vault_necklace_mythic",
            "idol_benevolent_mythic", "idol_malevolence_mythic", "idol_omniscient_mythic", "idol_timekeeper_mythic",
            "jewel_mythic", "void_stone_mythic");

    private static JsonObject emptyGearTable() {
        JsonObject groups = new JsonObject();
        for (String group : new String[]{"BASE_ATTRIBUTES", "IMPLICIT", "PREFIX", "SUFFIX", "CRAFTED_PREFIX", "CRAFTED_SUFFIX", "CORRUPTED_IMPLICIT"}) {
            groups.add(group, new JsonArray());
        }
        JsonObject table = new JsonObject();
        table.add("modifierGroup", groups);
        return table;
    }

    private static void removeCorruptedImplicits(Patch gear, String... identifiers) {
        for (String identifier : identifiers) {
            gear.at("modifierGroup", "CORRUPTED_IMPLICIT").remove(where("identifier", identifier));
        }
    }

    private void uniqueGear() {
        Node registry = patch("unique_gear.json").at("registry");
        registry.at("the_vault:stormcrown", "modifierIdentifiers", "PREFIX").append("the_vault:u_storm_tracking");
        registry.at("the_vault:warbound_helmet", "modifierIdentifiers", "PREFIX").append("the_vault:u_javelin_multi_hit");
        registry.at("the_vault:warbound_helmet", "modifierIdentifiers", "SUFFIX").append("the_vault:u_splitting_javelins");
        registry.at("the_vault:sweetheart", "modifierIdentifiers", "SUFFIX").append("the_vault:u_health_boost");
        registry.at("the_vault:crashguards", "modifierIdentifiers", "SUFFIX").append("the_vault:u_crashwave");
        registry.at("the_vault:inferno_reach").set("name", STEEL_INFERNO);
        registry.at("the_vault:inferno_reach", "modifierIdentifiers", "SUFFIX").set(value("the_vault:fireball_aoe_increase"), "the_vault:u_steel_inferno");

        Node page = patch("unique_codex.json").at("pages", where("uniqueId", "the_vault:inferno_reach"));
        page.at("title", "description").set("text", STEEL_INFERNO);
        page.at("description", "description", 1).set("text", STEEL_INFERNO);
    }

    private void etchings() {
        int share = Math.round(LuckyReset.REMAINING_SHARE * 100);
        patch("gear/etchings.json").at("ETCHINGS", "the_vault:etching_lucky_cooldown_reset")
                .set("description", "<gray>Proccing <#6DF5A3>Lucky Hit <gray>reduces the remaining cooldown of <yellow>%svalue/ability <gray>by <yellow>"
                        + share + "%<gray>, at most once per ability cast")
                .set("typeGroups", jsonArray("HELMET"));
    }

    private void deckCores() {
        Node values = patch("card/deck_modifiers.json").at("values");
        coreRange(values, "equilibrium", 0.12, 0.2, 0.06, 0.2, 0.36);
        coreRange(values, "shiny", 0.3, 0.6, 0.2, 0.6, 0.9);
        for (String colour : new String[]{"crimson", "azure", "viridian", "golden"}) {
            values.at(colour, "modifierValue").set("max", 0.3);
            values.at(colour, "modifierValues", "greater", "value").set("max", 0.6);
        }
        values.at("pure", "modifierValue").set("max", 0.03);
        values.at("pure", "modifierValues", "greater", "value").set("min", 0.03).set("max", 0.05);

        Node empyreal = values.at("empyreal");
        empyreal.set("type", "slot_multiplier");
        empyreal.at("modifierValue").set("min", 0.2).set("max", 0.3);
        empyreal.at("slotRoll").set("max", 2);
        empyreal.at("slotRolls", "lesser", "value").set("min", 0.2).set("max", 0.2);
        JsonObject greater = new JsonObject();
        greater.add("value", uniform(0.3, 0.4));
        greater.add("requiredGroups", jsonArray("Stat", "Evolution"));
        greater.add("requiredColors", new JsonArray());
        greater.add("slotRoll", uniform(3, 3));
        greater.addProperty("modelId", "the_vault:deck_cores/greater_empyreal_deck_core#inventory");
        greater.addProperty("name", "Greater Empyreal Core");
        greater.addProperty("colour", 16769382);
        empyreal.at("slotRolls").add("greater", greater);

        patch("recipes/deck_recipes.json").at("deckRecipes").remove(id("the_vault:mystery_deck"));
    }

    private static void coreRange(Node values, String core, double min, double max, double lesser, double greaterMin, double greaterMax) {
        values.at(core, "modifierValue").set("min", min).set("max", max);
        values.at(core, "modifierValues", "lesser", "value").set("min", lesser).set("max", lesser);
        values.at(core, "modifierValues", "greater", "value").set("min", greaterMin).set("max", greaterMax);
    }

    private void mapChestLoot() {
        mythicCharm("living_chest_100_map", 0.12, 0.03);
        mythicCharm("living_strongbox_100_map", 0.106, 0.018);
        blueprints("ornate_chest_100_map", 2.0, 0.56, "power_core", "radiator", "shield_array", "scanner", "drill");
        blueprints("ornate_strongbox_100_map", 6.4, 4.5, "chassis", "power_core", "drill", "legs", "shield_array", "radiator", "scanner", "attachment");
        blueprints("wooden_chest_map", 0.36, 0.31, "chassis", "legs", "attachment", "drill");
        royaleWeapons();
    }

    /** Adds the scepter and dagger to the Vault Royale tables upstream left out, each at the trident's weight. */
    private void royaleWeapons() {
        JsonObject completion = new JsonObject();
        completion.addProperty("the_vault:gear_roll_type_pool", "gear_completion");
        JsonObject legendary = new JsonObject();
        legendary.addProperty("the_vault:is_legendary", "true");
        legendary.addProperty("the_vault:gear_roll_type_pool", "gear_treasure");
        Node gear = patch("gen/1.0/loot_tables/royale/royale_loot_gear.json").at("entries", 0, "pool", 0, "pool");
        Node legendaryGear = patch("gen/1.0/loot_tables/royale/royale_loot_legendary.json").at("entries", 0, "pool", 0, "pool");
        Patch crate = patch("gen/1.0/loot_tables/royale_crate.json");
        for (String weapon : new String[]{"the_vault:scepter", "the_vault:dagger"}) {
            gear.append(lootEntry(16, weapon, completion));
            legendaryGear.append(lootEntry(80, weapon, legendary));
            crate.at("entries", 0, "pool").append(royaleCrateEntry(50, weapon, "Royale_Poor"));
            crate.at("entries", 1, "pool").append(royaleCrateEntry(50, weapon, "Royale"));
        }
    }

    private static JsonObject royaleCrateEntry(int weight, String item, String rollType) {
        JsonObject stack = new JsonObject();
        stack.addProperty("id", item);
        stack.addProperty("nbt", "{\"the_vault:gear_roll_type\": \"" + rollType + "\", \"the_vault:fullfill_all_affix\": true}");
        JsonObject count = new JsonObject();
        count.addProperty("type", "constant");
        count.addProperty("count", 1);
        stack.add("count", count);
        JsonObject entry = new JsonObject();
        entry.addProperty("weight", weight);
        entry.add("item", stack);
        return entry;
    }

    private void mythicCharm(String table, double rareWeight, double jackpotWeight) {
        JsonObject nbt = new JsonObject();
        nbt.addProperty("the_vault:gear_roll_type", "Mythic");
        Patch loot = patch("gen/1.0/loot_tables/" + table + ".json");
        lootPool(loot, 2).append(lootEntry(rareWeight, "woldsvaults:mythic_vault_charm", nbt));
        lootPool(loot, 0.00445).append(lootEntry(jackpotWeight, "woldsvaults:mythic_vault_charm", nbt));
    }

    private void blueprints(String table, double rareWeight, double jackpotWeight, String... parts) {
        Patch loot = patch("gen/1.0/loot_tables/" + table + ".json");
        for (String part : parts) {
            lootPool(loot, 2).append(lootEntry(rareWeight, "woldsvaults:" + part + "_blueprint", null));
        }
        for (String part : parts) {
            lootPool(loot, 0.00445).append(lootEntry(jackpotWeight, "woldsvaults:" + part + "_blueprint", null));
        }
    }

    private static Node lootPool(Patch loot, double poolWeight) {
        return loot.at("entries", 0, "pool", where("weight", poolWeight), "pool");
    }

    private static JsonObject lootEntry(double weight, String item, JsonObject nbt) {
        JsonObject stack = new JsonObject();
        stack.addProperty("id", item);
        JsonObject count = new JsonObject();
        count.addProperty("type", "uniform");
        count.addProperty("min", 1);
        count.addProperty("max", 1);
        stack.add("count", count);
        if (nbt != null) {
            stack.add("nbt", nbt);
        }
        JsonObject entry = new JsonObject();
        entry.addProperty("weight", weight);
        entry.add("item", stack);
        return entry;
    }

    private void sigils() {
        JsonArray neutralLevels = new JsonArray();
        neutralLevels.add(neutralSigilLevel());
        for (String sigil : new String[]{"adept", "expert", "master", "veteran", "legend"}) {
            patch("sigils/" + sigil + ".json").at().set("LEVELS", neutralLevels);
        }
        Node definitions = patch("sigils/definitions.json").at("sigils");
        for (int level = 70; level <= 100; level += 5) {
            JsonObject definition = new JsonObject();
            definition.addProperty("displayName", "Ancient");
            definition.addProperty("color", "#FFA500");
            definition.addProperty("modelId", "the_vault:sigil/legend_sigil#inventory");
            definition.addProperty("difficulty", 6.0 + (level - 70) / 10.0);
            definitions.add("ancient_" + level, definition);
            JsonObject tier = new JsonObject();
            tier.add("LEVELS", neutralLevels);
            patch("sigils/ancient_" + level + ".json").create(tier);
        }
    }

    private static JsonObject neutralSigilLevel() {
        JsonObject level = new JsonObject();
        level.addProperty("level", 0);
        level.add("monolithTarget", uniform(5, 5));
        JsonObject runes = new JsonObject();
        runes.addProperty("count", 5);
        runes.addProperty("type", "constant");
        level.add("runeBossMinRunes", runes);
        level.addProperty("bingoPool", "the_vault:default");
        level.addProperty("scavengerPool", "the_vault:default");
        level.addProperty("extraXp", 0.0);
        level.addProperty("xpMultiplier", 1.0);
        level.addProperty("crateTierModifierId", "the_vault:crate_tier");
        level.addProperty("extraCrateTiers", 0);
        level.addProperty("elixirTargetMultiplier", 1.0);
        level.addProperty("monolithModifiers", "the_vault:brazier_new");
        level.addProperty("monolithPillageModifiers", "the_vault:brazier_pillage");
        level.addProperty("monolithPillageLootTable", "the_vault:brazier_lvl0");
        return level;
    }

    private void misc() {
        Node descriptions = patch("menu_player_stat_description.json").at("MOD_GEAR_ATTRIBUTE_DESCRIPTIONS");
        descriptions.set("the_vault:healing_effectiveness", "Your \u00A7beffectiveness\u00A77 of \u00A7bhealing\u00A77 through \u00A7aany method except Leech\u00A77");
        descriptions.set("the_vault:execution_damage", "Hitting a target at or below this \u00A7bpercentage\u00A77 of its max health kills it; bosses use " + bossThresholdShare() + (bossThresholdShare().equals("half") ? "" : " it"));
        descriptions.add("the_vault:thorns_damage_flat_percentile", "Your \u00A7bpercentage increase\u00A77 of your flat \u00A7aThorns\u00A77 damage");

        patch("vault_rune_boss.json").at("WaveBlast").set("Radius", 15.0);
        patch("attribute_cap_overrides.json").at().set("luckyHitCap", 10.0);

        Node allowedBlocks = patch("void_crucible.json").at("allowedBlocks");
        for (String block : new String[]{"ornate_chain_rusty", "chromatic_planks", "stripped_driftwood_log", "driftwood_planks",
                "ornate_bricks_cracked", "ornate_bricks_chipped", "vault_stone_bricks_cracked", "ornate_bricks_velvet_stairs",
                "gilded_bricks_stairs", "vault_stone_bricks", "ornate_bricks_velvet_slab", "ornate_bricks_netherite", "ornate_bricks",
                "ornate_bricks_rusty", "gilded_cobble_stairs", "rotten_meat_block", "ancient_copper_block_pillar_weathered",
                "ancient_copper_trapdoor", "ancient_copper_bricks", "ancient_copper_block_pillar_exposed", "ancient_copper_block_pillar",
                "ancient_copper_bricks_exposed", "driftwood_slab", "driftwood_stairs", "vault_ash", "polished_vault_stone",
                "chromatic_stairs", "stripped_chromatic_log", "chromatic_slab"}) {
            allowedBlocks.append("the_vault:" + block);
        }

        Node otherResources = patch("void_crucible/misc.json").at("resources");
        otherResources.remove(value("minecraft:writeable_book"));
        for (String resource : new String[]{"minecraft:writable_book", "botania:stripped_livingwood_log",
                "immersiveengineering:steel_scaffolding_grate_top", "chipped:yellow_concrete_10", "tropicraft:lime_sapling",
                "hexcasting:amethyst_dust", "tropicraft:lime", "everycomp:db/botania/livingwood_support", "hexcasting:charged_amethyst",
                "quark:blue_blossom_sapling", "quark:lavender_blossom_sapling", "quark:orange_blossom_sapling", "quark:pink_blossom_sapling",
                "quark:red_blossom_sapling", "quark:blossom_post", "quark:dripstone_bricks_wall", "twigs:calcite_slab",
                "ecologics:walnut_sapling", "neoncraft2:wallneon_number_2_white", "neoncraft2:wallneon_number_0_white",
                "neoncraft2:wallneon_number_1_white", "minecraft:melon_slice", "chipped:netherrack_21", "buildersdelight:prismarine_5",
                "chipped:blackstone_14", "chipped:purpur_block_1", "chipped:blackstone_32", "minecraft:warped_stem", "chipped:netherrack_7",
                "chipped:blackstone_8", "chipped:magma_block_56", "chipped:red_mushroom_5", "minecraft:melon_seeds",
                "chipped:redstone_block_58", "chipped:vine_6", "rechiseled:iron_block_processed_connecting", "decorative_blocks:crimson_seat",
                "minecraft:stone_pickaxe", "minecraft:wooden_shovel", "minecraft:leather_horse_armor", "neoncraft2:wallneon_arrow_down_white",
                "occultism:spirit_lantern", "minecraft:raw_copper", "minecraft:crimson_sign", "supplementaries:sconce_soul",
                "create:belt_connector", "tropicraft:ashen_mask_screw_attack", "tropicraft:ashen_mask_square_horn",
                "tropicraft:ashen_mask_square_zord", "tropicraft:ashen_mask_horn_monkey", "tropicraft:ashen_mask_headinator",
                "tropicraft:ashen_mask_invader", "tropicraft:ashen_mask_mojo", "tropicraft:ashen_mask_enigma",
                "tropicraft:ashen_mask_warthog", "tropicraft:ashen_mask_oblongatron", "tropicraft:ashen_mask_bat_boy",
                "tropicraft:ashen_mask_the_heart", "tropicraft:ashen_mask_the_brain", "the_vault:driftwood_slab",
                "the_vault:driftwood_stairs", "the_vault:vault_ash", "the_vault:chromatic_slab"}) {
            otherResources.append(resource);
        }
    }

    private static JsonObject uniform(Number min, Number max) {
        JsonObject roll = new JsonObject();
        roll.addProperty("min", min);
        roll.addProperty("max", max);
        roll.addProperty("type", "uniform");
        return roll;
    }

    private static JsonArray jsonArray(String... values) {
        JsonArray array = new JsonArray();
        for (String value : values) {
            array.add(value);
        }
        return array;
    }
}
