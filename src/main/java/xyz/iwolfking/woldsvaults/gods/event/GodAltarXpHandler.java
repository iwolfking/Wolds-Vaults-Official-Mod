package xyz.iwolfking.woldsvaults.gods.event;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.task.source.EntityTaskSource;
import iskallia.vault.task.source.TaskSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.GodAlignmentData;

import java.util.Set;

/** Awards god alignment XP for completed in-vault god altars. */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class GodAltarXpHandler {
    public static final double BASE_ALTAR_XP = 300.0D;

    private static final Object LISTENER_REF = new Object();

    private GodAltarXpHandler() {
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> CommonEvents.GOD_ALTAR_EVENT.register(LISTENER_REF, GodAltarXpHandler::onAltar));
    }

    /** The altar XP award; n is the lifetime count of altars completed across all four gods, before this one. */
    public static long altarXpFor(int lifetimeCompletions) {
        return Math.round(BASE_ALTAR_XP * Math.cbrt((10.0D + lifetimeCompletions) / 10.0D));
    }

    private static void onAltar(iskallia.vault.core.event.common.GodAltarEvent.Data data) {
        if (!data.isCompleted()) {
            return;
        }
        VaultGod god = data.getTask().getGod();
        if (god == null) {
            WoldsVaults.LOGGER.error("God altar completed with no god on its task at {}; no alignment XP awarded.", data.getPos());
            return;
        }
        MinecraftServer server = data.getContext().getServer();
        TaskSource source = data.getContext().getSource();
        if (server == null || !(source instanceof EntityTaskSource entitySource)) {
            WoldsVaults.LOGGER.error("God altar at {} completed without an entity task source; no alignment XP awarded.", data.getPos());
            return;
        }
        Set<ServerPlayer> players = entitySource.getEntities(server, ServerPlayer.class);
        GodAlignmentData alignmentData = GodAlignmentData.get(server);
        for (ServerPlayer player : players) {
            long xp = altarXpFor(alignmentData.getTotalAltarCompletions(player.getUUID()));
            alignmentData.addGodXp(player, god, xp);
            alignmentData.incrementAltarCompletions(player.getUUID(), god);
        }
    }
}
