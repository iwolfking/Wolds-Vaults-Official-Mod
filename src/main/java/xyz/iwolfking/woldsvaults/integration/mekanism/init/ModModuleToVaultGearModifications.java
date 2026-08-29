package xyz.iwolfking.woldsvaults.integration.mekanism.init;

import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.IdentifiableItem;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.item.gear.VaultArmorItem;
import iskallia.vault.item.gear.VaultCharmItem;
import mekanism.api.gear.ModuleData;
import mekanism.common.item.ItemModule;
import mekanism.common.registries.MekanismItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.api.data.WoldConstants;
import xyz.iwolfking.woldsvaults.api.util.ItemHelper;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        if(!(stack.getItem() instanceof VaultGearItem)) {
            return List.of();
        }
        List<ModuleData<?>> moduleDataList = new ArrayList<>();
        VaultGearData data = VaultGearData.read(stack);
        for(VaultGearAttributeInstance<?> attributeInstance : data.getAllAttributes().toList()) {
            MODULE_TO_MODIFIER_MAP.forEach((item, modifier) -> {
                if(attributeInstance.getAttribute().equals(modifier.attribute)) {
                    moduleDataList.add(item.getModuleData());
                }
            });
        }

        return moduleDataList;
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
            if(!(gearStack.getItem() instanceof VaultGearItem)) {
                return gearStack;
            }

            ItemStack output = gearStack.copy();

            VaultGearData data = VaultGearData.read(output);
            VaultGearAttributeInstance<?> removed = data.removeAttribute(attribute, VaultGearData.Type.ALL);

            if (removed != null) {
                data.write(output);
            }

            data.write(output);
            return output;
        }
    }

    public static void init() {
        register(MekanismItems.MODULE_RADIATION_SHIELDING.asItem(), new ModuleModifier<>(true, ModGearAttributes.RADIATION_IMMUNITY, VaultGearModifier.AffixType.IMPLICIT, (stack) -> stack.getItem() instanceof VaultArmorItem));
    }
}
