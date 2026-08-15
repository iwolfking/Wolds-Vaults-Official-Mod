package xyz.iwolfking.woldsvaults.milestones.network;

import com.blakebr0.cucumber.network.BaseNetworkHandler;
import xyz.iwolfking.woldsvaults.WoldsVaults;

/**
 * Dedicated channel for milestone traffic, kept separate from the addon's other channels so the
 * delta stream cannot interleave with unrelated packets.
 */
public class MilestoneNetwork {
    public static final BaseNetworkHandler INSTANCE = new BaseNetworkHandler(WoldsVaults.id("milestones"));

    private MilestoneNetwork() {
    }

    public static void onCommonSetup() {
        INSTANCE.register(MilestoneSyncMessage.class, new MilestoneSyncMessage());
    }
}
