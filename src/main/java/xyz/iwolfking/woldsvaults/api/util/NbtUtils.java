package xyz.iwolfking.woldsvaults.api.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public class NbtUtils {
    public static CompoundTag flatten(CompoundTag tag) {
        CompoundTag flattened = new CompoundTag();
        for(String key : tag.getAllKeys()) {
            flattened.put(key, tag.get(key));
        }
        return flattened;
    }

    public static CompoundTag fullFlatten(CompoundTag tag) {
        CompoundTag flattened = new CompoundTag();
        for(String key : tag.getAllKeys()) {
            if(tag.get(key) instanceof CompoundTag compoundTag) {
                tag = flatten(compoundTag);
            }
            else {
                flattened.put(key, tag.get(key));
            }
        }
        return flattened;
    }
}
