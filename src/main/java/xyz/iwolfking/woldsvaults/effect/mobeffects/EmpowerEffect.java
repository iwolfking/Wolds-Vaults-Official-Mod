package xyz.iwolfking.woldsvaults.effect.mobeffects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.UUID;

public class EmpowerEffect extends MobEffect {

    private static final UUID DAMAGE_MULTIPLIER_UUID = UUID.fromString("a9a87ad4-1b2f-4a31-ae22-55f2c76e0abc");

    public EmpowerEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFAA00);


        this.addAttributeModifier(
                Attributes.ATTACK_DAMAGE,
                DAMAGE_MULTIPLIER_UUID.toString(),
                0.10D,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
        this.setRegistryName(WoldsVaults.id("empower"));
    }
}
