package xyz.iwolfking.woldsvaults.gods.combat;

import iskallia.vault.util.damage.PlayerDamageHelper;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods.DamageMultiplierInvoker;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods.PlayerDamageHelperApplyInvoker;

import java.util.UUID;
import java.util.function.Consumer;

/** Registers {@code PlayerDamageHelper} multipliers on its ability-power side, which its own factories never do. */
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

    public static float getMultiplier(ServerPlayer player) {
        return PlayerDamageHelper.getDamageMultiplier(player, true, true);
    }
}
