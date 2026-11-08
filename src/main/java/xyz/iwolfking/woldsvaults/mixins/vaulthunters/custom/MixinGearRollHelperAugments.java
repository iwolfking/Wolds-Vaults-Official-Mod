package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.gear.GearRollHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.augments.item.AugmentRoller;

@Mixin(value = GearRollHelper.class, remap = false)
public abstract class MixinGearRollHelperAugments {

    /** Rolls augment sockets onto a wand or focus once it has been identified. */
    @Inject(method = "initializeGear(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;)V", at = @At("TAIL"))
    private static void woldsvaults$rollAugments(ItemStack stack, Player player, CallbackInfo ci) {
        if (player == null || !player.level.isClientSide) {
            AugmentRoller.rollIfMissing(stack, GearRollHelper.rand);
        }
    }
}
