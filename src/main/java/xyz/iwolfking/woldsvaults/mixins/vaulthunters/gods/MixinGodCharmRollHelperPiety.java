package xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods;

import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.gear.GodCharmRollHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.iwolfking.woldsvaults.gods.GodPiety;

import java.util.UUID;

@Mixin(value = GodCharmRollHelper.class, remap = false)
public abstract class MixinGodCharmRollHelperPiety {
    /**
     * @author PoorMansPhysicist
     * @reason a freshly identified charm banks its scale units from piety rather than god
     * reputation, ten piety per unit, so charm rolls pick up god levels and tree bonuses too.
     */
    @Redirect(method = "initializeGodCharm",
            at = @At(value = "INVOKE",
                    target = "Liskallia/vault/world/data/PlayerReputationData;getReputation(Ljava/util/UUID;Liskallia/vault/core/vault/influence/VaultGod;)I"))
    private static int woldsVaults$rollFromPiety(UUID playerId, VaultGod god, ItemStack stack, Player player) {
        return GodPiety.scaleUnits(player, god);
    }
}
