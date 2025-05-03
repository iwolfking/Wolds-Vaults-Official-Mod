package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.enum_extension;

import iskallia.vault.network.message.transmog.DiscoveredEntriesMessage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.ArrayList;
import java.util.Arrays;

@Mixin(value = DiscoveredEntriesMessage.Type.class, remap = false)
public class MixinDiscoveredEntriesMessageEnum {
    @Shadow
    @Final
    @Mutable
    @SuppressWarnings("target")
    private static DiscoveredEntriesMessage.Type[] $VALUES;

    private static final DiscoveredEntriesMessage.Type THEME = enumExpansion$addVariant("THEME");
    private static final DiscoveredEntriesMessage.Type WOLD_RECIPE = enumExpansion$addVariant("WOLD_RECIPE");

    @Invoker("<init>")
    public static DiscoveredEntriesMessage.Type enumExpansion$invokeInit(String internalName, int internalId) {
        throw new AssertionError();
    }

    @Unique
    private static  DiscoveredEntriesMessage.Type enumExpansion$addVariant(String internalName) {
        ArrayList<DiscoveredEntriesMessage.Type> variants = new ArrayList<>(Arrays.asList(MixinDiscoveredEntriesMessageEnum.$VALUES));
        DiscoveredEntriesMessage.Type type = enumExpansion$invokeInit(internalName, variants.get(variants.size() - 1).ordinal() + 1);
        variants.add(type);
        MixinDiscoveredEntriesMessageEnum.$VALUES = variants.toArray(new  DiscoveredEntriesMessage.Type[0]);
        return type;
    }
}
