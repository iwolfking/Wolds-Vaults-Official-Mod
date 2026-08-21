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
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.ActiveGodResolver;
import xyz.iwolfking.woldsvaults.gods.combat.GlobalDamageMultiplierRegistry;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The Idona nodes whose damage multiplier is a function of slowly-changing player or vault state.
 * Each one is recomputed on a shared throttle and stored as a named factor in
 * {@link GlobalDamageMultiplierRegistry}, so the per-hit cost is a single map product and the
 * factors compose multiplicatively with every other god node.
 *
 * <p>Nothing here runs for a player with no god charm equipped, which is the cheap gate that keeps
 * this off the hot path for most of the server.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class IdonaTickHandlers {
    private static final int REFRESH_INTERVAL_TICKS = 10;
    private static final double BASE_MOVEMENT_SPEED = 0.1D;
    private static final UUID SPRINT_MODIFIER_UUID = UUID.fromString("662A6B8D-DA3E-4C1C-8813-96EA6097278D");
    private static final UUID CRUSHING_BLOWS_ID = UUID.nameUUIDFromBytes("woldsvaults:idona_crushing_blows".getBytes(java.nio.charset.StandardCharsets.UTF_8));

    private static final ResourceLocation KINETIC_IMPACT_KEY = IdonaNodes.key(IdonaNodes.KINETIC_IMPACT);
    private static final ResourceLocation SURROUNDED_KEY = IdonaNodes.key(IdonaNodes.SURROUNDED);
    private static final ResourceLocation UNDER_PRESSURE_KEY = IdonaNodes.key(IdonaNodes.UNDER_PRESSURE);
    private static final ResourceLocation BANKED_ANGER_KEY = IdonaNodes.key(IdonaNodes.BANKED_ANGER);

    private IdonaTickHandlers() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.side.isClient() || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        if ((player.tickCount + player.getId()) % REFRESH_INTERVAL_TICKS != 0) {
            return;
        }
        if (ActiveGodResolver.getActiveGod(player).isEmpty()) {
            clearAll(player);
            return;
        }
        refreshKineticImpact(player);
        refreshSurrounded(player);
        refreshUnderPressure(player);
        refreshBankedAnger(player);
        refreshCrushingBlows(player);
    }

    private static void clearAll(ServerPlayer player) {
        GlobalDamageMultiplierRegistry.remove(player, KINETIC_IMPACT_KEY);
        GlobalDamageMultiplierRegistry.remove(player, SURROUNDED_KEY);
        GlobalDamageMultiplierRegistry.remove(player, UNDER_PRESSURE_KEY);
        GlobalDamageMultiplierRegistry.remove(player, BANKED_ANGER_KEY);
        PlayerDamageHelper.removeMultiplier(player, CRUSHING_BLOWS_ID);
    }

    /**
     * Reads movement speed straight off the attribute, which already has the Weighted Boots cap
     * baked in as a MULTIPLY_TOTAL modifier. The vanilla sprint modifier is divided back out, so
     * sprinting does not spike the damage bonus -  the same treatment the cap itself gives it.
     */
    private static void refreshKineticImpact(ServerPlayer player) {
        int points = IdonaNodes.minorPoints(player, IdonaNodes.KINETIC_IMPACT);
        if (points <= 0) {
            GlobalDamageMultiplierRegistry.remove(player, KINETIC_IMPACT_KEY);
            return;
        }
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
            GlobalDamageMultiplierRegistry.remove(player, KINETIC_IMPACT_KEY);
            return;
        }
        float factor = (float) (1.0D + percentAboveBase * IdonaNodes.kineticImpactPerPercent() * points);
        GlobalDamageMultiplierRegistry.register(player, KINETIC_IMPACT_KEY, factor);
    }

    private static void refreshSurrounded(ServerPlayer player) {
        int points = IdonaNodes.minorPoints(player, IdonaNodes.SURROUNDED);
        if (points <= 0) {
            GlobalDamageMultiplierRegistry.remove(player, SURROUNDED_KEY);
            return;
        }
        double radius = IdonaNodes.surroundedRadius();
        double radiusSq = radius * radius;
        AABB box = new AABB(player.position(), player.position()).inflate(radius);
        List<Mob> nearby = player.getLevel().getEntitiesOfClass(Mob.class, box,
                mob -> mob.isAlive() && mob.distanceToSqr(player) <= radiusSq && IdonaTargeting.countsAsEnemy(mob));
        if (nearby.isEmpty()) {
            GlobalDamageMultiplierRegistry.remove(player, SURROUNDED_KEY);
            return;
        }
        float factor = 1.0F + IdonaNodes.surroundedPerMob() * nearby.size() * points;
        GlobalDamageMultiplierRegistry.register(player, SURROUNDED_KEY, factor);
    }

    /**
     * Scales linearly from x1 at fifteen minutes remaining to x2 at zero. Counting-up vaults are
     * converted to a remaining time from their limit, so a stopwatch objective does not invert the
     * node; outside a vault, and in an infinite vault, the node contributes nothing.
     */
    private static void refreshUnderPressure(ServerPlayer player) {
        int points = IdonaNodes.minorPoints(player, IdonaNodes.UNDER_PRESSURE);
        if (points <= 0) {
            GlobalDamageMultiplierRegistry.remove(player, UNDER_PRESSURE_KEY);
            return;
        }
        Optional<Integer> remaining = remainingTicks(player);
        if (remaining.isEmpty()) {
            GlobalDamageMultiplierRegistry.remove(player, UNDER_PRESSURE_KEY);
            return;
        }
        int ticks = Math.max(0, Math.min(remaining.get(), IdonaNodes.underPressureWindowTicks()));
        float progress = 1.0F - (float) ticks / IdonaNodes.underPressureWindowTicks();
        float factor = 1.0F + (IdonaNodes.underPressureMax() - 1.0F) * progress * points;
        GlobalDamageMultiplierRegistry.register(player, UNDER_PRESSURE_KEY, factor);
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

    /**
     * The sheet's {@code log_100000(100000 + damageTaken)} curve. It is deliberately read off the
     * vault stat collector rather than a private counter, which means it tracks mob-sourced damage
     * only (the collector ignores sources with no attacking entity) and degrades to a flat 1.0
     * outside a vault.
     */
    private static void refreshBankedAnger(ServerPlayer player) {
        int points = IdonaNodes.minorPoints(player, IdonaNodes.BANKED_ANGER);
        if (points <= 0) {
            GlobalDamageMultiplierRegistry.remove(player, BANKED_ANGER_KEY);
            return;
        }
        Optional<Vault> vault = ServerVaults.get(player.level);
        if (vault.isEmpty() || !vault.get().has(Vault.STATS)) {
            GlobalDamageMultiplierRegistry.remove(player, BANKED_ANGER_KEY);
            return;
        }
        StatsCollector stats = vault.get().get(Vault.STATS);
        StatCollector collector = stats.get(player.getUUID());
        if (collector == null) {
            GlobalDamageMultiplierRegistry.remove(player, BANKED_ANGER_KEY);
            return;
        }
        double taken = Math.max(0.0D, collector.getDamageTaken());
        if (!Double.isFinite(taken)) {
            WoldsVaults.LOGGER.error("Banked Anger read a non-finite damage-taken total for {}; treating it as zero.",
                    player.getGameProfile().getName());
            taken = 0.0D;
        }
        double curve = Math.log(IdonaNodes.bankedAngerBase() + taken) / Math.log(IdonaNodes.bankedAngerBase());
        float factor = (float) Math.pow(curve, points);
        GlobalDamageMultiplierRegistry.register(player, BANKED_ANGER_KEY, factor);
    }

    /**
     * Crushing Blows lands in the base mod's attack-damage multiplier registry rather than the
     * global one, because the sheet calls it an attack-damage multiplier: the base registry is
     * exactly the bucket that excludes ability and AoE damage, and it is the same bucket True Rage
     * and Cleave Expert re-export to the paths it skips.
     */
    private static void refreshCrushingBlows(ServerPlayer player) {
        int points = IdonaNodes.minorPoints(player, IdonaNodes.CRUSHING_BLOWS);
        if (points <= 0) {
            PlayerDamageHelper.removeMultiplier(player, CRUSHING_BLOWS_ID);
            return;
        }
        float value = (float) Math.pow(IdonaNodes.crushingBlowsMultiplier(), points);
        if (PlayerDamageHelper.hasMultiplier(player, CRUSHING_BLOWS_ID)) {
            return;
        }
        PlayerDamageHelper.applyMultiplier(CRUSHING_BLOWS_ID, player, value,
                PlayerDamageHelper.Operation.STACKING_MULTIPLY, false);
    }
}
