package xyz.iwolfking.woldsvaults.gods.combat;

import iskallia.vault.util.damage.PlayerDamageHelper;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods.DamageMultiplierInvoker;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods.PlayerDamageHelperApplyInvoker;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Producer for the ability-power side of the base mod's damage multiplier registry.
 *
 * <p>{@code PlayerDamageHelper} splits its registry into an attack-damage bucket and an
 * ability-power bucket, but every public factory hardcodes {@code isAPMult = false}, so the AP
 * bucket always folds to 1.0 and {@code getDamageMultiplier(player, true, true)} is dead. These
 * methods register on the AP side, making that bucket live for ability and poison-nova damage.
 * They mirror the AD-side factories exactly, so AP entries tick down, time out and sync like any
 * other multiplier.
 */
public final class AbilityPowerDamageMultipliers {
    private AbilityPowerDamageMultipliers() {
    }

    public static PlayerDamageHelper.DamageMultiplier apply(UUID id, ServerPlayer player, float value,
                                                            PlayerDamageHelper.Operation operation) {
        return apply(id, player, value, operation, true, Integer.MAX_VALUE, serverPlayer -> {
        });
    }

    public static PlayerDamageHelper.DamageMultiplier applyTimed(UUID id, ServerPlayer player, float value,
                                                                 PlayerDamageHelper.Operation operation,
                                                                 boolean showOnClient, int tickDuration) {
        return apply(id, player, value, operation, showOnClient, tickDuration, serverPlayer -> {
        });
    }

    public static PlayerDamageHelper.DamageMultiplier apply(UUID id, ServerPlayer player, float value,
                                                            PlayerDamageHelper.Operation operation,
                                                            boolean showOnClient, int tickDuration,
                                                            Consumer<ServerPlayer> onTimeout) {
        if (player.getServer() == null) {
            WoldsVaults.LOGGER.error("Cannot register AP damage multiplier {} for {}: no server on the player.",
                    id, player.getGameProfile().getName());
            return null;
        }
        PlayerDamageHelper.DamageMultiplier multiplier = DamageMultiplierInvoker.woldsvaults$create(
                id, player.getUUID(), value, operation, showOnClient, tickDuration, onTimeout, true);
        return PlayerDamageHelperApplyInvoker.woldsvaults$apply(player.getServer(), player.getUUID(), multiplier);
    }

    public static boolean remove(ServerPlayer player, UUID id) {
        return PlayerDamageHelper.removeMultiplier(player, id);
    }

    /** The live ability-power multiplier for this player, folded the same way the AD bucket is. */
    public static float getMultiplier(ServerPlayer player) {
        return PlayerDamageHelper.getDamageMultiplier(player, true, true);
    }
}
