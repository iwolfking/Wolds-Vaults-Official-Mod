package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.gear.VaultGearModifierHelper;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.attribute.config.ConfigurableAttributeGenerator;
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

    @Shadow
    private static List<Tuple<VaultGearModifier<?>, WeightedList<VaultGearTierConfig.ModifierOutcome<?>>>> getAvailableModifierConfigurationOutcomes(VaultGearData data, ItemStack stack, boolean includeOnlyModifiableModifiers) {
        return null;
    }
}