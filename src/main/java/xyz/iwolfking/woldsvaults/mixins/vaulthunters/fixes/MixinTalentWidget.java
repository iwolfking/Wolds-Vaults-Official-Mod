package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.atlas.TextureAtlasRegion;
import iskallia.vault.client.gui.screen.player.legacy.widget.AbilityNodeTextures;
import iskallia.vault.client.gui.screen.player.legacy.widget.NodeState;
import iskallia.vault.client.gui.screen.player.legacy.widget.SkillWidget;
import iskallia.vault.client.gui.screen.player.legacy.widget.TalentWidget;
import iskallia.vault.config.TalentsGUIConfig;
import iskallia.vault.config.entry.SkillStyle;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.SpecializedSkill;
import iskallia.vault.skill.base.TieredSkill;
import iskallia.vault.skill.tree.TalentTree;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = TalentWidget.class, remap = false)
public abstract class MixinTalentWidget extends SkillWidget<TalentTree> {
    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderIcon(matrixStack, mouseX, mouseY, partialTicks);
        if (this.hasPips()) {
            this.renderPips(matrixStack, mouseX, mouseY, partialTicks);
        }
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            RenderSystem.enableBlend();
            TieredSkill abilityNode = this.getSkill();
            int abilityLevel = abilityNode.getUnmodifiedTier();
            int actualAbilityLevel = abilityNode.getActualTier();
            int addedLevelDiff = actualAbilityLevel - abilityLevel;
            int abilityLevelMax = abilityNode.getMaxLearnableTier();

            if (abilityNode.isUnlocked()) {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                matrixStack.pushPose();
                TextureAtlasRegion region = AbilityNodeTextures.NODE_BACKGROUND_LEVEL;
                matrixStack.translate(-region.width() / 2.0F, -this.height / 2.0F + (2 - region.height()), 0.0);
                region.blit(matrixStack, this.x, this.y - 7);
                Font font = Minecraft.getInstance().font;
                String text = String.valueOf(abilityLevel);
                if (addedLevelDiff != 0) {
                    text = text + (addedLevelDiff > 0 ? "+" : "") + addedLevelDiff;
                }

                font.draw(matrixStack, text, this.x + region.width() / 2.0F - font.width(text) / 2.0F, this.y - 6, abilityLevel >= abilityLevelMax ? -16711936 : -1);
                RenderSystem.enableDepthTest();
                matrixStack.popPose();
            }
        }
    }

    public MixinTalentWidget(TalentTree skillTree, Component pMessage, TieredSkill skill, SkillStyle style) {
        super(skillTree, pMessage, skill, style);
    }

}
