package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft.client;

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
import xyz.iwolfking.woldsvaults.client.spellcraft.bar.SkillMenuSpellBar;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftDraw;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftPalette;
import xyz.iwolfking.woldsvaults.client.spellcraft.state.ClientSpellBar;
import xyz.iwolfking.woldsvaults.spellcraft.bar.BarMode;

@Mixin(value = AbilityWidgetSelectable.class, remap = false)
public class MixinAbilityWidgetSelectableSpellBar {
    @Unique
    private static final float TAG_SCALE = 0.5F;

    /**
     * Tags an ability's base node in Vault Hunters' tree with its slot number on the bar the skill menu shows, in slot
     * mode only. Text
     * drawing leaves the depth test off, so it is turned back on or later lines draw over the nodes.
     */
    @Inject(method = "render", at = @At("TAIL"), remap = true)
    private void woldsvaults$slotTag(PoseStack poseStack, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        SkillMenuSpellBar menu = SkillMenuSpellBar.current();
        if (menu == null || !SkillMenuSpellBar.isRenderingTree() || ClientSpellBar.mode() == BarMode.CLASSIC) {
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
        SpellcraftDraw.smallText(poseStack, font, text, -width, -8.0F * TAG_SCALE,
                menu.shown().isEffective() ? SpellcraftPalette.ARCANE_GLOW : SpellcraftPalette.GOLD, TAG_SCALE);
        poseStack.popPose();
        RenderSystem.enableDepthTest();
    }
}
