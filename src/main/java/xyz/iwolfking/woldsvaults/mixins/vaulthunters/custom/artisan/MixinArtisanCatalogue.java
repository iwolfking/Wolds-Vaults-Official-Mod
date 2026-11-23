package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.artisan;

import iskallia.vault.gear.modification.ArtisanActions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.init.ModGearModifications;
import xyz.iwolfking.woldsvaults.init.ModItems;

@Mixin(targets = "iskallia.vault.gear.modification.ArtisanActions$Catalogue")
public abstract class MixinArtisanCatalogue {

   @Shadow
   @Final
   @Mutable
   private static List<ArtisanActions.Action> BUTTONS;

   @Shadow
   @Final
   private static ArtisanActions.Action FACETED;

   @Shadow
   @Final
   @Mutable
   private static List<ArtisanActions.Action> ALL;

   @Inject(method = "<clinit>", at = @At("TAIL"))
   private static void woldsVaults$addUnusualModifierAction(CallbackInfo ci) {
      List<ArtisanActions.Action> updatedButtons = new ArrayList<>(BUTTONS);

      ModGearModifications.getWoldGearModifications().forEach(gearModification -> {
         boolean alreadyPresent = BUTTONS.stream()
                 .anyMatch(a -> a.modification() == gearModification);
         if (alreadyPresent) {
            return;
         }

         ArtisanActions.Action gearAction = new ArtisanActions.Action(
                 Objects.requireNonNull(gearModification.getDisplayStack().getItem().getRegistryName()).getPath(),
                 gearModification.getDisplayStack().getItem(),
                 gearModification,
                 ArtisanActions.Group.ADD_AND_REMOVE
         );

         updatedButtons.add(gearAction);
      });

      BUTTONS = Collections.unmodifiableList(updatedButtons);

      List<ArtisanActions.Action> updatedAll = new ArrayList<>(updatedButtons);

      if (FACETED != null) {
         updatedAll.add(FACETED);
      }

      ALL = Collections.unmodifiableList(updatedAll);
   }
}