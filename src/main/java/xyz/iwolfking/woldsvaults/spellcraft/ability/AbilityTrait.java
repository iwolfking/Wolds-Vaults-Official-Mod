package xyz.iwolfking.woldsvaults.spellcraft.ability;

/**
 * What an ability is, used to decide which glyphs can follow it. INSTANT, TOGGLE and HOLD say how VH casts it, AREA,
 * PROJECTILE, BEAM, BOLT and PATH what the cast produces (PATH: bolts the server flies along a stepped path with no
 * entity, such as Charged Bolts), and MANA that it costs Vault mana. DAMAGE: it deals player-attributed damage the
 * glyphs reach, DIRECT: it has a direct single-target hit a chain can start from, GRAVITY: its projectile falls,
 * PAYLOAD: it fires at the landing point and may ride in a Bundled Spell payload, LINGER: its damage comes from
 * something it leaves behind for a long time, INTENSIFY and EXTEND: it takes those buff glyphs.
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
    MANA,
    DAMAGE,
    DIRECT,
    GRAVITY,
    PAYLOAD,
    LINGER,
    INTENSIFY,
    EXTEND
}
