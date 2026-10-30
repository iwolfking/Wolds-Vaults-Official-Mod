package xyz.iwolfking.woldsvaults.integration.infernalmobs.modifiers;

import atomicstryker.infernalmobs.common.MobModifier;
import iskallia.vault.init.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class MM_Immortal extends MobModifier {
    private static final long coolDown = 6500L;
    private static String[] suffix = new String[]{"theImmortal", "theLongLasting", "ofEternity"};
    private static String[] prefix = new String[]{"immortal", "undying", "cheating"};
    private long nextAbilityUse = 0L;

    public MM_Immortal() {
    }

    public MM_Immortal(MobModifier next) {
        super(next);
    }

    public String getModName() {
        return "Immortal";
    }

    public boolean onUpdate(LivingEntity mob) {
        if (this.hasSteadyTarget()) {
            long time = System.currentTimeMillis();
            if (time > this.nextAbilityUse) {
                this.nextAbilityUse = time + coolDown;

                mob.addEffect(new MobEffectInstance(ModEffects.IMMORTALITY, 600, 0, false, true));
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