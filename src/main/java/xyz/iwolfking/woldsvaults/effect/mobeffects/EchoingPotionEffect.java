package xyz.iwolfking.woldsvaults.effect.mobeffects;

import iskallia.vault.util.damage.DamageUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import xyz.iwolfking.woldsvaults.events.WoldActiveFlags;

public class EchoingPotionEffect extends MobEffect {
    public EchoingPotionEffect(MobEffectCategory pCategory, int pColor, ResourceLocation id) {
        super(pCategory, pColor);
        this.setRegistryName(id);
    }

    private Player attacker = null;
    float damage = 0;
    DamageSource source;

    public void setAttacker(Player attacker) {
        this.attacker = attacker;
    }
    public Player getAttacker() {
        return this.attacker;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }
    public float getDamage() {
        return this.damage;
    }

    public void setSource(DamageSource source) {
        this.source = source;
    }
    public DamageSource getSource() {
        return this.source;
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return (pDuration % 10) == 1;
    }

    @Override
    public void applyEffectTick(LivingEntity affected, int pAmplifier) {
        WoldActiveFlags.IS_ECHOING_ATTACKING.runIfNotSet(() -> {
            DamageUtil.shotgunAttack(affected, e -> e.hurt(this.getSource(),this.getDamage()) );
        });
    }
}