package xyz.iwolfking.woldsvaults.mixins.vaulthunters.deslop;

import iskallia.vault.client.gui.screen.player.board.BoardCard;
import iskallia.vault.config.PrestigePowersConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Method;

@Mixin(targets = "iskallia.vault.client.gui.screen.player.prestige.PrestigePowerCard", remap = false)
public abstract class PrestigePowerCardMixin {
   @Unique
   private static final int SUBTITLE_RGB = 9079434;
   @Unique
   private static final int TEXT_RGB = 11184810;
   @Unique
   private static final int DETAIL_RGB = 9075860;
   @Unique
   private static final int WHITE_RGB = 16777215;
   @Unique
   private static final int APEX_TITLE_RGB = 15771903;

   @Inject(method = "ofApex", at = @At("HEAD"), cancellable = true)
   private static void vault_fixApexCardText(
           @Coerce Object plan, int powerCount, int tierCount, long openedAtMs, CallbackInfoReturnable<BoardCard> cir
   ) {
      BoardCard card = new BoardCard(openedAtMs);
      card.addTitle("Prestige Powers", APEX_TITLE_RGB);
      card.addLine("The Power of Knowledge", SUBTITLE_RGB);
      card.addSeparator();
      card.addWrapped(
              "Prestige Powers are permanent passive boosts earned with vast amounts of Knowledge Points. You can only unlock higher tiers if you've unlocked the same amount or greater of lower ones first.",
              TEXT_RGB
      );
      card.addSeparator();

      int countShownLearned = woldsVaults$getCountShownLearned(plan);
      int stepPercent = woldsVaults$getStepPercentPerTierOwned(plan);

      card.addLine("Owned: " + countShownLearned + " / " + powerCount, WHITE_RGB);
      card.addLine(
              "+" + stepPercent + "% cost per Prestige unlocked in that tier",
              DETAIL_RGB
      );
      cir.setReturnValue(card);
   }

   @Unique
   private static int woldsVaults$getCountShownLearned(Object plan) {
      try {
         Method method = plan.getClass().getDeclaredMethod("countShownLearned");
         method.setAccessible(true);
         return (int) method.invoke(plan);
      } catch (Exception e) {
         return 0;
      }
   }

   @Unique
   private static int woldsVaults$getStepPercentPerTierOwned(Object plan) {
      try {
         Method ladderMethod = plan.getClass().getDeclaredMethod("ladder");
         ladderMethod.setAccessible(true);
         PrestigePowersConfig.Ladder ladder = (PrestigePowersConfig.Ladder) ladderMethod.invoke(null);
         return ladder.getStepPercentPerTierOwned();
      } catch (Exception e) {
         return 0;
      }
   }
}