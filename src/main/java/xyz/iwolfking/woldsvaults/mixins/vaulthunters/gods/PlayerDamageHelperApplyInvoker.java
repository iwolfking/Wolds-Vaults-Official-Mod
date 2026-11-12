package xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods;

import iskallia.vault.util.damage.PlayerDamageHelper;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.UUID;

/** Access to the private registration path that stores a multiplier and re-syncs the client. */
@Mixin(value = PlayerDamageHelper.class, remap = false)
public interface PlayerDamageHelperApplyInvoker {
    @Invoker("apply")
    static PlayerDamageHelper.DamageMultiplier woldsvaults$apply(MinecraftServer server, UUID playerId,
                                                                PlayerDamageHelper.DamageMultiplier multiplier) {
        throw new UnsupportedOperationException();
    }
}
