package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.init.ModEffects;
import net.minecraft.world.effect.MobEffect;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ModEffects.class, remap = false)
public class MixinModEffects {
    @Shadow
    @Final
    @Mutable
    public static MobEffect BLEED = xyz.iwolfking.woldsvaults.init.ModEffects.BLEED_OVERRIDE;
}
