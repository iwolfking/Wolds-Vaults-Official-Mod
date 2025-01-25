package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.enum_extension;

import iskallia.vault.gear.VaultGearClassification;
import iskallia.vault.gear.VaultGearRarity;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Invoker;
import xyz.iwolfking.woldsvaults.api.helper.VaultGearClassificationHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;

@Mixin(value = VaultGearClassification.class, remap = false)
public abstract class MixinVaultGearClassificationEnum {
    @Shadow
    @Final
    @Mutable @SuppressWarnings("target")
    private static VaultGearClassification [] $VALUES;

    @Shadow public abstract int getModifierCount(VaultGearRarity rarity);

    private static final VaultGearClassification MAP = enumExpansion$addVariant("MAP", VaultGearClassificationHelper::getMapModifierCount);


    @Invoker("<init>")
    public static VaultGearClassification enumExpansion$invokeInit(String internalName, int internalId, Function<VaultGearRarity, Integer> modifierCountFn) {
        throw new AssertionError();
    }

    @Unique
    private static VaultGearClassification  enumExpansion$addVariant(String internalName, Function<VaultGearRarity, Integer> modifierCountFn) {
        ArrayList<VaultGearClassification > variants = new ArrayList<VaultGearClassification >(Arrays.asList(MixinVaultGearClassificationEnum.$VALUES));
        VaultGearClassification  type = enumExpansion$invokeInit(internalName, variants.get(variants.size() - 1).ordinal() + 1, modifierCountFn);
        variants.add(type);
        MixinVaultGearClassificationEnum.$VALUES = variants.toArray(new VaultGearClassification[0]);
        return type;
    }
}
