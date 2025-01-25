package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.type.EffectAttributeMerger;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.snapshot.AttributeSnapshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.AttributeSnapshotValueAccessor;

import java.util.*;

@Mixin(value = AttributeSnapshot.class, remap = false)
public class MixinAttributeSnapshot {

    @Shadow
    Map<VaultGearAttribute<?>, AttributeSnapshot.AttributeValue<?, ?>> gearAttributeValues;

    @SafeVarargs
    @Unique
    public final <T, V> V woldsVaults$getManyAttributeValues(VaultGearAttributeTypeMerger<T, V> merger, VaultGearAttribute<T>... manyAttributes) {
        V merged = merger.getBaseValue();
        AttributeSnapshot.AttributeValue<T, V> attributeCache = null;
        for (VaultGearAttribute<T> attribute : manyAttributes) {
            attributeCache = (AttributeSnapshot.AttributeValue<T, V>) this.gearAttributeValues.get(attribute);
            if (attributeCache != null) {
                Object value;
                for(Iterator<T> var5 = ((AttributeSnapshotValueAccessor<T, V>)attributeCache).getCachedValues().iterator(); var5.hasNext(); merged = merger.merge(merged, (T) value)) {
                    value = var5.next();
                }
            }
        }

        return merged;
    }

    /**
     * @author aida
     * @reason check for UNIQUE_EFFECTs
     */
    @Overwrite
    public EffectAttributeMerger.CombinedEffects getGrantedPotions() {
        return this.woldsVaults$getManyAttributeValues(EffectAttributeMerger.getInstance(), iskallia.vault.init.ModGearAttributes.EFFECT, ModGearAttributes.UNIQUE_EFFECT);
    }
}
