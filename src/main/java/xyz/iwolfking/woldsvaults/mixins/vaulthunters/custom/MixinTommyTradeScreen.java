package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.client.gui.screen.TommyTradeScreen;
import iskallia.vault.container.RoyaleDraftContainer;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TommyTradeScreen.class, remap = false)
public abstract class MixinTommyTradeScreen {
    @Unique
    private int wolds$selectedRewardStage;

    @Inject(method = "startTrinketAnimation", at = @At("HEAD"))
    private void captureSelectedStage(CallbackInfo callback) {
        RoyaleDraftContainer draft = ((TommyTradeScreen) (Object) this).getMenu();
        wolds$selectedRewardStage = draft.getTab(Minecraft.getInstance().player.getUUID());
    }

    @WrapOperation(method = "finishTrinketAnimation", at = @At(value = "INVOKE",
            target = "Liskallia/vault/container/RoyaleDraftContainer;getTab(Ljava/util/UUID;)I"))
    private int advanceFromSelectedStage(RoyaleDraftContainer draft, UUID player, Operation<Integer> original) {
        return wolds$selectedRewardStage;
    }
}
