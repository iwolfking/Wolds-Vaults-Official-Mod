package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.init.ModEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.RegistryObject;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ModEffects.class, remap = false)
public class MixinModEffects {
    @Shadow
    @Final
    @Mutable
    public static MobEffect BLEED = xyz.iwolfking.woldsvaults.init.ModEffects.BLEED_OVERRIDE;

    @Redirect(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/registries/RegistryObject;get()Ljava/lang/Object;"
            )
    )
    private static Object deferReachAttributeGet(RegistryObject<?> instance) {
        if (!instance.isPresent()) {
            return null;
        }
        return instance.get();
    }
}
