package xyz.iwolfking.woldsvaults.events.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import xyz.iwolfking.woldsvaults.init.client.ModEntityRenderers;
import xyz.iwolfking.woldsvaults.init.client.ModKeybinds;
import xyz.iwolfking.woldsvaults.init.client.ModScreens;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = {Dist.CLIENT})
public class ClientSetupEvents {
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void setupClient(FMLClientSetupEvent event) {
        ModScreens.register();
        ModEntityRenderers.register(event);
        ModKeybinds.registerKeyBinds();
    }
}