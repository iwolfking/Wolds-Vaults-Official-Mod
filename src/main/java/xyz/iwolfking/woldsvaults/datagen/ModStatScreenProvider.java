package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.config.ColorsConfig;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.init.ModGearAttributes;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.woldsvaults.config.StatScreenConfig;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractWoldsTableProvider;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The H menu Statistics tab layout: player stats grouped into Offensive, Defensive, Looting and Misc, and the
 * colours.json tag each stat value is drawn in. Every tag is checked against the seeded colours config so a typo fails
 * the data run instead of drawing the missing-colour magenta.
 */
public class ModStatScreenProvider extends AbstractWoldsTableProvider {
    private static final String OFFENSIVE = "Offensive";
    private static final String DEFENSIVE = "Defensive";
    private static final String LOOTING = "Looting";
    private static final String MISC = "Misc";
    private static final int MISSING_COLOR = 0xFF00AA;

    public ModStatScreenProvider(DataGenerator generator) {
        super(generator, "gui/stat_screen");
    }

    @Override
    public void registerConfigs() {
        StatScreenConfig config = layout();
        checkColors(config);
        this.table("player_stats", config);
    }

    public static StatScreenConfig layout() {
        StatScreenConfig config = new StatScreenConfig();
        config.categories = new ArrayList<>(List.of(OFFENSIVE, DEFENSIVE, LOOTING, MISC));
        Map<String, StatScreenConfig.Row> stats = new LinkedHashMap<>();

        stat(stats, ModGearAttributes.ABILITY_POWER, OFFENSIVE, "ability_power");
        stat(stats, xyz.iwolfking.woldsvaults.init.ModGearAttributes.AP_SCALING_DAMAGE, OFFENSIVE, "ability_power");
        stat(stats, ModGearAttributes.AREA_OF_EFFECT, OFFENSIVE, "areaOfEffect");
        stat(stats, ModGearAttributes.DAMAGE_ASSASSIN, OFFENSIVE, "damage");
        stat(stats, ModGearAttributes.ATTACK_DAMAGE, OFFENSIVE, "damage");
        stat(stats, ModGearAttributes.ATTACK_RANGE, OFFENSIVE, "range");
        stat(stats, ModGearAttributes.ATTACK_SPEED, OFFENSIVE, "speed");
        stat(stats, xyz.iwolfking.woldsvaults.init.ModGearAttributes.REAVING_DAMAGE, OFFENSIVE, "damage");
        stat(stats, ModGearAttributes.ON_HIT_CHAIN, OFFENSIVE, "chains");
        stat(stats, ModGearAttributes.DAMAGE_CHAMPION, OFFENSIVE, "damage");
        stat(stats, xyz.iwolfking.woldsvaults.init.ModGearAttributes.CHANNELING_CHANCE, OFFENSIVE, "chance");
        stat(stats, ModGearAttributes.COOLDOWN_REDUCTION, OFFENSIVE, "cooldown");
        label(stats, "Damage Increase", OFFENSIVE, "damage");
        stat(stats, ModGearAttributes.DOUBLE_HIT_CHANCE, OFFENSIVE, "chance");
        stat(stats, ModGearAttributes.DAMAGE_DUNGEON, OFFENSIVE, "damage");
        stat(stats, ModGearAttributes.DAMAGE_DWELLER, OFFENSIVE, "damage");
        stat(stats, xyz.iwolfking.woldsvaults.init.ModGearAttributes.ECHOING_CHANCE, OFFENSIVE, "chance");
        stat(stats, xyz.iwolfking.woldsvaults.init.ModGearAttributes.INCREASED_EFFECT_CLOUD_CHANCE, OFFENSIVE, "chance");
        stat(stats, ModGearAttributes.EFFECT_DURATION, OFFENSIVE, "duration");
        stat(stats, xyz.iwolfking.woldsvaults.init.ModGearAttributes.EXECUTION_DAMAGE, OFFENSIVE, "damage");
        stat(stats, xyz.iwolfking.woldsvaults.init.ModGearAttributes.HEXING_CHANCE, OFFENSIVE, "chance");
        stat(stats, ModGearAttributes.DAMAGE_HORDE, OFFENSIVE, "damage");
        stat(stats, xyz.iwolfking.woldsvaults.init.ModGearAttributes.ECHOING_DAMAGE, OFFENSIVE, "damage");
        stat(stats, xyz.iwolfking.woldsvaults.init.ModGearAttributes.THORNS_FLAT_PERCENT, OFFENSIVE, "thornsDamage");
        stat(stats, ModGearAttributes.MANA_ADDITIVE, OFFENSIVE, "manaCost");
        stat(stats, ModGearAttributes.MANA_REGEN_ADDITIVE_PERCENTILE, OFFENSIVE, "manaCost");
        stat(stats, xyz.iwolfking.woldsvaults.init.ModGearAttributes.CHAINING_DAMAGE, OFFENSIVE, "chaining");
        stat(stats, ModGearAttributes.SHOCKING_HIT_CHANCE, OFFENSIVE, "shocking");
        stat(stats, ModGearAttributes.ON_HIT_STUN, OFFENSIVE, "stun");
        stat(stats, ModGearAttributes.SWEEPING_HIT_CHANCE, OFFENSIVE, "chance");
        stat(stats, ModGearAttributes.DAMAGE_TANK, OFFENSIVE, "damage");
        stat(stats, ModGearAttributes.THORNS_DAMAGE_FLAT, OFFENSIVE, "thornsDamage");
        stat(stats, xyz.iwolfking.woldsvaults.init.ModGearAttributes.THORNS_SCALING_DAMAGE, OFFENSIVE, "thornsDamage");

        stat(stats, ModGearAttributes.ARMOR, DEFENSIVE, "armor");
        stat(stats, ModGearAttributes.BLOCK, DEFENSIVE, "absorb");
        stat(stats, ModGearAttributes.CRITICAL_HIT_TAKEN_REDUCTION, DEFENSIVE, "resistance");
        stat(stats, xyz.iwolfking.woldsvaults.init.ModGearAttributes.DODGE_PERCENT, DEFENSIVE, "ethereal");
        stat(stats, ModGearAttributes.HEALING_EFFECTIVENESS, DEFENSIVE, "heal");
        stat(stats, ModGearAttributes.HEALTH, DEFENSIVE, "health");
        stat(stats, ModGearAttributes.KNOCKBACK_RESISTANCE, DEFENSIVE, "knockback");
        stat(stats, ModGearAttributes.RESISTANCE, DEFENSIVE, "resistance");
        stat(stats, xyz.iwolfking.woldsvaults.init.ModGearAttributes.SOUL_LEECH_FLAT, DEFENSIVE, "leech");

        stat(stats, ModGearAttributes.COPIOUSLY, LOOTING, "fortune");
        stat(stats, xyz.iwolfking.woldsvaults.init.ModGearAttributes.DISMANTLE_CHANCE, LOOTING, "blocks");
        stat(stats, ModGearAttributes.HIT_HEARTS, LOOTING, "regret");
        stat(stats, ModGearAttributes.ITEM_QUANTITY, LOOTING, "itemquantity");
        stat(stats, ModGearAttributes.ITEM_RARITY, LOOTING, "itemrarity");
        stat(stats, ModGearAttributes.LUCKY_HIT_CHANCE, LOOTING, "luckyHit");
        stat(stats, ModGearAttributes.SOUL_QUANTITY, LOOTING, "soulchance");
        stat(stats, ModGearAttributes.TRAP_DISARMING, LOOTING, "chance");

        stat(stats, ModGearAttributes.DURABILITY_WEAR_REDUCTION, MISC, "durabilityWearReduction");
        stat(stats, ModGearAttributes.FRUIT_EFFECTIVENESS, MISC, "living");
        stat(stats, ModGearAttributes.IDONA_AFFINITY, MISC, "idona");
        stat(stats, ModGearAttributes.MINING_SPEED, MISC, "miningspeed");
        stat(stats, ModGearAttributes.MINING_SPEED_PERCENT, MISC, "miningspeed");
        stat(stats, ModGearAttributes.MOVEMENT_SPEED, MISC, "speed");
        label(stats, "Pickup Range", MISC, "distance");
        stat(stats, ModGearAttributes.VELOCITY, MISC, "force");
        stat(stats, ModGearAttributes.REACH, MISC, "range");
        stat(stats, ModGearAttributes.TENOS_AFFINITY, MISC, "tenos");
        stat(stats, ModGearAttributes.VELARA_AFFINITY, MISC, "velara");
        stat(stats, ModGearAttributes.WENDARR_AFFINITY, MISC, "wendarr");
        config.stats = stats;

        Map<String, String> prominent = new LinkedHashMap<>();
        prominent.put("Damage", "damage");
        prominent.put("Health", "health");
        prominent.put("Defense", "resistance");
        prominent.put("Eff. HP", "absorb");
        prominent.put("Mana", "manaCost");
        prominent.put("Greed Tier", "uniqueHighlight");
        config.prominent = prominent;
        return config;
    }

    private static void stat(Map<String, StatScreenConfig.Row> stats, VaultGearAttribute<?> attribute, String category, String color) {
        put(stats, attribute.getRegistryName().toString(), category, color);
    }

    private static void label(Map<String, StatScreenConfig.Row> stats, String label, String category, String color) {
        put(stats, StatScreenConfig.LABEL_PREFIX + label, category, color);
    }

    private static void put(Map<String, StatScreenConfig.Row> stats, String key, String category, String color) {
        if (stats.put(key, new StatScreenConfig.Row(category, color)) != null) {
            throw new IllegalStateException("Stat screen layout lists " + key + " twice");
        }
    }

    private static void checkColors(StatScreenConfig config) {
        ColorsConfig colors = new ColorsConfig().readConfig();
        List<String> tags = new ArrayList<>(config.prominent.values());
        config.stats.values().forEach(row -> tags.add(row.color));
        for (String tag : tags) {
            if (colors.getColor(tag).getValue() == MISSING_COLOR) {
                throw new IllegalStateException("Stat screen colour tag '" + tag + "' is not in the_vault colors.json");
            }
        }
    }
}
