package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.nbt.VMapNBT;
import iskallia.vault.world.data.PlayerReputationData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.api.util.GameruleHelper;
import xyz.iwolfking.woldsvaults.api.util.GodMasteryHelper;
import xyz.iwolfking.woldsvaults.api.util.VaultGodAffinityHelper;
import xyz.iwolfking.woldsvaults.init.ModGameRules;

import java.util.UUID;
import java.util.function.BiConsumer;

@Mixin(value = PlayerReputationData.class, remap = false)
public class MixinPlayerReputationData {
    @Inject(method = "attemptFavour", at = @At("HEAD"), cancellable = true)
    private static void returnOldFavourHandling(Player player, VaultGod god, CallbackInfoReturnable<Integer> cir) {
        if(GameruleHelper.isEnabled(ModGameRules.ENABLE_OLD_AFFINITY_HANDLING, player.getLevel())) {
            float chance = VaultGodAffinityHelper.getAffinityPercent(player, god);
            if (!(player.getRandom().nextFloat() + 0.5F <= chance)) {
                Component msg = new TextComponent("The god was not interested enough in you!").withStyle(god.getChatColor());
                player.displayClientMessage(msg, true);
                cir.setReturnValue(0);
            }
        }
    }

    /**
     * Publishes the player's effective reputation cap (50 + God's Mastery count) through
     * {@link GodMasteryHelper} before the read-side clamp below runs.
     */
    @Inject(method = "getReputation", at = @At("HEAD"))
    private static void woldsVaults$pushCapForRead(UUID player, VaultGod god, CallbackInfoReturnable<Integer> cir) {
        GodMasteryHelper.pushCap(player);
    }

    /** The read-side Math.min(value, 50) — the clamp every reputation consumer sees. */
    @ModifyConstant(method = "getReputation", constant = @Constant(intValue = 50), require = 1)
    private static int woldsVaults$readCap(int baseCap) {
        return GodMasteryHelper.currentCap();
    }

    /**
     * Publishes the effective cap before the write-side guards run, including the
     * Entry-level clamps in {@link MixinPlayerReputationDataEntry}.
     */
    @Inject(method = "addReputation", at = @At("HEAD"))
    private static void woldsVaults$pushCapForAdd(UUID playerUUID, VaultGod god, int reputation, CallbackInfo ci) {
        GodMasteryHelper.pushCap(playerUUID);
    }

    /**
     * The early-out ">= 50" guard in the static addReputation. The armor/model unlock check
     * further down lives in a lambda (a separate synthetic method), so it deliberately stays
     * at 50 — unlocks are a threshold, not the cap.
     */
    @ModifyConstant(method = "addReputation", constant = @Constant(intValue = 50), require = 1)
    private static int woldsVaults$addGuardCap(int baseCap) {
        return GodMasteryHelper.currentCap();
    }

    /**
     * VH's load-time migration wrote any stored reputation above 50 back down to 50 on every
     * server start. God's Mastery makes above-50 values legitimate; letting the clamp run
     * would silently erase every raised reputation on restart.
     */
    @Redirect(method = "load", at = @At(value = "INVOKE",
            target = "Liskallia/vault/nbt/VMapNBT;forEach(Ljava/util/function/BiConsumer;)V"))
    private static void woldsVaults$skipLegacyOverCapClamp(VMapNBT<?, ?> entries, BiConsumer<?, ?> clamp) {
        // Deliberately empty.
    }
}
