package xyz.iwolfking.woldsvaults.gods.event;

import iskallia.vault.snapshot.AttributeSnapshotHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.ActiveGodResolver;
import xyz.iwolfking.woldsvaults.gods.GodAlignmentData;
import xyz.iwolfking.woldsvaults.gods.GodNodeCache;
import xyz.iwolfking.woldsvaults.gods.GodNodeState;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeTicker;
import xyz.iwolfking.woldsvaults.gods.sacrifice.SacrificeAltarLogic;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

/**
 * Lifecycle glue for the god core: keeps the active-god cache and the shared gate cache honest
 * across equipment and dimension changes, tears down live node state on logout, syncs alignment
 * state to joining players, and announces level-ups.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class GodEventHandlers {
    private GodEventHandlers() {
    }

    /**
     * Fires on both logical sides; the client cache must drop too, or screens showing the active
     * god keep displaying the pre-swap charm until a dimension change. Server side additionally
     * schedules a snapshot rebuild, which is what folds the newly active tree's values in and
     * reconciles the vanilla-attribute bridges - without it a charm swap only takes effect on the
     * next unrelated gear change - and runs the tick contributors' deactivation diff, which is what
     * takes back anything the outgoing tree applied outside the snapshot.
     *
     * <p>The alignment sync is what refreshes piety. Piety is reputation and god level and the
     * bonus the active tree's Pious Devotion pays, so swapping charms changes it without any
     * alignment mutation having happened - and the tree screen and greed panel read the synced
     * copy, not the server's live one.
     */
    @SubscribeEvent
    public static void onCurioChange(CurioChangeEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (entity instanceof Player player) {
            ActiveGodResolver.invalidate(player);
            GodNodeCache.invalidate(player);
            if (player instanceof ServerPlayer serverPlayer && serverPlayer.getServer() != null) {
                AttributeSnapshotHelper.getInstance().refreshSnapshotDelayed(serverPlayer);
                GodNodeTicker.reconcile(serverPlayer);
                GodAlignmentData.get(serverPlayer.getServer()).sync(serverPlayer);
            }
        }
    }

    /**
     * No vanilla-attribute repair runs here on purpose. Every modifier the god core writes is
     * transient, so none of them survive to a new session and there is nothing a login pass could
     * find to remove.
     */
    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player && player.getServer() != null) {
            ActiveGodResolver.invalidate(player);
            GodNodeCache.invalidate(player);
            GodAlignmentData.get(player.getServer()).sync(player);
        }
    }

    /**
     * The tick contributors are deactivated first, while the player is still a live entity and
     * their scratch is still there, because that is the only moment a contributor can take back a
     * modifier or an effect it applied to them.
     */
    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            GodNodeTicker.deactivateAll(player);
        }
        ActiveGodResolver.invalidate(event.getPlayer());
        GodNodeCache.invalidate(event.getPlayer().getUUID());
        GodNodeState.clear(event.getPlayer().getUUID());
    }

    /**
     * Vault entry and exit change whether a charm counts as usable, so the resolved god is
     * recomputed on every dimension change and on respawn.
     */
    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        ActiveGodResolver.invalidate(event.getPlayer());
        GodNodeCache.invalidate(event.getPlayer());
        if (event.getPlayer() instanceof ServerPlayer player) {
            GodNodeTicker.reconcile(player);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        ActiveGodResolver.invalidate(event.getPlayer());
        GodNodeCache.invalidate(event.getPlayer());
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        ActiveGodResolver.invalidate(event.getPlayer());
        ActiveGodResolver.invalidate(event.getOriginal());
        GodNodeCache.invalidate(event.getPlayer());
        GodNodeCache.invalidate(event.getOriginal());
    }

    @SubscribeEvent
    public static void announceLevelUp(GodLevelUpEvent event) {
        event.getPlayer().sendMessage(new TranslatableComponent("message.woldsvaults.god_level_up",
                event.getGod().getHoverChatComponent(),
                event.getNewLevel()).withStyle(ChatFormatting.GOLD), net.minecraft.Util.NIL_UUID);
    }

    /**
     * Drops every server-scoped god cache when the server stops. On an integrated server the JVM
     * outlives the world, so without this the next world loads with the previous one's resolved
     * gods, gate results and vault scratch still in memory.
     */
    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        GodNodeState.clearAll();
        GodNodeCache.invalidateAll();
        ActiveGodResolver.invalidateAll();
        SacrificeAltarLogic.clearAll();
    }
}
