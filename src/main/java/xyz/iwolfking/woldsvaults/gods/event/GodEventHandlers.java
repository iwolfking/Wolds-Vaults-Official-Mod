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
import top.theillusivec4.curios.api.event.CurioChangeEvent;

/**
 * Lifecycle glue for the god core: invalidates the active-god and gate caches, tears down node state
 * on logout, syncs alignment to joining players, and announces level-ups.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class GodEventHandlers {
    private GodEventHandlers() {
    }

    /** Runs on both logical sides; the server side also rebuilds the snapshot and re-syncs alignment. */
    @SubscribeEvent
    public static void onCurioChange(CurioChangeEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (!(entity instanceof Player player)) {
            return;
        }
        boolean godMayChange = ActiveGodResolver.mayChangeActiveGod(event.getFrom(), event.getTo());
        if (godMayChange) {
            ActiveGodResolver.invalidate(player);
            GodNodeCache.invalidate(player);
        }
        if (!(player instanceof ServerPlayer serverPlayer) || serverPlayer.getServer() == null) {
            return;
        }
        if (godMayChange) {
            AttributeSnapshotHelper.getInstance().refreshSnapshotDelayed(serverPlayer);
            GodNodeTicker.reconcile(serverPlayer);
            GodAlignmentData.get(serverPlayer.getServer()).sync(serverPlayer);
        } else if (!isBlessingCountdownOnly(event.getFrom(), event.getTo())) {
            AttributeSnapshotHelper.getInstance().refreshSnapshotDelayed(serverPlayer);
        }
    }

    /** Whether the only difference between the stacks is a charm's blessing countdown tag. */
    private static boolean isBlessingCountdownOnly(net.minecraft.world.item.ItemStack from,
                                                   net.minecraft.world.item.ItemStack to) {
        if (from.getItem() != to.getItem() || from.getCount() != to.getCount()) {
            return false;
        }
        net.minecraft.world.item.ItemStack before = from.copy();
        net.minecraft.world.item.ItemStack after = to.copy();
        if (before.getTag() != null) {
            before.getTag().remove(xyz.iwolfking.woldsvaults.gods.charms.MythicCharmRolls.TEMPORAL_REMAINING_TAG);
        }
        if (after.getTag() != null) {
            after.getTag().remove(xyz.iwolfking.woldsvaults.gods.charms.MythicCharmRolls.TEMPORAL_REMAINING_TAG);
        }
        return net.minecraft.world.item.ItemStack.matches(before, after);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player && player.getServer() != null) {
            ActiveGodResolver.invalidate(player);
            GodNodeCache.invalidate(player);
            GodAlignmentData.get(player.getServer()).sync(player);
        }
    }

    /** Deactivates the tick contributors before dropping the caches, while the player is still live. */
    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            GodNodeTicker.deactivateAll(player);
        }
        ActiveGodResolver.invalidate(event.getPlayer());
        GodNodeCache.invalidate(event.getPlayer().getUUID());
        GodNodeState.clear(event.getPlayer().getUUID());
    }

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

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        GodNodeState.clearAll();
        GodNodeCache.invalidateAll();
        ActiveGodResolver.invalidateAll();
    }
}
