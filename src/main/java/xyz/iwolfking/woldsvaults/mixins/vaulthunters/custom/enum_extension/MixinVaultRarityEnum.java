package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.enum_extension;

import iskallia.vault.gear.VaultGearRarity;
import net.minecraft.network.chat.TextColor;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.ArrayList;
import java.util.Arrays;

@Mixin(value = VaultGearRarity.class, remap = false)
public class MixinVaultRarityEnum {
    @Shadow
    @Final
    @Mutable @SuppressWarnings("target")
    private static VaultGearRarity[] $VALUES;



    private static final VaultGearRarity MYTHIC = enumExpansion$addVariant("MYTHIC", TextColor.fromRgb(15597727), 7, 6, 6, 6, 6, 6, 6, 6, 0);


    @Invoker("<init>")
    public static VaultGearRarity enumExpansion$invokeInit(String internalName, int internalId, TextColor color, int armorModifierCount, int weaponModifierCount, int idolModifierCount, int shieldModifierCount, int jewelModifierCount, int magnetModifierCount, int wandModifierCount, int focusModifierCount, int voidStoneCount) {
        throw new AssertionError();
    }

    @Unique
    private static VaultGearRarity enumExpansion$addVariant(String internalName, TextColor color, int armorModifierCount, int weaponModifierCount, int idolModifierCount, int shieldModifierCount, int jewelModifierCount, int magnetModifierCount, int wandModifierCount, int focusModifierCount, int voidStoneCount) {
        ArrayList<VaultGearRarity > variants = new ArrayList<VaultGearRarity >(Arrays.asList(MixinVaultRarityEnum.$VALUES));
        VaultGearRarity  type = enumExpansion$invokeInit(internalName, variants.get(variants.size() - 1).ordinal() + 1, color, armorModifierCount, weaponModifierCount, idolModifierCount, shieldModifierCount, jewelModifierCount, magnetModifierCount, wandModifierCount, focusModifierCount, voidStoneCount);
        variants.add(type);
        MixinVaultRarityEnum.$VALUES = variants.toArray(new VaultGearRarity[0]);
        return type;
    }
}