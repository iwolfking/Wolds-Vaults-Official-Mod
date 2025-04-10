package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.artisan.enum_extension;

import iskallia.vault.container.VaultArtisanStationContainer;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.ArrayList;
import java.util.Arrays;

@Mixin(value = VaultArtisanStationContainer.Tab.class, remap = false)
public class VaultArtisanTabEnumExtension {
    @Shadow
    @Final
    @Mutable
    @SuppressWarnings("target")
    private static VaultArtisanStationContainer.Tab[] $VALUES;

    private static final VaultArtisanStationContainer.Tab MYTHICAL = enumExpansion$addVariant("MYTHICAL");


    @Invoker("<init>")
    public static VaultArtisanStationContainer.Tab enumExpansion$invokeInit(String internalName, int internalId) {
        throw new AssertionError();
    }

    @Unique
    private static VaultArtisanStationContainer.Tab  enumExpansion$addVariant(String internalName) {
        ArrayList<VaultArtisanStationContainer.Tab > variants = new ArrayList<>(Arrays.asList(VaultArtisanTabEnumExtension.$VALUES));
        VaultArtisanStationContainer.Tab type = enumExpansion$invokeInit(internalName, variants.get(variants.size() - 1).ordinal() + 1);
        variants.add(type);
        VaultArtisanTabEnumExtension.$VALUES = variants.toArray(new VaultArtisanStationContainer.Tab[0]);
        return type;
    }
}
