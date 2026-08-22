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

/**
 * Setup entry point for the Idona god tree. Everything the tree owns registers itself from here,
 * so the mod entrypoint stays untouched and the tree can be lifted out as a unit.
 *
 * <p>The handler types themselves are not registered here: they must exist before the god tree
 * configs are validated, which happens earlier than this, so {@code IdonaNodeHandlers.register()}
 * is called from the shared bootstrap in {@code GodNodeHandlerTypes} instead.
 *
 * <p>The remaining Forge listeners - the swing suppression, the prison marks and the transient
 * state teardown - subscribe through their own {@code @Mod.EventBusSubscriber} annotations, so
 * nothing outside this package has to know the tree exists.
 */
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
