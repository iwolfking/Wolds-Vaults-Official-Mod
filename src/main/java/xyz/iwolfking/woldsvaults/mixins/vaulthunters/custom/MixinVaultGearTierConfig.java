package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.VaultMod;
import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.gear.data.VaultGearData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;
import xyz.iwolfking.woldsvaults.items.gear.VaultMapItem;

import java.util.Optional;

@Mixin(value = VaultGearTierConfig.class, remap = false, priority = 1500)
public abstract class MixinVaultGearTierConfig {
    @Shadow
    static Optional<VaultGearTierConfig> getConfig(ResourceLocation key) {
        return null;
    }

    @Inject(method = "getConfig(Lnet/minecraft/world/item/ItemStack;)Ljava/util/Optional;", at = @At("HEAD"), cancellable = true)
    private static void getMapTierConfig(ItemStack stack, CallbackInfoReturnable<Optional<VaultGearTierConfig>> cir) {
        if(stack.getItem() instanceof VaultMapItem mapItem) {
            VaultGearData data = VaultGearData.read(stack);
            int tier = data.getFirstValue(ModGearAttributes.MAP_TIER).orElse(0);
            if(tier == 0) {
                cir.setReturnValue(getConfig(VaultMod.id("map")));
            }
            else {
                if(getConfig(VaultMod.id("map_" + tier)).isPresent()) {
                    cir.setReturnValue(getConfig(VaultMod.id("map_" + tier)));
                }
                else {
                    cir.setReturnValue(getConfig(VaultMod.id("map")));
                }
            }
        }
    }
}
