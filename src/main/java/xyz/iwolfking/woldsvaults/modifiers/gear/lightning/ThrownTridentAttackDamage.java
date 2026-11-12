package xyz.iwolfking.woldsvaults.modifiers.gear.lightning;

/** A thrown vault trident that remembers its thrower's attack damage from the moment of the throw. */
public interface ThrownTridentAttackDamage {
    void woldsVaults$setThrowAttackDamage(float attackDamage);

    float woldsVaults$getThrowAttackDamage();
}
