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
 * Per-player registry of named global damage multipliers. Every factor multiplies with every other
 * one, and the product applies once per hit on every path whose true source is a player.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class GlobalDamageMultiplierRegistry {
    /** Opt-out flag: a producer of %-of-health damage wraps its {@code hurt} call in {@code runWithFlag}. */
    public static final DamageFlag PERCENT_DAMAGE = new DamageFlag();

    private static final Map<UUID, Map<ResourceLocation, Factor>> FACTORS = new ConcurrentHashMap<>();
    private static final Set<String> EXCLUDED_SOURCES = ConcurrentHashMap.newKeySet();

    private GlobalDamageMultiplierRegistry() {
    }

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

    /** Marks a damage source id as percentage-based, so the global product skips it. */
    public static void excludeDamageSource(String damageSourceId) {
        EXCLUDED_SOURCES.add(damageSourceId);
    }

    /** Applies the global damage product to hits the player is the true source of, before armour. */
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

    /** Whether a hit computes its damage as a fraction of the target's health. */
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

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        FACTORS.clear();
    }
}
