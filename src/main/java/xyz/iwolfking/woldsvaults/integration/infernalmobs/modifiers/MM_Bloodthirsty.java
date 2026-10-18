package xyz.iwolfking.woldsvaults.integration.infernalmobs.modifiers;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import xyz.iwolfking.woldsvaults.init.ModEffects;

public class MM_Bloodthirsty extends MobModifier {
    private static String[] suffix = new String[]{"theBloodthirsty", "ofBrutality", "theBrutal"};
    private static String[] prefix = new String[]{"brutalizer", "bloodthirsty", "brutal"};

    public MM_Bloodthirsty() {
    }

    public MM_Bloodthirsty(MobModifier next) {
        super(next);
    }

    public String getModName() {
        return "Bloodthirsty";
    }

    public float onAttack(LivingEntity entity, DamageSource source, float damage) {
        if (entity != null) {
            entity.addEffect(new MobEffectInstance(ModEffects.BLEED_OVERRIDE, 100, 1));
            entity.hurt(DamageSource.MAGIC, entity.getMaxHealth() * 0.1F);
        }

        return super.onAttack(entity, source, damage);
    }

    protected String[] getModNameSuffix() {
        return suffix;
    }

    protected String[] getModNamePrefix() {
        return prefix;
    }
}