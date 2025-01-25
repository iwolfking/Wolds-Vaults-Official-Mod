package xyz.iwolfking.woldsvaults.mixins.bettercombat;

import iskallia.vault.gear.data.GearDataCache;
import iskallia.vault.gear.item.VaultGearItem;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.bettercombat.api.WeaponAttributes;
import net.bettercombat.logic.WeaponRegistry;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "bettercombat")
        }
)
@Mixin(value = WeaponRegistry.class, remap = false)
public class MixinWeaponRegistry {
    @Inject(method = "getAttributes(Lnet/minecraft/world/item/ItemStack;)Lnet/bettercombat/api/WeaponAttributes;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;", shift = At.Shift.AFTER), cancellable = true, remap = true)
    private static void ignoreFallbackForWeaponTypeAttribute(ItemStack itemStack, CallbackInfoReturnable<WeaponAttributes> cir) {
        if(WoldsVaultsConfig.COMMON.weaponsShouldntBeBetter.get()) {
            cir.setReturnValue(null);
        }
        else if(itemStack.getItem() instanceof VaultGearItem) {
            if(GearDataCache.of(itemStack).hasAttribute(ModGearAttributes.WEAPON_TYPE)) {
                cir.setReturnValue(null);
            }
        }

    }
}
