package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.mojang.blaze3d.systems.RenderSystem;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.screen.player.legacy.widget.AbilityWidget;
import iskallia.vault.client.gui.screen.player.legacy.widget.AbilityWidgetSelectable;
import iskallia.vault.skill.base.SpecializedSkill;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.client.spellcraft.SpellBarMenu;
import xyz.iwolfking.woldsvaults.client.spellcraft.SpellBarStrip;
import xyz.iwolfking.woldsvaults.client.spellcraft.SpellcraftGui;

@Mixin(value = AbilityWidgetSelectable.class, remap = false)
public class MixinAbilityWidgetSelectableSpellBar {
    @Unique
    private static final float TAG_SCALE = 0.5F;

    /**
     * Tags an ability's base node, bottom right like a stack count, with its slot number on the bar the skill menu shows
     * (gold for the default bar, violet for a focus bar). It sits in the node's own space, so it shrinks with the node
     * when the tree is zoomed out. Text drawing leaves the depth test off; it is turned back on, as VH does after its
     * level numbers, or later lines draw over the nodes.
     */
    @Inject(method = "render", at = @At("TAIL"), remap = true)
    private void woldsvaults$slotTag(PoseStack poseStack, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        SpellBarMenu menu = SpellBarMenu.current();
        if (menu == null || !SpellBarMenu.isRenderingTree()) {
            return;
        }
        AbilityWidget widget = (AbilityWidget) (Object) this;
        SpecializedSkill group = widget.getAbilityGroup();
        if (group == null || group.getSpecializations().isEmpty() || !group.getSpecialization(0).getId().equals(widget.getAbilityName())) {
            return;
        }
        int slot = menu.shown().slotOf(group.getId());
        if (slot < 0) {
            return;
        }
        Font font = Minecraft.getInstance().font;
        String text = String.valueOf(slot + 1);
        float width = font.width(text) * TAG_SCALE;
        poseStack.pushPose();
        poseStack.translate(widget.x + widget.getWidth() / 2.0F - 2.0F, widget.y + widget.getHeight() / 2.0F - 2.0F, 3.0);
        SpellcraftGui.smallText(poseStack, font, text, -width, -8.0F * TAG_SCALE,
                menu.shown().isItem() ? SpellcraftGui.ARCANE_GLOW : SpellcraftGui.GOLD, TAG_SCALE);
        poseStack.popPose();
        RenderSystem.enableDepthTest();
    }
}
