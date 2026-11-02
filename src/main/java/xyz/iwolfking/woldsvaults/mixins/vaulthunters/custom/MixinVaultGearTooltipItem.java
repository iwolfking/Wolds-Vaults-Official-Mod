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
import iskallia.vault.gear.VaultGearState;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TranslatableComponent;
import xyz.iwolfking.woldsvaults.api.gear.sets.GearSetHelper;
import xyz.iwolfking.woldsvaults.config.GearSetConfig;
import java.util.Optional;

import java.util.List;

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
        data.getAttributes(ModGearAttributes.LIVING)
            .filter(VaultGearAttributeInstance::getValue)
            .forEach(attr -> attr.getDisplay(data, VaultGearModifier.AffixType.IMPLICIT, stack, displayDetail).ifPresent(tooltip::add));
        data.getAttributes(xyz.iwolfking.woldsvaults.init.ModGearAttributes.DIVINE).filter(VaultGearAttributeInstance::getValue).forEach(booleanVaultGearAttributeInstance -> booleanVaultGearAttributeInstance.getDisplay(data, VaultGearModifier.AffixType.IMPLICIT, stack,displayDetail).ifPresent(tooltip::add));
        data.getAttributes(xyz.iwolfking.woldsvaults.init.ModGearAttributes.WEAPON_TYPE).forEach(weaponTypeAttributeInstance -> weaponTypeAttributeInstance.getDisplay(data, VaultGearModifier.AffixType.IMPLICIT, stack, displayDetail).ifPresent(tooltip::add));
        if (tooltip.size() > txtLength && displayDetail) {
            tooltip.add(TextComponent.EMPTY);
        }

    }

    /**
     * @author PoorMansPhysicist
     * @reason set pieces read "Set" or "Ancient Set" in the set colour instead of "Unique"
     */
    @Overwrite
    default void addTooltipRarity(VaultGearData data, ItemStack stack, List<Component> tooltip, VaultGearState state) {
        switch (state) {
            case UNIDENTIFIED -> data.getFirstValue(ModGearAttributes.GEAR_ROLL_TYPE)
                    .flatMap(iskallia.vault.init.ModConfigs.VAULT_GEAR_TYPE_CONFIG::getRollPool)
                    .ifPresent(pool -> tooltip.add(new TranslatableComponent("tooltip.the_vault.roll")
                            .append(new TextComponent(pool.getName()).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(pool.getColor()))))));
            case IDENTIFIED -> {
                Optional<GearSetConfig.GearSet> set = GearSetHelper.setOf(stack).flatMap(GearSetHelper.config()::getSet);
                if (set.isPresent()) {
                    boolean ancient = data.getFirstValue(xyz.iwolfking.woldsvaults.init.ModGearAttributes.ANCIENT_UNIQUE).orElse(false);
                    int color = ancient ? set.get().ancientColorRgb() : set.get().colorRgb();
                    tooltip.add(new TranslatableComponent("tooltip.the_vault.rarity")
                            .append(new TextComponent(ancient ? "Ancient Set" : "Set").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color)))));
                } else {
                    tooltip.add(new TranslatableComponent("tooltip.the_vault.rarity").append(data.getRarity().getDisplayName()));
                }
            }
        }
    }
}
