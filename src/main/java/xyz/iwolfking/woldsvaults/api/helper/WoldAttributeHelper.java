package xyz.iwolfking.woldsvaults.api.helper;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.snapshot.AttributeSnapshot;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import iskallia.vault.util.calc.PlayerStat;
import iskallia.vault.util.calc.ThornsHelper;
import net.minecraft.world.entity.LivingEntity;

public class WoldAttributeHelper {
    public static float getAdditionalAbilityPower(LivingEntity entity) {
        float additionalMultiplier = 0.0F;
        AttributeSnapshot snapshot = AttributeSnapshotHelper.getInstance().getSnapshot(entity);
        additionalMultiplier += (Float)snapshot.getAttributeValue(ModGearAttributes.ABILITY_POWER, VaultGearAttributeTypeMerger.floatSum());
        return additionalMultiplier;
    }
}
