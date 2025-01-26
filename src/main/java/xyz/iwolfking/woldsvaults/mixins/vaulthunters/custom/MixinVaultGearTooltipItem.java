package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.tooltip.VaultGearTooltipItem;
import iskallia.vault.init.ModGearAttributes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.compat.bettercombat.NoBetterCombatTester;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Mixin(value = VaultGearTooltipItem.class, remap = false)
public interface MixinVaultGearTooltipItem{
    /**
     * @author iwolfking
     * @reason Add Weapon type to display as a base attribute
     */
    @Overwrite
    default void addTooltipImportantBaseAttributes(VaultGearData data, ItemStack stack, List<Component> tooltip, boolean displayDetail) {
        int txtLength = tooltip.size();
        data.getAttributes(ModGearAttributes.DURABILITY)
                .filter(inst -> inst.getValue() > 0)
                .forEach(attr -> attr.getDisplay(data, VaultGearModifier.AffixType.IMPLICIT, stack, displayDetail).ifPresent(tooltip::add));
        data.getAttributes(ModGearAttributes.SOULBOUND)
                .filter(VaultGearAttributeInstance::getValue)
                .forEach(attr -> attr.getDisplay(data, VaultGearModifier.AffixType.IMPLICIT, stack, displayDetail).ifPresent(tooltip::add));
        if(WoldsVaults.BETTER_COMBAT_PRESENT || WoldsVaultsConfig.COMMON.weaponsShouldntBeBetter.get()) {
            data.getAttributes(xyz.iwolfking.woldsvaults.init.ModGearAttributes.WEAPON_TYPE)
                    .filter(Objects::nonNull)
                    .forEach(attr -> attr.getDisplay(data, VaultGearModifier.AffixType.IMPLICIT, stack, displayDetail).ifPresent(tooltip::add));
        }
        if (tooltip.size() > txtLength && displayDetail) {
            tooltip.add(TextComponent.EMPTY);
        }

    }
}
