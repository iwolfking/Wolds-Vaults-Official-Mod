package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.GodTreeAttributeProviders;
import xyz.iwolfking.woldsvaults.gods.PietyBonusSource;

/**
 * Startup for the Tenos god tree. Self-contained on purpose: the tree registers its own attribute
 * provider and its own listeners, so the mod entrypoint and the god core stay untouched and the
 * whole tree can be removed by deleting this package.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class TenosTree {
    private TenosTree() {
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            GodTreeAttributeProviders.register(TenosNodes.GOD, new TenosAttributeProvider());
            PietyBonusSource.register(new TenosPiety());
            ChestRateTracker.register();
            TenosLootStats.register();
            TenosWorldNodes.register();
            TenosVendoors.register();
            TenosCoinPiles.register();
            TenosGoldPlating.register();
            TenosSackOfMobs.register();
            TenosChallengeTackler.register();
            TenosNodes.INERT.forEach((nodeId, reason) ->
                    WoldsVaults.LOGGER.info("Tenos node {} is registered inert: {}", nodeId, reason));
        });
    }
}
