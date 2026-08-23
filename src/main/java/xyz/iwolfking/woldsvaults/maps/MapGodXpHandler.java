package xyz.iwolfking.woldsvaults.maps;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.Objectives;
import iskallia.vault.core.vault.player.Completion;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.vault.stat.StatCollector;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.PacketDistributor;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.GodAlignmentData;
import xyz.iwolfking.woldsvaults.gods.network.ClientboundVaultGodXpMessage;
import xyz.iwolfking.woldsvaults.network.NetworkHandler;

import java.util.UUID;

/**
 * Pays out a mapped vault's god experience. A map is a god quest: the vault it opened grants
 * experience only to the god the map was bound to, and only to players who leave it completed -
 * exactly the point where the milestone engine already reads {@code Completion} and
 * {@code Objectives.KEY}, so this listener uses the same event and the same reads.
 *
 * <p>Every runner who leaves completed is paid the full amount, by design: one map levelling a
 * whole party is the incentive to run maps together. "Leaves completed" is per runner and per
 * departure, not per vault end - a runner who stays behind after the objective falls is paid when
 * they walk out, because the binding outlives every individual exit and is only released once the
 * vault itself ends, which the base mod does after its last listener has gone. The award does not
 * care which charm, if any, the runner wears; the map names the god.
 *
 * <p>The award goes through {@link GodAlignmentData#addGodXp}, which means the God's Disciple
 * prestige powers scale it like every other god experience source.</p>
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class MapGodXpHandler {
    private static final Object OWNER = new Object();

    private MapGodXpHandler() {
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(MapGodXpHandler::register);
    }

    private static void register() {
        CommonEvents.LISTENER_LEAVE.register(OWNER, data -> onListenerLeave(data.getVault(), data.getListener()));
        CommonEvents.VAULT_END.register(OWNER, data -> {
            Vault vault = data.getVault();
            if (vault != null && vault.has(Vault.ID)) {
                MapGodVaultState.release(vault.get(Vault.ID));
            }
        });
    }

    private static void onListenerLeave(Vault vault, Listener listener) {
        if (vault == null || listener == null || !vault.has(Vault.ID)) {
            return;
        }
        UUID vaultId = vault.get(Vault.ID);
        MapGodVaultState.Binding binding = MapGodVaultState.get(vaultId).orElse(null);
        if (binding == null) {
            return;
        }
        ServerPlayer player = listener.getPlayer().orElse(null);
        if (player == null) {
            return;
        }
        Completion completion = vault.getOptional(Vault.STATS)
                .map(collector -> collector.get(listener))
                .flatMap(stats -> stats.getOptional(StatCollector.COMPLETION))
                .orElse(null);
        if (completion != Completion.COMPLETED) {
            return;
        }
        String objectiveKey = vault.getOptional(Vault.OBJECTIVES)
                .flatMap(objectives -> objectives.getOptional(Objectives.KEY))
                .orElse(null);
        long xp = MapGodXp.award(vault, objectiveKey, binding.difficultyMultiplier(), binding.bonusPercent());
        if (xp <= 0L) {
            WoldsVaults.LOGGER.warn("Mapped vault {} completed on objective '{}', which has no god xp value; {} was awarded nothing for {}.",
                    vaultId, objectiveKey, player.getGameProfile().getName(), binding.god().getName());
            return;
        }
        GodAlignmentData data = GodAlignmentData.get(player.server);
        NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                new ClientboundVaultGodXpMessage(vaultId, binding.god(), data.previewScaledXp(player, xp)));
        data.addGodXp(player, binding.god(), xp);
    }
}
