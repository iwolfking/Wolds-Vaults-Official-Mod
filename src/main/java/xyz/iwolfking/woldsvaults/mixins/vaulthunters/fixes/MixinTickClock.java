package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import iskallia.vault.client.gui.helper.FontHelper;
import iskallia.vault.client.gui.helper.ScreenDrawHelper;
import iskallia.vault.client.gui.helper.UIHelper;
import iskallia.vault.core.data.DataObject;
import iskallia.vault.core.data.key.FieldKey;
import iskallia.vault.core.data.key.registry.ISupplierKey;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.vault.objective.Objectives;
import iskallia.vault.core.vault.objective.OfferingBossObjective;
import iskallia.vault.core.vault.overlay.VaultOverlay;
import iskallia.vault.core.vault.time.TickClock;
import iskallia.vault.core.vault.time.modifier.ClockModifier;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = TickClock.class, remap = false)
public abstract class MixinTickClock extends DataObject<TickClock> implements ISupplierKey<TickClock> {
    @Shadow @Final public static FieldKey<Void> VISIBLE;

    @Shadow protected abstract int getTextColor(int time);

    @Shadow @Final public static FieldKey<Integer> DISPLAY_TIME;

    @Shadow protected abstract float getRotationTime(int time);

    @Shadow @Final public static FieldKey<Integer> GLOBAL_TIME;
    @Shadow @Final public static FieldKey<ClockModifier.List> MODIFIERS;
    @Shadow @Final public static FieldKey<Void> PAUSED;

    @Shadow protected abstract void tickTime();

    private boolean isOfferingBoss = false;
    private boolean checkedObjective = false;

    /**
     * @author iwolfking
     * @reason Render always
     */
    @Overwrite
    @OnlyIn(Dist.CLIENT)
    public void render(PoseStack matrixStack) {
        if(this.has(VISIBLE) || isOfferingBoss) {
            int hourglassWidth = 12;
            int hourglassHeight = 16;
            int color = this.getTextColor(this.get(DISPLAY_TIME));
            String text = UIHelper.formatTimeString(Math.abs(this.get(DISPLAY_TIME)));
            FontHelper.drawStringWithBorder(matrixStack, text, -12.0F, 13.0F, color, -16777216);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, VaultOverlay.VAULT_HUD);
            float rotationTime = this.getRotationTime(this.get(DISPLAY_TIME));
            float degrees = (float) this.get(DISPLAY_TIME) % rotationTime * 360.0F / rotationTime;
            matrixStack.mulPose(Vector3f.ZP.rotationDegrees(degrees));
            matrixStack.translate(-hourglassWidth / 2.0F, -hourglassHeight / 2.0F, 0.0);
            ScreenDrawHelper.drawTexturedQuads(buf -> {
                ScreenDrawHelper.rect(buf, matrixStack).dim(hourglassWidth, hourglassHeight).texVanilla(1.0F, 36.0F, hourglassWidth, hourglassHeight).draw();
            });
            RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public final void tickServer(ServerLevel world) {
        this.set(GLOBAL_TIME, this.get(GLOBAL_TIME) + 1);
        this.get(MODIFIERS).forEach(modifier -> modifier.tick(world, (TickClock) (Object)this));
        if (!this.has(PAUSED) || isOfferingBoss) {
            this.tickTime();
        }
        else {
            if(!checkedObjective) {
                Vault vault = ServerVaults.get(world).orElse(null);

                if(vault == null) {
                    return;
                }

                Objective.ObjList objList = vault.get(Vault.OBJECTIVES).get(Objectives.LIST);

                for(Objective obj : objList) {
                    if(obj instanceof OfferingBossObjective) {
                        isOfferingBoss = true;
                    }
                }
                checkedObjective = true;
            }
        }
    }
}
