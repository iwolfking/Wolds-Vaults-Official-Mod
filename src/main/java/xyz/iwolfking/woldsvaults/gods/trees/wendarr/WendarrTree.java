package xyz.iwolfking.woldsvaults.gods.trees.wendarr;

import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.GodTreeAttributeProviders;
import xyz.iwolfking.woldsvaults.gods.PietyBonusSource;

/**
 * Startup for the Wendarr god tree. Self-contained on purpose: the tree registers its own
 * attribute provider and its own listeners, so the mod entrypoint and the god core stay untouched
 * and the whole tree can be removed by deleting this package.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class WendarrTree {
    private WendarrTree() {
    }

    @Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Setup {
        private Setup() {
        }

        @SubscribeEvent
        public static void onCommonSetup(FMLCommonSetupEvent event) {
            event.enqueueWork(() -> {
                GodTreeAttributeProviders.register(WendarrNodes.GOD, new WendarrAttributeProvider());
                PietyBonusSource.register(new WendarrPiety());
                WendarrClockNodes.register();
                WendarrCombatNodes.register();
                WendarrGardener.register();
                WendarrPylons.register();
                WendarrNodes.INERT.forEach((nodeId, reason) ->
                        WoldsVaults.LOGGER.info("Wendarr node {} is registered inert: {}", nodeId, reason));
            });
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        WendarrClockNodes.clearPlayer(event.getPlayer().getUUID());
        WendarrCombatNodes.clearPlayer(event.getPlayer().getUUID());
    }
}
