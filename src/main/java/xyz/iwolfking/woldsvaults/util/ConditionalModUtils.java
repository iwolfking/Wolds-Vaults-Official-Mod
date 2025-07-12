package xyz.iwolfking.woldsvaults.util;

import net.minecraftforge.fml.loading.LoadingModList;

public class ConditionalModUtils {
    public static boolean isModPresent(String modId) {
        return LoadingModList.get().getModFileById(modId) != null;
    }
}
