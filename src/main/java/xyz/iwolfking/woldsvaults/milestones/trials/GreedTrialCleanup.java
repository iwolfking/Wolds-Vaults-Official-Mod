package xyz.iwolfking.woldsvaults.milestones.trials;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Drops trial marks whose vault is gone, every 200 server ticks. */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class GreedTrialCleanup {
    private static final int PERIOD_TICKS = 200;

    private static int ticks;

    private GreedTrialCleanup() {
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (++ticks % PERIOD_TICKS != 0) {
            return;
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        Set<UUID> live = new HashSet<>();
        for (Vault vault : ServerVaults.getAll()) {
            if (vault.has(Vault.ID)) {
                live.add(vault.get(Vault.ID));
            }
        }
        GreedTrialData data = GreedTrialData.get(server);
        for (UUID vaultId : data.getMarkedVaults()) {
            if (live.contains(vaultId)) {
                continue;
            }
            WoldsVaults.LOGGER.info("Clearing the rank {} greed trial mark for vault {}: the vault is gone, so the trial was not passed.",
                    data.getTargetRank(vaultId), vaultId);
            data.clear(vaultId);
        }
    }
}
