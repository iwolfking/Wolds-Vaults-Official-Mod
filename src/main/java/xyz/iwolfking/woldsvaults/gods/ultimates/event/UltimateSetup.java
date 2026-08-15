package xyz.iwolfking.woldsvaults.gods.ultimates.event;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.ultimates.CopeDeGraceState;
import xyz.iwolfking.woldsvaults.gods.ultimates.SaviorState;
import xyz.iwolfking.woldsvaults.gods.ultimates.UltimateReviveData;

import java.util.UUID;

/**
 * One-time wiring for the ultimates: the final-damage sub-stages the two damage-reduction
 * ultimates own, and the vault-end hook that retires spent Savior revive records.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class UltimateSetup {
    private static final Object LISTENER_REF = new Object();

    private UltimateSetup() {
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            CopeDeGraceState.register();
            SaviorState.register();
            CommonEvents.VAULT_END.register(LISTENER_REF, UltimateSetup::onVaultEnd);
        });
    }

    private static void onVaultEnd(iskallia.vault.core.event.common.VaultEndEvent.Data data) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        UUID vaultId = data.getVault() == null ? null : data.getVault().get(Vault.ID);
        if (server == null || vaultId == null) {
            return;
        }
        UltimateReviveData.get(server).forgetVault(vaultId);
    }
}
