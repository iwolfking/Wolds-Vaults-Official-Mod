package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.init.ModAttributes;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.event.RegistryEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.mixins.RangedAttributeAccessor;

@Mixin(value = ModAttributes.class, remap = false)
public class MixinModAttributes {
    /**
     * Lifts the attack damage cap (1,000,000) and the max mana cap (4096) that the_vault sets at registration,
     * so hyper scaling on boss hits and mana past 4096 are no longer silently clamped.
     */
    @Inject(method = "register", at = @At("TAIL"))
    private static void woldsVaults$uncapAttributes(RegistryEvent.Register<Attribute> event, CallbackInfo ci) {
        if (Attributes.ATTACK_DAMAGE instanceof RangedAttribute) {
            ((RangedAttributeAccessor) Attributes.ATTACK_DAMAGE).woldsVaults$setMaxValue(Double.MAX_VALUE);
        }
        if (ModAttributes.MANA_MAX instanceof RangedAttribute) {
            ((RangedAttributeAccessor) ModAttributes.MANA_MAX).woldsVaults$setMaxValue(Double.MAX_VALUE);
        }
    }
}
