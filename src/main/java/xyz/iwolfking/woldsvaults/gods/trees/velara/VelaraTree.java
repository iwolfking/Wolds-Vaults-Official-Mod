package xyz.iwolfking.woldsvaults.gods.trees.velara;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.influence.VaultGod;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.GodNodeState;
import xyz.iwolfking.woldsvaults.gods.GodPietySource;
import xyz.iwolfking.woldsvaults.gods.GodTreeAttributeProviders;
import xyz.iwolfking.woldsvaults.gods.PietyBonusSource;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeRegistry;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeTicker;
import xyz.iwolfking.woldsvaults.gods.node.GodTreeStatProvider;

/** Setup entry point for the Velara god tree. Handler types come up earlier, elsewhere. */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class VelaraTree {
    private static final Object LISTENER_REF = new Object();

    private VelaraTree() {
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            GodTreeAttributeProviders.register(VaultGod.VELARA, new GodTreeStatProvider(VaultGod.VELARA));
            PietyBonusSource.register(new GodPietySource(VelaraNodes.GOD, VelaraNodes.PIOUS_DEVOTION));
            VelaraStatBus.register();
            VelaraCounterstrike.register();
            VelaraDamage.register();
            GodNodeTicker.registerTreePass(VelaraTicker::pass);
            registerVaultLifecycle();
            WoldsVaults.LOGGER.info("Registered {} Velara god tree nodes",
                    GodNodeRegistry.tree(VaultGod.VELARA).map(tree -> tree.getNodes().size()).orElse(0));
        });
    }

    /** Rebuilds the Sacrifice partition on join, drops it on vault end, re-arms Immortal on exit. */
    private static void registerVaultLifecycle() {
        CommonEvents.LISTENER_JOIN.register(LISTENER_REF, data ->
                data.getListener().getPlayer().ifPresent(VelaraSacrificeFlocks::rebuildFor));
        CommonEvents.LISTENER_LEAVE.register(LISTENER_REF, data ->
                data.getListener().getPlayer().ifPresent(player ->
                        GodNodeState.clearPersistent(player, VelaraNodes.IMMORTAL)));
        CommonEvents.VAULT_END.register(LISTENER_REF, data -> {
            if (data.getVault().has(Vault.ID)) {
                VelaraSacrificeFlocks.clearVault(data.getVault().get(Vault.ID));
            }
        });
    }
}
