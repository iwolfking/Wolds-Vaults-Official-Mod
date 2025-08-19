package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.gear.VaultGearHelper;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.tooltip.GearTooltip;
import iskallia.vault.item.gear.VaultCharmItem;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(value = VaultCharmItem.class, remap = false)
public abstract class MixinGodCharmItem {
    @Shadow public abstract void addTooltipAffixGroupWithBaseValue(VaultGearData data, VaultGearModifier.AffixType type, ItemStack stack, List<Component> tooltip, boolean displayDetails, boolean showBaseValue);

    @Shadow
    public abstract boolean tryStartIdentification(Player player, ItemStack stack);

    @Inject(method = "appendHoverText", remap = true, at = @At(value = "INVOKE", target = "Liskallia/vault/gear/data/VaultGearData;getModifiers(Liskallia/vault/gear/attribute/VaultGearModifier$AffixType;)Ljava/util/List;", ordinal = 0, remap = false))
    private void addImplicits(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn, CallbackInfo ci, @Local VaultGearData data) {
        List<VaultGearModifier<?>> implicits = data.getModifiers(VaultGearModifier.AffixType.IMPLICIT);
        for(VaultGearModifier<?> mod : implicits) {
           MutableComponent display = mod.getDisplay(data, VaultGearModifier.AffixType.IMPLICIT, stack, GearTooltip.itemTooltip().displayModifierDetail()).orElse(null);
           if(display != null) {
               tooltip.add(display);
           }
        }
    }

    @Redirect(method = "canUnequip", at = @At(value = "INVOKE", target = "Ljava/util/Optional;isPresent()Z"))
    public boolean allowUnequip(Optional<Vault> vault, @Local(argsOnly = true) ItemStack stack) {
        return vault.isPresent() && !VaultCharmItem.isUsableInVault(stack, vault.get().get(Vault.ID));
    }

    @Redirect(method = "canEquip", at = @At(value = "INVOKE", target = "Ljava/util/Optional;isPresent()Z"))
    public boolean allowReequip(Optional<Vault> vault, @Local(argsOnly = true) ItemStack stack) {
        return vault.isPresent() && !VaultCharmItem.isUsableInVault(stack, vault.get().get(Vault.ID));
    }


    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isClientSide()Z"), remap = true, cancellable = true)
    public void use(Level world, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir, @Local ItemStack heldStack, @Local InteractionResultHolder<ItemStack> defaultAction) {
            cir.setReturnValue(VaultGearHelper.rightClick(world, player, hand, defaultAction));
    }
}
