package xyz.iwolfking.woldsvaults.client.init;

import xyz.iwolfking.vhapi.api.registry.PlayerStatEntriesRegistry;
import xyz.iwolfking.woldsvaults.api.data.WoldConstants;
import xyz.iwolfking.woldsvaults.api.util.WoldAttributeHelper;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;

public class ModPlayerStatEntries {
    public static void init() {
        PlayerStatEntriesRegistry registry = PlayerStatEntriesRegistry.INSTANCE;

        // Custom Wold's Vaults attributes
        registry.addPlayerAttribute(
                ModGearAttributes.DODGE_PERCENT,
                WoldAttributeHelper::getDodgeChance,
                player -> WoldConstants.DODGE_PERCENT_CAP,
                null
        );
        registry.addPlayerAttribute(xyz.iwolfking.woldsvaults.init.ModGearAttributes.SOUL_LEECH_FLAT);
        registry.addPlayerAttribute(xyz.iwolfking.woldsvaults.init.ModGearAttributes.AP_SCALING_DAMAGE);
        registry.addPlayerAttribute(xyz.iwolfking.woldsvaults.init.ModGearAttributes.CHAINING_DAMAGE);
        registry.addPlayerAttribute(xyz.iwolfking.woldsvaults.init.ModGearAttributes.THORNS_SCALING_DAMAGE);
        registry.addPlayerAttribute(xyz.iwolfking.woldsvaults.init.ModGearAttributes.CHANNELING_CHANCE);
        registry.addPlayerAttribute(xyz.iwolfking.woldsvaults.init.ModGearAttributes.DISMANTLE_CHANCE);
        registry.addPlayerAttribute(
                xyz.iwolfking.woldsvaults.init.ModGearAttributes.ECHOING_CHANCE,
                WoldAttributeHelper::getEchoingChance,
                player -> 1.0F,
                null
        );
        registry.addPlayerAttribute(xyz.iwolfking.woldsvaults.init.ModGearAttributes.ECHOING_DAMAGE);
        registry.addPlayerAttribute(
                xyz.iwolfking.woldsvaults.init.ModGearAttributes.HEXING_CHANCE,
                WoldAttributeHelper::getHexingChance,
                player -> 1.0F,
                null
        );
        registry.addPlayerAttribute(xyz.iwolfking.woldsvaults.init.ModGearAttributes.EXECUTION_DAMAGE);
        registry.addPlayerAttribute(xyz.iwolfking.woldsvaults.init.ModGearAttributes.REAVING_DAMAGE);
        registry.addPlayerAttribute(xyz.iwolfking.woldsvaults.init.ModGearAttributes.INCREASED_EFFECT_CLOUD_CHANCE);
    }
}
