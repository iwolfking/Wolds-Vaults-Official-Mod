package xyz.iwolfking.woldsvaults.mixins.vaulthunters;

import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.init.ModConfigs;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.data.gear.UnusualModifiers;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.VaultGearTierConfigAccessor;


@Mixin(value = ModConfigs.class, remap = false)
public class MixinModConfigs {

    @Inject(method = "register", at = @At("HEAD"), remap = false)
    private static void onReloadConfigsPre(CallbackInfo ci) {
        xyz.iwolfking.woldsvaults.init.ModConfigs.preregister();
    }

    @Inject(method = "register", at = @At("TAIL"), remap = false)
    private static void onReloadConfigs(CallbackInfo ci) {
        xyz.iwolfking.woldsvaults.init.ModConfigs.register();
        ModConfigs.ETERNAL_ATTRIBUTES = xyz.iwolfking.woldsvaults.init.ModConfigs.ETERNAL_ATTRIBUTES;
        for(ResourceLocation config : ModConfigs.VAULT_GEAR_CONFIG.keySet()) {
            if(UnusualModifiers.UNUSUAL_MODIFIERS_MAP_PREFIX.containsKey(config)) {
                ((VaultGearTierConfigAccessor)ModConfigs.VAULT_GEAR_CONFIG.get(config)).getModifierGroup().put(VaultGearTierConfig.ModifierAffixTagGroup.valueOf("UNUSUAL_PREFIX"), UnusualModifiers.UNUSUAL_MODIFIERS_MAP_PREFIX.get(config));
            }
        }
        for(ResourceLocation config : ModConfigs.VAULT_GEAR_CONFIG.keySet()) {
            if(UnusualModifiers.UNUSUAL_MODIFIERS_MAP_SUFFIX.containsKey(config)) {
                ((VaultGearTierConfigAccessor)ModConfigs.VAULT_GEAR_CONFIG.get(config)).getModifierGroup().put(VaultGearTierConfig.ModifierAffixTagGroup.valueOf("UNUSUAL_SUFFIX"), UnusualModifiers.UNUSUAL_MODIFIERS_MAP_SUFFIX.get(config));
            }
        }
    }


}
