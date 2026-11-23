package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.artisan;

import iskallia.vault.gear.modification.GearModification;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.init.ModGearModifications;

@Mixin(targets = "iskallia.vault.client.gui.screen.block.artisan.ArtisanFocusButtons", remap = false)
public abstract class MixinArtisanFocusButtons {

   @Unique
   private static final Map<Class<?>, MethodHandle> woldsVaults$STATE_OF_HANDLES = new ConcurrentHashMap<>();

   @Unique
   private static final MethodHandle woldsVaults$DRAW_BUTTON_HANDLE = woldsVaults$lookupDrawButton();

   @Unique
   private record ExtraFocus(GearModification modification, float x, float y) {
   }

   @Unique
   private static final List<ExtraFocus> woldsVaults$EXTRA_FOCUSES = List.of(
      new ExtraFocus(ModGearModifications.UNUSUAL_GEAR_MODIFICATION, 122.0F, 113.0F),
      new ExtraFocus(ModGearModifications.FREEZE_ALL_GEAR_MODIFICATION, 34.0F, 113.0F),
      new ExtraFocus(ModGearModifications.MAP_TIER_MODIFICATION, 65.0F, 79.0F),
      new ExtraFocus(ModGearModifications.BLAZING_GEAR_MODIFICATION, 91.0F, 79.0F),
      new ExtraFocus(ModGearModifications.WEAPON_TYPE_MODIFICATION, 65.0F, 109.0F)
   );

   @Inject(method = "draw", at = @At("TAIL"))
   private void woldsVaults$drawExtraFocuses(@Coerce Object host, long now, CallbackInfo ci) {
      if (host == null || woldsVaults$DRAW_BUTTON_HANDLE == null) {
         return;
      }

      MethodHandle stateOfHandle = woldsVaults$STATE_OF_HANDLES.computeIfAbsent(
         host.getClass(),
         MixinArtisanFocusButtons::woldsVaults$lookupStateOf
      );
      if (stateOfHandle == null) {
         return;
      }

      for (ExtraFocus extra : woldsVaults$EXTRA_FOCUSES) {
         GearModification mod = extra.modification();
         if (mod == null) {
            continue;
         }

         try {
            Object state = stateOfHandle.invoke(host, mod);
            if (state != null) {
               woldsVaults$DRAW_BUTTON_HANDLE.invoke(this, host, state, extra.x(), extra.y(), now);
            }
         } catch (Throwable ignored) {
         }
      }
   }

   @Unique
   private static MethodHandle woldsVaults$lookupStateOf(Class<?> hostClass) {
      try {
         Method method = hostClass.getMethod("stateOf", GearModification.class);
         method.setAccessible(true);
         return MethodHandles.lookup().unreflect(method);
      } catch (ReflectiveOperationException e) {
         for (Method m : hostClass.getMethods()) {
            if (m.getName().equals("stateOf") && m.getParameterCount() == 1) {
               m.setAccessible(true);
               try {
                  return MethodHandles.lookup().unreflect(m);
               } catch (IllegalAccessException ignored) {
               }
            }
         }
         return null;
      }
   }

   @Unique
   private static MethodHandle woldsVaults$lookupDrawButton() {
      try {
         Class<?> hostClass = Class.forName("iskallia.vault.client.gui.screen.block.artisan.ArtisanFocusButtons$Host");
         Class<?> stateClass = Class.forName("iskallia.vault.client.gui.screen.block.artisan.ArtisanActionState");
         Method method = null;
         for (Class<?> c = Class.forName("iskallia.vault.client.gui.screen.block.artisan.ArtisanFocusButtons"); c != null; c = c.getSuperclass()) {
            try {
               method = c.getDeclaredMethod("drawButton", hostClass, stateClass, float.class, float.class, long.class);
               break;
            } catch (NoSuchMethodException ignored) {
            }
         }
         if (method == null) {
            return null;
         }
         method.setAccessible(true);
         return MethodHandles.lookup().unreflect(method);
      } catch (ReflectiveOperationException e) {
         return null;
      }
   }
}