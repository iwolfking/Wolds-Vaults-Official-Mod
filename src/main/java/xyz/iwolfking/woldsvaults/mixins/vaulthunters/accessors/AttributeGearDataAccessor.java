package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.data.AttributeGearData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(value = AttributeGearData.class, remap = false)
public interface AttributeGearDataAccessor {
    @Accessor("attributes")
    List<VaultGearAttributeInstance<?>> woldsvaults$getAttributeList();
}
