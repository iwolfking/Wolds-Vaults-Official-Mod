package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.crystal_workbench;

import iskallia.vault.block.VaultCrateBlock;
import iskallia.vault.client.gui.helper.ObjectiveHudPreview;
import iskallia.vault.item.crystal.objective.CrystalObjective;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.objectives.*;

//Yeah why would anyone else want to add new objective seals or anything, that'd be crazy... thanks iskall!
@Mixin(targets = "iskallia.vault.client.gui.screen.block.crystal.ObjectiveSealLook", remap = false)
public class MixinObjectiveSealLookEnum {

   @Unique
   private static final Class<?> ENUM_EXPANSION$TARGET_CLASS = enumExpansion$targetClass();

   @Unique
   private static final Field ENUM_EXPANSION$VALUES_FIELD = enumExpansion$findValuesField(ENUM_EXPANSION$TARGET_CLASS);

   @Unique
   private static final MethodHandle ENUM_EXPANSION$CTOR = enumExpansion$findConstructor(ENUM_EXPANSION$TARGET_CLASS);

   @Unique
   private static final Object ALCHEMY = enumExpansion$addVariant(
           "ALCHEMY",
           ModItems.CRYSTAL_SEAL_ALCHEMY,
           AlchemyCrystalObjective::new,
           "Alchemy",
           VaultCrateBlock.Type.valueOf("ALCHEMY"),
           ObjectiveHudPreview.Kind.valueOf("ALCHEMY"),
           0,
           "Collect alchemical ingredients and reagents and brew them together!",
           "Going past the standard completion amount grants Crate Quantity!"
   );

   @Unique
   private static final Object ENCHANTED_ELIXIR = enumExpansion$addVariant(
           "ENCHANTED_ELIXIR",
           ModItems.CRYSTAL_SEAL_ENCHANTER,
           EnchantedElixirCrystalObjective::new,
           "Enchanted Elixir",
           VaultCrateBlock.Type.valueOf("ENCHANTED_ELIXIR"),
           ObjectiveHudPreview.Kind.ELIXIR,
           0,
           "Like a standard Elixir vault but random Enchanted events occur as you reach certain points in your completion!",
           null
   );

   @Unique
   private static final Object HAUNTED_BRAZIERS = enumExpansion$addVariant(
           "HAUNTED_BRAZIERS",
           ModItems.CRYSTAL_SEAL_SPIRITS,
           HauntedBraziersCrystalObjective::new,
           "Haunted Braziers",
           VaultCrateBlock.Type.valueOf("HAUNTED_BRAZIERS"),
           ObjectiveHudPreview.Kind.BRAZIERS,
           0,
           "Like a standard Braziers vault but Spooky Spirits come to stop you from lighting your way!",
           null
   );

   @Unique
   private static final Object UNHINGED_SCAVENGER = enumExpansion$addVariant(
           "UNHINGED_SCAVENGER",
           ModItems.CRYSTAL_SEAL_UNHINGED,
           UnhingedScavengerCrystalObjective::new,
           "Unhinged Scavenger Hunt",
           VaultCrateBlock.Type.valueOf("UNHINGED_SCAVENGER"),
           ObjectiveHudPreview.Kind.SCAVENGER,
           0,
           "Like Scavenger Hunt but the normal items are all jumbled up and mob drops are MUCH more specific!",
           null
   );

   @Unique
   private static final Object SENSELESS_SCAVINGO = enumExpansion$addVariant(
           "SENSELESS_SCAVINGO",
           ModItems.CRYSTAL_SEAL_UNHINGED_SCAVINGO,
           ScalingUnhingedScavengerBingoCrystalObjective::new,
           "Senseless Scavenger Bingo",
           VaultCrateBlock.Type.valueOf("UNHINGED_SCAVENGER"),
           ObjectiveHudPreview.Kind.COLLECTOR,
           5,
           "Like Collector but mixed with an Unhinged Scavenger Hunt!",
           null
   );

   @Unique
   private static final Object BALLISTIC_BINGO = enumExpansion$addVariant(
           "BALLISTIC_BINGO",
           ModItems.CRYSTAL_SEAL_DOOMSAYER,
           ScalingBallisticBingoCrystalObjective::new,
           "Ballistic Bingo",
           VaultCrateBlock.Type.valueOf("BALLISTIC_BINGO"),
           ObjectiveHudPreview.Kind.BINGO,
           5,
           "Like Bingo but each square grants a postive and negative modifier, and Bingos are much more wild!",
           null
   );

   @Unique
   private static final Object BRUTAL_BOSSES = enumExpansion$addVariant(
           "BRUTAL_BOSSES",
           ModItems.CRYSTAL_SEAL_TITAN,
           BrutalBossesCrystalObjective::new,
           "Brutal Bosses",
           VaultCrateBlock.Type.valueOf("BRUTAL_BOSSES"),
           ObjectiveHudPreview.Kind.valueOf("BRUTAL_BOSSES"),
           0,
           "Hunt down Obelisks to tackle enhanced Elite mobs for completion!",
           null
   );

   @Unique
   private static final Object ZEALOT = enumExpansion$addVariant(
           "ZEALOT",
           ModItems.CRYSTAL_SEAL_ZEALOT,
           ZealotCrystalObjective::new,
           "Zealot",
           VaultCrateBlock.Type.valueOf("ZEALOT"),
           ObjectiveHudPreview.Kind.valueOf("ZEALOT"),
           0,
           "Complete God Altars to impress the Vault Gods to complete!",
           null
   );

   @Unique
   private static final Object SURVIVAL = enumExpansion$addVariant(
           "SURVIVAL",
           ModItems.CRYSTAL_SEAL_SURVIVOR,
           SurvivalCrystalObjective::new,
           "Survival",
           VaultCrateBlock.Type.valueOf("SURVIVAL"),
           ObjectiveHudPreview.Kind.valueOf("SURVIVAL"),
           0,
           "Survive for as long as you can with increasing difficulty!",
           null
   );

   @Unique
   private static Object enumExpansion$addVariant(
           String internalName,
           Item seal,
           Supplier<CrystalObjective> objective,
           String objectiveName,
           VaultCrateBlock.Type crate,
           ObjectiveHudPreview.Kind preview,
           int boardSize,
           String description
   ) {
      return enumExpansion$addVariant(internalName, seal, objective, objectiveName, crate, preview, boardSize, description, null);
   }

   @Unique
   private static Object enumExpansion$addVariant(
           String internalName,
           Item seal,
           Supplier<CrystalObjective> objective,
           String objectiveName,
           VaultCrateBlock.Type crate,
           ObjectiveHudPreview.Kind preview,
           int boardSize,
           String description,
           @Nullable String rewardsNote
   ) {
      try {
         Object[] currentValues = enumExpansion$readCurrentValues();
         List<Object> variants = new ArrayList<>(Arrays.asList(currentValues));
         int nextOrdinal = variants.isEmpty() ? 0 : ((Enum<?>) variants.get(variants.size() - 1)).ordinal() + 1;

         Object variant = ENUM_EXPANSION$CTOR.invoke(
                 internalName,
                 nextOrdinal,
                 seal,
                 objective,
                 objectiveName,
                 crate,
                 preview,
                 boardSize,
                 description,
                 rewardsNote
         );

         variants.add(variant);
         Object[] resized = (Object[]) Array.newInstance(ENUM_EXPANSION$TARGET_CLASS, variants.size());
         for (int i = 0; i < variants.size(); i++) {
            resized[i] = variants.get(i);
         }

         enumExpansion$writeValues(resized);
         return variant;
      } catch (Throwable t) {
         throw new RuntimeException("Failed to add ObjectiveSealLook enum variant: " + internalName, t);
      }
   }

   @Unique
   private static Object[] enumExpansion$readCurrentValues() throws Exception {
      if (ENUM_EXPANSION$VALUES_FIELD != null) {
         Object raw = ENUM_EXPANSION$VALUES_FIELD.get(null);
         if (raw instanceof Object[] arr) {
            return arr;
         }
      }
      Method valuesMethod = ENUM_EXPANSION$TARGET_CLASS.getDeclaredMethod("values");
      valuesMethod.setAccessible(true);
      return (Object[]) valuesMethod.invoke(null);
   }

   @Unique
   private static void enumExpansion$writeValues(Object[] newValues) {
      if (ENUM_EXPANSION$VALUES_FIELD == null) {
         throw new IllegalStateException("Could not find $VALUES array field on ObjectiveSealLook");
      }
      try {
         MethodHandles.Lookup lookup = MethodHandles.lookup();
         MethodHandle setter;
         try {
            setter = lookup.findStaticSetter(
                    ENUM_EXPANSION$TARGET_CLASS,
                    ENUM_EXPANSION$VALUES_FIELD.getName(),
                    ENUM_EXPANSION$VALUES_FIELD.getType()
            );
         } catch (IllegalAccessException e) {
            ENUM_EXPANSION$VALUES_FIELD.setAccessible(true);
            setter = lookup.unreflectSetter(ENUM_EXPANSION$VALUES_FIELD);
         }
         setter.invoke(newValues);
      } catch (Throwable t) {
         try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            Object unsafe = theUnsafe.get(null);
            long offset = (long) unsafeClass.getMethod("staticFieldOffset", Field.class).invoke(unsafe, ENUM_EXPANSION$VALUES_FIELD);
            Object base = unsafeClass.getMethod("staticFieldBase", Field.class).invoke(unsafe, ENUM_EXPANSION$VALUES_FIELD);
            unsafeClass.getMethod("putObject", Object.class, long.class, Object.class).invoke(unsafe, base, offset, newValues);
         } catch (Throwable unsafeError) {
            IllegalStateException ex = new IllegalStateException(
                    "Unable to update $VALUES on ObjectiveSealLook (field=" + ENUM_EXPANSION$VALUES_FIELD.getName() + ")",
                    t
            );
            ex.addSuppressed(unsafeError);
            throw ex;
         }
      }
   }

   @Unique
   private static Class<?> enumExpansion$targetClass() {
      try {
         return Class.forName("iskallia.vault.client.gui.screen.block.crystal.ObjectiveSealLook");
      } catch (ClassNotFoundException e) {
         throw new RuntimeException("Could not find ObjectiveSealLook", e);
      }
   }

   @Unique
   private static Field enumExpansion$findValuesField(Class<?> enumClass) {
      for (Field field : enumClass.getDeclaredFields()) {
         if (Modifier.isStatic(field.getModifiers())
                 && field.getType().isArray()
                 && field.getType().getComponentType() == enumClass) {
            return field;
         }
      }
      return null;
   }

   @Unique
   private static MethodHandle enumExpansion$findConstructor(Class<?> enumClass) {
      try {
         Constructor<?> matched = null;
         for (Constructor<?> ctor : enumClass.getDeclaredConstructors()) {
            Class<?>[] params = ctor.getParameterTypes();
            if (params.length == 10
                    && params[0] == String.class
                    && params[1] == int.class
                    && Item.class.isAssignableFrom(params[2])
                    && Supplier.class.isAssignableFrom(params[3])
                    && VaultCrateBlock.Type.class.isAssignableFrom(params[5])
                    && ObjectiveHudPreview.Kind.class.isAssignableFrom(params[6])
                    && params[7] == int.class) {
               matched = ctor;
               break;
            }
         }
         if (matched == null) {
            throw new NoSuchMethodException("Matching ObjectiveSealLook constructor not found");
         }
         matched.setAccessible(true);
         return MethodHandles.lookup().unreflectConstructor(matched);
      } catch (ReflectiveOperationException e) {
         throw new RuntimeException("Failed to find ObjectiveSealLook enum constructor", e);
      }
   }
}