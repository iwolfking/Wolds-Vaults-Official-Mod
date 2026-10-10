package xyz.iwolfking.woldsvaults.integration.infernalmobs.modifiers;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import xyz.iwolfking.woldsvaults.init.ModEffects;

import java.util.List;

public class MM_Exposing extends MobModifier {
    private static final long coolDown = 2000L;
    private static String[] suffix = new String[]{"ofExposing", "theExposer"};
    private static String[] prefix = new String[]{"exposing", "shredder", "armor-piercing"};
    private long nextAbilityUse = 0L;

    public MM_Exposing() {
    }

    public MM_Exposing(MobModifier next) {
        super(next);
    }

    public String getModName() {
        return "Exposer";
    }

    public boolean onUpdate(LivingEntity mob) {
        if (this.hasSteadyTarget() && !mob.level.isClientSide()) {
            long time = System.currentTimeMillis();
            if (time > this.nextAbilityUse) {
                this.nextAbilityUse = time + coolDown;
                
                double radius = 6.0D;
                AABB boundingBox = mob.getBoundingBox().inflate(radius);
                List<Player> players = mob.level.getEntitiesOfClass(Player.class, boundingBox);

                for (Player player : players) {
                    player.addEffect(new MobEffectInstance(ModEffects.SHREDDED, 80, 7, false, true));
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