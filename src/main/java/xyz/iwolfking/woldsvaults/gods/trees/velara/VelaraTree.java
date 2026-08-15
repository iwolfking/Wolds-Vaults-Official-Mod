package xyz.iwolfking.woldsvaults.gods.trees.velara;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.core.vault.player.Listener;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.GodTreeAttributeProviders;
import xyz.iwolfking.woldsvaults.gods.PietyBonusSource;

import java.util.UUID;

/**
 * Setup entry point for the Velara god tree. Everything the tree owns registers itself from here,
 * so the mod entrypoint stays untouched and the tree can be lifted out as a unit.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class VelaraTree {
    private static final Object LISTENER_REF = new Object();

    private VelaraTree() {
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            GodTreeAttributeProviders.register(VaultGod.VELARA, new VelaraAttributeProvider());
            PietyBonusSource.register(new VelaraPiety());
            VelaraStatBus.register();
            Counterstrike.register();
            VelaraDamage.register();
            registerVaultLifecycle();
            WoldsVaults.LOGGER.info("Registered {} Velara god tree nodes", VelaraNode.values().length);
        });
    }

    private static void registerVaultLifecycle() {
        CommonEvents.LISTENER_JOIN.register(LISTENER_REF, data ->
                data.getListener().getPlayer().ifPresent(SacrificeFlocks::rebuildFor));
        CommonEvents.LISTENER_LEAVE.register(LISTENER_REF, data -> {
            UUID listenerId = data.getListener().get(Listener.ID);
            if (listenerId != null) {
                SacrificeFlocks.clearPlayer(listenerId);
                AdaptiveArmor.clear(listenerId);
                FleetingPhysicality.clear(listenerId);
                Immortal.clear(listenerId);
                VelaraAuras.clear(listenerId);
            }
        });
        CommonEvents.VAULT_END.register(LISTENER_REF, data -> {
            if (data.getVault().has(Vault.ID)) {
                SacrificeFlocks.clearVault(data.getVault().get(Vault.ID));
            }
        });
    }
}
