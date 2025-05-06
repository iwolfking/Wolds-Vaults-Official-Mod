package xyz.iwolfking.woldsvaults.mixins;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.data.GearDataCache;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = ItemRenderer.class, priority = 1500)
public abstract class MixinItemRenderer {

    @TargetHandler(
        mixin = "iskallia.vault.mixin.MixinItemRenderer",
        name = "renderModifierCategoryOverlay"
    )
    @Inject(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Liskallia/vault/gear/data/GearDataCache;getState()Liskallia/vault/gear/VaultGearState;", remap = false, ordinal = 0))
    public void renderEtchingOverlay(Font font, ItemStack stack, int x, int y, String text, CallbackInfo originalCi, CallbackInfo ci,
                                     @Local GearDataCache cache,
                                     @Local List<ResourceLocation> icons) {
        if (cache.getState() == VaultGearState.UNIDENTIFIED && cache.hasAttribute(xyz.iwolfking.woldsvaults.init.ModGearAttributes.IS_ETCHED)) {
            icons.add(VaultGearModifier.AffixCategory.valueOf("ETCHING").getOverlayIcon());
        }
    }

}
