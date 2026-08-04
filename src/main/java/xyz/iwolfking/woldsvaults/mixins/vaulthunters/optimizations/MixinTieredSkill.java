package xyz.iwolfking.woldsvaults.mixins.vaulthunters.optimizations;

import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.skill.base.TieredSkill;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TieredSkill.class, remap = false)
public class MixinTieredSkill {

}