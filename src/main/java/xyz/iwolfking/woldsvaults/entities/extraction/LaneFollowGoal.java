package xyz.iwolfking.woldsvaults.entities.extraction;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.config.ExtractionObjectiveConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.objectives.extraction.RoomGrid;

import java.util.EnumSet;
import java.util.function.Supplier;

/**
 * Walks a wave mob along its room's baked lanes towards the rig it targets. Every few ticks it aims vanilla
 * navigation at the farthest lane cell in straight-line view a short way down the rig's distance field, so every path
 * vanilla has to find is short; the mob's own attack goals take over once it is within engage range of the pad. A mob
 * that makes no progress for a while is nudged one lane cell forward; one knocked off the lanes walks back to them.
 */
public class LaneFollowGoal extends Goal {
    private static final double NEAR_RIG_MARGIN = 2.0D;
    /** Receives lane events for the spawn debug log. */
    public interface Listener {
        void arrived(int siteIndex, int pad, int walkTicks, Mob mob);

        void nudged(int siteIndex);
    }

    private final Mob mob;
    private final int siteIndex;
    private final Supplier<RoomGrid> grid;
    private final boolean wide;
    private final Listener listener;
    private BlockPos waypoint;
    private int repathTimer;
    private int bestDistance = Integer.MAX_VALUE;
    private int ticksSinceProgress;
    private int lastPad = -1;
    private boolean arrived;
    private boolean offLane;
    private BlockPos recoverySpot;

    public LaneFollowGoal(Mob mob, int siteIndex, Supplier<RoomGrid> grid, boolean wide, Listener listener) {
        this.mob = mob;
        this.siteIndex = siteIndex;
        this.grid = grid;
        this.wide = wide;
        this.listener = listener;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    private ExtractionObjectiveConfig.LanesEntry cfg() {
        return ModConfigs.EXTRACTION_OBJECTIVE.WAVES.lanes;
    }

    private RigTarget rig() {
        LivingEntity target = this.mob.getTarget();
        return target instanceof RigTarget rig && target.isAlive() && rig.isRigTargetable() ? rig : null;
    }

    private boolean travelling() {
        RigTarget rig = this.rig();
        RoomGrid room = this.grid.get();
        if (rig == null || room == null || !room.isReady()) {
            return false;
        }
        int pad = rig.getPadIndex();
        if (pad != this.lastPad) {
            this.lastPad = pad;
            this.bestDistance = Integer.MAX_VALUE;
            this.ticksSinceProgress = 0;
        }
        int cell = room.cellAt(this.mob.blockPosition(), this.wide);
        if (cell < 0) {
            double near = this.cfg().engageSteps + NEAR_RIG_MARGIN;
            if (this.mob.distanceToSqr(rig.asLiving()) <= near * near) {
                return false;
            }
            this.offLane = true;
            return true;
        }
        if (this.offLane) {
            this.offLane = false;
            this.recoverySpot = null;
            this.ticksSinceProgress = 0;
        }
        int distance = room.distance(pad, this.wide, cell);
        if (distance < 0) {
            return false;
        }
        if (this.engaged(distance, rig.asLiving())) {
            if (!this.arrived) {
                this.arrived = true;
                this.listener.arrived(this.siteIndex, pad, this.mob.tickCount, this.mob);
            }
            return false;
        }
        return true;
    }

    /**
     * Walks a mob that was knocked off the lanes back to the nearest lane cell of its rig's field, and puts it there if
     * it has not made it back after the stuck time.
     */
    private void recover(RoomGrid room, int pad, ExtractionObjectiveConfig.LanesEntry cfg) {
        if (--this.repathTimer <= 0 || this.mob.getNavigation().isDone()) {
            this.repathTimer = cfg.repathTicks;
            this.recoverySpot = room.recoverySpot(this.mob.blockPosition(), pad, this.wide, cfg.recoverRadius);
            if (this.recoverySpot == null) {
                WoldLogger.errorOnce("extraction_lane_recovery_" + this.siteIndex,
                        "Extraction site {} has a wave mob more than {} blocks from any lane; it walks to its rig without the lanes.",
                        this.siteIndex, cfg.recoverRadius);
                return;
            }
            this.mob.getNavigation().moveTo(this.recoverySpot.getX() + 0.5D, this.recoverySpot.getY(), this.recoverySpot.getZ() + 0.5D, 1.0D);
        }
        if (this.ticksSinceProgress >= cfg.stuckTicks && this.recoverySpot != null) {
            this.ticksSinceProgress = 0;
            this.mob.getNavigation().stop();
            this.mob.teleportTo(this.recoverySpot.getX() + 0.5D, this.recoverySpot.getY(), this.recoverySpot.getZ() + 0.5D);
            this.listener.nudged(this.siteIndex);
        }
    }

    private boolean engaged(int distance, LivingEntity target) {
        ExtractionObjectiveConfig.LanesEntry cfg = this.cfg();
        if (distance <= cfg.engageSteps) {
            return true;
        }
        if (this.mob instanceof RangedAttackMob) {
            double range = cfg.rangedEngageBlocks;
            return this.mob.distanceToSqr(target) <= range * range && this.mob.getSensing().hasLineOfSight(target);
        }
        return false;
    }

    @Override
    public boolean canUse() {
        return this.mob.tickCount % 5 == 0 && this.travelling();
    }

    @Override
    public boolean canContinueToUse() {
        return this.travelling();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        this.repathTimer = 0;
        this.waypoint = null;
    }

    @Override
    public void stop() {
        this.mob.getNavigation().stop();
        this.waypoint = null;
    }

    @Override
    public void tick() {
        RigTarget rig = this.rig();
        RoomGrid room = this.grid.get();
        if (rig == null || room == null) {
            return;
        }
        ExtractionObjectiveConfig.LanesEntry cfg = this.cfg();
        int pad = rig.getPadIndex();
        this.ticksSinceProgress++;
        if (this.offLane) {
            this.recover(room, pad, cfg);
            return;
        }
        boolean reached = this.waypoint != null
                && this.mob.distanceToSqr(this.waypoint.getX() + 0.5D, this.waypoint.getY(), this.waypoint.getZ() + 0.5D) < 2.25D;
        if (--this.repathTimer <= 0 || reached || this.mob.getNavigation().isDone()) {
            this.repathTimer = cfg.repathTicks;
            int cell = room.cellAt(this.mob.blockPosition(), this.wide);
            int distance = room.distance(pad, this.wide, cell);
            if (distance >= 0 && distance < this.bestDistance) {
                this.bestDistance = distance;
                this.ticksSinceProgress = 0;
            }
            this.waypoint = room.waypoint(pad, this.wide, cell, cfg.lookahead, this.mob.getId());
            if (this.waypoint != null) {
                this.mob.getNavigation().moveTo(this.waypoint.getX() + 0.5D, this.waypoint.getY(), this.waypoint.getZ() + 0.5D, 1.0D);
            }
        }
        if (this.ticksSinceProgress >= cfg.stuckTicks) {
            this.ticksSinceProgress = 0;
            int cell = room.cellAt(this.mob.blockPosition(), this.wide);
            BlockPos next = room.nextStep(pad, this.wide, cell);
            if (next != null) {
                this.mob.getNavigation().stop();
                this.mob.teleportTo(next.getX() + 0.5D, next.getY(), next.getZ() + 0.5D);
                this.bestDistance = Math.max(0, room.distance(pad, this.wide, room.cellAt(next, this.wide)));
                this.listener.nudged(this.siteIndex);
            }
        }
    }
}
