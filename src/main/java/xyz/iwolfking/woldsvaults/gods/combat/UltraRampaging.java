package xyz.iwolfking.woldsvaults.gods.combat;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.node.GodEffect;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeRegistry;
import xyz.iwolfking.woldsvaults.gods.trees.idona.IdonaNodes;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.network.message.RampageCdmMessage;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ultra Rampaging: the damage math Fury drives. The node adds no multiplier of its own; it rewrites
 * {@code AbstractRampageAbility#getDamageIncrease}.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class UltraRampaging {
    private static final int SYNC_INTERVAL_TICKS = 5;

    private static final Map<UUID, Curve> CURVES = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> LAST_SENT = new ConcurrentHashMap<>();

    private static volatile UltraRampagingConfig fallbackWarned;

    private UltraRampaging() {
    }

    /** The node's tuned numbers, falling back to {@link UltraRampagingConfig#defaults()} with an error. */
    public static UltraRampagingConfig config() {
        Optional<GodEffect> effect = GodNodeRegistry.effect(IdonaNodes.ULTRA_RAMPAGING);
        if (effect.isPresent() && effect.get().params() instanceof UltraRampagingConfig params) {
            return params;
        }
        UltraRampagingConfig defaults = UltraRampagingConfig.defaults();
        if (fallbackWarned == null) {
            fallbackWarned = defaults;
            WoldsVaults.LOGGER.error("Ultra Rampaging read its configuration before god_node_effects_idona.json "
                    + "finished loading (effect '{}' {}). Falling back to built-in defaults; any config override of "
                    + "the Fury numbers is being ignored until the registry loads.",
                    IdonaNodes.ULTRA_RAMPAGING, effect.isPresent() ? "has the wrong params type" : "is absent");
        }
        return defaults;
    }

    public static boolean isActive(ServerPlayer player) {
        return IdonaNodes.isActive(player, IdonaNodes.ULTRA_RAMPAGING);
    }

    public static float additivePercent(float fury, UltraRampagingConfig config) {
        if (fury <= 0.0F) {
            return 0.0F;
        }
        return 100.0F * (float) Math.cbrt(fury / config.cdm_additive_divisor());
    }

    /** The multiplier Fury applies to the CDM: exponential up to {@code cdm_sqrt_from}, root above. */
    public static float furyMultiplier(float fury, UltraRampagingConfig config) {
        if (fury <= 0.0F) {
            return 1.0F;
        }
        float handover = config.cdm_sqrt_from();
        if (fury <= handover) {
            return (float) Math.pow(config.cdm_multiplier_base(), fury / 100.0F);
        }
        float atHandover = (float) Math.pow(config.cdm_multiplier_base(), handover / 100.0F);
        return atHandover + (float) Math.sqrt(fury / handover - 1.0F);
    }

    /** The drawback: raw incoming damage is multiplied by this while the player holds Fury. */
    public static float incomingMultiplier(float fury, UltraRampagingConfig config) {
        if (fury <= 0.0F) {
            return 1.0F;
        }
        return 1.0F + (float) Math.sqrt(fury / config.incoming_divisor()) * config.incoming_scale();
    }

    /** Rewrites one Rampage specialization's damage increase for the Fury the player holds. */
    public static float applyCdm(ServerPlayer player, float baseIncrease) {
        Curve curve = curve(player);
        if (curve == null) {
            return baseIncrease;
        }
        return baseIncrease * curve.multiplier() + curve.addend();
    }

    private static Curve curve(ServerPlayer player) {
        if (!isActive(player)) {
            return null;
        }
        float fury = PlayerFuryHelper.get(player);
        if (fury <= 0.0F) {
            return null;
        }
        UUID id = player.getUUID();
        Curve cached = CURVES.get(id);
        if (cached != null && cached.fury() == fury) {
            return cached;
        }
        UltraRampagingConfig config = config();
        float multiplier = furyMultiplier(fury, config);
        float addend = additivePercent(fury, config) * multiplier / 100.0F;
        Curve curve = new Curve(fury, multiplier, addend);
        CURVES.put(id, curve);
        return curve;
    }

    static void onFuryChanged(ServerPlayer player) {
        CURVES.remove(player.getUUID());
    }

    @SubscribeEvent
    public static void syncDisplay(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null || server.getTickCount() % SYNC_INTERVAL_TICKS != 0) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            int shown = displayPercent(player);
            Integer previous = LAST_SENT.get(player.getUUID());
            if (previous != null && previous == shown) {
                continue;
            }
            LAST_SENT.put(player.getUUID(), shown);
            ModNetwork.sendToClient(new RampageCdmMessage(shown), player);
        }
    }

    /** The CDM as a whole percent, floored rather than rounded. */
    private static int displayPercent(ServerPlayer player) {
        if (PlayerFuryHelper.get(player) <= 0.0F && !RampageAccess.hasAnyRampageEffect(player)) {
            return 0;
        }
        return (int) Math.floor(RampageAccess.effectiveDamageIncrease(player) * 100.0F);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.getPlayer().getUUID();
        CURVES.remove(id);
        LAST_SENT.remove(id);
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        CURVES.clear();
        LAST_SENT.clear();
    }

    private record Curve(float fury, float multiplier, float addend) {
    }
}
