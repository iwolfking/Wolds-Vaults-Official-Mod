package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import iskallia.vault.core.vault.influence.VaultGod;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.GodPietySource;
import xyz.iwolfking.woldsvaults.gods.GodTreeAttributeProviders;
import xyz.iwolfking.woldsvaults.gods.PietyBonusSource;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeRegistry;
import xyz.iwolfking.woldsvaults.gods.node.GodTreeStatProvider;

/** Setup entry point for the Tenos god tree. Handler types come up earlier, elsewhere. */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class TenosTree {
    private TenosTree() {
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            GodTreeAttributeProviders.register(VaultGod.TENOS, new GodTreeStatProvider(VaultGod.TENOS));
            PietyBonusSource.register(new GodPietySource(TenosNodes.GOD, TenosNodes.PIOUS_DEVOTION));
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
