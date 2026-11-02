package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.snapshot.AttributeSnapshot;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import net.minecraft.world.entity.LivingEntity;
import xyz.iwolfking.woldsvaults.abilities.SneakyGetawayAbility;
import xyz.iwolfking.woldsvaults.api.data.WoldConstants;
import xyz.iwolfking.woldsvaults.init.ModEffects;

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

    public static float getDodgeChance(LivingEntity entity) {
        AttributeSnapshot snapshot = AttributeSnapshotHelper.getInstance().getSnapshot(entity);
        float chance = LuckHelper.getLuckAffectedChance(snapshot.getAttributeValue(xyz.iwolfking.woldsvaults.init.ModGearAttributes.DODGE_PERCENT, VaultGearAttributeTypeMerger.floatSum()), entity);
        if(entity.hasEffect(ModEffects.SNEAKY_GETAWAY)) {
            chance += SneakyGetawayAbility.SneakyGetawayEffect.getSneakyEtchingDodgeChance(entity);
        }

        return chance;
    }

    public static float getHexingChance(LivingEntity entity) {
        AttributeSnapshot snapshot = AttributeSnapshotHelper.getInstance().getSnapshot(entity);
        return LuckHelper.getLuckAffectedChance(snapshot.getAttributeValue(xyz.iwolfking.woldsvaults.init.ModGearAttributes.HEXING_CHANCE, VaultGearAttributeTypeMerger.floatSum()), entity);
    }

    public static float getBurningChance(LivingEntity entity) {
        AttributeSnapshot snapshot = AttributeSnapshotHelper.getInstance().getSnapshot(entity);
        return LuckHelper.getLuckAffectedChance(snapshot.getAttributeValue(xyz.iwolfking.woldsvaults.init.ModGearAttributes.BURNING_HIT_CHANCE, VaultGearAttributeTypeMerger.floatSum()), entity);
    }

    public static float getEchoingChance(LivingEntity entity) {
        AttributeSnapshot snapshot = AttributeSnapshotHelper.getInstance().getSnapshot(entity);
        return LuckHelper.getLuckAffectedChance(snapshot.getAttributeValue(xyz.iwolfking.woldsvaults.init.ModGearAttributes.ECHOING_CHANCE, VaultGearAttributeTypeMerger.floatSum()), entity);
    }


    public static float getDodgeChanceCap(LivingEntity entity) {
        return WoldConstants.DODGE_PERCENT_CAP;
    }

    public static void withSnapshot(LivingEntity entity, boolean serverOnly, BiConsumer<LivingEntity, AttributeSnapshot> fn) {
        if (AttributeSnapshotHelper.canHaveSnapshot(entity)) {
            if (!serverOnly || !entity.getCommandSenderWorld().isClientSide()) {
                fn.accept(entity, AttributeSnapshotHelper.getInstance().getSnapshot(entity));
            }
        }
    }
}
