package xyz.iwolfking.woldsvaults.api.util.ref;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class Effect {
    private final MobEffect effect;
    private final Integer amplitude;
    private final Integer duration;

    public Effect(MobEffect effect, Integer amplitude, Integer duration) {
        this.effect = effect;
        this.amplitude = amplitude;
        this.duration = duration;
    }

    public void apply(LivingEntity target) {
        target.addEffect(getInstance());
    }

    public MobEffectInstance getInstance() {
        return new MobEffectInstance(this.effect, this.duration, this.amplitude);
    }
}
