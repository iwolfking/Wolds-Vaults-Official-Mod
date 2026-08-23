package xyz.iwolfking.woldsvaults.gods.trees.idona;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.stat.StatCollector;
import iskallia.vault.core.vault.stat.StatsCollector;
import iskallia.vault.core.vault.time.TickClock;
import iskallia.vault.core.vault.time.TickStopwatch;
import iskallia.vault.util.damage.PlayerDamageHelper;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.combat.GlobalDamageMultiplierRegistry;
import xyz.iwolfking.woldsvaults.gods.node.GodEffect;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeContext;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeTicker;
import xyz.iwolfking.woldsvaults.gods.node.TickContributor;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Idona nodes whose damage multiplier tracks slowly-changing player or vault state, recomputed on
 * the {@link GodNodeTicker} pass into {@link GlobalDamageMultiplierRegistry}.
 */
public final class IdonaTickHandlers {
    private static final double BASE_MOVEMENT_SPEED = 0.1D;
    private static final UUID SPRINT_MODIFIER_UUID = UUID.fromString("662A6B8D-DA3E-4C1C-8813-96EA6097278D");
    private static final UUID CRUSHING_BLOWS_ID =
            UUID.nameUUIDFromBytes("woldsvaults:idona_crushing_blows".getBytes(StandardCharsets.UTF_8));

    private IdonaTickHandlers() {
    }

    /** Kinetic Impact: more damage per percent of movement speed above base, sprint divided out. */
    public record KineticImpactHandler(GodEffect effect) implements TickContributor {
        @Override
        public void tick(GodNodeContext context) {
            ServerPlayer player = context.player();
            ResourceLocation key = IdonaNodes.key(context.effectId());
            AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed == null) {
                return;
            }
            double value = speed.getValue();
            AttributeModifier sprint = speed.getModifier(SPRINT_MODIFIER_UUID);
            if (sprint != null && 1.0D + sprint.getAmount() > 0.0D) {
                value /= 1.0D + sprint.getAmount();
            }
            double percentAboveBase = (value / BASE_MOVEMENT_SPEED - 1.0D) * 100.0D;
            if (percentAboveBase <= 0.0D) {
                GlobalDamageMultiplierRegistry.remove(player, key);
                return;
            }
            float perPercent = this.effect.params(IdonaNodeHandlers.KineticImpactParams.class).per_percent();
            GlobalDamageMultiplierRegistry.register(player, key,
                    (float) (1.0D + percentAboveBase * perPercent * context.points()));
        }

        @Override
        public void onDeactivated(ServerPlayer player, String effectId) {
            GlobalDamageMultiplierRegistry.remove(player, IdonaNodes.key(effectId));
        }
    }

    /** Surrounded: more damage per hostile mob within the configured radius. */
    public record SurroundedHandler(GodEffect effect) implements TickContributor {
        @Override
        public void tick(GodNodeContext context) {
            ServerPlayer player = context.player();
            ResourceLocation key = IdonaNodes.key(context.effectId());
            IdonaNodeHandlers.SurroundedParams params =
                    this.effect.params(IdonaNodeHandlers.SurroundedParams.class);
            double radius = params.radius();
            double radiusSq = radius * radius;
            AABB box = new AABB(player.position(), player.position()).inflate(radius);
            List<Mob> nearby = player.getLevel().getEntitiesOfClass(Mob.class, box,
                    mob -> mob.isAlive() && mob.distanceToSqr(player) <= radiusSq && IdonaTargeting.countsAsEnemy(mob));
            if (nearby.isEmpty()) {
                GlobalDamageMultiplierRegistry.remove(player, key);
                return;
            }
            GlobalDamageMultiplierRegistry.register(player, key,
                    1.0F + params.per_mob() * nearby.size() * context.points());
        }

        @Override
        public void onDeactivated(ServerPlayer player, String effectId) {
            GlobalDamageMultiplierRegistry.remove(player, IdonaNodes.key(effectId));
        }
    }

    /** Under Pressure: x1 at the far end of the window, {@code max} at zero time remaining. */
    public record UnderPressureHandler(GodEffect effect) implements TickContributor {
        @Override
        public void tick(GodNodeContext context) {
            ServerPlayer player = context.player();
            ResourceLocation key = IdonaNodes.key(context.effectId());
            Optional<Integer> remaining = remainingTicks(player);
            if (remaining.isEmpty()) {
                GlobalDamageMultiplierRegistry.remove(player, key);
                return;
            }
            IdonaNodeHandlers.UnderPressureParams params =
                    this.effect.params(IdonaNodeHandlers.UnderPressureParams.class);
            int ticks = Math.max(0, Math.min(remaining.get(), params.window_ticks()));
            float progress = 1.0F - (float) ticks / params.window_ticks();
            GlobalDamageMultiplierRegistry.register(player, key,
                    1.0F + (params.max() - 1.0F) * progress * context.points());
        }

        @Override
        public void onDeactivated(ServerPlayer player, String effectId) {
            GlobalDamageMultiplierRegistry.remove(player, IdonaNodes.key(effectId));
        }
    }

    /** Banked Anger: {@code log_base(base + damageTaken)} over the vault stat collector. */
    public record BankedAngerHandler(GodEffect effect) implements TickContributor {
        @Override
        public void tick(GodNodeContext context) {
            ServerPlayer player = context.player();
            ResourceLocation key = IdonaNodes.key(context.effectId());
            Optional<Vault> vault = ServerVaults.get(player.level);
            if (vault.isEmpty() || !vault.get().has(Vault.STATS)) {
                GlobalDamageMultiplierRegistry.remove(player, key);
                return;
            }
            StatsCollector stats = vault.get().get(Vault.STATS);
            StatCollector collector = stats.get(player.getUUID());
            if (collector == null) {
                GlobalDamageMultiplierRegistry.remove(player, key);
                return;
            }
            double taken = Math.max(0.0D, collector.getDamageTaken());
            if (!Double.isFinite(taken)) {
                WoldsVaults.LOGGER.error("Banked Anger read a non-finite damage-taken total for {}; treating it as zero.",
                        player.getGameProfile().getName());
                taken = 0.0D;
            }
            double base = this.effect.params(IdonaNodeHandlers.BankedAngerParams.class).base();
            double curve = Math.log(base + taken) / Math.log(base);
            GlobalDamageMultiplierRegistry.register(player, key, (float) Math.pow(curve, context.points()));
        }

        @Override
        public void onDeactivated(ServerPlayer player, String effectId) {
            GlobalDamageMultiplierRegistry.remove(player, IdonaNodes.key(effectId));
        }
    }

    /** Crushing Blows: a stacking multiplier in the base mod's attack-damage registry. */
    public record CrushingBlowsHandler(GodEffect effect) implements TickContributor {
        @Override
        public void tick(GodNodeContext context) {
            ServerPlayer player = context.player();
            float value = (float) Math.pow(this.effect.params(IdonaNodeHandlers.CrushingBlowsParams.class).multiplier(),
                    context.points());
            PlayerDamageHelper.DamageMultiplier applied = PlayerDamageHelper.getMultiplier(player, CRUSHING_BLOWS_ID);
            if (applied != null && applied.getMultiplier() == value) {
                return;
            }
            PlayerDamageHelper.applyMultiplier(CRUSHING_BLOWS_ID, player, value,
                    PlayerDamageHelper.Operation.STACKING_MULTIPLY, false);
        }

        @Override
        public void onDeactivated(ServerPlayer player, String effectId) {
            PlayerDamageHelper.removeMultiplier(player, CRUSHING_BLOWS_ID);
        }
    }

    /** Ultra Rampaging: the tick does nothing; deactivation zeroes the player's Fury pool. */
    public record UltraRampagingHandler(GodEffect effect) implements TickContributor {
        @Override
        public void tick(GodNodeContext context) {
        }

        @Override
        public void onDeactivated(ServerPlayer player, String effectId) {
            xyz.iwolfking.woldsvaults.gods.combat.PlayerFuryHelper.clear(player);
        }
    }

    private static Optional<Integer> remainingTicks(ServerPlayer player) {
        Optional<Vault> vault = ServerVaults.get(player.level);
        if (vault.isEmpty() || !vault.get().has(Vault.CLOCK)) {
            return Optional.empty();
        }
        TickClock clock = vault.get().get(Vault.CLOCK);
        if (clock.has(TickClock.INFINITE) || !clock.has(TickClock.DISPLAY_TIME)) {
            return Optional.empty();
        }
        int display = clock.get(TickClock.DISPLAY_TIME);
        if (clock instanceof TickStopwatch stopwatch) {
            if (!stopwatch.has(TickStopwatch.LIMIT)) {
                return Optional.empty();
            }
            return Optional.of(stopwatch.get(TickStopwatch.LIMIT) - display);
        }
        return Optional.of(display);
    }
}
