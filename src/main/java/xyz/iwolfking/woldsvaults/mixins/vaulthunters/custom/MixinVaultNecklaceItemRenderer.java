package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.talent.TalentLevelAttribute;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.item.gear.VaultNecklaceItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = VaultNecklaceItemRenderer.class, remap = false)
public class MixinVaultNecklaceItemRenderer {
    @Inject(method = "getAbilityId", at = @At("HEAD"), cancellable = true)
    private static void renderIconForTalents(VaultGearData data, CallbackInfoReturnable<String> cir) {
        if(data.hasAttribute(ModGearAttributes.TALENT_LEVEL)) {
            TalentLevelAttribute talentLevelAttribute = data.getFirstValue(ModGearAttributes.TALENT_LEVEL).orElse(null);
            if(talentLevelAttribute == null) {
                return;
            }

            cir.setReturnValue("Fireball_Base");

        }


    }
}
