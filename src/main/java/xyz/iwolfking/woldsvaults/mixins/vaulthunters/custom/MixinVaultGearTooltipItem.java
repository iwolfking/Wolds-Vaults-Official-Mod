package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.mojang.blaze3d.platform.InputConstants;
import iskallia.vault.client.data.ClientPatreonGearStats;
import iskallia.vault.config.UniqueGearConfig;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.VaultGearType;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.gear.stats.PatreonGearStatHelper;
import iskallia.vault.gear.tooltip.*;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.util.TextUtil;
import iskallia.vault.www.patreon.PatreonManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.*;

@Mixin(value = VaultGearTooltipItem.class, remap = false)
public interface MixinVaultGearTooltipItem{


    @Shadow
    void addTooltipCraftingPotential(VaultGearData data, ItemStack stack, List<Component> tooltip, VaultGearState state);

    @Shadow
    void addTooltipToolCapacity(VaultGearData data, ItemStack stack, List<Component> tooltip, VaultGearState state);

    @Shadow
    void addTooltipRarity(VaultGearData data, ItemStack stack, List<Component> tooltip, VaultGearState state);

    @Shadow
    void addSlotTooltip(List<Component> tooltip, EquipmentSlot slot);

    @Shadow
    void addRepairTooltip(List<Component> tooltip, int usedRepairs, int totalRepairs);

    @Shadow
    void addTooltipDurability(List<Component> tooltip, ItemStack stack);

    @Shadow
    void addTooltipGearModel(VaultGearData data, ItemStack stack, List<Component> tooltip);

    @Shadow
    void addExtraTooltips(VaultGearData data, ItemStack stack, List<Component> tooltip, GearTooltip flag);

    @Shadow
    Component applyGearTooltipColour(Component component);

    @Shadow
    void addTooltipItemLevel(VaultGearData data, ItemStack stack, List<Component> tooltip, VaultGearState state, GearTooltipViewer viewer);

    @Shadow
    void addTooltipAffixGroup(VaultGearData data, VaultGearModifier.AffixType type, ItemStack stack, List<Component> tooltip, boolean displayDetails, GearTooltipViewer viewer);

    @Shadow
    void addTooltipPatreonGearStats(ItemStack stack, List<Component> tooltip);

    @Shadow
    void addTooltipEtching(ItemStack stack, List<Component> tooltip, VaultGearData data, GearTooltipViewer viewer);

    /**
     * @author
     * @reason
     */
    @Overwrite
    default void addTooltipImportantBaseAttributes(VaultGearData data, ItemStack stack, List<Component> tooltip, boolean displayDetail, GearTooltipViewer viewer) {
        int txtLength = tooltip.size();
        if (!VaultGearItem.of(stack).isImmortal(stack)) {
            data.getAttributes(ModGearAttributes.DURABILITY).filter((inst) -> (Integer)inst.getValue() > 0).forEach((attr) -> {
                Optional<Component> var10000 = attr.getDisplay(data, VaultGearModifier.AffixType.IMPLICIT, stack, displayDetail).map((display) -> (Component)(viewer.liveClientView() ? this.applyGearTooltipColour(display) : display));
                Objects.requireNonNull(tooltip);
                var10000.ifPresent(tooltip::add);
            });
        }

        data.getAttributes(ModGearAttributes.SOULBOUND).filter(VaultGearAttributeInstance::getValue).forEach((attr) -> {
            Optional<Component> var10000 = attr.getDisplay(data, VaultGearModifier.AffixType.IMPLICIT, stack, displayDetail).map((display) -> (Component)(viewer.liveClientView() ? this.applyGearTooltipColour(display) : display));
            Objects.requireNonNull(tooltip);
            var10000.ifPresent(tooltip::add);
        });
        data.getAttributes(ModGearAttributes.LIVING).filter(VaultGearAttributeInstance::getValue).forEach((attr) -> {
            Optional<Component> var10000 = attr.getDisplay(data, VaultGearModifier.AffixType.IMPLICIT, stack, displayDetail).map((display) -> (Component)(viewer.liveClientView() ? this.applyGearTooltipColour(display) : display));
            Objects.requireNonNull(tooltip);
            var10000.ifPresent(tooltip::add);
        });
        data.getAttributes(ModGearAttributes.NON_RECYCLABLE).filter(VaultGearAttributeInstance::getValue).forEach((attr) -> {
            Optional<Component> var10000 = attr.getDisplay(data, VaultGearModifier.AffixType.IMPLICIT, stack, displayDetail).map((display) -> (Component)(viewer.liveClientView() ? this.applyGearTooltipColour(display) : display));
            Objects.requireNonNull(tooltip);
            var10000.ifPresent(tooltip::add);
        });
        data.getAttributes(xyz.iwolfking.woldsvaults.init.ModGearAttributes.DIVINE).filter(VaultGearAttributeInstance::getValue).forEach(booleanVaultGearAttributeInstance -> booleanVaultGearAttributeInstance.getDisplay(data, VaultGearModifier.AffixType.IMPLICIT, stack,displayDetail).ifPresent(tooltip::add));
        if (tooltip.size() > txtLength && displayDetail) {
            tooltip.add(TextComponent.EMPTY);
        }

    }



    /**
     * @author iwolfking
     * @reason
     */
    @Overwrite
    default List<Component> createTooltip(ItemStack stack, GearTooltip flag, GearTooltipViewer viewer) {
        if (!ModConfigs.isInitialized()) {
            return Collections.emptyList();
        } else {
            boolean shiftDown = viewer.shiftDown();
            VaultGearData data = VaultGearData.read(stack);
            List<Component> tooltip = new ArrayList();
            VaultGearState state = data.getState();
            if (shiftDown && data.getState() == VaultGearState.IDENTIFIED && data.hasAttribute(ModGearAttributes.UNIQUE_ITEM_KEY)) {
                Optional<ResourceLocation> var10000 = data.getFirstValue(ModGearAttributes.UNIQUE_ITEM_KEY);
                UniqueGearConfig var10001 = ModConfigs.UNIQUE_GEAR;
                Objects.requireNonNull(var10001);
                var10000.flatMap(var10001::getEntry).map(UniqueGearConfig.Entry::getLore).filter((lore) -> !lore.isEmpty()).map(TextUtil::applyColorTags).ifPresent((mutableComponent) -> tooltip.add(mutableComponent.withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC)));
            }

            this.addTooltipItemLevel(data, stack, tooltip, state, viewer);
            if (flag.displayCraftingDetail()) {
                this.addTooltipCraftingPotential(data, stack, tooltip, state);
                this.addTooltipToolCapacity(data, stack, tooltip, state);
            }

            if (flag.displayBase()) {
                data.getFirstValue(ModGearAttributes.CRAFTED_BY).ifPresent((crafter) -> tooltip.add((new TranslatableComponent("tooltip.the_vault.crafted_by")).append((new TextComponent(crafter)).setStyle(Style.EMPTY.withColor(16770048)))));
            }

            if (shiftDown || state == VaultGearState.UNIDENTIFIED) {
                this.addTooltipRarity(data, stack, tooltip, state);
            }

            if (state == VaultGearState.IDENTIFIED) {
                Item var9 = stack.getItem();
                if (var9 instanceof VaultGearItem) {
                    VaultGearItem gearItem = (VaultGearItem) var9;
                    EquipmentSlot slot = gearItem.getGearType(stack).getEquipmentSlot();
                    if (slot != null) {
                        this.addSlotTooltip(tooltip, slot);
                    }
                }

                int usedRepairs = data.getUsedRepairSlots();
                int totalRepairs = data.getRepairSlots();
                this.addRepairTooltip(tooltip, usedRepairs, totalRepairs);
                this.addTooltipDurability(tooltip, stack);
                if (shiftDown && flag.displayBase()) {
                    this.addTooltipGearModel(data, stack, tooltip);
                }

                this.addExtraTooltips(data, stack, tooltip, flag);
                this.addTooltipImportantBaseAttributes(data, stack, tooltip, flag.displayModifierDetail(), viewer);
                List<VaultGearModifier<?>> implicits = data.getModifiers(VaultGearModifier.AffixType.IMPLICIT);
                if (!implicits.isEmpty()) {
                    this.addTooltipAffixGroup(data, VaultGearModifier.AffixType.IMPLICIT, stack, tooltip, flag.displayModifierDetail(), viewer);
                    tooltip.add(TextComponent.EMPTY);
                }

                int maxPrefixes = (Integer) data.getFirstValue(ModGearAttributes.PREFIXES).orElse(0);
                List<VaultGearModifier<?>> prefixes = data.getModifiers(VaultGearModifier.AffixType.PREFIX);
                int maxSuffixes = (Integer) data.getFirstValue(ModGearAttributes.SUFFIXES).orElse(0);
                List<VaultGearModifier<?>> suffixes = data.getModifiers(VaultGearModifier.AffixType.SUFFIX);
                if (maxPrefixes > 0 || !prefixes.isEmpty()) {
                    this.addTooltipAffixGroup(data, VaultGearModifier.AffixType.PREFIX, stack, tooltip, flag.displayModifierDetail(), viewer);
                    if (flag.displayModifierDetail() && (maxSuffixes > 0 || !suffixes.isEmpty())) {
                        tooltip.add(TextComponent.EMPTY);
                    }
                }

                if (maxSuffixes > 0 || !suffixes.isEmpty()) {
                    this.addTooltipAffixGroup(data, VaultGearModifier.AffixType.SUFFIX, stack, tooltip, flag.displayModifierDetail(), viewer);
                    if (!data.isModifiable()) {
                        tooltip.add(TextComponent.EMPTY);
                    }
                }
            }

            if (state == VaultGearState.IDENTIFIED) {
                DripTooltipLines.add(stack, tooltip, viewer);
                SealDealTooltipLines.add(stack, tooltip);
            }

            //Added
            data.getAttributes(xyz.iwolfking.woldsvaults.init.ModGearAttributes.WEAPON_TYPE).forEach(weaponTypeAttributeInstance -> weaponTypeAttributeInstance.getDisplay(data, VaultGearModifier.AffixType.IMPLICIT, stack, flag.displayModifierDetail()).map(tooltip::add));
            //End Added

            if (data.isImbued()) {
                MutableComponent ct = (new TextComponent("")).append((new TextComponent("Imbued")).setStyle(Style.EMPTY.withColor(11018748)));
                if (flag.displayModifierDetail()) {
                    ct.append((new TextComponent(" (Can't be modified further in Artisan Station)")).withStyle(ChatFormatting.GRAY));
                }

                tooltip.add(ct);
            }

            if (data.hasAttribute(ModGearAttributes.LEGENDARY_PENDING)) {
                tooltip.add((new TextComponent("One affix turns Legendary")).setStyle(Style.EMPTY.withColor(15853364)));
            }

            if (data.isSealed()) {
                MutableComponent ct = (new TextComponent("")).append((new TextComponent("Sealed")).setStyle(Style.EMPTY.withColor(12779386)));
                if (flag.displayModifierDetail()) {
                    ct.append((new TextComponent(" (Affixes can no longer be changed)")).withStyle(ChatFormatting.GRAY));
                }

                tooltip.add(ct);
            }

            if (data.isCorrupted()) {
                MutableComponent ct = (new TextComponent("")).append((new TranslatableComponent("tooltip.the_vault.corrupted")).setStyle(Style.EMPTY.withColor(7864324)));
                if (flag.displayModifierDetail()) {
                    ct.append((new TextComponent(" (Cannot be modified)")).withStyle(ChatFormatting.GRAY));
                }

                tooltip.add(ct);
            }

            if (viewer.liveClientView()) {
                this.addTooltipPatreonGearStats(stack, tooltip);
            }

            if (state == VaultGearState.IDENTIFIED) {
                this.addTooltipEtching(stack, tooltip, data, viewer);
            }

            return tooltip;
        }
    };
}
