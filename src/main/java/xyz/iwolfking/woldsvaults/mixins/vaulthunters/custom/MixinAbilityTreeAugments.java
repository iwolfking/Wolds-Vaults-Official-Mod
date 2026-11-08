package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.skill.tree.AbilityTree;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.augments.FocusAugments;

@Mixin(value = AbilityTree.class, remap = false)
public abstract class MixinAbilityTreeAugments {

    /**
     * Runs the off-hand item's recipe for the selected ability in place of the native key release. The recipe's first
     * step presses the same ability natively, so nothing VH checks on a key release is skipped.
     */
    @Inject(method = "onKeyUp", at = @At("HEAD"), cancellable = true)
    private void woldsvaults$castAugmentRecipe(SkillContext context, CallbackInfo ci) {
        ServerPlayer player = context.getSource().as(ServerPlayer.class).orElse(null);
        if (player != null && FocusAugments.tryCastRecipe(player, (AbilityTree) (Object) this)) {
            ci.cancel();
        }
    }
}
