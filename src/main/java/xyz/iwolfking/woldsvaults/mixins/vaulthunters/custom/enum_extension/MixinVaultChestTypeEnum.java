package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.enum_extension;

import iskallia.vault.core.vault.stat.VaultChestType;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.ArrayList;
import java.util.Arrays;

@Mixin(value = VaultChestType.class, remap = false)
public class MixinVaultChestTypeEnum {
    @Shadow
    @Final
    @Mutable @SuppressWarnings("target")
    private static VaultChestType[] $VALUES;

    //private static final VaultChestType CORRUPTED = enumExpansion$addVariant("CORRUPTED", "Corrupted");

    @Invoker("<init>")
    public static VaultChestType enumExpansion$invokeInit(String internalName, int internalId, String name) {
        throw new AssertionError();
    }

    @Unique
    private static VaultChestType enumExpansion$addVariant(String internalName, String name) {
        ArrayList<VaultChestType > variants = new ArrayList<VaultChestType >(Arrays.asList(MixinVaultChestTypeEnum.$VALUES));
        VaultChestType  type = enumExpansion$invokeInit(internalName, variants.get(variants.size() - 1).ordinal() + 1, name);
        variants.add(type);
        MixinVaultChestTypeEnum.$VALUES = variants.toArray(new VaultChestType[0]);
        return type;
    }
}
