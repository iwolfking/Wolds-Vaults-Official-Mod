package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.gear.VaultGearHelper;
import iskallia.vault.gear.data.VaultGearData;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;
import xyz.iwolfking.woldsvaults.items.gear.VaultMapItem;

@Mixin(value = VaultGearHelper.class, remap = false)
public class MixinVaultGearHelper {
    @Inject(method = "initializeGearRollType(Lnet/minecraft/world/item/ItemStack;ILiskallia/vault/core/random/RandomSource;)V", at= @At(value = "TAIL"))
    private static void initializeMapTier(ItemStack stack, int gearLevel, RandomSource random, CallbackInfo ci, @Local VaultGearData data) {
        if(stack.getItem() instanceof VaultMapItem mapItem) {
            if(!data.hasAttribute(ModGearAttributes.MAP_TIER)) {
                int tier = stack.getOrCreateTag().getInt("the_vault:map_tier");
                data.createOrReplaceAttributeValue(ModGearAttributes.MAP_TIER, tier);
                data.write(stack);
            }
        }
    }
}
