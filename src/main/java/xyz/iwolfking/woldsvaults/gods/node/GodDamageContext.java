package xyz.iwolfking.woldsvaults.gods.node;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

/**
 * One hit, as the god node combat pipeline sees it. Mutable by design: each
 * {@link CombatContributor} in the ordered pipeline reads the running amount and may replace it,
 * and the pipeline writes the result back to the event once.
 *
 * <p>{@code percentageBased} is resolved once by the pipeline instead of by each contributor -
 * the two hand-rolled copies of that test in the old per-god handlers had already drifted apart,
 * which is the class of bug this type exists to make impossible.
 */
public final class GodDamageContext {
    private final LivingEntity attacker;
    private final LivingEntity target;
    private final DamageSource source;
    private final boolean percentageBased;
    private float amount;

    public GodDamageContext(@Nullable LivingEntity attacker, LivingEntity target, DamageSource source,
                            float amount, boolean percentageBased) {
        this.attacker = attacker;
        this.target = target;
        this.source = source;
        this.amount = amount;
        this.percentageBased = percentageBased;
    }

    @Nullable
    public LivingEntity getAttacker() {
        return this.attacker;
    }

    public LivingEntity getTarget() {
        return this.target;
    }

    public DamageSource getSource() {
        return this.source;
    }

    public boolean isPercentageBased() {
        return this.percentageBased;
    }

    public float getAmount() {
        return this.amount;
    }

    public void setAmount(float amount) {
        this.amount = Math.max(0.0F, amount);
    }

    public void multiply(float factor) {
        this.setAmount(this.amount * factor);
    }

    public void add(float flat) {
        this.setAmount(this.amount + flat);
    }
}
