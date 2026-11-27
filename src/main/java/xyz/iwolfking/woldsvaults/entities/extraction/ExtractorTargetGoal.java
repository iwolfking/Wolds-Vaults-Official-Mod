package xyz.iwolfking.woldsvaults.entities.extraction;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.UUID;
import java.util.function.Function;

/**
 * Locks a mob's attack target onto one rig (the main extractor or a thumper) by UUID. Unlike the protect-villager goal
 * this never yields to nearby players. When the rig is lost, the retarget function picks the next one (the nearest
 * live rig of the room); with none left the goal lets go and the mob falls back to its own targeting.
 */
public class ExtractorTargetGoal extends Goal {
    private final Mob mob;
    private final Function<Mob, UUID> retarget;
    private UUID rigId;

    public ExtractorTargetGoal(Mob mob, UUID rigId, Function<Mob, UUID> retarget) {
        this.mob = mob;
        this.rigId = rigId;
        this.retarget = retarget;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    private LivingEntity resolve() {
        if (!(this.mob.level instanceof ServerLevel serverLevel) || this.rigId == null) {
            return null;
        }
        Entity entity = serverLevel.getEntity(this.rigId);
        return entity instanceof RigTarget rig && entity.isAlive() && rig.isRigTargetable() ? (LivingEntity) entity : null;
    }

    private LivingEntity resolveOrRetarget() {
        LivingEntity rig = this.resolve();
        if (rig == null && this.retarget != null && this.mob.tickCount % 10 == 0) {
            UUID next = this.retarget.apply(this.mob);
            if (next != null) {
                this.rigId = next;
                rig = this.resolve();
            }
        }
        return rig;
    }

    @Override
    public boolean canUse() {
        return this.resolveOrRetarget() != null;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity rig = this.resolve();
        return rig != null && this.mob.getTarget() == rig;
    }

    @Override
    public void start() {
        LivingEntity rig = this.resolve();
        if (rig != null) {
            this.mob.setTarget(rig);
        }
    }

    @Override
    public void stop() {
        if (this.mob.getTarget() instanceof RigTarget) {
            this.mob.setTarget(null);
        }
    }
}
