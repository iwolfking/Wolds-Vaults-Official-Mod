package xyz.iwolfking.woldsvaults.gods.ultimates.event;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
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
 * Server-side lifecycle for the god ultimates: the one tick loop the three stateful ultimates run
 * on, the periodic re-resolution that keeps Stirrings of Power morphed to the equipped charm, the
 * bullet-time dodge roll, and the teardown paths that guarantee no buff outlives its owner.
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
     * Bullet time's dodge is rolled here rather than folded into the gear dodge stat, so that the
     * hard 0.95 cap on gear dodge cannot swallow the whole of the ultimate's own ladder. Runs above
     * the addon's gear dodge handler; cancelling stops that handler from also rolling.
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
     * A charm swap changes which ultimate is owed, so the cached active god is dropped and the
     * ability re-pointed immediately rather than waiting for the next sweep.
     */
    @SubscribeEvent
    public static void onCurioChange(CurioChangeEvent event) {
        if (event.getEntityLiving() instanceof ServerPlayer player) {
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
     * Leaving the dimension means leaving the vault, so bullet time's clock factor is released and
     * its attribute modifiers are stripped before the player can carry them into the overworld.
     */
    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            BulletTimeState.end(player);
            UltimateSpecializationManager.refresh(player);
        }
    }

    private static void clearAll(net.minecraft.world.entity.player.Player player) {
        CopeDeGraceState.clear(player);
        SaviorState.clear(player);
        BulletTimeState.end(player);
    }
}
