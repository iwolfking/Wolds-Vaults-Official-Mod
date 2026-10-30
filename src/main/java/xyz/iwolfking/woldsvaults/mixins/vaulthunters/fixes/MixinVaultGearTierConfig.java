package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.gear.attribute.VaultGearModifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

@Mixin(value = VaultGearTierConfig.class, remap = false, priority = 1500)
public abstract class MixinVaultGearTierConfig {
    @Shadow
    @Final
    private Map<VaultGearTierConfig.ModifierAffixTagGroup, VaultGearTierConfig.AttributeGroup> modifierGroup;

    /**
     * @author PoorMansPhysicist
     * @reason Draw from one weighted pool of every rollable tier in the affix pool, so a tier's
     * weight is relative to all other modifiers rather than only the other tiers of its own
     * modifier.
     */
    @Overwrite
    public Optional<VaultGearModifier<?>> getRandomModifier(VaultGearTierConfig.ModifierAffixTagGroup affixTagGroup, int level, Random random, Set<String> excludedModGroups) {
        VaultGearTierConfig.AttributeGroup attributePool = this.modifierGroup.get(affixTagGroup);
        if (attributePool == null || attributePool.isEmpty()) {
            return Optional.empty();
        }
        WeightedList<VaultGearTierConfig.ModifierOutcome<?>> outcomes = new WeightedList<>();
        attributePool.forEach(group -> {
            if (!group.getModifierGroup().isEmpty() && excludedModGroups.contains(group.getModifierGroup())) {
                return;
            }
            group.getModifiersForLevel(level).forEach(tier -> outcomes.add(new VaultGearTierConfig.ModifierOutcome<>(tier, group), tier.getWeight()));
        });
        return outcomes.getRandom(random).map(outcome -> outcome.makeModifier(random));
    }

    /**
     * @author PoorMansPhysicist
     * @reason Draw from one weighted pool of every rollable tier in the affix pool, applying the
     * existing tag penalty to each tier weight rather than to a modifier's chance of being picked.
     */
    @Overwrite
    public Optional<VaultGearModifier<?>> getRandomModifierWithExistingTagPenalty(VaultGearTierConfig.ModifierAffixTagGroup affixTagGroup, int level, Random random, Set<String> excludedModGroups, Set<String> existingTags) {
        VaultGearTierConfig.AttributeGroup attributePool = this.modifierGroup.get(affixTagGroup);
        if (attributePool == null || attributePool.isEmpty()) {
            return Optional.empty();
        }
        WeightedList<VaultGearTierConfig.ModifierOutcome<?>> outcomes = new WeightedList<>();
        attributePool.forEach(group -> {
            if (!group.getModifierGroup().isEmpty() && excludedModGroups.contains(group.getModifierGroup())) {
                return;
            }
            boolean hasPenalty = !Collections.disjoint(group.getTags(), existingTags);
            group.getModifiersForLevel(level).forEach(tier -> {
                double weight = hasPenalty ? tier.getWeight() * 0.25D : tier.getWeight();
                if (weight > 0.0D) {
                    outcomes.add(new VaultGearTierConfig.ModifierOutcome<>(tier, group), weight);
                }
            });
        });
        return outcomes.getRandom(random).map(outcome -> outcome.makeModifier(random));
    }
}
