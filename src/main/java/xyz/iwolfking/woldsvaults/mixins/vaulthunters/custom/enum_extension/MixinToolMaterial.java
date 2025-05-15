package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.enum_extension;

import iskallia.vault.item.tool.ToolMaterial;
import net.minecraft.network.chat.TextColor;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.ArrayList;
import java.util.Arrays;

@Mixin(value = ToolMaterial.class, remap = false)
public class MixinToolMaterial {
    @Shadow
    @Final
    @Mutable
    @SuppressWarnings("target")
    private static ToolMaterial[] $VALUES;

    private static final ToolMaterial NULLITE = enumExpansion$addVariant("NULLITE", "nullite", 100, 600, 9.0F, 4096, 9);

    @Invoker("<init>")
    public static ToolMaterial enumExpansion$invokeInit(String internalName, int internalId, String id, int level, int capacity, float miningSpeed, int durability, int repairs) {
        throw new AssertionError();
    }

    @Unique
    private static ToolMaterial enumExpansion$addVariant(String internalName, String id, int level, int capacity, float miningSpeed, int durability, int repairs) {
        ArrayList<ToolMaterial> variants = new ArrayList<>(Arrays.asList(MixinToolMaterial.$VALUES));
        ToolMaterial type = enumExpansion$invokeInit(internalName, variants.get(variants.size() - 1).ordinal() + 1, id, level, capacity, miningSpeed, durability, repairs);
        variants.add(type);
        MixinToolMaterial.$VALUES = variants.toArray(new ToolMaterial[0]);
        return type;
    }

}
