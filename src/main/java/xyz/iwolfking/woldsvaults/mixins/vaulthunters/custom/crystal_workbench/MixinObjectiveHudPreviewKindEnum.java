package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.crystal_workbench;

import iskallia.vault.client.gui.helper.ObjectiveHudPreview;
import java.util.ArrayList;
import java.util.Arrays;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = ObjectiveHudPreview.Kind.class, remap = false)
public class MixinObjectiveHudPreviewKindEnum {

   @Shadow
   @Final
   @Mutable
   @SuppressWarnings("target")
   private static ObjectiveHudPreview.Kind[] $VALUES;

   private static final ObjectiveHudPreview.Kind ALCHEMY = enumExpansion$addVariant("ALCHEMY");
   private static final ObjectiveHudPreview.Kind ENCHANTED_ELIXIR = enumExpansion$addVariant("ENCHANTED_ELIXIR");
   private static final ObjectiveHudPreview.Kind HAUNTED_BRAZIERS = enumExpansion$addVariant("HAUNTED_BRAZIERS");
   private static final ObjectiveHudPreview.Kind UNHINGED_SCAV = enumExpansion$addVariant("UNHINGED_SCAVENGER");
   private static final ObjectiveHudPreview.Kind SENSELESS_SCAVINGO = enumExpansion$addVariant("SENSELESS_SCAVINGO");
   private static final ObjectiveHudPreview.Kind BALLISTIC_BINGO = enumExpansion$addVariant("BALLISTIC_BINGO");
   private static final ObjectiveHudPreview.Kind BRUTAL_BOSSES = enumExpansion$addVariant("BRUTAL_BOSSES");
   private static final ObjectiveHudPreview.Kind ZEALOT = enumExpansion$addVariant("ZEALOT");
   private static final ObjectiveHudPreview.Kind SURVIVAL = enumExpansion$addVariant("SURVIVAL");

   @Invoker("<init>")
   public static ObjectiveHudPreview.Kind enumExpansion$invokeInit(String internalName, int internalId) {
      throw new AssertionError();
   }

   @Unique
   private static ObjectiveHudPreview.Kind enumExpansion$addVariant(String internalName) {
      ArrayList<ObjectiveHudPreview.Kind> variants = new ArrayList<>(Arrays.asList(MixinObjectiveHudPreviewKindEnum.$VALUES));
      ObjectiveHudPreview.Kind type = enumExpansion$invokeInit(
         internalName,
         variants.get(variants.size() - 1).ordinal() + 1
      );
      variants.add(type);
      MixinObjectiveHudPreviewKindEnum.$VALUES = variants.toArray(new ObjectiveHudPreview.Kind[0]);
      return type;
   }
}