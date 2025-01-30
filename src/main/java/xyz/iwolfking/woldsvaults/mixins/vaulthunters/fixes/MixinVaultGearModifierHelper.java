package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.block.PlaceholderBlock;
import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.gear.VaultGearModifierHelper;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.attribute.config.ConfigurableAttributeGenerator;
import iskallia.vault.gear.attribute.talent.TalentLevelAttribute;
import iskallia.vault.gear.comparator.VaultGearAttributeComparator;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.modification.GearModification;
import iskallia.vault.util.MiscUtils;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Random;

@Mixin(value = VaultGearModifierHelper.class, remap = false)
public abstract class MixinVaultGearModifierHelper {

    /**
     * @author iwolfking
     * @reason Don't allow chaotic on Unusual modifiers.
     */
    @Overwrite
    public static GearModification.Result reForgeOutcomeOfRandomModifier(ItemStack stack, long worldGameTime, Random random) {
        VaultGearData data = VaultGearData.read(stack);
        if (!data.isModifiable()) {
            return GearModification.Result.errorUnmodifiable();
        } else {
            List<Tuple<VaultGearModifier<?>, WeightedList<VaultGearTierConfig.ModifierOutcome<?>>>> modifierReplacements = getAvailableModifierConfigurationOutcomes(data, stack, true);
            if (modifierReplacements == null) {
                return GearModification.Result.errorInternal();
            }
            modifierReplacements.removeIf(tpl -> {
                if(tpl.getA().hasCategory(VaultGearModifier.AffixCategory.valueOf("UNUSUAL"))){
                    return true;
                }
                return (tpl.getB()).size() <= 1;
            });
            if (modifierReplacements.isEmpty()) {
                return GearModification.Result.makeActionError("no_modifiers");
            } else {
                Tuple<VaultGearModifier<?>, WeightedList<VaultGearTierConfig.ModifierOutcome<?>>> potentialReplacements = MiscUtils.getRandomEntry(modifierReplacements);
                if (potentialReplacements == null) {
                    return GearModification.Result.errorInternal();
                } else {
                    VaultGearTierConfig.ModifierOutcome<?> replacement = potentialReplacements.getB().getRandom(random).orElse(null);
                    if (replacement == null) {
                        return GearModification.Result.errorInternal();
                    } else {
                        VaultGearModifier existing = potentialReplacements.getA();
                        VaultGearModifier newModifier = replacement.makeModifier(random);
                        VaultGearAttributeComparator comparator = existing.getAttribute().getAttributeComparator();
                        if (comparator != null && comparator.compare(existing.getValue(), newModifier.getValue()) == 0) {
                            return reForgeOutcomeOfRandomModifier(stack, worldGameTime, random);
                        } else {
                            data.getAllModifierAffixes().forEach(VaultGearModifier::resetGameTimeAdded);
                            existing.setValue(newModifier.getValue());
                            existing.setRolledTier(newModifier.getRolledTier());
                            existing.setGameTimeAdded(worldGameTime);
                            existing.clearCategories();
                            data.write(stack);
                            return GearModification.Result.makeSuccess();
                        }
                    }
                }
            }
        }
    }

    /**
     * @author iwolfking
     * @reason Add logs
     */
    @Overwrite
    public static GearModification.Result improveRandomModifier(ItemStack stack, long worldGameTime, Random random) {
        try {
            VaultGearData data = VaultGearData.read(stack);
            if (!data.isModifiable()) {
                return GearModification.Result.errorUnmodifiable();
            } else {
                List<Tuple<VaultGearModifier<?>, WeightedList<VaultGearTierConfig.ModifierOutcome<?>>>> modifierReplacements = getAvailableModifierConfigurationOutcomes(data, stack, true);
                if (modifierReplacements == null) {
                    return GearModification.Result.errorInternal();
                }
                modifierReplacements.removeIf(tpl -> {
                    VaultGearModifier<?> existing = tpl.getA();
                    if(existing.hasCategory(VaultGearModifier.AffixCategory.valueOf("UNUSUAL"))) {
                        return true;
                    }
                    VaultGearAttributeComparator comparator = existing.getAttribute().getAttributeComparator();
                    if (comparator == null) {
                        return true;
                    } else {
                        ConfigurableAttributeGenerator generator = existing.getAttribute().getGenerator();
                        (tpl.getB()).entrySet().removeIf(weightedOutcome -> {
                            VaultGearTierConfig.ModifierOutcome<?> outcome = weightedOutcome.getKey();
                            Object tierConfig = outcome.tier().getModifierConfiguration();
                            Object maxValue = generator.getMaximumValue(List.of(tierConfig)).orElse(null);
                            if (maxValue == null) {
                                return true;
                            } else {
                                return comparator.compare(maxValue, existing.getValue()) <= 0;
                            }
                        });
                        return (tpl.getB()).isEmpty() || (tpl.getB()).entrySet().stream().allMatch(weightedOutcome -> {
                            VaultGearTierConfig.ModifierOutcome<?> outcome = weightedOutcome.getKey();
                            Object tierConfig = outcome.tier().getModifierConfiguration();
                            Object minValue = generator.getMinimumValue(List.of(tierConfig)).orElse(null);
                            Object maxValue = generator.getMaximumValue(List.of(tierConfig)).orElse(null);
                            if (minValue != null && maxValue != null) {
                                return comparator.compare(minValue, existing.getValue()) == 0 && comparator.compare(maxValue, existing.getValue()) == 0;
                            } else {
                                return true;
                            }
                        });
                    }
                });
                if (modifierReplacements.isEmpty()) {
                    return GearModification.Result.makeActionError("all_max");
                } else {
                    Tuple<VaultGearModifier<?>, WeightedList<VaultGearTierConfig.ModifierOutcome<?>>> potentialReplacements = MiscUtils.getRandomEntry(modifierReplacements);
                    if (potentialReplacements == null) {
                        return GearModification.Result.errorInternal();
                    } else {
                        VaultGearTierConfig.ModifierOutcome<?> replacement = (potentialReplacements.getB()).getRandom(random).orElse(null);
                        if (replacement == null) {
                            return GearModification.Result.errorInternal();
                        } else {
                            VaultGearModifier existing = potentialReplacements.getA();
                            VaultGearAttributeComparator comparator = existing.getAttribute().getAttributeComparator();
                            if (comparator == null) {
                                return GearModification.Result.errorInternal();
                            } else {
                                VaultGearModifier newModifier;
                                int i = 0;
                                do {
                                    newModifier = replacement.makeModifier(random);
                                    i++;
                                    if(i > 100) {
                                        if(newModifier.getValue().getClass().isInstance(Integer.class)) {
                                            newModifier.setValue((Integer)newModifier.getValue() + 1);
                                        }
                                        else if(newModifier.getValue().getClass().isInstance(Float.class)) {
                                            newModifier.setValue((Float)newModifier.getValue() + 0.01F);
                                        }
                                        else if(newModifier.getValue().getClass().isInstance(Double.class)) {
                                            newModifier.setValue((Double)newModifier.getValue() + 0.01);
                                        }
                                        break;
                                    }
                                } while (comparator.compare(existing.getValue(), newModifier.getValue()) >= 0);

                                data.getAllModifierAffixes().forEach(VaultGearModifier::resetGameTimeAdded);
                                existing.setValue(newModifier.getValue());
                                existing.setRolledTier(newModifier.getRolledTier());
                                existing.setGameTimeAdded(worldGameTime);
                                existing.clearCategories();
                                data.write(stack);
                                return GearModification.Result.makeSuccess();
                            }
                        }

                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Shadow
    private static List<Tuple<VaultGearModifier<?>, WeightedList<VaultGearTierConfig.ModifierOutcome<?>>>> getAvailableModifierConfigurationOutcomes(VaultGearData data, ItemStack stack, boolean includeOnlyModifiableModifiers) {
        return null;
    }
}