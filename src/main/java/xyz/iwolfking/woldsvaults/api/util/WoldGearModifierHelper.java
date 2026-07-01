package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.VaultMod;
import iskallia.vault.config.gear.VaultEtchingConfig;
import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.gear.VaultGearModifierHelper;
import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.gear.VaultGearType;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.gear.modification.GearModification;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.item.gear.EtchingItem;
import iskallia.vault.util.MiscUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.VaultGearModifierHelperAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WoldGearModifierHelper {
    public static GearModification.Result addUnusualModifier(ItemStack stack, long worldGameTime, Random random) {
        VaultGearData data = VaultGearData.read(stack);
        VaultGearTierConfig cfg = VaultGearTierConfig.getConfig(stack).orElse(null);
        if (cfg == null) {
            VaultMod.LOGGER.error("Unknown VaultGear: " + stack);
            return GearModification.Result.errorUnmodifiable();
        } else if (!data.isModifiable()) {
            return GearModification.Result.errorUnmodifiable();
        } else {
            int itemLevel = data.getItemLevel();
            int prefixes = data.getFirstValue(ModGearAttributes.PREFIXES).orElse(0);
            int suffixes = data.getFirstValue(ModGearAttributes.SUFFIXES).orElse(0);
            prefixes -= data.getModifiers(VaultGearModifier.AffixType.PREFIX).size();
            suffixes -= data.getModifiers(VaultGearModifier.AffixType.SUFFIX).size();
            boolean hasUnusual = false;
            for (VaultGearModifier<?> modifier : data.getModifiers(VaultGearModifier.AffixType.PREFIX)) {
                hasUnusual = modifier.getCategories().contains(VaultGearModifier.AffixCategory.valueOf("UNUSUAL"));
                if (hasUnusual) {
                    break;
                }
            }

            if (hasUnusual) {
                return GearModification.Result.makeActionError("full");
            }

            for (VaultGearModifier<?> modifier : data.getModifiers(VaultGearModifier.AffixType.SUFFIX)) {
                hasUnusual = modifier.getCategories().contains(VaultGearModifier.AffixCategory.valueOf("UNUSUAL"));
                if (hasUnusual) {
                    break;
                }
            }

            if (hasUnusual) {
                return GearModification.Result.makeActionError("full");
            }

            if (prefixes <= 0 && suffixes <= 0) {
                return GearModification.Result.makeActionError("full");
            } else {
                List<VaultGearModifier.AffixType> types = new ArrayList<>();
                if (prefixes > 0) {
                    types.add(VaultGearModifier.AffixType.PREFIX);
                }

                if (suffixes > 0) {
                    types.add(VaultGearModifier.AffixType.SUFFIX);
                }


                VaultGearModifier.AffixType type = MiscUtils.getRandomEntry(types, random);
                if (type == null) {
                    return GearModification.Result.errorInternal();
                }

                VaultGearTierConfig.ModifierAffixTagGroup modifierAffixTagGroup = VaultGearTierConfig.ModifierAffixTagGroup.valueOf("UNUSUAL_PREFIX");


                if (type.equals(VaultGearModifier.AffixType.PREFIX)) {
                    return cfg.getRandomModifier(modifierAffixTagGroup, itemLevel, random, data.getExistingModifierGroups(VaultGearData.Type.EXPLICIT_MODIFIERS)).map(modifier -> {
                        data.getAllModifierAffixes().forEach(VaultGearModifier::resetGameTimeAdded);
                        modifier.setGameTimeAdded(worldGameTime);
                        modifier.addCategory(VaultGearModifier.AffixCategory.valueOf("UNUSUAL"));
                        data.addModifier(type, modifier);
                        data.write(stack);
                        return GearModification.Result.makeSuccess();
                    }).orElse(GearModification.Result.makeActionError("no_modifiers"));
                } else if (type.equals(VaultGearModifier.AffixType.SUFFIX)) {
                    return cfg.getRandomModifier(VaultGearTierConfig.ModifierAffixTagGroup.valueOf("UNUSUAL_SUFFIX"), itemLevel, random, data.getExistingModifierGroups(VaultGearData.Type.EXPLICIT_MODIFIERS)).map(modifier -> {
                        data.getAllModifierAffixes().forEach(VaultGearModifier::resetGameTimeAdded);
                        modifier.setGameTimeAdded(worldGameTime);
                        modifier.addCategory(VaultGearModifier.AffixCategory.valueOf("UNUSUAL"));
                        data.addModifier(type, modifier);
                        data.write(stack);
                        return GearModification.Result.makeSuccess();
                    }).orElse(GearModification.Result.makeActionError("no_modifiers"));
                }

                return GearModification.Result.errorInternal();

            }
        }
    }

    public static GearModification.Result removeRandomModifierAlways(ItemStack stack, Random random) {
        VaultGearData data = VaultGearData.read(stack);
        List<VaultGearModifier<?>> affixes = new ArrayList<>();
        affixes.addAll(data.getModifiers(VaultGearModifier.AffixType.PREFIX));
        affixes.addAll(data.getModifiers(VaultGearModifier.AffixType.SUFFIX));
        affixes.removeIf(modifier -> !modifier.hasNoCategoryMatching(VaultGearModifier.AffixCategory::cannotBeModifiedByArtisanFoci));
        if (affixes.isEmpty()) {
            return GearModification.Result.makeActionError("no_modifiers");
        } else {
            VaultGearModifier<?> randomMod = MiscUtils.getRandomEntry(affixes, random);
            data.removeModifier(randomMod);
            data.write(stack);
            return GearModification.Result.makeSuccess();
        }
    }

    public static GearModification.Result unfreezeAll(ItemStack stack) {
        VaultGearData data = VaultGearData.read(stack);
        List<VaultGearModifier<?>> affixes = new ArrayList<>();
        affixes.addAll(data.getModifiers(VaultGearModifier.AffixType.PREFIX));
        affixes.addAll(data.getModifiers(VaultGearModifier.AffixType.SUFFIX));
        affixes.removeIf(modifier -> !modifier.hasCategory(VaultGearModifier.AffixCategory.FROZEN));
        if (affixes.isEmpty()) {
            return GearModification.Result.makeActionError("no_modifiers");
        } else {
            for (VaultGearModifier<?> mod : affixes) {
                mod.removeCategory(VaultGearModifier.AffixCategory.FROZEN);
            }
            data.write(stack);
            return GearModification.Result.makeSuccess();
        }
    }

    public static GearModification.Result freezeAll(ItemStack stack) {
        VaultGearData data = VaultGearData.read(stack);
        List<VaultGearModifier<?>> affixes = new ArrayList<>();
        affixes.addAll(data.getModifiers(VaultGearModifier.AffixType.PREFIX));
        affixes.addAll(data.getModifiers(VaultGearModifier.AffixType.SUFFIX));
        affixes.removeIf(modifier -> modifier.hasCategory(VaultGearModifier.AffixCategory.FROZEN));
        if (affixes.isEmpty()) {
            return GearModification.Result.makeActionError("no_modifiers");
        } else {
            for (VaultGearModifier<?> mod : affixes) {
                mod.addCategory(VaultGearModifier.AffixCategory.FROZEN);
            }
            data.write(stack);
            return GearModification.Result.makeSuccess();
        }
    }

    public static GearModification.Result freezeGoodModifier(ItemStack stack) {
        VaultGearData data = VaultGearData.read(stack);
        List<VaultGearModifier<?>> affixes = new ArrayList<>();
        affixes.addAll(data.getModifiers(VaultGearModifier.AffixType.PREFIX));
        affixes.addAll(data.getModifiers(VaultGearModifier.AffixType.SUFFIX));
        if (affixes.stream().anyMatch((modifier) -> modifier.hasCategory(VaultGearModifier.AffixCategory.FROZEN))) {
            return GearModification.Result.makeActionError("frozen", new Component[0]);
        }
        affixes.removeIf(modifier -> modifier.hasCategory(VaultGearModifier.AffixCategory.FROZEN) || (!modifier.hasCategory(VaultGearModifier.AffixCategory.CORRUPTED) && !modifier.hasCategory(VaultGearModifier.AffixCategory.LEGENDARY) && !modifier.hasCategory(VaultGearModifier.AffixCategory.GREATER) && !modifier.hasCategory(VaultGearModifier.AffixCategory.valueOf("UNUSUAL"))));
        if (affixes.isEmpty()) {
            return GearModification.Result.makeActionError("no_modifiers");
        } else {
            for (VaultGearModifier<?> mod : affixes) {
                mod.addCategory(VaultGearModifier.AffixCategory.FROZEN);
                break;
            }
            data.write(stack);
            return GearModification.Result.makeSuccess();
        }
    }

    public static GearModification.Result addEtching(ItemStack gearStack, ItemStack etchingStack) {
        if (!gearStack.isEmpty() && !etchingStack.isEmpty()) {
            if (gearStack.getItem() instanceof VaultGearItem gear && etchingStack.getItem() instanceof EtchingItem) {
                VaultGearData gearData = VaultGearData.read(gearStack);
                if (gearData.hasAttribute(ModGearAttributes.ETCHING)) {
                    return GearModification.Result.errorInternal();
                } else if (gearData.isModifiable() && !gearData.getRarity().equals(VaultGearRarity.UNIQUE)) {
                    VaultGearData etchingData = VaultGearData.read(etchingStack);
                    ResourceLocation etchingId = etchingData.getFirstValue(ModGearAttributes.ETCHING).orElse(null);
                    if (etchingId == null) {
                        return GearModification.Result.errorInternal();
                    } else {
                        VaultEtchingConfig.EtchingEntry etchingConfig = ModConfigs.ETCHINGS.getEtchingConfig(etchingId);
                        if (etchingConfig == null) {
                            return GearModification.Result.errorInternal();
                        } else {
                            List<String> groups = etchingConfig.getTypeGroups();
                            if (!groups.isEmpty()) {
                                VaultGearType type = gear.getGearType(gearStack);
                                boolean allowed = groups.stream().anyMatch(g -> ModConfigs.ETCHINGS.getGroup(g).contains(type));
                                if (!allowed) {
                                    return GearModification.Result.errorInternal();
                                }
                            }

                            Random random = new Random();

                            VaultGearData outData = VaultGearData.read(gearStack);

                            int modifierFillAttempts = 0;

                            while(VaultGearModifierHelper.hasAnyOpenAffix(gearStack)) {
                                VaultGearModifierHelper.addNewModifier(gearStack, random.nextLong(), random);
                                modifierFillAttempts++;
                                if(modifierFillAttempts > 10) {
                                    break;
                                }
                            }

                            outData.setRarity(VaultGearRarity.UNIQUE);
                            outData.createOrReplaceAttributeValue(ModGearAttributes.ETCHING, etchingId);
                            etchingData.getModifiers(VaultGearModifier.AffixType.IMPLICIT)
                                    .forEach(modifier -> outData.addModifier(VaultGearModifier.AffixType.IMPLICIT, (VaultGearModifier<?>) modifier));
                            outData.write(gearStack);
                            String etchingName = etchingConfig.getName();
                            String gearName = gearStack.getHoverName().getString();
                            gearStack.setHoverName(new TextComponent(etchingName + " " + gearName));
                            return GearModification.Result.makeSuccess();
                        }
                    }
                } else {
                    return GearModification.Result.errorInternal();
                }
            } else {
                return GearModification.Result.errorInternal();
            }
        } else {
            return GearModification.Result.errorInternal();
        }
    }

    public static GearModification.Result reforgeModifiersForNewLevelForced(ItemStack stack, int newLevel, Random random, boolean takeRepairSlot) {
        VaultGearData data = VaultGearData.read(stack);
        if (data.getUsedRepairSlots() >= data.getRepairSlots() && takeRepairSlot) {
            return GearModification.Result.makeActionError("no_repair_slots");
        } else {
            newLevel = Math.min(100, newLevel);
            if (newLevel <= data.getItemLevel()) {
                return GearModification.Result.errorUnmodifiable();
            } else {
                VaultGearTierConfig cfg = VaultGearTierConfig.getConfig(stack).orElse(null);
                if (cfg == null) {
                    return GearModification.Result.errorUnmodifiable();
                } else {
                    data.setItemLevel(newLevel);
                    if (takeRepairSlot) {
                        data.setUsedRepairSlots(data.getUsedRepairSlots() + 1);
                    }

                    if(data.isImbued()) {
                        return GearModification.Result.errorUnmodifiable();
                    }

                    VaultGearModifierHelperAccessor.callReforgeBaseAttributesForNewLevel(data, cfg, newLevel, random);
                    VaultGearModifierHelperAccessor.callReforgeModifiersOfTypeForNewLevel(data, cfg, VaultGearModifier.AffixType.IMPLICIT, newLevel, random);
                    VaultGearModifierHelperAccessor.callReforgeModifiersOfTypeForNewLevel(data, cfg, VaultGearModifier.AffixType.PREFIX, newLevel, random);
                    VaultGearModifierHelperAccessor.callReforgeModifiersOfTypeForNewLevel(data, cfg, VaultGearModifier.AffixType.SUFFIX, newLevel, random);
                    data.write(stack);
                    return GearModification.Result.makeSuccess();
                }
            }
        }
    }

}
