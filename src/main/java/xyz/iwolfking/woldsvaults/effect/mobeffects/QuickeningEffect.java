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
                "ac1ef94f-48b2-49f0-992e-f836772a2cb4",
                0.10D,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
        this.setRegistryName(WoldsVaults.id("quickening"));

    }
}