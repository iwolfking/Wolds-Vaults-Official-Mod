package xyz.iwolfking.woldsvaults.util;

import iskallia.vault.event.ActiveFlags;

public class WoldEventHelper {
    public static boolean isNormalAttack() {
        return !ActiveFlags.IS_AOE_ATTACKING.isSet()
                && !ActiveFlags.IS_TOTEM_ATTACKING.isSet()
                && !ActiveFlags.IS_CHARMED_ATTACKING.isSet()
                && !ActiveFlags.IS_DOT_ATTACKING.isSet()
                && !ActiveFlags.IS_REFLECT_ATTACKING.isSet()
                && !ActiveFlags.IS_EFFECT_ATTACKING.isSet()
                && !ActiveFlags.IS_AP_ATTACKING.isSet()
                && !ActiveFlags.IS_THORNS_REFLECTING.isSet()
                && !ActiveFlags.IS_SMITE_ATTACKING.isSet()
                && !ActiveFlags.IS_GLACIAL_SHATTER_ATTACKING.isSet()
                ;
    }
}