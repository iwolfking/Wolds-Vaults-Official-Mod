package xyz.iwolfking.woldsvaults.gods.ultimates;

import iskallia.vault.event.ActiveFlags;
import iskallia.vault.util.AABBHelper;
import iskallia.vault.util.damage.PlayerDamageHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.combat.AbilityPowerDamageMultipliers;
import xyz.iwolfking.woldsvaults.gods.combat.FinalDamageStage;
import xyz.iwolfking.woldsvaults.gods.combat.GlobalDamageMultiplierRegistry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The whole of Copé de Grâce apart from the ability shell: the fifteen-second infusion, the
 * per-hit compounding stacks it builds, the damage it books while it runs, and the terminal dash
 * that spends that book.
 *
 * <p>Three seams carry it.
 *
 * <ul>
 *   <li><b>Attack damage</b> is a timed {@code STACKING_MULTIPLY} entry in the base mod's own
 *       multiplier registry, and its {@code onTimeout} consumer is what fires the dash — the
 *       timeout already runs on the server tick, so the happy path needs no second timer.
 *   <li><b>Ability power</b> is the mirror entry on the previously dead ability-power side of that
 *       same registry, through the wave-1 producer.
 *   <li><b>Resistance</b> is <em>not</em> the resistance stat. Copé's 0.5-0.85 is its own
 *       damage-reduction source, multiplicative with armour and with ordinary resistance, so it is
 *       a final-damage sub-stage and needs no cap mixin to reach its top value.
 * </ul>
 *
 * <p>Compounding is additive within the buff and multiplicative with everything outside it: four
 * hits at 5% give a 1.20x factor, published as a single global damage factor that is rewritten as
 * the count climbs. The book the dash spends is post-armour damage, sampled after every other
 * final-damage sub-stage has had its say.
 */
public final class CopeDeGraceState {
    private static final float DASH_RANGE = 4.0F;
    private static final int DASH_WINDOW_TICKS = 20;
    private static final int SAFETY_GRACE_TICKS = 40;
    private static final double DASH_MAGNITUDE = 1.8D;
    private static final double ACCUMULATOR_CAP = 1.0E9D;

    private static final Map<UUID, State> STATES = new ConcurrentHashMap<>();
    private static final Map<UUID, DashWindow> DASHES = new ConcurrentHashMap<>();

    private static boolean overflowLogged = false;

    private CopeDeGraceState() {
    }

    /** Installs the two final-damage sub-stages the infusion needs. Called once, from setup. */
    public static void register() {
        FinalDamageStage.register(UltimateIds.COPE_RESISTANCE_STAGE, FinalDamageStage.ORDER_REDUCTION,
                CopeDeGraceState::reduceIncoming);
        FinalDamageStage.register(UltimateIds.COPE_ACCUMULATOR_STAGE, FinalDamageStage.ORDER_FLOOR + 100,
                CopeDeGraceState::bookOutgoing);
    }

    public static boolean isActive(Player player) {
        return STATES.containsKey(player.getUUID());
    }

    /** Starts the infusion, replacing any infusion already running on this player. */
    public static void begin(ServerPlayer player, UltimateLevels.Cope values) {
        STATES.put(player.getUUID(), new State(values));
        GlobalDamageMultiplierRegistry.remove(player, UltimateIds.COPE_STACKS_FACTOR);
        PlayerDamageHelper.applyTimedMultiplier(UltimateIds.COPE_ATTACK_DAMAGE, player,
                values.attackDamageMultiplier(), PlayerDamageHelper.Operation.STACKING_MULTIPLY, true,
                values.durationTicks(), CopeDeGraceState::onInfusionTimeout);
        AbilityPowerDamageMultipliers.applyTimed(UltimateIds.COPE_ABILITY_POWER, player,
                values.abilityPowerMultiplier(), PlayerDamageHelper.Operation.STACKING_MULTIPLY, true,
                values.durationTicks());
    }

    private static float reduceIncoming(LivingDamageEvent event, float amount) {
        if (!(event.getEntityLiving() instanceof ServerPlayer player)) {
            return amount;
        }
        State state = STATES.get(player.getUUID());
        return state == null ? amount : amount * (1.0F - state.values.resistance());
    }

    private static float bookOutgoing(LivingDamageEvent event, float amount) {
        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof ServerPlayer player) || amount <= 0.0F) {
            return amount;
        }
        State state = STATES.get(player.getUUID());
        if (state == null || event.getEntityLiving() == player) {
            return amount;
        }
        state.accumulated += amount;
        if (state.accumulated > ACCUMULATOR_CAP) {
            state.accumulated = ACCUMULATOR_CAP;
            if (!overflowLogged) {
                overflowLogged = true;
                WoldsVaults.LOGGER.error("Cope de Grace damage book hit its {} cap; the dash is being clamped.",
                        ACCUMULATOR_CAP);
            }
        }
        state.hits++;
        GlobalDamageMultiplierRegistry.register(player, UltimateIds.COPE_STACKS_FACTOR,
                1.0F + state.values.compoundingPerHit() * state.hits, Math.max(state.buffTicks, 1));
        return amount;
    }

    /**
     * Fired from inside the damage helper's own tick, while it is iterating its multiplier map, so
     * this path must not remove anything from that map — the paired ability-power entry carries the
     * same duration and retires itself on the same pass.
     */
    private static void onInfusionTimeout(ServerPlayer player) {
        State state = STATES.remove(player.getUUID());
        if (state != null) {
            releaseDash(player, state, false);
        }
    }

    /**
     * Ends the infusion and launches the dash: a forward impulse plus a one-second sweep that hurts
     * every entity it passes once, for the booked damage times the level's compounding share.
     */
    private static void releaseDash(ServerPlayer player, State state, boolean tearDownMultipliers) {
        GlobalDamageMultiplierRegistry.remove(player, UltimateIds.COPE_STACKS_FACTOR);
        if (tearDownMultipliers) {
            PlayerDamageHelper.removeMultiplier(player, UltimateIds.COPE_ATTACK_DAMAGE);
            PlayerDamageHelper.removeMultiplier(player, UltimateIds.COPE_ABILITY_POWER);
        }
        if (!player.isAlive()) {
            return;
        }
        Vec3 look = player.getLookAngle();
        Vec3 dash = new Vec3(look.x(), Math.max(look.y(), 0.15D), look.z()).normalize().scale(DASH_MAGNITUDE);
        player.push(dash.x(), dash.y(), dash.z());
        player.hurtMarked = true;
        float damage = (float) Math.min(state.accumulated * state.values.compoundingPerHit(), ACCUMULATOR_CAP);
        if (damage > 0.0F) {
            DASHES.put(player.getUUID(), new DashWindow(damage));
        }
    }

    /**
     * Runs the infusion clocks and the dash sweeps. The clock here is a safety net rather than the
     * primary timer — the damage multiplier's own timeout is — so it runs a grace period past the
     * nominal duration and only catches an infusion whose multiplier stopped ticking, which happens
     * when its owner was offline at the moment it should have expired.
     */
    public static void tick(MinecraftServer server) {
        if (!STATES.isEmpty()) {
            Iterator<Map.Entry<UUID, State>> states = STATES.entrySet().iterator();
            while (states.hasNext()) {
                Map.Entry<UUID, State> entry = states.next();
                State state = entry.getValue();
                state.buffTicks--;
                if (--state.safetyTicks > 0) {
                    continue;
                }
                states.remove();
                ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
                if (player != null) {
                    releaseDash(player, state, true);
                }
            }
        }
        if (DASHES.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<UUID, DashWindow>> dashes = DASHES.entrySet().iterator();
        while (dashes.hasNext()) {
            Map.Entry<UUID, DashWindow> entry = dashes.next();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            DashWindow window = entry.getValue();
            if (player == null || !player.isAlive() || --window.remainingTicks < 0) {
                dashes.remove();
                continue;
            }
            sweep(player, window);
        }
    }

    private static void sweep(ServerPlayer player, DashWindow window) {
        TargetingConditions conditions = TargetingConditions.forNonCombat().range(DASH_RANGE)
                .selector(entity -> !(entity instanceof Player));
        List<LivingEntity> nearby = new ArrayList<>(player.level.getNearbyEntities(LivingEntity.class, conditions,
                player, AABBHelper.create(player.position(), DASH_RANGE)));
        for (LivingEntity target : nearby) {
            if (!window.hit.add(target.getUUID())) {
                continue;
            }
            ActiveFlags.IS_AOE_ATTACKING.runIfNotSet(() -> target.hurt(DamageSource.playerAttack(player), window.damage));
        }
    }

    public static void clear(Player player) {
        STATES.remove(player.getUUID());
        DASHES.remove(player.getUUID());
        GlobalDamageMultiplierRegistry.remove(player, UltimateIds.COPE_STACKS_FACTOR);
        if (player instanceof ServerPlayer serverPlayer) {
            PlayerDamageHelper.removeMultiplier(serverPlayer, UltimateIds.COPE_ATTACK_DAMAGE);
            PlayerDamageHelper.removeMultiplier(serverPlayer, UltimateIds.COPE_ABILITY_POWER);
        }
    }

    private static final class State {
        private final UltimateLevels.Cope values;
        private int buffTicks;
        private int safetyTicks;
        private int hits;
        private double accumulated;

        private State(UltimateLevels.Cope values) {
            this.values = values;
            this.buffTicks = values.durationTicks();
            this.safetyTicks = values.durationTicks() + SAFETY_GRACE_TICKS;
        }
    }

    private static final class DashWindow {
        private final float damage;
        private final Set<UUID> hit = new HashSet<>();
        private int remainingTicks = DASH_WINDOW_TICKS;

        private DashWindow(float damage) {
            this.damage = damage;
        }
    }

    /**
     * Drops every player's state. Called on server stop only: the modifiers these states apply are
     * transient, so the entries are what outlive a world, not the modifiers themselves.
     */
    public static void clearAll() {
        STATES.clear();
        DASHES.clear();
    }
}
