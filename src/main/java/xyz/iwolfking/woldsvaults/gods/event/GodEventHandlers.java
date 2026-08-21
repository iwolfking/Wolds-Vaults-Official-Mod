package xyz.iwolfking.woldsvaults.gods.event;

import iskallia.vault.snapshot.AttributeSnapshotHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.ActiveGodResolver;
import xyz.iwolfking.woldsvaults.gods.GodAlignmentData;
import xyz.iwolfking.woldsvaults.gods.GodNodeCache;
import xyz.iwolfking.woldsvaults.gods.GodNodeState;
import xyz.iwolfking.woldsvaults.gods.GodVanillaAttributes;
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
     * next unrelated gear change.
     */
    @SubscribeEvent
    public static void onCurioChange(CurioChangeEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (entity instanceof Player player) {
            ActiveGodResolver.invalidate(player);
            GodNodeCache.invalidate(player);
            if (player instanceof ServerPlayer serverPlayer) {
                AttributeSnapshotHelper.getInstance().refreshSnapshotDelayed(serverPlayer);
            }
        }
    }

    /**
     * The login reconcile is what repairs a save left dirty by a crash: any vanilla attribute
     * modifier a god node wrote before the process died is removed here, before the player can
     * observe the inflated stat.
     */
    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player && player.getServer() != null) {
            ActiveGodResolver.invalidate(player);
            GodNodeCache.invalidate(player);
            GodAlignmentData.get(player.getServer()).sync(player);
            GodVanillaAttributes.reconcile(player);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
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
            GodVanillaAttributes.reconcile(player);
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
}
