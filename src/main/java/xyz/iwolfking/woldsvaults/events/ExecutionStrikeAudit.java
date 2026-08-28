package xyz.iwolfking.woldsvaults.events;

import iskallia.vault.core.vault.Vault;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;

import iskallia.vault.core.event.CommonEvents;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Attribution for Execution Strike inside hyper vaults: how much of a runner's damage came from the
 * missing-health bonus rather than from their sheet.
 *
 * <p>The bonus is {@code (target maxHealth - target health) * executionStat}, so it grows with the
 * target's health pool and with every point of damage already dealt, which makes it worth far more
 * against a hyperboss than against anything else. Bosses and elites also take the whole hit through a
 * 0.25 multiplier, so carrying any execution at all quarters the runner's ordinary damage; the per-hit
 * line records both halves so the trade is visible.
 *
 * <p>Everything here is inert unless {@code logHyperbossDamage} or {@code enableDebugMode} is on.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ExecutionStrikeAudit {
    private static final Map<UUID, Map<UUID, Totals>> BY_VAULT = new ConcurrentHashMap<>();
    private static final Object OWNER = new Object();

    private ExecutionStrikeAudit() {
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> CommonEvents.VAULT_END.register(OWNER, data -> flush(data.getVault(), "vault end")));
    }

    private static final class Totals {
        private long hits;
        private double base;
        private double bonus;
        private double dealt;
    }

    public static boolean isEnabled() {
        return WoldsVaultsConfig.COMMON.logHyperbossDamage.get()
                || WoldsVaultsConfig.COMMON.enableDebugMode.get();
    }

    /**
     * Records one execution-modified hit and logs its breakdown.
     *
     * @param base     the hit before the execution bonus, as the earlier handlers left it
     * @param bonus    the raw missing-health bonus, before the target's tier multiplier
     * @param tier     the multiplier the whole hit is taken through: 1 normally, 0.25 on a boss or
     *                 elite, 0.01 on the Vessel
     * @param dealt    the hit this handler produced
     */
    public static void record(Vault vault, ServerPlayer player, LivingEntity target,
                              float stat, float base, float bonus, float tier, float dealt) {
        if (!isEnabled() || vault == null || !vault.has(Vault.ID)) {
            return;
        }
        Totals totals = BY_VAULT.computeIfAbsent(vault.get(Vault.ID), id -> new ConcurrentHashMap<>())
                .computeIfAbsent(player.getUUID(), id -> new Totals());
        totals.hits++;
        totals.base += base;
        totals.bonus += bonus * tier;
        totals.dealt += dealt;
        WoldsVaults.LOGGER.info(
                "Execution Strike: {} hit {} for {} = ({} base + {} bonus) x{} | stat {} on {} missing health | bonus is {}% of the hit",
                player.getGameProfile().getName(), target.getType().getRegistryName(),
                String.format("%.1f", dealt), String.format("%.1f", base), String.format("%.1f", bonus),
                String.format("%.2f", tier), String.format("%.3f", stat),
                String.format("%.0f", target.getMaxHealth() - target.getHealth()),
                dealt <= 0.0F ? "?" : String.format("%.1f", 100.0F * bonus * tier / dealt));
    }

    /**
     * Logs and clears what every runner accumulated in this vault. Called when a hyperboss dies and
     * again when the vault ends, so a multi-cycle run reports per cycle as well as in total.
     */
    public static void flush(Vault vault, String reason) {
        if (vault == null || !vault.has(Vault.ID)) {
            return;
        }
        Map<UUID, Totals> perPlayer = BY_VAULT.remove(vault.get(Vault.ID));
        if (perPlayer == null || perPlayer.isEmpty()) {
            return;
        }
        perPlayer.forEach((playerId, totals) -> WoldsVaults.LOGGER.info(
                "Execution Strike summary ({}): {} dealt {} over {} hits, of which {} was execution bonus ({}%).",
                reason, playerId, String.format("%.0f", totals.dealt), totals.hits,
                String.format("%.0f", totals.bonus),
                totals.dealt <= 0.0D ? "?" : String.format("%.1f", 100.0D * totals.bonus / totals.dealt)));
    }

    /** Drops a vault's totals without logging them, for a vault that ended without a boss kill. */
    public static void release(UUID vaultId) {
        if (vaultId != null) {
            BY_VAULT.remove(vaultId);
        }
    }
}
