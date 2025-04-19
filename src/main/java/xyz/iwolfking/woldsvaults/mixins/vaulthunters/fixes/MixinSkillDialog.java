package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.client.gui.screen.player.legacy.tab.split.dialog.SkillDialog;
import iskallia.vault.skill.base.TieredSkill;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = SkillDialog.class, remap = false)
public class MixinSkillDialog {
    @Redirect(method = "renderHeading", at = @At(value = "INVOKE", target = "Liskallia/vault/skill/base/TieredSkill;getUnmodifiedTier()I", ordinal = 0))
    private int useActualTierInstead(TieredSkill instance) {
        return instance.getActualTier();
    }
}
