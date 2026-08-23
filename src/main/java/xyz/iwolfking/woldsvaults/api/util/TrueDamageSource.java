package xyz.iwolfking.woldsvaults.api.util;

import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * Bypasses armor and magic but not invulnerability, so a true hit stays dodgeable and blockable, and carries its
 * authored amount for {@link VaultTrueDamage}'s floor to restore.
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
