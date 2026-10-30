package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.block.entity.ImbuementAltarTileEntity;
import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.data.VaultGearData;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(value = ImbuementAltarTileEntity.class,remap = false)
public class MixinImbuementAltarTileEntity {
    @Shadow
    public int time;

    @Redirect(
            method = "generateOptions",
            at = @At(
                    value = "INVOKE",
                    target = "Liskallia/vault/gear/data/VaultGearData;getModifiers(Liskallia/vault/gear/attribute/VaultGearModifier$AffixType;)Ljava/util/List;"
            )
    )
    private List<VaultGearModifier<?>> filterMaxedModifiersOnFetch(VaultGearData instance, VaultGearModifier.AffixType type, @Local(argsOnly = true) ItemStack stack) {
        List<VaultGearModifier<?>> originalModifiers = instance.getModifiers(type);

        VaultGearTierConfig tierConfig = VaultGearTierConfig.getConfig(stack).orElse(null);
        if (tierConfig == null) {
            return originalModifiers;
        }

        int itemLevel = instance.getItemLevel();

        return originalModifiers.stream()
                .filter(modifier -> vaultAltar$hasHigherTier(tierConfig, modifier, itemLevel))
                .collect(Collectors.toList());
    }

    @Unique
    private boolean vaultAltar$hasHigherTier(VaultGearTierConfig tierConfig, VaultGearModifier<?> modifier, int level) {
        VaultGearTierConfig.ModifierTierGroup tierGroup = tierConfig.getTierGroup(modifier.getModifierIdentifier());
        if (tierGroup == null || tierGroup.isEmpty()) {
            return false;
        }

        VaultGearTierConfig.ModifierTier<?> highestPossibleTier = tierGroup.getHighestForLevel(level);
        if (highestPossibleTier == null) {
            return false;
        }

        int currentTier = modifier.getRolledTier();

        if (currentTier >= (highestPossibleTier.getModifierTier() + 1)) {
            return false;
        }

        return currentTier < tierGroup.size() - 1;
    }
}
