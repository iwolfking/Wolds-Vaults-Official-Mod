package xyz.iwolfking.woldsvaults.api.lib;

/** Fireball Volley state one fireball sets on another. */
public interface VolleyFireball {
    /**
     * Turns a freshly made base fireball into a Volley Mitosis child: it flies and explodes on contact like a base
     * fireball but counts as Fireball Volley for its explosion, damage and radius lookups, and never splits.
     */
    void woldsvaults$makeMitosisChild();
}
