package xyz.iwolfking.woldsvaults.effect.mobeffects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class ShreddedEffect extends MobEffect {
    public ShreddedEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x55FFFF);
        this.addAttributeModifier(
                Attributes.ARMOR,
                "fef79c12-0332-4299-ade6-22d99f6cb92f",
                -0.10D,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
        this.setRegistryName(WoldsVaults.id("shredded"));

    }
}