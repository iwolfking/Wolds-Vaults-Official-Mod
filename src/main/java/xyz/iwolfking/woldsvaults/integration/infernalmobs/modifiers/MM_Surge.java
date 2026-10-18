package xyz.iwolfking.woldsvaults.integration.infernalmobs.modifiers;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public class MM_Surge extends MobModifier {
    private static final long coolDown = 10000L;
    private static String[] suffix = new String[]{"ofBolting", "theSwiftOne", "ofbeinginyourFace"};
    private static String[] prefix = new String[]{"sprinting", "swift", "charging"};
    private long nextAbilityUse = 0L;

    public MM_Surge() {
    }

    public MM_Surge(MobModifier next) {
        super(next);
    }

    public String getModName() {
        return "Surge";
    }

    public boolean onUpdate(LivingEntity mob) {
        if (this.hasSteadyTarget()) {
            long time = System.currentTimeMillis();
            if (time > this.nextAbilityUse) {
                this.nextAbilityUse = time + coolDown;
                mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 5, false, false));
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