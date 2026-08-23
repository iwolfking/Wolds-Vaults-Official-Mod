package xyz.iwolfking.woldsvaults.gods.trees.wendarr;

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

/** Setup entry point for the Wendarr god tree. Handler types come up earlier, elsewhere. */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class WendarrTree {
    private WendarrTree() {
    }

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            GodTreeAttributeProviders.register(VaultGod.WENDARR, new GodTreeStatProvider(VaultGod.WENDARR));
            PietyBonusSource.register(new GodPietySource(WendarrNodes.GOD, WendarrNodes.PIOUS_DEVOTION));
            WendarrClockNodes.register();
            WendarrCombatNodes.register();
            WendarrGardener.register();
            WendarrPylons.register();
            WoldsVaults.LOGGER.info("Registered {} Wendarr god tree nodes",
                    GodNodeRegistry.tree(VaultGod.WENDARR).map(tree -> tree.getNodes().size()).orElse(0));
        });
    }
}
