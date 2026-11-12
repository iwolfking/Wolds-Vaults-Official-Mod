package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.client.gui.screen.player.element.GearAttributeStatLabel;
import iskallia.vault.client.gui.screen.player.element.StatLabelElementBuilder;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.client.screens.stats.StatRowStyle;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;

import java.util.function.Function;

@Mixin(value = GearAttributeStatLabel.class, remap = false)
public class MixinGearAttributeStatLabel {
    @WrapOperation(method = "of(Lnet/minecraft/world/entity/player/Player;Liskallia/vault/gear/attribute/VaultGearAttribute;Liskallia/vault/gear/attribute/type/VaultGearAttributeTypeMerger;Ljava/util/function/Function;)Liskallia/vault/client/gui/screen/player/element/StatLabelElementBuilder;", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/TextColor;fromRgb(I)Lnet/minecraft/network/chat/TextColor;", remap = true))
    private static TextColor colorLabelOne(int pColor, Operation<TextColor> original, @Local(argsOnly = true) VaultGearAttribute<?> attribute) {
        if(WoldsVaultsConfig.CLIENT.coloredStatisticsScreen.get()) {
            return TextColor.fromRgb(attribute.getReader().getRgbColor());
        }

        return original.call(pColor);
    }

    @WrapOperation(method = "of(Lnet/minecraft/world/entity/player/Player;Liskallia/vault/gear/attribute/VaultGearAttribute;Ljava/util/function/Function;Ljava/util/function/Function;)Liskallia/vault/client/gui/screen/player/element/StatLabelElementBuilder;", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/TextColor;fromRgb(I)Lnet/minecraft/network/chat/TextColor;", remap = true))
    private static TextColor colorLabelTwo(int pColor, Operation<TextColor> original, @Local(argsOnly = true) VaultGearAttribute<?> attribute) {
        if(WoldsVaultsConfig.CLIENT.coloredStatisticsScreen.get()) {
            return TextColor.fromRgb(attribute.getReader().getRgbColor());
        }

        return original.call(pColor);
    }

    @Inject(method = "of(Lnet/minecraft/world/entity/player/Player;Liskallia/vault/gear/attribute/VaultGearAttribute;Liskallia/vault/gear/attribute/type/VaultGearAttributeTypeMerger;Ljava/util/function/Function;)Liskallia/vault/client/gui/screen/player/element/StatLabelElementBuilder;", at = @At("RETURN"))
    private static void keyMergedRow(Player player, VaultGearAttribute<?> attribute, VaultGearAttributeTypeMerger<?, ?> merger, Function<?, MutableComponent> format, CallbackInfoReturnable<StatLabelElementBuilder<?>> cir) {
        ((StatRowStyle) cir.getReturnValue()).woldsvaults$setStatKey(attribute.getRegistryName().toString());
    }

    @Inject(method = "of(Lnet/minecraft/world/entity/player/Player;Liskallia/vault/gear/attribute/VaultGearAttribute;Ljava/util/function/Function;Ljava/util/function/Function;)Liskallia/vault/client/gui/screen/player/element/StatLabelElementBuilder;", at = @At("RETURN"))
    private static void keySuppliedRow(Player player, VaultGearAttribute<?> attribute, Function<?, ?> value, Function<?, MutableComponent> format, CallbackInfoReturnable<StatLabelElementBuilder<?>> cir) {
        ((StatRowStyle) cir.getReturnValue()).woldsvaults$setStatKey(attribute.getRegistryName().toString());
    }
}
