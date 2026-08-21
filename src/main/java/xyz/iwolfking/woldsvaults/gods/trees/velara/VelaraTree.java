package xyz.iwolfking.woldsvaults.gods.trees.velara;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
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
 * Setup entry point for the Velara god tree. Everything the tree owns registers itself from here,
 * so the mod entrypoint stays untouched and the tree can be lifted out as a unit.
 *
 * <p>The handler types themselves are not registered here: they must exist before the god tree
 * configs are validated, which happens earlier than this, so {@code VelaraNodeHandlers.register()}
 * is called from the shared bootstrap in {@code GodNodeHandlerTypes} instead.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class VelaraTree {
    private static final Object LISTENER_REF = new Object();

    private VelaraTree() {
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            GodTreeAttributeProviders.register(VaultGod.VELARA, new GodTreeStatProvider(VaultGod.VELARA));
            PietyBonusSource.register(new VelaraPiety());
            VelaraStatBus.register();
            VelaraCounterstrike.register();
            VelaraDamage.register();
            registerVaultLifecycle();
            WoldsVaults.LOGGER.info("Registered {} Velara god tree nodes",
                    GodNodeRegistry.tree(VaultGod.VELARA).map(tree -> tree.getNodes().size()).orElse(0));
        });
    }

    /**
     * Only the two hooks the shared teardown cannot express. Node scratch is dropped for a leaving
     * listener and a logging-out player by the god core; what is left is rebuilding the Sacrifice
     * partition when a runner joins, and dropping the partition of a vault that has ended - by
     * vault id, so another party's flock is untouched.
     */
    private static void registerVaultLifecycle() {
        CommonEvents.LISTENER_JOIN.register(LISTENER_REF, data ->
                data.getListener().getPlayer().ifPresent(VelaraSacrificeFlocks::rebuildFor));
        CommonEvents.VAULT_END.register(LISTENER_REF, data -> {
            if (data.getVault().has(Vault.ID)) {
                VelaraSacrificeFlocks.clearVault(data.getVault().get(Vault.ID));
            }
        });
    }
}
