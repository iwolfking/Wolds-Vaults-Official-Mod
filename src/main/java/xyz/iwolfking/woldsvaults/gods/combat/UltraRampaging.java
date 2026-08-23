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
 * Ultra Rampaging: the damage math Fury drives, and the one place that decides whether it applies.
 *
 * <p>The node does not add a damage multiplier of its own. It rewrites the number Rampage already
 * uses - {@code AbstractRampageAbility#getDamageIncrease} - so the boost inherits every gate the
 * ordinary Rampage bonus has, and every node that reads that number reads the boosted one. That
 * includes True Rage and Cleave Expert, which is intended: those two becoming much larger nodes is
 * part of this node's design, not a side effect to be tuned out.
 *
 * <p>Two halves, joined at {@code cdm_sqrt_from}:
 *
 * <pre>
 *   additive = 100 * cbrt(fury / 6500)                     percentage points added to the CDM
 *   mult     = fury &lt;= 6000 ? 1.03^(fury / 100)
 *                           : 1.03^60 + sqrt(fury / 6000 - 1)
 *   CDM      = (CDM_base + additive) * mult
 * </pre>
 *
 * <p>The square-root half exists so the multiplier cannot balloon: left purely exponential, a
 * cleave build in a dense room reached ×3600 melee damage. The constant is the exponential's own
 * value at the handover, {@code 1.03^60 = 5.8916}, and the {@code -1} sits inside the root, so the
 * curve is exactly continuous there - a player crossing the threshold sees no step. With the Fury
 * cap at 15000 the whole design is hard-bounded at ×17.52 melee on a tier-8 Rampage and ×28.19 on
 * a tier-8 Berserker specialization.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class UltraRampaging {
    private static final int SYNC_INTERVAL_TICKS = 5;

    private static final Map<UUID, Curve> CURVES = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> LAST_SENT = new ConcurrentHashMap<>();

    private static volatile UltraRampagingConfig fallbackWarned;

    private UltraRampaging() {
    }

    /**
     * The node's tuned numbers, falling back to {@link UltraRampagingConfig#defaults()} if the god
     * node registry has not loaded yet. The fallback is logged once rather than every read: Fury
     * decays on the server tick, so a silent fallback here would be a config change nobody could
     * see, and a per-read log would be thousands of lines a minute.
     */
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

    /** Percentage points Fury adds to the CDM before the multiplier is applied. */
    public static float additivePercent(float fury, UltraRampagingConfig config) {
        if (fury <= 0.0F) {
            return 0.0F;
        }
        return 100.0F * (float) Math.cbrt(fury / config.cdm_additive_divisor());
    }

    /**
     * The multiplier Fury applies to the whole CDM. Exponential up to {@code cdm_sqrt_from},
     * square root above it, continuous at the handover by construction.
     */
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

    /**
     * Rewrites one Rampage specialization's damage increase for the Fury the player holds.
     *
     * <p>Expressed as {@code base * multiplier + addend} rather than by rebuilding the whole
     * expression, because both terms depend only on Fury. That is what lets the per-player curve
     * be cached and reused across the four calls the base mod makes per hit, one per Rampage
     * effect, without recomputing a cube root and a power each time.
     */
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

    /** Drops the cached curve so the next read recomputes it against the new Fury. */
    static void onFuryChanged(ServerPlayer player) {
        CURVES.remove(player.getUUID());
    }

    /**
     * Pushes the melee factor to the client when the number it would render changes.
     *
     * <p>Compared as the rendered integer rather than as a float, so a Fury value drifting by
     * fractions of a percent does not generate traffic. Fury only moves on a gain or on the
     * ten-tick decay, so in practice this is at most a couple of packets a second per player, and
     * only while the node is live.
     */
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

    /**
     * The CDM as a whole percent, floored rather than rounded. The damage itself is never floored -
     * that would be a needless discontinuity - but the readout is, so the number on screen is one
     * the player has definitely earned rather than one rounded up to.
     */
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
