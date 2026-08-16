package xyz.iwolfking.woldsvaults.medallions;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.util.calc.PlayerStat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;

/**
 * Startup and lifecycle for greed medallions. Self-contained in the same way the god trees are:
 * the medallion system registers its own listeners so the mod entrypoint stays untouched.
 *
 * <p>The vault-side registration itself happens in the crystal mixin, at the moment a vault is
 * built from a medallion-bearing crystal; this class only releases the entry when the vault ends
 * and hosts the effect hooks that are plain event listeners rather than mixins.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class GreedMedallionEvents {
    private static final Object OWNER = new Object();

    private GreedMedallionEvents() {
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(GreedMedallionEvents::register);
    }

    private static void register() {
        CommonEvents.VAULT_END.register(OWNER, data -> {
            Vault vault = data.getVault();
            if (vault != null && vault.has(Vault.ID)) {
                GreedMedallionVaultState.release(vault.get(Vault.ID));
            }
        });
        CommonEvents.PLAYER_STAT.of(PlayerStat.TRAP_DISARM_CHANCE).register(OWNER, data -> {
            if (!(data.getEntity() instanceof ServerPlayer player)) {
                return;
            }
            float penalty = GreedMedallionEffects.trapDisarmPenalty(player);
            if (penalty > 0.0F) {
                data.setValue(data.getValue() - penalty);
            }
        });
    }
}
