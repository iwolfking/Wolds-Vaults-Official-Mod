package xyz.iwolfking.woldsvaults.gods.trees.idona;

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

/** Setup entry point for the Idona god tree. Handler types come up earlier, elsewhere. */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class IdonaTree {
    private IdonaTree() {
    }

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            GodTreeAttributeProviders.register(VaultGod.IDONA, new GodTreeStatProvider(VaultGod.IDONA));
            PietyBonusSource.register(new GodPietySource(IdonaNodes.GOD, IdonaNodes.PIOUS_DEVOTION));
            IdonaStatHooks.register();
            IdonaState.registerVaultHooks();
            WoldsVaults.LOGGER.info("Registered {} Idona god tree nodes",
                    GodNodeRegistry.tree(VaultGod.IDONA).map(tree -> tree.getNodes().size()).orElse(0));
        });
    }
}
