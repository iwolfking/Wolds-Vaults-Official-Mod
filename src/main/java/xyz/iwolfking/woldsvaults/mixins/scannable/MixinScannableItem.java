package xyz.iwolfking.woldsvaults.mixins.scannable;

import li.cil.scannable.common.item.ModItem;
import li.cil.scannable.common.item.ScannerItem;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "scannable")
    }
)
@Mixin(value = ScannerItem.class, remap = false)
public abstract class MixinScannableItem extends ModItem {
    @Shadow private static float getRelativeEnergy(ItemStack stack) {
        throw new AssertionError("Mixin application failed");
    }
    @Inject(method = "isBarVisible", at = @At("HEAD"), cancellable = true, remap = true)
    public void hideBarWhenFullyCharged(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (getRelativeEnergy(stack) == 1) {
            cir.setReturnValue(false);
        }
    }

    @Redirect(method = {"appendHoverText", "isBarVisible"}, at = @At(value = "FIELD", target = "Lli/cil/scannable/common/config/CommonConfig;useEnergy:Z", opcode = Opcodes.GETSTATIC))
    private boolean alwaysUseEnergy() {
        return true;
    }

    @Redirect(method = {"tryConsumeEnergy", "getRelativeEnergy", "getModuleEnergyCost"}, at = @At(value = "FIELD", target = "Lli/cil/scannable/common/config/CommonConfig;useEnergy:Z", opcode = Opcodes.GETSTATIC))
    private static boolean alwaysUseEnergyStatic() {
        return true;
    }

    @Redirect(method = "finishUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemCooldowns;addCooldown(Lnet/minecraft/world/item/Item;I)V"), remap = true)
    private void adjustCooldown(ItemCooldowns instance, Item pItem, int pTicks) {
        instance.addCooldown(pItem, WoldsVaultsConfig.SERVER.scannableScannerCooldown.get());
    }
}
