package xyz.iwolfking.woldsvaults.gods.ultimates.event;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import top.theillusivec4.curios.api.event.CurioChangeEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.ActiveGodResolver;
import xyz.iwolfking.woldsvaults.gods.ultimates.BulletTimeState;
import xyz.iwolfking.woldsvaults.gods.ultimates.CopeDeGraceState;
import xyz.iwolfking.woldsvaults.gods.ultimates.SaviorState;
import xyz.iwolfking.woldsvaults.gods.ultimates.UltimateSpecializationManager;

/**
 * Server-side lifecycle for the god ultimates: the shared tick loop, the periodic re-specialization, the
 * bullet-time dodge roll, and the teardown paths.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class UltimateEventHandlers {
    private static final int RESPECIALIZE_INTERVAL_TICKS = 40;

    private UltimateEventHandlers() {
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        CopeDeGraceState.tick(server);
        SaviorState.tick();
        BulletTimeState.tick(server);
        if (server.getTickCount() % RESPECIALIZE_INTERVAL_TICKS == 0) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                UltimateSpecializationManager.refresh(player);
            }
        }
    }

    /**
     * Rolls bullet time's own dodge above the addon's gear dodge handler; cancelling stops that handler from
     * rolling too.
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBulletTimeDodge(LivingHurtEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (!(entity instanceof ServerPlayer player) || event.getSource().isBypassInvul()) {
            return;
        }
        float chance = BulletTimeState.getDodgeChance(player);
        if (chance > 0.0F && player.getRandom().nextFloat() < chance) {
            event.setCanceled(true);
        }
    }

    /**
     * Re-points the ability on a charm swap; Curios raises this on any NBT delta, so deltas that cannot move the
     * active god are ignored.
     */
    @SubscribeEvent
    public static void onCurioChange(CurioChangeEvent event) {
        if (event.getEntityLiving() instanceof ServerPlayer player
                && ActiveGodResolver.mayChangeActiveGod(event.getFrom(), event.getTo())) {
            ActiveGodResolver.invalidate(player);
            UltimateSpecializationManager.refresh(player);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            UltimateSpecializationManager.refresh(player);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        clearAll(event.getPlayer());
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntityLiving() instanceof ServerPlayer player) {
            clearAll(player);
        }
    }

    /**
     * Ends bullet time and any running Cope de Grace infusion, the latter without its dash; the cast's cooldown
     * keeps running.
     */
    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            BulletTimeState.end(player);
            CopeDeGraceState.clear(player);
            UltimateSpecializationManager.refresh(player);
        }
    }

    private static void clearAll(net.minecraft.world.entity.player.Player player) {
        CopeDeGraceState.clear(player);
        SaviorState.clear(player);
        BulletTimeState.end(player);
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        CopeDeGraceState.clearAll();
        SaviorState.clearAll();
        BulletTimeState.clearAll();
    }
}
