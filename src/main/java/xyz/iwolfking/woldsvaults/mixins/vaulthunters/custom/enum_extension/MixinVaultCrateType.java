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
    private static final VaultCrateBlock.Type BRUTAL_BOSSES = enumExpansion$addVariant("BRUTAL_BOSSES");
    private static final VaultCrateBlock.Type ENCHANTED_ELIXIR = enumExpansion$addVariant("ENCHANTED_ELIXIR");
    private static final VaultCrateBlock.Type UNHINGED_SCAVENGER = enumExpansion$addVariant("UNHINGED_SCAVENGER");
    private static final VaultCrateBlock.Type HAUNTED_BRAZIERS = enumExpansion$addVariant("HAUNTED_BRAZIERS");
    private static final VaultCrateBlock.Type BALLISTIC_BINGO = enumExpansion$addVariant("BALLISTIC_BINGO");
    private static final VaultCrateBlock.Type ZEALOT = enumExpansion$addVariant("ZEALOT");
    private static final VaultCrateBlock.Type SURVIVAL = enumExpansion$addVariant("SURVIVAL");
    private static final VaultCrateBlock.Type TIME_TRIAL_REWARD = enumExpansion$addVariant("TIME_TRIAL_REWARD");


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