package xyz.iwolfking.woldsvaults.api.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.math.Vector3f;
import iskallia.vault.event.ActiveFlags;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.util.calc.AbilityPowerHelper;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import xyz.iwolfking.woldsvaults.api.lib.ElementalType;

import java.util.*;

public class ElementHelper {
    public static List<ElementalType> getAllElementsFromGear(VaultGearData data) {
        List<ElementalType> elementalTypes = new ArrayList<>();
        for(ElementalType type : ElementalType.values()) {
            if(data.hasAttribute(type.getAttribute())) {
                elementalTypes.add(type);
            }
        }
        return elementalTypes;
    }

    public static void runWithElements(List<ElementalType> elementalTypes, Runnable action) {
        runWithElementsRecursive(elementalTypes, 0, action);
    }

    private static void runWithElementsRecursive(List<ElementalType> types, int index, Runnable action) {
        if (index >= types.size()) {
            action.run();
            return;
        }

        ElementalType type = types.get(index);
        type.getFlag().runWithFlag(() -> runWithElementsRecursive(types, index + 1, action));
    }

    public static void applyElementalEffects(LivingEntity target, LivingEntity attacker, Number effectValue, List<ElementalType> elementalTypes, boolean triggerIfPresent) {
        for(ElementalType type : elementalTypes) {
            if(triggerIfPresent || type.getFlag().isSet()) {
                if(type.equals(ElementalType.ICE)) {
                    effectValue = 1;
                }
                else if(type.equals(ElementalType.POISON)) {
                    if(attacker instanceof ServerPlayer player) {
                        effectValue = Math.min(7, Math.log10(AbilityPowerHelper.getAbilityPower(player) * 2));
                    }
                }

                type.applyEffect(target, attacker, effectValue);
            }
        }
    }

    public static void applyElementalEffects(LivingEntity target, LivingEntity attacker, Number effectValue) {
        applyElementalEffects(target, attacker, effectValue, Arrays.stream(ElementalType.values()).toList(), false);
    }

    public static void triggerLightningChain(LivingEntity target, LivingEntity attacker, Number damage) {
        if (target.level instanceof ServerLevel serverLevel) {
            float minimumDamage = 0.1F;
            Pair<Float, Float> stunChanceAndDuration = Pair.of(0F, 0F);
            if(attacker instanceof ServerPlayer serverPlayer) {
                minimumDamage += LightningTalentHelper.getMinimumDamageBonus(serverPlayer);
                stunChanceAndDuration = LightningTalentHelper.getStunChance(serverPlayer);
            }

            int maxBounces = 6;
            double bounceRange = 6.0D;

            LivingEntity currentSource = target;
            Set<UUID> hitEntities = new HashSet<>();
            hitEntities.add(attacker.getUUID());
            hitEntities.add(target.getUUID());

            for (int i = 0; i < maxBounces; i++) {
                float damageScaling = attacker.getRandom().nextFloat(minimumDamage, 1.5F);
                float chainDamage = damage.floatValue() * damageScaling;

                LivingEntity nextTarget = serverLevel.getEntitiesOfClass(
                        LivingEntity.class,
                        currentSource.getBoundingBox().inflate(bounceRange),
                        entity -> entity != attacker && !hitEntities.contains(entity.getUUID()) && entity.isAlive()
                ).stream().min(Comparator.comparingDouble(currentSource::distanceToSqr)).orElse(null);

                if (nextTarget == null) break;

                spawnLightningBeam(serverLevel, currentSource.position().add(0, currentSource.getBbHeight() / 2, 0), nextTarget.position().add(0, nextTarget.getBbHeight() / 2, 0));
                ActiveFlags.IS_AP_ATTACKING.push();
                nextTarget.hurt(DamageSource.indirectMagic(currentSource, attacker), chainDamage);
                if(attacker instanceof ServerPlayer serverPlayer) {
                    LightningTalentHelper.tryToApplyStun(serverPlayer, currentSource, stunChanceAndDuration);
                }
                ActiveFlags.IS_AP_ATTACKING.pop();
                hitEntities.add(nextTarget.getUUID());
                currentSource = nextTarget;
            }
        }
    }

    private static void spawnLightningBeam(ServerLevel level, Vec3 start, Vec3 end) {
        Vec3 direction = end.subtract(start);
        double distance = direction.length();
        Vec3 step = direction.normalize().scale(0.5D);

        double currentDistance = 0.0D;
        Vec3 currentPos = start;

        while (currentDistance < distance) {

            level.sendParticles(
                    new DustParticleOptions(new Vector3f(1.0F, 0.95F, 0.1F), 1.2F),
                    currentPos.x, currentPos.y, currentPos.z,
                    1,
                    0.1D, 0.1D, 0.1D,
                    0.0D
            );

            currentPos = currentPos.add(step);
            currentDistance += 0.5D;
        }
    }
}
