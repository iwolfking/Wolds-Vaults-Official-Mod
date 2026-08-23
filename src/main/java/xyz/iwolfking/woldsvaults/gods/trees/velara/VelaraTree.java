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

/**
 * Setup entry point for the Velara god tree. Everything the tree owns registers itself from here,
 * so the mod entrypoint stays untouched and the tree can be lifted out as a unit.
 *
 * <p>The handler types themselves are not registered here: they must exist before the god tree
 * configs are validated, which happens earlier than this, so {@code VelaraNodeHandlers.register()}
 * is called from the shared bootstrap in {@code GodNodeHandlerTypes} instead.
 *
 * <p>Eighteen of Velara's twenty handler types bind {@code ListenerBoundHandler}, far more than
 * any other tree, and that is deliberate rather than unfinished. Velara is the defensive party
 * tree, and its mechanics are the shapes the four capability seams cannot carry: Fleeting
 * Physicality cancels a hit outright, Immortal revives on death, Field Medic attributes a heal to
 * its caster, and Stonewall, Cactus, Immortal, Steadfast and Defender of the Faith compose
 * multiplicatively into one modifier per attribute using a census of the whole party. A
 * {@code CombatContributor} can only scale an amount and a {@code TickContributor} only ever sees
 * one player and one effect, so porting these would mean widening both interfaces for the three
 * trees that do fit them cleanly. The two effects that are ported - Immune and Utilized - are
 * exactly the two plain stat nodes.
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

    /**
     * Only the hooks the shared teardown cannot express. Node scratch is dropped for a leaving
     * listener and a logging-out player by the god core; what is left is rebuilding the Sacrifice
     * partition when a runner joins, dropping the partition of a vault that has ended - by vault
     * id, so another party's flock is untouched - and re-arming Immortal's revive on the way out.
     *
     * <p>Immortal's cooldown is the one piece of Velara state that outlives a relog, because a
     * transient one was a disconnect away from being free. Vault exit is where it is given back:
     * the cooldown is meant to be a limit inside a run, not something carried into the next one.
     */
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
