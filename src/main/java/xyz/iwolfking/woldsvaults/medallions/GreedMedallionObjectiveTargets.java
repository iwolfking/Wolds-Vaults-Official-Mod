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
 * Feeds a greed medallion's "+X% Objective Difficulty" into {@code CommonEvents.OBJECTIVE_TARGET}, whose
 * only consumers are the scavenger, scavenger bingo, elixir and monolith goals and their addon variants;
 * every other objective exposes no base target and is untouched, brutal bosses scaling through
 * {@code SigilUtils} instead. The registration is in-memory only.
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
     * Registers the medallion's objective-target increase for one vault; a repeat call for the same vault id is
     * dropped.
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

    private static void onServerStopping(ServerStoppingEvent event) {
        REGISTERED.forEach(CommonEvents.OBJECTIVE_TARGET::release);
        REGISTERED.clear();
    }
}
