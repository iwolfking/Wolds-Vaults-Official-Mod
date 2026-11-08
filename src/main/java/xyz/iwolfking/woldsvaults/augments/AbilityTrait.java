package xyz.iwolfking.woldsvaults.augments;

/**
 * What an ability is, used to decide which augments can follow it. INSTANT, TOGGLE and HOLD say how VH casts it;
 * AREA, PROJECTILE, BEAM and BOLT say what the cast produces; MANA says it costs Vault mana.
 */
public enum AbilityTrait {
    INSTANT,
    TOGGLE,
    HOLD,
    AREA,
    PROJECTILE,
    BEAM,
    BOLT,
    MANA
}
