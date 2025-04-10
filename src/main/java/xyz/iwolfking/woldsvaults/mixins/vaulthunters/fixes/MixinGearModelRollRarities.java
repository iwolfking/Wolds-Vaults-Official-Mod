package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.config.GearModelRollRaritiesConfig;
import iskallia.vault.gear.VaultGearRarity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin Class currently used to unlock gated Patreon transmogs for any person.
 */
@Mixin(value = GearModelRollRaritiesConfig.class, remap = false)
public class MixinGearModelRollRarities {

    @Inject(method = "getForcedTierRarity", at = @At(value = "INVOKE", target = "Ljava/util/Optional;get()Ljava/lang/Object;", ordinal = 0), cancellable = true)
    public void unlockGoblinAndChampionArmor(ItemStack stack, ResourceLocation modelId, CallbackInfoReturnable<VaultGearRarity> cir) {
        cir.setReturnValue(null);
    }

    @Inject(method = "getForcedTierRarity", at = @At(value = "INVOKE", target = "Ljava/util/Optional;get()Ljava/lang/Object;", ordinal = 1), cancellable = true)
    public void unlockGodSword(ItemStack stack, ResourceLocation modelId, CallbackInfoReturnable<VaultGearRarity> cir) {
        cir.setReturnValue(null);
    }

    @Inject(method = "getForcedTierRarity", at = @At(value = "INVOKE", target = "Ljava/util/Optional;get()Ljava/lang/Object;", ordinal = 2), cancellable = true)
    public void unlockGodAxe(ItemStack stack, ResourceLocation modelId, CallbackInfoReturnable<VaultGearRarity> cir) {
        cir.setReturnValue(null);
    }

}
