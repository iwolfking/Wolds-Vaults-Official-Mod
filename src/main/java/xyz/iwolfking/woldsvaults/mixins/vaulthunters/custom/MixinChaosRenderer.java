package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.task.ChaosTask;
import iskallia.vault.task.renderer.ChaosRenderer;
import iskallia.vault.task.renderer.context.TaskRendererContext;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.mixins.accessors.ChaosTaskAccessor;

@Mixin(value = ChaosRenderer.Root.class, remap = false)
public abstract class MixinChaosRenderer {

    @Inject(method = "onRender(Liskallia/vault/task/ChaosTask;Liskallia/vault/task/renderer/context/TaskRendererContext;)V", at = @At(value = "INVOKE", target = "Liskallia/vault/task/renderer/context/TaskRendererContext;renderText(Lnet/minecraft/network/chat/Component;FFZZIZ)V"))
    private void renderCount(ChaosTask chaosTask, TaskRendererContext context, CallbackInfo ci) {
        int completed = chaosTask.getCompletedCount();
        int required = ((ChaosTaskAccessor) chaosTask).getRequiredCompletions();

        context.push();
        float textScale = 0.85F;
        context.scale(textScale, textScale, textScale);
        context.renderText(new TextComponent(completed + "/" + required).withStyle(ChatFormatting.GRAY), 0.0F, 70.0F, true, true, 0xffffffff, false);
        context.pop();

    }
}
