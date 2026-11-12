package xyz.iwolfking.woldsvaults.abilities.block;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import xyz.iwolfking.woldsvaults.WoldsVaults;

@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class BlockAbilityEvents {
    private BlockAbilityEvents() {
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
        LifeBlockState.tick(server);
        ManaBlockState.tick(server);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onHeal(LivingHealEvent event) {
        LifeBlockState.onHeal(event);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        clear(event.getPlayer());
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntityLiving() instanceof ServerPlayer player) {
            clear(player);
        }
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        clear(event.getPlayer());
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        LifeBlockState.clearAll();
        ManaBlockState.clearAll();
    }

    private static void clear(Player player) {
        LifeBlockState.clear(player);
        ManaBlockState.clear(player);
    }

    @Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Setup {
        private Setup() {
        }

        @SubscribeEvent
        public static void onCommonSetup(FMLCommonSetupEvent event) {
            event.enqueueWork(() -> {
                LifeBlockState.register();
                ManaBlockState.register();
            });
        }
    }
}
