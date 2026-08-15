package xyz.iwolfking.woldsvaults.gods.trees.wendarr;

import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Snapshot-derived stats that a god node needs while the snapshot is being built.
 *
 * <p>Efficient Steps converts fruit efficiency into movement speed, but the god fold runs
 * <em>inside</em> snapshot construction, so asking {@code AttributeSnapshotHelper} for the value
 * there would re-enter a half-built snapshot. Sampling once a second from the server tick, well
 * outside construction, sidesteps that entirely; fruit efficiency only moves on gear and node
 * changes, so a one-second lag is invisible.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class WendarrStatCache {
    private static final int SAMPLE_INTERVAL_TICKS = 20;

    private static final Map<UUID, Float> FRUIT_EFFICIENCY = new ConcurrentHashMap<>();
    private static int tickCounter;

    private WendarrStatCache() {
    }

    public static float getFruitEfficiency(ServerPlayer player) {
        return FRUIT_EFFICIENCY.getOrDefault(player.getUUID(), 0.0F);
    }

    @SubscribeEvent
    public static void sample(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || ++tickCounter < SAMPLE_INTERVAL_TICKS) {
            return;
        }
        tickCounter = 0;
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!WendarrNodes.hasMinor(player, WendarrNodes.EFFICIENT_STEPS)) {
                FRUIT_EFFICIENCY.remove(player.getUUID());
                continue;
            }
            float efficiency = AttributeSnapshotHelper.getInstance().getSnapshot(player)
                    .getAttributeValue(ModGearAttributes.FRUIT_EFFECTIVENESS, VaultGearAttributeTypeMerger.floatSum());
            FRUIT_EFFICIENCY.put(player.getUUID(), efficiency);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        FRUIT_EFFICIENCY.remove(event.getPlayer().getUUID());
    }
}
