package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.enum_extension;

import com.google.gson.annotations.SerializedName;
import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.data.VaultGearData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.BiPredicate;

@Mixin(value = VaultGearTierConfig.ModifierAffixTagGroup.class, remap = false)
public abstract class MixinModifierAffixTagGroupEnum {
    @Shadow
    @Final
    @Mutable
    @SuppressWarnings("target")
    private static VaultGearTierConfig.ModifierAffixTagGroup[] $VALUES;

    @SerializedName("UNUSUAL_PREFIX")
    private static final VaultGearTierConfig.ModifierAffixTagGroup UNUSUAL_PREFIX = enumExpansion$addVariant("UNUSUAL_PREFIX", VaultGearModifier.AffixType.PREFIX, withTarget(VaultGearModifier.AffixType.PREFIX));
    private static final VaultGearTierConfig.ModifierAffixTagGroup UNUSUAL_SUFFIX = enumExpansion$addVariant("UNUSUAL_SUFFIX", VaultGearModifier.AffixType.SUFFIX, withTarget(VaultGearModifier.AffixType.SUFFIX));
    private static final VaultGearTierConfig.ModifierAffixTagGroup TRAIT = enumExpansion$addVariant("TRAIT", VaultGearModifier.AffixType.IMPLICIT, withTarget(VaultGearModifier.AffixType.IMPLICIT));


    @Invoker("<init>")
    public static VaultGearTierConfig.ModifierAffixTagGroup enumExpansion$invokeInit(String internalName, int internalId, @Nullable VaultGearModifier.AffixType targetAffixType, BiPredicate apply) {
        throw new AssertionError();
    }

    @Unique
    private static VaultGearTierConfig.ModifierAffixTagGroup enumExpansion$addVariant(String internalName, @Nullable VaultGearModifier.AffixType targetAffixType, BiPredicate apply) {
        ArrayList<VaultGearTierConfig.ModifierAffixTagGroup> variants = new ArrayList<>(Arrays.asList(MixinModifierAffixTagGroupEnum.$VALUES));
        VaultGearTierConfig.ModifierAffixTagGroup  type = enumExpansion$invokeInit(internalName, variants.get(variants.size() - 1).ordinal() + 1, targetAffixType, apply);
        variants.add(type);
        MixinModifierAffixTagGroupEnum.$VALUES = variants.toArray(new VaultGearTierConfig.ModifierAffixTagGroup[0]);
        return type;
    }

    @Shadow
    protected static BiPredicate<VaultGearData, VaultGearModifier<?>> withTarget(VaultGearModifier.AffixType type) {
        return null;
    }
}
