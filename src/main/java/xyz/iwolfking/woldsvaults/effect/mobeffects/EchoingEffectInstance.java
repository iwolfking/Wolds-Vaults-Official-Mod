package xyz.iwolfking.woldsvaults.effect.mobeffects;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import xyz.iwolfking.woldsvaults.init.ModEffects;
import xyz.iwolfking.woldsvaults.mixins.accessors.MobEffectInstanceAccessor;

public class EchoingEffectInstance extends MobEffectInstance {

    private Player attacker;
    float damage;
    DamageSource source;
    float decay;
    boolean noLuckyhit;
    boolean noCleave;

    public Player getAttacker() {
        return this.attacker;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }
    public float getDamage() {
        return this.damage;
    }

    public DamageSource getSource() {
        return this.source;
    }
    public float getDecay() {
        return this.decay;
    }

    public EchoingEffectInstance(EchoingEffectInstance echo, int duration) {
        this(echo.attacker, echo.damage, echo.source, duration, echo.decay, echo.noLuckyhit, echo.noCleave);
    }
    public EchoingEffectInstance(EchoingEffectInstance echo) {
        this(echo.attacker, echo.damage, echo.source, echo.getDuration(), echo.decay, echo.noLuckyhit, echo.noCleave);
        this.doDecay();
    }
    public EchoingEffectInstance(Player attacker, float damage, DamageSource source, int pDuration, float decay, boolean noLuckyhit, boolean noCleave) {
        super(ModEffects.ECHOING, pDuration);
        this.attacker = attacker;
        this.damage = damage;
        this.source = source;
        this.decay = decay;
        this.noLuckyhit = noLuckyhit;
        this.noCleave = noCleave;
    }

    public void doDecay() {
        this.decay = this.decay * 0.95F - 0.05F;
    }

    void setDetailsFrom(EchoingEffectInstance echo) {
        ((MobEffectInstanceAccessor) this).setDuration(echo.getDuration());
        this.attacker = echo.attacker;
        this.damage = echo.damage;
        this.source = echo.source;
        this.decay = echo.decay;
        this.noLuckyhit = echo.noLuckyhit;
        this.noCleave = echo.noCleave;
    }

    private void tickDownDuration() {
        MobEffectInstanceAccessor thiis = ((MobEffectInstanceAccessor) this);

        if (thiis.getHiddenEffect() instanceof EchoingEffectInstance hidden) {
            hidden.tickDownDuration();
        }

        thiis.setDuration(this.getDuration()-1);
    }

    public void remove() {
        MobEffectInstanceAccessor thiis = ((MobEffectInstanceAccessor) this);
        if (thiis.getHiddenEffect() != null) {
            this.setDetailsFrom((EchoingEffectInstance) thiis.getHiddenEffect());
            thiis.setHiddenEffect(((MobEffectInstanceAccessor) thiis.getHiddenEffect()).getHiddenEffect());
        }
    }

    public void reverberate() {
        if(this.getDuration() > 11) {
            EchoingEffectInstance reverb = new EchoingEffectInstance(this, this.getDuration()-10);
            this.remove();
            this.update(reverb);
        }
        else
            this.remove();
    }

    @Override
    public boolean tick(LivingEntity pEntity, Runnable pOnExpirationRunnable) {
        if (this.getDuration() > 0) {
            if (this.getEffect().isDurationEffectTick(this.getDuration(),0)) {
                this.applyEffect(pEntity);
            }

            this.tickDownDuration();
            if (this.getDuration() == 0) {
                this.remove();
                pOnExpirationRunnable.run();
            }
        }

        return this.getDuration() > 0;
    }

    @Override
    public boolean update(MobEffectInstance other) {
        if(other instanceof EchoingEffectInstance oEcho) {
            boolean didUpdate = false;

            if(oEcho.damage > this.damage) {
                if(this.getDuration() < oEcho.getDuration()) {
                    ((MobEffectInstanceAccessor) this).setHiddenEffect(new EchoingEffectInstance(this));
                    ((MobEffectInstanceAccessor) this).setHiddenEffect(((MobEffectInstanceAccessor) this).getHiddenEffect());
                }

                this.damage = oEcho.damage;
                this.decay = oEcho.decay;
                ((MobEffectInstanceAccessor) this).setDuration(oEcho.getDuration());
                didUpdate = true;
            } else if (oEcho.getDuration() > this.getDuration()) {
                if (this.damage == oEcho.damage) {
                    int newDur = (oEcho.getDuration() / 10) * 10 + Math.min((oEcho.getDuration()-1) % 10, (this.getDuration()-1) % 10) + 1;
                    ((MobEffectInstanceAccessor) this).setDuration(newDur);
                    this.decay = oEcho.decay;
                    didUpdate = true;
                } else
                if (((MobEffectInstanceAccessor) this).getHiddenEffect() == null)
                    ((MobEffectInstanceAccessor) this).setHiddenEffect(new EchoingEffectInstance(oEcho));
                else
                    ((MobEffectInstanceAccessor) this).getHiddenEffect().update(oEcho);
            }

            return didUpdate;
        }
        else
            return super.update(other);
    }
}
