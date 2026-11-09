package xyz.iwolfking.woldsvaults.augments;

/**
 * What an ability is, used to decide which augments can follow it. INSTANT, TOGGLE and HOLD say how VH casts it;
 * AREA, PROJECTILE, BEAM, BOLT and PATH say what the cast produces (PATH: bolts the server flies along a stepped path,
 * with no entity, such as Charged Bolts); MANA says it costs Vault mana.
 */
public enum AbilityTrait {
    INSTANT,
    TOGGLE,
    HOLD,
    AREA,
    PROJECTILE,
    BEAM,
    BOLT,
    PATH,
    MANA
}
