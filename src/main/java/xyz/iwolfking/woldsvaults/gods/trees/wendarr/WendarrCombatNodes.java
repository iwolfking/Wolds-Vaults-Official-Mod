package xyz.iwolfking.woldsvaults.gods.trees.wendarr;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.time.TickClock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.combat.FinalDamageStage;
import xyz.iwolfking.woldsvaults.gods.combat.GlobalDamageMultiplierRegistry;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Wendarr's damage nodes: Paced Strikes (r74), Edge of Time (r75) and Temporal Shielding (r79).
 *
 * <p>Both time-for-damage trades bill the vault clock. Vault time is shared by the whole party, so
 * the debt is accumulated per player and settled at most once a second instead of writing the
 * clock on every hit -  the per-instance cost is unchanged, the clock writes are not per-hit.
 * The two reducers sit in the shared final damage stage rather than owning their own
 * {@code LivingDamageEvent} listeners, which is what keeps them ordered against Second Chance.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class WendarrCombatNodes {
    public static final float EDGE_OF_TIME_MULTIPLIER = 10.0F;
    public static final float TEMPORAL_SHIELDING_REDUCTION = 0.10F;
    public static final int DRAIN_MIN_TICKS = 20;
    public static final int DRAIN_MAX_TICKS = 60;
    public static final float PACED_STRIKES_REFERENCE_MINUTES = 50.0F;

    private static final ResourceLocation PACED_STRIKES_KEY = WoldsVaults.id("wendarr_paced_strikes");
    private static final ResourceLocation EDGE_OF_TIME_KEY = WoldsVaults.id("wendarr_edge_of_time");
    private static final ResourceLocation TEMPORAL_SHIELDING_STAGE = WoldsVaults.id("wendarr_temporal_shielding");
    private static final int SETTLE_INTERVAL_TICKS = 20;

    private static final Map<UUID, Integer> PENDING_DRAIN = new ConcurrentHashMap<>();
    private static final Object OWNER = new Object();

    private WendarrCombatNodes() {
    }

    static void register() {
        FinalDamageStage.register(TEMPORAL_SHIELDING_STAGE, FinalDamageStage.ORDER_REDUCTION, (event, amount) -> {
            if (!(event.getEntityLiving() instanceof ServerPlayer player)) {
                return amount;
            }
            if (!WendarrNodes.hasMinor(player, WendarrNodes.TEMPORAL_SHIELDING)) {
                return amount;
            }
            queueDrain(player);
            return amount * TEMPORAL_SHIELDING_REDUCTION;
        });
        CommonEvents.LISTENER_TICK.register(OWNER, data -> {
            Vault vault = data.getVault();
            TickClock clock = vault.get(Vault.CLOCK);
            if (clock == null || clock.get(TickClock.GLOBAL_TIME) % SETTLE_INTERVAL_TICKS != 0) {
                return;
            }
            for (ServerPlayer player : WendarrVaultTime.runners(vault)) {
                updatePacedStrikes(vault, player);
                updateEdgeOfTime(player);
                settleDrain(vault, player);
            }
        });
    }

    /** Damage grows with the time left, so it is recomputed once a second rather than per hit. */
    private static void updatePacedStrikes(Vault vault, ServerPlayer player) {
        if (!WendarrNodes.hasMinor(player, WendarrNodes.PACED_STRIKES)) {
            GlobalDamageMultiplierRegistry.remove(player, PACED_STRIKES_KEY);
            return;
        }
        TickClock clock = vault.get(Vault.CLOCK);
        float minutesLeft = clock == null ? 0.0F : Math.max(0, clock.get(TickClock.DISPLAY_TIME)) / 20.0F / 60.0F;
        float factor = (float) Math.sqrt((PACED_STRIKES_REFERENCE_MINUTES + minutesLeft) / PACED_STRIKES_REFERENCE_MINUTES);
        GlobalDamageMultiplierRegistry.register(player, PACED_STRIKES_KEY, factor);
    }

    private static void updateEdgeOfTime(ServerPlayer player) {
        if (WendarrNodes.hasMajor(player, WendarrNodes.EDGE_OF_TIME)) {
            GlobalDamageMultiplierRegistry.register(player, EDGE_OF_TIME_KEY, EDGE_OF_TIME_MULTIPLIER);
        } else {
            GlobalDamageMultiplierRegistry.remove(player, EDGE_OF_TIME_KEY);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void billEdgeOfTime(LivingHurtEvent event) {
        if (event.isCanceled() || event.getAmount() <= 0.0F) {
            return;
        }
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (WendarrNodes.hasMajor(player, WendarrNodes.EDGE_OF_TIME)) {
            queueDrain(player);
        }
    }

    private static void queueDrain(ServerPlayer player) {
        int ticks = DRAIN_MIN_TICKS + player.getRandom().nextInt(DRAIN_MAX_TICKS - DRAIN_MIN_TICKS + 1);
        PENDING_DRAIN.merge(player.getUUID(), ticks, Integer::sum);
    }

    private static void settleDrain(Vault vault, ServerPlayer player) {
        Integer owed = PENDING_DRAIN.remove(player.getUUID());
        if (owed != null && owed > 0) {
            WendarrVaultTime.drainTicks(vault, owed);
        }
    }

    public static void clearPlayer(UUID playerId) {
        PENDING_DRAIN.remove(playerId);
    }
}
