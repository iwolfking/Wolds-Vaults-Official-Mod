package xyz.iwolfking.woldsvaults.api.util;

import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * A damage source that is meant to arrive at its authored value whatever the target is wearing.
 *
 * <p>The three vanilla bypasses are the same set the base mod uses for its own unnegatable hits -
 * {@code EliteHuskEntity} and {@code VaultMod.DMG_VAULT_TIMER} both build a source this way - and
 * between them they skip armor and toughness, Resistance and Protection, and invulnerability frames.
 *
 * <p>Vanilla mitigation is only half the problem in this pack: god nodes, gear attributes and Second
 * Chance all reduce damage from event handlers that never look at these flags. The authored amount
 * is therefore carried on the source itself so {@link VaultTrueDamage}'s final-stage floor can
 * restore it after every one of them has had its turn.</p>
 */
public class TrueDamageSource extends EntityDamageSource {
    private final float authoredAmount;

    public TrueDamageSource(LivingEntity attacker, float authoredAmount) {
        super("mob", attacker);
        this.authoredAmount = authoredAmount;
        this.bypassArmor();
        this.bypassInvul();
        this.bypassMagic();
    }

    public float getAuthoredAmount() {
        return this.authoredAmount;
    }
}
