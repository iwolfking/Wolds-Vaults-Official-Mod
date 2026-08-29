package xyz.iwolfking.woldsvaults.integration.mekanism.init;

import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.attribute.custom.effect.EffectGearAttribute;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.IdentifiableItem;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.item.MagnetItem;
import iskallia.vault.item.VaultModifierItem;
import iskallia.vault.item.gear.VaultArmorItem;
import iskallia.vault.item.gear.VaultCharmItem;
import mekanism.api.gear.ModuleData;
import mekanism.common.item.ItemModule;
import mekanism.common.registries.MekanismItems;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.api.data.WoldConstants;
import xyz.iwolfking.woldsvaults.api.util.ItemHelper;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

public class ModModuleToVaultGearModifications {
    private static final HashMap<ItemModule, ModuleModifier<?>> MODULE_TO_MODIFIER_MAP = new HashMap<>();

    public static void register(ItemModule moduleItem, ModuleModifier<?> modifier) {
        MODULE_TO_MODIFIER_MAP.put(moduleItem, modifier);
    }

    public static boolean supports(Item moduleItem) {
        return MODULE_TO_MODIFIER_MAP.containsKey(moduleItem);
    }

    public static ModuleModifier<?> getModification(Item moduleItem) {
        return MODULE_TO_MODIFIER_MAP.get(moduleItem);
    }

    public static List<ModuleData<?>> getModuleList(ItemStack stack) {
        if (!(stack.getItem() instanceof VaultGearItem)) {
            return List.of();
        }
        List<ModuleData<?>> moduleDataList = new ArrayList<>();
        VaultGearData data = VaultGearData.read(stack);

        for (VaultGearAttributeInstance<?> attributeInstance : data.getAllAttributes().toList()) {
            MODULE_TO_MODIFIER_MAP.forEach((item, modifier) -> {
                if (attributeInstance.getAttribute().equals(modifier.attribute())) {
                    Object instanceVal = attributeInstance.getValue();
                    Object modifierVal = modifier.value();

                    if (isMatchingValue(instanceVal, modifierVal)) {
                        moduleDataList.add(item.getModuleData());
                    }
                }
            });
        }

        return moduleDataList;
    }

    private static boolean isMatchingValue(Object val1, Object val2) {
        if (val1 instanceof EffectGearAttribute e1 && val2 instanceof EffectGearAttribute e2) {
            return e1.getEffect().equals(e2.getEffect());
        }

        return Objects.equals(val1, val2);
    }

    public static ItemStack removeModule(ItemStack stack, ModuleData<?> moduleData) {
        Item moduleItem = moduleData.getItemProvider().getItemStack().getItem();
        if(supports(moduleItem)) {
            ModuleModifier<?> moduleModifier = getModification(moduleItem);
            return moduleModifier.remove(stack);
        }

        return stack;
    }

    public static HashMap<ItemModule, ModuleModifier<?>> getRegisteredModifiers() {
        return MODULE_TO_MODIFIER_MAP;
    }


    public static List<ItemStack> getMatchingGearSamples(Predicate<ItemStack> itemFilter) {
        List<ItemStack> samples = new ArrayList<>();
        List<Item> checkItems = WoldConstants.ALL_VAULT_GEAR_ITEMS.get();

        for (Item item : checkItems) {
            ItemStack stack = new ItemStack(item);

            if(item instanceof IdentifiableItem identifiableItem && !(item instanceof VaultCharmItem)) {
                identifiableItem.instantIdentify(null, stack);
            }

            if (itemFilter.test(stack)) {
                samples.add(stack);
            }
        }

        return samples;
    }

    public record ModuleModifier<T>(T value,
            VaultGearAttribute<T> attribute,
            VaultGearModifier.AffixType affix,
            Predicate<ItemStack> itemsSupported) {

        public ItemStack apply(ItemStack gearStack) {
            if(!(gearStack.getItem() instanceof VaultGearItem)) {
                return gearStack;
            }

            ItemStack output = gearStack.copy();

            VaultGearData data = VaultGearData.read(output);
            data.addModifier(affix, new VaultGearModifier<>(attribute, value));
            data.write(output);
            return output;
        }

        public ItemStack remove(ItemStack gearStack) {
            if (!(gearStack.getItem() instanceof VaultGearItem)) {
                return gearStack;
            }

            ItemStack output = gearStack.copy();
            VaultGearData data = VaultGearData.read(output);

            List<VaultGearModifier<?>> modifiers = data.getModifiers(affix);
            VaultGearModifier<?> targetModifier = null;

            for (VaultGearModifier<?> modifier : modifiers) {
                if (modifier.getAttribute().equals(attribute) && isMatchingValue(modifier.getValue(), value)) {
                    targetModifier = modifier;
                    break;
                }
            }

            if (targetModifier != null) {
                data.removeModifier(targetModifier, true);
                data.write(output);
            }

            return output;
        }
    }

    public static void init() {
        register(MekanismItems.MODULE_RADIATION_SHIELDING.asItem(), new ModuleModifier<>(true, ModGearAttributes.RADIATION_IMMUNITY, VaultGearModifier.AffixType.IMPLICIT, (stack) -> stack.getItem() instanceof VaultArmorItem));
        register(MekanismItems.MODULE_ELECTROLYTIC_BREATHING.asItem(), new ModuleModifier<>(new EffectGearAttribute(MobEffects.WATER_BREATHING, 2), iskallia.vault.init.ModGearAttributes.EFFECT, VaultGearModifier.AffixType.IMPLICIT, (stack) -> stack.getItem() instanceof VaultArmorItem vaultArmorItem && vaultArmorItem.getEquipmentSlot(stack).equals(EquipmentSlot.HEAD)));
        register(MekanismItems.MODULE_VISION_ENHANCEMENT.asItem(), new ModuleModifier<>(new EffectGearAttribute(MobEffects.NIGHT_VISION, 2), iskallia.vault.init.ModGearAttributes.EFFECT, VaultGearModifier.AffixType.IMPLICIT, (stack) -> stack.getItem() instanceof VaultArmorItem vaultArmorItem && vaultArmorItem.getEquipmentSlot(stack).equals(EquipmentSlot.HEAD)));
        register(MekanismItems.MODULE_NUTRITIONAL_INJECTION.asItem(), new ModuleModifier<>(new EffectGearAttribute(MobEffects.SATURATION, 2), iskallia.vault.init.ModGearAttributes.EFFECT, VaultGearModifier.AffixType.IMPLICIT, (stack) -> stack.getItem() instanceof VaultArmorItem vaultArmorItem && vaultArmorItem.getEquipmentSlot(stack).equals(EquipmentSlot.HEAD)));
        register(MekanismItems.MODULE_MAGNETIC_ATTRACTION.asItem(), new ModuleModifier<>(10.0F, iskallia.vault.init.ModGearAttributes.RANGE, VaultGearModifier.AffixType.IMPLICIT, (stack) -> stack.getItem() instanceof VaultArmorItem vaultArmorItem && vaultArmorItem.getEquipmentSlot(stack).equals(EquipmentSlot.FEET)));
        register(MekanismItems.MODULE_TELEPORTATION.asItem(), new ModuleModifier<>(true, ModGearAttributes.MAGNET_ENDERGIZED, VaultGearModifier.AffixType.IMPLICIT, (stack) -> stack.getItem() instanceof MagnetItem));
    }
}
