package xyz.iwolfking.woldsvaults.events.client;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import xyz.iwolfking.woldsvaults.init.ModVaultarHudScreenRegistry;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.client.init.ModScreens;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.init.client.ModEntityRenderers;
import xyz.iwolfking.woldsvaults.init.client.ModKeybinds;
import xyz.iwolfking.woldsvaults.items.alchemy.DecoPotionItem;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = {Dist.CLIENT})
public class ClientSetupEvents {
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void setupClient(FMLClientSetupEvent event) {
        ModScreens.register();
        ModEntityRenderers.register(event);
        ModKeybinds.registerKeyBinds();
        ModVaultarHudScreenRegistry.init();
        registerItemProperties(event);
    }


    @OnlyIn(Dist.CLIENT)
    private static void registerItemProperties(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(ModItems.DECO_POTION, WoldsVaults.id("potion_variant"),
                    (pStack, pLevel, pEntity, pSeed) ->  {
                        if (!(pStack.getItem() instanceof DecoPotionItem item)) return 0.0f;
                        return switch (item.getType()) {
                            case ONE_INGREDIENT -> 0.1F;
                            case TWO_INGREDIENT -> 0.2F;
                            case THREE_INGREDIENT -> 0.3F;
                            case BREWING -> 0.4F;
                            case UNKNOWN -> 0.0F;
                        };
                    }
            );
        });
    }
}