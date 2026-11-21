package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.network.message.PrestigePowerLevelMessage;
import iskallia.vault.skill.base.LearnableSkill;
import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.skill.base.TieredSkill;
import iskallia.vault.skill.prestige.core.PrestigePower;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import xyz.iwolfking.woldsvaults.prestige.CraftingRecipePower;

//TODO: Fix CraftingRecipePower unlock?
@Mixin(value = PrestigePowerLevelMessage.class, remap = false)
public class MixinPrestigePowerLevelMessage {
//
//    @Inject(
//        method = "lambda$handle$0",
//        at = @At(value = "INVOKE", target = "Liskallia/vault/world/data/PlayerPrestigePowersData;learnPowers(Lnet/minecraft/server/level/ServerPlayer;Ljava/util/List;)V", shift = At.Shift.AFTER),
//        locals = LocalCapture.CAPTURE_FAILSOFT
//    )
//    private static void triggerCustomLearnLogic(NetworkEvent.Context context, PrestigePowerLevelMessage message, CallbackInfo ci) {
//        if(message instanceof CraftingRecipePower) {
//            power.learn(context);
//        }
//    }
}