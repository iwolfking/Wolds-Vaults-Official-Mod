package xyz.iwolfking.woldsvaults.mixins;

import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.data.GearDataCache;
import iskallia.vault.gear.item.VaultGearItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.client.init.ModAncientGearModels;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;

/**
 * The base mod's gear item overrides hand every unidentified piece its plain unidentified sprite. Unidentified gear
 * that already carries the ancient marker gets the ember-toned sprite for its gear type instead.
 */
@Mixin(targets = "iskallia.vault.dynamodel.DynamicBakedModel$1", remap = false)
public class MixinDynamicBakedModelOverrides {

    @Inject(method = "resolve", at = @At("HEAD"), cancellable = true, remap = true)
    private void resolveUnidentifiedAncient(BakedModel original, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int seed, CallbackInfoReturnable<BakedModel> cir) {
        if (!(stack.getItem() instanceof VaultGearItem) || !stack.hasTag()) {
            return;
        }
        GearDataCache cache = GearDataCache.of(stack);
        if (cache.getState() != VaultGearState.UNIDENTIFIED || !cache.hasAttribute(ModGearAttributes.ANCIENT_UNIQUE)) {
            return;
        }
        BakedModel ancient = ModAncientGearModels.get(stack.getItem());
        if (ancient != null) {
            cir.setReturnValue(ancient);
        }
    }
}
