package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import iskallia.vault.client.gui.helper.FontHelper;
import iskallia.vault.client.gui.helper.ScreenDrawHelper;
import iskallia.vault.client.gui.helper.UIHelper;
import iskallia.vault.core.vault.overlay.VaultOverlay;
import iskallia.vault.core.vault.time.TickClock;
import iskallia.vault.core.vault.time.TickTimer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.util.ComponentUtils;
import xyz.iwolfking.woldsvaults.util.VaultUtil;

import static net.minecraft.client.gui.GuiComponent.GUI_ICONS_LOCATION;

@Mixin(value = TickTimer.class, remap = false)
public abstract class MixinTickTimer extends TickClock {

    @Shadow protected abstract float getRotationTime(int time);
    @Shadow protected abstract int getTextColor(int time);

    @Inject(method = "getRotationTime", at = @At("HEAD"), cancellable = true)
    protected void modifyRotation(int time, CallbackInfoReturnable<Float> cir) {
        if(VaultUtil.isVaultCorrupted) {
            float value = super.getRotationTime(time);
            value /= 6;
            cir.setReturnValue(value); // Speed up rotation of the Vault Clock if the vault is Corrupted
        }
    }

    @Override
    public void render(PoseStack matrixStack) {
        if(VaultUtil.isVaultCorrupted) {
            TickTimer timer = ((TickTimer) (Object)this);
            if(!timer.has(VISIBLE)) return;
            int hourglassWidth = 12;
            int hourglassHeight = 16;
            int color = getTextColor(timer.get(DISPLAY_TIME));
            MutableComponent cmp = ComponentUtils.corruptComponent(new TextComponent(UIHelper.formatTimeString(timer.get(DISPLAY_TIME))));
            FontHelper.drawStringWithBorder(matrixStack, cmp, -12, 13, color, 0xFF000000);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, VaultOverlay.VAULT_HUD);
            float rotationTime = getRotationTime(this.get(DISPLAY_TIME));
            float degrees = timer.get(DISPLAY_TIME) % rotationTime * 360.0F / rotationTime;
            matrixStack.mulPose(Vector3f.ZP.rotationDegrees(degrees));
            matrixStack.translate(-hourglassWidth / 2.0F, -hourglassHeight / 2.0F, 0);
            ScreenDrawHelper.drawTexturedQuads(buf -> ScreenDrawHelper.rect(buf, matrixStack)
                    .dim(hourglassWidth, hourglassHeight)
                    .texVanilla(1, 36, hourglassWidth, hourglassHeight)
                    .draw());
            RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);
        } else {
            super.render(matrixStack);
        }
    }
}
