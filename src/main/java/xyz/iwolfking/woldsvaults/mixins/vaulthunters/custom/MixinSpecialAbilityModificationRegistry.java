package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.gear.attribute.ability.special.base.SpecialAbilityModification;
import iskallia.vault.gear.attribute.ability.special.base.SpecialAbilityModificationRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.modifiers.gear.special.FireballModification;
import xyz.iwolfking.woldsvaults.modifiers.gear.special.GlacialBlastModification;
import xyz.iwolfking.woldsvaults.modifiers.gear.special.ImplodingJavelinModification;

@Mixin(value = SpecialAbilityModificationRegistry.class, remap = false)
public abstract class MixinSpecialAbilityModificationRegistry {

    @Shadow
    protected static void register(SpecialAbilityModification<?, ?> modification) {
    }

    @Inject(method = "init", at = @At("TAIL"))
    private static void init(CallbackInfo ci) {
        register(new GlacialBlastModification());
        register(new FireballModification());
        register(new ImplodingJavelinModification());
    }
}
