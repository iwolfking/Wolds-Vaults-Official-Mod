package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.enum_extension;

import iskallia.vault.block.VaultCrateBlock;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.ArrayList;
import java.util.Arrays;

@Mixin(value = VaultCrateBlock.Type.class, remap = false)
public class MixinVaultCrateType {
    @Shadow
    @Final
    @Mutable
    @SuppressWarnings("target")
    private static VaultCrateBlock.Type[] $VALUES;

    private static final VaultCrateBlock.Type CORRUPTED = enumExpansion$addVariant("CORRUPTED");
    private static final VaultCrateBlock.Type ALCHEMY = enumExpansion$addVariant("ALCHEMY");


    @Invoker("<init>")
    public static VaultCrateBlock.Type enumExpansion$invokeInit(String internalName, int internalId) {
        throw new AssertionError();
    }

    @Unique
    private static VaultCrateBlock.Type enumExpansion$addVariant(String internalName) {
        ArrayList<VaultCrateBlock.Type> variants = new ArrayList<>(Arrays.asList(MixinVaultCrateType.$VALUES));
        VaultCrateBlock.Type type = enumExpansion$invokeInit(internalName, variants.get(variants.size() - 1).ordinal() + 1);
        variants.add(type);
        MixinVaultCrateType.$VALUES = variants.toArray(new VaultCrateBlock.Type[0]);
        return type;
    }
}