package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.VaultMod;
import iskallia.vault.config.UniqueGearConfig;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.gear.VaultGearModifierHelper;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.data.AttributeGearData;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.gear.modification.GearModification;
import iskallia.vault.init.ModConfigs;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;
import xyz.iwolfking.woldsvaults.items.gear.VaultMapItem;
import xyz.iwolfking.woldsvaults.models.Maps;

import java.util.List;
import java.util.Random;

@Mixin(value = VaultGearModifierHelper.class, remap = false)
public class MixinCorruptGearModification {
    /**
     * @author iwolfking
     * @reason Add Divine attribute
     */
    @Overwrite
    public static GearModification.Result setGearCorrupted(ItemStack stack) {
        VaultGearData data = VaultGearData.read(stack);
        if (!data.isModifiable()) {
            return GearModification.Result.errorUnmodifiable();
        }

        if(stack.getItem() instanceof VaultMapItem) {
            if(JavaRandom.ofNanoTime().nextFloat() <= 0.25F) {
                    VaultGearModifier<?> objModifier = null;
                    VaultGearModifier<?> themeModifier = null;
                    for(VaultGearModifier<?> mod : data.getModifiers(VaultGearModifier.AffixType.IMPLICIT)) {
                        if(mod.getAttribute().equals(ModGearAttributes.OBJECTIVE)) {
                            objModifier = mod;
                        }
                        else if(mod.getAttribute().equals(ModGearAttributes.THEME_POOL)) {
                            themeModifier = mod;
                        }
                    }

                    if(objModifier != null) {
                        data.removeModifier(objModifier);
                    }

                    if(themeModifier != null) {
                        data.removeModifier(themeModifier);
                    }
                    data.addModifier(VaultGearModifier.AffixType.IMPLICIT, new VaultGearModifier<String>(ModGearAttributes.OBJECTIVE, "corrupted"));
                    data.addModifier(VaultGearModifier.AffixType.IMPLICIT, new VaultGearModifier<String>(ModGearAttributes.THEME_POOL, "the_vault:corrupted"));
                    data.createOrReplaceAttributeValue(ModGearAttributes.DIVINE, false);
                    data.createOrReplaceAttributeValue(iskallia.vault.init.ModGearAttributes.IS_CORRUPTED, true);
                    data.createOrReplaceAttributeValue(iskallia.vault.init.ModGearAttributes.GEAR_MODEL, Maps.CORRUPTED_MAP.getId());
                    data.write(stack);
                    return GearModification.Result.makeSuccess();
            }
        }

        if(data.hasAttribute(ModGearAttributes.DIVINE)) {
            Random random = new Random();
            if(random.nextFloat() <= 0.75F) {
                data.createOrReplaceAttributeValue(ModGearAttributes.DIVINE, false);
                data.write(stack);
            }

            return GearModification.Result.makeSuccess();
        }

        data.createOrReplaceAttributeValue(iskallia.vault.init.ModGearAttributes.IS_CORRUPTED, true);
        data.write(stack);
        return GearModification.Result.makeSuccess();
    }
}
