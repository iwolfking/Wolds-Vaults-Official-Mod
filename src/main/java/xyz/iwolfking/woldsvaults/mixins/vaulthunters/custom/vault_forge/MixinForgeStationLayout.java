package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.vault_forge;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "iskallia.vault.client.gui.screen.block.forge.ForgeStationLayout", remap = false)
public class MixinForgeStationLayout {

    @Unique
    private static final List<Object> WOLDSVAULTS$GEAR_LIST = new ArrayList<>();

    @Unique
    private static final List<Object> WOLDSVAULTS$FOCUS_LIST = new ArrayList<>();

    @Unique
    private static boolean WOLDSVAULTS$SEEDED = false;

    @Unique
    private static final MethodHandle WOLDSVAULTS$NEW_SPOT = woldsVaults$spotCtor();

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void woldsVaults$onClassInit(CallbackInfo ci) throws Throwable {
        woldsVaults$ensureListsAreModifiable();
        woldsVaults$addFocusSpot("eccentric_focus", 8, 1);

        woldsVaults$addGearSpot("scepter", 148, 2);
        woldsVaults$addGearSpot("battlestaff", 8, 2);
        woldsVaults$addGearSpot("rang", 8, 2);
        woldsVaults$addGearSpot("plushie", 34, 3);
        woldsVaults$addGearSpot("loot_sack", 122, 3);
        woldsVaults$addGearSpot("trident", 148, 3);
        woldsVaults$addGearSpot("dagger", 8, 3);
    }

    @Unique
    private static void woldsVaults$addGearSpot(String key, int x, int row) {
        woldsVaults$ensureListsAreModifiable();
        WOLDSVAULTS$GEAR_LIST.add(woldsVaults$makeSpot(key, x, row));
    }

    @Unique
    private static void woldsVaults$addFocusSpot(String key, int x, int row) {
        woldsVaults$ensureListsAreModifiable();
        WOLDSVAULTS$FOCUS_LIST.add(woldsVaults$makeSpot(key, x, row));
    }

    @Unique
    private static void woldsVaults$ensureListsAreModifiable() {
        if (WOLDSVAULTS$SEEDED) {
            return;
        }
        try {
            Class<?> layout = Class.forName("iskallia.vault.client.gui.screen.block.forge.ForgeStationLayout");
            woldsVaults$swapWithArrayList(layout, "GEAR_SPOTS", WOLDSVAULTS$GEAR_LIST);
            woldsVaults$swapWithArrayList(layout, "FOCUS_SPOTS", WOLDSVAULTS$FOCUS_LIST);
            WOLDSVAULTS$SEEDED = true;
        } catch (Throwable t) {
            throw new RuntimeException("Failed to initialize ForgeStationLayout spot lists", t);
        }
    }

    @Unique
    private static Object woldsVaults$makeSpot(String key, int x, int row) {
        try {
            return WOLDSVAULTS$NEW_SPOT.invoke(key, x, row);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Unique
    @SuppressWarnings("unchecked")
    private static void woldsVaults$swapWithArrayList(Class<?> layout, String fieldName, List<Object> targetList) throws Throwable {
        Field field = layout.getDeclaredField(fieldName);
        field.setAccessible(true);
        Object current = field.get(null);
        if (current instanceof List<?> currentList && currentList != targetList) {
            for (Object item : currentList) {
                if (!targetList.contains(item)) {
                    targetList.add(item);
                }
            }
        }

        try {
            MethodHandles.privateLookupIn(layout, MethodHandles.lookup())
                    .findStaticSetter(layout, fieldName, field.getType())
                    .invoke(targetList);
        } catch (Throwable ignored) {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            Object unsafe = theUnsafe.get(null);
            long offset = (long) unsafeClass.getMethod("staticFieldOffset", Field.class).invoke(unsafe, field);
            Object base = unsafeClass.getMethod("staticFieldBase", Field.class).invoke(unsafe, field);
            unsafeClass.getMethod("putObject", Object.class, long.class, Object.class).invoke(unsafe, base, offset, targetList);
        }
    }

    @Unique
    private static MethodHandle woldsVaults$spotCtor() {
        try {
            Class<?> spot = Class.forName("iskallia.vault.client.gui.screen.block.forge.ForgeStationLayout$Spot");
            Constructor<?> ctor = spot.getDeclaredConstructor(String.class, int.class, int.class);
            ctor.setAccessible(true);
            return MethodHandles.lookup().unreflectConstructor(ctor);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void woldsVaults$shiftForgeLayout(CallbackInfo ci) throws Throwable {
        int deltaY = 24;
        Class<?> layout = Class.forName("iskallia.vault.client.gui.screen.block.forge.ForgeStationLayout");

        woldsVaults$bumpIntConstant(layout, "STRIP_LABEL_Y", deltaY);
        woldsVaults$bumpIntConstant(layout, "TRACK_Y", deltaY);
        woldsVaults$bumpIntConstant(layout, "INVENTORY_LABEL_Y", deltaY);
    }

    @Unique
    private static void woldsVaults$bumpIntConstant(Class<?> targetClass, String fieldName, int delta) throws Throwable {
        Field field = targetClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        int updated = field.getInt(null) + delta;

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            lookup.findStaticSetter(targetClass, fieldName, int.class).invoke(updated);
        } catch (Throwable ignored) {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            Object unsafe = theUnsafe.get(null);
            long offset = (long) unsafeClass.getMethod("staticFieldOffset", Field.class).invoke(unsafe, field);
            Object base = unsafeClass.getMethod("staticFieldBase", Field.class).invoke(unsafe, field);
            unsafeClass.getMethod("putInt", Object.class, long.class, int.class).invoke(unsafe, base, offset, updated);
        }
    }
}