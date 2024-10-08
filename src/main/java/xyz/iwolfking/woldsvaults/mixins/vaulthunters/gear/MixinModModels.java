package xyz.iwolfking.woldsvaults.mixins.vaulthunters.gear;

import iskallia.vault.init.ModModels;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.iwolfking.woldsvaults.api.registry.CustomVaultGearRegistry;

@Mixin(value = ModModels.class)
public class MixinModModels {


    @Redirect(method = "registerItemColors", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/color/item/ItemColors;register(Lnet/minecraft/client/color/item/ItemColor;[Lnet/minecraft/world/level/ItemLike;)V", ordinal = 0))
    private static void redirectCall(ItemColors instance, ItemColor itemColor, ItemLike[] p_92690_) {
        instance.register(itemColor, CustomVaultGearRegistry.getItemLikes());
    }
}
