package xyz.iwolfking.woldsvaults.gods.trees.idona;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.GodTreeAttributeProviders;

/**
 * Entry point for the Idona god tree. Everything the tree owns registers itself: the attribute
 * provider that feeds the snapshot, the player-stat and soul-shard listeners, the piety source and
 * the vault-lifecycle cleanup for the tree's transient state.
 *
 * <p>Damage handlers and the transient state store subscribe to the Forge bus through their own
 * {@code @Mod.EventBusSubscriber} annotations, so nothing outside this package has to know the
 * tree exists.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class IdonaTree {
    private IdonaTree() {
    }

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            GodTreeAttributeProviders.register(IdonaNodes.GOD, new IdonaAttributeProvider());
            IdonaStatHooks.register();
            IdonaState.registerVaultHooks();
            WoldsVaults.LOGGER.info("Registered {} Idona god tree nodes", IdonaNodes.ALL_NODES.size());
        });
    }
}
