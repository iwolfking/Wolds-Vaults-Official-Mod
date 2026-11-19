package xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.task.GodAltarTask;
import iskallia.vault.world.data.PlayerReputationData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

import java.util.UUID;

@Mixin(value = GodAltarTask.class, remap = false)
public class MixinGodAltarTaskKeepReputation {

    /** A failed altar (timeout, leaving the vault, the vault ending) no longer takes reputation from its players. */
    @WrapWithCondition(method = "onFail", at = @At(value = "INVOKE",
            target = "Liskallia/vault/world/data/PlayerReputationData;addReputation(Ljava/util/UUID;Liskallia/vault/core/vault/influence/VaultGod;I)V"),
            require = 1)
    private boolean woldsVaults$keepReputationOnFail(UUID player, VaultGod god, int reputation) {
        WoldLogger.debug("God altar failed for {}: kept {} reputation instead of applying {}", player, god, reputation);
        return false;
    }
}
