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
     * @reason answering "already used" for a mythic stack closes the base one-shot temporal path for
     * it, so a mythic charm's blessings flow only through the toggle keybind and its budget
     */
    @Inject(method = "hasUsedTemporalIn", at = @At("HEAD"), cancellable = true)
    private static void woldsVaults$mythicUsesToggleOnly(ItemStack stack, UUID vaultId,
                                                         CallbackInfoReturnable<Boolean> cir) {
        if (MythicVaultCharmItem.isMythic(stack)) {
            cir.setReturnValue(true);
        }
    }
}
