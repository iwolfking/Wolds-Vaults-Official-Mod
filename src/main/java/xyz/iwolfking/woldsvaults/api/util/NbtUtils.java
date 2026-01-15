package xyz.iwolfking.woldsvaults.api.util;

import net.minecraft.nbt.CompoundTag;

public class NbtUtils {
    public static CompoundTag flatten(CompoundTag tag) {
        CompoundTag flattened = new CompoundTag();
        for(String key : tag.getAllKeys()) {
            flattened.put(key, tag.get(key));
        }
        return flattened;
    }
}
