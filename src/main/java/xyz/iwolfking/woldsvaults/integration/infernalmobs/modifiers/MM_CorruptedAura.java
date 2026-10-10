package xyz.iwolfking.woldsvaults.integration.infernalmobs.modifiers;

import atomicstryker.infernalmobs.common.MobModifier;
import atomicstryker.infernalmobs.common.mods.MM_Lifesteal;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import xyz.iwolfking.woldsvaults.init.ModEffects;

import java.util.List;

public class MM_CorruptedAura extends MobModifier {
    private static final long coolDown = 2000L;
    private static String[] suffix = new String[]{"ofDecay", "theCorrupter", "ofPlagues"};
    private static String[] prefix = new String[]{"corrupted", "pestilent", "decaying"};
    private long nextAbilityUse = 0L;

    public MM_CorruptedAura() {
    }

    public MM_CorruptedAura(MobModifier next) {
        super(next);
    }

    public String getModName() {
        return "CorruptedAura";
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
                    player.addEffect(new MobEffectInstance(ModEffects.HEALING_BLOCK, 80, 9, false, true));
                    player.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 0, false, true));
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