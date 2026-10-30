package xyz.iwolfking.woldsvaults.effect.mobeffects;

import iskallia.vault.init.ModAttributes;
import iskallia.vault.init.ModGearAttributes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.UUID;

public class QuickeningEffect extends MobEffect {
    public QuickeningEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x55FFFF);
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                "ea4f6958-8ab0-4a1e-910c-dcf7417ec18f",
                0.10D,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
        this.setRegistryName(WoldsVaults.id("quickening"));

    }
}