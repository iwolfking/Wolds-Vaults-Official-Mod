package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.config.GearModelRollRaritiesConfig;
import iskallia.vault.gear.VaultGearRarity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin Class currently used to unlock gated Patreon transmogs for any person.
 */
@Mixin(value = GearModelRollRaritiesConfig.class, remap = false)
public class MixinGearModelRollRarities {

}
