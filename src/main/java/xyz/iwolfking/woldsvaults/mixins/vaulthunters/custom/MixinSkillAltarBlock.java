package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.block.SkillAltarBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.api.helper.GameruleHelper;
import xyz.iwolfking.woldsvaults.init.ModGameRules;

@Mixin(value = SkillAltarBlock.class, remap = false)
public class MixinSkillAltarBlock {
    @Inject(method = "use", at = @At("HEAD"),cancellable = true, remap = true)
    private void preventSkilLAltarUsage(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        if(!GameruleHelper.isEnabled(ModGameRules.ENABLE_SKILL_ALTARS, level)) {
            player.displayClientMessage(new TextComponent("Skill Altars are disabled in this world!").withStyle(ChatFormatting.RED), true);
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}
