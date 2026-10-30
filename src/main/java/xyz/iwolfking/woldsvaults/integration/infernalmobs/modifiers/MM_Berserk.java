package xyz.iwolfking.woldsvaults.integration.infernalmobs.modifiers;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public class MM_Berserk extends MobModifier {
    private static final long coolDown = 2000L;
    private static String[] suffix = new String[]{"ofRage", "theBerserker", "ofPureAnger"};
    private static String[] prefix = new String[]{"berserk", "raging", "furious"};
    private long nextAbilityUse = 0L;

    public MM_Berserk() {
    }

    public MM_Berserk(MobModifier next) {
        super(next);
    }

    public String getModName() {
        return "Berserking";
    }

    public boolean onUpdate(LivingEntity mob) {
        if (this.hasSteadyTarget()) {
            long time = System.currentTimeMillis();
            if (time > this.nextAbilityUse) {
                this.nextAbilityUse = time + coolDown;
                
                float maxHealth = mob.getMaxHealth();
                float currentHealth = mob.getHealth();
                float healthPercent = currentHealth / maxHealth;

                if (healthPercent <= 0.1F) {
                    mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 4, false, false));
                    mob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 4, false, false));
                }
                else if (healthPercent <= 0.25F) {
                    mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 3, false, false));
                    mob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 3, false, false));
                } else if (healthPercent <= 0.50F) {
                    mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 2, false, false));
                    mob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 1, false, false));
                }
                else if (healthPercent <= 0.75F) {
                    mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 1, false, false));
                    mob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 0, false, false));
                }
            }
        }

        return super.onUpdate(mob);
    }

    protected String[] getModNameSuffix() {
        return suffix;
    }

    protected String[] getModNamePrefix() {
        return prefix;
    }
}