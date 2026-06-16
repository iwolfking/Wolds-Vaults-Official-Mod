package xyz.iwolfking.woldsvaults.client.servertransfer;

import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;

import java.util.regex.Pattern;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ServerTransferState {
    private static final Pattern TRANSFER_PATTERN = Pattern.compile("^Connecting to \\w+\\.\\.\\.$");
    public static boolean disableJEIRefresh = false;
    public static boolean disableVHAPITemplateSync = false;
    public static boolean disableCraftTweaker = false;
    public static boolean disableThermalRecipeRefresh = false;


    @SubscribeEvent
    public static void onClientChatReceived(ClientChatReceivedEvent event) {
        if (WoldsVaultsConfig.CLIENT.serverTransferReloadSkip.get()) {
            // Saving... => cross proxy (EU <=> US)
            // Connecting to <servername>...  => in proxy (EU <=> EU or US <=> US)
            if (event.getMessage() instanceof TextComponent tc && ("Saving your data before transfer...".equals(tc.getText()) || TRANSFER_PATTERN.matcher(tc.getText()).matches())) {
                WoldsVaults.LOGGER.info("DETECTED SERVER TRANSFER");
                disableJEIRefresh = true;
                disableVHAPITemplateSync = true;
                disableCraftTweaker = true;
                disableThermalRecipeRefresh = true;
            }
        }
    }
}
