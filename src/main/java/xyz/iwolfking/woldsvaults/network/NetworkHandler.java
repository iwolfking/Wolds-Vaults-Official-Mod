package xyz.iwolfking.woldsvaults.network;

import com.blakebr0.cucumber.network.BaseNetworkHandler;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.network.message.EjectModeSwitchMessage;
import xyz.iwolfking.woldsvaults.network.message.InputLimitSwitchMessage;

public class NetworkHandler {
    public static final BaseNetworkHandler INSTANCE = new BaseNetworkHandler(new ResourceLocation(WoldsVaults.MOD_ID, "main"));

    public static void onCommonSetup() {
        INSTANCE.register(EjectModeSwitchMessage.class, new EjectModeSwitchMessage());
        INSTANCE.register(InputLimitSwitchMessage.class, new InputLimitSwitchMessage());
    }
}