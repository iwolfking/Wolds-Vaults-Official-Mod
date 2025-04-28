package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.tooltip.GearTooltip;
import iskallia.vault.item.gear.VaultCharmItem;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = VaultCharmItem.class, remap = false)
public abstract class MixinGodCharmItem {
    @Shadow public abstract void addTooltipAffixGroupWithBaseValue(VaultGearData data, VaultGearModifier.AffixType type, ItemStack stack, List<Component> tooltip, boolean displayDetails, boolean showBaseValue);

    @Inject(method = "appendHoverText", remap = true, at = @At(value = "INVOKE", target = "Liskallia/vault/gear/data/VaultGearData;getModifiers(Liskallia/vault/gear/attribute/VaultGearModifier$AffixType;)Ljava/util/List;", ordinal = 0))
    private void addImplicits(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn, CallbackInfo ci, @Local VaultGearData data) {
        List<VaultGearModifier<?>> implicits = data.getModifiers(VaultGearModifier.AffixType.IMPLICIT);
        for(VaultGearModifier<?> mod : implicits) {
           MutableComponent display = mod.getDisplay(data, VaultGearModifier.AffixType.IMPLICIT, stack, GearTooltip.itemTooltip().displayModifierDetail()).orElse(null);
           if(display != null) {
               tooltip.add(display);
           }
        }
    }
}
