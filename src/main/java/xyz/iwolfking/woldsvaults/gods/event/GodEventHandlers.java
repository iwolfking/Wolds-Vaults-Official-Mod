package xyz.iwolfking.woldsvaults.gods.event;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.ActiveGodResolver;
import xyz.iwolfking.woldsvaults.gods.GodAlignmentData;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

/**
 * Lifecycle glue for the god core: keeps the active-god cache honest across equipment and
 * dimension changes, syncs alignment state to joining players, and announces level-ups.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class GodEventHandlers {
    private GodEventHandlers() {
    }

    @SubscribeEvent
    public static void onCurioChange(CurioChangeEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (entity instanceof ServerPlayer player) {
            ActiveGodResolver.invalidate(player);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player && player.getServer() != null) {
            ActiveGodResolver.invalidate(player);
            GodAlignmentData.get(player.getServer()).sync(player);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        ActiveGodResolver.invalidate(event.getPlayer());
    }

    /**
     * Vault entry and exit change whether a charm counts as usable, so the resolved god is
     * recomputed on every dimension change and on respawn.
     */
    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        ActiveGodResolver.invalidate(event.getPlayer());
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        ActiveGodResolver.invalidate(event.getPlayer());
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        ActiveGodResolver.invalidate(event.getPlayer());
        ActiveGodResolver.invalidate(event.getOriginal());
    }

    @SubscribeEvent
    public static void announceLevelUp(GodLevelUpEvent event) {
        event.getPlayer().sendMessage(new TranslatableComponent("message.woldsvaults.god_level_up",
                event.getGod().getHoverChatComponent(),
                event.getNewLevel()).withStyle(ChatFormatting.GOLD), net.minecraft.Util.NIL_UUID);
    }
}
