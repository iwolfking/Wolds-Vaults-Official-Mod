package xyz.iwolfking.woldsvaults.recipes.gear;

import iskallia.vault.recipe.anvil.AnvilContext;
import iskallia.vault.recipe.anvil.VanillaAnvilRecipe;
import mezz.jei.api.registration.IRecipeRegistration;

import java.util.List;

public class SetWeaponTypeFocusRecipe  extends VanillaAnvilRecipe {
    @Override
    public boolean onSimpleCraft(AnvilContext context) {
        return false;
//
//        ItemStack primary = context.getInput()[0];
//        ItemStack secondary = context.getInput()[1];
//        if (primary.getItem() instanceof VaultGearItem gearItem && secondary.getItem() instanceof WeaponTypeSettingItem weaponTypeSettingItem) {
//            ItemStack output = primary.copy();
//            VaultGearData gear = VaultGearData.read(output);
//
//
//            if(!gearItem.isIntendedForSlot(primary, EquipmentSlot.MAINHAND)) {
//                return false;
//            }
//
//            if(!gear.isModifiable()) {
//                return false;
//            }
//
//            String weaponType = weaponTypeSettingItem.getModifierTagString(secondary);
//
//            List<String> allowedValues = ModConfigs.WEAPON_TYPES.WEAPON_TYPES_MAP.get(weaponType).ALLOWED_TYPES;
//
//            if(!allowedValues.contains(StringUtils.convertToTitleCase(primary.getItem().getRegistryName().getPath()))) {
//                return false;
//            }
//
//            gear.createOrReplaceAttributeValue(ModGearAttributes.WEAPON_TYPE, weaponType);
//
//            gear.write(output);
//            context.setOutput(output);
//
//            context.onTake(context.getTake().append(() -> {
//                context.getInput()[0].shrink(1);
//                context.getInput()[1].shrink(1);
//            }));
//            return true;
//        } else {
//            return false;
//        }
    }

    @Override
    public void onRegisterJEI(IRecipeRegistration iRecipeRegistration) {

    }
}
