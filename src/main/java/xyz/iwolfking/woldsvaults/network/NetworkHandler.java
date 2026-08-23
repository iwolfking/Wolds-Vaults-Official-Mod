package xyz.iwolfking.woldsvaults.network;

import com.blakebr0.cucumber.network.BaseNetworkHandler;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.network.ClientboundGodNodePreviewMessage;
import xyz.iwolfking.woldsvaults.gods.network.ClientboundSacrificeMenuMessage;
import xyz.iwolfking.woldsvaults.gods.network.ClientboundVaultGodXpMessage;
import xyz.iwolfking.woldsvaults.gods.network.GodAlignmentSyncMessage;
import xyz.iwolfking.woldsvaults.gods.network.ServerboundOpenGodTreeMessage;
import xyz.iwolfking.woldsvaults.gods.network.ServerboundRequestGodNodePreviewMessage;
import xyz.iwolfking.woldsvaults.gods.network.ServerboundRequestSacrificeMenuMessage;
import xyz.iwolfking.woldsvaults.gods.network.ServerboundSelectSacrificeGodMessage;
import xyz.iwolfking.woldsvaults.gods.network.ServerboundSetMinorTransferMessage;
import xyz.iwolfking.woldsvaults.gods.network.ServerboundToggleCharmTemporalMessage;
import xyz.iwolfking.woldsvaults.gods.network.ServerboundUnlockGodNodeMessage;
import xyz.iwolfking.woldsvaults.milestones.network.GreedTablesMessage;
import xyz.iwolfking.woldsvaults.milestones.network.MilestoneStatusMessage;
import xyz.iwolfking.woldsvaults.milestones.network.MilestoneSyncMessage;
import xyz.iwolfking.woldsvaults.milestones.network.ServerboundClaimMilestoneMessage;
import xyz.iwolfking.woldsvaults.milestones.network.ServerboundOpenMilestonesMessage;
import xyz.iwolfking.woldsvaults.milestones.network.ServerboundPinMilestoneMessage;
import xyz.iwolfking.woldsvaults.milestones.network.ServerboundTakeTrialMessage;
import xyz.iwolfking.woldsvaults.network.message.EjectModeSwitchMessage;
import xyz.iwolfking.woldsvaults.network.message.InputLimitSwitchMessage;

public class NetworkHandler {
    public static final BaseNetworkHandler INSTANCE = new BaseNetworkHandler(WoldsVaults.id("main"));

    /**
     * Registers every cucumber message the addon sends. Called once from common setup; the
     * registration order defines the packet discriminators, so it must stay identical on both
     * logical sides.
     */
    public static void onCommonSetup() {
        INSTANCE.register(EjectModeSwitchMessage.class, new EjectModeSwitchMessage());
        INSTANCE.register(InputLimitSwitchMessage.class, new InputLimitSwitchMessage());
        INSTANCE.register(GodAlignmentSyncMessage.class, new GodAlignmentSyncMessage());
        INSTANCE.register(ServerboundOpenGodTreeMessage.class, new ServerboundOpenGodTreeMessage());
        INSTANCE.register(ServerboundUnlockGodNodeMessage.class, new ServerboundUnlockGodNodeMessage());
        INSTANCE.register(ClientboundVaultGodXpMessage.class, new ClientboundVaultGodXpMessage());
        INSTANCE.register(ServerboundToggleCharmTemporalMessage.class, new ServerboundToggleCharmTemporalMessage());
        INSTANCE.register(ClientboundSacrificeMenuMessage.class, new ClientboundSacrificeMenuMessage());
        INSTANCE.register(ServerboundSelectSacrificeGodMessage.class, new ServerboundSelectSacrificeGodMessage());
        INSTANCE.register(ServerboundRequestSacrificeMenuMessage.class, new ServerboundRequestSacrificeMenuMessage());
        INSTANCE.register(MilestoneSyncMessage.class, new MilestoneSyncMessage());
        INSTANCE.register(MilestoneStatusMessage.class, new MilestoneStatusMessage());
        INSTANCE.register(ServerboundClaimMilestoneMessage.class, new ServerboundClaimMilestoneMessage());
        INSTANCE.register(ServerboundPinMilestoneMessage.class, new ServerboundPinMilestoneMessage());
        INSTANCE.register(ServerboundOpenMilestonesMessage.class, new ServerboundOpenMilestonesMessage());
        INSTANCE.register(ServerboundTakeTrialMessage.class, new ServerboundTakeTrialMessage());
        INSTANCE.register(ServerboundSetMinorTransferMessage.class, new ServerboundSetMinorTransferMessage());
        INSTANCE.register(ServerboundRequestGodNodePreviewMessage.class, new ServerboundRequestGodNodePreviewMessage());
        INSTANCE.register(ClientboundGodNodePreviewMessage.class, new ClientboundGodNodePreviewMessage());
        INSTANCE.register(GreedTablesMessage.class, new GreedTablesMessage());
    }
}
