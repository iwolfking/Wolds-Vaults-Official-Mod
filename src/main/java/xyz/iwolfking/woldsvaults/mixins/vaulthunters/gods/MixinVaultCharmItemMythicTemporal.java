package xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods;

import iskallia.vault.item.gear.VaultCharmItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.items.gear.MythicVaultCharmItem;

import java.util.UUID;

@Mixin(value = VaultCharmItem.class, remap = false)
public abstract class MixinVaultCharmItemMythicTemporal {

    /**
     * @author PoorMansPhysicist
     * @reason the base one-shot god charm temporal keybind reads the blessing attribute off the
     * snapshot and would apply a mythic charm's temporal at its FULL duration, once per vault,
     * without touching the charm's remaining blessing time - bypassing the toggle budget and
     * stacking with it. This method's single caller is that message handler, so answering
     * "already used" for mythic stacks retires the one-shot path for them; mythic blessings flow
     * only through the toggle keybind.
     */
    @Inject(method = "hasUsedTemporalIn", at = @At("HEAD"), cancellable = true)
    private static void woldsVaults$mythicUsesToggleOnly(ItemStack stack, UUID vaultId,
                                                         CallbackInfoReturnable<Boolean> cir) {
        if (MythicVaultCharmItem.isMythic(stack)) {
            cir.setReturnValue(true);
        }
    }
}
