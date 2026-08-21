package xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.snapshot.AttributeSnapshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.gods.charms.MythicCharmStats;

@Mixin(value = AttributeSnapshot.class, remap = false)
public abstract class MixinAttributeSnapshotMultipliers {

    /**
     * @author PoorMansPhysicist
     * @reason apply the mythic charm's item quantity, item rarity and trap disarm multipliers at
     * the merged-snapshot read, the single point every consumer of those stats - loot generation,
     * the statistics screen, tooltips - shares. The multiplier attributes themselves are not in
     * the mapping, so the nested read cannot recurse.
     */
    @ModifyReturnValue(method = "getAttributeValue", at = @At("RETURN"))
    private Object woldsVaults$applyMythicMultipliers(Object result, VaultGearAttribute<?> attribute,
                                                     VaultGearAttributeTypeMerger<?, ?> merger) {
        if (!(result instanceof Float base)) {
            return result;
        }
        VaultGearAttribute<Float> multiplierAttribute = MythicCharmStats.snapshotMultiplierFor(attribute);
        if (multiplierAttribute == null) {
            return result;
        }
        AttributeSnapshot self = (AttributeSnapshot) (Object) this;
        float multiplier = self.getAttributeValue(multiplierAttribute, VaultGearAttributeTypeMerger.floatSum());
        return multiplier > 0.0F ? base * (1.0F + multiplier) : result;
    }
}
