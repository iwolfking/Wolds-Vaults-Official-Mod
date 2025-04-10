package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.gear.data.GearDataCache;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

@Mixin(value = GearDataCache.class, remap = false)
public abstract class MixinGearDataCache {
    @Shadow @Nullable public abstract VaultGearRarity getRarity();

    @Inject(method = "getGearColorComponents", at = @At("HEAD"), cancellable = true)
    private void useCustomColorConfigInsteadOfCache(CallbackInfoReturnable<List<Integer>> cir) {
        if(ModConfigs.VAULT_GEAR_RARITY_COLOR_CONFIG.GEAR_RARITY_COLOR_MAP.containsKey(getRarity())) {
            cir.setReturnValue(List.of(ModConfigs.VAULT_GEAR_RARITY_COLOR_CONFIG.getColorSafe(getRarity()).getValue()));
        }
    }
}
