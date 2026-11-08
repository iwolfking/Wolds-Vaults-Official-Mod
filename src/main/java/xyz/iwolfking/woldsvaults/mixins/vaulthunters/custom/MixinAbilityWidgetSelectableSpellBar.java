package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.screen.player.legacy.widget.AbilityWidget;
import iskallia.vault.client.gui.screen.player.legacy.widget.AbilityWidgetSelectable;
import iskallia.vault.skill.base.SpecializedSkill;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.client.spellcraft.SpellBarMenu;
import xyz.iwolfking.woldsvaults.client.spellcraft.SpellBarStrip;
import xyz.iwolfking.woldsvaults.client.spellcraft.SpellcraftGui;

@Mixin(value = AbilityWidgetSelectable.class, remap = false)
public class MixinAbilityWidgetSelectableSpellBar {
    /**
     * Tags an ability's base node, bottom right like a stack count, with its slot number on the bar the skill menu shows
     * (gold for the default bar, violet for a focus bar). The tag undoes the pan's zoom so it stays readable.
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
        float width = font.width(text) * SpellBarStrip.KEY_SCALE;
        float zoom = menu.treeZoom();
        poseStack.pushPose();
        poseStack.translate(widget.x + widget.getWidth() / 2.0F - 1.0F, widget.y + widget.getHeight() / 2.0F - 1.0F, 3.0);
        poseStack.scale(1.0F / zoom, 1.0F / zoom, 1.0F);
        SpellcraftGui.smallText(poseStack, font, text, -width - 1.0F, -8.0F * SpellBarStrip.KEY_SCALE - 1.0F,
                menu.shown().isItem() ? SpellcraftGui.ARCANE_GLOW : SpellcraftGui.GOLD, SpellBarStrip.KEY_SCALE);
        poseStack.popPose();
    }
}
