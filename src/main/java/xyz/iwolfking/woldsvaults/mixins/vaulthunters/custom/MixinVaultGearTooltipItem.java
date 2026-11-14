package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.client.data.ClientPatreonGearStats;
import iskallia.vault.config.UniqueGearConfig;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.VaultGearType;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.gear.tooltip.GearTooltip;
import iskallia.vault.gear.tooltip.VaultGearTooltipItem;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.gear.stats.PatreonGearStatHelper;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.util.TextUtil;
import iskallia.vault.www.patreon.PatreonManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import xyz.iwolfking.woldsvaults.api.gear.sets.GearSetHelper;
import xyz.iwolfking.woldsvaults.config.GearSetConfig;

import java.util.*;

@Mixin(value = VaultGearTooltipItem.class, remap = false)
public interface MixinVaultGearTooltipItem{


    @Shadow
    void addTooltipItemLevel(VaultGearData data, ItemStack stack, List<Component> tooltip, VaultGearState state);

    @Shadow
    void addTooltipCraftingPotential(VaultGearData data, ItemStack stack, List<Component> tooltip, VaultGearState state);

    @Shadow
    void addTooltipToolCapacity(VaultGearData data, ItemStack stack, List<Component> tooltip, VaultGearState state);

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
    void addTooltipAffixGroup(VaultGearData data, VaultGearModifier.AffixType type, ItemStack stack, List<Component> tooltip, boolean displayDetails);

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
                    .flatMap(ModConfigs.VAULT_GEAR_TYPE_CONFIG::getRollPool)
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

    /**
     * @author iwolfking
     * @reason Why must mixin fail me so
     */
    @Overwrite
    default List<Component> createTooltip(ItemStack stack, GearTooltip flag) {
        if (!ModConfigs.isInitialized()) {
            return Collections.emptyList();
        } else {
            boolean shiftDown = Screen.hasShiftDown();
            VaultGearData data = VaultGearData.read(stack);
            List<Component> tooltip = new ArrayList<>();
            VaultGearState state = data.getState();
            if (shiftDown && data.getState() == VaultGearState.IDENTIFIED && data.hasAttribute(ModGearAttributes.UNIQUE_ITEM_KEY)) {
                Optional<ResourceLocation> var10000 = data.getFirstValue(ModGearAttributes.UNIQUE_ITEM_KEY);
                UniqueGearConfig var10001 = ModConfigs.UNIQUE_GEAR;
                Objects.requireNonNull(var10001);
                var10000.flatMap(var10001::getEntry).map(UniqueGearConfig.Entry::getLore).filter((lore) -> !lore.isEmpty()).map(TextUtil::applyColorTags).ifPresent((mutableComponent) -> tooltip.add(mutableComponent.withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC)));
            }

            this.addTooltipItemLevel(data, stack, tooltip, state);
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
                Item var10 = stack.getItem();
                if (var10 instanceof VaultGearItem gearItem) {
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
                this.addTooltipImportantBaseAttributes(data, stack, tooltip, flag.displayModifierDetail());
                List<VaultGearModifier<?>> implicits = data.getModifiers(VaultGearModifier.AffixType.IMPLICIT);
                if (!implicits.isEmpty()) {
                    this.addTooltipAffixGroup(data, VaultGearModifier.AffixType.IMPLICIT, stack, tooltip, flag.displayModifierDetail());
                    tooltip.add(TextComponent.EMPTY);
                }

                int maxPrefixes = data.getFirstValue(ModGearAttributes.PREFIXES).orElse(0);
                List<VaultGearModifier<?>> prefixes = data.getModifiers(VaultGearModifier.AffixType.PREFIX);
                int maxSuffixes = data.getFirstValue(ModGearAttributes.SUFFIXES).orElse(0);
                List<VaultGearModifier<?>> suffixes = data.getModifiers(VaultGearModifier.AffixType.SUFFIX);
                if (maxPrefixes > 0 || !prefixes.isEmpty()) {
                    this.addTooltipAffixGroup(data, VaultGearModifier.AffixType.PREFIX, stack, tooltip, flag.displayModifierDetail());
                    if (flag.displayModifierDetail() && (maxSuffixes > 0 || !suffixes.isEmpty())) {
                        tooltip.add(TextComponent.EMPTY);
                    }
                }

                if (maxSuffixes > 0 || !suffixes.isEmpty()) {
                    this.addTooltipAffixGroup(data, VaultGearModifier.AffixType.SUFFIX, stack, tooltip, flag.displayModifierDetail());
                    if (!data.isModifiable()) {
                        tooltip.add(TextComponent.EMPTY);
                    }
                }
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

            if (data.isSealed()) {
                MutableComponent ct = (new TextComponent("")).append((new TextComponent("Sealed")).setStyle(Style.EMPTY.withColor(12779386)));
                if (flag.displayModifierDetail()) {
                    ct.append((new TextComponent(" (Item locked and final)")).withStyle(ChatFormatting.GRAY));
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


            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null && PatreonManager.getInstance().isUserPatreon(minecraft.player.getUUID())) {
                Item itemType = stack.getItem();
                if (itemType instanceof VaultGearItem gearItem) {
                    VaultGearType type = gearItem.getGearType(stack);
                    if (type != VaultGearType.SWORD && type != VaultGearType.AXE) {
                        if (PatreonGearStatHelper.isTrackableArmor(stack)) {
                            double damageTaken = ClientPatreonGearStats.getDamageTaken(stack);
                            if (damageTaken > (double) 0.0F) {
                                String formatted = String.format(Locale.US, "%.1f", damageTaken);
                                tooltip.add((new TextComponent("Damage Taken: ")).withStyle(Style.EMPTY.withColor(ModConfigs.COLORS.getColor("damage"))).append((new TextComponent(formatted)).withStyle(ChatFormatting.WHITE)));
                            }
                        }
                    } else {
                        int kills = ClientPatreonGearStats.getKills(stack);
                        if (kills > 0) {
                            tooltip.add((new TextComponent("Killed ")).withStyle(Style.EMPTY.withColor(ModConfigs.COLORS.getColor("damage"))).append((new TextComponent(String.valueOf(kills))).withStyle(ChatFormatting.WHITE)).append(" mobs"));
                        }
                    }
                }
            }

            return tooltip;
        }
    }
}
