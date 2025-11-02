package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.snapshot.AttributeSnapshot;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.BiConsumer;

public class WoldAttributeHelper {
    public static float getAdditionalAbilityPower(LivingEntity entity) {
        float additionalMultiplier = 0.0F;
        float apMulti = 1.0F;
        AttributeSnapshot snapshot = AttributeSnapshotHelper.getInstance().getSnapshot(entity);
        additionalMultiplier += (Float)snapshot.getAttributeValue(ModGearAttributes.ABILITY_POWER, VaultGearAttributeTypeMerger.floatSum());
        apMulti += snapshot.getAttributeValue(ModGearAttributes.ABILITY_POWER_PERCENT, VaultGearAttributeTypeMerger.floatSum());
        return additionalMultiplier * apMulti;
    }

    public static void withSnapshot(LivingEntity entity, boolean serverOnly, BiConsumer<LivingEntity, AttributeSnapshot> fn) {
        if (AttributeSnapshotHelper.canHaveSnapshot(entity)) {
            if (!serverOnly || !entity.getCommandSenderWorld().isClientSide()) {
                fn.accept(entity, AttributeSnapshotHelper.getInstance().getSnapshot(entity));
            }
        }
    }
}
