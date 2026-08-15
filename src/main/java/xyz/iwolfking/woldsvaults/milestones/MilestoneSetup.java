package xyz.iwolfking.woldsvaults.milestones;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.milestones.network.MilestoneNetwork;

/**
 * Owns the milestone engine's startup. Deliberately self-contained so the engine can be added or
 * removed without touching the main mod class.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MilestoneSetup {
    private MilestoneSetup() {
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            MilestoneNetwork.onCommonSetup();
            MilestoneDispatcher.register();
        });
    }
}
