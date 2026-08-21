package xyz.iwolfking.woldsvaults.gods.network;

import com.blakebr0.cucumber.network.BaseNetworkHandler;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;

/**
 * The god system's own network channel, kept separate from the addon's main channel so the god
 * packages self-register without touching the mod entrypoint.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class GodNetwork {
    public static final BaseNetworkHandler INSTANCE = new BaseNetworkHandler(WoldsVaults.id("gods"));

    private GodNetwork() {
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            INSTANCE.register(GodAlignmentSyncMessage.class, new GodAlignmentSyncMessage());
            INSTANCE.register(ServerboundOpenGodTreeMessage.class, new ServerboundOpenGodTreeMessage());
            INSTANCE.register(ServerboundUnlockGodNodeMessage.class, new ServerboundUnlockGodNodeMessage());
            INSTANCE.register(ClientboundVaultGodXpMessage.class, new ClientboundVaultGodXpMessage());
            INSTANCE.register(ServerboundToggleCharmTemporalMessage.class, new ServerboundToggleCharmTemporalMessage());
            INSTANCE.register(ClientboundSacrificeMenuMessage.class, new ClientboundSacrificeMenuMessage());
            INSTANCE.register(ServerboundSelectSacrificeGodMessage.class, new ServerboundSelectSacrificeGodMessage());
            INSTANCE.register(ServerboundRequestSacrificeMenuMessage.class, new ServerboundRequestSacrificeMenuMessage());
        });
    }
}
