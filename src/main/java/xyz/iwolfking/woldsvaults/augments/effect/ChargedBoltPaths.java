package xyz.iwolfking.woldsvaults.augments.effect;

import iskallia.vault.skill.ability.effect.ChargedBoltsAbility;
import iskallia.vault.util.ServerScheduler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import xyz.iwolfking.woldsvaults.augments.FocusAugments;
import xyz.iwolfking.woldsvaults.augments.cast.CastModifiers;
import xyz.iwolfking.woldsvaults.augments.cast.CastScope;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.ChargedBoltsAbilityInvoker;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Charged Bolts cast from a spell. VH flies each bolt along a fixed straight line stepped by scheduled tasks, with no
 * entity to tag or steer, so a spell cast flies them here instead: the same fan, step length, range, hit test, damage
 * roll, repeat-hit falloff, sparks and trail, plus the step's glyphs. Accelerate lengthens each step (same range),
 * Size Up and Size Down scale the hit radius, Ethereal passes through blocks, Homing turns each bolt like a homing
 * projectile, and hits deal damage inside the cast's scope so damage glyphs, Charge and Echo power apply.
 */
public final class ChargedBoltPaths {
    private static final double SPAWN_DROP = 0.25;
    private static final double SPAWN_FORWARD = 0.75;
    private static final double SEARCH_RADIUS = 0.75;
    private static final double HIT_RADIUS = 0.85;
    private static final double MIN_SPEED = 0.05;

    private ChargedBoltPaths() {
    }

    public static void cast(ChargedBoltsAbility ability, ServerPlayer player, CastModifiers modifiers) {
        FocusAugmentsConfig config = FocusAugments.config();
        ChargedBoltsAbilityInvoker invoker = (ChargedBoltsAbilityInvoker) ability;
        Vec3 look = player.getLookAngle().normalize();
        Vec3 start = player.getEyePosition().add(0.0, -SPAWN_DROP, 0.0).add(look.scale(SPAWN_FORWARD));
        int bolts = Math.max(1, ability.getBoltCount());
        double speed = Math.max(MIN_SPEED, ability.getThrowPower()) * modifiers.speed();
        int maxTicks = Math.max(1, Mth.ceil(ability.getDistance() / speed));
        boolean homing = config != null && modifiers.homing();
        LivingEntity preferred = homing
                ? ProjectileSteering.cursorTarget(player, config, e -> ChargedBoltsAbilityInvoker.woldsvaults$canHit(player, e)).orElse(null)
                : null;
        Map<Integer, Integer> hitCounts = new HashMap<>();
        player.level.playSound(null, player, SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 0.55F, 1.65F);
        for (int index = 0; index < bolts; index++) {
            Vec3 direction = ChargedBoltsAbilityInvoker.woldsvaults$rotateY(look, Math.toRadians(invoker.woldsvaults$getSpreadOffset(index, bolts))).normalize();
            Bolt bolt = new Bolt(ability, player, modifiers, start, direction, speed, homing ? preferred : null, homing, hitCounts);
            for (int tick = 0; tick <= maxTicks; tick++) {
                int step = tick;
                ServerScheduler.INSTANCE.schedule(step, () -> bolt.step(step));
            }
        }
    }

    private static final class Bolt {
        final ChargedBoltsAbility ability;
        final ServerPlayer player;
        final CastModifiers modifiers;
        final double speed;
        final boolean homing;
        final Map<Integer, Integer> hitCounts;
        final double radiusScale;
        @Nullable final LivingEntity preferred;
        @Nullable LivingEntity target;
        Vec3 position;
        Vec3 direction;
        boolean active = true;

        Bolt(ChargedBoltsAbility ability, ServerPlayer player, CastModifiers modifiers, Vec3 start, Vec3 direction, double speed,
             @Nullable LivingEntity preferred, boolean homing, Map<Integer, Integer> hitCounts) {
            this.ability = ability;
            this.player = player;
            this.modifiers = modifiers;
            this.position = start;
            this.direction = direction;
            this.speed = speed;
            this.preferred = preferred;
            this.homing = homing;
            this.hitCounts = hitCounts;
            this.radiusScale = modifiers.scale();
        }

        void step(int tick) {
            if (!this.active || !this.player.isAlive() || this.player.level.isClientSide()) {
                return;
            }
            if (this.homing && tick > 0) {
                steer();
            }
            Vec3 previous = this.position;
            Vec3 current = tick == 0 ? previous : previous.add(this.direction.scale(this.speed));
            this.position = current;
            if (!this.modifiers.ethereal() && ChargedBoltsAbilityInvoker.woldsvaults$isBlocked(this.player, previous, current)) {
                this.active = false;
                ChargedBoltsAbilityInvoker.woldsvaults$spark(this.player.getLevel(), current, 0.35F);
                return;
            }
            LivingEntity hit = findHit(previous, current);
            if (hit != null) {
                this.active = false;
                CastScope.call(this.player, this.modifiers, null, CastScope.Billing.free(true), () -> {
                    ((ChargedBoltsAbilityInvoker) this.ability).woldsvaults$hitTarget(this.player, hit, this.hitCounts);
                    return null;
                });
                ChargedBoltsAbilityInvoker.woldsvaults$spark(this.player.getLevel(), hit.getBoundingBox().getCenter(), 0.55F);
                return;
            }
            ChargedBoltsAbilityInvoker.woldsvaults$trail(this.player.getLevel(), current, this.direction);
        }

        private void steer() {
            FocusAugmentsConfig config = FocusAugments.config();
            if (config == null) {
                return;
            }
            double range = this.modifiers.homingRange();
            if (this.preferred != null && inRange(this.preferred, range)) {
                this.target = this.preferred;
            } else if (this.target == null || !inRange(this.target, range)) {
                this.target = nearest(range);
            }
            if (this.target == null) {
                return;
            }
            Vec3 desired = this.target.getBoundingBox().getCenter().subtract(this.position);
            if (desired.lengthSqr() < 1.0E-8) {
                return;
            }
            this.direction = ProjectileSteering.rotateToward(this.direction, desired.normalize(), Math.toRadians(this.modifiers.homingTurn() / 20.0));
        }

        private boolean inRange(LivingEntity entity, double range) {
            return entity.isAlive() && entity.level == this.player.level
                    && entity.getBoundingBox().getCenter().distanceToSqr(this.position) <= range * range
                    && ChargedBoltsAbilityInvoker.woldsvaults$canHit(this.player, entity);
        }

        @Nullable
        private LivingEntity nearest(double range) {
            LivingEntity best = null;
            double bestDistance = range * range;
            AABB area = new AABB(this.position, this.position).inflate(range);
            for (LivingEntity entity : this.player.level.getEntitiesOfClass(LivingEntity.class, area,
                    e -> ChargedBoltsAbilityInvoker.woldsvaults$canHit(this.player, e))) {
                double distance = entity.getBoundingBox().getCenter().distanceToSqr(this.position);
                if (distance <= bestDistance) {
                    bestDistance = distance;
                    best = entity;
                }
            }
            return best;
        }

        @Nullable
        private LivingEntity findHit(Vec3 previous, Vec3 current) {
            AABB area = new AABB(previous, current).inflate(SEARCH_RADIUS * this.radiusScale);
            double maxDistance = HIT_RADIUS * this.radiusScale;
            LivingEntity closest = null;
            double closestDistance = Double.MAX_VALUE;
            for (LivingEntity entity : this.player.level.getEntitiesOfClass(LivingEntity.class, area,
                    e -> ChargedBoltsAbilityInvoker.woldsvaults$canHit(this.player, e))) {
                double distance = ChargedBoltsAbilityInvoker.woldsvaults$distanceToSegment(entity.getBoundingBox().getCenter(), previous, current);
                double fromStart = previous.distanceToSqr(entity.position());
                if (distance <= maxDistance && fromStart < closestDistance) {
                    closestDistance = fromStart;
                    closest = entity;
                }
            }
            return closest;
        }
    }
}
