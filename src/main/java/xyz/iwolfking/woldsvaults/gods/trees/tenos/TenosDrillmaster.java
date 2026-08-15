package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Drillmaster (r111): raises the Fortune cap from 5 to 7 for this player.
 *
 * <p>The cap lives on the enchantment read path, and that path is one of the hottest methods in
 * the game, so the node membership is sampled once a second into a set and the read path only ever
 * does a hash lookup - never a god data lookup.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class TenosDrillmaster {
    public static final int RAISED_FORTUNE_CAP = 7;

    private static final int SAMPLE_INTERVAL_TICKS = 20;

    private static final Set<UUID> HOLDERS = ConcurrentHashMap.newKeySet();
    private static int tickCounter;

    private TenosDrillmaster() {
    }

    /** Called from the enchantment cap map; must stay allocation free. */
    public static boolean hasRaisedFortuneCap(LivingEntity entity) {
        return !HOLDERS.isEmpty() && entity != null && HOLDERS.contains(entity.getUUID());
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
            if (TenosNodes.hasMinor(player, TenosNodes.DRILLMASTER)) {
                HOLDERS.add(player.getUUID());
            } else {
                HOLDERS.remove(player.getUUID());
            }
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        HOLDERS.remove(event.getPlayer().getUUID());
    }
}
