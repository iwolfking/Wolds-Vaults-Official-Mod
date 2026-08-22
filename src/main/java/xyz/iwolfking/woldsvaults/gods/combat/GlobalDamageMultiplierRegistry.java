package xyz.iwolfking.woldsvaults.gods.combat;

import iskallia.vault.event.ActiveFlags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-player registry of named global damage multipliers. Every registered factor multiplies with
 * every other one (never additive), and the product is applied once per hit.
 *
 * <p>This is deliberately separate from the base mod's {@code PlayerDamageHelper} registry, which
 * excludes AoE- and AP-flagged damage. A factor registered here reaches every path where the true
 * source of the hit is a player.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class GlobalDamageMultiplierRegistry {
    /**
     * Reference-counted opt-out for percentage-based damage. Any producer that computes damage as
     * a fraction of the target's health must wrap its {@code hurt} call in
     * {@code PERCENT_DAMAGE.runWithFlag(...)} so the global product does not compound it.
     */
    public static final DamageFlag PERCENT_DAMAGE = new DamageFlag();

    private static final Map<UUID, Map<ResourceLocation, Factor>> FACTORS = new ConcurrentHashMap<>();
    private static final Set<String> EXCLUDED_SOURCES = ConcurrentHashMap.newKeySet();

    private GlobalDamageMultiplierRegistry() {
    }

    /** Registers or overwrites a permanent factor. A factor of 1.0 is a no-op but still stored. */
    public static void register(Player player, ResourceLocation key, float factor) {
        register(player, key, factor, Integer.MAX_VALUE);
    }

    /** Registers or overwrites a factor that expires after {@code durationTicks} server ticks. */
    public static void register(Player player, ResourceLocation key, float factor, int durationTicks) {
        if (factor < 0.0F) {
            WoldsVaults.LOGGER.error("Refusing to register negative global damage factor {} for key {}; clamping to 0.", factor, key);
            factor = 0.0F;
        }
        FACTORS.computeIfAbsent(player.getUUID(), id -> Collections.synchronizedMap(new LinkedHashMap<>()))
                .put(key, new Factor(factor, durationTicks));
    }

    /** Resets an existing factor's remaining duration without changing its value. */
    public static boolean refresh(Player player, ResourceLocation key, int durationTicks) {
        Factor factor = FACTORS.getOrDefault(player.getUUID(), Collections.emptyMap()).get(key);
        if (factor == null) {
            return false;
        }
        factor.remainingTicks = durationTicks;
        return true;
    }

    public static boolean remove(Player player, ResourceLocation key) {
        return FACTORS.getOrDefault(player.getUUID(), Collections.emptyMap()).remove(key) != null;
    }

    public static void removeAll(Player player) {
        FACTORS.remove(player.getUUID());
    }

    public static float getFactor(Player player, ResourceLocation key) {
        Factor factor = FACTORS.getOrDefault(player.getUUID(), Collections.emptyMap()).get(key);
        return factor == null ? 1.0F : factor.value;
    }

    /** The multiplicative product of every live factor for this player. */
    public static float getProduct(Player player) {
        Map<ResourceLocation, Factor> factors = FACTORS.get(player.getUUID());
        if (factors == null || factors.isEmpty()) {
            return 1.0F;
        }
        float product = 1.0F;
        synchronized (factors) {
            for (Factor factor : factors.values()) {
                product *= factor.value;
            }
        }
        return product;
    }

    public static Map<ResourceLocation, Float> view(Player player) {
        Map<ResourceLocation, Factor> factors = FACTORS.get(player.getUUID());
        if (factors == null) {
            return Collections.emptyMap();
        }
        Map<ResourceLocation, Float> copy = new LinkedHashMap<>();
        synchronized (factors) {
            factors.forEach((key, factor) -> copy.put(key, factor.value));
        }
        return copy;
    }

    /**
     * Marks a damage source id as percentage-based, so the global product skips it. Producers that
     * cannot wrap their call site in {@link #PERCENT_DAMAGE} use this instead.
     */
    public static void excludeDamageSource(String damageSourceId) {
        EXCLUDED_SOURCES.add(damageSourceId);
    }

    /**
     * Applies the player's global damage product to every hit they are the true source of.
     *
     * <p>Runs at {@link EventPriority#LOW} on {@link LivingHurtEvent} — after the base mod's
     * {@code NORMAL}-priority damage pipeline ({@code GearAttributeEvents.increaseDamageDealt} and
     * {@code PlayerDamageHelper.onPlayerDamage}) so it composes multiplicatively on top of them,
     * and before {@code LuckyHitTalent} at {@code LOWEST}. {@code LivingHurtEvent} rather than
     * {@code LivingDamageEvent} so the result is still subject to the target's armour and
     * resistance, exactly like every other damage bonus in the game.
     *
     * <p>Percentage-based damage is excluded, because multiplying a fraction of the target's max
     * health is not what "global damage multiplier" means. Excluded are: anything flagged
     * {@code IS_DOT_ATTACKING} (damage-over-time ticks), anything flagged
     * {@code IS_GLACIAL_SHATTER_ATTACKING} (Glacial Shatter deals max health x0.25 / x1.5),
     * anything running under {@link #PERCENT_DAMAGE}, and any damage source id registered through
     * {@link #excludeDamageSource(String)}. Bleed needs no exclusion — it drains health directly
     * and never raises this event.
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void applyGlobalMultiplier(LivingHurtEvent event) {
        DamageSource source = event.getSource();
        Entity attacker = source.getEntity();
        if (!(attacker instanceof ServerPlayer player)) {
            return;
        }
        if (isPercentageBased(source)) {
            return;
        }
        float product = getProduct(player);
        if (product != 1.0F) {
            event.setAmount(event.getAmount() * product);
        }
    }

    /**
     * Whether a hit computes its damage as a fraction of the target's health, and so must not be
     * scaled by anything that means "more damage". True for anything flagged
     * {@code IS_DOT_ATTACKING} or {@code IS_GLACIAL_SHATTER_ATTACKING}, anything running under
     * {@link #PERCENT_DAMAGE}, and any damage source id passed to
     * {@link #excludeDamageSource(String)}.
     *
     * <p>This is the one implementation of that test. Every damage multiplier in the god core -
     * this registry, the god node combat pipeline and the per-god hit handlers - asks here, because
     * two hand-rolled copies of it had already drifted apart once.
     */
    public static boolean isPercentageBased(DamageSource source) {
        return PERCENT_DAMAGE.isSet()
                || ActiveFlags.IS_DOT_ATTACKING.isSet()
                || ActiveFlags.IS_GLACIAL_SHATTER_ATTACKING.isSet()
                || EXCLUDED_SOURCES.contains(source.getMsgId());
    }

    @SubscribeEvent
    public static void tickTimedFactors(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        FACTORS.values().forEach(factors -> {
            synchronized (factors) {
                factors.values().removeIf(Factor::tickAndCheckExpiry);
            }
        });
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        FACTORS.remove(event.getPlayer().getUUID());
    }

    private static final class Factor {
        private final float value;
        private int remainingTicks;

        private Factor(float value, int remainingTicks) {
            this.value = value;
            this.remainingTicks = remainingTicks;
        }

        private boolean tickAndCheckExpiry() {
            if (this.remainingTicks == Integer.MAX_VALUE) {
                return false;
            }
            return --this.remainingTicks <= 0;
        }
    }

    /** Reference-counted thread-local flag, matching the shape of the addon's own active flags. */
    public static final class DamageFlag {
        private final ThreadLocal<Integer> references = ThreadLocal.withInitial(() -> 0);

        private DamageFlag() {
        }

        public boolean isSet() {
            return this.references.get() > 0;
        }

        public void runWithFlag(Runnable run) {
            this.references.set(this.references.get() + 1);
            try {
                run.run();
            } finally {
                this.references.set(this.references.get() - 1);
            }
        }
    }

    /** Drops every player's factors when the server stops, so they cannot cross a world switch. */
    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        FACTORS.clear();
    }
}
