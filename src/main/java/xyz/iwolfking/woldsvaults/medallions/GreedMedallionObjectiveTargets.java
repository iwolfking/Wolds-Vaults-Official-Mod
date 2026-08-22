package xyz.iwolfking.woldsvaults.medallions;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Feeds a greed medallion's "+X% Objective Difficulty" into the base game's own objective-target
 * machinery, so it reaches every objective the vanilla {@code objective_target} vault modifier
 * reaches rather than only the brutal-boss sigil path.
 *
 * <p>The base modifier {@code ObjectiveTargetModifier} does nothing more than register a listener on
 * {@code CommonEvents.OBJECTIVE_TARGET} that adds its own increase to the running total; the four
 * objective goals that support scaling re-derive {@code TARGET = round(BASE_TARGET * (1 + increase))}
 * from an untouched base every server tick. This class registers exactly that listener for the
 * medallion, which is why it does not have to care whether it runs before or after the objective is
 * generated. It is deliberately not a registered {@code VaultModifier} instance: medallion
 * percentages are 25/35/40/45/50 while the shipped modifiers come in fixed 20% steps, and code
 * registrations into {@code VaultModifierRegistry} are wiped whenever the modifier config reloads.</p>
 *
 * <p>Coverage, verified against the_vault 3.21.6 — the only consumers of the event are
 * {@code ScavengerGoal}, {@code ScavengerBingoTile}, {@code ElixirGoal} and {@code MonolithObjective},
 * so the scaled objectives are scavenger, unhinged scavenger, scavenger bingo, unhinged scavenger
 * bingo, elixir, enchanted elixir, monolith and haunted braziers. Bosses, bingo lines, chaos, rune
 * bosses, hyper, alchemy, zealot, corrupted, cakes and survival expose no base-target field to the
 * event and are untouched here; brutal bosses keep scaling through
 * {@code SigilUtils#getDifficultyMultiplier(String, Vault)}, which multiplies the medallion factor
 * on top of the sigil factor.</p>
 *
 * <p>Like every other medallion effect this is in-memory only: a vault that outlives a server
 * restart loses the listener and reverts to unscaled targets.</p>
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class GreedMedallionObjectiveTargets {
    private static final Object OWNER = new Object();
    private static final Set<UUID> REGISTERED = ConcurrentHashMap.newKeySet();

    private GreedMedallionObjectiveTargets() {
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            CommonEvents.VAULT_END.register(OWNER, data -> {
                Vault vault = data.getVault();
                if (vault != null && vault.has(Vault.ID)) {
                    release(vault.get(Vault.ID));
                }
            });
            MinecraftForge.EVENT_BUS.addListener(GreedMedallionObjectiveTargets::onServerStopping);
        });
    }

    /**
     * Registers the medallion's objective-target increase for one vault. Safe to call more than
     * once for the same vault; the second call is dropped rather than stacking a second listener.
     * The listener matches on vault id rather than on instance identity so that a vault reloaded
     * from disk inside the same server session keeps its scaling.
     */
    public static void register(Vault vault, GreedMedallionTier tier) {
        if (vault == null || tier == null || !vault.has(Vault.ID)) {
            return;
        }
        int percent = tier.getObjectiveDifficultyBonus();
        if (percent <= 0) {
            return;
        }
        UUID vaultId = vault.get(Vault.ID);
        if (!REGISTERED.add(vaultId)) {
            WoldsVaults.LOGGER.warn("Greed medallion objective difficulty was already registered for vault {}; ignoring the repeat registration.", vaultId);
            return;
        }
        double increase = percent / 100.0D;
        CommonEvents.OBJECTIVE_TARGET.register(vaultId, data -> {
            Vault target = data.getVault();
            if (target == null || !target.has(Vault.ID) || !vaultId.equals(target.get(Vault.ID))) {
                return;
            }
            data.setIncrease(data.getIncrease() + increase);
        });
    }

    public static void release(UUID vaultId) {
        if (vaultId != null && REGISTERED.remove(vaultId)) {
            CommonEvents.OBJECTIVE_TARGET.release(vaultId);
        }
    }

    /**
     * Drops every vault's registration when the server stops. A vault that never ended - a crash
     * mid-run - otherwise leaves both its listener and its id behind for the life of the process.
     *
     * <p>Added to the Forge bus by hand rather than with {@code @SubscribeEvent}, because this
     * class subscribes the mod bus and Forge rejects a mod-bus subscriber that declares a handler
     * for a game event.
     */
    private static void onServerStopping(ServerStoppingEvent event) {
        REGISTERED.forEach(CommonEvents.OBJECTIVE_TARGET::release);
        REGISTERED.clear();
    }
}
