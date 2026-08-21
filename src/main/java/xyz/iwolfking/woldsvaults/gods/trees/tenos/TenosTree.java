package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.GodTreeAttributeProviders;
import xyz.iwolfking.woldsvaults.gods.PietyBonusSource;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeRegistry;
import xyz.iwolfking.woldsvaults.gods.node.GodTreeStatProvider;

/**
 * Setup entry point for the Tenos god tree. Everything the tree owns registers itself from here, so
 * the mod entrypoint stays untouched and the tree can be lifted out as a unit.
 *
 * <p>The handler types themselves are not registered here: they must exist before the god tree
 * configs are validated, which happens earlier than this, so {@code TenosNodeHandlers.register()} is
 * called from the shared bootstrap in {@code GodNodeHandlerTypes} instead.
 *
 * <p>The tree's stat nodes need no provider of their own - {@link GodTreeStatProvider} walks the
 * registry for every effect of this god whose handler is a stat contributor - and it needs no
 * logout listener either: the tick contributors are deactivated and the shared scratch is cleared
 * by the god core's own player lifecycle.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class TenosTree {
    private TenosTree() {
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            GodTreeAttributeProviders.register(VaultGod.TENOS, new GodTreeStatProvider(VaultGod.TENOS));
            PietyBonusSource.register(new TenosPiety());
            ChestRateTracker.register();
            TenosLootStats.register();
            TenosWorldNodes.register();
            TenosVendoors.register();
            TenosCoinPiles.register();
            TenosGoldPlating.register();
            WoldsVaults.LOGGER.info("Registered {} Tenos god tree nodes",
                    GodNodeRegistry.tree(VaultGod.TENOS).map(tree -> tree.getNodes().size()).orElse(0));
        });
    }
}
