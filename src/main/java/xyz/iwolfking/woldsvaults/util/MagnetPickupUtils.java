package xyz.iwolfking.woldsvaults.util;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

public class MagnetPickupUtils {
    private static MethodHandle getPreviousStack;
    private static boolean isInitialized = false;
    private static MethodHandle getMethHandle() {
        try {
            Field field = ItemEntity.class.getDeclaredField("previousStack");
            field.setAccessible(true);
            return MethodHandles.lookup().unreflectGetter(field);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {}
        return null;
    }

    /**
     * Gets stack before it was deleted (for example by pickup upgrade from backpacks)
     */
    public static ItemStack getPreviousStack(ItemEntity itemEntity) {
        if (!isInitialized) {
            getPreviousStack = getMethHandle();
            isInitialized = true;
        }
        if (getPreviousStack == null) {
            return ItemStack.EMPTY;
        }
        try {
            return (ItemStack) getPreviousStack.invoke(itemEntity);
        } catch (Throwable ignored) {}
        return ItemStack.EMPTY;
    }
}
