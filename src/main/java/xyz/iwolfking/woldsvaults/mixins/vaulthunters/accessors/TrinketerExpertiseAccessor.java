package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.skill.expertise.type.TrinketerExpertise;
import iskallia.vault.snapshot.AttributeSnapshot;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.UUID;

@Mixin(value = TrinketerExpertise.class, remap = false)
public interface TrinketerExpertiseAccessor {
    @Accessor("damageAvoidanceChance")
    void setDamageAvoidanceChance(float damageAvoidanceChance);
}
