package xyz.iwolfking.woldsvaults.api.util;

import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * A damage source that is meant to arrive at its authored value if it lands at all.
 *
 * <p>It bypasses armor and magic - armor and toughness, Resistance and Protection - but
 * deliberately not invulnerability: {@code bypassInvul} is the flag the pack's dodge roll and the
 * base mod's shield block both refuse to roll against, and a true hit is meant to be dodgeable
 * and blockable. Leaving it off is also what keeps creative and spectator players, and anyone
 * inside an immunity window, out of reach. The invulnerability <em>frames</em> a true hit has to
 * land through are handled by {@link VaultTrueDamage#deal} directly; the flag never skipped those.
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
        this.bypassMagic();
    }

    public float getAuthoredAmount() {
        return this.authoredAmount;
    }
}
