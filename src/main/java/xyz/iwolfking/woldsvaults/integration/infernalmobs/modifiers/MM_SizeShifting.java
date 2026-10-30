package xyz.iwolfking.woldsvaults.integration.infernalmobs.modifiers;

import atomicstryker.infernalmobs.common.MobModifier;
import iskallia.vault.init.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public class MM_SizeShifting extends MobModifier {
    private static final long coolDown = 9000L;
    private static String[] suffix = new String[]{"ofChange", "ofSizeShifting", "theShifting"};
    private static String[] prefix = new String[]{"shifting", "shapeless", "shifter"};
    private long nextAbilityUse = 0L;

    public MM_SizeShifting() {
    }

    public MM_SizeShifting(MobModifier next) {
        super(next);
    }

    public String getModName() {
        return "SizeShifting";
    }

    public boolean onUpdate(LivingEntity mob) {
        if (this.hasSteadyTarget()) {
            long time = System.currentTimeMillis();
            if (time > this.nextAbilityUse) {
                this.nextAbilityUse = time + coolDown;
                if(mob.getRandom().nextBoolean()) {
                    mob.addEffect(new MobEffectInstance(ModEffects.SHRINK, 450, 1, false, false));
                }
                else {
                    mob.addEffect(new MobEffectInstance(ModEffects.GROW, 450, 1, false, false));
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