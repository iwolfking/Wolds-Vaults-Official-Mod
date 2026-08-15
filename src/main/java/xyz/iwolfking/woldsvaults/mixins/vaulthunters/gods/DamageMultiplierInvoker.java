package xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods;

import iskallia.vault.util.damage.PlayerDamageHelper;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Factory access to the private DamageMultiplier constructor, which is the only place the
 * {@code isAPMult} flag can be set. Every public factory in PlayerDamageHelper hardcodes it to
 * false, leaving the ability-power side of the registry with no producer.
 */
@Mixin(value = PlayerDamageHelper.DamageMultiplier.class, remap = false)
public interface DamageMultiplierInvoker {
    @Invoker("<init>")
    static PlayerDamageHelper.DamageMultiplier woldsvaults$create(UUID id, UUID playerId, float value,
                                                                 PlayerDamageHelper.Operation operation,
                                                                 boolean showOnClient, int tickTimeout,
                                                                 Consumer<ServerPlayer> onTimeout, boolean isAPMult) {
        throw new UnsupportedOperationException();
    }
}
