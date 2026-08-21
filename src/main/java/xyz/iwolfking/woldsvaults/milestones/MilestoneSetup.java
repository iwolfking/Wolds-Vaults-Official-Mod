package xyz.iwolfking.woldsvaults.milestones;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;

/**
 * Owns the milestone engine's startup wiring. Message registration lives with the rest of the
 * addon's packets on the shared channel in {@code NetworkHandler}.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MilestoneSetup {
    private MilestoneSetup() {
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            MilestoneDispatcher.register();
        });
    }
}
