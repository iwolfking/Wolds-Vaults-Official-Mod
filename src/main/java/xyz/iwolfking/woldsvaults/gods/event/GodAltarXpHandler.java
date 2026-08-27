package xyz.iwolfking.woldsvaults.gods.event;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.task.source.EntityTaskSource;
import iskallia.vault.task.source.TaskSource;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.PrestigePowerHelper;
import xyz.iwolfking.woldsvaults.gods.GodAlignmentData;
import xyz.iwolfking.woldsvaults.gods.GodLevels;
import xyz.iwolfking.woldsvaults.prestige.GodExperiencePrestigePower;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Awards god alignment XP for completed in-vault god altars. Altars pay whether or not the player has made
 * the god's Initiation offering - the sacrifice gates hold the level back, not the experience.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class GodAltarXpHandler {
    private static final Object LISTENER_REF = new Object();

    private static final Map<UUID, Integer> PENDING_REPUTATION = new ConcurrentHashMap<>();

    private GodAltarXpHandler() {
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> CommonEvents.GOD_ALTAR_EVENT.register(LISTENER_REF, GodAltarXpHandler::onAltar));
    }

    /**
     * Banks the reputation {@code PlayerReputationData#attemptFavour} just handed this player, for the
     * altar award to report alongside its experience. One entry per player, overwritten each altar.
     */
    public static void recordReputationGained(UUID playerId, int reputation) {
        PENDING_REPUTATION.put(playerId, reputation);
    }

    /** The altar XP award; n is the lifetime count of altars completed across all four gods, before this one. */
    public static long altarXpFor(int lifetimeCompletions) {
        return altarXpFor(lifetimeCompletions, 1.0D);
    }

    /** As above, with {@code baseMultiplier} scaling the configured base before the repeat multiplier. */
    public static long altarXpFor(int lifetimeCompletions, double baseMultiplier) {
        return Math.round(GodLevels.baseAltarXp() * baseMultiplier
                * Math.cbrt((10.0D + lifetimeCompletions) / 10.0D));
    }

    /** The product of every God's Disciple altar multiplier the player owns; 1 when they own none. */
    private static double altarBaseMultiplier(ServerPlayer player) {
        double multiplier = 1.0D;
        for (GodExperiencePrestigePower power
                : PrestigePowerHelper.getPrestigePowersOfType(player, GodExperiencePrestigePower.class)) {
            multiplier *= power.getAltarBaseMultiplier();
        }
        return multiplier;
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
            long xp = altarXpFor(alignmentData.getTotalAltarCompletions(player.getUUID()), altarBaseMultiplier(player));
            long banked = alignmentData.previewScaledXp(player, xp);
            alignmentData.grantGodXp(player, god, xp);
            alignmentData.incrementAltarCompletions(player.getUUID(), god);
            reportAward(player, god, banked, data.getReputationGained());
        }
    }

    /**
     * Replaces the base mod's bare "+N reputation" action bar with one line carrying both halves of the
     * altar's reward. Falls back to the party-wide figure when nothing was recorded for this player.
     */
    private static void reportAward(ServerPlayer player, VaultGod god, long xp, int fallbackReputation) {
        Integer recorded = PENDING_REPUTATION.remove(player.getUUID());
        int reputation = recorded == null ? fallbackReputation : recorded;
        if (recorded == null) {
            WoldsVaults.LOGGER.debug("No reputation was recorded for {} at a {} altar; reporting the task's {}.",
                    player.getGameProfile().getName(), god.getName(), fallbackReputation);
        }
        String text = reputation > 0
                ? "+" + reputation + " reputation, +" + xp + " " + god.getName() + " alignment XP"
                : "+" + xp + " " + god.getName() + " alignment XP";
        player.displayClientMessage(new TextComponent(text).withStyle(god.getChatColor()), true);
    }
}
