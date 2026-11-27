package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft.item;

import iskallia.vault.gear.GearRollHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.spellcraft.roll.SpellcraftRoller;

@Mixin(value = GearRollHelper.class, remap = false)
public abstract class MixinGearRollHelperSpellcraft {

    /** Rolls spellcraft implicits (arcana, glyph lines, depth) onto a wand or focus once it has been identified. */
    @Inject(method = "initializeGear(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;)V", at = @At("TAIL"))
    private static void woldsvaults$rollSpellcraft(ItemStack stack, Player player, CallbackInfo ci) {
        if (player == null || !player.level.isClientSide) {
            SpellcraftRoller.rollIfMissing(stack, GearRollHelper.rand);
        }
    }
}
