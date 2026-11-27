package xyz.iwolfking.woldsvaults.client.screens.armory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.helper.FontHelper;
import iskallia.vault.config.entry.SkillStyle;
import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.iwolfking.woldsvaults.client.screens.gods.GodNodeWidget;
import xyz.iwolfking.woldsvaults.client.screens.gods.GodTreeTheme;
import xyz.iwolfking.woldsvaults.gods.node.GodNode;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/** A star of a saved constellation, with a +1 / -1 tag at its top right when Load would learn or remove it. */
@OnlyIn(Dist.CLIENT)
public class ArmoryGodNodeWidget extends GodNodeWidget {
    public static final int TAG_ADD = 0xFF6EF08C;
    public static final int TAG_REMOVE = 0xFFFF5A5A;
    private static final int TAG_BORDER = 0xFF1E1E1E;
    private static final float TAG_SCALE = 0.75F;

    private final VaultGod god;
    private final int tag;

    public ArmoryGodNodeWidget(GodNode node, SkillStyle style, VaultGod god, boolean saved, int tag) {
        super(node, style, god, saved, false);
        this.god = god;
        this.tag = tag;
    }

    @Override
    public void renderWidget(PoseStack renderStack, Rectangle containerBounds, int screenMouseX, int screenMouseY,
                             int containerMouseX, int containerMouseY, float pTicks, List<Runnable> postContainerRender) {
        super.renderWidget(renderStack, containerBounds, screenMouseX, screenMouseY, containerMouseX, containerMouseY,
                pTicks, postContainerRender);
        if (this.tag == 0) {
            return;
        }
        renderStack.pushPose();
        renderStack.translate(this.x + this.width * 0.6F, this.y - 2.0F, 0.0D);
        renderStack.scale(TAG_SCALE, TAG_SCALE, 1.0F);
        FontHelper.drawStringWithBorder(renderStack, this.tagText(), 0.0F, 0.0F, this.tag > 0 ? TAG_ADD : TAG_REMOVE, TAG_BORDER);
        renderStack.popPose();
        RenderSystem.enableDepthTest();
    }

    private String tagText() {
        return this.tag > 0 ? "+" + this.tag : "-" + (-this.tag);
    }

    @Override
    protected void renderTooltip(PoseStack renderStack, int screenMouseX, int screenMouseY, List<Runnable> postContainerRender) {
        List<Component> tooltip = new ArrayList<>();
        GodNode node = this.getNode();
        tooltip.add(new TextComponent(node.name())
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(GodTreeTheme.accent(this.god) & 0xFFFFFF))));
        if (!node.enabled()) {
            tooltip.add(new TextComponent("Coming soon").withStyle(ChatFormatting.GRAY));
        } else if (this.isUnlocked()) {
            tooltip.add(new TextComponent("Saved").withStyle(ChatFormatting.GREEN));
        } else {
            tooltip.add(new TextComponent("Not saved").withStyle(ChatFormatting.GRAY));
        }
        if (this.tag != 0) {
            String effect = this.tag > 0 ? ": learned on Load" : ": removed on Load";
            int color = (this.tag > 0 ? TAG_ADD : TAG_REMOVE) & 0xFFFFFF;
            tooltip.add(new TextComponent(this.tagText() + effect).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color))));
        }
        postContainerRender.add(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.screen != null) {
                minecraft.screen.renderComponentTooltip(renderStack, tooltip, screenMouseX, screenMouseY);
            }
        });
    }
}
