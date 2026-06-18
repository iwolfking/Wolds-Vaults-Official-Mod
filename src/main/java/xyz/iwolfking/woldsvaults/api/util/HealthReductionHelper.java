package xyz.iwolfking.woldsvaults.api.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Overwrite;

import static iskallia.vault.item.ItemVaultFruit.MAX_HEALTH_REDUCTION_ATTRIBUTE_MODIFIER_UUID;

public class HealthReductionHelper {

    private static final double DEFAULT_MULT = 1.0849795594911;
    private static final double DEFAULT_MULT_SCALING = 0.827;

    public static void reducePlayerMaxHealth(ServerPlayer player, double mult, double multScaling) {
        AttributeInstance attributeInstance = player.getAttribute(Attributes.MAX_HEALTH);
        if(attributeInstance != null) {
            AttributeModifier existingModifier = attributeInstance.getModifier(MAX_HEALTH_REDUCTION_ATTRIBUTE_MODIFIER_UUID);
            if (existingModifier != null) {
                mult = existingModifier.getAmount() + 1;
                attributeInstance.removeModifier(MAX_HEALTH_REDUCTION_ATTRIBUTE_MODIFIER_UUID);
            }

            mult *= multScaling;

            attributeInstance.addPermanentModifier(new AttributeModifier(MAX_HEALTH_REDUCTION_ATTRIBUTE_MODIFIER_UUID, "VaultFruitMaxHealthReduction", mult - 1, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    public static void reducePlayerMaxHealth(ServerPlayer player) {
        reducePlayerMaxHealth(player, DEFAULT_MULT, DEFAULT_MULT_SCALING);
    }
}
